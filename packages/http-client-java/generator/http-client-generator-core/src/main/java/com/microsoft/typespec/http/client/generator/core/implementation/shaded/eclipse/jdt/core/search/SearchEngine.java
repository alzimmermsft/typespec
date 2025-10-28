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
 *     Stephan Herrmann - Contributions for bug 215139 and bug 295894
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.search;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.Flags;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.ICompilationUnit;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IPackageFragment;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IPackageFragmentRoot;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IWorkingCopy;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaCore;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.WorkingCopyOwner;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.AccessRestriction;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.search.BasicSearchEngine;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.search.IRestrictedAccessTypeRequestor;

/**
 * A {@link SearchEngine} searches for Java elements following a search pattern.
 * The search can be limited to a search scope.
 * <p>For example, one can search for references to a method in the hierarchy of a type,
 * or one can search for the declarations of types starting with "Abstract" in a project.
 * </p>
 * <p>
 * This class may be instantiated.
 * </p>
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
public class SearchEngine {

    /**
     * Internal adapter class.
     * 
     * @deprecated marking deprecated as it uses deprecated ISearchPattern
     */
    static class SearchPatternAdapter implements ISearchPattern {
        SearchPattern pattern;

        SearchPatternAdapter(SearchPattern pattern) {
            this.pattern = pattern;
        }
    }

    /**
     * Internal adapter class.
     * 
     * @deprecated marking deprecated as it uses deprecated IJavaSearchResultCollector
     */
    static class ResultCollectorAdapter extends SearchRequestor {
        IJavaSearchResultCollector resultCollector;

        ResultCollectorAdapter(IJavaSearchResultCollector resultCollector) {
            this.resultCollector = resultCollector;
        }

        /**
         * @see org.eclipse.jdt.core.search.SearchRequestor#acceptSearchMatch(SearchMatch)
         */
        @Override
        public void acceptSearchMatch(SearchMatch match) throws CoreException {
            this.resultCollector.accept(match.getResource(), match.getOffset(), match.getOffset() + match.getLength(),
                (IJavaElement) match.getElement(), match.getAccuracy());
        }

        /**
         * @see org.eclipse.jdt.core.search.SearchRequestor#beginReporting()
         */
        @Override
        public void beginReporting() {
            this.resultCollector.aboutToStart();
        }

        /**
         * @see org.eclipse.jdt.core.search.SearchRequestor#endReporting()
         */
        @Override
        public void endReporting() {
            this.resultCollector.done();
        }
    }

    /**
     * Internal adapter class.
     * 
     * @deprecated marking deprecated as it uses deprecated ITypeNameRequestor
     */
    static class TypeNameRequestorAdapter implements IRestrictedAccessTypeRequestor {
        ITypeNameRequestor nameRequestor;

        TypeNameRequestorAdapter(ITypeNameRequestor requestor) {
            this.nameRequestor = requestor;
        }

        @Override
        public void acceptType(int modifiers, char[] packageName, char[] simpleTypeName, char[][] enclosingTypeNames,
            String path, AccessRestriction access) {
            if (Flags.isInterface(modifiers)) {
                this.nameRequestor.acceptInterface(packageName, simpleTypeName, enclosingTypeNames, path);
            } else {
                this.nameRequestor.acceptClass(packageName, simpleTypeName, enclosingTypeNames, path);
            }
        }
    }

    // Search engine now uses basic engine functionalities
    private final BasicSearchEngine basicEngine;

    /**
     * Creates a new search engine.
     */
    public SearchEngine() {
        this.basicEngine = new BasicSearchEngine();
    }

    /**
     * Creates a new search engine with a list of working copies that will take precedence over
     * their original compilation units in the subsequent search operations.
     * <p>
     * Note that passing an empty working copy will be as if the original compilation
     * unit had been deleted.</p>
     * <p>
     * Since 3.0 the given working copies take precedence over primary working copies (if any).
     *
     * @param workingCopies the working copies that take precedence over their original compilation units
     * @since 3.0
     */
    public SearchEngine(ICompilationUnit[] workingCopies) {
        this.basicEngine = new BasicSearchEngine(workingCopies);
    }

    /**
     * Creates a new search engine with a list of working copies that will take precedence over
     * their original compilation units in the subsequent search operations.
     * <p>
     * Note that passing an empty working copy will be as if the original compilation
     * unit had been deleted.</p>
     * <p>
     * Since 3.0 the given working copies take precedence over primary working copies (if any).
     *
     * @param workingCopies the working copies that take precedence over their original compilation units
     * @since 2.0
     * @deprecated Use {@link #SearchEngine(ICompilationUnit[])} instead.
     */
    public SearchEngine(IWorkingCopy[] workingCopies) {
        int length = workingCopies.length;
        ICompilationUnit[] units = new ICompilationUnit[length];
        System.arraycopy(workingCopies, 0, units, 0, length);
        this.basicEngine = new BasicSearchEngine(units);
    }

    /**
     * Creates a new search engine with the given working copy owner.
     * The working copies owned by this owner will take precedence over
     * the primary compilation units in the subsequent search operations.
     *
     * @param workingCopyOwner the owner of the working copies that take precedence over their original compilation
     * units
     * @since 3.0
     */
    public SearchEngine(WorkingCopyOwner workingCopyOwner) {
        this.basicEngine = new BasicSearchEngine(workingCopyOwner);
    }

    /**
     * Returns a Java search scope limited to the given Java elements.
     * The Java elements resulting from a search with this scope will
     * be children of the given elements.
     * <p>
     * If an element is an {@link IJavaProject}, then the project's source folders,
     * its jars (external and internal) and its referenced projects (with their source
     * folders and jars, recursively) will be included.</p>
     * <p>If an element is an {@link IPackageFragmentRoot}, then only the package fragments of
     * this package fragment root will be included.</p>
     * <p>If an element is an {@link IPackageFragment}, then only the compilation unit and class
     * files of this package fragment will be included. Subpackages will NOT be
     * included.</p>
     *
     * <p>In other words, this is equivalent to using SearchEngine.createJavaSearchScope(elements, true).</p>
     *
     * @param elements the Java elements the scope is limited to
     * @return a new Java search scope
     * @since 2.0
     */
    public static IJavaSearchScope createJavaSearchScope(IJavaElement[] elements) {
        return BasicSearchEngine.createJavaSearchScope(elements);
    }

    /**
     * Returns a Java search scope with the workspace as the only limit.
     *
     * @return a new workspace scope
     */
    public static IJavaSearchScope createWorkspaceScope() {
        return BasicSearchEngine.createWorkspaceScope();
    }

    /**
     * Returns a new default Java search participant.
     *
     * @return a new default Java search participant
     * @since 3.0
     */
    public static SearchParticipant getDefaultSearchParticipant() {
        return BasicSearchEngine.getDefaultSearchParticipant();
    }
}
