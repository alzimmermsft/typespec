/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.BufferManager;

/**
 * The owner of an {@link ICompilationUnit} handle in working copy mode.
 * An owner is used to identify a working copy and to create its buffer.
 * <p>
 * Clients should subclass this class to instantiate a working copy owner that is specific to their need and that
 * they can pass in to various APIs (e.g. {@link IType#resolveType(String, WorkingCopyOwner)}.
 * Clients can also override the default implementation of {@link #createBuffer(ICompilationUnit)}.
 * </p><p>
 * Note: even though this class has no abstract method, which means that it provides functional default behavior,
 * it is still an abstract class, as clients are intended to own their owner implementation.
 * </p>
 *
 * @since 3.0
 */
public abstract class WorkingCopyOwner {

    /**
     * Creates a buffer for the given working copy.
     * The new buffer will be initialized with the contents of the underlying file
     * if and only if it was not already initialized by the compilation owner (a buffer is
     * uninitialized if its content is <code>null</code>).
     * <p>
     * Note: This buffer will be associated to the working copy for its entire life-cycle. Another
     * working copy on same unit but owned by a different owner would not share the same buffer
     * unless its owner decided to implement such a sharing behaviour.
     * </p>
     *
     * @param workingCopy the working copy of the buffer
     * @return IBuffer the created buffer for the given working copy
     * @see IBuffer
     */
    public IBuffer createBuffer(ICompilationUnit workingCopy) {

        return BufferManager.createBuffer(workingCopy);
    }

    /**
     * Returns the problem requestor used by a working copy of this working copy owner.
     * <p>
     * By default, no problem requestor is configured. Clients can override this
     * method to provide a requestor.
     * </p>
     *
     * @param workingCopy The problem requestor used for the given working copy.
     * @return the problem requestor to be used by working copies of this working
     * copy owner or <code>null</code> if no problem requestor is configured.
     *
     * @since 3.3
     */
    public IProblemRequestor getProblemRequestor(ICompilationUnit workingCopy) {
        return null;
    }

    /**
     * Returns the source of the compilation unit that defines the given type in
     * the given package, or <code>null</code> if the type is unknown to this
     * owner.
     * <p>This method is called before the normal lookup (i.e. before looking
     * at the project's classpath and before looking at the working copies of this
     * owner.)</p>
     * <p>This allows to provide types that are not normally available, or to hide
     * types that would normally be available by returning an empty source for
     * the given type and package.</p>
     * <p>Example of use:
     * 
     * <pre>
     * {@code
     * WorkingCopyOwner owner = new WorkingCopyOwner() {
     *   public String findSource(String typeName, String packageName) {
     *     if ("to.be".equals(packageName) && "Generated".equals(typeName)) {
     *       return
     *         "package to.be;\n" +
     *         "public class Generated {\n" +
     *         "}";
     *     }
     *     return super.findSource(typeName, packageName);
     *   }
     *   public boolean isPackage(String[] pkg) {
     *     switch (pkg.length) {
     *     case 1:
     *       return "to".equals(pkg[0]);
     *     case 2:
     *       return "to".equals(pkg[0]) && "be".equals(pkg[1]);
     *     }
     *     return false;
     *   }
     * };
     * // Working copy on X.java with the following contents:
     * //    public class X extends to.be.Generated {
     * //    }
     * ICompilationUnit workingCopy = ...
     * ASTParser parser = ASTParser.newParser(AST.JLS3);
     * parser.setSource(workingCopy);
     * parser.setResolveBindings(true);
     * parser.setWorkingCopyOwner(owner);
     * CompilationUnit cu = (CompilationUnit) parser.createAST(null);
     * assert cu.getProblems().length == 0;
     * }
     * </pre>
     *
     * @param typeName the simple name of the type to lookup
     * @param packageName the dot-separated name of the package of type
     * @return the source of the compilation unit that defines the given type in
     * the given package, or <code>null</code> if the type is unknown
     * @see #isPackage(String[])
     * @since 3.5
     */
    public String findSource(String typeName, String packageName) {
        return null;
    }

    /**
     * Returns whether the given package segments represent a package.
     * <p>This method is called before the normal lookup (i.e. before looking
     * at the project's classpath and before looking at the working copies of this
     * owner.)</p>
     * <p>This allows to provide packages that are not normally available.</p>
     * <p>If <code>false</code> is returned, then normal lookup is used on
     * this package.</p>
     *
     * @param pkg the segments of a package to lookup
     * @return whether the given package segments represent a package.
     * @see #findSource(String, String)
     * @since 3.5
     */
    public boolean isPackage(String[] pkg) {
        return false;
    }

}
