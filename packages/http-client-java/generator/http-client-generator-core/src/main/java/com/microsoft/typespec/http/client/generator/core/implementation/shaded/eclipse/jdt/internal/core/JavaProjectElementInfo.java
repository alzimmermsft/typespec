/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IClasspathEntry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.ICompilationUnit;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IPackageFragmentRoot;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaModelException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.DeltaProcessor.RootInfo;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.HashSetOfArray;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.HashtableOfArrayToObject;
import java.util.HashMap;
import java.util.Map;

/**
 * Info for IJavaProject.
 * <p>
 * Note: <code>getChildren()</code> returns all of the <code>IPackageFragmentRoots</code>
 * specified on the classpath for the project. This can include roots external to the
 * project. See <code>JavaProject#getAllPackageFragmentRoots()</code> and
 * <code>JavaProject#getPackageFragmentRoots()</code>. To get only the <code>IPackageFragmentRoots</code>
 * that are internal to the project, use <code>JavaProject#getChildren()</code>.
 */

/* package */
class JavaProjectElementInfo extends OpenableElementInfo {

    static final IPackageFragmentRoot[] NO_ROOTS = new IPackageFragmentRoot[0];

    static class ProjectCache {
        ProjectCache(IPackageFragmentRoot[] allPkgFragmentRootsCache,
            Map<IPackageFragmentRoot, IClasspathEntry> rootToResolvedEntries,
            Map<IPackageFragmentRoot, HashSetOfArray> pkgFragmentsCaches) {
            this.allPkgFragmentRootsCache = allPkgFragmentRootsCache;
            this.rootToResolvedEntries = rootToResolvedEntries;
            this.pkgFragmentsCaches = pkgFragmentsCaches;
        }

        /*
         * A cache of all package fragment roots of this project.
         */
        public IPackageFragmentRoot[] allPkgFragmentRootsCache;

        /*
         * A cache of all package fragments in this project.
         * (a map from String[] (the package name) to IPackageFragmentRoot[] (the package fragment roots that contain a
         * package fragment with this name))
         */
        public HashtableOfArrayToObject allPkgFragmentsCache;

        /*
         * A cache of package fragments for each package fragment root of this project
         * (a map from IPackageFragmentRoot to a set of String[] (the package name))
         */
        public Map<IPackageFragmentRoot, HashSetOfArray> pkgFragmentsCaches;

        /*
         * A cache of package fragment roots to corresponding resolved CP entry
         * (so as to be able to figure inclusion/exclusion rules)
         */
        public Map<IPackageFragmentRoot, IClasspathEntry> rootToResolvedEntries;
    }

    ProjectCache projectCache;
    ProjectCache mainProjectCache;

    /*
     * Adds the given name and its super names to the given set
     * (e.g. for {"a", "b", "c"}, adds {"a", "b", "c"}, {"a", "b"}, and {"a"})
     */
    static void addSuperPackageNames(String[] pkgName, HashtableOfArrayToObject packageFragments) {
        for (int i = pkgName.length - 1; i > 0; i--) {
            if (packageFragments.getKey(pkgName, i) == null) {
                System.arraycopy(pkgName, 0, pkgName = new String[i], 0, i);
                packageFragments.put(pkgName, NO_ROOTS);
            }
        }
    }

    /**
     * Create and initialize a new instance of the receiver
     */
    public JavaProjectElementInfo() {
        this.nonJavaResources = null;
    }

    ProjectCache getProjectCache(JavaProject project, boolean excludeTestCode) {
        ProjectCache cache = excludeTestCode ? this.mainProjectCache : this.projectCache;
        if (cache != null) {
            for (IPackageFragmentRoot root : cache.allPkgFragmentRootsCache) {
                IJavaProject rootProject = root.getJavaProject();
                if (rootProject != project && !rootProject.exists()) {
                    cache = null; // force rebuilding
                    break;
                }
            }
        }
        if (cache == null) {
            IPackageFragmentRoot[] roots;
            Map<IPackageFragmentRoot, IClasspathEntry> reverseMap = new HashMap<>(3);
            try {
                roots = project.getAllPackageFragmentRoots(reverseMap, excludeTestCode);
            } catch (JavaModelException e) {
                // project does not exist: cannot happen since this is the info of the project
                roots = new IPackageFragmentRoot[0];
                reverseMap.clear();
            }

            Map<IPath, RootInfo> rootInfos = JavaModelManager.getJavaModelManager().deltaState.roots;
            HashMap<IPackageFragmentRoot, HashSetOfArray> pkgFragmentsCaches = new HashMap<>();
            int length = roots.length;
            JavaModelManager manager = JavaModelManager.getJavaModelManager();
            for (int i = 0; i < length; i++) {
                IPackageFragmentRoot root = roots[i];
                RootInfo rootInfo = rootInfos.get(root.getPath());
                if (rootInfo == null || rootInfo.project.equals(project)) {
                    // ensure that an identical root is used (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=217059 )
                    roots[i] = root = (IPackageFragmentRoot) manager.getExistingElement(root);
                    // compute fragment cache
                    HashSetOfArray fragmentsCache = new HashSetOfArray();
                    initializePackageNames(root, fragmentsCache);
                    pkgFragmentsCaches.put(root, fragmentsCache);
                }
            }

            cache = new ProjectCache(roots, reverseMap, pkgFragmentsCaches);
            if (excludeTestCode) {
                this.mainProjectCache = cache;
            } else {
                this.projectCache = cache;
            }
        }
        return cache;
    }

    private void initializePackageNames(IPackageFragmentRoot root, HashSetOfArray fragmentsCache) {
        IJavaElement[] frags;
        try {
            if (!root.isOpen()) {
                PackageFragmentRootInfo info
                    = root.isArchive() ? new JarPackageFragmentRootInfo() : new PackageFragmentRootInfo();
                ((PackageFragmentRoot) root).computeChildren(info, ((JavaElement) root).resource());
                frags = info.children;
            } else
                frags = root.getChildren();
        } catch (JavaModelException e) {
            // root doesn't exist: ignore
            return;
        }
        for (IJavaElement frag : frags) {
            if (frag instanceof PackageFragment)
                fragmentsCache.add(((PackageFragment) frag).names);
        }
    }

    /*
     * Creates a new name lookup for this project info.
     * The given project is assumed to be the handle of this info.
     * This name lookup first looks in the given working copies.
     */
    NameLookup newNameLookup(JavaProject project, ICompilationUnit[] workingCopies, boolean excludeTestCode) {
        ProjectCache cache = getProjectCache(project, excludeTestCode);
        HashtableOfArrayToObject allPkgFragmentsCache = cache.allPkgFragmentsCache;
        if (allPkgFragmentsCache == null) {
            Map<IPath, RootInfo> rootInfos = JavaModelManager.getJavaModelManager().deltaState.roots;
            IPackageFragmentRoot[] allRoots = cache.allPkgFragmentRootsCache;
            int length = allRoots.length;
            allPkgFragmentsCache = new HashtableOfArrayToObject();
            for (IPackageFragmentRoot root : allRoots) {
                RootInfo rootInfo = rootInfos.get(root.getPath());
                JavaProject rootProject = rootInfo == null ? project : rootInfo.project;
                HashSetOfArray fragmentsCache;
                if (rootProject.equals(project)) {
                    // retrieve package fragments cache from this project
                    fragmentsCache = cache.pkgFragmentsCaches.get(root);
                } else {
                    // retrieve package fragments cache from the root's project
                    ProjectCache rootProjectCache;
                    try {
                        rootProjectCache = rootProject.getProjectCache(excludeTestCode);
                    } catch (JavaModelException e) {
                        // project doesn't exit
                        continue;
                    }
                    fragmentsCache = rootProjectCache.pkgFragmentsCaches.get(root);
                }
                if (fragmentsCache == null) { // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=183833
                    fragmentsCache = new HashSetOfArray();
                    initializePackageNames(root, fragmentsCache);
                }
                Object[][] set = fragmentsCache.set;
                for (Object[] element : set) {
                    String[] pkgName = (String[]) element;
                    if (pkgName == null)
                        continue;
                    Object existing = allPkgFragmentsCache.get(pkgName);
                    if (existing == null || existing == NO_ROOTS) {
                        allPkgFragmentsCache.put(pkgName, root);
                        // ensure super packages (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=119161)
                        // are also in the map
                        addSuperPackageNames(pkgName, allPkgFragmentsCache);
                    } else {
                        if (existing instanceof PackageFragmentRoot) {
                            allPkgFragmentsCache.put(pkgName,
                                new IPackageFragmentRoot[] { (PackageFragmentRoot) existing, root });
                        } else {
                            IPackageFragmentRoot[] roots = (IPackageFragmentRoot[]) existing;
                            int rootLength = roots.length;
                            System.arraycopy(roots, 0, roots = new IPackageFragmentRoot[rootLength + 1], 0, rootLength);
                            roots[rootLength] = root;
                            allPkgFragmentsCache.put(pkgName, roots);
                        }
                    }
                }
            }
            cache.allPkgFragmentsCache = allPkgFragmentsCache;
        }
        return new NameLookup(project, cache.allPkgFragmentRootsCache, cache.allPkgFragmentsCache, workingCopies,
            cache.rootToResolvedEntries);
    }

    /*
     * Reset the package fragment roots and package fragment caches
     */
    void resetCaches() {
        this.projectCache = null;
        this.mainProjectCache = null;
    }
}
