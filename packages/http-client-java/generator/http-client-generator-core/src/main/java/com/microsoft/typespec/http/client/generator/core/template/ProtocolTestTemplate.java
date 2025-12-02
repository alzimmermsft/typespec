// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.typespec.http.client.generator.core.template;

import com.microsoft.typespec.http.client.generator.core.model.clientmodel.ProtocolExample;
import com.microsoft.typespec.http.client.generator.core.model.clientmodel.TestContext;
import com.microsoft.typespec.http.client.generator.core.model.javamodel.JavaFile;
import com.microsoft.typespec.http.client.generator.core.template.example.ProtocolExampleWriter;
import com.microsoft.typespec.http.client.generator.core.template.example.ProtocolTestWriter;

public class ProtocolTestTemplate implements IJavaTemplate<TestContext<ProtocolExample>, JavaFile> {

    private static final ProtocolTestTemplate INSTANCE = new ProtocolTestTemplate();

    protected ProtocolTestTemplate() {
    }

    public static ProtocolTestTemplate getInstance() {
        return INSTANCE;
    }

    @Override
    public void write(TestContext<ProtocolExample> testContext, JavaFile context) {

        final String className = testContext.getTestCase().getFilename() + "Tests";

        ProtocolTestWriter writer = new ProtocolTestWriter(testContext);
        ProtocolExampleWriter caseWriter = new ProtocolExampleWriter(testContext.getTestCase());

        context.declareImport(writer.getImports());
        context.declareImport(caseWriter.getImports());

        context.publicFinalClass(className + " extends " + testContext.getTestBaseClassName(), classBlock -> {
            classBlock.annotation("Test", "Disabled");  // "DoNotRecord(skipInPlayback = true)" not added
            classBlock.publicMethod("void test" + className + "()", methodBlock -> {
                caseWriter.writeClientMethodInvocation(methodBlock, true);
                caseWriter.writeAssertion(methodBlock);
            });
        });
    }
}
