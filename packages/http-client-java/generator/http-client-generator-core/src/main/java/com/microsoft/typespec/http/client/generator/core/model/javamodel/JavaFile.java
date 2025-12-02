// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.typespec.http.client.generator.core.model.javamodel;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Simple representation of a .java source file.
 * <p>
 * This type just holds the file path and the contents of the file.
 */
public final class JavaFile implements JavaContext {
    private final String filePath;
    private final JavaFileContents contents;

    public JavaFile(String filePath) {
        this.filePath = filePath;
        this.contents = new JavaFileContents();
    }

    public String getFilePath() {
        return filePath;
    }

    public JavaFileContents getContents() {
        return contents;
    }

    public void line(String text) {
        contents.line(text);
    }

    public void line() {
        contents.line();
    }

    public void indent(Runnable indentAction) {
        contents.indent(indentAction);
    }

    public void publicFinalClass(String classDeclaration, Consumer<JavaClass> classAction) {
        publicClass(Collections.singletonList(JavaModifier.Final), classDeclaration, classAction);
    }

    public void publicClass(List<JavaModifier> modifiers, String classDeclaration, Consumer<JavaClass> classAction) {
        classBlock(JavaVisibility.Public, modifiers, classDeclaration, classAction);
    }

    public void classBlock(JavaVisibility visibility, List<JavaModifier> modifiers, String classDeclaration,
        Consumer<JavaClass> classAction) {
        getContents().classBlock(visibility, modifiers, classDeclaration, classAction);
    }

    public void setFileHeader(String fileHeader) {
        contents.setFileHeader(fileHeader);
    }

    public void declarePackage(String packageName) {
        contents.declarePackage(packageName);
    }

    public void declareImport(String... imports) {
        contents.declareImport(imports);
    }

    public void declareImport(Collection<String> imports) {
        contents.declareImport(imports);
    }

    public void javadocComment(Consumer<JavaJavadocComment> commentAction) {
        contents.javadocComment(commentAction);
    }

    public void lineComment(Consumer<JavaLineComment> commentAction) {
        contents.lineComment(commentAction);
    }

    public void annotation(String... annotations) {
        contents.annotation(annotations);
    }

    public void publicEnum(String enumName, Consumer<JavaEnum> enumAction) {
        enumBlock(JavaVisibility.Public, enumName, enumAction);
    }

    public void enumBlock(JavaVisibility visibility, String enumName, Consumer<JavaEnum> enumAction) {
        contents.enumBlock(visibility, enumName, enumAction);
    }

    public void publicInterface(String interfaceName, Consumer<JavaInterface> interfaceAction) {
        interfaceBlock(JavaVisibility.Public, interfaceName, interfaceAction);
    }

    public void interfaceBlock(JavaVisibility visibility, String interfaceName,
        Consumer<JavaInterface> interfaceAction) {
        contents.interfaceBlock(visibility, interfaceName, interfaceAction);
    }
}
