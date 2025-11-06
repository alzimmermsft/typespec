// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.typespec.http.client.generator.core.model.clientmodel;

import java.util.Set;

public class Annotation {
    private static final String V1_ANNOTATIONS_PACKAGE = "com.azure.core.annotation";
    private static final String CLIENT_CORE_HTTP_ANNOTATIONS_PACKAGE = "io.clientcore.core.http.annotations";
    private static final String CLIENT_CORE_ANNOTATIONS_PACKAGE = "io.clientcore.core.annotations";

    public static final Annotation GENERATED = new Annotation(V1_ANNOTATIONS_PACKAGE, "Generated");
    public static final Annotation HOST = new Annotation(V1_ANNOTATIONS_PACKAGE, "Host");
    public static final Annotation SERVICE_INTERFACE = new Annotation(V1_ANNOTATIONS_PACKAGE, "ServiceInterface");
    public static final Annotation SERVICE_CLIENT = new Annotation(V1_ANNOTATIONS_PACKAGE, "ServiceClient");
    public static final Annotation SERVICE_METHOD = new Annotation(V1_ANNOTATIONS_PACKAGE, "ServiceMethod");
    public static final Annotation SERVICE_CLIENT_BUILDER
        = new Annotation(V1_ANNOTATIONS_PACKAGE, "ServiceClientBuilder");
    public static final Annotation UNEXPECTED_RESPONSE_EXCEPTION_TYPE
        = new Annotation(V1_ANNOTATIONS_PACKAGE, "UnexpectedResponseExceptionType");
    public static final Annotation EXPECTED_RESPONSE = new Annotation(V1_ANNOTATIONS_PACKAGE, "ExpectedResponses");
    public static final Annotation HEADERS = new Annotation(V1_ANNOTATIONS_PACKAGE, "Headers");
    public static final Annotation FORM_PARAM = new Annotation(V1_ANNOTATIONS_PACKAGE, "FormParam");
    public static final Annotation RETURN_VALUE_WIRE_TYPE
        = new Annotation(V1_ANNOTATIONS_PACKAGE, "ReturnValueWireType");
    public static final Annotation RETURN_TYPE = new Annotation(V1_ANNOTATIONS_PACKAGE, "ReturnType");
    public static final Annotation IMMUTABLE = new Annotation(V1_ANNOTATIONS_PACKAGE, "Immutable");
    public static final Annotation FLUENT = new Annotation(V1_ANNOTATIONS_PACKAGE, "Fluent");
    public static final Annotation HEADER_COLLECTION = new Annotation(V1_ANNOTATIONS_PACKAGE, "HeaderCollection");

    public static final Annotation METADATA = new Annotation(CLIENT_CORE_ANNOTATIONS_PACKAGE, "Metadata");
    public static final Annotation METADATA_PROPERTIES
        = new Annotation(CLIENT_CORE_ANNOTATIONS_PACKAGE, "MetadataProperties");
    public static final Annotation HTTP_REQUEST_INFORMATION
        = new Annotation(CLIENT_CORE_HTTP_ANNOTATIONS_PACKAGE, "HttpRequestInformation");
    public static final Annotation UNEXPECTED_RESPONSE_EXCEPTION_INFORMATION
        = new Annotation(CLIENT_CORE_HTTP_ANNOTATIONS_PACKAGE, "UnexpectedResponseExceptionDetail");

    private final String fullName;
    private final String packageName;
    private final String name;

    private Annotation(String packageName, String name) {
        this.packageName = packageName;
        this.name = name;
        this.fullName = packageName + "." + name;
    }

    public final String getPackage() {
        return packageName;
    }

    public final String getName() {
        return name;
    }

    public final void addImportsTo(Set<String> imports) {
        imports.add(fullName);
    }
}
