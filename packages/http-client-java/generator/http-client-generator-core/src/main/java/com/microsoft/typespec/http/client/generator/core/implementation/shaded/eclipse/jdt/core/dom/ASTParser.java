/*******************************************************************************
 * Copyright (c) 2004, 2025 IBM Corporation and others.
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
 *     Stephan Herrmann - Contribution for
 *								Bug 458577 - IClassFile.getWorkingCopy() may lead to NPE in BecomeWorkingCopyOperation
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.dom;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.SubMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.*;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.CategorizedProblem;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.CharOperation;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.batch.Main;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.IBinaryType;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.parser.RecoveryScanner;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.parser.RecoveryScannerData;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.parser.Scanner;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.util.SuffixConstants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.*;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.dom.ICompilationUnitResolver;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.dom.util.DOMASTUtil;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.CodeSnippetParsingUtil;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.RecordedParsingInformation;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.Util;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Java language parser for creating abstract syntax trees (ASTs).
 * <p>
 * Example: Create basic AST from source string
 * 
 * <pre>
 * char[] source = ...;
 * ASTParser parser = ASTParser.newParser(AST.JLS3);  // handles JDK 1.0, 1.1, 1.2, 1.3, 1.4, 1.5, 1.6
 * parser.setSource(source);
 * // In order to parse 1.5 code, some compiler options need to be set to 1.5
 * Map options = JavaCore.getOptions();
 * JavaCore.setComplianceOptions(JavaCore.VERSION_1_5, options);
 * parser.setCompilerOptions(options);
 * CompilationUnit result = (CompilationUnit) parser.createAST(null);
 * </pre>
 * 
 * <p>
 * Once a configured parser instance has been used to create an AST,
 * the settings are automatically reset to their defaults,
 * ready for the parser instance to be reused.
 * </p>
 * <p>
 * There are a number of configurable features:
 * <ul>
 * <li>Source string from {@link #setSource(char[]) char[]},
 * {@link #setSource(ICompilationUnit) ICompilationUnit},
 * or {@link #setSource(IClassFile) IClassFile}, and limited
 * to a specified {@linkplain #setSourceRange(int,int) subrange}.</li>
 * <li>Whether {@linkplain #setResolveBindings(boolean) bindings} will be created.</li>
 * <li>Which {@linkplain #setWorkingCopyOwner(WorkingCopyOwner)
 * working copy owner} to use when resolving bindings.</li>
 * <li>A hypothetical {@linkplain #setUnitName(String) compilation unit file name}
 * and {@linkplain #setProject(IJavaProject) Java project}
 * for locating a raw source string in the Java model (when
 * resolving bindings)</li>
 * <li>Which {@linkplain #setCompilerOptions(Map) compiler options}
 * to use. This is especially important to use if the parsing/scanning of the source code requires a
 * different version than the default of the workspace. For example, the workspace defaults are 1.4 and
 * you want to create an AST for a source code that is using 1.5 constructs.</li>
 * <li>Whether to parse just {@linkplain #setKind(int) an expression, statements,
 * or body declarations} rather than an entire compilation unit.</li>
 * </ul>
 *
 * @since 3.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
@SuppressWarnings({ "rawtypes" })
public class ASTParser {

    /**
     * Kind constant used to request that the source be parsed
     * as a single expression.
     */
    public static final int K_EXPRESSION = 0x01;

    /**
     * Kind constant used to request that the source be parsed
     * as a sequence of statements.
     */
    public static final int K_STATEMENTS = 0x02;

    /**
     * Kind constant used to request that the source be parsed
     * as a sequence of class body declarations.
     */
    public static final int K_CLASS_BODY_DECLARATIONS = 0x04;

    /**
     * Kind constant used to request that the source be parsed
     * as a compilation unit.
     */
    public static final int K_COMPILATION_UNIT = 0x08;

    /**
     * Creates a new object for creating a Java abstract syntax tree
     * (AST) following the specified set of API rules.
     *
     * @param level the API level; one of the <code>.JLS*</code> level constants declared on {@link AST} or
     * {@link AST#getJLSLatest}
     * @return new ASTParser instance
     */
    public static ASTParser newParser(int level) {
        return new ASTParser(level);
    }

    /**
     * Level of AST API desired.
     */
    private final int apiLevel;

    /**
     * Kind of parse requested. Defaults to an entire compilation unit.
     */
    private int astKind;

    /**
     * Compiler options. Defaults to JavaCore.getOptions().
     */
    private Map<String, String> compilerOptions;

    /**
     * The focal point for a partial AST request.
     * Only used when <code>partial</code> is <code>true</code>.
     */
    private int focalPointPosition;

    /**
     * Source string.
     */
    private char[] rawSource = null;

    /**
     * Java model class file or compilation unit supplying the source.
     */
    private ITypeRoot typeRoot = null;

    /**
     * Character-based offset into the source string where parsing is to
     * begin. Defaults to 0.
     */
    private int sourceOffset = 0;

    /**
     * Character-based length limit, or -1 if unlimited.
     * All characters in the source string between <code>offset</code>
     * and <code>offset+length-1</code> inclusive are parsed. Defaults to -1,
     * which means the rest of the source string.
     */
    private int sourceLength = -1;

    /**
     * Working copy owner. Defaults to primary owner.
     */
    private WorkingCopyOwner workingCopyOwner = DefaultWorkingCopyOwner.PRIMARY;

    /**
     * Java project used to resolve names, or <code>null</code> if none.
     * Defaults to none.
     */
    private IJavaProject project = null;

    /**
     * Name of the compilation unit for resolving bindings, or
     * <code>null</code> if none. Defaults to none.
     */
    private String unitName = null;

    /**
     * Classpath entries to use to resolve bindings when no java project are available.
     */
    private String[] classpaths;

    /**
     * Sourcepath entries to use to resolve bindings when no java project are available.
     */
    private String[] sourcepaths;

    /**
     * Encoding of the given sourcepaths entries.
     */
    private String[] sourcepathsEncodings;

    /**
     * Bits used to set the different values from CompilationUnitResolver values.
     */
    private int bits;

    private final ICompilationUnitResolver unitResolver;

    /**
     * Creates a new AST parser for the given API level.
     * <p>
     * N.B. This constructor is package-private.
     * </p>
     *
     * @param level the API level; one of the <code>JLS*</code> level constants
     * declared on {@link AST} or {@link AST#getJLSLatest()}
     */
    ASTParser(int level) {
        DOMASTUtil.checkASTLevel(level);
        this.apiLevel = level;
        this.unitResolver = CompilationUnitResolverDiscovery.getInstance();
        initializeDefaults();
    }

    private List<Classpath> getClasspath() throws IllegalStateException {
        Main main = new Main(new PrintWriter(System.out), new PrintWriter(System.err), false/* systemExit */,
            null/* options */, null/* progress */);
        ArrayList<Classpath> allClasspaths = new ArrayList<>();
        try {
            if ((this.bits & CompilationUnitResolver.INCLUDE_RUNNING_VM_BOOTCLASSPATH) != 0) {
                com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.util.Util
                    .collectRunningVMBootclasspath(allClasspaths);
            }
            if (this.sourcepaths != null) {
                for (int i = 0, max = this.sourcepaths.length; i < max; i++) {
                    String sourcePath = this.sourcepaths[i];
                    if (sourcePath != null) {
                        String encoding = this.sourcepathsEncodings == null ? null : this.sourcepathsEncodings[i];
                        main.processPathEntries(Main.DEFAULT_SIZE_CLASSPATH, allClasspaths, sourcePath, encoding, true,
                            false);
                    }
                }
            }
            if (this.classpaths != null) {
                for (String classpath : this.classpaths) {
                    main.processPathEntries(Main.DEFAULT_SIZE_CLASSPATH, allClasspaths, classpath, null, false, false);
                }
            }
            ArrayList pendingErrors = main.pendingErrors;
            if (pendingErrors != null && pendingErrors.size() != 0) {
                throw new IllegalStateException("invalid environment settings"); //$NON-NLS-1$
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("invalid environment settings", e); //$NON-NLS-1$
        }
        if ((this.bits & CompilationUnitResolver.RESOLVE_BINDING) != 0) {
            checkForSystemLibrary(allClasspaths);
        }
        return allClasspaths;
    }

    private void checkForSystemLibrary(List<Classpath> allClasspaths) {
        if (!hasJavaNature()) {
            return;
        }
        boolean hasSystemLibrary = true; // default for 1.8 setting without a valid project
        boolean hasModule = false;
        Throwable exception = null;
        String compliance = this.compilerOptions.get(JavaCore.COMPILER_COMPLIANCE);
        if (CompilerOptions.versionToJdkLevel(compliance) >= ClassFileConstants.JDK9) {
            hasSystemLibrary = allClasspaths.stream().anyMatch(cp -> cp.getModule(TypeConstants.JAVA_DOT_BASE) != null);
            if (!hasSystemLibrary && this.project != null) {
                // not found in allClasspaths, try this.project instead:
                try {
                    // try module java.base:
                    for (IPackageFragmentRoot root : this.project.getAllPackageFragmentRoots()) {
                        IModuleDescription moduleDescription = root.getModuleDescription();
                        if (moduleDescription != null) {
                            hasModule = true;
                            if (moduleDescription.getElementName()
                                .equals(String.valueOf(TypeConstants.JAVA_DOT_BASE))) {
                                hasSystemLibrary = true;
                                break;
                            }
                        }
                    }
                } catch (JavaModelException e) {
                    exception = e;
                }
                if (!hasModule) {
                    try {
                        // if no modules try class java.lang.Object:
                        hasSystemLibrary
                            = this.project.findType(String.valueOf(TypeConstants.CharArray_JAVA_LANG_OBJECT)) != null;
                    } catch (JavaModelException e) {
                        exception = e;
                    }
                }
            }
            if (!hasSystemLibrary)
                throw new IllegalStateException("Missing system library", exception); //$NON-NLS-1$
        }
    }

    /**
     * Sets all the setting to their default values.
     */
    private void initializeDefaults() {
        this.astKind = K_COMPILATION_UNIT;
        this.rawSource = null;
        this.typeRoot = null;
        this.bits = 0;
        this.sourceLength = -1;
        this.sourceOffset = 0;
        this.workingCopyOwner = DefaultWorkingCopyOwner.PRIMARY;
        this.unitName = null;
        this.project = null;
        this.classpaths = null;
        this.sourcepaths = null;
        this.sourcepathsEncodings = null;
        Map<String, String> options = JavaCore.getOptions();
        options.remove(JavaCore.COMPILER_TASK_TAGS); // no need to parse task tags
        this.compilerOptions = options;
    }

    /**
     * Requests that the compiler should perform bindings recovery.
     * When bindings recovery is enabled the compiler returns incomplete bindings.
     * <p>
     * Default to <code>false</code>.
     * </p>
     * <p>This should be set to true only if bindings are resolved. It has no effect if there is no binding
     * resolution.</p>
     *
     * @param enabled <code>true</code> if incomplete bindings are expected,
     * and <code>false</code> if only complete bindings are expected.
     *
     * @see IBinding#isRecovered()
     * @since 3.3
     */
    public void setBindingsRecovery(boolean enabled) {
        if (enabled) {
            this.bits |= CompilationUnitResolver.BINDING_RECOVERY;
        } else {
            this.bits &= ~CompilationUnitResolver.BINDING_RECOVERY;
        }
    }

    /**
     * Sets the compiler options to be used when parsing.
     * <p>
     * Note that {@link #setSource(IClassFile)},
     * {@link #setSource(ICompilationUnit)},
     * and {@link #setProject(IJavaProject)} reset the compiler options
     * based on the Java project. In other cases, compiler options default
     * to {@link JavaCore#getOptions()}. In either case, and especially
     * in the latter, the caller should carefully weight the consequences of
     * allowing compiler options to be defaulted as opposed to being
     * explicitly specified for the {@link ASTParser} instance.
     * For instance, there is a compiler option called "Source Compatibility Mode"
     * which determines which JDK level the source code is expected to meet.
     * If you specify "1.4", then "assert" is treated as a keyword and disallowed
     * as an identifier; if you specify "1.3", then "assert" is allowed as an
     * identifier. So this particular setting has a major bearing on what is
     * considered syntactically legal. By explicitly specifying the setting,
     * the client control exactly how the parser works. On the other hand,
     * allowing default settings means the parsing behaves like other JDT tools.
     * </p>
     *
     * @param options the table of options (key type: <code>String</code>;
     * value type: <code>String</code>), or <code>null</code>
     * to set it back to the default
     */
    public void setCompilerOptions(Map<String, String> options) {
        if (options == null) {
            options = JavaCore.getOptions();
        } else {
            // copy client's options so as to not do any side effect on them
            options = new HashMap<>(options);
        }
        // https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2179
        // options.remove(JavaCore.COMPILER_TASK_TAGS); // no need to parse task tags
        this.compilerOptions = options;
    }

    /**
     * Requests that the compiler should provide binding information for
     * the AST nodes it creates.
     * <p>
     * Defaults to <code>false</code> (no bindings).
     * </p>
     * <p>
     * If {@link #setResolveBindings(boolean) setResolveBindings(true)}, the various names
     * and types appearing in the AST can be resolved to "bindings"
     * by calling the <code>resolveBinding</code> methods. These bindings
     * draw connections between the different parts of a program, and
     * generally afford a more powerful vantage point for clients who wish to
     * analyze a program's structure more deeply. These bindings come at a
     * considerable cost in both time and space, however, and should not be
     * requested frivolously. The additional space is not reclaimed until the
     * AST, all its nodes, and all its bindings become garbage. So it is very
     * important to not retain any of these objects longer than absolutely
     * necessary. Bindings are resolved at the time the AST is created. Subsequent
     * modifications to the AST do not affect the bindings returned by
     * <code>resolveBinding</code> methods in any way; these methods return the
     * same binding as before the AST was modified (including modifications
     * that rearrange subtrees by reparenting nodes).
     * If {@link #setResolveBindings(boolean) setResolveBindings(false)}, (the default), the analysis
     * does not go beyond parsing and building the tree, and all
     * <code>resolveBinding</code> methods return <code>null</code> from the outset.
     * </p>
     * <p>
     * When bindings are requested, instead of considering compilation units on disk only,
     * one can also supply a <code>WorkingCopyOwner</code>. Working copies owned
     * by this owner take precedence over the underlying compilation units when looking
     * up names and drawing the connections.
     * </p>
     * <p>Note that working copy owners are used only if the
     * <code>com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core</code>
     * bundle is initialized.</p>
     * <p>
     * Binding information is obtained from the Java model.
     * This means that the compilation unit must be located relative to the
     * Java model. This happens automatically when the source code comes from
     * either {@link #setSource(ICompilationUnit) setSource(ICompilationUnit)}
     * or {@link #setSource(IClassFile) setSource(IClassFile)}.
     * When source is supplied by {@link #setSource(char[]) setSource(char[])},
     * the location must be established explicitly by setting an environment using
     * and a unit name {@link #setUnitName(String)}.
     * Note that the compiler options that affect doc comment checking may also
     * affect whether any bindings are resolved for nodes within doc comments.
     * </p>
     *
     * @param enabled <code>true</code> if bindings are wanted,
     * and <code>false</code> if bindings are not of interest
     */
    public void setResolveBindings(boolean enabled) {
        if (enabled) {
            this.bits |= CompilationUnitResolver.RESOLVE_BINDING;
        } else {
            this.bits &= ~CompilationUnitResolver.RESOLVE_BINDING;
        }
    }

    /**
     * Sets the kind of constructs to be parsed from the source.
     * Defaults to an entire compilation unit.
     * <p>
     * When the parse is successful the result returned includes the ASTs for the
     * requested source:
     * <ul>
     * <li>{@link #K_COMPILATION_UNIT K_COMPILATION_UNIT}: The result node
     * is a {@link CompilationUnit}.</li>
     * <li>{@link #K_CLASS_BODY_DECLARATIONS K_CLASS_BODY_DECLARATIONS}: The result node
     * is a {@link TypeDeclaration} whose
     * {@link TypeDeclaration#bodyDeclarations() bodyDeclarations}
     * are the new trees. Other aspects of the type declaration are unspecified.</li>
     * <li>{@link #K_STATEMENTS K_STATEMENTS}: The result node is a
     * {@link Block Block} whose {@link Block#statements() statements}
     * are the new trees. Other aspects of the block are unspecified.</li>
     * <li>{@link #K_EXPRESSION K_EXPRESSION}: The result node is a subclass of
     * {@link Expression Expression}. Other aspects of the expression are unspecified.</li>
     * </ul>
     * <p>
     * The resulting AST node is rooted under (possibly contrived)
     * {@link CompilationUnit CompilationUnit} node, to allow the
     * client to retrieve the following pieces of information
     * available there:
     * </p>
     * <ul>
     * <li>{@linkplain CompilationUnit#getLineNumber(int) Line number map}. Line
     * numbers start at 1 and only cover the subrange scanned
     * (<code>source[offset]</code> through <code>source[offset+length-1]</code>).</li>
     * <li>{@linkplain CompilationUnit#getMessages() Compiler messages}
     * and {@linkplain CompilationUnit#getProblems() detailed problem reports}.
     * Character positions are relative to the start of
     * <code>source</code>; line positions are for the subrange scanned.</li>
     * <li>{@linkplain CompilationUnit#getCommentList() Comment list}
     * for the subrange scanned.</li>
     * </ul>
     * <p>
     * The contrived nodes do not have source positions. Other aspects of the
     * {@link CompilationUnit CompilationUnit} node are unspecified, including
     * the exact arrangement of intervening nodes.
     * </p>
     * <p>
     * Lexical or syntax errors detected while parsing can result in
     * a result node being marked as {@link ASTNode#MALFORMED MALFORMED}.
     * In more severe failure cases where the parser is unable to
     * recognize the input, this method returns
     * a {@link CompilationUnit CompilationUnit} node with at least the
     * compiler messages.
     * </p>
     * <p>Each node in the subtree (other than the contrived nodes)
     * carries source range(s) information relating back
     * to positions in the given source (the given source itself
     * is not remembered with the AST).
     * The source range usually begins at the first character of the first token
     * corresponding to the node; leading whitespace and comments are <b>not</b>
     * included. The source range usually extends through the last character of
     * the last token corresponding to the node; trailing whitespace and
     * comments are <b>not</b> included. There are a handful of exceptions
     * (including the various body declarations); the
     * specification for these node type spells out the details.
     * Source ranges nest properly: the source range for a child is always
     * within the source range of its parent, and the source ranges of sibling
     * nodes never overlap.
     * </p>
     * <p>
     * Binding information is only computed when <code>kind</code> is
     * {@link #K_COMPILATION_UNIT}.
     * </p>
     *
     * @param kind the kind of construct to parse: one of
     * {@link #K_COMPILATION_UNIT},
     * {@link #K_CLASS_BODY_DECLARATIONS},
     * {@link #K_EXPRESSION},
     * {@link #K_STATEMENTS}
     */
    public void setKind(int kind) {
        if ((kind != K_COMPILATION_UNIT)
            && (kind != K_CLASS_BODY_DECLARATIONS)
            && (kind != K_EXPRESSION)
            && (kind != K_STATEMENTS)) {
            throw new IllegalArgumentException();
        }
        this.astKind = kind;
    }

    /**
     * Sets the source code to be parsed.
     *
     * <p>If this method is used, the user needs to specify compiler options explicitly using
     * {@link #setCompilerOptions(Map)} as 1.5 code will not be properly parsed without setting
     * the appropriate values for the compiler options: {@link JavaCore#COMPILER_SOURCE},
     * {@link JavaCore#COMPILER_CODEGEN_TARGET_PLATFORM}, and {@link JavaCore#COMPILER_COMPLIANCE}.</p>
     * <p>Otherwise the default values for the compiler options will be used to parse the given source.</p>
     *
     * @param source the source string to be parsed,
     * or <code>null</code> if none
     * @see JavaCore#setComplianceOptions(String, Map)
     */
    public void setSource(char[] source) {
        this.rawSource = source;
        // clear the type root
        this.typeRoot = null;
    }

    /**
     * Sets the source code to be parsed.
     *
     * <p>This method automatically sets the project (and compiler
     * options) based on the given compilation unit, in a manner
     * equivalent to {@link #setProject(IJavaProject) setProject(source.getJavaProject())}
     * and the custom compiler options supported by the compilation unit through
     * {@link ICompilationUnit#getCustomOptions() getCustomOptions()}.</p>
     *
     * @param source the Java model compilation unit whose source code
     * is to be parsed, or <code>null</code> if none
     */
    public void setSource(ICompilationUnit source) {
        setSource((ITypeRoot) source);
        if (source != null) {
            setCompilerOptions(source.getOptions(true));
        }
    }

    /**
     * Sets the source code to be parsed.
     *
     * <p>This method automatically sets the project (and compiler
     * options) based on the given compilation unit, in a manner
     * equivalent to {@link #setProject(IJavaProject) setProject(source.getJavaProject())}.</p>
     * <p>If the given class file has no source attachment, the creation of the
     * ast will fail with an {@link IllegalStateException}.</p>
     *
     * @param source the Java model class file whose corresponding source code
     * is to be parsed, or <code>null</code> if none
     */
    public void setSource(IClassFile source) {
        setSource((ITypeRoot) source);
    }

    /**
     * Sets the source code to be parsed.
     *
     * <p>This method automatically sets the project (and compiler
     * options) based on the given compilation unit of class file, in a manner
     * equivalent to {@link #setProject(IJavaProject) setProject(source.getJavaProject())}.</p>
     * <p>If the source is a class file without source attachment, the creation of the
     * ast will fail with an {@link IllegalStateException}.</p>
     *
     * @param source the Java model compilation unit or class file whose corresponding source code
     * is to be parsed, or <code>null</code> if none
     * @since 3.3
     */
    public void setSource(ITypeRoot source) {
        this.typeRoot = source;
        // clear the raw source
        this.rawSource = null;
        if (source != null) {
            this.project = source.getJavaProject();
            Map<String, String> options = this.project.getOptions(true);
            options.remove(JavaCore.COMPILER_TASK_TAGS); // no need to parse task tags
            this.compilerOptions = options;
        }
    }

    /**
     * Sets the source code to be parsed.
     *
     *
     * <p>This method automatically sets the project (and compiler
     * options) based on the given compilation unit of class file, in a manner
     * equivalent to {@link #setProject(IJavaProject) setProject(source.getJavaProject())}.</p>
     * <p>If the source is a class file without source attachment, the creation of the
     * ast will fail with an {@link IllegalStateException}.</p>
     *
     * <p>If this method is used, the user need not specify compiler options explicitly.
     * The @param astLevel will be used for setting the corresponding values for the compiler
     * options: {@link JavaCore#COMPILER_SOURCE}, {@link JavaCore#COMPILER_CODEGEN_TARGET_PLATFORM}
     * and {@link JavaCore#COMPILER_COMPLIANCE}.</p>
     *
     * @param source the Java model compilation unit or class file whose corresponding source code
     * is to be parsed, or <code>null</code> if none
     * @param astLevel the API level; one of the <code>JLS*</code> level constants
     * declared on {@link AST}
     * @since 3.27
     */
    public void setSource(ITypeRoot source, int astLevel) {
        this.typeRoot = source;
        // clear the raw source
        this.rawSource = null;
        if (source != null) {
            this.project = source.getJavaProject();
            Map<String, String> options = this.project.getOptions(true);
            options.remove(JavaCore.COMPILER_TASK_TAGS); // no need to parse task tags
            this.compilerOptions = options;
            String compliance = DOMASTUtil.getCompliance(astLevel);
            this.compilerOptions.put(JavaCore.COMPILER_COMPLIANCE, compliance);
            this.compilerOptions.put(JavaCore.COMPILER_SOURCE, compliance);
            this.compilerOptions.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, compliance);
        }
    }

    /**
     * Sets the subrange of the source code to be parsed.
     * By default, the entire source string will be parsed
     * (<code>offset</code> 0 and <code>length</code> -1).
     *
     * @param offset the index of the first character to parse
     * @param length the number of characters to parse, or -1 if
     * the remainder of the source string is to be parsed
     */
    public void setSourceRange(int offset, int length) {
        if (offset < 0 || length < -1) {
            throw new IllegalArgumentException();
        }
        this.sourceOffset = offset;
        this.sourceLength = length;
    }

    /**
     * Requests that the compiler should perform statements recovery.
     * When statements recovery is enabled the compiler tries to create statement nodes
     * from code containing syntax errors
     * <p>
     * Default to <code>false</code>.
     * </p>
     *
     * @param enabled <code>true</code> if statements containing syntax errors are wanted,
     * and <code>false</code> if these statements aren't wanted.
     *
     * @since 3.2
     */
    public void setStatementsRecovery(boolean enabled) {
        if (enabled) {
            this.bits |= CompilationUnitResolver.STATEMENT_RECOVERY;
        } else {
            this.bits &= ~CompilationUnitResolver.STATEMENT_RECOVERY;
        }
    }

    /**
     * Requests an abstract syntax tree without method bodies.
     *
     * <p>When ignore method bodies is enabled, all method bodies are discarded.
     * This has no impact on the binding resolution.</p>
     *
     * <p>This setting is not used when the kind used in {@link #setKind(int)} is either
     * {@link #K_EXPRESSION} or {@link #K_STATEMENTS}.</p>
     * 
     * @since 3.5.2
     */
    public void setIgnoreMethodBodies(boolean enabled) {
        if (enabled) {
            this.bits |= CompilationUnitResolver.IGNORE_METHOD_BODIES;
        } else {
            this.bits &= ~CompilationUnitResolver.IGNORE_METHOD_BODIES;
        }
    }

    /**
     * Sets the working copy owner used when resolving bindings, where
     * <code>null</code> means the primary owner. Defaults to the primary owner.
     *
     * @param owner the owner of working copies that take precedence over underlying
     * compilation units, or <code>null</code> if the primary owner should be used
     */
    public void setWorkingCopyOwner(WorkingCopyOwner owner) {
        if (owner == null) {
            this.workingCopyOwner = DefaultWorkingCopyOwner.PRIMARY;
        } else {
            this.workingCopyOwner = owner;
        }
    }

    /**
     * Sets the name of the compilation unit that would hypothetically contains the
     * source string.
     *
     * <p>This is used in conjunction with {@link #setSource(char[])}
     * and {@link #setProject(IJavaProject)} to locate the compilation unit relative to a Java project.
     * Defaults to none (<code>null</code>).</p>
     *
     * <p>
     * For compilation of a module-info.java file (since Java 9), the name of the compilation unit must be supplied.
     * Otherwise, module-info.java will be compiled as an ordinary Java file resulting in compilation errors.</p>
     *
     * <p>This name must represent the full path of the unit inside the given project. For example, if the source
     * declares a public class named "Foo" in a project "P" where the source folder is the project itself, the name
     * of the compilation unit must be "/P/Foo.java".
     * If the source declares a public class name "Bar" in a package "p1.p2" in a project "P" in a source folder "src",
     * the name of the compilation unit must be "/P/src/p1/p2/Bar.java".</p>
     *
     * @param unitName the name of the compilation unit that would contain the source
     * string, or <code>null</code> if none
     */
    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    /**
     * Sets the Java project used when resolving bindings.
     *
     * <p>This method automatically sets the compiler
     * options based on the given project:</p>
     * 
     * <pre>
     * setCompilerOptions(project.getOptions(true));
     * </pre>
     * 
     * <p>See {@link #setCompilerOptions(Map)} for a discussion of
     * the pros and cons of using these options vs specifying
     * compiler options explicitly.</p>
     * <p>This setting is used in conjunction with {@link #setSource(char[])}.
     * For the purposes of resolving bindings, types declared in the
     * source string will hide types by the same name available
     * through the classpath of the given project.</p>
     * <p>Defaults to none (<code>null</code>).</p>
     *
     * @param project the Java project used to resolve names, or
     * <code>null</code> if none
     */
    public void setProject(IJavaProject project) {
        this.project = project;
        if (project != null) {
            Map<String, String> options = project.getOptions(true);
            options.remove(JavaCore.COMPILER_TASK_TAGS); // no need to parse task tags
            this.compilerOptions = options;
        }
    }

    /**
     * Creates an abstract syntax tree.
     * <p>
     * A successful call to this method returns all settings to their
     * default values so the object is ready to be reused.
     * </p>
     * <p>For identifying a module-info.java file as a special file instead of an ordinary
     * Java file (Since Java 9), a call to this should be preceded by a call to
     * {@link #setUnitName(String)} that sets the unit name as module-info.java</p>
     *
     * @param monitor the progress monitor used to report progress and request cancellation,
     * or <code>null</code> if none
     * @return an AST node whose type depends on the kind of parse
     * requested, with a fallback to a <code>CompilationUnit</code>
     * in the case of severe parsing errors
     * @exception IllegalStateException if the settings provided
     * are insufficient, contradictory, or otherwise unsupported
     */
    public ASTNode createAST(IProgressMonitor monitor) {
        SubMonitor subMonitor = SubMonitor.convert(monitor, 1);
        ASTNode result = null;
        try {
            if (this.rawSource == null && this.typeRoot == null) {
                throw new IllegalStateException("source not specified"); //$NON-NLS-1$
            }
            result = internalCreateAST(subMonitor.split(1));
        } finally {
            // reset to defaults to allow reuse (and avoid leaking)
            initializeDefaults();
        }
        return result;
    }

    private static <K, V> Map<K, V> safeUnmodifiableMap(Map<? extends K, ? extends V> m) {
        return m == null ? null : Collections.unmodifiableMap(m);
    }

    private ASTNode internalCreateAST(IProgressMonitor monitor) {
        return JavaModelManager.cacheZipFiles(() -> internalCreateASTCached(monitor));
    }

    private ASTNode internalCreateASTCached(IProgressMonitor monitor) {
        boolean needToResolveBindings = (this.bits & CompilationUnitResolver.RESOLVE_BINDING) != 0;
        switch (this.astKind) {
            case K_CLASS_BODY_DECLARATIONS:
            case K_EXPRESSION:
            case K_STATEMENTS:
                if (this.rawSource == null) {
                    if (this.typeRoot != null) {
                        // get the source from the type root
                        if (this.typeRoot instanceof ICompilationUnit) {
                            com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit
                                = (com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.ICompilationUnit) this.typeRoot;
                            this.rawSource = sourceUnit.getContents();
                        } else if (this.typeRoot instanceof IClassFile) {
                            try {
                                String sourceString = this.typeRoot.getSource();
                                if (sourceString != null) {
                                    this.rawSource = sourceString.toCharArray();
                                }
                            } catch (JavaModelException e) {
                                // an error occured accessing the java element
                                CharSequence stackTrace
                                    = com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.util.Util
                                        .getStackTrace(e);
                                throw new IllegalStateException(stackTrace.toString());
                            }
                        }
                    }
                }
                if (this.rawSource != null) {
                    if (this.sourceOffset + this.sourceLength > this.rawSource.length) {
                        throw new IllegalStateException();
                    }
                    return internalCreateASTForKind();
                }
                break;

            case K_COMPILATION_UNIT:
                boolean useSearcher = false;
                com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit
                    = null;
                WorkingCopyOwner wcOwner = this.workingCopyOwner;
                if (this.typeRoot instanceof ClassFileWorkingCopy) {
                    // special case: class file mimics as compilation unit, but that would use a wrong file name
                    // below, so better unwrap now:
                    this.typeRoot = ((ClassFileWorkingCopy) this.typeRoot).classFile;
                }
                if (this.typeRoot instanceof ICompilationUnit) {
                    /*
                     * this.compilationUnitSource is an instance of org.eclipse.jdt.internal.core.CompilationUnit
                     * that implements
                     * both org.eclipse.jdt.core.ICompilationUnit and
                     * org.eclipse.jdt.internal.compiler.env.ICompilationUnit
                     */
                    sourceUnit
                        = (com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.ICompilationUnit) this.typeRoot;
                    /*
                     * use a BasicCompilation that caches the source instead of using the compilationUnitSource
                     * directly
                     * (if it is a working copy, the source can change between the parse and the AST convertion)
                     * (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=75632)
                     */
                    sourceUnit = new BasicCompilationUnit(sourceUnit.getContents(), sourceUnit.getPackageName(),
                        new String(sourceUnit.getFileName()), this.project);
                    wcOwner = ((ICompilationUnit) this.typeRoot).getOwner();
                } else if (this.typeRoot instanceof IClassFile) {
                    try {
                        String sourceString = this.typeRoot.getSource();
                        if (sourceString == null) {
                            throw new IllegalStateException();
                        }
                        PackageFragment packageFragment = (PackageFragment) this.typeRoot.getParent();
                        BinaryType type = (BinaryType) this.typeRoot.findPrimaryType();
                        String fileNameString = null;
                        if (type != null) {
                            IBinaryType binaryType = type.getElementInfo();
                            // file name is used to recreate the Java element, so it has to be the toplevel .class
                            // file name
                            char[] fileName = binaryType.getFileName();

                            int firstDollar = CharOperation.indexOf('$', fileName);
                            if (firstDollar != -1) {
                                char[] suffix = SuffixConstants.SUFFIX_class;
                                int suffixLength = suffix.length;
                                char[] newFileName = new char[firstDollar + suffixLength];
                                System.arraycopy(fileName, 0, newFileName, 0, firstDollar);
                                System.arraycopy(suffix, 0, newFileName, firstDollar, suffixLength);
                                fileName = newFileName;
                            }
                            fileNameString = new String(fileName);
                        } else {
                            // assumed to be "module-info.class" (which has no type):
                            fileNameString = this.typeRoot.getElementName();
                        }
                        sourceUnit = new BasicCompilationUnit(sourceString.toCharArray(),
                            Util.toCharArrays(packageFragment.names), fileNameString, this.typeRoot);
                    } catch (JavaModelException e) {
                        // an error occured accessing the java element
                        CharSequence stackTrace
                            = com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.util.Util
                                .getStackTrace(e);
                        throw new IllegalStateException(stackTrace.toString());
                    }
                } else if (this.rawSource != null) {
                    needToResolveBindings = ((this.bits & CompilationUnitResolver.RESOLVE_BINDING) != 0)
                        && this.unitName != null
                        && (this.project != null
                            || this.classpaths != null
                            || this.sourcepaths != null
                            || ((this.bits & CompilationUnitResolver.INCLUDE_RUNNING_VM_BOOTCLASSPATH) != 0))
                        && this.compilerOptions != null;
                    sourceUnit = new BasicCompilationUnit(this.rawSource, null,
                        this.unitName == null ? "" : this.unitName, this.project); //$NON-NLS-1$
                } else {
                    throw new IllegalStateException();
                }
                if ((this.bits & CompilationUnitResolver.PARTIAL) != 0) {
                    useSearcher = true;
                }
                int flags = 0;
                if ((this.bits & CompilationUnitResolver.STATEMENT_RECOVERY) != 0) {
                    flags |= ICompilationUnit.ENABLE_STATEMENTS_RECOVERY;
                }
                if (!useSearcher && ((this.bits & CompilationUnitResolver.IGNORE_METHOD_BODIES) != 0)) {
                    flags |= ICompilationUnit.IGNORE_METHOD_BODIES;
                }

                if (needToResolveBindings) {
                    if ((this.bits & CompilationUnitResolver.BINDING_RECOVERY) != 0) {
                        flags |= ICompilationUnit.ENABLE_BINDINGS_RECOVERY;
                    }
                }

                CompilationUnit result = this.unitResolver.toCompilationUnit(sourceUnit, needToResolveBindings,
                    this.project, getClasspath(), useSearcher ? this.focalPointPosition : -1, this.apiLevel,
                    safeUnmodifiableMap(this.compilerOptions), this.workingCopyOwner, wcOwner, flags, monitor);
                result.setTypeRoot(this.typeRoot);
                return result;
        }
        throw new IllegalStateException();
    }

    /**
     * Parses the given source between the bounds specified by the given offset (inclusive)
     * and the given length and creates and returns a corresponding abstract syntax tree.
     * <p>
     * When the parse is successful the result returned includes the ASTs for the
     * requested source:
     * <ul>
     * <li>{@link #K_CLASS_BODY_DECLARATIONS K_CLASS_BODY_DECLARATIONS}: The result node
     * is a {@link TypeDeclaration TypeDeclaration} whose
     * {@link TypeDeclaration#bodyDeclarations() bodyDeclarations}
     * are the new trees. Other aspects of the type declaration are unspecified.</li>
     * <li>{@link #K_STATEMENTS K_STATEMENTS}: The result node is a
     * {@link Block Block} whose {@link Block#statements() statements}
     * are the new trees. Other aspects of the block are unspecified.</li>
     * <li>{@link #K_EXPRESSION K_EXPRESSION}: The result node is a subclass of
     * {@link Expression Expression}. Other aspects of the expression are unspecified.</li>
     * </ul>
     * The resulting AST node is rooted under an contrived
     * {@link CompilationUnit CompilationUnit} node, to allow the
     * client to retrieve the following pieces of information
     * available there:
     * <ul>
     * <li>{@linkplain CompilationUnit#getLineNumber(int) Line number map}. Line
     * numbers start at 1 and only cover the subrange scanned
     * (<code>source[offset]</code> through <code>source[offset+length-1]</code>).</li>
     * <li>{@linkplain CompilationUnit#getMessages() Compiler messages}
     * and {@linkplain CompilationUnit#getProblems() detailed problem reports}.
     * Character positions are relative to the start of
     * <code>source</code>; line positions are for the subrange scanned.</li>
     * <li>{@linkplain CompilationUnit#getCommentList() Comment list}
     * for the subrange scanned.</li>
     * </ul>
     * <p>
     * The contrived nodes do not have source positions. Other aspects of the
     * {@link CompilationUnit CompilationUnit} node are unspecified, including
     * the exact arrangment of intervening nodes.
     * </p>
     * <p>
     * Lexical or syntax errors detected while parsing can result in
     * a result node being marked as {@link ASTNode#MALFORMED MALFORMED}.
     * In more severe failure cases where the parser is unable to
     * recognize the input, this method returns
     * a {@link CompilationUnit CompilationUnit} node with at least the
     * compiler messages.
     * </p>
     * <p>Each node in the subtree (other than the contrived nodes)
     * carries source range(s) information relating back
     * to positions in the given source (the given source itself
     * is not remembered with the AST).
     * The source range usually begins at the first character of the first token
     * corresponding to the node; leading whitespace and comments are <b>not</b>
     * included. The source range usually extends through the last character of
     * the last token corresponding to the node; trailing whitespace and
     * comments are <b>not</b> included. There are a handful of exceptions
     * (including the various body declarations); the
     * specification for these node type spells out the details.
     * Source ranges nest properly: the source range for a child is always
     * within the source range of its parent, and the source ranges of sibling
     * nodes never overlap.
     * </p>
     * <p>
     * This method does not compute binding information; all <code>resolveBinding</code>
     * methods applied to nodes of the resulting AST return <code>null</code>.
     * </p>
     *
     * @return an AST node whose type depends on the kind of parse
     * requested, with a fallback to a <code>CompilationUnit</code>
     * in the case of severe parsing errors
     * @see ASTNode#getStartPosition()
     * @see ASTNode#getLength()
     */
    private ASTNode internalCreateASTForKind() {
        final ASTConverter converter = new ASTConverter(this.compilerOptions, false, null);
        converter.compilationUnitSource = this.rawSource;
        converter.compilationUnitSourceLength = this.rawSource.length;
        converter.scanner.setSource(this.rawSource);

        AST ast = AST.newAST(this.apiLevel,
            JavaCore.ENABLED.equals(this.compilerOptions.get(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES)));
        ast.setDefaultNodeFlag(ASTNode.ORIGINAL);
        ast.setBindingResolver(new BindingResolver());
        if ((this.bits & CompilationUnitResolver.STATEMENT_RECOVERY) != 0) {
            ast.setFlag(ICompilationUnit.ENABLE_STATEMENTS_RECOVERY);
        }
        ast.scanner.previewEnabled
            = JavaCore.ENABLED.equals(this.compilerOptions.get(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES));
        converter.setAST(ast);
        CodeSnippetParsingUtil codeSnippetParsingUtil
            = new CodeSnippetParsingUtil((this.bits & CompilationUnitResolver.IGNORE_METHOD_BODIES) != 0);
        CompilationUnit compilationUnit = ast.newCompilationUnit();
        if (this.sourceLength == -1) {
            this.sourceLength = this.rawSource.length;
        }
        switch (this.astKind) {
            case K_STATEMENTS:
                ConstructorDeclaration constructorDeclaration
                    = codeSnippetParsingUtil.parseStatements(this.rawSource, this.sourceOffset, this.sourceLength,
                        this.compilerOptions, true, (this.bits & CompilationUnitResolver.STATEMENT_RECOVERY) != 0);
                RecoveryScannerData data = constructorDeclaration.compilationResult.recoveryScannerData;
                if (data != null) {
                    Scanner scanner = converter.scanner;
                    converter.scanner = new RecoveryScanner(scanner, data.removeUnused());
                    converter.docParser.scanner = converter.scanner;
                    converter.scanner.setSource(scanner.source);

                    compilationUnit.setStatementsRecoveryData(data);
                }
                RecordedParsingInformation recordedParsingInformation
                    = codeSnippetParsingUtil.recordedParsingInformation;
                int[][] comments = recordedParsingInformation.commentPositions;
                if (comments != null) {
                    converter.buildCommentsTable(compilationUnit, comments);
                }
                compilationUnit.setLineEndTable(recordedParsingInformation.lineEnds);
                Block block = ast.newBlock();
                block.setSourceRange(this.sourceOffset, this.sourceOffset + this.sourceLength);
                ExplicitConstructorCall constructorCall = constructorDeclaration.constructorCall;
                if (constructorCall != null && constructorCall.accessMode != ExplicitConstructorCall.ImplicitSuper) {
                    block.statements().add(converter.convert(constructorCall));
                }
                com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.Statement[] statements
                    = constructorDeclaration.statements;
                if (statements != null) {
                    int statementsLength = statements.length;
                    for (int i = 0; i < statementsLength; i++) {
                        if (statements[i] instanceof LocalDeclaration) {
                            converter.checkAndAddMultipleLocalDeclaration(statements, i, block.statements());
                        } else {
                            Statement statement = converter.convert(statements[i]);
                            if (statement != null) {
                                block.statements().add(statement);
                            }
                        }
                    }
                }
                rootNodeToCompilationUnit(ast, compilationUnit, block, recordedParsingInformation, data);
                ast.setDefaultNodeFlag(0);
                ast.setOriginalModificationCount(ast.modificationCount());
                return block;

            case K_EXPRESSION:
                com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.Expression expression = codeSnippetParsingUtil
                    .parseExpression(this.rawSource, this.sourceOffset, this.sourceLength, this.compilerOptions, true);
                recordedParsingInformation = codeSnippetParsingUtil.recordedParsingInformation;
                comments = recordedParsingInformation.commentPositions;
                if (comments != null) {
                    converter.buildCommentsTable(compilationUnit, comments);
                }
                compilationUnit.setLineEndTable(recordedParsingInformation.lineEnds);
                if (expression != null) {
                    Expression expression2 = converter.convert(expression);
                    rootNodeToCompilationUnit(expression2.getAST(), compilationUnit, expression2,
                        codeSnippetParsingUtil.recordedParsingInformation, null);
                    ast.setDefaultNodeFlag(0);
                    ast.setOriginalModificationCount(ast.modificationCount());
                    return expression2;
                } else {
                    CategorizedProblem[] problems = recordedParsingInformation.problems;
                    if (problems != null) {
                        compilationUnit.setProblems(problems);
                    }
                    ast.setDefaultNodeFlag(0);
                    ast.setOriginalModificationCount(ast.modificationCount());
                    return compilationUnit;
                }
            case K_CLASS_BODY_DECLARATIONS:
                final com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.ASTNode[] nodes
                    = codeSnippetParsingUtil.parseClassBodyDeclarations(this.rawSource, this.sourceOffset,
                        this.sourceLength, this.compilerOptions, true,
                        (this.bits & CompilationUnitResolver.STATEMENT_RECOVERY) != 0);
                recordedParsingInformation = codeSnippetParsingUtil.recordedParsingInformation;
                comments = recordedParsingInformation.commentPositions;
                if (comments != null) {
                    converter.buildCommentsTable(compilationUnit, comments);
                }
                compilationUnit.setLineEndTable(recordedParsingInformation.lineEnds);
                if (nodes != null) {
                    // source has no syntax error or the statement recovery is enabled
                    TypeDeclaration typeDeclaration = converter.convert(nodes);
                    typeDeclaration.setSourceRange(this.sourceOffset, this.sourceOffset + this.sourceLength);
                    rootNodeToCompilationUnit(typeDeclaration.getAST(), compilationUnit, typeDeclaration,
                        codeSnippetParsingUtil.recordedParsingInformation, null);
                    ast.setDefaultNodeFlag(0);
                    ast.setOriginalModificationCount(ast.modificationCount());
                    return typeDeclaration;
                } else {
                    // source has syntax error and the statement recovery is disabled
                    CategorizedProblem[] problems = recordedParsingInformation.problems;
                    if (problems != null) {
                        compilationUnit.setProblems(problems);
                    }
                    ast.setDefaultNodeFlag(0);
                    ast.setOriginalModificationCount(ast.modificationCount());
                    return compilationUnit;
                }
        }
        throw new IllegalStateException();
    }

    private void propagateErrors(ASTNode astNode, CategorizedProblem[] problems, RecoveryScannerData data) {
        astNode.accept(new ASTSyntaxErrorPropagator(problems));
        if (data != null) {
            astNode.accept(new ASTRecoveryPropagator(problems, data));
        }
    }

    private void rootNodeToCompilationUnit(AST ast, CompilationUnit compilationUnit, ASTNode node,
        RecordedParsingInformation recordedParsingInformation, RecoveryScannerData data) {
        final int problemsCount = recordedParsingInformation.problemsCount;
        switch (node.getNodeType()) {
            case ASTNode.BLOCK: {
                Block block = (Block) node;
                if (problemsCount != 0) {
                    // propagate and record problems
                    final CategorizedProblem[] problems = recordedParsingInformation.problems;
                    propagateErrors(block, problems, data);
                    compilationUnit.setProblems(problems);
                }
                TypeDeclaration typeDeclaration = ast.newTypeDeclaration();
                Initializer initializer = ast.newInitializer();
                initializer.setBody(block);
                typeDeclaration.bodyDeclarations().add(initializer);
                compilationUnit.types().add(typeDeclaration);
            }
                break;

            case ASTNode.TYPE_DECLARATION: {
                TypeDeclaration typeDeclaration = (TypeDeclaration) node;
                if (problemsCount != 0) {
                    // propagate and record problems
                    final CategorizedProblem[] problems = recordedParsingInformation.problems;
                    propagateErrors(typeDeclaration, problems, data);
                    compilationUnit.setProblems(problems);
                }
                compilationUnit.types().add(typeDeclaration);
            }
                break;

            default:
                if (node instanceof Expression) {
                    Expression expression = (Expression) node;
                    if (problemsCount != 0) {
                        // propagate and record problems
                        final CategorizedProblem[] problems = recordedParsingInformation.problems;
                        propagateErrors(expression, problems, data);
                        compilationUnit.setProblems(problems);
                    }
                    ExpressionStatement expressionStatement = ast.newExpressionStatement(expression);
                    Block block = ast.newBlock();
                    block.statements().add(expressionStatement);
                    Initializer initializer = ast.newInitializer();
                    initializer.setBody(block);
                    TypeDeclaration typeDeclaration = ast.newTypeDeclaration();
                    typeDeclaration.bodyDeclarations().add(initializer);
                    compilationUnit.types().add(typeDeclaration);
                }
        }
    }

    private boolean hasJavaNature() {
        return this.project == null || JavaProject.hasJavaNature(this.project.getProject());
    }
}
