/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation and others.
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

package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.log;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.equinox.log.Logger;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.framework.log.FrameworkLog;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.framework.log.FrameworkLogEntry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Bundle;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.FrameworkEvent;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.ServiceFactory;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.ServiceRegistration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.log.LogService;
import java.io.File;
import java.io.IOException;

class EquinoxLogFactory implements ServiceFactory<FrameworkLog> {
    final EquinoxLogWriter defaultWriter;
    final LogServiceManager logManager;

    public EquinoxLogFactory(EquinoxLogWriter defaultWriter, LogServiceManager logManager) {
        this.defaultWriter = defaultWriter;
        this.logManager = logManager;
    }

    @Override
    public FrameworkLog getService(final Bundle bundle, ServiceRegistration<FrameworkLog> registration) {
        return createFrameworkLog(bundle, defaultWriter);
    }

    FrameworkLog createFrameworkLog(Bundle bundle, EquinoxLogWriter eclipseWriter) {
        final EquinoxLogWriter logWriter = eclipseWriter == null ? defaultWriter : eclipseWriter;
        final Logger logger = bundle == null
            ? logManager.getSystemBundleLog().getLogger(eclipseWriter.getLoggerName())
            : logManager.getSystemBundleLog().getLogger(bundle, logWriter.getLoggerName());
        return new FrameworkLog() {

            @Override
            public void setFile(File newFile, boolean append) throws IOException {
                logWriter.setFile(newFile, append);
            }

            @Override
            public void log(FrameworkLogEntry logEntry) {
                logger.log(logEntry, convertLevel(logEntry), logEntry.getMessage(), logEntry.getThrowable());
            }

            @Override
            public void log(FrameworkEvent frameworkEvent) {
                Bundle b = frameworkEvent.getBundle();
                Throwable t = frameworkEvent.getThrowable();
                String entry = b.getSymbolicName() == null ? b.getLocation() : b.getSymbolicName();
                int severity;
                switch (frameworkEvent.getType()) {
                    case FrameworkEvent.INFO:
                        severity = FrameworkLogEntry.INFO;
                        break;

                    case FrameworkEvent.ERROR:
                        severity = FrameworkLogEntry.ERROR;
                        break;

                    case FrameworkEvent.WARNING:
                        severity = FrameworkLogEntry.WARNING;
                        break;

                    default:
                        severity = FrameworkLogEntry.OK;
                }
                FrameworkLogEntry logEntry = new FrameworkLogEntry(entry, severity, 0, "", 0, t, null); //$NON-NLS-1$
                log(logEntry);
            }

            @Override
            public File getFile() {
                return logWriter.getFile();
            }

            @Override
            public void close() {
                logWriter.close();
            }
        };
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration<FrameworkLog> registration, FrameworkLog service) {
        // nothing
    }

    @SuppressWarnings("deprecation")
    static int convertLevel(FrameworkLogEntry logEntry) {
        switch (logEntry.getSeverity()) {
            case FrameworkLogEntry.ERROR:
                return LogService.LOG_ERROR;

            case FrameworkLogEntry.WARNING:
                return LogService.LOG_WARNING;

            case FrameworkLogEntry.INFO:
                return LogService.LOG_INFO;

            case FrameworkLogEntry.OK:
                return LogService.LOG_DEBUG;

            case FrameworkLogEntry.CANCEL:
            default:
                return 32; // unknown
        }
    }
}
