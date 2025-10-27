/*******************************************************************************
 *  Copyright (c) 2000, 2009 IBM Corporation and others.
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
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;

/**
 * A lightweight interface for requesting information about a resource.
 * All of the "get" methods on a resource proxy have trivial performance cost.
 * Requesting the full path or the actual resource handle will cause extra objects
 * to be created and will thus have greater cost.
 * <p>
 * When a resource proxy is used within an {@link IResourceProxyVisitor},
 * it is a transient object that is only valid for the duration of a single visit method.
 * A proxy should not be referenced once the single resource visit is complete.
 * The equals and hashCode methods should not be relied on.
 * </p>
 * <p>
 * A proxy can also be created using {@link IResource#createProxy()}. In
 * this case the proxy is valid indefinitely, but will not remain in sync with
 * the state of the corresponding resource.
 * </p>
 *
 * @see IResourceProxyVisitor
 * @since 2.1
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IResourceProxy {
	/**
	 * Returns the modification stamp of the resource being visited.
	 *
	 * @return the modification stamp, or <code>NULL_STAMP</code> if the
	 * resource either does not exist or exists as a closed project
	 * @see IResource#getModificationStamp()
	 */
	long getModificationStamp();

	/**
	 * Returns whether the resource being visited is accessible.
	 *
	 * @return <code>true</code> if the resource is accessible, and
	 * <code>false</code> otherwise
	 * @see IResource#isAccessible()
	 */
	boolean isAccessible();

	/**
	 * Returns whether the resource being visited is derived.
	 *
	 * @return <code>true</code> if the resource is marked as derived, and
	 * <code>false</code> otherwise
	 * @see IResource#isDerived()
	 */
	boolean isDerived();

	/**
	 * Returns whether the resource being visited is a linked resource.
	 *
	 * @return <code>true</code> if the resource is linked, and
	 * <code>false</code> otherwise
	 * @see IResource#isLinked()
	 */
	boolean isLinked();

	/**
	 * Returns whether the resource being visited is a phantom resource.
	 *
	 * @return <code>true</code> if the resource is a phantom resource, and
	 * <code>false</code> otherwise
	 * @see IResource#isPhantom()
	 */
	boolean isPhantom();

	/**
	 * Returns whether the resource being visited is a hidden resource.
	 *
	 * @return <code>true</code> if the resource is a hidden resource, and
	 * <code>false</code> otherwise
	 * @see IResource#isHidden()
	 *
	 * @since 3.4
	 */
	boolean isHidden();

    /**
	 * Returns the simple name of the resource being visited.
	 *
	 * @return the name of the resource
	 * @see IResource#getName()
	 */
	String getName();

    /**
	 * Returns the type of the resource being visited.
	 *
	 * @return the resource type
	 * @see IResource#getType()
	 */
	int getType();

	/**
	 * Returns the full workspace path of the resource being visited.
	 * <p>
	 * Note that this is not a &quot;free&quot; proxy operation.  This method
	 * will generally cause a path object to be created.  For an optimal
	 * visitor, only call this method when absolutely necessary.  Note that the
	 * simple resource name can be obtained from the proxy with no cost.
	 * </p>
	 * @return the full path of the resource
	 * @see IResource#getFullPath()
	 */
	IPath requestFullPath();

	/**
	 * Returns the handle of the resource being visited.
	 * <p>
	 * Note that this is not a &quot;free&quot; proxy operation.  This method will
	 * generally cause both a path object and a resource object to be created.
	 * For an optimal visitor, only call this method when absolutely necessary.
	 * Note that the simple resource name can be obtained from the proxy with no
	 * cost, and the full path of the resource can be obtained through the proxy
	 * with smaller cost.
	 * </p>
	 * @return the resource handle
	 */
	IResource requestResource();
}
