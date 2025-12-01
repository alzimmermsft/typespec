// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.typespec.http.client.generator.core.postprocessor.implementation;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.printer.configuration.ImportOrderingStrategy;
import com.github.javaparser.printer.configuration.imports.DefaultImportOrderingStrategy;
import com.microsoft.typespec.http.client.generator.core.extension.plugin.NewPlugin;
import com.microsoft.typespec.http.client.generator.core.util.Constants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;
import org.slf4j.Logger;
import org.w3c.dom.NodeList;

/**
 * Utility class that handles code formatting.
 */
public final class CodeFormatterUtil {

    /**
     * Formats the given files by removing unused imports and applying Eclipse code formatting.
     *
     * @param files The files to format.
     * @param plugin The plugin to use to write the formatted files.
     */
    public static void formatCode(Map<String, String> files, NewPlugin plugin, Logger logger) {
        formatCodeInternal(files, logger).forEach(entry -> plugin.writeFile(entry.getKey(), entry.getValue(), null));
    }

    /**
     * Formats the given files by removing unused imports and applying Eclipse code formatting.
     *
     * @param files The files to format. The entry is filename and content.
     * @return the files after format.
     * @throws RuntimeException If code formatting fails.
     */
    public static List<String> formatCode(Map<String, String> files) {
        return formatCodeInternal(files, null).map(Map.Entry::getValue).collect(Collectors.toList());
    }

    private static Stream<Map.Entry<String, String>> formatCodeInternal(Map<String, String> files, Logger logger) {
        Map<String, String> eclipseSettings = loadEclipseSettings();
        DefaultImportOrderingStrategy orderingStrategy = new DefaultImportOrderingStrategy();
        orderingStrategy.setSortImportsAlphabetically(true);

        return files.entrySet().stream().map(entry -> {
            try {
                String file = removeAndReorderImports(entry.getValue(), orderingStrategy);
                file = formatCode(file, entry.getKey(), ToolFactory.createCodeFormatter(eclipseSettings));
                return Map.entry(entry.getKey(), file);
            } catch (Exception e) {
                // print file content
                String errorMessage
                    = "Failed to format file: " + entry.getKey() + ". File content: \n" + entry.getValue();
                if (logger != null) {
                    logger.error(errorMessage);
                }

                throw new RuntimeException(errorMessage, e);
            }
        });
    }

    /**
     * Loads the Eclipse formatter settings from the XML file.
     *
     * @return The Eclipse formatter settings.
     * @throws RuntimeException If the formatter settings could not be loaded.
     */
    private static Map<String, String> loadEclipseSettings() {
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            org.w3c.dom.Document document = documentBuilder.parse(
                CodeFormatterUtil.class.getClassLoader().getResourceAsStream("eclipse-format-azure-sdk-for-java.xml"));

            NodeList formatterSettingXml = document.getElementsByTagName("setting");
            Map<String, String> formatterSettings = new HashMap<>();
            for (int i = 0; i < formatterSettingXml.getLength(); i++) {
                org.w3c.dom.Node node = formatterSettingXml.item(i);
                formatterSettings.put(node.getAttributes().getNamedItem("id").getNodeValue(),
                    node.getAttributes().getNamedItem("value").getNodeValue());
            }

            return formatterSettings;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Removes unused imports and reorders them using alphabetical ordering.
     * <p>
     * This helper method performs many tasks manually to maintain the original formatting of the file as much as
     * possible. Using {@link CompilationUnit} to manipulate the imports and then printing the entire file back
     * results in newline removal and trailing space removal which is just noise for us.
     *
     * @param file The Java file to reorder imports for.
     * @param orderingStrategy The import ordering strategy to use.
     * @return The Java file with reordered imports, or if the file has no imports the file as-is.
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static String removeAndReorderImports(String file, ImportOrderingStrategy orderingStrategy) {
        CompilationUnit compilationUnit = StaticJavaParser.parse(file);
        com.github.javaparser.ast.NodeList<ImportDeclaration> imports = compilationUnit.getImports();
        if (imports.isEmpty()) {
            // File has no imports, nothing to remove or reorder.
            return file;
        }

        UnusedImportVisitor unusedImportVisitor = new UnusedImportVisitor();
        unusedImportVisitor.visit(compilationUnit, null);
        imports.removeIf(importDecl -> {
            Set<String> unusedImports = unusedImportVisitor.getUnusedImports();
            return unusedImports.contains(importDecl.getNameAsString());
        });

        // Positions of the existing imports in the file.
        // Position uses 1-based indexing, so when we replace imports later we need to adjust this to 0-based indexing
        // for Java's List.
        int importStartLine = imports.stream().mapToInt(i -> i.getBegin().get().line).min().getAsInt();
        int importEndLine = imports.stream().mapToInt(i -> i.getEnd().get().line).max().getAsInt();

        // Using DefaultImportOrderingStrategy which returns a single NodeList after sorting.
        // If this strategy is changed, inspect the orderer used for how many NodeLists are returned.
        // For example, a made up SplitInstanceAndStaticImportOrderingStrategy could return two NodeLists,
        // one for sorted instance imports and one for sorted static imports.
        imports = orderingStrategy.sortImports(imports).get(0);

        List<String> lines = file.lines().collect(Collectors.toList());

        int lastLineReplaced = importStartLine - 1;
        for (ImportDeclaration importDeclaration : distinctImports(imports)) {
            lines.set(lastLineReplaced, importToString(importDeclaration));
            lastLineReplaced++;
        }

        // Remove any remaining old import lines if the new import list is shorter.
        if (importEndLine >= lastLineReplaced) {
            // Use importLineEnd as-is since Position is 1-based and subList's end index is exclusive.
            lines.subList(lastLineReplaced, importEndLine).clear();
        }

        return String.join("\n", lines);
    }

    private static List<ImportDeclaration> distinctImports(List<ImportDeclaration> imports) {
        Map<String, ImportDeclaration> importMap = new LinkedHashMap<>();
        for (ImportDeclaration importDecl : imports) {
            importMap.putIfAbsent(importDecl.toString(), importDecl);
        }
        return new ArrayList<>(importMap.values());
    }

    /**
     * Converts an {@link ImportDeclaration} to its string representation.
     * <p>
     * This is done as {@link ImportDeclaration#toString()} uses an internal printer which adds newline characters we
     * don't want. And instead of configuring our own printer just for this, we manually build the string.
     *
     * @param importDeclaration The import declaration.
     * @return The import statement representation of the import declaration.
     */
    private static String importToString(ImportDeclaration importDeclaration) {
        StringBuilder sb = new StringBuilder();
        sb.append("import ");
        if (importDeclaration.isStatic()) {
            sb.append("static ");
        }
        sb.append(importDeclaration.getNameAsString());
        if (importDeclaration.isAsterisk()) {
            sb.append(".*");
        }
        sb.append(";");
        return sb.toString();
    }

    private static String formatCode(String file, String fileName, CodeFormatter codeFormatter) throws Exception {
        IDocument doc = new Document(file);

        boolean isModuleInfo = IModule.MODULE_INFO_JAVA.equals(fileName);
        int kind = isModuleInfo ? CodeFormatter.K_MODULE_INFO : CodeFormatter.K_COMPILATION_UNIT;
        kind |= CodeFormatter.F_INCLUDE_COMMENTS;
        TextEdit edit = codeFormatter.format(kind, file, 0, file.length(), 0, Constants.NEW_LINE);
        edit.apply(doc);

        return doc.get();
    }
}
