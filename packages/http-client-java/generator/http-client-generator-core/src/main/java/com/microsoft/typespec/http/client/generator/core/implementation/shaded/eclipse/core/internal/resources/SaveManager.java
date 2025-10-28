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
 *     Francis Lynch (Wind River) - [301563] Save and load tree snapshots
 *     Francis Lynch (Wind River) - [305718] Allow reading snapshot into renamed project
 *     Baltasar Belyavsky (Texas Instruments) - [361675] Order mismatch when saving/restoring workspace trees
 *     Broadcom Corporation - ongoing development
 *     Sergey Prigogin (Google) - [437005] Out-of-date .snap file prevents Eclipse from running
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 473427
 *     Mickael Istria (Red Hat Inc.) - Bug 488937
 *     Christoph Läubrich - Issue #77 - SaveManager access the ResourcesPlugin.getWorkspace at init phase
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.events.BuilderPersistentInfo;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.events.ResourceComparator;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.localstore.SafeChunkyInputStream;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.localstore.SafeFileInputStream;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.localstore.SafeFileOutputStream;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.utils.IStringPoolParticipant;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.utils.Messages;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.utils.Policy;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.utils.StringPool;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.watson.ElementTree;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.watson.ElementTreeWriter;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.watson.IElementInfoFlattener;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IContainer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResourceStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.ISaveParticipant;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IWorkspaceRoot;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.ResourcesPlugin;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Assert;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.MultiStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Platform;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Status;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.SubMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.util.NLS;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SaveManager implements IElementInfoFlattener, IStringPoolParticipant {
    static class MasterTable extends Properties {
        private static final long serialVersionUID = 1L;

        @Override
        public synchronized Object put(Object key, Object value) {
            Object prev = super.put(key, value);
            if (prev != null && ROOT_SEQUENCE_NUMBER_KEY.equals(key)) {
                int prevSeqNum = Integer.parseInt((String) prev);
                int currSeqNum = Integer.parseInt((String) value);
                if (prevSeqNum > currSeqNum) {
                    // revert last put operation
                    super.put(key, prev);
                    // notify about the problem, do not throw exception but add the exception to know where it occurred
                    String message = "Cannot set lower sequence number for root (previous: " + prevSeqNum + ", new: " //$NON-NLS-1$ //$NON-NLS-2$
                        + currSeqNum + "). Ignoring the new value.";  //$NON-NLS-1$
                    Policy.log(new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, IResourceStatus.INTERNAL_ERROR,
                        message, new IllegalArgumentException(message)));
                }
            }
            return prev;
        }
    }

    protected static final String ROOT_SEQUENCE_NUMBER_KEY = IPath.ROOT + LocalMetaArea.F_TREE;
    protected static final String CLEAR_DELTA_PREFIX = "clearDelta_"; //$NON-NLS-1$
    protected static final String DELTA_EXPIRATION_PREFIX = "deltaExpiration_"; //$NON-NLS-1$

    /** constants */
    protected static final String SAVE_NUMBER_PREFIX = "saveNumber_"; //$NON-NLS-1$
    protected ElementTree lastSnap;
    protected final MasterTable masterTable;

    /**
     * The number of non-trivial operations since the last snapshot.
     */
    protected int operationCount = 0;

    /**
     * In-memory representation of plugins saved state. Maps String (plugin id)-&gt; SavedState.
     * This map is accessed from API that is not synchronized, so it requires
     * independent synchronization. This is accomplished using a synchronized
     * wrapper map.
     */
    protected Map<String, SavedState> savedStates;

    /**
     * Ids of plugins that participate on a workspace save. Maps String (plugin id)-&gt; ISaveParticipant.
     * This map is accessed from API that is not synchronized, so it requires
     * independent synchronization. This is accomplished using a synchronized
     * wrapper map.
     */
    protected Map<String, ISaveParticipant> saveParticipants;

    protected volatile boolean snapshotRequested;
    protected Workspace workspace;
    // declare debug messages as fields to get sharing
    private static final int TREE_BUFFER_SIZE = 1024 * 64;// 64KB buffer

    public SaveManager(Workspace workspace) {
        this.workspace = workspace;
        this.masterTable = new MasterTable();
        snapshotRequested = false;
        saveParticipants = Collections.synchronizedMap(new HashMap<>(10));
    }

    /**
     * Remove the delta expiration timestamp from the master table, either
     * because the saved state has been processed, or the delta has expired.
     */
    protected void clearDeltaExpiration(String pluginId) {
        masterTable.remove(DELTA_EXPIRATION_PREFIX + pluginId);
    }

    protected void commit(Map<String, SaveContext> contexts) throws CoreException {
        for (SaveContext saveContext : contexts.values()) {
            saveContext.commit();
        }
    }

    /**
     * Used in the policy for cleaning up tree's of plug-ins that are not often activated.
     */
    protected long getDeltaExpiration(String pluginId) {
        String result = masterTable.getProperty(DELTA_EXPIRATION_PREFIX + pluginId);
        return (result == null) ? System.currentTimeMillis() : Long.parseLong(result);
    }

    protected Properties getMasterTable() {
        return masterTable;
    }

    public int getSaveNumber(String pluginId) {
        String value = masterTable.getProperty(SAVE_NUMBER_PREFIX + pluginId);
        return (value == null) ? 0 : Integer.parseInt(value);
    }

    /**
     * Initializes the snapshot mechanism for this workspace.
     */
    protected void initSnap() {
        // Discard any pending snapshot request.
        snapshotJob.cancel();
        // The "lastSnap" tree must be frozen as the exact tree obtained from startup,
        // otherwise ensuing snapshot deltas may be based on an incorrect tree (see bug 12575).
        lastSnap = workspace.getElementTree();
        lastSnap.immutable();
        workspace.newWorkingTree();
        operationCount = 0;
        // Delete the snapshot files, if any.
        IPath location = workspace.getMetaArea().getSnapshotLocationFor(workspace.getRoot());
        File target = location.toFile().getParentFile();
        FilenameFilter filter = (dir, name) -> {
            if (!name.endsWith(LocalMetaArea.F_SNAP)) {
                return false;
            }
            for (int i = 0; i < name.length() - LocalMetaArea.F_SNAP.length(); i++) {
                char c = name.charAt(i);
                if (c < '0' || c > '9') {
                    return false;
                }
            }
            return true;
        };
        String[] candidates = target.list(filter);
        if (candidates != null) {
            removeFiles(target, candidates, Collections.<String>emptyList());
        }
    }

    protected boolean isDeltaCleared(String pluginId) {
        String clearDelta = masterTable.getProperty(CLEAR_DELTA_PREFIX + pluginId);
        return clearDelta != null && clearDelta.equals("true"); //$NON-NLS-1$
    }

    protected boolean isOldPluginTree(String pluginId) {
        // first, check if this plug-ins was marked not to receive a delta
        if (isDeltaCleared(pluginId)) {
            return false;
        }
        // see if the plugin is still installed
        if (Platform.getBundle(pluginId) == null) {
            return true;
        }

        // finally see if the delta has past its expiry date
        long deltaAge = System.currentTimeMillis() - getDeltaExpiration(pluginId);
        return deltaAge > workspace.internalGetDescription().getDeltaExpiration();
    }

    /**
     * @see IElementInfoFlattener#readElement(IPath, DataInput)
     */
    @Override
    public Object readElement(IPath path, DataInput input) throws IOException {
        Assert.isNotNull(path);
        Assert.isNotNull(input);
        // read the flags and pull out the type.
        int flags = input.readInt();
        int type = (flags & ICoreConstants.M_TYPE) >> ICoreConstants.M_TYPE_START;
        ResourceInfo info = workspace.newElement(type);
        info.readFrom(flags, input);
        return info;
    }

    protected void removeFiles(File root, String[] candidates, List<String> exclude) {
        for (String candidate : candidates) {
            boolean delete = true;
            for (ListIterator<String> it = exclude.listIterator(); it.hasNext();) {
                String s = it.next();
                if (s.equals(candidate)) {
                    it.remove();
                    delete = false;
                    break;
                }
            }
            if (delete) {
                new File(root, candidate).delete();
            }
        }
    }

    public void requestSnapshot() {
        snapshotRequested = true;
    }

    /**
     * Restores the state of this workspace by opening the projects
     * which were open when it was last saved.
     */
    protected void restore(IProgressMonitor monitor) throws CoreException {
        monitor = Policy.monitorFor(monitor);
        try {
            monitor.beginTask("", 50); //$NON-NLS-1$
            // need to open the tree to restore, but since we're not
            // inside an operation, be sure to close it afterwards
            workspace.newWorkingTree();
            try {
                String msg = Messages.resources_startupProblems;
                MultiStatus problems
                    = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.FAILED_READ_METADATA, msg, null);

                restoreMasterTable();
                // restore the saved tree and overlay the snapshots if any
                restoreTree(Policy.subMonitorFor(monitor, 10));
                restoreSnapshots(Policy.subMonitorFor(monitor, 10));

                // tolerate failure for non-critical information
                // if startup fails, the entire workspace is shot
                try {
                    restoreMarkers(workspace.getRoot(), false, Policy.subMonitorFor(monitor, 10));
                } catch (CoreException e) {
                    problems.merge(e.getStatus());
                }
                try {
                    restoreSyncInfo(workspace.getRoot(), Policy.subMonitorFor(monitor, 10));
                } catch (CoreException e) {
                    problems.merge(e.getStatus());
                }
                // restore meta info last because it might close a project if its description is not readable
                restoreMetaInfo(problems, Policy.subMonitorFor(monitor, 10));
                IProject[] roots = workspace.getRoot().getProjects(IContainer.INCLUDE_HIDDEN);
                for (IProject root : roots) {
                    ((Project) root).startup();
                }
                if (!problems.isOK()) {
                    Policy.log(problems);
                }
            } finally {
                workspace.getElementTree().immutable();
            }
        } finally {
            monitor.done();
        }
    }

    /**
     * Restores the contents of this project. Throw
     * an exception if the project could not be restored.
     * 
     * @return <code>true</code> if the project data was restored successfully,
     * and <code>false</code> if non-critical problems occurred while restoring.
     * @exception CoreException if the project could not be restored.
     */
    protected boolean restore(Project project, IProgressMonitor monitor) throws CoreException {
        boolean status = true;
        monitor = Policy.monitorFor(monitor);
        try {
            monitor.beginTask("", 40); //$NON-NLS-1$
            if (project.isOpen()) {
                status = restoreTree(project, Policy.subMonitorFor(monitor, 10));
            } else {
                monitor.worked(10);
            }
            restoreMarkers(project, true, Policy.subMonitorFor(monitor, 10));
            restoreSyncInfo(project, Policy.subMonitorFor(monitor, 10));
            // restore meta info last because it might close a project if its description is not found
            restoreMetaInfo(project, Policy.subMonitorFor(monitor, 10));
        } finally {
            monitor.done();
        }
        return status;
    }

    /**
     * Restores the contents of this project from a refresh snapshot, if possible.
     * Throws an exception if the snapshot is found but an error occurs when reading
     * the file.
     * 
     * @return <code>true</code> if the project data was restored successfully,
     * and <code>false</code> if the refresh snapshot was not found or could not be opened.
     * @exception CoreException if an error occurred reading the snapshot file.
     */
    protected boolean restoreFromRefreshSnapshot(Project project, IProgressMonitor monitor) throws CoreException {
        boolean status;
        IPath snapshotPath = workspace.getMetaArea().getRefreshLocationFor(project);
        File snapshotFile = snapshotPath.toFile();
        if (!snapshotFile.exists()) {
            return false;
        }
        monitor = Policy.monitorFor(monitor);
        try {
            monitor.beginTask("", 40); //$NON-NLS-1$
            status = restoreTreeFromRefreshSnapshot(project, snapshotFile, Policy.subMonitorFor(monitor, 40));
            if (status) {
                // load the project description and set internal description
                ProjectDescription description = workspace.getFileSystemManager().read(project, true);
                project.internalSetDescription(description, false);
                workspace.getMetaArea().clearRefresh(project);
            }
        } finally {
            monitor.done();
        }
        return status;
    }

    /**
     * Reads the markers which were originally saved
     * for the tree rooted by the given resource.
     */
    protected void restoreMarkers(IResource resource, boolean generateDeltas, IProgressMonitor monitor)
        throws CoreException {
        Assert.isLegal(resource.getType() == IResource.ROOT || resource.getType() == IResource.PROJECT);
        MarkerManager markerManager = workspace.getMarkerManager();
        // when restoring a project, only load markers if it is open
        if (resource.isAccessible()) {
            markerManager.restore(resource, generateDeltas, monitor);
        }

        // if we have the workspace root then restore markers for its projects
        if (resource.getType() == IResource.PROJECT) {
            return;
        }
        IProject[] projects = ((IWorkspaceRoot) resource).getProjects(IContainer.INCLUDE_HIDDEN);
        for (IProject project : projects) {
            if (project.isAccessible()) {
                markerManager.restore(project, generateDeltas, monitor);
            }
        }
    }

    protected void restoreMasterTable() throws CoreException {
        masterTable.clear();
        IPath location = workspace.getMetaArea().getSafeTableLocationFor(ResourcesPlugin.PI_RESOURCES);
        File target = location.toFile();
        if (!target.exists()) {
            location = workspace.getMetaArea().getBackupLocationFor(location);
            target = location.toFile();
            if (!target.exists()) {
                return;
            }
        }
        try (SafeChunkyInputStream input = new SafeChunkyInputStream(target)) {
            masterTable.load(input);
        } catch (IOException e) {
            String message = Messages.resources_exMasterTable;
            throw new ResourceException(IResourceStatus.INTERNAL_ERROR, null, message, e);
        }
    }

    /**
     * Restores the state of this workspace by opening the projects
     * which were open when it was last saved.
     */
    protected void restoreMetaInfo(MultiStatus problems, IProgressMonitor monitor) {
        IProject[] roots = workspace.getRoot().getProjects(IContainer.INCLUDE_HIDDEN);
        for (IProject root : roots) {
            // fatal to throw exceptions during startup
            try {
                restoreMetaInfo((Project) root, monitor);
            } catch (CoreException e) {
                String message = NLS.bind(Messages.resources_readMeta, root.getName());
                problems
                    .merge(new ResourceStatus(IResourceStatus.FAILED_READ_METADATA, root.getFullPath(), message, e));
            }
        }
    }

    /**
     * Restores the contents of this project. Throw an exception if the
     * project description could not be restored.
     */
    protected void restoreMetaInfo(Project project, IProgressMonitor monitor) throws CoreException {
        ProjectDescription description = null;
        CoreException failure = null;
        try {
            if (project.isOpen()) {
                description = workspace.getFileSystemManager().read(project, true);
            } else {
                // for closed projects, just try to read the legacy .prj file,
                // because the project location is stored there.
                description = workspace.getMetaArea().readOldDescription(project);
            }
        } catch (CoreException e) {
            failure = e;
        }
        // If we had an open project and there was an error reading the description
        // from disk, close the project and give it a default description. If the project
        // was already closed then just set a default description.
        if (description == null) {
            description = new ProjectDescription();
            description.setName(project.getName());
            // try to read private metadata and add to the description
            workspace.getMetaArea().readPrivateDescription(project, description);
        }
        project.internalSetDescription(description, false, true);
        if (failure != null) {
            try {
                // write the project tree ...
                writeTree(project);
            } finally {
                // ... and close the project
                project.internalClose(monitor);
            }
            throw failure;
        }
    }

    /**
     * Restores the workspace tree from snapshot files in the event
     * of a crash. The workspace tree must be open when this method
     * is called, and will be open at the end of this method. In the
     * event of a crash recovery, the snapshot file is not deleted until
     * the next successful save.
     */
    protected void restoreSnapshots(IProgressMonitor monitor) {
        monitor = Policy.monitorFor(monitor);
        String message;
        try {
            monitor.beginTask("", Policy.totalWork); //$NON-NLS-1$
            IPath snapLocation = workspace.getMetaArea().getSnapshotLocationFor(workspace.getRoot());
            File localFile = snapLocation.toFile();

            if (!localFile.exists()) {
                // The snapshot corresponding to the current tree version doesn't exist.
                // Try the legacy non-versioned snapshot, but ignore it if it is older than
                // the tree.
                snapLocation = workspace.getMetaArea().getLegacySnapshotLocationFor(workspace.getRoot());
                localFile = snapLocation.toFile();
                if (!localFile.exists() || isSnapshotOlderThanTree(localFile)) {
                    // If the snapshot file doesn't exist, there was no crash.
                    // Just initialize the snapshot file and return.
                    initSnap();
                    return;
                }
            }
            // If we have a snapshot file, the workspace was shutdown without being saved or crashed.
            workspace.setCrashed(true);
            try {
                /* Read each of the snapshots and lay them on top of the current tree. */
                ElementTree complete = workspace.getElementTree();
                complete.immutable();
                try (DataInputStream input = new DataInputStream(new SafeChunkyInputStream(localFile))) {
                    WorkspaceTreeReader reader = WorkspaceTreeReader.getReader(workspace, input.readInt());
                    complete = reader.readSnapshotTree(input, complete, monitor);
                } finally {
                    // reader returned an immutable tree, but since we're inside
                    // an operation, we must return an open tree
                    lastSnap = complete;
                    complete = complete.newEmptyDelta();
                    workspace.tree = complete;
                }
            } catch (Exception e) {
                // only log the exception, we should not fail restoring the snapshot
                message = Messages.resources_snapRead;
                Policy.log(new ResourceStatus(IResourceStatus.FAILED_READ_METADATA, null, message, e));
            }
        } finally {
            monitor.done();
        }
    }

    /**
     * Checks if the given snapshot file is older than the tree file.
     *
     * @param snapshot the snapshot file to check
     * @return {@code true} if the snapshot file is older than the tree file or the tree file
     * does not exist
     */
    private boolean isSnapshotOlderThanTree(File snapshot) {
        IPath treeLocation = workspace.getMetaArea().getTreeLocationFor(workspace.getRoot(), false);
        File tree = treeLocation.toFile();
        if (!tree.exists()) {
            treeLocation = workspace.getMetaArea().getBackupLocationFor(treeLocation);
            tree = treeLocation.toFile();
            if (!tree.exists()) {
                return false;
            }
        }
        return snapshot.lastModified() < tree.lastModified();
    }

    /**
     * Reads the sync info which was originally saved
     * for the tree rooted by the given resource.
     */
    protected void restoreSyncInfo(IResource resource, IProgressMonitor monitor) throws CoreException {
        Assert.isLegal(resource.getType() == IResource.ROOT || resource.getType() == IResource.PROJECT);
        Synchronizer synchronizer = (Synchronizer) workspace.getSynchronizer();
        // when restoring a project, only load sync info if it is open
        if (resource.isAccessible()) {
            synchronizer.restore(resource, monitor);
        }

        // restore sync info for all projects if we were given the workspace root.
        if (resource.getType() == IResource.PROJECT) {
            return;
        }
        IProject[] projects = ((IWorkspaceRoot) resource).getProjects(IContainer.INCLUDE_HIDDEN);
        for (IProject project : projects) {
            if (project.isAccessible()) {
                synchronizer.restore(project, monitor);
            }
        }
    }

    /**
     * Reads the contents of the tree rooted by the given resource from the
     * file system. This method is used when restoring a complete workspace
     * after workspace save/shutdown.
     * 
     * @exception CoreException if the workspace could not be restored.
     */
    protected void restoreTree(IProgressMonitor monitor) throws CoreException {
        IPath treeLocation = workspace.getMetaArea().getTreeLocationFor(workspace.getRoot(), false);
        IPath tempLocation = workspace.getMetaArea().getBackupLocationFor(treeLocation);
        if (!treeLocation.toFile().exists() && !tempLocation.toFile().exists()) {
            savedStates = Collections.synchronizedMap(new HashMap<>(10));
            return;
        }
        try (DataInputStream input = new DataInputStream(
            new SafeFileInputStream(treeLocation.toOSString(), tempLocation.toOSString(), TREE_BUFFER_SIZE))) {
            WorkspaceTreeReader.getReader(workspace, input.readInt()).readTree(input, monitor);
        } catch (Exception e) { // "Unknown format" is passed as ResourceException
            String msg = NLS.bind(Messages.resources_readMeta, treeLocation.toOSString());
            throw new ResourceException(IResourceStatus.FAILED_READ_METADATA, treeLocation, msg, e);
        }
    }

    /**
     * Restores the trees for the builders of this project from the local disk.
     * Does nothing if the tree file does not exist (this means the
     * project has never been saved). This method is
     * used when restoring a saved/closed project. restoreTree(Workspace) is
     * used when restoring a complete workspace after workspace save/shutdown.
     * 
     * @return <code>true</code> if the tree file exists, <code>false</code> otherwise.
     * @exception CoreException if the project could not be restored.
     */
    protected boolean restoreTree(Project project, IProgressMonitor monitor) throws CoreException {
        monitor = Policy.monitorFor(monitor);
        String message;
        try {
            monitor.beginTask("", Policy.totalWork); //$NON-NLS-1$
            IPath treeLocation = workspace.getMetaArea().getTreeLocationFor(project, false);
            IPath tempLocation = workspace.getMetaArea().getBackupLocationFor(treeLocation);
            if (!treeLocation.toFile().exists() && !tempLocation.toFile().exists()) {
                return false;
            }
            try (DataInputStream input
                = new DataInputStream(new SafeFileInputStream(treeLocation.toOSString(), tempLocation.toOSString()))) {
                WorkspaceTreeReader reader = WorkspaceTreeReader.getReader(workspace, input.readInt());
                reader.readTree(project, input, Policy.subMonitorFor(monitor, Policy.totalWork));
            }
        } catch (IOException e) {
            message = NLS.bind(Messages.resources_readMeta, project.getFullPath());
            throw new ResourceException(IResourceStatus.FAILED_READ_METADATA, project.getFullPath(), message, e);
        } finally {
            monitor.done();
        }
        return true;
    }

    /**
     * Restores a tree saved as a refresh snapshot to a specified URI.
     * 
     * @return <code>true</code> if the snapshot exists, <code>false</code> otherwise.
     * @exception CoreException if the project could not be restored.
     */
    protected boolean restoreTreeFromRefreshSnapshot(Project project, File snapshotFile, IProgressMonitor monitor)
        throws CoreException {
        monitor = Policy.monitorFor(monitor);
        String message;
        IPath snapshotPath;
        monitor.beginTask("", Policy.totalWork); //$NON-NLS-1$
        try (ZipInputStream zip = new ZipInputStream(new FileInputStream(snapshotFile))) {
            ZipEntry treeEntry = zip.getNextEntry();
            if (treeEntry == null || !treeEntry.getName().equals("resource-index.tree")) { //$NON-NLS-1$
                return false;
            }
            try (DataInputStream input = new DataInputStream(zip)) {
                WorkspaceTreeReader reader = WorkspaceTreeReader.getReader(workspace, input.readInt(), true);
                reader.readTree(project, input, Policy.subMonitorFor(monitor, Policy.totalWork));
            }
        } catch (IOException e) {
            snapshotPath = IPath.fromOSString(snapshotFile.getPath());
            message = NLS.bind(Messages.resources_readMeta, snapshotPath);
            throw new ResourceException(IResourceStatus.FAILED_READ_METADATA, snapshotPath, message, e);
        } finally {
            monitor.done();
        }
        return true;
    }

    /**
     * Should only be used for read purposes.
     */
    void setPluginsSavedState(HashMap<String, SavedState> savedStates) {
        this.savedStates = Collections.synchronizedMap(savedStates);
    }

    protected void setSaveNumber(String pluginId, int number) {
        masterTable.setProperty(SAVE_NUMBER_PREFIX + pluginId, Integer.toString(number));
    }

    @Override
    public void shareStrings(StringPool pool) {
        lastSnap.shareStrings(pool);
    }

    /**
     * Returns a sorted copy of a chain of trees.
     *
     * @param trees an unordered array of ElementTrees that should be immutable. The
     * array may contain duplicates. The ElementTrees should have been
     * created by repeated calls to Workspace.newWorkingTree() such
     * that the getParent() relationship forms an unambiguous sequence
     * (except of duplicates) from newest ElementTree to oldest. I.e.
     * the newest ElementTree (and its duplicates) has no parent (or at
     * least no ancestor within the given array), while all other
     * ElementTreess have the newest ElementTree as ancestor
     * (transitive parent). The given trees do not need to contain all
     * ElementTrees from the getParent() relationship.
     * @return null or trees ordered by ElementTree.treeStamp descending. i.e.
     * newest ElementTree (without ancestor within the trees) first. Returns
     * null when the preconditions for sorting are not met.
     */
    public static ElementTree[] sortTrees(ElementTree[] trees) {
        int numTrees = trees.length;
        ElementTree[] sorted = new ElementTree[numTrees];

        /* first build a table of ElementTree -> Number of duplicates */
        Map<ElementTree, Integer> duplicateCount = new LinkedHashMap<>(numTrees * 2 + 1);
        for (ElementTree tree : trees) {
            duplicateCount.compute(tree, (k, duplicates) -> (duplicates == null ? 0 : duplicates) + 1);
        }

        /* find the oldest tree (a descendent of all other trees) */
        ElementTree oldest = trees[ElementTree.findOldest(trees)];

        /**
         * Walk through the chain of trees from oldest to newest,
         * adding them to the sorted list as we go.
         */
        int i = numTrees - 1;
        while (oldest != null) {
            /* add "oldest" and its duplicates at the end of the sorted list: */
            Integer duplicates = duplicateCount.remove(oldest);
            for (int j = 0; j < duplicates; j++) {
                sorted[i] = oldest;
                i--;
            }
            /* find the next tree in the list */
            oldest = oldest.getParent();
            while (oldest != null && duplicateCount.get(oldest) == null) {
                /* skip elements that are not elements of "trees" */
                oldest = oldest.getParent();
            }
        }
        if (!duplicateCount.isEmpty()) {
            // could happen if trees contains elements t3,t2,t1 where the parent relations
            // are t3->t1, t2->t1, t1->null
            // either t2 or t3 is not found
            // because it's not unambiguous defined if t3 or t2 is the "older"
            // t3 should have parent t2 instead.
            // happens while trees are mutable.
            Exception e = new NullPointerException(
                "Unable to save workspace - Given trees not in unambiguous order (Bug 352867)"); //$NON-NLS-1$
            IStatus status = new Status(IStatus.WARNING, ResourcesPlugin.PI_RESOURCES, IResourceStatus.INTERNAL_ERROR,
                e.getMessage(), e);
            Policy.log(status);
            return null;
        }
        return sorted;
    }

    /**
     * Writes out persistent information about all builders for which a last built
     * tree is available. File format is:
     * int - number of builders
     * for each builder:
     * UTF - project name
     * UTF - fully qualified builder extension name
     * int - number of interesting projects for builder
     * For each interesting project:
     * UTF - interesting project name
     */
    private void writeBuilderPersistentInfo(DataOutputStream output, List<BuilderPersistentInfo> builders)
        throws IOException {
        // write the number of builders we are saving
        int numBuilders = builders.size();
        output.writeInt(numBuilders);
        for (int i = 0; i < numBuilders; i++) {
            BuilderPersistentInfo info = builders.get(i);
            output.writeUTF(info.getProjectName());
            output.writeUTF(info.getBuilderName());
            // write interesting projects
            IProject[] interestingProjects = info.getInterestingProjects();
            output.writeInt(interestingProjects.length);
            for (IProject interestingProject : interestingProjects) {
                output.writeUTF(interestingProject.getName());
            }
        }
    }

    @Override
    public void writeElement(IPath path, Object element, DataOutput output) throws IOException {
        Assert.isNotNull(path);
        Assert.isNotNull(element);
        Assert.isNotNull(output);
        ResourceInfo info = (ResourceInfo) element;
        output.writeInt(info.getFlags());
        info.writeTo(output);
    }

    /**
     * Discovers the trees which need to be saved for the passed in project's builders.
     * In a pre-3.7 workspace, only one tree is saved per builder.
     * Since 3.7 one tree may be persisted per build configuration per multi-config builder.
     *
     * We still provide one tree per builder first so the workspace can be opened in an older Eclipse.
     * Newer eclipses will be able to load the additional per-configuration trees.
     * 
     * @param project project to fetch builder trees for
     * @param trees list of trees to be persisted
     * @param builderInfos list of builder infos; one per builder
     * @param configNames configuration names persisted for builder infos above
     * @param additionalTrees remaining trees to be persisted for other configurations
     * @param additionalBuilderInfos remaining builder infos for other configurations
     * @param additionalConfigNames configuration names of the remaining per-configuration trees
     */
    private void getTreesToSave(IProject project, List<ElementTree> trees, List<BuilderPersistentInfo> builderInfos,
        List<String> configNames, List<ElementTree> additionalTrees, List<BuilderPersistentInfo> additionalBuilderInfos,
        List<String> additionalConfigNames) throws CoreException {
        if (project.isOpen()) {
            String activeConfigName = project.getActiveBuildConfig().getName();
            List<BuilderPersistentInfo> infos = workspace.getBuildManager().createBuildersPersistentInfo(project);
            if (infos != null) {
                for (BuilderPersistentInfo info : infos) {
                    // Nothing to persist if there isn't a previous delta tree.
                    // There used to be code which serialized the current workspace tree
                    // but this will result in the next build of the builder getting an empty delta...
                    if (info.getLastBuiltTree() == null) {
                        continue;
                    }

                    // Add to the correct list of builders info and add to the configuration names
                    String configName = info.getConfigName() == null ? activeConfigName : info.getConfigName();
                    if (configName.equals(activeConfigName)) {
                        // Serializes the active configurations's build tree
                        // TODO could probably do better by serializing the 'oldest' tree
                        builderInfos.add(info);
                        configNames.add(configName);
                        trees.add(info.getLastBuiltTree());
                    } else {
                        additionalBuilderInfos.add(info);
                        additionalConfigNames.add(configName);
                        additionalTrees.add(info.getLastBuiltTree());
                    }
                }
            }
        }
    }

    /**
     * Attempts to save all the trees for the given project. This includes the current
     * workspace tree and a tree for each builder that has previously built state information.
     *
     * The following is written to the output stream:
     * <ul>
     * <li> Builder info for all the builders for the project's active build configuration </li>
     * <li> Workspace trees for all the project's builders </li>
     * <li> Since 3.7: </li>
     * <li> Builder info for all the builders of all the other project's buildConfigs </li>
     * <li> Name of the project's buildConfigs </li>
     * </ul>
     * This format is designed to work with WorkspaceTreeReader versions 2.
     *
     * @throws IOException if anything went wrong during save.
     * @see WorkspaceTreeReader_2
     */
    protected void writeTree(Project project, DataOutputStream output, IProgressMonitor monitor)
        throws IOException, CoreException {
        SubMonitor subMonitor = SubMonitor.convert(monitor, 10);
        boolean wasImmutable = false;
        try {
            // Create an array of trees to save and ensure that the current one is immutable
            // before we add other trees
            ElementTree current = workspace.getElementTree();
            wasImmutable = current.isImmutable();
            current.immutable();
            List<ElementTree> trees = new ArrayList<>(2);
            subMonitor.worked(1);

            // Get the the builder info and configuration names, and add all the associated
            // workspace trees in the correct order
            List<String> configNames = new ArrayList<>(5);
            List<BuilderPersistentInfo> builderInfos = new ArrayList<>(5);
            List<String> additionalConfigNames = new ArrayList<>(5);
            List<BuilderPersistentInfo> additionalBuilderInfos = new ArrayList<>(5);
            List<ElementTree> additionalTrees = new ArrayList<>(5);
            getTreesToSave(project, trees, builderInfos, configNames, additionalTrees, additionalBuilderInfos,
                additionalConfigNames);

            // Save the version 2 builders info
            writeBuilderPersistentInfo(output, builderInfos);

            // Builder infos of non-active configurations are persisted after the active
            // configuration's builder infos. So, their trees have to follow the same order.
            trees.addAll(additionalTrees);

            // Add the current tree in the list as the last tree in the chain
            trees.add(current);

            // Save the trees
            ElementTreeWriter writer = new ElementTreeWriter(this);
            ElementTree[] treesToSave = trees.toArray(new ElementTree[0]);
            writer.writeDeltaChain(treesToSave, project.getFullPath(), ElementTreeWriter.D_INFINITE, output,
                ResourceComparator.getSaveComparator());
            subMonitor.worked(5);

            // Since 3.7: Save the builders info and get the workspace trees associated with
            // those builders
            writeBuilderPersistentInfo(output, additionalBuilderInfos);

            // Save configuration names for the builders in the order they were saved
            for (String string : configNames) {
                output.writeUTF(string);
            }
            for (String string : additionalConfigNames) {
                output.writeUTF(string);
            }
        } finally {
            subMonitor.done();
            if (!wasImmutable) {
                workspace.newWorkingTree();
            }
        }
    }

    protected void writeTree(Project project) throws CoreException {
        IPath treeLocation = workspace.getMetaArea().getTreeLocationFor(project, true);
        IPath tempLocation = workspace.getMetaArea().getBackupLocationFor(treeLocation);
        try {
            SafeFileOutputStream safe = new SafeFileOutputStream(treeLocation.toOSString(), tempLocation.toOSString());
            try (DataOutputStream output = new DataOutputStream(safe)) {
                output.writeInt(ICoreConstants.WORKSPACE_TREE_VERSION_2);
                writeTree(project, output, null);
            }
        } catch (IOException e) {
            String msg = NLS.bind(Messages.resources_writeMeta, project.getFullPath());
            throw new ResourceException(IResourceStatus.FAILED_WRITE_METADATA, treeLocation, msg, e);
        }
    }

}
