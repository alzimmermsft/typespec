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
 *     IBM Corporation - added getOption(String, boolean), getOptions(boolean) and setOptions(Map)
 *     IBM Corporation - deprecated getPackageFragmentRoots(IClasspathEntry) and
 *                               added findPackageFragmentRoots(IClasspathEntry)
 *     IBM Corporation - added isOnClasspath(IResource)
 *     IBM Corporation - added setOption(String, String)
 *     IBM Corporation - added forceClasspathReload(IProgressMonitor)
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;

import java.util.Map;

/**
 * A Java project represents a view of a project resource in terms of Java
 * elements such as package fragments, types, methods and fields.
 * A project may contain several package roots, which contain package fragments.
 * A package root corresponds to an underlying folder or JAR.
 * <p>
 * Each Java project has a classpath, defining which folders contain source code and
 * where required libraries are located. Each Java project also has an output location,
 * defining where the builder writes <code>.class</code> files. A project that
 * references packages in another project can access the packages by including
 * the required project in a classpath entry. The Java model will present the
 * source elements in the required project; when building, the compiler will use
 * the corresponding generated class files from the required project's output
 * location(s)). The classpath format is a sequence of classpath entries
 * describing the location and contents of package fragment roots.
 * <p>
 * Java project elements need to be opened before they can be navigated or manipulated.
 * The children of a Java project are the package fragment roots that are
 * defined by the classpath and contained in this project (in other words, it
 * does not include package fragment roots for other projects). The children
 * (i.e. the package fragment roots) appear in the order they are defined by
 * the classpath.
 * <p>
 * An instance of one of these handles can be created via
 * <code>JavaCore.create(project)</code>.
 * </p>
 *
 * @see JavaCore#create(IProject)
 * @see IClasspathEntry
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IJavaProject extends IParent, IJavaElement, IOpenable {

    /**
     * Path of the file containing the project's classpath relative to the project's root.
     *
     * <p>The file is a child of the project folder.</p>
     * <p>The format of this file is unspecified and it is not meant to be modified.
     * Its contents is modified by using the <code>IJavaProject#setRawClasspath(..)</code> methods.</p>
     *
     * @since 3.7
     */
    String CLASSPATH_FILE_NAME = ".classpath"; //$NON-NLS-1$

    /**
     * Returns the <code>IJavaElement</code> corresponding to the given
     * classpath-relative path, or <code>null</code> if no such
     * <code>IJavaElement</code> is found. The result is one of an
     * <code>ICompilationUnit</code>, <code>IClassFile</code>, or
     * <code>IPackageFragment</code>.
     * <p>
     * When looking for a package fragment, there might be several potential
     * matches; only one of them is returned.
     *
     * <p>For example, the path "java/lang/Object.java", would result in the
     * <code>ICompilationUnit</code> or <code>IClassFile</code> corresponding to
     * "java.lang.Object". The path "java/lang" would result in the
     * <code>IPackageFragment</code> for "java.lang".
     * 
     * @param path the given classpath-relative path
     * @exception JavaModelException if the given path is <code>null</code>
     * or absolute
     * @return the <code>IJavaElement</code> corresponding to the given
     * classpath-relative path, or <code>null</code> if no such
     * <code>IJavaElement</code> is found
     */
    IJavaElement findElement(IPath path) throws JavaModelException;

    /**
     * Returns the <code>IJavaElement</code> corresponding to the given
     * classpath-relative path, or <code>null</code> if no such
     * <code>IJavaElement</code> is found. The result is one of an
     * <code>ICompilationUnit</code>, <code>IClassFile</code>, or
     * <code>IPackageFragment</code>. If it is an <code>ICompilationUnit</code>,
     * its owner is the given owner.
     * <p>
     * When looking for a package fragment, there might be several potential
     * matches; only one of them is returned.
     *
     * <p>For example, the path "java/lang/Object.java", would result in the
     * <code>ICompilationUnit</code> or <code>IClassFile</code> corresponding to
     * "java.lang.Object". The path "java/lang" would result in the
     * <code>IPackageFragment</code> for "java.lang".
     * 
     * @param path the given classpath-relative path
     * @param owner the owner of the returned compilation unit, ignored if it is
     * not a compilation unit.
     * @exception JavaModelException if the given path is <code>null</code>
     * or absolute
     * @return the <code>IJavaElement</code> corresponding to the given
     * classpath-relative path, or <code>null</code> if no such
     * <code>IJavaElement</code> is found
     * @since 3.0
     */
    IJavaElement findElement(IPath path, WorkingCopyOwner owner) throws JavaModelException;

    /**
     * Returns the existing package fragment root on this project's classpath
     * whose path matches the given (absolute) path, or <code>null</code> if
     * one does not exist.
     * The path can be:
     * - internal to the workbench: "/Compiler/src"
     * - external to the workbench: "c:/jdk/classes.zip"
     * 
     * @param path the given absolute path
     * @exception JavaModelException if this project does not exist or if an
     * exception occurs while accessing its corresponding resource
     * @return the existing package fragment root on this project's classpath
     * whose path matches the given (absolute) path, or <code>null</code> if
     * one does not exist
     */
    IPackageFragmentRoot findPackageFragmentRoot(IPath path) throws JavaModelException;

    /**
     * Returns the existing package fragment roots identified by the given entry.
     * A classpath entry within the current project identifies a single root.
     * <p>
     * If the classpath entry denotes a variable, it will be resolved and return
     * the roots of the target entry (empty if not resolvable).
     * <p>
     * If the classpath entry denotes a container, it will be resolved and return
     * the roots corresponding to the set of container entries (empty if not resolvable).
     * <p>
     * The result does not include package fragment roots in other projects
     * referenced on this project's classpath.
     *
     * @param entry the given entry
     * @return the existing package fragment roots identified by the given entry
     * @see IClasspathContainer
     * @since 2.1
     */
    IPackageFragmentRoot[] findPackageFragmentRoots(IClasspathEntry entry);

    /**
     * Returns the first type (excluding secondary types) found following this project's
     * classpath with the given fully qualified name or <code>null</code> if none is found.
     * The fully qualified name is a dot-separated name. For example,
     * a class B defined as a member type of a class A in package x.y should have a
     * the fully qualified name "x.y.A.B".
     *
     * Note that in order to be found, a type name (or its top level enclosing
     * type name) must match its corresponding compilation unit name. As a
     * consequence, secondary types cannot be found using this functionality.
     * To find secondary types use {@link #findType(String, IProgressMonitor)} instead.
     *
     * @param fullyQualifiedName the given fully qualified name
     * @exception JavaModelException if this project does not exist or if an
     * exception occurs while accessing its corresponding resource
     * @return the first type found following this project's classpath
     * with the given fully qualified name or <code>null</code> if none is found
     * @see IType#getFullyQualifiedName(char)
     * @since 2.0
     */
    IType findType(String fullyQualifiedName) throws JavaModelException;

    /**
     * Returns the first type (excluding secondary types) found following this project's
     * classpath with the given fully qualified name or <code>null</code> if none is found.
     * The fully qualified name is a dot-separated name. For example,
     * a class B defined as a member type of a class A in package x.y should have a
     * the fully qualified name "x.y.A.B".
     * If the returned type is part of a compilation unit, its owner is the given
     * owner.
     *
     * Note that in order to be found, a type name (or its top level enclosing
     * type name) must match its corresponding compilation unit name. As a
     * consequence, secondary types cannot be found using this functionality.
     * To find secondary types use {@link #findType(String, WorkingCopyOwner, IProgressMonitor)}
     * instead.
     *
     * @param fullyQualifiedName the given fully qualified name
     * @param owner the owner of the returned type's compilation unit
     * @exception JavaModelException if this project does not exist or if an
     * exception occurs while accessing its corresponding resource
     * @return the first type found following this project's classpath
     * with the given fully qualified name or <code>null</code> if none is found
     * @see IType#getFullyQualifiedName(char)
     * @since 3.0
     */
    IType findType(String fullyQualifiedName, WorkingCopyOwner owner) throws JavaModelException;

    /**
     * Finds the first module with the given name found following this project's module path.
     * If the returned module descriptor is part of a compilation unit, its owner is the given owner.
     * 
     * @param moduleName the given module name
     * @param owner the owner of the returned module descriptor's compilation unit
     *
     * @exception JavaModelException if this project does not exist or if an
     * exception occurs while accessing its corresponding resource
     * @return the first module found following this project's module path
     * with the given name or <code>null</code> if none is found
     * @since 3.14
     */
    IModuleDescription findModule(String moduleName, WorkingCopyOwner owner) throws JavaModelException;

    /**
     * Returns all of the existing package fragment roots that exist
     * on the classpath, in the order they are defined by the classpath.
     *
     * @return all of the existing package fragment roots that exist
     * on the classpath
     * @exception JavaModelException if this element does not exist or if an
     * exception occurs while accessing its corresponding resource
     */
    IPackageFragmentRoot[] getAllPackageFragmentRoots() throws JavaModelException;

    /**
     * Returns the table of the current custom options for this project. Projects remember their custom options,
     * in other words, only the options different from the the JavaCore global options for the workspace.
     * A boolean argument allows to directly merge the project options with global ones from <code>JavaCore</code>.
     * <p>
     * For a complete description of the configurable options, see <code>JavaCore#getDefaultOptions</code>.
     * </p>
     *
     * @param inheritJavaCoreOptions - boolean indicating whether JavaCore options should be inherited as well
     * @return table of current settings of all options
     * (key type: <code>String</code>; value type: <code>String</code>)
     * @see JavaCore#getDefaultOptions()
     * @since 2.1
     */
    Map<String, String> getOptions(boolean inheritJavaCoreOptions);

    /**
     * Returns the default output location for this project as a workspace-
     * relative absolute path.
     * <p>
     * The default output location is where class files are ordinarily generated
     * (and resource files, copied). Each source classpath entry can also
     * specify an output location for the generated class files (and copied
     * resource files) corresponding to compilation units under that source
     * folder. This makes it possible to arrange generated class files for
     * different source folders in different output folders, and not
     * necessarily the default output folder. This means that the generated
     * class files for the project may end up scattered across several folders,
     * rather than all in the default output folder (which is more standard).
     * </p>
     *
     * @return the workspace-relative absolute path of the default output folder
     * @exception JavaModelException if this element does not exist
     * @see #setOutputLocation(IPath, IProgressMonitor)
     * @see IClasspathEntry#getOutputLocation()
     */
    IPath getOutputLocation() throws JavaModelException;

    /**
     * Returns a package fragment root for an external library
     * (a ZIP archive - e.g. a <code>.jar</code>, a <code>.zip</code> file, etc. -
     * or - since 3.4 - a class folder) at the specified file system path.
     * This is a handle-only method. The underlying <code>java.io.File</code>
     * may or may not exist. No resource is associated with this local library
     * package fragment root.
     *
     * @param externalLibraryPath the library's file system path
     * @return a package fragment root for the external library at the specified file system path
     */
    IPackageFragmentRoot getPackageFragmentRoot(String externalLibraryPath);

    /**
     * Returns a package fragment root for the given resource, which
     * must either be a folder representing the top of a package hierarchy,
     * or a ZIP archive (e.g. a <code>.jar</code>, a <code>.zip</code> file, etc.)
     * This is a handle-only method. The underlying resource may or may not exist.
     *
     * @param resource the given resource
     * @return a package fragment root for the given resource, which
     * must either be a folder representing the top of a package hierarchy,
     * or a ZIP archive (e.g. a <code>.jar</code>, a <code>.zip</code> file, etc.)
     */
    IPackageFragmentRoot getPackageFragmentRoot(IResource resource);

    /**
     * Returns all of the package fragment roots contained in this
     * project, identified on this project's resolved classpath. The result
     * does not include package fragment roots in other projects referenced
     * on this project's classpath. The package fragment roots appear in the
     * order they are defined by the classpath.
     *
     * <p>NOTE: This is equivalent to <code>getChildren()</code>.
     *
     * @return all of the package fragment roots contained in this
     * project, identified on this project's resolved classpath
     * @exception JavaModelException if this element does not exist or if an
     * exception occurs while accessing its corresponding resource
     */
    IPackageFragmentRoot[] getPackageFragmentRoots() throws JavaModelException;

    /**
     * Returns the <code>IProject</code> on which this <code>IJavaProject</code>
     * was created. This is handle-only method.
     *
     * @return the <code>IProject</code> on which this <code>IJavaProject</code>
     * was created
     */
    IProject getProject();

    /**
     * Returns the {@link IModuleDescription} this project represents or
     * null if the Java project doesn't represent any named module. A Java
     * project is said to represent a module if any of its source package
     * fragment roots (see {@link IPackageFragmentRoot#K_SOURCE}) contains a
     * valid Java module descriptor, or if one of its classpath entries
     * has a valid {@link IClasspathAttribute#PATCH_MODULE} attribute
     * affecting the current project.
     * In the latter case the corresponding module description of the
     * location referenced by that classpath entry is returned.
     *
     * @return the {@link IModuleDescription} this project represents.
     * @exception JavaModelException if this element does not exist or if an
     * exception occurs while accessing its corresponding resource
     * @since 3.14
     */
    IModuleDescription getModuleDescription() throws JavaModelException;

    /**
     * Returns the raw classpath for the project, as a list of classpath
     * entries. This corresponds to the exact set of entries which were assigned
     * using <code>setRawClasspath</code>, in particular such a classpath may
     * contain classpath variable and classpath container entries. Classpath
     * variable and classpath container entries can be resolved using the
     * helper method <code>getResolvedClasspath</code>; classpath variable
     * entries also can be resolved individually using
     * <code>JavaCore#getClasspathVariable</code>).
     * <p>
     * Both classpath containers and classpath variables provides a level of
     * indirection that can make the <code>.classpath</code> file stable across
     * workspaces.
     * As an example, classpath variables allow a classpath to no longer refer
     * directly to external JARs located in some user specific location.
     * The classpath can simply refer to some variables defining the proper
     * locations of these external JARs. Similarly, classpath containers
     * allows classpath entries to be computed dynamically by the plug-in that
     * defines that kind of classpath container.
     * </p>
     * <p>
     * Note that in case the project isn't yet opened, the classpath will
     * be read directly from the associated <code>.classpath</code> file.
     * </p>
     *
     * @return the raw classpath for the project, as a list of classpath entries
     * @exception JavaModelException if this element does not exist or if an
     * exception occurs while accessing its corresponding resource
     * @see IClasspathEntry
     */
    IClasspathEntry[] getRawClasspath() throws JavaModelException;

    /**
     * Returns the names of the projects that are directly required by this
     * project. A project is required if it is in its classpath.
     * <p>
     * The project names are returned in the order they appear on the classpath.
     *
     * @return the names of the projects that are directly required by this
     * project in classpath order
     * @exception JavaModelException if this element does not exist or if an
     * exception occurs while accessing its corresponding resource
     */
    String[] getRequiredProjectNames() throws JavaModelException;

    /**
     * This is a helper method returning the resolved classpath for the project
     * as a list of simple (non-variable, non-container) classpath entries.
     * All classpath variable and classpath container entries in the project's
     * raw classpath will be replaced by the simple classpath entries they
     * resolve to.
     * <p>
     * The resulting resolved classpath is accurate for the given point in time.
     * If the project's raw classpath is later modified, or if classpath
     * variables are changed, the resolved classpath can become out of date.
     * Because of this, hanging on resolved classpath is not recommended.
     * </p>
     * <p>
     * Note that if the resolution creates duplicate entries
     * (i.e. {@link IClasspathEntry entries} which are {@link Object#equals(Object)}),
     * only the first one is added to the resolved classpath.
     * </p>
     *
     * @param ignoreUnresolvedEntry indicates how to handle unresolvable
     * variables and containers; <code>true</code> indicates that missing
     * variables and unresolvable classpath containers should be silently
     * ignored, and that the resulting list should consist only of the
     * entries that could be successfully resolved; <code>false</code> indicates
     * that a <code>JavaModelException</code> should be thrown for the first
     * unresolved variable or container
     * @return the resolved classpath for the project as a list of simple
     * classpath entries, where all classpath variable and container entries
     * have been resolved and substituted with their final target entries
     * @exception JavaModelException in one of the corresponding situation:
     * <ul>
     * <li>this element does not exist</li>
     * <li>an exception occurs while accessing its corresponding resource</li>
     * <li>a classpath variable or classpath container was not resolvable
     * and <code>ignoreUnresolvedEntry</code> is <code>false</code>.</li>
     * </ul>
     * @see IClasspathEntry
     */
    IClasspathEntry[] getResolvedClasspath(boolean ignoreUnresolvedEntry) throws JavaModelException;

    /**
     * Returns the classpath entry that refers to the given path or <code>null</code> if there is no reference to the
     * path.
     *
     * @param path
     * IPath
     * @return the classpath entry or <code>null</code>.
     * @since 3.14
     */
    IClasspathEntry getClasspathEntryFor(IPath path) throws JavaModelException;

}
