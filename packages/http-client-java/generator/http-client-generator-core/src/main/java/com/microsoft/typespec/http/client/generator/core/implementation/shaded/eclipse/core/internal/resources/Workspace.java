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
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.events.ILifecycleListener;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.events.LifecycleEvent;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.events.ResourceComparator;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.localstore.FileSystemResourceManager;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.properties.IPropertyManager;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.utils.BitMask;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.utils.Messages;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.utils.Policy;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.watson.ElementTree;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.watson.ElementTreeIterator;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.watson.IElementContentVisitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IContainer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IFile;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IFileModificationValidator;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IFilterMatcherDescriptor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IPathVariableManager;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IProjectDescription;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IProjectNatureDescriptor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResourceRuleFactory;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResourceStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.ISynchronizer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IWorkspace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IWorkspaceDescription;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IWorkspaceRoot;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IWorkspaceRunnable;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.ResourcesPlugin;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.team.FileModificationValidationContext;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.team.FileModificationValidator;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.team.IMoveDeleteHook;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.team.TeamHook;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Assert;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IConfigurationElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.ICoreRunnable;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.ISafeRunnable;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.MultiStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.NullProgressMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.OperationCanceledException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Platform;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.PlatformObject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.SafeRunner;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Status;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.SubMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.jobs.ISchedulingRule;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.util.NLS;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Bundle;
import java.net.URI;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The workspace class is the monolithic nerve center of the resources plugin.
 * All interesting functionality stems from this class.
 * <p>
 * The lifecycle of the resources plugin is encapsulated by the {@link #open()}
 * and {@link #close(IProgressMonitor)} methods. A closed workspace is completely
 * unusable - any attempt to access or modify interesting workspace state on a closed
 * workspace will fail.
 * </p>
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
 * <p>
 * Major areas of functionality are farmed off to various manager classes. Open a
 * type hierarchy on {@link IManager} to see all the different managers.
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
    protected AliasManager aliasManager;
    protected BuildManager buildManager;
    protected CharsetManager charsetManager;
    protected ContentDescriptionManager contentDescriptionManager;
    /** indicates if the workspace crashed in a previous session */
    protected boolean crashed = false;
    protected final IWorkspaceRoot defaultRoot = new WorkspaceRoot(IPath.ROOT, this);
    protected WorkspacePreferences description;
    protected FileSystemResourceManager fileSystemManager;
    protected final CopyOnWriteArrayList<ILifecycleListener> lifecycleListeners = new CopyOnWriteArrayList<>();
    protected final LocalMetaArea localMetaArea;
    /**
     * Helper class for performing validation of resource names and locations.
     */
    protected final LocationValidator locationValidator = new LocationValidator(this);
    protected MarkerManager markerManager;
    /**
     * The currently installed Move/Delete hook.
     */
    protected IMoveDeleteHook moveDeleteHook = null;
    protected NatureManager natureManager;
    protected FilterTypeManager filterManager;
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

    protected SaveManager saveManager;
    /**
     * File modification validation. If it is true and validator is null, we try/initialize
     * validator first time through. If false, there is no validator.
     */
    protected boolean shouldValidate = true;

    /**
     * The synchronizer
     */
    protected Synchronizer synchronizer;

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
     * reconciles the state of the files on disk with this tree (@link
     * {@link IResource#refreshLocal(int, IProgressMonitor)}).
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
     * The currently installed file modification validator.
     */
    protected IFileModificationValidator validator = null;

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

    public static WorkspaceDescription defaultWorkspaceDescription() {
        return new WorkspaceDescription("Workspace"); //$NON-NLS-1$
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

    /**
     * Adds a listener for internal workspace lifecycle events. There is no way to
     * remove lifecycle listeners.
     */
    public void addLifecycleListener(ILifecycleListener listener) {
        lifecycleListeners.addIfAbsent(listener);
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

    /**
     * Broadcasts an internal workspace lifecycle event to interested
     * internal listeners.
     */
    protected void broadcastEvent(LifecycleEvent event) throws CoreException {
        for (ILifecycleListener listener : lifecycleListeners) {
            listener.handleEvent(event);
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
     * Returns the number of resources in a subtree of the resource tree.
     *
     * @param root The subtree to count resources for
     * @param depth The depth of the subtree to count
     * @param phantom If true, phantoms are included, otherwise they are ignored.
     */
    public int countResources(IPath root, int depth, final boolean phantom) {
        if (!tree.includes(root)) {
            return 0;
        }
        switch (depth) {
            case IResource.DEPTH_ZERO:
                return 1;

            case IResource.DEPTH_ONE:
                return 1 + tree.getChildCount(root);

            case IResource.DEPTH_INFINITE:
                final int[] count = new int[1];
                IElementContentVisitor visitor = (aTree, requestor, elementContents) -> {
                    if (phantom || !((ResourceInfo) elementContents).isSet(M_PHANTOM)) {
                        count[0]++;
                    }
                    return true;
                };
                new ElementTreeIterator(tree, root).iterate(visitor);
                return count[0];
        }
        return 0;
    }

    /*
     * Creates the given resource in the tree and returns the new resource info object.
     * If phantom is true, the created element is marked as a phantom.
     * If there is already be an element in the tree for the given resource
     * in the given state (i.e., phantom), a CoreException is thrown.
     * If there is already a phantom in the tree and the phantom flag is false,
     * the element is overwritten with the new element. (but the synchronization
     * information is preserved)
     */
    public ResourceInfo createResource(IResource resource, boolean phantom) throws CoreException {
        return createResource(resource, null, phantom, false, false);
    }

    /**
     * Creates a resource, honoring update flags requesting that the resource
     * be immediately made derived, hidden and/or team private
     */
    public ResourceInfo createResource(IResource resource, int updateFlags) throws CoreException {
        ResourceInfo info = createResource(resource, null, false, BitMask.isSet(updateFlags, IResource.REPLACE), false);
        if ((updateFlags & IResource.DERIVED) != 0) {
            info.set(M_DERIVED);
        }
        if ((updateFlags & IResource.TEAM_PRIVATE) != 0) {
            info.set(M_TEAM_PRIVATE_MEMBER);
        }
        if ((updateFlags & IResource.HIDDEN) != 0) {
            info.set(M_HIDDEN);
        }
        // if ((updateFlags & IResource.VIRTUAL) != 0)
        // info.set(M_VIRTUAL);
        return info;
    }

    /*
     * Creates the given resource in the tree and returns the new resource info object.
     * If phantom is true, the created element is marked as a phantom.
     * If there is already be an element in the tree for the given resource
     * in the given state (i.e., phantom), a CoreException is thrown.
     * If there is already a phantom in the tree and the phantom flag is false,
     * the element is overwritten with the new element. (but the synchronization
     * information is preserved) If the specified resource info is null, then create
     * a new one.
     *
     * If keepSyncInfo is set to be true, the sync info in the given ResourceInfo is NOT
     * cleared before being created and thus any sync info already existing at that namespace
     * (as indicated by an already existing phantom resource) will be lost.
     */
    public ResourceInfo createResource(IResource resource, ResourceInfo info, boolean phantom, boolean overwrite,
        boolean keepSyncInfo) throws CoreException {
        info = info == null ? newElement(resource.getType()) : (ResourceInfo) info.clone();
        ResourceInfo original = getResourceInfo(resource.getFullPath(), true, false);
        if (phantom) {
            info.set(M_PHANTOM);
            info.clearModificationStamp();
        }
        // if nothing existed at the destination then just create the resource in the tree
        if (original == null) {
            // we got here from a copy/move. we don't want to copy over any sync info
            // from the source so clear it.
            if (!keepSyncInfo) {
                info.setSyncInfo(null);
            }
            tree.createElement(resource.getFullPath(), info);
        } else {
            // if overwrite==true then slam the new info into the tree even if one existed before
            if (overwrite || (!phantom && original.isSet(M_PHANTOM))) {
                // copy over the sync info and flags from the old resource info
                // since we are replacing a phantom with a real resource
                // DO NOT set the sync info dirty flag because we want to
                // preserve the old sync info so its not dirty
                // XXX: must copy over the generic sync info from the old info to the new
                // XXX: do we really need to clone the sync info here?
                if (!keepSyncInfo) {
                    info.setSyncInfo(original.getSyncInfo(true));
                }
                // mark the markers bit as dirty so we snapshot an empty marker set for
                // the new resource
                info.set(ICoreConstants.M_MARKERS_SNAP_DIRTY);
                tree.setElementData(resource.getFullPath(), info);
            } else {
                String message = NLS.bind(Messages.resources_mustNotExist, resource.getFullPath());
                throw new ResourceException(IResourceStatus.RESOURCE_EXISTS, resource.getFullPath(), message, null);
            }
        }
        return info;
    }

    /**
     * Delete the given resource from the current tree of the receiver.
     * This method simply removes the resource from the tree. No cleanup or
     * other management is done. Use IResource.delete for proper deletion.
     * If the given resource is the root, all of its children (i.e., all projects) are
     * deleted but the root is left.
     */
    void deleteResource(IResource resource) {
        IPath path = resource.getFullPath();
        if (path.equals(IPath.ROOT)) {
            IProject[] children = getRoot().getProjects(IContainer.INCLUDE_HIDDEN);
            for (IProject element : children) {
                tree.deleteElement(element.getFullPath());
            }
        } else {
            tree.deleteElement(path);
        }
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

    public AliasManager getAliasManager() {
        return aliasManager;
    }

    /**
     * Returns this workspace's build manager
     */
    public BuildManager getBuildManager() {
        return buildManager;
    }

    public CharsetManager getCharsetManager() {
        return charsetManager;
    }

    public ContentDescriptionManager getContentDescriptionManager() {
        return contentDescriptionManager;
    }

    @Override
    public IWorkspaceDescription getDescription() {
        WorkspaceDescription workingCopy = defaultWorkspaceDescription();
        description.copyTo(workingCopy);
        return workingCopy;
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

    protected IMoveDeleteHook getMoveDeleteHook() {
        if (moveDeleteHook == null) {
            initializeMoveDeleteHook();
        }
        return moveDeleteHook;
    }

    @Override
    public IFilterMatcherDescriptor getFilterMatcherDescriptor(String filterMatcherId) {
        return filterManager.getFilterDescriptor(filterMatcherId);
    }

    @Override
    public IProjectNatureDescriptor getNatureDescriptor(String natureId) {
        return natureManager.getNatureDescriptor(natureId);
    }

    /**
     * Returns the nature manager for this workspace.
     */
    public NatureManager getNatureManager() {
        return natureManager;
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

    public SaveManager getSaveManager() {
        return saveManager;
    }

    @Override
    public ISynchronizer getSynchronizer() {
        return synchronizer;
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
     * A move/delete hook hasn't been initialized. Check the extension point and
     * try to create a new hook if a user has one defined as an extension. Otherwise
     * use the Core's implementation as the default.
     */
    protected void initializeMoveDeleteHook() {
        try {
            if (!canCreateExtensions()) {
                return;
            }
            IConfigurationElement[] configs = Platform.getExtensionRegistry()
                .getConfigurationElementsFor(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PT_MOVE_DELETE_HOOK);
            // no-one is plugged into the extension point so disable validation
            if (configs == null || configs.length == 0) {
                return;
            }
            // can only have one defined at a time. log a warning
            if (configs.length > 1) {
                // XXX: should provide a meaningful status code
                IStatus status = new ResourceStatus(IStatus.ERROR, 1, null, Messages.resources_oneHook, null);
                Policy.log(status);
                return;
            }
            // otherwise we have exactly one hook extension. Try to create a new instance
            // from the user-specified class.
            try {
                IConfigurationElement config = configs[0];
                moveDeleteHook = (IMoveDeleteHook) config.createExecutableExtension("class"); //$NON-NLS-1$
            } catch (CoreException e) {
                // ignore the failure if we are shutting down (expected since extension
                // provider plugin has probably already shut down
                if (canCreateExtensions()) {
                    IStatus status = new ResourceStatus(IStatus.ERROR, 1, null, Messages.resources_initHook, e);
                    Policy.log(status);
                }
            }
        } finally {
            // for now just use Core's implementation
            if (moveDeleteHook == null) {
                moveDeleteHook = new MoveDeleteHook();
            }
        }
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

    /**
     * A file modification validator hasn't been initialized. Check the extension point and
     * try to create a new validator if a user has one defined as an extension.
     */
    protected void initializeValidator() {
        shouldValidate = false;
        if (!canCreateExtensions()) {
            return;
        }
        IConfigurationElement[] configs = Platform.getExtensionRegistry()
            .getConfigurationElementsFor(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PT_FILE_MODIFICATION_VALIDATOR);
        // no-one is plugged into the extension point so disable validation
        if (configs == null || configs.length == 0) {
            return;
        }
        // can only have one defined at a time. log a warning, disable validation, but continue with
        // the #setContents (e.g. don't throw an exception)
        if (configs.length > 1) {
            // XXX: should provide a meaningful status code
            IStatus status = new ResourceStatus(IStatus.ERROR, 1, null, Messages.resources_oneValidator, null);
            Policy.log(status);
            return;
        }
        // otherwise we have exactly one validator extension. Try to create a new instance
        // from the user-specified class.
        try {
            IConfigurationElement config = configs[0];
            validator = (IFileModificationValidator) config.createExecutableExtension("class"); //$NON-NLS-1$
            shouldValidate = true;
        } catch (CoreException e) {
            // ignore the failure if we are shutting down (expected since extension
            // provider plugin has probably already shut down
            if (canCreateExtensions()) {
                IStatus status = new ResourceStatus(IStatus.ERROR, 1, null, Messages.resources_initValidator, e);
                Policy.log(status);
            }
        }
    }

    public WorkspaceDescription internalGetDescription() {
        return description;
    }

    @Override
    public boolean isAutoBuilding() {
        return description.isAutoBuilding();
    }

    public boolean isOpen() {
        return openFlag;
    }

    @Override
    public boolean isTreeLocked() {
        return treeLocked == Thread.currentThread();
    }

    /**
     * Link the given tree into the receiver's tree at the specified resource.
     */
    protected void linkTrees(IPath path, ElementTree[] newTrees) {
        tree = tree.mergeDeltaChain(path, newTrees);
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

    @Override
    public IProjectDescription newProjectDescription(String projectName) {
        IProjectDescription result = new ProjectDescription();
        result.setName(projectName);
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

    public void setCrashed(boolean value) {
        crashed = value;
        if (crashed) {
            String msg
                = "The workspace exited with unsaved changes in the previous session; refreshing workspace to recover changes."; //$NON-NLS-1$
            Policy.log(new ResourceStatus(ICoreConstants.CRASH_DETECTED, msg));
        }
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
    public IStatus validateEdit(final IFile[] files, final Object context) {
        // if validation is turned off then just return
        if (!shouldValidate) {
            String message = Messages.resources_readOnly2;
            MultiStatus result
                = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.READ_ONLY_LOCAL, message, null);
            for (IFile file : files) {
                if (file.isReadOnly()) {
                    IPath filePath = file.getFullPath();
                    message = NLS.bind(Messages.resources_readOnly, filePath);
                    result.add(new ResourceStatus(IResourceStatus.READ_ONLY_LOCAL, filePath, message));
                }
            }
            return result.getChildren().length == 0 ? Status.OK_STATUS : result;
        }
        // first time through the validator hasn't been initialized so try and create it
        if (validator == null) {
            initializeValidator();
        }
        // we were unable to initialize the validator. Validation has been turned off and
        // a warning has already been logged so just return.
        if (validator == null) {
            return Status.OK_STATUS;
        }
        // otherwise call the API and throw an exception if appropriate
        final IStatus[] status = new IStatus[1];
        ISafeRunnable body = new ISafeRunnable() {
            @Override
            public void handleException(Throwable exception) {
                status[0] = new ResourceStatus(IStatus.ERROR, null, Messages.resources_errorValidator, exception);
            }

            @Override
            public void run() {
                Object c = context;
                // must null any reference to FileModificationValidationContext for backwards compatibility
                if (!(validator instanceof FileModificationValidator)) {
                    if (c instanceof FileModificationValidationContext) {
                        c = null;
                    }
                }
                status[0] = validator.validateEdit(files, c);
            }
        };
        SafeRunner.run(body);
        return status[0];
    }

    @Override
    public IStatus validateLinkLocationURI(IResource resource, URI unresolvedLocation) {
        return locationValidator.validateLinkLocationURI(resource, unresolvedLocation);
    }

    @Override
    public IStatus validateName(String segment, int type) {
        return locationValidator.validateName(segment, type);
    }

    @Override
    public IStatus validateProjectLocationURI(IProject project, URI location) {
        return locationValidator.validateProjectLocationURI(project, location);
    }

}
