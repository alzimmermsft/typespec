/*******************************************************************************
 * Copyright (c) 2006, 2020 IBM Corporation and others.
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

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.adaptor.EclipseStarter;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.framework.log.FrameworkLog;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.framework.log.FrameworkLogEntry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.framework.EquinoxConfiguration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.service.datalocation.Location;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.log.LogLevel;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.log.admin.LoggerContext;
import java.io.File;
import java.io.Writer;

public class EquinoxLogServices {
    static final String EQUINOX_LOGGER_NAME
        = "com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.equinox.logger"; //$NON-NLS-1$
    static final String PERF_LOGGER_NAME
        = "com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.performance.logger"; //$NON-NLS-1$
    private static final String PROP_LOG_ENABLED = "eclipse.log.enabled"; //$NON-NLS-1$

    // The eclipse log file extension */
    private static final String LOG_EXT = ".log"; //$NON-NLS-1$
    private final FrameworkLog rootFrameworkLog;

    public EquinoxLogServices(EquinoxConfiguration environmentInfo) {
        Location configuration = environmentInfo.getEquinoxLocations().getConfigurationLocation();
        String logFilePath = environmentInfo.getConfiguration(EclipseStarter.PROP_LOGFILE);
        if (logFilePath == null) {
            logFilePath = System.currentTimeMillis() + EquinoxLogServices.LOG_EXT;
        }

        File logFile = new File(logFilePath);
        if (!logFile.isAbsolute()) {
            File configAreaDirectory = null;
            if (configuration != null)
                // TODO assumes the URL is a file: url
                configAreaDirectory = new File(configuration.getURL().getPath());

            if (configAreaDirectory != null) {
                logFile = new File(configAreaDirectory, logFilePath);
            } else {
                logFile = null;

            }
        }

        boolean enabled = "true".equals(environmentInfo.getConfiguration(PROP_LOG_ENABLED, "true")); //$NON-NLS-1$ //$NON-NLS-2$
        EquinoxLogWriter logWriter;
        EquinoxLogWriter perfWriter;
        if (logFile != null) {
            environmentInfo.setConfiguration(EclipseStarter.PROP_LOGFILE, logFile.getAbsolutePath());
            logWriter = new EquinoxLogWriter(logFile, EQUINOX_LOGGER_NAME, enabled, environmentInfo);

            File perfLogFile = new File(logFile.getParentFile(), "performance.log"); //$NON-NLS-1$
            perfWriter = new EquinoxLogWriter(perfLogFile, PERF_LOGGER_NAME, true, environmentInfo);
        } else {
            logWriter = new EquinoxLogWriter((Writer) null, EQUINOX_LOGGER_NAME, enabled, environmentInfo);
            perfWriter = new EquinoxLogWriter((Writer) null, PERF_LOGGER_NAME, true, environmentInfo);
        }

        if ("true".equals(environmentInfo.getConfiguration(EclipseStarter.PROP_CONSOLE_LOG))) //$NON-NLS-1$
            logWriter.setConsoleLog(true);
        String logHistoryMaxProp = environmentInfo.getConfiguration(EquinoxConfiguration.PROP_LOG_HISTORY_MAX);
        int logHistoryMax = 0;
        if (logHistoryMaxProp != null) {
            try {
                logHistoryMax = Integer.parseInt(logHistoryMaxProp);
            } catch (NumberFormatException e) {
                // ignore and use 0
            }
        }

        LogLevel defaultLevel = LogLevel.WARN;
        try {
            String defaultLevelConfig = environmentInfo.getConfiguration(LoggerContext.LOGGER_CONTEXT_DEFAULT_LOGLEVEL);
            if (defaultLevelConfig != null) {
                defaultLevel = LogLevel.valueOf(defaultLevelConfig);
            }
        } catch (IllegalArgumentException e) {
            // ignore and use LogLevel.WARN
        }

        LogServiceManager logServiceManager
            = new LogServiceManager(logHistoryMax, defaultLevel, environmentInfo, logWriter, perfWriter);
        EquinoxLogFactory eclipseLogFactory = new EquinoxLogFactory(logWriter, logServiceManager);
        rootFrameworkLog = eclipseLogFactory.createFrameworkLog(null, logWriter);

        logWriter.setLoggerAdmin(logServiceManager.getLoggerAdmin());
        perfWriter.setLoggerAdmin(logServiceManager.getLoggerAdmin());
    }

    public FrameworkLog getFrameworkLog() {
        return rootFrameworkLog;
    }

    public void log(String entry, int severity, String message, Throwable throwable) {
        log(entry, severity, message, throwable, null);
    }

    public void log(String entry, int severity, String message, Throwable throwable, FrameworkLogEntry[] children) {
        getFrameworkLog().log(new FrameworkLogEntry(entry, severity, 0, message, 0, throwable, children));
    }
}
