// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.typespec.http.client.generator.mgmt.model.clientmodel.fluentmodel.update;

import com.microsoft.typespec.http.client.generator.core.model.clientmodel.ModelProperty;
import com.microsoft.typespec.http.client.generator.mgmt.model.clientmodel.fluentmodel.FluentInterfaceStage;

public class UpdateStage extends FluentInterfaceStage {

    public UpdateStage(String name) {
        super(name);
    }

    protected UpdateStage(String name, ModelProperty property) {
        super(name, property);
    }

    public String getDescription(String modelName) {
        return property == null
            ? "The stage of the " + modelName + " update."
            : "The stage of the " + modelName + " update allowing to specify " + property.getName() + ".";
    }

    public ModelProperty getModelProperty() {
        return this.property;
    }
}
