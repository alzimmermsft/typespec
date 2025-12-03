// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.typespec.http.client.generator.core.model.javamodel;

import com.microsoft.typespec.http.client.generator.core.extension.plugin.JavaSettings;
import java.io.File;
import java.nio.file.Paths;

public final class JavaFileFactory {
    private final JavaSettings settings;

    public JavaFileFactory(JavaSettings settings) {
        this.settings = settings;
    }

    public JavaFile createEmptySourceFile(String packageName, String fileNameWithoutExtension) {
        return createFile(packageName, fileNameWithoutExtension, "main", false);
    }

    public JavaFile createSourceFile(String packageName, String fileNameWithoutExtension) {
        return createFile(packageName, fileNameWithoutExtension, "main", true);
    }

    public JavaFile createSampleFile(String packageName, String fileNameWithoutExtension) {
        return createFile(packageName, fileNameWithoutExtension, "samples", true);
    }

    public JavaFile createTestFile(String packageName, String fileNameWithoutExtension) {
        return createFile(packageName, fileNameWithoutExtension, "test", true);
    }

    private JavaFile createFile(String packageName, String fileNameWithoutExtension, String kind,
        boolean addHeaderAndPackage) {
        String folderPath = Paths.get("src", kind, "java", packageName.replace('.', File.separatorChar)).toString();
        String filePath = Paths.get(folderPath)
            .resolve(fileNameWithoutExtension + ".java")
            .toString()
            .replace('\\', '/')
            .replace("//", "/");

        JavaFile javaFile = new JavaFile(filePath);
        if (addHeaderAndPackage) {
            javaFile.setFileHeader(JavaSettings.getInstance().getFileHeaderText());
            javaFile.declarePackage(packageName);
        }

        return javaFile;
    }
}
