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
 *******************************************************************************/

package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.dom;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaCore;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.parser.Scanner;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jface.text.IDocument;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.text.edits.TextEdit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Umbrella owner and abstract syntax tree node factory.
 * An <code>AST</code> instance serves as the common owner of any number of
 * AST nodes, and as the factory for creating new AST nodes owned by that
 * instance.
 * <p>
 * Abstract syntax trees may be hand constructed by clients, using the
 * <code>new<i>TYPE</i></code> factory methods to create new nodes, and the
 * various <code>set<i>CHILD</i></code> methods
 * (see {@link ASTNode ASTNode} and its subclasses)
 * to connect them together.
 * </p>
 * <p>
 * Each AST node belongs to a unique AST instance, called the owning AST.
 * The children of an AST node always have the same owner as their parent node.
 * If a node from one AST is to be added to a different AST, the subtree must
 * be cloned first to ensures that the added nodes have the correct owning AST.
 * </p>
 * <p>
 * There can be any number of AST nodes owned by a single AST instance that are
 * unparented. Each of these nodes is the root of a separate little tree of nodes.
 * The method <code>ASTNode.getRoot()</code> navigates from any node to the root
 * of the tree that it is contained in. Ordinarily, an AST instance has one main
 * tree (rooted at a <code>CompilationUnit</code>), with newly-created nodes appearing
 * as additional roots until they are parented somewhere under the main tree.
 * One can navigate from any node to its AST instance, but not conversely.
 * </p>
 * <p>
 * The class {@link ASTParser} parses a string
 * containing a Java source code and returns an abstract syntax tree
 * for it. The resulting nodes carry source ranges relating the node back to
 * the original source characters.
 * </p>
 * <p>
 * Compilation units created by <code>ASTParser</code> from a
 * source document can be serialized after arbitrary modifications
 * with minimal loss of original formatting. Here is an example:
 * </p>
 * 
 * <pre>
 * Document doc = new Document("import java.util.List;\nclass X {}\n");
 * ASTParser parser = ASTParser.newParser(AST.JLS3);
 * parser.setSource(doc.get().toCharArray());
 * CompilationUnit cu = (CompilationUnit) parser.createAST(null);
 * cu.recordModifications();
 * AST ast = cu.getAST();
 * ImportDeclaration id = ast.newImportDeclaration();
 * id.setName(ast.newName(new String[] {"java", "util", "Set"});
 * cu.imports().add(id); // add import declaration at end
 * TextEdit edits = cu.rewrite(document, null);
 * UndoEdit undo = edits.apply(document);
 * </pre>
 * 
 * <p>
 * See also
 * {@link com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.dom.rewrite.ASTRewrite}
 * for
 * an alternative way to describe and serialize changes to a
 * read-only AST.
 * </p>
 * <p>
 * Clients may create instances of this class using {@link #newAST(int, boolean)},
 * but this class is not intended to be subclassed.
 * </p>
 *
 * @see ASTParser
 * @see ASTNode
 * @since 2.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public final class AST {
    /**
     * new Class[] {AST.class}
     * 
     * @since 3.0
     */
    private static final Class[] AST_CLASS = new Class[] { AST.class };

    /**
     * Constant for indicating the AST API that handles JLS2.
     * <p>
     * This API is capable of handling all constructs
     * in the Java language as described in the Java Language
     * Specification, Second Edition (JLS2).
     * JLS2 is a superset of all earlier versions of the
     * Java language, and the JLS2 API can be used to manipulate
     * programs written in all versions of the Java language
     * up to and including J2SE 1.4.
     * </p>
     *
     * @since 3.0
     * @deprecated Clients should use the {@link #getJLSLatest()} AST API instead.
     */
    @Deprecated
    public static final int JLS2 = 2;

    /**
     * Internal synonym for {@link #JLS2}. Use to alleviate
     * deprecation warnings.
     * 
     * @since 3.1
     */
    /* package */ static final int JLS2_INTERNAL = JLS2;

    /**
     * Constant for indicating the AST API that handles JLS3.
     * <p>
     * This API is capable of handling all constructs in the
     * Java language as described in the Java Language
     * Specification, Third Edition (JLS3).
     * JLS3 is a superset of all earlier versions of the
     * Java language, and the JLS3 API can be used to manipulate
     * programs written in all versions of the Java language
     * up to and including J2SE 5 (aka JDK 1.5).
     * </p>
     *
     * @since 3.1
     * @deprecated Clients should use the {@link #getJLSLatest()} AST API instead.
     */
    @Deprecated
    public static final int JLS3 = 3;

    /**
     * Internal synonym for {@link #JLS3}. Use to alleviate
     * deprecation warnings.
     * 
     * @since 3.8
     */
    /* package */ static final int JLS3_INTERNAL = JLS3;

    /**
     * Constant for indicating the AST API that handles JLS4 (aka JLS7).
     * <p>
     * This API is capable of handling all constructs in the
     * Java language as described in the Java Language
     * Specification, Java SE 7 Edition (JLS7) as specified by JSR336.
     * JLS4 is a superset of all earlier versions of the
     * Java language, and the JLS4 API can be used to manipulate
     * programs written in all versions of the Java language
     * up to and including Java SE 7 (aka JDK 1.7).
     * </p>
     *
     * @since 3.7.1
     * @deprecated Clients should use the {@link #getJLSLatest()} AST API instead.
     */
    @Deprecated
    public static final int JLS4 = 4;

    /**
     * Internal synonym for {@link #JLS4}. Use to alleviate
     * deprecation warnings.
     * 
     * @since 3.10
     */
    /* package */ static final int JLS4_INTERNAL = JLS4;

    /**
     * Constant for indicating the AST API that handles JLS8.
     * <p>
     * This API is capable of handling all constructs in the
     * Java language as described in the Java Language
     * Specification, Java SE 8 Edition (JLS8) as specified by JSR337.
     * JLS8 is a superset of all earlier versions of the
     * Java language, and the JLS8 API can be used to manipulate
     * programs written in all versions of the Java language
     * up to and including Java SE 8 (aka JDK 1.8).
     * </p>
     *
     * @since 3.10
     * @deprecated Clients should use the {@link #getJLSLatest()} AST API instead.
     */
    @Deprecated
    public static final int JLS8 = 8;

    /**
     * Internal synonym for {@link #JLS8}. Use to alleviate
     * deprecation warnings.
     * 
     * @since 3.14
     */
    /* package */ static final int JLS8_INTERNAL = JLS8;

    /**
     * Constant for indicating the AST API that handles JLS9.
     * <p>
     * This API is capable of handling all constructs in the
     * Java language as described in the Java Language
     * Specification, Java SE 9 Edition (JLS9).
     * JLS9 is a superset of all earlier versions of the
     * Java language, and the JLS9 API can be used to manipulate
     * programs written in all versions of the Java language
     * up to and including Java SE 9 (aka JDK 9).
     * </p>
     *
     * @since 3.14
     * @deprecated Clients should use the {@link #getJLSLatest()} AST API instead.
     */
    @Deprecated
    public static final int JLS9 = 9;

    /**
     * Internal synonym for {@link #JLS9}. Use to alleviate
     * deprecation warnings once JLS9 is deprecated
     * 
     * @since 3.14
     */
    /* package */ static final int JLS9_INTERNAL = JLS9;

    /**
     * Constant for indicating the AST API that handles JLS10.
     * <p>
     * This API is capable of handling all constructs in the
     * Java language as described in the Java Language
     * Specification, Java SE 10 Edition (JLS10).
     * JLS10 is a superset of all earlier versions of the
     * Java language, and the JLS10 API can be used to manipulate
     * programs written in all versions of the Java language
     * up to and including Java SE 10 (aka JDK 10).
     * </p>
     *
     * @since 3.14
     * @deprecated Clients should use the {@link #getJLSLatest()} AST API instead.
     */
    @Deprecated
    public static final int JLS10 = 10;

    /**
     * Internal synonym for {@link #JLS10}. Use to alleviate
     * deprecation warnings once JLS10 is deprecated
     * 
     * @since 3.14
     */
    /* package */ static final int JLS10_INTERNAL = JLS10;

    /**
     * Constant for indicating the AST API that handles JLS11.
     * <p>
     * This API is capable of handling all constructs in the
     * Java language as described in the Java Language
     * Specification, Java SE 11 Edition (JLS11).
     * JLS11 is a superset of all earlier versions of the
     * Java language, and the JLS11 API can be used to manipulate
     * programs written in all versions of the Java language
     * up to and including Java SE 11 (aka JDK 11).
     * </p>
     *
     * @deprecated Clients should use the {@link #getJLSLatest()} AST API instead.
     * @since 3.16
     */
    @Deprecated
    public static final int JLS11 = 11;

    /**
     * Internal synonym for {@link #JLS11}. Use to alleviate
     * deprecation warnings once JLS11 is deprecated
     * 
     * @since 3.14
     */
    /* package */ static final int JLS11_INTERNAL = JLS11;

    /**
     * Constant for indicating the AST API that handles JLS12.
     * <p>
     * This API is capable of handling all constructs in the
     * Java language as described in the Java Language
     * Specification, Java SE 12 Edition (JLS12).
     * JLS12 is a superset of all earlier versions of the
     * Java language, and the JLS12 API can be used to manipulate
     * programs written in all versions of the Java language
     * up to and including Java SE 12 (aka JDK 12).
     * </p>
     * 
     * @deprecated Clients should use the {@link #getJLSLatest()} AST API instead.
     * @since 3.18
     */
    @Deprecated
    public static final int JLS12 = 12;
    /**
     * Internal synonym for {@link #JLS12}. Use to alleviate
     * deprecation warnings once JLS12 is deprecated
     * 
     * @since 3.18
     */
    static final int JLS12_INTERNAL = JLS12;

    /**
     * Constant for indicating the AST API that handles JLS13.
     * <p>
     * This API is capable of handling all constructs in the
     * Java language as described in the Java Language
     * Specification, Java SE 13 Edition (JLS13).
     * JLS13 is a superset of all earlier versions of the
     * Java language, and the JLS13 API can be used to manipulate
     * programs written in all versions of the Java language
     * up to and including Java SE 13 (aka JDK 13).
     * </p>
     * 
     * @deprecated Clients should use the {@link #getJLSLatest()} AST API instead.
     * @since 3.20
     */
    @Deprecated
    public static final int JLS13 = 13;

    /**
     * Internal synonym for {@link #JLS13}. Use to alleviate
     * deprecation warnings once JLS13 is deprecated
     * 
     * @since 3.20
     */
    static final int JLS13_INTERNAL = JLS13;

    /**
     * Constant for indicating the AST API that handles JLS14.
     * <p>
     * This API is capable of handling all constructs in the
     * Java language as described in the Java Language
     * Specification, Java SE 14 Edition (JLS14).
     * JLS14 is a superset of all earlier versions of the
     * Java language, and the JLS14 API can be used to manipulate
     * programs written in all versions of the Java language
     * up to and including Java SE 14(aka JDK 14).
     * </p>
     * 
     * @deprecated Clients should use the {@link #getJLSLatest()} AST API instead.
     * @since 3.22
     */
    @Deprecated
    public static final int JLS14 = 14;

    /**
     * Internal synonym for {@link #JLS14}. Use to alleviate
     * deprecation warnings once JLS14 is deprecated
     * 
     * @since 3.22
     */
    static final int JLS14_INTERNAL = JLS14;

    /**
     * Constant for indicating the AST API that handles JLS15.
     * <p>
     * This API is capable of handling all constructs in the
     * Java language as described in the Java Language
     * Specification, Java SE 15 Edition (JLS15).
     * JLS15 is a superset of all earlier versions of the
     * Java language, and the JLS15 API can be used to manipulate
     * programs written in all versions of the Java language
     * up to and including Java SE 15(aka JDK 15).
     * </p>
     * 
     * @deprecated Clients should use the {@link #getJLSLatest()} AST API instead.
     * @since 3.24
     */
    @Deprecated
    public static final int JLS15 = 15;
    /**
     * Constant for indicating the AST API that handles JLS16.
     * <p>
     * This API is capable of handling all constructs in the
     * Java language as described in the Java Language
     * Specification, Java SE 16 Edition (JLS16).
     * JLS16 is a superset of all earlier versions of the
     * Java language, and the JLS16 API can be used to manipulate
     * programs written in all versions of the Java language
     * up to and including Java SE 16(aka JDK 16).
     * </p>
     * 
     * @deprecated Clients should use the {@link #getJLSLatest()} AST API instead.
     * @since 3.26
     */
    @Deprecated
    public static final int JLS16 = 16;
    /**
     * Constant for indicating the AST API that handles JLS17.
     * <p>
     * This API is capable of handling all constructs in the
     * Java language as described in the Java Language
     * Specification, Java SE 17 Edition (JLS17).
     * JLS17 is a superset of all earlier versions of the
     * Java language, and the JLS17 API can be used to manipulate
     * programs written in all versions of the Java language
     * up to and including Java SE 17(aka JDK 17).
     * </p>
     * 
     * @deprecated Clients should use the {@link #getJLSLatest()} AST API instead.
     * @since 3.28
     */
    @Deprecated
    public static final int JLS17 = 17;
    /**
     * Constant for indicating the AST API that handles JLS17.
     * <p>
     * This API is capable of handling all constructs in the
     * Java language as described in the Java Language
     * Specification, Java SE 18 Edition (JLS18).
     * JLS18 is a superset of all earlier versions of the
     * Java language, and the JLS18 API can be used to manipulate
     * programs written in all versions of the Java language
     * up to and including Java SE 18(aka JDK 18).
     * </p>
     *
     * @deprecated
     * @since 3.30
     */
    @Deprecated
    public static final int JLS18 = 18;

    /**
     * Constant for indicating the AST API that handles JLS17.
     * <p>
     * This API is capable of handling all constructs in the
     * Java language as described in the Java Language
     * Specification, Java SE 18 Edition (JLS18).
     * JLS18 is a superset of all earlier versions of the
     * Java language, and the JLS18 API can be used to manipulate
     * programs written in all versions of the Java language
     * up to and including Java SE 18(aka JDK 18).
     * </p>
     *
     * @deprecated
     * @since 3.32
     */
    @Deprecated
    public static final int JLS19 = 19;

    /**
     * Constant for indicating the AST API that handles JLS20.
     * <p>
     * This API is capable of handling all constructs in the
     * Java language as described in the Java Language
     * Specification, Java SE 20 Edition (JLS20).
     * JLS20 is a superset of all earlier versions of the
     * Java language, and the JLS20 API can be used to manipulate
     * programs written in all versions of the Java language
     * up to and including Java SE 20(aka JDK 20).
     * </p>
     *
     * @since 3.34
     */
    public static final int JLS20 = 20;
    /**
     * Constant for indicating the AST API that handles JLS21.
     * <p>
     * This API is capable of handling all constructs in the
     * Java language as described in the Java Language
     * Specification, Java SE 21 Edition (JLS21).
     * JLS21 is a superset of all earlier versions of the
     * Java language, and the JLS21 API can be used to manipulate
     * programs written in all versions of the Java language
     * up to and including Java SE 21(aka JDK 21).
     * </p>
     *
     * @since 3.36
     */
    public static final int JLS21 = 21;
    /**
     * Constant for indicating the AST API that handles JLS22.
     * <p>
     * This API is capable of handling all constructs in the
     * Java language as described in the Java Language
     * Specification, Java SE 22 Edition (JLS22).
     * JLS22 is a superset of all earlier versions of the
     * Java language, and the JLS22 API can be used to manipulate
     * programs written in all versions of the Java language
     * up to and including Java SE 22(aka JDK 22).
     * </p>
     *
     * @since 3.38
     */
    public static final int JLS22 = 22;
    /**
     * Constant for indicating the AST API that handles JLS23.
     * <p>
     * This API is capable of handling all constructs in the
     * Java language as described in the Java Language
     * Specification, Java SE 23 Edition (JLS23).
     * JLS23 is a superset of all earlier versions of the
     * Java language, and the JLS23 API can be used to manipulate
     * programs written in all versions of the Java language
     * up to and including Java SE 23(aka JDK 23).
     * </p>
     *
     * @since 3.38
     */
    public static final int JLS23 = 23;
    /**
     * Constant for indicating the AST API that handles JLS24.
     * <p>
     * This API is capable of handling all constructs in the
     * Java language as described in the Java Language
     * Specification, Java SE 24 Edition (JLS24).
     * JLS24 is a superset of all earlier versions of the
     * Java language, and the JLS24 API can be used to manipulate
     * programs written in all versions of the Java language
     * up to and including Java SE 24(aka JDK 24).
     * </p>
     *
     * @since 3.42
     */
    public static final int JLS24 = 24;
    /**
     * Internal synonym for {@link #JLS15}. Use to alleviate
     * deprecation warnings once JLS15 is deprecated
     */
    static final int JLS15_INTERNAL = JLS15;
    /**
     * Internal synonym for {@link #JLS16}. Use to alleviate
     * deprecation warnings once JLS16 is deprecated
     */
    static final int JLS16_INTERNAL = JLS16;
    /**
     * Internal synonym for {@link #JLS17}. Use to alleviate
     * deprecation warnings once JLS17 is deprecated
     */
    static final int JLS17_INTERNAL = JLS17;
    /**
     * Internal synonym for {@link #JLS18}. Use to alleviate
     * deprecation warnings once JLS18 is deprecated
     */
    static final int JLS18_INTERNAL = JLS18;
    /**
     * Internal synonym for {@link #JLS19}. Use to alleviate
     * deprecation warnings once JLS19 is deprecated
     */
    static final int JLS19_INTERNAL = JLS19;
    /**
     * Internal synonym for {@link #JLS20}. Use to alleviate
     * deprecation warnings once JLS20 is deprecated
     */
    static final int JLS20_INTERNAL = JLS20;
    /**
     * Internal synonym for {@link #JLS21}. Use to alleviate
     * deprecation warnings once JLS21 is deprecated
     */
    static final int JLS21_INTERNAL = JLS21;
    /**
     * Internal synonym for {@link #JLS22}. Use to alleviate
     * deprecation warnings once JLS22 is deprecated
     */
    static final int JLS22_INTERNAL = JLS22;
    /**
     * Internal synonym for {@link #JLS23}. Use to alleviate
     * deprecation warnings once JLS23 is deprecated
     */
    static final int JLS23_INTERNAL = JLS23;
    /**
     * Internal synonym for {@link #JLS24}. Use to alleviate
     * deprecation warnings once JLS24 is deprecated
     */
    static final int JLS24_INTERNAL = JLS24;
    /**
     * Internal property for latest supported JLS level
     * This provides the latest JLS level.
     */
    private static final int JLS_INTERNAL_Latest = JLS24;

    /**
     * @since 3.26
     * This provides the latest JLS level.
     * @deprecated use {@link #getJLSLatest()}
     */
    @Deprecated
    public static final int JLS_Latest = JLS_INTERNAL_Latest;

    private static final List<Integer> ALL_VERSIONS = List.of(JLS2, JLS3, JLS4, JLS8, JLS9, JLS10, JLS11, JLS12, JLS13,
        JLS14, JLS15, JLS16, JLS17, JLS18, JLS19, JLS20, JLS21, JLS22, JLS23, JLS24);
    private static final List<Integer> UNSUPPORTED_VERSIONS = List.of(JLS2, JLS3, JLS4);
    private static final List<Integer> SUPPORTED_VERSIONS;
    static {
        List<Integer> temp = new ArrayList<>(ALL_VERSIONS);
        temp.removeAll(UNSUPPORTED_VERSIONS);
        SUPPORTED_VERSIONS = Collections.unmodifiableList(temp);
    }

    /*
     * Must not collide with a value for ICompilationUnit constants
     */
    static final int RESOLVED_BINDINGS = 0x80000000;

    private static final Map<String, Long> jdkLevelMap = getLevelMapTable();

    private static final Map<String, Integer> apiLevelMap = getApiLevelMapTable();

    /**
     * Creates a new Java abstract syntax tree
     * (AST) following the specified set of API rules.
     * <p>
     * Clients should use this method specifying {@link #getJLSLatest} as the
     * AST level in all cases, even when dealing with source of earlier JDK versions.
     * </p>
     *
     * @param level the API level; one of the <code>JLS*</code> level constants or {@link AST#getJLSLatest}
     * @param previewEnabled <code>true</code> if preview feature is enabled else <code>false</code>
     * @return new AST instance following the specified set of API rules.
     * @exception IllegalArgumentException if:
     * <ul>
     * <li>the API level is not one of the <code>JLS*</code> level constants</li>
     * </ul>
     * @since 3.19
     */
    public static AST newAST(int level, boolean previewEnabled) {
        return new AST(level, previewEnabled);
    }

    /**
     * Level of AST API supported by this AST.
     * 
     * @since 3.0
     */
    int apiLevel;

    private final boolean previewEnabled;

    /**
     * Tag bit value. This represents internal state of the tree.
     */
    private int bits;

    /**
     * Default value of <code>flag</code> when a new node is created.
     */
    private int defaultNodeFlag = 0;

    /**
     * When disableEvents > 0, events are not reported and
     * the modification count stays fixed.
     * <p>
     * This mechanism is used in lazy initialization of a node
     * to prevent events from being reported for the modification
     * of the node as well as for the creation of the missing child.
     * </p>
     * 
     * @since 3.0
     */
    private final AtomicInteger disableEvents = new AtomicInteger();

    /**
     * The event handler for this AST.
     * Initially an event handler that does not nothing.
     * 
     * @since 3.0
     */
    private NodeEventHandler eventHandler = new NodeEventHandler();

    /**
     * Internal object unique to the AST instance. Readers must synchronize on
     * this object when the modifying instance fields.
     * 
     * @since 3.0
     */
    private final Object internalASTLock = new Object();

    /**
     * Internal modification count; initially 0; increases monotonically
     * <b>by one or more</b> as the AST is successively modified.
     */
    private final AtomicLong modificationCount = new AtomicLong();

    /**
     * Internal original modification count; value is equals to <code>
     * modificationCount</code> at the end of the parse (<code>ASTParser
     * </code>). If this ast is not created with a parser then value is 0.
     * 
     * @since 3.0
     */
    private volatile long originalModificationCount;

    /**
     * The binding resolver for this AST. Initially a binding resolver that
     * does not resolve names at all.
     */
    private BindingResolver resolver = new BindingResolver();

    /**
     * Internal ast rewriter used to record ast modification when record mode is enabled.
     */
    InternalASTRewrite rewriter;

    /**
     * Java Scanner used to validate preconditions for the creation of specific nodes
     * like CharacterLiteral, NumberLiteral, StringLiteral or SimpleName.
     */
    Scanner scanner;

    /**
     * new Object[] {this}
     * 
     * @since 3.0
     */
    private final Object[] THIS_AST = new Object[] { this };

    /**
     * Creates a new Java abstract syntax tree
     * (AST) following the specified set of API rules.
     *
     * @param level the API level; one of the <code>JLS*</code> level constants
     * @since 3.0
     */
    private AST(int level, boolean previewEnabled) {
        this.previewEnabled = previewEnabled;
        if (ALL_VERSIONS.contains(level) && !SUPPORTED_VERSIONS.contains(level)) {
            level = SUPPORTED_VERSIONS.get(0);
        }
        switch (level) {
            case JLS8_INTERNAL:
                this.apiLevel = level;
                // initialize a scanner
                this.scanner = new Scanner(true /* comment */, true /* whitespace */, false /* nls */,
                    ClassFileConstants.JDK1_8 /* sourceLevel */, ClassFileConstants.JDK1_8 /* complianceLevel */,
                    null/* taskTag */, null/* taskPriorities */, true/* taskCaseSensitive */,
                    false/* isPreviewEnabled */);
                break;

            case JLS9_INTERNAL:
                this.apiLevel = level;
                // initialize a scanner
                this.scanner = new Scanner(true /* comment */, true /* whitespace */, false /* nls */,
                    ClassFileConstants.JDK9 /* sourceLevel */, ClassFileConstants.JDK9 /* complianceLevel */,
                    null/* taskTag */, null/* taskPriorities */, true/* taskCaseSensitive */,
                    false/* isPreviewEnabled */);
                break;

            case JLS10_INTERNAL:
                this.apiLevel = level;
                // initialize a scanner
                this.scanner = new Scanner(true /* comment */, true /* whitespace */, false /* nls */,
                    ClassFileConstants.JDK10 /* sourceLevel */, ClassFileConstants.JDK10 /* complianceLevel */,
                    null/* taskTag */, null/* taskPriorities */, true/* taskCaseSensitive */,
                    false/* isPreviewEnabled */);
                break;

            default:
                if (!ALL_VERSIONS.contains(level)) {
                    throw new IllegalArgumentException("Unsupported JLS level : " + level); //$NON-NLS-1$
                }
                this.apiLevel = level;
                // initialize a scanner
                // As long as the AST levels and ClassFileConstants.MAJOR_VERSION grow simultaneously,
                // we can use the offset of +44 to compute the Major version from the given AST Level
                long compliance = ClassFileConstants.getComplianceLevelForJavaVersion(level + 44);
                this.scanner = new Scanner(true /* comment */, true /* whitespace */, false /* nls */,
                    compliance /* sourceLevel */, compliance /* complianceLevel */, null/* taskTag */,
                    null/* taskPriorities */, true/* taskCaseSensitive */, false/* isPreviewEnabled */);
                break;
        }
    }

    /**
     * Creates a new Java abstract syntax tree
     * Following option keys are significant:
     * <ul>
     * <li><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.source"</code>
     * indicates the api level and source compatibility mode (as per <code>JavaCore</code>) - defaults to 1.8
     * <ul>
     * <li>
     * <li><code>"1.8"</code> implies the respective source JDK level 1.8 and api level {@link #JLS8}.</li>
     * <li><code>"9", "10", "11" up to "23"</code> implies the respective JDK levels 9, 10, 11 up to 23
     * and api levels {@link #JLS9}, {@link #JLS10}, {@link #JLS11} up to {@link #JLS23}.</li>
     * <li>Additional legal values may be added later.</li>
     * </ul>
     * <li><code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.problem.enablePreviewFeatures"</code>
     * -
     * indicates whether the preview is enabled or disabled
     * legal values are <code>"enabled"</code> and <code>"disabled"</code> implying preview enabled and disabled
     * respectively.
     * preview enabling has an effect only with the latest ast level.
     * </li>
     * </ul>
     *
     * @param options the table of options (key type: <code>String</code>;
     * value type: <code>String</code>)
     * @see JavaCore#getDefaultOptions()
     */
    public AST(Map options) {
        this(apiLevelMap.get(options.get(JavaCore.COMPILER_SOURCE)),
            JavaCore.ENABLED.equals(options.get(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES)));

        long sourceLevel = AST.jdkLevelMap.get(options.get(JavaCore.COMPILER_SOURCE));
        this.scanner = new Scanner(true /* comment */, true /* whitespace */, false /* nls */,
            sourceLevel /* sourceLevel */, sourceLevel /* complianceLevel */, null/* taskTag */,
            null/* taskPriorities */, true/* taskCaseSensitive */, this.previewEnabled /* isPreviewEnabled */);
    }

    private static Map<String, Long> getLevelMapTable() {
        Map<String, Long> t = new HashMap<>();
        t.put(null, CompilerOptions.getFirstSupportedJdkLevel());
        t.put(JavaCore.VERSION_1_2, CompilerOptions.getFirstSupportedJdkLevel());
        t.put(JavaCore.VERSION_1_3, CompilerOptions.getFirstSupportedJdkLevel());
        t.put(JavaCore.VERSION_1_4, CompilerOptions.getFirstSupportedJdkLevel());
        t.put(JavaCore.VERSION_1_5, CompilerOptions.getFirstSupportedJdkLevel());
        t.put(JavaCore.VERSION_1_6, CompilerOptions.getFirstSupportedJdkLevel());
        t.put(JavaCore.VERSION_1_7, CompilerOptions.getFirstSupportedJdkLevel());
        t.put(JavaCore.VERSION_1_8, CompilerOptions.getFirstSupportedJdkLevel());
        t.put(JavaCore.VERSION_9, ClassFileConstants.JDK9);
        t.put(JavaCore.VERSION_10, ClassFileConstants.JDK10);
        t.put(JavaCore.VERSION_11, ClassFileConstants.JDK11);
        t.put(JavaCore.VERSION_12, ClassFileConstants.JDK12);
        t.put(JavaCore.VERSION_13, ClassFileConstants.JDK13);
        t.put(JavaCore.VERSION_14, ClassFileConstants.JDK14);
        t.put(JavaCore.VERSION_15, ClassFileConstants.JDK15);
        t.put(JavaCore.VERSION_16, ClassFileConstants.JDK16);
        t.put(JavaCore.VERSION_17, ClassFileConstants.JDK17);
        t.put(JavaCore.VERSION_18, ClassFileConstants.JDK18);
        t.put(JavaCore.VERSION_19, ClassFileConstants.JDK19);
        t.put(JavaCore.VERSION_20, ClassFileConstants.JDK20);
        t.put(JavaCore.VERSION_21, ClassFileConstants.JDK21);
        t.put(JavaCore.VERSION_22, ClassFileConstants.JDK22);
        t.put(JavaCore.VERSION_23, ClassFileConstants.JDK23);
        t.put(JavaCore.VERSION_24, ClassFileConstants.JDK24);
        return Collections.unmodifiableMap(t);
    }

    private static Map<String, Integer> getApiLevelMapTable() {
        Map<String, Integer> t = new HashMap<>();
        t.put(null, JLS8_INTERNAL);
        t.put(JavaCore.VERSION_1_2, JLS8_INTERNAL);
        t.put(JavaCore.VERSION_1_3, JLS8_INTERNAL);
        t.put(JavaCore.VERSION_1_4, JLS8_INTERNAL);
        t.put(JavaCore.VERSION_1_5, JLS8_INTERNAL);
        t.put(JavaCore.VERSION_1_6, JLS8_INTERNAL);
        t.put(JavaCore.VERSION_1_7, JLS8_INTERNAL);
        t.put(JavaCore.VERSION_1_8, JLS8_INTERNAL);
        t.put(JavaCore.VERSION_9, JLS9_INTERNAL);
        t.put(JavaCore.VERSION_10, JLS10_INTERNAL);
        t.put(JavaCore.VERSION_11, JLS11_INTERNAL);
        t.put(JavaCore.VERSION_12, JLS12_INTERNAL);
        t.put(JavaCore.VERSION_13, JLS13_INTERNAL);
        t.put(JavaCore.VERSION_14, JLS14_INTERNAL);
        t.put(JavaCore.VERSION_15, JLS15_INTERNAL);
        t.put(JavaCore.VERSION_16, JLS16_INTERNAL);
        t.put(JavaCore.VERSION_17, JLS17_INTERNAL);
        t.put(JavaCore.VERSION_18, JLS18_INTERNAL);
        t.put(JavaCore.VERSION_19, JLS19_INTERNAL);
        t.put(JavaCore.VERSION_20, JLS20_INTERNAL);
        t.put(JavaCore.VERSION_21, JLS21_INTERNAL);
        t.put(JavaCore.VERSION_22, JLS22_INTERNAL);
        t.put(JavaCore.VERSION_23, JLS23_INTERNAL);
        t.put(JavaCore.VERSION_24, JLS24_INTERNAL);
        return Collections.unmodifiableMap(t);
    }

    /**
     * Return the API level supported by this AST.
     *
     * @return level the API level; one of the <code>JLS*</code> level constants
     * declared on <code>AST</code>; assume this set is open-ended
     * @since 3.0
     */
    public int apiLevel() {
        return this.apiLevel;
    }

    /**
     * Creates an unparented node of the given node class
     * (non-abstract subclass of {@link ASTNode}).
     *
     * @param nodeClass AST node class
     * @return a new unparented node owned by this AST
     * @exception IllegalArgumentException if <code>nodeClass</code> is
     * <code>null</code> or is not a concrete node type class
     * or is not supported for this AST's API level
     * @since 3.0
     */
    public ASTNode createInstance(Class nodeClass) {
        if (nodeClass == null) {
            throw new IllegalArgumentException();
        }
        try {
            // invoke constructor with signature Foo(AST)
            Constructor c = nodeClass.getDeclaredConstructor(AST_CLASS);
            Object result = c.newInstance(this.THIS_AST);
            return (ASTNode) result;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            // all AST node classes have an accessible Foo(AST) constructor
            // therefore nodeClass is not legit
            throw new IllegalArgumentException(e);
        } catch (InvocationTargetException e) {
            // concrete AST node classes do not die in the constructor
            // therefore nodeClass is not legit
            throw new IllegalArgumentException(e.getCause());
        }
    }

    /**
     * Creates an unparented node of the given node type.
     * This convenience method is equivalent to:
     * 
     * <pre>
     * createInstance(ASTNode.nodeClassForType(nodeType))
     * </pre>
     *
     * @param nodeType AST node type, one of the node type
     * constants declared on {@link ASTNode}
     * @return a new unparented node owned by this AST
     * @exception IllegalArgumentException if <code>nodeType</code> is
     * not a legal AST node type or if it's not supported for this AST's API level
     * @since 3.0
     */
    public ASTNode createInstance(int nodeType) {
        // nodeClassForType throws IllegalArgumentException if nodeType is bogus
        Class nodeClass = ASTNode.nodeClassForType(nodeType);
        return createInstance(nodeClass);
    }

    /**
     * Disable events.
     * This method is thread-safe for AST readers.
     *
     * @see #reenableEvents()
     * @since 3.0
     */
    final void disableEvents() {
        synchronized (this.internalASTLock) {
            // guard against concurrent access by another reader
            this.disableEvents.incrementAndGet();
        }
        // while disableEvents > 0 no events will be reported, and mod count will stay fixed
    }

    /**
     * Returns the binding resolver for this AST.
     *
     * @return the binding resolver for this AST
     */
    BindingResolver getBindingResolver() {
        return this.resolver;
    }

    /**
     * Returns default node flags of new nodes of this AST.
     *
     * @return the default node flags of new nodes of this AST
     * @since 3.0
     */
    int getDefaultNodeFlag() {
        return this.defaultNodeFlag;
    }

    /**
     * Returns the event handler for this AST.
     *
     * @return the event handler for this AST
     * @since 3.0
     */
    NodeEventHandler getEventHandler() {
        return this.eventHandler;
    }

    /*
     * (omit javadoc for this method)
     * This method is a copy of setName(String[]) that doesn't do any validation.
     */
    Name internalNewName(String[] identifiers) {
        int count = identifiers.length;
        if (count == 0) {
            throw new IllegalArgumentException();
        }
        final SimpleName simpleName = new SimpleName(this);
        simpleName.internalSetIdentifier(identifiers[0]);
        Name result = simpleName;
        for (int i = 1; i < count; i++) {
            SimpleName name = new SimpleName(this);
            name.internalSetIdentifier(identifiers[i]);
            result = newQualifiedName(result, name);
        }
        return result;
    }

    /**
     * Returns the modification count for this AST. The modification count
     * is a non-negative value that increases (by 1 or perhaps by more) as
     * this AST or its nodes are changed. The initial value is unspecified.
     * <p>
     * The following things count as modifying an AST:
     * <ul>
     * <li>creating a new node owned by this AST,</li>
     * <li>adding a child to a node owned by this AST,</li>
     * <li>removing a child from a node owned by this AST,</li>
     * <li>setting a non-node attribute of a node owned by this AST.</li>
     * </ul>
     * <p>
     * Operations which do not entail creating or modifying existing nodes
     * do not increase the modification count.
     * </p>
     * <p>
     * N.B. This method may be called several times in the course
     * of a single client operation. The only promise is that the modification
     * count increases monotonically as the AST or its nodes change; there is
     * no promise that a modifying operation increases the count by exactly 1.
     * </p>
     *
     * @return the current value (non-negative) of the modification counter of
     * this AST
     */
    public long modificationCount() {
        return this.modificationCount.get();
    }

    /**
     * Indicates that this AST is about to be modified.
     * <p>
     * The following things count as modifying an AST:
     * <ul>
     * <li>creating a new node owned by this AST</li>
     * <li>adding a child to a node owned by this AST</li>
     * <li>removing a child from a node owned by this AST</li>
     * <li>setting a non-node attribute of a node owned by this AST</li>.
     * </ul>
     * <p>
     * N.B. This method may be called several times in the course
     * of a single client operation.
     * </p>
     */
    void modifying() {
        // when this method is called during lazy init, events are disabled
        // and the modification count will not be increased
        if (this.disableEvents.get() > 0) {
            return;
        }
        // increase the modification count
        this.modificationCount.incrementAndGet();
    }

    /**
     * A local method to workaround calling deprecated method in array type.
     * 
     * @deprecated
     */
    private void setArrayComponentType(ArrayType arrayType, Type type) {
        arrayType.setComponentType(type);
    }

    /**
     * Creates and returns a new unparented annotation type declaration
     * node for an unspecified, but legal, name; no modifiers; no javadoc;
     * and an empty list of member declarations.
     *
     * @return a new unparented annotation type declaration node
     * @exception UnsupportedOperationException if this operation is used in
     * a JLS2 AST
     * @since 3.1
     */
    public AnnotationTypeDeclaration newAnnotationTypeDeclaration() {
        return new AnnotationTypeDeclaration(this);
    }

    /**
     * Creates and returns a new unparented array type node with the given
     * element type, which cannot be an array type for API levels JLS8 and later.
     * By default, the array type has one non-annotated dimension.
     * <p>
     * For JLS4 and before, the given component type may be another array type.
     * </p>
     *
     * @param elementType element type for API level JLS8 and later, or the
     * component type (possibly another array type) for levels less than JLS8
     * @return a new unparented array type node
     * @exception IllegalArgumentException if:
     * <ul>
     * <li>the node belongs to a different AST</li>
     * <li>the node already has a parent</li>
     * <li>API level is JLS8 or later and type is an array type</li>
     * </ul>
     */
    public ArrayType newArrayType(Type elementType) {
        ArrayType result;
        if (this.apiLevel < JLS8_INTERNAL) {
            result = new ArrayType(this);
            setArrayComponentType(result, elementType);
            return result;
        }
        if (elementType.isArrayType()) {
            throw new IllegalArgumentException();
        }
        result = new ArrayType(this);
        result.setElementType(elementType);
        return result;
    }

    /**
     * Creates and returns a new unparented array type node with the given
     * element type and number of dimensions.
     * <p>
     * For JLS4 and before, the element type passed in can be an array type, but in that case, the
     * element type of the result will not be the same as what was passed in.
     * For JLS4 and before, the dimensions cannot be 0.
     * </p>
     *
     * @param elementType the element type (cannot be an array type for JLS8 and later)
     * @param dimensions the number of dimensions, a non-negative number
     * @return a new unparented array type node
     * @exception IllegalArgumentException if:
     * <ul>
     * <li>the node belongs to a different AST</li>
     * <li>the node already has a parent</li>
     * <li>the element type is null</li>
     * <li>the number of dimensions is lower than 0 (for JLS4 and before: lower than 1)</li>
     * <li>the number of dimensions is greater than 255</li>
     * <li>for levels from JLS8 and later, if the element type is an array type </li>
     * </ul>
     */
    public ArrayType newArrayType(Type elementType, int dimensions) {
        if (elementType == null) {
            throw new IllegalArgumentException();
        }
        ArrayType result;
        if (this.apiLevel < JLS8_INTERNAL) {
            if (dimensions < 1) {
                throw new IllegalArgumentException();
            }
            result = new ArrayType(this);
            setArrayComponentType(result, elementType);
            for (int i = 2; i <= dimensions; i++) {
                result = newArrayType(result);
            }
            return result;
        }
        // level >= JLS8
        if (elementType.isArrayType()) {
            throw new IllegalArgumentException();
        }
        result = new ArrayType(this, 0);
        result.setElementType(elementType);
        for (int i = 0; i < dimensions; ++i) {
            result.dimensions().add(new Dimension(this));
        }
        return result;

    }

    /**
     * Creates an unparented block node owned by this AST, for an empty list
     * of statements.
     *
     * @return a new unparented, empty block node
     */
    public Block newBlock() {
        return new Block(this);
    }

    // =============================== DECLARATIONS ===========================
    /**
     * Creates an unparented compilation unit node owned by this AST.
     * The compilation unit initially has no package declaration, no
     * import declarations, and no type declarations.
     *
     * @return the new unparented compilation unit node
     */
    public CompilationUnit newCompilationUnit() {
        return new CompilationUnit(this);
    }

    /**
     * Creates a new unparented expression statement node owned by this AST,
     * for the given expression.
     * <p>
     * This method can be used to convert an expression
     * (<code>Expression</code>) into a statement (<code>Type</code>)
     * by wrapping it. Note, however, that the result is only legal for
     * limited expression types, including method invocations, assignments,
     * and increment/decrement operations.
     * </p>
     *
     * @param expression the expression
     * @return a new unparented statement node
     * @exception IllegalArgumentException if:
     * <ul>
     * <li>the node belongs to a different AST</li>
     * <li>the node already has a parent</li>
     * </ul>
     */
    public ExpressionStatement newExpressionStatement(Expression expression) {
        ExpressionStatement result = new ExpressionStatement(this);
        result.setExpression(expression);
        return result;
    }

    /**
     * Creates and returns a new unparented annotatable dimension node
     * (Supported only in JLS8 level).
     *
     * @return a new unparented annotatable dimension node
     * @exception IllegalArgumentException if:
     * <ul>
     * <li>the node belongs to a different AST</li>
     * <li>the node already has a parent</li>
     * </ul>
     * @exception UnsupportedOperationException if this operation is used
     * in a JLS2, JLS3 or JLS4 AST
     * @since 3.10
     */
    public Dimension newDimension() {
        return new Dimension(this);
    }

    /**
     * Creates an unparented initializer node owned by this AST, with an
     * empty block. By default, the initializer has no modifiers and
     * an empty block.
     *
     * @return a new unparented initializer node
     */
    public Initializer newInitializer() {
        return new Initializer(this);
    }

    /**
     * Creates and returns a new doc comment region node.
     * Initially the new node has an empty list of tag elements
     * (and, for backwards compatability, an unspecified, but legal,
     * doc comment string)
     *
     * @return a new unparented doc comment node
     * @since 3.30
     */
    public JavaDocRegion newJavaDocRegion() {
        return new JavaDocRegion(this);
    }

    /**
     * Creates and returns a new text element node.
     * Initially the new node has an empty text string.
     * <p>
     * Note that this node type is used only inside doc comments
     * ({@link Javadoc Javadoc}).
     * </p>
     *
     * @return a new unparented JavaDoc text element node
     * @since 3.31
     */
    public JavaDocTextElement newJavaDocTextElement() {
        return new JavaDocTextElement(this);
    }

    /**
     * Creates and returns a new member reference node.
     * Initially the new node has no qualifier name and
     * an unspecified, but legal, member name.
     * <p>
     * Note that this node type is used only inside doc comments
     * ({@link Javadoc}).
     * </p>
     *
     * @return a new unparented member reference node
     * @since 3.0
     */
    public MemberRef newMemberRef() {
        return new MemberRef(this);
    }

    // =============================== COMMENTS ===========================

    /**
     * Creates and returns a new method reference node.
     * Initially the new node has no qualifier name,
     * an unspecified, but legal, method name, and an
     * empty parameter list.
     * <p>
     * Note that this node type is used only inside doc comments
     * ({@link Javadoc Javadoc}).
     * </p>
     *
     * @return a new unparented method reference node
     * @since 3.0
     */
    public MethodRef newMethodRef() {
        return new MethodRef(this);
    }

    /**
     * Creates and returns a new method reference node.
     * Initially the new node has an unspecified, but legal,
     * type, not variable arity, and no parameter name.
     * <p>
     * Note that this node type is used only inside doc comments
     * ({@link Javadoc}).
     * </p>
     *
     * @return a new unparented method reference parameter node
     * @since 3.0
     */
    public MethodRefParameter newMethodRefParameter() {
        return new MethodRefParameter(this);
    }

    /**
     * Creates and returns a new unparented modifier node for the given
     * modifier.
     *
     * @param keyword one of the modifier keyword constants
     * @return a new unparented modifier node
     * @exception IllegalArgumentException if the primitive type code is invalid
     * @exception UnsupportedOperationException if this operation is used in
     * a JLS2 AST
     * @since 3.1
     */
    public Modifier newModifier(Modifier.ModifierKeyword keyword) {
        Modifier result = new Modifier(this);
        result.setKeyword(keyword);
        return result;
    }

    /**
     * Creates and returns a list of new unparented modifier nodes
     * for the given modifier flags. When multiple modifiers are
     * requested, the modifier nodes will appear in the following order:
     * 
     * <pre>
     *  public protected private
     * abstract default static final synchronized native strictfp transient volatile
     * </pre>
     * 
     * <p>
     * This order is consistent with the recommendations in JLS8 ("*Modifier:" rules in chapters 8 and 9).
     * </p>
     *
     * @param flags bitwise or of modifier flags declared on {@link Modifier}
     * @return a possibly empty list of new unparented modifier nodes
     * (element type <code>Modifier</code>)
     * @exception UnsupportedOperationException if this operation is used in
     * a JLS2 AST
     * @since 3.1
     */
    public List newModifiers(int flags) {
        if (this.apiLevel == AST.JLS2) {
            unsupportedIn2();
        }
        List result = new ArrayList(3); // 3 modifiers is more than average
        if (Modifier.isPublic(flags)) {
            result.add(newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
        }
        if (Modifier.isProtected(flags)) {
            result.add(newModifier(Modifier.ModifierKeyword.PROTECTED_KEYWORD));
        }
        if (Modifier.isPrivate(flags)) {
            result.add(newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD));
        }
        if (Modifier.isAbstract(flags)) {
            result.add(newModifier(Modifier.ModifierKeyword.ABSTRACT_KEYWORD));
        }
        if (Modifier.isDefault(flags)) {
            result.add(newModifier(Modifier.ModifierKeyword.DEFAULT_KEYWORD));
        }
        if (Modifier.isStatic(flags)) {
            result.add(newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD));
        }
        if (Modifier.isFinal(flags)) {
            result.add(newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD));
        }
        if (Modifier.isSynchronized(flags)) {
            result.add(newModifier(Modifier.ModifierKeyword.SYNCHRONIZED_KEYWORD));
        }
        if (Modifier.isNative(flags)) {
            result.add(newModifier(Modifier.ModifierKeyword.NATIVE_KEYWORD));
        }
        if (Modifier.isStrictfp(flags)) {
            result.add(newModifier(Modifier.ModifierKeyword.STRICTFP_KEYWORD));
        }
        if (Modifier.isTransient(flags)) {
            result.add(newModifier(Modifier.ModifierKeyword.TRANSIENT_KEYWORD));
        }
        if (Modifier.isVolatile(flags)) {
            result.add(newModifier(Modifier.ModifierKeyword.VOLATILE_KEYWORD));
        }
        if (Modifier.isSealed(flags)) {
            result.add(newModifier(Modifier.ModifierKeyword.SEALED_KEYWORD));
        }
        if (Modifier.isNonSealed(flags)) {
            result.add(newModifier(Modifier.ModifierKeyword.NON_SEALED_KEYWORD));
        }
        if (Modifier.isModule(flags)) {
            result.add(newModifier(Modifier.ModifierKeyword.MODULE_KEYWORD));
        }
        return result;
    }

    /**
     * Creates and returns a new unparented module declaration
     * node for an unspecified, but legal, name; no modifiers; no javadoc;
     * and an empty list of statements.
     *
     * @return a new unparented module declaration node
     * @exception UnsupportedOperationException if this operation is used in an AST with level less than JLS9
     * @since 3.14
     */
    public ModuleDeclaration newModuleDeclaration() {
        return new ModuleDeclaration(this);
    }

    /**
     * Creates and returns a new unparented name node for the given name.
     * The name string must consist of 1 or more name segments separated
     * by single dots '.'. Returns a {@link QualifiedName} if the name has
     * dots, and a {@link SimpleName} otherwise. Each of the name
     * segments should be legal Java identifiers (this constraint may or may
     * not be enforced), and there must be at least one name segment.
     * The string must not contains white space, '&lt;', '&gt;',
     * '[', ']', or other any other characters that are not
     * part of the Java identifiers or separating '.'s.
     *
     * @param qualifiedName string consisting of 1 or more name segments,
     * each of which is a legal Java identifier, separated by single dots '.'
     * @return a new unparented name node
     * @exception IllegalArgumentException if:
     * <ul>
     * <li>the string is empty</li>
     * <li>the string begins or ends in a '.'</li>
     * <li>the string has adjacent '.'s</li>
     * <li>the segments between the '.'s are not valid Java identifiers</li>
     * </ul>
     * @since 3.1
     */
    public Name newName(String qualifiedName) {
        StringTokenizer t = new StringTokenizer(qualifiedName, ".", true); //$NON-NLS-1$
        Name result = null;
        // balance is # of name tokens - # of period tokens seen so far
        // initially 0; finally 1; should never drop < 0 or > 1
        int balance = 0;
        while (t.hasMoreTokens()) {
            String s = t.nextToken();
            if (s.indexOf('.') >= 0) {
                // this is a delimiter
                if (s.length() > 1) {
                    // too many dots in a row
                    throw new IllegalArgumentException();
                }
                balance--;
                if (balance < 0) {
                    throw new IllegalArgumentException();
                }
            } else {
                // this is an identifier segment
                balance++;
                SimpleName name = newSimpleName(s);
                if (result == null) {
                    result = name;
                } else {
                    result = newQualifiedName(result, name);
                }
            }
        }
        if (balance != 1) {
            throw new IllegalArgumentException();
        }
        return result;
    }

    /**
     * Creates and returns a new unparented name node for the given name
     * segments. Returns a simple name if there is only one name segment, and
     * a qualified name if there are multiple name segments. Each of the name
     * segments should be legal Java identifiers (this constraint may or may
     * not be enforced), and there must be at least one name segment.
     *
     * @param identifiers a list of 1 or more name segments, each of which
     * is a legal Java identifier
     * @return a new unparented name node
     * @exception IllegalArgumentException if:
     * <ul>
     * <li>the identifier is invalid</li>
     * <li>the list of identifiers is empty</li>
     * </ul>
     */
    public Name newName(String[] identifiers) {
        // update internalSetName(String[] if changed
        int count = identifiers.length;
        if (count == 0) {
            throw new IllegalArgumentException();
        }
        Name result = newSimpleName(identifiers[0]);
        for (int i = 1; i < count; i++) {
            SimpleName name = newSimpleName(identifiers[i]);
            result = newQualifiedName(result, name);
        }
        return result;
    }

    /**
     * Creates and returns a new unparented null literal node.
     *
     * @return a new unparented null literal node
     */
    public NullLiteral newNullLiteral() {
        return new NullLiteral(this);
    }

    /**
     * Creates and returns a new unparented null pattern node .
     *
     * @return a new unparented null pattern node
     * @since 3.28
     */
    public NullPattern newNullPattern() {
        return new NullPattern(this);
    }

    /**
     * Creates and returns a new unparented primitive type node with the given
     * type code.
     *
     * @param typeCode one of the primitive type code constants declared in
     * <code>PrimitiveType</code>
     * @return a new unparented primitive type node
     * @exception IllegalArgumentException if the primitive type code is invalid
     */
    public PrimitiveType newPrimitiveType(PrimitiveType.Code typeCode) {
        PrimitiveType result = new PrimitiveType(this);
        result.setPrimitiveTypeCode(typeCode);
        return result;
    }

    /**
     * Creates and returns a new unparented qualified name node for the given
     * qualifier and simple name child node.
     *
     * @param qualifier the qualifier name node
     * @param name the simple name being qualified
     * @return a new unparented qualified name node
     * @exception IllegalArgumentException if:
     * <ul>
     * <li>the node belongs to a different AST</li>
     * <li>the node already has a parent</li>
     * </ul>
     */
    public QualifiedName newQualifiedName(Name qualifier, SimpleName name) {
        QualifiedName result = new QualifiedName(this);
        result.setQualifier(qualifier);
        result.setName(name);
        return result;

    }

    // =============================== NAMES ===========================
    /**
     * Creates and returns a new unparented simple name node for the given
     * identifier. The identifier should be a legal Java identifier, but not
     * a keyword, boolean literal ("true", "false") or null literal ("null").
     *
     * @param identifier the identifier
     * @return a new unparented simple name node
     * @exception IllegalArgumentException if the identifier is invalid
     */
    public SimpleName newSimpleName(String identifier) {
        if (identifier == null) {
            throw new IllegalArgumentException();
        }
        SimpleName result = new SimpleName(this);
        result.setIdentifier(identifier);
        return result;
    }

    // =============================== TYPES ===========================
    /**
     * Creates and returns a new unparented simple type node with the given
     * type name.
     * <p>
     * This method can be used to convert a name (<code>Name</code>) into a
     * type (<code>Type</code>) by wrapping it.
     * </p>
     *
     * @param typeName the name of the class or interface
     * @return a new unparented simple type node
     * @exception IllegalArgumentException if:
     * <ul>
     * <li>the node belongs to a different AST</li>
     * <li>the node already has a parent</li>
     * </ul>
     */
    public SimpleType newSimpleType(Name typeName) {
        SimpleType result = new SimpleType(this);
        result.setName(typeName);
        return result;
    }

    /**
     * Creates an unparented single variable declaration node owned by this AST.
     * By default, the declaration is for a variable with an unspecified, but
     * legal, name and type; no modifiers; no array dimensions after the
     * variable; no initializer; not variable arity.
     *
     * @return a new unparented single variable declaration node
     */
    public SingleVariableDeclaration newSingleVariableDeclaration() {
        return new SingleVariableDeclaration(this);
    }

    // =============================== EXPRESSIONS ===========================

    /**
     * Creates and returns a new tag element node.
     * Initially the new node has no tag name and an empty list of fragments and properties.
     * <p>
     * Note that this node type is used only inside doc comments
     * ({@link Javadoc}).
     * </p>
     *
     * @return a new unparented tag element node
     * @since 3.0
     */
    public TagElement newTagElement() {
        return new TagElement(this);
    }

    /**
     * Creates and returns a new tag property node.
     * Initially the new node has no property name and value.
     * <p>
     * Note that this node type is used only inside doc comments
     * ({@link Javadoc}).
     * </p>
     *
     * @return a new unparented tag element node
     * @since 3.30
     */
    public TagProperty newTagProperty() {
        return new TagProperty(this);
    }

    /**
     * Creates and returns a new text element node.
     * Initially the new node has an empty text string.
     * <p>
     * Note that this node type is used only inside doc comments
     * ({@link Javadoc Javadoc}).
     * </p>
     *
     * @return a new unparented text element node
     * @since 3.0
     */
    public TextElement newTextElement() {
        return new TextElement(this);
    }

    /**
     * Creates an unparented class declaration node owned by this AST.
     * The name of the class is an unspecified, but legal, name;
     * no modifiers; no doc comment; no superclass or superinterfaces;
     * and an empty class body.
     * <p>
     * To create an interface, use this method and then call
     * <code>TypeDeclaration.setInterface(true)</code>.
     * </p>
     *
     * @return a new unparented type declaration node
     */
    public TypeDeclaration newTypeDeclaration() {
        TypeDeclaration result = new TypeDeclaration(this);
        result.setInterface(false);
        return result;
    }

    /**
     * Creates an unparented variable declaration fragment node owned by this
     * AST. By default, the fragment is for a variable with an unspecified, but
     * legal, name; no extra array dimensions; and no initializer.
     *
     * @return a new unparented variable declaration fragment node
     */
    public VariableDeclarationFragment newVariableDeclarationFragment() {
        return new VariableDeclarationFragment(this);
    }

    // =============================== STATEMENTS ===========================

    /**
     * Creates and returns a new unparented wildcard type node with no
     * type bound.
     *
     * @return a new unparented wildcard type node
     * @exception UnsupportedOperationException if this operation is used in
     * a JLS2 AST
     * @since 3.1
     */
    public WildcardType newWildcardType() {
        return new WildcardType(this);
    }

    /**
     * Reports that the given node has just gained a child.
     *
     * @param node the node that was modified
     * @param child the node that was added as a child
     * @param property the child or child list property descriptor
     * @since 3.0
     */
    void postAddChildEvent(ASTNode node, ASTNode child, StructuralPropertyDescriptor property) {
        // IMPORTANT: this method is called by readers during lazy init
        synchronized (this.internalASTLock) {
            // guard against concurrent access by a reader doing lazy init
            if (this.disableEvents.get() > 0) {
                // doing lazy init OR already processing an event
                // System.out.println("[BOUNCE ADD]");
                return;
            } else {
                disableEvents();
            }
        }
        try {
            this.eventHandler.postAddChildEvent(node, child, property);
            // N.B. even if event handler blows up, the AST is not
            // corrupted since node has already been changed
        } finally {
            reenableEvents();
        }
    }

    /**
     * Reports that the given node has just been cloned.
     *
     * @param node the node that was cloned
     * @param clone the clone of <code>node</code>
     * @since 3.0
     */
    void postCloneNodeEvent(ASTNode node, ASTNode clone) {
        synchronized (this.internalASTLock) {
            // guard against concurrent access by a reader doing lazy init
            if (this.disableEvents.get() > 0) {
                // doing lazy init OR already processing an event
                // System.out.println("[BOUNCE CLONE]");
                return;
            } else {
                disableEvents();
            }
        }
        try {
            this.eventHandler.postCloneNodeEvent(node, clone);
            // N.B. even if event handler blows up, the AST is not
            // corrupted since node has already been changed
        } finally {
            reenableEvents();
        }
    }

    /**
     * Reports that the given node jsut lost a child.
     *
     * @param node the node that was modified
     * @param child the child node that was removed
     * @param property the child or child list property descriptor
     * @since 3.0
     */
    void postRemoveChildEvent(ASTNode node, ASTNode child, StructuralPropertyDescriptor property) {
        // IMPORTANT: this method is called by readers during lazy init
        synchronized (this.internalASTLock) {
            // guard against concurrent access by a reader doing lazy init
            if (this.disableEvents.get() > 0) {
                // doing lazy init OR already processing an event
                // System.out.println("[BOUNCE DEL]");
                return;
            } else {
                disableEvents();
            }
        }
        try {
            this.eventHandler.postRemoveChildEvent(node, child, property);
            // N.B. even if event handler blows up, the AST is not
            // corrupted since node has not been changed yet
        } finally {
            reenableEvents();
        }
    }

    /**
     * Reports that the given node has just had a child replaced.
     *
     * @param node the node modified
     * @param child the child removed
     * @param newChild the replacement child
     * @param property the child or child list property descriptor
     * @since 3.0
     */
    void postReplaceChildEvent(ASTNode node, ASTNode child, ASTNode newChild, StructuralPropertyDescriptor property) {
        // IMPORTANT: this method is called by readers during lazy init
        synchronized (this.internalASTLock) {
            // guard against concurrent access by a reader doing lazy init
            if (this.disableEvents.get() > 0) {
                // doing lazy init OR already processing an event
                // System.out.println("[BOUNCE REP]");
                return;
            } else {
                disableEvents();
            }
        }
        try {
            this.eventHandler.postReplaceChildEvent(node, child, newChild, property);
            // N.B. even if event handler blows up, the AST is not
            // corrupted since node has not been changed yet
        } finally {
            reenableEvents();
        }
    }

    /**
     * Reports that the given node has just changed the value of a
     * non-child property.
     *
     * @param node the node that was modified
     * @param property the property descriptor
     * @since 3.0
     */
    void postValueChangeEvent(ASTNode node, SimplePropertyDescriptor property) {
        // IMPORTANT: this method is called by readers during lazy init
        synchronized (this.internalASTLock) {
            // guard against concurrent access by a reader doing lazy init
            if (this.disableEvents.get() > 0) {
                // doing lazy init OR already processing an event
                // System.out.println("[BOUNCE CHANGE]");
                return;
            } else {
                disableEvents();
            }
        }
        try {
            this.eventHandler.postValueChangeEvent(node, property);
            // N.B. even if event handler blows up, the AST is not
            // corrupted since node has already been changed
        } finally {
            reenableEvents();
        }
    }

    /**
     * Reports that the given node is about to gain a child.
     *
     * @param node the node that to be modified
     * @param child the node that to be added as a child
     * @param property the child or child list property descriptor
     * @since 3.0
     */
    void preAddChildEvent(ASTNode node, ASTNode child, StructuralPropertyDescriptor property) {
        // IMPORTANT: this method is called by readers during lazy init
        synchronized (this.internalASTLock) {
            // guard against concurrent access by a reader doing lazy init
            if (this.disableEvents.get() > 0) {
                // doing lazy init OR already processing an event
                // System.out.println("[BOUNCE ADD]");
                return;
            } else {
                disableEvents();
            }
        }
        try {
            this.eventHandler.preAddChildEvent(node, child, property);
            // N.B. even if event handler blows up, the AST is not
            // corrupted since node has already been changed
        } finally {
            reenableEvents();
        }
    }

    /**
     * Reports that the given node is about to be cloned.
     *
     * @param node the node to be cloned
     * @since 3.0
     */
    void preCloneNodeEvent(ASTNode node) {
        synchronized (this.internalASTLock) {
            // guard against concurrent access by a reader doing lazy init
            if (this.disableEvents.get() > 0) {
                // doing lazy init OR already processing an event
                // System.out.println("[BOUNCE CLONE]");
                return;
            } else {
                disableEvents();
            }
        }
        try {
            this.eventHandler.preCloneNodeEvent(node);
            // N.B. even if event handler blows up, the AST is not
            // corrupted since node has already been changed
        } finally {
            reenableEvents();
        }
    }

    /**
     * Reports that the given node is about to lose a child.
     *
     * @param node the node about to be modified
     * @param child the node about to be removed
     * @param property the child or child list property descriptor
     * @since 3.0
     */
    void preRemoveChildEvent(ASTNode node, ASTNode child, StructuralPropertyDescriptor property) {
        // IMPORTANT: this method is called by readers during lazy init
        synchronized (this.internalASTLock) {
            // guard against concurrent access by a reader doing lazy init
            if (this.disableEvents.get() > 0) {
                // doing lazy init OR already processing an event
                // System.out.println("[BOUNCE DEL]");
                return;
            } else {
                disableEvents();
            }
        }
        try {
            this.eventHandler.preRemoveChildEvent(node, child, property);
            // N.B. even if event handler blows up, the AST is not
            // corrupted since node has not been changed yet
        } finally {
            reenableEvents();
        }
    }

    /**
     * Reports that the given node is about have a child replaced.
     *
     * @param node the node about to be modified
     * @param child the child node about to be removed
     * @param newChild the replacement child
     * @param property the child or child list property descriptor
     * @since 3.0
     */
    void preReplaceChildEvent(ASTNode node, ASTNode child, ASTNode newChild, StructuralPropertyDescriptor property) {
        // IMPORTANT: this method is called by readers during lazy init
        synchronized (this.internalASTLock) {
            // guard against concurrent access by a reader doing lazy init
            if (this.disableEvents.get() > 0) {
                // doing lazy init OR already processing an event
                // System.out.println("[BOUNCE REP]");
                return;
            } else {
                disableEvents();
            }
        }
        try {
            this.eventHandler.preReplaceChildEvent(node, child, newChild, property);
            // N.B. even if event handler blows up, the AST is not
            // corrupted since node has not been changed yet
        } finally {
            reenableEvents();
        }
    }

    /**
     * Reports that the given node is about to change the value of a
     * non-child property.
     *
     * @param node the node to be modified
     * @param property the property descriptor
     * @since 3.0
     */
    void preValueChangeEvent(ASTNode node, SimplePropertyDescriptor property) {
        // IMPORTANT: this method is called by readers during lazy init
        synchronized (this.internalASTLock) {
            // guard against concurrent access by a reader doing lazy init
            if (this.disableEvents.get() > 0) {
                // doing lazy init OR already processing an event
                // System.out.println("[BOUNCE CHANGE]");
                return;
            } else {
                disableEvents();
            }
        }
        try {
            this.eventHandler.preValueChangeEvent(node, property);
            // N.B. even if event handler blows up, the AST is not
            // corrupted since node has already been changed
        } finally {
            reenableEvents();
        }
    }

    /**
     * Enables the recording of changes to the given compilation
     * unit and its descendents. The compilation unit must have
     * been created by <code>ASTParser</code> and still be in
     * its original state. Once recording is on,
     * arbitrary changes to the subtree rooted at the compilation
     * unit are recorded internally. Once the modification has
     * been completed, call <code>rewrite</code> to get an object
     * representing the corresponding edits to the original
     * source code string.
     *
     * @exception IllegalArgumentException if this compilation unit is
     * marked as unmodifiable, or if this compilation unit has already
     * been tampered with, or if recording has already been enabled,
     * or if <code>root</code> is not owned by this AST
     * @see CompilationUnit#recordModifications()
     * @since 3.0
     */
    void recordModifications(CompilationUnit root) {
        if (this.modificationCount.get() != this.originalModificationCount) {
            throw new IllegalArgumentException("AST is already modified"); //$NON-NLS-1$
        } else if (this.rewriter != null) {
            throw new IllegalArgumentException("AST modifications are already recorded"); //$NON-NLS-1$
        } else if ((root.getFlags() & ASTNode.PROTECT) != 0) {
            throw new IllegalArgumentException("Root node is unmodifiable"); //$NON-NLS-1$
        } else if (root.getAST() != this) {
            throw new IllegalArgumentException("Root node is not owned by this ast"); //$NON-NLS-1$
        }

        this.rewriter = new InternalASTRewrite(root);
        setEventHandler(this.rewriter);
    }

    // =============================== ANNOTATIONS ====================

    /**
     * Reenable events.
     * This method is thread-safe for AST readers.
     *
     * @see #disableEvents()
     * @since 3.0
     */
    final void reenableEvents() {
        synchronized (this.internalASTLock) {
            // guard against concurrent access by another reader
            this.disableEvents.decrementAndGet();
        }
    }

    /**
     * Returns the type binding for a "well known" type.
     * <p>
     * Note that bindings are generally unavailable unless requested when the
     * AST is being built.
     * </p>
     * <p>
     * The following type names are supported:
     * <ul>
     * <li><code>"boolean"</code></li>
     * <li><code>"byte"</code></li>
     * <li><code>"char"</code></li>
     * <li><code>"double"</code></li>
     * <li><code>"float"</code></li>
     * <li><code>"int"</code></li>
     * <li><code>"long"</code></li>
     * <li><code>"short"</code></li>
     * <li><code>"void"</code></li>
     * <li><code>"java.lang.AssertionError"</code> (since 3.7)</li>
     * <li><code>"java.lang.Boolean"</code> (since 3.1)</li>
     * <li><code>"java.lang.Byte"</code> (since 3.1)</li>
     * <li><code>"java.lang.Character"</code> (since 3.1)</li>
     * <li><code>"java.lang.Class"</code></li>
     * <li><code>"java.lang.Cloneable"</code></li>
     * <li><code>"java.lang.Double"</code> (since 3.1)</li>
     * <li><code>"java.lang.Error"</code></li>
     * <li><code>"java.lang.Exception"</code></li>
     * <li><code>"java.lang.Float"</code> (since 3.1)</li>
     * <li><code>"java.lang.Integer"</code> (since 3.1)</li>
     * <li><code>"java.lang.Long"</code> (since 3.1)</li>
     * <li><code>"java.lang.Object"</code></li>
     * <li><code>"java.lang.RuntimeException"</code></li>
     * <li><code>"java.lang.Short"</code> (since 3.1)</li>
     * <li><code>"java.lang.String"</code></li>
     * <li><code>"java.lang.StringBuffer"</code></li>
     * <li><code>"java.lang.Throwable"</code></li>
     * <li><code>"java.lang.Void"</code> (since 3.1)</li>
     * <li><code>"java.io.Serializable"</code></li>
     * </ul>
     *
     * @param name the name of a well known type
     * @return the corresponding type binding, or <code>null</code> if the
     * named type is not considered well known or if no binding can be found
     * for it
     */
    public ITypeBinding resolveWellKnownType(String name) {
        if (name == null) {
            return null;
        }
        return getBindingResolver().resolveWellKnownType(name);
    }

    /**
     * Converts all modifications recorded into an object
     * representing the corresponding text edits to the
     * given document containing the original source
     * code for the compilation unit that gave rise to
     * this AST.
     *
     * @param document original document containing source code
     * for the compilation unit
     * @param options the table of formatter options
     * (key type: <code>String</code>; value type: <code>String</code>);
     * or <code>null</code> to use the standard global options
     * {@link JavaCore#getOptions() JavaCore.getOptions()}.
     * @return text edit object describing the changes to the
     * document corresponding to the recorded AST modifications
     * @exception IllegalArgumentException if the document passed is
     * <code>null</code> or does not correspond to this AST
     * @exception IllegalStateException if <code>recordModifications</code>
     * was not called to enable recording
     * @see CompilationUnit#rewrite(IDocument, Map)
     * @since 3.0
     */
    TextEdit rewrite(IDocument document, Map options) {
        if (document == null) {
            throw new IllegalArgumentException();
        }
        if (this.rewriter == null) {
            throw new IllegalStateException("Modifications record is not enabled"); //$NON-NLS-1$
        }
        return this.rewriter.rewriteAST(document, options);
    }

    /**
     * Sets the binding resolver for this AST.
     *
     * @param resolver the new binding resolver for this AST
     */
    void setBindingResolver(BindingResolver resolver) {
        if (resolver == null) {
            throw new IllegalArgumentException();
        }
        this.resolver = resolver;
    }

    /**
     * Sets default node flags of new nodes of this AST.
     *
     * @param flag node flags of new nodes of this AST
     * @since 3.0
     */
    void setDefaultNodeFlag(int flag) {
        this.defaultNodeFlag = flag;
    }

    /**
     * Sets the event handler for this AST.
     *
     * @param eventHandler the event handler for this AST
     * @since 3.0
     */
    void setEventHandler(NodeEventHandler eventHandler) {
        if (this.eventHandler == null) {
            throw new IllegalArgumentException();
        }
        this.eventHandler = eventHandler;
    }

    void setFlag(int newValue) {
        this.bits |= newValue;
    }

    /**
     * Set <code>originalModificationCount</code> to the current modification count
     *
     * @since 3.0
     */
    void setOriginalModificationCount(long count) {
        this.originalModificationCount = count;
    }

    /**
     * Checks that this AST operation is not used when
     * building level JLS2 ASTs.
     * 
     * @exception UnsupportedOperationException
     * @since 3.0
     */
    void unsupportedIn2() {
        if (this.apiLevel == AST.JLS2) {
            throw new UnsupportedOperationException("Operation not supported in JLS2 AST"); //$NON-NLS-1$
        }
    }

    /**
     *
     * @return If previewEnabled flag is set to <code>true</code>, return <code>true</code> else <code>false</code>
     * @since 3.21
     * @noreference This method is not intended to be referenced by clients.
     */
    public boolean isPreviewEnabledSet() {
        return this.previewEnabled;
    }

    /**
     *
     * @return If preview is enabled and apiLevel is latest, return <code>true</code> else <code>false</code>
     * @since 3.19
     */
    public boolean isPreviewEnabled() {
        if (this.apiLevel == AST.JLS_INTERNAL_Latest && this.previewEnabled) {
            return true;
        }
        return false;
    }

    /**
     * Returns latest supported JLS level
     *
     * @return the latest supported JLS level
     * @since 3.27
     */
    public static int getJLSLatest() {
        return JLS_INTERNAL_Latest;
    }

    /**
     * Returns all {@link AST}{@code #JLS*} levels in the order of their
     * introduction. For e.g., {@link AST#JLS8} appears before {@link AST#JLS10}
     *
     * @return all available versions
     * @since 3.41
     */
    public static List<Integer> getAllVersions() {
        return ALL_VERSIONS;
    }

}
