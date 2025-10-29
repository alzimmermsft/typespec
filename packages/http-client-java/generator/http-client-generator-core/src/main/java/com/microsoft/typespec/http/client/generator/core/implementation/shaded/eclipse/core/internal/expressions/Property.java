/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.expressions;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Assert;

public class Property {

    private final Class<?> fType;
    private final String fNamespace;
    private final String fName;

    /* package */ Property(Class<?> type, String namespace, String name) {
        Assert.isNotNull(type);
        Assert.isNotNull(namespace);
        Assert.isNotNull(name);

        fType = type;
        fNamespace = namespace;
        fName = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Property other)) {
            return false;
        }
        return fType.equals(other.fType) && fNamespace.equals(other.fNamespace) && fName.equals(other.fName);
    }

    @Override
    public int hashCode() {
        return (fType.hashCode() << 16) | fNamespace.hashCode() << 8 | fName.hashCode();
    }

    @Override
    public String toString() {
        // $NON-NLS-1$
        return "Property [" //$NON-NLS-1$
            + fNamespace + "." //$NON-NLS-1$
            + fName + ", type=" //$NON-NLS-1$
            + fType + "]";
    }
}
