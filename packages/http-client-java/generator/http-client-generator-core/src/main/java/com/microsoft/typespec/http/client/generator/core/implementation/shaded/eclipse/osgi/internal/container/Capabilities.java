/*******************************************************************************
 * Copyright (c) 2012, 2021 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.container;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.ModuleCapability;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.framework.FilterImpl;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.util.ManifestElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Filter;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.InvalidSyntaxException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.namespace.AbstractWiringNamespace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.namespace.BundleNamespace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.namespace.HostNamespace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.namespace.PackageNamespace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.resource.Capability;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.resource.Namespace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.resource.Requirement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Capabilities {
    static class NamespaceSet {
        private final String name;
        private final Set<ModuleCapability> all = new HashSet<>();
        private final Set<ModuleCapability> nonStringIndexes = new HashSet<>(0);
        private final boolean matchMandatory;

        NamespaceSet(String name) {
            this.name = name;
            this.matchMandatory = PackageNamespace.PACKAGE_NAMESPACE.equals(name)
                || BundleNamespace.BUNDLE_NAMESPACE.equals(name)
                || HostNamespace.HOST_NAMESPACE.equals(name);
        }

        List<ModuleCapability> findCapabilities(Requirement requirement) {
            if (!name.equals(requirement.getNamespace())) {
                throw new IllegalArgumentException(
                    "Invalid namespace: " + requirement.getNamespace() + ": expecting: " + name); //$NON-NLS-1$//$NON-NLS-2$
            }
            FilterImpl f = null;
            String filterSpec = requirement.getDirectives().get(Namespace.REQUIREMENT_FILTER_DIRECTIVE);
            if (filterSpec != null) {
                try {
                    f = FilterImpl.newInstance(filterSpec);
                } catch (InvalidSyntaxException e) {
                    return Collections.emptyList();
                }
            }
            Object syntheticAttr = requirement.getAttributes().get(SYNTHETIC_REQUIREMENT);
            boolean synthetic = syntheticAttr instanceof Boolean && (Boolean) syntheticAttr;

            List<ModuleCapability> result;
            if (filterSpec == null) {
                result = match(null, all, synthetic);
            } else {
                String indexKey = f.getPrimaryKeyValue(name);
                if (indexKey == null) {
                    result = match(f, all, synthetic);
                } else {
                    result = new ArrayList<>(0);
                    if (!nonStringIndexes.isEmpty()) {
                        List<ModuleCapability> nonStringResult = match(f, nonStringIndexes, synthetic);
                        for (ModuleCapability capability : nonStringResult) {
                            if (!result.contains(capability)) {
                                result.add(capability);
                            }
                        }
                    }
                }
            }
            return result;
        }

        private List<ModuleCapability> match(Filter f, Set<ModuleCapability> candidates, boolean synthetic) {
            List<ModuleCapability> result = new ArrayList<>(1);
            for (ModuleCapability candidate : candidates) {
                if (matches(f, candidate, !synthetic && matchMandatory)) {
                    result.add(candidate);
                }
            }
            return result;
        }
    }

    public static final Pattern MANDATORY_ATTR = Pattern.compile("\\(([^(=<>]+)\\s*[=<>]\\s*[^)]+\\)"); //$NON-NLS-1$
    public static final String SYNTHETIC_REQUIREMENT
        = "com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.synthetic"; //$NON-NLS-1$

    public static boolean matches(Filter f, Capability candidate, boolean matchMandatory) {
        if (f != null && !f.matches(candidate.getAttributes())) {
            return false;
        }
        if (matchMandatory) {
            // check for mandatory directive
            String mandatory = candidate.getDirectives().get(AbstractWiringNamespace.CAPABILITY_MANDATORY_DIRECTIVE);
            if (mandatory == null) {
                return true;
            }
            if (f == null) {
                return false;
            }
            Matcher matcher = MANDATORY_ATTR.matcher(f.toString());
            String[] mandatoryAttrs = ManifestElement.getArrayFromList(mandatory, ","); //$NON-NLS-1$
            boolean allPresent = true;
            for (String mandatoryAttr : mandatoryAttrs) {
                matcher.reset();
                boolean found = false;
                while (matcher.find()) {
                    int numGroups = matcher.groupCount();
                    for (int i = 1; i <= numGroups; i++) {
                        if (mandatoryAttr.equals(matcher.group(i))) {
                            found = true;
                            break;
                        }
                    }
                }
                allPresent &= found;
            }
            return allPresent;
        }
        return true;
    }

    Map<String, NamespaceSet> namespaceSets = new HashMap<>();

    /**
     * Returns a mutable snapshot of capabilities that are candidates for satisfying
     * the specified requirement.
     * 
     * @param requirement the requirement
     * @return the candidates for the requirement
     */
    public List<ModuleCapability> findCapabilities(Requirement requirement) {
        NamespaceSet namespaceSet = namespaceSets.get(requirement.getNamespace());
        if (namespaceSet == null) {
            return Collections.emptyList();
        }
        return namespaceSet.findCapabilities(requirement);
    }
}
