/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
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
 *     IBM Corporation - added J2SE 1.5 support
 *     Microsoft Corporation - support custom options at compilation unit level
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.dom.ASTParser;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.dom.IBinding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.text.edits.TextEdit;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.text.edits.UndoEdit;
import java.util.Collections;
import java.util.Map;

/**
 * Represents an entire Java compilation unit (source file with one of the
 * {@link JavaCore#getJavaLikeExtensions() Java-like extensions}).
 * Compilation unit elements need to be opened before they can be navigated or manipulated.
 * The children are of type {@link IPackageDeclaration},
 * {@link IImportContainer}, and {@link IType},
 * and appear in the order in which they are declared in the source.
 * If a source file cannot be parsed, its structure remains unknown.
 * Use {@link IJavaElement#isStructureKnown} to determine whether this is
 * the case.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 */
@SuppressWarnings("deprecation")
public interface ICompilationUnit extends ITypeRoot, IWorkingCopy, ISourceManipulation {
    /**
     * Constant indicating that a reconcile operation should not return an AST.
     * 
     * @since 3.0
     */
    int NO_AST = 0;

    /**
     * Constant indicating that a reconcile operation should recompute the problems
     * even if the source hasn't changed.
     * 
     * @since 3.3
     */
    int FORCE_PROBLEM_DETECTION = 0x01;

    /**
     * Constant indicating that a reconcile operation should enable the statements recovery.
     * 
     * @see ASTParser#setStatementsRecovery(boolean)
     * @since 3.3
     */
    int ENABLE_STATEMENTS_RECOVERY = 0x02;

    /**
     * Constant indicating that a reconcile operation should enable the bindings recovery
     * 
     * @see ASTParser#setBindingsRecovery(boolean)
     * @see IBinding#isRecovered()
     * @since 3.3
     */
    int ENABLE_BINDINGS_RECOVERY = 0x04;

    /**
     * Constant indicating that a reconcile operation could ignore to parse the method bodies.
     * 
     * @see ASTParser#setIgnoreMethodBodies(boolean)
     * @since 3.5.2
     */
    int IGNORE_METHOD_BODIES = 0x08;

    /**
     * Applies a text edit to the compilation unit's buffer.
     * <p>
     * Note that the edit is simply applied to the compilation unit's buffer.
     * In particular the undo edit is not grouped with previous undo edits
     * if the buffer doesn't implement {@link IBuffer.ITextEditCapability}.
     * If it does, the exact semantics for grouping undo edit depends
     * on how {@link IBuffer.ITextEditCapability#applyTextEdit(TextEdit, IProgressMonitor)}
     * is implemented.
     * </p>
     *
     * @param edit the edit to apply
     * @param monitor the progress monitor to use or <code>null</code> if no progress should be reported
     * @return the undo edit
     * @throws JavaModelException if this edit can not be applied to the compilation unit's buffer. Reasons include:
     * <ul>
     * <li>This compilation unit does not exist ({@link IJavaModelStatusConstants#ELEMENT_DOES_NOT_EXIST}).</li>
     * <li>The provided edit can not be applied as there is a problem with the text edit locations
     * ({@link IJavaModelStatusConstants#BAD_TEXT_EDIT_LOCATION}).</li>
     * </ul>
     *
     * @since 3.4
     */
    UndoEdit applyTextEdit(TextEdit edit, IProgressMonitor monitor) throws JavaModelException;

    /**
     * Finds the elements in this compilation unit that correspond to
     * the given element.
     * An element A corresponds to an element B if:
     * <ul>
     * <li>A has the same element name as B.
     * <li>If A is a method, A must have the same number of arguments as
     * B and the simple names of the argument types must be equals.
     * <li>The parent of A corresponds to the parent of B recursively up to
     * their respective compilation units.
     * <li>A exists.
     * </ul>
     * Returns <code>null</code> for the following cases:
     * <ul>
     * <li>if no such java elements can be found or if the given element is not included in this compilation unit</li>
     * <li>the element is a lambda expression, i.e. calling {@link IType#isLambda()} returns true</li>
     * <li>the element is an {@link ILocalVariable}</li>
     * </ul>
     *
     * @param element the given element
     * @return the found elements in this compilation unit that correspond to the given element
     * @since 3.0
     */
    @Override
    IJavaElement[] findElements(IJavaElement element);

    /**
     * Finds the working copy for this compilation unit, given a {@link WorkingCopyOwner}.
     * If no working copy has been created for this compilation unit associated with this
     * working copy owner, returns <code>null</code>.
     * <p>
     * Users of this method must not destroy the resulting working copy.
     *
     * @param owner the given {@link WorkingCopyOwner}
     * @return the found working copy for this compilation unit, <code>null</code> if none
     * @see WorkingCopyOwner
     * @since 3.0
     */
    ICompilationUnit findWorkingCopy(WorkingCopyOwner owner);

    /**
     * Returns all types declared in this compilation unit in the order
     * in which they appear in the source.
     * This includes all top-level types and nested member types.
     * It does NOT include local types (types defined in methods).
     *
     * @return the array of top-level and member types defined in a compilation unit, in declaration order.
     * @throws JavaModelException if this element does not exist or if an
     * exception occurs while accessing its corresponding resource
     */
    IType[] getAllTypes() throws JavaModelException;

    /**
     * Returns the first import declaration in this compilation unit with the given name.
     * This is a handle-only method. The import declaration may or may not exist. This
     * is a convenience method - imports can also be accessed from a compilation unit's
     * import container.
     *
     * @param name the name of the import to find as defined by JLS2 7.5. (For example: <code>"java.io.File"</code>
     * or <code>"java.awt.*"</code>)
     * @return a handle onto the corresponding import declaration. The import declaration may or may not exist.
     */
    IImportDeclaration getImport(String name);

    /**
     * Returns the import container for this compilation unit.
     * This is a handle-only method. The import container may or
     * may not exist. The import container can used to access the
     * imports.
     * 
     * @return a handle onto the corresponding import container. The
     * import contain may or may not exist.
     */
    IImportContainer getImportContainer();

    /**
     * Returns the import declarations in this compilation unit
     * in the order in which they appear in the source. This is
     * a convenience method - import declarations can also be
     * accessed from a compilation unit's import container.
     *
     * @return the import declarations in this compilation unit
     * @throws JavaModelException if this element does not exist or if an
     * exception occurs while accessing its corresponding resource
     */
    IImportDeclaration[] getImports() throws JavaModelException;

    /**
     * Returns the primary compilation unit (whose owner is the primary owner)
     * this working copy was created from, or this compilation unit if this a primary
     * compilation unit.
     * <p>
     * Note that the returned primary compilation unit can be in working copy mode.
     * </p>
     *
     * @return the primary compilation unit this working copy was created from,
     * or this compilation unit if it is primary
     * @since 3.0
     */
    ICompilationUnit getPrimary();

    /**
     * Returns <code>null</code> if this <code>ICompilationUnit</code> is the primary
     * working copy, or this <code>ICompilationUnit</code> is not a working copy,
     * otherwise the <code>WorkingCopyOwner</code>
     *
     * @return <code>null</code> if this <code>ICompilationUnit</code> is the primary
     * working copy, or this <code>ICompilationUnit</code> is not a working copy,
     * otherwise the <code>WorkingCopyOwner</code>
     * @since 3.0
     */
    WorkingCopyOwner getOwner();

    /**
     * Returns the first package declaration in this compilation unit with the given package name
     * (there normally is at most one package declaration).
     * This is a handle-only method. The package declaration may or may not exist.
     *
     * @param name the name of the package declaration as defined by JLS2 7.4. (For example, <code>"java.lang"</code>)
     * @return the first package declaration in this compilation unit with the given package name
     */
    IPackageDeclaration getPackageDeclaration(String name);

    /**
     * Returns the package declarations in this compilation unit
     * in the order in which they appear in the source.
     * There normally is at most one package declaration.
     *
     * @return an array of package declaration (normally of size one)
     *
     * @throws JavaModelException if this element does not exist or if an
     * exception occurs while accessing its corresponding resource
     */
    IPackageDeclaration[] getPackageDeclarations() throws JavaModelException;

    /**
     * Returns the top-level type declared in this compilation unit with the given simple type name.
     * The type name has to be a valid compilation unit name.
     * This is a handle-only method. The type may or may not exist.
     *
     * @param name the simple name of the requested type in the compilation unit
     * @return a handle onto the corresponding type. The type may or may not exist.
     * @see JavaConventions#validateCompilationUnitName(String name, String sourceLevel, String complianceLevel)
     */
    IType getType(String name);

    /**
     * Returns the top-level types declared in this compilation unit
     * in the order in which they appear in the source.
     *
     * @return the top-level types declared in this compilation unit
     * @throws JavaModelException if this element does not exist or if an
     * exception occurs while accessing its corresponding resource
     */
    IType[] getTypes() throws JavaModelException;

    /**
     * Returns whether the resource of this working copy has changed since the
     * inception of this working copy.
     * Returns <code>false</code> if this compilation unit is not in working copy mode.
     *
     * @return whether the resource has changed
     * @since 3.0
     */
    boolean hasResourceChanged();

    /**
     * Returns whether this element is a working copy.
     *
     * @return true if this element is a working copy, false otherwise
     * @since 3.0
     */
    @Override
    boolean isWorkingCopy();

    /**
     * Sets the ICompilationUnit custom options. All and only the options explicitly included in the given table
     * are remembered; all previous option settings are forgotten, including ones not explicitly
     * mentioned.
     * <p>
     * For a complete description of the configurable options, see <code>JavaCore#getDefaultOptions</code>.
     * </p>
     *
     * @param newOptions the new custom options for this compilation unit
     * @see JavaCore#setOptions(java.util.Hashtable)
     * @since 3.25
     */
    default void setOptions(Map<String, String> newOptions) {
        // Do nothing by default
    }

    /**
     * Returns the table of the current custom options for this ICompilationUnit. If there is no <code>setOptions</code>
     * called
     * for the ICompliationUnit, then return an empty table.
     *
     * @return the table of the current custom options for this ICompilationUnit
     * @since 3.25
     */
    default Map<String, String> getCustomOptions() {
        return Collections.emptyMap();
    }

    /**
     * Returns the table of the options for this ICompilationUnit, which includes its custom options and options
     * inherited from its parent JavaProject. The boolean argument <code>inheritJavaCoreOptions</code> allows
     * to directly merge the global ones from <code>JavaCore</code>.
     * <p>
     * For a complete description of the configurable options, see <code>JavaCore#getDefaultOptions</code>.
     * </p>
     *
     * @param inheritJavaCoreOptions - boolean indicating whether the JavaCore options should be inherited as well
     * @return table of current settings of all options
     * @see JavaCore#getDefaultOptions()
     * @since 3.25
     */
    default Map<String, String> getOptions(boolean inheritJavaCoreOptions) {
        IJavaProject parentProject = getJavaProject();
        Map<String, String> options
            = parentProject == null ? JavaCore.getOptions() : parentProject.getOptions(inheritJavaCoreOptions);
        Map<String, String> customOptions = getCustomOptions();
        if (customOptions != null) {
            options.putAll(customOptions);
        }

        return options;
    }
}
