/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.*;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.dom.ASTNode;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.dom.CompilationUnit;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.DOMFinder;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.MementoTokenizer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.Util;

/**
 * Abstract class for Java elements which implement ISourceReference.
 */
public abstract class SourceRefElement extends JavaElement implements ISourceReference {
    /*
     * A count to uniquely identify this element in the case
     * that a duplicate named element exists. For example, if
     * there are two fields in a compilation unit with the
     * same name, the occurrence count is used to distinguish
     * them. The occurrence count starts at 1 (thus the first
     * occurrence is occurrence 1, not occurrence 0).
     */
    private int occurrenceCount; // XXX should be final

    protected SourceRefElement(JavaElement parent) {
        this(parent, 1);
    }

    protected SourceRefElement(JavaElement parent, int occurrenceCount) {
        super(parent);
        // 0 is not valid: this first occurrence is occurrence 1.
        if (occurrenceCount <= 0)
            throw new IllegalArgumentException(Integer.toString(occurrenceCount));
        this.occurrenceCount = occurrenceCount;
    }

    /**
     * This element is being closed. Do any necessary cleanup.
     */
    @Override
    protected void closing(Object info) throws JavaModelException {
        // Do any necessary cleanup
    }

    /**
     * Returns a new element info for this element.
     */
    @Override
    protected JavaElementInfo createElementInfo() {
        return null; // not used for source ref elements
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SourceRefElement other))
            return false;
        return this.occurrenceCount == other.occurrenceCount && super.equals(o);
    }

    @Override
    protected int calculateHashCode() {
        return Util.combineHashCodes(super.calculateHashCode(), this.occurrenceCount);
    }

    /**
     * Returns the <code>ASTNode</code> that corresponds to this <code>JavaElement</code>
     * or <code>null</code> if there is no corresponding node.
     */
    @Override
    public ASTNode findNode(CompilationUnit ast) {
        DOMFinder finder = new DOMFinder(ast, this, false);
        try {
            return finder.search();
        } catch (JavaModelException e) {
            // receiver doesn't exist
            return null;
        }
    }

    public IAnnotation getAnnotation(String name) {
        return new Annotation(this, name);
    }

    public IAnnotation[] getAnnotations() throws JavaModelException {
        AnnotatableInfo info = (AnnotatableInfo) getElementInfo();
        return info.annotations;
    }

    /**
     * @see IMember
     */
    @Override
    public ICompilationUnit getCompilationUnit() {
        return (ICompilationUnit) getAncestor(COMPILATION_UNIT);
    }

    /**
     * Elements within compilation units and class files have no
     * corresponding resource.
     *
     * @see IJavaElement
     */
    @Override
    public IResource getCorrespondingResource() throws JavaModelException {
        if (!exists())
            throw newNotPresentException();
        return null;
    }

    /*
     * @see JavaElement
     */
    @Override
    public IJavaElement getHandleFromMemento(String token, MementoTokenizer memento,
        WorkingCopyOwner workingCopyOwner) {
        switch (token.charAt(0)) {
            case JEM_COUNT:
                return getHandleUpdatingCountFromMemento(memento, workingCopyOwner);
        }
        return this;
    }

    @Override
    protected void getHandleMemento(StringBuilder buff) {
        super.getHandleMemento(buff);
        if (this.occurrenceCount > 1) {
            buff.append(JEM_COUNT);
            buff.append(this.occurrenceCount);
        }
    }

    /*
     * Update the occurence count of the receiver and creates a Java element handle from the given memento.
     * The given working copy owner is used only for compilation unit handles.
     */
    public IJavaElement getHandleUpdatingCountFromMemento(MementoTokenizer memento, WorkingCopyOwner owner) {
        if (!memento.hasMoreTokens())
            return this;
        this.occurrenceCount = Integer.parseInt(memento.nextToken());
        if (!memento.hasMoreTokens())
            return this;
        String token = memento.nextToken();
        return getHandleFromMemento(token, memento, owner);
    }

    /*
     * @see IMember#getOccurrenceCount()
     */
    public int getOccurrenceCount() {
        return this.occurrenceCount;
    }

    public void incOccurrenceCount() {
        this.occurrenceCount++;
        resetHashCode();
    }

    public void setOccurrenceCount(int occurrenceCount) {
        this.occurrenceCount = occurrenceCount;
        resetHashCode();
    }

    /**
     * Return the first instance of IOpenable in the hierarchy of this
     * type (going up the hierarchy from this type);
     */
    @Override
    public IOpenable getOpenableParent() {
        IJavaElement current = getParent();
        while (current != null) {
            if (current instanceof IOpenable) {
                return (IOpenable) current;
            }
            current = current.getParent();
        }
        return null;
    }

    /*
     * @see IJavaElement
     */
    @Override
    public IPath getPath() {
        return getParent().getPath();
    }

    /*
     * @see IJavaElement
     */
    @Override
    public IResource resource() {
        return this.getParent().resource();
    }

    /**
     * @see ISourceReference
     */
    @Override
    public String getSource() throws JavaModelException {
        IOpenable openable = getOpenableParent();
        IBuffer buffer = openable.getBuffer();
        if (buffer == null) {
            return null;
        }
        ISourceRange range = getSourceRange();
        int offset = range.getOffset();
        int length = range.getLength();
        if (offset == -1 || length == 0) {
            return null;
        }
        try {
            return buffer.getText(offset, length);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * @see ISourceReference
     */
    @Override
    public ISourceRange getSourceRange() throws JavaModelException {
        SourceRefElementInfo info = (SourceRefElementInfo) getElementInfo();
        return info.getSourceRange();
    }

    /**
     * @see IJavaElement
     */
    @Override
    public IResource getUnderlyingResource() throws JavaModelException {
        if (!exists())
            throw newNotPresentException();
        return getParent().getUnderlyingResource();
    }

    /**
     * @see IParent
     */
    @Override
    public boolean hasChildren() throws JavaModelException {
        return getChildren().length > 0;
    }

    /**
     * @see IJavaElement
     */
    @Override
    public boolean isStructureKnown() throws JavaModelException {
        // structure is always known inside an openable
        return true;
    }

    @Override
    protected void toStringName(StringBuilder buffer) {
        super.toStringName(buffer);
        if (this.occurrenceCount > 1) {
            buffer.append("#"); //$NON-NLS-1$
            buffer.append(this.occurrenceCount);
        }
    }
}
