/*******************************************************************************
 * Copyright (c) 2000, 2025 IBM Corporation and others.
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
 *     IBM Corporation - added the following constants:
 *                                 COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE
 *                                 COMPILER_PB_STATIC_ACCESS_RECEIVER
 *                                 COMPILER_TASK_TAGS
 *                                 CORE_CIRCULAR_CLASSPATH
 *                                 CORE_INCOMPLETE_CLASSPATH
 *     IBM Corporation - added run(IWorkspaceRunnable, IProgressMonitor)
 *     IBM Corporation - added exclusion patterns to source classpath entries
 *     IBM Corporation - added specific output location to source classpath entries
 *     IBM Corporation - added the following constants:
 *                                 CORE_JAVA_BUILD_CLEAN_OUTPUT_FOLDER
 *                                 CORE_JAVA_BUILD_RECREATE_MODIFIED_CLASS_FILES_IN_OUTPUT_FOLDER
 *                                 CLEAN
 *     IBM Corporation - added getClasspathContainerInitializer(String)
 *     IBM Corporation - added the following constants:
 *                                 CODEASSIST_ARGUMENT_PREFIXES
 *                                 CODEASSIST_ARGUMENT_SUFFIXES
 *                                 CODEASSIST_FIELD_PREFIXES
 *                                 CODEASSIST_FIELD_SUFFIXES
 *                                 CODEASSIST_LOCAL_PREFIXES
 *                                 CODEASSIST_LOCAL_SUFFIXES
 *                                 CODEASSIST_STATIC_FIELD_PREFIXES
 *                                 CODEASSIST_STATIC_FIELD_SUFFIXES
 *                                 COMPILER_PB_CHAR_ARRAY_IN_STRING_CONCATENATION
 *     IBM Corporation - added the following constants:
 *                                 COMPILER_PB_LOCAL_VARIABLE_HIDING
 *                                 COMPILER_PB_SPECIAL_PARAMETER_HIDING_FIELD
 *                                 COMPILER_PB_FIELD_HIDING
 *                                 COMPILER_PB_POSSIBLE_ACCIDENTAL_BOOLEAN_ASSIGNMENT
 *                                 CORE_INCOMPATIBLE_JDK_LEVEL
 *                                 VERSION_1_5
 *                                 COMPILER_PB_EMPTY_STATEMENT
 *     IBM Corporation - added the following constants:
 *                                 COMPILER_PB_INDIRECT_STATIC_ACCESS
 *                                 COMPILER_PB_BOOLEAN_METHOD_THROWING_EXCEPTION
 *                                 COMPILER_PB_UNNECESSARY_CAST
 *     IBM Corporation - added the following constants:
 *                                 COMPILER_PB_INVALID_JAVADOC
 *                                 COMPILER_PB_INVALID_JAVADOC_TAGS
 *                                 COMPILER_PB_INVALID_JAVADOC_TAGS_VISIBILITY
 *                                 COMPILER_PB_MISSING_JAVADOC_TAGS
 *                                 COMPILER_PB_MISSING_JAVADOC_TAGS_VISIBILITY
 *                                 COMPILER_PB_MISSING_JAVADOC_TAGS_OVERRIDING
 *                                 COMPILER_PB_MISSING_JAVADOC_COMMENTS
 *                                 COMPILER_PB_MISSING_JAVADOC_COMMENTS_VISIBILITY
 *                                 COMPILER_PB_MISSING_JAVADOC_COMMENTS_OVERRIDING
 *                                 COMPILER_PB_DEPRECATION_WHEN_OVERRIDING_DEPRECATED_METHOD
 *                                 COMPILER_PB_UNUSED_DECLARED_THROWN_EXCEPTION_WHEN_OVERRIDING
 *     IBM Corporation - added the following constants:
 *                                 TIMEOUT_FOR_PARAMETER_NAME_FROM_ATTACHED_JAVADOC
 *     IBM Corporation - added the following constants:
 *                                 COMPILER_PB_FALLTHROUGH_CASE
 *                                 COMPILER_PB_PARAMETER_ASSIGNMENT
 *                                 COMPILER_PB_NULL_REFERENCE
 *     IBM Corporation - added the following constants:
 *                                 CODEASSIST_DEPRECATION_CHECK
 *     IBM Corporation - added the following constants:
 *                                 COMPILER_PB_POTENTIAL_NULL_REFERENCE
 *                                 COMPILER_PB_REDUNDANT_NULL_CHECK
 *     IBM Corporation - added the following constants:
 *                                 COMPILER_PB_UNUSED_PARAMETER_INCLUDE_DOC_COMMENT_REFERENCE
 *     IBM Corporation - added the following constants:
 *                                 COMPILER_PB_UNUSED_DECLARED_THROWN_EXCEPTION_INCLUDE_DOC_COMMENT_REFERENCE
 *     IBM Corporation - added the following constants:
 *                                 COMPILER_PB_MISSING_JAVADOC_TAG_DESCRIPTION
 *								   COMPILER_PB_MISSING_JAVADOC_TAG_DESCRIPTION_NO_TAG
 *								   COMPILER_PB_MISSING_JAVADOC_TAG_DESCRIPTION_RETURN_TAG
 *								   COMPILER_PB_MISSING_JAVADOC_TAG_DESCRIPTION_ALL_TAGS
 *     IBM Corporation - added the following constants:
 *                                 COMPILER_PB_REDUNDANT_SUPERINTERFACE
 *     IBM Corporation - added the following constant:
 *                                 COMPILER_PB_UNUSED_DECLARED_THROWN_EXCEPTION_EXEMPT_EXCEPTION_AND_THROWABLE
 *     IBM Corporation - added getOptionForConfigurableSeverity(int)
 *     Benjamin Muskalla - added COMPILER_PB_MISSING_SYNCHRONIZED_ON_INHERITED_METHOD
 *     Stephan Herrmann  - added COMPILER_PB_UNUSED_OBJECT_ALLOCATION
 *     Stephan Herrmann  - added COMPILER_PB_SUPPRESS_OPTIONAL_ERRORS
 *     Stephan Herrmann  - added the following constants:
 *     								COMPILER_PB_UNCLOSED_CLOSEABLE,
 *     								COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE
 *     								COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE
 *     								COMPILER_ANNOTATION_NULL_ANALYSIS
 *     								COMPILER_NULLABLE_ANNOTATION_NAME
 *     								COMPILER_NONNULL_ANNOTATION_NAME
 *     								COMPILER_PB_NULL_SPECIFICATION_VIOLATION
 *     								COMPILER_PB_POTENTIAL_NULL_SPECIFICATION_VIOLATION
 *     								COMPILER_PB_NULL_SPECIFICATION_INSUFFICIENT_INFO
 *									COMPILER_PB_MISSING_ENUM_CASE_DESPITE_DEFAULT
 *									COMPILER_PB_SWITCH_MISSING_DEFAULT_CASE
 *									COMPILER_INHERIT_NULL_ANNOTATIONS
 *									COMPILER_PB_NONNULL_PARAMETER_ANNOTATION_DROPPED
 *									COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS
 *									COMPILER_PB_UNLIKELY_COLLECTION_METHOD_ARGUMENT_TYPE
 *									COMPILER_PB_UNLIKELY_EQUALS_ARGUMENT_TYPE
 *     Jesper S Moller   - Contributions for bug 381345 : [1.8] Take care of the Java 8 major version
 *                       - added the following constants:
 *									COMPILER_CODEGEN_METHOD_PARAMETERS_ATTR
 *     Harry Terkelsen (het@google.com) - Bug 449262 - Allow the use of third-party Java formatters
 *     Gábor Kövesdán - Contribution for Bug 350000 - [content assist] Include non-prefix matches in auto-complete suggestions
 *     Karsten Thoms - Bug 532505 - Reduce memory footprint of ClasspathAccessRule
 *
 *******************************************************************************/

package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IFile;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IFolder;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IWorkspaceRoot;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IConfigurationElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IExtension;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IExtensionPoint;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Platform;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Plugin;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.BufferFactoryWrapper;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.BufferManager;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.ClasspathAttribute;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.ClasspathEntry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.JavaCorePreferenceInitializer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.JavaModel;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.JavaModelManager;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.JavaProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.Region;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.MementoTokenizer;

import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.JavaModelManager.trace;

/**
 * The plug-in runtime class for the Java model plug-in containing the core
 * (UI-free) support for Java projects.
 * <p>
 * Like all plug-in runtime classes (subclasses of <code>Plugin</code>), this
 * class is automatically instantiated by the platform when the plug-in gets
 * activated. Clients must not attempt to instantiate plug-in runtime classes
 * directly.
 * </p>
 * <p>
 * The single instance of this class can be accessed from any plug-in declaring
 * the Java model plug-in as a prerequisite via
 * <code>JavaCore.getJavaCore()</code>. The Java model plug-in will be activated
 * automatically if not already active.
 * </p>
 * &#064;noinstantiate This class is not intended to be instantiated by clients.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public final class JavaCore extends Plugin {

    private static Plugin JAVA_CORE_PLUGIN = null;
    /**
     * The plug-in identifier of the Java core support
     * (value <code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core"</code>).
     */
    public static final String PLUGIN_ID
        = "com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core"; //$NON-NLS-1$

    /**
     * The identifier for the Java nature
     * (value
     * <code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.javanature"</code>).
     * The presence of this nature on a project indicates that it is
     * Java-capable.
     *
     * @see org.eclipse.core.resources.IProject#hasNature(String)
     */
    public static final String NATURE_ID = PLUGIN_ID + ".javanature"; //$NON-NLS-1$

    /**
     * Name of the User Library Container id.
     * 
     * @since 3.0
     */
    public static final String USER_LIBRARY_CONTAINER_ID
        = "com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.USER_LIBRARY"; //$NON-NLS-1$

    /**
     * @since 3.14
     */
    public static final String MODULE_PATH_CONTAINER_ID
        = "com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.MODULE_PATH"; //$NON-NLS-1$

    // Begin configurable option IDs {

    /**
     * Compiler option ID: Generating Local Variable Debug Attribute.
     * <p>When generated, this attribute will enable local variable names
     * to be displayed in debugger, only in place where variables are
     * definitely assigned (.class file is then bigger).</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.debug.localVariable"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "generate", "do not generate" }</code></dd>
     * <dt>Default:</dt><dd><code>"generate"</code></dd>
     * </dl>
     * &#064;category CompilerOptionID
     */
    public static final String COMPILER_LOCAL_VARIABLE_ATTR = PLUGIN_ID + ".compiler.debug.localVariable"; //$NON-NLS-1$
    /**
     * Compiler option ID: Preserving Unused Local Variables.
     * <p>Unless requested to preserve unused local variables (that is, never read), the
     * compiler will optimize them out, potentially altering debugging.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.codegen.unusedLocal"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "preserve", "optimize out" }</code></dd>
     * <dt>Default:</dt><dd><code>"preserve"</code></dd>
     * </dl>
     * &#064;category CompilerOptionID
     */
    public static final String COMPILER_CODEGEN_UNUSED_LOCAL = PLUGIN_ID + ".compiler.codegen.unusedLocal"; //$NON-NLS-1$
    /**
     * Compiler option ID: Defining Target Java Platform.
     * <p>For binary compatibility reasons, .class files are tagged with a minimal required VM version.</p>
     * <p>Note that <code>"1.8"</code> and higher target versions require the compliance mode to be at least as high
     * as the target version. Usually, compliance, target, and source versions are set to the same values.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.codegen.targetPlatform"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "1.8", "9", ..., {@link #latestSupportedJavaVersion()} }</code></dd>
     * <dt>Default:</dt><dd><code>"1.8"</code></dd>
     * </dl>
     * &#064;category CompilerOptionID
     * 
     * @see #COMPILER_COMPLIANCE
     * @see #COMPILER_SOURCE
     * @see #setComplianceOptions(String, Map)
     */
    public static final String COMPILER_CODEGEN_TARGET_PLATFORM = PLUGIN_ID + ".compiler.codegen.targetPlatform"; //$NON-NLS-1$
    /**
     * Compiler option ID: Inline JSR Bytecode Instruction.
     * <p>When enabled, the compiler will no longer generate JSR instructions, but will rather inline the corresponding
     * finally blocks). The generated code will thus
     * get bigger, but will load faster on virtual machines since the verification process is then much simpler.</p>
     * <p>This mode is anticipating support for the Java Specification Request 202.</p>
     * <p>Note that from 1.5 on, the JSR inlining is mandatory (also see related setting
     * {@link #COMPILER_CODEGEN_TARGET_PLATFORM}).</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.codegen.inlineJsrBytecode"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "enabled", "disabled" }</code></dd>
     * <dt>Default:</dt><dd><code>"enabled"</code></dd>
     * </dl>
     * 
     * @since 3.0
     * &#064;category CompilerOptionID
     * @deprecated this option is implicitly enabled and can't be switched off anymore
     */
    @Deprecated
    public static final String COMPILER_CODEGEN_INLINE_JSR_BYTECODE = PLUGIN_ID + ".compiler.codegen.inlineJsrBytecode"; //$NON-NLS-1$
    /**
     * Compiler option ID: Javadoc Comment Support.
     * <p>When this support is disabled, the compiler will ignore all javadoc problems options settings
     * and will not report any javadoc problem. It will also not find any reference in javadoc comment and
     * DOM AST Javadoc node will be only a flat text instead of having structured tag elements.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.doc.comment.support"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "enabled", "disabled" }</code></dd>
     * <dt>Default:</dt><dd><code>"enabled"</code></dd>
     * </dl>
     * 
     * @since 3.0
     * &#064;category CompilerOptionID
     */
    public static final String COMPILER_DOC_COMMENT_SUPPORT = PLUGIN_ID + ".compiler.doc.comment.support"; //$NON-NLS-1$
    /**
     * @deprecated Discontinued since turning off would violate language specs.
     * &#064;category DeprecatedOptionID
     */
    public static final String COMPILER_PB_UNREACHABLE_CODE = PLUGIN_ID + ".compiler.problem.unreachableCode"; //$NON-NLS-1$
    /**
     * @deprecated Discontinued since turning off would violate language specs.
     * &#064;category DeprecatedOptionID
     */
    public static final String COMPILER_PB_INVALID_IMPORT = PLUGIN_ID + ".compiler.problem.invalidImport"; //$NON-NLS-1$
    /**
     * Compiler option ID: Reporting Usage of <code>'assert'</code> Identifier.
     * <p>When enabled, the compiler will issue an error or a warning whenever <code>'assert'</code> is
     * used as an identifier (reserved keyword in 1.4).</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.problem.assertIdentifier"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "error", "warning", "info", "ignore" }</code></dd>
     * <dt>Default:</dt><dd><code>"error"</code></dd>
     * </dl>
     * 
     * @since 2.0
     * &#064;category CompilerOptionID
     */
    public static final String COMPILER_PB_ASSERT_IDENTIFIER = PLUGIN_ID + ".compiler.problem.assertIdentifier"; //$NON-NLS-1$
    /**
     * Compiler option ID: Reporting Usage of <code>'enum'</code> Identifier.
     * <p>When enabled, the compiler will issue an error or a warning whenever <code>'enum'</code> is
     * used as an identifier (reserved keyword in 1.5).</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.problem.enumIdentifier"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "error", "warning", "info", "ignore" }</code></dd>
     * <dt>Default:</dt><dd><code>"error"</code></dd>
     * </dl>
     * 
     * @since 3.1
     * &#064;category CompilerOptionID
     */
    public static final String COMPILER_PB_ENUM_IDENTIFIER = PLUGIN_ID + ".compiler.problem.enumIdentifier"; //$NON-NLS-1$
    /**
     * Compiler option ID.
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.problem.booleanMethodThrowingException"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "error", "warning", "info", "ignore" }</code></dd>
     * <dt>Default:</dt><dd><code>"ignore"</code></dd>
     * </dl>
     * 
     * @since 3.0
     * &#064;category CompilerOptionID
     * @deprecated - this option has no effect
     */
    public static final String COMPILER_PB_BOOLEAN_METHOD_THROWING_EXCEPTION
        = PLUGIN_ID + ".compiler.problem.booleanMethodThrowingException"; //$NON-NLS-1$

    /**
     * Core option ID: Read external annotations from all build path entries.
     * <p>This option controls where the compiler will look for external annotations for enhanced null analysis</p>
     * <p>When enabled, the compiler will search all buildpath entries of a given project to locate external annotation
     * files
     * ({@code .eea}) in order to superimpose null annotations over classes read from dependencies.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.builder.annotationPath.allLocations"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "disabled", "enabled" }</code></dd>
     * <dt>Default:</dt><dd><code>"disabled"</code></dd>
     * </dl>
     * 
     * @since 3.27
     * &#064;category CoreOptionID
     */
    public static final String CORE_JAVA_BUILD_EXTERNAL_ANNOTATIONS_FROM_ALL_LOCATIONS
        = PLUGIN_ID + ".builder.annotationPath.allLocations"; //$NON-NLS-1$
    /**
     * Compiler option ID: Setting Source Compatibility Mode.
     * <p>Specify whether which source level compatibility is used. From 1.4 on, <code>'assert'</code> is a keyword
     * reserved for assertion support. Also note, than when toggling to 1.4 mode, the target VM
     * level should be set to <code>"1.4"</code> and the compliance mode should be <code>"1.4"</code>.</p>
     * <p>Source level 1.5 is necessary to enable generics, autoboxing, covariance, annotations, enumerations
     * enhanced for loop, static imports and varargs.</p>
     * <p>In source levels <code>"1.5"</code> and higher, the compliance and target settings should be
     * set to the same version as the source level.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.source"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "1.8", "9", ..., {@link #latestSupportedJavaVersion()} }</code></dd>
     * <dt>Default:</dt><dd><code>"1.8"</code></dd>
     * </dl>
     * 
     * @since 2.0
     * &#064;category CompilerOptionID
     * @see #COMPILER_COMPLIANCE
     * @see #COMPILER_CODEGEN_TARGET_PLATFORM
     * @see #setComplianceOptions(String, Map)
     */
    public static final String COMPILER_SOURCE = PLUGIN_ID + ".compiler.source"; //$NON-NLS-1$
    /**
     * Compiler option ID: Setting Compliance Level.
     * <p>Select the compliance level for the compiler.
     * {@link #COMPILER_SOURCE} and {@link #COMPILER_CODEGEN_TARGET_PLATFORM} settings cannot be
     * higher than the compiler compliance level. In <code>"1.5"</code> and higher compliance, source and target
     * settings
     * should match the compliance setting.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.compliance"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "1.8", "9", ..., {@link #latestSupportedJavaVersion()} }</code></dd>
     * <dt>Default:</dt><dd><code>"1.8"</code></dd>
     * </dl>
     * 
     * @since 2.0
     * &#064;category CompilerOptionID
     * @see #COMPILER_SOURCE
     * @see #COMPILER_CODEGEN_TARGET_PLATFORM
     * @see #setComplianceOptions(String, Map)
     */
    public static final String COMPILER_COMPLIANCE = PLUGIN_ID + ".compiler.compliance"; //$NON-NLS-1$
    /**
     * Compiler option ID: Use system libraries from release.
     * <p>When enabled, the compiler will compile against the system libraries from release
     * of the specified compliance level</p>
     * <p>Setting this option sets the {@link #COMPILER_CODEGEN_TARGET_PLATFORM}) and {@link #COMPILER_SOURCE} to
     * the same level as the compiler compliance. This option is available to a project only when a supporting
     * JDK is found in the project's build path</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.release"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "enabled", "disabled" }</code></dd>
     * <dt>Default:</dt><dd><code>"disabled"</code></dd>
     * </dl>
     * 
     * @since 3.14
     * &#064;category CompilerOptionID
     */
    public static final String COMPILER_RELEASE = PLUGIN_ID + ".compiler.release"; //$NON-NLS-1$
    /**
     * Compiler option ID: Defining the Automatic Task Priorities.
     * <p>In parallel with the Automatic Task Tags, this list defines the priorities (high, normal or low)
     * of the task markers issued by the compiler.
     * If the default is specified, the priority of each task marker is <code>"NORMAL"</code>.</p>
     * <p>Task Priorities and task tags must have the same length. If task priorities are set, then task tags should
     * also
     * be set.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.taskPriorities"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "&lt;priority&gt;[,&lt;priority&gt;]*" }</code> where
     * <code>&lt;priority&gt;</code> is one of <code>"HIGH"</code>, <code>"NORMAL"</code> or <code>"LOW"</code></dd>
     * <dt>Default:</dt><dd><code>"NORMAL,HIGH,NORMAL"</code></dd>
     * </dl>
     * 
     * @since 2.1
     * &#064;category CompilerOptionID
     * @see #COMPILER_TASK_TAGS
     */
    public static final String COMPILER_TASK_PRIORITIES = PLUGIN_ID + ".compiler.taskPriorities"; //$NON-NLS-1$
    /**
     * Compiler option ID: Defining the Automatic Task Tags.
     * <p>When the tag list is not empty, the compiler will issue a task marker whenever it encounters
     * one of the corresponding tags inside any comment in Java source code.</p>
     * <p>Generated task messages will start with the tag, and range until the next line separator,
     * comment ending, or tag.</p>
     * <p>When a given line of code bears multiple tags, each tag will be reported separately.
     * Moreover, a tag immediately followed by another tag will be reported using the contents of the
     * next non-empty tag of the line, if any.</p>
     * <p>Note that tasks messages are trimmed. If a tag is starting with a letter or digit, then it cannot be leaded by
     * another letter or digit to be recognized (<code>"fooToDo"</code> will not be recognized as a task for tag
     * <code>"ToDo"</code>, but <code>"foo#ToDo"</code>
     * will be detected for either tag <code>"ToDo"</code> or <code>"#ToDo"</code>). Respectively, a tag ending with a
     * letter or digit cannot be followed
     * by a letter or digit to be recognized (<code>"ToDofoo"</code> will not be recognized as a task for tag
     * <code>"ToDo"</code>, but <code>"ToDo:foo"</code> will
     * be detected either for tag <code>"ToDo"</code> or <code>"ToDo:"</code>).</p>
     * <p>Task Priorities and task tags must have the same length. If task tags are set, then task priorities should
     * also
     * be set.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.taskTags"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "&lt;tag&gt;[,&lt;tag&gt;]*" }</code> where <code>&lt;tag&gt;</code> is a
     * String without any wild-card or leading/trailing spaces</dd>
     * <dt>Default:</dt><dd><code>"TODO,FIXME,XXX"</code></dd>
     * </dl>
     * 
     * @since 2.1
     * &#064;category CompilerOptionID
     * @see #COMPILER_TASK_PRIORITIES
     */
    public static final String COMPILER_TASK_TAGS = PLUGIN_ID + ".compiler.taskTags"; //$NON-NLS-1$
    /**
     * Compiler option ID: Determining whether task tags are case-sensitive.
     * <p>When enabled, task tags are considered in a case-sensitive way.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.taskCaseSensitive"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "enabled", "disabled" }</code></dd>
     * <dt>Default:</dt><dd><code>"enabled"</code></dd>
     * </dl>
     * 
     * @since 3.0
     * &#064;category CompilerOptionID
     */
    public static final String COMPILER_TASK_CASE_SENSITIVE = PLUGIN_ID + ".compiler.taskCaseSensitive"; //$NON-NLS-1$
    /**
     * Compiler option ID: Reporting Forbidden Reference to Type with Restricted Access.
     * <p>When enabled, the compiler will issue an error or a warning when referring to a type that is non accessible,
     * as defined according
     * to the access rule specifications.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.problem.forbiddenReference"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "error", "warning", "info", "ignore" }</code></dd>
     * <dt>Default:</dt><dd><code>"error"</code></dd>
     * </dl>
     * 
     * @since 3.1
     * &#064;category CompilerOptionID
     */
    public static final String COMPILER_PB_FORBIDDEN_REFERENCE = PLUGIN_ID + ".compiler.problem.forbiddenReference"; //$NON-NLS-1$
    /**
     * Compiler option ID: Reporting Discouraged Reference to Type with Restricted Access.
     * <p>When enabled, the compiler will issue an error or a warning when referring to a type with discouraged access,
     * as defined according
     * to the access rule specifications.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.problem.discouragedReference"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "error", "warning", "info", "ignore" }</code></dd>
     * <dt>Default:</dt><dd><code>"warning"</code></dd>
     * </dl>
     * 
     * @since 3.1
     * &#064;category CompilerOptionID
     */
    public static final String COMPILER_PB_DISCOURAGED_REFERENCE = PLUGIN_ID + ".compiler.problem.discouragedReference"; //$NON-NLS-1$
    /**
     * Core option ID: Computing Project Build Order.
     * <p>Indicate whether JavaCore should enforce the project build order to be based on
     * the classpath prerequisite chain. When requesting to compute, this takes over
     * the platform default order (based on project references).</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.computeJavaBuildOrder"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "compute", "ignore" }</code></dd>
     * <dt>Default:</dt><dd><code>"ignore"</code></dd>
     * </dl>
     * &#064;category CoreOptionID
     */
    public static final String CORE_JAVA_BUILD_ORDER = PLUGIN_ID + ".computeJavaBuildOrder"; //$NON-NLS-1$
    /**
     * Core option ID: Specifying Filters for Resource Copying Control.
     * <p>Allow to specify some filters to control the resource copy process.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.builder.resourceCopyExclusionFilter"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "&lt;name&gt;[,&lt;name&gt;]* }</code> where <code>&lt;name&gt;</code> is a
     * file name pattern (* and ? wild-cards allowed)
     * or the name of a folder which ends with <code>'/'</code></dd>
     * <dt>Default:</dt><dd><code>""</code></dd>
     * </dl>
     * 
     * @since 2.0
     * &#064;category CoreOptionID
     */
    public static final String CORE_JAVA_BUILD_RESOURCE_COPY_FILTER
        = PLUGIN_ID + ".builder.resourceCopyExclusionFilter"; //$NON-NLS-1$
    /**
     * Core option ID: Reporting Duplicate Resources.
     * <p>Indicate the severity of the problem reported when more than one occurrence
     * of a resource is to be copied into the output location.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.builder.duplicateResourceTask"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "error", "warning" }</code></dd>
     * <dt>Default:</dt><dd><code>"warning"</code></dd>
     * </dl>
     * 
     * @since 2.1
     * &#064;category CoreOptionID
     */
    public static final String CORE_JAVA_BUILD_DUPLICATE_RESOURCE = PLUGIN_ID + ".builder.duplicateResourceTask"; //$NON-NLS-1$
    /**
     * Core option ID: Cleaning Output Folder(s).
     * <p>Indicate whether the JavaBuilder is allowed to clean the output folders
     * when performing full build operations.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.builder.cleanOutputFolder"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "clean", "ignore" }</code></dd>
     * <dt>Default:</dt><dd><code>"clean"</code></dd>
     * </dl>
     * 
     * @since 2.1
     * &#064;category CoreOptionID
     */
    public static final String CORE_JAVA_BUILD_CLEAN_OUTPUT_FOLDER = PLUGIN_ID + ".builder.cleanOutputFolder"; //$NON-NLS-1$
    /**
     * Core option ID: Recreate Modified class files in Output Folder.
     * <p>Indicate whether the JavaBuilder should check for any changes to .class files
     * in the output folders while performing incremental build operations. If changes
     * are detected to managed .class files, then a full build is performed, otherwise
     * the changes are left as is. Tools further altering generated .class files, like optimizers,
     * should ensure this option remains set in its default state of ignore.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.builder.recreateModifiedClassFileInOutputFolder"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "enabled", "ignore" }</code></dd>
     * <dt>Default:</dt><dd><code>"ignore"</code></dd>
     * </dl>
     * 
     * @since 3.2
     * &#064;category CoreOptionID
     */
    public static final String CORE_JAVA_BUILD_RECREATE_MODIFIED_CLASS_FILES_IN_OUTPUT_FOLDER
        = PLUGIN_ID + ".builder.recreateModifiedClassFileInOutputFolder"; //$NON-NLS-1$
    /**
     * Core option ID: Reporting Incomplete Classpath.
     * <p>Indicate the severity of the problem reported when an entry on the classpath does not exist,
     * is not legitimate or is not visible (for example, a referenced project is closed).</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.incompleteClasspath"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "error", "warning"}</code></dd>
     * <dt>Default:</dt><dd><code>"error"</code></dd>
     * </dl>
     * 
     * @since 2.1
     * &#064;category CoreOptionID
     */
    public static final String CORE_INCOMPLETE_CLASSPATH = PLUGIN_ID + ".incompleteClasspath"; //$NON-NLS-1$
    /**
     * Core option ID: Reporting Classpath Cycle.
     * <p>Indicate the severity of the problem reported when a project is involved in a cycle.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.circularClasspath"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "error", "warning" }</code></dd>
     * <dt>Default:</dt><dd><code>"error"</code></dd>
     * </dl>
     * 
     * @since 2.1
     * &#064;category CoreOptionID
     */
    public static final String CORE_CIRCULAR_CLASSPATH = PLUGIN_ID + ".circularClasspath"; //$NON-NLS-1$
    /**
     * Core option ID: Reporting Incompatible JDK Level for Required Binaries.
     * <p>Indicate the severity of the problem reported when a project prerequisites another project
     * or library with an incompatible target JDK level (e.g. project targeting 1.1 vm, but compiled against 1.4
     * libraries).</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.incompatibleJDKLevel"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "error", "warning", "ignore" }</code></dd>
     * <dt>Default:</dt><dd><code>"ignore"</code></dd>
     * </dl>
     * 
     * @since 3.0
     * &#064;category CoreOptionID
     */
    public static final String CORE_INCOMPATIBLE_JDK_LEVEL = PLUGIN_ID + ".incompatibleJDKLevel"; //$NON-NLS-1$
    /**
     * Core option ID: Abort if Invalid Classpath.
     * <p>Allow to toggle the builder to abort if the classpath is invalid.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.builder.invalidClasspath"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "abort", "ignore" }</code></dd>
     * <dt>Default:</dt><dd><code>"abort"</code></dd>
     * </dl>
     * 
     * @since 2.0
     * &#064;category CoreOptionID
     */
    public static final String CORE_JAVA_BUILD_INVALID_CLASSPATH = PLUGIN_ID + ".builder.invalidClasspath"; //$NON-NLS-1$
    /**
     * Core option ID: Enabling Usage of Classpath Exclusion Patterns.
     * <p>When disabled, no entry on a project classpath can be associated with
     * an exclusion pattern.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.classpath.exclusionPatterns"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "enabled", "disabled" }</code></dd>
     * <dt>Default:</dt><dd><code>"enabled"</code></dd>
     * </dl>
     * 
     * @since 2.1
     * &#064;category CoreOptionID
     */
    public static final String CORE_ENABLE_CLASSPATH_EXCLUSION_PATTERNS = PLUGIN_ID + ".classpath.exclusionPatterns"; //$NON-NLS-1$
    /**
     * Core option ID: Enabling Usage of Classpath Multiple Output Locations.
     * <p>When disabled, no entry on a project classpath can be associated with
     * a specific output location, preventing thus usage of multiple output locations.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.classpath.multipleOutputLocations"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "enabled", "disabled" }</code></dd>
     * <dt>Default:</dt><dd><code>"enabled"</code></dd>
     * </dl>
     * 
     * @since 2.1
     * &#064;category CoreOptionID
     */
    public static final String CORE_ENABLE_CLASSPATH_MULTIPLE_OUTPUT_LOCATIONS
        = PLUGIN_ID + ".classpath.multipleOutputLocations"; //$NON-NLS-1$
    /**
     * Core option ID: Reporting an output location overlapping another source location.
     * <p> Indicate the severity of the problem reported when a source entry's output location overlaps another
     * source entry.</p>
     *
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.classpath.outputOverlappingAnotherSource"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "error", "warning", "ignore" }</code></dd>
     * <dt>Default:</dt><dd><code>"error"</code></dd>
     * </dl>
     * 
     * @since 3.6.4
     */
    public static final String CORE_OUTPUT_LOCATION_OVERLAPPING_ANOTHER_SOURCE
        = PLUGIN_ID + ".classpath.outputOverlappingAnotherSource";  //$NON-NLS-1$

    /**
     * Core option ID: Reporting if a project which has only main sources depends on a project with only test sources.
     * <p> Indicate the severity of the problem reported when a project that has one or more main source folders but
     * no test source folders has a project on its build path that only has one or more test source folders, but no main
     * source folders.</p>
     *
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.classpath.mainOnlyProjectHasTestOnlyDependency"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "error", "ignore" }</code></dd>
     * <dt>Default:</dt><dd><code>"error"</code></dd>
     * </dl>
     * 
     * @since 3.16
     */
    public static final String CORE_MAIN_ONLY_PROJECT_HAS_TEST_ONLY_DEPENDENCY
        = PLUGIN_ID + ".classpath.mainOnlyProjectHasTestOnlyDependency";  //$NON-NLS-1$

    /**
     * Compiler option ID: Enabling support for preview language features.
     * <p>When enabled, the compiler will activate the preview language features of this Java version.</p>
     *
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.problem.enablePreviewFeatures"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "enabled", "disabled" }</code></dd>
     * <dt>Default:</dt><dd><code>"disabled"</code></dd>
     * </dl>
     * &#064;category CompilerOptionID
     * 
     * @since 3.18
     */
    public static final String COMPILER_PB_ENABLE_PREVIEW_FEATURES
        = PLUGIN_ID + ".compiler.problem.enablePreviewFeatures"; //$NON-NLS-1$
    /**
     * Compiler option ID: Reporting Preview features.
     * <p>When enabled, the compiler will issue a warning when a preview feature is used.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.problem.reportPreviewFeatures"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "warning", "info", "ignore" }</code></dd>
     * <dt>Default:</dt><dd><code>"warning"</code></dd>
     * </dl>
     * &#064;category CompilerOptionID
     * 
     * @since 3.18
     */
    public static final String COMPILER_PB_REPORT_PREVIEW_FEATURES
        = PLUGIN_ID + ".compiler.problem.reportPreviewFeatures"; //$NON-NLS-1$
    /**
     * Core option ID: Set the timeout value for retrieving the method's parameter names from javadoc.
     * <p>Timeout in milliseconds to retrieve the method's parameter names from javadoc.</p>
     * <p>If the value is <code>0</code>, the parameter names are not fetched and the raw names are returned.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.timeoutForParameterNameFromAttachedJavadoc"</code></dd>
     * <dt>Possible values:</dt><dd><code>"&lt;n&gt;"</code>, where <code>n</code> is an integer greater than or equal
     * to <code>0</code></dd>
     * <dt>Default:</dt><dd><code>"50"</code></dd>
     * </dl>
     * 
     * @since 3.2
     * &#064;category CoreOptionID
     */
    public static final String TIMEOUT_FOR_PARAMETER_NAME_FROM_ATTACHED_JAVADOC
        = PLUGIN_ID + ".timeoutForParameterNameFromAttachedJavadoc"; //$NON-NLS-1$

    /**
     * Core option ID: The ID of the formatter to use in formatting operations.
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.javaFormatter"</code></dd>
     * <dt>Default:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.defaultJavaFormatter"</code></dd>
     * </dl>
     * 
     * @see #DEFAULT_JAVA_FORMATTER
     * @see #JAVA_FORMATTER_EXTENSION_POINT_ID
     * @since 3.11
     * &#064;category CoreOptionID
     */
    public static final String JAVA_FORMATTER = PLUGIN_ID + ".javaFormatter"; //$NON-NLS-1$

    /**
     * @since 2.0
     * @deprecated Use
     * {@link com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants#FORMATTER_BRACE_POSITION_FOR_ANONYMOUS_TYPE_DECLARATION},
     * {@link com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants#FORMATTER_BRACE_POSITION_FOR_BLOCK}
     * ,
     * {@link com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants#FORMATTER_BRACE_POSITION_FOR_CONSTRUCTOR_DECLARATION},
     * {@link com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants#FORMATTER_BRACE_POSITION_FOR_METHOD_DECLARATION},
     * {@link com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants#FORMATTER_BRACE_POSITION_FOR_SWITCH},
     * {@link com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants#FORMATTER_BRACE_POSITION_FOR_TYPE_DECLARATION}
     * instead.
     * &#064;category DeprecatedOptionID
     */
    public static final String FORMATTER_NEWLINE_OPENING_BRACE = PLUGIN_ID + ".formatter.newline.openingBrace"; //$NON-NLS-1$
    /**
     * @since 2.0
     * @deprecated Use
     * {@link com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants#FORMATTER_INSERT_NEW_LINE_BEFORE_CATCH_IN_TRY_STATEMENT},
     * {@link com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants#FORMATTER_INSERT_NEW_LINE_BEFORE_ELSE_IN_IF_STATEMENT},
     * {@link com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants#FORMATTER_INSERT_NEW_LINE_BEFORE_FINALLY_IN_TRY_STATEMENT},
     * {@link com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants#FORMATTER_INSERT_NEW_LINE_BEFORE_WHILE_IN_DO_STATEMENT}
     * instead.
     * &#064;category DeprecatedOptionID
     */
    public static final String FORMATTER_NEWLINE_CONTROL = PLUGIN_ID + ".formatter.newline.controlStatement"; //$NON-NLS-1$
    /**
     * @since 2.0
     * @deprecated Use
     * {@link com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants#FORMATTER_COMPACT_ELSE_IF}
     * instead.
     * &#064;category DeprecatedOptionID
     */
    public static final String FORMATTER_NEWLINE_ELSE_IF = PLUGIN_ID + ".formatter.newline.elseIf"; //$NON-NLS-1$
    /**
     * @since 2.0
     * @deprecated Use
     * {@link com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants#FORMATTER_INSERT_NEW_LINE_IN_EMPTY_BLOCK}
     * instead.
     * &#064;category DeprecatedOptionID
     */
    public static final String FORMATTER_NEWLINE_EMPTY_BLOCK = PLUGIN_ID + ".formatter.newline.emptyBlock"; //$NON-NLS-1$
    /**
     * @since 2.0
     * @deprecated Use
     * {@link com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants#FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE}
     * instead.
     * &#064;category DeprecatedOptionID
     */
    public static final String FORMATTER_CLEAR_BLANK_LINES = PLUGIN_ID + ".formatter.newline.clearAll"; //$NON-NLS-1$
    /**
     * @since 2.0
     * @deprecated Use
     * {@link com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants#FORMATTER_LINE_SPLIT}
     * instead
     * &#064;category DeprecatedOptionID
     */
    public static final String FORMATTER_LINE_SPLIT = PLUGIN_ID + ".formatter.lineSplit"; //$NON-NLS-1$
    /**
     * @since 2.0
     * @deprecated Use
     * {@link com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants#FORMATTER_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATOR}
     * instead.
     * &#064;category DeprecatedOptionID
     */
    public static final String FORMATTER_COMPACT_ASSIGNMENT = PLUGIN_ID + ".formatter.style.assignment"; //$NON-NLS-1$
    /**
     * @since 2.0
     * @deprecated Use
     * {@link com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants#FORMATTER_TAB_CHAR}
     * instead.
     * &#064;category DeprecatedOptionID
     */
    public static final String FORMATTER_TAB_CHAR = PLUGIN_ID + ".formatter.tabulation.char"; //$NON-NLS-1$
    /**
     * @since 2.0
     * @deprecated Use
     * {@link com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants#FORMATTER_TAB_SIZE}
     * instead.
     * &#064;category DeprecatedOptionID
     */
    public static final String FORMATTER_TAB_SIZE = PLUGIN_ID + ".formatter.tabulation.size"; //$NON-NLS-1$
    /**
     * @since 2.1
     * @deprecated Use
     * {@link com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants#FORMATTER_INSERT_SPACE_AFTER_CLOSING_PAREN_IN_CAST}
     * instead.
     * &#064;category DeprecatedOptionID
     */
    public static final String FORMATTER_SPACE_CASTEXPRESSION = PLUGIN_ID + ".formatter.space.castexpression"; //$NON-NLS-1$
    /**
     * Code assist option ID: Activate Visibility Sensitive Completion.
     * <p>When active, completion doesn't show that you can not see
     * (for example, you can not see private methods of a super class).</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.codeComplete.visibilityCheck"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "enabled", "disabled" }</code></dd>
     * <dt>Default:</dt><dd><code>"disabled"</code></dd>
     * </dl>
     * 
     * @since 2.0
     * &#064;category CodeAssistOptionID
     */
    public static final String CODEASSIST_VISIBILITY_CHECK = PLUGIN_ID + ".codeComplete.visibilityCheck"; //$NON-NLS-1$
    /**
     * Code assist option ID: Activate Deprecation Sensitive Completion.
     * <p>When enabled, completion doesn't propose deprecated members and types.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.codeComplete.deprecationCheck"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "enabled", "disabled" }</code></dd>
     * <dt>Default:</dt><dd><code>"disabled"</code></dd>
     * </dl>
     * 
     * @since 3.2
     * &#064;category CodeAssistOptionID
     */
    public static final String CODEASSIST_DEPRECATION_CHECK = PLUGIN_ID + ".codeComplete.deprecationCheck"; //$NON-NLS-1$
    /**
     * Code assist option ID: Activate Camel Case Sensitive Completion.
     * <p>When enabled, completion shows proposals whose name match the CamelCase
     * pattern.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.codeComplete.camelCaseMatch"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "enabled", "disabled" }</code></dd>
     * <dt>Default:</dt><dd><code>"enabled"</code></dd>
     * </dl>
     * 
     * @since 3.2
     * &#064;category CodeAssistOptionID
     */
    public static final String CODEASSIST_CAMEL_CASE_MATCH = PLUGIN_ID + ".codeComplete.camelCaseMatch"; //$NON-NLS-1$
    /**
     * Code assist option ID: Activate Substring Code Completion.
     * <p>When enabled, completion shows proposals in which the pattern can
     * be found as a substring in a case-insensitive way.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.codeComplete.substringMatch"</code></dd>
     * </dl>
     * 
     * @since 3.12
     * @deprecated - this option has no effect
     * &#064;category DeprecatedOptionID
     */
    public static final String CODEASSIST_SUBSTRING_MATCH = PLUGIN_ID + ".codeComplete.substringMatch"; //$NON-NLS-1$
    /**
     * Code assist option ID: Activate Subword Code Completion.
     * <p>When enabled, completion shows proposals in which the pattern can
     * be found as a subword in a case-insensitive way.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.codeComplete.subwordMatch"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "enabled", "disabled" }</code></dd>
     * <dt>Default:</dt><dd><code>"enabled"</code></dd>
     * </dl>
     * 
     * @since 3.21
     * &#064;category CodeAssistOptionID
     */
    public static final String CODEASSIST_SUBWORD_MATCH = PLUGIN_ID + ".codeComplete.subwordMatch"; //$NON-NLS-1$
    /**
     * Code assist option ID: Automatic Qualification of Implicit Members.
     * <p>When active, completion automatically qualifies completion on implicit
     * field references and message expressions.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.codeComplete.forceImplicitQualification"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "enabled", "disabled" }</code></dd>
     * <dt>Default:</dt><dd><code>"disabled"</code></dd>
     * </dl>
     * 
     * @since 2.0
     * &#064;category CodeAssistOptionID
     */
    public static final String CODEASSIST_IMPLICIT_QUALIFICATION
        = PLUGIN_ID + ".codeComplete.forceImplicitQualification"; //$NON-NLS-1$
    /**
     * Code assist option ID: Define the Prefixes for Field Name.
     * <p>When the prefixes is non empty, completion for field name will begin with
     * one of the proposed prefixes.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.codeComplete.fieldPrefixes"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "&lt;prefix&gt;[,&lt;prefix&gt;]*" }</code> where
     * <code>&lt;prefix&gt;</code> is a String without any wild-card</dd>
     * <dt>Default:</dt><dd><code>""</code></dd>
     * </dl>
     * 
     * @since 2.1
     * &#064;category CodeAssistOptionID
     */
    public static final String CODEASSIST_FIELD_PREFIXES = PLUGIN_ID + ".codeComplete.fieldPrefixes"; //$NON-NLS-1$
    /**
     * Code assist option ID: Define the Prefixes for Static Field Name.
     * <p>When the prefixes is non empty, completion for static field name will begin with
     * one of the proposed prefixes.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.codeComplete.staticFieldPrefixes"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "&lt;prefix&gt;[,&lt;prefix&gt;]*" }</code> where
     * <code>&lt;prefix&gt;</code> is a String without any wild-card</dd>
     * <dt>Default:</dt><dd><code>""</code></dd>
     * </dl>
     * 
     * @since 2.1
     * &#064;category CodeAssistOptionID
     */
    public static final String CODEASSIST_STATIC_FIELD_PREFIXES = PLUGIN_ID + ".codeComplete.staticFieldPrefixes"; //$NON-NLS-1$
    /**
     * Code assist option ID: Define the Prefixes for Static Final Field Name.
     * <p>When the prefixes is non empty, completion for static final field name will begin with
     * one of the proposed prefixes.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.codeComplete.staticFinalFieldPrefixes"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "&lt;prefix&gt;[,&lt;prefix&gt;]*" }</code> where
     * <code>&lt;prefix&gt;</code> is a String without any wild-card</dd>
     * <dt>Default:</dt><dd><code>""</code></dd>
     * </dl>
     * 
     * @since 3.5
     * &#064;category CodeAssistOptionID
     */
    public static final String CODEASSIST_STATIC_FINAL_FIELD_PREFIXES
        = PLUGIN_ID + ".codeComplete.staticFinalFieldPrefixes"; //$NON-NLS-1$
    /**
     * Code assist option ID: Define the Prefixes for Local Variable Name.
     * <p>When the prefixes is non empty, completion for local variable name will begin with
     * one of the proposed prefixes.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.codeComplete.localPrefixes"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "&lt;prefix&gt;[,&lt;prefix&gt;]*" }</code> where
     * <code>&lt;prefix&gt;</code> is a String without any wild-card</dd>
     * <dt>Default:</dt><dd><code>""</code></dd>
     * </dl>
     * 
     * @since 2.1
     * &#064;category CodeAssistOptionID
     */
    public static final String CODEASSIST_LOCAL_PREFIXES = PLUGIN_ID + ".codeComplete.localPrefixes"; //$NON-NLS-1$
    /**
     * Code assist option ID: Define the Prefixes for Argument Name.
     * <p>When the prefixes is non empty, completion for argument name will begin with
     * one of the proposed prefixes.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.codeComplete.argumentPrefixes"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "&lt;prefix&gt;[,&lt;prefix&gt;]*" }</code> where
     * <code>&lt;prefix&gt;</code> is a String without any wild-card</dd>
     * <dt>Default:</dt><dd><code>""</code></dd>
     * </dl>
     * 
     * @since 2.1
     * &#064;category CodeAssistOptionID
     */
    public static final String CODEASSIST_ARGUMENT_PREFIXES = PLUGIN_ID + ".codeComplete.argumentPrefixes"; //$NON-NLS-1$
    /**
     * Code assist option ID: Define the Suffixes for Field Name.
     * <p>When the suffixes is non empty, completion for field name will end with
     * one of the proposed suffixes.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.codeComplete.fieldSuffixes"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "&lt;suffix&gt;[,&lt;suffix&gt;]*" }</code> where
     * <code>&lt;suffix&gt;</code> is a String without any wild-card</dd>
     * <dt>Default:</dt><dd><code>""</code></dd>
     * </dl>
     * 
     * @since 2.1
     * &#064;category CodeAssistOptionID
     */
    public static final String CODEASSIST_FIELD_SUFFIXES = PLUGIN_ID + ".codeComplete.fieldSuffixes"; //$NON-NLS-1$
    /**
     * Code assist option ID: Define the Suffixes for Static Field Name.
     * <p>When the suffixes is non empty, completion for static field name will end with
     * one of the proposed suffixes.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.codeComplete.staticFieldSuffixes"</code></dd>
     * <dt>Possible values:</dt><dd>{@code  "<suffix>[,<suffix>]*" }&lt; where {@code <suffix> } is a String without any
     * wild-card</dd>
     * <dt>Default:</dt><dd><code>""</code></dd>
     * </dl>
     * 
     * @since 2.1
     * &#064;category CodeAssistOptionID
     */
    public static final String CODEASSIST_STATIC_FIELD_SUFFIXES = PLUGIN_ID + ".codeComplete.staticFieldSuffixes"; //$NON-NLS-1$
    /**
     * Code assist option ID: Define the Suffixes for Static Final Field Name.
     * <p>When the suffixes is non empty, completion for static final field name will end with
     * one of the proposed suffixes.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.codeComplete.staticFinalFieldSuffixes"</code></dd>
     * <dt>Possible values:</dt><dd>{@code "<suffix>[<suffix>]*" }&lt; where {@code <suffix>} is a String without any
     * wild-card</dd>
     * <dt>Default:</dt><dd><code>""</code></dd>
     * </dl>
     * 
     * @since 3.5
     * &#064;category CodeAssistOptionID
     */
    public static final String CODEASSIST_STATIC_FINAL_FIELD_SUFFIXES
        = PLUGIN_ID + ".codeComplete.staticFinalFieldSuffixes"; //$NON-NLS-1$
    /**
     * Code assist option ID: Define the Suffixes for Local Variable Name.
     * <p>When the suffixes is non empty, completion for local variable name will end with
     * one of the proposed suffixes.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.codeComplete.localSuffixes"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "&lt;suffix&gt;[,&lt;suffix&gt;]*" }</code> where
     * <code>&lt;suffix&gt;</code> is a String without any wild-card</dd>
     * <dt>Default:</dt><dd><code>""</code></dd>
     * </dl>
     * 
     * @since 2.1
     * &#064;category CodeAssistOptionID
     */
    public static final String CODEASSIST_LOCAL_SUFFIXES = PLUGIN_ID + ".codeComplete.localSuffixes"; //$NON-NLS-1$
    /**
     * Code assist option ID: Define the Suffixes for Argument Name.
     * <p>When the suffixes is non empty, completion for argument name will end with
     * one of the proposed suffixes.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.codeComplete.argumentSuffixes"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "&lt;suffix&gt;[,&lt;suffix&gt;]*" }</code> where
     * <code>&lt;suffix&gt;</code> is a String without any wild-card</dd>
     * <dt>Default:</dt><dd><code>""</code></dd>
     * </dl>
     * 
     * @since 2.1
     * &#064;category CodeAssistOptionID
     */
    public static final String CODEASSIST_ARGUMENT_SUFFIXES = PLUGIN_ID + ".codeComplete.argumentSuffixes"; //$NON-NLS-1$
    /**
     * Code assist option ID: Activate Forbidden Reference Sensitive Completion.
     * <p>When enabled, completion doesn't propose elements which match a
     * forbidden reference rule.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.codeComplete.forbiddenReferenceCheck"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "enabled", "disabled" }</code></dd>
     * <dt>Default:</dt><dd><code>"enabled"</code></dd>
     * </dl>
     * 
     * @since 3.1
     * &#064;category CodeAssistOptionID
     */
    public static final String CODEASSIST_FORBIDDEN_REFERENCE_CHECK
        = PLUGIN_ID + ".codeComplete.forbiddenReferenceCheck"; //$NON-NLS-1$
    /**
     * Code assist option ID: Activate Discouraged Reference Sensitive Completion.
     * <p>When enabled, completion doesn't propose elements which match a
     * discouraged reference rule.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.codeComplete.discouragedReferenceCheck"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "enabled", "disabled" }</code></dd>
     * <dt>Default:</dt><dd><code>"disabled"</code></dd>
     * </dl>
     * 
     * @since 3.1
     * &#064;category CodeAssistOptionID
     */
    public static final String CODEASSIST_DISCOURAGED_REFERENCE_CHECK
        = PLUGIN_ID + ".codeComplete.discouragedReferenceCheck"; //$NON-NLS-1$
    /**
     * Code assist option ID: Activate Suggestion of Static Import.
     * <p>When enabled, completion proposals can contain static import
     * pattern.</p>
     * <dl>
     * <dt>Option
     * id:</dt><dd><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.codeComplete.suggestStaticImports"</code></dd>
     * <dt>Possible values:</dt><dd><code>{ "enabled", "disabled" }</code></dd>
     * <dt>Default:</dt><dd><code>"enabled"</code></dd>
     * </dl>
     * 
     * @since 3.3
     * &#064;category CodeAssistOptionID
     */
    public static final String CODEASSIST_SUGGEST_STATIC_IMPORTS = PLUGIN_ID + ".codeComplete.suggestStaticImports"; //$NON-NLS-1$
    // end configurable option IDs }
    // Begin configurable option values {
    /**
     * @deprecated Use {@link #DEFAULT_TASK_TAGS} instead.
     * @since 2.1
     * &#064;category DeprecatedOptionValue
     */
    public static final String DEFAULT_TASK_TAG = "TODO"; //$NON-NLS-1$
    /**
     * @deprecated Use {@link #DEFAULT_TASK_PRIORITIES} instead.
     * @since 2.1
     * &#064;category DeprecatedOptionValue
     */
    public static final String DEFAULT_TASK_PRIORITY = "NORMAL"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 3.0
     * &#064;category OptionValue
     */
    public static final String DEFAULT_TASK_TAGS = "TODO,FIXME,XXX"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 3.0
     * &#064;category OptionValue
     */
    public static final String DEFAULT_TASK_PRIORITIES = "NORMAL,HIGH,NORMAL"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * &#064;category OptionValue
     */
    public static final String GENERATE = "generate"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * &#064;category OptionValue
     */
    public static final String PRESERVE = "preserve"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * &#064;category OptionValue
     */
    public static final String VERSION_1_1 = "1.1"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * &#064;category OptionValue
     */
    public static final String VERSION_1_2 = "1.2"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 2.0
     * &#064;category OptionValue
     */
    public static final String VERSION_1_3 = "1.3"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 2.0
     * &#064;category OptionValue
     */
    public static final String VERSION_1_4 = "1.4"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 3.0
     * &#064;category OptionValue
     */
    public static final String VERSION_1_5 = "1.5"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 3.2
     * &#064;category OptionValue
     */
    public static final String VERSION_1_6 = "1.6"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 3.3
     * &#064;category OptionValue
     */
    public static final String VERSION_1_7 = "1.7"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 3.10
     * &#064;category OptionValue
     */
    public static final String VERSION_1_8 = "1.8"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 3.14
     * &#064;category OptionValue
     */
    public static final String VERSION_9 = "9"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 3.14
     * &#064;category OptionValue
     */
    public static final String VERSION_10 = "10"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 3.16
     * &#064;category OptionValue
     */
    public static final String VERSION_11 = "11"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 3.18
     * &#064;category OptionValue
     */
    public static final String VERSION_12 = "12"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 3.20
     * &#064;category OptionValue
     */
    public static final String VERSION_13 = "13"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 3.22
     * &#064;category OptionValue
     */
    public static final String VERSION_14 = "14"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 3.24
     * &#064;category OptionValue
     */
    public static final String VERSION_15 = "15"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 3.26
     * &#064;category OptionValue
     */
    public static final String VERSION_16 = "16"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 3.28
     * &#064;category OptionValue
     */
    public static final String VERSION_17 = "17"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 3.30
     * &#064;category OptionValue
     */
    public static final String VERSION_18 = "18"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 3.32
     * &#064;category OptionValue
     */
    public static final String VERSION_19 = "19"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 3.34
     * &#064;category OptionValue
     */
    public static final String VERSION_20 = "20"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 3.36
     * &#064;category OptionValue
     */
    public static final String VERSION_21 = "21"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 3.38
     * &#064;category OptionValue
     */
    public static final String VERSION_22 = "22"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 3.38
     * &#064;category OptionValue
     */
    public static final String VERSION_23 = "23"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 3.42
     * &#064;category OptionValue
     */
    public static final String VERSION_24 = "24"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 3.4
     * &#064;category OptionValue
     */
    public static final String VERSION_CLDC_1_1 = "cldc1.1"; //$NON-NLS-1$
    private static final List<String> allVersions = Collections.unmodifiableList(Arrays.asList(VERSION_CLDC_1_1,
        VERSION_1_1, VERSION_1_2, VERSION_1_3, VERSION_1_4, VERSION_1_5, VERSION_1_6, VERSION_1_7, VERSION_1_8,
        VERSION_9, VERSION_10, VERSION_11, VERSION_12, VERSION_13, VERSION_14, VERSION_15, VERSION_16, VERSION_17,
        VERSION_18, VERSION_19, VERSION_20, VERSION_21, VERSION_22, VERSION_23, VERSION_24));

    /**
     * Returns all {@link JavaCore}{@code #VERSION_*} levels in the order of their
     * introduction. For e.g., {@link JavaCore#VERSION_1_8} appears before {@link JavaCore#VERSION_10}
     *
     * @return all available versions
     * @since 3.14
     */
    public static List<String> getAllVersions() {
        return allVersions;
    }

    /**
     * Configurable option value: {@value}.
     * 
     * @since 2.0
     * &#064;category OptionValue
     */
    public static final String ABORT = "abort"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * &#064;category OptionValue
     */
    public static final String ERROR = "error"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * &#064;category OptionValue
     */
    public static final String WARNING = "warning"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * &#064;category OptionValue
     */
    public static final String IGNORE = "ignore"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * &#064;category OptionValue
     * 
     * @since 3.12
     */
    public static final String INFO = "info"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 2.0
     * &#064;category OptionValue
     */
    public static final String INSERT = "insert"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 2.0
     * &#064;category OptionValue
     */
    public static final String DO_NOT_INSERT = "do not insert"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 2.0
     * &#064;category OptionValue
     */
    public static final String PRESERVE_ONE = "preserve one"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 2.0
     * &#064;category OptionValue
     */
    public static final String NORMAL = "normal"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 2.0
     * &#064;category OptionValue
     */
    public static final String COMPACT = "compact"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 2.0
     * &#064;category OptionValue
     */
    public static final String TAB = "tab"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 2.0
     * &#064;category OptionValue
     */
    public static final String SPACE = "space"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 2.0
     * &#064;category OptionValue
     */
    public static final String ENABLED = "enabled"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 2.0
     * &#064;category OptionValue
     */
    public static final String DISABLED = "disabled"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 2.1
     * &#064;category OptionValue
     */
    public static final String CLEAN = "clean"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 3.0
     * &#064;category OptionValue
     */
    public static final String PUBLIC = "public"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 3.0
     * &#064;category OptionValue
     */
    public static final String PROTECTED = "protected"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 3.0
     * &#064;category OptionValue
     */
    public static final String DEFAULT = "default"; //$NON-NLS-1$
    /**
     * Configurable option value: {@value}.
     * 
     * @since 3.0
     * &#064;category OptionValue
     */
    public static final String PRIVATE = "private"; //$NON-NLS-1$
    // end configurable option values }

    /**
     * Name of the extension point for contributing a source code formatter
     * 
     * @see #JAVA_FORMATTER
     * @see #DEFAULT_JAVA_FORMATTER
     * @since 3.11
     */
    public static final String JAVA_FORMATTER_EXTENSION_POINT_ID = "javaFormatter";  //$NON-NLS-1$

    /**
     * Creates the Java core plug-in.
     * <p>
     * The plug-in instance is created automatically by the
     * Eclipse platform. Clients must not call.
     * </p>
     *
     * @since 3.0
     */
    public JavaCore() {
        super();
        JAVA_CORE_PLUGIN = this;
    }

    /**
     * Adds the given listener for changes to Java elements.
     * Has no effect if an identical listener is already registered.
     * <p>
     * This listener will only be notified during the POST_CHANGE resource change notification
     * and any reconcile operation (POST_RECONCILE).
     * </p>
     * <p>
     * For finer control of the notification, use <code>addElementChangedListener(IElementChangedListener,int)</code>,
     * which allows to specify a different eventMask.
     * </p>
     *
     * @param listener the listener
     * @see ElementChangedEvent
     */
    public static void addElementChangedListener(IElementChangedListener listener) {
        addElementChangedListener(listener, ElementChangedEvent.POST_CHANGE | ElementChangedEvent.POST_RECONCILE);
    }

    /**
     * Adds the given listener for changes to Java elements.
     * Has no effect if an identical listener is already registered.
     * After completion of this method, the given listener will be registered for exactly
     * the specified events. If they were previously registered for other events, they
     * will be deregistered.
     * <p>
     * Once registered, a listener starts receiving notification of changes to
     * java elements in the model. The listener continues to receive
     * notifications until it is replaced or removed.
     * </p>
     * <p>
     * Listeners can listen for several types of event as defined in <code>ElementChangeEvent</code>.
     * Clients are free to register for any number of event types however if they register
     * for more than one, it is their responsibility to ensure they correctly handle the
     * case where the same java element change shows up in multiple notifications.
     * Clients are guaranteed to receive only the events for which they are registered.
     * </p>
     *
     * @param listener the listener
     * @param eventMask the bit-wise OR of all event types of interest to the listener
     * @since 2.0
     */
    public static void addElementChangedListener(IElementChangedListener listener, int eventMask) {
        JavaModelManager.getDeltaState().addElementChangedListener(listener, eventMask);
    }

    /**
     * Returns the Java model element corresponding to the given handle identifier
     * generated by <code>IJavaElement.getHandleIdentifier()</code>, or
     * <code>null</code> if unable to create the associated element.
     *
     * @param handleIdentifier the given handle identifier
     * @return the Java element corresponding to the handle identifier
     */
    public static IJavaElement create(String handleIdentifier) {
        return create(handleIdentifier, DefaultWorkingCopyOwner.PRIMARY);
    }

    /**
     * Returns the Java model element corresponding to the given handle identifier
     * generated by <code>IJavaElement.getHandleIdentifier()</code>, or
     * <code>null</code> if unable to create the associated element.
     * If the returned Java element is an <code>ICompilationUnit</code> or an element
     * inside a compilation unit, the compilation unit's owner is the given owner if such a
     * working copy exists, otherwise the compilation unit is a primary compilation unit.
     *
     * @param handleIdentifier the given handle identifier
     * @param owner the owner of the returned compilation unit, ignored if the returned
     * element is not a compilation unit, or an element inside a compilation unit
     * @return the Java element corresponding to the handle identifier
     * @since 3.0
     */
    public static IJavaElement create(String handleIdentifier, WorkingCopyOwner owner) {
        if (handleIdentifier == null) {
            return null;
        }
        if (owner == null)
            owner = DefaultWorkingCopyOwner.PRIMARY;
        MementoTokenizer memento = new MementoTokenizer(handleIdentifier);
        JavaModel model = JavaModelManager.getJavaModelManager().getJavaModel();
        return model.getHandleFromMemento(memento, owner);
    }

    /**
     * Returns the Java element corresponding to the given file, or
     * <code>null</code> if unable to associate the given file
     * with a Java element.
     *
     * <p>The file must be one of:</p>
     * <ul>
     * <li>a <code>.class</code> file - the element returned is the corresponding <code>IClassFile</code></li>
     * <li>a ZIP archive (e.g. a <code>.jar</code>, a <code>.zip</code> file, etc.) - the element returned is the
     * corresponding <code>IPackageFragmentRoot</code></li>
     * </ul>
     * <p>
     * Creating a Java element has the side effect of creating and opening all of the
     * element's parents if they are not yet open.
     * </p>
     *
     * @param file the given file
     * @return the Java element corresponding to the given file, or
     * <code>null</code> if unable to associate the given file
     * with a Java element
     */
    public static IJavaElement create(IFile file) {
        return JavaModelManager.create(file, null/* unknown java project */);
    }

    /**
     * Returns the package fragment or package fragment root corresponding to the given folder, or
     * <code>null</code> if unable to associate the given folder with a Java element.
     * <p>
     * Note that a package fragment root is returned rather than a default package.
     * </p>
     * <p>
     * Creating a Java element has the side effect of creating and opening all of the
     * element's parents if they are not yet open.
     * </p>
     *
     * @param folder the given folder
     * @return the package fragment or package fragment root corresponding to the given folder, or
     * <code>null</code> if unable to associate the given folder with a Java element
     */
    public static IJavaElement create(IFolder folder) {
        return JavaModelManager.create(folder, null/* unknown java project */);
    }

    /**
     * Returns the Java project corresponding to the given project.
     * <p>
     * Creating a Java Project has the side effect of creating and opening all of the
     * project's parents if they are not yet open.
     * </p>
     * <p>
     * Note that no check is done at this time on the existence or the java nature of this project.
     * </p>
     *
     * @param project the given project
     * @return the Java project corresponding to the given project, null if the given project is null
     */
    public static IJavaProject create(IProject project) {
        if (project == null) {
            return null;
        }
        JavaModel javaModel = JavaModelManager.getJavaModelManager().getJavaModel();
        return javaModel.getJavaProject(project);
    }

    /**
     * Returns the Java element corresponding to the given resource, or
     * <code>null</code> if unable to associate the given resource
     * with a Java element.
     * <p>
     * The resource must be one of:
     * </p>
     * <ul>
     * <li>a project - the element returned is the corresponding <code>IJavaProject</code></li>
     * <li>a <code>.class</code> file - the element returned is the corresponding <code>IClassFile</code></li>
     * <li>a ZIP archive (e.g. a <code>.jar</code>, a <code>.zip</code> file, etc.) - the element returned is the
     * corresponding <code>IPackageFragmentRoot</code></li>
     * <li>a folder - the element returned is the corresponding <code>IPackageFragmentRoot</code>
     * or <code>IPackageFragment</code></li>
     * <li>the workspace root resource - the element returned is the <code>IJavaModel</code></li>
     * </ul>
     * <p>
     * Creating a Java element has the side effect of creating and opening all of the
     * element's parents if they are not yet open.
     * </p>
     *
     * @param resource the given resource
     * @return the Java element corresponding to the given resource, or
     * <code>null</code> if unable to associate the given resource
     * with a Java element
     */
    public static IJavaElement create(IResource resource) {
        return JavaModelManager.create(resource, null/* unknown java project */);
    }

    /**
     * Returns the Java element corresponding to the given file, its project being the given
     * project. Returns <code>null</code> if unable to associate the given resource
     * with a Java element.
     * <p>
     * The resource must be one of:
     * </p>
     * <ul>
     * <li>a project - the element returned is the corresponding <code>IJavaProject</code></li>
     * <li>a <code>.class</code> file - the element returned is the corresponding <code>IClassFile</code></li>
     * <li>a ZIP archive (e.g. a <code>.jar</code>, a <code>.zip</code> file, etc.) - the element returned is the
     * corresponding <code>IPackageFragmentRoot</code></li>
     * <li>a folder - the element returned is the corresponding <code>IPackageFragmentRoot</code>
     * or <code>IPackageFragment</code></li>
     * <li>the workspace root resource - the element returned is the <code>IJavaModel</code></li>
     * </ul>
     * <p>
     * Creating a Java element has the side effect of creating and opening all of the
     * element's parents if they are not yet open.
     * </p>
     *
     * @param resource the given resource
     * @return the Java element corresponding to the given file, or
     * <code>null</code> if unable to associate the given file
     * with a Java element
     * @since 3.3
     */
    public static IJavaElement create(IResource resource, IJavaProject project) {
        return JavaModelManager.create(resource, project);
    }

    /**
     * Returns the Java model.
     *
     * @param root the given root
     * @return the Java model, or <code>null</code> if the root is null
     */
    public static IJavaModel create(IWorkspaceRoot root) {
        if (root == null) {
            return null;
        }
        return JavaModelManager.getJavaModelManager().getJavaModel();
    }

    /**
     * Creates and returns a compilation unit element for
     * the given source file. Returns <code>null</code> if unable
     * to recognize the compilation unit.
     *
     * @param file the given source file
     * @return a compilation unit element for the given source file, or <code>null</code> if unable
     * to recognize the compilation unit
     */
    public static ICompilationUnit createCompilationUnitFrom(IFile file) {
        return JavaModelManager.createCompilationUnitFrom(file, null/* unknown java project */);
    }

    /**
     * Helper method finding the classpath container initializer registered for a given classpath container ID
     * or <code>null</code> if none was found while iterating over the contributions to extension point to
     * the extension point
     * "com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.classpathContainerInitializer".
     * <p>
     * A containerID is the first segment of any container path, used to identify the registered container initializer.
     * </p>
     * 
     * @param containerID - a containerID identifying a registered initializer
     * @return ClasspathContainerInitializer - the registered classpath container initializer or <code>null</code> if
     * none was found.
     * @since 2.1
     */
    public static ClasspathContainerInitializer getClasspathContainerInitializer(String containerID) {
        Hashtable containerInitializersCache = JavaModelManager.getJavaModelManager().containerInitializersCache;
        ClasspathContainerInitializer initializer
            = (ClasspathContainerInitializer) containerInitializersCache.get(containerID);
        if (initializer == null) {
            initializer = computeClasspathContainerInitializer(containerID);
            if (initializer == null)
                return null;
            containerInitializersCache.put(containerID, initializer);
        }
        return initializer;
    }

    private static ClasspathContainerInitializer computeClasspathContainerInitializer(String containerID) {
        Plugin jdtCorePlugin = JavaCore.getPlugin();
        if (jdtCorePlugin == null)
            return null;

        IExtensionPoint extension = Platform.getExtensionRegistry()
            .getExtensionPoint(JavaCore.PLUGIN_ID, JavaModelManager.CPCONTAINER_INITIALIZER_EXTPOINT_ID);
        if (extension != null) {
            IExtension[] extensions = extension.getExtensions();
            for (IExtension ext : extensions) {
                IConfigurationElement[] configElements = ext.getConfigurationElements();
                for (IConfigurationElement configurationElement : configElements) {
                    String initializerID = configurationElement.getAttribute("id"); //$NON-NLS-1$
                    if (initializerID != null && initializerID.equals(containerID)) {
                        if (JavaModelManager.CP_RESOLVE_VERBOSE_ADVANCED)
                            verbose_found_container_initializer(containerID, configurationElement);
                        try {
                            Object execExt = configurationElement.createExecutableExtension("class"); //$NON-NLS-1$
                            if (execExt instanceof ClasspathContainerInitializer) {
                                return (ClasspathContainerInitializer) execExt;
                            }
                        } catch (CoreException e) {
                            // executable extension could not be created: ignore this initializer
                            if (JavaModelManager.CP_RESOLVE_VERBOSE || JavaModelManager.CP_RESOLVE_VERBOSE_FAILURE) {
                                verbose_failed_to_instanciate_container_initializer(containerID, configurationElement,
                                    e);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private static void verbose_failed_to_instanciate_container_initializer(String containerID,
        IConfigurationElement configurationElement, CoreException e) {
        trace("CPContainer INIT - failed to instanciate initializer\n" + //$NON-NLS-1$
            "	container ID: " + containerID + '\n' + //$NON-NLS-1$
            "	class: " + configurationElement.getAttribute("class"), //$NON-NLS-1$ //$NON-NLS-2$
            e);
    }

    private static void verbose_found_container_initializer(String containerID,
        IConfigurationElement configurationElement) {
        trace("CPContainer INIT - found initializer\n" + //$NON-NLS-1$
            "	container ID: " + containerID + '\n' + //$NON-NLS-1$
            "	class: " + configurationElement.getAttribute("class")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Returns the path held in the given classpath variable.
     * Returns <code>null</code> if unable to bind.
     * <p>
     * Classpath variable values are persisted locally to the workspace, and
     * are preserved from session to session.
     * </p>
     * <p>
     * Note that classpath variables can be contributed registered initializers for,
     * using the extension point
     * "com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.classpathVariableInitializer".
     * If an initializer is registered for a variable, its persisted value will be ignored:
     * its initializer will thus get the opportunity to rebind the variable differently on
     * each session.
     * </p>
     *
     * @param variableName the name of the classpath variable
     * @return the path, or <code>null</code> if none
     */
    public static IPath getClasspathVariable(final String variableName) {

        JavaModelManager manager = JavaModelManager.getJavaModelManager();
        IPath variablePath = manager.variableGet(variableName);
        if (variablePath == JavaModelManager.VARIABLE_INITIALIZATION_IN_PROGRESS) {
            return manager.getPreviousSessionVariable(variableName);
        }

        if (variablePath != null) {
            if (variablePath == JavaModelManager.CP_ENTRY_IGNORE_PATH)
                return null;
            return variablePath;
        }

        // even if persisted value exists, initializer is given priority, only if no initializer is found the persisted
        // value is reused
        final ClasspathVariableInitializer initializer = JavaCore.getClasspathVariableInitializer(variableName);
        if (initializer != null) {
            if (JavaModelManager.CP_RESOLVE_VERBOSE)
                verbose_triggering_variable_initialization(variableName, initializer);
            if (JavaModelManager.CP_RESOLVE_VERBOSE_ADVANCED)
                verbose_triggering_variable_initialization_invocation_trace();
            manager.variablePut(variableName, JavaModelManager.VARIABLE_INITIALIZATION_IN_PROGRESS); // avoid
                                                                                                     // initialization
                                                                                                     // cycles
            boolean ok = false;
            try {
                // let OperationCanceledException go through
                // (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=59363)
                initializer.initialize(variableName);

                variablePath = manager.variableGet(variableName); // initializer should have performed side-effect
                if (variablePath == JavaModelManager.VARIABLE_INITIALIZATION_IN_PROGRESS)
                    return null; // break cycle (initializer did not init or reentering call)
                if (JavaModelManager.CP_RESOLVE_VERBOSE_ADVANCED)
                    verbose_variable_value_after_initialization(variableName, variablePath);
                manager.variablesWithInitializer.add(variableName);
                ok = true;
            } catch (RuntimeException | Error e) {
                if (JavaModelManager.CP_RESOLVE_VERBOSE || JavaModelManager.CP_RESOLVE_VERBOSE_FAILURE)
                    trace("", new Exception(e)); //$NON-NLS-1$
                throw e;
            } finally {
                if (!ok)
                    JavaModelManager.getJavaModelManager().variablePut(variableName, null); // flush cache
            }
        } else {
            if (JavaModelManager.CP_RESOLVE_VERBOSE_ADVANCED || JavaModelManager.CP_RESOLVE_VERBOSE_FAILURE)
                verbose_no_variable_initializer_found(variableName);
        }
        return variablePath;
    }

    private static void verbose_no_variable_initializer_found(String variableName) {
        trace("CPVariable INIT - no initializer found\n" + //$NON-NLS-1$
            "	variable: " + variableName); //$NON-NLS-1$
    }

    private static void verbose_variable_value_after_initialization(String variableName, IPath variablePath) {
        trace("CPVariable INIT - after initialization\n" + //$NON-NLS-1$
            "	variable: " + variableName + '\n' + //$NON-NLS-1$
            "	variable path: " + variablePath); //$NON-NLS-1$
    }

    private static void verbose_triggering_variable_initialization(String variableName,
        ClasspathVariableInitializer initializer) {
        trace("CPVariable INIT - triggering initialization\n" + //$NON-NLS-1$
            "	variable: " + variableName + '\n' + //$NON-NLS-1$
            "	initializer: " + initializer); //$NON-NLS-1$
    }

    private static void verbose_triggering_variable_initialization_invocation_trace() {
        trace("CPVariable INIT - triggering initialization\n" + //$NON-NLS-1$
            "	invocation trace:", new Exception("<Fake exception>")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Returns deprecation message of a given classpath variable.
     *
     * @return A string if the classpath variable is deprecated, <code>null</code> otherwise.
     * @since 3.3
     */
    public static String getClasspathVariableDeprecationMessage(String variableName) {
        JavaModelManager manager = JavaModelManager.getJavaModelManager();

        // Returns the stored deprecation message
        String message = manager.deprecatedVariables.get(variableName);
        if (message != null) {
            return message;
        }

        // If the variable has been already initialized, then there's no deprecation message
        IPath variablePath = manager.variableGet(variableName);
        if (variablePath != null && variablePath != JavaModelManager.VARIABLE_INITIALIZATION_IN_PROGRESS) {
            return null;
        }

        // Search for extension point to get the possible deprecation message
        Plugin jdtCorePlugin = JavaCore.getPlugin();
        if (jdtCorePlugin == null)
            return null;

        IExtensionPoint extension = Platform.getExtensionRegistry()
            .getExtensionPoint(JavaCore.PLUGIN_ID, JavaModelManager.CPVARIABLE_INITIALIZER_EXTPOINT_ID);
        if (extension != null) {
            IExtension[] extensions = extension.getExtensions();
            for (IExtension ext : extensions) {
                IConfigurationElement[] configElements = ext.getConfigurationElements();
                for (IConfigurationElement configElement : configElements) {
                    String varAttribute = configElement.getAttribute("variable"); //$NON-NLS-1$
                    if (variableName.equals(varAttribute)) {
                        String deprecatedAttribute = configElement.getAttribute("deprecated"); //$NON-NLS-1$
                        if (deprecatedAttribute != null) {
                            return deprecatedAttribute;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Helper method finding the classpath variable initializer registered for a given classpath variable name
     * or <code>null</code> if none was found while iterating over the contributions to extension point to
     * the extension point
     * "com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.classpathVariableInitializer".
     *
     * @param variable the given variable
     * @return ClasspathVariableInitializer - the registered classpath variable initializer or <code>null</code> if
     * none was found.
     * @since 2.1
     */
    public static ClasspathVariableInitializer getClasspathVariableInitializer(String variable) {

        Plugin jdtCorePlugin = JavaCore.getPlugin();
        if (jdtCorePlugin == null)
            return null;

        IExtensionPoint extension = Platform.getExtensionRegistry()
            .getExtensionPoint(JavaCore.PLUGIN_ID, JavaModelManager.CPVARIABLE_INITIALIZER_EXTPOINT_ID);
        if (extension != null) {
            IExtension[] extensions = extension.getExtensions();
            for (IExtension ext : extensions) {
                IConfigurationElement[] configElements = ext.getConfigurationElements();
                for (IConfigurationElement configElement : configElements) {
                    try {
                        String varAttribute = configElement.getAttribute("variable"); //$NON-NLS-1$
                        if (variable.equals(varAttribute)) {
                            if (JavaModelManager.CP_RESOLVE_VERBOSE_ADVANCED)
                                verbose_found_variable_initializer(variable, configElement);
                            Object execExt = configElement.createExecutableExtension("class"); //$NON-NLS-1$
                            if (execExt instanceof ClasspathVariableInitializer) {
                                ClasspathVariableInitializer initializer = (ClasspathVariableInitializer) execExt;
                                String deprecatedAttribute = configElement.getAttribute("deprecated"); //$NON-NLS-1$
                                if (deprecatedAttribute != null) {
                                    JavaModelManager.getJavaModelManager().deprecatedVariables.put(variable,
                                        deprecatedAttribute);
                                }
                                String readOnlyAttribute = configElement.getAttribute("readOnly"); //$NON-NLS-1$
                                if (JavaModelManager.TRUE.equals(readOnlyAttribute)) {
                                    JavaModelManager.getJavaModelManager().readOnlyVariables.add(variable);
                                }
                                return initializer;
                            }
                        }
                    } catch (CoreException e) {
                        // executable extension could not be created: ignore this initializer
                        if (JavaModelManager.CP_RESOLVE_VERBOSE || JavaModelManager.CP_RESOLVE_VERBOSE_FAILURE) {
                            verbose_failed_to_instanciate_variable_initializer(variable, configElement, e);
                        }
                    }
                }
            }
        }
        return null;
    }

    private static void verbose_failed_to_instanciate_variable_initializer(String variable,
        IConfigurationElement configElement, CoreException e) {
        trace("CPContainer INIT - failed to instanciate initializer\n" + //$NON-NLS-1$
            "	variable: " + variable + '\n' + //$NON-NLS-1$
            "	class: " + configElement.getAttribute("class"), //$NON-NLS-1$ //$NON-NLS-2$
            e);
    }

    private static void verbose_found_variable_initializer(String variable, IConfigurationElement configElement) {
        trace("CPVariable INIT - found initializer\n" + //$NON-NLS-1$
            "	variable: " + variable + '\n' + //$NON-NLS-1$
            "	class: " + configElement.getAttribute("class")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Returns the single instance of the Java core plug-in runtime class.
     * Equivalent to <code>(JavaCore) getPlugin()</code>.
     *
     * @return the single instance of the Java core plug-in runtime class
     */
    public static JavaCore getJavaCore() {
        return (JavaCore) getPlugin();
    }

    /**
     * Returns the table of the current options. Initially, all options have their default values,
     * and this method returns a table that includes all known options.
     * <p>
     * Helper constants have been defined on JavaCore for each of the option IDs
     * (categorized in Code assist option ID, Compiler option ID and Core option ID)
     * and some of their acceptable values (categorized in Option value). Some
     * options accept open value sets beyond the documented constant values.
     * </p>
     * <p>
     * Note: each release may add new options.
     * </p>
     * <p>Returns a default set of options even if the platform is not running.</p>
     *
     * @return table of current settings of all options
     * (key type: <code>String</code>; value type: <code>String</code>)
     * @see JavaCorePreferenceInitializer for changing default settings
     */
    public static Hashtable<String, String> getOptions() {
        return JavaModelManager.getJavaModelManager().getOptions();
    }

    /**
     * Returns the single instance of the Java core plug-in runtime class.
     *
     * @return the single instance of the Java core plug-in runtime class
     */
    public static Plugin getPlugin() {
        return JAVA_CORE_PLUGIN;
    }

    /**
     * This is a helper method, which returns the resolved classpath entry denoted
     * by a given entry (if it is a variable entry). It is obtained by resolving the variable
     * reference in the first segment. Returns <code>null</code> if unable to resolve using
     * the following algorithm:
     * <ul>
     * <li> if variable segment cannot be resolved, returns <code>null</code></li>
     * <li> finds a project, JAR or binary folder in the workspace at the resolved path location</li>
     * <li> if none finds an external JAR file or folder outside the workspace at the resolved path location </li>
     * <li> if none returns <code>null</code></li>
     * </ul>
     * <p>
     * Variable source attachment path and root path are also resolved and recorded in the resulting classpath entry.
     * </p>
     * <p>
     * NOTE: This helper method does not handle classpath containers, for which should rather be used
     * <code>JavaCore#getClasspathContainer(IPath, IJavaProject)</code>.
     * </p>
     *
     * @param entry the given variable entry
     * @return the resolved library or project classpath entry, or <code>null</code>
     * if the given variable entry could not be resolved to a valid classpath entry
     */
    public static IClasspathEntry getResolvedClasspathEntry(IClasspathEntry entry) {
        return JavaModelManager.getJavaModelManager()
            .resolveVariableEntry(entry, false/* don't use previous session value */);
    }

    /**
     * Resolve a variable path (helper method).
     *
     * @param variablePath the given variable path
     * @return the resolved variable path or <code>null</code> if none
     */
    public static IPath getResolvedVariablePath(IPath variablePath) {
        return JavaModelManager.getJavaModelManager()
            .getResolvedVariablePath(variablePath, false/* don't use previous session value */);
    }

    /**
     * Answers the shared working copies currently registered for this buffer factory.
     * Working copies can be shared by several clients using the same buffer factory,see
     * <code>IWorkingCopy.getSharedWorkingCopy</code>.
     *
     * @param factory the given buffer factory
     * @return the list of shared working copies for a given buffer factory
     * @since 2.0
     * @deprecated Use {@link #getWorkingCopies(WorkingCopyOwner)} instead
     */
    public static IWorkingCopy[] getSharedWorkingCopies(IBufferFactory factory) {

        // if factory is null, default factory must be used
        if (factory == null)
            factory = BufferManager.getDefaultBufferManager().getDefaultBufferFactory();

        return getWorkingCopies(BufferFactoryWrapper.create(factory));
    }

    /**
     * Returns the working copies that have the given owner.
     * Only compilation units in working copy mode are returned.
     * If the owner is <code>null</code>, primary working copies are returned.
     *
     * @param owner the given working copy owner or <code>null</code> for primary working copy owner
     * @return the list of working copies for a given owner
     * @since 3.0
     */
    public static ICompilationUnit[] getWorkingCopies(WorkingCopyOwner owner) {

        JavaModelManager manager = JavaModelManager.getJavaModelManager();
        if (owner == null)
            owner = DefaultWorkingCopyOwner.PRIMARY;
        ICompilationUnit[] result = manager.getWorkingCopies(owner, false/* don't add primary WCs */);
        if (result == null)
            return JavaModelManager.NO_WORKING_COPY;
        return result;
    }

    /**
     * Creates and returns a new access rule with the given file pattern and kind.
     * <p>
     * The rule kind is one of {@link IAccessRule#K_ACCESSIBLE}, {@link IAccessRule#K_DISCOURAGED},
     * or {@link IAccessRule#K_NON_ACCESSIBLE}, optionally combined with {@link IAccessRule#IGNORE_IF_BETTER},
     * e.g. <code>IAccessRule.K_NON_ACCESSIBLE | IAccessRule.IGNORE_IF_BETTER</code>.
     * </p>
     *
     * @param filePattern the file pattern this access rule should match
     * @param kind one of {@link IAccessRule#K_ACCESSIBLE}, {@link IAccessRule#K_DISCOURAGED},
     * or {@link IAccessRule#K_NON_ACCESSIBLE}, optionally combined with
     * {@link IAccessRule#IGNORE_IF_BETTER}
     * @return a new access rule
     * @since 3.1
     *
     * @see IClasspathEntry#getExclusionPatterns()
     */
    public static IAccessRule newAccessRule(IPath filePattern, int kind) {
        return JavaModelManager.getJavaModelManager().getAccessRule(filePattern, kind);
    }

    /**
     * Creates and returns a new classpath attribute with the given name and the given value.
     *
     * @return a new classpath attribute
     * @since 3.1
     */
    public static IClasspathAttribute newClasspathAttribute(String name, String value) {
        return new ClasspathAttribute(name, value);
    }

    /**
     * Creates and returns a new classpath entry of kind <code>CPE_LIBRARY</code> for the JAR or folder
     * identified by the given absolute path. This specifies that all package fragments within the root
     * will have children of type <code>IClassFile</code>.
     * <p>
     * A library entry is used to denote a prerequisite JAR or root folder containing binaries.
     * The target JAR can either be defined internally to the workspace (absolute path relative
     * to the workspace root), or externally to the workspace (absolute path in the file system).
     * The target root folder can also be defined internally to the workspace (absolute path relative
     * to the workspace root), or - since 3.4 - externally to the workspace (absolute path in the file system).
     * Since 3.5, the path to the library can also be relative to the project using ".." as the first segment.
     * </p>
     * <p>
     * e.g. Here are some examples of binary path usage
     * </p>
     * <ul>
     * <li><code> "c:\jdk1.2.2\jre\lib\rt.jar" </code> - reference to an external JAR on Windows</li>
     * <li><code> "/Project/someLib.jar" </code> - reference to an internal JAR on Windows or Linux</li>
     * <li><code> "/Project/classes/" </code> - reference to an internal binary folder on Windows or Linux</li>
     * <li><code> "/home/usr/classes" </code> - reference to an external binary folder on Linux</li>
     * <li><code> "../../lib/someLib.jar" </code> - reference to an external JAR that is a sibling of the workspace on
     * either platform</li>
     * </ul>
     * Note that on non-Windows platform, a path <code>"/some/lib.jar"</code> is ambiguous.
     * It can be a path to an external JAR (its file system path being <code>"/some/lib.jar"</code>)
     * or it can be a path to an internal JAR (<code>"some"</code> being a project in the workspace).
     * Such an ambiguity is solved when the classpath entry is used (e.g. in
     * {@link IJavaProject#getPackageFragmentRoots()}).
     * If the resource <code>"lib.jar"</code> exists in project <code>"some"</code>, then it is considered an
     * internal JAR. Otherwise it is an external JAR.
     * <p>Also note that this operation does not attempt to validate or access the
     * resources at the given paths.
     * </p><p>
     * The access rules determine the set of accessible class files
     * in the library. If the list of access rules is empty then all files
     * in this library are accessible.
     * See {@link IAccessRule} for a detailed description of access
     * rules.
     * </p>
     * <p>
     * The <code>extraAttributes</code> list contains name/value pairs that must be persisted with
     * this entry. If no extra attributes are provided, an empty array must be passed in.<br>
     * Note that this list should not contain any duplicate name.
     * </p>
     * <p>
     * The <code>isExported</code> flag indicates whether this entry is contributed to dependent
     * projects. If not exported, dependent projects will not see any of the classes from this entry.
     * If exported, dependent projects will concatenate the accessible files patterns of this entry with the
     * accessible files patterns of the projects, and they will concatenate the non accessible files patterns of this
     * entry
     * with the non accessible files patterns of the project.
     * </p>
     * <p>
     * Since 3.5, if the library is a ZIP archive, the "Class-Path" clause (if any) in the "META-INF/MANIFEST.MF" is
     * read
     * and referenced ZIP archives are added to the {@link IJavaProject#getResolvedClasspath(boolean) resolved
     * classpath}.
     * </p>
     *
     * @param path the path to the library
     * @param sourceAttachmentPath the absolute path of the corresponding source archive or folder,
     * or <code>null</code> if none. Note, since 3.0, an empty path is allowed to denote no source attachment.
     * and will be automatically converted to <code>null</code>. Since 3.4, this path can also denote a path external
     * to the workspace.
     * @param sourceAttachmentRootPath the location of the root of the source files within the source archive or folder
     * or <code>null</code> if this location should be automatically detected.
     * @param accessRules the possibly empty list of access rules for this entry
     * @param extraAttributes the possibly empty list of extra attributes to persist with this entry
     * @param isExported indicates whether this entry is contributed to dependent
     * projects in addition to the output location
     * @return a new library classpath entry
     * @since 3.1
     */
    public static IClasspathEntry newLibraryEntry(IPath path, IPath sourceAttachmentPath,
        IPath sourceAttachmentRootPath, IAccessRule[] accessRules, IClasspathAttribute[] extraAttributes,
        boolean isExported) {

        if (path == null)
            throw new ClasspathEntry.AssertionFailedException("Library path cannot be null"); //$NON-NLS-1$
        if (accessRules == null || accessRules.length == 0) {
            accessRules = ClasspathEntry.NO_ACCESS_RULES;
        }
        if (extraAttributes == null || extraAttributes.length == 0) {
            extraAttributes = ClasspathEntry.NO_EXTRA_ATTRIBUTES;
        }
        boolean hasDotDot = ClasspathEntry.hasDotDot(path);
        if (!hasDotDot && !path.isAbsolute())
            throw new ClasspathEntry.AssertionFailedException("Path for IClasspathEntry must be absolute: " + path); //$NON-NLS-1$
        if (sourceAttachmentPath != null) {
            if (sourceAttachmentPath.isEmpty()) {
                sourceAttachmentPath = null; // treat empty path as none
            } else if (!sourceAttachmentPath.isAbsolute()) {
                throw new ClasspathEntry.AssertionFailedException("Source attachment path '" //$NON-NLS-1$
                    + sourceAttachmentPath + "' for IClasspathEntry must be absolute"); //$NON-NLS-1$
            }
        }
        return new ClasspathEntry(IPackageFragmentRoot.K_BINARY, IClasspathEntry.CPE_LIBRARY,
            hasDotDot ? path : JavaProject.createPackageFragementKey(path), ClasspathEntry.INCLUDE_ALL, // inclusion
                                                                                                        // patterns
            ClasspathEntry.EXCLUDE_NONE, // exclusion patterns
            sourceAttachmentPath, sourceAttachmentRootPath, null, // specific output folder
            isExported, accessRules, false, // no access rules to combine
            extraAttributes);
    }

    /**
     * Creates and returns a new non-exported classpath entry of kind <code>CPE_PROJECT</code>
     * for the project identified by the given absolute path.
     * <p>
     * This method is fully equivalent to calling
     * {@link #newProjectEntry(IPath, IAccessRule[], boolean, IClasspathAttribute[], boolean)
     * newProjectEntry(path, new IAccessRule[0], true, new IClasspathAttribute[0], false)}.
     * </p>
     *
     * @param path the absolute path of the binary archive
     * @return a new project classpath entry
     */
    public static IClasspathEntry newProjectEntry(IPath path) {
        return newProjectEntry(path, false);
    }

    /**
     * Creates and returns a new classpath entry of kind <code>CPE_PROJECT</code>
     * for the project identified by the given absolute path.
     * <p>
     * This method is fully equivalent to calling
     * {@link #newProjectEntry(IPath, IAccessRule[], boolean, IClasspathAttribute[], boolean)
     * newProjectEntry(path, new IAccessRule[0], true, new IClasspathAttribute[0], isExported)}.
     * </p>
     *
     * @param path the absolute path of the prerequisite project
     * @param isExported indicates whether this entry is contributed to dependent
     * projects in addition to the output location
     * @return a new project classpath entry
     * @since 2.0
     */
    public static IClasspathEntry newProjectEntry(IPath path, boolean isExported) {

        if (!path.isAbsolute())
            throw new ClasspathEntry.AssertionFailedException("Path for IClasspathEntry must be absolute"); //$NON-NLS-1$

        return newProjectEntry(path, ClasspathEntry.NO_ACCESS_RULES, true, ClasspathEntry.NO_EXTRA_ATTRIBUTES,
            isExported);
    }

    /**
     * Creates and returns a new classpath entry of kind <code>CPE_PROJECT</code>
     * for the project identified by the given absolute path.
     * <p>
     * A project entry is used to denote a prerequisite project on a classpath.
     * The referenced project will be contributed as a whole, either as sources (in the Java Model, it
     * contributes all its package fragment roots) or as binaries (when building, it contributes its
     * whole output location).
     * </p>
     * <p>
     * A project reference allows to indirect through another project, independently from its internal layout.
     * </p><p>
     * The prerequisite project is referred to using an absolute path relative to the workspace root.
     * </p>
     * <p>
     * The access rules determine the set of accessible class files
     * in the project. If the list of access rules is empty then all files
     * in this project are accessible.
     * See {@link IAccessRule} for a detailed description of access rules.
     * </p>
     * <p>
     * The <code>combineAccessRules</code> flag indicates whether access rules of one (or more)
     * exported entry of the project should be combined with the given access rules. If they should
     * be combined, the given access rules are considered first, then the entry's access rules are
     * considered.
     * </p>
     * <p>
     * The <code>extraAttributes</code> list contains name/value pairs that must be persisted with
     * this entry. If no extra attributes are provided, an empty array must be passed in.<br>
     * Note that this list should not contain any duplicate name.
     * </p>
     * <p>
     * The <code>isExported</code> flag indicates whether this entry is contributed to dependent
     * projects. If not exported, dependent projects will not see any of the classes from this entry.
     * If exported, dependent projects will concatenate the accessible files patterns of this entry with the
     * accessible files patterns of the projects, and they will concatenate the non accessible files patterns of this
     * entry
     * with the non accessible files patterns of the project.
     * </p>
     *
     * @param path the absolute path of the prerequisite project
     * @param accessRules the possibly empty list of access rules for this entry
     * @param combineAccessRules whether the access rules of the project's exported entries should be combined with the
     * given access rules
     * @param extraAttributes the possibly empty list of extra attributes to persist with this entry
     * @param isExported indicates whether this entry is contributed to dependent
     * projects in addition to the output location
     * @return a new project classpath entry
     * @since 3.1
     */
    public static IClasspathEntry newProjectEntry(IPath path, IAccessRule[] accessRules, boolean combineAccessRules,
        IClasspathAttribute[] extraAttributes, boolean isExported) {

        if (!path.isAbsolute())
            throw new ClasspathEntry.AssertionFailedException("Path for IClasspathEntry must be absolute"); //$NON-NLS-1$
        if (accessRules == null || accessRules.length == 0) {
            accessRules = ClasspathEntry.NO_ACCESS_RULES;
        }
        if (extraAttributes == null || extraAttributes.length == 0) {
            extraAttributes = ClasspathEntry.NO_EXTRA_ATTRIBUTES;
        }
        return new ClasspathEntry(IPackageFragmentRoot.K_SOURCE, IClasspathEntry.CPE_PROJECT, path,
            ClasspathEntry.INCLUDE_ALL, // inclusion patterns
            ClasspathEntry.EXCLUDE_NONE, // exclusion patterns
            null, // source attachment
            null, // source attachment root
            null, // specific output folder
            isExported, accessRules, combineAccessRules, extraAttributes);
    }

    /**
     * Returns a new empty region.
     *
     * @return a new empty region
     */
    public static IRegion newRegion() {
        return new Region();
    }

    /**
     * Creates and returns a new classpath entry of kind <code>CPE_SOURCE</code>
     * for all files in the project's source folder identified by the given
     * absolute workspace-relative path.
     * <p>
     * The convenience method is fully equivalent to:
     * </p>
     * 
     * <pre>
     * newSourceEntry(path, new IPath[] { }, new IPath[] { }, null);
     * </pre>
     *
     * @param path the absolute workspace-relative path of a source folder
     * @return a new source classpath entry
     * @see #newSourceEntry(IPath, IPath[], IPath[], IPath)
     */
    public static IClasspathEntry newSourceEntry(IPath path) {

        return newSourceEntry(path, ClasspathEntry.INCLUDE_ALL, ClasspathEntry.EXCLUDE_NONE,
            null /* output location */);
    }

    /**
     * Creates and returns a new classpath entry of kind <code>CPE_SOURCE</code>
     * for the project's source folder identified by the given absolute
     * workspace-relative path but excluding all source files with paths
     * matching any of the given patterns, and associated with a specific output location
     * (that is, ".class" files are not going to the project default output location).
     * <p>
     * The convenience method is fully equivalent to:
     * </p>
     * 
     * <pre>
     * newSourceEntry(path, new IPath[] { }, exclusionPatterns, specificOutputLocation, new IClasspathAttribute[] { });
     * </pre>
     *
     * @param path the absolute workspace-relative path of a source folder
     * @param inclusionPatterns the possibly empty list of inclusion patterns
     * represented as relative paths
     * @param exclusionPatterns the possibly empty list of exclusion patterns
     * represented as relative paths
     * @param specificOutputLocation the specific output location for this source entry (<code>null</code> if using
     * project default output location)
     * @return a new source classpath entry
     * @see #newSourceEntry(IPath, IPath[], IPath[], IPath, IClasspathAttribute[])
     * @since 3.0
     */
    public static IClasspathEntry newSourceEntry(IPath path, IPath[] inclusionPatterns, IPath[] exclusionPatterns,
        IPath specificOutputLocation) {
        return newSourceEntry(path, inclusionPatterns, exclusionPatterns, specificOutputLocation,
            ClasspathEntry.NO_EXTRA_ATTRIBUTES);
    }

    /**
     * Creates and returns a new classpath entry of kind <code>CPE_SOURCE</code>
     * for the project's source folder identified by the given absolute
     * workspace-relative path using the given inclusion and exclusion patterns
     * to determine which source files are included, and the given output path
     * to control the output location of generated files.
     * <p>
     * The source folder is referred to using an absolute path relative to the
     * workspace root, e.g. <code>/Project/src</code>. A project's source
     * folders are located with that project. That is, a source classpath
     * entry specifying the path <code>/P1/src</code> is only usable for
     * project <code>P1</code>.
     * </p>
     * <p>
     * The inclusion patterns determines the initial set of source files that
     * are to be included; the exclusion patterns are then used to reduce this
     * set. When no inclusion patterns are specified, the initial file set
     * includes all relevant files in the resource tree rooted at the source
     * entry's path. On the other hand, specifying one or more inclusion
     * patterns means that all <b>and only</b> files matching at least one of
     * the specified patterns are to be included. If exclusion patterns are
     * specified, the initial set of files is then reduced by eliminating files
     * matched by at least one of the exclusion patterns. Inclusion and
     * exclusion patterns look like relative file paths with wildcards and are
     * interpreted relative to the source entry's path. File patterns are
     * case-sensitive can contain '**', '*' or '?' wildcards (see
     * {@link IClasspathEntry#getExclusionPatterns()} for the full description
     * of their syntax and semantics). The resulting set of files are included
     * in the corresponding package fragment root; all package fragments within
     * the root will have children of type <code>ICompilationUnit</code>.
     * </p>
     * <p>
     * For example, if the source folder path is
     * <code>/Project/src</code>, there are no inclusion filters, and the
     * exclusion pattern is
     * <code>com/xyz/tests/&#42;&#42;</code>, then source files
     * like <code>/Project/src/com/xyz/Foo.java</code>
     * and <code>/Project/src/com/xyz/utils/Bar.java</code> would be included,
     * whereas <code>/Project/src/com/xyz/tests/T1.java</code>
     * and <code>/Project/src/com/xyz/tests/quick/T2.java</code> would be
     * excluded.
     * </p>
     * <p>
     * Additionally, a source entry can be associated with a specific output location.
     * By doing so, the Java builder will ensure that the generated ".class" files will
     * be issued inside this output location, as opposed to be generated into the
     * project default output location (when output location is <code>null</code>).
     * Note that multiple source entries may target the same output location.
     * The output location is referred to using an absolute path relative to the
     * workspace root, e.g. <code>"/Project/bin"</code>, it must be located inside
     * the same project as the source folder.
     * </p>
     * <p>
     * Also note that all sources/binaries inside a project are contributed as
     * a whole through a project entry
     * (see <code>JavaCore.newProjectEntry</code>). Particular source entries
     * cannot be selectively exported.
     * </p>
     * <p>
     * The <code>extraAttributes</code> list contains name/value pairs that must be persisted with
     * this entry. If no extra attributes are provided, an empty array must be passed in.<br>
     * Note that this list should not contain any duplicate name.
     * </p>
     *
     * @param path the absolute workspace-relative path of a source folder
     * @param inclusionPatterns the possibly empty list of inclusion patterns
     * represented as relative paths
     * @param exclusionPatterns the possibly empty list of exclusion patterns
     * represented as relative paths
     * @param specificOutputLocation the specific output location for this source entry (<code>null</code> if using
     * project default ouput location)
     * @param extraAttributes the possibly empty list of extra attributes to persist with this entry
     * @return a new source classpath entry with the given exclusion patterns
     * @see IClasspathEntry#getInclusionPatterns()
     * @see IClasspathEntry#getExclusionPatterns()
     * @see IClasspathEntry#getOutputLocation()
     * @since 3.1
     */
    public static IClasspathEntry newSourceEntry(IPath path, IPath[] inclusionPatterns, IPath[] exclusionPatterns,
        IPath specificOutputLocation, IClasspathAttribute[] extraAttributes) {

        if (path == null)
            throw new ClasspathEntry.AssertionFailedException("Source path cannot be null"); //$NON-NLS-1$
        if (!path.isAbsolute())
            throw new ClasspathEntry.AssertionFailedException("Path for IClasspathEntry must be absolute"); //$NON-NLS-1$
        if (exclusionPatterns == null) {
            exclusionPatterns = ClasspathEntry.EXCLUDE_NONE;
        }
        if (inclusionPatterns == null) {
            inclusionPatterns = ClasspathEntry.INCLUDE_ALL;
        }
        if (extraAttributes == null) {
            extraAttributes = ClasspathEntry.NO_EXTRA_ATTRIBUTES;
        }
        return new ClasspathEntry(IPackageFragmentRoot.K_SOURCE, IClasspathEntry.CPE_SOURCE, path, inclusionPatterns,
            exclusionPatterns, null, // source attachment
            null, // source attachment root
            specificOutputLocation, // custom output location
            false, null, false, // no access rules to combine
            extraAttributes);
    }

    /**
     * Creates and returns a new classpath entry of kind <code>CPE_VARIABLE</code>
     * for the given path. The first segment of the path is the name of a classpath variable.
     * The trailing segments of the path will be appended to resolved variable path.
     * <p>
     * A variable entry allows to express indirect references on a classpath to other projects or libraries,
     * depending on what the classpath variable is referring.
     * </p>
     * <p>
     * It is possible to register an automatic initializer (<code>ClasspathVariableInitializer</code>),
     * which will be invoked through the extension point
     * "com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.classpathVariableInitializer".
     * After resolution, a classpath variable entry may either correspond to a project or a library entry.
     * </p>
     * <p>
     * e.g. Here are some examples of variable path usage
     * </p>
     * <ul>
     * <li> "JDTCORE" where variable <code>JDTCORE</code> is
     * bound to "c:/jars/jdtcore.jar". The resolved classpath entry is denoting the library "c:\jars\jdtcore.jar"</li>
     * <li> "JDTCORE" where variable <code>JDTCORE</code> is
     * bound to "/Project_JDTCORE". The resolved classpath entry is denoting the project "/Project_JDTCORE"</li>
     * <li> "PLUGINS/com.example/example.jar" where variable <code>PLUGINS</code>
     * is bound to "c:/eclipse/plugins". The resolved classpath entry is denoting the library
     * "c:\eclipse\plugins\com.example\example.jar"</li>
     * </ul>
     * <p>
     * The access rules determine the set of accessible class files
     * in the project or library. If the list of access rules is empty then all files
     * in this project or library are accessible.
     * See {@link IAccessRule} for a detailed description of access rules.
     * </p>
     * <p>
     * The <code>extraAttributes</code> list contains name/value pairs that must be persisted with
     * this entry. If no extra attributes are provided, an empty array must be passed in.<br>
     * Note that this list should not contain any duplicate name.
     * </p>
     * <p>
     * The <code>isExported</code> flag indicates whether this entry is contributed to dependent
     * projects. If not exported, dependent projects will not see any of the classes from this entry.
     * If exported, dependent projects will concatenate the accessible files patterns of this entry with the
     * accessible files patterns of the projects, and they will concatenate the non accessible files patterns of this
     * entry
     * with the non accessible files patterns of the project.
     * </p>
     * <p>
     * Note that this operation does not attempt to validate classpath variables
     * or access the resources at the given paths.
     * </p>
     *
     * @param variablePath the path of the binary archive; first segment is the
     * name of a classpath variable
     * @param variableSourceAttachmentPath the path of the corresponding source archive,
     * or <code>null</code> if none; if present, the first segment is the
     * name of a classpath variable (not necessarily the same variable
     * as the one that begins <code>variablePath</code>)
     * @param variableSourceAttachmentRootPath the location of the root of the source files within the source archive
     * or <code>null</code> if <code>variableSourceAttachmentPath</code> is also <code>null</code>
     * @param accessRules the possibly empty list of access rules for this entry
     * @param extraAttributes the possibly empty list of extra attributes to persist with this entry
     * @param isExported indicates whether this entry is contributed to dependent
     * projects in addition to the output location
     * @return a new variable classpath entry
     * @since 3.1
     */
    public static IClasspathEntry newVariableEntry(IPath variablePath, IPath variableSourceAttachmentPath,
        IPath variableSourceAttachmentRootPath, IAccessRule[] accessRules, IClasspathAttribute[] extraAttributes,
        boolean isExported) {

        if (variablePath == null)
            throw new ClasspathEntry.AssertionFailedException("Variable path cannot be null"); //$NON-NLS-1$
        if (variablePath.segmentCount() < 1) {
            throw new ClasspathEntry.AssertionFailedException("Illegal classpath variable path: '" //$NON-NLS-1$
                + variablePath.makeRelative().toString() + "', must have at least one segment"); //$NON-NLS-1$
        }
        if (accessRules == null || accessRules.length == 0) {
            accessRules = ClasspathEntry.NO_ACCESS_RULES;
        }
        if (extraAttributes == null || extraAttributes.length == 0) {
            extraAttributes = ClasspathEntry.NO_EXTRA_ATTRIBUTES;
        }

        return new ClasspathEntry(IPackageFragmentRoot.K_SOURCE, IClasspathEntry.CPE_VARIABLE, variablePath,
            ClasspathEntry.INCLUDE_ALL, // inclusion patterns
            ClasspathEntry.EXCLUDE_NONE, // exclusion patterns
            variableSourceAttachmentPath, // source attachment
            variableSourceAttachmentRootPath, // source attachment root
            null, // specific output folder
            isExported, accessRules, false, // no access rules to combine
            extraAttributes);
    }

    /**
     * Returns an array of classpath entries that are referenced directly or indirectly
     * by a given classpath entry. For the entry kind {@link IClasspathEntry#CPE_LIBRARY},
     * the method returns the libraries that are included in the Class-Path section of
     * the MANIFEST.MF file. If a referenced JAR file has further references to other library
     * entries, they are processed recursively and added to the list. For entry kinds other
     * than {@link IClasspathEntry#CPE_LIBRARY}, this method returns an empty array.
     * <p>
     * When a non-null project is passed, any additional attributes that may have been stored
     * previously in the project's .classpath file are retrieved and populated in the
     * corresponding referenced entry. If the project is <code>null</code>, the raw referenced
     * entries are returned without any persisted attributes.
     * </p>
     *
     * @param libraryEntry the library entry whose referenced entries are sought
     * @param project project where the persisted referenced entries to be retrieved from. If <code>null</code>
     * persisted attributes are not attempted to be retrieved.
     * @return an array of classpath entries that are referenced directly or indirectly by the given entry.
     * If not applicable, returns an empty array.
     * @since 3.6
     */
    public static IClasspathEntry[] getReferencedClasspathEntries(IClasspathEntry libraryEntry, IJavaProject project) {
        JavaModelManager manager = JavaModelManager.getJavaModelManager();
        return manager.getReferencedClasspathEntries(libraryEntry, project);
    }

    /**
     * @since 3.37
     */
    @FunctionalInterface
    public interface JavaCallable<V, E extends Exception> {
        /**
         * Computes a value or throws an exception.
         *
         * @return the result
         * @throws E the Exception of given type
         */
        V call() throws E;
    }

    /**
     * Sets the default compiler options inside the given options map according
     * to the given compliance.
     *
     * <p>The given compliance must be one of those supported by the compiler,
     * that is one of the acceptable values for option {@link #COMPILER_COMPLIANCE}.</p>
     *
     * <p>The list of modified options is currently:</p>
     * <ul>
     * <li>{@link #COMPILER_COMPLIANCE}</li>
     * <li>{@link #COMPILER_SOURCE}</li>
     * <li>{@link #COMPILER_CODEGEN_TARGET_PLATFORM}</li>
     * <li>{@link #COMPILER_PB_ASSERT_IDENTIFIER}</li>
     * <li>{@link #COMPILER_PB_ENUM_IDENTIFIER}</li>
     * <li>{@link #COMPILER_CODEGEN_INLINE_JSR_BYTECODE} for compliance levels 1.5 and greater</li>
     * <li>{@link #COMPILER_PB_ENABLE_PREVIEW_FEATURES} for compliance levels 11 and greater</li>
     * <li>{@link #COMPILER_PB_REPORT_PREVIEW_FEATURES} for compliance levels 11 and greater</li>
     * </ul>
     *
     * <p>If the given compliance is unknown, the given map is unmodified.</p>
     *
     * @param compliance the given {@link #COMPILER_COMPLIANCE compliance}
     * @param options the given options map
     * @since 3.3
     */
    public static void setComplianceOptions(String compliance, Map options) {
        long jdkLevel = CompilerOptions.versionToJdkLevel(compliance);
        int major = (int) (jdkLevel >>> 16);
        switch (major) {
            case ClassFileConstants.MAJOR_VERSION_1_8:
                options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
                options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
                options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
                options.put(JavaCore.COMPILER_PB_ASSERT_IDENTIFIER, JavaCore.ERROR);
                options.put(JavaCore.COMPILER_PB_ENUM_IDENTIFIER, JavaCore.ERROR);
                break;

            case ClassFileConstants.MAJOR_VERSION_9:
                options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_9);
                options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_9);
                options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_9);
                options.put(JavaCore.COMPILER_PB_ASSERT_IDENTIFIER, JavaCore.ERROR);
                options.put(JavaCore.COMPILER_PB_ENUM_IDENTIFIER, JavaCore.ERROR);
                options.put(JavaCore.COMPILER_RELEASE, JavaCore.ENABLED);
                break;

            case ClassFileConstants.MAJOR_VERSION_10:
                options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_10);
                options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_10);
                options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_10);
                options.put(JavaCore.COMPILER_PB_ASSERT_IDENTIFIER, JavaCore.ERROR);
                options.put(JavaCore.COMPILER_PB_ENUM_IDENTIFIER, JavaCore.ERROR);
                options.put(JavaCore.COMPILER_RELEASE, JavaCore.ENABLED);
                break;

            default:
                if (major > ClassFileConstants.MAJOR_VERSION_10) {
                    String version = CompilerOptions.versionFromJdkLevel(jdkLevel);
                    options.put(JavaCore.COMPILER_COMPLIANCE, version);
                    options.put(JavaCore.COMPILER_SOURCE, version);
                    options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, version);
                    options.put(JavaCore.COMPILER_PB_ASSERT_IDENTIFIER, JavaCore.ERROR);
                    options.put(JavaCore.COMPILER_PB_ENUM_IDENTIFIER, JavaCore.ERROR);
                    options.put(JavaCore.COMPILER_RELEASE, JavaCore.ENABLED);
                    options.put(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
                    options.put(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.WARNING);
                }
                break;
        }
    }

    /**
     * Returns the latest version of Java supported by the Java Model. This is usually the last entry
     * from {@link JavaCore#getAllVersions()}.
     *
     * @since 3.16
     * @return the latest Java version support by Java Model
     */
    public static String latestSupportedJavaVersion() {
        return allVersions.get(allVersions.size() - 1);
    }

    /**
     * Compares two given versions of the Java platform. The versions being compared must both be
     * one of the supported values mentioned in
     * {@link #COMPILER_CODEGEN_TARGET_PLATFORM COMPILER_CODEGEN_TARGET_PLATFORM},
     * both values from {@link #COMPILER_COMPLIANCE}, or both values from {@link #COMPILER_SOURCE}.
     *
     * @param first first version to be compared
     * @param second second version to be compared
     * @return the value {@code 0} if both versions are the same;
     * a value less than {@code 0} if <code>first</code> is smaller than <code>second</code>; and
     * a value greater than {@code 0} if <code>first</code> is higher than <code>second</code>
     * @since 3.12
     */
    public static int compareJavaVersions(String first, String second) {
        return Long.compare(CompilerOptions.versionToJdkLevel(first), CompilerOptions.versionToJdkLevel(second));
    }
}
