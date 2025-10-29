/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
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

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.log.ExtendedLogServiceFactory.EquinoxLoggerContext;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Bundle;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Version;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.log.admin.LoggerContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoggerContextTargetMap {
    private final Map<Bundle, ExtendedLogServiceImpl> logServices = new HashMap<>();
    private final Map<String, EquinoxLoggerContext> loggerContexts = new HashMap<>();
    private final Map<Bundle, List<String>> targetToQualifiedNames = new HashMap<>();
    private final Map<String, Collection<Bundle>> qualifiedNameToTargets = new HashMap<>();

    List<String> add(Bundle b) {
        String bsn = b.getSymbolicName();
        if (bsn == null) {
            bsn = ""; //$NON-NLS-1$
        }
        Version v = b.getVersion();
        String version = v == null ? "" : v.toString(); //$NON-NLS-1$
        String location = ExtendedLogServiceFactory.secureAction.getLocation(b);

        List<String> result = new ArrayList<>(3);

        StringBuilder sb = new StringBuilder(bsn);
        getTargetsInternal(bsn).add(b);

        sb.append('|').append(version);
        String bsnVersion = sb.toString();
        getTargetsInternal(bsnVersion).add(b);

        sb.append('|').append(location);
        String bsnVersionLocation = sb.toString();
        getTargetsInternal(bsnVersionLocation).add(b);

        result.add(bsnVersionLocation);
        result.add(bsnVersion);
        result.add(bsn);

        List<String> unmodifiable = Collections.unmodifiableList(result);
        targetToQualifiedNames.put(b, unmodifiable);
        return unmodifiable;
    }

    private Collection<Bundle> getTargetsInternal(String pid) {
        Collection<Bundle> targets = qualifiedNameToTargets.get(pid);
        if (targets == null) {
            targets = new ArrayList<>(1);
            qualifiedNameToTargets.put(pid, targets);
        }
        return targets;
    }

    ExtendedLogServiceImpl getLogService(Bundle bundle, ExtendedLogServiceFactory factory) {
        ExtendedLogServiceImpl logService = logServices.get(bundle);
        if (logService == null) {
            // add bundle to target maps before constructing
            add(bundle);
            logService = new ExtendedLogServiceImpl(factory, bundle);
            if (bundle != null && bundle.getState() != Bundle.UNINSTALLED)
                logServices.put(bundle, logService);
        }
        return logService;
    }

    LoggerContext createLoggerContext(String name, ExtendedLogServiceFactory factory) {
        EquinoxLoggerContext loggerContext = loggerContexts.get(name);
        if (loggerContext == null) {
            loggerContext = factory.createEquinoxLoggerContext(name);
            loggerContexts.put(name, loggerContext);
        }
        return loggerContext;
    }

    EquinoxLoggerContext getRootLoggerContext() {
        return loggerContexts.get(null);
    }

    EquinoxLoggerContext applyLogLevels(EquinoxLoggerContext loggerContext) {
        Collection<Bundle> matching;
        boolean isRoot = loggerContext.getName() == null;
        if (isRoot) {
            // root applies to all loggers
            matching = logServices.keySet();
        } else {
            matching = qualifiedNameToTargets.get(loggerContext.getName());
        }
        if (matching == null) {
            return null;
        }
        EquinoxLoggerContext systemBundleLoggerContext = null;
        for (Bundle bundle : matching) {
            ExtendedLogServiceImpl logService = logServices.get(bundle);
            if (logService != null) {
                // Always apply the effective log context.
                // This may be more costly but it is more simple than checking
                // if the changed context overrides the existing settings
                EquinoxLoggerContext effectiveLoggerContext = getEffectiveLoggerContext(bundle);
                logService.applyLogLevels(effectiveLoggerContext);
                if (bundle.getBundleId() == 0) {
                    systemBundleLoggerContext = effectiveLoggerContext;
                }
            }
        }
        return systemBundleLoggerContext;
    }

    EquinoxLoggerContext getEffectiveLoggerContext(Bundle bundle) {
        List<String> qualifiedNames = targetToQualifiedNames.get(bundle);
        if (qualifiedNames != null) {
            for (String qualifiedName : qualifiedNames) {
                EquinoxLoggerContext loggerContext = loggerContexts.get(qualifiedName);
                if (loggerContext != null && !loggerContext.isEmpty()) {
                    return loggerContext;
                }
            }
        }
        return getRootLoggerContext();
    }
}
