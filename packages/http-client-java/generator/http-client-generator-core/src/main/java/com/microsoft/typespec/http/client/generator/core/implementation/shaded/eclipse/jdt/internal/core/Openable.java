/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
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

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IContainer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.BufferChangedEvent;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IBuffer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IBufferChangedListener;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IBufferFactory;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IOpenable;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaModelException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.IElementInfo;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.Util;

import java.util.Enumeration;
import java.util.List;

/**
 * Abstract class for implementations of java elements which are IOpenable.
 *
 * @see IJavaElement
 * @see IOpenable
 */
public abstract class Openable extends JavaElement implements IOpenable, IBufferChangedListener {

protected Openable(JavaElement parent) {
	super(parent);
}
/**
 * The buffer associated with this element has changed. Registers
 * this element as being out of synch with its buffer's contents.
 * If the buffer has been closed, this element is set as NOT out of
 * synch with the contents.
 *
 * @see IBufferChangedListener
 */
@Override
public void bufferChanged(BufferChangedEvent event) {
	if (event.getBuffer().isClosed()) {
		JavaModelManager.getJavaModelManager().getElementsOutOfSynchWithBuffers().remove(this);
		getBufferManager().removeBuffer(event.getBuffer());
	} else {
		JavaModelManager.getJavaModelManager().getElementsOutOfSynchWithBuffers().add(this);
	}
}

    /*
 * Returns whether this element can be removed from the Java model cache to make space.
 */
public boolean canBeRemovedFromCache() {
	try {
		return !hasUnsavedChanges();
	} catch (JavaModelException e) {
		return false;
	}
}
/*
 * Returns whether the buffer of this element can be removed from the Java model cache to make space.
 */
public boolean canBufferBeRemovedFromCache(IBuffer buffer) {
	return !buffer.hasUnsavedChanges();
}
/**
 * Close the buffer associated with this element, if any.
 */
protected void closeBuffer() {
	if (!hasBuffer()) return; // nothing to do
	IBuffer buffer = getBufferManager().getBuffer(this);
	if (buffer != null) {
		buffer.close();
		buffer.removeBufferChangedListener(this);
	}
}
/**
 * This element is being closed.  Do any necessary cleanup.
 */
@Override
protected void closing(Object info) {
	closeBuffer();
}

    /*
 * Returns a new element info for this element.
 */
@Override
protected OpenableElementInfo createElementInfo() {
	return new OpenableElementInfo();
}
/**
 * @see IJavaElement
 */
@Override
public boolean exists() {
	if (JavaModelManager.getJavaModelManager().getInfo(this) != null)
		return true;
	switch (getElementType()) {
		case IJavaElement.PACKAGE_FRAGMENT:
			PackageFragmentRoot root = getPackageFragmentRoot();
			if (root.isArchive()) {
				// pkg in a jar -> need to open root to know if this pkg exists
				JarPackageFragmentRootInfo rootInfo;
				try {
					rootInfo = (JarPackageFragmentRootInfo) root.getElementInfo();
				} catch (JavaModelException e) {
					return false;
				}
				return rootInfo.rawPackageInfo.containsKey(List.of(((PackageFragment) this).names));
			}
			break;
		case IJavaElement.CLASS_FILE:
			if (getPackageFragmentRoot().isArchive()) {
				// class file in a jar -> need to open this class file to know if it exists
				return super.exists();
			}
			break;
	}
	return validateExistence(resource()).isOK();
}
@Override
public String findRecommendedLineSeparator() throws JavaModelException {
	IBuffer buffer = getBuffer();
	String source = buffer == null ? null : buffer.getContents();
	return Util.getLineSeparator(source, getJavaProject());
}

    /**
 * Note: a buffer with no unsaved changes can be closed by the Java Model
 * since it has a finite number of buffers allowed open at one time. If this
 * is the first time a request is being made for the buffer, an attempt is
 * made to create and fill this element's buffer. If the buffer has been
 * closed since it was first opened, the buffer is re-created.
 *
 * @see IOpenable
 */
@Override
public IBuffer getBuffer() throws JavaModelException {
	if (hasBuffer()) {
		// ensure element is open
		IElementInfo info = getElementInfo();
		IBuffer buffer = getBufferManager().getBuffer(this);
		if (buffer == null) {
			// try to (re)open a buffer
			buffer = openBuffer(null, info);
		}
		if (buffer instanceof NullBuffer) {
			return null;
		}
		return buffer;
	} else {
		return null;
	}
}
/**
 * Answers the buffer factory to use for creating new buffers
 * @deprecated
 */
public IBufferFactory getBufferFactory(){
	return getBufferManager().getDefaultBufferFactory();
}

/**
 * Returns the buffer manager for this element.
 */
protected BufferManager getBufferManager() {
	return BufferManager.getDefaultBufferManager();
}
/**
 * Return my underlying resource. Elements that may not have a
 * corresponding resource must override this method.
 *
 * @see IJavaElement
 */
@Override
public IResource getCorrespondingResource() throws JavaModelException {
	return getUnderlyingResource();
}
/*
 * @see IJavaElement
 */
@Override
public IOpenable getOpenable() {
	return this;
}



/**
 * @see IJavaElement
 */
@Override
public IResource getUnderlyingResource() throws JavaModelException {
	IResource parentResource = this.getParent().getUnderlyingResource();
	if (parentResource == null) {
		return null;
	}
	int type = parentResource.getType();
	if (type == IResource.FOLDER || type == IResource.PROJECT) {
		IContainer folder = (IContainer) parentResource;
		IResource resource = folder.findMember(getElementName());
		if (resource == null) {
			throw newNotPresentException();
		} else {
			return resource;
		}
	} else {
		return parentResource;
	}
}

/**
 * Returns true if this element may have an associated source buffer,
 * otherwise false. Subclasses must override as required.
 */
protected boolean hasBuffer() {
	return false;
}
/**
 * @see IOpenable
 */
@Override
public boolean hasUnsavedChanges() throws JavaModelException{

	if (isReadOnly() || !isOpen()) {
		return false;
	}
	IBuffer buf = getBuffer();
	if (buf != null && buf.hasUnsavedChanges()) {
		return true;
	}
	// for package fragments, package fragment roots, and projects must check open buffers
	// to see if they have an child with unsaved changes
	int elementType = getElementType();
	if (elementType == PACKAGE_FRAGMENT ||
		elementType == PACKAGE_FRAGMENT_ROOT ||
		elementType == JAVA_PROJECT ||
		elementType == JAVA_MODEL) { // fix for 1FWNMHH
		Enumeration<IBuffer> openBuffers= getBufferManager().getOpenBuffers();
		while (openBuffers.hasMoreElements()) {
			IBuffer buffer= openBuffers.nextElement();
			if (buffer.hasUnsavedChanges()) {
				IJavaElement owner= (IJavaElement)buffer.getOwner();
				if (isAncestorOf(owner)) {
					return true;
				}
			}
		}
	}

	return false;
}
/**
 * Subclasses must override as required.
 *
 * @see IOpenable
 */
@Override
public boolean isConsistent() {
	return true;
}
/**
 *
 * @see IOpenable
 */
@Override
public boolean isOpen() {
	return JavaModelManager.getJavaModelManager().getInfo(this) != null;
}
/**
 * Returns true if this represents a source element.
 * Openable source elements have an associated buffer created
 * when they are opened.
 */
protected boolean isSourceElement() {
	return false;
}
/**
 * @see IJavaElement
 */
@Override
public boolean isStructureKnown() throws JavaModelException {
	return ((OpenableElementInfo)getElementInfo()).isStructureKnown();
}
/**
 * @see IOpenable
 */
@Override
public void makeConsistent(IProgressMonitor monitor) throws JavaModelException {
	// only compilation units can be inconsistent
	// other openables cannot be inconsistent so default is to do nothing
}
/**
 * @see IOpenable
 */
@Override
public void open(IProgressMonitor pm) throws JavaModelException {
	getElementInfo(pm);
}

/**
 * Opens a buffer on the contents of this element, and returns
 * the buffer, or returns <code>null</code> if opening fails.
 * By default, do nothing - subclasses that have buffers
 * must override as required.
 */
protected IBuffer openBuffer(IProgressMonitor pm, IElementInfo info) throws JavaModelException {
	return null;
}

@Override
public IResource getResource() {
	PackageFragmentRoot root = getPackageFragmentRoot();
	if (root != null) {
		if (root.isExternal())
			return null;
		if (root.isArchive())
			return root.resource(root);
	}
	return resource(root);
}

@Override
public IResource resource() {
	PackageFragmentRoot root = getPackageFragmentRoot();
	if (root != null && root.isArchive())
		return root.resource(root);
	return resource(root);
}

protected abstract IResource resource(PackageFragmentRoot root);

/**
 * Returns whether the corresponding resource or associated file exists
 */
protected boolean resourceExists(IResource underlyingResource) {
	return underlyingResource.isAccessible();
}

/**
 * Find enclosing package fragment root if any
 */
public PackageFragmentRoot getPackageFragmentRoot() {
	return (PackageFragmentRoot) getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
}

/*
 * Validates the existence of this openable. Returns a non ok status if it doesn't exist.
 */
abstract protected IStatus validateExistence(IResource underlyingResource);

}
