/*******************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google) - use parameterized types (bug 442021)
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.runtime;

import java.util.*;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.*;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.equinox.log.*;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.framework.log.FrameworkLogEntry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Bundle;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.log.LogEntry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.log.LogService;

/**
 * A log writer that writes log entries.
 * <p>
 * Note that this class just provides a bridge from the old ILog interface to
 * the new extended log service
 */
@SuppressWarnings("deprecation") // LogService, PackageAdmin
public class PlatformLogWriter implements SynchronousLogListener, LogFilter {
	public static final String EQUINOX_LOGGER_NAME = "com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.equinox.logger"; //$NON-NLS-1$

    public PlatformLogWriter() {
    }

    public static int getLevel(IStatus status) {
		switch (status.getSeverity()) {
		case IStatus.ERROR:
			return LogService.LOG_ERROR;
		case IStatus.WARNING:
			return LogService.LOG_WARNING;
		case IStatus.INFO:
			return LogService.LOG_INFO;
		case IStatus.OK:
			return LogService.LOG_DEBUG;
		case IStatus.CANCEL:
		default:
			return 32; // unknown
		}
	}

	public static FrameworkLogEntry getLog(IStatus status) {
		return getLogImpl(status, Collections.newSetFromMap(new IdentityHashMap<>()));
	}

	private static FrameworkLogEntry getLogImpl(IStatus status, Set<IStatus> visited) {
		Throwable t = status.getException();
		ArrayList<FrameworkLogEntry> childlist = new ArrayList<>();

		int stackCode = t instanceof CoreException ? 1 : 0;
		// ensure a substatus inside a CoreException is properly logged
		if (stackCode == 1) {
			IStatus coreStatus = ((CoreException) t).getStatus();
			if (coreStatus != null) {
				if (visited.add(coreStatus)) {
					childlist.add(getLogImpl(coreStatus, visited));
					visited.remove(coreStatus);
				}
			}
		}

		if (status.isMultiStatus()) {
			IStatus[] children = status.getChildren();
			for (IStatus child : children) {
				if (visited.add(child)) {
					childlist.add(getLogImpl(child, visited));
					visited.remove(child);
				}
			}
		}

		FrameworkLogEntry[] children = childlist.isEmpty() ? null
				: childlist.toArray(new FrameworkLogEntry[childlist.size()]);

		return new FrameworkLogEntry(status, status.getPlugin(), status.getSeverity(), status.getCode(),
				status.getMessage(), stackCode, t, children);
	}

    @Override
	public boolean isLoggable(Bundle b, String loggerName, int logLevel) {
		return EQUINOX_LOGGER_NAME.equals(loggerName) && RuntimeLog.hasListeners();
	}

	@Override
	public void logged(LogEntry entry) {
		RuntimeLog.logToListeners(convertToStatus(entry));
	}

	public static IStatus convertToStatus(LogEntry logEntry) {
		Object context = null;
		if (logEntry instanceof ExtendedLogEntry) {
			context = ((ExtendedLogEntry) logEntry).getContext();
		}
		if (context instanceof IStatus) {
			return (IStatus) context;
		}
		if (context instanceof FrameworkLogEntry fLogEntry) {
			context = fLogEntry.getContext();
			if (context instanceof IStatus) {
				return (IStatus) context;
			}
			return convertToStatus(fLogEntry);
		}
		return convertRawEntryToStatus(logEntry);
	}

	private static IStatus convertToStatus(FrameworkLogEntry entry) {
		FrameworkLogEntry[] children = entry.getChildren();
		if (children != null) {
			IStatus[] statusChildren = new Status[children.length];
			for (int i = 0; i < statusChildren.length; i++) {
				statusChildren[i] = convertToStatus(children[i]);
			}
			return new MultiStatus(entry.getEntry(), entry.getBundleCode(), statusChildren, entry.getMessage(),
					entry.getThrowable());
		}
		return new Status(entry.getSeverity(), entry.getEntry(), entry.getBundleCode(), entry.getMessage(),
				entry.getThrowable());
	}

	private static IStatus convertRawEntryToStatus(LogEntry logEntry) {
		int severity;
		switch (logEntry.getLogLevel()) {
		case ERROR:
			severity = IStatus.ERROR;
			break;
		case WARN:
			severity = IStatus.WARNING;
			break;
		case INFO:
			severity = IStatus.INFO;
			break;
		case DEBUG:
		case TRACE:
		case AUDIT:
			severity = IStatus.OK;
			break;
		default:
			severity = -1;
			break;
		}
		Bundle bundle = logEntry.getBundle();
		return new Status(severity, bundle == null ? null : bundle.getSymbolicName(), logEntry.getMessage(),
				logEntry.getException());
	}
}
