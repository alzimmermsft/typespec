/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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
 *     Theodora Yeung (tyeung@bea.com) - ensure that JarPackageFragmentRoot make it into cache
 *                                                           before its contents
 *                                                           (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=102422)
 *     Stephan Herrmann - Contributions for
 *								Bug 346010 - [model] strange initialization dependency in OptionTests
 *								Bug 440477 - [null] Infrastructure for feeding external annotations into compilation
 *     Terry Parker <tparker@google.com> - DeltaProcessor misses state changes in archive files, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=357425
 *     Thirumala Reddy Mutchukota <thirumala@google.com> - Contribution to bug: https://bugs.eclipse.org/bugs/show_bug.cgi?id=411423
 *     Terry Parker <tparker@google.com> - [performance] Low hit rates in JavaModel caches - https://bugs.eclipse.org/421165
 *     Terry Parker <tparker@google.com> - Enable the Java model caches to recover from IO errors - https://bugs.eclipse.org/455042
 *     Gábor Kövesdán - Contribution for Bug 350000 - [content assist] Include non-prefix matches in auto-complete suggestions
 *     Karsten Thoms - Bug 532505 - Reduce memory footprint of ClasspathAccessRule
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IFile;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IFolder;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IWorkspaceRoot;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.ResourcesPlugin;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IConfigurationElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IExtension;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IExtensionPoint;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Path;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Platform;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Status;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.preferences.IEclipsePreferences;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.ClasspathContainerInitializer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IAccessRule;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IClassFile;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IClasspathContainer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IClasspathEntry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.ICompilationUnit;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaModelStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaModelStatusConstants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IPackageFragment;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IPackageFragmentRoot;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IParent;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IProblemRequestor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IType;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaConventions;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaCore;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaCore.JavaCallable;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaModelException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.WorkingCopyOwner;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.CharOperation;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.IProblem;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.IElementInfo;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.util.ObjectVector;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.JavaProjectElementInfo.ProjectCache;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.DeduplicationUtil;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.HashtableOfArrayToObject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.LRUCache;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.Messages;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.NoSuchFileException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * The <code>JavaModelManager</code> manages instances of <code>IJavaModel</code>.
 * <code>IElementChangedListener</code>s register with the <code>JavaModelManager</code>,
 * and receive <code>ElementChangedEvent</code>s for all <code>IJavaModel</code>s.
 * <p>
 * The single instance of <code>JavaModelManager</code> is available from
 * the static method <code>JavaModelManager.getJavaModelManager()</code>.
 */
public class JavaModelManager {

    public enum ArchiveValidity {
        INVALID, VALID;

        public boolean isValid() {
            return this == VALID;
        }
    }

    /**
     * Define a zip cache object.
     */
    static class ZipCache {
        private final Map<Object, ZipFile> map;
        Object owner;

        ZipCache(Object owner) {
            this.map = new HashMap<>();
            this.owner = owner;
        }

        public void flush() {
            Thread currentThread = Thread.currentThread();
            for (ZipFile zf : this.map.values()) {
                String zipFileName = null;
                try (ZipFile zipFile = zf) {
                    zipFileName = zipFile.getName();
                    if (JavaModelManager.ZIP_ACCESS_VERBOSE) {
                        trace("(" + currentThread + ") [ZipCache[" + this.owner //$NON-NLS-1$//$NON-NLS-2$
                            + "].flush()] Closing ZipFile on " + zipFile.getName()); //$NON-NLS-1$
                    }
                } catch (IOException e) {
                    // problem occured closing zip file: cannot do much more
                    JavaCore.getPlugin()
                        .getLog()
                        .log(new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, "Error closing " + zipFileName, e)); //$NON-NLS-1$
                }
            }
        }

        public ZipFile getCache(IPath path) {
            return this.map.get(path);
        }

        public void setCache(IPath path, ZipFile zipFile) {
            try (ZipFile old = this.map.put(path, zipFile)) {
                if (old != null) {
                    if (JavaModelManager.ZIP_ACCESS_VERBOSE) {
                        Thread currentThread = Thread.currentThread();
                        trace("(" + currentThread + ") [ZipCache[" + this.owner //$NON-NLS-1$//$NON-NLS-2$
                            + "].setCache()] leaked ZipFile on " + old.getName() + " for path: " + path); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
            } catch (IOException e) {
                if (VERBOSE) {
                    trace("", e); //$NON-NLS-1$
                }
            }
        }
    }

    /**
     * Unique handle onto the JavaModel
     */
    final JavaModel javaModel = new JavaModel();

    /**
     * Classpath variables pool
     */
    public HashMap<String, IPath> variables = new HashMap<>(5);
    public HashSet<String> variablesWithInitializer = new HashSet<>(5);
    public HashMap<String, String> deprecatedVariables = new HashMap<>(5);
    public HashSet<String> readOnlyVariables = new HashSet<>(5);
    public HashMap<String, IPath> previousSessionVariables = new HashMap<>(5);
    private final ThreadLocal<Set<String>> variableInitializationInProgress = new ThreadLocal<>();

    /**
     * Classpath containers pool
     */
    public HashMap<IJavaProject, Map<IPath, IClasspathContainer>> containers = new HashMap<>(5);

    public static final int NO_BATCH_INITIALIZATION = 0;

    public Hashtable<String, ClasspathContainerInitializer> containerInitializersCache = new Hashtable<>(5);

    /*
     * A HashSet that contains the IJavaProject whose classpath is being resolved.
     */
    private final ThreadLocal<Set<IJavaProject>> classpathsBeingResolved = new ThreadLocal<>();

    /*
     * Map from a package fragment root's path to a source attachment property (source path +
     * ATTACHMENT_PROPERTY_DELIMITER + source root path)
     */
    public Map<IPath, String> rootPathToAttachments = new Hashtable<>();

    public final static String CP_ENTRY_IGNORE = "##<cp entry ignore>##"; //$NON-NLS-1$
    public final static IPath CP_ENTRY_IGNORE_PATH = new Path(CP_ENTRY_IGNORE);
    public final static String TRUE = "true"; //$NON-NLS-1$

    /**
     * Name of the extension point for contributing classpath variable initializers
     */
    public static final String CPVARIABLE_INITIALIZER_EXTPOINT_ID = "classpathVariableInitializer"; //$NON-NLS-1$

    /**
     * Name of the extension point for contributing classpath container initializers
     */
    public static final String CPCONTAINER_INITIALIZER_EXTPOINT_ID = "classpathContainerInitializer"; //$NON-NLS-1$

    /**
     * Name of the extension point for contributing a compilation participant
     */
    public static final String COMPILATION_PARTICIPANT_EXTPOINT_ID = "compilationParticipant"; //$NON-NLS-1$

    /**
     * Special value used for recognizing ongoing initialization and breaking initialization cycles
     */
    public final static IPath VARIABLE_INITIALIZATION_IN_PROGRESS = new Path("Variable Initialization In Progress"); //$NON-NLS-1$

    // Non-static, which will give it a chance to retain the default when and if JavaModelManager is restarted.
    boolean resolveReferencedLibrariesForContainers = false;

    public final static ICompilationUnit[] NO_WORKING_COPY = new ICompilationUnit[0];

    // Options
    HashSet<String> optionNames = new HashSet<>(20);
    Map<String, String[]> deprecatedOptions = new HashMap<>();
    volatile Hashtable<String, String> optionsCache;

    // Preferences
    public final IEclipsePreferences[] preferencesLookup = new IEclipsePreferences[2];
    static final int PREF_INSTANCE = 0;

    static final Object[][] NO_PARTICIPANTS = new Object[0][];

    public static class CompilationParticipants {

        static final int MAX_SOURCE_LEVEL = JavaCore.getAllVersions().size() - 1; // All except VERSION_CLDC_1_1

        /*
         * The registered compilation participants (a table from int (source level) to Object[])
         * The Object array contains first IConfigurationElements when not resolved yet, then
         * it contains CompilationParticipants.
         */
        private Object[][] registeredParticipants = null;
        private HashSet<String> managedMarkerTypes;

        public HashSet<String> managedMarkerTypes() {
            if (this.managedMarkerTypes == null) {
                // force extension points to be read
                getRegisteredParticipants();
            }
            return this.managedMarkerTypes;
        }

        private synchronized Object[][] getRegisteredParticipants() {
            if (this.registeredParticipants != null) {
                return this.registeredParticipants;
            }
            this.managedMarkerTypes = new HashSet<>();
            IExtensionPoint extension = Platform.getExtensionRegistry()
                .getExtensionPoint(JavaCore.PLUGIN_ID, COMPILATION_PARTICIPANT_EXTPOINT_ID);
            if (extension == null)
                return this.registeredParticipants = NO_PARTICIPANTS;
            final ArrayList<IConfigurationElement> modifyingEnv = new ArrayList<>();
            final ArrayList<IConfigurationElement> creatingProblems = new ArrayList<>();
            final ArrayList<IConfigurationElement> others = new ArrayList<>();
            IExtension[] extensions = extension.getExtensions();
            // for all extensions of this point...
            for (IExtension ext : extensions) {
                IConfigurationElement[] configElements = ext.getConfigurationElements();
                // for all config elements named "compilationParticipant"
                for (final IConfigurationElement configElement : configElements) {
                    String elementName = configElement.getName();
                    if (!("compilationParticipant".equals(elementName))) { //$NON-NLS-1$
                        continue;
                    }
                    // add config element in the group it belongs to
                    if (TRUE.equals(configElement.getAttribute("modifiesEnvironment"))) //$NON-NLS-1$
                        modifyingEnv.add(configElement);
                    else if (TRUE.equals(configElement.getAttribute("createsProblems"))) //$NON-NLS-1$
                        creatingProblems.add(configElement);
                    else
                        others.add(configElement);
                    // add managed marker types
                    IConfigurationElement[] managedMarkers = configElement.getChildren("managedMarker"); //$NON-NLS-1$
                    for (IConfigurationElement element : managedMarkers) {
                        String markerType = element.getAttribute("markerType"); //$NON-NLS-1$
                        if (markerType != null)
                            this.managedMarkerTypes.add(markerType);
                    }
                }
            }
            int size = modifyingEnv.size() + creatingProblems.size() + others.size();
            if (size == 0)
                return this.registeredParticipants = NO_PARTICIPANTS;

            // sort config elements in each group
            IConfigurationElement[] configElements = new IConfigurationElement[size];
            int index = 0;
            index = sortParticipants(modifyingEnv, configElements, index);
            index = sortParticipants(creatingProblems, configElements, index);
            index = sortParticipants(others, configElements, index);

            // create result table
            Object[][] result = new Object[MAX_SOURCE_LEVEL][];
            int length = configElements.length;
            for (int i = 0; i < MAX_SOURCE_LEVEL; i++) {
                result[i] = new Object[length];
            }
            for (int i = 0; i < length; i++) {
                String sourceLevel = configElements[i].getAttribute("requiredSourceLevel"); //$NON-NLS-1$
                int sourceLevelIndex = indexForSourceLevel(sourceLevel);
                for (int j = sourceLevelIndex; j < MAX_SOURCE_LEVEL; j++) {
                    result[j][i] = configElements[i];
                }
            }
            return this.registeredParticipants = result;
        }

        /*
         * 1.1 -> 0
         * 1.2 -> 1
         * ...
         * 1.6 -> 5
         * 1.7 -> 6
         * 1.8 -> 7
         * 9 -> 8
         * null -> 0
         */
        private int indexForSourceLevel(String sourceLevel) {
            if (sourceLevel == null)
                return 0;
            int majVersion = (int) (CompilerOptions.versionToJdkLevel(sourceLevel) >>> 16);
            if (majVersion > ClassFileConstants.MAJOR_VERSION_1_2) {
                return (majVersion - ClassFileConstants.MAJOR_VERSION_1_1);
            }
            // all other cases including ClassFileConstants.MAJOR_VERSION_1_1
            return 0;
        }

        private int sortParticipants(ArrayList<IConfigurationElement> group, IConfigurationElement[] configElements,
            int index) {
            int size = group.size();
            if (size == 0)
                return index;
            Object[] elements = group.toArray();
            Util.sort(elements, (a, b) -> {
                if (a == b)
                    return 0;
                String id = ((IConfigurationElement) a).getAttribute("id"); //$NON-NLS-1$
                if (id == null)
                    return -1;
                IConfigurationElement[] requiredElements = ((IConfigurationElement) b).getChildren("requires"); //$NON-NLS-1$
                for (IConfigurationElement required : requiredElements) {
                    if (id.equals(required.getAttribute("id"))) //$NON-NLS-1$
                        return -1;
                }
                return 1;
            });
            for (int i = 0; i < size; i++)
                configElements[index + i] = (IConfigurationElement) elements[i];
            return index + size;
        }
    }

    public final CompilationParticipants compilationParticipants = new CompilationParticipants();

    /* whether an AbortCompilationUnit should be thrown when the source of a compilation unit cannot be retrieved */
    public ThreadLocal<Boolean> abortOnMissingSource = new ThreadLocal<>();

    private final ExternalFoldersManager externalFoldersManager = ExternalFoldersManager.getExternalFoldersManager();

    /**
     * Returns the Java element corresponding to the given resource, or
     * <code>null</code> if unable to associate the given resource
     * with a Java element.
     * <p>
     * The resource must be one of:<ul>
     * <li>a project - the element returned is the corresponding <code>IJavaProject</code></li>
     * <li>a <code>.java</code> file - the element returned is the corresponding <code>ICompilationUnit</code></li>
     * <li>a <code>.class</code> file - the element returned is the corresponding <code>IClassFile</code></li>
     * <li>a ZIP archive (e.g. a <code>.jar</code>, a <code>.zip</code> file, etc.) - the element returned is the
     * corresponding <code>IPackageFragmentRoot</code></li>
     * <li>a folder - the element returned is the corresponding <code>IPackageFragmentRoot</code>
     * or <code>IPackageFragment</code></li>
     * <li>the workspace root resource - the element returned is the <code>IJavaModel</code></li>
     * </ul>
     * <p>
     * Creating a Java element has the side effect of creating and opening all of the
     * element's parents if they are not yet open.
     */
    public static IJavaElement create(IResource resource, IJavaProject project) {
        if (resource == null) {
            return null;
        }
        int type = resource.getType();
        switch (type) {
            case IResource.PROJECT:
                return JavaCore.create((IProject) resource);

            case IResource.FILE:
                return create((IFile) resource, project);

            case IResource.FOLDER:
                return create((IFolder) resource, project);

            case IResource.ROOT:
                return JavaCore.create((IWorkspaceRoot) resource);

            default:
                return null;
        }
    }

    /**
     * Returns the Java element corresponding to the given file, its project being the given
     * project.
     * Returns <code>null</code> if unable to associate the given file
     * with a Java element.
     *
     * <p>The file must be one of:<ul>
     * <li>a <code>.java</code> file - the element returned is the corresponding <code>ICompilationUnit</code></li>
     * <li>a <code>.class</code> file - the element returned is the corresponding <code>IClassFile</code></li>
     * <li>a ZIP archive (e.g. a <code>.jar</code>, a <code>.zip</code> file, etc.) - the element returned is the
     * corresponding <code>IPackageFragmentRoot</code></li>
     * </ul>
     * <p>
     * Creating a Java element has the side effect of creating and opening all of the
     * element's parents if they are not yet open.
     */
    public static IJavaElement create(IFile file, IJavaProject project) {
        if (file == null) {
            return null;
        }
        if (project == null) {
            project = JavaCore.create(file.getProject());
        }

        if (file.getFileExtension() != null) {
            String name = file.getName();
            if (Util.isJavaLikeFileName(name))
                return createCompilationUnitFrom(file, project);
            if (com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.util.Util
                .isClassFileName(name))
                return createClassFileFrom(file, project);
            return createJarPackageFragmentRootFrom(file, project);
        }
        return null;
    }

    private static volatile String lastProjectNameUsed;

    /**
     * Returns the package fragment or package fragment root corresponding to the given folder,
     * its parent or great parent being the given project.
     * or <code>null</code> if unable to associate the given folder with a Java element.
     * <p>
     * Note that a package fragment root is returned rather than a default package.
     * <p>
     * Creating a Java element has the side effect of creating and opening all of the
     * element's parents if they are not yet open.
     */
    public static IJavaElement create(IFolder folder, IJavaProject project) {
        if (folder == null) {
            return null;
        }
        IJavaElement element;
        if (project == null) {
            project = JavaCore.create(folder.getProject());
            element = determineIfOnClasspath(folder, project);
            if (element == null) {
                IJavaProject lastProject = lastProjectNameUsed == null
                    ? null
                    : JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject(lastProjectNameUsed);
                if (lastProject != null) {
                    // try to avoid searching through all projects
                    element = determineIfOnClasspath(folder, lastProject);
                    if (element != null) {
                        return element;
                    }
                }
                // walk all projects and find one that have the given folder on its classpath
                IJavaProject[] projects;
                try {
                    projects = JavaModelManager.getJavaModelManager().getJavaModel().getJavaProjects();
                } catch (JavaModelException e) {
                    return null;
                }
                for (IJavaProject p : projects) {
                    if (!p.equals(lastProject)) {
                        element = determineIfOnClasspath(folder, p);
                        if (element != null) {
                            lastProjectNameUsed = p.getElementName();
                            return element;
                        }
                    }
                }
            }
        } else {
            element = determineIfOnClasspath(folder, project);
        }
        return element;
    }

    /**
     * Creates and returns a class file element for the given <code>.class</code> file,
     * its project being the given project. Returns <code>null</code> if unable
     * to recognize the class file.
     */
    public static IClassFile createClassFileFrom(IFile file, IJavaProject project) {
        if (file == null) {
            return null;
        }
        if (project == null) {
            project = JavaCore.create(file.getProject());
        }
        IPackageFragment pkg = (IPackageFragment) determineIfOnClasspath(file, project);
        if (pkg == null) {
            // fix for 1FVS7WE
            // not on classpath - make the root its folder, and a default package
            PackageFragmentRoot root = (PackageFragmentRoot) project.getPackageFragmentRoot(file.getParent());
            pkg = root.getPackageFragment(CharOperation.NO_STRINGS);
        }
        String fileName = file.getName();
        if (TypeConstants.MODULE_INFO_CLASS_NAME_STRING.equals(fileName))
            return pkg.getModularClassFile();
        return pkg.getClassFile(file.getName());
    }

    /**
     * Creates and returns a compilation unit element for the given <code>.java</code>
     * file, its project being the given project. Returns <code>null</code> if unable
     * to recognize the compilation unit.
     */
    public static ICompilationUnit createCompilationUnitFrom(IFile file, IJavaProject project) {

        if (file == null)
            return null;

        if (project == null) {
            project = JavaCore.create(file.getProject());
        }
        IPackageFragment pkg = (IPackageFragment) determineIfOnClasspath(file, project);
        if (pkg == null) {
            // not on classpath - make the root its folder, and a default package
            PackageFragmentRoot root = (PackageFragmentRoot) project.getPackageFragmentRoot(file.getParent());
            pkg = root.getPackageFragment(CharOperation.NO_STRINGS);

            if (VERBOSE) {
                trace("WARNING : creating unit element outside classpath (" + Thread.currentThread() + "): " //$NON-NLS-1$//$NON-NLS-2$
                    + file.getFullPath());
            }
        }
        return pkg.getCompilationUnit(file.getName());
    }

    /**
     * Creates and returns a handle for the given JAR file, its project being the given project.
     * The Java model associated with the JAR's project may be
     * created as a side effect.
     * Returns <code>null</code> if unable to create a JAR package fragment root.
     * (for example, if the JAR file represents a non-Java resource)
     */
    public static IPackageFragmentRoot createJarPackageFragmentRootFrom(IFile file, IJavaProject project) {
        if (file == null) {
            return null;
        }
        if (project == null) {
            project = JavaCore.create(file.getProject());
        }

        // Create a jar package fragment root only if on the classpath
        IPath resourcePath = file.getFullPath();
        try {
            IClasspathEntry entry = ((JavaProject) project).getClasspathEntryFor(resourcePath);
            if (entry != null) {
                return project.getPackageFragmentRoot(file);
            }
        } catch (JavaModelException e) {
            // project doesn't exist: return null
        }
        return null;
    }

    /**
     * Returns the package fragment root represented by the resource, or
     * the package fragment the given resource is located in, or <code>null</code>
     * if the given resource is not on the classpath of the given project.
     */
    public static IJavaElement determineIfOnClasspath(IResource resource, IJavaProject project) {
        IPath resourcePath = resource.getFullPath();
        boolean isExternal = ExternalFoldersManager.isInternalPathForExternalFolder(resourcePath);
        if (isExternal)
            resourcePath = resource.getLocation();

        try {
            JavaProjectElementInfo projectInfo = (JavaProjectElementInfo) getJavaModelManager().getInfo(project);
            ProjectCache projectCache = projectInfo == null ? null : projectInfo.projectCache;
            HashtableOfArrayToObject allPkgFragmentsCache
                = projectCache == null ? null : projectCache.allPkgFragmentsCache;
            boolean isJavaLike = Util.isJavaLikeFileName(resourcePath.lastSegment());
            IClasspathEntry[] entries = isJavaLike
                ? project.getRawClasspath() // JAVA file can only live inside SRC folder (on the raw path)
                : ((JavaProject) project).getResolvedClasspath();

            int length = entries.length;
            if (length > 0) {
                String sourceLevel = "1.8";
                String complianceLevel = "1.8";
                for (IClasspathEntry entry : entries) {
                    if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT)
                        continue;
                    IPath rootPath = entry.getPath();
                    if (rootPath.equals(resourcePath)) {
                        if (isJavaLike)
                            return null;
                        return project.getPackageFragmentRoot(resource);
                    } else if (rootPath.isPrefixOf(resourcePath)) {
                        // allow creation of package fragment if it contains a .java file that is included
                        if (!Util.isExcluded(resource, ((ClasspathEntry) entry).fullInclusionPatternChars(),
                            ((ClasspathEntry) entry).fullExclusionPatternChars())) {
                            // given we have a resource child of the root, it cannot be a JAR pkg root
                            PackageFragmentRoot root = isExternal
                                ? new ExternalPackageFragmentRoot(rootPath, (JavaProject) project)
                                : (PackageFragmentRoot) ((JavaProject) project).getFolderPackageFragmentRoot(rootPath);
                            if (root == null)
                                return null;
                            IPath pkgPath = resourcePath.removeFirstSegments(rootPath.segmentCount());

                            if (resource.getType() == IResource.FILE) {
                                // if the resource is a file, then remove the last segment which
                                // is the file name in the package
                                pkgPath = pkgPath.removeLastSegments(1);
                            }
                            String[] pkgName = pkgPath.segments();

                            // if package name is in the cache, then it has already been validated
                            // (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=133141)
                            if (allPkgFragmentsCache != null && allPkgFragmentsCache.containsKey(pkgName))
                                return root.getPackageFragment(pkgName);

                            if (pkgName.length != 0
                                && JavaConventions
                                    .validatePackageName(Util.packageName(pkgPath, sourceLevel, complianceLevel),
                                        sourceLevel, complianceLevel)
                                    .getSeverity() == IStatus.ERROR) {
                                return null;
                            }
                            return root.getPackageFragment(pkgName);
                        }
                    }
                }
            }
        } catch (JavaModelException npe) {
            return null;
        }
        return null;
    }

    /**
     * The current JavaModelManager. Normally a singleton but alternating during junit tests
     */
    private static final JavaModelManager MANAGER = new JavaModelManager();

    /**
     * Infos cache.
     */
    private JavaModelCache cache;

    /*
     * Temporary cache of newly opened elements
     */
    private final ThreadLocal<HashMap<IJavaElement, IElementInfo>> temporaryCache = new ThreadLocal<>();

    /**
     * Set of elements which are out of sync with their buffers.
     */
    protected HashSet<Openable> elementsOutOfSynchWithBuffers = new HashSet<>(11);

    /**
     * Holds the state used for delta processing.
     */
    public DeltaProcessingState deltaState = new DeltaProcessingState();

    /**
     * Table from IProject to PerProjectInfo.
     * NOTE: this object itself is used as a lock to synchronize creation/removal of per project infos
     */
    protected Map<IProject, PerProjectInfo> perProjectInfos = new HashMap<>(5);

    /**
     * Table from WorkingCopyOwner to a table of ICompilationUnit (working copy handle) to PerWorkingCopyInfo.
     * NOTE: this object itself is used as a lock to synchronize creation/removal of per working copy infos
     */
    protected HashMap<WorkingCopyOwner, Map<CompilationUnit, PerWorkingCopyInfo>> perWorkingCopyInfos
        = new HashMap<>(5);

    /**
     * Current secondary types cache and temporary indexing cache
     *
     * @param secondaryTypes
     * the main cache containing known secondary types in a project, can be null to indicate no search were
     * done. Note, the cache might be not complete if the search is still running.
     * @param indexingSecondaryCache
     * the temporary structure used while indexing, previously known as INDEXED_SECONDARY_TYPES entry. If it
     * is null, no cache updates are running now
     */
    private record SecondaryTypesCache(Hashtable<String, Map<String, IType>> secondaryTypes,
        Map<IFile, Map<String, Map<String, IType>>> indexingSecondaryCache) {

    }

    /**
     * Secondary types cache management code for a {@link PerProjectInfo}
     */
    private static class SecondaryTypes {
        private volatile SecondaryTypesCache cache;

        public SecondaryTypes() {
            this.cache = new SecondaryTypesCache(null, null);
        }

        private synchronized SecondaryTypesCache getOrCreateCache() {
            Hashtable<String, Map<String, IType>> secondaryTypes = this.cache.secondaryTypes;
            Map<IFile, Map<String, Map<String, IType>>> indexingSecondaryCache = this.cache.indexingSecondaryCache;
            if (secondaryTypes != null && indexingSecondaryCache != null) {
                return this.cache;
            }
            if (secondaryTypes == null) {
                secondaryTypes = new Hashtable<>(3);
            }
            if (indexingSecondaryCache == null) {
                indexingSecondaryCache = Collections.synchronizedMap(new HashMap<>(3));
            }
            this.cache = new SecondaryTypesCache(secondaryTypes, indexingSecondaryCache);
            return this.cache;
        }

    }

    public static class PerProjectInfo {
        private static final int JAVADOC_CACHE_INITIAL_SIZE = 10;

        static final IJavaModelStatus NEED_RESOLUTION = new JavaModelStatus();

        public final IProject project;
        public volatile IClasspathEntry[] rawClasspath;
        public volatile IClasspathEntry[] referencedEntries;
        public volatile IJavaModelStatus rawClasspathStatus;
        public volatile int rawTimeStamp;
        public volatile IClasspathEntry[] resolvedClasspath;
        public volatile IJavaModelStatus unresolvedEntryStatus;
        public volatile Map<IPath, IClasspathEntry> rootPathToRawEntries; // reverse map from a package fragment root's
                                                                          // path to the raw entry
        private Map<IPath, IClasspathEntry> rootPathToResolvedEntries; // map from a package fragment root's path to the
                                                                       // resolved entry
        public volatile IPath outputLocation;
        public volatile Map<IPath, ObjectVector> jrtRoots; // A map between a JRT file system (as a string) and the
                                                           // package fragment roots found in it.

        public volatile IEclipsePreferences preferences;
        public volatile Hashtable<String, String> options;

        private final SecondaryTypes secondaryTypes;

        // NB: PackageFragment#getAttachedJavadoc uses this map differently
        // and stores String data, not JavadocContents as values
        public volatile LRUCache<IJavaElement, Object> javadocCache;

        public PerProjectInfo(IProject project) {
            this.project = project;
            this.javadocCache = new LRUCache<>(JAVADOC_CACHE_INITIAL_SIZE);
            this.secondaryTypes = new SecondaryTypes();
        }

        public synchronized IClasspathEntry[] getResolvedClasspath() {
            if (this.unresolvedEntryStatus == NEED_RESOLUTION)
                return null;
            return this.resolvedClasspath;
        }

        public synchronized Map<IPath, IClasspathEntry> getRootPathToResolvedEntries() {
            Map<IPath, IClasspathEntry> entries = this.rootPathToResolvedEntries;
            if (entries == null) {
                return Map.of();
            }
            return entries;
        }

        private ClasspathChange setClasspath(IClasspathEntry[] newRawClasspath, IClasspathEntry[] referencedEntries,
            IPath newOutputLocation, IJavaModelStatus newRawClasspathStatus, IClasspathEntry[] newResolvedClasspath,
            Map<IPath, IClasspathEntry> newRootPathToRawEntries,
            Map<IPath, IClasspathEntry> newRootPathToResolvedEntries, IJavaModelStatus newUnresolvedEntryStatus,
            boolean addClasspathChange) {
            if (DEBUG_CLASSPATH) {
                trace("Setting resolved classpath for " + this.project.getFullPath()); //$NON-NLS-1$
                if (newResolvedClasspath == null) {
                    trace("New classpath = null"); //$NON-NLS-1$
                } else {
                    for (IClasspathEntry next : newResolvedClasspath) {
                        trace("    " + next); //$NON-NLS-1$
                    }
                }
            }
            ClasspathChange classpathChange = addClasspathChange ? addClasspathChange() : null;

            synchronized (this) {
                if (referencedEntries != null)
                    this.referencedEntries = referencedEntries;
                if (this.referencedEntries == null)
                    this.referencedEntries = ClasspathEntry.NO_ENTRIES;
                this.rawClasspath = newRawClasspath;
                this.outputLocation = newOutputLocation;
                this.rawClasspathStatus = newRawClasspathStatus;
                this.resolvedClasspath = newResolvedClasspath;
                this.rootPathToRawEntries = newRootPathToRawEntries;
                this.rootPathToResolvedEntries = newRootPathToResolvedEntries;
                this.unresolvedEntryStatus = newUnresolvedEntryStatus;
                this.javadocCache = new LRUCache<>(JAVADOC_CACHE_INITIAL_SIZE);
            }

            return classpathChange;
        }

        protected ClasspathChange addClasspathChange() {
            // remember old info
            JavaModelManager manager = JavaModelManager.getJavaModelManager();
            ClasspathChange classpathChange = manager.deltaState.addClasspathChange(this.project, this.rawClasspath,
                this.outputLocation, this.resolvedClasspath);
            return classpathChange;
        }

        public ClasspathChange setRawClasspath(IClasspathEntry[] newRawClasspath, IPath newOutputLocation,
            IJavaModelStatus newRawClasspathStatus) {
            return setRawClasspath(newRawClasspath, null, newOutputLocation, newRawClasspathStatus);
        }

        public synchronized ClasspathChange setRawClasspath(IClasspathEntry[] newRawClasspath,
            IClasspathEntry[] referencedEntries, IPath newOutputLocation, IJavaModelStatus newRawClasspathStatus) {
            this.rawTimeStamp++;
            return setClasspath(newRawClasspath, referencedEntries, newOutputLocation, newRawClasspathStatus,
                null/* resolved classpath */, null/* root to raw map */, null/* root to resolved map */,
                null/* unresolved status */, true/* add classpath change */);
        }

        public synchronized ClasspathChange setResolvedClasspath(IClasspathEntry[] newResolvedClasspath,
            IClasspathEntry[] referencedEntries, Map<IPath, IClasspathEntry> newRootPathToRawEntries,
            Map<IPath, IClasspathEntry> newRootPathToResolvedEntries, IJavaModelStatus newUnresolvedEntryStatus,
            int timeStamp, boolean addClasspathChange) {
            if (this.rawTimeStamp != timeStamp)
                return null;
            return setClasspath(this.rawClasspath, referencedEntries, this.outputLocation, this.rawClasspathStatus,
                newResolvedClasspath, newRootPathToRawEntries, newRootPathToResolvedEntries, newUnresolvedEntryStatus,
                addClasspathChange);
        }

        public synchronized void setJrtPackageRoots(IPath jrtPath, ObjectVector roots) {
            if (this.jrtRoots == null)
                this.jrtRoots = new HashMap<>();
            this.jrtRoots.put(jrtPath, roots);
        }

        /**
         * Reads the classpath and caches the entries. Returns a two-dimensional array, where the number of elements in
         * the row is fixed to 2.
         * The first element is an array of raw classpath entries and the second element is an array of referenced
         * entries that may have been stored
         * by the client earlier.
         */
        public synchronized IClasspathEntry[][] readAndCacheClasspath(JavaProject javaProject) {
            // read file entries and update status
            IClasspathEntry[][] classpath;
            IJavaModelStatus status;
            try {
                classpath = javaProject.readFileEntriesWithException(null/* not interested in unknown elements */);
                status = JavaModelStatus.VERIFIED_OK;
            } catch (CoreException e) {
                classpath = new IClasspathEntry[][] { JavaProject.INVALID_CLASSPATH, ClasspathEntry.NO_ENTRIES };
                status = new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH_FILE_FORMAT,
                    Messages.bind(Messages.classpath_cannotReadClasspathFile, javaProject.getElementName()));
            } catch (IOException e) {
                classpath = new IClasspathEntry[][] { JavaProject.INVALID_CLASSPATH, ClasspathEntry.NO_ENTRIES };
                if (Messages.file_badFormat.equals(e.getMessage()))
                    status = new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH_FILE_FORMAT, Messages.bind(
                        Messages.classpath_xmlFormatError, javaProject.getElementName(), Messages.file_badFormat));
                else
                    status = new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH_FILE_FORMAT,
                        Messages.bind(Messages.classpath_cannotReadClasspathFile, javaProject.getElementName()));
            } catch (ClasspathEntry.AssertionFailedException e) {
                classpath = new IClasspathEntry[][] { JavaProject.INVALID_CLASSPATH, ClasspathEntry.NO_ENTRIES };
                status = new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH_FILE_FORMAT,
                    Messages.bind(Messages.classpath_illegalEntryInClasspathFile,
                        new String[] { javaProject.getElementName(), e.getMessage() }));
            }

            // extract out the output location
            int rawClasspathLength = classpath[0].length;
            IPath output = null;
            if (rawClasspathLength > 0) {
                IClasspathEntry entry = classpath[0][rawClasspathLength - 1];
                if (entry.getContentKind() == ClasspathEntry.K_OUTPUT) {
                    output = entry.getPath();
                    IClasspathEntry[] copy = new IClasspathEntry[rawClasspathLength - 1];
                    System.arraycopy(classpath[0], 0, copy, 0, copy.length);
                    classpath[0] = copy;
                }
            }

            // store new raw classpath, new output and new status, and null out resolved info
            setRawClasspath(classpath[0], classpath[1], output, status);

            return classpath;
        }

        @Override
        public String toString() {
            StringBuilder buffer = new StringBuilder();
            buffer.append("Info for "); //$NON-NLS-1$
            buffer.append(this.project.getFullPath());
            buffer.append("\nRaw classpath:\n"); //$NON-NLS-1$
            if (this.rawClasspath == null) {
                buffer.append("  <null>\n"); //$NON-NLS-1$
            } else {
                for (IClasspathEntry cpe : this.rawClasspath) {
                    buffer.append("  "); //$NON-NLS-1$
                    buffer.append(cpe);
                    buffer.append('\n');
                }
            }
            buffer.append("Resolved classpath:\n"); //$NON-NLS-1$
            IClasspathEntry[] resolvedCP = this.resolvedClasspath;
            if (resolvedCP == null) {
                buffer.append("  <null>\n"); //$NON-NLS-1$
            } else {
                for (IClasspathEntry cpe : resolvedCP) {
                    buffer.append("  "); //$NON-NLS-1$
                    buffer.append(cpe);
                    buffer.append('\n');
                }
            }
            buffer.append("Resolved classpath status: "); //$NON-NLS-1$
            if (this.unresolvedEntryStatus == NEED_RESOLUTION)
                buffer.append("NEED RESOLUTION"); //$NON-NLS-1$
            else
                buffer.append(this.unresolvedEntryStatus == null ? "<null>\n" : this.unresolvedEntryStatus.toString()); //$NON-NLS-1$
            buffer.append("Output location:\n  "); //$NON-NLS-1$
            if (this.outputLocation == null) {
                buffer.append("<null>"); //$NON-NLS-1$
            } else {
                buffer.append(this.outputLocation);
            }
            return buffer.toString();
        }

    }

    public static class PerWorkingCopyInfo implements IProblemRequestor {
        int useCount = 0;
        private final IProblemRequestor problemRequestor;
        final CompilationUnit workingCopy;

        public PerWorkingCopyInfo(CompilationUnit workingCopy, IProblemRequestor problemRequestor) {
            this.workingCopy = workingCopy;
            this.problemRequestor = problemRequestor;
        }

        @Override
        public void acceptProblem(IProblem problem) {
            IProblemRequestor requestor = getProblemRequestor();
            if (requestor == null)
                return;
            requestor.acceptProblem(problem);
        }

        @Override
        public void beginReporting() {
            IProblemRequestor requestor = getProblemRequestor();
            if (requestor == null)
                return;
            requestor.beginReporting();
        }

        @Override
        public void endReporting() {
            IProblemRequestor requestor = getProblemRequestor();
            if (requestor == null)
                return;
            requestor.endReporting();
        }

        public IProblemRequestor getProblemRequestor() {
            if (this.problemRequestor == null && this.workingCopy.owner != null) {
                return this.workingCopy.owner.getProblemRequestor(this.workingCopy);
            }
            return this.problemRequestor;
        }

        public ICompilationUnit getWorkingCopy() {
            return this.workingCopy;
        }

        @Override
        public boolean isActive() {
            IProblemRequestor requestor = getProblemRequestor();
            return requestor != null && requestor.isActive();
        }

        @Override
        public String toString() {
            StringBuilder buffer = new StringBuilder();
            buffer.append("Info for "); //$NON-NLS-1$
            buffer.append(this.workingCopy.toStringWithAncestors());
            buffer.append("\nUse count = "); //$NON-NLS-1$
            buffer.append(this.useCount);
            buffer.append("\nProblem requestor:\n  "); //$NON-NLS-1$
            buffer.append(this.problemRequestor);
            if (this.problemRequestor == null) {
                IProblemRequestor requestor = getProblemRequestor();
                buffer.append("\nOwner problem requestor:\n  "); //$NON-NLS-1$
                buffer.append(requestor);
            }
            return buffer.toString();
        }
    }

    public static boolean VERBOSE = false;
    public static boolean DEBUG_CLASSPATH = false;
    public static boolean DEBUG_INVALID_ARCHIVES = false;
    public static boolean CP_RESOLVE_VERBOSE = false;
    public static boolean CP_RESOLVE_VERBOSE_ADVANCED = false;
    public static boolean CP_RESOLVE_VERBOSE_FAILURE = false;
    public static boolean ZIP_ACCESS_WARNING = false;
    public static boolean ZIP_ACCESS_VERBOSE = false;

    /**
     * A cache of opened zip files per thread.
     * (for a given thread, the object value is a HashMap from IPath to java.io.ZipFile)
     */
    private final ThreadLocal<ZipCache> zipFiles = new ThreadLocal<>();

    private ModuleSourcePathManager modulePathManager;
    /*
     * A set of IPaths for jars that are known to not contain a chaining (through MANIFEST.MF) to another library
     */
    private Set<IPath> nonChainingJars;

    private static boolean TRACE_TO_STDOUT;

    private static class InvalidArchiveInfo {
        /**
         * Time at which this entry will be removed from the invalid archive list.
         */
        final long evictionTimestamp;

        /**
         * Reason the entry was added to the invalid archive list.
         */
        final ArchiveValidity reason;

        InvalidArchiveInfo(long evictionTimestamp, ArchiveValidity reason) {
            this.evictionTimestamp = evictionTimestamp;
            this.reason = reason;
        }
    }

    /*
     * A map of IPaths for jars with known validity (such as being in a valid/known format or not), to an eviction
     * timestamp.
     * Synchronize on invalidArchives before accessing.
     */
    private final Map<IPath, InvalidArchiveInfo> invalidArchives = new HashMap<>();

    /*
     * Paths that are known to exists or not exists on the FileSystem (unrelated to the eclipse workspace).
     * Need not be referenced by the classpath.
     */
    private Map<IPath, Boolean> externalFiles;

    /*
     * A set of IPaths for files that do not exist on the file system but are assumed to be
     * external archives (rather than external folders).
     */
    private Set<IPath> assumedExternalFiles;

    /**
     * Constructs a new JavaModelManager
     */
    private JavaModelManager() {
        // singleton: prevent others from creating a new instance
        /*
         * It is required to initialize all fields that depends on a headless environment
         * only if the platform is running. Otherwise this breaks the ability to use
         * ASTParser in a non-headless environment.
         */
    }

    public void addNonChainingJar(IPath path) {
        if (this.nonChainingJars != null)
            this.nonChainingJars.add(path);
    }

    public void addInvalidArchive(IPath path, ArchiveValidity reason) {
        if (DEBUG_INVALID_ARCHIVES) {
            trace("JAR cache: adding " + reason + " " + path);  //$NON-NLS-1$//$NON-NLS-2$
        }
        synchronized (this.invalidArchives) {
            // The amount of time from when an invalid archive is first sensed until that state is considered stale.
            long INVALID_ARCHIVE_TTL_MILLISECONDS = 2 * 60 * 1000;
            this.invalidArchives.put(path,
                new InvalidArchiveInfo(System.currentTimeMillis() + INVALID_ARCHIVE_TTL_MILLISECONDS, reason));
        }
    }

    /**
     * Adds a path to the external files cache. It is the responsibility of callers to
     * determine the file's existence, as determined by {@link File#isFile()}.
     */
    public void addExternalFile(IPath path, boolean exits) {
        // unlikely to be null
        if (this.externalFiles == null) {
            this.externalFiles = new ConcurrentHashMap<>();
        }
        this.externalFiles.put(path, exits);
    }

    /**
     * Starts caching ZipFiles.
     * Ignores if there are already clients.
     */
    public void cacheZipFiles(Object owner) {
        ZipCache zipCache = this.zipFiles.get();
        if (zipCache != null) {
            return;
        }
        // the owner will be responsible for flushing the cache
        this.zipFiles.set(new ZipCache(owner));
    }

    public void closeZipFile(ZipFile zipFile) {
        if (zipFile == null)
            return;
        if (this.zipFiles.get() != null) {
            if (JavaModelManager.ZIP_ACCESS_VERBOSE) {
                trace("(" + Thread.currentThread() //$NON-NLS-1$
                    + ") [JavaModelManager.closeZipFile(ZipFile)] NOT closed ZipFile (cache exist!) on " //$NON-NLS-1$
                    + zipFile.getName());
            }
            return; // zip file will be closed by call to flushZipFiles
        }
        try {
            if (JavaModelManager.ZIP_ACCESS_VERBOSE) {
                trace("(" + Thread.currentThread() + ") [JavaModelManager.closeZipFile(ZipFile)] Closing ZipFile on " //$NON-NLS-1$ //$NON-NLS-2$
                    + zipFile.getName());
            }
            zipFile.close();
        } catch (IOException e) {
            // problem occured closing zip file: cannot do much more
            JavaCore.getPlugin()
                .getLog()
                .log(new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, "Error closing " + zipFile.getName(), e)); //$NON-NLS-1$
        }
    }

    /**
     * Flushes ZipFiles cache if there are no more clients.
     */
    public void flushZipFiles(Object owner) {
        ZipCache zipCache = this.zipFiles.get();
        if (zipCache == null) {
            if (JavaModelManager.ZIP_ACCESS_VERBOSE) {
                trace("(" + Thread.currentThread() + ") [JavaModelManager.flushZipFiles(String)] NOT found cache for " //$NON-NLS-1$ //$NON-NLS-2$
                    + owner);
            }
            return;
        }
        // the owner will be responsible for flushing the cache
        // we want to check object identity to make sure this is the owner that created the cache
        if (zipCache.owner == owner) {
            this.zipFiles.remove();
            zipCache.flush();
        } else {
            if (JavaModelManager.ZIP_ACCESS_VERBOSE) {
                trace("(" + Thread.currentThread() //$NON-NLS-1$
                    + ") [JavaModelManager.flushZipFiles(String)] NOT closed cache, wrong owner, expected: " //$NON-NLS-1$
                    + zipCache.owner + ", got: " + owner); //$NON-NLS-1$
            }
        }
    }

    public IClasspathEntry[] getReferencedClasspathEntries(IClasspathEntry libraryEntry, IJavaProject project) {

        IClasspathEntry[] referencedEntries = ((ClasspathEntry) libraryEntry).resolvedChainedLibraries();

        if (project == null)
            return referencedEntries;

        PerProjectInfo perProjectInfo = getPerProjectInfo(project.getProject(), false);
        if (perProjectInfo == null)
            return referencedEntries;

        LinkedHashSet<IPath> pathToReferencedEntries = new LinkedHashSet<>(referencedEntries.length);
        for (int index = 0; index < referencedEntries.length; index++) {

            if (pathToReferencedEntries.contains(referencedEntries[index].getPath()))
                continue;

            IClasspathEntry persistedEntry;
            Map<IPath, IClasspathEntry> rootPathToResolvedEntries = perProjectInfo.getRootPathToResolvedEntries();
            if ((persistedEntry = rootPathToResolvedEntries.get(referencedEntries[index].getPath())) != null) {
                // TODO: reconsider this - may want to copy the values instead of reference assignment?
                referencedEntries[index] = persistedEntry;
            }
            pathToReferencedEntries.add(referencedEntries[index].getPath());
        }
        return referencedEntries;
    }

    public static DeltaProcessingState getDeltaState() {
        return MANAGER.deltaState;
    }

    /**
     * Returns the set of elements which are out of synch with their buffers.
     */
    protected HashSet<Openable> getElementsOutOfSynchWithBuffers() {
        return this.elementsOutOfSynchWithBuffers;
    }

    public static ExternalFoldersManager getExternalManager() {
        return MANAGER.externalFoldersManager;
    }

    /**
     * Returns the info for the element.
     */
    public synchronized IElementInfo getInfo(IJavaElement element) {
        HashMap<IJavaElement, IElementInfo> tempCache = this.temporaryCache.get();
        if (tempCache != null) {
            IElementInfo result = tempCache.get(element);
            if (result != null) {
                return result;
            }
        }
        return this.cache.getInfo(element);
    }

    /**
     * Returns the existing element in the cache that is equal to the given element.
     */
    public synchronized IJavaElement getExistingElement(IJavaElement element) {
        return this.cache.getExistingElement(element);
    }

    /**
     * Get workspace eclipse preference for JavaCore plug-in.
     */
    public IEclipsePreferences getInstancePreferences() {
        return this.preferencesLookup[PREF_INSTANCE];
    }

    /**
     * Returns the handle to the active Java Model.
     */
    public final JavaModel getJavaModel() {
        return this.javaModel;
    }

    /**
     * Returns the singleton JavaModelManager
     */
    public final static JavaModelManager getJavaModelManager() {
        return MANAGER;
    }

    /**
     * Returns whether an option name is known or not.
     *
     * @param optionName The name of the option
     * @return <code>true</code> when the option name,
     * <code>false</code> otherwise.
     */
    public boolean knowsOption(String optionName) {
        boolean knownOption = this.optionNames.contains(optionName);
        if (!knownOption) {
            knownOption = this.deprecatedOptions.get(optionName) != null;
        }
        return knownOption;
    }

    public Hashtable<String, String> getOptions() {

        // return cached options if already computed
        Hashtable<String, String> cachedOptions; // use a local variable to avoid race condition (see
                                                 // https://bugs.eclipse.org/bugs/show_bug.cgi?id=256329 )
        if ((cachedOptions = this.optionsCache) != null) {
            return new Hashtable<>(cachedOptions);
        }
        Hashtable<String, String> defaults = getDefaultOptionsNoInitialization();
        this.optionsCache = defaults;
        return new Hashtable<>(defaults);
    }

    // Do not modify without modifying getDefaultOptions()
    private Hashtable<String, String> getDefaultOptionsNoInitialization() {
        Map<String, String> defaultOptionsMap = new CompilerOptions().getMap(); // compiler defaults

        // Override some compiler defaults
        defaultOptionsMap.put(JavaCore.COMPILER_LOCAL_VARIABLE_ATTR, JavaCore.GENERATE);
        defaultOptionsMap.put(JavaCore.COMPILER_CODEGEN_UNUSED_LOCAL, JavaCore.PRESERVE);
        defaultOptionsMap.put(JavaCore.COMPILER_TASK_TAGS, JavaCore.DEFAULT_TASK_TAGS);
        defaultOptionsMap.put(JavaCore.COMPILER_TASK_PRIORITIES, JavaCore.DEFAULT_TASK_PRIORITIES);
        defaultOptionsMap.put(JavaCore.COMPILER_TASK_CASE_SENSITIVE, JavaCore.ENABLED);
        defaultOptionsMap.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
        defaultOptionsMap.put(JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE, JavaCore.ERROR);

        // Builder settings
        defaultOptionsMap.put(JavaCore.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER, ""); //$NON-NLS-1$
        defaultOptionsMap.put(JavaCore.CORE_JAVA_BUILD_INVALID_CLASSPATH, JavaCore.ABORT);
        defaultOptionsMap.put(JavaCore.CORE_JAVA_BUILD_DUPLICATE_RESOURCE, JavaCore.WARNING);
        defaultOptionsMap.put(JavaCore.CORE_JAVA_BUILD_CLEAN_OUTPUT_FOLDER, JavaCore.CLEAN);
        defaultOptionsMap.put(JavaCore.CORE_JAVA_BUILD_EXTERNAL_ANNOTATIONS_FROM_ALL_LOCATIONS, JavaCore.DISABLED);

        // JavaCore settings
        defaultOptionsMap.put(JavaCore.CORE_JAVA_BUILD_ORDER, JavaCore.IGNORE);
        defaultOptionsMap.put(JavaCore.CORE_INCOMPLETE_CLASSPATH, JavaCore.ERROR);
        defaultOptionsMap.put(JavaCore.CORE_CIRCULAR_CLASSPATH, JavaCore.ERROR);
        defaultOptionsMap.put(JavaCore.CORE_INCOMPATIBLE_JDK_LEVEL, JavaCore.IGNORE);
        defaultOptionsMap.put(JavaCore.CORE_MAIN_ONLY_PROJECT_HAS_TEST_ONLY_DEPENDENCY, JavaCore.ERROR);
        defaultOptionsMap.put(JavaCore.CORE_OUTPUT_LOCATION_OVERLAPPING_ANOTHER_SOURCE, JavaCore.ERROR);
        defaultOptionsMap.put(JavaCore.CORE_ENABLE_CLASSPATH_EXCLUSION_PATTERNS, JavaCore.ENABLED);
        defaultOptionsMap.put(JavaCore.CORE_ENABLE_CLASSPATH_MULTIPLE_OUTPUT_LOCATIONS, JavaCore.ENABLED);

        // Formatter settings
        defaultOptionsMap.putAll(DefaultCodeFormatterConstants.getEclipseDefaultSettings());

        // CodeAssist settings
        defaultOptionsMap.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.DISABLED);
        defaultOptionsMap.put(JavaCore.CODEASSIST_DEPRECATION_CHECK, JavaCore.DISABLED);
        defaultOptionsMap.put(JavaCore.CODEASSIST_IMPLICIT_QUALIFICATION, JavaCore.DISABLED);
        defaultOptionsMap.put(JavaCore.CODEASSIST_FIELD_PREFIXES, ""); //$NON-NLS-1$
        defaultOptionsMap.put(JavaCore.CODEASSIST_STATIC_FIELD_PREFIXES, ""); //$NON-NLS-1$
        defaultOptionsMap.put(JavaCore.CODEASSIST_STATIC_FINAL_FIELD_PREFIXES, ""); //$NON-NLS-1$
        defaultOptionsMap.put(JavaCore.CODEASSIST_LOCAL_PREFIXES, ""); //$NON-NLS-1$
        defaultOptionsMap.put(JavaCore.CODEASSIST_ARGUMENT_PREFIXES, ""); //$NON-NLS-1$
        defaultOptionsMap.put(JavaCore.CODEASSIST_FIELD_SUFFIXES, ""); //$NON-NLS-1$
        defaultOptionsMap.put(JavaCore.CODEASSIST_STATIC_FIELD_SUFFIXES, ""); //$NON-NLS-1$
        defaultOptionsMap.put(JavaCore.CODEASSIST_STATIC_FINAL_FIELD_SUFFIXES, ""); //$NON-NLS-1$
        defaultOptionsMap.put(JavaCore.CODEASSIST_LOCAL_SUFFIXES, ""); //$NON-NLS-1$
        defaultOptionsMap.put(JavaCore.CODEASSIST_ARGUMENT_SUFFIXES, ""); //$NON-NLS-1$
        defaultOptionsMap.put(JavaCore.CODEASSIST_FORBIDDEN_REFERENCE_CHECK, JavaCore.ENABLED);
        defaultOptionsMap.put(JavaCore.CODEASSIST_DISCOURAGED_REFERENCE_CHECK, JavaCore.DISABLED);
        defaultOptionsMap.put(JavaCore.CODEASSIST_CAMEL_CASE_MATCH, JavaCore.ENABLED);
        defaultOptionsMap.put(JavaCore.CODEASSIST_SUBWORD_MATCH, JavaCore.ENABLED);
        defaultOptionsMap.put(JavaCore.CODEASSIST_SUGGEST_STATIC_IMPORTS, JavaCore.ENABLED);

        // Time out for parameter names
        defaultOptionsMap.put(JavaCore.TIMEOUT_FOR_PARAMETER_NAME_FROM_ATTACHED_JAVADOC, "50"); //$NON-NLS-1$

        return new Hashtable<>(defaultOptionsMap);
    }

    /*
     * Returns the per-project info for the given project. If specified, create the info if the info doesn't exist.
     */
    public PerProjectInfo getPerProjectInfo(IProject project, boolean create) {
        synchronized (this.perProjectInfos) { // use the perProjectInfo collection as its own lock
            PerProjectInfo info = this.perProjectInfos.get(project);
            if (info == null && create) {
                info = new PerProjectInfo(project);
                this.perProjectInfos.put(project, info);
            }
            return info;
        }
    }

    /*
     * Returns the per-project info for the given project.
     * If the info doesn't exist, check for the project existence and create the info.
     * 
     * @throws JavaModelException if the project doesn't exist.
     */
    public PerProjectInfo getPerProjectInfoCheckExistence(IProject project) throws JavaModelException {
        PerProjectInfo info = getPerProjectInfo(project, false /* don't create info */);
        if (info == null) {
            if (!JavaProject.hasJavaNature(project)) {
                throw ((JavaProject) JavaCore.create(project)).newNotPresentException();
            }
            info = getPerProjectInfo(project, true /* create info */);
        }
        return info;
    }

    /*
     * Returns the per-working copy info for the given working copy at the given path.
     * If it doesn't exist and if create, add a new per-working copy info with the given problem requestor.
     * If recordUsage, increment the per-working copy info's use count.
     * Returns null if it doesn't exist and not create.
     */
    public PerWorkingCopyInfo getPerWorkingCopyInfo(CompilationUnit workingCopy, boolean create, boolean recordUsage,
        IProblemRequestor problemRequestor) {
        synchronized (this.perWorkingCopyInfos) { // use the perWorkingCopyInfo collection as its own lock
            WorkingCopyOwner owner = workingCopy.owner;
            Map<CompilationUnit, PerWorkingCopyInfo> workingCopyToInfos = this.perWorkingCopyInfos.get(owner);
            if (workingCopyToInfos == null && create) {
                workingCopyToInfos = new HashMap<>();
                this.perWorkingCopyInfos.put(owner, workingCopyToInfos);
            }

            PerWorkingCopyInfo info = workingCopyToInfos == null ? null : workingCopyToInfos.get(workingCopy);
            if (info == null && create) {
                info = new PerWorkingCopyInfo(workingCopy, problemRequestor);
                workingCopyToInfos.put(workingCopy, info);
            }
            if (info != null && recordUsage)
                info.useCount++;
            return info;
        }
    }

    /**
     * Returns a persisted container from previous session if any
     */
    public IPath getPreviousSessionVariable(String variableName) {
        IPath previousPath = this.previousSessionVariables.get(variableName);
        if (previousPath != null) {
            if (CP_RESOLVE_VERBOSE_ADVANCED)
                verbose_reentering_variable_access(variableName, previousPath);
            return previousPath;
        }
        return null; // break cycle
    }

    private void verbose_reentering_variable_access(String variableName, IPath previousPath) {
        trace("CPVariable INIT - reentering access to variable during its initialization, will see previous value\n" + //$NON-NLS-1$
            "	variable: " + variableName + '\n' + //$NON-NLS-1$
            "	previous value: " + previousPath, new Exception("<Fake exception>")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Returns the temporary cache for newly opened elements for the current thread.
     * Creates it if not already created.
     */
    public HashMap<IJavaElement, IElementInfo> getTemporaryCache() {
        HashMap<IJavaElement, IElementInfo> result = this.temporaryCache.get();
        if (result == null) {
            result = new HashMap<>();
            this.temporaryCache.set(result);
        }
        return result;
    }

    public IClasspathEntry resolveVariableEntry(IClasspathEntry entry, boolean usePreviousSession) {

        if (entry.getEntryKind() != IClasspathEntry.CPE_VARIABLE)
            return entry;

        IPath resolvedPath = getResolvedVariablePath(entry.getPath(), usePreviousSession);
        if (resolvedPath == null)
            return null;
        // By passing a null reference path, we keep it relative to workspace root.
        resolvedPath = ClasspathEntry.resolveDotDot(null, resolvedPath);

        Object target = JavaModel.getTarget(resolvedPath, false);
        if (target == null)
            return null;

        // inside the workspace
        if (target instanceof IResource) {
            IResource resolvedResource = (IResource) target;
            switch (resolvedResource.getType()) {

                case IResource.PROJECT:
                    // internal project
                    return JavaCore.newProjectEntry(resolvedPath, entry.getAccessRules(), entry.combineAccessRules(),
                        entry.getExtraAttributes(), entry.isExported());

                case IResource.FILE:
                    // internal binary archive
                    return JavaCore.newLibraryEntry(resolvedPath,
                        getResolvedVariablePath(entry.getSourceAttachmentPath(), usePreviousSession),
                        getResolvedVariablePath(entry.getSourceAttachmentRootPath(), usePreviousSession),
                        entry.getAccessRules(), entry.getExtraAttributes(), entry.isExported());

                case IResource.FOLDER:
                    // internal binary folder
                    return JavaCore.newLibraryEntry(resolvedPath,
                        getResolvedVariablePath(entry.getSourceAttachmentPath(), usePreviousSession),
                        getResolvedVariablePath(entry.getSourceAttachmentRootPath(), usePreviousSession),
                        entry.getAccessRules(), entry.getExtraAttributes(), entry.isExported());
            }
        }
        if (target instanceof File tf) {
            File externalFile = JavaModel.getFile(tf);
            if (externalFile != null) {
                // external binary archive
                return JavaCore.newLibraryEntry(resolvedPath,
                    getResolvedVariablePath(entry.getSourceAttachmentPath(), usePreviousSession),
                    getResolvedVariablePath(entry.getSourceAttachmentRootPath(), usePreviousSession),
                    entry.getAccessRules(), entry.getExtraAttributes(), entry.isExported());
            } else {
                // non-existing file
                if (resolvedPath.isAbsolute()) {
                    return JavaCore.newLibraryEntry(resolvedPath,
                        getResolvedVariablePath(entry.getSourceAttachmentPath(), usePreviousSession),
                        getResolvedVariablePath(entry.getSourceAttachmentRootPath(), usePreviousSession),
                        entry.getAccessRules(), entry.getExtraAttributes(), entry.isExported());
                }
            }
        }
        return null;
    }

    public IPath getResolvedVariablePath(IPath variablePath, boolean usePreviousSession) {

        if (variablePath == null)
            return null;
        int count = variablePath.segmentCount();
        if (count == 0)
            return null;

        // lookup variable
        String variableName = variablePath.segment(0);
        IPath resolvedPath = usePreviousSession
            ? getPreviousSessionVariable(variableName)
            : JavaCore.getClasspathVariable(variableName);
        if (resolvedPath == null)
            return null;

        // append path suffix
        if (count > 1) {
            resolvedPath = resolvedPath.append(variablePath.removeFirstSegments(1));
        }
        return resolvedPath;
    }

    public static ModuleSourcePathManager getModulePathManager() {
        JavaModelManager m = MANAGER;
        synchronized (m) {
            if (m.modulePathManager != null) {
                return m.modulePathManager;
            }
            return (m.modulePathManager = new ModuleSourcePathManager());
        }
    }

    /*
     * Returns all the working copies which have the given owner.
     * Adds the working copies of the primary owner if specified.
     * Returns null if it has none.
     */
    public ICompilationUnit[] getWorkingCopies(WorkingCopyOwner owner, boolean addPrimary) {
        synchronized (this.perWorkingCopyInfos) {
            ICompilationUnit[] primaryWCs = addPrimary && owner != DefaultWorkingCopyOwner.PRIMARY
                ? getWorkingCopies(DefaultWorkingCopyOwner.PRIMARY, false)
                : null;
            Map<CompilationUnit, PerWorkingCopyInfo> workingCopyToInfos = this.perWorkingCopyInfos.get(owner);
            if (workingCopyToInfos == null)
                return primaryWCs;
            int primaryLength = primaryWCs == null ? 0 : primaryWCs.length;
            int size = workingCopyToInfos.size(); // note size is > 0 otherwise pathToPerWorkingCopyInfos would be null
            ICompilationUnit[] result = new ICompilationUnit[primaryLength + size];
            int index = 0;
            if (primaryWCs != null) {
                for (int i = 0; i < primaryLength; i++) {
                    ICompilationUnit primaryWorkingCopy = primaryWCs[i];
                    ICompilationUnit workingCopy = new CompilationUnit((PackageFragment) primaryWorkingCopy.getParent(),
                        primaryWorkingCopy.getElementName(), owner);
                    if (!workingCopyToInfos.containsKey(workingCopy))
                        result[index++] = primaryWorkingCopy;
                }
                if (index != primaryLength)
                    System.arraycopy(result, 0, result = new ICompilationUnit[index + size], 0, index);
            }
            for (PerWorkingCopyInfo info : workingCopyToInfos.values()) {
                result[index++] = info.getWorkingCopy();
            }
            return result;
        }
    }

    public static boolean isJrt(IPath path) {
        return org.eclipse.jdt.internal.compiler.util.Util.isJrt(path.lastSegment());
    }

    public static boolean isJrt(String path) {
        return org.eclipse.jdt.internal.compiler.util.Util.isJrt(path);
    }

    public void verifyArchiveContent(IPath path) throws CoreException {
        // TODO: we haven't finalized what path the JRT is represented by. Don't attempt to validate it.
        if (isJrt(path)) {
            return;
        }
        if (isArchiveStateKnownToBeValid(path)) {
            return; // known to be valid
        }
        ZipFile file = getZipFile(path);
        closeZipFile(file);
    }

    /**
     * Returns the open ZipFile at the given path. If the ZipFile
     * does not yet exist, it is created, opened, and added to the cache
     * of open ZipFiles.
     *
     * The path must be a file system path if representing an external
     * zip/jar, or it must be an absolute workspace relative path if
     * representing a zip/jar inside the workspace.
     *
     * @exception CoreException If unable to create/open the ZipFile. The
     * cause will be a {@link ZipException} if the file was corrupt, a
     * {@link FileNotFoundException} if the file does not exist, or a
     * {@link IOException} if we were unable to read the file.
     */
    public ZipFile getZipFile(IPath path) throws CoreException {
        return getZipFile(path, true);
    }

    /**
     * For use in the JDT unit tests only. Used for testing error handling. Causes an
     * {@link IOException} to be thrown in {@link #getZipFile} whenever it attempts to
     * read a zip file.
     *
     * @noreference This field is not intended to be referenced by clients.
     */
    public static boolean throwIoExceptionsInGetZipFile = false;

    /** for tracing only **/
    private final ThreadLocal<Map<IPath, Deque<Instant>>> lastAccessByPath = ThreadLocal.withInitial(HashMap::new);
    /** for tracing only **/
    private final ThreadLocal<Instant> lastWarning = new ThreadLocal<>();

    private void traceZipAccessWarning(IPath path) {
        Instant now = Instant.now();
        Deque<Instant> lastAcesses
            = this.lastAccessByPath.get().compute(path, (p, l) -> (l == null) ? new ArrayDeque<>() : l);
        Instant recentAccess = lastAcesses.peekFirst();
        Instant lastAccess = lastAcesses.peekLast();
        lastAcesses.offerLast(now);
        if (lastAccess != null) {
            long elapsedMs = lastAccess.until(now, java.time.temporal.ChronoUnit.MILLIS);
            if (elapsedMs <= 100000) {
                trace(path + " opened again in this thread after " + elapsedMs + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        // only warn if there have recently multiple accesses, because it's common but not that bad to have 2 of them
        if (recentAccess != null && lastAcesses.size() > 2) {
            long elapsedMs = recentAccess.until(now, java.time.temporal.ChronoUnit.MILLIS);
            lastAcesses.pollFirst();
            Instant lastInstant = this.lastWarning.get();
            long elapsedWarningMs
                = lastInstant == null ? Long.MAX_VALUE : lastInstant.until(now, java.time.temporal.ChronoUnit.MILLIS);
            if (elapsedMs < 100 && elapsedWarningMs > 1000) {
                this.lastWarning.set(now);
                new Exception("Zipfile was opened multiple times wihtin " + elapsedMs + "ms in same thread " //$NON-NLS-1$ //$NON-NLS-2$
                    + Thread.currentThread() + ", consider caching: " + path) //$NON-NLS-1$
                        .printStackTrace();
            }
        }
    }

    public ZipFile getZipFile(IPath path, boolean checkInvalidArchiveCache) throws CoreException {
        if (checkInvalidArchiveCache) {
            isArchiveStateKnownToBeValid(path);
        }
        ZipCache zipCache;
        ZipFile zipFile;
        if ((zipCache = this.zipFiles.get()) != null && (zipFile = zipCache.getCache(path)) != null) {
            return zipFile;
        }
        File localFile = getLocalFile(path);
        try {
            if (ZIP_ACCESS_WARNING) {
                traceZipAccessWarning(path);
            }
            if (ZIP_ACCESS_VERBOSE) {
                trace("(" + Thread.currentThread() + ") [JavaModelManager.getZipFile(IPath)] Creating ZipFile on " //$NON-NLS-1$ //$NON-NLS-2$
                    + localFile);
            }
            if (throwIoExceptionsInGetZipFile) {
                throw new IOException();
            }
            zipFile = new ZipFile(localFile);
            if (zipCache != null) {
                zipCache.setCache(path, zipFile);
            }
            addInvalidArchive(path, ArchiveValidity.VALID); // remember its valid
            return zipFile;
        } catch (IOException e) {
            // file may exist but for some reason is inaccessible
            ArchiveValidity reason = ArchiveValidity.INVALID;
            addInvalidArchive(path, reason);
            int code = -1;
            if (e instanceof FileNotFoundException || e instanceof NoSuchFileException) {
                code = IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST;
            }
            throw new JavaModelException(new JavaModelStatus(code, e));
        }
    }

    public static File getLocalFile(IPath path) throws CoreException {
        File localFile;
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IResource file = root.findMember(path);
        if (file != null) {
            // internal resource
            URI location;
            if (file.getType() != IResource.FILE || (location = file.getLocationURI()) == null) {
                throw new CoreException(new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1,
                    Messages.bind(Messages.file_notFound, path.toString()), null));
            }
            localFile = Util.toLocalFile(location, null/* no progress availaible */);
            if (localFile == null)
                throw new CoreException(new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1,
                    Messages.bind(Messages.file_notFound, path.toString()), null));
        } else {
            // external resource -> it is ok to use toFile()
            localFile = path.toFile();
        }
        return localFile;
    }

    private boolean isArchiveStateKnownToBeValid(IPath path) {
        ArchiveValidity validity = getArchiveValidity(path);
        return validity != null && validity != ArchiveValidity.INVALID; // chance the file has become
                                                                        // accessible/readable now.
    }

    /*
     * Returns whether there is a temporary cache for the current thread.
     */
    public boolean hasTemporaryCache() {
        return this.temporaryCache.get() != null;
    }

    private Set<IJavaProject> getClasspathBeingResolved() {
        Set<IJavaProject> result = this.classpathsBeingResolved.get();
        if (result == null) {
            result = new HashSet<>();
            this.classpathsBeingResolved.set(result);
        }
        return result;
    }

    public boolean isClasspathBeingResolved(IJavaProject project) {
        return getClasspathBeingResolved().contains(project);
    }

    public boolean isNonChainingJar(IPath path) {
        return this.nonChainingJars != null && this.nonChainingJars.contains(path);
    }

    public ArchiveValidity getArchiveValidity(IPath path) {
        InvalidArchiveInfo invalidArchiveInfo;
        synchronized (this.invalidArchives) {
            invalidArchiveInfo = this.invalidArchives.get(path);
        }
        if (invalidArchiveInfo == null) {
            if (DEBUG_INVALID_ARCHIVES) {
                trace("JAR cache: UNKNOWN validity for " + path);  //$NON-NLS-1$
            }
            return null;
        }
        long now = System.currentTimeMillis();

        // If the TTL for this cache entry has expired, directly check whether the archive is still invalid.
        if (now > invalidArchiveInfo.evictionTimestamp) {
            try {
                ZipFile zipFile = getZipFile(path, false);
                closeZipFile(zipFile);
                removeFromInvalidArchiveCache(path);
                addInvalidArchive(path, ArchiveValidity.VALID); // update TTL
                return ArchiveValidity.VALID;
            } catch (CoreException e) {
                // Archive is still invalid, fall through to reporting it is invalid.
            }
            addInvalidArchive(path, ArchiveValidity.INVALID); // update TTL
            return ArchiveValidity.INVALID;
        }
        if (DEBUG_INVALID_ARCHIVES) {
            trace("JAR cache: " + invalidArchiveInfo.reason + " " + path);  //$NON-NLS-1$ //$NON-NLS-2$
        }
        return invalidArchiveInfo.reason;
    }

    public void removeFromInvalidArchiveCache(IPath path) {
        synchronized (this.invalidArchives) {
            InvalidArchiveInfo entry = this.invalidArchives.get(path);
            if (entry != null && entry.reason == ArchiveValidity.VALID) {
                if (DEBUG_INVALID_ARCHIVES) {
                    trace("JAR cache: keep VALID " + path);  //$NON-NLS-1$
                }
                return; // do not remove the VALID information
            }
            // If it transitioned to being valid then force an update to project caches.
            if (this.invalidArchives.remove(path) != null) {
                if (DEBUG_INVALID_ARCHIVES) {
                    trace("JAR cache: removed INVALID " + path);  //$NON-NLS-1$
                }
                try {
                    // Bug 455042: Force an update of the JavaProjectElementInfo project caches.
                    for (IJavaProject project : getJavaModel().getJavaProjects()) {
                        if (project.findPackageFragmentRoot(path) != null) {
                            ((JavaProject) project).resetCaches();
                        }
                    }
                } catch (JavaModelException e) {
                    Util.log(e, "Unable to retrieve the Java model."); //$NON-NLS-1$
                }
            }
        }
    }

    /**
     * Returns the cached value for whether the file referred to by <code>path</code> exists
     * and is a file, as determined by the return value of {@link File#isFile()}.
     */
    public boolean isExternalFile(IPath path) {
        if (this.externalFiles == null)
            return false;
        Boolean exists = this.externalFiles.get(path);
        return exists != null && exists;
    }

    public boolean knownToNotExistOnFileSystem(IPath path) {
        if (this.externalFiles == null)
            return false;
        Boolean exists = this.externalFiles.get(path);
        return exists != null && !exists;
    }

    /**
     * Returns whether the provided {@link IPath} appears to be an external file,
     * which is true if the path does not represent an internal resource, does not
     * exist on the file system, and does have a file extension (this is the definition
     * provided by {@link ExternalFoldersManager#isExternalFolderPath}).
     */
    public boolean isAssumedExternalFile(IPath path) {
        if (this.assumedExternalFiles == null) {
            return false;
        }
        return this.assumedExternalFiles.contains(path);
    }

    /**
     * Adds the provided {@link IPath} to the list of assumed external files.
     */
    public void addAssumedExternalFile(IPath path) {
        this.assumedExternalFiles.add(path);
    }

    public void setClasspathBeingResolved(IJavaProject project, boolean classpathIsResolved) {
        if (classpathIsResolved) {
            getClasspathBeingResolved().add(project);
        } else {
            getClasspathBeingResolved().remove(project);
        }
    }

    /**
     * Returns the info for this element without
     * disturbing the cache ordering.
     */
    protected synchronized IElementInfo peekAtInfo(IJavaElement element) {
        HashMap<IJavaElement, IElementInfo> tempCache = this.temporaryCache.get();
        if (tempCache != null) {
            IElementInfo result = tempCache.get(element);
            if (result != null) {
                return result;
            }
        }
        return this.cache.peekAtInfo(element);
    }

    /*
     * Puts the infos in the given map (keys are IJavaElements and values are JavaElementInfos)
     * in the Java model cache in an atomic way if the info is not already present in the cache.
     * If the info is already present in the cache, it depends upon the forceAdd parameter.
     * If forceAdd is false it just returns the existing info and if true, this element and it's children are closed and
     * then
     * this particular info is added to the cache.
     */
    protected synchronized IElementInfo putInfos(IJavaElement openedElement, IElementInfo newInfo, boolean forceAdd,
        Map<IJavaElement, IElementInfo> newElements) {
        // remove existing children as the are replaced with the new children contained in newElements
        IElementInfo existingInfo = this.cache.peekAtInfo(openedElement);
        if (existingInfo != null && !forceAdd) {
            // If forceAdd is false, then it could mean that the particular element
            // wasn't in cache at that point of time, but would have got added through
            // another thread. In that case, removing the children could remove it's own
            // children. So, we should not remove the children but return the already existing
            // info.
            // https://bugs.eclipse.org/bugs/show_bug.cgi?id=372687
            return existingInfo;
        }
        if (openedElement instanceof IParent) {
            closeChildren(existingInfo);
        }

        // Need to put any JarPackageFragmentRoot in first.
        // This is due to the way the LRU cache flushes entries.
        // When a JarPackageFragment is flushed from the LRU cache, the entire
        // jar is flushed by removing the JarPackageFragmentRoot and all of its
        // children (see ElementCache.close()). If we flush the JarPackageFragment
        // when its JarPackageFragmentRoot is not in the cache and the root is about to be
        // added (during the 'while' loop), we will end up in an inconsistent state.
        // Subsequent resolution against package in the jar would fail as a result.
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=102422
        // (theodora)
        for (Iterator<Entry<IJavaElement, IElementInfo>> it = newElements.entrySet().iterator(); it.hasNext();) {
            Entry<IJavaElement, IElementInfo> entry = it.next();
            IJavaElement element = entry.getKey();
            if (element instanceof JarPackageFragmentRoot) {
                IElementInfo info = entry.getValue();
                it.remove();
                this.cache.putInfo(element, info);
            }
        }

        for (Entry<IJavaElement, IElementInfo> entry : newElements.entrySet()) {
            this.cache.putInfo(entry.getKey(), entry.getValue());
        }
        return newInfo;
    }

    private void closeChildren(Object info) {
        if (info instanceof JavaElementInfo) {
            for (IJavaElement child : ((JavaElementInfo) info).getChildren()) {
                try {
                    ((JavaElement) child).close();
                } catch (JavaModelException e) {
                    // ignore
                }
            }
            for (IJavaElement child : ((JavaElementInfo) info).getExtendedChildren()) {
                try {
                    ((JavaElement) child).close();
                } catch (JavaModelException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * Remember the info for the jar binary type
     * 
     * @param info instanceof IBinaryType or {@link JavaModelCache#NON_EXISTING_JAR_TYPE_INFO}
     */
    protected synchronized void putJarTypeInfo(IJavaElement type, IElementInfo info) {
        this.cache.jarTypeCache.put(type, info);
    }

    /*
     * Removes all cached info for the given element (including all children)
     * from the cache.
     * Returns the info for the given element, or null if it was closed.
     */
    public synchronized Object removeInfoAndChildren(JavaElement element) throws JavaModelException {
        Object info = this.cache.peekAtInfo(element);
        if (info != null) {
            element.closing(info);
            if (element instanceof IParent) {
                closeChildren(info);
            }
            this.cache.removeInfo(element);
            return info;
        }
        return null;
    }

    void removeFromJarTypeCache(BinaryType type) {
        this.cache.removeFromJarTypeCache(type);
    }

    /*
     * Reset project options stored in info cache.
     */
    public void resetProjectOptions(JavaProject javaProject) {
        synchronized (this.perProjectInfos) { // use the perProjectInfo collection as its own lock
            IProject project = javaProject.getProject();
            PerProjectInfo info = this.perProjectInfos.get(project);
            if (info != null) {
                info.options = null;
            }
        }
    }

    /*
     * Reset project preferences stored in info cache.
     */
    public void resetProjectPreferences(JavaProject javaProject) {
        synchronized (this.perProjectInfos) { // use the perProjectInfo collection as its own lock
            IProject project = javaProject.getProject();
            PerProjectInfo info = this.perProjectInfos.get(project);
            if (info != null) {
                info.preferences = null;
            }
        }
    }

    /*
     * Resets the temporary cache for newly created elements to null.
     */
    public void resetTemporaryCache() {
        this.temporaryCache.remove();
    }

    public static void trace(String msg) {
        if (TRACE_TO_STDOUT) {
            System.out.println(msg);
        } else {
        }
    }

    public static void trace(String msg, Exception e) {
    }

    public synchronized IPath variableGet(String variableName) {
        // check initialization in progress first
        Set<String> initializations = variableInitializationInProgress();
        if (initializations.contains(variableName)) {
            return VARIABLE_INITIALIZATION_IN_PROGRESS;
        }
        return this.variables.get(variableName);
    }

    /*
     * Returns the set of variable names that are being initialized in the current thread.
     */
    private Set<String> variableInitializationInProgress() {
        Set<String> initializations = this.variableInitializationInProgress.get();
        if (initializations == null) {
            initializations = new HashSet<>();
            this.variableInitializationInProgress.set(initializations);
        }
        return initializations;
    }

    public synchronized void variablePut(String variableName, IPath variablePath) {

        // set/unset the initialization in progress
        Set<String> initializations = variableInitializationInProgress();
        if (variablePath == VARIABLE_INITIALIZATION_IN_PROGRESS) {
            initializations.add(variableName);
        } else {
            initializations.remove(variableName);

            // update cache - do not only rely on listener refresh
            if (variablePath == null) {
                // if path is null, record that the variable was removed to avoid asking the initializer to initialize
                // it again
                // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=112609
                this.variables.put(variableName, CP_ENTRY_IGNORE_PATH);
                // clean other variables caches
                this.variablesWithInitializer.remove(variableName);
                this.deprecatedVariables.remove(variableName);
            } else {
                this.variables.put(variableName, variablePath);
            }
            // discard obsoleted information about previous session
            this.previousSessionVariables.remove(variableName);
        }
    }

    /**
     * Get a cached access rule, or when the cache did not contain the rule, creates a new one.
     *
     * @param filePattern the file pattern this access rule should match
     * @param kind one of {@link IAccessRule#K_ACCESSIBLE}, {@link IAccessRule#K_DISCOURAGED},
     * or {@link IAccessRule#K_NON_ACCESSIBLE}, optionally combined with
     * {@link IAccessRule#IGNORE_IF_BETTER}
     * @return an access rule
     */
    public IAccessRule getAccessRule(IPath filePattern, int kind) {
        ClasspathAccessRule rule = new ClasspathAccessRule(filePattern, kind);
        return getFromCache(rule);
    }

    /**
     * Used only for loading rules from disk.
     */
    public ClasspathAccessRule getAccessRuleForProblemId(char[] filePattern, int problemId) {
        ClasspathAccessRule rule = new ClasspathAccessRule(filePattern, problemId);
        return getFromCache(rule);
    }

    private ClasspathAccessRule getFromCache(ClasspathAccessRule rule) {
        return DeduplicationUtil.internObject(rule);
    }

    private static final ThreadLocal<Boolean> readOnly = ThreadLocal.withInitial(() -> Boolean.FALSE);

    public static boolean isReadOnly() {
        return readOnly.get();
    }

    public static <T, E extends Exception> T cacheZipFiles(JavaCallable<T, E> callable) throws E {
        Object instance = new Object();
        try {
            getJavaModelManager().cacheZipFiles(instance);
            return callable.call();
        } finally {
            getJavaModelManager().flushZipFiles(instance);
        }
    }
}
