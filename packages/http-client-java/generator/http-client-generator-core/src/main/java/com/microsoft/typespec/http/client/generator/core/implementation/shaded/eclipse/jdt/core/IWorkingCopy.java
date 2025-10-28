/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IMarker;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;

/**
 * Common protocol for Java elements that support working copies.
 * <p>
 * A working copy of a Java element acts just like a regular element (handle),
 * except it is not attached to an underlying resource. A working copy is not
 * visible to the rest of the Java model. Changes in a working copy's
 * buffer are not realized in a resource. To bring the Java model up-to-date with a working
 * copy's contents, an explicit commit must be performed on the working copy.
 * Other operations performed on a working copy update the
 * contents of the working copy's buffer but do not commit the contents
 * of the working copy.
 * </p>
 * <p>
 * Note: The contents of a working copy is determined when a working
 * copy is created, based on the current content of the element the working
 * copy is created from. If a working copy is an <code>IOpenable</code> and is explicitly
 * closed, the working copy's buffer will be thrown away. However, clients should not
 * explicitly open and close working copies.
 * </p>
 * <p>
 * The client that creates a working copy is responsible for
 * destroying the working copy. The Java model will never automatically
 * destroy or close a working copy. (Note that destroying a working copy
 * does not commit it to the model, it only frees up the memory occupied by
 * the element). After a working copy is destroyed, the working copy cannot
 * be accessed again. Non-handle methods will throw a
 * <code>JavaModelException</code> indicating the Java element does not exist.
 * </p>
 * <p>
 * A working copy cannot be created from another working copy.
 * Calling <code>getWorkingCopy</code> on a working copy returns the receiver.
 * </p>
 *
 * @deprecated Use {@link ICompilationUnit} instead
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IWorkingCopy {

    /**
     * Finds the shared working copy for this element, given a <code>IBuffer</code> factory.
     * If no working copy has been created for this element associated with this
     * buffer factory, returns <code>null</code>.
     * <p>
     * Users of this method must not destroy the resulting working copy.
     *
     * @param bufferFactory the given <code>IBuffer</code> factory
     * @return the found shared working copy for this element, <code>null</code> if none
     * @see IBufferFactory
     * @since 2.0
     *
     * @deprecated Use {@link ICompilationUnit#findWorkingCopy(WorkingCopyOwner)} instead.
     */
    IJavaElement findSharedWorkingCopy(IBufferFactory bufferFactory);

    /**
     * Returns the original element the specified working copy element was created from,
     * or <code>null</code> if this is not a working copy element. This is a handle
     * only method, the returned element may or may not exist.
     *
     * @param workingCopyElement the specified working copy element
     * @return the original element the specified working copy element was created from,
     * or <code>null</code> if this is not a working copy element
     *
     * @deprecated Use {@link IJavaElement#getPrimaryElement()} instead.
     */
    IJavaElement getOriginal(IJavaElement workingCopyElement);

    /**
     * Returns the original element this working copy was created from,
     * or <code>null</code> if this is not a working copy.
     *
     * @return the original element this working copy was created from,
     * or <code>null</code> if this is not a working copy
     *
     * @deprecated Use {@link ICompilationUnit#getPrimaryElement()} instead.
     */
    IJavaElement getOriginalElement();

    /**
     * Finds the elements in this compilation unit that correspond to
     * the given element.
     * An element A corresponds to an element B if:
     * <ul>
     * <li>A has the same element name as B.
     * <li>If A is a method, A must have the same number of arguments as
     * B and the simple names of the argument types must be equals.
     * <li>The parent of A corresponds to the parent of B recursively up to
     * their respective compilation units.
     * <li>A exists.
     * </ul>
     * Returns <code>null</code> if no such java elements can be found
     * or if the given element is not included in a compilation unit.
     *
     * @param element the given element
     * @return the found elements in this compilation unit that correspond to the given element
     * @since 2.0
     *
     * @deprecated Use {@link ICompilationUnit#findElements(IJavaElement)} instead.
     */
    IJavaElement[] findElements(IJavaElement element);

    /**
     * Finds the primary type of this compilation unit (that is, the type with the same name as the
     * compilation unit), or <code>null</code> if no such a type exists.
     *
     * @return the found primary type of this compilation unit, or <code>null</code> if no such a type exists
     * @since 2.0
     *
     * @deprecated Use {@link ITypeRoot#findPrimaryType()} instead.
     */
    IType findPrimaryType();

    /**
     * Returns a shared working copy on this element using the given factory to create
     * the buffer, or this element if this element is already a working copy.
     * This API can only answer an already existing working copy if it is based on the same
     * original compilation unit AND was using the same buffer factory (that is, as defined by
     * <code>Object.equals</code>).
     * <p>
     * The life time of a shared working copy is as follows:
     * <ul>
     * <li>The first call to <code>getSharedWorkingCopy(...)</code> creates a new working copy for this
     * element</li>
     * <li>Subsequent calls increment an internal counter.</li>
     * <li>A call to <code>destroy()</code> decrements the internal counter.</li>
     * <li>When this counter is 0, the working copy is destroyed.
     * </ul>
     * So users of this method must destroy exactly once the working copy.
     * <p>
     * Note that the buffer factory will be used for the life time of this working copy, that is if the
     * working copy is closed then reopened, this factory will be used.
     * The buffer will be automatically initialized with the original's compilation unit content
     * upon creation.
     * <p>
     * When the shared working copy instance is created, an ADDED IJavaElementDelta is reported on this
     * working copy.
     *
     * @param monitor a progress monitor used to report progress while opening this compilation unit
     * or <code>null</code> if no progress should be reported
     * @param factory the factory that creates a buffer that is used to get the content of the working copy
     * or <code>null</code> if the internal factory should be used
     * @param problemRequestor a requestor which will get notified of problems detected during
     * reconciling as they are discovered. The requestor can be set to <code>null</code> indicating
     * that the client is not interested in problems.
     * @exception JavaModelException if the contents of this element can
     * not be determined.
     * @return a shared working copy on this element using the given factory to create
     * the buffer, or this element if this element is already a working copy
     * @see IBufferFactory
     * @see IProblemRequestor
     * @since 2.0
     *
     * @deprecated Use {@link ICompilationUnit#getWorkingCopy(WorkingCopyOwner, IProblemRequestor, IProgressMonitor)}
     * instead.
     */
    IJavaElement getSharedWorkingCopy(IProgressMonitor monitor, IBufferFactory factory,
        IProblemRequestor problemRequestor) throws JavaModelException;

    /**
     * Returns a new working copy of this element if this element is not
     * a working copy, or this element if this element is already a working copy.
     * <p>
     * Note: if intending to share a working copy amongst several clients, then
     * <code>#getSharedWorkingCopy</code> should be used instead.
     * </p><p>
     * When the working copy instance is created, an ADDED IJavaElementDelta is
     * reported on this working copy.
     * </p><p>
     * Since 2.1, a working copy can be created on a not-yet existing compilation
     * unit. In particular, such a working copy can then be committed in order to create
     * the corresponding compilation unit.
     * </p>
     * 
     * @exception JavaModelException if the contents of this element can
     * not be determined.
     * @return a new working copy of this element if this element is not
     * a working copy, or this element if this element is already a working copy
     *
     * @deprecated Use {@link ICompilationUnit#getWorkingCopy(IProgressMonitor)} instead.
     */
    IJavaElement getWorkingCopy() throws JavaModelException;

    /**
     * Returns a new working copy of this element using the given factory to create
     * the buffer, or this element if this element is already a working copy.
     * Note that this factory will be used for the life time of this working copy, that is if the
     * working copy is closed then reopened, this factory will be reused.
     * The buffer will be automatically initialized with the original's compilation unit content
     * upon creation.
     * <p>
     * Note: if intending to share a working copy amongst several clients, then
     * <code>#getSharedWorkingCopy</code> should be used instead.
     * </p><p>
     * When the working copy instance is created, an ADDED IJavaElementDelta is
     * reported on this working copy.
     * </p><p>
     * Since 2.1, a working copy can be created on a not-yet existing compilation
     * unit. In particular, such a working copy can then be committed in order to create
     * the corresponding compilation unit.
     * </p>
     * 
     * @param monitor a progress monitor used to report progress while opening this compilation unit
     * or <code>null</code> if no progress should be reported
     * @param factory the factory that creates a buffer that is used to get the content of the working copy
     * or <code>null</code> if the internal factory should be used
     * @param problemRequestor a requestor which will get notified of problems detected during
     * reconciling as they are discovered. The requestor can be set to <code>null</code> indicating
     * that the client is not interested in problems.
     * @exception JavaModelException if the contents of this element can
     * not be determined.
     * @return a new working copy of this element using the given factory to create
     * the buffer, or this element if this element is already a working copy
     * @since 2.0
     *
     * @deprecated Use {@link ICompilationUnit#getWorkingCopy(WorkingCopyOwner, IProblemRequestor, IProgressMonitor)}
     * instead.
     */
    IJavaElement getWorkingCopy(IProgressMonitor monitor, IBufferFactory factory, IProblemRequestor problemRequestor)
        throws JavaModelException;

    /**
     * Returns whether this working copy's original element's content
     * has not changed since the inception of this working copy.
     *
     * @param resource this working copy's resource
     * @return true if this working copy's original element's content
     * has not changed since the inception of this working copy, false otherwise
     *
     * @deprecated Use {@link ICompilationUnit#hasResourceChanged()} instead.
     */
    boolean isBasedOn(IResource resource);

    /**
     * Returns whether this element is a working copy.
     *
     * @return true if this element is a working copy, false otherwise
     *
     * @deprecated Use {@link ICompilationUnit#isWorkingCopy()} instead.
     */
    boolean isWorkingCopy();
}
