/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.expressions;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IAdaptable;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IAdapterManager;

/**
 * Objects that are adaptable to <code>ICountable</code> can be used
 * as the default variable in a count expression.
 *
 * @see IAdaptable
 * @see IAdapterManager
 *
 * @since 3.3
 */
public interface ICountable {

	/**
	 * Returns the number of elements.
	 *
	 * @return the number of elements
	 */
	int count();
}