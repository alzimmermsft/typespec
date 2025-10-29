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

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.equinox.log.ExtendedLogEntry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Bundle;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.ServiceReference;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.log.LogEntry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.log.LogLevel;
import java.util.Map;
import java.util.WeakHashMap;

public class ExtendedLogEntryImpl implements ExtendedLogEntry, LogEntry {

    private static long nextSequenceNumber = 1L;
    private static long nextThreadId = 1L;
    private static final Map<Thread, Long> threadIds = createThreadIdMap();

    private final String loggerName;
    private final Bundle bundle;
    private final int level;
    private final LogLevel logLevelEnum;
    private final String message;
    private final ServiceReference<?> ref;
    private final Throwable throwable;
    private final Object contextObject;
    private final long time;
    private final long threadId;
    private final String threadName;
    private final long sequenceNumber;
    private final StackTraceElement stackTraceElement;

    private static Map<Thread, Long> createThreadIdMap() {
        try {
            Thread.class.getMethod("getId", (Class[]) null); //$NON-NLS-1$
        } catch (NoSuchMethodException e) {
            return new WeakHashMap<>();
        }
        return null;
    }

    private static long getId(Thread thread) {
        if (threadIds == null)
            return thread.getId();

        Long threadId = threadIds.get(thread);
        if (threadId == null) {
            threadId = Long.valueOf(nextThreadId++);
            threadIds.put(thread, threadId);
        }
        return threadId.longValue();
    }

    public ExtendedLogEntryImpl(Bundle bundle, String loggerName, StackTraceElement stackTraceElement,
        Object contextObject, LogLevel logLevelEnum, int level, String message, ServiceReference<?> ref,
        Throwable throwable) {
        this.time = System.currentTimeMillis();
        this.loggerName = loggerName;
        this.bundle = bundle;
        this.level = level;
        this.logLevelEnum = logLevelEnum;
        this.message = message;
        this.throwable = throwable;
        this.ref = ref;
        this.contextObject = contextObject;

        Thread currentThread = Thread.currentThread();
        this.threadName = currentThread.getName();

        synchronized (ExtendedLogEntryImpl.class) {
            this.threadId = getId(currentThread);
            this.sequenceNumber = nextSequenceNumber++;
        }

        this.stackTraceElement = stackTraceElement;
    }

    @Override
    public long getSequenceNumber() {
        return sequenceNumber;
    }

    @Override
    public Bundle getBundle() {
        return bundle;
    }

    @Override
    public Throwable getException() {
        return throwable;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public ServiceReference<?> getServiceReference() {
        return ref;
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public Object getContext() {
        return contextObject;
    }

    @Override
    public LogLevel getLogLevel() {
        return logLevelEnum;
    }

    @Override
    public long getSequence() {
        return getSequenceNumber();
    }

    @Override
    public StackTraceElement getLocation() {
        return stackTraceElement;
    }
}
