// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.typespec.http.client.generator.core.postprocessor.implementation;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.CompactConstructorDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.IntersectionType;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.ast.type.UnknownType;
import com.github.javaparser.ast.type.VarType;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.type.WildcardType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.types.ResolvedType;
import java.util.HashSet;
import java.util.Set;

public final class UnusedImportVisitor extends VoidVisitorAdapter<Void> {
    private String packageDeclaration = null;

    private final Set<String> importDeclarations = new HashSet<>();
    private final Set<String> resolvedTypes = new HashSet<>();

    public Set<String> getUnusedImports() {
        Set<String> unusedImports = new HashSet<>(importDeclarations);
        for (String resolvedType : resolvedTypes) {
            unusedImports.remove(resolvedType);
        }

        // Also remove imports that belong to the same package
        if (packageDeclaration != null) {
            unusedImports.removeIf(importDecl -> {
                int lastDotIndex = importDecl.lastIndexOf('.');
                if (lastDotIndex == -1) {
                    return false;
                }
                return packageDeclaration.regionMatches(0, importDecl, 0, lastDotIndex);
            });
        }

        return unusedImports;
    }

    @Override
    public void visit(AnnotationDeclaration n, Void arg) {
        resolvedTypes.add(n.resolve().getQualifiedName());
        super.visit(n, arg);
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Void arg) {
        resolvedTypes.add(n.resolve().getQualifiedName());
        super.visit(n, arg);
    }

    @Override
    public void visit(ClassOrInterfaceType n, Void arg) {
        handleResolvedType(n.resolve());
        super.visit(n, arg);
    }

    @Override
    public void visit(ConstructorDeclaration n, Void arg) {
        resolvedTypes.add(n.resolve().getQualifiedName());
        super.visit(n, arg);
    }

    @Override
    public void visit(EnumDeclaration n, Void arg) {
        resolvedTypes.add(n.resolve().getQualifiedName());
        super.visit(n, arg);
    }

    @Override
    public void visit(JavadocComment n, Void arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(MarkerAnnotationExpr n, Void arg) {
        resolvedTypes.add(n.resolve().getQualifiedName());
        super.visit(n, arg);
    }

    @Override
    public void visit(MethodCallExpr n, Void arg) {
        resolvedTypes.add(n.resolve().getQualifiedName());
        super.visit(n, arg);
    }

    @Override
    public void visit(MethodDeclaration n, Void arg) {
        resolvedTypes.add(n.resolve().getQualifiedName());
        super.visit(n, arg);
    }

    @Override
    public void visit(PackageDeclaration n, Void arg) {
        if (this.packageDeclaration == null) {
            this.packageDeclaration = n.getNameAsString();
        } else {
            throw new IllegalStateException("Multiple package declarations found.");
        }
        super.visit(n, arg);
    }

    @Override
    public void visit(ArrayType n, Void arg) {
        handleResolvedType(n.resolve());
        super.visit(n, arg);
    }

    @Override
    public void visit(IntersectionType n, Void arg) {
        handleResolvedType(n.resolve());
        super.visit(n, arg);
    }

    @Override
    public void visit(UnionType n, Void arg) {
        handleResolvedType(n.resolve());
        super.visit(n, arg);
    }

    @Override
    public void visit(TypeParameter n, Void arg) {
        handleResolvedType(n.resolve());
        super.visit(n, arg);
    }

    @Override
    public void visit(UnknownType n, Void arg) {
        handleResolvedType(n.resolve());
        super.visit(n, arg);
    }

    @Override
    public void visit(VoidType n, Void arg) {
        handleResolvedType(n.resolve());
        super.visit(n, arg);
    }

    @Override
    public void visit(WildcardType n, Void arg) {
        handleResolvedType(n.resolve());
        super.visit(n, arg);
    }

    @Override
    public void visit(ImportDeclaration n, Void arg) {
        if (n.isAsterisk()) {
            throw new IllegalStateException("Asterisk imports are not supported.");
        }

        importDeclarations.add(n.getNameAsString());
        super.visit(n, arg);
    }

    @Override
    public void visit(VarType n, Void arg) {
        handleResolvedType(n.resolve());
        super.visit(n, arg);
    }

    @Override
    public void visit(RecordDeclaration n, Void arg) {
        resolvedTypes.add(n.resolve().getQualifiedName());
        super.visit(n, arg);
    }

    @Override
    public void visit(CompactConstructorDeclaration n, Void arg) {
        resolvedTypes.add(n.resolve().getQualifiedName());
        super.visit(n, arg);
    }

    private void handleResolvedType(ResolvedType resolved) {
        if (resolved.isReferenceType()) {
            resolvedTypes.add(resolved.asReferenceType().getQualifiedName());
        } else if (resolved.isArray()) {
            // Need to check the component type, ex: SomeType[]
            handleResolvedType(resolved.asArrayType().getComponentType());
        } else if (resolved.isTypeVariable()) {
            resolvedTypes.add(resolved.asTypeVariable().qualifiedName());
        } else if (resolved.isWildcard()) {
            // Need to check the bound type, ex: <? extends SomeType>
            handleResolvedType(resolved.asWildcard().getBoundedType());
        } else if (resolved.isUnionType()) {
            // Need to check each type in the union, ex: <SomeType1 | SomeType2>
            resolved.asUnionType().getElements().forEach(this::handleResolvedType);
        } else if (resolved.isConstraint()) {
            // Need to check the bound type, ex: <? extends SomeType>
            handleResolvedType(resolved.asConstraintType().getBound());
        }
    }
}
