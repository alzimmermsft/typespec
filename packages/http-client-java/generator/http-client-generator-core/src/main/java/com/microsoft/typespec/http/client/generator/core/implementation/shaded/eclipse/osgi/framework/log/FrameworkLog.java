/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.framework.log;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.FrameworkEvent;
import java.io.*;

/**
 * The FramworkLog interface. A FrameworkLog implementation is provided by the
 * FrameworkAdaptor and used by the Framework to log any error messages and
 * FrameworkEvents of type ERROR. The FrameworkLog may persist the log messages
 * to the filesystem or allow other ways of accessing the log information.
 * 
 * @since 3.1
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface FrameworkLog {

    /**
     * Logs the information from a FrameworkEvent to the FrameworkLog.
     * 
     * @param frameworkEvent The FrameworkEvent to log.
     */
    void log(FrameworkEvent frameworkEvent);

    /**
     * Logs the FrameworkLogEntry to the FrameworkLog
     * 
     * @param logEntry The entry to log.
     */
    void log(FrameworkLogEntry logEntry);

    /**
     * Sets the current File used to log messages to a FileWriter
     * using the specified File. If append is set to true then the
     * content of the current Writer will be appended to the
     * new File if possible.
     * 
     * @param newFile The File to create a new FileWriter which will be
     * used for logging messages.
     * @param append Indicates whether the content of the current Writer
     * used for logging messages should be appended to the end of the new
     * File.
     * @throws IOException if any problem occurs while constructing a
     * FileWriter from the newFile. If this exception is thrown the
     * FrameworkLog will not be affected and will continue to use the
     * current Writer to log messages.
     */
    void setFile(File newFile, boolean append) throws IOException;

    /**
     * Returns the log File if it is set, otherwise null is returned.
     * 
     * @return the log File if it is set, otherwise null is returned.
     */
    File getFile();

    /**
     * Closes the FrameworkLog. After the FrameworkLog is closed messages may
     * no longer be logged to it.
     */
    void close();
}
