// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.typespec.http.client.generator.core.model.clientmodel;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * The constructor in a ServiceClient.
 */
public class Constructor {
    /**
     * The parameters of this constructor.
     */
    private final List<ClientMethodParameter> parameters;

    public Constructor(List<ClientMethodParameter> parameters) {
        this.parameters = parameters;
    }

    public final List<ClientMethodParameter> getParameters() {
        return parameters;
    }

    public final void addImportsTo(Consumer<Collection<String>> importConsumer, boolean includeImplementationImports) {
        for (ClientMethodParameter parameter : getParameters()) {
            parameter.addImportsTo(importConsumer, includeImplementationImports);
        }
    }
}
