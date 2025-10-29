/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
 *     Stephan Herrmann - Contribution for
 *								Bug 458577 - IClassFile.getWorkingCopy() may lead to NPE in BecomeWorkingCopyOperation
 *								Bug 440477 - [null] Infrastructure for feeding external annotations into compilation
 *								Bug 462768 - [null] NPE when using linked folder for external annotations
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IContainer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IFile;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IFolder;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.OperationCanceledException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Path;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.*;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.util.SuffixConstants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.Util;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Common parts of ClassFile (containing a BinaryType) and ModularClassFile (containing a BinaryModule).
 * Prior to Java 9, most of this content was directly in ClassFile.
 */
public abstract class AbstractClassFile extends Openable implements IClassFile, SuffixConstants {

    protected final String name;

    protected AbstractClassFile(PackageFragment parent, String nameWithoutExtension) {
        super(parent);
        this.name = nameWithoutExtension;
    }

    /**
     * Returns a new element info for this element.
     */
    @Override
    protected ClassFileInfo createElementInfo() {
        return new ClassFileInfo();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AbstractClassFile other))
            return false;
        return this.name.equals(other.name) && this.getParent().equals(other.getParent());
    }

    @Override
    protected int calculateHashCode() {
        return Util.combineHashCodes(this.name.hashCode(), this.getParent().hashCode());
    }

    /**
     * Finds the deepest <code>IJavaElement</code> in the hierarchy of
     * <code>elt</code>'s children (including <code>elt</code> itself)
     * which has a source range that encloses <code>position</code>
     * according to <code>mapper</code>.
     */
    protected IJavaElement findElement(IJavaElement elt, int position, SourceMapper mapper) {
        SourceRange range = mapper.getSourceRange(elt);
        if (range == null || position < range.getOffset() || range.getOffset() + range.getLength() - 1 < position) {
            return null;
        }
        if (elt instanceof IParent) {
            try {
                IJavaElement[] children = ((IParent) elt).getChildren();
                for (IJavaElement child : children) {
                    IJavaElement match = findElement(child, position, mapper);
                    if (match != null) {
                        return match;
                    }
                }
            } catch (JavaModelException npe) {
                // elt doesn't exist: return the element
            }
        }
        return elt;
    }

    @Override
    public byte[] getBytes() throws JavaModelException {
        JavaElement pkg = getParent();
        if (pkg instanceof JarPackageFragment) {
            JarPackageFragmentRoot root = (JarPackageFragmentRoot) pkg.getParent();
            try {
                String entryName = Util.concatWith(((PackageFragment) pkg).names, getElementName(), '/');
                entryName = root.getClassFilePath(entryName);
                return getClassFileContent(root, entryName);
                // Java 9 - The below exception is not thrown in new scheme of things. Could cause issues?
                // throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST,
                // this));
            } catch (IOException ioe) {
                throw new JavaModelException(ioe, IJavaModelStatusConstants.IO_EXCEPTION);
            } catch (CoreException e) {
                if (e instanceof JavaModelException) {
                    throw (JavaModelException) e;
                } else {
                    throw new JavaModelException(e);
                }
            }
        } else {
            IFile file = (IFile) resource();
            return Util.getResourceContentsAsByteArray(file);
        }
    }

    protected byte[] getClassFileContent(JarPackageFragmentRoot root, String className)
        throws CoreException, IOException {
        byte[] contents = null;
        String rootPath = root.getPath().toOSString();
        if (com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.util.Util
            .isJrt(rootPath)) {
            contents
                = com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.util.JRTUtil
                    .getClassfileContent(new File(rootPath), className, root.getElementName());
        } else {
            ZipFile zip = root.getJar();
            try {
                ZipEntry ze = zip.getEntry(className);
                if (ze != null) {
                    contents
                        = com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.util.Util
                            .getZipEntryByteContent(ze, zip);
                }
            } finally {
                JavaModelManager.getJavaModelManager().closeZipFile(zip);
            }
        }
        if (contents == null && Thread.interrupted()) // reading from JRT is interruptible
            throw new OperationCanceledException();
        return contents;
    }

    @Override
    public IBuffer getBuffer() throws JavaModelException {
        IStatus status = validateClassFile();
        if (status.isOK()) {
            return super.getBuffer();
        } else {
            switch (status.getCode()) {
                case IJavaModelStatusConstants.ELEMENT_NOT_ON_CLASSPATH: // don't throw a JavaModelException to be able
                                                                         // to open .class file outside the classpath
                                                                         // (see
                                                                         // https://bugs.eclipse.org/bugs/show_bug.cgi?id=138507
                                                                         // )
                case IJavaModelStatusConstants.INVALID_ELEMENT_TYPES: // don't throw a JavaModelException to be able to
                                                                      // open .class file in proj==src case without
                                                                      // source (see
                                                                      // https://bugs.eclipse.org/bugs/show_bug.cgi?id=221904
                                                                      // )
                    return null;

                default:
                    throw new JavaModelException(status);
            }
        }
    }

    /**
     * @see IMember#getTypeRoot()
     */
    public ITypeRoot getTypeRoot() {
        return this;
    }

    /**
     * A class file has a corresponding resource unless it is contained
     * in a jar.
     *
     * @see IJavaElement
     */
    @Override
    public IResource getCorrespondingResource() throws JavaModelException {
        IPackageFragmentRoot root = (IPackageFragmentRoot) getParent().getParent();
        if (root.isArchive()) {
            return null;
        } else {
            return getUnderlyingResource();
        }
    }

    @Override
    public String getElementName() {
        return this.name + SuffixConstants.SUFFIX_STRING_class;
    }

    /**
     * @see IJavaElement
     */
    @Override
    public int getElementType() {
        return CLASS_FILE;
    }

    /*
     * @see IJavaElement
     */
    @Override
    public IPath getPath() {
        PackageFragmentRoot root = getPackageFragmentRoot();
        if (root.isArchive()) {
            return root.getPath();
        } else {
            return getParent().getPath().append(getElementName());
        }
    }

    /*
     * @see IJavaElement
     */
    @Override
    public IResource resource(PackageFragmentRoot root) {
        return ((IContainer) ((Openable) this.getParent()).resource(root)).getFile(new Path(getElementName()));
    }

    /**
     * @see ISourceReference
     */
    @Override
    public String getSource() throws JavaModelException {
        IBuffer buffer = getBuffer();
        if (buffer == null) {
            return null;
        }
        return buffer.getContents();
    }

    /**
     * @see ISourceReference
     */
    @Override
    public ISourceRange getSourceRange() throws JavaModelException {
        IBuffer buffer = getBuffer();
        if (buffer != null) {
            String contents = buffer.getContents();
            if (contents == null)
                return null;
            return new SourceRange(0, contents.length());
        } else {
            return null;
        }
    }

    /**
     * @see Openable
     */
    @Override
    protected boolean hasBuffer() {
        return true;
    }

    /**
     * Returns true - class files are always read only.
     */
    @Override
    public boolean isReadOnly() {
        return true;
    }

    private IStatus validateClassFile() {
        IPackageFragmentRoot root = getPackageFragmentRoot();
        try {
            if (root.getKind() != IPackageFragmentRoot.K_BINARY)
                return new JavaModelStatus(IJavaModelStatusConstants.INVALID_ELEMENT_TYPES, root);
        } catch (JavaModelException e) {
            return e.getJavaModelStatus();
        }
        return JavaConventions.validateClassFileName(getElementName(), "1.8", "1.8");
    }

    @Override
    protected IStatus validateExistence(IResource underlyingResource) {
        // check whether the class file can be opened
        IStatus status = validateClassFile();
        if (!status.isOK())
            return status;
        if (underlyingResource != null) {
            if (!underlyingResource.isAccessible())
                return newDoesNotExistStatus();
            PackageFragmentRoot root;
            if ((underlyingResource instanceof IFolder) && (root = getPackageFragmentRoot()).isArchive()) { // see
                                                                                                            // https://bugs.eclipse.org/bugs/show_bug.cgi?id=204652
                return root.newDoesNotExistStatus();
            }
        }
        return JavaModelStatus.VERIFIED_OK;
    }

    @Override
    public ISourceRange getNameRange() {
        return null;
    }
}
