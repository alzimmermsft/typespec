/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Serge Beauchamp (Freescale Semiconductor) - [229633] Project Path Variable Support
 *     Anton Leherbauer (Wind River) - [198591] Allow Builder to specify scheduling rule
 *     Francis Lynch (Wind River) - [301563] Save and load tree snapshots
 *     Markus Schorn (Wind River) - [306575] Save snapshot location with project
 *     Broadcom Corporation - ongoing development
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 473427
 *     Christoph Läubrich - Issue #80 - CharsetManager access the ResourcesPlugin.getWorkspace before init
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.filesystem.EFS;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.filesystem.IFileInfo;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.filesystem.IFileStore;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.events.LifecycleEvent;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.preferences.EclipsePreferences;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.utils.FileUtil;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.utils.Messages;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.utils.Policy;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IBuildConfiguration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IContainer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IFile;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IProjectDescription;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IProjectNature;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResourceStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.ProjectScope;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.ResourcesPlugin;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.MultiStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.OperationCanceledException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Platform;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Status;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.SubMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.jobs.ISchedulingRule;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.preferences.DefaultScope;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.preferences.InstanceScope;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.util.NLS;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.prefs.Preferences;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

public class Project extends Container implements IProject {
    /**
     * Option constant (value 2) indicating that the snapshot location shall be
     * persisted with the project for autoloading the snapshot when the project
     * is imported in another workspace.
     * <p>
     * <strong>EXPERIMENTAL</strong>. This constant has been added as
     * part of a work in progress. There is no guarantee that this API will
     * work or that it will remain the same. Please do not use this API without
     * consulting with the Platform Core team.
     * </p>
     * 
     * @see #saveSnapshot(int, URI, IProgressMonitor)
     * @since 3.6
     */
    public static final int SNAPSHOT_SET_AUTOLOAD = 2;

    protected Project(IPath path, Workspace container) {
        super(path, container);
    }

    protected void assertCreateRequirements(IProjectDescription description) throws CoreException {
        checkDoesNotExist();
        checkDescription(this, description, false);
        URI location = description.getLocationURI();
        if (location != null) {
            return;
        }
        // if the project is in the default location, need to check for collision with existing folder of different case
        if (!Workspace.caseSensitive) {
            IFileStore store = getStore();
            IFileInfo localInfo = store.fetchInfo();
            if (localInfo.exists()) {
                String name = getLocalManager().getLocalName(store);
                if (name != null && !store.getName().equals(name)) {
                    String msg = NLS.bind(Messages.resources_existsLocalDifferentCase,
                        IPath.fromOSString(store.toString()).removeLastSegments(1).append(name).toOSString());
                    throw new ResourceException(IResourceStatus.CASE_VARIANT_EXISTS, getFullPath(), msg, null);
                }
            }
        }
    }

    /*
     * If the creation boolean is true then this method is being called on project creation.
     * Otherwise it is being called via #setDescription. The difference is that we don't allow
     * some description fields to change value after project creation. (e.g. project location)
     */
    protected MultiStatus basicSetDescription(ProjectDescription description, int updateFlags) {
        String message = Messages.resources_projectDesc;
        MultiStatus result
            = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.FAILED_WRITE_METADATA, message, null);
        ProjectDescription current = internalGetDescription();
        current.setComment(description.getComment());
        current.setSnapshotLocationURI(description.getSnapshotLocationURI());

        // set the build order before setting the references or the natures
        current.setBuildSpec(description.getBuildSpec(true));

        // set the references before the natures
        IProject[] oldReferences = current.getReferencedProjects();
        IProject[] newReferences = description.getReferencedProjects();
        if (!Arrays.equals(oldReferences, newReferences)) {
            current.setReferencedProjects(newReferences);
        }
        // Update the dynamic state

        // the natures last as this may cause recursive calls to setDescription.
        if ((updateFlags & IResource.AVOID_NATURE_CONFIG) == 0) {
            workspace.getNatureManager().configureNatures(this, current, description, result);
        } else {
            current.setNatureIds(description.getNatureIds(false));
        }
        return result;
    }

    /**
     * Checks that this resource is accessible. Typically this means that it
     * exists. In the case of projects, they must also be open.
     * If phantom is true, phantom resources are considered.
     *
     * @exception CoreException if this resource is not accessible
     */
    @Override
    public void checkAccessible(int flags) throws CoreException {
        super.checkAccessible(flags);
        if (!isOpen(flags)) {
            String message = NLS.bind(Messages.resources_mustBeOpen, getName());
            throw new ResourceException(IResourceStatus.PROJECT_NOT_OPEN, getFullPath(), message, null);
        }
    }

    /**
     * Checks validity of the given project description.
     */
    protected void checkDescription(IProject project, IProjectDescription desc, boolean moving) throws CoreException {
        URI location = desc.getLocationURI();
        String message = NLS.bind(Messages.resources_invalidProjDesc, project.getName(), String.valueOf(location));
        MultiStatus status
            = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.INVALID_VALUE, message, null);
        status.merge(workspace.validateName(desc.getName(), IResource.PROJECT));
        if (moving) {
            // if we got here from a move call then we should check the location in the description since
            // its possible that we want to do a rename without moving the contents. (and we shouldn't
            // throw an Overlapping mapping exception in this case) So if the source description's location
            // is null (we are using the default) or if the locations aren't equal, then validate the location
            // of the new description. Otherwise both locations aren't null and they are equal so ignore validation.
            URI sourceLocation = internalGetDescription().getLocationURI();
            if (sourceLocation == null || !sourceLocation.equals(location)) {
                status.merge(workspace.validateProjectLocationURI(project, location));
            }
        } else {
            // otherwise continue on like before
            status.merge(workspace.validateProjectLocationURI(project, location));
        }
        if (!status.isOK()) {
            throw new ResourceException(status);
        }
    }

    @Override
    public void create(IProjectDescription description, int updateFlags, IProgressMonitor monitor)
        throws CoreException {
        SubMonitor subMonitor = SubMonitor.convert(monitor, Messages.resources_create, 100);
        checkValidPath(path, PROJECT, false);
        final ISchedulingRule rule = workspace.getRuleFactory().createRule(this);
        try {
            workspace.prepareOperation(rule, subMonitor);
            if (description == null) {
                description = new ProjectDescription();
                description.setName(getName());
            }
            assertCreateRequirements(description);
            workspace.broadcastEvent(LifecycleEvent.newEvent(LifecycleEvent.PRE_PROJECT_CREATE, this));
            workspace.beginOperation(true);
            workspace.createResource(this, updateFlags);
            workspace.getMetaArea().create(this);
            ProjectInfo info = (ProjectInfo) getResourceInfo(false, true);

            // setup description to obtain project location
            ProjectDescription desc = (ProjectDescription) ((ProjectDescription) description).clone();
            desc.setLocationURI(FileUtil.canonicalURI(description.getLocationURI()));
            desc.setName(getName());
            internalSetDescription(desc, false);
            // see if there potentially are already contents on disk
            final boolean hasSavedDescription = getLocalManager().hasSavedDescription(this);
            boolean hasContent = hasSavedDescription;
            // if there is no project description, there might still be content on disk
            if (!hasSavedDescription) {
                hasContent = getLocalManager().hasSavedContent(this);
            }
            try {
                // look for a description on disk
                if (hasSavedDescription) {
                    updateDescription();
                    // make sure the .location file is written
                    workspace.getMetaArea().writePrivateDescription(this);
                } else {
                    // write out the project
                    writeDescription();
                }
            } catch (CoreException e) {
                workspace.deleteResource(this);
                throw e;
            }
            // inaccessible projects have a null modification stamp.
            // set this after setting the description as #setDescription
            // updates the stamp
            info.clearModificationStamp();
            // if a project already had content on disk, mark the project as having unknown
            // children
            if (hasContent) {
                info.set(ICoreConstants.M_CHILDREN_UNKNOWN);
            }
            workspace.getSaveManager().requestSnapshot();
        } catch (OperationCanceledException e) {
            workspace.getWorkManager().operationCanceled();
            throw e;
        } finally {
            subMonitor.done();
            workspace.endOperation(rule, true);
        }
    }

    @Override
    public void delete(boolean deleteContent, boolean force, IProgressMonitor monitor) throws CoreException {
        int updateFlags = force ? IResource.FORCE : IResource.NONE;
        updateFlags |= deleteContent ? IResource.ALWAYS_DELETE_PROJECT_CONTENT : IResource.NEVER_DELETE_PROJECT_CONTENT;
        delete(updateFlags, monitor);
    }

    @Override
    public void delete(boolean force, IProgressMonitor monitor) throws CoreException {
        int updateFlags = force ? IResource.FORCE : IResource.NONE;
        delete(updateFlags, monitor);
    }

    @Override
    public void deleteResource(boolean convertToPhantom, MultiStatus status) throws CoreException {
        super.deleteResource(convertToPhantom, status);
        // Clear the history store.
        clearHistory(null);
        // Delete the project metadata.
        workspace.getMetaArea().delete(this);
    }

    @Override
    protected void fixupAfterMoveSource() throws CoreException {
        workspace.deleteResource(this);
        // check if we deleted a preferences file
        ProjectPreferences.deleted(this);
    }

    @Override
    public IBuildConfiguration getActiveBuildConfig() throws CoreException {
        ResourceInfo info = getResourceInfo(false, false);
        int flags = getFlags(info);
        checkAccessible(flags);
        return internalGetActiveBuildConfig();
    }

    @Override
    public IBuildConfiguration getBuildConfig(String configName) throws CoreException {
        if (configName == null) {
            return getActiveBuildConfig();
        }
        ProjectInfo info = (ProjectInfo) getResourceInfo(false, false);
        checkAccessible(getFlags(info));
        IBuildConfiguration[] configs = internalGetBuildConfigs(false);
        for (IBuildConfiguration config : configs) {
            if (config.getName().equals(configName)) {
                return config;
            }
        }
        throw new ResourceException(IResourceStatus.BUILD_CONFIGURATION_NOT_FOUND, getFullPath(), null, null);
    }

    @Override
    public IBuildConfiguration[] getBuildConfigs() throws CoreException {
        ProjectInfo info = (ProjectInfo) getResourceInfo(false, false);
        checkAccessible(getFlags(info));
        return internalGetBuildConfigs(true);
    }

    @Override
    public String getDefaultCharset(boolean checkImplicit) {
        // non-existing resources default to parent's charset
        if (!exists()) {
            return checkImplicit ? ResourcesPlugin.getEncoding() : null;
        }
        return workspace.getCharsetManager().getCharsetFor(getFullPath(), checkImplicit);
    }

    @Override
    public IProjectDescription getDescription() throws CoreException {
        ResourceInfo info = getResourceInfo(false, false);
        checkAccessible(getFlags(info));
        ProjectDescription description = ((ProjectInfo) info).getDescription();
        // if the project is currently in the middle of being created, the description might not be available yet
        if (description == null) {
            checkAccessible(NULL_FLAG);
        }
        return (IProjectDescription) description.clone();
    }

    @Override
    public IProjectNature getNature(String natureID) throws CoreException {
        // Has it already been initialized?
        ProjectInfo info = (ProjectInfo) getResourceInfo(false, false);
        checkAccessible(getFlags(info));
        IProjectNature nature = info.getNature(natureID);
        if (nature == null) {
            // Not initialized yet. Does this project have the nature?
            if (!hasNature(natureID)) {
                return null;
            }
            nature = workspace.getNatureManager().createNature(this, natureID);
            info.setNature(natureID, nature);
        }
        return nature;
    }

    @Override
    public IContainer getParent() {
        return workspace.getRoot();
    }

    @Override
    public IProject getProject() {
        return this;
    }

    @Override
    public IPath getProjectRelativePath() {
        return IPath.EMPTY;
    }

    @Override
    public IPath getRawLocation() {
        ProjectDescription description = internalGetDescription();
        return description == null ? null : description.getLocation();
    }

    @Override
    public URI getRawLocationURI() {
        ProjectDescription description = internalGetDescription();
        return description == null ? null : description.getLocationURI();
    }

    @Override
    public void clearCachedDynamicReferences() {
        ResourceInfo info = getResourceInfo(false, false);
        if (info == null) {
            // If the project is not open there is no cached state and so there is nothing to do.
            return;
        }
        ProjectDescription description = ((ProjectInfo) info).getDescription();
        if (description == null) {
            // If the project is in the process of being created there is no cached state and nothing to do.
            return;
        }
        description.clearCachedDynamicReferences(null);
    }

    @Override
    public IProject[] getReferencingProjects() {
        IProject[] projects = workspace.getRoot().getProjects(IContainer.INCLUDE_HIDDEN);
        List<IProject> result = new ArrayList<>(projects.length);
        for (IProject p : projects) {
            Project project = (Project) p;
            if (!project.isAccessible()) {
                continue;
            }
            ProjectDescription description = project.internalGetDescription();
            if (description == null) {
                continue;
            }
            IProject[] references = description.getAllReferences(project, false);
            for (IProject reference : references) {
                if (reference.equals(this)) {
                    result.add(project);
                    break;
                }
            }
        }
        return result.toArray(new IProject[result.size()]);
    }

    @Override
    public int getType() {
        return PROJECT;
    }

    @Override
    public IPath getWorkingLocation(String id) {
        if (id == null || !exists()) {
            return null;
        }
        IPath result = workspace.getMetaArea().getWorkingLocation(this, id);
        result.toFile().mkdirs();
        return result;
    }

    @Override
    public boolean hasNature(String natureID) throws CoreException {
        checkAccessible(getFlags(getResourceInfo(false, false)));
        // use #internal method to avoid copy but still throw an
        // exception if the resource doesn't exist.
        IProjectDescription desc = internalGetDescription();
        if (desc == null) {
            checkAccessible(NULL_FLAG);
        }
        return desc.hasNature(natureID);
    }

    /**
     * Closes the project. This is called during restore when there is a failure
     * to read the project description. Since it is called during workspace restore,
     * it cannot start any operations.
     */
    protected void internalClose(IProgressMonitor monitor) throws CoreException {
        SubMonitor subMonitor = SubMonitor.convert(monitor, 2);
        getMarkerManager().removeMarkers(this, IResource.DEPTH_INFINITE);
        subMonitor.worked(1);
        // remove each member from the resource tree.
        // DO NOT use resource.delete() as this will delete it from disk as well.
        IResource[] members = members(
            IContainer.INCLUDE_PHANTOMS | IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS | IContainer.INCLUDE_HIDDEN);
        subMonitor.setWorkRemaining(members.length);
        for (IResource member2 : members) {
            Resource member = (Resource) member2;
            workspace.deleteResource(member);
            subMonitor.worked(1);
        }
        // finally mark the project as closed.
        ResourceInfo info = getResourceInfo(false, true);
        info.clear(M_OPEN);
        info.clearSessionProperties();
        info.clearModificationStamp();
        info.clearCharsetGenerationCount();
        info.setSyncInfo(null);
    }

    /**
     * Like {@link #getActiveBuildConfig()} but doesn't check accessibility.
     * Project must be accessible.
     * 
     * @see #getActiveBuildConfig()
     */
    IBuildConfiguration internalGetActiveBuildConfig() {
        ProjectDescription description = internalGetDescription();
        if (description == null) {
            return new BuildConfiguration(this, IBuildConfiguration.DEFAULT_CONFIG_NAME);
        }
        String configName = description.activeConfiguration;
        try {
            if (configName != null) {
                return getBuildConfig(configName);
            }
        } catch (CoreException e) {
            // Build configuration doesn't exist; treat the first as active.
        }
        return internalGetBuildConfigs(false)[0];
    }

    /**
     * @return IBuildConfiguration[] always contains at least one build configuration
     */
    public IBuildConfiguration[] internalGetBuildConfigs(boolean makeCopy) {
        ProjectDescription desc = internalGetDescription();
        if (desc == null) {
            return new IBuildConfiguration[] { new BuildConfiguration(this, IBuildConfiguration.DEFAULT_CONFIG_NAME) };
        }
        return desc.getBuildConfigs(this, makeCopy);
    }

    /**
     * This is an internal helper method. This implementation is different from the API
     * method getDescription(). This one does not check the project accessibility. It exists
     * in order to prevent "chicken and egg" problems in places like the project creation.
     * It may return null.
     */
    public ProjectDescription internalGetDescription() {
        ProjectInfo info = (ProjectInfo) getResourceInfo(false, false);
        if (info == null) {
            return null;
        }
        return info.getDescription();
    }

    /**
     * Sets this project's description to the given value. This is the body of the
     * corresponding API method but is needed separately since it is used
     * during workspace restore (i.e., when you cannot do an operation)
     */
    void internalSetDescription(IProjectDescription value, boolean incrementContentId) {
        internalSetDescription(value, incrementContentId, false);
    }

    void internalSetDescription(IProjectDescription value, boolean incrementContentId,
        boolean locationAlreadyNormalized) {

        ProjectInfo info = (ProjectInfo) getResourceInfo(false, true);
        info.setDescription((ProjectDescription) value);
        if (locationAlreadyNormalized) {
            getLocalManager().setNormalizedLocation(this, info, value.getLocationURI());
        } else {
            getLocalManager().setLocation(this, info, value.getLocationURI());
        }
        if (incrementContentId) {
            info.incrementContentId();
            // if the project is not accessible, stamp will be null and should remain null
            if (info.getModificationStamp() != NULL_STAMP) {
                workspace.updateModificationStamp(info);
            }
        }
    }

    @Override
    public void internalSetLocal(boolean flag, int depth) throws CoreException {
        // do nothing for projects, but call for its children
        if (depth == IResource.DEPTH_ZERO) {
            return;
        }
        if (depth == IResource.DEPTH_ONE) {
            depth = IResource.DEPTH_ZERO;
        }
        // get the children via the workspace since we know that this
        // resource exists (it is local).
        IResource[] children = getChildren(IResource.NONE);
        for (IResource element : children) {
            ((Resource) element).internalSetLocal(flag, depth);
        }
    }

    @Override
    public boolean isAccessible() {
        return isOpen();
    }

    @Override
    public boolean isDerived(int options) {
        // projects are never derived
        return false;
    }

    @Override
    public boolean isLinked(int options) {
        return false;// projects are never linked
    }

    @Override
    public boolean isVirtual() {
        return false; // projects are never virtual
    }

    @Deprecated
    @Override
    public boolean isLocal(int depth) {
        // the flags parameter is ignored for projects so pass anything
        return isLocal(-1, depth);
    }

    @Deprecated
    @Override
    public boolean isLocal(int flags, int depth) {
        // don't check the flags....projects are always local
        if (depth == DEPTH_ZERO) {
            return true;
        }
        if (depth == DEPTH_ONE) {
            depth = DEPTH_ZERO;
        }
        // get the children via the workspace since we know that this
        // resource exists (it is local).
        IResource[] children = getChildren(IResource.NONE);
        for (IResource element : children) {
            if (!element.isLocal(depth)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isOpen() {
        ResourceInfo info = getResourceInfo(false, false);
        return isOpen(getFlags(info));
    }

    public boolean isOpen(int flags) {
        return flags != NULL_FLAG && ResourceInfo.isSet(flags, M_OPEN);
    }

    @Override
    public void loadSnapshot(int options, URI snapshotLocation, IProgressMonitor monitor) throws CoreException {
        // load a snapshot of refresh information when project is not opened
        if (isOpen()) {
            String message = Messages.resources_projectMustNotBeOpen;
            MultiStatus status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IStatus.ERROR, message, null);
            throw new CoreException(status);
        }
        internalLoadSnapshot(options, snapshotLocation, monitor);
    }

    /**
     * Loads a snapshot of project meta-data from the given location URI.
     * Like {@link IProject#loadSnapshot(int, URI, IProgressMonitor)} but can be
     * called when the project is open.
     *
     * @see IProject#saveSnapshot(int, URI, IProgressMonitor)
     */
    private void internalLoadSnapshot(int options, URI snapshotLocation, IProgressMonitor monitor)
        throws CoreException {
        if ((options & SNAPSHOT_TREE) != 0) {
            // ensure that path variables are resolved: only ws accessible while project is closed
            snapshotLocation = workspace.getPathVariableManager().resolveURI(snapshotLocation);
            if (!snapshotLocation.isAbsolute()) {
                String message = NLS.bind(Messages.projRead_badSnapshotLocation, snapshotLocation.toString());
                throw new CoreException(new Status(IStatus.WARNING, ResourcesPlugin.PI_RESOURCES, message, null));
            }
            // copy the snapshot from the URI into the project metadata
            IPath snapshotPath = workspace.getMetaArea().getRefreshLocationFor(this);
            IFileStore snapshotFileStore = EFS.getStore(
                com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.filesystem.URIUtil
                    .toURI(snapshotPath));
            EFS.getStore(snapshotLocation).copy(snapshotFileStore, EFS.OVERWRITE, monitor);
        }
    }

    @Override
    public void open(int updateFlags, IProgressMonitor monitor) throws CoreException {
        monitor = Policy.monitorFor(monitor);
        boolean encodingWritten = false;
        try {
            String msg = NLS.bind(Messages.resources_opening_1, getName());
            monitor.beginTask(msg, Policy.totalWork);
            monitor.subTask(msg);
            final ISchedulingRule rule = workspace.getRuleFactory().modifyRule(this);
            try {
                workspace.prepareOperation(rule, monitor);
                ProjectInfo info = (ProjectInfo) getResourceInfo(false, false);
                int flags = getFlags(info);
                checkExists(flags, true);
                if (isOpen(flags)) {
                    return;
                }

                workspace.beginOperation(true);
                info = (ProjectInfo) getResourceInfo(false, true);
                info.set(M_OPEN);
                // clear the unknown children immediately to avoid background refresh
                boolean unknownChildren = info.isSet(M_CHILDREN_UNKNOWN);
                if (unknownChildren) {
                    info.clear(M_CHILDREN_UNKNOWN);
                }
                // the M_USED flag is used to indicate the difference between opening a project
                // for the first time and opening it from a previous close (restoring it from disk)
                boolean used = info.isSet(M_USED);
                boolean snapshotLoaded = false;
                if (!used && !workspace.getMetaArea().getRefreshLocationFor(this).toFile().exists()) {
                    // auto-load a refresh snapshot if it is set
                    final boolean hasSavedDescription = getLocalManager().hasSavedDescription(this);
                    if (hasSavedDescription) {
                        ProjectDescription updatedDesc = info.getDescription();
                        if (updatedDesc != null) {
                            URI autoloadURI = updatedDesc.getSnapshotLocationURI();
                            if (autoloadURI != null) {
                                try {
                                    autoloadURI = getPathVariableManager().resolveURI(autoloadURI);
                                    internalLoadSnapshot(SNAPSHOT_TREE, autoloadURI,
                                        Policy.subMonitorFor(monitor, Policy.opWork * 5 / 100));
                                    snapshotLoaded = true;
                                } catch (CoreException ce) {
                                    // Log non-existing autoload snapshot as warning only
                                    String msgerr = NLS.bind(Messages.projRead_cannotReadSnapshot, getName(),
                                        ce.getLocalizedMessage());
                                    Policy.log(new Status(IStatus.WARNING, ResourcesPlugin.PI_RESOURCES, msgerr));
                                }
                            }
                        }
                    }
                }
                boolean minorIssuesDuringRestore = false;
                if (used) {
                    minorIssuesDuringRestore = workspace.getSaveManager()
                        .restore(this, Policy.subMonitorFor(monitor, Policy.opWork * 20 / 100));
                } else {
                    info.set(M_USED);
                    // reconcile any links and groups in the project description
                    IStatus result = reconcileLinksAndGroups(info.getDescription());
                    if (!result.isOK()) {
                        throw new CoreException(result);
                    }
                    workspace.updateModificationStamp(info);
                    monitor.worked(Policy.opWork * (snapshotLoaded ? 15 : 20) / 100);
                }
                startup();
                // request a refresh if the project is new and has unknown members on disk
                // or restore of the project is not fully successful
                if ((!used && unknownChildren) || !minorIssuesDuringRestore) {
                    boolean refreshed = false;
                    if (!used) {
                        refreshed = workspace.getSaveManager()
                            .restoreFromRefreshSnapshot(this, Policy.subMonitorFor(monitor, Policy.opWork * 20 / 100));
                        if (refreshed) { // account for the refresh work
                            monitor.worked(Policy.opWork * 60 / 100);
                        }
                    }
                    // refresh either in background or foreground
                    if (!refreshed) {
                        if ((updateFlags & IResource.BACKGROUND_REFRESH) != 0) {
                            monitor.worked(Policy.opWork * 60 / 100);
                        } else {
                            refreshLocal(IResource.DEPTH_INFINITE,
                                Policy.subMonitorFor(monitor, Policy.opWork * 60 / 100));
                        }
                    }
                } else {
                    // Bug 544975 - When opening a closed project, refresh it in the background
                    if ((updateFlags & IResource.BACKGROUND_REFRESH) != 0) {
                        monitor.worked(Policy.opWork * 60 / 100);
                    }
                }

                // Project is new and does not have any content already (not imported)
                if (!used && !unknownChildren) {
                    writeEncodingAfterOpen(monitor);
                    encodingWritten = true;
                }
                // creation of this project may affect overlapping resources
                workspace.getAliasManager().updateAliases(this, getStore(), IResource.DEPTH_INFINITE, monitor);
            } catch (OperationCanceledException e) {
                workspace.getWorkManager().operationCanceled();
                throw e;
            } finally {
                workspace.endOperation(rule, true);
            }
        } finally {
            monitor.done();
        }
        if (!encodingWritten) {
            ValidateProjectEncoding.scheduleProjectValidation((Workspace) getWorkspace(), this);
        }
    }

    /**
     * Try to set encoding if we open the project for the first time. See bug 479450
     */
    private void writeEncodingAfterOpen(IProgressMonitor monitor) throws CoreException {
        IPath settings = IPath.fromOSString(EclipsePreferences.DEFAULT_PREFERENCES_DIRNAME)
            .append(ResourcesPlugin.PI_RESOURCES)
            .addFileExtension(EclipsePreferences.PREFS_FILE_EXTENSION);
        IFile file = getFile(settings);

        // The file could not yet be up-to-date with underlined resource
        // force refresh to force reading project preferences via
        // org.eclipse.core.internal.resources.ProjectPreferences.updatePreferences(IFile)
        IPath location = file.getLocation();
        if (!file.exists() && location != null && location.toFile().exists()) {
            file.refreshLocal(IResource.DEPTH_ZERO, monitor);
        }
        String charset = workspace.getCharsetManager().getCharsetFor(getFullPath(), false);
        if (charset == null) {
            String encoding = ResourcesPlugin.getEncoding();
            workspace.getCharsetManager().setCharsetFor(getFullPath(), encoding);
        }
    }

    @Override
    public void open(IProgressMonitor monitor) throws CoreException {
        open(IResource.NONE, monitor);
    }

    /**
     * The project description file has changed on disk, resulting in a changed
     * set of linked resources. Perform the necessary creations and deletions of
     * links to bring the links in sync with those described in the project description.
     * 
     * @param newDescription the new project description that may have
     * changed link descriptions.
     * @return status ok if everything went well, otherwise an ERROR multi-status
     * describing the problems encountered.
     */
    public IStatus reconcileLinksAndGroups(ProjectDescription newDescription) {
        String msg = Messages.links_errorLinkReconcile;
        HashMap<IPath, LinkDescription> newLinks = newDescription.getLinks();
        MultiStatus status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.OPERATION_FAILED, msg, null);
        // walk over old linked resources and remove those that are no longer defined
        ProjectDescription oldDescription = internalGetDescription();
        if (oldDescription != null) {
            HashMap<IPath, LinkDescription> oldLinks = oldDescription.getLinks();
            if (oldLinks != null) {
                for (LinkDescription oldLink : oldLinks.values()) {
                    Resource oldLinkResource = (Resource) findMember(oldLink.getProjectRelativePath());
                    if (oldLinkResource == null || !oldLinkResource.isLinked()) {
                        continue;
                    }
                    LinkDescription newLink = null;
                    if (newLinks != null) {
                        newLink = newLinks.get(oldLink.getProjectRelativePath());
                    }
                    // if the new link is missing, or has different (raw) location or gender, then remove old link
                    if (newLink == null
                        || !newLink.getLocationURI().equals(oldLinkResource.getRawLocationURI())
                        || newLink.getType() != oldLinkResource.getType()) {
                        try {
                            oldLinkResource.delete(IResource.NONE, null);
                            // refresh the resource, because removing a link can reveal a previously hidden resource in
                            // parent
                            oldLinkResource.refreshLocal(IResource.DEPTH_INFINITE, null);
                        } catch (CoreException e) {
                            status.merge(e.getStatus());
                        }
                    }
                }
            }
        }
        // walk over new links and groups and create if necessary
        // Recreate them in order of the higher up in the tree hierarchy first,
        // so we don't have to create intermediate directories that would turn
        // out
        // to be groups or link folders.
        if (newLinks == null) {
            return status;
        }
        // sort links to avoid creating nested links before their parents
        TreeSet<LinkDescription> newLinksAndGroups = new TreeSet<>((arg0, arg1) -> {
            int numberOfSegments0 = arg0.getProjectRelativePath().segmentCount();
            int numberOfSegments1 = arg1.getProjectRelativePath().segmentCount();
            if (numberOfSegments0 != numberOfSegments1) {
                return numberOfSegments0 - numberOfSegments1;
            } else if (arg0.equals(arg1)) {
                return 0;
            }

            return -1;
        });
        newLinksAndGroups.addAll(newLinks.values());

        for (LinkDescription newLink : newLinksAndGroups) {
            try {
                Resource toLink
                    = workspace.newResource(getFullPath().append(newLink.getProjectRelativePath()), newLink.getType());
                IContainer parent = toLink.getParent();
                if (parent != null && !parent.exists() && parent.getType() == FOLDER) {
                    ((Folder) parent).ensureExists(Policy.monitorFor(null));
                }
                if (!toLink.exists() || !toLink.isLinked()) {
                    if (newLink.isGroup()) {
                        ((Folder) toLink).create(IResource.REPLACE | IResource.VIRTUAL, true, null);
                    } else {
                        toLink.createLink(newLink.getLocationURI(), IResource.REPLACE | IResource.ALLOW_MISSING_LOCAL,
                            null);
                    }
                }
            } catch (CoreException e) {
                status.merge(e.getStatus());
            }
        }
        return status;
    }

    /**
     * Restore the non-persisted state for the project. For example, read and set
     * the description from the local meta area. Also, open the property store etc.
     * This method is used when an open project is restored and so emulates
     * the behaviour of open().
     */
    protected void startup() throws CoreException {
        if (!isOpen()) {
            return;
        }
        workspace.broadcastEvent(LifecycleEvent.newEvent(LifecycleEvent.PRE_PROJECT_OPEN, this));
    }

    @Override
    public void touch(IProgressMonitor monitor) throws CoreException {
        monitor = Policy.monitorFor(monitor);
        try {
            String message = NLS.bind(Messages.resources_touch, getFullPath());
            monitor.beginTask(message, Policy.totalWork);
            final ISchedulingRule rule = workspace.getRuleFactory().modifyRule(this);
            try {
                workspace.prepareOperation(rule, monitor);
                workspace.beginOperation(true);
                super.touch(Policy.subMonitorFor(monitor, Policy.opWork));
            } catch (OperationCanceledException e) {
                workspace.getWorkManager().operationCanceled();
                throw e;
            } finally {
                try {
                    workspace.broadcastEvent(LifecycleEvent.newEvent(LifecycleEvent.POST_PROJECT_CHANGE, this));
                } finally {
                    workspace.endOperation(rule, true);
                }
            }
        } finally {
            monitor.done();
        }
    }

    /**
     * The project description file on disk is better than the description in memory.
     * Make sure the project description in memory is synchronized with the
     * description file contents.
     */
    protected void updateDescription() throws CoreException {
        if (ProjectDescription.isWriting) {
            return;
        }
        ProjectDescription.isReading = true;
        try {
            ProjectDescription description = getLocalManager().read(this, false);
            // links can only be created if the project is open
            IStatus result = null;
            if (isOpen()) {
                result = reconcileLinksAndGroups(description);
            }
            internalSetDescription(description, true);
            if (result != null && !result.isOK()) {
                throw new CoreException(result);
            }
        } finally {
            workspace.broadcastEvent(LifecycleEvent.newEvent(LifecycleEvent.POST_PROJECT_CHANGE, this));
            ProjectDescription.isReading = false;
        }
    }

    /**
     * Writes the project's current description file to disk.
     */
    public void writeDescription() throws CoreException {
        writeDescription(internalGetDescription(), true, true);
    }

    /**
     * Writes the project description file to disk. This is the only method
     * that should ever be writing the description, because it ensures that
     * the description isn't then immediately discovered as an incoming
     * change and read back from disk.
     * 
     * @param description The description to write
     * @param hasPublicChanges Whether the public sections of the description have changed
     * @param hasPrivateChanges Whether the private sections of the description have changed
     * @exception CoreException On failure to write the description
     */
    public void writeDescription(IProjectDescription description, boolean hasPublicChanges, boolean hasPrivateChanges)
        throws CoreException {
        if (ProjectDescription.isReading) {
            return;
        }
        ProjectDescription.isWriting = true;
        try {
            getLocalManager().internalWrite(this, description, hasPublicChanges, hasPrivateChanges);
        } finally {
            ProjectDescription.isWriting = false;
        }
    }

    @Override
    public String getDefaultLineSeparator() {
        Preferences rootNode = Platform.getPreferencesService().getRootNode();
        // if the file does not exist or has no content yet, try with project preference
        String value = getLineSeparatorFromPreferences(rootNode.node(ProjectScope.SCOPE).node(getProject().getName()));
        if (value != null) {
            return value;
        }
        // try with instance preferences
        value = getLineSeparatorFromPreferences(rootNode.node(InstanceScope.SCOPE));
        if (value != null) {
            return value;
        }
        // try with default preferences
        value = getLineSeparatorFromPreferences(rootNode.node(DefaultScope.SCOPE));
        if (value != null) {
            return value;
        }
        // if there is no preference set, fall back to OS default value
        return System.lineSeparator();
    }

    private static String getLineSeparatorFromPreferences(Preferences node) {
        try {
            // be careful looking up for our node so not to create any nodes as side effect
            if (node.nodeExists(Platform.PI_RUNTIME)) {
                return node.node(Platform.PI_RUNTIME).get(Platform.PREF_LINE_SEPARATOR, null);
            }
        } catch (com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.prefs.BackingStoreException e) {
            // ignore
        }
        return null;
    }
}
