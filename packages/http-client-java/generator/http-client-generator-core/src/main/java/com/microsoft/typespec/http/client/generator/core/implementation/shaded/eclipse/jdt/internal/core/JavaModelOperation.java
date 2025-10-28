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
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core;

import static com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.JavaModelManager.trace;

import java.util.ArrayList;
import java.util.HashMap;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResourceStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IWorkspace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IWorkspaceRunnable;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.ResourcesPlugin;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.OperationCanceledException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.SubMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.jobs.ISchedulingRule;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.*;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.Messages;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jface.text.IDocument;

/**
 * Defines behavior common to all Java Model operations
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class JavaModelOperation implements IWorkspaceRunnable, IProgressMonitor {
	protected interface IPostAction {
		/*
		 * Returns the id of this action.
		 * @see JavaModelOperation#postAction
		 */
		String getID();
		/*
		 * Run this action.
		 */
		void run() throws JavaModelException;
	}

    /*
	 * Whether tracing post actions is enabled.
	 */
	protected static boolean POST_ACTION_VERBOSE;

	/*
	 * A list of IPostActions.
	 */
	protected IPostAction[] actions;
	protected int actionsStart = 0;
	protected int actionsEnd = -1;
	/*
	 * A HashMap of attributes that can be used by operations
	 */
	protected HashMap attributes;

	public static final String HAS_MODIFIED_RESOURCE_ATTR = "hasModifiedResource"; //$NON-NLS-1$
	public static final String TRUE = JavaModelManager.TRUE;
	//public static final String FALSE = "false";

	/**
	 * The elements this operation operates on,
	 * or <code>null</code> if this operation
	 * does not operate on specific elements.
	 */
	protected IJavaElement[] elementsToProcess;
	/**
	 * The parent elements this operation operates with
	 * or <code>null</code> if this operation
	 * does not operate with specific parent elements.
	 */
	protected IJavaElement[] parentElements;
	/**
	 * An empty collection of <code>IJavaElement</code>s - the common
	 * empty result if no elements are created, or if this
	 * operation is not actually executed.
	 */
	protected static final IJavaElement[] NO_ELEMENTS= new IJavaElement[] {};


	/**
	 * The elements created by this operation - empty
	 * until the operation actually creates elements.
	 */
	protected IJavaElement[] resultElements= NO_ELEMENTS;

	/**
	 * The progress monitor passed into this operation
	 */
	public SubMonitor progressMonitor= SubMonitor.convert(null);
	/**
	 * A flag indicating whether this operation is nested.
	 */
	protected boolean isNested = false;
	/**
	 * Conflict resolution policy - by default do not force (fail on a conflict).
	 */
	protected boolean force = false;

	/*
	 * A per thread stack of java model operations (PerThreadObject of ArrayList).
	 */
	protected static final ThreadLocal OPERATION_STACKS = new ThreadLocal();
	protected JavaModelOperation() {
		// default constructor used in subclasses
	}
	/**
	 * A common constructor for all Java Model operations.
	 */
	protected JavaModelOperation(IJavaElement[] elements) {
		this.elementsToProcess = elements;
	}

    /**
	 * Common constructor for all Java Model operations.
	 */
	protected JavaModelOperation(IJavaElement element) {
		this.elementsToProcess = new IJavaElement[]{element};
	}

    /*
	 * Registers the given delta with the Java Model Manager.
	 */
	protected void addDelta(IJavaElementDelta delta) {
		JavaModelManager.getJavaModelManager().getDeltaProcessor().registerJavaModelDelta(delta);
	}

    /*
	 * Deregister the reconcile delta for the given working copy
	 */
	protected void removeReconcileDelta(ICompilationUnit workingCopy) {
		JavaModelManager.getJavaModelManager().getDeltaProcessor().reconcileDeltas.remove(workingCopy);
	}

    /**
	 * @see IProgressMonitor
	 */
	@Override
	public void beginTask(String name, int totalWork) {
		if (this.progressMonitor != null) {
			this.progressMonitor.beginTask(name, totalWork);
		}
	}
	/*
	 * Returns whether this operation can modify the package fragment roots.
	 */
	protected boolean canModifyRoots() {
		return false;
	}
	/**
	 * Checks with the progress monitor to see whether this operation
	 * should be canceled. An operation should regularly call this method
	 * during its operation so that the user can cancel it.
	 *
	 * @exception OperationCanceledException if cancelling the operation has been requested
	 * @see IProgressMonitor#isCanceled
	 */
	protected void checkCanceled() {
		if (isCanceled()) {
			throw new OperationCanceledException(Messages.operation_cancelled);
		}
	}
	/**
	 * Common code used to verify the elements this operation is processing.
	 * @see JavaModelOperation#verify()
	 */
	protected IJavaModelStatus commonVerify() {
		if (this.elementsToProcess == null || this.elementsToProcess.length == 0) {
			return new JavaModelStatus(IJavaModelStatusConstants.NO_ELEMENTS_TO_PROCESS);
		}
		for (IJavaElement element : this.elementsToProcess) {
			if (element == null) {
				return new JavaModelStatus(IJavaModelStatusConstants.NO_ELEMENTS_TO_PROCESS);
			}
		}
		return JavaModelStatus.VERIFIED_OK;
	}

    /**
	 * @see IProgressMonitor
	 */
	@Override
	public void done() {
		if (this.progressMonitor != null) {
			this.progressMonitor.done();
		}
	}

    /**
	 * Performs the operation specific behavior. Subclasses must override.
	 */
	protected abstract void executeOperation() throws JavaModelException;
	/*
	 * Returns the attribute registered at the given key with the top level operation.
	 * Returns null if no such attribute is found.
	 */
	protected static Object getAttribute(Object key) {
		ArrayList stack = getCurrentOperationStack();
		if (stack.isEmpty()) return null;
		JavaModelOperation topLevelOp = (JavaModelOperation)stack.get(0);
		if (topLevelOp.attributes == null) {
			return null;
		} else {
			return topLevelOp.attributes.get(key);
		}
	}

    /*
	 * Returns the stack of operations running in the current thread.
	 * Returns an empty stack if no operations are currently running in this thread.
	 */
	protected static ArrayList getCurrentOperationStack() {
		ArrayList stack = (ArrayList)OPERATION_STACKS.get();
		if (stack == null) {
			stack = new ArrayList();
			OPERATION_STACKS.set(stack);
		}
		return stack;
	}
	/*
	 * Returns the existing document for the given cu, or a DocumentAdapter if none.
	 */
	protected IDocument getDocument(ICompilationUnit cu) throws JavaModelException {
		IBuffer buffer = cu.getBuffer();
		if (buffer instanceof IDocument)
			return (IDocument) buffer;
		return new DocumentAdapter(buffer);
	}
	/**
	 * Returns the element to which this operation applies,
	 * or <code>null</code> if not applicable.
	 */
	protected IJavaElement getElementToProcess() {
		if (this.elementsToProcess == null || this.elementsToProcess.length == 0) {
			return null;
		}
		return this.elementsToProcess[0];
	}
	/**
	 * Returns the Java Model this operation is operating in.
	 */
	public IJavaModel getJavaModel() {
		return JavaModelManager.getJavaModelManager().getJavaModel();
	}

    /*
	 * Returns the scheduling rule for this operation (i.e. the resource that needs to be locked
	 * while this operation is running.
	 * Subclasses can override.
	 */
	protected ISchedulingRule getSchedulingRule() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

    /**
	 * Returns whether this operation has performed any resource modifications.
	 * Returns false if this operation has not been executed yet.
	 */
	public boolean hasModifiedResource() {
		return !isReadOnly() && getAttribute(HAS_MODIFIED_RESOURCE_ATTR) == TRUE;
	}
	@Override
	public void internalWorked(double work) {
		if (this.progressMonitor != null) {
			this.progressMonitor.internalWorked(work);
		}
	}
	/**
	 * @see IProgressMonitor
	 */
	@Override
	public boolean isCanceled() {
		if (this.progressMonitor != null) {
			return this.progressMonitor.isCanceled();
		}
		return false;
	}
	/**
	 * Returns <code>true</code> if this operation performs no resource modifications,
	 * otherwise <code>false</code>. Subclasses must override.
	 */
	public boolean isReadOnly() {
		return false;
	}
	/*
	 * Returns whether this operation is the first operation to run in the current thread.
	 */
	protected boolean isTopLevelOperation() {
		ArrayList stack;
		return !(stack = getCurrentOperationStack()).isEmpty()
			&& stack.get(0) == this;
	}

    /*
	 * Removes the last pushed operation from the stack of running operations.
	 * Returns the poped operation or null if the stack was empty.
	 */
	protected JavaModelOperation popOperation() {
		ArrayList stack = getCurrentOperationStack();
		int size = stack.size();
		if (size > 0) {
			if (size == 1) { // top level operation
				OPERATION_STACKS.remove(); // release reference (see http://bugs.eclipse.org/bugs/show_bug.cgi?id=33927)
			}
			return (JavaModelOperation)stack.remove(size-1);
		} else {
			return null;
		}
	}

    /*
	 * Pushes the given operation on the stack of operations currently running in this thread.
	 */
	protected void pushOperation(JavaModelOperation operation) {
		getCurrentOperationStack().add(operation);
	}

    /**
	 * Runs this operation and registers any deltas created.
	 *
	 * @see IWorkspaceRunnable
	 * @exception CoreException if the operation fails
	 */
	@Override
	public void run(IProgressMonitor monitor) throws CoreException {
		SubMonitor oldMonitor = this.progressMonitor;
		try {
			JavaModelManager manager = JavaModelManager.getJavaModelManager();
			DeltaProcessor deltaProcessor = manager.getDeltaProcessor();
			int previousDeltaCount = deltaProcessor.javaModelDeltas.size();
			try {
				this.progressMonitor = SubMonitor.convert(monitor);
				pushOperation(this);
				try {
					if (canModifyRoots()) {
						// computes the root infos before executing the operation
						// noop if aready initialized
						JavaModelManager.getDeltaState().initializeRoots(false/*not initiAfterLoad*/);
					}

					executeOperation();
				} finally {
					if (isTopLevelOperation()) {
						runPostActions();
					}
				}
			} finally {
				try {
					// reacquire delta processor as it can have been reset during executeOperation()
					deltaProcessor = manager.getDeltaProcessor();

					// update JavaModel using deltas that were recorded during this operation
					for (int i = previousDeltaCount, size = deltaProcessor.javaModelDeltas.size(); i < size; i++) {
						deltaProcessor.updateJavaModel(deltaProcessor.javaModelDeltas.get(i));
					}

					// close the parents of the created elements and reset their project's cache (in case we are in an
					// IWorkspaceRunnable and the clients wants to use the created element's parent)
					// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=83646
					for (IJavaElement element : this.resultElements) {
						Openable openable = (Openable) element.getOpenable();
						if (!(openable instanceof CompilationUnit) || !((CompilationUnit) openable).isWorkingCopy()) { // a working copy must remain a child of its parent even after a move
							openable.getParent().close();
						}
						switch (element.getElementType()) {
							case IJavaElement.PACKAGE_FRAGMENT_ROOT:
							case IJavaElement.PACKAGE_FRAGMENT:
								deltaProcessor.projectCachesToReset.add(element.getJavaProject());
								break;
						}
					}
					deltaProcessor.resetProjectCaches();

					// fire only iff:
					// - the operation is a top level operation
					// - the operation did produce some delta(s)
					// - but the operation has not modified any resource
					if (isTopLevelOperation()) {
						if ((deltaProcessor.javaModelDeltas.size() > previousDeltaCount || !deltaProcessor.reconcileDeltas.isEmpty())
								&& !hasModifiedResource()) {
							deltaProcessor.fire(null, DeltaProcessor.DEFAULT_CHANGE_EVENT);
						} // else deltas are fired while processing the resource delta
					}
				} finally {
					popOperation();
				}
			}
		} finally {
			if (monitor != null) {
				monitor.done();
			}
			this.progressMonitor = oldMonitor;
		}
	}
	/**
	 * Main entry point for Java Model operations. Runs a Java Model Operation as an IWorkspaceRunnable
	 * if not read-only.
	 */
	public void runOperation(IProgressMonitor monitor) throws JavaModelException {
		IJavaModelStatus status= verify();
		if (!status.isOK()) {
			throw new JavaModelException(status);
		}
		try {
			if (isReadOnly()) {
				run(monitor);
			} else {
				// Use IWorkspace.run(...) to ensure that resource changes are batched
				// Note that if the tree is locked, this will throw a CoreException, but this is ok
				// as this operation is modifying the tree (not read-only) and a CoreException will be thrown anyway.
				ResourcesPlugin.getWorkspace().run(this, getSchedulingRule(), IWorkspace.AVOID_UPDATE, monitor);
			}
		} catch (CoreException ce) {
			if (ce instanceof JavaModelException) {
				throw (JavaModelException)ce;
			} else {
				if (ce.getStatus().getCode() == IResourceStatus.OPERATION_FAILED) {
					Throwable e= ce.getStatus().getException();
					if (e instanceof JavaModelException) {
						throw (JavaModelException) e;
					}
				}
				throw new JavaModelException(ce);
			}
		}
	}
	protected void runPostActions() throws JavaModelException {
		while (this.actionsStart <= this.actionsEnd) {
			IPostAction postAction = this.actions[this.actionsStart++];
			if (POST_ACTION_VERBOSE) {
				trace("(" + Thread.currentThread() + ") [JavaModelOperation.runPostActions()] Running action " + postAction.getID()); //$NON-NLS-1$ //$NON-NLS-2$
			}
			postAction.run();
		}
	}
	/*
	 * Registers the given attribute at the given key with the top level operation.
	 */
	protected static void setAttribute(Object key, Object attribute) {
		ArrayList operationStack = getCurrentOperationStack();
		if (operationStack.isEmpty())
			return;
		JavaModelOperation topLevelOp = (JavaModelOperation) operationStack.get(0);
		if (topLevelOp.attributes == null) {
			topLevelOp.attributes = new HashMap();
		}
		topLevelOp.attributes.put(key, attribute);
	}
	/**
	 * @see IProgressMonitor
	 */
	@Override
	public void setCanceled(boolean b) {
		if (this.progressMonitor != null) {
			this.progressMonitor.setCanceled(b);
		}
	}

    /**
	 * @see IProgressMonitor
	 */
	@Override
	public void setTaskName(String name) {
		if (this.progressMonitor != null) {
			this.progressMonitor.setTaskName(name);
		}
	}
	/**
	 * @see IProgressMonitor
	 */
	@Override
	public void subTask(String name) {
		if (this.progressMonitor != null) {
			this.progressMonitor.subTask(name);
		}
	}
	/**
	 * Returns a status indicating if there is any known reason
	 * this operation will fail.  Operations are verified before they
	 * are run.
	 *
	 * Subclasses must override if they have any conditions to verify
	 * before this operation executes.
	 *
	 * @see IJavaModelStatus
	 */
	protected IJavaModelStatus verify() {
		return commonVerify();
	}

	/**
	 * @see IProgressMonitor
	 */
	@Override
	public void worked(int work) {
		if (this.progressMonitor != null) {
			this.progressMonitor.worked(work);
			checkCanceled();
		}
	}
}
