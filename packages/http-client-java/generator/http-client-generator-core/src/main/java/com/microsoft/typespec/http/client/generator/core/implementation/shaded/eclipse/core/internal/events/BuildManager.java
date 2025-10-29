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
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IProject;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BuildManager implements ICoreConstants {

    // the job for performing background autobuild
    private final Set<IProject> builtProjects = Collections.synchronizedSet(new HashSet<>());

    // the following four fields only apply for the lifetime of a single builder invocation.
    protected final Set<InternalBuilder> currentBuilders;

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

    public BuildManager() {
        this.currentBuilders = Collections.synchronizedSet(new HashSet<>());
        projectsToRebuild = ConcurrentHashMap.newKeySet();
        restartBuildImmediately = new ConcurrentHashMap<>();
        InternalBuilder.buildManager = this;
    }

    /**
     * The outermost workspace operation has finished. Do an autobuild if necessary.
     */
    public void endTopLevel(boolean needsBuild) {
    }

    /**
     * Returns true if at least one of the given project's configs have been built
     * during this build cycle; and false otherwise.
     */
    boolean hasBeenBuilt(IProject project) {
        return builtProjects.contains(project);
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

}
