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

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Bundle;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.BundlePermission;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.CapabilityPermission;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.PackagePermission;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.namespace.BundleNamespace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.namespace.HostNamespace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.namespace.PackageNamespace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.wiring.BundleCapability;
import java.security.Permission;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;

public class InternalUtils {

    /**
     * Returns a mutable wrapped around the given list, that creates a copy of the
     * given list only if the list is about to be modified.
     * <p>
     * This method assumes that the given List is immutable and a
     * {@link RandomAccess} list.
     * </p>
     * 
     * @param list the list to be copied.
     * @return an effectively mutable and lazy copy of the given list.
     */
    public static <T> List<T> asCopy(List<? extends T> list) {
        if (list == null) {
            return null;
        }
        if (!(list instanceof RandomAccess)) {
            throw new IllegalArgumentException("Only RandomAccess lists are supported"); //$NON-NLS-1$
        }
        return new CopyOnFirstWriteList<>(list);
    }

    private static final class CopyOnFirstWriteList<T> extends AbstractList<T> implements RandomAccess {
        private List<T> copy;
        private boolean copied = false;

        CopyOnFirstWriteList(List<? extends T> list) {
            copy = asList(list);
        }

        @Override
        public T get(int index) {
            return copy.get(index);
        }

        @Override
        public int size() {
            return copy.size();
        }

        @Override
        public void add(int index, T element) {
            ensureCopied();
            copy.add(index, element);
            modCount++;
        }

        @Override
        public T remove(int index) {
            ensureCopied();
            T removed = copy.remove(index);
            modCount++;
            return removed;
        }

        @Override
        public T set(int index, T element) {
            ensureCopied();
            T set = copy.set(index, element);
            modCount++;
            return set;
        }

        private void ensureCopied() {
            if (!copied) {
                copy = new ArrayList<>(copy);
                copied = true;
            }
        }
    }

    /**
     * Coerce the generic type of a list from List<? extends T> to List<T>
     * 
     * @param l List to be coerced.
     * @return l coerced to List<Capability>
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> asList(List<? extends T> l) {
        return (List<T>) l;
    }

    public static void filterCapabilityPermissions(Collection<? extends BundleCapability> capabilities) {
        if (System.getSecurityManager() == null) {
            return;
        }
        for (Iterator<? extends BundleCapability> iCapabilities = capabilities.iterator(); iCapabilities.hasNext();) {
            BundleCapability capability = iCapabilities.next();
            Permission permission = getProvidePermission(capability);
            Bundle provider = capability.getRevision().getBundle();
            if (provider != null && !provider.hasPermission(permission)) {
                iCapabilities.remove();
            }
        }
    }

    public static Permission getRequirePermission(BundleCapability candidate) {
        String name = candidate.getNamespace();
        if (PackageNamespace.PACKAGE_NAMESPACE.equals(name)) {
            return new PackagePermission(getPermisionName(candidate), candidate.getRevision().getBundle(),
                PackagePermission.IMPORT);
        }
        if (HostNamespace.HOST_NAMESPACE.equals(name)) {
            return new BundlePermission(getPermisionName(candidate), BundlePermission.FRAGMENT);
        }
        if (BundleNamespace.BUNDLE_NAMESPACE.equals(name)) {
            return new BundlePermission(getPermisionName(candidate), BundlePermission.REQUIRE);
        }
        return new CapabilityPermission(name, candidate.getAttributes(), candidate.getRevision().getBundle(),
            CapabilityPermission.REQUIRE);
    }

    public static Permission getProvidePermission(BundleCapability candidate) {
        String name = candidate.getNamespace();
        if (PackageNamespace.PACKAGE_NAMESPACE.equals(name)) {
            return new PackagePermission(getPermisionName(candidate), PackagePermission.EXPORTONLY);
        }
        if (HostNamespace.HOST_NAMESPACE.equals(name)) {
            return new BundlePermission(getPermisionName(candidate), BundlePermission.HOST);
        }
        if (BundleNamespace.BUNDLE_NAMESPACE.equals(name)) {
            return new BundlePermission(getPermisionName(candidate), BundlePermission.PROVIDE);
        }
        return new CapabilityPermission(name, CapabilityPermission.PROVIDE);
    }

    private static String getPermisionName(BundleCapability candidate) {
        Object name = candidate.getAttributes().get(candidate.getNamespace());
        if (name instanceof String) {
            return (String) name;
        }
        if (name instanceof Collection) {
            Collection<?> names = (Collection<?>) name;
            return names.isEmpty() ? "unknown" : names.iterator().next().toString(); //$NON-NLS-1$
        }
        return "unknown"; //$NON-NLS-1$
    }

    public static <E> Enumeration<E> asEnumeration(Iterator<E> it) {
        return new Enumeration<E>() {
            @Override
            public boolean hasMoreElements() {
                return it.hasNext();
            }

            @Override
            public E nextElement() {
                return it.next();
            }
        };
    }
}
