/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources.mapping;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IContainer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.PlatformObject;

/**
 * A special model object used to represent shallow folders
 */
public class ShallowContainer extends PlatformObject {

	private final IContainer container;

	public ShallowContainer(IContainer container) {
		this.container = container;
	}

	public IContainer getResource() {
		return container;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof ShallowContainer other) {
			return other.getResource().equals(getResource());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getResource().hashCode();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == IResource.class || adapter == IContainer.class) {
			return (T) container;
		}
		return super.getAdapter(adapter);
	}

}
