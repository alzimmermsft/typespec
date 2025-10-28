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
 *     Terry Parker <tparker@google.com> - DeltaProcessor exhibits O(N^2) behavior, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=354332
 *     Terry Parker <tparker@google.com> - DeltaProcessor misses state changes in archive files, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=357425
 *     Terry Parker <tparker@google.com> - [performance] Low hit rates in JavaModel caches - https://bugs.eclipse.org/421165
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResourceDelta;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.ILog;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.ISafeRunnable;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Path;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.SafeRunner;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.ElementChangedEvent;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IClasspathAttribute;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IClasspathEntry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.ICompilationUnit;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IElementChangedListener;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaElementDelta;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IPackageFragmentRoot;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.search.AbstractSearchScope;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.search.JavaWorkspaceScope;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.JavaModelManager.trace;

/**
 * This class is used by <code>JavaModelManager</code> to convert
 * <code>IResourceDelta</code>s into <code>IJavaElementDelta</code>s.
 * It also does some processing on the <code>JavaElement</code>s involved
 * (e.g. closing them or updating classpaths).
 * <p>
 * High level summary of what the delta processor does:
 * <ul>
 * <li>reacts to resource deltas</li>
 * <li>fires corresponding Java element deltas</li>
 * <li>deltas also contain non-Java resources changes</li>
 * <li>updates the model to reflect the Java element changes</li>
 * <li>notifies type hierarchies of the changes</li>
 * <li>triggers indexing of the changed elements</li>
 * <li>refresh external archives (delta, model update, indexing)</li>
 * <li>is thread safe (one delta processor instance per thread, see DeltaProcessingState#resourceChanged(...))</li>
 * <li>handles .classpath changes (updates package fragment roots, update project references, validate classpath (.classpath format,
 * 		resolved classpath, cycles))</li>
 * </ul>
 */
public class DeltaProcessor {

    /*
	 * An object to hold information about IPackageFragmentRoots (which correspond to
	 * individual classpath entry items, e.g., a java/javatests source root or library
	 * archive jar.)
	 */
	public static class RootInfo {
		final char[][] inclusionPatterns;
		final char[][] exclusionPatterns;
		final public JavaProject project;
		final IPath rootPath;
		final int entryKind;
		final IClasspathAttribute[] extraAttributes;
		IPackageFragmentRoot root;
		IPackageFragmentRoot cache;

		RootInfo(JavaProject project, IPath rootPath, char[][] inclusionPatterns, char[][] exclusionPatterns, IClasspathEntry entry) {
			this.project = project;
			this.rootPath = rootPath;
			this.inclusionPatterns = inclusionPatterns;
			this.exclusionPatterns = exclusionPatterns;
			this.entryKind = entry.getEntryKind();
			this.extraAttributes = entry.getExtraAttributes();
			this.cache = getPackageFragmentRoot();
		}
		public IPackageFragmentRoot getPackageFragmentRoot() {
			IPackageFragmentRoot tRoot = null;
			Object target = JavaModel.getTarget(this.rootPath, false/*don't check existence*/);
			if (target instanceof IResource) {
				tRoot = this.project.getPackageFragmentRoot((IResource)target, this.rootPath, this.extraAttributes);
			} else {
				IPath canonicalizedPath = JavaProject.createPackageFragementKey(new Path(this.rootPath.toOSString()));
				tRoot = this.project.getPackageFragmentRoot0(canonicalizedPath, this.extraAttributes);
			}
			return tRoot;
		}
		public IPackageFragmentRoot getPackageFragmentRoot(IResource resource) {
			if (this.root == null) {
				if (resource != null) {
					this.root = this.project.getPackageFragmentRoot(resource, null/*no entry path*/, this.extraAttributes);
				} else {
					this.root = getPackageFragmentRoot();
				}
			}
			if (this.root != null)
				this.cache = this.root;
			return this.root;
		}

        @Override
		public String toString() {
			StringBuilder buffer = new StringBuilder("project="); //$NON-NLS-1$
			if (this.project == null) {
				buffer.append("null"); //$NON-NLS-1$
			} else {
				buffer.append(this.project.getElementName());
			}
			buffer.append("\npath="); //$NON-NLS-1$
			if (this.rootPath == null) {
				buffer.append("null"); //$NON-NLS-1$
			} else {
				buffer.append(this.rootPath);
			}
			buffer.append("\nincluding="); //$NON-NLS-1$
			if (this.inclusionPatterns == null) {
				buffer.append("null"); //$NON-NLS-1$
			} else {
				for (int i = 0, length = this.inclusionPatterns.length; i < length; i++) {
					buffer.append(new String(this.inclusionPatterns[i]));
					if (i < length-1) {
						buffer.append("|"); //$NON-NLS-1$
					}
				}
			}
			buffer.append("\nexcluding="); //$NON-NLS-1$
			if (this.exclusionPatterns == null) {
				buffer.append("null"); //$NON-NLS-1$
			} else {
				for (int i = 0, length = this.exclusionPatterns.length; i < length; i++) {
					buffer.append(new String(this.exclusionPatterns[i]));
					if (i < length-1) {
						buffer.append("|"); //$NON-NLS-1$
					}
				}
			}
			return buffer.toString();
		}
	}

    public static boolean DEBUG = false;
	public static boolean VERBOSE = false;

	public static final int DEFAULT_CHANGE_EVENT = 0; // must not collide with ElementChangedEvent event masks

	/*
	 * Answer a combination of the lastModified stamp and the size.
	 * Used for detecting external JAR changes
	 */
	public static long getTimeStamp(File file) {
		return file.lastModified() + Long.hashCode(file.length()) * 31L;
	}

	/*
	 * The global state of delta processing.
	 */
	private final DeltaProcessingState state;

	/*
	 * The Java model manager
	 */
	JavaModelManager manager;

    /* The java element that was last created (see createElement(IResource)).
     * This is used as a stack of java elements (using getParent() to pop it, and
     * using the various get*(...) to push it. */

    /*
	 * Queue of deltas created explicily by the Java Model that
	 * have yet to be fired.
	 */
	public List<IJavaElementDelta> javaModelDeltas= new ArrayList<>();

	/*
	 * Queue of reconcile deltas on working copies that have yet to be fired.
	 * This is a table form IWorkingCopy to IJavaElementDelta
	 */
	public Map<ICompilationUnit, IJavaElementDelta> reconcileDeltas = new HashMap<>();

    /*
	 * Used to update the JavaModel for <code>IJavaElementDelta</code>s.
	 */
	private final ModelUpdater modelUpdater = new ModelUpdater();

	/* A set of IJavaProject whose caches need to be reset */
	public Set<IJavaElement> projectCachesToReset = new HashSet<>();

	/* A table from IJavaProject to an array of IPackageFragmentRoot.
	 * This table contains the pkg fragment roots of the project that are being deleted.
	 */
	public Map<IJavaProject, IPackageFragmentRoot[]> oldRoots;

    /*
     * Cache SourceElementParser for the project being visited
     */

    public DeltaProcessor(DeltaProcessingState state, JavaModelManager manager) {
		this.state = state;
		this.manager = manager;
	}

	/*
	 * Adds the dependents of the given project to the list of the projects
	 * to update.
	 */
	private void addDependentProjects(IJavaProject project, Map<IJavaProject, IJavaProject[]> projectDependencies, Set<IJavaElement> result) {
		IJavaProject[] dependents = projectDependencies.get(project);
		if (dependents == null) return;
		for (IJavaProject dependent : dependents) {
			if (result.contains(dependent))
				continue; // no need to go further as the project is already known
			result.add(dependent);
			addDependentProjects(dependent, projectDependencies, result);
		}
	}

    /*
	 * Flushes all deltas without firing them.
	 */
	public void flush() {
		this.javaModelDeltas = new ArrayList<>();
	}

    /*
	 * Fire Java Model delta, flushing them after the fact after post_change notification.
	 * If the firing mode has been turned off, this has no effect.
	 */
	public void fire(IJavaElementDelta customDelta, int eventType) {
		if (DEBUG) {
			trace("-----------------------------------------------------------------------------------------------------------------------");//$NON-NLS-1$
		}

		IJavaElementDelta deltaToNotify;
		if (customDelta == null){
			deltaToNotify = mergeDeltas(this.javaModelDeltas);
		} else {
			deltaToNotify = customDelta;
		}

		// Refresh internal scopes
		if (deltaToNotify != null) {
			for (AbstractSearchScope scope : this.manager.searchScopes.keySet()) {
				scope.processDelta(deltaToNotify, eventType);
			}
			JavaWorkspaceScope workspaceScope = this.manager.workspaceScope;
			if (workspaceScope != null)
				workspaceScope.processDelta(deltaToNotify, eventType);
		}

		// Notification

		// Important: if any listener reacts to notification by updating the listeners list or mask, these lists will
		// be duplicated, so it is necessary to remember original lists in a variable (since field values may change under us)
		IElementChangedListener[] listeners;
		int[] listenerMask;
		int listenerCount;
		synchronized (this.state) {
			listeners = this.state.elementChangedListeners;
			listenerMask = this.state.elementChangedListenerMasks;
			listenerCount = this.state.elementChangedListenerCount;
		}

		switch (eventType) {
			case DEFAULT_CHANGE_EVENT:
			case ElementChangedEvent.POST_CHANGE:
				firePostChangeDelta(deltaToNotify, listeners, listenerMask, listenerCount);
				fireReconcileDelta(listeners, listenerMask, listenerCount);
				break;
		}
	}

	private void firePostChangeDelta(
		IJavaElementDelta deltaToNotify,
		IElementChangedListener[] listeners,
		int[] listenerMask,
		int listenerCount) {

		// post change deltas
		if (DEBUG){
			trace("FIRING POST_CHANGE Delta ["+Thread.currentThread()+"]:"); //$NON-NLS-1$//$NON-NLS-2$
			trace(deltaToNotify == null ? "<NONE>" : deltaToNotify.toString()); //$NON-NLS-1$
		}
		if (deltaToNotify != null) {
			// flush now so as to keep listener reactions to post their own deltas for subsequent iteration
			flush();

			// mark the operation stack has not modifying resources since resource deltas are being fired
			JavaModelOperation.setAttribute(JavaModelOperation.HAS_MODIFIED_RESOURCE_ATTR, null);

			notifyListeners(deltaToNotify, ElementChangedEvent.POST_CHANGE, listeners, listenerMask, listenerCount);
		}
	}
	private void fireReconcileDelta(
		IElementChangedListener[] listeners,
		int[] listenerMask,
		int listenerCount) {


		IJavaElementDelta deltaToNotify = mergeDeltas(this.reconcileDeltas.values());
		if (DEBUG){
			trace("FIRING POST_RECONCILE Delta ["+Thread.currentThread()+"]:"); //$NON-NLS-1$//$NON-NLS-2$
			trace(deltaToNotify == null ? "<NONE>" : deltaToNotify.toString()); //$NON-NLS-1$
		}
		if (deltaToNotify != null) {
			// flush now so as to keep listener reactions to post their own deltas for subsequent iteration
			this.reconcileDeltas = new HashMap<>();

			notifyListeners(deltaToNotify, ElementChangedEvent.POST_RECONCILE, listeners, listenerMask, listenerCount);
		}
	}

    /*
	 * Merges all awaiting deltas.
	 */
	private IJavaElementDelta mergeDeltas(Collection<IJavaElementDelta> deltas) {
		if (deltas.size() == 0) return null;
		if (deltas.size() == 1) return deltas.iterator().next();

		if (VERBOSE) {
			trace("MERGING " + deltas.size() + " DELTAS ["+Thread.currentThread()+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		Iterator<IJavaElementDelta> iterator = deltas.iterator();
		JavaElementDelta rootDelta = new JavaElementDelta(this.manager.javaModel);
		boolean insertedTree = false;
		while (iterator.hasNext()) {
			JavaElementDelta delta = (JavaElementDelta)iterator.next();
			if (VERBOSE) {
				trace(delta.toString());
			}
			IJavaElement element = delta.getElement();
			if (this.manager.javaModel.equals(element)) {
				IJavaElementDelta[] children = delta.getAffectedChildren();
				for (IJavaElementDelta child : children) {
					JavaElementDelta projectDelta = (JavaElementDelta) child;
					rootDelta.insertDeltaTree(projectDelta.getElement(), projectDelta);
					insertedTree = true;
				}
				IResourceDelta[] resourceDeltas = delta.getResourceDeltas();
				if (resourceDeltas != null) {
					for (IResourceDelta rd : resourceDeltas) {
						rootDelta.addResourceDelta(rd);
						insertedTree = true;
					}
				}
			} else {
				rootDelta.insertDeltaTree(element, delta);
				insertedTree = true;
			}
		}
		if (insertedTree) return rootDelta;
		return null;
	}
	private void notifyListeners(IJavaElementDelta deltaToNotify, int eventType, IElementChangedListener[] listeners, int[] listenerMask, int listenerCount) {
		final ElementChangedEvent extraEvent = new ElementChangedEvent(deltaToNotify, eventType);
		for (int i= 0; i < listenerCount; i++) {
			if ((listenerMask[i] & eventType) != 0){
				final IElementChangedListener listener = listeners[i];
				long start = -1;
				if (VERBOSE) {
					System.out.print("Listener #" + (i+1) + "=" + listener.toString());//$NON-NLS-1$//$NON-NLS-2$
					start = System.currentTimeMillis();
				}
				// wrap callbacks with Safe runnable for subsequent listeners to be called when some are causing grief
				SafeRunner.run(new ISafeRunnable() {
					@Override
					public void handleException(Throwable exception) {
						Util.log(exception, "Exception occurred in listener of Java element change notification"); //$NON-NLS-1$
					}
					@Override
					public void run() throws Exception {
						listener.elementChanged(extraEvent);
					}
				});
				if (VERBOSE) {
					trace(" -> " + (System.currentTimeMillis()-start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
	}

    /*
	 * Traverse the set of projects which have changed namespace, and reset their
	 * caches and their dependents
	 */
	public void resetProjectCaches() {
		if (this.projectCachesToReset.isEmpty())
			return;

		JavaModelManager.getJavaModelManager().resetJarTypeCache();

		Iterator<IJavaElement> iterator = this.projectCachesToReset.iterator();
		Map<IJavaProject, IJavaProject[]> projectDepencies = this.state.projectDependencies;
		Set<IJavaElement> affectedDependents = new HashSet<>();
		while (iterator.hasNext()) {
			JavaProject project = (JavaProject)iterator.next();
			project.resetCaches();
			addDependentProjects(project, projectDepencies, affectedDependents);
		}
		// reset caches of dependent projects
		iterator = affectedDependents.iterator();
		while (iterator.hasNext()) {
			JavaProject project = (JavaProject) iterator.next();
			project.resetCaches();
		}

		this.projectCachesToReset.clear();
	}
	/*
	 * Registers the given delta with this delta processor.
	 */
	public void registerJavaModelDelta(IJavaElementDelta delta) {
		if (VERBOSE && JavaModelManager.isReadOnly()) {
			ILog.get().warn("JavaModel change during read only operation", new IllegalStateException( //$NON-NLS-1$
					"JavaModel modified during 'read only' operation. Consider to report this warning to https://github.com/eclipse-jdt/eclipse.jdt.core/issues. delta=" //$NON-NLS-1$
							+ delta));
		}
		this.javaModelDeltas.add(delta);
	}

    /*
	 * Update Java Model given some delta
	 */
	public void updateJavaModel(IJavaElementDelta customDelta) {

		if (customDelta == null){
			for (IJavaElementDelta delta : this.javaModelDeltas) {
				this.modelUpdater.processJavaDelta(delta);
			}
		} else {
			this.modelUpdater.processJavaDelta(customDelta);
		}
	}

}
