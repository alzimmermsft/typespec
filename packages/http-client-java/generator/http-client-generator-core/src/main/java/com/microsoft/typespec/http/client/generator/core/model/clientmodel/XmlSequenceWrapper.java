// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.typespec.http.client.generator.core.model.clientmodel;

import com.microsoft.typespec.http.client.generator.core.extension.model.codemodel.ArraySchema;
import com.microsoft.typespec.http.client.generator.core.extension.plugin.JavaSettings;
import com.microsoft.typespec.http.client.generator.core.mapper.Mappers;
import com.microsoft.typespec.http.client.generator.core.util.CodeNamer;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * The details needed to create an XML sequence wrapper class for the client.
 */
public final class XmlSequenceWrapper {
    private final String packageName;
    private final IType sequenceType;
    private final String xmlRootElementName;
    private final String xmlRootElementNamespace;
    private final String xmlListElementName;
    private final String xmlListElementNamespace;
    private final String wrapperClassName;

    public XmlSequenceWrapper(String modelTypeName, ArraySchema arraySchema, JavaSettings settings) {
        boolean wrapperHasXmlSerialization
            = arraySchema.getSerialization() != null && arraySchema.getSerialization().getXml() != null;
        boolean elementHasXmlSerialization = arraySchema.getElementType().getSerialization() != null
            && arraySchema.getElementType().getSerialization().getXml() != null;

        if (wrapperHasXmlSerialization) {
            xmlRootElementName = arraySchema.getSerialization().getXml().getName();
            xmlRootElementNamespace = arraySchema.getSerialization().getXml().getNamespace();
        } else {
            xmlRootElementName = arraySchema.getLanguage().getDefault().getSerializedName();
            xmlRootElementNamespace = arraySchema.getLanguage().getDefault().getNamespace();
        }

        if (elementHasXmlSerialization) {
            xmlListElementName = arraySchema.getElementType().getSerialization().getXml().getName();
            xmlListElementNamespace = arraySchema.getElementType().getSerialization().getXml().getNamespace();
        } else {
            xmlListElementName = arraySchema.getElementType().getLanguage().getDefault().getSerializedName();
            xmlListElementNamespace = arraySchema.getElementType().getLanguage().getDefault().getNamespace();
        }

        sequenceType = Mappers.getSchemaMapper().map(arraySchema);
        boolean isCustomType = settings.isCustomType(CodeNamer.toPascalCase(modelTypeName + "Wrapper"));
        packageName = isCustomType
            ? settings.getPackage(settings.getCustomTypesSubpackage())
            : settings.getPackage(settings.getImplementationSubpackage() + ".models");

        this.wrapperClassName = modelTypeName + "Wrapper";
    }

    public String getPackage() {
        return packageName;
    }

    public IType getSequenceType() {
        return sequenceType;
    }

    public String getXmlRootElementName() {
        return xmlRootElementName;
    }

    public String getXmlRootElementNamespace() {
        return xmlRootElementNamespace;
    }

    public String getXmlListElementName() {
        return xmlListElementName;
    }

    public String getXmlListElementNamespace() {
        return xmlListElementNamespace;
    }

    public String getWrapperClassName() {
        return wrapperClassName;
    }

    /**
     * Consumes the XML sequence wrapper's imports.
     *
     * @param importConsumer The import consumer.
     */
    public void addImportsTo(Consumer<Collection<String>> importConsumer) {
        if (!JavaSettings.getInstance().isStreamStyleSerialization()) {
            importConsumer.accept(
                List.of("com.fasterxml.jackson.annotation.JsonCreator", "com.fasterxml.jackson.annotation.JsonProperty",
                    "com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty",
                    "com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement",
                    "com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText"));
        }

        sequenceType.addImportsTo(importConsumer, true);
    }
}
