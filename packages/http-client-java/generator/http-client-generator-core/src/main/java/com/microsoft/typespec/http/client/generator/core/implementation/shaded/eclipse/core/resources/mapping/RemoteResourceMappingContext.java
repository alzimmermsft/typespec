/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
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
 ******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.mapping;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.*;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;

/**
 * A remote mapping context provides a model element with a view of the remote
 * state of local resources as they relate to a repository operation that is in
 * progress. A repository provider can pass an instance of this interface to a
 * model element when obtaining a set of traversals for a model element. This
 * allows the model element to query the remote state of a resource in order to
 * determine if there are resources that exist remotely but do not exist locally
 * that should be included in the traversal.
 * <p>
 * This class may be subclassed by clients.
 * </p>
 *
 * @see ResourceMapping
 * @see ResourceMappingContext
 * @since 3.2
 */
public abstract class RemoteResourceMappingContext extends ResourceMappingContext {

    /**
	 * Refresh flag constant (bit mask value 0) indicating that no additional
	 * refresh behavior is required.
	 */
	public static final int NONE = 0;

    /**
	 * Return the list of projects that apply to this context.
	 * In other words, the context is only capable of querying the
	 * remote state for projects that are contained in the
	 * returned list.
	 * @return the list of projects that apply to this context
	 */
	public abstract IProject[] getProjects();

    /**
	 * Refresh the known remote state for any resources covered by the given
	 * traversals. Clients who require the latest remote state should invoke
	 * this method before invoking any others of the class. Mappings can use
	 * this method as a hint to the context provider of which resources will be
	 * required for the mapping to generate the proper set of traversals.
	 * <p>
	 * Note that this is really only a hint to the context provider. It is up to
	 * implementors to decide, based on the provided traversals, how to
	 * efficiently perform the refresh. In the ideal case, calls to
	 * {@link #hasRemoteChange(IResource, IProgressMonitor)} and
	 * {@link #fetchMembers} would not need to contact the server after a call to a
	 * refresh with appropriate traversals.  Also, ideally, if
	 * {@link #FILE_CONTENTS_REQUIRED} is on of the flags, then the contents
	 * for these files will be cached as efficiently as possible so that calls to
	 * {@link #fetchRemoteContents} will also not need to contact the server. This
	 * may not be possible for all context providers, so clients cannot assume that
	 * the above mentioned methods will not be long running. It is still advisable
	 * for clients to call {@link #refresh} with as much details as possible since, in
	 * the case where a provider is optimized, performance will be much better.
	 * </p>
	 *
	 * @param traversals the resource traversals that indicate which resources
	 * are to be refreshed
	 * @param flags additional refresh behavior. For instance, if
	 * {@link #FILE_CONTENTS_REQUIRED} is one of the flags, this indicates
	 * that the client will be accessing the contents of the files covered by
	 * the traversals. {@link #NONE} should be used when no additional
	 * behavior is required
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 * reporting is not desired
	 * @exception CoreException if the refresh fails. Reasons include:
	 * <ul>
	 * <li>The server could not be contacted for some reason. </li>
	 * </ul>
	 */
	public abstract void refresh(ResourceTraversal[] traversals, int flags, IProgressMonitor monitor) throws CoreException;
}
