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
 *     Red Hat Incorporated - get/setResourceAttribute code
 *     Oakland Software Incorporated - added getSessionProperties and getPersistentProperties
 *     Serge Beauchamp (Freescale Semiconductor) - [252996] add hasFilters()
 *     Serge Beauchamp (Freescale Semiconductor) - [229633] Group and Project Path Variable Support
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.QualifiedName;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.jobs.ISchedulingRule;

import java.net.URI;
import java.util.Map;

/**
 * The workspace analog of file system files
 * and directories. There are exactly four <i>types</i> of resource:
 * files, folders, projects and the workspace root.
 * <p>
 * File resources are similar to files in that they
 * hold data directly. Folder resources are analogous to directories in that they
 * hold other resources but cannot directly hold data. Project resources
 * group files and folders into reusable clusters. The workspace root is the
 * top level resource under which all others reside.
 * </p>
 * <p>
 * Features of resources:
 * </p>
 * <ul>
 * <li><code>IResource</code> objects are <i>handles</i> to state maintained
 * by a workspace. That is, resource objects do not actually contain data
 * themselves but rather represent resource state and give it behavior. Programmers
 * are free to manipulate handles for resources that do not exist in a workspace
 * but must keep in mind that some methods and operations require that an actual
 * resource be available.</li>
 * <li>Resources have two different kinds of properties as detailed below. All
 * properties are keyed by qualified names.
 * <ul>
 * <li>Session properties: Session properties live for the lifetime of one execution of
 * the workspace. They are not stored on disk. They can carry arbitrary
 * object values. Clients should be aware that these values are kept in memory
 * at all times and, as such, the values should not be large.</li>
 * <li>Persistent properties: Persistent properties have string values which are stored
 * on disk across platform sessions. The value of a persistent property is a
 * string which should be short (i.e., under 2KB). </li>
 * </ul>
 * </li>
 * <li>Resources are identified by type and by their <i>path</i>, which is similar to a file system
 * path. The name of a resource is the last segment of its path. A resource's parent
 * is located by removing the last segment (the resource's name) from the resource's full path.</li>
 * <li>Resources can be local or non-local. A non-local resource is one whose
 * contents and properties have not been fetched from a repository.</li>
 * <li><i>Phantom</i> resources represent incoming additions or outgoing deletions
 * which have yet to be reconciled with a synchronization partner. </li>
 * </ul>
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IResource extends ISchedulingRule {

    /*
     * ====================================================================
     * Constants defining resource types: There are four possible resource types
     * and their type constants are in the integer range 1 to 8 as defined below.
     * ====================================================================
     */

    /**
     * Type constant (bit mask value 1) which identifies file resources.
     *
     * @see IResource#getType()
     * @see IFile
     */
    int FILE = 0x1;

    /**
     * Type constant (bit mask value 2) which identifies folder resources.
     *
     * @see IResource#getType()
     * @see IFolder
     */
    int FOLDER = 0x2;

    /**
     * Type constant (bit mask value 4) which identifies project resources.
     *
     * @see IResource#getType()
     * @see IProject
     */
    int PROJECT = 0x4;

    /**
     * Type constant (bit mask value 8) which identifies the root resource.
     *
     * @see IResource#getType()
     * @see IWorkspaceRoot
     */
    int ROOT = 0x8;

    /*
     * ====================================================================
     * Constants defining the depth of resource tree traversal:
     * ====================================================================
     */

    /**
     * Depth constant (value 0) indicating this resource, but not any of its members.
     */
    int DEPTH_ZERO = 0;

    /**
     * Depth constant (value 1) indicating this resource and its direct members.
     */
    int DEPTH_ONE = 1;

    /**
     * Depth constant (value 2) indicating this resource and its direct and
     * indirect members at any depth.
     */
    int DEPTH_INFINITE = 2;

    /*
     * ====================================================================
     * Constants for update flags for delete, move, copy, open, etc.:
     * ====================================================================
     */

    /**
     * Update flag constant (bit mask value 64) indicating that setting the
     * project description should not attempt to configure and de-configure
     * natures.
     *
     * @since 3.0
     */
    int AVOID_NATURE_CONFIG = 0x40;

    /**
     * Update flag constant (bit mask value 512) indicating that ancestor
     * resources of the target resource should be checked.
     *
     * @see IResource#isLinked(int)
     * @since 3.2
     */
    int CHECK_ANCESTORS = 0x200;

    /*
     * ====================================================================
     * Other constants:
     * ====================================================================
     */

    /**
     * Modification stamp constant (value -1) indicating no modification stamp is
     * available.
     *
     * @see #getModificationStamp()
     */
    int NULL_STAMP = -1;

    /**
     * General purpose zero-valued bit mask constant. Useful whenever you need to
     * supply a bit mask with no bits set.
     * <p>
     * Example usage:
     * </p>
     *
     * <pre>
     * <code>
     *    delete(IResource.NONE, null)
     * </code>
     * </pre>
     *
     * @since 2.0
     */
    int NONE = 0;

    /**
     * Accepts the given visitor for an optimized traversal.
     * The visitor's <code>visit</code> method is called, and is provided with a
     * proxy to this resource. The proxy is a transient object that can be queried
     * very quickly for information about the resource. If the actual resource
     * handle is needed, it can be obtained from the proxy. Requesting the resource
     * handle, or the full path of the resource, will degrade performance of the
     * visit.
     * <p>
     * The entire subtree under the given resource is traversed to infinite depth,
     * unless the visitor ignores a subtree by returning <code>false</code> from its
     * <code>visit</code> method.
     * </p>
     * <p>
     * This is a convenience method, fully equivalent to
     * <code>accept(visitor, IResource.DEPTH_INFINITE, memberFlags)</code>.
     * </p>
     * <p>No guarantees are made about the behavior of this method if resources
     * are deleted or added during the traversal of this resource hierarchy. If
     * resources are deleted during the traversal, they may still be passed to the
     * visitor; if resources are created, they may not be passed to the visitor. If
     * resources other than the one being visited are modified during the traversal,
     * the resource proxy may contain stale information when that resource is
     * visited.
     * </p>
     * <p>
     * If the {@link IContainer#INCLUDE_PHANTOMS} flag is not specified in the member
     * flags (recommended), only member resources that exist will be visited.
     * If the {@link IContainer#INCLUDE_PHANTOMS} flag is specified, the visit will
     * also include any phantom member resource that the workspace is keeping track of.
     * </p>
     * <p>
     * If the {@link IContainer#INCLUDE_TEAM_PRIVATE_MEMBERS} flag is not specified
     * (recommended), team private members will not be visited. If the
     * {@link IContainer#INCLUDE_TEAM_PRIVATE_MEMBERS} flag is specified in the member
     * flags, team private member resources are visited as well.
     * </p>
     * <p>
     * If the {@link IContainer#INCLUDE_HIDDEN} flag is not specified (recommended),
     * hidden resources will not be visited. If the {@link IContainer#INCLUDE_HIDDEN} flag is specified
     * in the member flags, hidden resources are visited as well.
     * </p>
     * <p>
     * If the {@link IContainer#DO_NOT_CHECK_EXISTENCE} flag is not specified (recommended),
     * the resource is checked for existence before the visitor's <code>visit</code>
     * method is called. If the {@link IContainer#DO_NOT_CHECK_EXISTENCE} flag is specified
     * in the member flags, the resource is not checked for existence before the visitor's
     * <code>visit</code> method is called. Children of the resource are never checked
     * for existence.
     * </p>
     *
     * @param visitor the visitor
     * @param memberFlags bit-wise or of member flag constants
     * ({@link IContainer#INCLUDE_PHANTOMS}, {@link IContainer#INCLUDE_TEAM_PRIVATE_MEMBERS}
     * and {@link IContainer#INCLUDE_HIDDEN}) indicating which members are of interest
     * and {@link IContainer#DO_NOT_CHECK_EXISTENCE} if the resource on which the method is
     * called should not be checked for existence
     * @exception CoreException if this request fails. Reasons include:
     * <ul>
     * <li> the {@link IContainer#INCLUDE_PHANTOMS} flag is not specified and
     * this resource does not exist.</li>
     * <li> the {@link IContainer#INCLUDE_PHANTOMS} flag is not specified and
     * this resource is a project that is not open.</li>
     * <li> the {@link IContainer#DO_NOT_CHECK_EXISTENCE} flag is not specified and
     * this resource does not exist.</li>
     * <li> The visitor failed with this exception.</li>
     * </ul>
     * @since 2.1
     */
    void accept(IResourceProxyVisitor visitor, int memberFlags) throws CoreException;

    /**
     * Accepts the given visitor for an optimized traversal.
     * The visitor's <code>visit</code> method is called, and is provided with a
     * proxy to this resource. The proxy is a transient object that can be queried
     * very quickly for information about the resource. If the actual resource
     * handle is needed, it can be obtained from the proxy. Requesting the resource
     * handle, or the full path of the resource, will degrade performance of the
     * visit.
     * <p>
     * The entire subtree under the given resource is traversed to the supplied depth,
     * unless the visitor ignores a subtree by returning <code>false</code> from its
     * <code>visit</code> method.
     * </p>
     * <p>No guarantees are made about the behavior of this method if resources
     * are deleted or added during the traversal of this resource hierarchy. If
     * resources are deleted during the traversal, they may still be passed to the
     * visitor; if resources are created, they may not be passed to the visitor. If
     * resources other than the one being visited are modified during the traversal,
     * the resource proxy may contain stale information when that resource is
     * visited.
     * </p>
     * <p>
     * If the {@link IContainer#INCLUDE_PHANTOMS} flag is not specified in the member
     * flags (recommended), only member resources that exist will be visited.
     * If the {@link IContainer#INCLUDE_PHANTOMS} flag is specified, the visit will
     * also include any phantom member resource that the workspace is keeping track of.
     * </p>
     * <p>
     * If the {@link IContainer#INCLUDE_TEAM_PRIVATE_MEMBERS} flag is not specified
     * (recommended), team private members will not be visited. If the
     * {@link IContainer#INCLUDE_TEAM_PRIVATE_MEMBERS} flag is specified in the member
     * flags, team private member resources are visited as well.
     * </p>
     * <p>
     * If the {@link IContainer#INCLUDE_HIDDEN} flag is not specified (recommended),
     * hidden resources will not be visited. If the {@link IContainer#INCLUDE_HIDDEN} flag is specified
     * in the member flags, hidden resources are visited as well.
     * </p>
     * <p>
     * If the {@link IContainer#DO_NOT_CHECK_EXISTENCE} flag is not specified (recommended),
     * the resource is checked for existence before the visitor's <code>visit</code>
     * method is called. If the {@link IContainer#DO_NOT_CHECK_EXISTENCE} flag is specified
     * in the member flags, the resource is not checked for existence before the visitor's
     * <code>visit</code> method is called. Children of the resource are never checked
     * for existence.
     * </p>
     *
     * @param visitor the visitor
     * @param depth the depth to which members of this resource should be
     * visited. One of {@link IResource#DEPTH_ZERO}, {@link IResource#DEPTH_ONE},
     * or {@link IResource#DEPTH_INFINITE}.
     * @param memberFlags bit-wise or of member flag constants
     * ({@link IContainer#INCLUDE_PHANTOMS}, {@link IContainer#INCLUDE_TEAM_PRIVATE_MEMBERS}
     * and {@link IContainer#INCLUDE_HIDDEN}) indicating which members are of interest
     * and {@link IContainer#DO_NOT_CHECK_EXISTENCE} if the resource on which the method is
     * called should not be checked for existence
     * @exception CoreException if this request fails. Reasons include:
     * <ul>
     * <li> the {@link IContainer#INCLUDE_PHANTOMS} flag is not specified and
     * this resource does not exist.</li>
     * <li> the {@link IContainer#INCLUDE_PHANTOMS} flag is not specified and
     * this resource is a project that is not open.</li>
     * <li> the {@link IContainer#DO_NOT_CHECK_EXISTENCE} flag is not specified and
     * this resource does not exist.</li>
     * <li> The visitor failed with this exception.</li>
     * </ul>
     * @since 3.8
     */
    void accept(IResourceProxyVisitor visitor, int depth, int memberFlags) throws CoreException;

    /**
     * Accepts the given visitor.
     * The visitor's <code>visit</code> method is called with this
     * resource. If the visitor returns <code>true</code>, this method
     * visits this resource's members.
     * <p>
     * This is a convenience method, fully equivalent to
     * <code>accept(visitor, IResource.DEPTH_INFINITE, IResource.NONE)</code>.
     * </p>
     *
     * @param visitor the visitor
     * @exception CoreException if this method fails. Reasons include:
     * <ul>
     * <li> This resource does not exist.</li>
     * <li> The visitor failed with this exception.</li>
     * </ul>
     */
    void accept(IResourceVisitor visitor) throws CoreException;

    /**
     * Accepts the given visitor.
     * The visitor's <code>visit</code> method is called with this
     * resource. If the visitor returns <code>false</code>,
     * this resource's members are not visited.
     * <p>
     * The subtree under the given resource is traversed to the supplied depth.
     * </p>
     * <p>
     * This is a convenience method, fully equivalent to:
     * </p>
     * 
     * <pre>
     * accept(visitor, depth, includePhantoms ? IContainer.INCLUDE_PHANTOMS : IResource.NONE);
     * </pre>
     *
     * @param visitor the visitor
     * @param depth the depth to which members of this resource should be
     * visited. One of {@link IResource#DEPTH_ZERO}, {@link IResource#DEPTH_ONE},
     * or {@link IResource#DEPTH_INFINITE}.
     * @param includePhantoms <code>true</code> if phantom resources are
     * of interest; <code>false</code> if phantom resources are not of
     * interest.
     * @exception CoreException if this request fails. Reasons include:
     * <ul>
     * <li> <code>includePhantoms</code> is <code>false</code> and
     * this resource does not exist.</li>
     * <li> <code>includePhantoms</code> is <code>true</code> and
     * this resource does not exist and is not a phantom.</li>
     * <li> The visitor failed with this exception.</li>
     * </ul>
     * @see IResource#isPhantom()
     * @see IResourceVisitor#visit(IResource)
     * @see IResource#DEPTH_ZERO
     * @see IResource#DEPTH_ONE
     * @see IResource#DEPTH_INFINITE
     * @see IResource#accept(IResourceVisitor,int,int)
     */
    void accept(IResourceVisitor visitor, int depth, boolean includePhantoms) throws CoreException;

    /**
     * Accepts the given visitor.
     * The visitor's <code>visit</code> method is called with this
     * resource. If the visitor returns <code>false</code>,
     * this resource's members are not visited.
     * <p>
     * The subtree under the given resource is traversed to the supplied depth.
     * </p>
     * <p>
     * No guarantees are made about the behavior of this method if resources are
     * deleted or added during the traversal of this resource hierarchy. If
     * resources are deleted during the traversal, they may still be passed to the
     * visitor; if resources are created, they may not be passed to the visitor.
     * </p>
     * <p>
     * If the {@link IContainer#INCLUDE_PHANTOMS} flag is not specified in the member
     * flags (recommended), only member resources that exists are visited.
     * If the {@link IContainer#INCLUDE_PHANTOMS} flag is specified, the visit also
     * includes any phantom member resource that the workspace is keeping track of.
     * </p>
     * <p>
     * If the {@link IContainer#INCLUDE_TEAM_PRIVATE_MEMBERS} flag is not specified
     * (recommended), team private members are not visited. If the
     * {@link IContainer#INCLUDE_TEAM_PRIVATE_MEMBERS} flag is specified in the member
     * flags, team private member resources are visited as well.
     * </p>
     * <p>
     * If the {@link IContainer#EXCLUDE_DERIVED} flag is not specified
     * (recommended), derived resources are visited. If the
     * {@link IContainer#EXCLUDE_DERIVED} flag is specified in the member
     * flags, derived resources are not visited.
     * </p>
     * <p>
     * If the {@link IContainer#INCLUDE_HIDDEN} flag is not specified (recommended),
     * hidden resources will not be visited. If the {@link IContainer#INCLUDE_HIDDEN} flag is specified
     * in the member flags, hidden resources are visited as well.
     * </p>
     * <p>
     * If the {@link IContainer#DO_NOT_CHECK_EXISTENCE} flag is not specified (recommended),
     * the resource is checked for existence before the visitor's <code>visit</code>
     * method is called. If the {@link IContainer#DO_NOT_CHECK_EXISTENCE} flag is specified
     * in the member flags, the resource is not checked for existence before the visitor's
     * <code>visit</code> method is called. Children of the resource are never checked
     * for existence.
     * </p>
     *
     * @param visitor the visitor
     * @param depth the depth to which members of this resource should be
     * visited. One of {@link IResource#DEPTH_ZERO}, {@link IResource#DEPTH_ONE},
     * or {@link IResource#DEPTH_INFINITE}.
     * @param memberFlags bit-wise or of member flag constants
     * ({@link IContainer#INCLUDE_PHANTOMS}, {@link IContainer#INCLUDE_TEAM_PRIVATE_MEMBERS},
     * {@link IContainer#INCLUDE_HIDDEN} and {@link IContainer#EXCLUDE_DERIVED}) indicating
     * which members are of interest and {@link IContainer#DO_NOT_CHECK_EXISTENCE}
     * if the resource on which the method is called should not be checked for existence
     * @exception CoreException if this request fails. Reasons include:
     * <ul>
     * <li> the {@link IContainer#INCLUDE_PHANTOMS} flag is not specified and
     * this resource does not exist.</li>
     * <li> the {@link IContainer#INCLUDE_PHANTOMS} flag is not specified and
     * this resource is a project that is not open.</li>
     * <li> the {@link IContainer#DO_NOT_CHECK_EXISTENCE} flag is not specified and
     * this resource does not exist.</li>
     * <li> The visitor failed with this exception.</li>
     * </ul>
     * @since 2.0
     */
    void accept(IResourceVisitor visitor, int depth, int memberFlags) throws CoreException;

    /**
     * Creates and returns the marker with the specified type on this resource.
     * Marker type ids should be the id of an extension installed in the
     * <code>com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.markers</code>
     * extension
     * point. The specified type string must not be <code>null</code>.
     *
     * @param type the type of the marker to create
     * @return the handle of the new marker
     * @exception CoreException if this method fails. Reasons include:
     * <ul>
     * <li> This resource does not exist.</li>
     * <li> This resource is a project that is not open.</li>
     * </ul>
     * @see IResourceRuleFactory#markerRule(IResource)
     */
    IMarker createMarker(String type) throws CoreException;

    /**
     * Creates and returns the marker with the specified type on this resource.
     * Marker type ids should be the id of an extension installed in the
     * <code>com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.markers</code>
     * extension point. The
     * specified type string must not be <code>null</code>.
     *
     * <p>
     * Note: default implementation is provided for backwards compatibility only and
     * is not optimized for performance.
     *
     * @param type the type of the marker to create
     * @param attributes a map of attribute names to attribute values (key type :
     * <code>String</code> value type : <code>String</code>,
     * <code>Integer</code>, or <code>Boolean</code>) or
     * <code>null</code>
     * @return the handle of the new marker
     * @exception CoreException if this method fails. Reasons include:
     * <ul>
     * <li>This resource does not exist.</li>
     * <li>This resource is a project that is not
     * open.</li>
     * </ul>
     * @see IResourceRuleFactory#markerRule(IResource)
     *
     * @since 3.14
     */
    default IMarker createMarker(String type, Map<String, ? extends Object> attributes) throws CoreException {
        IMarker marker = createMarker(type);
        marker.setAttributes(attributes);
        return marker;
    }

    /**
     * Deletes all markers on this resource of the given type, and,
     * optionally, deletes such markers from its children. If <code>includeSubtypes</code>
     * is <code>false</code>, only markers whose type exactly matches
     * the given type are deleted.
     * <p>
     * This method changes resources; these changes will be reported
     * in a subsequent resource change event.
     * </p>
     *
     * @param type the type of marker to consider, or <code>null</code> to indicate all types
     * @param includeSubtypes whether or not to consider sub-types of the given type
     * @param depth how far to recurse (see <code>IResource.DEPTH_* </code>)
     * @exception CoreException if this method fails. Reasons include:
     * <ul>
     * <li> This resource does not exist.</li>
     * <li> This resource is a project that is not open.</li>
     * <li> Resource changes are disallowed during certain types of resource change
     * event notification. See <code>IResourceChangeEvent</code> for more details.</li>
     * </ul>
     * @see IResource#DEPTH_ZERO
     * @see IResource#DEPTH_ONE
     * @see IResource#DEPTH_INFINITE
     * @see IResourceRuleFactory#markerRule(IResource)
     */
    void deleteMarkers(String type, boolean includeSubtypes, int depth) throws CoreException;

    /**
     * Compares two objects for equality;
     * for resources, equality is defined in terms of their handles:
     * same resource type, equal full paths, and identical workspaces.
     * Resources are not equal to objects other than resources.
     *
     * @param other the other object
     * @return an indication of whether the objects are equals
     * @see #getType()
     * @see #getFullPath()
     * @see #getWorkspace()
     */
    @Override
    boolean equals(Object other);

    /**
     * Returns whether this resource exists in the workspace.
     * <p>
     * <code>IResource</code> objects are lightweight handle objects
     * used to access resources in the workspace. However, having a
     * handle object does not necessarily mean the workspace really
     * has such a resource. When the workspace does have a genuine
     * resource of a matching type, the resource is said to
     * <em>exist</em>, and this method returns <code>true</code>;
     * in all other cases, this method returns <code>false</code>.
     * In particular, it returns <code>false</code> if the workspace
     * has no resource at that path, or if it has a resource at that
     * path with a type different from the type of this resource handle.
     * </p>
     * <p>
     * Note that no resources ever exist under a project
     * that is closed; opening a project may bring some
     * resources into existence.
     * </p>
     * <p>
     * The name and path of a resource handle may be invalid.
     * However, validation checks are done automatically as a
     * resource is created; this means that any resource that exists
     * can be safely assumed to have a valid name and path.
     * </p>
     *
     * @return <code>true</code> if the resource exists, otherwise
     * <code>false</code>
     */
    boolean exists();

    /**
     * Returns all markers of the specified type on this resource,
     * and, optionally, on its children. If <code>includeSubtypes</code>
     * is <code>false</code>, only markers whose type exactly matches
     * the given type are returned. Returns an empty array if there
     * are no matching markers.
     *
     * @param type the type of marker to consider, or <code>null</code> to indicate all types
     * @param includeSubtypes whether or not to consider sub-types of the given type
     * @param depth how far to recurse (see <code>IResource.DEPTH_* </code>)
     * @return an array of markers
     * @exception CoreException if this method fails. Reasons include:
     * <ul>
     * <li> This resource does not exist.</li>
     * <li> This resource is a project that is not open.</li>
     * </ul>
     * @see IResource#DEPTH_ZERO
     * @see IResource#DEPTH_ONE
     * @see IResource#DEPTH_INFINITE
     */
    IMarker[] findMarkers(String type, boolean includeSubtypes, int depth) throws CoreException;

    /**
     * Returns the file extension portion of this resource's name,
     * or <code>null</code> if it does not have one.
     * <p>
     * The file extension portion is defined as the string
     * following the last period (".") character in the name.
     * If there is no period in the name, the path has no
     * file extension portion. If the name ends in a period,
     * the file extension portion is the empty string.
     * </p>
     * <p>
     * This is a resource handle operation; the resource need
     * not exist.
     * </p>
     *
     * @return a string file extension
     * @see #getName()
     */
    String getFileExtension();

    /**
     * Returns the full, absolute path of this resource relative to the
     * workspace.
     * <p>
     * This is a resource handle operation; the resource need
     * not exist.
     * If this resource does exist, its path can be safely assumed to be valid.
     * </p>
     * <p>
     * A resource's full path indicates the route from the root of the workspace
     * to the resource. Within a workspace, there is exactly one such path
     * for any given resource. The first segment of these paths name a project;
     * remaining segments, folders and/or files within that project.
     * The returned path never has a trailing separator. The path of the
     * workspace root is <code>Path.ROOT</code>.
     * </p>
     * <p>
     * Since absolute paths contain the name of the project, they are
     * vulnerable when the project is renamed. For most situations,
     * project-relative paths are recommended over absolute paths.
     * </p>
     *
     * @return the absolute path of this resource
     * @see #getProjectRelativePath()
     * @see IPath#ROOT
     */
    IPath getFullPath();

    /**
     * Returns the absolute path in the local file system to this resource,
     * or <code>null</code> if no path can be determined.
     * <p>
     * If this resource is the workspace root, this method returns
     * the absolute local file system path of the platform working area.
     * </p><p>
     * If this resource is a project that exists in the workspace, this method
     * returns the path to the project's local content area. This is true regardless
     * of whether the project is open or closed. This value will be null in the case
     * where the location is relative to an undefined workspace path variable.
     * </p><p>
     * If this resource is a linked resource under a project that is open, this
     * method returns the resolved path to the linked resource's local contents.
     * This value will be null in the case where the location is relative to an
     * undefined workspace path variable.
     * </p><p>
     * If this resource is a file or folder under a project that exists, or a
     * linked resource under a closed project, this method returns a (non-
     * <code>null</code>) path computed from the location of the project's local
     * content area and the project- relative path of the file or folder. This is
     * true regardless of whether the file or folders exists, or whether the project
     * is open or closed. In the case of linked resources, the location of a linked resource
     * within a closed project is too computed from the location of the
     * project's local content area and the project-relative path of the resource. If the
     * linked resource resides in an open project then its location is computed
     * according to the link.
     * </p><p>
     * If this resource is a project that does not exist in the workspace,
     * or a file or folder below such a project, this method returns
     * <code>null</code>. This method also returns <code>null</code> if called
     * on a resource that is not stored in the local file system. For such resources
     * {@link #getLocationURI()} should be used instead.
     * </p>
     *
     * @return the absolute path of this resource in the local file system,
     * or <code>null</code> if no path can be determined
     */
    IPath getLocation();

    /**
     * Returns the absolute URI of this resource,
     * or <code>null</code> if no URI can be determined.
     * <p>
     * If this resource is the workspace root, this method returns
     * the absolute location of the platform working area.
     * </p><p>
     * If this resource is a project that exists in the workspace, this method
     * returns the URI to the project's local content area. This is true regardless
     * of whether the project is open or closed. This value will be null in the case
     * where the location is relative to an undefined workspace path variable.
     * </p><p>
     * If this resource is a linked resource under a project that is open, this
     * method returns the resolved URI to the linked resource's local contents.
     * This value will be null in the case where the location is relative to an
     * undefined workspace path variable.
     * </p><p>
     * If this resource is a file or folder under a project that exists, or a
     * linked resource under a closed project, this method returns a (non-
     * <code>null</code>) URI computed from the location of the project's local
     * content area and the project- relative path of the file or folder. This is
     * true regardless of whether the file or folders exists, or whether the project
     * is open or closed. In the case of linked resources, the location of a linked resource
     * within a closed project is computed from the location of the
     * project's local content area and the project-relative path of the resource. If the
     * linked resource resides in an open project then its location is computed
     * according to the link.
     * </p><p>
     * If this resource is a project that does not exist in the workspace,
     * or a file or folder below such a project, this method returns
     * <code>null</code>.
     * </p>
     *
     * @return the absolute URI of this resource,
     * or <code>null</code> if no URI can be determined
     * @since 3.2
     */
    URI getLocationURI();

    /**
     * Returns a non-negative modification stamp, or {@link #NULL_STAMP} if
     * the resource does not exist or is not local or is not accessible.
     * <p>
     * A resource's modification stamp gets updated each time a resource is modified.
     * If a resource's modification stamp is the same, the resource has not changed.
     * Conversely, if a resource's modification stamp is different, some aspect of it
     * (other than properties) has been modified at least once (possibly several times).
     * Resource modification stamps are preserved across project close/re-open,
     * and across workspace shutdown/restart.
     * The magnitude or sign of the numerical difference between two modification stamps
     * is not significant.
     * </p>
     * <p>
     * The following things affect a resource's modification stamp:
     * </p>
     * <ul>
     * <li>creating a non-project resource (changes from {@link #NULL_STAMP})</li>
     * <li>changing the contents of a file</li>
     * <li><code>touch</code>ing a resource</li>
     * <li>setting the attributes of a project presented in a project description</li>
     * <li>deleting a resource (changes to {@link #NULL_STAMP})</li>
     * <li>moving a resource (source changes to {@link #NULL_STAMP},
     * destination changes from {@link #NULL_STAMP})</li>
     * <li>copying a resource (destination changes from {@link #NULL_STAMP})</li>
     * <li>making a resource local</li>
     * <li>closing a project (changes to {@link #NULL_STAMP})</li>
     * <li>opening a project (changes from {@link #NULL_STAMP})</li>
     * <li>adding or removing a project nature (changes from {@link #NULL_STAMP})</li>
     * </ul>
     * The following things do not affect a resource's modification stamp:
     * <ul>
     * <li>"reading" a resource</li>
     * <li>adding or removing a member of a project or folder</li>
     * <li>setting a session property</li>
     * <li>setting a persistent property</li>
     * <li>saving the workspace</li>
     * <li>shutting down and re-opening a workspace</li>
     * </ul>
     *
     * @return the modification stamp, or {@link #NULL_STAMP} if this resource either does
     * not exist or exists as a closed project
     */
    long getModificationStamp();

    /**
     * Returns the name of this resource.
     * The name of a resource is synonymous with the last segment
     * of its full (or project-relative) path for all resources other than the
     * workspace root. The workspace root's name is the empty string.
     * <p>
     * This is a resource handle operation; the resource need
     * not exist.
     * </p>
     * <p>
     * If this resource exists, its name can be safely assumed to be valid.
     * </p>
     *
     * @return the name of the resource
     * @see #getFullPath()
     * @see #getProjectRelativePath()
     */
    String getName();

    /**
     * Returns the path variable manager for this resource.
     *
     * @return the path variable manager
     * @see IPathVariableManager
     * @since 3.6
     */
    IPathVariableManager getPathVariableManager();

    /**
     * Returns the resource which is the parent of this resource,
     * or <code>null</code> if it has no parent (that is, this
     * resource is the workspace root).
     * <p>
     * The full path of the parent resource is the same as this
     * resource's full path with the last segment removed.
     * </p>
     * <p>
     * This is a resource handle operation; neither the resource
     * nor the resulting resource need exist.
     * </p>
     *
     * @return the parent resource of this resource,
     * or <code>null</code> if it has no parent
     */
    IContainer getParent();

    /**
     * Returns the value of the persistent property of this resource identified
     * by the given key, or <code>null</code> if this resource has no such property.
     *
     * @param key the qualified name of the property
     * @return the string value of the property,
     * or <code>null</code> if this resource has no such property
     * @exception CoreException if this method fails. Reasons include:
     * <ul>
     * <li> This resource does not exist.</li>
     * <li> This resource is not local.</li>
     * <li> This resource is a project that is not open.</li>
     * </ul>
     */
    String getPersistentProperty(QualifiedName key) throws CoreException;

    /**
     * Returns the project which contains this resource.
     * Returns itself for projects and <code>null</code>
     * for the workspace root.
     * <p>
     * A resource's project is the one named by the first segment
     * of its full path.
     * </p>
     * <p>
     * This is a resource handle operation; neither the resource
     * nor the resulting project need exist.
     * </p>
     *
     * @return the project handle
     */
    IProject getProject();

    /**
     * Returns a relative path of this resource with respect to its project.
     * Returns the empty path for projects and the workspace root.
     * <p>
     * This is a resource handle operation; the resource need not exist.
     * If this resource does exist, its path can be safely assumed to be valid.
     * </p>
     * <p>
     * A resource's project-relative path indicates the route from the project
     * to the resource. Within a project, there is exactly one such path
     * for any given resource. The returned path never has a trailing slash.
     * </p>
     * <p>
     * Project-relative paths are recommended over absolute paths, since
     * the former are not affected if the project is renamed.
     * </p>
     *
     * @return the relative path of this resource with respect to its project
     * @see #getFullPath()
     * @see #getProject()
     * @see IPath#EMPTY
     */
    IPath getProjectRelativePath();

    /**
     * Gets this resource's extended attributes from the file system,
     * or <code>null</code> if the attributes could not be obtained.
     * <p>
     * Reasons for a <code>null</code> return value include:</p>
     * <ul>
     * <li> This resource does not exist.</li>
     * <li> This resource is not local.</li>
     * <li> This resource is a project that is not open.</li>
     * </ul>
     * <p>
     * Attributes that are not supported by the underlying file system
     * will have a value of <code>false</code>.
     * </p><p>
     * Sample usage:
     * </p>
     * 
     * <pre>
     * <code>
     *  IResource resource;
     *  ...
     *  ResourceAttributes attributes = resource.getResourceAttributes();
     *  if (attributes != null) {
     *     attributes.setExecutable(true);
     *     resource.setResourceAttributes(attributes);
     *  }
     * </code>
     * </pre>
     *
     * @return the extended attributes from the file system, or
     * <code>null</code> if they could not be obtained
     * @see #setResourceAttributes(ResourceAttributes)
     * @see ResourceAttributes
     * @since 3.1
     */
    ResourceAttributes getResourceAttributes();

    /**
     * Returns the type of this resource.
     * The returned value will be one of {@link #FILE}, {@link #FOLDER}, {@link #PROJECT}, {@link #ROOT}.
     * <ul>
     * <li> All resources of type {@link #FILE} implement {@link IFile}.</li>
     * <li> All resources of type {@link #FOLDER} implement {@link IFolder}.</li>
     * <li> All resources of type {@link #PROJECT} implement {@link IProject}.</li>
     * <li> All resources of type {@link #ROOT} implement {@link IWorkspaceRoot}.</li>
     * </ul>
     * <p>
     * This is a resource handle operation; the resource need not exist in the workspace.
     * </p>
     *
     * @return the type of this resource
     * @see #FILE
     * @see #FOLDER
     * @see #PROJECT
     * @see #ROOT
     */
    int getType();

    /**
     * Returns the workspace which manages this resource.
     * <p>
     * This is a resource handle operation; the resource need not exist in the workspace.
     * </p>
     *
     * @return the workspace
     */
    IWorkspace getWorkspace();

    /**
     * Returns whether this resource is accessible. For files and folders,
     * this is equivalent to existing; for projects,
     * this is equivalent to existing and being open. The workspace root
     * is always accessible.
     *
     * @return <code>true</code> if this resource is accessible, and <code>false</code> otherwise
     * @see #exists()
     * @see IProject#isOpen()
     */
    boolean isAccessible();

    /**
     * Returns whether this resource subtree is marked as derived. Returns
     * <code>false</code> if this resource does not exist.
     *
     * <p>
     * This is a convenience method,
     * fully equivalent to <code>isDerived(IResource.NONE)</code>.
     * </p>
     *
     * @return <code>true</code> if this resource is marked as derived, and
     * <code>false</code> otherwise
     * @since 2.0
     */
    boolean isDerived();

    /**
     * Returns whether this resource subtree is marked as derived. Returns
     * <code>false</code> if this resource does not exist.
     *
     * <p>
     * The {@link #CHECK_ANCESTORS} option flag indicates whether this method
     * should consider ancestor resources in its calculation. If the
     * {@link #CHECK_ANCESTORS} flag is present, this method will return
     * <code>true</code>, if this resource, or any parent resource, is marked
     * as derived. If the {@link #CHECK_ANCESTORS} option flag is not specified,
     * this method returns false for children of derived resources.
     * </p>
     *
     * @param options bit-wise or of option flag constants (only {@link #CHECK_ANCESTORS} is applicable)
     * @return <code>true</code> if this resource subtree is derived, and <code>false</code> otherwise
     * @since 3.4
     */
    boolean isDerived(int options);

    /**
     * Returns whether this resource is hidden in the resource tree. Returns
     * <code>false</code> if this resource does not exist.
     * <p>
     * This operation is not related to the file system hidden attribute accessible using
     * {@link ResourceAttributes#isHidden()}.
     * </p>
     *
     * @return <code>true</code> if this resource is hidden, and <code>false</code> otherwise
     * @see #setHidden(boolean)
     * @since 3.4
     */
    boolean isHidden();

    /**
     * Returns whether this resource is hidden in the resource tree. Returns
     * <code>false</code> if this resource does not exist.
     * <p>
     * This operation is not related to the file system hidden attribute
     * accessible using {@link ResourceAttributes#isHidden()}.
     * </p>
     * <p>
     * The {@link #CHECK_ANCESTORS} option flag indicates whether this method
     * should consider ancestor resources in its calculation. If the
     * {@link #CHECK_ANCESTORS} flag is present, this method will return
     * <code>true</code> if this resource, or any parent resource, is a hidden
     * resource. If the {@link #CHECK_ANCESTORS} option flag is not specified,
     * this method returns false for children of hidden resources.
     * </p>
     *
     * @param options
     * bit-wise or of option flag constants (only
     * {@link #CHECK_ANCESTORS} is applicable)
     * @return <code>true</code> if this resource is hidden , and
     * <code>false</code> otherwise
     * @see #setHidden(boolean)
     * @since 3.5
     */
    boolean isHidden(int options);

    /**
     * Returns whether this resource has been linked to
     * a location other than the default location calculated by the platform.
     * <p>
     * This is a convenience method, fully equivalent to
     * <code>isLinked(IResource.NONE)</code>.
     * </p>
     *
     * @return <code>true</code> if this resource is linked, and
     * <code>false</code> otherwise
     * @since 2.1
     */
    boolean isLinked();

    /**
     * Returns whether this resource is a virtual resource. Returns <code>true</code>
     * for folders that have been marked virtual using the {@link #VIRTUAL} update
     * flag. Returns <code>false</code> in all other cases, including
     * the case where this resource does not exist. The workspace root, projects
     * and files currently cannot be made virtual.
     *
     * @return <code>true</code> if this resource is virtual, and
     * <code>false</code> otherwise
     * @since 3.6
     */
    boolean isVirtual();

    /**
     * Returns <code>true</code> if this resource has been linked to
     * a location other than the default location calculated by the platform. This
     * location can be outside the project's content area or another location
     * within the project. Returns <code>false</code> in all other cases, including
     * the case where this resource does not exist. The workspace root and
     * projects are never linked.
     * <p>
     * This method returns true for a resource that has been linked using
     * the <code>createLink</code> method.
     * </p>
     * <p>
     * The {@link #CHECK_ANCESTORS} option flag indicates whether this method
     * should consider ancestor resources in its calculation. If the
     * {@link #CHECK_ANCESTORS} flag is present, this method will return
     * <code>true</code> if this resource, or any parent resource, is a linked
     * resource. If the {@link #CHECK_ANCESTORS} option flag is not specified,
     * this method returns false for children of linked resources.
     * </p>
     *
     * @param options bit-wise or of option flag constants
     * (only {@link #CHECK_ANCESTORS} is applicable)
     * @return <code>true</code> if this resource is linked, and
     * <code>false</code> otherwise
     * @since 3.2
     */
    boolean isLinked(int options);

    /**
     * Returns whether this resource and its members (to the
     * specified depth) are expected to have their contents (and properties)
     * available locally. Returns <code>false</code> in all other cases,
     * including the case where this resource does not exist. The workspace
     * root and projects are always local.
     * <p>
     * When a resource is not local, its content and properties are
     * unavailable for both reading and writing.
     * </p>
     *
     * @param depth valid values are {@link #DEPTH_ZERO}, {@link #DEPTH_ONE}, or {@link #DEPTH_INFINITE}
     * @return <code>true</code> if this resource is local, and <code>false</code> otherwise
     *
     * @see #setLocal(boolean, int, IProgressMonitor)
     * @deprecated This API is no longer in use. Note that this API is unrelated
     * to whether the resource is in the local file system versus some other file system.
     */
    @Deprecated
    boolean isLocal(int depth);

    /**
     * Returns whether this resource is a phantom resource.
     * <p>
     * The workspace uses phantom resources to remember outgoing deletions and
     * incoming additions relative to an external synchronization partner. Phantoms
     * appear and disappear automatically as a byproduct of synchronization.
     * Since the workspace root cannot be synchronized in this way, it is never a phantom.
     * Projects are also never phantoms.
     * </p>
     * <p>
     * The key point is that phantom resources do not exist (in the technical
     * sense of <code>exists</code>, which returns <code>false</code>
     * for phantoms) are therefore invisible except through a handful of
     * phantom-enabled API methods (notably <code>IContainer.members(boolean)</code>).
     * </p>
     *
     * @return <code>true</code> if this resource is a phantom resource, and
     * <code>false</code> otherwise
     */
    boolean isPhantom();

    /**
     * Sets whether this resource and its members are hidden in the resource tree.
     * <p>
     * Hidden resources are invisible to most clients. Newly-created resources
     * are not hidden resources by default.
     * </p>
     * <p>
     * The workspace root is never considered hidden resource;
     * attempts to mark it as hidden are ignored.
     * </p>
     * <p>
     * This operation does <b>not</b> result in a resource change event, and does not
     * trigger autobuilds.
     * </p>
     * <p>
     * This operation is not related to {@link ResourceAttributes#setHidden(boolean)}.
     * Whether a resource is hidden in the resource tree is unrelated to whether the
     * underlying file is hidden in the file system.
     * </p>
     *
     * @param isHidden <code>true</code> if this resource is to be marked
     * as hidden, and <code>false</code> otherwise
     * @exception CoreException if this method fails. Reasons include:
     * <ul>
     * <li> This resource does not exist.</li>
     * <li> Resource changes are disallowed during certain types of resource change
     * event notification. See <code>IResourceChangeEvent</code> for more details.</li>
     * </ul>
     * @see #isHidden()
     * @since 3.4
     */
    void setHidden(boolean isHidden) throws CoreException;

    /**
     * Sets the local time stamp on disk for this resource. The time must be represented
     * as the number of milliseconds since the epoch (00:00:00 GMT, January 1, 1970).
     * Returns the actual time stamp that was recorded.
     * Due to varying file system timing granularities, the provided value may be rounded
     * or otherwise truncated, so the actual recorded time stamp that is returned may
     * not be the same as the supplied value.
     *
     * @param value a time stamp in milliseconds.
     * @return a local file system time stamp.
     * @exception CoreException if this method fails. Reasons include:
     * <ul>
     * <li> This resource does not exist.</li>
     * <li> This resource is not local.</li>
     * <li> This resource is not accessible.</li>
     * <li> Resource changes are disallowed during certain types of resource change
     * event notification. See <code>IResourceChangeEvent</code> for more details.</li>
     * </ul>
     * @since 3.0
     */
    long setLocalTimeStamp(long value) throws CoreException;

    /**
     * Sets this resource with the given extended attributes. This sets the
     * attributes in the file system. Only attributes that are supported by
     * the underlying file system will be set.
     * <p>
     * Sample usage: <br>
     * <br>
     * <code>
     * IResource resource; <br>
     * ... <br>
     * if (attributes != null) {
     * attributes.setExecutable(true); <br>
     * resource.setResourceAttributes(attributes); <br>
     * }
     * </code>
     * </p>
     * <p>
     * Note that a resource cannot be converted into a symbolic link by
     * setting resource attributes with {@link ResourceAttributes#isSymbolicLink()}
     * set to true.
     * </p>
     *
     * @param attributes the attributes to set
     * @exception CoreException if this method fails. Reasons include:
     * <ul>
     * <li> This resource does not exist.</li>
     * <li> This resource is not local.</li>
     * <li> This resource is a project that is not open.</li>
     * </ul>
     * @see #getResourceAttributes()
     * @since 3.1
     */
    void setResourceAttributes(ResourceAttributes attributes) throws CoreException;

}
