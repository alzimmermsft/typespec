/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *     Stephan Herrmann <stephan@cs.tu-berlin.de> - inconsistent initialization of classpath container backed by external class folder, see https://bugs.eclipse.org/320618
 *     Thirumala Reddy Mutchukota <thirumala@google.com> - Contribution to bug: https://bugs.eclipse.org/bugs/show_bug.cgi?id=411423
 *     Terry Parker <tparker@google.com> - [performance] Low hit rates in JavaModel caches - https://bugs.eclipse.org/421165
 *     Andrey Loskutov <loskutov@gmx.de> - ExternalFoldersManager.RefreshJob interrupts auto build job - https://bugs.eclipse.org/476059
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IFolder;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IWorkspaceRoot;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.ResourcesPlugin;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IClasspathEntry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.Util;
import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ExternalFoldersManager {
    private static final boolean WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");  //$NON-NLS-1$//$NON-NLS-2$
    private static final String EXTERNAL_PROJECT_NAME = ".org.eclipse.jdt.core.external.folders"; //$NON-NLS-1$
    private static final String LINKED_FOLDER_NAME = ".link"; //$NON-NLS-1$
    private volatile Map<IPath, IFolder> folders;
    private Set<IPath> pendingFolders; // subset of keys of 'folders', for which linked folders haven't been created
                                       // yet.
    private final AtomicInteger counter = new AtomicInteger(0);
    /* Singleton instance */
    private static final ExternalFoldersManager INSTANCE = new ExternalFoldersManager();

    private ExternalFoldersManager() {
        // Prevent instantiation
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=377806
    }

    public static ExternalFoldersManager getExternalFoldersManager() {
        return INSTANCE;
    }

    /**
     * Returns <code>true</code> if the provided path is a folder external to the project.
     * The path is expected to be one matching the {@link IClasspathEntry#CPE_LIBRARY} case in
     * {@link IClasspathEntry#getPath()} definition.
     */
    public static boolean isExternalFolderPath(IPath externalPath) {
        if (externalPath == null || externalPath.isEmpty()) {
            return false;
        }

        JavaModelManager manager = JavaModelManager.getJavaModelManager();
        if (manager.isExternalFile(externalPath) || manager.isAssumedExternalFile(externalPath)) {
            return false;
        }
        if (!externalPath.isAbsolute() || (WINDOWS && (externalPath.getDevice() == null && !externalPath.isUNC()))) {
            // can be only project relative path
            return false;
        }
        // Test if this an absolute path in local file system (not the workspace path)
        File externalFolder = externalPath.toFile();
        if (Files.isRegularFile(externalFolder.toPath())) {
            manager.addExternalFile(externalPath, true);
            return false;
        }
        if (Files.isDirectory(externalFolder.toPath())) {
            return true;
        }
        // this can be now only full workspace path or an external path to a not existing file or folder
        if (isInternalFilePath(externalPath)) {
            return false;
        }
        if (isInternalContainerPath(externalPath)) {
            return false;
        }
        // From here on the legacy code assumes that not existing resource must be external.
        // We just follow the old assumption.
        if (externalPath.getFileExtension() != null/* likely a .jar, .zip, .rar or other file */) {
            manager.addAssumedExternalFile(externalPath);
            // assume not existing external (?) file (?) (can also be a folder with dotted name!)
            return false;
        }
        // assume not existing external (?) folder (?)
        return true;
    }

    /**
     * @param path full absolute workspace path
     */
    private static boolean isInternalFilePath(IPath path) {
        IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
        // in case this is full workspace path it should start with project segment
        if (path.segmentCount() > 1 && wsRoot.getFile(path).exists()) {
            return true;
        }
        return false;
    }

    /**
     * @param path full absolute workspace path
     */
    private static boolean isInternalContainerPath(IPath path) {
        IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
        // in case this is full workspace path it should start with project segment
        int segmentCount = path.segmentCount();
        if (segmentCount == 1 && wsRoot.getProject(path.segment(0)).exists()) {
            return true;
        }
        if (segmentCount > 1 && wsRoot.getFolder(path).exists()) {
            return true;
        }
        return false;
    }

    public static boolean isInternalPathForExternalFolder(IPath resourcePath) {
        return EXTERNAL_PROJECT_NAME.equals(resourcePath.segment(0));
    }

    public IFolder addFolder(IPath externalFolderPath, boolean scheduleForCreation) {
        return addFolder(externalFolderPath, getExternalFoldersProject(), scheduleForCreation);
    }

    private IFolder addFolder(IPath externalFolderPath, IProject externalFoldersProject, boolean scheduleForCreation) {
        Map<IPath, IFolder> knownFolders = getFolders();

        IFolder existing;
        synchronized (this) {
            existing = knownFolders.get(externalFolderPath);
            if (existing != null) {
                return existing;
            }
        }

        IFolder result;
        do {
            result = externalFoldersProject.getFolder(LINKED_FOLDER_NAME + this.counter.incrementAndGet());
        } while (result.exists());

        synchronized (this) {
            if (scheduleForCreation) {
                if (this.pendingFolders == null)
                    this.pendingFolders = new LinkedHashSet<>();
                this.pendingFolders.add(externalFolderPath);
            }
            existing = knownFolders.get(externalFolderPath);
            if (existing != null) {
                return existing;
            }
            knownFolders.put(externalFolderPath, result);
        }
        return result;
    }

    public IProject getExternalFoldersProject() {
        return ResourcesPlugin.getWorkspace().getRoot().getProject(EXTERNAL_PROJECT_NAME);
    }

    /*
     * Attempt to open the given project (assuming it exists).
     * If failing to open, make all attempts to recreate the missing pieces.
     */
    private void openExternalFoldersProject(IProject project, IProgressMonitor monitor) throws CoreException {
    }

    public IFolder getFolder(IPath externalFolderPath) {
        return getFolders().get(externalFolderPath);
    }

    Map<IPath, IFolder> getFolders() {
        if (this.folders == null) {
            Map<IPath, IFolder> tempFolders = new LinkedHashMap<>();
            IProject project = getExternalFoldersProject();
            try {
                if (!project.isAccessible()) {
                    if (project.exists()) {
                        // workspace was moved (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=252571 )
                        openExternalFoldersProject(project, null/* no progress */);
                    } else {
                        // if project doesn't exist, do not open and recreate it as it means that there are no external
                        // folders
                        return this.folders = Collections.synchronizedMap(tempFolders);
                    }
                }
                IResource[] members = project.members();
                for (IResource member : members) {
                    if (member.getType() == IResource.FOLDER
                        && member.isLinked()
                        && member.getName().startsWith(LINKED_FOLDER_NAME)) {
                        IPath externalFolderPath = member.getLocation();
                        tempFolders.put(externalFolderPath, (IFolder) member);
                    }
                }
            } catch (CoreException e) {
                Util.log(e, "Exception while initializing external folders"); //$NON-NLS-1$
            }
            synchronized (this) {
                if (this.folders == null) {
                    this.folders = Collections.synchronizedMap(tempFolders);
                }
            }
        }
        return this.folders;
    }

}
