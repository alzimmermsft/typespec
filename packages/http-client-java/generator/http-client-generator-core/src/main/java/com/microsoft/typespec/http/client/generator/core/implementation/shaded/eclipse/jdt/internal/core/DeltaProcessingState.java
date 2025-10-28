/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResourceChangeListener;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Path;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IClasspathEntry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IElementChangedListener;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaModel;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaCore;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaModelException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.DeltaProcessor.RootInfo;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.Util;

/**
 * Keep the global states used during Java element delta processing.
 */
public class DeltaProcessingState implements IResourceChangeListener {

	/*
	 * Collection of listeners for Java element deltas
	 */
	public IElementChangedListener[] elementChangedListeners = new IElementChangedListener[5];
	public int[] elementChangedListenerMasks = new int[5];
	public int elementChangedListenerCount = 0;

	/*
	 * Collection of pre Java resource change listeners
	 */
	public IResourceChangeListener[] preResourceChangeListeners = new IResourceChangeListener[1];
	public int[] preResourceChangeEventMasks = new int[1];
	public int preResourceChangeListenerCount = 0;

	/*
	 * The delta processor for the current thread.
	 */
	private final ThreadLocal<DeltaProcessor> deltaProcessors = new ThreadLocal<>();

    /* A table from IPath (from a classpath entry) to DeltaProcessor.RootInfo */
	public Map<IPath, RootInfo> roots = new LinkedHashMap<>();

	/* A table from IPath (from a classpath entry) to ArrayList of DeltaProcessor.RootInfo
	 * Used when an IPath corresponds to more than one root */
	public Map<IPath, List<RootInfo>> otherRoots = new HashMap<>();

	/* A table from IPath (from a classpath entry) to DeltaProcessor.RootInfo
	 * from the last time the delta processor was invoked. */
	public Map<IPath, RootInfo> oldRoots = new LinkedHashMap<>();

	/* A table from IPath (from a classpath entry) to ArrayList of DeltaProcessor.RootInfo
	 * from the last time the delta processor was invoked.
	 * Used when an IPath corresponds to more than one root */
	public Map<IPath, List<RootInfo>> oldOtherRoots = new HashMap<>();

	/* A table from IPath (a source attachment path from a classpath entry) to IPath (a root path) */
	public Map<IPath, IPath> sourceAttachments = new HashMap<>();

	/* A table from IJavaProject to IJavaProject[] (the list of direct dependent of the key) */
	public Map<IJavaProject, IJavaProject[]> projectDependencies = new HashMap<>();

	/* Whether the roots tables should be recomputed */
	public boolean rootsAreStale = true;

	/* Threads that are currently running initializeRoots() */
	private final Set<Thread> initializingThreads = Collections.synchronizedSet(new HashSet<>());

	/* A table from file system absoulte path (String) to timestamp (Long) */
	public Hashtable<IPath, Long> externalTimeStamps;

	/*
	 * Map from IProject to ClasspathChange
	 * Note these changes need to be kept on the delta processing state to ensure we don't loose them
	 * (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=271102 Java model corrupt after switching target platform)
	 */
	private Map<IProject, ClasspathChange> classpathChanges = new LinkedHashMap<>();

	/* A table from JavaProject to ClasspathValidation */
	private final Map<JavaProject, ClasspathValidation> classpathValidations = new LinkedHashMap<>();

	/* A table from JavaProject to ProjectReferenceChange */
	private Set<IJavaProject> projectReferenceChanges = new LinkedHashSet<>();

	/* A table from JavaProject to ExternalFolderChange */
	private final Map<JavaProject, ExternalFolderChange> externalFolderChanges = new LinkedHashMap<>();

	/**
	 * Workaround for bug 15168 circular errors not reported
	 * This is a cache of the projects before any project addition/deletion has started.
	 */
	private Set<String> javaProjectNamesCache;

	/*
	 * A list of IJavaElement used as a scope for external archives refresh during POST_CHANGE.
	 * This is null if no refresh is needed.
	 */
	private Set<IJavaElement> externalElementsToRefresh;
	private final Object mutex = new Object();

	/*
	 * Need to clone defensively the listener information, in case some listener is reacting to some notification iteration by adding/changing/removing
	 * any of the other (for example, if it deregisters itself).
	 */
	public synchronized void addElementChangedListener(IElementChangedListener listener, int eventMask) {
		for (int i = 0; i < this.elementChangedListenerCount; i++){
			if (this.elementChangedListeners[i] == listener){

				// only clone the masks, since we could be in the middle of notifications and one listener decide to change
				// any event mask of another listeners (yet not notified).
				int cloneLength = this.elementChangedListenerMasks.length;
				System.arraycopy(this.elementChangedListenerMasks, 0, this.elementChangedListenerMasks = new int[cloneLength], 0, cloneLength);
				this.elementChangedListenerMasks[i] |= eventMask; // could be different
				return;
			}
		}
		// may need to grow, no need to clone, since iterators will have cached original arrays and max boundary and we only add to the end.
		int length;
		if ((length = this.elementChangedListeners.length) == this.elementChangedListenerCount){
			System.arraycopy(this.elementChangedListeners, 0, this.elementChangedListeners = new IElementChangedListener[length*2], 0, length);
			System.arraycopy(this.elementChangedListenerMasks, 0, this.elementChangedListenerMasks = new int[length*2], 0, length);
		}
		this.elementChangedListeners[this.elementChangedListenerCount] = listener;
		this.elementChangedListenerMasks[this.elementChangedListenerCount] = eventMask;
		this.elementChangedListenerCount++;
	}

	/*
	 * Adds the given element to the list of elements used as a scope for external jars refresh.
	 */
	public synchronized void addForRefresh(IJavaElement externalElement) {
		if (this.externalElementsToRefresh == null) {
			this.externalElementsToRefresh = new LinkedHashSet<>();
		}
		this.externalElementsToRefresh.add(externalElement);
	}

	public synchronized void addPreResourceChangedListener(IResourceChangeListener listener, int eventMask) {
		for (int i = 0; i < this.preResourceChangeListenerCount; i++){
			if (this.preResourceChangeListeners[i] == listener) {
				this.preResourceChangeEventMasks[i] |= eventMask;
				return;
			}
		}
		// may need to grow, no need to clone, since iterators will have cached original arrays and max boundary and we only add to the end.
		int length;
		if ((length = this.preResourceChangeListeners.length) == this.preResourceChangeListenerCount) {
			System.arraycopy(this.preResourceChangeListeners, 0, this.preResourceChangeListeners = new IResourceChangeListener[length*2], 0, length);
			System.arraycopy(this.preResourceChangeEventMasks, 0, this.preResourceChangeEventMasks = new int[length*2], 0, length);
		}
		this.preResourceChangeListeners[this.preResourceChangeListenerCount] = listener;
		this.preResourceChangeEventMasks[this.preResourceChangeListenerCount] = eventMask;
		this.preResourceChangeListenerCount++;
	}

	public DeltaProcessor getDeltaProcessor() {
		DeltaProcessor deltaProcessor = this.deltaProcessors.get();
		if (deltaProcessor != null) return deltaProcessor;
		deltaProcessor = new DeltaProcessor(this, JavaModelManager.getJavaModelManager());
		this.deltaProcessors.set(deltaProcessor);
		return deltaProcessor;
	}

	public ClasspathChange addClasspathChange(IProject project, IClasspathEntry[] oldRawClasspath, IPath oldOutputLocation, IClasspathEntry[] oldResolvedClasspath) {
		synchronized (this.mutex) {
			ClasspathChange change = this.classpathChanges.get(project);
			if (change == null) {
				change = new ClasspathChange((JavaProject) JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject(project), oldRawClasspath, oldOutputLocation, oldResolvedClasspath);
				this.classpathChanges.put(project, change);
			} else {
				if (change.oldRawClasspath == null)
					change.oldRawClasspath = oldRawClasspath;
				if (change.oldOutputLocation == null)
					change.oldOutputLocation = oldOutputLocation;
				if (change.oldResolvedClasspath == null)
					change.oldResolvedClasspath = oldResolvedClasspath;
			}
			return change;
		}
	}

    public synchronized ClasspathValidation addClasspathValidation(JavaProject project) {
        return this.classpathValidations.computeIfAbsent(project, ClasspathValidation::new);
	}

	public synchronized void addExternalFolderChange(JavaProject project, IClasspathEntry[] oldResolvedClasspath) {
        this.externalFolderChanges.computeIfAbsent(project, p -> new ExternalFolderChange(p, oldResolvedClasspath));
    }

	public synchronized void addProjectReferenceChange(IJavaProject project) {
		this.projectReferenceChanges.add(project);
	}

	public void initializeRoots(boolean initAfterLoad) {

		// recompute root infos only if necessary
		RootInfos rootInfos = null;
		if (this.rootsAreStale) {
			Thread currentThread = Thread.currentThread();
			boolean addedCurrentThread = false;
			try {
				// if reentering initialization (through a container initializer for example) no need to compute roots again
				// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=47213
				if (!this.initializingThreads.add(currentThread)) return;
				addedCurrentThread = true;

				// all classpaths in the workspace are going to be resolved
				// ensure that containers are initialized in one batch
				JavaModelManager.getJavaModelManager().forceBatchInitializations(initAfterLoad);

				rootInfos = getRootInfos(/*don't use previous session values*/);

			} finally {
				if (addedCurrentThread) {
					this.initializingThreads.remove(currentThread);
				}
			}
		}
		synchronized(this) {
			this.oldRoots = this.roots;
			this.oldOtherRoots = this.otherRoots;
			if (this.rootsAreStale && rootInfos != null) { // double check again
				this.roots = rootInfos.roots;
				this.otherRoots = rootInfos.otherRoots;
				this.sourceAttachments = rootInfos.sourceAttachments;
				this.projectDependencies = rootInfos.projectDependencies;
				this.rootsAreStale = false;
			}
		}
	}

    private RootInfos getRootInfos() {
		RootInfos ri = new RootInfos();

		IJavaModel model = JavaModelManager.getJavaModelManager().getJavaModel();
		IJavaProject[] projects;
		try {
			projects = model.getJavaProjects();
		} catch (JavaModelException e) {
			// nothing can be done
			return null;
		}
		for (IJavaProject p : projects) {
			JavaProject project = (JavaProject) p;
			IClasspathEntry[] classpath;
			try {
                classpath = project.getResolvedClasspath();
            } catch (JavaModelException e) {
				// continue with next project
				continue;
			}
			for (IClasspathEntry entry : classpath) {
				if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
					IJavaProject key = model.getJavaProject(entry.getPath().segment(0)); // TODO (jerome) reuse handle
					IJavaProject[] dependents = ri.projectDependencies.get(key);
					if (dependents == null) {
						dependents = new IJavaProject[] {project};
					} else {
						int dependentsLength = dependents.length;
						System.arraycopy(dependents, 0, dependents = new IJavaProject[dependentsLength+1], 0, dependentsLength);
						dependents[dependentsLength] = project;
					}
					ri.projectDependencies.put(key, dependents);
					continue;
				}

				// root path
				IPath path = entry.getPath();
				if (ri.roots.get(path) == null) {
					ri.roots.put(path, new RootInfo(project, path, ((ClasspathEntry)entry).fullInclusionPatternChars(), ((ClasspathEntry)entry).fullExclusionPatternChars(), entry));
				} else {
                    List<RootInfo> rootList = ri.otherRoots.computeIfAbsent(path, k -> new ArrayList<>());
                    rootList.add(new RootInfo(project, path, ((ClasspathEntry)entry).fullInclusionPatternChars(), ((ClasspathEntry)entry).fullExclusionPatternChars(), entry));
				}

				// source attachment path
				if (entry.getEntryKind() != IClasspathEntry.CPE_LIBRARY) continue;
				String propertyString = null;
				try {
					propertyString = Util.getSourceAttachmentProperty(path);
				} catch (JavaModelException e) {
					if (JavaModelManager.VERBOSE) {
						JavaModelManager.trace("", e); //$NON-NLS-1$
					}
				}
				IPath sourceAttachmentPath;
				if (propertyString != null) {
					int index= propertyString.lastIndexOf(PackageFragmentRoot.ATTACHMENT_PROPERTY_DELIMITER);
					sourceAttachmentPath = (index < 0) ?  new Path(propertyString) : new Path(propertyString.substring(0, index));
				} else {
					sourceAttachmentPath = entry.getSourceAttachmentPath();
				}
				if (sourceAttachmentPath != null) {
					ri.sourceAttachments.put(sourceAttachmentPath, path);
				}
			}
		}
		return ri;
	}

    public synchronized void removeElementChangedListener(IElementChangedListener listener) {

		for (int i = 0; i < this.elementChangedListenerCount; i++){

			if (this.elementChangedListeners[i] == listener){

				// need to clone defensively since we might be in the middle of listener notifications (#fire)
				int length = this.elementChangedListeners.length;
				IElementChangedListener[] newListeners = new IElementChangedListener[length];
				System.arraycopy(this.elementChangedListeners, 0, newListeners, 0, i);
				int[] newMasks = new int[length];
				System.arraycopy(this.elementChangedListenerMasks, 0, newMasks, 0, i);

				// copy trailing listeners
				int trailingLength = this.elementChangedListenerCount - i - 1;
				if (trailingLength > 0){
					System.arraycopy(this.elementChangedListeners, i+1, newListeners, i, trailingLength);
					System.arraycopy(this.elementChangedListenerMasks, i+1, newMasks, i, trailingLength);
				}

				// update manager listener state (#fire need to iterate over original listeners through a local variable to hold onto
				// the original ones)
				this.elementChangedListeners = newListeners;
				this.elementChangedListenerMasks = newMasks;
				this.elementChangedListenerCount--;
				return;
			}
		}
	}

	public Hashtable<IPath, Long> getExternalLibTimeStamps() {
		if (this.externalTimeStamps == null) {
			Hashtable<IPath, Long> timeStamps = new Hashtable<>();
			File timestampsFile = getTimeStampsFile();
			try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(timestampsFile)))) {
				int size = in.readInt();
				while (size-- > 0) {
					String key = in.readUTF();
					long timestamp = in.readLong();
					timeStamps.put(Path.fromPortableString(key), Long.valueOf(timestamp));
				}
			} catch (IOException e) {
				if (timestampsFile.exists())
					Util.log(e, "Unable to read external time stamps"); //$NON-NLS-1$
			}
			this.externalTimeStamps = timeStamps;
		}
		return this.externalTimeStamps;
	}

	public IJavaProject findJavaProject(String name) {
		if (getOldJavaProjecNames().contains(name))
			return JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject(name);
		return null;
	}

	/*
	 * Workaround for bug 15168 circular errors not reported
	 * Returns the list of java projects before resource delta processing
	 * has started.
	 */
	public synchronized Set<String> getOldJavaProjecNames() {
		if (this.javaProjectNamesCache == null) {
			IJavaProject[] projects;
			try {
				projects = JavaModelManager.getJavaModelManager().getJavaModel().getJavaProjects();
			} catch (JavaModelException e) {
				return this.javaProjectNamesCache;
			}
			HashSet<String> result = new LinkedHashSet<>();
			for (IJavaProject project : projects) {
				result.add(project.getElementName());
			}
			return this.javaProjectNamesCache = result;
		}
		return this.javaProjectNamesCache;
	}

    private File getTimeStampsFile() {
		return JavaCore.getPlugin().getStateLocation().append("externalLibsTimeStamps").toFile(); //$NON-NLS-1$
	}

    private static final class RootInfos {
		final Map<IPath, RootInfo> roots;
		final Map<IPath, List<RootInfo>> otherRoots;
		final Map<IPath, IPath> sourceAttachments;
		final Map<IJavaProject, IJavaProject[]> projectDependencies;

		public RootInfos() {
			this.roots = new LinkedHashMap<>();
			this.otherRoots = new HashMap<>();
			this.sourceAttachments = new HashMap<>();
			this.projectDependencies = new HashMap<>();
		}
	}
}
