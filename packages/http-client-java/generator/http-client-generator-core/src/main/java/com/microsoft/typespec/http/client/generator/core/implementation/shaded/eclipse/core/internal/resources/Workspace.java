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
 *     Red Hat Incorporated - loadProjectDescription(InputStream)
 *     Serge Beauchamp (Freescale Semiconductor) - [252996] add resource filtering
 *     Serge Beauchamp (Freescale Semiconductor) - [229633] Group and Project Path Variable Support
 *     Broadcom Corporation - ongoing development
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 473427
 *     Christoph Läubrich - Issue #77, Issue #86, Issue #124
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.filesystem.EFS;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.filesystem.IFileStore;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.events.BuildManager;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.events.ResourceComparator;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.localstore.FileSystemResourceManager;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.properties.IPropertyManager;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.utils.Messages;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.utils.Policy;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.watson.ElementTree;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IPathVariableManager;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResourceRuleFactory;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResourceStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IWorkspace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IWorkspaceRoot;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IWorkspaceRunnable;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.ResourcesPlugin;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.team.TeamHook;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Assert;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IConfigurationElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.ICoreRunnable;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.NullProgressMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.OperationCanceledException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Platform;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.PlatformObject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.SubMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.jobs.ISchedulingRule;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Bundle;

import java.util.concurrent.atomic.AtomicLong;

/**
 * The workspace class is the monolithic nerve center of the resources plugin.
 * All interesting functionality stems from this class.
 * <p>
 * All modifications to the workspace occur within the context of a workspace operation.
 * A workspace operation is implemented using the following sequence:
 * </p>
 * 
 * <pre>
 * 	try {
 *		prepareOperation(...);
 *		//check preconditions
 *		beginOperation(...);
 *		//perform changes
 *	} finally {
 *		endOperation(...);
 *	}
 * </pre>
 * 
 * Workspace operations can be nested arbitrarily. A "top level" workspace operation
 * is an operation that is not nested within another workspace operation in the current
 * thread.
 * <p>
 * See the javadoc of {@link #prepareOperation(ISchedulingRule, IProgressMonitor)},
 * {@link #beginOperation(boolean)}, and {@link #endOperation(ISchedulingRule, boolean)}
 * for more details.
 * </p>
 */
public class Workspace extends PlatformObject implements IWorkspace, ICoreConstants {
    public static final boolean caseSensitive
        = !Platform.OS_MACOSX.equals(Platform.getOS()) && new java.io.File("a").compareTo(new java.io.File("A")) != 0; //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Work manager should never be accessed directly because accessor
     * asserts that workspace is still open.
     */
    protected WorkManager _workManager;
    protected BuildManager buildManager;
    protected final IWorkspaceRoot defaultRoot = new WorkspaceRoot(IPath.ROOT, this);
    protected FileSystemResourceManager fileSystemManager;
    protected final LocalMetaArea localMetaArea;
    /**
     * Helper class for performing validation of resource names and locations.
     */
    protected final LocationValidator locationValidator = new LocationValidator();
    protected MarkerManager markerManager;
    protected final AtomicLong nextMarkerId = new AtomicLong();
    protected final AtomicLong nextNodeId = new AtomicLong(1L);

    protected boolean openFlag = false;
    protected ElementTree operationTree; // tree at the start of the current operation
    protected PathVariableManager pathVariableManager;
    protected IPropertyManager propertyManager;

    /**
     * Scheduling rule factory. This field is null if the factory has not been used
     * yet. The accessor method should be used rather than accessing this field
     * directly.
     */
    private IResourceRuleFactory ruleFactory;

    /**
     * The currently installed team hook.
     */
    protected TeamHook teamHook = null;

    /**
     * The workspace tree. The tree is an in-memory representation
     * of the resources that make up the workspace. The tree caches
     * the structure and state of files and directories on disk (their existence
     * and last modified times). When external parties make changes to
     * the files on disk, this representation becomes out of sync. A local refresh
     * reconciles the state of the files on disk with this tree
     * The tree is also used to store metadata associated with resources in
     * the workspace (markers, properties, etc).
     *
     * While the ElementTree data structure can handle both concurrent
     * reads and concurrent writes, write access to the tree is governed
     * by {@link WorkManager}.
     */
    protected volatile ElementTree tree;

    /**
     * This field is used to control access to the workspace tree during
     * resource change notifications. It tracks which thread, if any, is
     * in the middle of a resource change notification. This is used to cause
     * attempts to modify the workspace during notifications to fail.
     */
    protected volatile Thread treeLocked;

    /**
     * Deletes all the files and directories from the given root down (inclusive).
     * Returns false if we could not delete some file or an exception occurred
     * at any point in the deletion.
     * Even if an exception occurs, a best effort is made to continue deleting.
     */
    public static boolean clear(java.io.File root) {
        IFileStore fileStore = EFS.getLocalFileSystem().fromLocalFile(root);
        try {
            fileStore.delete(EFS.NONE, new NullProgressMonitor());
        } catch (CoreException e) {
            return false;
        }
        return true;
    }

    public Workspace() {
        super();
        localMetaArea = new LocalMetaArea(this);
        tree = new ElementTree();
        /* tree should only be modified during operations */
        tree.immutable();
        treeLocked = Thread.currentThread();
        tree.setTreeData(newElement(IResource.ROOT));
    }

    public void beginOperation(boolean createNewTree) throws CoreException {
        WorkManager workManager = getWorkManager();
        workManager.incrementNestedOperations();
        if (!workManager.isBalanced()) {
            Assert.isTrue(false, "Operation was not prepared."); //$NON-NLS-1$
        }
        if (workManager.getPreparedOperationDepth() > 1) {
            if (createNewTree && tree.isImmutable()) {
                newWorkingTree();
            }
            return;
        }
        // stash the current tree as the basis for this operation.
        operationTree = tree;
        if (createNewTree && tree.isImmutable()) {
            newWorkingTree();
        }
    }

    public void broadcastPostChange() {
    }

    /**
     * Returns whether creating executable extensions is acceptable
     * at this point in time. In particular, returns <code>false</code>
     * when the system bundle is shutting down, which only occurs
     * when the entire framework is exiting.
     */
    private boolean canCreateExtensions() {
        return Platform
            .getBundle("com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi") //$NON-NLS-1$
            .getState() != Bundle.STOPPING;
    }

    /**
     * End an operation (group of resource changes).
     * Notify interested parties that resource changes have taken place. All
     * registered resource change listeners are notified. If autobuilding is
     * enabled, a build is run.
     */
    public void endOperation(ISchedulingRule rule, boolean build) throws CoreException {
        WorkManager workManager = getWorkManager();
        // don't do any end operation work if we failed to check in
        if (workManager.checkInFailed(rule)) {
            return;
        }
        // This is done in a try finally to ensure that we always decrement the operation count
        // and release the workspace lock. This must be done at the end because snapshot
        // and "hasChanges" comparison have to happen without interference from other threads.
        boolean hasTreeChanges;
        boolean depthOne;
        try {
            workManager.setBuild(build);
            // if we are not exiting a top level operation then just decrement the count and return
            depthOne = workManager.getPreparedOperationDepth() == 1;
            // do the following in a try/finally to ensure that the operation tree is nulled at the end
            // as we are completing a top level operation.
            try {
                // check for a programming error on using beginOperation/endOperation
                Assert.isTrue(workManager.getPreparedOperationDepth() > 0, "Mismatched begin/endOperation"); //$NON-NLS-1$

                // At this time we need to re-balance the nested operations. It is necessary because
                // build() and snapshot() should not fail if they are called.
                workManager.rebalanceNestedOperations();

                // find out if any operation has potentially modified the tree
                hasTreeChanges = workManager.shouldBuild();
                // double check if the tree has actually changed
                if (hasTreeChanges) {
                    hasTreeChanges = operationTree != null
                        && ElementTree.hasChanges(tree, operationTree, ResourceComparator.getBuildComparator(), true);
                }
                broadcastPostChange();
            } finally {
                // make sure the tree is immutable if we are ending a top-level operation.
                if (depthOne) {
                    tree.immutable();
                    operationTree = null;
                } else {
                    newWorkingTree();
                }
            }
        } finally {
            workManager.checkOut(rule);
        }
        if (depthOne) {
            buildManager.endTopLevel(hasTreeChanges);
        }
    }

    /**
     * Returns the current element tree for this workspace
     */
    public ElementTree getElementTree() {
        return tree;
    }

    public FileSystemResourceManager getFileSystemManager() {
        return fileSystemManager;
    }

    /**
     * Returns the marker manager for this workspace
     */
    public MarkerManager getMarkerManager() {
        return markerManager;
    }

    public LocalMetaArea getMetaArea() {
        return localMetaArea;
    }

    @Override
    public IPathVariableManager getPathVariableManager() {
        return pathVariableManager;
    }

    public IPropertyManager getPropertyManager() {
        return propertyManager;
    }

    /**
     * Returns the resource info for the identified resource.
     * null is returned if no such resource can be found.
     * If the phantom flag is true, phantom resources are considered.
     * If the mutable flag is true, the info is opened for change.
     *
     * This method DOES NOT throw an exception if the resource is not found.
     */
    public ResourceInfo getResourceInfo(IPath path, boolean phantom, boolean mutable) {
        try {
            if (path.segmentCount() == 0) {
                ResourceInfo info = (ResourceInfo) tree.getTreeData();
                Assert.isNotNull(info, "Tree root info must never be null"); //$NON-NLS-1$
                return info;
            }
            ResourceInfo result;
            if (!tree.includes(path)) {
                return null;
            }
            if (mutable) {
                result = (ResourceInfo) tree.openElementData(path);
            } else {
                result = (ResourceInfo) tree.getElementData(path);
            }
            if (result != null && (!phantom && result.isSet(M_PHANTOM))) {
                return null;
            }
            return result;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public IWorkspaceRoot getRoot() {
        return defaultRoot;
    }

    @Override
    public IResourceRuleFactory getRuleFactory() {
        // note that the rule factory is created lazily because it
        // requires loading the teamHook extension
        if (ruleFactory == null) {
            ruleFactory = new Rules(this);
        }
        return ruleFactory;
    }

    /**
     * Returns the installed team hook. Never returns null.
     */
    protected TeamHook getTeamHook() {
        if (teamHook == null) {
            initializeTeamHook();
        }
        return teamHook;
    }

    /**
     * We should not have direct references to this field. All references should go through
     * this method.
     */
    public WorkManager getWorkManager() throws CoreException {
        if (_workManager == null) {
            String message = Messages.resources_shutdown;
            throw new ResourceException(IResourceStatus.INTERNAL_ERROR, null, message, null);
        }
        return _workManager;
    }

    /**
     * A team hook hasn't been initialized. Check the extension point and
     * try to create a new hook if a user has one defined as an extension.
     * Otherwise use the Core's implementation as the default.
     */
    protected void initializeTeamHook() {
        try {
            if (!canCreateExtensions()) {
                return;
            }
            IConfigurationElement[] configs = Platform.getExtensionRegistry()
                .getConfigurationElementsFor(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PT_TEAM_HOOK);
            // no-one is plugged into the extension point so disable validation
            if (configs == null || configs.length == 0) {
                return;
            }
            // can only have one defined at a time. log a warning
            if (configs.length > 1) {
                // XXX: should provide a meaningful status code
                IStatus status = new ResourceStatus(IStatus.ERROR, 1, null, Messages.resources_oneTeamHook, null);
                Policy.log(status);
                return;
            }
            // otherwise we have exactly one hook extension. Try to create a new instance
            // from the user-specified class.
            try {
                IConfigurationElement config = configs[0];
                teamHook = (TeamHook) config.createExecutableExtension("class"); //$NON-NLS-1$
            } catch (CoreException e) {
                // ignore the failure if we are shutting down (expected since extension
                // provider plugin has probably already shut down
                if (canCreateExtensions()) {
                    IStatus status = new ResourceStatus(IStatus.ERROR, 1, null, Messages.resources_initTeamHook, e);
                    Policy.log(status);
                }
            }
        } finally {
            // default to use Core's implementation
            // create anonymous subclass because TeamHook is abstract
            if (teamHook == null) {
                teamHook = new TeamHook(this) {
                    // empty
                };
            }
        }
    }

    public boolean isOpen() {
        return openFlag;
    }

    @Override
    public boolean isTreeLocked() {
        return treeLocked == Thread.currentThread();
    }

    /**
     * Create and return a new tree element of the given type.
     */
    protected ResourceInfo newElement(int type) {
        ResourceInfo result = null;
        switch (type) {
            case IResource.FILE:
            case IResource.FOLDER:
                result = new ResourceInfo();
                break;

            case IResource.PROJECT:
                result = new ProjectInfo();
                break;

            case IResource.ROOT:
                result = new RootInfo();
                break;
        }
        result.setNodeId(nextNodeId());
        updateModificationStamp(result);
        result.setType(type);
        return result;
    }

    public Resource newResource(IPath path, int type) {
        String message;
        switch (type) {
            case IResource.FOLDER:
                if (path.segmentCount() < ICoreConstants.MINIMUM_FOLDER_SEGMENT_LENGTH) {
                    message = "Path must include project and resource name: " + path; //$NON-NLS-1$
                    Assert.isLegal(false, message);
                }
                return new Folder(path.makeAbsolute(), this);

            case IResource.FILE:
                if (path.segmentCount() < ICoreConstants.MINIMUM_FILE_SEGMENT_LENGTH) {
                    message = "Path must include project and resource name: " + path; //$NON-NLS-1$
                    Assert.isLegal(false, message);
                }
                return new File(path.makeAbsolute(), this);

            case IResource.PROJECT:
                return (Resource) getRoot().getProject(path.lastSegment());

            case IResource.ROOT:
                return (Resource) getRoot();
        }
        Assert.isLegal(false);
        // will never get here because of assertion.
        return null;
    }

    /**
     * Opens a new mutable element tree layer, thus allowing
     * modifications to the tree.
     */
    public ElementTree newWorkingTree() {
        // synchronized for atomic swap. Should have already synchronized by
        // getWorkManager().checkIn/checkout, but it's not guaranteed
        synchronized (this) {
            tree = tree.newEmptyDelta();
            return tree;
        }
    }

    /**
     * Returns the next, previously unassigned, marker id.
     */
    protected long nextMarkerId() {
        return nextMarkerId.getAndIncrement();
    }

    protected long nextNodeId() {
        return nextNodeId.getAndIncrement();
    }

    /**
     * Called before checking the pre-conditions of an operation. Optionally supply
     * a scheduling rule to determine when the operation is safe to run. If a
     * scheduling rule is supplied, this method will block until it is safe to run.
     * Even if no scheduling is supplied this method blocks until no other workspace
     * operation concurrently runs.
     *
     * @param rule the scheduling rule that describes what this operation intends to
     * modify.
     */
    public void prepareOperation(ISchedulingRule rule, IProgressMonitor monitor) throws CoreException {
        getWorkManager().checkIn(rule, monitor);
        if (!isOpen()) {
            String message = Messages.resources_workspaceClosed;
            throw new ResourceException(IResourceStatus.OPERATION_FAILED, null, message, null);
        }
    }

    @Override
    public void run(ICoreRunnable action, ISchedulingRule rule, int options, IProgressMonitor monitor)
        throws CoreException {
        SubMonitor subMonitor = SubMonitor.convert(monitor, Policy.totalWork); // $NON-NLS-1$
        int depth = -1;
        try {
            prepareOperation(rule, subMonitor);
            beginOperation(true);
            depth = getWorkManager().beginUnprotected();
            action.run(subMonitor.newChild(Policy.opWork));
        } catch (OperationCanceledException e) {
            getWorkManager().operationCanceled();
            throw e;
        } catch (CoreException e) {
            if (e.getStatus().getSeverity() == IStatus.CANCEL) {
                getWorkManager().operationCanceled();
            }
            throw e;
        } finally {
            subMonitor.done();
            if (depth >= 0) {
                getWorkManager().endUnprotected(depth);
            }
            endOperation(rule, false);
        }
    }

    @Override
    public void run(IWorkspaceRunnable action, IProgressMonitor monitor) throws CoreException {
        run((ICoreRunnable) action, defaultRoot, IWorkspace.AVOID_UPDATE, monitor);
    }

    @Override
    public void run(IWorkspaceRunnable action, ISchedulingRule rule, int options, IProgressMonitor monitor)
        throws CoreException {
        run((ICoreRunnable) action, rule, options, monitor);
    }

    /** for debugging only **/
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[localMetaArea=" + localMetaArea.getLocation() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void updateModificationStamp(ResourceInfo info) {
        info.incrementModificationStamp();
    }

    @Override
    public IStatus validateName(String segment, int type) {
        return locationValidator.validateName(segment, type);
    }
}
