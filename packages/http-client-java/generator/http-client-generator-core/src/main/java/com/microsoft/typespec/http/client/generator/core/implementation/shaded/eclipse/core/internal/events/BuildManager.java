/*******************************************************************************
 *  Copyright (c) 2000, 2021 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Isaac Pacht (isaacp3@gmail.com) - fix for bug 206540
 *     Anton Leherbauer (Wind River) - [305858] Allow Builder to return null rule
 *     James Blackburn (Broadcom) - [306822] Provide Context for Builder getRule()
 *     Broadcom Corporation - ongoing development
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 473427
 *     Torbjörn Svensson (STMicroelectronics) - bug #552606
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.events;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources.ICoreConstants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources.Project;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources.ProjectDescription;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources.ResourceStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources.Workspace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.utils.Policy;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.watson.ElementTree;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IBuildConfiguration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.ICommand;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResourceDelta;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IncrementalProjectBuilder;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.jobs.ILock;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.jobs.ISchedulingRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class BuildManager implements ICoreConstants, ILifecycleListener {

    /**
     * Cache used to optimize the common case of an autobuild against
     * a workspace where only a single project has changed (and hence
     * only a single delta is interesting).
     */
    static class DeltaCache<E> {
        private final Map<IPath, E> deltas = new HashMap<>();
        private ElementTree newTree;
        private ElementTree oldTree;

        public void flush() {
            deltas.clear();
            this.oldTree = null;
            this.newTree = null;
        }

        /**
         * Returns the cached resource delta for the given project and trees, or
         * calls calculator to compute a new delta if there is no matching one in the cache.
         */
        public E computeIfAbsent(IPath project, ElementTree anOldTree, ElementTree aNewTree, Supplier<E> calculator) {
            if (!(areEqual(this.oldTree, anOldTree) && areEqual(this.newTree, aNewTree))) {
                this.oldTree = anOldTree;
                this.newTree = aNewTree;
                deltas.clear();
            }
            return deltas.computeIfAbsent(project, p -> calculator.get());
        }

        private static boolean areEqual(ElementTree cached, ElementTree requested) {
            return !ElementTree.hasChanges(requested, cached, ResourceComparator.getBuildComparator(), true);
        }
    }

    /**
     * These builders are added to build tables in place of builders that couldn't be instantiated
     */
    static class MissingBuilder extends IncrementalProjectBuilder {

        MissingBuilder() {
        }

        @Override
        public ISchedulingRule getRule(int kind, Map<String, String> args) {
            return null;
        }

    }

    // the job for performing background autobuild
    private final Set<IProject> builtProjects = Collections.synchronizedSet(new HashSet<>());

    // the following four fields only apply for the lifetime of a single builder invocation.
    protected final Set<InternalBuilder> currentBuilders;
    private ElementTree currentLastBuiltTree;
    private ElementTree currentTree;

    /**
     * Caches the IResourceDelta for a pair of trees
     */
    final private DeltaCache<IResourceDelta> deltaCache = new DeltaCache<>();

    private final ILock lock;

    /**
     * Set of projects for which builders requested rebuild. Has no effect if any
     * builder requested rebuild of everything via {@link #rebuildRequested}
     */
    private final Set<IProject> projectsToRebuild;

    /**
     * Map of projects for which builders requested rebuild for the current build
     * cycle. If the value is "true" - stop building project with other builders
     * immediately, "false" to continue build and start project build again after
     * all builders were done. If no value is set, no rebuild is requested.
     */
    private final Map<IProject, Boolean> restartBuildImmediately;

    // used for debug/trace timing
    private final Workspace workspace;

    public BuildManager(Workspace workspace, ILock workspaceLock) {
        this.workspace = workspace;
        this.currentBuilders = Collections.synchronizedSet(new HashSet<>());
        projectsToRebuild = ConcurrentHashMap.newKeySet();
        restartBuildImmediately = new ConcurrentHashMap<>();
        this.lock = workspaceLock;
        InternalBuilder.buildManager = this;
    }

    /**
     * Creates and returns an ArrayList of BuilderPersistentInfo.
     * The list includes entries for all builders for all configs that are
     * in the builder spec, and that have a last built state, even if they
     * have not been instantiated this session.
     *
     * e.g.
     * For a project with 3 builders, 2 build configurations and the second
     * builder doesn't support configurations.
     * The returned List of BuilderInfos is ordered:
     * builder_id, config_name,builder_index
     * builder_1, config_1, 1
     * builder_1, config_2, 1
     * builder_2, null, 2
     * builder_3, config_1, 3
     * builder_3, config_1, 3
     */
    public ArrayList<BuilderPersistentInfo> createBuildersPersistentInfo(IProject project) throws CoreException {
        /* get the old builders (those not yet instantiated) */
        ArrayList<BuilderPersistentInfo> oldInfos = getBuildersPersistentInfo(project);

        ProjectDescription desc = ((Project) project).internalGetDescription();
        if (desc == null) {
            return null;
        }
        ICommand[] commands = desc.getBuildSpec(false);
        if (commands.length == 0) {
            return null;
        }
        IBuildConfiguration[] configs = project.getBuildConfigs();

        /* build the new list */
        ArrayList<BuilderPersistentInfo> newInfos = new ArrayList<>(commands.length * configs.length);
        for (int i = 0; i < commands.length; i++) {
            BuildCommand command = (BuildCommand) commands[i];
            String builderName = command.getBuilderName();

            // If the builder doesn't support configurations, only 1 delta tree to persist
            boolean supportsConfigs = command.supportsConfigs();
            int numberConfigs = supportsConfigs ? configs.length : 1;

            for (int j = 0; j < numberConfigs; j++) {
                IBuildConfiguration config = configs[j];
                BuilderPersistentInfo info = null;
                IncrementalProjectBuilder builder = ((BuildCommand) commands[i]).getBuilder(config);
                if (builder == null) {
                    // if the builder was not instantiated, use the old info if any.
                    if (oldInfos != null) {
                        info = getBuilderInfo(oldInfos, builderName, supportsConfigs ? config.getName() : null, i);
                    }
                } else if (!(builder instanceof MissingBuilder)) {
                    ElementTree oldTree = ((InternalBuilder) builder).getLastBuiltTree();
                    // don't persist build state for builders that have no last built state
                    if (oldTree != null) {
                        // if the builder was instantiated, construct a memento with the important info
                        info = new BuilderPersistentInfo(project.getName(), supportsConfigs ? config.getName() : null,
                            builderName, i);
                        info.setLastBuildTree(oldTree);
                        info.setInterestingProjects(((InternalBuilder) builder).getInterestingProjects());
                    }
                }
                if (info != null) {
                    newInfos.add(info);
                }
            }
        }
        return newInfos;
    }

    /**
     * The outermost workspace operation has finished. Do an autobuild if necessary.
     */
    public void endTopLevel(boolean needsBuild) {
    }

    /**
     * Removes the builder persistent info from the map corresponding to the
     * given builder name, configuration name and build spec index, or <code>null</code> if not found
     *
     * @param configName or null if the builder doesn't support configurations
     * @param buildSpecIndex The index in the build spec, or -1 if unknown
     */
    private BuilderPersistentInfo getBuilderInfo(ArrayList<BuilderPersistentInfo> infos, String builderName,
        String configName, int buildSpecIndex) {
        // try to match on builder index, but if not match is found, use the builder name and config name
        // this is because older workspace versions did not store builder infos in build spec order
        BuilderPersistentInfo nameMatch = null;
        for (BuilderPersistentInfo info : infos) {
            // match on name, config name and build spec index if known
            // Note: the config name may be null for builders that don't support configurations, or old workspaces
            if (info.getBuilderName().equals(builderName)
                && (info.getConfigName() == null || info.getConfigName().equals(configName))) {
                // we have found a match on name alone
                if (nameMatch == null) {
                    nameMatch = info;
                }
                // see if the index matches
                if (buildSpecIndex == -1
                    || info.getBuildSpecIndex() == -1
                    || buildSpecIndex == info.getBuildSpecIndex()) {
                    return info;
                }
            }
        }
        // no exact index match, so return name match, if any
        return nameMatch;
    }

    /**
     * Returns a list of BuilderPersistentInfo.
     * The list includes entries for all builders that are in the builder spec,
     * and that have a last built state but have not been instantiated this session.
     */
    @SuppressWarnings({ "unchecked" })
    public ArrayList<BuilderPersistentInfo> getBuildersPersistentInfo(IProject project) throws CoreException {
        return (ArrayList<BuilderPersistentInfo>) project.getSessionProperty(K_BUILD_LIST);
    }

    /**
     * Gets a workspace delta for a given project, based on the state of the workspace
     * tree the last time the current builder was run.
     * <p>
     * Returns null if:
     * <ul>
     * <li> The state of the workspace is unknown. </li>
     * <li> The current builder has not indicated that it is interested in deltas
     * for the given project. </li>
     * <li> If the project does not exist. </li>
     * </ul>
     * <p>
     * Deltas are computed once and cached for efficiency.
     *
     * @param project the project to get a delta for
     */
    IResourceDelta getDelta(IProject project) {
        try {
            lock.acquire();
            if (currentTree == null) {
                return null;
            }
            Set<InternalBuilder> interestedBuilders = getInterestedBuilders(project);
            // check if this builder has indicated it cares about this project
            if (interestedBuilders.isEmpty()) {
                return null;
            }

            // now check against the cache
            return getDeltaCached(project, currentLastBuiltTree, currentTree);
        } finally {
            lock.release();
        }
    }

    private IResourceDelta getDeltaCached(IProject project, ElementTree oldTree, ElementTree newTree) {
        final IPath fullPath = project.getFullPath();
        IResourceDelta resultDelta = deltaCache.computeIfAbsent(fullPath, oldTree, newTree, () -> {
            IResourceDelta result;
            if (!project.exists() && !newTree.includes(fullPath) && !oldTree.includes(fullPath)) {
                result = null;
            } else {
                result = ResourceDeltaFactory.computeDelta(workspace, oldTree, newTree, fullPath, -1);
            }

            return result;
        });
        return resultDelta;
    }

    @Override
    public void handleEvent(LifecycleEvent event) {
        IProject project = null;
        switch (event.kind) {
            case LifecycleEvent.PRE_PROJECT_DELETE:
            case LifecycleEvent.PRE_PROJECT_MOVE:
                project = (IProject) event.resource;
                // make sure the builder persistent info is deleted for the project move case
                if (project.isAccessible()) {
                    setBuildersPersistentInfo(project, null);
                }
        }
    }

    /**
     * Returns true if at least one of the given project's configs have been built
     * during this build cycle; and false otherwise.
     */
    boolean hasBeenBuilt(IProject project) {
        return builtProjects.contains(project);
    }

    /**
     * Returns true if the current builder is interested in changes
     * to the given project, and false otherwise.
     */
    private boolean isInterestingProject(InternalBuilder currentBuilder, IProject project) {
        if (project.equals(currentBuilder.getProject())) {
            return true;
        }
        IProject[] interestingProjects = currentBuilder.getInterestingProjects();
        for (IProject interestingProject : interestingProjects) {
            if (interestingProject.equals(project)) {
                return true;
            }
        }
        return false;
    }

    private Set<InternalBuilder> getInterestedBuilders(final IProject project) {
        final Set<InternalBuilder> res = new HashSet<>();
        for (final InternalBuilder builder : this.currentBuilders) {
            if (isInterestingProject(builder, project)) {
                res.add(builder);
            }
        }
        return res;
    }

    /**
     * Hook for builders to request a global rebuild for the main build loop on next
     * build cycle. All projects will be rebuilt at least once after the current
     * build cycle.
     */
    void requestRebuild() {
    }

    /**
     * Hook for builders to request a rebuild for given project during the current
     * build call. The builders configured to run after the current one will be
     * still processed. To force an immediate rebuild of a project that wasn't fully
     * built yet, {@code processOtherBuilders} argument should be set to
     * {@code false}.
     * <p>
     * <b>Note</b> if {@code processOtherBuilders} is set to {@code false}, the
     * project that is built with current builder will be only rebuilt again, if
     * this builder is not the first one configured to run.
     *
     * @param processOtherBuilders to continue building project with other builders
     * and not start from scratch immediately
     */
    void requestRebuild(IProject project, boolean processOtherBuilders) {
        if (project == null) {
            return;
        }
        restartBuildImmediately.put(project, !processOtherBuilders);
    }

    /**
     * Hook for builders to request a rebuild for given projects. This request will
     * cause the main build loop to cycle once again <b>at least</b> for given
     * projects but the build loop also may run over all projects in build cycle if
     * the {@link #requestRebuild()} flag was set.
     * <p>
     * <b>Note</b> the current project (that is currently built with current
     * builder) will be not rebuilt in the current builld cycle, but scheduled for
     * rebuild on next round. To perform immediate rebuild of the current project,
     * use {@link #requestRebuild(IProject, boolean)}.
     *
     * @param toBeRebuilt to be rebuilt on next build round
     * @param current project currently built with current builder
     */
    void requestRebuild(Collection<IProject> toBeRebuilt, IProject current) {
        for (IProject project : toBeRebuilt) {
            if (project != null && hasBeenBuilt(project) || project.equals(current)) {
                requestRebuildOnNextRound(project);
            }
        }
    }

    /**
     * Hook for builders to request an <b>unconditional<b> rebuild for given
     * project, in the next build round, independently if the project was already
     * built or not.
     */
    void requestRebuildOnNextRound(IProject project) {
        projectsToRebuild.add(project);
    }

    /**
     * Sets the builder infos for the given build config. The builder infos are
     * an ArrayList of BuilderPersistentInfo.
     * The list includes entries for all builders that are
     * in the builder spec, and that have a last built state, even if they
     * have not been instantiated this session.
     */
    public void setBuildersPersistentInfo(IProject project, List<BuilderPersistentInfo> list) {
        try {
            project.setSessionProperty(K_BUILD_LIST, list);
        } catch (CoreException e) {
            // project is missing -- build state will be lost
            // can't throw an exception because this happens on startup
            logProjectAccessError(project, e, "Project missing in setBuildersPersistentInfo"); //$NON-NLS-1$
        }
    }

    private void logProjectAccessError(IProject project, CoreException e, String message) {
        Policy.log(new ResourceStatus(IStatus.ERROR, 1, project.getFullPath(), message, e));
    }

}
