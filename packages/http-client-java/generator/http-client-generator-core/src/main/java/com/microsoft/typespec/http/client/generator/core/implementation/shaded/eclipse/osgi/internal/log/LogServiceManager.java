/*******************************************************************************
 * Copyright (c) 2006, 2020 Cognos Incorporated, IBM Corporation and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.log;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.equinox.log.ExtendedLogService;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.equinox.log.LogFilter;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.framework.EquinoxConfiguration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.framework.EquinoxContainer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.AllServiceListener;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Bundle;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.BundleContext;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Constants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.FrameworkEvent;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.FrameworkListener;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.SynchronousBundleListener;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Version;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.log.LogLevel;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.log.LogListener;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.log.LogService;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.log.admin.LoggerAdmin;
import java.io.File;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogServiceManager implements SynchronousBundleListener, FrameworkListener, AllServiceListener {
    private static final String LOGGER_FRAMEWORK_EVENT = "Events.Framework"; //$NON-NLS-1$

    private final ExtendedLogReaderServiceFactory logReaderServiceFactory;
    private final ExtendedLogServiceFactory logServiceFactory;
    private final ExtendedLogServiceImpl systemBundleLog;

    public LogServiceManager(int maxHistory, LogLevel defaultLevel, EquinoxConfiguration equinoxConfiguration,
        LogListener... systemListeners) {
        logReaderServiceFactory = new ExtendedLogReaderServiceFactory(maxHistory, defaultLevel);
        logServiceFactory
            = new ExtendedLogServiceFactory(logReaderServiceFactory, equinoxConfiguration, new MockSystemBundle());
        systemBundleLog = logServiceFactory.getSystemBundleLog();
        for (LogListener logListener : systemListeners) {
            if (logListener instanceof LogFilter)
                logReaderServiceFactory.addLogListener(logListener, (LogFilter) logListener);
            else
                logReaderServiceFactory.addLogListener(logListener, ExtendedLogReaderServiceFactory.NULL_LOGGER_FILTER);
        }

    }

    public ExtendedLogService getSystemBundleLog() {
        return systemBundleLog;
    }

    LoggerAdmin getLoggerAdmin() {
        return logServiceFactory.getLoggerAdmin();
    }

    /**
     * FrameworkListener.frameworkEvent method.
     */
    @SuppressWarnings("deprecation")
    @Override
    public void frameworkEvent(FrameworkEvent event) {
        Bundle bundle = event.getBundle();
        int eventType = event.getType();
        int logType = switch (eventType) {
            case FrameworkEvent.ERROR -> LogService.LOG_ERROR;
            case FrameworkEvent.WARNING -> LogService.LOG_WARNING;
            default -> LogService.LOG_INFO;
        };
        String bsn = (bundle == null) ? null : bundle.getSymbolicName();
        String loggerName = (bsn == null) ? LOGGER_FRAMEWORK_EVENT : LOGGER_FRAMEWORK_EVENT + "." + bsn; //$NON-NLS-1$
        if (logReaderServiceFactory.isLoggable(bundle, loggerName, logType)) {
            LoggerImpl logger = (LoggerImpl) systemBundleLog.getLogger(loggerName);
            logger.log(bundle, event, null, logType, getFrameworkEventTypeName(eventType), null, event.getThrowable());
        }
    }

    /**
     * Convert FrameworkEvent type to a string.
     */
    private static String getFrameworkEventTypeName(int type) {
        return switch (type) {
            case FrameworkEvent.ERROR -> ("FrameworkEvent ERROR"); //$NON-NLS-1$

            case FrameworkEvent.INFO -> ("FrameworkEvent INFO"); //$NON-NLS-1$

            case FrameworkEvent.PACKAGES_REFRESHED -> ("FrameworkEvent PACKAGES REFRESHED"); //$NON-NLS-1$

            case FrameworkEvent.STARTED -> ("FrameworkEvent STARTED"); //$NON-NLS-1$

            case FrameworkEvent.STARTLEVEL_CHANGED -> ("FrameworkEvent STARTLEVEL CHANGED"); //$NON-NLS-1$

            case FrameworkEvent.WARNING -> ("FrameworkEvent WARNING"); //$NON-NLS-1$

            default -> ("FrameworkEvent " + Integer.toHexString(type)); //$NON-NLS-1$
        };
    }

    static class MockSystemBundle implements Bundle {

        @Override
        public int compareTo(Bundle o) {
            long idcomp = getBundleId() - o.getBundleId();
            return (idcomp < 0L) ? -1 : ((idcomp > 0L) ? 1 : 0);
        }

        @Override
        public int getState() {
            return Bundle.RESOLVED;
        }

        @Override
        public long getBundleId() {
            return 0;
        }

        @Override
        public String getLocation() {
            return Constants.SYSTEM_BUNDLE_LOCATION;
        }

        @Override
        public boolean hasPermission(Object permission) {
            return true;
        }

        @Override
        public Dictionary<String, String> getHeaders(String locale) {
            return null;
        }

        @Override
        public String getSymbolicName() {
            return EquinoxContainer.NAME;
        }

        @Override
        public URL getEntry(String path) {
            return null;
        }

        @Override
        public long getLastModified() {
            return System.currentTimeMillis();
        }

        @Override
        public Enumeration<URL> findEntries(String path, String filePattern, boolean recurse) {
            return null;
        }

        @Override
        public BundleContext getBundleContext() {
            return null;
        }

        @Override
        public Map<X509Certificate, List<X509Certificate>> getSignerCertificates(int signersType) {
            return new HashMap<>();
        }

        @Override
        public Version getVersion() {
            return new Version(0, 0, 0);
        }

        @Override
        public <A> A adapt(Class<A> type) {
            return null;
        }

        @Override
        public File getDataFile(String filename) {
            return null;
        }

    }
}
