// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.typespec.http.client.generator.core.model.clientmodel;

import com.microsoft.typespec.http.client.generator.core.extension.plugin.JavaSettings;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The details of an array type that is used by a client.
 */
public final class ArrayType implements IType {
    /**
     * The {@code byte[]} type.
     */
    public static final ArrayType BYTE_ARRAY = new ArrayType(PrimitiveType.BYTE, defaultValueExpression -> {
        if (defaultValueExpression != null) {
            return "\"" + defaultValueExpression + "\".getBytes()";
        } else {
            return JavaSettings.getInstance().isNullByteArrayMapsToEmptyArray() ? "EMPTY_BYTE_ARRAY" : "null";
        }
    });

    private final String toStringValue;
    private final IType elementType;
    private final Function<String, String> defaultValueExpressionConverter;

    private ArrayType(IType elementType, Function<String, String> defaultValueExpressionConverter) {
        this.toStringValue = elementType + "[]";
        this.elementType = elementType;
        this.defaultValueExpressionConverter = defaultValueExpressionConverter;
    }

    /**
     * Gets the element type of the array.
     *
     * @return The element type of the array.
     */
    public IType getElementType() {
        return elementType;
    }

    @Override
    public String toString() {
        return toStringValue;
    }

    @Override
    public IType asNullable() {
        return this;
    }

    @Override
    public boolean contains(IType type) {
        return this == type || getElementType().contains(type);
    }

    @Override
    public void addImportsTo(Consumer<Collection<String>> importConsumer, boolean includeImplementationImports) {
        getElementType().addImportsTo(importConsumer, includeImplementationImports);
    }

    @Override
    public String defaultValueExpression(String sourceExpression) {
        return defaultValueExpressionConverter.apply(sourceExpression);
    }

    @Override
    public String defaultValueExpression() {
        return defaultValueExpression(null);
    }

    @Override
    public IType getClientType() {
        // The only supported array type is byte[]
        return this;
    }

    @Override
    public String convertToClientType(String expression) {
        // The only supported array type is byte[]
        return expression;
    }

    @Override
    public String convertFromClientType(String expression) {
        // The only supported array type is byte[]
        return expression;
    }

    @Override
    public String validate(String expression) {
        return null;
    }

    @Override
    public String jsonToken() {
        return "JsonToken.START_ARRAY";
    }

    @Override
    public String jsonDeserializationMethod(String jsonReaderName) {
        return jsonReaderName + ".getBinary()";
    }

    @Override
    public String jsonSerializationMethodCall(String jsonWriterName, String fieldName, String valueGetter,
        boolean jsonMergePatch) {
        return fieldName == null
            ? jsonWriterName + ".writeBinary(" + valueGetter + ")"
            : jsonWriterName + ".writeBinaryField(\"" + fieldName + "\", " + valueGetter + ")";
    }

    @Override
    public String xmlDeserializationMethod(String xmlReaderName, String attributeName, String attributeNamespace,
        boolean namespaceIsConstant) {
        if (attributeName == null) {
            return xmlReaderName + ".getBinaryElement()";
        } else if (attributeNamespace == null) {
            return xmlReaderName + ".getBinaryAttribute(null, \"" + attributeName + "\")";
        } else {
            return namespaceIsConstant
                ? xmlReaderName + ".getBinaryAttribute(" + attributeNamespace + ", \"" + attributeName + "\")"
                : xmlReaderName + ".getBinaryAttribute(\"" + attributeNamespace + "\", \"" + attributeName + "\")";
        }
    }

    @Override
    public String xmlSerializationMethodCall(String xmlWriterName, String attributeOrElementName, String namespaceUri,
        String valueGetter, boolean isAttribute, boolean nameIsVariable, boolean namespaceIsConstant) {
        return ClassType.xmlSerializationCallHelper(xmlWriterName, "writeBinary", attributeOrElementName, namespaceUri,
            valueGetter, isAttribute, nameIsVariable, namespaceIsConstant);
    }

    @Override
    public boolean isUsedInXml() {
        return false;
    }
}
