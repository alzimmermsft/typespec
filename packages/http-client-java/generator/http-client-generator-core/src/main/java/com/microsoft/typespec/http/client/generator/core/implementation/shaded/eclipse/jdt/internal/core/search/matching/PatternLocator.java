/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.search.matching;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.CharOperation;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.search.SearchMatch;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.search.SearchPattern;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.ASTNode;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.Annotation;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.Expression;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.LambdaExpression;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.MessageSend;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.ModuleDeclaration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.ModuleReference;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.Reference;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.ReferenceExpression;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.TypeParameter;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.TypeReference;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.IQualifiedTypeResolutionListener;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.search.indexing.IIndexConstants;

import java.util.regex.Pattern;

public abstract class PatternLocator implements IIndexConstants, IQualifiedTypeResolutionListener {

    // store pattern info
    protected int matchMode;
    protected boolean isCaseSensitive;
    protected boolean isEquivalentMatch;
    protected boolean isErasureMatch;
    protected boolean mustResolve;
    protected boolean mayBeGeneric;

    public boolean isCaseSensitive() {
        return this.isCaseSensitive;
    }

    public boolean isMustResolve() {
        return this.mustResolve;
    }

    // match to report
    SearchMatch match = null;

    /* match levels */
    public static final int IMPOSSIBLE_MATCH = 0;
    public static final int INACCURATE_MATCH = 1;
    public static final int POSSIBLE_MATCH = 2;
    public static final int ACCURATE_MATCH = 3;
    public static final int ERASURE_MATCH = 4;

    // Possible rule match flavors
    int flavors = 0;
    // see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=79866
    public static final int NO_FLAVOR = 0x0000;
    public static final int EXACT_FLAVOR = 0x0010;
    public static final int SUPER_INVOCATION_FLAVOR = 0x0200;
    public static final int SUB_INVOCATION_FLAVOR = 0x0400;
    public static final int OVERRIDDEN_METHOD_FLAVOR = 0x0800;
    public static final int SUPERTYPE_REF_FLAVOR = 0x1000;
    public static final int MATCH_LEVEL_MASK = 0x0F;
    public static final int FLAVORS_MASK = ~MATCH_LEVEL_MASK;

    /* match container */
    public static final int COMPILATION_UNIT_CONTAINER = 1;
    public static final int CLASS_CONTAINER = 2;
    public static final int METHOD_CONTAINER = 4;
    public static final int FIELD_CONTAINER = 8;
    public static final int ALL_CONTAINER = COMPILATION_UNIT_CONTAINER | CLASS_CONTAINER | METHOD_CONTAINER
        | FIELD_CONTAINER;

    public static char[] qualifiedPattern(char[] simpleNamePattern, char[] qualificationPattern) {
        // NOTE: if case insensitive search then simpleNamePattern & qualificationPattern are assumed to be lowercase
        if (simpleNamePattern == null) {
            if (qualificationPattern == null)
                return null;
            return CharOperation.concat(qualificationPattern, ONE_STAR, '.');
        } else {
            return qualificationPattern == null
                ? CharOperation.concat(ONE_STAR, simpleNamePattern)
                : CharOperation.concat(qualificationPattern, simpleNamePattern, '.');
        }
    }

    public static char[] qualifiedSourceName(TypeBinding binding) {
        if (binding instanceof ReferenceBinding) {
            ReferenceBinding type = (ReferenceBinding) binding;
            if (type.isLocalType())
                return type.isMemberType()
                    ? CharOperation.concat(qualifiedSourceName(type.enclosingType()), type.sourceName(), '.')
                    : CharOperation.concat(qualifiedSourceName(type.enclosingType()), new char[] { '.', '1', '.' },
                        type.sourceName());
        }
        return binding != null ? binding.qualifiedSourceName() : null;
    }

    public PatternLocator(SearchPattern pattern) {
        if (pattern != null) {
            int matchRule = pattern.getMatchRule();
            this.isCaseSensitive = (matchRule & SearchPattern.R_CASE_SENSITIVE) != 0;
            this.isErasureMatch = (matchRule & SearchPattern.R_ERASURE_MATCH) != 0;
            this.isEquivalentMatch = (matchRule & SearchPattern.R_EQUIVALENT_MATCH) != 0;
            this.matchMode = matchRule & JavaSearchPattern.MATCH_MODE_MASK;
            this.mustResolve = pattern.mustResolve;
        }
    }

    /*
     * Clear caches
     */
    protected void clear() {
        // nothing to clear by default
    }

    public int match(Annotation node, MatchingNodeSet nodeSet) {
        // each subtype should override if needed
        return IMPOSSIBLE_MATCH;
    }

    /**
     * Check if the given ast node syntactically matches this pattern.
     * If it does, add it to the match set.
     * Returns the match level.
     */
    public int match(ASTNode node, MatchingNodeSet nodeSet) { // needed for some generic nodes
        // each subtype should override if needed
        return IMPOSSIBLE_MATCH;
    }

    public int match(ConstructorDeclaration node, MatchingNodeSet nodeSet) {
        // each subtype should override if needed
        return IMPOSSIBLE_MATCH;
    }

    public int match(Expression node, MatchingNodeSet nodeSet) {
        // each subtype should override if needed
        return IMPOSSIBLE_MATCH;
    }

    public int match(FieldDeclaration node, MatchingNodeSet nodeSet) {
        // each subtype should override if needed
        return IMPOSSIBLE_MATCH;
    }

    public int match(LambdaExpression node, MatchingNodeSet nodeSet) {
        // each subtype should override if needed
        return IMPOSSIBLE_MATCH;
    }

    public int match(LocalDeclaration node, MatchingNodeSet nodeSet) {
        // each subtype should override if needed
        return IMPOSSIBLE_MATCH;
    }

    public int match(MethodDeclaration node, MatchingNodeSet nodeSet) {
        // each subtype should override if needed
        return IMPOSSIBLE_MATCH;
    }

    public int match(MemberValuePair node, MatchingNodeSet nodeSet) {
        // each subtype should override if needed
        return IMPOSSIBLE_MATCH;
    }

    public int match(MessageSend node, MatchingNodeSet nodeSet) {
        // each subtype should override if needed
        return IMPOSSIBLE_MATCH;
    }

    protected int match(ModuleDeclaration node, MatchingNodeSet nodeSet) {
        return IMPOSSIBLE_MATCH;
    }

    protected int match(ModuleReference node, MatchingNodeSet nodeSet) {
        return IMPOSSIBLE_MATCH;
    }

    public int match(Reference node, MatchingNodeSet nodeSet) {
        // each subtype should override if needed
        return IMPOSSIBLE_MATCH;
    }

    public int match(ReferenceExpression node, MatchingNodeSet nodeSet) {
        // each subtype should override if needed
        return IMPOSSIBLE_MATCH;
    }

    public int match(TypeDeclaration node, MatchingNodeSet nodeSet) {
        // each subtype should override if needed
        return IMPOSSIBLE_MATCH;
    }

    public int match(TypeParameter node, MatchingNodeSet nodeSet) {
        // each subtype should override if needed
        return IMPOSSIBLE_MATCH;
    }

    public int match(TypeReference node, MatchingNodeSet nodeSet) {
        // each subtype should override if needed
        return IMPOSSIBLE_MATCH;
    }

    protected int fineGrain() {
        return 0;
    }

    /**
     * Returns whether the given name matches the given pattern.
     */
    protected boolean matchesName(char[] pattern, char[] name) {
        if (pattern == null)
            return true; // null is as if it was "*"
        if (name == null)
            return false; // cannot match null name
        return matchNameValue(pattern, name) != IMPOSSIBLE_MATCH;
    }

    /**
     * Return how the given name matches the given pattern.
     *
     * @return Possible values are:
     * <ul>
     * 	<li> {@link #ACCURATE_MATCH}</li>
     * 	<li> {@link #IMPOSSIBLE_MATCH}</li>
     * </ul>
     * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=79866"
     */
    protected int matchNameValue(char[] pattern, char[] name) {
        if (pattern == null)
            return ACCURATE_MATCH; // null is as if it was "*"
        if (name == null)
            return IMPOSSIBLE_MATCH; // cannot match null name
        if (name.length == 0) { // empty name
            if (pattern.length == 0) { // can only matches empty pattern
                return ACCURATE_MATCH;
            }
            return IMPOSSIBLE_MATCH;
        } else if (pattern.length == 0) {
            return IMPOSSIBLE_MATCH; // need to have both name and pattern length==0 to be accurate
        }
        boolean matchFirstChar = !this.isCaseSensitive || pattern[0] == name[0];
        boolean sameLength = pattern.length == name.length;
        boolean canBePrefix = name.length >= pattern.length;
        switch (this.matchMode) {
            case SearchPattern.R_EXACT_MATCH:
                if (sameLength && matchFirstChar && CharOperation.equals(pattern, name, this.isCaseSensitive)) {
                    return POSSIBLE_MATCH | EXACT_FLAVOR;
                }
                break;

            case SearchPattern.R_PREFIX_MATCH:
                if (canBePrefix && matchFirstChar && CharOperation.prefixEquals(pattern, name, this.isCaseSensitive)) {
                    return POSSIBLE_MATCH;
                }
                break;

            case SearchPattern.R_PATTERN_MATCH:
                // TODO_PERFS (frederic) Not sure this lowercase is necessary
                if (!this.isCaseSensitive) {
                    pattern = CharOperation.toLowerCase(pattern);
                }
                if (CharOperation.match(pattern, name, this.isCaseSensitive)) {
                    return POSSIBLE_MATCH;
                }
                break;

            case SearchPattern.R_REGEXP_MATCH:
                if (Pattern.matches(new String(pattern), new String(name))) {
                    return POSSIBLE_MATCH;
                }
                break;

            case SearchPattern.R_CAMELCASE_MATCH:
                if (CharOperation.camelCaseMatch(pattern, name, false)) {
                    return POSSIBLE_MATCH;
                }
                // only test case insensitive as CamelCase same part count already verified prefix case sensitive
                if (!this.isCaseSensitive && CharOperation.prefixEquals(pattern, name, false)) {
                    return POSSIBLE_MATCH;
                }
                break;

            case SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH:
                if (CharOperation.camelCaseMatch(pattern, name, true)) {
                    return POSSIBLE_MATCH;
                }
                break;
        }
        return IMPOSSIBLE_MATCH;
    }

    protected int referenceType() {
        return 0; // defaults to unknown (a generic JavaSearchMatch will be created)
    }

    /**
     * Set the flavors for which the locator has to be focused on.
     * If not set, the locator will accept all matches with or without flavors.
     * When set, the locator will only accept match having the corresponding flavors.
     *
     * @param flavors Bits mask specifying the flavors to be accepted or
     * <code>0</code> to ignore the flavors while accepting matches.
     */
    void setFlavors(int flavors) {
        this.flavors = flavors;
    }

    @Override
    public String toString() {
        return "SearchPattern"; //$NON-NLS-1$
    }

    @Override
    public void recordResolution(QualifiedTypeReference typeReference, TypeBinding resolution) {
        // noop by default
    }
}
