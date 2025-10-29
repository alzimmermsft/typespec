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

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.ICoreRunnable;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.OperationCanceledException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.jobs.ISchedulingRule;

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
 * ServiceTracker&lt;IWorkspace, IWorkspace&gt; workspaceTracker
 *     = new ServiceTracker&lt;&gt;(bundleContext, IWorkspace.class, null);
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
 *     // do something with the workspace
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
public interface IWorkspace {
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
     * Returns whether the workspace tree is currently locked. Resource changes
     * are disallowed during certain types of resource change event
     * notification. See <code>IResourceChangeEvent</code> for more details.
     *
     * @return boolean <code>true</code> if the workspace tree is locked,
     * <code>false</code> otherwise
     * @since 2.1
     */
    boolean isTreeLocked();

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
     * Returns the path variable manager for this workspace.
     *
     * @return the path variable manager
     * @see IPathVariableManager
     * @since 2.1
     */
    IPathVariableManager getPathVariableManager();
}
