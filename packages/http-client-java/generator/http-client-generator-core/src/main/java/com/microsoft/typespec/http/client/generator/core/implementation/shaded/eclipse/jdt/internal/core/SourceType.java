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
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Assert;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.*;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.CharOperation;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.Binding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.DeduplicationUtil;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.MementoTokenizer;

import java.util.ArrayList;

/**
 * Handle for a source type. Info object is a SourceTypeElementInfo.
 *
 * Note: Parent is either an IClassFile, an ICompilationUnit or an IType.
 *
 * @see IType
 */

@SuppressWarnings({ "rawtypes", "unchecked" })
public class SourceType extends NamedMember implements IType {

    /*
     * A count to uniquely identify this element within the context of the enclosing type.
     * Currently this is computed and used only for anonymous types.
     */
    public int localOccurrenceCount = 1;
    private static final IField[] NO_FIELDS = new IField[0];

    protected SourceType(JavaElement parent, String name) {
        super(parent, name);
    }

    protected SourceType(JavaElement parent, String name, int occurrenceCount) {
        super(parent, name, occurrenceCount);
    }

    @Override
    protected void closing(Object info) throws JavaModelException {
        super.closing(info);
        SourceTypeElementInfo elementInfo = (SourceTypeElementInfo) info;
        ITypeParameter[] typeParameters = elementInfo.typeParameters;
        for (ITypeParameter typeParameter : typeParameters) {
            ((TypeParameter) typeParameter).close();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SourceType))
            return false;
        if (((SourceType) o).isLambda())
            return false;
        return super.equals(o);
    }

    /*
     * @see IType
     */
    @Override
    public IMethod[] findMethods(IMethod method) {
        try {
            return findMethods(method, getMethods());
        } catch (JavaModelException e) {
            // if type doesn't exist, no matching method can exist
            return null;
        }
    }

    @Override
    public IAnnotation[] getAnnotations() throws JavaModelException {
        AnnotatableInfo info = (AnnotatableInfo) getElementInfo();
        return info.annotations;
    }

    /**
     * @see IMember
     */
    @Override
    public IType getDeclaringType() {
        IJavaElement parentElement = getParent();
        while (parentElement != null) {
            if (parentElement.getElementType() == IJavaElement.TYPE) {
                return (IType) parentElement;
            } else if (parentElement instanceof IMember) {
                parentElement = parentElement.getParent();
            } else {
                return null;
            }
        }
        return null;
    }

    @Override
    public IOrdinaryClassFile getClassFile() {
        return null;
    }

    /**
     * @see IJavaElement
     */
    @Override
    public int getElementType() {
        return TYPE;
    }

    /**
     * @see IType#getField
     */
    @Override
    public IField getField(String fieldName) {
        return new SourceField(this, fieldName);
    }

    /**
     * @see IType
     */
    @Override
    public IField[] getFields() throws JavaModelException {
        if (!isRecord()) {
            ArrayList list = getChildrenOfType(FIELD);
            if (list.size() == 0) {
                return NO_FIELDS;
            }
            IField[] array = new IField[list.size()];
            list.toArray(array);
            return array;
        }
        return getFieldsOrComponents(false);
    }

    @Override
    public IField[] getRecordComponents() throws JavaModelException {
        if (!isRecord())
            return NO_FIELDS;
        return getFieldsOrComponents(true);
    }

    private IField[] getFieldsOrComponents(boolean component) throws JavaModelException {
        ArrayList list = getChildrenOfType(FIELD);
        if (list.size() == 0) {
            return NO_FIELDS;
        }
        ArrayList<IField> fields = new ArrayList<>();
        for (Object object : list) {
            IField field = (IField) object;
            if (field.isRecordComponent() == component)
                fields.add(field);
        }
        IField[] array = new IField[fields.size()];
        fields.toArray(array);
        return array;
    }

    /**
     * @see IType#getFullyQualifiedName()
     */
    @Override
    public String getFullyQualifiedName() {
        return this.getFullyQualifiedName('$');
    }

    /**
     * @see IType#getFullyQualifiedName(char)
     */
    @Override
    public String getFullyQualifiedName(char enclosingTypeSeparator) {
        try {
            return getFullyQualifiedName(enclosingTypeSeparator, false/* don't show parameters */);
        } catch (JavaModelException e) {
            // exception thrown only when showing parameters
            return null;
        }
    }

    /*
     * For source types, the occurrence count is the one computed in the context of the immediately enclosing type.
     */
    @Override
    protected String getOccurrenceCountSignature() {
        return Integer.toString(this.localOccurrenceCount);
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

            case JEM_FIELD:
                if (!memento.hasMoreTokens())
                    return this;
                String fieldName = memento.nextToken();
                JavaElement field = (JavaElement) getField(fieldName);
                return field.getHandleFromMemento(memento, workingCopyOwner);

            case JEM_INITIALIZER:
                if (!memento.hasMoreTokens())
                    return this;
                String count = memento.nextToken();
                JavaElement initializer = (JavaElement) getInitializer(Integer.parseInt(count));
                return initializer.getHandleFromMemento(memento, workingCopyOwner);

            case JEM_METHOD:
                if (!memento.hasMoreTokens())
                    return this;
                String selector = memento.nextToken();
                ArrayList params = new ArrayList();
                nextParam: while (memento.hasMoreTokens()) {
                    token = memento.nextToken();
                    switch (token.charAt(0)) {
                        case JEM_TYPE:
                        case JEM_TYPE_PARAMETER:
                        case JEM_ANNOTATION:
                            break nextParam;

                        case JEM_METHOD:
                            if (!memento.hasMoreTokens())
                                return this;
                            String param = memento.nextToken();
                            StringBuilder buffer = new StringBuilder();
                            while (param.length() == 1 && Signature.C_ARRAY == param.charAt(0)) { // backward compatible
                                                                                                  // with 3.0 mementos
                                buffer.append(Signature.C_ARRAY);
                                if (!memento.hasMoreTokens())
                                    return this;
                                param = memento.nextToken();
                            }
                            params.add(buffer.toString() + param);
                            break;

                        default:
                            break nextParam;
                    }
                }
                String[] parameters = new String[params.size()];
                params.toArray(parameters);
                JavaElement method = (JavaElement) getMethod(selector, parameters);
                switch (token.charAt(0)) {
                    case JEM_LAMBDA_EXPRESSION:
                    case JEM_TYPE:
                    case JEM_TYPE_PARAMETER:
                    case JEM_LOCALVARIABLE:
                    case JEM_ANNOTATION:
                        return method.getHandleFromMemento(token, memento, workingCopyOwner);

                    default:
                        return method;
                }
            case JEM_TYPE:
                String typeName;
                if (memento.hasMoreTokens()) {
                    typeName = memento.nextToken();
                    char firstChar = typeName.charAt(0);
                    if (firstChar == JEM_FIELD
                        || firstChar == JEM_INITIALIZER
                        || firstChar == JEM_METHOD
                        || firstChar == JEM_TYPE
                        || firstChar == JEM_COUNT) {
                        token = typeName;
                        typeName = ""; //$NON-NLS-1$
                    } else {
                        token = null;
                    }
                } else {
                    typeName = ""; //$NON-NLS-1$
                    token = null;
                }
                JavaElement type = (JavaElement) getType(typeName);
                if (token == null) {
                    return type.getHandleFromMemento(memento, workingCopyOwner);
                } else {
                    return type.getHandleFromMemento(token, memento, workingCopyOwner);
                }
            case JEM_TYPE_PARAMETER:
                if (!memento.hasMoreTokens())
                    return this;
                String typeParameterName = memento.nextToken();
                JavaElement typeParameter = new TypeParameter(this, typeParameterName);
                return typeParameter.getHandleFromMemento(memento, workingCopyOwner);

            case JEM_ANNOTATION:
                if (!memento.hasMoreTokens())
                    return this;
                String annotationName = memento.nextToken();
                JavaElement annotation = new Annotation(this, annotationName);
                return annotation.getHandleFromMemento(memento, workingCopyOwner);
        }
        return null;
    }

    /**
     * @see IType
     */
    @Override
    public IInitializer getInitializer(int count) {
        return new Initializer(this, count);
    }

    /**
     * @see IType
     */
    @Override
    public IInitializer[] getInitializers() throws JavaModelException {
        ArrayList list = getChildrenOfType(INITIALIZER);
        IInitializer[] array = new IInitializer[list.size()];
        list.toArray(array);
        return array;
    }

    @Override
    public String getKey() {
        try {
            return getKey(this /* don't open */);
        } catch (JavaModelException e) {
            // happen only if force open is true
            return null;
        }
    }

    /**
     * @see IType#getMethod
     */
    @Override
    public IMethod getMethod(String selector, String[] parameterTypeSignatures) {
        return new SourceMethod(this, selector, parameterTypeSignatures);
    }

    /**
     * @see IType
     */
    @Override
    public IMethod[] getMethods() throws JavaModelException {
        ArrayList list = getChildrenOfType(METHOD);
        IMethod[] array = new IMethod[list.size()];
        list.toArray(array);
        return array;
    }

    /**
     * @see IType
     */
    @Override
    public IPackageFragment getPackageFragment() {
        IJavaElement parentElement = this.getParent();
        while (parentElement != null) {
            if (parentElement.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
                return (IPackageFragment) parentElement;
            } else {
                parentElement = parentElement.getParent();
            }
        }
        Assert.isTrue(false);  // should not happen
        return null;
    }

    @Override
    public JavaElement getPrimaryElement(boolean checkOwner) {
        if (checkOwner) {
            CompilationUnit cu = (CompilationUnit) getAncestor(COMPILATION_UNIT);
            if (cu.isPrimary())
                return this;
        }
        IJavaElement primaryParent = this.getParent().getPrimaryElement(false);
        switch (primaryParent.getElementType()) {
            case IJavaElement.COMPILATION_UNIT:
                return (JavaElement) ((ICompilationUnit) primaryParent).getType(this.name);

            case IJavaElement.TYPE:
                return (JavaElement) ((IType) primaryParent).getType(this.name);

            case IJavaElement.FIELD:
            case IJavaElement.INITIALIZER:
            case IJavaElement.METHOD:
                return (JavaElement) ((IMember) primaryParent).getType(this.name, this.getOccurrenceCount());
        }
        return this;
    }

    /**
     * @see IType
     */
    @Override
    public String getSuperclassName() throws JavaModelException {
        SourceTypeElementInfo info = (SourceTypeElementInfo) getElementInfo();
        char[] superclassName = info.getSuperclassName();
        if (superclassName == null) {
            return null;
        }
        return new String(superclassName);
    }

    /**
     * @see IType#getSuperclassTypeSignature()
     * @since 3.0
     */
    @Override
    public String getSuperclassTypeSignature() throws JavaModelException {
        SourceTypeElementInfo info = (SourceTypeElementInfo) getElementInfo();
        char[] superclassName = info.getSuperclassName();
        if (superclassName == null) {
            return null;
        }
        return Signature.createTypeSignature(superclassName, false);
    }

    /**
     * @see IType
     */
    @Override
    public String[] getSuperInterfaceNames() throws JavaModelException {
        SourceTypeElementInfo info = (SourceTypeElementInfo) getElementInfo();
        char[][] names = info.getInterfaceNames();
        return CharOperation.toStrings(names);
    }

    /**
     * @see IType#getSuperInterfaceTypeSignatures()
     * @since 3.0
     */
    @Override
    public String[] getSuperInterfaceTypeSignatures() throws JavaModelException {
        SourceTypeElementInfo info = (SourceTypeElementInfo) getElementInfo();
        char[][] names = info.getInterfaceNames();
        if (names == null) {
            return CharOperation.NO_STRINGS;
        }
        String[] strings = new String[names.length];
        for (int i = 0; i < names.length; i++) {
            strings[i] = Signature.createTypeSignature(names[i], false);
        }
        return strings;
    }

    @Override
    public ITypeParameter[] getTypeParameters() throws JavaModelException {
        SourceTypeElementInfo info = (SourceTypeElementInfo) getElementInfo();
        return info.typeParameters;
    }

    /**
     * @see IType#getTypeParameterSignatures()
     * @since 3.0
     */
    @Override
    public String[] getTypeParameterSignatures() throws JavaModelException {
        ITypeParameter[] typeParameters = getTypeParameters();
        int length = typeParameters.length;
        String[] typeParameterSignatures = new String[length];
        for (int i = 0; i < length; i++) {
            TypeParameter typeParameter = (TypeParameter) typeParameters[i];
            TypeParameterElementInfo info = (TypeParameterElementInfo) typeParameter.getElementInfo();
            char[][] bounds = info.bounds;
            if (bounds == null) {
                typeParameterSignatures[i]
                    = Signature.createTypeParameterSignature(typeParameter.getElementName(), CharOperation.NO_STRINGS);
            } else {
                int boundsLength = bounds.length;
                char[][] boundSignatures = new char[boundsLength][];
                for (int j = 0; j < boundsLength; j++) {
                    boundSignatures[j] = Signature.createCharArrayTypeSignature(bounds[j], false);
                }
                typeParameterSignatures[i] = new String(Signature
                    .createTypeParameterSignature(typeParameter.getElementName().toCharArray(), boundSignatures));
            }
        }
        return typeParameterSignatures;
    }

    /**
     * @see IType
     */
    @Override
    public IType getType(String typeName) {
        return new SourceType(this, typeName);
    }

    @Override
    public ITypeParameter getTypeParameter(String typeParameterName) {
        return new TypeParameter(this, typeParameterName);
    }

    /**
     * @see IType#getTypeQualifiedName()
     */
    @Override
    public String getTypeQualifiedName() {
        return this.getTypeQualifiedName('$');
    }

    /**
     * @see IType#getTypeQualifiedName(char)
     */
    @Override
    public String getTypeQualifiedName(char enclosingTypeSeparator) {
        try {
            return getTypeQualifiedName(enclosingTypeSeparator, false/* don't show parameters */);
        } catch (JavaModelException e) {
            // exception thrown only when showing parameters
            return null;
        }
    }

    /**
     * @see IType
     */
    @Override
    public IType[] getTypes() throws JavaModelException {
        ArrayList list = getChildrenOfType(TYPE);
        IType[] array = new IType[list.size()];
        list.toArray(array);
        return array;
    }

    /**
     * @see IType#isAnonymous()
     */
    @Override
    public boolean isAnonymous() {
        return this.name.length() == 0;
    }

    /**
     * @see IType
     */
    @Override
    public boolean isClass() throws JavaModelException {
        SourceTypeElementInfo info = (SourceTypeElementInfo) getElementInfo();
        return TypeDeclaration.kind(info.getModifiers()) == TypeDeclaration.CLASS_DECL;
    }

    /**
     * @see IType#isEnum()
     * @since 3.0
     */
    @Override
    public boolean isEnum() throws JavaModelException {
        SourceTypeElementInfo info = (SourceTypeElementInfo) getElementInfo();
        return TypeDeclaration.kind(info.getModifiers()) == TypeDeclaration.ENUM_DECL;
    }

    /**
     * @see IType#isRecord()
     * @since 3.26
     */
    @Override
    public boolean isRecord() throws JavaModelException {
        SourceTypeElementInfo info = (SourceTypeElementInfo) getElementInfo();
        return TypeDeclaration.kind(info.getModifiers()) == TypeDeclaration.RECORD_DECL;
    }

    /**
     * @see IType#isSealed()
     */
    @Override
    public boolean isSealed() throws JavaModelException {
        SourceTypeElementInfo info = (SourceTypeElementInfo) getElementInfo();
        return Flags.isSealed(info.getModifiers());
    }

    /**
     * @see IType
     */
    @Override
    public boolean isInterface() throws JavaModelException {
        SourceTypeElementInfo info = (SourceTypeElementInfo) getElementInfo();
        switch (TypeDeclaration.kind(info.getModifiers())) {
            case TypeDeclaration.INTERFACE_DECL:
            case TypeDeclaration.ANNOTATION_TYPE_DECL: // annotation is interface too
                return true;
        }
        return false;
    }

    /**
     * @see IType#isAnnotation()
     * @since 3.0
     */
    @Override
    public boolean isAnnotation() throws JavaModelException {
        SourceTypeElementInfo info = (SourceTypeElementInfo) getElementInfo();
        return TypeDeclaration.kind(info.getModifiers()) == TypeDeclaration.ANNOTATION_TYPE_DECL;
    }

    /**
     * @see IType#isLocal()
     */
    @Override
    public boolean isLocal() {
        switch (this.getParent().getElementType()) {
            case IJavaElement.METHOD:
            case IJavaElement.INITIALIZER:
            case IJavaElement.FIELD:
                return true;

            default:
                return false;
        }
    }

    /**
     * @see IType#isMember()
     */
    @Override
    public boolean isMember() {
        return getDeclaringType() != null;
    }

    @Override
    public boolean isResolved() {
        return false;
    }

    @Override
    public JavaElement resolved(Binding binding) {
        ResolvedSourceType resolvedHandle = new ResolvedSourceType(this.getParent(), this.name,
            DeduplicationUtil.toString(binding.computeUniqueKey()), this.getOccurrenceCount());
        resolvedHandle.localOccurrenceCount = this.localOccurrenceCount;
        return resolvedHandle;
    }

    /**
     * for debugging only
     */
    @Override
    protected void toStringInfo(int tab, StringBuilder buffer, Object info, boolean showResolvedInfo) {
        buffer.append(tabString(tab));
        if (info == null) {
            if (isAnonymous()) {
                buffer.append("<anonymous #"); //$NON-NLS-1$
                buffer.append(this.getOccurrenceCount());
                buffer.append(">"); //$NON-NLS-1$
            } else {
                toStringName(buffer);
            }
            buffer.append(" (not open)"); //$NON-NLS-1$
        } else if (info == NO_INFO) {
            if (isAnonymous()) {
                buffer.append("<anonymous #"); //$NON-NLS-1$
                buffer.append(this.getOccurrenceCount());
                buffer.append(">"); //$NON-NLS-1$
            } else {
                toStringName(buffer);
            }
        } else {
            try {
                if (isSealed()) {
                    buffer.append("sealed "); //$NON-NLS-1$
                }
                if (isRecord()) {
                    buffer.append("record "); //$NON-NLS-1$
                } else if (isEnum()) {
                    buffer.append("enum "); //$NON-NLS-1$
                } else if (isAnnotation()) {
                    buffer.append("@interface "); //$NON-NLS-1$
                } else if (isInterface()) {
                    buffer.append("interface "); //$NON-NLS-1$
                } else {
                    buffer.append("class "); //$NON-NLS-1$
                }
                if (isAnonymous()) {
                    buffer.append("<anonymous #"); //$NON-NLS-1$
                    buffer.append(this.getOccurrenceCount());
                    buffer.append(">"); //$NON-NLS-1$
                } else {
                    toStringName(buffer);
                }
            } catch (JavaModelException e) {
                buffer.append("<JavaModelException in toString of " + getElementName()); //$NON-NLS-1$
            }
        }
    }

    @Override
    public boolean isLambda() {
        return false;
    }
}
