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
 *     Stephan Herrmann - Contribution for
 *								Bug 438458 - [1.8][null] clean up handling of null type annotations wrt type variables
 *								Bug 429813 - [1.8][dom ast] IMethodBinding#getJavaElement() should return IMethod for lambda
 *******************************************************************************/

package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.dom;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.OperationCanceledException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaCore;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.CharOperation;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.Wildcard;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.Binding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.CaptureBinding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.CaptureBinding18;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.IntersectionTypeBinding18;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.TagBits;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.TypeIds;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.WildcardBinding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.util.SuffixConstants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.JavaElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.PackageFragment;

/**
 * Internal implementation of type bindings.
 */
class TypeBinding implements ITypeBinding {

    protected static final IMethodBinding[] NO_METHOD_BINDINGS = new IMethodBinding[0];

    private static final String NO_NAME = ""; //$NON-NLS-1$
    protected static final ITypeBinding[] NO_TYPE_BINDINGS = new ITypeBinding[0];
    protected static final IVariableBinding[] NO_VARIABLE_BINDINGS = new IVariableBinding[0];

    private static final int VALID_MODIFIERS
        = Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE | Modifier.ABSTRACT | Modifier.STATIC | Modifier.FINAL
            | Modifier.STRICTFP | Modifier.SEALED | Modifier.NON_SEALED;

    com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.TypeBinding binding;
    private final TypeBinding prototype;
    private String key;
    protected BindingResolver resolver;
    private IVariableBinding[] fields;
    private IMethodBinding[] methods;
    private ITypeBinding[] interfaces;
    private ITypeBinding[] typeArguments;
    private ITypeBinding[] bounds;
    private ITypeBinding[] typeParameters;

    /**
     * Create either a regular TypeBinding or an AnonymousTypeBinding (if declaringMember is given).
     */
    public static TypeBinding createTypeBinding(BindingResolver resolver,
        com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.TypeBinding referenceBinding,
        IBinding declaringMember) {
        return declaringMember != null
            ? new LocalTypeBinding(resolver, referenceBinding, declaringMember)
            : new TypeBinding(resolver, referenceBinding);
    }

    public TypeBinding(BindingResolver resolver,
        com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.TypeBinding binding) {
        this.binding = binding;
        this.resolver = resolver;
        com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.TypeBinding compilerPrototype
            = binding.prototype();
        this.prototype = (TypeBinding) (compilerPrototype == null || compilerPrototype == binding ? null : resolver.getTypeBinding(compilerPrototype)); //$IDENTITY-COMPARISON$
    }

    @Override
    public ITypeBinding createArrayType(int dimension) {
        int realDimensions = dimension;
        realDimensions += getDimensions();
        if (realDimensions < 1 || realDimensions > 255) {
            throw new IllegalArgumentException();
        }
        return this.resolver.resolveArrayType(this, dimension);
    }

    @Override
    public ITypeBinding getBound() {
        switch (this.binding.kind()) {
            case Binding.WILDCARD_TYPE:
            case Binding.INTERSECTION_TYPE:
                WildcardBinding wildcardBinding = (WildcardBinding) this.binding;
                if (wildcardBinding.bound != null) {
                    return this.resolver.getTypeBinding(wildcardBinding.bound);
                }
                break;
        }
        return null;
    }

    @Override
    public int getRank() {
        return switch (this.binding.kind()) {
            case Binding.WILDCARD_TYPE, Binding.INTERSECTION_TYPE -> {
                WildcardBinding wildcardBinding = (WildcardBinding) this.binding;
                yield wildcardBinding.rank;
            }
            default -> -1;
        };
    }

    @Override
    public ITypeBinding getComponentType() {
        if (!isArray()) {
            return null;
        }
        ArrayBinding arrayBinding = (ArrayBinding) this.binding;
        return this.resolver.getTypeBinding(arrayBinding.elementsType());
    }

    @Override
    public synchronized IVariableBinding[] getDeclaredFields() {
        if (this.prototype != null) {
            return this.prototype.getDeclaredFields();
        }
        if (this.fields != null) {
            return this.fields;
        }
        try {
            if (isClass() || isInterface() || isEnum()) {
                ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
                FieldBinding[] fieldBindings = referenceBinding.availableFields(); // resilience
                int length = fieldBindings.length;
                if (length != 0) {
                    int convertedFieldCount = 0;
                    IVariableBinding[] newFields = new IVariableBinding[length];
                    for (FieldBinding fieldBinding : fieldBindings) {
                        IVariableBinding variableBinding = this.resolver.getVariableBinding(fieldBinding);
                        if (variableBinding != null) {
                            newFields[convertedFieldCount++] = variableBinding;
                        }
                    }

                    if (convertedFieldCount != length) {
                        if (convertedFieldCount == 0) {
                            return this.fields = NO_VARIABLE_BINDINGS;
                        }
                        System.arraycopy(newFields, 0, (newFields = new IVariableBinding[convertedFieldCount]), 0,
                            convertedFieldCount);
                    }
                    return this.fields = newFields;
                }
            }
        } catch (RuntimeException e) {
            /*
             * in case a method cannot be resolvable due to missing jars on the classpath
             * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=57871
             * https://bugs.eclipse.org/bugs/show_bug.cgi?id=63550
             * https://bugs.eclipse.org/bugs/show_bug.cgi?id=64299
             */
            com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.Util
                .log(e, "Could not retrieve declared fields"); //$NON-NLS-1$
        }
        return this.fields = NO_VARIABLE_BINDINGS;
    }

    @Override
    public synchronized IMethodBinding[] getDeclaredMethods() {
        if (this.prototype != null) {
            return this.prototype.getDeclaredMethods();
        }
        if (this.methods != null) {
            return this.methods;
        }
        try {
            if (isClass() || isInterface() || isEnum()) {
                ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
                MethodBinding[] internalMethods = referenceBinding.availableMethods(); // be resilient
                int length = internalMethods.length;
                if (length != 0) {
                    int convertedMethodCount = 0;
                    IMethodBinding[] newMethods = new IMethodBinding[length];
                    for (MethodBinding methodBinding : internalMethods) {
                        if (methodBinding.isDefaultAbstract() || methodBinding.isSynthetic() || (
                            methodBinding.isConstructor() && isInterface())) {
                            continue;
                        }
                        IMethodBinding methodBinding2 = this.resolver.getMethodBinding(methodBinding);
                        if (methodBinding2 != null) {
                            newMethods[convertedMethodCount++] = methodBinding2;
                        }
                    }
                    if (convertedMethodCount != length) {
                        if (convertedMethodCount == 0) {
                            return this.methods = NO_METHOD_BINDINGS;
                        }
                        System.arraycopy(newMethods, 0, (newMethods = new IMethodBinding[convertedMethodCount]), 0,
                            convertedMethodCount);
                    }
                    return this.methods = newMethods;
                }
            }
        } catch (RuntimeException e) {
            /*
             * in case a method cannot be resolvable due to missing jars on the classpath
             * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=57871
             * https://bugs.eclipse.org/bugs/show_bug.cgi?id=63550
             * https://bugs.eclipse.org/bugs/show_bug.cgi?id=64299
             */
            com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.Util
                .log(e, "Could not retrieve declared methods"); //$NON-NLS-1$
        }
        return this.methods = NO_METHOD_BINDINGS;
    }

    /*
     * @see ITypeBinding#getDeclaredModifiers()
     * 
     * @deprecated Use ITypeBinding#getModifiers() instead
     */
    @Override
    public int getDeclaredModifiers() {
        return getModifiers();
    }

    @Override
    public synchronized IMethodBinding getDeclaringMethod() {
        if (this.binding instanceof com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.LocalTypeBinding localTypeBinding) {
            MethodBinding methodBinding = localTypeBinding.enclosingMethod;
            if (methodBinding != null) {
                try {
                    return this.resolver.getMethodBinding(localTypeBinding.enclosingMethod);
                } catch (RuntimeException e) {
                    /*
                     * in case a method cannot be resolvable due to missing jars on the classpath
                     * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=57871
                     * https://bugs.eclipse.org/bugs/show_bug.cgi?id=63550
                     * https://bugs.eclipse.org/bugs/show_bug.cgi?id=64299
                     */
                    com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.Util
                        .log(e, "Could not retrieve declaring method"); //$NON-NLS-1$
                }
            }
        } else if (this.binding.isTypeVariable()) {
            TypeVariableBinding typeVariableBinding = (TypeVariableBinding) this.binding;
            Binding declaringElement = typeVariableBinding.declaringElement;
            if (declaringElement instanceof MethodBinding) {
                try {
                    return this.resolver.getMethodBinding((MethodBinding) declaringElement);
                } catch (RuntimeException e) {
                    /*
                     * in case a method cannot be resolvable due to missing jars on the classpath
                     * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=57871
                     * https://bugs.eclipse.org/bugs/show_bug.cgi?id=63550
                     * https://bugs.eclipse.org/bugs/show_bug.cgi?id=64299
                     */
                    com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.Util
                        .log(e, "Could not retrieve declaring method"); //$NON-NLS-1$
                }
            }
        }
        return null;
    }

    @Override
    public synchronized ITypeBinding getDeclaringClass() {
        if (isClass() || isInterface() || isEnum()) {
            ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
            if (referenceBinding.isNestedType()) {
                try {
                    return this.resolver.getTypeBinding(referenceBinding.enclosingType());
                } catch (RuntimeException e) {
                    /*
                     * in case a method cannot be resolvable due to missing jars on the classpath
                     * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=57871
                     * https://bugs.eclipse.org/bugs/show_bug.cgi?id=63550
                     * https://bugs.eclipse.org/bugs/show_bug.cgi?id=64299
                     */
                    com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.Util
                        .log(e, "Could not retrieve declaring class"); //$NON-NLS-1$
                }
            }
        } else if (this.binding.isTypeVariable()) {
            TypeVariableBinding typeVariableBinding = (TypeVariableBinding) this.binding;
            Binding declaringElement = typeVariableBinding.isCapture()
                ? ((CaptureBinding) typeVariableBinding).sourceType
                : typeVariableBinding.declaringElement;
            if (declaringElement instanceof ReferenceBinding) {
                try {
                    return this.resolver.getTypeBinding((ReferenceBinding) declaringElement);
                } catch (RuntimeException e) {
                    /*
                     * in case a method cannot be resolvable due to missing jars on the classpath
                     * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=57871
                     * https://bugs.eclipse.org/bugs/show_bug.cgi?id=63550
                     * https://bugs.eclipse.org/bugs/show_bug.cgi?id=64299
                     */
                    com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.Util
                        .log(e, "Could not retrieve declaring class"); //$NON-NLS-1$
                }
            }
        }
        return null;
    }

    @Override
    public IModuleBinding getModule() {
        if (this.binding instanceof ReferenceBinding && !this.binding.isTypeVariable()) {
            IPackageBinding packageBinding
                = this.resolver.getPackageBinding(this.binding.getPackage());
            return packageBinding != null ? packageBinding.getModule() : null;
        }
        return null;
    }

    @Override
    public IBinding getDeclaringMember() {
        return null;
    }

    @Override
    public int getDimensions() {
        if (!isArray()) {
            return 0;
        }
        ArrayBinding arrayBinding = (ArrayBinding) this.binding;
        return arrayBinding.dimensions;
    }

    @Override
    public ITypeBinding getElementType() {
        if (!isArray()) {
            return null;
        }
        ArrayBinding arrayBinding = (ArrayBinding) this.binding;
        return this.resolver.getTypeBinding(arrayBinding.leafComponentType);
    }

    @Override
    public ITypeBinding getTypeDeclaration() {
        if (this.binding instanceof ParameterizedTypeBinding)
            return this.resolver.getTypeBinding(((ParameterizedTypeBinding) this.binding).genericType());
        return this.resolver.getTypeBinding(this.binding.unannotated());
    }

    @Override
    public ITypeBinding getErasure() {
        return this.resolver.getTypeBinding(this.binding.erasure());
    }

    @Override
    public synchronized ITypeBinding[] getInterfaces() {
        if (this.prototype != null) {
            return this.prototype.getInterfaces();
        }
        if (this.interfaces != null) {
            return this.interfaces;
        }
        if (this.binding == null)
            return this.interfaces = NO_TYPE_BINDINGS;
        switch (this.binding.kind()) {
            case Binding.ARRAY_TYPE:
            case Binding.BASE_TYPE:
                return this.interfaces = NO_TYPE_BINDINGS;
        }
        ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
        ReferenceBinding[] internalInterfaces = null;
        try {
            internalInterfaces = referenceBinding.superInterfaces();
        } catch (RuntimeException e) {
            /*
             * in case a method cannot be resolvable due to missing jars on the classpath
             * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=57871
             * https://bugs.eclipse.org/bugs/show_bug.cgi?id=63550
             * https://bugs.eclipse.org/bugs/show_bug.cgi?id=64299
             */
            com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.Util
                .log(e, "Could not retrieve interfaces"); //$NON-NLS-1$
        }
        int length = internalInterfaces == null ? 0 : internalInterfaces.length;
        if (length != 0) {
            ITypeBinding[] newInterfaces = new ITypeBinding[length];
            int interfacesCounter = 0;
            for (int i = 0; i < length; i++) {
                ITypeBinding typeBinding = this.resolver.getTypeBinding(internalInterfaces[i]);
                if (typeBinding == null) {
                    continue;
                }
                newInterfaces[interfacesCounter++] = typeBinding;
            }
            if (length != interfacesCounter) {
                System.arraycopy(newInterfaces, 0, (newInterfaces = new ITypeBinding[interfacesCounter]), 0,
                    interfacesCounter);
            }
            return this.interfaces = newInterfaces;
        }
        return this.interfaces = NO_TYPE_BINDINGS;
    }

    private ITypeBinding[] getIntersectingTypes() {
        ITypeBinding[] intersectionBindings;
        ReferenceBinding[] intersectingTypes = this.binding.getIntersectingTypes();
        int l = intersectingTypes.length;
        intersectionBindings = new ITypeBinding[l];
        for (int i = 0; i < l; ++i) {
            intersectionBindings[i] = this.resolver.getTypeBinding(intersectingTypes[i]);
        }
        return intersectionBindings;
    }

    @Override
    public IJavaElement getJavaElement() {
        JavaElement element = getUnresolvedJavaElement();
        if (element != null)
            return element.resolved(this.binding);
        if (isRecovered()) {
            IPackageBinding packageBinding = getPackage();
            if (packageBinding != null) {
                final IJavaElement javaElement = packageBinding.getJavaElement();
                if (javaElement != null && javaElement.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
                    // best effort: we don't know if the recovered binding is a binary or source binding, so go with a
                    // simple source type
                    return ((PackageFragment) javaElement)
                        .getCompilationUnit(new String(this.binding.sourceName()) + SuffixConstants.SUFFIX_STRING_java)
                        .getType(this.getName());
                }
            }
            return null;
        }
        return null;
    }

    private JavaElement getUnresolvedJavaElement() {
        return getUnresolvedJavaElement(this.binding);
    }

    private JavaElement getUnresolvedJavaElement(
        com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.TypeBinding typeBinding) {
        if (JavaCore.getPlugin() == null) {
            return null;
        }
        if (this.resolver instanceof DefaultBindingResolver defaultBindingResolver) {
            if (!defaultBindingResolver.fromJavaProject)
                return null;
            return com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.Util.getUnresolvedJavaElement(typeBinding,
                defaultBindingResolver.workingCopyOwner, defaultBindingResolver.getBindingsToNodesMap());
        }
        return null;
    }

    @Override
    public String getKey() {
        if (this.key == null) {
            this.key = new String(this.binding.computeUniqueKey());
        }
        return this.key;
    }

    @Override
    public int getKind() {
        return IBinding.TYPE;
    }

    @Override
    public int getModifiers() {
        if (isClass()) {
            ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
            final int accessFlags = referenceBinding.getAccessFlags() & VALID_MODIFIERS;

            if (referenceBinding.isSealed()) {
                return accessFlags | Modifier.SEALED;
            }
            if (referenceBinding.isNonSealed()) {
                return accessFlags | Modifier.NON_SEALED;
            }
            if (referenceBinding.isAnonymousType()) {
                return accessFlags & ~Modifier.FINAL;
            }
            return accessFlags;
        } else if (isAnnotation()) {
            ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
            final int accessFlags = referenceBinding.getAccessFlags() & VALID_MODIFIERS;
            // clear the AccAbstract, AccAnnotation and the AccInterface bits
            return accessFlags & ~(ClassFileConstants.AccAbstract | ClassFileConstants.AccInterface
                | ClassFileConstants.AccAnnotation);
        } else if (isInterface()) {
            ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
            int accessFlags = referenceBinding.getAccessFlags() & VALID_MODIFIERS;
            // clear the AccAbstract and the AccInterface bits
            accessFlags = accessFlags & ~(ClassFileConstants.AccAbstract | ClassFileConstants.AccInterface);

            if (referenceBinding.isSealed()) {
                return accessFlags | Modifier.SEALED;
            }
            if (referenceBinding.isNonSealed()) {
                return accessFlags | Modifier.NON_SEALED;
            }
            return accessFlags;
        } else if (isEnum()) {
            ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
            final int accessFlags = referenceBinding.getAccessFlags() & VALID_MODIFIERS;
            // clear the AccEnum bits
            return accessFlags & ~ClassFileConstants.AccEnum;
        } else {
            return Modifier.NONE;
        }
    }

    @Override
    public String getName() {
        StringBuilder buffer;
        switch (this.binding.kind()) {

            case Binding.WILDCARD_TYPE:
            case Binding.INTERSECTION_TYPE:
                WildcardBinding wildcardBinding = (WildcardBinding) this.binding;
                buffer = new StringBuilder();
                buffer.append(TypeConstants.WILDCARD_NAME);
                if (wildcardBinding.bound != null) {
                    switch (wildcardBinding.boundKind) {
                        case Wildcard.SUPER:
                            buffer.append(TypeConstants.WILDCARD_SUPER);
                            break;

                        case Wildcard.EXTENDS:
                            buffer.append(TypeConstants.WILDCARD_EXTENDS);
                    }
                    buffer.append(getBound().getName());
                }
                return String.valueOf(buffer);

            case Binding.TYPE_PARAMETER:
                if (isCapture()) {
                    return NO_NAME;
                }
                TypeVariableBinding typeVariableBinding = (TypeVariableBinding) this.binding;
                return new String(typeVariableBinding.sourceName);

            case Binding.PARAMETERIZED_TYPE:
                ParameterizedTypeBinding parameterizedTypeBinding = (ParameterizedTypeBinding) this.binding;
                buffer = new StringBuilder();
                buffer.append(parameterizedTypeBinding.sourceName());
                ITypeBinding[] tArguments = getTypeArguments();
                final int typeArgumentsLength = tArguments.length;
                if (typeArgumentsLength != 0) {
                    buffer.append('<');
                    for (int i = 0; i < typeArgumentsLength; i++) {
                        if (i > 0) {
                            buffer.append(',');
                        }
                        buffer.append(tArguments[i].getName());
                    }
                    buffer.append('>');
                }
                return String.valueOf(buffer);

            case Binding.RAW_TYPE:
                return getTypeDeclaration().getName();

            case Binding.ARRAY_TYPE:
                ITypeBinding elementType = getElementType();
                if (elementType.isLocal() || elementType.isAnonymous() || elementType.isCapture()) {
                    return NO_NAME;
                }
                int dimensions = getDimensions();
                char[] brackets = new char[dimensions * 2];
                for (int i = dimensions * 2 - 1; i >= 0; i -= 2) {
                    brackets[i] = ']';
                    brackets[i - 1] = '[';
                }
                buffer = new StringBuilder(elementType.getName());
                buffer.append(brackets);
                return String.valueOf(buffer);

            case Binding.INTERSECTION_TYPE18:
                // just use the first bound for now (same kludge as in IntersectionTypeBinding18#constantPoolName())
                return new String(this.binding.getIntersectingTypes()[0].sourceName());

            default:
                if (isPrimitive() || isNullType()) {
                    BaseTypeBinding baseTypeBinding = (BaseTypeBinding) this.binding;
                    return new String(baseTypeBinding.simpleName);
                }
                if (isAnonymous()) {
                    return NO_NAME;
                }
                return new String(this.binding.sourceName());
        }
    }

    @Override
    public IPackageBinding getPackage() {
        switch (this.binding.kind()) {
            case Binding.BASE_TYPE:
            case Binding.ARRAY_TYPE:
            case Binding.TYPE_PARAMETER: // includes capture scenario
            case Binding.WILDCARD_TYPE:
            case Binding.INTERSECTION_TYPE:
            case Binding.INTERSECTION_TYPE18:
                return null;
        }
        ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
        return this.resolver.getPackageBinding(referenceBinding.getPackage());
    }

    /**
     * @see org.eclipse.jdt.core.dom.ITypeBinding#getQualifiedName()
     */
    @Override
    public String getQualifiedName() {
        StringBuilder buffer;
        switch (this.binding.kind()) {

            case Binding.WILDCARD_TYPE:
            case Binding.INTERSECTION_TYPE:
                WildcardBinding wildcardBinding = (WildcardBinding) this.binding;
                buffer = new StringBuilder();
                buffer.append(TypeConstants.WILDCARD_NAME);
                final ITypeBinding bound = getBound();
                if (bound != null) {
                    switch (wildcardBinding.boundKind) {
                        case Wildcard.SUPER:
                            buffer.append(TypeConstants.WILDCARD_SUPER);
                            break;

                        case Wildcard.EXTENDS:
                            buffer.append(TypeConstants.WILDCARD_EXTENDS);
                    }
                    buffer.append(bound.getQualifiedName());
                }
                return String.valueOf(buffer);

            case Binding.RAW_TYPE:
                return getTypeDeclaration().getQualifiedName();

            case Binding.ARRAY_TYPE:
                ITypeBinding elementType = getElementType();
                if (elementType.isLocal() || elementType.isAnonymous() || elementType.isCapture()) {
                    return elementType.getQualifiedName();
                }
                final int dimensions = getDimensions();
                char[] brackets = new char[dimensions * 2];
                for (int i = dimensions * 2 - 1; i >= 0; i -= 2) {
                    brackets[i] = ']';
                    brackets[i - 1] = '[';
                }
                buffer = new StringBuilder(elementType.getQualifiedName());
                buffer.append(brackets);
                return String.valueOf(buffer);

            case Binding.TYPE_PARAMETER:
                if (isCapture()) {
                    return NO_NAME;
                }
                TypeVariableBinding typeVariableBinding = (TypeVariableBinding) this.binding;
                return new String(typeVariableBinding.sourceName);

            case Binding.PARAMETERIZED_TYPE:
                if (this.binding.isLocalType()) {
                    return NO_NAME;
                }
                buffer = new StringBuilder();
                if (isMember()) {
                    buffer.append(getDeclaringClass().getQualifiedName()).append('.');
                    ParameterizedTypeBinding parameterizedTypeBinding = (ParameterizedTypeBinding) this.binding;
                    buffer.append(parameterizedTypeBinding.sourceName());
                    ITypeBinding[] tArguments = getTypeArguments();
                    final int typeArgumentsLength = tArguments.length;
                    if (typeArgumentsLength != 0) {
                        buffer.append('<');
                        for (int i = 0; i < typeArgumentsLength; i++) {
                            if (i > 0) {
                                buffer.append(',');
                            }
                            buffer.append(tArguments[i].getQualifiedName());
                        }
                        buffer.append('>');
                    }
                    return String.valueOf(buffer);
                }
                buffer.append(getTypeDeclaration().getQualifiedName());
                ITypeBinding[] tArguments = getTypeArguments();
                final int typeArgumentsLength = tArguments.length;
                if (typeArgumentsLength != 0) {
                    buffer.append('<');
                    for (int i = 0; i < typeArgumentsLength; i++) {
                        if (i > 0) {
                            buffer.append(',');
                        }
                        buffer.append(tArguments[i].getQualifiedName());
                    }
                    buffer.append('>');
                }
                return String.valueOf(buffer);

            default:
                if (isAnonymous() || this.binding.isLocalType() || this.binding.isIntersectionType18()) {
                    return NO_NAME;
                }
                if (isPrimitive() || isNullType()) {
                    BaseTypeBinding baseTypeBinding = (BaseTypeBinding) this.binding;
                    return new String(baseTypeBinding.simpleName);
                }
                if (isMember()) {
                    buffer = new StringBuilder();
                    buffer.append(getDeclaringClass().getQualifiedName()).append('.');
                    buffer.append(getName());
                    return String.valueOf(buffer);
                }
                PackageBinding packageBinding = this.binding.getPackage();
                buffer = new StringBuilder();
                if (packageBinding != null && packageBinding.compoundName != CharOperation.NO_CHAR_CHAR) {
                    buffer.append(CharOperation.concatWith(packageBinding.compoundName, '.')).append('.');
                }
                buffer.append(getName());
                return String.valueOf(buffer);
        }
    }

    @Override
    public synchronized ITypeBinding getSuperclass() {
        if (this.binding == null)
            return null;
        switch (this.binding.kind()) {
            case Binding.ARRAY_TYPE:
            case Binding.BASE_TYPE:
                return null;

            default:
                // no superclass for interface types (interface | annotation type)
                if (this.binding.isInterface())
                    return null;
        }
        ReferenceBinding superclass;
        try {
            superclass = this.binding.superclass();
        } catch (OperationCanceledException e) {
            throw e; // https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1925
        } catch (RuntimeException e) {
            /*
             * in case a method cannot be resolvable due to missing jars on the classpath
             * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=57871
             * https://bugs.eclipse.org/bugs/show_bug.cgi?id=63550
             * https://bugs.eclipse.org/bugs/show_bug.cgi?id=64299
             */
            com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.Util
                .log(e, "Could not retrieve superclass"); //$NON-NLS-1$
            return this.resolver.resolveWellKnownType("java.lang.Object"); //$NON-NLS-1$
        }
        if (superclass == null) {
            return null;
        }
        return this.resolver.getTypeBinding(superclass);
    }

    @Override
    public ITypeBinding[] getTypeArguments() {
        if (this.prototype != null) {
            return this.prototype.getTypeArguments();
        }
        if (this.typeArguments != null) {
            return this.typeArguments;
        }
        if (this.binding.isParameterizedTypeWithActualArguments()) {
            ParameterizedTypeBinding parameterizedTypeBinding = (ParameterizedTypeBinding) this.binding;
            final com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.TypeBinding[] arguments
                = parameterizedTypeBinding.arguments;
            int argumentsLength = arguments.length;
            ITypeBinding[] newTypeArguments = new ITypeBinding[argumentsLength];
            for (int i = 0; i < argumentsLength; i++) {
                ITypeBinding typeBinding = this.resolver.getTypeBinding(arguments[i]);
                if (typeBinding == null) {
                    return this.typeArguments = NO_TYPE_BINDINGS;
                }
                newTypeArguments[i] = typeBinding;
            }
            return this.typeArguments = newTypeArguments;
        }
        return this.typeArguments = NO_TYPE_BINDINGS;
    }

    @Override
    public ITypeBinding[] getTypeBounds() {
        if (this.prototype != null) {
            return this.prototype.getTypeBounds();
        }
        if (this.bounds != null) {
            return this.bounds;
        }
        TypeVariableBinding typeVariableBinding = null;
        if (this.binding instanceof TypeVariableBinding) {
            typeVariableBinding = (TypeVariableBinding) this.binding;
        } else if (this.binding instanceof WildcardBinding wildcardBinding) {
            typeVariableBinding = wildcardBinding.typeVariable();
            if (typeVariableBinding == null) {
                com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.TypeBinding allBounds
                    = wildcardBinding.allBounds();
                if (allBounds instanceof IntersectionTypeBinding18) { // doubles up as null check
                    ITypeBinding typeBinding = this.resolver.getTypeBinding(allBounds);
                    if (typeBinding instanceof TypeBinding) { // doubles up as null check
                        return ((TypeBinding) typeBinding).getIntersectingTypes();
                    }
                }
            }
        } else if (this.binding instanceof IntersectionTypeBinding18) {
            return this.bounds = getIntersectingTypes();
        }
        if (typeVariableBinding != null) {
            ReferenceBinding varSuperclass = typeVariableBinding.superclass();
            com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.TypeBinding firstClassOrArrayBound
                = typeVariableBinding.firstBound;
            int boundsLength = 0;
            if (firstClassOrArrayBound == null) {
                if (varSuperclass != null && varSuperclass.id != TypeIds.T_JavaLangObject) {
                    firstClassOrArrayBound = varSuperclass;
                    boundsLength++;
                }
            } else {
                if (com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.TypeBinding
                    .equalsEquals(firstClassOrArrayBound, varSuperclass)) {
                    boundsLength++;
                } else if (firstClassOrArrayBound.isArrayType()) { // capture of ? extends/super arrayType
                    boundsLength++;
                } else {
                    firstClassOrArrayBound = null;
                }
            }
            ReferenceBinding[] superinterfaces = typeVariableBinding.superInterfaces();
            int superinterfacesLength = 0;
            if (superinterfaces != null) {
                superinterfacesLength = superinterfaces.length;
                boundsLength += superinterfacesLength;
            }
            if (boundsLength != 0) {
                ITypeBinding[] typeBounds = new ITypeBinding[boundsLength];
                int boundsIndex = 0;
                if (firstClassOrArrayBound != null) {
                    ITypeBinding typeBinding = this.resolver.getTypeBinding(firstClassOrArrayBound);
                    if (typeBinding == null) {
                        return this.bounds = NO_TYPE_BINDINGS;
                    }
                    typeBounds[boundsIndex++] = typeBinding;
                }
                if (superinterfaces != null) {
                    for (int i = 0; i < superinterfacesLength; i++, boundsIndex++) {
                        ITypeBinding typeBinding = this.resolver.getTypeBinding(superinterfaces[i]);
                        if (typeBinding == null) {
                            return this.bounds = NO_TYPE_BINDINGS;
                        }
                        typeBounds[boundsIndex] = typeBinding;
                    }
                }
                return this.bounds = typeBounds;
            }
        }
        return this.bounds = NO_TYPE_BINDINGS;
    }

    @Override
    public ITypeBinding[] getTypeParameters() {
        if (this.prototype != null) {
            return this.prototype.getTypeParameters();
        }
        if (this.typeParameters != null) {
            return this.typeParameters;
        }
        switch (this.binding.kind()) {
            case Binding.RAW_TYPE:
            case Binding.PARAMETERIZED_TYPE:
                return this.typeParameters = NO_TYPE_BINDINGS;
        }
        TypeVariableBinding[] typeVariableBindings = this.binding.typeVariables();
        int typeVariableBindingsLength = typeVariableBindings == null ? 0 : typeVariableBindings.length;
        if (typeVariableBindingsLength != 0) {
            ITypeBinding[] newTypeParameters = new ITypeBinding[typeVariableBindingsLength];
            for (int i = 0; i < typeVariableBindingsLength; i++) {
                ITypeBinding typeBinding = this.resolver.getTypeBinding(typeVariableBindings[i]);
                if (typeBinding == null) {
                    return this.typeParameters = NO_TYPE_BINDINGS;
                }
                newTypeParameters[i] = typeBinding;
            }
            return this.typeParameters = newTypeParameters;
        }
        return this.typeParameters = NO_TYPE_BINDINGS;
    }

    @Override
    public ITypeBinding getWildcard() {
        if (this.binding instanceof CaptureBinding captureBinding) {
            return this.resolver.getTypeBinding(captureBinding.wildcard);
        }
        return null;
    }

    @Override
    public boolean isGenericType() {
        // equivalent to return getTypeParameters().length > 0;
        if (isRawType()) {
            return false;
        }
        TypeVariableBinding[] typeVariableBindings = this.binding.typeVariables();
        return (typeVariableBindings != null && typeVariableBindings.length > 0);
    }

    @Override
    public boolean isAnnotation() {
        return this.binding.isAnnotationType();
    }

    @Override
    public boolean isAnonymous() {
        if (isClass() || isInterface() || isEnum()) {
            ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
            return referenceBinding.isAnonymousType();
        }
        return false;
    }

    @Override
    public boolean isArray() {
        return this.binding.isArrayType();
    }

    @Override
    public boolean isCapture() {
        return this.binding.isCapture() && !(this.binding instanceof CaptureBinding18);
    }

    @Override
    public boolean isClass() {
        return switch (this.binding.kind()) {
            case Binding.TYPE_PARAMETER, Binding.WILDCARD_TYPE, Binding.INTERSECTION_TYPE -> false;
            default -> this.binding.isClass();
        };
    }

    @Override
    public boolean isDeprecated() {
        if (isClass() || isInterface() || isEnum()) {
            ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
            return referenceBinding.isDeprecated();
        }
        return false;
    }

    @Override
    public boolean isEnum() {
        return this.binding.isEnum();
    }

    @Override
    public boolean isRecord() {
        return this.binding.isRecord();
    }

    @Override
    public boolean isEqualTo(IBinding other) {
        if (other == this) {
            // identical binding - equal (key or no key)
            return true;
        }
        if (other == null) {
            // other binding missing
            return false;
        }
        if (!(other instanceof TypeBinding)) {
            return false;
        }
        com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.TypeBinding otherBinding
            = ((TypeBinding) other).binding;
        if (com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.TypeBinding
            .equalsEquals(otherBinding.unannotated(), this.binding.unannotated())) {
            return true;
        }
        // check return type
        return BindingComparator.isEqual(this.binding, otherBinding);
    }

    @Override
    public boolean isInterface() {
        return switch (this.binding.kind()) {
            case Binding.TYPE_PARAMETER, Binding.WILDCARD_TYPE, Binding.INTERSECTION_TYPE -> false;
            default -> this.binding.isInterface();
        };
    }

    @Override
    public boolean isLocal() {
        if (isClass() || isInterface() || isEnum()) {
            ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
            return referenceBinding.isLocalType() && !referenceBinding.isMemberType();
        }
        return false;
    }

    @Override
    public boolean isMember() {
        if (isClass() || isInterface() || isEnum()) {
            ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
            return referenceBinding.isMemberType();
        }
        return false;
    }

    @Override
    public boolean isNested() {
        if (isClass() || isInterface() || isEnum()) {
            ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
            return referenceBinding.isNestedType();
        }
        return false;
    }

    /**
     * @see ITypeBinding#isNullType()
     */
    @Override
    public boolean isNullType() {
        return this.binding
            == com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.TypeBinding.NULL;
    }

    @Override
    public boolean isParameterizedType() {
        return this.binding.isParameterizedTypeWithActualArguments();
    }

    @Override
    public boolean isPrimitive() {
        return !isNullType() && this.binding.isBaseType();
    }

    @Override
    public boolean isRawType() {
        return this.binding.isRawType();
    }

    @Override
    public boolean isRecovered() {
        return (this.binding.tagBits & TagBits.HasMissingType) != 0;
    }

    /**
     * @see IBinding#isSynthetic()
     */
    @Override
    public boolean isSynthetic() {
        return false;
    }

    @Override
    public boolean isTypeVariable() {
        return this.binding.isTypeVariable() && !this.binding.isCapture();
    }

    @Override
    public boolean isUpperbound() {
        return switch (this.binding.kind()) {
            case Binding.WILDCARD_TYPE -> ((WildcardBinding) this.binding).boundKind == Wildcard.EXTENDS;
            case Binding.INTERSECTION_TYPE -> true;
            case Binding.TYPE_PARAMETER -> {
                if (this.binding instanceof CaptureBinding18 captureBinding18) {
                    com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.TypeBinding
                        upperBound = captureBinding18.upperBound();
                    if (upperBound != null && upperBound.id != TypeIds.T_JavaLangObject) {
                        yield true;
                    }
                }
                yield false;
            }
            default -> false;
        };
    }

    @Override
    public boolean isWildcardType() {
        return this.binding.isWildcard() || this.binding instanceof CaptureBinding18;
    }

    /*
     * For debugging purpose only.
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.binding.toString();
    }

    static class LocalTypeBinding extends TypeBinding {

        private final IBinding declaringMember;

        public LocalTypeBinding(BindingResolver resolver,
            com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.TypeBinding binding,
            IBinding declaringMember) {
            super(resolver, binding);
            this.declaringMember = declaringMember;
        }

        @Override
        public IBinding getDeclaringMember() {
            return this.declaringMember;
        }
    }
}
