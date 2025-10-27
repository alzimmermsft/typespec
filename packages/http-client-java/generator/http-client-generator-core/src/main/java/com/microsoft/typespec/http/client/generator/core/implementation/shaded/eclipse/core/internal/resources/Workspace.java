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
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.filesystem.URIUtil;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.events.BuildManager;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.events.ILifecycleListener;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.events.LifecycleEvent;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.events.NotificationManager;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.events.ResourceChangeEvent;
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
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResourceChangeEvent;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResourceChangeListener;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResourceRuleFactory;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResourceStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.ISaveContext;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.ISaveParticipant;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.ISavedState;
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
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.jobs.MultiRule;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.util.NLS;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Bundle;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The workspace class is the monolithic nerve center of the resources plugin.
 * All interesting functionality stems from this class.
 * <p>
 * The lifecycle of the resources plugin is encapsulated by the {@link #open()}
 * and {@link #close(IProgressMonitor)} methods.  A closed workspace is completely
 * unusable - any attempt to access or modify interesting workspace state on a closed
 * workspace will fail.
 * </p>
 * <p>
 * All modifications to the workspace occur within the context of a workspace operation.
 * A workspace operation is implemented using the following sequence:
 * </p>
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
 * Workspace operations can be nested arbitrarily. A "top level" workspace operation
 * is an operation that is not nested within another workspace operation in the current
 * thread.
 * <p>
 * See the javadoc of {@link #prepareOperation(ISchedulingRule, IProgressMonitor)},
 * {@link #beginOperation(boolean)}, and {@link #endOperation(ISchedulingRule, boolean)}
 * for more details.
 * </p>
 * <p>
 * Major areas of functionality are farmed off to various manager classes.  Open a
 * type hierarchy on {@link IManager} to see all the different managers.
 * </p>
 */
public class Workspace extends PlatformObject implements IWorkspace, ICoreConstants {
	public static final boolean caseSensitive = !Platform.OS_MACOSX.equals(Platform.getOS())
        && new java.io.File("a").compareTo(new java.io.File("A")) != 0; //$NON-NLS-1$ //$NON-NLS-2$

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

	protected NotificationManager notificationManager;
	protected boolean openFlag = false;
	protected ElementTree operationTree; // tree at the start of the current operation
	protected PathVariableManager pathVariableManager;
	protected IPropertyManager propertyManager;

    /**
	 * Scheduling rule factory. This field is null if the factory has not been used
	 * yet.  The accessor method should be used rather than accessing this field
	 * directly.
	 */
	private IResourceRuleFactory ruleFactory;

	protected SaveManager saveManager;
	/**
	 * File modification validation.  If it is true and validator is null, we try/initialize
	 * validator first time through.  If false, there is no validator.
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
	 * The workspace tree.  The tree is an in-memory representation
	 * of the resources that make up the workspace.  The tree caches
	 * the structure and state of files and directories on disk (their existence
	 * and last modified times).  When external parties make changes to
	 * the files on disk, this representation becomes out of sync. A local refresh
	 * reconciles the state of the files on disk with this tree (@link {@link IResource#refreshLocal(int, IProgressMonitor)}).
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
	 * in the middle of a resource change notification.  This is used to cause
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
	 * Indicates that a build is about to occur. Broadcasts the necessary
	 * deltas before the build starts. Note that this will cause POST_BUILD
	 * to be automatically done at the end of the operation in which
	 * the build occurs.
	 */
	protected void aboutToBuild(Object source, int trigger) {
		//fire a POST_CHANGE first to ensure everyone is up to date before firing PRE_BUILD
		broadcastPostChange();
		broadcastBuildEvent(source, IResourceChangeEvent.PRE_BUILD, trigger);
	}

	/**
	 * Adds a listener for internal workspace lifecycle events.  There is no way to
	 * remove lifecycle listeners.
	 */
	public void addLifecycleListener(ILifecycleListener listener) {
		lifecycleListeners.addIfAbsent(listener);
	}

	@Override
	public void addResourceChangeListener(IResourceChangeListener listener) {
		notificationManager.addListener(listener, IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE | IResourceChangeEvent.POST_CHANGE);
	}

	@Override
	public void addResourceChangeListener(IResourceChangeListener listener, int eventMask) {
		notificationManager.addListener(listener, eventMask);
	}

	@Override
	public ISavedState addSaveParticipant(String pluginId, ISaveParticipant participant) throws CoreException {
		Assert.isNotNull(pluginId, "Plugin id must not be null"); //$NON-NLS-1$
		Assert.isNotNull(participant, "Participant must not be null"); //$NON-NLS-1$
		return saveManager.addParticipant(pluginId, participant);
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

	public void broadcastBuildEvent(Object source, int type, int buildTrigger) {
		ResourceChangeEvent event = new ResourceChangeEvent(source, type, buildTrigger, null);
		notificationManager.broadcastChanges(tree, event, false);
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
		ResourceChangeEvent event = new ResourceChangeEvent(this, IResourceChangeEvent.POST_CHANGE, 0, null);
		notificationManager.broadcastChanges(tree, event, true);
	}

    boolean isRelaxedRule(ISchedulingRule rule) {
		return rule == null || !rule.contains(getRoot());
	}

	/**
	 * Returns whether creating executable extensions is acceptable
	 * at this point in time.  In particular, returns <code>false</code>
	 * when the system bundle is shutting down, which only occurs
	 * when the entire framework is exiting.
	 */
	private boolean canCreateExtensions() {
		return Platform.getBundle("com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi").getState() != Bundle.STOPPING; //$NON-NLS-1$
	}

    protected void copyTree(IResource source, IPath destination) throws CoreException {
		copyTree(source, destination, IResource.DEPTH_ZERO, IResource.NONE, false, false, source.getType() == IResource.PROJECT);
	}

	private void copyTree(IResource source, IPath destination, int depth, int updateFlags, boolean keepSyncInfo, boolean moveResources, boolean movingProject) throws CoreException {

		// retrieve the resource at the destination if there is one (phantoms included).
		// if there isn't one, then create a new handle based on the type that we are
		// trying to copy
		IResource destinationResource = getRoot().findMember(destination, true);
		int destinationType;
		if (destinationResource == null) {
			if (source.getType() == IResource.FILE) {
				destinationType = IResource.FILE;
			} else if (destination.segmentCount() == 1) {
				destinationType = IResource.PROJECT;
			} else {
				destinationType = IResource.FOLDER;
			}
			destinationResource = newResource(destination, destinationType);
		} else {
			destinationType = destinationResource.getType();
		}

		// create the resource at the destination
		ResourceInfo sourceInfo = ((Resource) source).getResourceInfo(true, false);
		if (destinationType != source.getType()) {
			sourceInfo = (ResourceInfo) sourceInfo.clone();
			sourceInfo.setType(destinationType);
		}
		ResourceInfo newInfo = createResource(destinationResource, sourceInfo, false, true, keepSyncInfo);
		// get/set the node id from the source's resource info so we can later put it in the
		// info for the destination resource. This will help us generate the proper deltas,
		// indicating a move rather than a add/delete
		newInfo.setNodeId(sourceInfo.getNodeId());

		// preserve local sync info but not location info
		newInfo.setFlags(newInfo.getFlags() | (sourceInfo.getFlags() & M_LOCAL_EXISTS));
		newInfo.setFileStoreRoot(null);

		// forget content-related caching flags
		newInfo.clear(M_CONTENT_CACHE);

		// update link locations in project descriptions
		if (source.isLinked()) {
			LinkDescription linkDescription;
			URI sourceLocationURI = transferVariableDefinition(source, destinationResource, source.getLocationURI());
			if (((updateFlags & IResource.SHALLOW) != 0) || ((Resource) source).isUnderVirtual()) {
				//for shallow move the destination is a linked resource with the same location
				newInfo.set(ICoreConstants.M_LINK);
				linkDescription = new LinkDescription(destinationResource, sourceLocationURI);
			} else {
				//for deep move the destination is not a linked resource
				newInfo.clear(ICoreConstants.M_LINK);
				linkDescription = null;
			}
			if (moveResources && !movingProject) {
				if (((Project) source.getProject()).internalGetDescription().setLinkLocation(source.getProjectRelativePath(), null)) {
					((Project) source.getProject()).writeDescription();
				}
			}
			Project project = (Project) destinationResource.getProject();
			project.internalGetDescription().setLinkLocation(destinationResource.getProjectRelativePath(), linkDescription);
			project.writeDescription();
			newInfo.setFileStoreRoot(null);
		}

		// Update folder filters in project descriptions - copy filters for any container except project.
		// Filters that are set on project itself are copied automatically.
		if (!movingProject && source instanceof Container && source.getProject().exists()
				&& ((Container) source).hasFilters()) {
			Project sourceProject = (Project) source.getProject();
			LinkedList<FilterDescription> originalDescriptions = sourceProject.internalGetDescription().getFilter(source.getProjectRelativePath());
			LinkedList<FilterDescription> filterDescriptions = FilterDescription.copy(originalDescriptions, destinationResource);
			if (moveResources) {
				if (((Project) source.getProject()).internalGetDescription().setFilters(source.getProjectRelativePath(), null)) {
					((Project) source.getProject()).writeDescription();
				}
			}
			Project project = (Project) destinationResource.getProject();
			project.internalGetDescription().setFilters(destinationResource.getProjectRelativePath(), filterDescriptions);
			project.writeDescription();
		}

		// do the recursion. if we have a file then it has no members so return. otherwise
		// recursively call this method on the container's members if the depth tells us to
		if (depth == IResource.DEPTH_ZERO || source.getType() == IResource.FILE) {
			return;
		}
		if (depth == IResource.DEPTH_ONE) {
			depth = IResource.DEPTH_ZERO;
		}
		//copy .project file first if project is being copied, otherwise links won't be able to update description
		boolean projectCopy = source.getType() == IResource.PROJECT && destinationType == IResource.PROJECT;
		if (projectCopy) {
			IResource dotProject = ((Project) source).findMember(IProjectDescription.DESCRIPTION_FILE_NAME);
			if (dotProject != null) {
				copyTree(dotProject, destination.append(dotProject.getName()), depth, updateFlags, keepSyncInfo, moveResources, movingProject);
			}
		}
		IResource[] children = ((IContainer) source).members(IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS | IContainer.INCLUDE_HIDDEN);
		for (IResource element : children) {
			String childName = element.getName();
			if (!projectCopy || !childName.equals(IProjectDescription.DESCRIPTION_FILE_NAME)) {
				IPath childPath = destination.append(childName);
				copyTree(element, childPath, depth, updateFlags, keepSyncInfo, moveResources, movingProject);
			}
		}
	}

	public URI transferVariableDefinition(IResource source, IResource dest, URI sourceURI) throws CoreException {
		IPath srcLoc = source.getLocation();
		IPath srcRawLoc = source.getRawLocation();
		if ((srcLoc != null) && (srcRawLoc != null) && !srcLoc.equals(srcRawLoc)) {
			// the location is variable relative
			if (!source.getProject().equals(dest.getProject())) {
				String variable = srcRawLoc.segment(0);
				variable = copyVariable(source, dest, variable);
				IPath newLocation = IPath.fromPortableString(variable).append(srcRawLoc.removeFirstSegments(1));
				sourceURI = toURI(newLocation);
			} else {
				sourceURI = toURI(srcRawLoc);
			}
		}
		return sourceURI;
	}

	URI toURI(IPath path) {
		if (path.isAbsolute()) {
			return org.eclipse.core.filesystem.URIUtil.toURI(path);
		}
		try {
			return new URI(null, null, path.toPortableString(), null);
		} catch (URISyntaxException e) {
			return org.eclipse.core.filesystem.URIUtil.toURI(path);
		}
	}

	String copyVariable(IResource source, IResource dest, String variable) throws CoreException {
		IPathVariableManager destPathVariableManager = dest.getPathVariableManager();
		IPathVariableManager srcPathVariableManager = source.getPathVariableManager();

		IPath srcValue = URIUtil.toPath(srcPathVariableManager.getURIValue(variable));
		if (srcValue == null) { // if the variable doesn't exist, return another
			// variable that doesn't exist either
			return PathVariableUtil.getUniqueVariableName(variable, dest);
		}
		IPath resolvedSrcValue = URIUtil.toPath(srcPathVariableManager.resolveURI(URIUtil.toURI(srcValue)));

		boolean variableExisted = false;
		// look if the exact same variable exists
		if (destPathVariableManager.isDefined(variable)) {
			variableExisted = true;
			IPath destValue = URIUtil.toPath(destPathVariableManager.getURIValue(variable));
			if (destValue != null && URIUtil.toPath(destPathVariableManager.resolveURI(URIUtil.toURI(destValue))).equals(resolvedSrcValue)) {
				return variable;
			}
		}
		// look if one variable in the destination project matches
		String[] variables = destPathVariableManager.getPathVariableNames();
		for (String other : variables) {
			if (!PathVariableUtil.isPreferred(other)) {
				continue;
			}
			IPath resolveDestVariable = URIUtil.toPath(destPathVariableManager.resolveURI(destPathVariableManager.getURIValue(other)));
			if (resolveDestVariable != null && resolveDestVariable.equals(resolvedSrcValue)) {
				return other;
			}
		}
		// if the variable doesn't exist in the dest project, or
		// if the value is different than the source project, we have to create
		// an equivalent.
		String destVariable = PathVariableUtil.getUniqueVariableName(variable, dest);

		boolean shouldConvertToRelative = true;
		if (!srcValue.equals(resolvedSrcValue) && !variableExisted) {
			// the variable content contains references to more variables

			String[] referencedVariables = PathVariableUtil.splitVariableNames(srcValue.toPortableString());
			shouldConvertToRelative = false;
			// If the variable value is of type ${PARENT-COUNT-VAR},
			// we can avoid generating an intermediate variable and convert it directly.
			if (referencedVariables.length == 1) {
				if (PathVariableUtil.isParentVariable(referencedVariables[0])) {
					shouldConvertToRelative = true;
				}
			}

			if (!shouldConvertToRelative) {
				String[] segments = PathVariableUtil.splitVariablesAndContent(srcValue.toPortableString());
				StringBuilder result = new StringBuilder();
				for (String segment : segments) {
					String var = PathVariableUtil.extractVariable(segment);
					if (var.length() > 0) {
						String copiedVariable = copyVariable(source, dest, var);
						int index = segment.indexOf(var);
						if (index != -1) {
							result.append(segment, 0, index);
							result.append(copiedVariable);
							int start = index + var.length();
							int end = segment.length();
							result.append(segment, start, end);
						}
					} else {
						result.append(segment);
					}
				}
				srcValue = IPath.fromPortableString(result.toString());
			}
		}
		if (shouldConvertToRelative) {
			IPath relativeSrcValue = PathVariableUtil.convertToPathRelativeMacro(destPathVariableManager, resolvedSrcValue, dest, true, null);
			if (relativeSrcValue != null) {
				srcValue = relativeSrcValue;
			}
		}
		destPathVariableManager.setURIValue(destVariable, URIUtil.toURI(srcValue));
		return destVariable;
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
			case IResource.DEPTH_ZERO :
				return 1;
			case IResource.DEPTH_ONE :
				return 1 + tree.getChildCount(root);
			case IResource.DEPTH_INFINITE :
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
		//		if ((updateFlags & IResource.VIRTUAL) != 0)
		//			info.set(M_VIRTUAL);
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
	public ResourceInfo createResource(IResource resource, ResourceInfo info, boolean phantom, boolean overwrite, boolean keepSyncInfo) throws CoreException {
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

	@Override
	public IStatus delete(IResource[] resources, int updateFlags, IProgressMonitor monitor) throws CoreException {

		String message = Messages.resources_deleteProblem;
		MultiStatus result = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.INTERNAL_ERROR, message,
				null);
		if (resources.length == 0) {
			return result;
		}
		resources = resources.clone(); // to avoid concurrent changes to this array
		SubMonitor subMonitor = SubMonitor.convert(monitor, message, resources.length);
		try {
			prepareOperation(getRoot(), subMonitor);
			beginOperation(true);
			for (IResource r : resources) {
				Resource resource = (Resource) r;
				if (resource == null) {
					subMonitor.split(1);
					continue;
				}
				try {
					resource.delete(updateFlags, subMonitor.newChild(1));
				} catch (CoreException e) {
					// Don't really care about the exception unless the resource is still around.
					ResourceInfo info = resource.getResourceInfo(false, false);
					if (resource.exists(resource.getFlags(info), false)) {
						message = NLS.bind(Messages.resources_couldnotDelete, resource.getFullPath());
						result.merge(new ResourceStatus(IResourceStatus.FAILED_DELETE_LOCAL, resource.getFullPath(),
								message));
						result.merge(e.getStatus());
					}
				}
				subMonitor.worked(1);
			}
			if (result.matches(IStatus.ERROR)) {
				throw new ResourceException(result);
			}
			return result;
		} catch (OperationCanceledException e) {
			getWorkManager().operationCanceled();
			throw e;
		} finally {
			subMonitor.done();
			endOperation(getRoot(), true);
		}
	}

    /**
	 * Delete the given resource from the current tree of the receiver.
	 * This method simply removes the resource from the tree.  No cleanup or
	 * other management is done.  Use IResource.delete for proper deletion.
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
	 * Notify interested parties that resource changes have taken place.  All
	 * registered resource change listeners are notified.  If autobuilding is
	 * enabled, a build is run.
	 */
	public void endOperation(ISchedulingRule rule, boolean build) throws CoreException {
		WorkManager workManager = getWorkManager();
		//don't do any end operation work if we failed to check in
		if (workManager.checkInFailed(rule)) {
			return;
		}
		// This is done in a try finally to ensure that we always decrement the operation count
		// and release the workspace lock.  This must be done at the end because snapshot
		// and "hasChanges" comparison have to happen without interference from other threads.
		boolean hasTreeChanges;
		boolean depthOne;
		try {
			workManager.setBuild(build);
			// if we are not exiting a top level operation then just decrement the count and return
			depthOne = workManager.getPreparedOperationDepth() == 1;
			if (!(notificationManager.shouldNotify() || depthOne)) {
				notificationManager.requestNotify();
				return;
			}
			// do the following in a try/finally to ensure that the operation tree is nulled at the end
			// as we are completing a top level operation.
			try {
				notificationManager.beginNotify();
				// check for a programming error on using beginOperation/endOperation
				Assert.isTrue(workManager.getPreparedOperationDepth() > 0, "Mismatched begin/endOperation"); //$NON-NLS-1$

				// At this time we need to re-balance the nested operations. It is necessary because
				// build() and snapshot() should not fail if they are called.
				workManager.rebalanceNestedOperations();

				//find out if any operation has potentially modified the tree
				hasTreeChanges = workManager.shouldBuild();
				//double check if the tree has actually changed
				if (hasTreeChanges) {
					hasTreeChanges = operationTree != null && ElementTree.hasChanges(tree, operationTree, ResourceComparator.getBuildComparator(), true);
				}
				broadcastPostChange();
				// Request a snapshot if we are sufficiently out of date.
				saveManager.snapshotIfNeeded(hasTreeChanges);
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

	public NotificationManager getNotificationManager() {
		return notificationManager;
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
		//note that the rule factory is created lazily because it
		//requires loading the teamHook extension
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
	 * Returns the installed team hook.  Never returns null.
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
			IConfigurationElement[] configs = Platform.getExtensionRegistry().getConfigurationElementsFor(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PT_MOVE_DELETE_HOOK);
			// no-one is plugged into the extension point so disable validation
			if (configs == null || configs.length == 0) {
				return;
			}
			// can only have one defined at a time. log a warning
			if (configs.length > 1) {
				//XXX: should provide a meaningful status code
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
				//ignore the failure if we are shutting down (expected since extension
				//provider plugin has probably already shut down
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
			IConfigurationElement[] configs = Platform.getExtensionRegistry().getConfigurationElementsFor(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PT_TEAM_HOOK);
			// no-one is plugged into the extension point so disable validation
			if (configs == null || configs.length == 0) {
				return;
			}
			// can only have one defined at a time. log a warning
			if (configs.length > 1) {
				//XXX: should provide a meaningful status code
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
				//ignore the failure if we are shutting down (expected since extension
				//provider plugin has probably already shut down
				if (canCreateExtensions()) {
					IStatus status = new ResourceStatus(IStatus.ERROR, 1, null, Messages.resources_initTeamHook, e);
					Policy.log(status);
				}
			}
		} finally {
			// default to use Core's implementation
			//create anonymous subclass because TeamHook is abstract
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
		IConfigurationElement[] configs = Platform.getExtensionRegistry().getConfigurationElementsFor(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PT_FILE_MODIFICATION_VALIDATOR);
		// no-one is plugged into the extension point so disable validation
		if (configs == null || configs.length == 0) {
			return;
		}
		// can only have one defined at a time. log a warning, disable validation, but continue with
		// the #setContents (e.g. don't throw an exception)
		if (configs.length > 1) {
			//XXX: should provide a meaningful status code
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
			//ignore the failure if we are shutting down (expected since extension
			//provider plugin has probably already shut down
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
	 * Moves this resource's subtree to the destination. This operation should only be
	 * used by move methods. Destination must be a valid destination for this resource.
	 * The keepSyncInfo boolean is used to indicated whether or not the sync info should
	 * be moved from the source to the destination.
	 */

	/* package */
	void move(Resource source, IPath destination, int depth, int updateFlags, boolean keepSyncInfo) throws CoreException {
		// overlay the tree at the destination path, preserving any important info
		// in any already existing resource information
		copyTree(source, destination, depth, updateFlags, keepSyncInfo, true, source.getType() == IResource.PROJECT);
		source.fixupAfterMoveSource();
	}

	/**
	 * Create and return a new tree element of the given type.
	 */
	protected ResourceInfo newElement(int type) {
		ResourceInfo result = null;
		switch (type) {
			case IResource.FILE :
			case IResource.FOLDER :
				result = new ResourceInfo();
				break;
			case IResource.PROJECT :
				result = new ProjectInfo();
				break;
			case IResource.ROOT :
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
			case IResource.FOLDER :
				if (path.segmentCount() < ICoreConstants.MINIMUM_FOLDER_SEGMENT_LENGTH) {
					message = "Path must include project and resource name: " + path; //$NON-NLS-1$
					Assert.isLegal(false, message);
				}
				return new Folder(path.makeAbsolute(), this);
			case IResource.FILE :
				if (path.segmentCount() < ICoreConstants.MINIMUM_FILE_SEGMENT_LENGTH) {
					message = "Path must include project and resource name: " + path; //$NON-NLS-1$
					Assert.isLegal(false, message);
				}
				return new File(path.makeAbsolute(), this);
			case IResource.PROJECT :
				return (Resource) getRoot().getProject(path.lastSegment());
			case IResource.ROOT :
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
	 *             modify.
	 */
	public void prepareOperation(ISchedulingRule rule, IProgressMonitor monitor) throws CoreException {
        getWorkManager().checkIn(rule, monitor);
		if (!isOpen()) {
			String message = Messages.resources_workspaceClosed;
			throw new ResourceException(IResourceStatus.OPERATION_FAILED, null, message, null);
		}
	}

    @Override
	public void removeResourceChangeListener(IResourceChangeListener listener) {
		notificationManager.removeListener(listener);
	}

	@Override
	public void removeSaveParticipant(String pluginId) {
		Assert.isNotNull(pluginId, "Plugin id must not be null"); //$NON-NLS-1$
		saveManager.removeParticipant(pluginId);
	}

	@Override
	public void run(ICoreRunnable action, ISchedulingRule rule, int options, IProgressMonitor monitor) throws CoreException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, Policy.totalWork); // $NON-NLS-1$
		int depth = -1;
		boolean avoidNotification = (options & IWorkspace.AVOID_UPDATE) != 0;
		try {
			prepareOperation(rule, subMonitor);
			beginOperation(true);
			if (avoidNotification) {
				avoidNotification = notificationManager.beginAvoidNotify();
			}
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
			if (avoidNotification) {
				notificationManager.endAvoidNotify();
			}
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
	public void run(IWorkspaceRunnable action, ISchedulingRule rule, int options, IProgressMonitor monitor) throws CoreException {
		run((ICoreRunnable) action, rule, options, monitor);
	}

	public IStatus save(boolean full, boolean keepConsistencyWhenCanceled, IProgressMonitor monitor) throws CoreException {
		String message;
		if (full) {
			//according to spec it is illegal to start a full save inside another operation
			if (getWorkManager().isLockAlreadyAcquired()) {
				message = Messages.resources_saveOp;
				throw new ResourceException(IResourceStatus.OPERATION_FAILED, null, message, new IllegalStateException());
			}
			return saveManager.save(ISaveContext.FULL_SAVE, keepConsistencyWhenCanceled, null, monitor);
		}
		// A snapshot was requested.  Start an operation (if not already started) and
		// signal that a snapshot should be done at the end.
		try {
			prepareOperation(getRoot(), monitor);
			beginOperation(false);
			saveManager.requestSnapshot();
			message = Messages.resources_snapRequest;
			return new ResourceStatus(IStatus.OK, message);
		} finally {
			endOperation(getRoot(), false);
		}
	}

	public void setCrashed(boolean value) {
		crashed = value;
		if (crashed) {
			String msg = "The workspace exited with unsaved changes in the previous session; refreshing workspace to recover changes."; //$NON-NLS-1$
			Policy.log(new ResourceStatus(ICoreConstants.CRASH_DETECTED, msg));
		}
	}

	public void setTreeLocked(boolean locked) {
		Assert.isTrue(!locked || treeLocked == null, "The workspace tree is already locked"); //$NON-NLS-1$
		treeLocked = locked ? Thread.currentThread() : null;
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
			MultiStatus result = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.READ_ONLY_LOCAL, message, null);
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
			public void run() throws Exception {
				Object c = context;
				//must null any reference to FileModificationValidationContext for backwards compatibility
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

	/**
	 * Internal method. To be called only from the following methods:
	 * <ul>
	 * <li><code>IFile#appendContents</code></li>
	 * <li><code>IFile#setContents(InputStream, boolean, boolean, IProgressMonitor)</code></li>
	 * <li><code>IFile#setContents(IFileState, boolean, boolean, IProgressMonitor)</code></li>
	 * </ul>
	 *
	 * @see IFileModificationValidator#validateSave(IFile)
	 */
	protected void validateSave(final IFile file) throws CoreException {
		// if validation is turned off then just return
		if (!shouldValidate) {
			return;
		}
		// first time through the validator hasn't been initialized so try and create it
		if (validator == null) {
			initializeValidator();
		}
		// we were unable to initialize the validator. Validation has been turned off and
		// a warning has already been logged so just return.
		if (validator == null) {
			return;
		}
		// otherwise call the API and throw an exception if appropriate
		final IStatus[] status = new IStatus[1];
		ISafeRunnable body = new ISafeRunnable() {
			@Override
			public void handleException(Throwable exception) {
				status[0] = new ResourceStatus(IStatus.ERROR, null, Messages.resources_errorValidator, exception);
			}

			@Override
			public void run() throws Exception {
				status[0] = validator.validateSave(file);
			}
		};
		SafeRunner.run(body);
		if (!status[0].isOK()) {
			throw new ResourceException(status[0]);
		}
	}

    @Override
	public void write(Map<IFile, byte[]> contentMap, boolean force, boolean derived, boolean keepHistory,
			IProgressMonitor monitor, ExecutorService executorService) throws CoreException {
		Objects.requireNonNull(contentMap);
		ConcurrentMap<File, byte[]> filesToCreate = new ConcurrentHashMap<>(contentMap.size());
		ConcurrentMap<File, byte[]> filesToReplace = new ConcurrentHashMap<>(contentMap.size());
		int updateFlags = (derived ? IResource.DERIVED : IResource.NONE) | (force ? IResource.FORCE : IResource.NONE)
				| (keepHistory ? IResource.KEEP_HISTORY : IResource.NONE);
		int createFlags = (force ? IResource.FORCE : IResource.NONE) | (derived ? IResource.DERIVED : IResource.NONE);
		SubMonitor subMon = SubMonitor.convert(monitor, contentMap.size());
		for (Entry<IFile, byte[]> e : contentMap.entrySet()) {
			IFile file = Objects.requireNonNull(e.getKey());
			byte[] content = Objects.requireNonNull(e.getValue());
			if (file.exists()) {
				if (file instanceof File f) {
					filesToReplace.put(f, content);
				} else {
					file.setContents(content, updateFlags, subMon.split(1));
				}
			} else {
				if (file instanceof File f) {
					filesToCreate.put(f, content);
				} else {
					file.create(content, createFlags, subMon.split(1));
				}
			}
		}
		for (Entry<File, byte[]> e : filesToReplace.entrySet()) {
			File file = e.getKey();
			byte[] content = e.getValue();
			file.setContents(content, updateFlags, subMon.split(1));
		}
		if (!filesToCreate.isEmpty()) {
			createMultiple(filesToCreate, createFlags, subMon.split(filesToCreate.size()), executorService);
		}
	}

	/** @see File#create(byte[], int, IProgressMonitor) **/
	private void createMultiple(ConcurrentMap<File, byte[]> filesToCreate, int updateFlags, IProgressMonitor monitor,
			ExecutorService executorService) throws CoreException {
		Set<File> files = filesToCreate.keySet();
		for (File file : files) {
			file.checkValidPath(file.path, IResource.FILE, true);
		}

		IPath name = files.iterator().next().getFullPath(); // XXX any name
		SubMonitor subMonitor = SubMonitor.convert(monitor, NLS.bind(Messages.resources_creating, name), 1);
		try {
			ISchedulingRule rule = MultiRule
					.combine(files.stream().map(getRuleFactory()::createRule).toArray(ISchedulingRule[]::new));
			NullProgressMonitor npm = new NullProgressMonitor();
			try {
				prepareOperation(rule, npm);
				for (File file : files) {
					file.checkCreatable();
				}
				beginOperation(true);
				try {
					File.internalSetMultipleContents(filesToCreate, updateFlags, false, subMonitor.newChild(1),
							executorService);
				} catch (CoreException | OperationCanceledException e) {
					// CoreException when a problem happened creating a file on disk
					// OperationCanceledException when the operation of setting contents has been
					// canceled
					// In either case delete from the workspace and disk
					for (File file : files) {
						try {
							deleteResource(file);
							IFileStore store = file.getStore();
							store.delete(EFS.NONE, null);
						} catch (Exception e2) {
							e.addSuppressed(e);
						}
					}
					throw e;
				}
			} catch (OperationCanceledException e) {
				getWorkManager().operationCanceled();
				throw e;
			} finally {
				endOperation(rule, true);
			}
		} finally {
			subMonitor.done();
		}
	}
}
