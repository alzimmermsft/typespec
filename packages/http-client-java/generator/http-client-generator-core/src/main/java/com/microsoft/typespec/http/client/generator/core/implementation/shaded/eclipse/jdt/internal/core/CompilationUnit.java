/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
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
 *     Alex Smirnoff (alexsmr@sympatico.ca) - part of the changes to support Java-like extension
 *                                                            (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=71460)
 *     Microsoft Corporation - support custom options at compilation unit level
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IContainer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IFile;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Path;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.BufferChangedEvent;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IBuffer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IBufferFactory;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.ICompilationUnit;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IImportContainer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IImportDeclaration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaModelStatusConstants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IMember;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IMethod;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IModuleDescription;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IOpenable;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IPackageDeclaration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IPackageFragmentRoot;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IProblemRequestor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.ISourceRange;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.ISourceReference;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IType;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.ITypeRoot;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IWorkingCopy;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaConventions;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaCore;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaModelException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.WorkingCopyOwner;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.CategorizedProblem;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.CharOperation;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.IElementInfo;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.problem.AbortCompilationUnit;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.util.SuffixConstants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.DeduplicationUtil;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.MementoTokenizer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.Messages;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.Util;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jface.text.BadLocationException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jface.text.IDocument;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.text.edits.MalformedTreeException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.text.edits.TextEdit;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.text.edits.UndoEdit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @see ICompilationUnit
 */
public class CompilationUnit extends Openable implements ICompilationUnit, com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.ICompilationUnit, SuffixConstants {
    private static final IImportDeclaration[] NO_IMPORTS = new IImportDeclaration[0];

	protected final String name;
	public final WorkingCopyOwner owner;

    /**
 * Constructs a handle to a compilation unit with the given name in the
 * specified package for the specified owner
 */
public CompilationUnit(PackageFragment parent, String name, WorkingCopyOwner owner) {
	super(parent);
	this.name = name;
	this.owner = owner;
}


@Override
public UndoEdit applyTextEdit(TextEdit edit, IProgressMonitor monitor) throws JavaModelException {
	IBuffer buffer = getBuffer();
	if (buffer instanceof IBuffer.ITextEditCapability) {
		return ((IBuffer.ITextEditCapability) buffer).applyTextEdit(edit, monitor);
	} else if (buffer != null) {
		IDocument document = buffer instanceof IDocument ? (IDocument) buffer : new DocumentAdapter(buffer);
		try {
			UndoEdit undoEdit= edit.apply(document);
			return undoEdit;
		} catch (MalformedTreeException | BadLocationException e) {
			throw new JavaModelException(e, IJavaModelStatusConstants.BAD_TEXT_EDIT_LOCATION);
		}
	}
	return null; // can not happen, there are no compilation units without buffer
}

@Override
public void becomeWorkingCopy(IProblemRequestor problemRequestor, IProgressMonitor monitor) throws JavaModelException {
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	JavaModelManager.PerWorkingCopyInfo perWorkingCopyInfo = manager.getPerWorkingCopyInfo(this, false/*don't create*/, true /*record usage*/, null/*no problem requestor needed*/);
	if (perWorkingCopyInfo == null) {
		// close cu and its children
		close();

		BecomeWorkingCopyOperation operation = new BecomeWorkingCopyOperation(this, problemRequestor);
		operation.runOperation(monitor);
	}
}

@Override
public void becomeWorkingCopy(IProgressMonitor monitor) throws JavaModelException {
	IProblemRequestor requestor = this.owner == null ? null : this.owner.getProblemRequestor(this);
	becomeWorkingCopy(requestor, monitor);
}

    @Override
public boolean canBeRemovedFromCache() {
	if (getPerWorkingCopyInfo() != null) return false; // working copies should remain in the cache until they are destroyed
	return super.canBeRemovedFromCache();
}

@Override
public boolean canBufferBeRemovedFromCache(IBuffer buffer) {
	if (getPerWorkingCopyInfo() != null) return false; // working copy buffers should remain in the cache until working copy is destroyed
	return super.canBufferBeRemovedFromCache(buffer);
}

@Override
public void close() throws JavaModelException {
	if (getPerWorkingCopyInfo() != null) return; // a working copy must remain opened until it is discarded
	super.close();
}

@Override
protected void closing(Object info) {
	if (getPerWorkingCopyInfo() == null) {
		super.closing(info);
	} // else the buffer of a working copy must remain open for the lifetime of the working copy
}

    @Override
public void bufferChanged(BufferChangedEvent event) {
        super.bufferChanged(event);
}
/**
 * Returns a new element info for this element.
 */
@Override
protected CompilationUnitElementInfo createElementInfo() {
	return new CompilationUnitElementInfo();
}

/**
 * Returns true if this handle represents the same Java element
 * as the given handle.
 *
 * @see Object#equals(Object)
 */
@Override
public boolean equals(Object obj) {
	if (!(obj instanceof CompilationUnit other)) return false;
	return this.owner.equals(other.owner) && super.equals(obj);
}

@Override
protected int calculateHashCode() {
	return Util.combineHashCodes(super.calculateHashCode(), this.owner.hashCode());
}

/**
 * @see ICompilationUnit#findElements(IJavaElement)
 */
@Override
public IJavaElement[] findElements(IJavaElement element) {
	if (element instanceof IType && ((IType) element).isLambda()) {
		return null;
	}
	ArrayList<IJavaElement> children = new ArrayList<>();
	while (element != null && element.getElementType() != IJavaElement.COMPILATION_UNIT) {
		children.add(element);
		element = element.getParent();
	}
	if (element == null) return null;
	IJavaElement currentElement = this;
	for (int i = children.size()-1; i >= 0; i--) {
		SourceRefElement child = (SourceRefElement)children.get(i);
		switch (child.getElementType()) {
			case IJavaElement.PACKAGE_DECLARATION:
				currentElement = ((ICompilationUnit)currentElement).getPackageDeclaration(child.getElementName());
				break;
			case IJavaElement.IMPORT_CONTAINER:
				currentElement = ((ICompilationUnit)currentElement).getImportContainer();
				break;
			case IJavaElement.IMPORT_DECLARATION:
				currentElement = ((IImportContainer)currentElement).getImport(child.getElementName());
				break;
			case IJavaElement.TYPE:
				switch (currentElement.getElementType()) {
					case IJavaElement.COMPILATION_UNIT:
						currentElement = ((ICompilationUnit)currentElement).getType(child.getElementName());
						break;
					case IJavaElement.TYPE:
						currentElement = ((IType)currentElement).getType(child.getElementName());
						break;
					case IJavaElement.FIELD:
					case IJavaElement.INITIALIZER:
					case IJavaElement.METHOD:
						currentElement =  ((IMember)currentElement).getType(child.getElementName(), child.getOccurrenceCount());
						break;
				}
				break;
			case IJavaElement.INITIALIZER:
				currentElement = ((IType)currentElement).getInitializer(child.getOccurrenceCount());
				break;
			case IJavaElement.FIELD:
				currentElement = ((IType)currentElement).getField(child.getElementName());
				break;
			case IJavaElement.METHOD:
				currentElement = ((IType)currentElement).getMethod(child.getElementName(), ((IMethod)child).getParameterTypes());
				break;
		}

	}
	if (currentElement != null && currentElement.exists()) {
		return new IJavaElement[] {currentElement};
	} else {
		return null;
	}
}
/**
 * @see ICompilationUnit#findPrimaryType()
 */
@Override
public IType findPrimaryType() {
	String typeName = DeduplicationUtil.intern(Util.getNameWithoutJavaLikeExtension(getElementName()));
	IType primaryType= getType(typeName);
	if (primaryType.exists()) {
		return primaryType;
	}
	return null;
}

/**
 * @see IWorkingCopy#findSharedWorkingCopy(IBufferFactory)
 * @deprecated
 */
@Override
public IJavaElement findSharedWorkingCopy(IBufferFactory factory) {

	// if factory is null, default factory must be used
	if (factory == null) factory = getBufferManager().getDefaultBufferFactory();

	return findWorkingCopy(BufferFactoryWrapper.create(factory));
}

/**
 * @see ICompilationUnit#findWorkingCopy(WorkingCopyOwner)
 */
@Override
public ICompilationUnit findWorkingCopy(WorkingCopyOwner workingCopyOwner) {
	CompilationUnit cu = new CompilationUnit((PackageFragment)this.getParent(), getElementName(), workingCopyOwner);
	if (workingCopyOwner == DefaultWorkingCopyOwner.PRIMARY) {
		return cu;
	} else {
		// must be a working copy
		JavaModelManager.PerWorkingCopyInfo perWorkingCopyInfo = cu.getPerWorkingCopyInfo();
		if (perWorkingCopyInfo != null) {
			return perWorkingCopyInfo.getWorkingCopy();
		} else {
			return null;
		}
	}
}
/**
 * @see ICompilationUnit#getAllTypes()
 */
@Override
public IType[] getAllTypes() throws JavaModelException {
	IType[] types = getTypes();
	ArrayList<IType> allTypes = new ArrayList<>(types.length);
	ArrayList<IType> typesToTraverse = new ArrayList<>(types.length);
	Collections.addAll(typesToTraverse, types);
	while (!typesToTraverse.isEmpty()) {
		IType type = typesToTraverse.get(0);
		typesToTraverse.remove(type);
		allTypes.add(type);
		types = type.getTypes();
		Collections.addAll(typesToTraverse, types);
	}
	IType[] arrayOfAllTypes = new IType[allTypes.size()];
	allTypes.toArray(arrayOfAllTypes);
	return arrayOfAllTypes;
}
/**
 * @see IMember#getCompilationUnit()
 */
@Override
public CompilationUnit getCompilationUnit() {
	return this;
}
/**
 * @see org.eclipse.jdt.internal.compiler.env.ICompilationUnit#getContents()
 */
@Override
public char[] getContents() {
	IBuffer buffer = getBufferManager().getBuffer(this);
	char[] contents = null;
	if (buffer != null) {
		contents = buffer.getCharacters();
	}
	if (buffer == null || (!isWorkingCopy() && contents == null && buffer.isClosed())) {
		// no need to force opening of CU to get the content
		// also this cannot be a working copy, as its buffer is never closed while the working copy is alive
		IFile file = (IFile) getResource();
		try {
			return Util.getResourceContentsAsCharArray(file);
		} catch (JavaModelException e) {
			if (JavaModelManager.getJavaModelManager().abortOnMissingSource.get() == Boolean.TRUE) {
				IOException ioException =
					e.getJavaModelStatus().getCode() == IJavaModelStatusConstants.IO_EXCEPTION ?
						(IOException)e.getException() :
						new IOException(e.getMessage());
				throw new AbortCompilationUnit(null, ioException, null);
			} else {
				Util.log(e);
			}
			return CharOperation.NO_CHAR;
		}
	}
	if (contents == null) { // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=129814
		if (JavaModelManager.getJavaModelManager().abortOnMissingSource.get() == Boolean.TRUE) {
			IOException ioException = new IOException(Messages.buffer_closed);
			IFile file = (IFile) getResource();
			// Get encoding from file
			String encoding;
			try {
				encoding = file.getCharset();
			} catch(CoreException ce) {
				// do not use any encoding
				encoding = null;
			}
			throw new AbortCompilationUnit(null, ioException, encoding);
		}
		return CharOperation.NO_CHAR;
	}
	return contents;
}
/**
 * A compilation unit has a corresponding resource unless it is contained
 * in a jar.
 *
 * @see IJavaElement#getCorrespondingResource()
 */
@Override
public IResource getCorrespondingResource() throws JavaModelException {
	PackageFragmentRoot root = getPackageFragmentRoot();
	if (root == null || root.isArchive()) {
		return null;
	} else {
		return getUnderlyingResource();
	}
}
/**
 * @see ICompilationUnit#getElementAt(int)
 */
@Override
public IJavaElement getElementAt(int position) throws JavaModelException {

	IJavaElement e= getSourceElementAt(position);
	if (e == this) {
		return null;
	} else {
		return e;
	}
}
@Override
public String getElementName() {
	return this.name;
}
/**
 * @see IJavaElement
 */
@Override
public int getElementType() {
	return COMPILATION_UNIT;
}
/**
 * @see org.eclipse.jdt.internal.compiler.env.IDependent#getFileName()
 */
@Override
public char[] getFileName(){
	return getPath().toString().toCharArray();
}

@Override
public IJavaElement getHandleFromMemento(String token, MementoTokenizer memento, WorkingCopyOwner workingCopyOwner) {
	switch (token.charAt(0)) {
		case JEM_IMPORTDECLARATION:
			JavaElement container = getImportContainer();
			return container.getHandleFromMemento(token, memento, workingCopyOwner);
		case JEM_PACKAGEDECLARATION:
			if (!memento.hasMoreTokens()) return this;
			String pkgName = memento.nextToken();
			JavaElement pkgDecl = getPackageDeclaration(pkgName);
			return pkgDecl.getHandleFromMemento(memento, workingCopyOwner);
		case JEM_TYPE:
			if (!memento.hasMoreTokens()) return this;
			String typeName = memento.nextToken();
			JavaElement type = (JavaElement)getType(typeName);
			return type.getHandleFromMemento(memento, workingCopyOwner);
		case JEM_MODULE:
			if (!memento.hasMoreTokens()) return this;
			String modName = memento.nextToken();
			JavaElement mod = new SourceModule(this, modName);
			return mod.getHandleFromMemento(memento, workingCopyOwner);
	}
	return null;
}

/**
 * @see JavaElement#getHandleMementoDelimiter()
 */
@Override
protected char getHandleMementoDelimiter() {
	return JavaElement.JEM_COMPILATIONUNIT;
}
/**
 * @see ICompilationUnit#getImport(String)
 */
@Override
public ImportDeclaration getImport(String importName) {
	return getImportContainer().getImport(importName);
}
/**
 * @see ICompilationUnit#getImportContainer()
 */
@Override
public ImportContainer getImportContainer() {
	return new ImportContainer(this);
}


/**
 * @see ICompilationUnit#getImports()
 */
@Override
public IImportDeclaration[] getImports() throws JavaModelException {
	IImportContainer container= getImportContainer();
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	Object info = manager.getInfo(container);
	if (info == null) {
		if (manager.getInfo(this) != null)
			// CU was opened, but no import container, then no imports
			return NO_IMPORTS;
		else {
			open(null); // force opening of CU
			info = manager.getInfo(container);
			if (info == null)
				// after opening, if no import container, then no imports
				return NO_IMPORTS;
		}
	}
	IJavaElement[] elements = ((ImportContainerInfo) info).children;
	int length = elements.length;
	IImportDeclaration[] imports = new IImportDeclaration[length];
	System.arraycopy(elements, 0, imports, 0, length);
	return imports;
}
/**
 * @see IMember#getTypeRoot()
 */
public ITypeRoot getTypeRoot() {
	return this;
}
/**
 * @see org.eclipse.jdt.internal.compiler.env.ICompilationUnit#getMainTypeName()
 */
@Override
public char[] getMainTypeName(){
	return Util.getNameWithoutJavaLikeExtension(getElementName()).toCharArray();
}
/**
 * @see IWorkingCopy#getOriginal(IJavaElement)
 * @deprecated
 */
@Override
public IJavaElement getOriginal(IJavaElement workingCopyElement) {
	// backward compatibility
	if (!isWorkingCopy()) return null;
	CompilationUnit cu = (CompilationUnit)workingCopyElement.getAncestor(COMPILATION_UNIT);
	if (cu == null || !this.owner.equals(cu.owner)) {
		return null;
	}

	return workingCopyElement.getPrimaryElement();
}
/**
 * @see IWorkingCopy#getOriginalElement()
 * @deprecated
 */
@Override
public IJavaElement getOriginalElement() {
	// backward compatibility
	if (!isWorkingCopy()) return null;

	return getPrimaryElement();
}

@Override
public WorkingCopyOwner getOwner() {
	return isPrimary() || !isWorkingCopy() ? null : this.owner;
}
/**
 * @see ICompilationUnit#getPackageDeclaration(String)
 */
@Override
public PackageDeclaration getPackageDeclaration(String pkg) {
	return new PackageDeclaration(this, pkg);
}
/**
 * @see ICompilationUnit#getPackageDeclarations()
 */
@Override
public IPackageDeclaration[] getPackageDeclarations() throws JavaModelException {
	List<IPackageDeclaration> list = getChildrenOfType(PACKAGE_DECLARATION);
	return list.toArray(IPackageDeclaration[]::new);
}
/**
 * @see org.eclipse.jdt.internal.compiler.env.ICompilationUnit#getPackageName()
 */
@Override
public char[][] getPackageName() {
	PackageFragment packageFragment = (PackageFragment) getParent();
	if (packageFragment == null) return CharOperation.NO_CHAR_CHAR;
	return Util.toCharArrays(packageFragment.names);
}

/**
 * @see IJavaElement#getPath()
 */
@Override
public IPath getPath() {
	PackageFragmentRoot root = getPackageFragmentRoot();
	if (root == null) return new Path(getElementName()); // working copy not in workspace
	if (root.isArchive()) {
		return root.getPath();
	} else {
		return getParent().getPath().append(getElementName());
	}
}
/*
 * Returns the per working copy info for the receiver, or null if none exist.
 * Note: the use count of the per working copy info is NOT incremented.
 */
public JavaModelManager.PerWorkingCopyInfo getPerWorkingCopyInfo() {
	return JavaModelManager.getJavaModelManager().getPerWorkingCopyInfo(this, false/*don't create*/, false/*don't record usage*/, null/*no problem requestor needed*/);
}

@Override
public ICompilationUnit getPrimary() {
	return (ICompilationUnit)getPrimaryElement(true);
}

@Override
public JavaElement getPrimaryElement(boolean checkOwner) {
	if (checkOwner && isPrimary()) return this;
	return new CompilationUnit((PackageFragment)getParent(), getElementName(), DefaultWorkingCopyOwner.PRIMARY);
}

@Override
public IResource resource(PackageFragmentRoot root) {
	if (root == null) return null; // working copy not in workspace
	return ((IContainer) ((Openable) this.getParent()).resource(root)).getFile(new Path(getElementName()));
}
/**
 * @see ISourceReference#getSource()
 */
@Override
public String getSource() throws JavaModelException {
	IBuffer buffer = getBuffer();
	if (buffer == null) return ""; //$NON-NLS-1$
	return buffer.getContents();
}
/**
 * @see ISourceReference#getSourceRange()
 */
@Override
public ISourceRange getSourceRange() throws JavaModelException {
	return ((CompilationUnitElementInfo) getElementInfo()).getSourceRange();
}
/**
 * @see ICompilationUnit#getType(String)
 */
@Override
public IType getType(String typeName) {
	return new SourceType(this, typeName);
}
/**
 * @see ICompilationUnit#getTypes()
 */
@Override
public IType[] getTypes() throws JavaModelException {
	ArrayList<IType> list = getChildrenOfType(TYPE);
	return list.toArray(IType[]::new);
}
/**
 * @see IJavaElement
 */
@Override
public IResource getUnderlyingResource() throws JavaModelException {
	if (isWorkingCopy() && !isPrimary()) return null;
	return super.getUnderlyingResource();
}
/**
 * @see IWorkingCopy#getSharedWorkingCopy(IProgressMonitor, IBufferFactory, IProblemRequestor)
 * @deprecated
 */
@Override
public IJavaElement getSharedWorkingCopy(IProgressMonitor pm, IBufferFactory factory, IProblemRequestor problemRequestor) throws JavaModelException {

	// if factory is null, default factory must be used
	if (factory == null) factory = getBufferManager().getDefaultBufferFactory();

	return getWorkingCopy(BufferFactoryWrapper.create(factory), problemRequestor, pm);
}
/**
 * @see IWorkingCopy#getWorkingCopy()
 * @deprecated
 */
@Override
public IJavaElement getWorkingCopy() throws JavaModelException {
	return getWorkingCopy(null);
}
/**
 * @see ICompilationUnit#getWorkingCopy(IProgressMonitor)
 */
@Override
public ICompilationUnit getWorkingCopy(IProgressMonitor monitor) throws JavaModelException {
	return getWorkingCopy(new WorkingCopyOwner() {/*non shared working copy*/}, null/*no problem requestor*/, monitor);
}
/**
 * @see ITypeRoot#getWorkingCopy(WorkingCopyOwner, IProgressMonitor)
 */
@Override
public ICompilationUnit getWorkingCopy(WorkingCopyOwner workingCopyOwner, IProgressMonitor monitor) throws JavaModelException {
	return getWorkingCopy(workingCopyOwner, null, monitor);
}
/**
 * @see IWorkingCopy#getWorkingCopy(IProgressMonitor, IBufferFactory, IProblemRequestor)
 * @deprecated
 */
@Override
public IJavaElement getWorkingCopy(IProgressMonitor monitor, IBufferFactory factory, IProblemRequestor problemRequestor) throws JavaModelException {
	return getWorkingCopy(BufferFactoryWrapper.create(factory), problemRequestor, monitor);
}
/**
 * @see ICompilationUnit#getWorkingCopy(WorkingCopyOwner, IProblemRequestor, IProgressMonitor)
 * @deprecated
 */
@Override
public ICompilationUnit getWorkingCopy(WorkingCopyOwner workingCopyOwner, IProblemRequestor problemRequestor, IProgressMonitor monitor) throws JavaModelException {
	if (!isPrimary()) return this;

	JavaModelManager manager = JavaModelManager.getJavaModelManager();

	CompilationUnit workingCopy = new CompilationUnit((PackageFragment)getParent(), getElementName(), workingCopyOwner);
	JavaModelManager.PerWorkingCopyInfo perWorkingCopyInfo =
		manager.getPerWorkingCopyInfo(workingCopy, false/*don't create*/, true/*record usage*/, null/*not used since don't create*/);
	if (perWorkingCopyInfo != null) {
		return perWorkingCopyInfo.getWorkingCopy(); // return existing handle instead of the one created above
	}
	BecomeWorkingCopyOperation op = new BecomeWorkingCopyOperation(workingCopy, problemRequestor);
	op.runOperation(monitor);
	return workingCopy;
}
/**
 * @see Openable#hasBuffer()
 */
@Override
protected boolean hasBuffer() {
	return true;
}

@Override
public boolean hasResourceChanged() {
	if (!isWorkingCopy()) return false;

	// if resource got deleted, then #getModificationStamp() will answer IResource.NULL_STAMP, which is always different from the cached
	// timestamp
	Object info = JavaModelManager.getJavaModelManager().getInfo(this);
	if (info == null) return false;
	IResource resource = getResource();
	if (resource == null) return false;
	return ((CompilationUnitElementInfo)info).timestamp != resource.getModificationStamp();
}
@Override
public boolean ignoreOptionalProblems() {
	return getPackageFragmentRoot().ignoreOptionalProblems();
}
/**
 * @see IWorkingCopy#isBasedOn(IResource)
 * @deprecated
 */
@Override
public boolean isBasedOn(IResource resource) {
	if (!isWorkingCopy()) return false;
	if (!getResource().equals(resource)) return false;
	return !hasResourceChanged();
}
/**
 * @see IOpenable#isConsistent()
 */
@Override
public boolean isConsistent() {
	return !JavaModelManager.getJavaModelManager().getElementsOutOfSynchWithBuffers().contains(this);
}
public boolean isPrimary() {
	return this.owner == DefaultWorkingCopyOwner.PRIMARY;
}
/**
 * @see Openable#isSourceElement()
 */
@Override
protected boolean isSourceElement() {
	return true;
}
protected IStatus validateCompilationUnit(IResource resource) {
	PackageFragmentRoot root = getPackageFragmentRoot();
	// root never null as validation is not done for working copies
	try {
		if (root.getKind() != IPackageFragmentRoot.K_SOURCE)
			return new JavaModelStatus(IJavaModelStatusConstants.INVALID_ELEMENT_TYPES, root);
	} catch (JavaModelException e) {
		return e.getJavaModelStatus();
	}
	if (resource != null) {
		char[][] inclusionPatterns = root.fullInclusionPatternChars();
		char[][] exclusionPatterns = root.fullExclusionPatternChars();
		if (Util.isExcluded(resource, inclusionPatterns, exclusionPatterns))
			return new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_NOT_ON_CLASSPATH, this);
		if (!resource.isAccessible())
			return new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this);
	}
    return JavaConventions.validateCompilationUnitName(getElementName(), "1.8", "1.8");
}

@Override
public boolean isWorkingCopy() {
	// For backward compatibility, non primary working copies are always returning true; in removal
	// delta, clients can still check that element was a working copy before being discarded.
	return !isPrimary() || getPerWorkingCopyInfo() != null;
}
/**
 * @see IOpenable#makeConsistent(IProgressMonitor)
 */
@Override
public void makeConsistent(IProgressMonitor monitor) throws JavaModelException {
	makeConsistent(NO_AST, false/*don't resolve bindings*/, 0 /* don't perform statements recovery */, null/*don't collect problems but report them*/, monitor);
}
public com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.dom.CompilationUnit makeConsistent(int astLevel, boolean resolveBindings, int reconcileFlags, Map<String, CategorizedProblem[]>  problems, IProgressMonitor monitor) throws JavaModelException {
	if (isConsistent()) return null;

	try {
		JavaModelManager.getJavaModelManager().abortOnMissingSource.set(Boolean.TRUE);
		// create a new info and make it the current info
		// (this will remove the info and its children just before storing the new infos)
		if (astLevel != NO_AST || problems != null) {
			ASTHolderCUInfo info = new ASTHolderCUInfo();
			info.astLevel = astLevel;
			info.resolveBindings = resolveBindings;
			info.reconcileFlags = reconcileFlags;
			info.problems = problems;
			openWhenClosed(info, true, monitor);
			com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.dom.CompilationUnit result = info.ast;
			info.ast = null;
			return astLevel != NO_AST ? result : null;
		} else {
			openWhenClosed(createElementInfo(), true, monitor);
			return null;
		}
	} finally {
		JavaModelManager.getJavaModelManager().abortOnMissingSource.remove();
	}
}

@Override
protected IBuffer openBuffer(IProgressMonitor pm, IElementInfo info) throws JavaModelException {

	// create buffer
	BufferManager bufManager = getBufferManager();
	boolean isWorkingCopy = isWorkingCopy();
	IBuffer buffer =
		isWorkingCopy
			? this.owner.createBuffer(this)
			: BufferManager.createBuffer(this);
	if (buffer == null) return null;

	ICompilationUnit original = null;
	boolean mustSetToOriginalContent = false;
	if (isWorkingCopy) {
		// ensure that isOpen() is called outside the bufManager synchronized block
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=237772
		mustSetToOriginalContent = !isPrimary() && (original = new CompilationUnit((PackageFragment)getParent(), getElementName(), DefaultWorkingCopyOwner.PRIMARY)).isOpen() ;
	}

	// synchronize to ensure that 2 threads are not putting 2 different buffers at the same time
	// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=146331
	IBuffer existingBuffer;
	synchronized(bufManager) {
		existingBuffer = bufManager.getBuffer(this);
		if (existingBuffer == null) {
			// set the buffer source
			if (buffer.getCharacters() == null) {
				if (isWorkingCopy) {
					if (mustSetToOriginalContent) {
						buffer.setContents(original.getSource());
					} else {
						IFile file = (IFile)getResource();
						if (file == null || !file.exists()) {
							// initialize buffer with empty contents
							buffer.setContents(CharOperation.NO_CHAR);
						} else {
							buffer.setContents(Util.getResourceContentsAsCharArray(file));
						}
					}
				} else {
					IFile file = (IFile)getResource();
					if (file == null || !file.exists()) throw newNotPresentException();
					buffer.setContents(Util.getResourceContentsAsCharArray(file));
				}
			}

			// add buffer to buffer cache
			// note this may cause existing buffers to be removed from the buffer cache, but only primary compilation unit's buffer
			// can be closed, thus no call to a client's IBuffer#close() can be done in this synchronized block.
			bufManager.addBuffer(buffer);

			// listen to buffer changes
			buffer.addBufferChangedListener(this);
		}
	}
	if(existingBuffer != null) {
		buffer.close();
		return existingBuffer;
	}
	return buffer;
}
/*
 * @see #cloneCachingContents()
 */
public CompilationUnit originalFromClone() {
	return this;
}

/**
 * Debugging purposes
 */
@Override
protected void toStringInfo(int tab, StringBuilder buffer, Object info, boolean showResolvedInfo) {
	if (!isPrimary()) {
		buffer.append(tabString(tab));
		buffer.append("[Working copy] "); //$NON-NLS-1$
		toStringName(buffer);
	} else {
		if (isWorkingCopy()) {
			buffer.append(tabString(tab));
			buffer.append("[Working copy] "); //$NON-NLS-1$
			toStringName(buffer);
			if (info == null) {
				buffer.append(" (not open)"); //$NON-NLS-1$
			}
		} else {
			super.toStringInfo(tab, buffer, info, showResolvedInfo);
		}
	}
}

    @Override
protected IStatus validateExistence(IResource underlyingResource) {
	// check if this compilation unit can be opened
	if (!isWorkingCopy()) { // no check is done on root kind or exclusion pattern for working copies
		IStatus status = validateCompilationUnit(underlyingResource);
		if (!status.isOK())
			return status;
	}

	// prevents reopening of non-primary working copies (they are closed when they are discarded and should not be reopened)
	if (!isPrimary() && getPerWorkingCopyInfo() == null) {
		return newDoesNotExistStatus();
	}

	return JavaModelStatus.VERIFIED_OK;
}

@Override
public ISourceRange getNameRange() {
	return null;
}


@Override
public IModuleDescription getModule() throws JavaModelException {
	if (TypeConstants.MODULE_INFO_FILE_NAME_STRING.equals(getElementName()))
		return ((CompilationUnitElementInfo) getElementInfo()).getModule();
	return null;
}

@Override
public char[] getModuleName() {
	try {
		IModuleDescription module = getModule();
		if (module == null) {
			JavaProject project = (JavaProject) getAncestor(IJavaElement.JAVA_PROJECT);
			module = project.getModuleDescription();
		}
		if (module != null)
			return module.getElementName().toCharArray();
	} catch (JavaModelException e) {
		if (JavaModelManager.VERBOSE) {
			JavaModelManager.trace("", e); //$NON-NLS-1$
		}
	}
	return null;
}

@Override
public void setOptions(Map<String, String> newOptions) {
	Map<String, String> customOptions = newOptions == null ? null : new ConcurrentHashMap<>(newOptions);
	try {
		this.getCompilationUnitElementInfo().setCustomOptions(customOptions);
	} catch (JavaModelException e) {
		// do nothing
	}
}

@Override
public Map<String, String> getCustomOptions() {
	if (this.owner != null) {
		try {
			Map<String, String> customOptions = this.getCompilationUnitElementInfo().getCustomOptions();
			IJavaProject parentProject = getJavaProject();
			Map<String, String> parentOptions = parentProject == null ? JavaCore.getOptions() : parentProject.getOptions(true);
			if (JavaCore.ENABLED.equals(parentOptions.get(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES)) &&
				CompilerOptions.versionToJdkLevel(parentOptions.getOrDefault(JavaCore.COMPILER_SOURCE, JavaCore.latestSupportedJavaVersion())) < ClassFileConstants.getLatestJDKLevel()) {
				// Disable preview features for older Java releases as it causes the compiler to fail later
				if (customOptions != null) {
					customOptions.put(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
				} else {
					customOptions = Map.of(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
				}
			}
			return customOptions == null ? Collections.emptyMap() : customOptions;
		} catch (JavaModelException e) {
			// do nothing
		}
	}

	return Collections.emptyMap();
}

private CompilationUnitElementInfo getCompilationUnitElementInfo() throws JavaModelException {
	return (CompilationUnitElementInfo) this.getElementInfo();
}
}
