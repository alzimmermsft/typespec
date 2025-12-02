// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.typespec.http.client.generator.core.model.clientmodel;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * A return value from a ClientMethod.
 */
public class ReturnValue {
    /**
     * The description of the return value.
     */
    private final String description;
    /**
     * The type of the return value.
     */
    private final IType type;

    /**
     * Create a new ReturnValue object from the provided properties.
     * 
     * @param description The description of the return value.
     * @param type The type of the return value.
     */
    public ReturnValue(String description, IType type) {
        this.description = description;
        this.type = type;
    }

    public final String getDescription() {
        return description;
    }

    public final IType getType() {
        return type;
    }

    /**
     * Consume this return value's imports.
     * 
     * @param importConsumer The import consumer.
     * @param includeImplementationImports Whether to include imports that are only necessary for method
     * implementations.
     */
    public final void addImportsTo(Consumer<Collection<String>> importConsumer, boolean includeImplementationImports) {
        getType().addImportsTo(importConsumer, includeImplementationImports);
    }
}
