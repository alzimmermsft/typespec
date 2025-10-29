/*******************************************************************************
 * Copyright (c) 2012, 2016 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.container.Capabilities;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.framework.FilterImpl;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.InvalidSyntaxException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.namespace.BundleNamespace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.namespace.HostNamespace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.namespace.PackageNamespace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.wiring.BundleCapability;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.wiring.BundleRequirement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.resource.Namespace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.resource.Requirement;
import java.util.Map;

/**
 * An implementation of {@link BundleRequirement}. This requirement implements
 * the matches method according to the OSGi specification which includes
 * implementing the mandatory directive for the osgi.wiring.* namespaces.
 * 
 * @since 3.10
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ModuleRequirement implements BundleRequirement {
    private final String namespace;
    private final Map<String, String> directives;
    private final Map<String, Object> attributes;
    private final ModuleRevision revision;

    ModuleRequirement(String namespace, Map<String, String> directives, Map<String, ?> attributes,
        ModuleRevision revision) {
        this.namespace = namespace;
        this.directives = ModuleRevisionBuilder.unmodifiableMap(directives);
        this.attributes = ModuleRevisionBuilder.unmodifiableMap(attributes);
        this.revision = revision;
    }

    @Override
    public ModuleRevision getRevision() {
        return revision;
    }

    @Override
    public boolean matches(BundleCapability capability) {
        if (!namespace.equals(capability.getNamespace()))
            return false;
        String filterSpec = directives.get(Namespace.REQUIREMENT_FILTER_DIRECTIVE);
        FilterImpl f = null;
        if (filterSpec != null) {
            try {
                f = FilterImpl.newInstance(filterSpec);
            } catch (InvalidSyntaxException e) {
                return false;
            }
        }
        boolean matchMandatory = PackageNamespace.PACKAGE_NAMESPACE.equals(namespace)
            || BundleNamespace.BUNDLE_NAMESPACE.equals(namespace)
            || HostNamespace.HOST_NAMESPACE.equals(namespace);
        return Capabilities.matches(f, capability, matchMandatory);
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public Map<String, String> getDirectives() {
        return directives;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public ModuleRevision getResource() {
        return revision;
    }

    @Override
    public String toString() {
        return namespace + ModuleContainer.toString(attributes, false) + ModuleContainer.toString(directives, true);
    }

    class DynamicModuleRequirement extends ModuleRequirement {

        DynamicModuleRequirement(ModuleRevision host, Map<String, String> directives) {
            super(ModuleRequirement.this.getNamespace(), directives, ModuleRequirement.this.getAttributes(), host);
        }

        ModuleRequirement getOriginal() {
            return ModuleRequirement.this;
        }
    }

    static boolean isOptional(Requirement req) {
        String resolution = req.getDirectives().get(Namespace.REQUIREMENT_RESOLUTION_DIRECTIVE);
        return Namespace.RESOLUTION_OPTIONAL.equalsIgnoreCase(resolution);
    }

}
