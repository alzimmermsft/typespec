/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.dom;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ASTVisitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.Initializer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.ClassScope;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.MethodScope;

class NodeSearcher extends ASTVisitor {
    public com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.ASTNode found;
    public TypeDeclaration enclosingType;
    public int position;

    NodeSearcher(int position) {
        this.position = position;
    }

    @Override
    public boolean visit(ConstructorDeclaration constructorDeclaration, ClassScope scope) {

        if (constructorDeclaration.declarationSourceStart <= this.position
            && this.position <= constructorDeclaration.declarationSourceEnd) {
            this.found = constructorDeclaration;
            return false;
        }
        return true;
    }

    @Override
    public boolean visit(FieldDeclaration fieldDeclaration, MethodScope scope) {
        if (fieldDeclaration.declarationSourceStart <= this.position
            && this.position <= fieldDeclaration.declarationSourceEnd) {
            this.found = fieldDeclaration;
            return false;
        }
        return true;
    }

    @Override
    public boolean visit(Initializer initializer, MethodScope scope) {
        if (initializer.declarationSourceStart <= this.position && this.position <= initializer.declarationSourceEnd) {
            this.found = initializer;
            return false;
        }
        return true;
    }

    @Override
    public boolean visit(TypeDeclaration memberTypeDeclaration, ClassScope scope) {
        if (memberTypeDeclaration.declarationSourceStart <= this.position
            && this.position <= memberTypeDeclaration.declarationSourceEnd) {
            this.enclosingType = memberTypeDeclaration;
            return true;

        }
        return false;
    }

    @Override
    public boolean visit(MethodDeclaration methodDeclaration, ClassScope scope) {

        if (methodDeclaration.declarationSourceStart <= this.position
            && this.position <= methodDeclaration.declarationSourceEnd) {
            this.found = methodDeclaration;
            return false;
        }
        return true;
    }

    @Override
    public boolean visit(TypeDeclaration typeDeclaration, CompilationUnitScope scope) {
        if (typeDeclaration.declarationSourceStart <= this.position
            && this.position <= typeDeclaration.declarationSourceEnd) {
            this.enclosingType = typeDeclaration;
            return true;
        }
        return false;
    }

}
