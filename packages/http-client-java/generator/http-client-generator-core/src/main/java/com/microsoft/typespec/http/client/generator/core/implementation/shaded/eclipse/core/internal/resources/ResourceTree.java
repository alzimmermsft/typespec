/*******************************************************************************
 * Copyright (c) 2000, 2025 IBM Corporation and others.
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
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.filesystem.*;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.filesystem.URIUtil;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.localstore.FileSystemResourceManager;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.utils.*;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.*;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.team.IResourceTree;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.*;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.jobs.ILock;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.util.NLS;
import java.net.URI;

/**
 * @since 2.0
 *
 * Implementation note: Since the move/delete hook involves running third
 * party code, the workspace lock is not held. This means the workspace
 * lock must be re-acquired whenever we need to manipulate the workspace
 * in any way. All entry points from third party code back into the tree must
 * be done in an acquire/release pair.
 */
class ResourceTree implements IResourceTree {
    private boolean isValid = true;
    private final FileSystemResourceManager localManager;
    /**
     * The lock to acquire when the workspace needs to be manipulated
     */
    private final ILock lock;
    private final MultiStatus multistatus;
    private final int updateFlags;

    /**
     * Constructor for this class.
     */
    public ResourceTree(FileSystemResourceManager localManager, ILock lock, MultiStatus status, int updateFlags) {
        super();
        this.localManager = localManager;
        this.lock = lock;
        this.multistatus = status;
        this.updateFlags = updateFlags;
    }

    /**
     * @see IResourceTree#addToLocalHistory(IFile)
     */
    @Override
    public void addToLocalHistory(IFile file) {
        if (!FileSystemResourceManager.storeHistory(file)) {
            return;
        }
        Assert.isLegal(isValid);
        try {
            lock.acquire();
            if (!file.exists()) {
                return;
            }
            IFileStore store = localManager.getStore(file);
            final IFileInfo fileInfo = store.fetchInfo();
            if (!fileInfo.exists()) {
                return;
            }
            localManager.getHistoryStore().addState(file.getFullPath(), store, fileInfo, false);
        } finally {
            lock.release();
        }
    }

    private IFileStore computeDestinationStore(IProjectDescription destDescription) throws CoreException {
        URI destLocation = destDescription.getLocationURI();
        // Use the default area if necessary for the destination.
        if (destLocation == null) {
            IPath rootLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation();
            destLocation = rootLocation.append(destDescription.getName()).toFile().toURI();
        }
        return EFS.getStore(destLocation);
    }

    /**
     * @see IResourceTree#computeTimestamp(IFile)
     */
    @Override
    public long computeTimestamp(IFile file) {
        Assert.isLegal(isValid);
        try {
            lock.acquire();
            if (!file.getProject().exists()) {
                return NULL_TIMESTAMP;
            }
            return internalComputeTimestamp(file);
        } finally {
            lock.release();
        }
    }

    /**
     * Copies the local history of source to destination. Note that if source
     * is an IFolder, it is assumed that the same structure exists under destination
     * and the local history of any IFile under source will be copied to the
     * associated IFile under destination.
     */
    private void copyLocalHistory(IResource source, IResource destination) {
        localManager.getHistoryStore().copyHistory(source, destination, true);
    }

    /**
     * @see IResourceTree#deletedFile(IFile)
     */
    @Override
    public void deletedFile(IFile file) {
        Assert.isLegal(isValid);
        try {
            lock.acquire();
            // Do nothing if the resource doesn't exist.
            if (!file.exists()) {
                return;
            }
            try {
                // Delete properties, generate marker deltas, and remove the node from the workspace tree.
                ((Resource) file).deleteResource(true, null);
            } catch (CoreException e) {
                String message = NLS.bind(Messages.resources_errorDeleting, file.getFullPath());
                IStatus status = new ResourceStatus(IStatus.ERROR, file.getFullPath(), message, e);
                failed(status);
            }
        } finally {
            lock.release();
        }
    }

    /**
     * @see IResourceTree#deletedFolder(IFolder)
     */
    @Override
    public void deletedFolder(IFolder folder) {
        Assert.isLegal(isValid);
        try {
            lock.acquire();
            // Do nothing if the resource doesn't exist.
            if (!folder.exists()) {
                return;
            }
            try {
                // Delete properties, generate marker deltas, and remove the node from the workspace tree.
                ((Resource) folder).deleteResource(true, null);
            } catch (CoreException e) {
                String message = NLS.bind(Messages.resources_errorDeleting, folder.getFullPath());
                IStatus status = new ResourceStatus(IStatus.ERROR, folder.getFullPath(), message, e);
                failed(status);
            }
        } finally {
            lock.release();
        }
    }

    /**
     * @see IResourceTree#deletedProject(IProject)
     */
    @Override
    public void deletedProject(IProject target) {
        Assert.isLegal(isValid);
        try {
            lock.acquire();
            // Do nothing if the resource doesn't exist.
            if (!target.exists()) {
                return;
            }
            // Delete properties, generate marker deltas, and remove the node from the workspace tree.
            try {
                ((Project) target).deleteResource(false, null);
            } catch (CoreException e) {
                String message = NLS.bind(Messages.resources_errorDeleting, target.getFullPath());
                IStatus status = new ResourceStatus(IStatus.ERROR, target.getFullPath(), message, e);
                // log the status but don't return until we try and delete the rest of the project info
                failed(status);
            }
        } finally {
            lock.release();
        }
    }

    /**
     * Makes sure that the destination directory for a project move is unoccupied.
     * Returns true if successful, and false if the move should be aborted
     */
    private boolean ensureDestinationEmpty(IProject source, IFileStore destinationStore, IProgressMonitor monitor)
        throws CoreException {
        String message;
        // Make sure the destination location is unoccupied
        if (!destinationStore.fetchInfo().exists()) {
            return true;
        }
        // check for existing children
        if (destinationStore.childNames(EFS.NONE, Policy.subMonitorFor(monitor, 0)).length > 0) {
            // allow case rename to proceed
            if (((Resource) source).getStore().equals(destinationStore)) {
                return true;
            }
            // fail because the destination is occupied
            message = NLS.bind(Messages.localstore_resourceExists, destinationStore);
            IStatus status = new ResourceStatus(IStatus.ERROR, source.getFullPath(), message, null);
            failed(status);
            return false;
        }
        // delete the destination directory to allow for efficient renaming
        destinationStore.delete(EFS.NONE, Policy.subMonitorFor(monitor, 0));
        return true;
    }

    /**
     * This operation has failed for the given reason. Add it to this
     * resource tree's status.
     */
    @Override
    public void failed(IStatus reason) {
        Assert.isLegal(isValid);
        multistatus.add(reason);
    }

    /**
     * Returns the status object held onto by this resource tree.
     */
    protected IStatus getStatus() {
        return multistatus;
    }

    /**
     * @see IResourceTree#getTimestamp(IFile)
     */
    @Override
    public long getTimestamp(IFile file) {
        Assert.isLegal(isValid);
        try {
            lock.acquire();
            if (!file.exists()) {
                return NULL_TIMESTAMP;
            }
            ResourceInfo info = ((File) file).getResourceInfo(false, false);
            return info == null ? NULL_TIMESTAMP : info.getLocalSyncInfo();
        } finally {
            lock.release();
        }
    }

    /**
     * Returns the local timestamp for a file.
     *
     * @return The local file system timestamp
     */
    private long internalComputeTimestamp(IFile file) {
        IFileInfo fileInfo = localManager.getStore(file).fetchInfo();
        return fileInfo.exists() ? fileInfo.getLastModified() : NULL_TIMESTAMP;
    }

    /**
     * Helper method for #standardDeleteFile. Returns a boolean indicating whether or
     * not the delete was successful.
     */
    private boolean internalDeleteFile(IFile file, int flags, IProgressMonitor monitor) {
        try {
            String message = NLS.bind(Messages.resources_deleting, file.getFullPath());
            monitor.beginTask(message, Policy.totalWork);
            Policy.checkCanceled(monitor);

            // Do nothing if the file doesn't exist in the workspace.
            if (!file.exists()) {
                // Indicate that the delete was successful.
                return true;
            }
            // Don't delete contents if this is a linked resource
            if (file.isLinked()) {
                deletedFile(file);
                return true;
            }
            // If the file doesn't exist on disk then signal to the workspace to delete the
            // file and return.
            IFileStore fileStore = localManager.getStore(file);
            boolean localExists = fileStore.fetchInfo().exists();
            if (!localExists) {
                deletedFile(file);
                // Indicate that the delete was successful.
                return true;
            }

            boolean keepHistory = (flags & IResource.KEEP_HISTORY) != 0;
            boolean force = (flags & IResource.FORCE) != 0;

            // Add the file to the local history if requested by the user.
            if (keepHistory) {
                addToLocalHistory(file);
            }
            monitor.worked(Policy.totalWork / 4);

            // We want to fail if force is false and the file is not synchronized with the
            // local file system.
            if (!force) {
                boolean inSync = isSynchronized(file, IResource.DEPTH_ZERO);
                // only want to fail if the file still exists.
                if (!inSync && localExists) {
                    message = NLS.bind(Messages.localstore_resourceIsOutOfSync, file.getFullPath());
                    IStatus status = new ResourceStatus(IResourceStatus.OUT_OF_SYNC_LOCAL, file.getFullPath(), message);
                    failed(status);
                    // Indicate that the delete was unsuccessful.
                    return false;
                }
            }
            monitor.worked(Policy.totalWork / 4);

            // Try to delete the file from the file system.
            try {
                fileStore.delete(EFS.NONE, Policy.subMonitorFor(monitor, Policy.totalWork / 4));
                // If the file was successfully deleted from the file system the
                // workspace tree should be updated accordingly.
                deletedFile(file);
                // Indicate that the delete was successful.
                return true;
            } catch (CoreException e) {
                message = NLS.bind(Messages.resources_couldnotDelete, fileStore.toString());
                IStatus status
                    = new ResourceStatus(IResourceStatus.FAILED_DELETE_LOCAL, file.getFullPath(), message, e);
                failed(status);
            }
            // Indicate that the delete was unsuccessful.
            return false;
        } finally {
            monitor.done();
        }
    }

    /**
     * Helper method for #standardDeleteFolder. Returns a boolean indicating
     * whether or not the deletion of this folder was successful. Does a best effort
     * delete of this resource and its children.
     */
    private boolean internalDeleteFolder(IFolder folder, int flags, IProgressMonitor monitor) {
        String message = NLS.bind(Messages.resources_deleting, folder.getFullPath());
        SubMonitor subMonitor = SubMonitor.convert(monitor, Policy.totalWork).checkCanceled();
        subMonitor.subTask(message);

        // Do nothing if the folder doesn't exist in the workspace.
        if (!folder.exists()) {
            return true;
        }

        // Don't delete contents if this is a linked resource
        if (folder.isLinked()) {
            deletedFolder(folder);
            return true;
        }

        // If the folder doesn't exist on disk then update the tree and return.
        IFileStore fileStore = localManager.getStore(folder);
        if (!fileStore.fetchInfo().exists()) {
            deletedFolder(folder);
            return true;
        }

        try {
            // this will delete local and workspace
            localManager.delete(folder, flags, subMonitor.split(Policy.totalWork, SubMonitor.SUPPRESS_NONE));
        } catch (CoreException ce) {
            message = NLS.bind(Messages.localstore_couldnotDelete, folder.getFullPath());
            IStatus status = new ResourceStatus(IStatus.ERROR, IResourceStatus.FAILED_DELETE_LOCAL,
                folder.getFullPath(), message, ce);
            failed(status);
            return false;
        }
        return true;
    }

    /**
     * Does a best-effort delete on this resource and all its children.
     */
    private boolean internalDeleteProject(IProject project, int flags, IProgressMonitor monitor) {
        // Recursively delete each member of the project.
        IResource[] members = null;
        try {
            members = project.members(IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS | IContainer.INCLUDE_HIDDEN);
        } catch (CoreException e) {
            String message = NLS.bind(Messages.resources_errorMembers, project.getFullPath());
            IStatus status = new ResourceStatus(IStatus.ERROR, project.getFullPath(), message, e);
            failed(status);
            // Indicate that the delete was unsuccessful.
            return false;
        }
        boolean deletedChildren = true;
        for (IResource member : members) {
            IResource child = member;
            switch (child.getType()) {
                case IResource.FILE:
                    // ignore the .project file for now and delete it last
                    if (!IProjectDescription.DESCRIPTION_FILE_NAME.equals(child.getName())) {
                        deletedChildren &= internalDeleteFile((IFile) child, flags,
                            Policy.subMonitorFor(monitor, Policy.totalWork / members.length));
                    }
                    break;

                case IResource.FOLDER:
                    deletedChildren &= internalDeleteFolder((IFolder) child, flags,
                        Policy.subMonitorFor(monitor, Policy.totalWork / members.length));
                    break;
            }
        }
        IFileStore projectStore = localManager.getStore(project);
        // Check to see if the children were deleted ok. If there was a problem
        // just return as the problem should have been logged by the recursive
        // call to the child.
        if (!deletedChildren) {
            // Indicate that the delete was unsuccessful.
            return false;
        }

        // Check if there are any undiscovered children of the project on disk other than description file
        String[] children;
        try {
            children = projectStore.childNames(EFS.NONE, null);
        } catch (CoreException e) {
            // treat failure to access the directory as a non-existent directory
            children = new String[0];
        }
        boolean force = BitMask.isSet(flags, IResource.FORCE);
        if (!force && (children.length != 1 || !IProjectDescription.DESCRIPTION_FILE_NAME.equals(children[0]))) {
            String message = NLS.bind(Messages.localstore_resourceIsOutOfSync, project.getName());
            failed(new ResourceStatus(IResourceStatus.OUT_OF_SYNC_LOCAL, project.getFullPath(), message));
            return false;
        }

        // Now delete the project description file
        IResource file = project.findMember(IProjectDescription.DESCRIPTION_FILE_NAME);
        if (file == null) {
            // the .project have may have been recreated on disk automatically by snapshot
            IFileStore dotProject = projectStore.getChild(IProjectDescription.DESCRIPTION_FILE_NAME);
            try {
                dotProject.delete(EFS.NONE, null);
            } catch (CoreException e) {
                failed(e.getStatus());
            }
        } else {
            boolean deletedProjectFile = internalDeleteFile((IFile) file, flags, Policy.monitorFor(null));
            if (!deletedProjectFile) {
                String message = NLS.bind(Messages.resources_couldnotDelete, file.getFullPath());
                IStatus status = new ResourceStatus(IResourceStatus.FAILED_DELETE_LOCAL, file.getFullPath(), message);
                failed(status);
                // Indicate that the delete was unsuccessful.
                return false;
            }
        }

        // children are deleted, so now delete the parent
        try {
            projectStore.delete(EFS.NONE, null);
            deletedProject(project);
            // Indicate that the delete was successful.
            return true;
        } catch (CoreException e) {
            String message = NLS.bind(Messages.resources_couldnotDelete, projectStore.toString());
            IStatus status = new ResourceStatus(IResourceStatus.FAILED_DELETE_LOCAL, project.getFullPath(), message, e);
            failed(status);
            // Indicate that the delete was unsuccessful.
            return false;
        }
    }

    /**
     * Return <code>true</code> if there is a change in the content area for the project.
     */
    private boolean isContentChange(IProject project, IProjectDescription destDescription) {
        IProjectDescription srcDescription = ((Project) project).internalGetDescription();
        URI srcLocation = srcDescription.getLocationURI();
        URI destLocation = destDescription.getLocationURI();
        if (srcLocation == null || destLocation == null) {
            return true;
        }
        // don't use URIUtil because we want to treat case rename as a content change
        return !srcLocation.equals(destLocation);
    }

    /**
     * Return <code>true</code> if there is a change in the name of the project.
     */
    private boolean isNameChange(IProject project, IProjectDescription description) {
        return !project.getName().equals(description.getName());
    }

    /**
     * Refreshes the resource hierarchy with its children. In case of failure
     * adds an appropriate status to the resource tree's status.
     */
    private void safeRefresh(IResource resource) {
        try {
            resource.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
        } catch (CoreException ce) {
            IStatus status = new ResourceStatus(IStatus.ERROR, IResourceStatus.FAILED_DELETE_LOCAL,
                resource.getFullPath(), Messages.refresh_refreshErr, ce);
            failed(status);
        }
    }

    /**
     * @see IResourceTree#isSynchronized(IResource, int)
     */
    @Override
    public boolean isSynchronized(IResource resource, int depth) {
        try {
            lock.acquire();
            return localManager.isSynchronized(resource, depth);
        } finally {
            lock.release();
        }
    }

    /**
     * The specific operation for which this tree was created has completed and this tree
     * should not be used anymore. Ensure that this is the case by making it invalid. This
     * is checked by all API methods.
     */
    void makeInvalid() {
        this.isValid = false;
    }

    /**
     * Helper method for moving the project content. Determines the content location
     * based on the project description. (default location or user defined?)
     */
    private void moveProjectContent(IProject source, IFileStore destStore, int flags, IProgressMonitor monitor)
        throws CoreException {
        try {
            String message = NLS.bind(Messages.resources_moving, source.getFullPath());
            monitor.beginTask(message, 10);
            IProjectDescription srcDescription = source.getDescription();
            URI srcLocation = srcDescription.getLocationURI();
            // If the locations are the same (and non-default) then there is nothing to do.
            if (srcLocation != null && URIUtil.equals(srcLocation, destStore.toURI())) {
                return;
            }

            // If this is a replace, just make sure the destination location exists, and return
            boolean replace = (flags & IResource.REPLACE) != 0;
            if (replace) {
                destStore.mkdir(EFS.NONE, Policy.subMonitorFor(monitor, 10));
                return;
            }

            // Move the contents on disk.
            localManager.move(source, destStore, Policy.subMonitorFor(monitor, 9));

            // if this is a deep move, move the contents of any linked resources
            if ((flags & IResource.SHALLOW) == 0) {
                IResource[] children = source.members();
                for (IResource element : children) {
                    if (element.isLinked()) {
                        message = NLS.bind(Messages.resources_moving, element.getFullPath());
                        monitor.subTask(message);
                        IFileStore linkDestination = destStore.getChild(element.getName());
                        try {
                            localManager.move(element, linkDestination, Policy.monitorFor(null));
                        } catch (CoreException ce) {
                            // log the failure, but keep trying on remaining links
                            failed(ce.getStatus());
                        }
                    }
                }
            }
            monitor.worked(1);
        } finally {
            monitor.done();
        }
    }

    /**
     * @see IResourceTree#standardDeleteFile(IFile, int, IProgressMonitor)
     */
    @Override
    public void standardDeleteFile(IFile file, int flags, IProgressMonitor monitor) {
        Assert.isLegal(isValid);
        try {
            lock.acquire();
            internalDeleteFile(file, flags, monitor);
        } finally {
            lock.release();
        }
    }

    @Override
    public void standardDeleteFolder(IFolder folder, int flags, IProgressMonitor monitor) {
        Assert.isLegal(isValid);
        try {
            lock.acquire();
            internalDeleteFolder(folder, flags, monitor);
        } catch (OperationCanceledException oce) {
            safeRefresh(folder);
            throw oce;
        } finally {
            lock.release();
            monitor.done();
        }
    }

    /**
     * @see IResourceTree#standardDeleteProject(IProject, int, IProgressMonitor)
     */
    @Override
    public void standardDeleteProject(IProject project, int flags, IProgressMonitor monitor) {
        Assert.isLegal(isValid);
        try {
            lock.acquire();
            String message = NLS.bind(Messages.resources_deleting, project.getFullPath());
            monitor.beginTask(message, Policy.totalWork);
            // Do nothing if the project doesn't exist in the workspace tree.
            if (!project.exists()) {
                return;
            }

            boolean alwaysDeleteContent = (flags & IResource.ALWAYS_DELETE_PROJECT_CONTENT) != 0;
            boolean neverDeleteContent = (flags & IResource.NEVER_DELETE_PROJECT_CONTENT) != 0;
            boolean success = true;

            // Delete project content. Don't do anything if the user specified explicitly asked
            // not to delete the project content or if the project is closed and
            // ALWAYS_DELETE_PROJECT_CONTENT was not specified.
            if (alwaysDeleteContent || (project.isOpen() && !neverDeleteContent)) {
                // Force is implied if alwaysDeleteContent is true or if the project is in sync
                // with the local file system.
                if (alwaysDeleteContent || isSynchronized(project, IResource.DEPTH_INFINITE)) {
                    flags |= IResource.FORCE;
                }

                // If the project is open we have to recursively try and delete all the files doing best-effort.
                if (project.isOpen()) {
                    success = internalDeleteProject(project, flags, monitor);
                    if (!success) {
                        IFileStore store = localManager.getStore(project);
                        message = NLS.bind(Messages.resources_couldnotDelete, store.toString());
                        IStatus status
                            = new ResourceStatus(IResourceStatus.FAILED_DELETE_LOCAL, project.getFullPath(), message);
                        failed(status);
                    }
                    return;
                }

                // If the project is closed we can short circuit this operation and delete all the files on disk.
                // The .project file is deleted at the end of the operation.
                try {
                    IFileStore projectStore = localManager.getStore(project);
                    IFileStore members[] = projectStore.childStores(EFS.NONE, null);
                    for (IFileStore member : members) {
                        if (!IProjectDescription.DESCRIPTION_FILE_NAME.equals(member.getName())) {
                            member.delete(EFS.NONE,
                                Policy.subMonitorFor(monitor, Policy.totalWork * 7 / 8 / members.length));
                        }
                    }
                    projectStore.delete(EFS.NONE, Policy.subMonitorFor(monitor,
                        Policy.totalWork * 7 / 8 / (members.length > 0 ? members.length : 1)));
                } catch (OperationCanceledException oce) {
                    safeRefresh(project);
                    throw oce;
                } catch (CoreException ce) {
                    message = NLS.bind(Messages.localstore_couldnotDelete, project.getFullPath());
                    IStatus status = new ResourceStatus(IStatus.ERROR, IResourceStatus.FAILED_DELETE_LOCAL,
                        project.getFullPath(), message, ce);
                    failed(status);
                    return;
                }
            }

            // Signal that the workspace tree should be updated that the project has been deleted.
            if (success) {
                deletedProject(project);
            } else {
                message = NLS.bind(Messages.localstore_couldnotDelete, project.getFullPath());
                IStatus status
                    = new ResourceStatus(IResourceStatus.FAILED_DELETE_LOCAL, project.getFullPath(), message);
                failed(status);
            }
        } finally {
            lock.release();
            monitor.done();
        }
    }

    /**
     * @see IResourceTree#updateMovedFileTimestamp(IFile, long)
     */
    @Override
    public void updateMovedFileTimestamp(IFile file, long timestamp) {
        Assert.isLegal(isValid);
        try {
            lock.acquire();
            // Do nothing if the file doesn't exist in the workspace tree.
            if (!file.exists()) {
                return;
            }
            // Update the timestamp in the tree.
            ResourceInfo info = ((Resource) file).getResourceInfo(false, true);
            // The info should never be null since we just checked that the resource exists in the tree.
            localManager.updateLocalSync(info, timestamp);
            // remove the linked bit since this resource has been moved in the file system
            info.clear(ICoreConstants.M_LINK);
        } finally {
            lock.release();
        }
    }

    /**
     * Helper method to update all the timestamps in the tree to match
     * those in the file system. Used after a #move.
     */
    private void updateTimestamps(IResource root, final boolean isDeep) {
        IResourceVisitor visitor = resource -> {
            if (resource.isLinked()) {
                if (isDeep && !((Resource) resource).isUnderVirtual()) {
                    // clear the linked resource bit, if any
                    ResourceInfo info = ((Resource) resource).getResourceInfo(false, true);
                    info.clear(ICoreConstants.M_LINK);
                }
                return true;
            }
            // only needed if underlying file system does not preserve timestamps
            // if (resource.getType() == IResource.FILE) {
            // IFile file = (IFile) resource;
            // updateMovedFileTimestamp(file, computeTimestamp(file));
            // }
            return true;
        };
        try {
            root.accept(visitor, IResource.DEPTH_INFINITE,
                IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS | IContainer.INCLUDE_HIDDEN);
        } catch (CoreException e) {
            // No exception should be thrown.
        }
    }
}
