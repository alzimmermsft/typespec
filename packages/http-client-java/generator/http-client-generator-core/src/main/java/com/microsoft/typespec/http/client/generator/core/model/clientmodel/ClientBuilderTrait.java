// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.typespec.http.client.generator.core.model.clientmodel;

import com.microsoft.typespec.http.client.generator.core.extension.plugin.JavaSettings;
import com.microsoft.typespec.http.client.generator.core.model.javamodel.JavaBlock;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Class representing a builder trait that adds additional context to the {@link ClientBuilder} client model.
 */
public class ClientBuilderTrait {

    public static final ClientBuilderTrait HTTP_TRAIT = createHttpTrait();

    public static final ClientBuilderTrait CONFIGURATION_TRAIT = createConfigurationTrait();

    public static final ClientBuilderTrait AZURE_KEY_CREDENTIAL_TRAIT = createAzureKeyCredentialTrait();

    public static final ClientBuilderTrait KEY_CREDENTIAL_TRAIT = createKeyCredentialTrait();

    public static final ClientBuilderTrait TOKEN_CREDENTIAL_TRAIT = createTokenCredentialTrait();

    public static final ClientBuilderTrait PROXY_TRAIT = createProxyTrait();

    private static ClientBuilderTrait endpointTrait;

    private String traitInterfaceName;
    private List<String> importPackages;
    private List<ClientBuilderTraitMethod> clientBuilderTraitMethods;

    /**
     * Returns the trait interface name.
     * 
     * @return the trait interface name.
     */
    public String getTraitInterfaceName() {
        return traitInterfaceName;
    }

    /**
     * Sets the trait interface name.
     * 
     * @param traitInterfaceName the trait interface name.
     */
    public void setTraitInterfaceName(String traitInterfaceName) {
        this.traitInterfaceName = traitInterfaceName;
    }

    /**
     * Returns the list of packages that needs to be imported for this trait.
     * 
     * @return The list of packages that needs to be imported for this trait.
     */
    public List<String> getImportPackages() {
        return importPackages;
    }

    /**
     * Sets the list of packages that needs to be imported for this trait.
     * 
     * @param importPackages the list of packages that needs to be imported for this trait.
     */
    public void setImportPackages(List<String> importPackages) {
        this.importPackages = importPackages;
    }

    public void setImportPackages(Set<String> importPackages) {
        this.importPackages = new ArrayList<>(importPackages);
    }

    /**
     * Returns the list of methods that this trait interface contains.
     * 
     * @return the list of methods that this trait interface contains.
     */
    public List<ClientBuilderTraitMethod> getTraitMethods() {
        return clientBuilderTraitMethods;
    }

    /**
     * Sets the list of methods that this trait interface contains.
     * 
     * @param clientBuilderTraitMethods the list of methods that this trait interface contains.
     */
    public void setTraitMethods(List<ClientBuilderTraitMethod> clientBuilderTraitMethods) {
        this.clientBuilderTraitMethods = clientBuilderTraitMethods;
    }

    private static ClientBuilderTrait createHttpTrait() {
        boolean isAzureV1 = JavaSettings.getInstance().isAzureV1();

        ClientBuilderTrait httpTrait = new ClientBuilderTrait();
        httpTrait.setTraitInterfaceName("HttpTrait");
        List<String> importPackages = new ArrayList<>();
        httpTrait.setImportPackages(importPackages);
        importPackages.add(ClassType.HTTP_TRAIT.getFullName());
        List<ClientBuilderTraitMethod> httpClientBuilderTraitMethods = new ArrayList<>();
        httpTrait.setTraitMethods(httpClientBuilderTraitMethods);

        // httpClient
        ServiceClientProperty httpClientProperty = new ServiceClientProperty(
            "The HTTP client used to send the request.", ClassType.HTTP_CLIENT, "httpClient", false, null);
        ClientBuilderTraitMethod httpClientMethod
            = createTraitMethod("httpClient", "httpClient", ClassType.HTTP_CLIENT, httpClientProperty);
        importPackages.add(ClassType.HTTP_CLIENT.getFullName());

        httpClientBuilderTraitMethods.add(httpClientMethod);

        if (isAzureV1) {
            // pipeline
            String pipelineMethodName = "pipeline";
            ServiceClientProperty pipelineProperty = new ServiceClientProperty(
                "The HTTP pipeline to send requests through.", ClassType.HTTP_PIPELINE, "pipeline", false,
                JavaSettings.getInstance().isFluent()
                    ? "new HttpPipelineBuilder().policies(new UserAgentPolicy(), new RetryPolicy()).build()"
                    : "createHttpPipeline()");
            importPackages.add(ClassType.LOG_LEVEL.getFullName());
            Consumer<JavaBlock> pipelineValidation = JavaSettings.getInstance().isUseClientLogger()
                ? function -> function.ifBlock("this.pipeline != null && pipeline == null", ifBlock -> ifBlock.line(
                    "LOGGER.atInfo().log(\"HttpPipeline is being set to 'null' when it was previously configured.\");"))
                : null;
            ClientBuilderTraitMethod pipelineMethod = createTraitMethod(pipelineMethodName, "pipeline",
                ClassType.HTTP_PIPELINE, pipelineProperty, pipelineValidation);
            importPackages.add(ClassType.HTTP_PIPELINE.getFullName());

            httpClientBuilderTraitMethods.add(pipelineMethod);

            // httpLogOptions
            ServiceClientProperty httpLogOptionsProperty
                = new ServiceClientProperty("The logging configuration for HTTP requests and responses.",
                    ClassType.HTTP_LOG_OPTIONS, "httpLogOptions", false, null);
            ClientBuilderTraitMethod httpLogOptionsMethod = createTraitMethod("httpLogOptions", "httpLogOptions",
                ClassType.HTTP_LOG_OPTIONS, httpLogOptionsProperty);
            importPackages.add(ClassType.HTTP_LOG_OPTIONS.getFullName());

            httpClientBuilderTraitMethods.add(httpLogOptionsMethod);

            // clientOptions
            ServiceClientProperty clientOptionsProperty = new ServiceClientProperty(
                "The client options such as application ID and custom headers to set on a request.",
                ClassType.CLIENT_OPTIONS, "clientOptions", false, null);
            ClientBuilderTraitMethod clientOptionsMethod
                = createTraitMethod("clientOptions", "clientOptions", ClassType.CLIENT_OPTIONS, clientOptionsProperty);
            importPackages.add(ClassType.CLIENT_OPTIONS.getFullName());

            httpClientBuilderTraitMethods.add(clientOptionsMethod);
        }

        // retryOptions
        ServiceClientProperty retryOptionsProperty
            = new ServiceClientProperty("The retry options to configure retry policy for failed requests.",
                ClassType.RETRY_OPTIONS, "retryOptions", false, null);
        String retryOptionsMethodName = isAzureV1 ? "retryOptions" : "httpRetryOptions";
        ClientBuilderTraitMethod retryOptionsMethod
            = createTraitMethod(retryOptionsMethodName, "retryOptions", ClassType.RETRY_OPTIONS, retryOptionsProperty);
        importPackages.add(ClassType.RETRY_OPTIONS.getFullName());
        httpClientBuilderTraitMethods.add(retryOptionsMethod);

        // addPolicy
        Consumer<JavaBlock> addPolicyValidation
            = function -> function.line("Objects.requireNonNull(customPolicy, \"'customPolicy' cannot be null.\");");
        String addPolicyMethodName = isAzureV1 ? "addPolicy" : "addHttpPipelinePolicy";
        ClientBuilderTraitMethod addPolicyMethod = createTraitMethod(addPolicyMethodName, "customPolicy",
            ClassType.HTTP_PIPELINE_POLICY, null, addPolicyValidation);
        importPackages.add(ClassType.HTTP_PIPELINE_POLICY.getFullName());
        httpClientBuilderTraitMethods.add(addPolicyMethod);

        if (!isAzureV1 || JavaSettings.getInstance().isAzureV2()) {
            // redirectOptions
            ServiceClientProperty redirectOptionsProperty
                = new ServiceClientProperty("The redirect options to configure redirect policy",
                    ClassType.REDIRECT_OPTIONS, "redirectOptions", false, null);
            ClientBuilderTraitMethod redirectOptionsMethod = createTraitMethod("httpRedirectOptions", "redirectOptions",
                ClassType.REDIRECT_OPTIONS, redirectOptionsProperty);
            importPackages.add(ClassType.REDIRECT_OPTIONS.getFullName());
            httpClientBuilderTraitMethods.add(redirectOptionsMethod);

            // instrumentation options
            ServiceClientProperty httpInstrumentationOptionsProperty
                = new ServiceClientProperty("The instrumentation configuration for HTTP requests and responses.",
                    ClassType.HTTP_LOG_OPTIONS, "httpInstrumentationOptions", false, null);
            importPackages.add(ClassType.HTTP_LOGGING_POLICY.getFullName());

            ClientBuilderTraitMethod httpInstrumentationOptionsMethod = createTraitMethod("httpInstrumentationOptions",
                "httpInstrumentationOptions", ClassType.HTTP_LOG_OPTIONS, httpInstrumentationOptionsProperty);
            importPackages.add(ClassType.HTTP_LOG_OPTIONS.getFullName());

            httpClientBuilderTraitMethods.add(httpInstrumentationOptionsMethod);
        }

        return httpTrait;
    }

    private static ClientBuilderTrait createConfigurationTrait() {
        ClientBuilderTrait configurationTrait = new ClientBuilderTrait();
        configurationTrait.setTraitInterfaceName("ConfigurationTrait");
        List<String> importPackages = new ArrayList<>();
        configurationTrait.setImportPackages(importPackages);
        importPackages.add(ClassType.CONFIGURATION_TRAIT.getFullName());

        List<ClientBuilderTraitMethod> configurationClientBuilderTraitMethods = new ArrayList<>();
        configurationTrait.setTraitMethods(configurationClientBuilderTraitMethods);

        ServiceClientProperty configurationProperty = new ServiceClientProperty(
            "The configuration store that is used during construction of the service client.", ClassType.CONFIGURATION,
            "configuration", false, null);

        ClientBuilderTraitMethod configurationMethod
            = createTraitMethod("configuration", "configuration", ClassType.CONFIGURATION, configurationProperty);
        importPackages.add(ClassType.CONFIGURATION.getFullName());

        configurationClientBuilderTraitMethods.add(configurationMethod);
        return configurationTrait;
    }

    private static ClientBuilderTrait createProxyTrait() {
        ClientBuilderTrait proxyTrait = new ClientBuilderTrait();
        proxyTrait.setTraitInterfaceName("ProxyTrait");
        List<String> importPackages = new ArrayList<>();
        proxyTrait.setImportPackages(importPackages);
        importPackages.add(ClassType.PROXY_TRAIT.getFullName());

        List<ClientBuilderTraitMethod> proxyClientBuilderTraitMethods = new ArrayList<>();
        proxyTrait.setTraitMethods(proxyClientBuilderTraitMethods);

        ServiceClientProperty proxyOptionsProperty
            = new ServiceClientProperty("The proxy options used during construction of the service client.",
                ClassType.PROXY_OPTIONS, "proxyOptions", false, null);

        ClientBuilderTraitMethod proxyMethod
            = createTraitMethod("proxyOptions", "proxyOptions", ClassType.PROXY_OPTIONS, proxyOptionsProperty);
        importPackages.add(ClassType.PROXY_OPTIONS.getFullName());

        proxyClientBuilderTraitMethods.add(proxyMethod);
        return proxyTrait;
    }

    public static ClientBuilderTrait getEndpointTrait(ServiceClientProperty property) {
        ClientBuilderTrait endpointTrait = ClientBuilderTrait.endpointTrait;
        if (endpointTrait == null) {
            endpointTrait = new ClientBuilderTrait();
            endpointTrait.setTraitInterfaceName(ClassType.ENDPOINT_TRAIT.getName());

            List<String> importPackages = new ArrayList<>();
            endpointTrait.setImportPackages(importPackages);
            importPackages.add(ClassType.ENDPOINT_TRAIT.getFullName());

            List<ClientBuilderTraitMethod> endpointClientBuilderTraitMethods = new ArrayList<>();
            endpointTrait.setTraitMethods(endpointClientBuilderTraitMethods);

            ServiceClientProperty endpointProperty = new ServiceClientProperty.Builder().name("endpoint")
                .type(ClassType.STRING)
                .description("The service endpoint")
                .readOnly(false)
                .required(property.isRequired())
                .defaultValueExpression(property.getDefaultValueExpression())
                .requestParameterName(property.getRequestParameterName())
                .build();

            ClientBuilderTraitMethod endpointMethod
                = createTraitMethod("endpoint", "endpoint", ClassType.STRING, endpointProperty);

            endpointClientBuilderTraitMethods.add(endpointMethod);
            ClientBuilderTrait.endpointTrait = endpointTrait;
        }
        return endpointTrait;
    }

    private static ClientBuilderTrait createTokenCredentialTrait() {
        ClientBuilderTrait tokenCredentialTrait = new ClientBuilderTrait();
        tokenCredentialTrait.setTraitInterfaceName(ClassType.TOKEN_CREDENTIAL_TRAIT.getName());
        Set<String> importPackages = new HashSet<>();
        ClassType.TOKEN_CREDENTIAL_TRAIT.addImportsTo(importPackages, false);
        ClassType.TOKEN_CREDENTIAL.addImportsTo(importPackages, false);
        ClassType.OAUTH_TOKEN_REQUEST_CONTEXT.addImportsTo(importPackages, false);
        tokenCredentialTrait.setImportPackages(importPackages);

        List<ClientBuilderTraitMethod> clientBuilderTraitMethods = new ArrayList<>();
        tokenCredentialTrait.setTraitMethods(clientBuilderTraitMethods);

        ServiceClientProperty property = new ServiceClientProperty("The TokenCredential used for authentication.",
            ClassType.TOKEN_CREDENTIAL, "tokenCredential", false, null);

        ClientBuilderTraitMethod clientMethod
            = createTraitMethod("credential", "tokenCredential", ClassType.TOKEN_CREDENTIAL, property);

        clientBuilderTraitMethods.add(clientMethod);
        return tokenCredentialTrait;
    }

    private static ClientBuilderTrait createAzureKeyCredentialTrait() {
        ClientBuilderTrait azureKeyCredentialTrait = new ClientBuilderTrait();
        azureKeyCredentialTrait.setTraitInterfaceName(ClassType.AZURE_KEY_CREDENTIAL_TRAIT.getName());
        List<String> importPackages = new ArrayList<>();
        azureKeyCredentialTrait.setImportPackages(importPackages);
        importPackages.add(ClassType.AZURE_KEY_CREDENTIAL_TRAIT.getFullName());

        List<ClientBuilderTraitMethod> clientBuilderTraitMethods = new ArrayList<>();
        azureKeyCredentialTrait.setTraitMethods(clientBuilderTraitMethods);

        ServiceClientProperty property = new ServiceClientProperty("The AzureKeyCredential used for authentication.",
            ClassType.AZURE_KEY_CREDENTIAL, "azureKeyCredential", false, null);

        ClientBuilderTraitMethod clientMethod
            = createTraitMethod("credential", "azureKeyCredential", ClassType.AZURE_KEY_CREDENTIAL, property);
        importPackages.add(ClassType.AZURE_KEY_CREDENTIAL.getFullName());

        clientBuilderTraitMethods.add(clientMethod);
        return azureKeyCredentialTrait;
    }

    private static ClientBuilderTrait createKeyCredentialTrait() {
        ClientBuilderTrait keyCredentialTrait = new ClientBuilderTrait();
        keyCredentialTrait.setTraitInterfaceName(ClassType.KEY_CREDENTIAL_TRAIT.getName());
        List<String> importPackages = new ArrayList<>();
        keyCredentialTrait.setImportPackages(importPackages);
        importPackages.add(ClassType.KEY_CREDENTIAL_TRAIT.getFullName());

        List<ClientBuilderTraitMethod> clientBuilderTraitMethods = new ArrayList<>();
        keyCredentialTrait.setTraitMethods(clientBuilderTraitMethods);

        ServiceClientProperty property = new ServiceClientProperty("The KeyCredential used for authentication.",
            ClassType.KEY_CREDENTIAL, "keyCredential", false, null);

        ClientBuilderTraitMethod clientMethod
            = createTraitMethod("credential", "keyCredential", ClassType.KEY_CREDENTIAL, property);
        importPackages.add(ClassType.KEY_CREDENTIAL.getFullName());

        clientBuilderTraitMethods.add(clientMethod);
        return keyCredentialTrait;
    }

    private static ClientBuilderTraitMethod createTraitMethod(String methodName, String methodParamName,
        ClassType paramType, ServiceClientProperty property) {
        return createTraitMethod(methodName, methodParamName, paramType, property, null);
    }

    private static ClientBuilderTraitMethod createTraitMethod(String methodName, String methodParamName,
        ClassType paramType, ServiceClientProperty property, Consumer<JavaBlock> validation) {
        Consumer<JavaBlock> methodImpl = function -> {
            if (validation != null) {
                validation.accept(function);
            }
            function.line("this." + methodParamName + " = " + methodParamName + ";");
            function.methodReturn("this");
        };
        ClientBuilderTraitMethod pipelineMethod = new ClientBuilderTraitMethod();
        pipelineMethod.setMethodName(methodName);
        pipelineMethod.setMethodParamName(methodParamName);
        pipelineMethod.setMethodParamType(paramType);
        pipelineMethod.setProperty(property);
        pipelineMethod.setDocumentation("{@inheritDoc}");
        pipelineMethod.setMethodImpl(methodImpl);
        return pipelineMethod;
    }
}
