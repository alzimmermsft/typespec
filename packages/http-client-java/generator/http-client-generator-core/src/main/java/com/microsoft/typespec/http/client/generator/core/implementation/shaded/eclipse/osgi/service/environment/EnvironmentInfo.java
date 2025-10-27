/*******************************************************************************
 * Copyright (c) 2003, 2008 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.service.environment;

/**
 * A Framework service which gives access to the command line used to start
 * this running framework as well as information about the environment
 * such as the current operating system, machine architecture, locale and
 * windowing system.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @since 3.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
// TODO  Need to add a method that gets the real application args.
//We might also want to explain how the command line is organized: fwk / non fwk. Non-fwk is rt args and app args.
public interface EnvironmentInfo {

	/**
	 * Returns all command line arguments specified when the running framework was started.
	 *
	 * @return the array of command line arguments.
	 */
    String[] getCommandLineArgs();

    /**
	 * Returns the arguments not consumed by the framework implementation itself.  Which
	 * arguments are consumed is implementation specific.
	 *
	 * @return the array of command line arguments not consumed by the framework.
	 */
    String[] getNonFrameworkArgs();

	/**
	 * Returns the string name of the current system architecture.
	 * The value is a user-defined string if the architecture is
	 * specified on the command line, otherwise it is the value
	 * returned by <code>java.lang.System.getProperty("os.arch")</code>.
	 *
	 * @return the string name of the current system architecture
	 */
    String getOSArch();

	/**
	 * Returns the string name of the current locale for use in finding files
	 * whose path starts with <code>$nl$</code>.
	 *
	 * @return the string name of the current locale
	 */
    String getNL();

	/**
	 * Returns the string name of the current operating system for use in finding
	 * files whose path starts with <code>$os$</code>.  Return {@link Constants#OS_UNKNOWN}
	 * if the operating system cannot be determined.
	 * <p>
	 * The value may indicate one of the operating systems known to the platform
	 * (as specified in <code>com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Platform#knownOSValues</code>)
	 * or a user-defined string if the operating system name is specified on the command line.
	 * </p>
	 *
	 * @return the string name of the current operating system
	 */
    String getOS();

	/**
	 * Returns the string name of the current window system for use in finding files
	 * whose path starts with <code>$ws$</code>.  Return <code>null</code>
	 * if the window system cannot be determined.
	 *
	 * @return the string name of the current window system or <code>null</code>
	 */
    String getWS();

    /**
	 * Returns the value for the specified property.  Environment Properties are
	 * backed by the Java system properties.  When the option
	 * <code>osgi.framework.useSystemProperties</code> is used then
	 * the environment properties are specific for each instance of the framework.
	 * <p>
	 * This method should be used instead of the <code>System.getProperty(String)</code>
	 * method to avoid the global nature of system properties.
	 * </p>
	 * @param key the property key
	 * @return the value of the property, or null
	 * @since 3.4
	 */
    String getProperty(String key);

	/**
	 * Sets the value for the specified property.  Environment Properties are
	 * backed by the Java system properties.  When the option
	 * <code>osgi.framework.useSystemProperties</code> is used then
	 * the environment properties are specific for each instance of the framework.
	 * <p>
	 * This method should be used instead of the <code>System.setProperty(String, String)</code>
	 * method to avoid the global nature of system properties.
	 * </p>
	 * @param key the property key
	 * @param value the value of the property
	 * @return the old value of the property, or null
	 * @since 3.4
	 */
    String setProperty(String key, String value);
}
