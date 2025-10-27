/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.runtime;

public interface IRuntimeConstants {

	/**
	 * The unique identifier constant (value
	 * "<code>com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime</code>") of the Core Runtime (pseudo-)
	 * plug-in.
	 */
    String PI_RUNTIME = "com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime"; //$NON-NLS-1$

	/**
	 * Name of this bundle.
	 */
    String PI_COMMON = "com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.equinox.common"; //$NON-NLS-1$

	/**
	 * Status code constant (value 2) indicating an error occurred while running a
	 * plug-in.
	 */
    int PLUGIN_ERROR = 2;

	/**
	 * Status code constant (value 5) indicating the platform could not write some
	 * of its metadata.
	 */
    int FAILED_WRITE_METADATA = 5;

}
