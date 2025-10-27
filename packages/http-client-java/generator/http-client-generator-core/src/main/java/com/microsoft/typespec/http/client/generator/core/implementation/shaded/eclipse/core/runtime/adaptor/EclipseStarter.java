/*******************************************************************************
 * Copyright (c) 2003, 2016 IBM Corporation and others.
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
 *     Alex Blewitt (bug 172969)
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.adaptor;

/**
 * Special startup class for the Eclipse Platform. This class cannot be
 * instantiated; all functionality is provided by static methods.
 * <p>
 * The Eclipse Platform makes heavy use of Java class loaders for loading
 * plug-ins. Even the Eclipse Runtime itself and the OSGi framework need to be
 * loaded by special class loaders. The upshot is that a client program (such as
 * a Java main program, a servlet) cannot reference any part of Eclipse
 * directly. Instead, a client must use this loader class to start the platform,
 * invoking functionality defined in plug-ins, and shutting down the platform
 * when done.
 * </p>
 * <p>
 * Note that the fields on this class are not API.
 * </p>
 * 
 * @since 3.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class EclipseStarter {
    public static final String PROP_LOGFILE = "osgi.logfile"; //$NON-NLS-1$

    public static final String PROP_EXITCODE = "eclipse.exitcode"; //$NON-NLS-1$
	public static final String PROP_EXITDATA = "eclipse.exitdata"; //$NON-NLS-1$
	public static final String PROP_CONSOLE_LOG = "eclipse.consoleLog"; //$NON-NLS-1$
}
