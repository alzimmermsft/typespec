/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Harry Terkelsen (het@google.com) - Bug 449262 - Allow the use of third-party Java formatters
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IFile;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IConfigurationElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IExtension;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IExtensionPoint;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Platform;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.formatter.CodeFormatter;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.util.ClassFormatException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.util.IClassFileReader;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.util.SuffixConstants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.util.Util;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.JavaElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.JavaModelManager;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.PackageFragment;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.ClassFileReader;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.formatter.DefaultCodeFormatter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Factory for creating various compiler tools, such as scanners, parsers and compilers.
 * <p>
 * This class provides static methods only.
 * </p>
 *
 * @since 2.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ToolFactory {

    /**
     * This mode is used for formatting new code when some formatter options should not be used.
     * In particular, options that preserve the indentation of comments are not used.
     * In the future, newly added options may be ignored as well.
     * <p>Clients that are formatting new code are recommended to use this mode.
     * </p>
     *
     * @see DefaultCodeFormatterConstants#FORMATTER_NEVER_INDENT_BLOCK_COMMENTS_ON_FIRST_COLUMN
     * @see DefaultCodeFormatterConstants#FORMATTER_NEVER_INDENT_LINE_COMMENTS_ON_FIRST_COLUMN
     * @see #createCodeFormatter(Map, int)
     * @since 3.3
     */
    // Supposed to be a non-compile time constant
    public static final int M_FORMAT_NEW = 0;

    /**
     * Create an instance of the built-in code formatter.
     * <p>The given options should at least provide the source level ({@link JavaCore#COMPILER_SOURCE}),
     * the compiler compliance level ({@link JavaCore#COMPILER_COMPLIANCE}) and the target platform
     * ({@link JavaCore#COMPILER_CODEGEN_TARGET_PLATFORM}).
     * Without these options, it is not possible for the code formatter to know what kind of source it needs to format.
     * </p><p>
     * Note this is equivalent to <code>createCodeFormatter(options, M_FORMAT_NEW)</code>. Thus some code formatter
     * options
     * may be ignored. See @{link {@link #M_FORMAT_NEW} for more details.
     * </p>
     * 
     * @param options - the options map to use for formatting with the default code formatter. Recognized options
     * are documented on <code>JavaCore#getDefaultOptions()</code>. If set to <code>null</code>, then use
     * the current settings from <code>JavaCore#getOptions</code>.
     * @return an instance of the built-in code formatter
     * @see CodeFormatter
     * @see JavaCore#getOptions()
     * @since 3.0
     */
    public static CodeFormatter createCodeFormatter(Map options) {
        return createCodeFormatter(options, M_FORMAT_NEW);
    }

    /**
     * Creates an instance of a code formatter. A code formatter implementation can be contributed via the extension
     * point "com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.javaFormatter".
     * The formatter id specified in the
     * "com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.javaFormatter" is
     * instantiated. If unable to find a registered extension, the factory will
     * default to using the default code formatter.
     * <p>The given options should at least provide the source level ({@link JavaCore#COMPILER_SOURCE}),
     * the compiler compliance level ({@link JavaCore#COMPILER_COMPLIANCE}) and the target platform
     * ({@link JavaCore#COMPILER_CODEGEN_TARGET_PLATFORM}).
     * Without these options, it is not possible for the code formatter to know what kind of source it needs to format.
     * </p>
     * <p>The given mode determines what options should be enabled when formatting the code. It can have the following
     * values: {@link #M_FORMAT_NEW}, {@link #M_FORMAT_EXISTING}, but other values may be added in the future.
     * </p>
     *
     * @param options the options map to use for formatting with the default code formatter. Recognized options
     * are documented on <code>JavaCore#getDefaultOptions()</code>. If set to <code>null</code>, then use
     * the current settings from <code>JavaCore#getOptions</code>.
     * @param mode the given mode to modify the given options.
     *
     * @return an instance of the built-in code formatter
     * @see CodeFormatter
     * @see JavaCore#getOptions()
     * @since 3.3
     */
    public static CodeFormatter createCodeFormatter(Map options, int mode) {
        if (options == null)
            options = JavaCore.getOptions();
        Map currentOptions = new HashMap(options);
        if (mode == M_FORMAT_NEW) {
            // disable the option for not formatting comments starting on first column
            currentOptions.put(
                DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_LINE_COMMENT_STARTING_ON_FIRST_COLUMN,
                DefaultCodeFormatterConstants.TRUE);
            // disable the option for not indenting comments starting on first column
            currentOptions.put(DefaultCodeFormatterConstants.FORMATTER_NEVER_INDENT_BLOCK_COMMENTS_ON_FIRST_COLUMN,
                DefaultCodeFormatterConstants.FALSE);
            currentOptions.put(DefaultCodeFormatterConstants.FORMATTER_NEVER_INDENT_LINE_COMMENTS_ON_FIRST_COLUMN,
                DefaultCodeFormatterConstants.FALSE);
        }
        String formatterId = (String) options.get(JavaCore.JAVA_FORMATTER);
        if (formatterId != null) {
            IExtensionPoint extension = Platform.getExtensionRegistry()
                .getExtensionPoint(JavaCore.PLUGIN_ID, JavaCore.JAVA_FORMATTER_EXTENSION_POINT_ID);
            if (extension != null) {
                IExtension[] extensions = extension.getExtensions();
                for (IExtension ext : extensions) {
                    IConfigurationElement[] configElements = ext.getConfigurationElements();
                    for (IConfigurationElement configElement : configElements) {
                        String initializerID = configElement.getAttribute("id"); //$NON-NLS-1$
                        if (initializerID != null && initializerID.equals(formatterId)) {
                            try {
                                Object execExt = configElement.createExecutableExtension("class"); //$NON-NLS-1$
                                if (execExt instanceof CodeFormatter) {
                                    CodeFormatter formatter = (CodeFormatter) execExt;
                                    formatter.setOptions(currentOptions);
                                    return formatter;
                                }
                            } catch (CoreException e) {
                                com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.Util
                                    .log(e.getStatus());
                                break;
                            }
                        }
                    }
                }
            }
            com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.Util
                .log(IStatus.WARNING,
                    "Unable to instantiate formatter extension '" + formatterId + "', returning built-in formatter."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return new DefaultCodeFormatter(currentOptions);
    }

    /**
     * Create a classfile reader onto a classfile Java element.
     * Create a default classfile reader, able to expose the internal representation of a given classfile
     * according to the decoding flag used to initialize the reader.
     * Answer null if the file named fileName doesn't represent a valid .class file.
     *
     * The decoding flags are described in IClassFileReader.
     *
     * @param classfile the classfile element to introspect
     * @param decodingFlag the flag used to decode the class file reader.
     * @return a default classfile reader
     *
     * @see IClassFileReader
     */
    public static IClassFileReader createDefaultClassFileReader(IClassFile classfile, int decodingFlag) {

        IPackageFragmentRoot root = (IPackageFragmentRoot) classfile.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
        if (root != null) {
            try {
                if (root instanceof JarPackageFragmentRoot) {
                    String archiveName;
                    ZipFile jar = null;
                    try {
                        jar = ((JarPackageFragmentRoot) root).getJar();
                        archiveName = jar.getName();
                    } finally {
                        JavaModelManager.getJavaModelManager().closeZipFile(jar);
                    }
                    PackageFragment packageFragment = (PackageFragment) classfile.getParent();
                    String classFileName = classfile.getElementName();
                    String entryName
                        = com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.Util
                            .concatWith(packageFragment.names, classFileName, '/');
                    return createDefaultClassFileReader(archiveName, entryName, decodingFlag);
                } else {
                    try (InputStream in = ((IFile) ((JavaElement) classfile).resource()).getContents()) {
                        return createDefaultClassFileReader(in, decodingFlag);
                    } catch (IOException e) {
                        // ignore
                    }
                }
            } catch (CoreException e) {
                // unable to read
            }
        }
        return null;
    }

    /**
     * Create a default classfile reader, able to expose the internal representation of a given classfile
     * according to the decoding flag used to initialize the reader.
     * Answer null if the input stream contents cannot be retrieved
     *
     * The decoding flags are described in IClassFileReader.
     *
     * @param stream the given input stream to read
     * @param decodingFlag the flag used to decode the class file reader.
     * @return a default classfile reader
     *
     * @see IClassFileReader
     * @since 3.2
     */
    public static IClassFileReader createDefaultClassFileReader(InputStream stream, int decodingFlag) {
        try {
            return new ClassFileReader(Util.getInputStreamAsByteArray(stream), decodingFlag);
        } catch (ClassFormatException | IOException e) {
            return null;
        }
    }

    /**
     * Create a default classfile reader, able to expose the internal representation of a given classfile
     * according to the decoding flag used to initialize the reader.
     * Answer null if the file named zipFileName doesn't represent a valid zip file or if the zipEntryName
     * is not a valid entry name for the specified zip file or if the bytes don't represent a valid
     * .class file according to the JVM specifications.
     *
     * The decoding flags are described in IClassFileReader.
     *
     * @param zipFileName the name of the zip file
     * @param zipEntryName the name of the entry in the zip file to be read
     * @param decodingFlag the flag used to decode the class file reader.
     * @return a default classfile reader
     * @see IClassFileReader
     */
    public static IClassFileReader createDefaultClassFileReader(String zipFileName, String zipEntryName,
        int decodingFlag) {
        if (JavaModelManager.ZIP_ACCESS_VERBOSE) {
            JavaModelManager.trace("(" + Thread.currentThread() //$NON-NLS-1$
                + ") [ToolFactory.createDefaultClassFileReader()] Creating ZipFile on " + zipFileName);  //$NON-NLS-1$
        }
        try (ZipFile zipFile = new ZipFile(zipFileName)) {
            ZipEntry zipEntry = zipFile.getEntry(zipEntryName);
            if (zipEntry == null) {
                return null;
            }
            if (!zipEntryName.toLowerCase().endsWith(SuffixConstants.SUFFIX_STRING_class)) {
                return null;
            }
            byte classFileBytes[] = Util.getZipEntryByteContent(zipEntry, zipFile);
            return new ClassFileReader(classFileBytes, decodingFlag);
        } catch (ClassFormatException | IOException e) {
            return null;
        }
    }

}
