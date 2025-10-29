/*******************************************************************************
 * Copyright (c) 2024 Kamil Krzywanski and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Kamil Krzywanski - initial creation if Interesection type and Implementation
 *******************************************************************************/

package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.apt.model;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.javax.lang.model.type.IntersectionType;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.javax.lang.model.type.TypeKind;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.javax.lang.model.type.TypeMirror;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.javax.lang.model.type.TypeVisitor;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of the WildcardType
 */
public class IntersectionTypeImpl extends TypeMirrorImpl implements IntersectionType {
    private final List<? extends TypeMirror> bounds;

    IntersectionTypeImpl(BaseProcessingEnvImpl env, TypeVariableBinding binding) {
        super(env, binding);
        this.bounds = Arrays.stream(binding.superInterfaces)
            .map(referenceBinding -> this._env.getFactory().newTypeMirror(referenceBinding))
            .toList();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.lang.model.type.TypeMirror#getKind()
     */
    @Override
    public TypeKind getKind() {
        return TypeKind.INTERSECTION;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.lang.model.type.WildcardType#getSuperBound()
     */
    @Override
    public <R, P> R accept(TypeVisitor<R, P> v, P p) {
        return v.visitIntersection(this, p);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.lang.model.type.IntersectionType#getBounds()
     */
    @Override
    public List<? extends TypeMirror> getBounds() {
        return this.bounds;
    }
}
