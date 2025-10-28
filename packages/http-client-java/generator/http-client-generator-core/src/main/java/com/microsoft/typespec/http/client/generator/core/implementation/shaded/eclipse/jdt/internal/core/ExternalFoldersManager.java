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
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IProjectDescription;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResourceStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IWorkspaceRoot;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.ResourcesPlugin;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IClasspathEntry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaCore;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.DeltaProcessor.RootInfo;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ExternalFoldersManager {
	private static final boolean WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");  //$NON-NLS-1$//$NON-NLS-2$
	private static final String EXTERNAL_PROJECT_NAME = ".org.eclipse.jdt.core.external.folders"; //$NON-NLS-1$
	private static final String LINKED_FOLDER_NAME = ".link"; //$NON-NLS-1$
	private volatile Map<IPath, IFolder> folders;
	private Set<IPath> pendingFolders; // subset of keys of 'folders', for which linked folders haven't been created yet.
	private final AtomicInteger counter = new AtomicInteger(0);
	/* Singleton instance */
	private static final ExternalFoldersManager INSTANCE= new ExternalFoldersManager();

    private ExternalFoldersManager() {
		// Prevent instantiation
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=377806
    }

	public static ExternalFoldersManager getExternalFoldersManager() {
		return INSTANCE;
	}

	/**
	 * Returns a set of external paths to external folders referred to on the given classpath.
	 * Returns <code>null</code> if there are none.
	 */
	public static Set<IPath> getExternalFolders(IClasspathEntry[] classpath) {
		if (classpath == null)
			return null;
		Set<IPath> folders = null;
		for (IClasspathEntry entry : classpath) {
			if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
				IPath entryPath = entry.getPath();
				if (isExternalFolderPath(entryPath)) {
					if (folders == null)
						folders = new LinkedHashSet<>();
					folders.add(entryPath);
				}
				IPath attachmentPath = entry.getSourceAttachmentPath();
				if (isExternalFolderPath(attachmentPath)) {
					if (folders == null)
						folders = new LinkedHashSet<>();
					folders.add(attachmentPath);
				}
			}
		}
		return folders;
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
		if (!externalPath.isAbsolute()
				|| (WINDOWS && (externalPath.getDevice() == null && !externalPath.isUNC()))) {
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
		if (externalPath.getFileExtension() != null/*likely a .jar, .zip, .rar or other file*/) {
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
		if(path.segmentCount() > 1 && wsRoot.getFile(path).exists()) {
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
		if(segmentCount == 1 && wsRoot.getProject(path.segment(0)).exists()) {
			return true;
		}
		if(segmentCount > 1 && wsRoot.getFolder(path).exists()) {
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

	/**
	 * Try to remove the argument from the list of folders pending for creation.
	 * @param externalPath to link to
	 * @return true if the argument was found in the list of pending folders and could be removed from it.
	 */
	public synchronized boolean removePendingFolder(Object externalPath) {
		if (this.pendingFolders == null)
			return false;
		return this.pendingFolders.remove(externalPath);
	}

	public IFolder createLinkFolder(IPath externalFolderPath, boolean refreshIfExistAlready, IProgressMonitor monitor) throws CoreException {
		IProject externalFoldersProject = createExternalFoldersProject(monitor); // run outside synchronized as this can create a resource
		return createLinkFolder(externalFolderPath, refreshIfExistAlready, externalFoldersProject, monitor);
	}

	private IFolder createLinkFolder(IPath externalFolderPath, boolean refreshIfExistAlready,
									IProject externalFoldersProject, IProgressMonitor monitor) throws CoreException {

		IFolder result = addFolder(externalFolderPath, externalFoldersProject, false);
		if (!result.exists()) {
			try {
				result.createLink(externalFolderPath, IResource.ALLOW_MISSING_LOCAL, monitor);
			} catch (CoreException e) {
				// If we managed to create the folder in the meantime, don't complain
				if (!result.exists()) {
					throw e;
				}
			}
		} else if (refreshIfExistAlready) {
			result.refreshLocal(IResource.DEPTH_INFINITE,  monitor);
		}
		return result;
	}

    public void cleanUp(IProgressMonitor monitor) throws CoreException {
		List<Entry<IPath, IFolder>> toDelete = getFoldersToCleanUp(monitor);
		if (toDelete == null)
			return;
		for (Entry<IPath, IFolder> entry : toDelete) {
			IFolder folder = entry.getValue();
			folder.delete(true, monitor);
			IPath key = entry.getKey();
			this.folders.remove(key);
		}
		IProject project = getExternalFoldersProject();
		if (project.isAccessible() && project.members().length == 1/*remaining member is .project*/)
			project.delete(true, monitor);
	}

	private List<Entry<IPath, IFolder>> getFoldersToCleanUp(IProgressMonitor monitor) throws CoreException {
		DeltaProcessingState state = JavaModelManager.getDeltaState();
		Map<IPath, RootInfo> roots = state.roots;
		Map<IPath, IPath> sourceAttachments = state.sourceAttachments;
		if (roots == null && sourceAttachments == null)
			return null;
		Map<IPath, IFolder> knownFolders = getFolders();
		List<Entry<IPath, IFolder>> result = null;
		synchronized (knownFolders) {
			for (Entry<IPath, IFolder> entry : knownFolders.entrySet()) {
				IPath path = entry.getKey();
				if ((roots != null && !roots.containsKey(path))
						&& (sourceAttachments != null && !sourceAttachments.containsKey(path))) {
					if (entry.getValue() != null) {
						if (result == null)
							result = new ArrayList<>();
						result.add(entry);
					}
				}
			}
		}
		return result;
	}

	public IProject getExternalFoldersProject() {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(EXTERNAL_PROJECT_NAME);
	}

	public IProject createExternalFoldersProject(IProgressMonitor monitor) throws CoreException {
		IProject project = getExternalFoldersProject();
		if (!project.isAccessible()) {
			if (!project.exists()) {
				createExternalFoldersProject(project, monitor);
			}
			openExternalFoldersProject(project, monitor);
		}
		return project;
	}

	/*
	 * Attempt to open the given project (assuming it exists).
	 * If failing to open, make all attempts to recreate the missing pieces.
	 */
	private void openExternalFoldersProject(IProject project, IProgressMonitor monitor) throws CoreException {
		try {
			project.open(monitor);
		} catch (CoreException e1) {
			if (e1.getStatus().getCode() == IResourceStatus.FAILED_READ_METADATA) {
				// workspace was moved
				// (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=241400 and https://bugs.eclipse.org/bugs/show_bug.cgi?id=252571 )
				project.delete(false/*don't delete content*/, true/*force*/, monitor);
				createExternalFoldersProject(project, monitor);
			} else {
				// .project or folder on disk have been deleted, recreate them
				IPath stateLocation = JavaCore.getPlugin().getStateLocation();
				IPath projectPath = stateLocation.append(EXTERNAL_PROJECT_NAME);
				try {
					Files.createDirectories(projectPath.toFile().toPath());
					try (FileOutputStream output = new FileOutputStream(projectPath.append(".project").toOSString())){ //$NON-NLS-1$
				        output.write((
				        		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + //$NON-NLS-1$
				        		"<projectDescription>\n" + //$NON-NLS-1$
				        		"	<name>" + EXTERNAL_PROJECT_NAME + "</name>\n" + //$NON-NLS-1$ //$NON-NLS-2$
				        		"	<comment></comment>\n" + //$NON-NLS-1$
				        		"	<projects>\n" + //$NON-NLS-1$
				        		"	</projects>\n" + //$NON-NLS-1$
				        		"	<buildSpec>\n" + //$NON-NLS-1$
				        		"	</buildSpec>\n" + //$NON-NLS-1$
				        		"	<natures>\n" + //$NON-NLS-1$
				        		"	</natures>\n" + //$NON-NLS-1$
				        		"</projectDescription>").getBytes()); //$NON-NLS-1$
				    }
				} catch (IOException e) {
					// fallback to re-creating the project
					project.delete(false/*don't delete content*/, true/*force*/, monitor);
					createExternalFoldersProject(project, monitor);
				}
			}
			project.open(monitor);
		}
	}


	private void createExternalFoldersProject(IProject project, IProgressMonitor monitor) throws CoreException {
		IProjectDescription desc = project.getWorkspace().newProjectDescription(project.getName());
		IPath stateLocation = JavaCore.getPlugin().getStateLocation();
		desc.setLocation(stateLocation.append(EXTERNAL_PROJECT_NAME));
		try {
			project.create(desc, IResource.HIDDEN, monitor);
		} catch (CoreException e) {
			// If we managed to create the project in the meantime, don't complain
			if (!project.exists()) {
				throw e;
			}
		}
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
						openExternalFoldersProject(project, null/*no progress*/);
					} else {
						// if project doesn't exist, do not open and recreate it as it means that there are no external folders
						return this.folders = Collections.synchronizedMap(tempFolders);
					}
				}
				IResource[] members = project.members();
				for (IResource member : members) {
					if (member.getType() == IResource.FOLDER && member.isLinked() && member.getName().startsWith(LINKED_FOLDER_NAME)) {
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
