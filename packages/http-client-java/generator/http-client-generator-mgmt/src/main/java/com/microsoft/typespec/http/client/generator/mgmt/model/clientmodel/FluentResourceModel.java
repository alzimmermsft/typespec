// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.typespec.http.client.generator.mgmt.model.clientmodel;

import com.microsoft.typespec.http.client.generator.core.extension.plugin.JavaSettings;
import com.microsoft.typespec.http.client.generator.core.model.clientmodel.ClassType;
import com.microsoft.typespec.http.client.generator.core.model.clientmodel.ClientModel;
import com.microsoft.typespec.http.client.generator.core.template.prototype.MethodTemplate;
import com.microsoft.typespec.http.client.generator.mgmt.model.arm.ModelCategory;
import com.microsoft.typespec.http.client.generator.mgmt.model.clientmodel.fluentmodel.ResourceImplementation;
import com.microsoft.typespec.http.client.generator.mgmt.model.clientmodel.fluentmodel.action.ResourceActions;
import com.microsoft.typespec.http.client.generator.mgmt.model.clientmodel.fluentmodel.create.ResourceCreate;
import com.microsoft.typespec.http.client.generator.mgmt.model.clientmodel.fluentmodel.get.ResourceRefresh;
import com.microsoft.typespec.http.client.generator.mgmt.model.clientmodel.fluentmodel.update.ResourceUpdate;
import com.microsoft.typespec.http.client.generator.mgmt.util.FluentUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Model for Azure resource instance.
 */
// Fluent resource instance. E.g. StorageAccount.
// Also include some simple wrapper class.
public class FluentResourceModel {

    // inner model. E.g. StorageAccountInner.
    private final ClientModel innerModel;

    // class type for interface and implementation
    private final ClassType interfaceType;
    private final ClassType implementationType;

    // resource properties
    private final Map<String, FluentModelProperty> propertiesMap = new LinkedHashMap<>();
    private final List<FluentModelProperty> properties = new ArrayList<>();

    // category of the resource
    private ModelCategory category = ModelCategory.IMMUTABLE;
    private ResourceCreate resourceCreate;
    private ResourceUpdate resourceUpdate;
    private ResourceRefresh resourceRefresh;
    private ResourceActions resourceActions;
    private final List<MethodTemplate> additionalMethods = new ArrayList<>();

    public FluentResourceModel(ClientModel innerModel, List<ClientModel> parentModels) {
        JavaSettings settings = JavaSettings.getInstance();

        this.innerModel = innerModel;
        // all parent models of the inner model (property of which need to be put to resource class as well)

        interfaceType = FluentUtils.resourceModelInterfaceClassType(innerModel.getName());
        implementationType
            = new ClassType.Builder().packageName(settings.getPackage(settings.getImplementationSubpackage()))
                .name(interfaceType.getName() + ModelNaming.MODEL_IMPL_SUFFIX)
                .build();

        List<List<FluentModelProperty>> propertiesFromTypeAndParents = new ArrayList<>();
        propertiesFromTypeAndParents.add(new ArrayList<>());
        this.innerModel.getAccessibleProperties().stream().map(FluentModelProperty::new).forEach(p -> {
            propertiesMap.putIfAbsent(p.getName(), p);
            propertiesFromTypeAndParents.get(propertiesFromTypeAndParents.size() - 1).add(p);
        });

        for (ClientModel parent : parentModels) {
            propertiesFromTypeAndParents.add(new ArrayList<>());

            parent.getAccessibleProperties().stream().map(FluentModelProperty::new).forEach(p -> {
                if (propertiesMap.putIfAbsent(p.getName(), p) == null) {
                    propertiesFromTypeAndParents.get(propertiesFromTypeAndParents.size() - 1).add(p);
                }
            });
        }

        Collections.reverse(propertiesFromTypeAndParents);
        for (List<FluentModelProperty> properties1 : propertiesFromTypeAndParents) {
            properties.addAll(properties1);
        }
    }

    public String getName() {
        return interfaceType.getName();
    }

    public ClientModel getInnerModel() {
        return innerModel;
    }

    public ClassType getInterfaceType() {
        return interfaceType;
    }

    public ClassType getImplementationType() {
        return implementationType;
    }

    public boolean hasProperty(String name) {
        return propertiesMap.containsKey(name);
    }

    public FluentModelProperty getProperty(String name) {
        return propertiesMap.get(name);
    }

    public Collection<FluentModelProperty> getProperties() {
        return properties;
    }

    public String getDescription() {
        return String.format("An immutable client-side representation of %s.", interfaceType.getName());
    }

    // method signature for inner model
    public String getInnerMethodSignature() {
        return getInnerModel().getName() + " " + FluentUtils.getGetterName(ModelNaming.METHOD_INNER_MODEL) + "()";
    }

    public ModelCategory getCategory() {
        return category;
    }

    public void setCategory(ModelCategory category) {
        this.category = category;
    }

    public ResourceImplementation getResourceImplementation() {
        return new ResourceImplementation(this);
    }

    public ResourceCreate getResourceCreate() {
        return resourceCreate;
    }

    public void setResourceCreate(ResourceCreate resourceCreate) {
        this.resourceCreate = resourceCreate;
    }

    public ResourceUpdate getResourceUpdate() {
        return resourceUpdate;
    }

    public void setResourceUpdate(ResourceUpdate resourceUpdate) {
        this.resourceUpdate = resourceUpdate;
    }

    public ResourceRefresh getResourceRefresh() {
        return resourceRefresh;
    }

    public void setResourceRefresh(ResourceRefresh resourceRefresh) {
        this.resourceRefresh = resourceRefresh;
    }

    public ResourceActions getResourceActions() {
        return resourceActions;
    }

    public void setResourceActions(ResourceActions resourceActions) {
        this.resourceActions = resourceActions;
    }

    public List<MethodTemplate> getAdditionalMethods() {
        return additionalMethods;
    }

    public void addImportsTo(Consumer<Collection<String>> importConsumer, boolean includeImplementationImports) {
        importConsumer.accept(List.of(this.getInnerModel().getFullName()));

        this.getProperties().forEach(p -> p.addImportsTo(importConsumer, includeImplementationImports));

        if (includeImplementationImports) {
            interfaceType.addImportsTo(importConsumer, false);
        }

        if (resourceCreate != null) {
            resourceCreate.addImportsTo(importConsumer, includeImplementationImports);
        }
        if (resourceUpdate != null) {
            resourceUpdate.addImportsTo(importConsumer, includeImplementationImports);
        }
        if (resourceRefresh != null) {
            resourceRefresh.addImportsTo(importConsumer, includeImplementationImports);
        }
        if (resourceActions != null) {
            resourceActions.addImportsTo(importConsumer, includeImplementationImports);
        }
        additionalMethods.forEach(m -> m.addImportsTo(importConsumer));
    }
}
