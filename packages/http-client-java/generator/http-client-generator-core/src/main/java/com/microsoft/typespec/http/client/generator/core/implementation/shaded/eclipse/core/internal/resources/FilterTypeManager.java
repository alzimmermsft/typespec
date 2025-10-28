/*******************************************************************************
 * Copyright (c) 2008, 2015 Freescale Semiconductor and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Serge Beauchamp (Freescale Semiconductor) - initial API and implementation
 *     IBM Corporation - ongoing development
 *     James Blackburn (Broadcom Corp.) - ongoing development
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 473427
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IFilterMatcherDescriptor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.ResourcesPlugin;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.filtermatchers.AbstractFileInfoMatcher;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IConfigurationElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IExtension;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IExtensionPoint;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IRegistryEventListener;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.RegistryFactory;
import java.util.HashMap;

/**
 * This class collects all the registered {@link AbstractFileInfoMatcher} instances along
 * with their properties.
 */
class FilterTypeManager {

    private static final String FILTER_ELEMENT = "filterMatcher"; //$NON-NLS-1$

    private final HashMap<String, IFilterMatcherDescriptor> factories = new HashMap<>();

    public FilterTypeManager() {
        IExtensionPoint point = RegistryFactory.getRegistry()
            .getExtensionPoint(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PT_FILTER_MATCHERS);
        if (point != null) {
            // initial population
            for (IExtension extension : point.getExtensions()) {
                processExtension(extension);
            }
            RegistryFactory.getRegistry().addListener(new IRegistryEventListener() {
                @Override
                public void added(IExtension[] extensions) {
                    for (IExtension extension : extensions) {
                        processExtension(extension);
                    }
                }

                @Override
                public void added(IExtensionPoint[] extensionPoints) {
                    // nothing to do
                }

                @Override
                public void removed(IExtension[] extensions) {
                    for (IExtension extension : extensions) {
                        processRemovedExtension(extension);
                    }
                }

                @Override
                public void removed(IExtensionPoint[] extensionPoints) {
                    // nothing to do
                }
            });
        }
    }

    public IFilterMatcherDescriptor getFilterDescriptor(String id) {
        return factories.get(id);
    }

    protected void processExtension(IExtension extension) {
        IConfigurationElement[] elements = extension.getConfigurationElements();
        for (IConfigurationElement element : elements) {
            if (element.getName().equalsIgnoreCase(FILTER_ELEMENT)) {
                IFilterMatcherDescriptor desc = new FilterDescriptor(element);
                factories.put(desc.getId(), desc);
            }
        }
    }

    protected void processRemovedExtension(IExtension extension) {
        IConfigurationElement[] elements = extension.getConfigurationElements();
        for (IConfigurationElement element : elements) {
            if (element.getName().equalsIgnoreCase(FILTER_ELEMENT)) {
                IFilterMatcherDescriptor desc = new FilterDescriptor(element, false);
                factories.remove(desc.getId());
            }
        }
    }
}
