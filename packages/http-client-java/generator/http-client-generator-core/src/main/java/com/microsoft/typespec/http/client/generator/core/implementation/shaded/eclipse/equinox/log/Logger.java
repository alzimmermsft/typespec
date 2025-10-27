/*******************************************************************************
 * Copyright (c) 2006, 2017 IBM Corporation and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.equinox.log;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.ServiceReference;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.log.LogService;

/**
  * Provides named logger support for the OSGi LogService. Otherwise very similar to the <code>LogService</code>.
  * @ThreadSafe
  * @see LogService
 * @since 3.7
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface Logger extends com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.log.Logger {
	/**
	 * @see LogService#log(int, String)
	 */
    void log(int level, String message);

	/**
	 * @see LogService#log(int, String, Throwable)
	 */
    void log(int level, String message, Throwable exception);

	/**
	 * @see LogService#log(ServiceReference, int, String)
	 */
    void log(ServiceReference<?> sr, int level, String message);

	/**
	 * @see LogService#log(ServiceReference, int, String, Throwable)
	 */
    void log(ServiceReference<?> sr, int level, String message, Throwable exception);

	/**
	 * Extends the <code>LogService</code>
	 * Logs a message with a context object
	 *
	 * @param context The context object this message is associated with.
	 * @param level The log level or severity of the message.
	 * @param message A human readable string to associate with log entry.
	 * @see LogService#log(int, String)
	 */
    void log(Object context, int level, String message);

	/**
	 * Logs a message with an exception associated and a
	 * context object.
	 *
	 *
	 * @param context The context object this message is associated with.
	 * @param level The log level or severity of the message.
	 * @param message A human readable string to associate with log entry.
	 * @param exception The exception associated with this entry
	 * @see LogService#log(int, String, Throwable)
	 */
    void log(Object context, int level, String message, Throwable exception);

	/**
	 * Pre-checks if there are LogListeners who are listening for a matching log entry from this <code>Logger</code>.
	 *
	 * @param level The log level or severity of the message.
	 * @return <code>boolean</code> True if there a LogListener listening that can handle a log entry for this log level; false otherwise.
	 * @see ExtendedLogReaderService#addLogListener(com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.log.LogListener, LogFilter)
	 * @see LogFilter
	 */
    boolean isLoggable(int level);

	/**
	 * Returns the name associated with this <code>Logger</code>
	 * object.
	 *
	 * @return <code>String</code> containing the name associated with this
	 *         <code>Logger</code> object;<code>null</code> if no name is
	 *         associated.
	 */
	@Override
    String getName();
}
