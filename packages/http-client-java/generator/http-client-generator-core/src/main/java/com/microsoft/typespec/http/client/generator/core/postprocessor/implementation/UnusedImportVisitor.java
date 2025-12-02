// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.typespec.http.client.generator.core.postprocessor.implementation;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocInlineTag;
import java.util.HashSet;
import java.util.Set;

/**
 * Effective copy of how Google Java Format detects unused imports.
 * <p>
 * Google Java Format scans the AST to find all simple names that could be used in imports. Then compares that to the
 * import declarations to find unused ones.
 */
final class UnusedImportVisitor extends VoidVisitorAdapter<Void> {
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
    public void visit(ClassOrInterfaceType n, Void arg) {
        // Add name with scope as this will include full package path if used.
        resolvedTypes.add(n.getNameWithScope());
        super.visit(n, arg);
    }

    @Override
    public void visit(JavadocComment n, Void arg) {
        Javadoc javadoc = n.parse();
        javadoc.getDescription().getElements().forEach(element -> {
            if (element instanceof JavadocInlineTag) {
                JavadocInlineTag inlineTag = (JavadocInlineTag) element;
                if (inlineTag.getType() == JavadocInlineTag.Type.LINK
                    || inlineTag.getType() == JavadocInlineTag.Type.LINKPLAIN) {
                    // Need to handle link and linkplain tags to catch types mentioned in Javadocs.
                    // These can come in various forms, such as:
                    // {@link com.example.MyClass}, {@link MyClass}, {@linkplain #myMethod(ParamType, ParamType2)}, etc.
                    // And also with optional label after the reference:
                    // {@link com.example.MyClass My Class}, {@linkplain #myMethod(ParamType, ParamType2) My Method},
                    // etc.

                    // Get the content and trim it.
                    String content = inlineTag.getContent().trim();

                    int closingParenIndex = content.indexOf(')');
                    if (closingParenIndex == -1) {
                        throw new IllegalStateException("Invalid Javadoc link / linkplain: " + content);
                    }

                    content = content.substring(0, closingParenIndex + 1);

                    int hashIndex = content.indexOf('#');
                    if (hashIndex != -1) {

                    } else {

                    }
                }
            }
        });

        super.visit(n, arg);
    }

    /**
     *
     * @param linkContent
     */
    private void extractTypeFromJavadocLink(String linkContent) {
        // Remove any label after the reference
        String[] parts = linkContent.split("\\s+", 2);
        String reference = parts[0];

        // If it's a method reference, extract the parameter types
        int parenIndex = reference.indexOf('(');
        if (parenIndex != -1) {
            String methodPart = reference.substring(0, parenIndex);
            String paramsPart = reference.substring(parenIndex + 1, reference.length() - 1); // Exclude closing
                                                                                             // parenthesis

            // Add the method name (without parameters) to resolved types
            resolvedTypes.add(methodPart);

            // Split parameters and add their types
            String[] paramTypes = paramsPart.split(",");
            for (String paramType : paramTypes) {
                resolvedTypes.add(paramType.trim());
            }
        } else {
            // It's a class or field reference
            resolvedTypes.add(reference);
        }
    }

    @Override
    public void visit(MarkerAnnotationExpr n, Void arg) {
        resolvedTypes.add(n.getNameAsString());
        super.visit(n, arg);
    }

    @Override
    public void visit(NormalAnnotationExpr n, Void arg) {
        resolvedTypes.add(n.getNameAsString());
        super.visit(n, arg);
    }

    @Override
    public void visit(PackageDeclaration n, Void arg) {
        this.packageDeclaration = n.getNameAsString();
        super.visit(n, arg);
    }

    @Override
    public void visit(SingleMemberAnnotationExpr n, Void arg) {
        resolvedTypes.add(n.getNameAsString());
        super.visit(n, arg);
    }

    @Override
    public void visit(ImportDeclaration n, Void arg) {
        if (n.isAsterisk()) {
            throw new IllegalStateException("Wildcard imports are not supported: " + n);
        }

        importDeclarations.add(n.getNameAsString());
        super.visit(n, arg);
    }
}
