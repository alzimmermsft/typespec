// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.typespec.http.client.generator.core.model.clientmodel;

import com.microsoft.typespec.http.client.generator.core.extension.plugin.JavaSettings;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Details that describe the dynamic proxy.
 */
public class Proxy {
    /**
     * Get the name of the REST API interface.
     */
    private final String name;
    /**
     * Get the name of the method group.
     */
    private final String clientTypeName;
    /**
     * Get the base URL that will be used for each REST API method.
     */
    private final String baseURL;
    /**
     * Get the methods of this REST API.
     */
    private final List<ProxyMethod> methods;

    /**
     * Create a new Proxy using the provided properties.
     * 
     * @param name The name of the REST API interface.
     * @param clientTypeName The name of the method group.
     * @param baseURL The base URL that will be used for each REST API method.
     * @param methods The methods of this REST API.
     */
    protected Proxy(String name, String clientTypeName, String baseURL, List<ProxyMethod> methods) {
        this.name = name;
        this.clientTypeName = clientTypeName;
        this.baseURL = baseURL;
        this.methods = methods;
    }

    public final String getName() {
        return name;
    }

    public final String getClientTypeName() {
        return clientTypeName;
    }

    public final String getBaseURL() {
        return baseURL;
    }

    public final List<ProxyMethod> getMethods() {
        return methods;
    }

    /**
     * Consume this property's imports.
     * 
     * @param importConsumer The import consumer.
     * @param includeImplementationImports Whether to include imports that are only necessary for method
     * implementations.
     */
    public void addImportsTo(Consumer<Collection<String>> importConsumer, boolean includeImplementationImports,
        JavaSettings settings) {
        if (includeImplementationImports) {
            importConsumer.accept(List.of(Annotation.HOST.getFullName(), Annotation.SERVICE_INTERFACE.getFullName()));
        }

        for (ProxyMethod method : getMethods()) {
            method.addImportsTo(importConsumer, includeImplementationImports, settings);
        }
    }

    public static class Builder {
        protected String name;
        protected String clientTypeName;
        protected String baseURL;
        protected List<ProxyMethod> methods;

        /**
         * Sets the name of the REST API interface.
         * 
         * @param name the name of the REST API interface
         * @return the Builder itself
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the name of the method group.
         * 
         * @param clientTypeName the name of the method group
         * @return the Builder itself
         */
        public Builder clientTypeName(String clientTypeName) {
            this.clientTypeName = clientTypeName;
            return this;
        }

        /**
         * Sets the base URL that will be used for each REST API method.
         * 
         * @param baseURL the base URL that will be used for each REST API method
         * @return the Builder itself
         */
        public Builder baseURL(String baseURL) {
            this.baseURL = baseURL;
            return this;
        }

        /**
         * Sets the methods of this REST API.
         * 
         * @param methods the methods of this REST API
         * @return the Builder itself
         */
        public Builder methods(List<ProxyMethod> methods) {
            this.methods = methods;
            return this;
        }

        public Proxy build() {
            return new Proxy(name, clientTypeName, baseURL, methods);
        }
    }
}
