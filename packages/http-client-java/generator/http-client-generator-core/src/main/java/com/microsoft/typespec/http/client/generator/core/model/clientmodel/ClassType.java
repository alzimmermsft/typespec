// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.typespec.http.client.generator.core.model.clientmodel;

import com.microsoft.typespec.http.client.generator.core.extension.model.extensionmodel.XmsExtensions;
import com.microsoft.typespec.http.client.generator.core.extension.plugin.JavaSettings;
import com.microsoft.typespec.http.client.generator.core.util.TemplateUtil;
import io.clientcore.core.utils.CoreUtils;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * The details of a class type that is used by a client.
 */
public class ClassType implements IType {

    private static class ClassDetails {

        private final String azureClass;
        private final String genericClass;
        private final String azureVNextClass;

        ClassDetails(String azureClass, String genericClass, String azureVNextClass) {
            this.azureClass = azureClass;
            this.genericClass = genericClass;
            this.azureVNextClass = azureVNextClass;
        }

        String getAzureClass() {
            return azureClass;
        }

        String getAzureVNextClass() {
            return azureVNextClass;
        }

        String getGenericClass() {
            return genericClass;
        }

    }

    private static final Map<String, ClassDetails> CLASS_TYPE_MAPPING = Map.ofEntries(
        sameName("com.azure.core.http.rest", "io.clientcore.core.http", "RestProxy"),
        sameName("com.azure.core.http", "io.clientcore.core.http.pipeline", "HttpPipeline"),
        sameName("com.azure.core.http", "io.clientcore.core.http.pipeline", "HttpPipelineBuilder"),
        sameName("com.azure.core.util", "io.clientcore.core.utils", "Context"),
        sameName("com.azure.core.http", "io.clientcore.core.http.client", "HttpClient"),
        fullPath("com.azure.core.http.policy.HttpLogOptions",
            "io.clientcore.core.http.pipeline.HttpInstrumentationOptions"),
        sameName("com.azure.core.http.policy", "io.clientcore.core.http.pipeline", "HttpPipelinePolicy"),
        sameName("com.azure.core.credential", "io.clientcore.core.credentials", "KeyCredential"),
        sameName("com.azure.core.http.policy", "io.clientcore.core.http.pipeline", "KeyCredentialPolicy"),
        fullPath("com.azure.core.http.policy.RetryPolicy", "io.clientcore.core.http.pipeline.HttpRetryPolicy"),
        fullPath("com.azure.core.http.policy.RedirectPolicy", "io.clientcore.core.http.pipeline.HttpRedirectPolicy"),
        fullPath("com.azure.core.http.policy.HttpLoggingPolicy",
            "io.clientcore.core.http.pipeline.HttpInstrumentationPolicy"),
        sameName("com.azure.core.util", "io.clientcore.core.utils.configuration", "Configuration"),
        sameName("com.azure.core.http", "io.clientcore.core.http.models", "HttpHeaders"),
        sameName("com.azure.core.http", "io.clientcore.core.http.models", "HttpHeader"),
        sameName("com.azure.core.http", "io.clientcore.core.http.models", "HttpHeaderName"),
        sameName("com.azure.core.http", "io.clientcore.core.http.models", "HttpRequest"),
        sameName("com.azure.core.http", "io.clientcore.core.http.models", "HttpResponse"),
        sameName("com.azure.core.util", "io.clientcore.core.models.binarydata", "BinaryData"),
        fullPath("com.azure.core.http.policy.RetryOptions", "io.clientcore.core.http.pipeline.HttpRetryOptions"),
        sameName("com.azure.core.http", "io.clientcore.core.http.models", "ProxyOptions"),
        sameName("com.azure.core.http.rest", "io.clientcore.core.http.models", "Response"),
        sameName("com.azure.core.http.rest", "io.clientcore.core.http", "SimpleResponse"),
        fullPath("com.azure.core.util.ExpandableStringEnum", "io.clientcore.core.utils.ExpandableEnum"),
        sameName("com.azure.core.util", "io.clientcore.core.utils", "ExpandableEnum"),
        sameName("com.azure.core.exception", "io.clientcore.core.http.models", "HttpResponseException"),
        sameName("com.azure.core.client.traits", "io.clientcore.core.traits", "HttpTrait"),
        sameName("com.azure.core.client.traits", "io.clientcore.core.traits", "ConfigurationTrait"),
        sameName("com.azure.core.client.traits", "io.clientcore.core.traits", "EndpointTrait"),
        sameName("com.azure.core.client.traits", "io.clientcore.core.traits", "KeyCredentialTrait"),
        sameName("com.azure.core.util.logging", "io.clientcore.core.instrumentation.logging", "ClientLogger"),
        sameName("com.azure.core.util.logging", "io.clientcore.core.instrumentation.logging", "LogLevel"),
        sameName("com.azure.core.util", "io.clientcore.core.http.models", "ServiceVersion"),
        sameName("com.azure.core.http.policy", "io.clientcore.core.http.pipeline", "UserAgentPolicy"),
        sameName("com.azure.core.util", "io.clientcore.core.utils", "DateTimeRfc1123"),
        fullPath("com.azure.core.util.Base64Url", "io.clientcore.core.utils.Base64Uri"),
        fullPath("com.azure.core.credential.TokenCredential",
            "io.clientcore.core.credentials.oauth.OAuthTokenCredential",
            "com.azure.v2.core.credentials.TokenCredential"),
        fullPath("com.azure.core.client.traits.TokenCredentialTrait",
            "io.clientcore.core.traits.OAuthTokenCredentialTrait", "com.azure.v2.core.traits.TokenCredentialTrait"),
        fullPath("com.azure.core.http.policy.BearerTokenAuthenticationPolicy",
            "io.clientcore.core.http.pipeline.OAuthBearerTokenAuthenticationPolicy",
            "com.azure.v2.core.http.pipeline.BearerTokenAuthenticationPolicy"),
        sameName("com.azure.core.util", "io.clientcore.core.utils", "CoreUtils"),
        fullPath("com.azure.core.http.MatchConditions", "io.clientcore.core.http.models.HttpMatchConditions"),
        fullPath("com.azure.core.http.RequestConditions", "io.clientcore.core.http.models.HttpRequestConditions"),
        fullPath("com.azure.core.models.ResponseError", "io.clientcore.core.Error",
            "com.azure.v2.core.models.AzureResponseError"),
        fullPath("com.azure.core.util.polling.SyncPoller", "io.clientcore.core.Poller",
            "com.azure.v2.core.http.polling.Poller"),
        fullPath("com.azure.core.util.polling.PollingStrategyOptions", "io.clientcore.core.PollingStrategyOptions",
            "com.azure.v2.core.http.polling.PollingStrategyOptions"));

    private static Map.Entry<String, ClassDetails> sameName(String azurePackage, String genericPackage,
        String className) {
        String azureFullPath = azurePackage + "." + className;
        String genericFullPath = genericPackage + "." + className;

        return fullPath(azureFullPath, genericFullPath, genericFullPath);
    }

    private static Map.Entry<String, ClassDetails> fullPath(String azureClass, String genericClass) {
        return fullPath(azureClass, genericClass, genericClass);
    }

    private static Map.Entry<String, ClassDetails> fullPath(String azureClass, String genericClass,
        String azureVNextClass) {
        return Map.entry(azureClass, new ClassDetails(azureClass, genericClass, azureVNextClass));
    }

    private static ClassType getClassType(String packageName, String className) {
        return getClassTypeBuilder(packageName, className).build();
    }

    private static Builder getClassTypeBuilder(String packageName, String className) {
        return getClassTypeBuilder(packageName, className, false);
    }

    private static Builder getClassTypeBuilder(String packageName, String className, boolean isSwaggerType) {
        ClassDetails mapping = CLASS_TYPE_MAPPING.get(packageName + "." + className);
        if (JavaSettings.getInstance().isAzureV2()) {
            if (mapping != null) {
                return new Builder(false).knownClass(mapping.getAzureVNextClass());
            } else {
                return new Builder(isSwaggerType).packageName(replacePakcageName(packageName)).name(className);
            }
        } else if (!JavaSettings.getInstance().isAzureV1()) {
            if (mapping != null) {
                String genericClass = mapping.getGenericClass();
                if (CoreUtils.isNullOrEmpty(genericClass)) {
                    return null;
                }
                return new Builder(isSwaggerType).knownClass(genericClass);
            } else {
                return new Builder(isSwaggerType).packageName(replacePakcageName(packageName)).name(className);
            }
        } else {
            if (mapping != null) {
                return new Builder(isSwaggerType).knownClass(mapping.getAzureClass());
            } else {
                return new Builder(isSwaggerType).packageName(packageName).name(className);
            }
        }
    }

    private static String replacePakcageName(String packageName) {
        return packageName.replace(ExternalPackage.AZURE_CORE_PACKAGE_NAME, ExternalPackage.CLIENTCORE_PACKAGE_NAME)
            .replace(ExternalPackage.AZURE_JSON_PACKAGE_NAME, ExternalPackage.CLIENTCORE_JSON_PACKAGE_NAME)
            .replace(ExternalPackage.AZURE_XML_PACKAGE_NAME, ExternalPackage.CLIENTCORE_XML_PACKAGE_NAME);
    }

    public static final ClassType REQUEST_CONDITIONS = getClassType("com.azure.core.http", "RequestConditions");
    public static final ClassType MATCH_CONDITIONS = getClassType("com.azure.core.http", "MatchConditions");
    public static final ClassType CORE_UTILS = getClassType("com.azure.core.util", "CoreUtils");
    public static final ClassType RESPONSE = getClassType("com.azure.core.http.rest", "Response");
    public static final ClassType SIMPLE_RESPONSE = getClassType("com.azure.core.http.rest", "SimpleResponse");
    public static final ClassType EXPANDABLE_STRING_ENUM = getClassType("com.azure.core.util", "ExpandableStringEnum");
    public static final ClassType EXPANDABLE_ENUM = getClassType("com.azure.core.util", "ExpandableEnum");
    public static final ClassType HTTP_PIPELINE_BUILDER = getClassType("com.azure.core.http", "HttpPipelineBuilder");
    public static final ClassType KEY_CREDENTIAL_POLICY
        = getClassType("com.azure.core.http.policy", "KeyCredentialPolicy");
    public static final ClassType BEARER_TOKEN_POLICY
        = getClassType("com.azure.core.http.policy", "BearerTokenAuthenticationPolicy");
    public static final ClassType AZURE_KEY_CREDENTIAL_TRAIT
        = new ClassType("com.azure.core.client.traits", "AzureKeyCredentialTrait");
    public static final ClassType KEY_CREDENTIAL_TRAIT
        = getClassType("com.azure.core.client.traits", "KeyCredentialTrait");
    public static final ClassType ENDPOINT_TRAIT = getClassType("com.azure.core.client.traits", "EndpointTrait");
    public static final ClassType HTTP_TRAIT = getClassType("com.azure.core.client.traits", "HttpTrait");
    public static final ClassType CONFIGURATION_TRAIT
        = getClassType("com.azure.core.client.traits", "ConfigurationTrait");
    public static final ClassType PROXY_TRAIT = new ClassType("io.clientcore.core.traits", "ProxyTrait");
    public static final ClassType POLL_OPERATION_DETAILS
        = getClassType("com.azure.core.util.polling", "PollOperationDetails");
    public static final ClassType JSON_SERIALIZABLE = getClassType("com.azure.json", "JsonSerializable");
    public static final ClassType JSON_WRITER = getClassType("com.azure.json", "JsonWriter");
    public static final ClassType JSON_READER = getClassType("com.azure.json", "JsonReader");
    public static final ClassType JSON_TOKEN = getClassType("com.azure.json", "JsonToken");

    public static final ClassType XML_SERIALIZABLE = getClassType("com.azure.xml", "XmlSerializable");
    public static final ClassType XML_WRITER = getClassType("com.azure.xml", "XmlWriter");
    public static final ClassType XML_READER = getClassType("com.azure.xml", "XmlReader");
    public static final ClassType XML_TOKEN = getClassType("com.azure.xml", "XmlToken");

    public static final ClassType VOID = new ClassType(Void.class);

    public static final ClassType INSTRUMENTATION
        = new ClassType("io.clientcore.core.instrumentation", "Instrumentation");

    public static final ClassType SDK_INSTRUMENTATION_OPTIONS
        = new ClassType("io.clientcore.core.instrumentation", "SdkInstrumentationOptions");

    public static final ClassType BOOLEAN = new Builder(false).knownClass(Boolean.class)
        .defaultValueExpressionConverter(String::toLowerCase)
        .jsonToken("JsonToken.BOOLEAN")
        .jsonDeserializationMethod("getNullable(JsonReader::getBoolean)")
        .serializationMethodBase("writeBoolean")
        .xmlElementDeserializationMethod("getNullableElement(Boolean::parseBoolean)")
        .xmlAttributeDeserializationTemplate("%s.getNullableAttribute(%s, %s, Boolean::parseBoolean)")
        .build();

    public static final ClassType BYTE = new Builder(false).knownClass(Byte.class)
        .jsonDeserializationMethod("getNullable(JsonReader::getInt)")
        .jsonToken("JsonToken.NUMBER")
        .serializationMethodBase("writeNumber")
        .xmlElementDeserializationMethod("getNullableElement(Byte::parseByte)")
        .xmlAttributeDeserializationTemplate("%s.getNullableAttribute(%s, %s, Byte::parseByte)")
        .build();

    public static final ClassType INTEGER = new Builder(false).knownClass(Integer.class)
        .defaultValueExpressionConverter(Function.identity())
        .jsonToken("JsonToken.NUMBER")
        .jsonDeserializationMethod("getNullable(JsonReader::getInt)")
        .serializationMethodBase("writeNumber")
        .xmlElementDeserializationMethod("getNullableElement(Integer::parseInt)")
        .xmlAttributeDeserializationTemplate("%s.getNullableAttribute(%s, %s, Integer::parseInt)")
        .build();

    public static final ClassType INTEGER_AS_STRING = new Builder(false).knownClass(Integer.class)
        .defaultValueExpressionConverter(
            defaultValueExpression -> "Integer.parseInt(\"" + defaultValueExpression + "\")")
        .jsonToken("JsonToken.NUMBER")
        .jsonDeserializationMethod("getNullable(nonNullReader -> Integer.parseInt(nonNullReader.getString()))")
        .serializationMethodBase("writeString")
        .serializationValueGetterModifier(valueGetter -> "Objects.toString(" + valueGetter + ", null)")
        .xmlElementDeserializationMethod("getNullableElement(Integer::valueOf)")
        .xmlAttributeDeserializationTemplate("%s.getNullableAttribute(%s, %s, Integer::valueOf)")
        .build();

    public static final ClassType LONG = new Builder(false).prototypeAsLong().build();

    public static final ClassType LONG_AS_STRING = new Builder(false).prototypeAsLong()
        .defaultValueExpressionConverter(defaultValueExpression -> "Long.parseLong(\"" + defaultValueExpression + "\")")
        .jsonToken("JsonToken.STRING")
        .jsonDeserializationMethod("getNullable(nonNullReader -> Long.parseLong(nonNullReader.getString()))")
        .serializationMethodBase("writeString")
        .serializationValueGetterModifier(valueGetter -> "Objects.toString(" + valueGetter + ", null)")
        .xmlElementDeserializationMethod("getNullableElement(Long::valueOf)")
        .xmlAttributeDeserializationTemplate("%s.getNullableAttribute(%s, %s, Long::valueOf)")
        .build();

    public static final ClassType FLOAT = new Builder(false).knownClass(Float.class)
        .defaultValueExpressionConverter(defaultValueExpression -> Float.parseFloat(defaultValueExpression) + "F")
        .jsonToken("JsonToken.NUMBER")
        .jsonDeserializationMethod("getNullable(JsonReader::getFloat)")
        .serializationMethodBase("writeNumber")
        .xmlElementDeserializationMethod("getNullableElement(Float::parseFloat)")
        .xmlAttributeDeserializationTemplate("%s.getNullableAttribute(%s, %s, Float::parseFloat)")
        .build();

    public static final ClassType DOUBLE = new Builder(false).knownClass(Double.class).prototypeAsDouble().build();

    public static final ClassType CHARACTER = new Builder(false).knownClass(Character.class)
        .defaultValueExpressionConverter(defaultValueExpression -> String.valueOf((defaultValueExpression.charAt(0))))
        .jsonToken("JsonToken.STRING")
        .serializationValueGetterModifier(valueGetter -> "Objects.toString(" + valueGetter + ", null)")
        .jsonDeserializationMethod("getNullable(nonNullReader -> nonNullReader.getString().charAt(0))")
        .serializationMethodBase("writeString")
        .xmlElementDeserializationMethod("getNullableElement(nonNullString -> nonNullString.charAt(0))")
        .xmlAttributeDeserializationTemplate(
            "%s.getNullableAttribute(%s, %s, nonNullString -> nonNullString.charAt(0))")
        .build();

    public static final ClassType STRING = new Builder(false).knownClass(String.class)
        .defaultValueExpressionConverter(
            defaultValueExpression -> "\"" + TemplateUtil.escapeString(defaultValueExpression) + "\"")
        .jsonToken("JsonToken.STRING")
        .jsonDeserializationMethod("getString()")
        .serializationMethodBase("writeString")
        .xmlElementDeserializationMethod("getStringElement()")
        .xmlAttributeDeserializationTemplate("%s.getStringAttribute(%s, %s)")
        .build();

    public static final ClassType BASE_64_URL = getClassTypeBuilder("com.azure.core.util", "Base64Url")
        .serializationValueGetterModifier(valueGetter -> "Objects.toString(" + valueGetter + ", null)")
        .jsonToken("JsonToken.STRING")
        .jsonDeserializationMethod("getNullable(nonNullReader -> new "
            + (JavaSettings.getInstance().isAzureV1() ? "Base64Url" : "Base64Uri") + "(nonNullReader.getString()))")
        .serializationMethodBase("writeString")
        .xmlElementDeserializationMethod(
            "getNullableElement(" + (JavaSettings.getInstance().isAzureV1() ? "Base64Url" : "Base64Uri") + "::new)")
        .xmlAttributeDeserializationTemplate("%s.getNullableAttribute(%s, %s, "
            + (JavaSettings.getInstance().isAzureV1() ? "Base64Url" : "Base64Uri") + "::new)")
        .build();

    public static final ClassType LOCAL_DATE = new Builder(false).knownClass(java.time.LocalDate.class)
        .defaultValueExpressionConverter(
            defaultValueExpression -> "LocalDate.parse(\"" + defaultValueExpression + "\")")
        .jsonToken("JsonToken.STRING")
        .serializationValueGetterModifier(valueGetter -> "Objects.toString(" + valueGetter + ", null)")
        .jsonDeserializationMethod("getNullable(nonNullReader -> LocalDate.parse(nonNullReader.getString()))")
        .serializationMethodBase("writeString")
        .xmlElementDeserializationMethod("getNullableElement(LocalDate::parse)")
        .xmlAttributeDeserializationTemplate("%s.getNullableAttribute(%s, %s, LocalDate::parse)")
        .build();

    public static final ClassType DATE_TIME = new Builder(false).knownClass(OffsetDateTime.class)
        .defaultValueExpressionConverter(
            defaultValueExpression -> "OffsetDateTime.parse(\"" + defaultValueExpression + "\")")
        .jsonToken("JsonToken.STRING")
        .serializationValueGetterModifier(valueGetter -> valueGetter
            + " == null ? null : DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(" + valueGetter + ")")
        .jsonDeserializationMethod(JavaSettings.getInstance().isAzureV1()
            ? ("getNullable(nonNullReader -> " + CORE_UTILS.getName()
                + ".parseBestOffsetDateTime(nonNullReader.getString()))")
            : ("getNullable(nonNullReader -> OffsetDateTime.parse(nonNullReader.getString()))"))
        .serializationMethodBase("writeString")
        .xmlElementDeserializationMethod(JavaSettings.getInstance().isAzureV1()
            ? ("getNullableElement(dateString -> " + CORE_UTILS.getName() + ".parseBestOffsetDateTime(dateString))")
            : ("getNullableElement(dateString -> OffsetDateTime.parse(dateString))"))
        .xmlAttributeDeserializationTemplate(JavaSettings.getInstance().isAzureV1()
            ? ("%s.getNullableAttribute(%s, %s, dateString -> " + CORE_UTILS.getName()
                + ".parseBestOffsetDateTime(dateString))")
            : ("%s.getNullableAttribute(%s, %s, dateString -> OffsetDateTime.parse(dateString))"))
        .build();

    public static final ClassType DURATION = new Builder(false).knownClass(Duration.class)
        .defaultValueExpressionConverter(defaultValueExpression -> "Duration.parse(\"" + defaultValueExpression + "\")")
        .jsonToken("JsonToken.STRING")
        .serializationValueGetterModifier(valueGetter -> JavaSettings.getInstance().isAzureV1()
            ? CORE_UTILS.getName() + ".durationToStringWithDays(" + valueGetter + ")"
            : "Objects.toString(" + valueGetter + ", null)")
        .jsonDeserializationMethod("getNullable(nonNullReader -> Duration.parse(nonNullReader.getString()))")
        .serializationMethodBase("writeString")
        .xmlElementDeserializationMethod("getNullableElement(Duration::parse)")
        .xmlAttributeDeserializationTemplate("%s.getNullableAttribute(%s, %s, Duration::parse)")
        .build();

    public static final ClassType DATE_TIME_RFC_1123 = getClassTypeBuilder("com.azure.core.util", "DateTimeRfc1123")
        .defaultValueExpressionConverter(
            defaultValueExpression -> "new DateTimeRfc1123(\"" + defaultValueExpression + "\")")
        .jsonToken("JsonToken.STRING")
        .serializationValueGetterModifier(valueGetter -> "Objects.toString(" + valueGetter + ", null)")
        .jsonDeserializationMethod("getNullable(nonNullReader -> new DateTimeRfc1123(nonNullReader.getString()))")
        .serializationMethodBase("writeString")
        .xmlElementDeserializationMethod("getNullableElement(DateTimeRfc1123::new)")
        .xmlAttributeDeserializationTemplate("%s.getNullableAttribute(%s, %s, DateTimeRfc1123::new)")
        .build();

    public static final ClassType BIG_DECIMAL = new Builder(false).knownClass(BigDecimal.class)
        .defaultValueExpressionConverter(defaultValueExpression -> "new BigDecimal(\"" + defaultValueExpression + "\")")
        .jsonToken("JsonToken.NUMBER")
        .serializationMethodBase("writeNumber")
        .jsonDeserializationMethod("getNullable(nonNullReader -> new BigDecimal(nonNullReader.getString()))")
        .xmlElementDeserializationMethod("getNullableElement(BigDecimal::new)")
        .xmlAttributeDeserializationTemplate("%s.getNullableAttribute(%s, %s, BigDecimal::new)")
        .build();

    public static final ClassType UUID = new Builder(false).knownClass(java.util.UUID.class)
        .defaultValueExpressionConverter(
            defaultValueExpression -> "UUID.fromString(\"" + defaultValueExpression + "\")")
        .jsonToken("JsonToken.STRING")
        .serializationValueGetterModifier(valueGetter -> "Objects.toString(" + valueGetter + ", null)")
        .jsonDeserializationMethod("getNullable(nonNullReader -> UUID.fromString(nonNullReader.getString()))")
        .serializationMethodBase("writeString")
        .xmlElementDeserializationMethod("getNullableElement(UUID::fromString)")
        .xmlAttributeDeserializationTemplate("%s.getNullableAttribute(%s, %s, UUID::fromString)")
        .build();

    public static final ClassType OBJECT = new ClassType(Objects.class);

    public static final ClassType ACCESS_TOKEN = new ClassType("com.azure.core.credential", "AccessToken");
    public static final ClassType TOKEN_CREDENTIAL = getClassType("com.azure.core.credential", "TokenCredential");
    public static final ClassType OAUTH_TOKEN_REQUEST_CONTEXT
        = new ClassType("io.clientcore.core.credentials.oauth", "OAuthTokenRequestContext");
    public static final ClassType TOKEN_CREDENTIAL_TRAIT
        = getClassType("com.azure.core.client.traits", "TokenCredentialTrait");

    public static final ClassType UNIX_TIME_DATE_TIME = new Builder(false)
        .defaultValueExpressionConverter(
            defaultValueExpression -> "OffsetDateTime.parse(\"" + defaultValueExpression + "\")")
        .jsonToken("JsonToken.STRING")
        .knownClass(OffsetDateTime.class)
        .serializationValueGetterModifier(valueGetter -> "Objects.toString(" + valueGetter + ", null)")
        .jsonDeserializationMethod("getNullable(nonNullReader -> OffsetDateTime.parse(nonNullReader.getString()))")
        .serializationMethodBase("writeString")
        .xmlElementDeserializationMethod("getNullableElement(OffsetDateTime::parse)")
        .xmlAttributeDeserializationTemplate("%s.getNullableAttribute(%s, %s, OffsetDateTime::parse)")
        .build();

    public static final ClassType UNIX_TIME_LONG = new Builder(false).prototypeAsLong().build();

    public static final ClassType DURATION_LONG = new Builder(false).prototypeAsLong().build();

    public static final ClassType DURATION_DOUBLE = new Builder(false).prototypeAsDouble().build();

    public static final ClassType HTTP_PIPELINE = getClassType("com.azure.core.http", "HttpPipeline");

    public static final ClassType REST_PROXY = getClassType("com.azure.core.http.rest", "RestProxy");

    public static final ClassType COLLECTION_FORMAT
        = new ClassType("com.azure.core.util.serializer", "CollectionFormat");
    public static final ClassType SERIALIZER_ADAPTER
        = new ClassType("com.azure.core.util.serializer", "SerializerAdapter");
    public static final ClassType SERIALIZE_ENCODING
        = new ClassType("com.azure.core.util.serializer", "SerializerEncoding");

    public static final ClassType JSON_SERIALIZER = getClassType("com.azure.core.util.serializer", "JsonSerializer");

    public static final ClassType FUNCTION = new ClassType(Function.class);
    public static final ClassType BYTE_BUFFER = new ClassType(ByteBuffer.class);

    public static final ClassType URL = new Builder(false)
        .defaultValueExpressionConverter(defaultValueExpression -> "new URL(\"" + defaultValueExpression + "\")")
        .knownClass(java.net.URL.class)
        .jsonToken("JsonToken.STRING")
        .serializationValueGetterModifier(valueGetter -> "Objects.toString(" + valueGetter + ", null)")
        .jsonDeserializationMethod("getNullable(nonNullReader -> new URL(nonNullReader.getString()))")
        .serializationMethodBase("writeString")
        .xmlElementDeserializationMethod(
            "getNullableElement(urlString -> { try { return new URL(urlString); } catch (MalformedURLException e) { throw new XMLStreamException(e); } })")
        .xmlAttributeDeserializationTemplate("%s.getNullableAttribute(%s, %s, URL::new)")
        .build();

    public static final ClassType STREAM_RESPONSE = new ClassType("com.azure.core.http.rest", "StreamResponse");

    public static final ClassType INPUT_STREAM = new ClassType(InputStream.class);
    public static final ClassType PAGED_FLUX = new ClassType("com.azure.core.http.rest", "PagedFlux");
    public static final ClassType PAGED_ITERABLE = getClassType("com.azure.core.http.rest", "PagedIterable");
    public static final ClassType RESPONSE_BASE = getClassType("com.azure.core.http.rest", "ResponseBase");
    public static final ClassType PAGED_RESPONSE = getClassType("com.azure.core.http.rest", "PagedResponse");
    public static final ClassType PAGED_RESPONSE_BASE = getClassType("com.azure.core.http.rest", "PagedResponseBase");

    public static final ClassType CONTEXT
        = getClassTypeBuilder("com.azure.core.util", "Context").defaultValueExpressionConverter(
            epr -> (JavaSettings.getInstance().isAzureV1() ? "com.azure.core.util." : "io.clientcore.core.utils.")
                + TemplateUtil.getContextNone())
            .build();

    public static final ClassType CLIENT_LOGGER = getClassType("com.azure.core.util.logging", "ClientLogger");
    public static final ClassType LOG_LEVEL = getClassType("com.azure.core.util.logging", "LogLevel");

    public static final ClassType AZURE_CLOUD = new ClassType("com.azure.core.models", "AzureCloud");

    public static final ClassType AZURE_ENVIRONMENT = new ClassType("com.azure.core.management", "AzureEnvironment");

    public static final ClassType HTTP_CLIENT = getClassType("com.azure.core.http", "HttpClient");

    public static final ClassType HTTP_PIPELINE_POLICY
        = getClassType("com.azure.core.http.policy", "HttpPipelinePolicy");
    public static final ClassType HTTP_PIPELINE_POSITION = getClassType("com.azure.core.http", "HttpPipelinePosition");
    public static final ClassType HTTP_LOG_OPTIONS = getClassType("com.azure.core.http.policy", "HttpLogOptions");
    public static final ClassType HTTP_LOG_DETAIL_LEVEL
        = getClassType("com.azure.core.http.policy", "HttpLogDetailLevel");

    public static final ClassType CONFIGURATION = getClassType("com.azure.core.util", "Configuration");

    public static final ClassType SERVICE_VERSION = getClassType("com.azure.core.util", "ServiceVersion");

    public static final ClassType AZURE_KEY_CREDENTIAL
        = new ClassType("com.azure.core.credential", "AzureKeyCredential");

    public static final ClassType KEY_CREDENTIAL = getClassType("com.azure.core.credential", "KeyCredential");
    public static final ClassType BASE_64_UTIL = getClassType("com.azure.core.util", "Base64Util");

    public static final ClassType HTTP_POLICY_PROVIDERS
        = new ClassType("com.azure.core.http.policy", "HttpPolicyProviders");
    public static final ClassType ADD_HEADERS_POLICY = getClassType("com.azure.core.http.policy", "AddHeadersPolicy");
    public static final ClassType ADD_HEADERS_FROM_CONTEXT_POLICY
        = new ClassType("com.azure.core.http.policy", "AddHeadersFromContextPolicy");
    public static final ClassType AZURE_KEY_CREDENTIAL_POLICY
        = new ClassType("com.azure.core.credential", "AzureKeyCredentialPolicy");
    public static final ClassType REQUEST_ID_POLICY = getClassType("com.azure.core.http.policy", "RequestIdPolicy");
    public static final ClassType ADD_DATE_POLICY = getClassType("com.azure.core.http.policy", "AddDatePolicy");
    public static final ClassType RETRY_POLICY = getClassType("com.azure.core.http.policy", "RetryPolicy");
    public static final ClassType USER_AGENT_POLICY = getClassType("com.azure.core.http.policy", "UserAgentPolicy");
    public static final ClassType USER_AGENT_OPTIONS
        = new ClassType("io.clientcore.core.http.pipeline", "UserAgentOptions");
    public static final ClassType REDIRECT_POLICY = getClassType("com.azure.core.http.policy", "RedirectPolicy");
    public static final ClassType HTTP_LOGGING_POLICY = getClassType("com.azure.core.http.policy", "HttpLoggingPolicy");

    public static final ClassType RETRY_OPTIONS = getClassType("com.azure.core.http.policy", "RetryOptions");

    public static final ClassType REDIRECT_OPTIONS
        = new ClassType("io.clientcore.core.http.pipeline", "HttpRedirectOptions");

    public static final ClassType JSON_PATCH_DOCUMENT = new Builder(false).packageName("com.azure.core.models")
        .name("JsonPatchDocument")
        .jsonToken("JsonToken.START_OBJECT")
        .build();

    public static final ClassType BINARY_DATA = getClassTypeBuilder("com.azure.core.util", "BinaryData")
        .defaultValueExpressionConverter(
            defaultValueExpression -> "BinaryData.fromObject(\"" + defaultValueExpression + "\")")
        // When used as model property, serialization code will not use the "writeUntyped(nullableVar)",
        // because some backend would fail the request on "null" value.
        .serializationMethodBase("writeUntyped")
        .serializationValueGetterModifier(
            valueGetter -> valueGetter + " == null ? null : " + valueGetter + ".toObject(Object.class)")
        .jsonDeserializationMethod("getNullable(nonNullReader -> BinaryData.fromObject(nonNullReader.readUntyped()))")
        .xmlElementDeserializationMethod("getNullableElement(BinaryData::fromObject)")
        .xmlAttributeDeserializationTemplate("%s.getNullableAttribute(%s, %s, BinaryData::fromObject)")
        .build();

    public static final ClassType REQUEST_OPTIONS = getClassType("com.azure.core.http.rest", "RequestOptions");
    public static final ClassType REQUEST_CONTEXT = new ClassType("io.clientcore.core.http.models", "RequestContext");
    public static final ClassType PROXY_OPTIONS = getClassType("com.azure.core.http", "ProxyOptions");
    public static final ClassType CLIENT_OPTIONS = getClassType("com.azure.core.util", "ClientOptions");
    public static final ClassType HTTP_REQUEST = getClassType("com.azure.core.http", "HttpRequest");
    public static final ClassType HTTP_HEADERS = getClassType("com.azure.core.http", "HttpHeaders");
    public static final ClassType HTTP_HEADER = getClassType("com.azure.core.http", "HttpHeader");
    public static final ClassType HTTP_HEADER_NAME = getClassType("com.azure.core.http", "HttpHeaderName");
    public static final ClassType HTTP_RESPONSE = getClassType("com.azure.core.http", "HttpResponse");

    // Java exception types
    public static final ClassType HTTP_RESPONSE_EXCEPTION
        = getClassType("com.azure.core.exception", "HttpResponseException");
    public static final ClassType CLIENT_AUTHENTICATION_EXCEPTION
        = getClassType("com.azure.core.exception", "ClientAuthenticationException");
    public static final ClassType RESOURCE_EXISTS_EXCEPTION
        = getClassType("com.azure.core.exception", "ResourceExistsException");
    public static final ClassType RESOURCE_MODIFIED_EXCEPTION
        = getClassType("com.azure.core.exception", "ResourceModifiedException");
    public static final ClassType RESOURCE_NOT_FOUND_EXCEPTION
        = getClassType("com.azure.core.exception", "ResourceNotFoundException");
    public static final ClassType TOO_MANY_REDIRECTS_EXCEPTION
        = getClassType("com.azure.core.exception", "TooManyRedirectsException");
    public static final ClassType RESPONSE_ERROR
        = getClassTypeBuilder("com.azure.core.models", "ResponseError", true).jsonToken("JsonToken.START_OBJECT")
            .build();
    public static final ClassType RESPONSE_INNER_ERROR = new Builder().packageName("com.azure.core.models")
        .name("ResponseInnerError")
        .jsonToken("JsonToken.START_OBJECT")
        .build();
    public static final ClassType POLL_RESULT = new ClassType("com.azure.core.management.polling", "PollResult");
    public static final ClassType POLLER_FACTORY = new ClassType("com.azure.core.management.polling", "PollerFactory");
    public static final ClassType SYNC_POLLER_FACTORY
        = new ClassType("com.azure.core.management.polling", "SyncPollerFactory");

    public static final ClassType ASYNC_POLL_RESPONSE
        = new ClassType("com.azure.core.utl.polling", "AsyncPollResponse");
    public static final ClassType LONG_RUNNING_OPERATION_STATUS
        = new ClassType("com.azure.core.util.polling", "LongRunningOperationStatus");
    public static final ClassType POLLER_FLUX = new ClassType("com.azure.core.util.polling", "PollerFlux");
    public static final ClassType SYNC_POLLER = getClassType("com.azure.core.util.polling", "SyncPoller");
    public static final ClassType POLLING_STRATEGY_OPTIONS
        = getClassType("com.azure.core.util.polling", "PollingStrategyOptions");

    public static final ClassType FLUX = new ClassType("reactor.core.publisher", "Flux");
    public static final ClassType MONO = new ClassType("reactor.core.publisher", "Mono");

    private final String fullName;
    private final String packageName;
    private final String name;
    private final List<String> implementationImports;
    private final XmsExtensions extensions;
    private final Function<String, String> defaultValueExpressionConverter;
    private final boolean isSwaggerType;
    private final Function<String, String> serializationValueGetterModifier;
    private final String jsonToken;
    private final String serializationMethodBase;
    private final String jsonDeserializationMethod;
    private final String xmlAttributeDeserializationTemplate;
    private final String xmlElementDeserializationMethod;
    private final boolean usedInXml;

    private ClassType(Class<?> knownClass) {
        this(knownClass.getPackageName(), knownClass.getSimpleName());
    }

    private ClassType(String packageName, String name) {
        this(packageName, name, null, null, null, false, null, null, null, null, null, null, false);
    }

    private ClassType(String packageName, String name, List<String> implementationImports, XmsExtensions extensions,
        Function<String, String> defaultValueExpressionConverter, boolean isSwaggerType, String jsonToken,
        String serializationMethodBase, Function<String, String> serializationValueGetterModifier,
        String jsonDeserializationMethod, String xmlAttributeDeserializationTemplate,
        String xmlElementDeserializationMethod, boolean usedInXml) {
        this.fullName = packageName + "." + name;
        this.packageName = packageName;
        this.name = name;
        this.implementationImports = implementationImports;
        this.extensions = extensions;
        this.defaultValueExpressionConverter = defaultValueExpressionConverter;
        this.isSwaggerType = isSwaggerType;
        this.jsonToken = jsonToken;
        this.serializationMethodBase = serializationMethodBase;
        this.serializationValueGetterModifier = serializationValueGetterModifier;
        this.jsonDeserializationMethod = jsonDeserializationMethod;
        this.xmlAttributeDeserializationTemplate = xmlAttributeDeserializationTemplate;
        this.xmlElementDeserializationMethod = xmlElementDeserializationMethod;
        this.usedInXml = usedInXml;
    }

    public final String getPackage() {
        return packageName;
    }

    public final String getName() {
        return name;
    }

    private List<String> getImplementationImports() {
        return implementationImports;
    }

    public XmsExtensions getExtensions() {
        return extensions;
    }

    private Function<String, String> getDefaultValueExpressionConverter() {
        return defaultValueExpressionConverter;
    }

    public final boolean isBoxedType() {
        // TODO (alzimmer): This should be a property on the ClassType
        return this.equals(VOID)
            || this.equals(BOOLEAN)
            || this.equals(BYTE)
            || this.equals(INTEGER)
            || this.equals(LONG)
            || this.equals(FLOAT)
            || this.equals(DOUBLE);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ClassType)) {
            return false;
        }
        ClassType that = (ClassType) other;
        return Objects.equals(this.name, that.name) && Objects.equals(this.packageName, that.packageName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageName, name);
    }

    public final IType asNullable() {
        return this;
    }

    public final boolean contains(IType type) {
        return this.equals(type);
    }

    public final String getFullName() {
        return fullName;
    }

    public final void addImportsTo(Set<String> imports, boolean includeImplementationImports) {
        if (!getPackage().equals("java.lang")) {
            imports.add(fullName);
        }

        if (this == UNIX_TIME_LONG) {
            imports.add(Instant.class.getName());
            imports.add(ZoneOffset.class.getName());
        }

        if (this == DATE_TIME) {
            imports.add(DateTimeFormatter.class.getName());
        }

        if (this == DATE_TIME_RFC_1123) {
            // May need OffsetDateTime when consuming DateTimeRfc1123 APIs as DateTimeRfc1123 APIs consume and return
            // OffsetDateTime.
            // If OffsetDateTime isn't needed, when running Spotless the unused import will be removed.
            imports.add(OffsetDateTime.class.getName());
        }

        if (this == URL) {
            imports.add(java.net.URL.class.getName());
            imports.add(java.net.MalformedURLException.class.getName());
        }

        if (includeImplementationImports && getImplementationImports() != null) {
            imports.addAll(getImplementationImports());
        }
    }

    public final String defaultValueExpression(String sourceExpression) {
        String result = sourceExpression;
        if (result != null) {
            if (getDefaultValueExpressionConverter() != null) {
                result = defaultValueExpressionConverter.apply(sourceExpression);
            } else {
                result = "new " + this + "()";
            }
        }
        return result;
    }

    @Override
    public String defaultValueExpression() {
        return "null";
    }

    public final IType getClientType() {
        IType clientType = this;
        if (this == DATE_TIME_RFC_1123) {
            clientType = DATE_TIME;
        } else if (this == UNIX_TIME_LONG) {
            clientType = DATE_TIME;
        } else if (this == BASE_64_URL) {
            clientType = ArrayType.BYTE_ARRAY;
        } else if (this == DURATION_LONG) {
            clientType = DURATION;
        } else if (this == DURATION_DOUBLE) {
            clientType = DURATION;
        }
        return clientType;
    }

    public String convertToClientType(String expression) {
        if (this == DATE_TIME_RFC_1123) {
            expression = expression + ".getDateTime()";
        } else if (this == UNIX_TIME_LONG) {
            expression = "OffsetDateTime.ofInstant(Instant.ofEpochSecond(" + expression + "), ZoneOffset.UTC)";
        } else if (this == BASE_64_URL) {
            expression = expression + ".decodedBytes()";
        } else if (this == URL) {
            expression = "new URL(" + expression + ")";
        } else if (this == DURATION_LONG) {
            expression = "Duration.ofSeconds(" + expression + ")";
        } else if (this == DURATION_DOUBLE) {
            expression = "Duration.ofNanos((long) (" + expression + " * 1000_000_000L))";
        }

        return expression;
    }

    public String convertFromClientType(String expression) {
        if (this == DATE_TIME_RFC_1123) {
            expression = "new DateTimeRfc1123(" + expression + ")";
        } else if (this == UNIX_TIME_LONG) {
            expression = expression + ".toEpochSecond()";
        } else if (this == BASE_64_URL) {
            expression = BASE_64_URL.getName() + ".encode(" + expression + ")";
        } else if (this == URL) {
            expression = expression + ".toString()";
        } else if (this == DURATION_LONG) {
            expression = expression + ".getSeconds()";
        } else if (this == DURATION_DOUBLE) {
            expression = "(double) " + expression + ".toNanos() / 1000_000_000L";
        }

        return expression;
    }

    public String validate(String expression) {
        if (packageName.startsWith(JavaSettings.getInstance().getPackage())) {
            return expression + ".validate()";
        } else {
            return null;
        }
    }

    public boolean isSwaggerType() {
        return isSwaggerType;
    }

    @Override
    public String jsonToken() {
        return jsonToken;
    }

    @Override
    public String jsonDeserializationMethod(String jsonReaderName) {
        if (jsonDeserializationMethod == null) {
            return null;
        }

        return jsonReaderName + "." + jsonDeserializationMethod;
    }

    @Override
    public String jsonSerializationMethodCall(String jsonWriterName, String fieldName, String valueGetter,
        boolean jsonMergePatch) {
        if (!isSwaggerType && CoreUtils.isNullOrEmpty(serializationMethodBase)) {
            return null;
        }

        String methodBase = isSwaggerType ? "writeJson" : serializationMethodBase;
        String value = serializationValueGetterModifier != null
            ? serializationValueGetterModifier.apply(valueGetter)
            : valueGetter;

        return fieldName == null
            ? jsonWriterName + "." + methodBase + "(" + value + ")"
            : jsonWriterName + "." + methodBase + "Field(\"" + fieldName + "\", " + value + ")";
    }

    @Override
    public String xmlDeserializationMethod(String xmlReaderName, String attributeName, String attributeNamespace,
        boolean namespaceIsConstant) {
        if (attributeName == null) {
            return xmlReaderName + "." + xmlElementDeserializationMethod;
        } else if (attributeNamespace == null) {
            return String.format(xmlAttributeDeserializationTemplate, xmlReaderName, "null",
                "\"" + attributeName + "\"");
        } else {
            String namespace = namespaceIsConstant ? attributeNamespace : "\"" + attributeNamespace + "\"";
            return String.format(xmlAttributeDeserializationTemplate, xmlReaderName, namespace,
                "\"" + attributeName + "\"");
        }
    }

    @Override
    public String xmlSerializationMethodCall(String xmlWriterName, String attributeOrElementName, String namespaceUri,
        String valueGetter, boolean isAttribute, boolean nameIsVariable, boolean namespaceIsConstant) {
        if (isSwaggerType) {
            if (isAttribute) {
                throw new RuntimeException("Swagger types cannot be written as attributes.");
            }

            return xmlWriterName + ".writeXml(" + valueGetter + ", \"" + attributeOrElementName + "\")";
        }

        String value = serializationValueGetterModifier != null
            ? serializationValueGetterModifier.apply(valueGetter)
            : valueGetter;
        return xmlSerializationCallHelper(xmlWriterName, serializationMethodBase, attributeOrElementName, namespaceUri,
            value, isAttribute, nameIsVariable, namespaceIsConstant);
    }

    @Override
    public boolean isUsedInXml() {
        return usedInXml;
    }

    public static class Builder {
        /*
         * Used to indicate if the class type is generated based on a Swagger definition and isn't a pre-defined,
         * handwritten type.
         */
        private final boolean isSwaggerType;

        private String packageName;
        private String name;
        private List<String> implementationImports;
        private XmsExtensions extensions;
        private Function<String, String> defaultValueExpressionConverter;
        private Function<String, String> serializationValueGetterModifier;
        private String jsonToken;
        private String jsonDeserializationMethod;
        private String serializationMethodBase;
        private String xmlAttributeDeserializationTemplate;
        private String xmlElementDeserializationMethod;
        private boolean usedInXml;

        public Builder() {
            this(true);
        }

        private Builder(boolean isSwaggerType) {
            this.isSwaggerType = isSwaggerType;
        }

        public Builder packageName(String packageName) {
            this.packageName = packageName;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder prototypeAsLong() {
            return this.knownClass(Long.class)
                .defaultValueExpressionConverter(defaultValueExpression -> defaultValueExpression + 'L')
                .jsonToken("JsonToken.NUMBER")
                .serializationMethodBase("writeNumber")
                .jsonDeserializationMethod("getNullable(JsonReader::getLong)")
                .xmlElementDeserializationMethod("getNullableElement(Long::parseLong)")
                .xmlAttributeDeserializationTemplate("%s.getNullableAttribute(%s, %s, Long::parseLong)");
        }

        public Builder prototypeAsDouble() {
            return this.knownClass(Double.class)
                .defaultValueExpressionConverter(defaultValueExpression -> java.lang.String
                    .valueOf(java.lang.Double.parseDouble(defaultValueExpression)) + 'D')
                .jsonToken("JsonToken.NUMBER")
                .serializationMethodBase("writeNumber")
                .jsonDeserializationMethod("getNullable(JsonReader::getDouble)")
                .xmlElementDeserializationMethod("getNullableElement(Double::parseDouble)")
                .xmlAttributeDeserializationTemplate("%s.getNullableAttribute(%s, %s, Double::parseDouble)");
        }

        public Builder knownClass(Class<?> clazz) {
            return packageName(clazz.getPackage().getName()).name(clazz.getSimpleName());
        }

        private Builder knownClass(String fullName) {
            int index = fullName.lastIndexOf(".");
            return packageName(fullName.substring(0, index)).name(fullName.substring(index + 1));
        }

        public Builder implementationImports(String... implementationImports) {
            this.implementationImports = Arrays.asList(implementationImports);
            return this;
        }

        public Builder extensions(XmsExtensions extensions) {
            this.extensions = extensions;
            return this;
        }

        public Builder defaultValueExpressionConverter(Function<String, String> defaultValueExpressionConverter) {
            this.defaultValueExpressionConverter = defaultValueExpressionConverter;
            return this;
        }

        public Builder jsonToken(String jsonToken) {
            this.jsonToken = jsonToken;
            return this;
        }

        public Builder serializationValueGetterModifier(Function<String, String> serializationValueGetterModifier) {
            this.serializationValueGetterModifier = serializationValueGetterModifier;
            return this;
        }

        public Builder jsonDeserializationMethod(String jsonDeserializationMethod) {
            this.jsonDeserializationMethod = jsonDeserializationMethod;
            return this;
        }

        public Builder serializationMethodBase(String serializationMethodBase) {
            this.serializationMethodBase = serializationMethodBase;
            return this;
        }

        public Builder xmlAttributeDeserializationTemplate(String xmlAttributeDeserializationTemplate) {
            this.xmlAttributeDeserializationTemplate = xmlAttributeDeserializationTemplate;
            return this;
        }

        public Builder xmlElementDeserializationMethod(String xmlElementDeserializationMethod) {
            this.xmlElementDeserializationMethod = xmlElementDeserializationMethod;
            return this;
        }

        public Builder usedInXml(boolean usedInXml) {
            this.usedInXml = usedInXml;
            return this;
        }

        public ClassType build() {
            // Deserialization of Swagger types needs to be handled differently as the named reader needs
            // to be passed to the deserialization method and the reader name cannot be determined here.
            String jsonDeserializationMethod = isSwaggerType ? null : this.jsonDeserializationMethod;
            String xmlAttributeDeserializationTemplate
                = isSwaggerType ? null : this.xmlAttributeDeserializationTemplate;
            String xmlElementDeserializationMethod = isSwaggerType ? null : this.xmlElementDeserializationMethod;

            return new ClassType(packageName, name, implementationImports, extensions, defaultValueExpressionConverter,
                isSwaggerType, jsonToken, serializationMethodBase, serializationValueGetterModifier,
                jsonDeserializationMethod, xmlAttributeDeserializationTemplate, xmlElementDeserializationMethod,
                usedInXml);
        }
    }

    static String xmlSerializationCallHelper(String writer, String method, String xmlName, String namespace,
        String value, boolean isAttribute, boolean nameIsVariable, boolean namespaceIsConstant) {
        String name = (xmlName == null) ? null : nameIsVariable ? xmlName : "\"" + xmlName + "\"";
        namespace = (namespace == null) ? null : namespaceIsConstant ? namespace : "\"" + namespace + "\"";

        if (isAttribute) {
            method = method + "Attribute";
            return (namespace == null)
                ? writer + "." + method + "(" + name + ", " + value + ")"
                : writer + "." + method + "(" + namespace + ", " + name + ", " + value + ")";
        }

        if (name == null) {
            return writer + "." + method + "(" + value + ")";
        } else {
            method = method + "Element";
            return (namespace == null)
                ? writer + "." + method + "(" + name + ", " + value + ")"
                : writer + "." + method + "(" + namespace + ", " + name + ", " + value + ")";
        }
    }
}
