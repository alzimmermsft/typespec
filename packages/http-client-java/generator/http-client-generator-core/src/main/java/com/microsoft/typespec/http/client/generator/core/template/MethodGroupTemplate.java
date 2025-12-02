// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.typespec.http.client.generator.core.template;

import com.microsoft.typespec.http.client.generator.core.extension.plugin.JavaSettings;
import com.microsoft.typespec.http.client.generator.core.model.clientmodel.ClassType;
import com.microsoft.typespec.http.client.generator.core.model.clientmodel.IType;
import com.microsoft.typespec.http.client.generator.core.model.clientmodel.MethodGroupClient;
import com.microsoft.typespec.http.client.generator.core.model.clientmodel.ServiceClientProperty;
import com.microsoft.typespec.http.client.generator.core.model.javamodel.JavaBlock;
import com.microsoft.typespec.http.client.generator.core.model.javamodel.JavaFile;
import com.microsoft.typespec.http.client.generator.core.model.javamodel.JavaVisibility;
import com.microsoft.typespec.http.client.generator.core.util.ClientModelUtil;
import com.microsoft.typespec.http.client.generator.core.util.ModelNamer;
import com.microsoft.typespec.http.client.generator.core.util.TemplateUtil;
import io.clientcore.core.serialization.ObjectSerializer;
import io.clientcore.core.utils.CoreUtils;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Writes a MethodGroupClient to a JavaFile.
 */
public class MethodGroupTemplate implements IJavaTemplate<MethodGroupClient, JavaFile> {
    private static final MethodGroupTemplate INSTANCE = new MethodGroupTemplate();

    protected MethodGroupTemplate() {
    }

    public static MethodGroupTemplate getInstance() {
        return INSTANCE;
    }

    public final void write(MethodGroupClient methodGroupClient, JavaFile javaFile) {
        JavaSettings settings = JavaSettings.getInstance();
        if (settings.isUseClientLogger()) {
            ClassType.CLIENT_LOGGER.addImportsTo(javaFile::declareImport, false);
        }

        methodGroupClient.addImportsTo(javaFile::declareImport, true, settings);

        String serviceClientPackageName
            = ClientModelUtil.getServiceClientPackageName(methodGroupClient.getServiceClientName());
        javaFile.declareImport(serviceClientPackageName + "." + methodGroupClient.getServiceClientName(),
            InvocationTargetException.class.getName(), ObjectSerializer.class.getName(),
            ClassType.INSTRUMENTATION.getFullName(), ClassType.HTTP_PIPELINE.getFullName());

        List<String> interfaces
            = methodGroupClient.getSupportedInterfaces().stream().map(IType::toString).collect(Collectors.toList());
        interfaces.addAll(methodGroupClient.getImplementedInterfaces());
        String parentDeclaration = !interfaces.isEmpty() ? " implements " + String.join(", ", interfaces) : "";

        final JavaVisibility visibility = methodGroupClient.getPackage().equals(serviceClientPackageName)
            ? JavaVisibility.PackagePrivate
            : JavaVisibility.Public;

        javaFile.javadocComment(comment -> comment.description(
            String.format("An instance of this class provides access to all the operations defined in %1$s.",
                methodGroupClient.getInterfaceName())));
        javaFile.publicFinalClass(methodGroupClient.getClassName() + parentDeclaration, classBlock -> {
            final boolean hasProxy = methodGroupClient.getProxy() != null;

            if (hasProxy) {
                classBlock.javadocComment("The proxy service used to perform REST calls.");
                classBlock.privateFinalMemberVariable(methodGroupClient.getProxy().getName(), "service");
            }

            classBlock.javadocComment("The service client containing this operation class.");
            classBlock.privateFinalMemberVariable(methodGroupClient.getServiceClientName(), "client");

            boolean writeInstrumentation = !settings.isAzureV1();
            if (writeInstrumentation) {
                classBlock.javadocComment("The instance of instrumentation to report telemetry.");
                classBlock.privateFinalMemberVariable(ClassType.INSTRUMENTATION.getName(), "instrumentation");
            }

            classBlock.javadocComment(comment -> {
                comment.description("Initializes an instance of " + methodGroupClient.getClassName() + ".");
                comment.param("client", "the instance of the service client containing this operation class.");
            });
            classBlock.constructor(visibility, String.format("%1$s(%2$s client)", methodGroupClient.getClassName(),
                methodGroupClient.getServiceClientName()), constructor -> {
                    if (methodGroupClient.getProxy() != null) {
                        writeServiceProxyConstruction(constructor, methodGroupClient);
                    }
                    constructor.line("this.client = client;");

                    if (writeInstrumentation) {
                        constructor.line("this.instrumentation = client.getInstrumentation();");
                    }
                });

            if (!CoreUtils.isNullOrEmpty(methodGroupClient.getProperties())) {
                for (ServiceClientProperty property : methodGroupClient.getProperties()) {
                    classBlock.javadocComment(comment -> {
                        comment.description("Gets " + property.getDescription());
                        comment.methodReturns("the " + property.getName() + " value.");
                    });
                    String getter = new ModelNamer().modelPropertyGetterName(property);
                    classBlock.method(property.getMethodVisibility(), null, property.getType() + " " + getter + "()",
                        function -> function.methodReturn("client." + getter + "()"));
                }
            }

            if (hasProxy) {
                Templates.getProxyTemplate().write(methodGroupClient.getProxy(), classBlock);
            }

            TemplateUtil.writeClientMethodsAndHelpers(classBlock, methodGroupClient.getClientMethods());

            if (settings.isUseClientLogger()) {
                TemplateUtil.addClientLogger(classBlock, methodGroupClient.getClassName(), javaFile.getContents());
            }
        });
    }

    protected void writeServiceProxyConstruction(JavaBlock constructor, MethodGroupClient methodGroupClient) {
        ClassType proxyType = ClassType.REST_PROXY;
        if (JavaSettings.getInstance().isAzureV1()) {
            constructor.line(
                "this.service = %1$s.create(%2$s.class, client.getHttpPipeline(), client.getSerializerAdapter());",
                proxyType.getName(), methodGroupClient.getProxy().getName());
        } else {
            constructor.line("this.service = %1$s.create(%2$s.class, client.getHttpPipeline());", proxyType.getName(),
                methodGroupClient.getProxy().getName());
        }
    }
}
