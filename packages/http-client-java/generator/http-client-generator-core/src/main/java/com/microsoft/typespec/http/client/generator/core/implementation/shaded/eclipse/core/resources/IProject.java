/*******************************************************************************
 *  Copyright (c) 2000, 2014 IBM Corporation and others.
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
 * Francis Lynch (Wind River) - [301563] Save and load tree snapshots
 * Broadcom Corporation - build configurations and references
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.*;
import java.net.URI;

/**
 * A project is a type of resource which groups resources
 * into buildable, reusable units.
 * <p>
 * Features of projects include:
 * </p>
 * <ul>
 * <li>A project collects together a set of files and folders.</li>
 * <li>A project's location controls where the project's resources are
 * stored in the local file system.</li>
 * <li>A project's build spec controls how building is done on the project.</li>
 * <li>A project can carry session and persistent properties.</li>
 * <li>A project can be open or closed; a closed project is
 * passive and has a minimal in-memory footprint.</li>
 * <li>A project can have one or more project build configurations.</li>
 * <li>A project can carry references to other project build configurations.</li>
 * <li>A project can have one or more project natures.</li>
 * </ul>
 * <p>
 * Projects implement the <code>IAdaptable</code> interface;
 * extensions are managed by the platform's adapter manager.
 * </p>
 *
 * @see Platform#getAdapterManager()
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IProject extends IContainer, IAdaptable {
    /**
     * Option constant (value 1) indicating that a snapshot to be
     * loaded or saved contains a resource tree (refresh information).
     * 
     * @see #loadSnapshot(int, URI, IProgressMonitor)
     * @see #saveSnapshot(int, URI, IProgressMonitor)
     * @since 3.6
     */
    int SNAPSHOT_TREE = 1;

    /**
     * Creates a new project resource in the workspace using the given project
     * description. Upon successful completion, the project will exist but be closed.
     * <p>
     * Newly created projects have no session or persistent properties.
     * </p>
     * <p>
     * If the project content area given in the project description does not
     * contain a project description file, a project description file is written
     * in the project content area with the natures, build spec, comment, and
     * referenced projects as specified in the given project description.
     * If there is an existing project description file, it is not overwritten.
     * In either case, this method does <b>not</b> cause natures to be configured.
     * </p>
     * <p>
     * This method changes resources; these changes will be reported
     * in a subsequent resource change event, including an indication
     * that the project has been added to the workspace.
     * </p>
     * <p>
     * This method is long-running; progress and cancellation are provided
     * by the given progress monitor.
     * </p>
     * <p>
     * The {@link IResource#HIDDEN} update flag indicates that this resource
     * should immediately be set as a hidden resource. Specifying this flag
     * is equivalent to atomically calling {@link IResource#setHidden(boolean)}
     * with a value of <code>true</code> immediately after creating the resource.
     * </p>
     * <p>
     * Update flags other than those listed above are ignored.
     * </p>
     *
     * @param description the project description
     * @param monitor a progress monitor, or <code>null</code> if progress
     * reporting is not desired
     * @exception CoreException if this method fails. Reasons include:
     * <ul>
     * <li> This project already exists in the workspace.</li>
     * <li> The name of this resource is not valid (according to
     * <code>IWorkspace.validateName</code>).</li>
     * <li> The project location is not valid (according to
     * <code>IWorkspace.validateProjectLocation</code>).</li>
     * <li> The project description file could not be created in the project
     * content area.</li>
     * <li> Resource changes are disallowed during certain types of resource change
     * event notification. See <code>IResourceChangeEvent</code> for more details.</li>
     * </ul>
     * @exception OperationCanceledException if the operation is canceled.
     * Cancelation can occur even if no progress monitor is provided.
     *
     * @since 3.4
     */
    void create(IProjectDescription description, int updateFlags, IProgressMonitor monitor) throws CoreException;

    /**
     * Deletes this project from the workspace.
     * No action is taken if this project does not exist.
     * <p>
     * This is a convenience method, fully equivalent to:
     * </p>
     * 
     * <pre>
     * delete((deleteContent ? IResource.ALWAYS_DELETE_PROJECT_CONTENT : IResource.NEVER_DELETE_PROJECT_CONTENT)
     *     | (force ? FORCE : IResource.NONE), monitor);
     * </pre>
     * 
     * <p>
     * This method is long-running; progress and cancellation are provided
     * by the given progress monitor.
     * </p>
     *
     * @param deleteContent a flag controlling how whether content is
     * aggressively deleted
     * @param force a flag controlling whether resources that are not
     * in sync with the local file system will be tolerated
     * @param monitor a progress monitor, or <code>null</code> if progress
     * reporting is not desired
     * @exception CoreException if this method fails. Reasons include:
     * <ul>
     * <li> This project could not be deleted.</li>
     * <li> This project's contents could not be deleted.</li>
     * <li> Resource changes are disallowed during certain types of resource change
     * event notification. See <code>IResourceChangeEvent</code> for more details.</li>
     * </ul>
     * @exception OperationCanceledException if the operation is canceled.
     * Cancelation can occur even if no progress monitor is provided.
     */
    void delete(boolean deleteContent, boolean force, IProgressMonitor monitor) throws CoreException;

    /**
     * Returns the active build configuration for the project.
     * <p>
     * If at any point the active configuration is removed from the project, for example
     * when updating the list of build configurations, the active build configuration will be set to
     * the first build configuration specified by {@link IProjectDescription#setBuildConfigs(String[])}.
     * <p>
     * If all of the build configurations are removed, the active build configuration will be set to the
     * default configuration.
     * </p>
     * 
     * @return the active build configuration
     * @exception CoreException if this method fails. Reasons include:
     * <ul>
     * <li> This project does not exist.</li>
     * <li> This project is not open.</li>
     * </ul>
     * @since 3.7
     */
    IBuildConfiguration getActiveBuildConfig() throws CoreException;

    /**
     * Returns the project {@link IBuildConfiguration} with the given name for this project.
     * 
     * @param configName the name of the configuration to get
     * @return a project configuration
     * @exception CoreException if this method fails. Reasons include:
     * <ul>
     * <li> This project does not exist.</li>
     * <li> This project is not open.</li>
     * <li> The configuration does not exist in this project.</li>
     * </ul>
     * @see #getBuildConfigs()
     * @since 3.7
     */
    IBuildConfiguration getBuildConfig(String configName) throws CoreException;

    /**
     * Returns the build configurations for this project. A project always has at
     * least one build configuration, so this will never return an empty list or null.
     * The result will not contain duplicates.
     * 
     * @return a list of project build configurations
     * @exception CoreException if this method fails. Reasons include:
     * <ul>
     * <li> This project does not exist.</li>
     * <li> This project is not open.</li>
     * </ul>
     * @since 3.7
     */
    IBuildConfiguration[] getBuildConfigs() throws CoreException;

    /**
     * Returns the description for this project.
     * The returned value is a copy and cannot be used to modify
     * this project. The returned value is suitable for use in creating,
     * copying and moving other projects.
     *
     * @return the description for this project
     * @exception CoreException if this method fails. Reasons include:
     * <ul>
     * <li> This project does not exist.</li>
     * <li> This project is not open.</li>
     * </ul>
     */
    IProjectDescription getDescription() throws CoreException;

    /**
     * Returns a handle to the file with the given name in this project.
     * <p>
     * This is a resource handle operation; neither the resource nor
     * the result need exist in the workspace.
     * The validation check on the resource name/path is not done
     * when the resource handle is constructed; rather, it is done
     * automatically as the resource is created.
     * </p>
     *
     * @param name the string name of the member file
     * @return the (handle of the) member file
     * @see #getFolder(String)
     */
    IFile getFile(String name);

    /**
     * Returns a handle to the folder with the given name in this project.
     * <p>
     * This is a resource handle operation; neither the container
     * nor the result need exist in the workspace.
     * The validation check on the resource name/path is not done
     * when the resource handle is constructed; rather, it is done
     * automatically as the resource is created.
     * </p>
     *
     * @param name the string name of the member folder
     * @return the (handle of the) member folder
     * @see #getFile(String)
     */
    IFolder getFolder(String name);

    /**
     * Returns the specified project nature for this project or <code>null</code> if
     * the project nature has not been added to this project.
     * Clients may downcast to a more concrete type for more nature-specific methods.
     * The documentation for a project nature specifies any such additional protocol.
     * <p>
     * This may cause the plug-in that provides the given nature to be activated.
     * </p>
     *
     * @param natureId the fully qualified nature extension identifier, formed
     * by combining the nature extension id with the id of the declaring plug-in.
     * (e.g. <code>"com.example.acmeplugin.coolnature"</code>)
     * @return the project nature object
     * @exception CoreException if this method fails. Reasons include:
     * <ul>
     * <li> This project does not exist.</li>
     * <li> This project is not open.</li>
     * <li> The project nature extension could not be found.</li>
     * </ul>
     */
    IProjectNature getNature(String natureId) throws CoreException;

    /**
     * Returns the location in the local file system of the project-specific
     * working data area for use by the bundle/plug-in with the given identifier,
     * or <code>null</code> if the project does not exist.
     * <p>
     * The content, structure, and management of this area is
     * the responsibility of the bundle/plug-in. This area is deleted when the
     * project is deleted.
     * </p><p>
     * This project needs to exist but does not need to be open.
     * </p>
     * 
     * @param id the bundle or plug-in's identifier
     * @return a local file system path
     * @since 3.0
     */
    IPath getWorkingLocation(String id);

    /**
     * Clears the cache of dynamic project references for this project. Invoking this
     * method will cause the dynamic project references to be recomputed the next time
     * they are accessed (for example, in a call to {@link #getReferencedProjects()}.
     * It is not necessary to hold the workspace lock when invoking this method. Plugins
     * that provide an {@link IDynamicReferenceProvider} should invoke this method to
     * inform the rest of the application when one or more dynamic project references
     * may have changed. This will also clear any other cached data that is derived from
     * the dynamic references.
     *
     * @since 3.12
     */
    void clearCachedDynamicReferences();

    /**
     * Returns the list of all open projects which reference
     * this project. This project may or may not exist. Returns
     * an empty array if there are no referencing projects.
     *
     * @return a list of open projects referencing this project
     */
    IProject[] getReferencingProjects();

    /**
     * Returns whether the project nature specified by the given
     * nature extension id has been added to this project.
     *
     * @param natureId the nature extension identifier
     * @return <code>true</code> if the project has the given nature
     * @exception CoreException if this method fails. Reasons include:
     * <ul>
     * <li> This project does not exist.</li>
     * <li> This project is not open.</li>
     * </ul>
     */
    boolean hasNature(String natureId) throws CoreException;

    /**
     * Returns whether this project is open.
     * <p>
     * A project must be opened before it can be manipulated.
     * A closed project is passive and has a minimal memory
     * footprint; a closed project has no members.
     * </p>
     *
     * @return <code>true</code> if this project is open, <code>false</code> if
     * this project is closed or does not exist
     * @see #open(IProgressMonitor)
     * @see #close(IProgressMonitor)
     */
    boolean isOpen();

    /**
     * Loads a snapshot of project meta-data from the given location URI.
     * Must be called after the project has been created, but before it is
     * opened. The options constant controls what kind of snapshot information
     * to load. Valid option values include:<ul>
     * <li>{@link IProject#SNAPSHOT_TREE} - load resource tree (refresh info)
     * </ul>
     *
     * @param options kind of snapshot information to load
     * @param snapshotLocation URI to load from
     * @param monitor a progress monitor, or <code>null</code> if progress
     * reporting is not desired
     * @exception CoreException if this method fails. Reasons include:
     * <ul>
     * <li> The snapshot was not found at the specified URI.</li>
     * </ul>
     * @exception OperationCanceledException if the operation is canceled.
     * @see #saveSnapshot(int, URI, IProgressMonitor)
     * @since 3.6
     */
    void loadSnapshot(int options, URI snapshotLocation, IProgressMonitor monitor) throws CoreException;

    /**
     * Opens this project. No action is taken if the project is already open.
     * <p>
     * Opening a project constructs an in-memory representation
     * of its resources from information stored on disk.
     * </p>
     * <p>
     * When a project is opened for the first time, initial information about the
     * project's existing resources can be obtained in the following ways:
     * </p>
     * <ul>
     * <li>If a {@link #loadSnapshot(int, URI, IProgressMonitor)} call has been made
     * before the open, resources are restored from that file (a file written by
     * {@link #saveSnapshot(int, URI, IProgressMonitor)}). When the snapshot includes
     * resource tree information and can be loaded without error, no refresh is initiated,
     * so the project's resource tree will match what the snapshot provides.</li>
     * <li>Otherwise, when the {@link IResource#BACKGROUND_REFRESH} flag is specified,
     * resources on disk will be added to the project in the background after
     * this method returns. Child resources of the project may not be available
     * until this background refresh completes.</li>
     * <li>Otherwise, resource information is obtained with a refresh operation in the
     * foreground, before this method returns.</li>
     * </ul>
     * <p>
     * This method changes resources; these changes will be reported
     * in a subsequent resource change event that includes
     * an indication that the project has been opened and its resources
     * have been added to the tree. If the <code>BACKGROUND_REFRESH</code>
     * update flag is specified, multiple resource change events may occur as
     * resources on disk are discovered and added to the tree.
     * </p>
     * <p>
     * This method is long-running; progress and cancellation are provided
     * by the given progress monitor.
     * </p>
     *
     * @param updateFlags if {@link IResource#BACKGROUND_REFRESH} is passed,
     * and the project is not new in the workspace (i.e. is not being opened for the first time)
     * a background refresh is scheduled with the workspace refresh manager.
     * See description above for cases in which the project is new in the workspace.
     * @param monitor a progress monitor, or <code>null</code> if progress
     * reporting is not desired
     * @exception CoreException if this method fails. Reasons include:
     * <ul>
     * <li> Resource changes are disallowed during certain types of resource change
     * event notification. See <code>IResourceChangeEvent</code> for more details.</li>
     * </ul>
     * @exception OperationCanceledException if the operation is canceled.
     * Cancelation can occur even if no progress monitor is provided.
     * @see #close(IProgressMonitor)
     * @see IResource#BACKGROUND_REFRESH
     * @see IResourceRuleFactory#modifyRule(IResource)
     * @since 3.1
     */
    void open(int updateFlags, IProgressMonitor monitor) throws CoreException;

    /**
     * Opens this project. No action is taken if the project is already open.
     * <p>
     * This is a convenience method, fully equivalent to
     * <code>open(IResource.NONE, monitor)</code>.
     * </p>
     * <p>
     * This method changes resources; these changes will be reported
     * in a subsequent resource change event that includes
     * an indication that the project has been opened and its resources
     * have been added to the tree.
     * </p>
     * <p>
     * This method is long-running; progress and cancellation are provided
     * by the given progress monitor.
     * </p>
     *
     * @param monitor a progress monitor, or <code>null</code> if progress
     * reporting is not desired
     * @exception CoreException if this method fails. Reasons include:
     * <ul>
     * <li> Resource changes are disallowed during certain types of resource change
     * event notification. See <code>IResourceChangeEvent</code> for more details.</li>
     * </ul>
     * @exception OperationCanceledException if the operation is canceled.
     * Cancelation can occur even if no progress monitor is provided.
     * @see #close(IProgressMonitor)
     * @see IResourceRuleFactory#modifyRule(IResource)
     */
    void open(IProgressMonitor monitor) throws CoreException;

    /**
     * Returns line separator appropriate for new files in the given project.
     * <p>
     * This method uses the following algorithm to determine the line separator to
     * be returned:
     * </p>
     * <ol>
     * <li>the line separator defined in project preferences, or</li>
     * <li>the line separator defined in workspace preferences, or</li>
     * <li>the line separator defined in default preferences, or</li>
     * <li>Operating system default line separator</li>
     * </ol>
     *
     * @return line separator for the current file
     * @exception CoreException if this method fails.
     * @since 3.18
     */
    String getDefaultLineSeparator() throws CoreException;
}
