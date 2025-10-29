/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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

package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.dom;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Status;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaCore;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.CharOperation;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.INameEnvironment;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.ModuleBinding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.util.Util;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.NameLookup;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.SearchableEnvironment;

/**
 * Internal implementation of package bindings.
 */
@SuppressWarnings("rawtypes")
class PackageBinding implements IPackageBinding {

    private static final String[] NO_NAME_COMPONENTS = CharOperation.NO_STRINGS;
    private static final String UNNAMED = Util.EMPTY_STRING;
    private static final char PACKAGE_NAME_SEPARATOR = '.';

    private final com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.PackageBinding binding;
    private String name;
    private final BindingResolver resolver;
    private String[] components;

    PackageBinding(
        com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.PackageBinding binding,
        BindingResolver resolver) {
        this.binding = binding;
        this.resolver = resolver;
    }

    @Override
    public IModuleBinding getModule() {
        ModuleBinding moduleBinding = this.binding.enclosingModule;
        return moduleBinding != null ? this.resolver.getModuleBinding(moduleBinding) : null;
    }

    @Override
    public String getName() {
        if (this.name == null) {
            computeNameAndComponents();
        }
        return this.name;
    }

    @Override
    public boolean isUnnamed() {
        return getName().equals(UNNAMED);
    }

    @Override
    public String[] getNameComponents() {
        if (this.components == null) {
            computeNameAndComponents();
        }
        return this.components;
    }

    @Override
    public int getKind() {
        return IBinding.PACKAGE;
    }

    @Override
    public int getModifiers() {
        return Modifier.NONE;
    }

    @Override
    public boolean isDeprecated() {
        return false;
    }

    /**
     * @see IBinding#isRecovered()
     */
    @Override
    public boolean isRecovered() {
        return false;
    }

    /**
     * @see IBinding#isSynthetic()
     */
    @Override
    public boolean isSynthetic() {
        return false;
    }

    @Override
    public IJavaElement getJavaElement() {
        INameEnvironment nameEnvironment = this.binding.environment.nameEnvironment; // a package binding always has a
                                                                                     // LooupEnvironment set
        if (!(nameEnvironment instanceof SearchableEnvironment))
            return null;
        // this is not true in standalone DOM/AST
        NameLookup nameLookup = ((SearchableEnvironment) nameEnvironment).nameLookup;
        if (nameLookup == null)
            return null;
        IJavaElement[] pkgs = nameLookup.findPackageFragments(getName(), false/* exact match */);
        if (pkgs == null)
            return null;
        if (pkgs.length == 0) {
            // add additional tracing as this should not happen
            org.eclipse.jdt.internal.core.util.Util.log(new Status(IStatus.WARNING, JavaCore.PLUGIN_ID,
                "Searching for package " + getName() + " returns an empty array")); //$NON-NLS-1$ //$NON-NLS-2$
            return null;
        }
        return pkgs[0];
    }

    @Override
    public String getKey() {
        return new String(this.binding.computeUniqueKey());
    }

    @Override
    public boolean isEqualTo(IBinding other) {
        if (other == this) {
            // identical binding - equal (key or no key)
            return true;
        }
        if (other == null) {
            // other binding missing
            return false;
        }
        if (!(other instanceof PackageBinding)) {
            return false;
        }
        com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.PackageBinding packageBinding2
            = ((PackageBinding) other).binding;
        return CharOperation.equals(this.binding.compoundName, packageBinding2.compoundName);
    }

    private void computeNameAndComponents() {
        char[][] compoundName = this.binding.compoundName;
        if (compoundName == CharOperation.NO_CHAR_CHAR || compoundName == null) {
            this.name = UNNAMED;
            this.components = NO_NAME_COMPONENTS;
        } else {
            int length = compoundName.length;
            this.components = new String[length];
            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < length - 1; i++) {
                this.components[i] = new String(compoundName[i]);
                buffer.append(compoundName[i]).append(PACKAGE_NAME_SEPARATOR);
            }
            this.components[length - 1] = new String(compoundName[length - 1]);
            buffer.append(compoundName[length - 1]);
            this.name = buffer.toString();
        }
    }

    com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.PackageBinding
        getCompilerBinding() {
        return this.binding;
    }

    /*
     * For debugging purpose only.
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.binding.toString();
    }
}
