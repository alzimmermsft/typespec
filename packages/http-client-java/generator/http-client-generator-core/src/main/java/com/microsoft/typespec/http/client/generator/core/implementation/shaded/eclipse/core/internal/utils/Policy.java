/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.utils;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.ResourcesPlugin;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.ILog;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.NullProgressMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.OperationCanceledException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Platform;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Status;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.SubProgressMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Bundle;

public class Policy {

    public static final boolean buildOnCancel = false;
    public static final long MAX_BUILD_DELAY = 1000;

    public static final long MIN_BUILD_DELAY = 100;
    public static int opWork = 100;
    public static final int totalWork = 100;

    public static void checkCanceled(IProgressMonitor monitor) {
        if (monitor.isCanceled()) {
            throw new OperationCanceledException();
        }
    }

    public static void log(int severity, String message, Throwable t) {
        if (message == null) {
            message = ""; //$NON-NLS-1$
        }
        log(new Status(severity, ResourcesPlugin.PI_RESOURCES, 1, message, t));
    }

    public static void log(IStatus status) {
        final Bundle bundle = Platform.getBundle(ResourcesPlugin.PI_RESOURCES);
        if (bundle == null) {
            return;
        }
        ILog.of(bundle).log(status);
    }

    /**
     * Logs a throwable, assuming severity of error
     */
    public static void log(Throwable t) {
        log(IStatus.ERROR, "Internal Error", t); //$NON-NLS-1$
    }

    public static IProgressMonitor monitorFor(IProgressMonitor monitor) {
        return monitor == null ? new NullProgressMonitor() : monitor;
    }

    public static IProgressMonitor subMonitorFor(IProgressMonitor monitor, int ticks) {
        if (monitor == null) {
            return new NullProgressMonitor();
        }
        if (monitor instanceof NullProgressMonitor) {
            return monitor;
        }
        return new SubProgressMonitor(monitor, ticks);
    }
}
