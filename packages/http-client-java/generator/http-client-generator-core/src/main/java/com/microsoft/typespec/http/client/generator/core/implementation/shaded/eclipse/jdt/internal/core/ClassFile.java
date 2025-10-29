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
 *     Stephan Herrmann - Contribution for
 *								Bug 458577 - IClassFile.getWorkingCopy() may lead to NPE in BecomeWorkingCopyOperation
 *								Bug 440477 - [null] Infrastructure for feeding external annotations into compilation
 *								Bug 462768 - [null] NPE when using linked folder for external annotations
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IWorkspaceRoot;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Path;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Status;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IBuffer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IClassFile;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IClasspathEntry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.ICompilationUnit;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaModelStatusConstants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IMember;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IOrdinaryClassFile;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IPackageFragmentRoot;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IType;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.ITypeRoot;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaCore;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaModelException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.WorkingCopyOwner;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.classfmt.ExternalAnnotationDecorator;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.classfmt.ExternalAnnotationProvider;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.IBinaryType;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.IDependent;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.IElementInfo;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.IModule;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.util.SuffixConstants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.nd.java.model.BinaryTypeDescriptor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.nd.java.model.BinaryTypeFactory;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.DeduplicationUtil;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.MementoTokenizer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.Util;
import java.io.IOException;
import java.util.zip.ZipFile;

/**
 * @see IClassFile
 */

public class ClassFile extends AbstractClassFile implements IOrdinaryClassFile {

    protected BinaryType binaryType = null;

    private IPath externalAnnotationBase;
    final String typeName;

    /*
     * Creates a handle to a class file.
     */
    protected ClassFile(PackageFragment parent, String nameWithoutExtension) {
        super(parent, nameWithoutExtension);
        // Internal class file name doesn't contain ".class" file extension
        int lastDollar = this.name.lastIndexOf('$');
        this.typeName = lastDollar > -1
            ? DeduplicationUtil.intern(Util.localTypeName(this.name, lastDollar, this.name.length()))
            : this.name;
    }

    public boolean existsUsingJarTypeCache() {
        if (getPackageFragmentRoot().isArchive()) {
            JavaModelManager manager = JavaModelManager.getJavaModelManager();
            IType type = getType();
            IElementInfo info = manager.getInfo(type);
            if (info == JavaModelCache.NON_EXISTING_JAR_TYPE_INFO)
                return false;
            else if (info != null)
                return true;
            // info is null
            JavaElementInfo parentInfo = (JavaElementInfo) manager.getInfo(getParent());
            if (parentInfo != null) {
                // if parent is open, this class file must be in its children
                IJavaElement[] children = parentInfo.getChildren();
                for (IJavaElement child : children) {
                    if (child instanceof ClassFile && this.name.equals(((ClassFile) child).name))
                        return true;
                }
                return false;
            }
            try {
                info = getJarBinaryTypeInfo();
            } catch (CoreException | IOException | ClassFormatException e) {
                // leave info null
            }
            manager.putJarTypeInfo(type, info == null ? JavaModelCache.NON_EXISTING_JAR_TYPE_INFO : info);
            return info != null;
        } else
            return exists();
    }

    /**
     * @see ITypeRoot#findPrimaryType()
     */
    @Override
    public IType findPrimaryType() {
        IType primaryType = getType();
        if (primaryType.exists()) {
            return primaryType;
        }
        return null;
    }

    @Override
    public String getAttachedJavadoc(IProgressMonitor monitor) throws JavaModelException {
        return getType().getAttachedJavadoc(monitor);
    }

    /**
     * Returns the <code>ClassFileReader</code>specific for this IClassFile, based
     * on its underlying resource, or <code>null</code> if unable to create
     * the diet class file.
     * There are two cases to consider:<ul>
     * <li>a class file corresponding to an IFile resource</li>
     * <li>a class file corresponding to a zip entry in a JAR</li>
     * </ul>
     *
     * @exception JavaModelException when the IFile resource or JAR is not available
     * or when this class file is not present in the JAR
     */
    public IBinaryType getBinaryTypeInfo() throws JavaModelException {
        try {
            IBinaryType info = getJarBinaryTypeInfo();
            if (info == null) {
                throw newNotPresentException();
            }
            return info;
        } catch (ClassFormatException cfe) {
            // the structure remains unknown
            return null;
        } catch (IOException ioe) {
            throw new JavaModelException(ioe, IJavaModelStatusConstants.IO_EXCEPTION);
        } catch (CoreException e) {
            if (e instanceof JavaModelException) {
                throw (JavaModelException) e;
            } else {
                throw new JavaModelException(e);
            }
        }
    }

    public String getName() {
        return this.name;
    }

    private IBinaryType getJarBinaryTypeInfo() throws CoreException, IOException, ClassFormatException {
        BinaryTypeDescriptor descriptor = BinaryTypeFactory.createDescriptor(this);

        if (descriptor == null) {
            return null;
        }
        IBinaryType result = null;
        IPackageFragmentRoot root = getPackageFragmentRoot();
        if (getPackageFragmentRoot() instanceof JarPackageFragmentRoot) {
            if (root instanceof JrtPackageFragmentRoot || this.name.equals(IModule.MODULE_INFO)) {
                PackageFragment pkg = (PackageFragment) getParent();
                JarPackageFragmentRoot jarRoot = (JarPackageFragmentRoot) getPackageFragmentRoot();
                String entryName = jarRoot.getClassFilePath(Util.concatWith(pkg.names, getElementName(), '/'));
                byte[] contents = getClassFileContent(jarRoot, entryName);
                if (contents != null) {
                    String fileName = root.getHandleIdentifier() + IDependent.JAR_FILE_ENTRY_SEPARATOR + entryName;
                    result = new ClassFileReader(contents, fileName.toCharArray(), false);
                }
            } else {
                result = BinaryTypeFactory.readType(descriptor, null);
            }
        } else {
            result = BinaryTypeFactory.readType(descriptor, null);
        }

        if (result == null) {
            return null;
        }

        // TODO(sxenos): setup the external annotation provider if the IBinaryType came from the index
        if (root.getKind() == IPackageFragmentRoot.K_BINARY) {
            JavaProject javaProject = (JavaProject) getAncestor(IJavaElement.JAVA_PROJECT);
            IClasspathEntry entry;
            try {
                entry = javaProject.getClasspathEntryFor(getPath());
            } catch (JavaModelException jme) {
                // Access via cached ClassFile/PF/PFR of a closed project?
                // Ignore and continue with result undecorated
                return result;
            }
            if (entry != null) {
                PackageFragment pkg = (PackageFragment) getParent();
                String entryName = Util.concatWith(pkg.names, getElementName(), '/');
                entryName
                    = new String(Util.concat(BinaryTypeFactory.fieldDescriptorToBinaryName(descriptor.fieldDescriptor),
                        SuffixConstants.SUFFIX_CLASS));
                IProject project = javaProject.getProject();
                IPath externalAnnotationPath = entry.getExternalAnnotationPath(project, false); // unresolved for use in
                                                                                                // ExternalAnnotationTracker
                if (externalAnnotationPath != null) {
                    result = setupExternalAnnotationProvider(project, externalAnnotationPath, result,
                        entryName.substring(0, entryName.length() - SuffixConstants.SUFFIX_CLASS.length));
                } else if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                    result = new ExternalAnnotationDecorator(result, true);
                }
            }
        }

        return result;
    }

    private IBinaryType setupExternalAnnotationProvider(IProject project, final IPath externalAnnotationPath,
        IBinaryType reader, final String typeName) {
        IBinaryType result = reader;
        // try resolve path within the workspace:
        IWorkspaceRoot root = project.getWorkspace().getRoot();
        IResource resource;
        if (externalAnnotationPath.segmentCount() == 1) {
            resource = root.getProject(externalAnnotationPath.lastSegment());
        } else {
            resource = root.getFolder(externalAnnotationPath);
            if (!resource.exists())
                resource = root.getFile(externalAnnotationPath);
        }
        String resolvedPath;
        if (resource.exists()) {
            if (resource.isVirtual()) {
                Util.log(new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, "Virtual resource " + externalAnnotationPath //$NON-NLS-1$
                    + " cannot be used as annotationpath for project " + project.getName()));  //$NON-NLS-1$
                return reader;
            }
            resolvedPath = resource.getLocation().toString(); // workspace lookup succeeded -> resolve it
        } else {
            resolvedPath = externalAnnotationPath.toString(); // not in workspace, use as is
        }
        ZipFile annotationZip = null;
        try {
            annotationZip = ExternalAnnotationDecorator.getAnnotationZipFile(resolvedPath, () -> {
                try {
                    return JavaModelManager.getJavaModelManager().getZipFile(externalAnnotationPath); // use (absolute,
                                                                                                      // but) unresolved
                                                                                                      // path here
                } catch (CoreException e) {
                    throw new IOException(
                        "Failed to read annotation file for " + typeName + " from " + externalAnnotationPath.toString(), //$NON-NLS-1$ //$NON-NLS-2$
                        e);
                }
            });

            ExternalAnnotationProvider annotationProvider
                = ExternalAnnotationDecorator.externalAnnotationProvider(resolvedPath, typeName, annotationZip);
            result = new ExternalAnnotationDecorator(reader, annotationProvider);
        } catch (IOException e) {
            Util.log(e);
            return result;
        } finally {
            if (annotationZip != null)
                JavaModelManager.getJavaModelManager().closeZipFile(annotationZip);
        }
        if (annotationZip == null) {
            // Additional change listening for individual types only when annotations are in individual files.
            // Note that we also listen for classes that don't yet have an annotation file, to detect its creation
            this.externalAnnotationBase = externalAnnotationPath; // remember so we can unregister later
            ExternalAnnotationTracker.registerClassFile(externalAnnotationPath, new Path(typeName), this);
        }
        return result;
    }

    void closeAndRemoveFromJarTypeCache() throws JavaModelException {
        super.close();
        // triggered when external annotations have changed we need to recreate this class file
        JavaModelManager.getJavaModelManager().removeFromJarTypeCache(this.binaryType);
    }

    @Override
    public void close() throws JavaModelException {
        if (this.externalAnnotationBase != null) {
            String entryName = Util.concatWith(((PackageFragment) getParent()).names, this.name, '/');
            ExternalAnnotationTracker.unregisterClassFile(this.externalAnnotationBase, new Path(entryName));
        }
        super.close();
    }

    /**
     * @see IMember
     */
    @Override
    public ClassFile getClassFile() {
        return this;
    }

    /**
     * @see IClassFile
     */
    @Override
    public IJavaElement getElementAt(int position) throws JavaModelException {
        IJavaElement parentElement = getParent();
        while (parentElement.getElementType() != IJavaElement.PACKAGE_FRAGMENT_ROOT) {
            parentElement = parentElement.getParent();
        }
        PackageFragmentRoot root = (PackageFragmentRoot) parentElement;
        SourceMapper mapper = root.getSourceMapper();
        if (mapper == null) {
            return null;
        } else {
            // ensure this class file's buffer is open so that source ranges are computed
            getBuffer();

            IType type = getType();
            return findElement(type, position, mapper);
        }
    }

    /*
     * @see JavaElement
     */
    @Override
    public IJavaElement getHandleFromMemento(String token, MementoTokenizer memento, WorkingCopyOwner owner) {
        switch (token.charAt(0)) {
            case JEM_TYPE:
                if (!memento.hasMoreTokens())
                    return this;
                String newtypeName = memento.nextToken();
                JavaElement type = new BinaryType(this, DeduplicationUtil.intern(newtypeName));
                return type.getHandleFromMemento(memento, owner);
        }
        return null;
    }

    /**
     * @see JavaElement#getHandleMemento()
     */
    @Override
    protected char getHandleMementoDelimiter() {
        return JavaElement.JEM_CLASSFILE;
    }

    /**
     * @see IClassFile
     */
    @Override
    public IType getType() {
        if (this.binaryType == null) {
            this.binaryType = new BinaryType(this, getTypeName());
        }
        return this.binaryType;
    }

    public String getTypeName() {
        return this.typeName;
    }

    /**
     * @see IClassFile
     */
    @Override
    public boolean isClass() throws JavaModelException {
        return getType().isClass();
    }

    /**
     * @see IClassFile
     */
    @Override
    public boolean isInterface() throws JavaModelException {
        return getType().isInterface();
    }

    /**
     * Opens and returns buffer on the source code associated with this class file.
     * Maps the source code to the children elements of this class file.
     * If no source code is associated with this class file,
     * <code>null</code> is returned.
     *
     * @see Openable
     */
    @Override
    protected IBuffer openBuffer(IProgressMonitor pm, IElementInfo info) throws JavaModelException {
        // Check the cache for the top-level type first
        IType outerMostEnclosingType = getOuterMostEnclosingType();
        IBuffer buffer = getBufferManager().getBuffer(outerMostEnclosingType.getClassFile());
        if (buffer == null) {
            SourceMapper mapper = getSourceMapper();
            IBinaryType typeInfo = info instanceof IBinaryType ? (IBinaryType) info : null;
            if (mapper != null) {
                buffer = mapSource(mapper, typeInfo, outerMostEnclosingType.getClassFile());
            }
        }
        return buffer;
    }

    /** Loads the buffer via SourceMapper, and maps it in SourceMapper */
    private IBuffer mapSource(SourceMapper mapper, IBinaryType info, IClassFile bufferOwner) {
        char[] contents = mapper.findSource(getType(), info);
        if (contents != null) {
            // create buffer
            IBuffer buffer = BufferManager.createBuffer(bufferOwner);
            if (buffer == null)
                return null;
            BufferManager bufManager = getBufferManager();
            bufManager.addBuffer(buffer);

            // set the buffer source
            if (buffer.getCharacters() == null) {
                buffer.setContents(contents);
            }

            // listen to buffer changes
            buffer.addBufferChangedListener(this);

            // do the source mapping
            mapper.mapSource((NamedMember) getOuterMostEnclosingType(), contents, info);

            return buffer;
        } else {
            // create buffer
            IBuffer buffer = BufferManager.createNullBuffer(bufferOwner);
            if (buffer == null)
                return null;
            BufferManager bufManager = getBufferManager();
            bufManager.addBuffer(buffer);

            // listen to buffer changes
            buffer.addBufferChangedListener(this);
            return buffer;
        }
    }

    /* package */ static String simpleName(char[] className) {
        if (className == null)
            return null;
        String simpleName = new String(unqualifiedName(className));
        int lastDollar = simpleName.lastIndexOf('$');
        if (lastDollar != -1)
            return Util.localTypeName(simpleName, lastDollar, simpleName.length());
        else
            return simpleName;
    }

    /** Returns the type of the top-level declaring class used to find the source code */
    private IType getOuterMostEnclosingType() {
        IType type = getType();
        IType enclosingType = type.getDeclaringType();
        while (enclosingType != null) {
            type = enclosingType;
            enclosingType = type.getDeclaringType();
        }
        return type;
    }

    /**
     * Returns the Java Model representation of the given name
     * which is provided in diet class file format, or <code>null</code>
     * if the given name is <code>null</code>.
     *
     * <p><code>ClassFileReader</code> format is similar to "java/lang/Object",
     * and corresponding Java Model format is "java.lang.Object".
     */

    public static char[] translatedName(char[] name) {
        if (name == null)
            return null;
        int nameLength = name.length;
        char[] newName = new char[nameLength];
        for (int i = 0; i < nameLength; i++) {
            if (name[i] == '/') {
                newName[i] = '.';
            } else {
                newName[i] = name[i];
            }
        }
        return newName;
    }

    /**
     * Returns the Java Model representation of the given names
     * which are provided in diet class file format, or <code>null</code>
     * if the given names are <code>null</code>.
     *
     * <p><code>ClassFileReader</code> format is similar to "java/lang/Object",
     * and corresponding Java Model format is "java.lang.Object".
     */

    /* package */ static char[][] translatedNames(char[][] names) {
        if (names == null)
            return null;
        int length = names.length;
        char[][] newNames = new char[length][];
        for (int i = 0; i < length; i++) {
            newNames[i] = translatedName(names[i]);
        }
        return newNames;
    }

    /**
     * Returns the Java Model format of the unqualified class name for the
     * given className which is provided in diet class file format,
     * or <code>null</code> if the given className is <code>null</code>.
     * (This removes the package name, but not enclosing type names).
     *
     * <p><code>ClassFileReader</code> format is similar to "java/lang/Object",
     * and corresponding Java Model simple name format is "Object".
     */

    /* package */ static char[] unqualifiedName(char[] className) {
        if (className == null)
            return null;
        int count = 0;
        for (int i = className.length - 1; i > -1; i--) {
            if (className[i] == '/') {
                char[] name = new char[count];
                System.arraycopy(className, i + 1, name, 0, count);
                return name;
            }
            count++;
        }
        return className;
    }
}
