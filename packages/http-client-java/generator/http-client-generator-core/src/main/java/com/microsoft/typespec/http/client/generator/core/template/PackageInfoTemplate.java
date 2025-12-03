// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.typespec.http.client.generator.core.template;

import com.microsoft.typespec.http.client.generator.core.extension.plugin.JavaSettings;
import com.microsoft.typespec.http.client.generator.core.model.clientmodel.PackageInfo;
import com.microsoft.typespec.http.client.generator.core.model.javamodel.JavaFile;
import com.microsoft.typespec.http.client.generator.core.partialupdate.util.PartialUpdateHandler;
import java.util.regex.Pattern;

/**
 * Writes a PackageInfo to a JavaFile.
 */
public class PackageInfoTemplate implements IJavaTemplate<PackageInfo, JavaFile> {
    private static final PackageInfoTemplate INSTANCE = new PackageInfoTemplate();

    private static final Pattern NEW_LINE = Pattern.compile(Pattern.quote("\r\n"));

    private PackageInfoTemplate() {
    }

    public static PackageInfoTemplate getInstance() {
        return INSTANCE;
    }

    public final void write(PackageInfo packageInfo, JavaFile javaFile) {
        // package-info.java gets handled slightly differently than standard JavaFile's as the package declaration
        // itself will have Javadocs. Since this is a one-off, instead of making a proper system for this, for now
        // just treat the package declaration as a normal line.
        // TODO (alzimmer): In the future use a proper system to track the package declarations Javadoc.
        JavaSettings settings = JavaSettings.getInstance();
        javaFile.setFileHeader(settings.getFileHeaderText());

        javaFile.javadocComment(comment -> {
            if (settings.isHandlePartialUpdate()) {
                comment.line(PartialUpdateHandler.START_GENERATED_JAVA_DOC);
            }

            for (String desc : NEW_LINE.split(packageInfo.getDescription(), -1)) {
                comment.description(desc);
            }

            if (settings.isHandlePartialUpdate()) {
                comment.line(PartialUpdateHandler.END_GENERATED_JAVA_DOC);
            }
        });

        javaFile.line("package " + packageInfo.getPackage() + ";");
    }
}
