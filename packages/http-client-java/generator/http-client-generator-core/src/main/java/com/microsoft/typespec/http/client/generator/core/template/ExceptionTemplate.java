// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.typespec.http.client.generator.core.template;

import com.microsoft.typespec.http.client.generator.core.model.clientmodel.ClassType;
import com.microsoft.typespec.http.client.generator.core.model.clientmodel.ClientException;
import com.microsoft.typespec.http.client.generator.core.model.javamodel.JavaFile;
import com.microsoft.typespec.http.client.generator.core.model.javamodel.JavaJavadocComment;

/**
 * Writes a ClientException to a JavaFile.
 */
public class ExceptionTemplate implements IJavaTemplate<ClientException, JavaFile> {
    private static final ExceptionTemplate INSTANCE = new ExceptionTemplate();

    protected ExceptionTemplate() {
    }

    public static ExceptionTemplate getInstance() {
        return INSTANCE;
    }

    @Override
    public void write(ClientException exception, JavaFile javaFile) {
        javaFile.declareImport(ClassType.HTTP_RESPONSE.getFullName());
        exception.getParentType().addImportsTo(javaFile::declareImport, false);
        javaFile.javadocComment(comment -> comment.description(
            "Exception thrown for an invalid response with " + exception.getErrorName() + " information."));
        javaFile.publicFinalClass(exception.getName() + " extends " + exception.getParentType(), classBlock -> {
            classBlock.javadocComment(comment -> {
                comment.description("Initializes a new instance of the " + exception.getName() + " class.");
                comment.param("message", "the exception message or the response content if a message is not available");
                comment.param("response", "the HTTP response");
            });
            classBlock.publicConstructor(exception.getName() + "(String message, HttpResponse response)",
                constructorBlock -> constructorBlock.line("super(message, response);"));

            classBlock.javadocComment(comment -> {
                comment.description("Initializes a new instance of the " + exception.getName() + " class.");
                comment.param("message", "the exception message or the response content if a message is not available");
                comment.param("response", "the HTTP response");
                comment.param("value", "the deserialized response value");
            });
            classBlock.publicConstructor(
                exception.getName() + "(String message, HttpResponse response, " + exception.getErrorName() + " value)",
                constructor -> constructor.line("super(message, response, value);"));

            classBlock.javadocComment(JavaJavadocComment::inheritDoc);
            classBlock.annotation("Override");
            classBlock.publicMethod(exception.getErrorName() + " getValue()",
                methodBlock -> methodBlock.methodReturn("(" + exception.getErrorName() + ") super.getValue()"));
        });
    }
}
