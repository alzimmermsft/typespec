/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.registry;

public class RegistryIndexElement implements KeyedElement {

    // The key on which indexing is done
    final protected String key;

    // Extension points matching the key
    private RegistryIndexChildren extensionPoints;

    // Extensions matching the key
    private RegistryIndexChildren extensions;

    public RegistryIndexElement(String key) {
        this.key = key;
    }

    protected int[] getExtensions() {
        if (extensions == null) {
            return RegistryIndexChildren.EMPTY_ARRAY;
        }
        return extensions.getChildren();
    }

    public boolean updateExtensions(int[] IDs, boolean add) {
        if (extensions == null) {
            extensions = new RegistryIndexChildren();
        }

        if (add) {
            return extensions.linkChildren(IDs);
        }
        return extensions.unlinkChildren(IDs);
    }

    public boolean updateExtensionPoints(int[] IDs, boolean add) {
        if (extensionPoints == null) {
            extensionPoints = new RegistryIndexChildren();
        }

        if (add) {
            return extensionPoints.linkChildren(IDs);
        }
        return extensionPoints.unlinkChildren(IDs);
    }

    // Implements the KeyedElement interface
    @Override
    public int getKeyHashCode() {
        return getKey().hashCode();
    }

    @Override
    public Object getKey() {
        return key;
    }

    @Override
    public boolean compare(KeyedElement other) {
        return key.equals(((RegistryIndexElement) other).key);
    }
}
