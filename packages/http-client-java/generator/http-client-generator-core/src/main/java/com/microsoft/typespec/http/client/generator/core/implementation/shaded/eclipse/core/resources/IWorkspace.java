/*******************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *     Red Hat Incorporated - loadProjectDescription(InputStream)
 *     Broadcom Corporation - build configurations and references
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.team.FileModificationValidationContext;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IAdaptable;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.ICoreRunnable;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.OperationCanceledException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.SubMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.jobs.ISchedulingRule;

import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * Workspaces are the basis for Eclipse Platform resource management. There is
 * only one workspace per running platform. All resources exist in the context
 * of this workspace.
 * <p>
 * A workspace corresponds closely to discreet areas in the local file system.
 * Each project in a workspace maps onto a specific area of the file system. The
 * folders and files within a project map directly onto the corresponding
 * directories and files in the file system. One sub-directory, the workspace
 * metadata area, contains internal information about the workspace and its
 * resources. This metadata area should be accessed only by the Platform or via
 * Platform API calls.
 * </p>
 * <p>
 * Workspaces add value over using the file system directly in that they allow
 * for comprehensive change tracking (through <code>IResourceDelta</code> s),
 * various forms of resource metadata (e.g., markers and properties) as well as
 * support for managing application/tool state (e.g., saving and restoring).
 * </p>
 * <p>
 * The workspace as a whole is thread safe and allows one writer concurrent with
 * multiple readers. It also supports mechanisms for saving and snapshotting the
 * current resource state.
 * </p>
 * <p>
 * The workspace is provided by the Resources plug-in and is automatically
 * created when that plug-in is activated. The default workspace data area
 * (i.e., where its resources are stored) overlap exactly with the platform's
 * data area. That is, by default, the workspace's projects are found directly
 * in the platform's data area. Individual project locations can be specified
 * explicitly.
 * </p>
 *
 * <p>
 * The recommended way of accessing the Workspace is fetching it as a service
 * what could be performed in several ways:
 * </p>
 * <ul>
 * <li>For example in a Bundle/Plugin Activator:
 *
 * <pre>
 * ServiceTracker&lt;IWorkspace, IWorkspace&gt; workspaceTracker = new ServiceTracker&lt;&gt;(bundleContext, IWorkspace.class,
 * 		null);
 * workspaceTracker.open();
 * IWorkspace workspace = workspaceTracker.getService();
 * </pre>
 *
 * </li>
 * <li>Using declarative service with annotations:
 *
 * <pre>
 * &#64;Reference
 * private IWorkspace workspace;
 * </pre>
 *
 * </li>
 * <li>Helper classes like ServiceCaller:
 *
 * <pre>
 * ServiceCaller.callOnce(getClass(), IWorkspace.class, workspace -&gt; {
 * 	// do something with the workspace
 * });
 * </pre>
 *
 * </li>
 * </ul>
 *
 * <p>
 * The workspace resource namespace is always case-sensitive and
 * case-preserving. Thus the workspace allows multiple sibling resources to
 * exist with names that differ only in case. The workspace also imposes no
 * restrictions on valid characters in resource names, the length of resource
 * names, or the size of resources on disk. In situations where one or more
 * resources are stored in a file system that is not case-sensitive, or that
 * imposes restrictions on resource names, any failure to store or retrieve
 * those resources will be propagated back to the caller of workspace API.
 * </p>
 * <p>
 * Workspaces implement the <code>IAdaptable</code> interface; extensions are
 * managed by the platform's adapter manager.
 * </p>
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IWorkspace extends IAdaptable {
	/**
	 * flag constant (bit mask value 1) indicating that resource change
	 * notifications should be avoided during the invocation of a compound
	 * resource changing operation.
	 *
	 * @see IWorkspace#run(ICoreRunnable, ISchedulingRule, int, IProgressMonitor)
	 * @since 3.0
	 */
	int AVOID_UPDATE = 1;

	/**
	 * Constant that can be passed to {@link #validateEdit(IFile[], Object)}
	 * to indicate that the caller does not have access to a UI context but would still
	 * like to have UI-based validation if possible.
	 * @since 3.3
	 * @see #validateEdit(IFile[], Object)
	 */
	Object VALIDATE_PROMPT = FileModificationValidationContext.VALIDATE_PROMPT;

    /**
	 * Adds the given listener for resource change events to this workspace. Has
	 * no effect if an identical listener is already registered.
	 * <p>
	 * This method is equivalent to:
	 * </p>
	 *
	 * <pre>
	 * addResourceChangeListener(listener, IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE | IResourceChangeEvent.POST_CHANGE);
	 * </pre>
	 *
	 *
	 * @param listener the listener
	 * @see IResourceChangeListener
	 * @see IResourceChangeEvent
	 * @see #addResourceChangeListener(IResourceChangeListener, int)
	 * @see #removeResourceChangeListener(IResourceChangeListener)
	 */
	void addResourceChangeListener(IResourceChangeListener listener);

	/**
	 * Adds the given listener for the specified resource change events to this
	 * workspace. Has no effect if an identical listener is already registered
	 * for these events. After completion of this method, the given listener
	 * will be registered for exactly the specified events. If they were
	 * previously registered for other events, they will be de-registered.
	 * <p>
	 * Once registered, a listener starts receiving notification of changes to
	 * resources in the workspace. The resource deltas in the resource change
	 * event are rooted at the workspace root. Most resource change
	 * notifications occur well after the fact; the exception is
	 * pre-notification of impending project closures and deletions. The
	 * listener continues to receive notifications until it is replaced or
	 * removed.
	 * </p>
	 * <p>
	 * Listeners can listen for several types of event as defined in
	 * <code>IResourceChangeEvent</code>. Clients are free to register for
	 * any number of event types however if they register for more than one, it
	 * is their responsibility to ensure they correctly handle the case where
	 * the same resource change shows up in multiple notifications. Clients are
	 * guaranteed to receive only the events for which they are registered.
	 * </p>
	 *
	 * @param listener the listener
	 * @param eventMask the bit-wise OR of all event types of interest to the
	 * listener
	 * @see IResourceChangeListener
	 * @see IResourceChangeEvent
	 * @see #removeResourceChangeListener(IResourceChangeListener)
	 */
	void addResourceChangeListener(IResourceChangeListener listener, int eventMask);

	/**
	 * Registers the given plug-in's workspace save participant, and returns an
	 * object describing the workspace state at the time of the last save in
	 * which the bundle participated.
	 * <p>
	 * Once registered, the workspace save participant will actively participate
	 * in the saving of this workspace.
	 * </p>
	 *
	 * @param pluginId the unique identifier of the plug-in
	 * @param participant the participant
	 * @return the last saved state in which the plug-in participated, or
	 * <code>null</code> if the plug-in has not participated before
	 * @exception CoreException if the method fails to add the participant.
	 * Reasons include:
	 * <ul>
	 * <li>The previous state could not be recovered.</li>
	 * </ul>
	 * @see ISaveParticipant
	 * @see #removeSaveParticipant(String)
	 * @since 3.6
	 */
	ISavedState addSaveParticipant(String pluginId, ISaveParticipant participant) throws CoreException;

    /**
	 * Deletes the given resources.
	 * <p>
	 * This method can be expressed as a series of calls to
	 * <code>IResource.delete(int,IProgressMonitor)</code>.
	 * </p>
	 * <p>
	 * The semantics of multiple deletion are:
	 * </p>
	 * <ul>
	 * <li>Resources are deleted in the order presented, using the given update
	 * flags.</li>
	 * <li>Resources that do not exist are ignored.</li>
	 * <li>An individual deletion fails if the resource still exists
	 * afterwards.</li>
	 * <li>The failure of an individual deletion does not prevent the method
	 * from attempting to delete other resources.</li>
	 * <li>This method fails if one or more of the individual resource
	 * deletions fails; that is, if at least one of the resources in the list
	 * still exists at the end of this method.</li>
	 * </ul>
	 * <p>
	 * This method changes resources; these changes will be reported in a
	 * subsequent resource change event.
	 * </p>
	 * <p>
	 * This method is long-running; progress and cancellation are provided by
	 * the given progress monitor.
	 * </p>
	 *
	 * @param resources the resources to delete
	 * @param updateFlags bit-wise or of update flag constants
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 * reporting is not desired
	 * @return status with code <code>OK</code> if there were no problems;
	 * otherwise a description (possibly a multi-status) consisting of
	 * low-severity warnings or informational messages
	 * @exception CoreException if the method fails to delete some resource. The
	 * status contained in the exception is a multi-status indicating where the
	 * individual failures occurred.
	 * @exception OperationCanceledException if the operation is canceled.
	 * Cancelation can occur even if no progress monitor is provided.
	 * @see IResource#delete(int,IProgressMonitor)
	 * @see IResourceRuleFactory#deleteRule(IResource)
	 * @since 2.0
	 */
	IStatus delete(IResource[] resources, int updateFlags, IProgressMonitor monitor) throws CoreException;

	/**
	 * Returns the filter descriptor with the given unique identifier, or
	 * <code>null</code> if there is no such filter.
	 *
	 * @param filterMatcherId the filter matcher extension identifier (e.g.
	 * <code>"com.example.coolFilter"</code>).
	 * @return the filter matcher descriptor, or <code>null</code>
	 * @since 3.6
	 */
	IFilterMatcherDescriptor getFilterMatcherDescriptor(String filterMatcherId);

    /**
	 * Returns the nature descriptor with the given unique identifier, or
	 * <code>null</code> if there is no such nature.
	 *
	 * @param natureId the nature extension identifier (e.g.
	 * <code>"com.example.coolNature"</code>).
	 * @return the nature descriptor, or <code>null</code>
	 * @since 2.0
	 */
	IProjectNatureDescriptor getNatureDescriptor(String natureId);

    /**
	 * Returns the workspace description. This object is responsible for
	 * defining workspace preferences. The returned value is a modifiable copy
	 * but changes are not automatically applied to the workspace. In order to
	 * changes take effect, <code>IWorkspace.setDescription</code> needs to be
	 * called. The workspace description values are store in the preference
	 * store.
	 *
	 * @return the workspace description
	 */
	IWorkspaceDescription getDescription();

	/**
	 * Returns the root resource of this workspace.
	 *
	 * @return the workspace root
	 */
	IWorkspaceRoot getRoot();

	/**
	 * Returns a factory for obtaining scheduling rules prior to modifying
	 * resources in the workspace.
	 *
	 * @see IResourceRuleFactory
	 * @return a resource rule factory
	 * @since 3.0
	 */
	IResourceRuleFactory getRuleFactory();

	/**
	 * Returns the synchronizer for this workspace.
	 *
	 * @return the synchronizer
	 * @see ISynchronizer
	 */
	ISynchronizer getSynchronizer();

	/**
	 * Returns whether this workspace performs autobuilds.
	 *
	 * @return <code>true</code> if autobuilding is on, <code>false</code>
	 * otherwise
	 */
	boolean isAutoBuilding();

	/**
	 * Returns whether the workspace tree is currently locked. Resource changes
	 * are disallowed during certain types of resource change event
	 * notification. See <code>IResourceChangeEvent</code> for more details.
	 *
	 * @return boolean <code>true</code> if the workspace tree is locked,
	 * <code>false</code> otherwise
	 * @see IResourceChangeEvent
	 * @since 2.1
	 */
	boolean isTreeLocked();

	/**
	 * Creates and returns a new project description for a project with the
	 * given name. This object is useful when creating, moving or copying
	 * projects.
	 * <p>
	 * The project description is initialized to:
	 * </p>
	 * <ul>
	 * <li>the given project name</li>
	 * <li>no references to other projects</li>
	 * <li>an empty build spec</li>
	 * <li>an empty comment</li>
	 * </ul>
	 * <p>
	 * The returned value is writeable.
	 * </p>
	 *
	 * @param projectName the name of the project
	 * @return a new project description
	 */
	IProjectDescription newProjectDescription(String projectName);

	/**
	 * Removes the given resource change listener from this workspace. Has no
	 * effect if an identical listener is not registered.
	 *
	 * @param listener the listener
	 * @see IResourceChangeListener
	 * @see #addResourceChangeListener(IResourceChangeListener)
	 */
	void removeResourceChangeListener(IResourceChangeListener listener);

	/**
	 * Removes the workspace save participant for the given plug-in from this
	 * workspace. If no such participant is registered, no action is taken.
	 * <p>
	 * Once removed, the workspace save participant no longer actively
	 * participates in any future saves of this workspace.
	 * </p>
	 *
	 * @param pluginId the unique identifier of the plug-in
	 * @see ISaveParticipant
	 * @see #addSaveParticipant(String, ISaveParticipant)
	 * @since 3.6
	 */
	void removeSaveParticipant(String pluginId);

	/**
	 * Runs the given action as an atomic workspace operation.
	 * <p>
	 * After running a method that modifies resources in the workspace,
	 * registered listeners receive after-the-fact notification of what just
	 * transpired, in the form of a resource change event. This method allows
	 * clients to call a number of methods that modify resources and only have
	 * resource change event notifications reported at the end of the entire
	 * batch.
	 * </p>
	 * <p>
	 * If this method is called outside the dynamic scope of another such call,
	 * this method runs the action and then reports a single resource change
	 * event describing the net effect of all changes done to resources by the
	 * action.
	 * </p>
	 * <p>
	 * If this method is called in the dynamic scope of another such call, this
	 * method simply runs the action.
	 * </p>
	 * <p>
	 * The supplied scheduling rule is used to determine whether this operation
	 * can be run simultaneously with workspace changes in other threads. If the
	 * scheduling rule conflicts with another workspace change that is currently
	 * running, the calling thread will be blocked until that change completes.
	 * If the action attempts to make changes to the workspace that were not
	 * specified in the scheduling rule, it will fail. If no scheduling rule is
	 * supplied, there are no scheduling restrictions for this operation.
	 * If a non-<code>null</code> scheduling rule is supplied, this operation
	 * must always support cancelation in the case where this operation becomes
	 * blocked by a long running background operation.
	 * </p>
	 * <p>
	 * The AVOID_UPDATE flag controls whether periodic resource change
	 * notifications should occur during the scope of this call. If this flag is
	 * specified, and no other threads modify the workspace concurrently, then
	 * all resource change notifications will be deferred until the end of this
	 * call. If this flag is not specified, the platform may decide to broadcast
	 * periodic resource change notifications during the scope of this call.
	 * </p>
	 * <p>
	 * Flags other than <code>AVOID_UPDATE</code> are ignored.
	 * </p>
	 *
	 * @param action the action to perform
	 * @param rule the scheduling rule to use when running this operation, or
	 * <code>null</code> if there are no scheduling restrictions for this
	 * operation.
	 * @param flags bit-wise or of flag constants (only AVOID_UPDATE is relevant
	 * here)
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 * reporting is not desired.
	 * @exception CoreException if the operation failed.
	 * @exception OperationCanceledException if the operation is canceled. If a
	 * non-<code>null</code> scheduling rule is supplied, cancelation can occur
	 * even if no progress monitor is provided.
	 *
	 * @see #AVOID_UPDATE
	 * @see IResourceRuleFactory
	 * @since 3.11
	 */
	void run(ICoreRunnable action, ISchedulingRule rule, int flags, IProgressMonitor monitor) throws CoreException;

	/**
	 * Identical to {@link #run(ICoreRunnable, ISchedulingRule, int, IProgressMonitor)}.
	 * New code should use that method.
	 *
	 * @param action the action to perform
	 * @param rule the scheduling rule to use when running this operation, or
	 * <code>null</code> if there are no scheduling restrictions for this
	 * operation.
	 * @param flags bit-wise or of flag constants (only AVOID_UPDATE is relevant
	 * here)
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 * reporting is not desired.
	 * @exception CoreException if the operation failed.
	 * @since 3.0
	 */
	void run(IWorkspaceRunnable action, ISchedulingRule rule, int flags, IProgressMonitor monitor) throws CoreException;

	/**
	 * Identical to {@link #run(ICoreRunnable, IProgressMonitor)}.
	 * New code should use that method.
	 *
	 * @param action the action to perform
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 * reporting is not desired
	 * @exception CoreException if the operation failed.
	 */
	void run(IWorkspaceRunnable action, IProgressMonitor monitor) throws CoreException;

    /**
	 * Advises that the caller intends to modify the contents of the given files
	 * in the near future and asks whether modifying all these files would be
	 * reasonable. The files must all exist. This method is used to give the VCM
	 * component an opportunity to check out (or otherwise prepare) the files if
	 * required. (It is provided in this component rather than in the UI so that
	 * "core" (i.e., head-less) clients can use it. Similarly, it is located
	 * outside the VCM component for the convenience of clients that must also
	 * operate in configurations without VCM.)
	 * <p>
	 * A client (such as an editor) should perform a <code>validateEdit</code>
	 * on a file whenever it finds itself in the following position: (a) the
	 * file is marked read-only, and (b) the client believes it likely (not
	 * necessarily certain) that it will modify the file's contents at some
	 * point. A case in point is an editor that has a buffer opened on a file.
	 * When the user starts to dirty the buffer, the editor should check to see
	 * whether the file is read-only. If it is, it should call
	 * <code>validateEdit</code>, and can reasonably expect this call, when
	 * successful, to cause the file to become read-write. An editor should also
	 * be sensitive to a file becoming read-only again even after a successful
	 * <code>validateEdit</code> (e.g., due to the user checking in the file
	 * in a different view); the editor should again call
	 * <code>validateEdit</code> if the file is read-only before attempting to
	 * save the contents of the file.
	 * </p>
	 * <p>
	 * By passing a UI context, the caller indicates that the VCM component may
	 * contact the user to help decide how best to proceed. If no UI context is
	 * provided, the VCM component will make its decision without additional
	 * interaction with the user. If OK is returned, the caller can safely
	 * assume that all of the given files haven been prepared for modification
	 * and that there is good reason to believe that
	 * <code>IFile.setContents</code> (or <code>appendContents</code>)
	 * would be successful on any of them. If the result is not OK, modifying
	 * the given files might not succeed for the reason(s) indicated.
	 * </p>
	 * <p>
	 * If a shell is passed in as the context, the VCM component may bring up a
	 * dialogs to query the user or report difficulties; the shell should be
	 * used to parent any such dialogs; the caller may safely assume that the
	 * reasons for failure will have been made clear to the user. If
	 * {@link IWorkspace#VALIDATE_PROMPT} is passed
	 * as the context, this indicates that the caller does not have access to
	 * a UI context but would still like the user to be prompted if required.
	 * If <code>null</code> is passed, the user should not be contacted; any
	 * failures should be reported via the result; the caller may chose to
	 * present these to the user however they see fit. The ideal implementation
	 * of this method is transactional; no files would be affected unless the
	 * go-ahead could be given. (In practice, there may be no feasible way to
	 * ensure such changes get done atomically.)
	 * </p>
	 * <p>
	 * The method calls <code>FileModificationValidator.validateEdit</code>
	 * for the file modification validator (if provided by the VCM plug-in).
	 * When there is no file modification validator, this method returns a
	 * status with an <code>IResourceStatus.READ_ONLY_LOCAL</code> code if one
	 * of the files is read-only, and a status with an <code>IStatus.OK</code>
	 * code otherwise.
	 * </p>
	 * <p>
	 * This method may be called from any thread. If the UI context is used, it
	 * is the responsibility of the implementor of
	 * <code>FileModificationValidator.validateEdit</code> to interact with
	 * the UI context in an appropriate thread.
	 * </p>
	 *
	 * @param files the files that are to be modified; these files must all
	 * exist in the workspace
	 * @param context either {@link IWorkspace#VALIDATE_PROMPT},
	 * or the <code>com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.swt.widgets.Shell</code> that is
	 * to be used to parent any dialogs with the user, or <code>null</code> if
	 * there is no UI context (declared as an <code>Object</code> to avoid any
	 * direct references on the SWT component)
	 * @return a status object that is <code>OK</code> if things are fine,
	 * otherwise a status describing reasons why modifying the given files is not
	 * reasonable. A status with a severity of <code>CANCEL</code> is returned
	 * if the validation was canceled, indicating the edit should not proceed.
	 * @see IResourceRuleFactory#validateEditRule(IResource[])
	 * @since 2.0
	 */
	IStatus validateEdit(IFile[] files, Object context);

    /**
	 * Validates the given {@link URI} as the location of the given resource on disk.
	 * The location must be either an absolute URI, or a relative URI
	 * whose first segment is the name of a defined workspace path variable.
	 * A link location must obey the following rules:
	 * <ul>
	 * <li>must not overlap with the platform's metadata directory</li>
	 * <li>must not be the same as or a parent of the root directory of the
	 * project the linked resource is contained in</li>
	 * </ul>
	 * <p>
	 * This method also checks that the given resource can legally become a
	 * linked resource. This includes the following restrictions:
	 * <ul>
	 * <li>must have a project as its immediate parent</li>
	 * <li>project natures and the team hook may disallow linked resources on
	 * projects they are associated with</li>
	 * <li>the global workspace preference to disable linking,
	 * <code>ResourcesPlugin.PREF_DISABLE_LINKING</code> must not be set to
	 * &quot;true&quot;</li>
	 * </ul>
	 * <p>
	 * This method will return a status with severity <code>IStatus.ERROR</code>
	 * if the location does not obey the above rules. Also, this method will
	 * return a status with severity <code>IStatus.WARNING</code> if the
	 * location overlaps the location of any existing resource in the workspace.
	 * </p>
	 * <p>
	 * Note: this method does not consider whether files or directories exist in
	 * the file system at the specified location.
	 *
	 * @param resource the resource to validate the location for
	 * @param location the location of the linked resource contents in some file system
	 * @return a status object with code <code>IStatus.OK</code> if the given
	 * location is valid as the linked resource location, otherwise a status
	 * object with severity <code>IStatus.WARNING</code> or
	 * <code>IStatus.ERROR</code> indicating what is wrong with the location
	 * @see IStatus#OK
	 * @see ResourcesPlugin#PREF_DISABLE_LINKING
	 * @since 3.2
	 */
	IStatus validateLinkLocationURI(IResource resource, URI location);

	/**
	 * Validates the given string as the name of a resource valid for one of the
	 * given types.
	 * <p>
	 * In addition to the basic restrictions on paths in general (see
	 * {@link IPath#isValidSegment(String)}), a resource name must also not
	 * contain any characters or substrings that are not valid on the file system
	 * on which workspace root is located. In addition, the names "." and ".."
	 * are reserved due to their special meaning in file system paths.
	 * </p>
	 * <p>
	 * This validation check is done automatically as a resource is created (but
	 * not when the resource handle is constructed); this means that any
	 * resource that exists can be safely assumed to have a valid name and path.
	 * Note that the name of the workspace root resource is inherently invalid.
	 * </p>
	 *
	 * @param segment the name segment to be checked
	 * @param typeMask bitwise-or of the resource type constants (
	 * <code>FILE</code>,<code>FOLDER</code>,<code>PROJECT</code> or
	 * <code>ROOT</code>) indicating expected resource type(s)
	 * @return a status object with code <code>IStatus.OK</code> if the given
	 * string is valid as a resource name, otherwise a status object indicating
	 * what is wrong with the string
	 * @see IResource#PROJECT
	 * @see IResource#FOLDER
	 * @see IResource#FILE
	 * @see IStatus#OK
	 */
	IStatus validateName(String segment, int typeMask);

    /**
	 * Validates the given URI as the location of the given project.
	 * The location must be either an absolute URI, or a relative URI
	 * whose first segment is the name of a defined workspace path variable.
	 * A project location must obey the following rules:
	 * <ul>
	 * <li>must not be the same as another open or closed project</li>
	 * <li>must not occupy the default location for any project, whether existing or not</li>
	 * <li>must not be the same as or a parent of the platform's working directory</li>
	 * <li>must not be the same as or a child of the location of any existing
	 * linked resource in the given project</li>
	 * </ul>
	 * <p>
	 * Note: this method does not consider whether files or directories exist in
	 * the file system at the specified path.
	 * </p>
	 *
	 * @param project the project to validate the location for, can be <code>null</code>
	 * if non default project location is validated
	 * @param location the location of the project contents on disk, or <code>null</code>
	 * if the default project location is used
	 * @return a status object with code <code>IStatus.OK</code> if the given
	 * location is valid as the project content location, otherwise a status
	 * object indicating what is wrong with the location
	 * @see IProjectDescription#getLocationURI()
	 * @see IProjectDescription#setLocationURI(URI)
	 * @see IStatus#OK
	 * @since 3.2
	 */
	IStatus validateProjectLocationURI(IProject project, URI location);

	/**
	 * Returns the path variable manager for this workspace.
	 *
	 * @return the path variable manager
	 * @see IPathVariableManager
	 * @since 2.1
	 */
	IPathVariableManager getPathVariableManager();

	/**
	 * Creates the files and sets/replaces the files content. This is a batch
	 * version of {@code IFile.write(...)}. The files are touched in no particuar
	 * order and the operation is not guaranteed to be atomic: Exceptions may relate
	 * to one or multiple files - some files may have been created and other not.
	 * IResourceChangeListener may receive one or multiple events.
	 *
	 * @param contentMap      the new content bytes for each IFile. The map must not
	 *                        be null and must not contain null keys or null values.
	 * @param force           a flag controlling how to deal with resources that are
	 *                        not in sync with the local file system
	 * @param derived         Specifying this flag is equivalent to atomically
	 *                        calling {@link IResource#setDerived(boolean)}
	 *                        immediately after creating the resource or atomically
	 *                        setting the derived flag before setting the content of
	 *                        an already existing file if derived==true. A value of
	 *                        false will not update the derived flag of an existing
	 *                        file.
	 * @param keepHistory     a flag indicating whether or not store the current
	 *                        contents in the local history if the file did already
	 *                        exist
	 * @param monitor         a progress monitor, or <code>null</code> if progress
	 *                        reporting is not desired
	 * @param executorService a ExecutorService to support parallel IO
	 * @throws CoreException if this method fails or is canceled.
	 * @since 3.22
	 * @see IFile#write(byte[], boolean, boolean, boolean, IProgressMonitor)
	 */
	default void write(Map<IFile, byte[]> contentMap, boolean force, boolean derived, boolean keepHistory,
        IProgressMonitor monitor, ExecutorService executorService) throws CoreException {
		// this code is just meant as an explanation and
		// meant to be overridden with a parallel implementation for local files:
		Objects.requireNonNull(contentMap);
		SubMonitor subMon = SubMonitor.convert(monitor, contentMap.size());
		for (Entry<IFile, byte[]> e : contentMap.entrySet()) {
			e.getKey().write(e.getValue(), force, derived, keepHistory, subMon.split(1));
		}
	}
}
