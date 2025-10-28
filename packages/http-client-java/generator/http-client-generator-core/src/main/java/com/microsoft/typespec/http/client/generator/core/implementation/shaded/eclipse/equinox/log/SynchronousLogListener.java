/*******************************************************************************
 * Copyright (c) 2008, 2011 IBM Corporation and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.equinox.log;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.log.LogListener;

/**
 * Marker interface to denotes a log listener that should be called on the logging thread
 * 
 * @see LogListener
 * @since 3.7
 */
public interface SynchronousLogListener extends LogListener {
    //
}
