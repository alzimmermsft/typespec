// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.typespec.http.client.generator.mgmt.model.clientmodel.fluentmodel.update;

import com.microsoft.typespec.http.client.generator.core.model.clientmodel.ClientMethodParameter;

public class UpdateStageMisc extends UpdateStage {

    public UpdateStageMisc(String name, ClientMethodParameter parameter) {
        super(name);
        this.parameter = parameter;
    }

    public String getDescription(String modelName) {
        return "The stage of the " + modelName + " update allowing to specify " + parameter.getName() + ".";
    }

    public ClientMethodParameter getMethodParameter() {
        return parameter;
    }
}
