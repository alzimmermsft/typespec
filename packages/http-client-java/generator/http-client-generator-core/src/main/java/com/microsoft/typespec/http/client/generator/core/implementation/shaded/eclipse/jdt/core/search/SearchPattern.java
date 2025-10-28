/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
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
 *     Microsoft Corporation - contribution for bug 575562 - improve completion search performance
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.search;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.OperationCanceledException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.CharOperation;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.index.EntryResult;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.index.Index;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.search.HierarchyScope;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.search.IndexQueryRequestor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.search.JavaSearchScope;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.search.indexing.QualifierQuery;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.search.indexing.QualifierQuery.QueryCategory;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * A search pattern defines how search results are found. Use <code>SearchPattern.createPattern</code>
 * to create a search pattern.
 * <p>
 * Search patterns are used during the search phase to decode index entries that were added during the indexing phase
 * (see {@link SearchDocument#addIndexEntry(char[], char[])}). When an index is queried, the
 * index categories and keys to consider are retrieved from the search pattern using {@link #getIndexCategories()} and
 * {@link #getIndexKey()}, as well as the match rule (see {@link #getMatchRule()}). A blank pattern is
 * then created (see {@link #getBlankPattern()}). This blank pattern is used as a record as follows.
 * For each index entry in the given index categories and that starts with the given key, the blank pattern is fed using
 * {@link #decodeIndexKey(char[])}. The original pattern is then asked if it matches the decoded key using
 * {@link #matchesDecodedKey(SearchPattern)}. If it matches, a search document is created for this index entry
 * using {@link SearchParticipant#getDocument(String)}.
 *
 * </p><p>
 * This class is intended to be sub-classed by clients. A default behavior is provided for each of the methods above,
 * that
 * clients can override if they wish.
 * </p>
 *
 * @since 3.0
 */
public abstract class SearchPattern {

    // Rules for pattern matching: (exact, prefix, pattern) [ | case sensitive]
    /**
     * Match rule: The search pattern matches exactly the search result,
     * that is, the source of the search result equals the search pattern.
     */
    public static final int R_EXACT_MATCH = 0;

    /**
     * Match rule: The search pattern is a prefix of the search result.
     */
    public static final int R_PREFIX_MATCH = 0x0001;

    /**
     * Match rule: The search pattern contains one or more wild cards ('*' or '?').
     * A '*' wild-card can replace 0 or more characters in the search result.
     * A '?' wild-card replaces exactly 1 character in the search result.
     */
    public static final int R_PATTERN_MATCH = 0x0002;

    /**
     * Match rule: The search pattern contains a regular expression.
     * <p><b>Warning:</b> Implemented only for module declaration search.
     * The support for this rule is <b>not yet implemented for others</b></p>
     */
    public static final int R_REGEXP_MATCH = 0x0004;

    /**
     * Match rule: The search pattern matches the search result only if cases are the same.
     * Can be combined to previous rules, e.g. {@link #R_EXACT_MATCH} | {@link #R_CASE_SENSITIVE}
     */
    public static final int R_CASE_SENSITIVE = 0x0008;

    /**
     * Match rule: The search pattern matches search results as raw/parameterized types/methods with same erasure.
     * This mode has no effect on other java elements search.<br>
     * Type search example:
     * <ul>
     * <li>pattern: <code>List&lt;Exception&gt;</code></li>
     * <li>match: <code>List&lt;Object&gt;</code></li>
     * </ul>
     * Method search example:
     * <ul>
     * <li>declaration: <code>&lt;T&gt;foo(T t)</code></li>
     * <li>pattern: <code>&lt;Exception&gt;foo(new Exception())</code></li>
     * <li>match: <code>&lt;Object&gt;foo(new Object())</code></li>
     * </ul>
     * Can be combined to all other match rules, e.g. {@link #R_CASE_SENSITIVE} | {@link #R_ERASURE_MATCH}
     * This rule is not activated by default, so raw types or parameterized types with same erasure will not be found
     * for pattern List&lt;String&gt;,
     * Note that with this pattern, the match selection will be only on the erasure even for parameterized types.
     * 
     * @since 3.1
     */
    public static final int R_ERASURE_MATCH = 0x0010;

    /**
     * Match rule: The search pattern matches search results as raw/parameterized types/methods with equivalent type
     * parameters.
     * This mode has no effect on other java elements search.<br>
     * Type search example:
     * <ul>
     * <li>pattern: <code>List&lt;Exception&gt;</code></li>
     * <li>match:
     * <ul>
     * <li><code>List&lt;? extends Throwable&gt;</code></li>
     * <li><code>List&lt;? super RuntimeException&gt;</code></li>
     * <li><code>List&lt;?&gt;</code></li>
     * </ul>
     * </li>
     * </ul>
     * Method search example:
     * <ul>
     * <li>declaration: <code>&lt;T&gt;foo(T t)</code></li>
     * <li>pattern: <code>&lt;Exception&gt;foo(new Exception())</code></li>
     * <li>match:
     * <ul>
     * <li><code>&lt;? extends Throwable&gt;foo(new Exception())</code></li>
     * <li><code>&lt;? super RuntimeException&gt;foo(new Exception())</code></li>
     * <li><code>foo(new Exception())</code></li>
     * </ul>
     * </ul>
     * Can be combined to all other match rules, e.g. {@link #R_CASE_SENSITIVE} | {@link #R_EQUIVALENT_MATCH}
     * This rule is not activated by default, so raw types or equivalent parameterized types will not be found
     * for pattern List&lt;String&gt;,
     * This mode is overridden by {@link #R_ERASURE_MATCH} as erasure matches obviously include equivalent ones.
     * That means that pattern with rule set to {@link #R_EQUIVALENT_MATCH} | {@link #R_ERASURE_MATCH}
     * will return same results than rule only set with {@link #R_ERASURE_MATCH}.
     * 
     * @since 3.1
     */
    public static final int R_EQUIVALENT_MATCH = 0x0020;

    /**
     * Match rule: The search pattern matches exactly the search result,
     * that is, the source of the search result equals the search pattern.
     * 
     * @since 3.1
     */
    public static final int R_FULL_MATCH = 0x0040;

    /**
     * Match rule: The search pattern contains a Camel Case expression.
     * <p>
     * Examples:
     * </p>
     * <ul>
     * <li>'NPE' type string pattern will match
     * 'NullPointerException' and 'NoPermissionException' types,</li>
     * <li>'NuPoEx' type string pattern will only match
     * 'NullPointerException' type.</li>
     * </ul>
     *
     * This rule is not intended to be combined with any other match rule. In case
     * of other match rule flags are combined with this one, then match rule validation
     * will return a modified rule in order to perform a better appropriate search request
     * (see {@link #validateMatchRule(String, int)} for more details).
     *
     * @see #camelCaseMatch(String, String) for a detailed explanation of Camel
     * Case matching.
     *
     * @since 3.2
     */
    public static final int R_CAMELCASE_MATCH = 0x0080;

    /**
     * Match rule: The search pattern contains a Camel Case expression with
     * a strict expected number of parts.
     * <br>
     * Examples:
     * <ul>
     * <li>'HM' type string pattern will match 'HashMap' and 'HtmlMapper' types,
     * but not 'HashMapEntry'
     * </li>
     * <li>'HMap' type string pattern will still match previous 'HashMap' and
     * 'HtmlMapper' types, but not 'HighMagnitude'
     * </li>
     * </ul>
     *
     * This rule is not intended to be combined with any other match rule. In case
     * of other match rule flags are combined with this one, then match rule validation
     * will return a modified rule in order to perform a better appropriate search request
     * (see {@link #validateMatchRule(String, int)} for more details).
     *
     * @see CharOperation#camelCaseMatch(char[], char[], boolean) for a detailed
     * explanation of Camel Case matching.
     *
     * @since 3.4
     */
    public static final int R_CAMELCASE_SAME_PART_COUNT_MATCH = 0x0100;

    /**
     * Match rule: The search pattern contains a substring expression in a case-insensitive way.
     * <p>
     * Examples:
     * <ul>
     * <li>'bar' string pattern will match
     * 'bar1', 'Bar' and 'removeBar' types,</li>
     * </ul>
     *
     * This rule is not intended to be combined with any other match rule. In case
     * of other match rule flags are combined with this one, then match rule validation
     * will return a modified rule in order to perform a better appropriate search request
     * (see {@link #validateMatchRule(String, int)} for more details).
     *
     * <p>
     * This is implemented only for code assist and not available for normal search.
     *
     * @since 3.12
     */
    public static final int R_SUBSTRING_MATCH = 0x0200;

    /**
     * Match rule: The search pattern contains a subword expression in a case-insensitive way.
     * <p>
     * Examples:
     * <ul>
     * <li>'addlist' string pattern will match
     * 'addListener' and 'addChangeListener'</li>
     * </ul>
     *
     * This rule is not intended to be combined with any other match rule. In case
     * of other match rule flags are combined with this one, then match rule validation
     * will return a modified rule in order to perform a better appropriate search request
     * (see {@link #validateMatchRule(String, int)} for more details).
     *
     * <p>
     * This is implemented only for code assist and not available for normal search.
     *
     * @noreference This is not intended to be referenced by clients as it is a part of Java preview feature.
     * @since 3.21
     */
    public static final int R_SUBWORD_MATCH = 0x0400;

    private static final int MODE_MASK = R_EXACT_MATCH | R_PREFIX_MATCH | R_PATTERN_MATCH | R_REGEXP_MATCH
        | R_CAMELCASE_MATCH | R_CAMELCASE_SAME_PART_COUNT_MATCH | R_SUBSTRING_MATCH | R_SUBWORD_MATCH;

    private final int matchRule;

    /**
     * The focus element (used for reference patterns)
     * 
     * @noreference This field is not intended to be referenced by clients.
     */
    public IJavaElement focus;

    /**
     * The encoded index qualifier query which is used to narrow down number of indexes to search based on the
     * qualifier.
     * This is optional. In absence all indexes provided by scope will be searched.
     * <br>
     * The encoded query format is as following
     * 
     * <pre>
     * CATEGORY1[,CATEGORY2]:SIMPLE_KEY:QUALIFIED_KEY
     * </pre>
     * 
     * if the category is not provided, then the index qualifier search will be done for all type of qualifiers.
     *
     * @noreference This field is not intended to be referenced by clients.
     * @see QualifierQuery#encodeQuery(QueryCategory[], char[], char[])
     */
    public char[] indexQualifierQuery;

    /**
     * @noreference This field is not intended to be referenced by clients.
     */
    public int kind;

    /**
     * @noreference This field is not intended to be referenced by clients.
     */
    public boolean mustResolve = true;

    /**
     * Creates a search pattern with the rule to apply for matching index keys.
     * It can be exact match, prefix match, pattern match or regexp match.
     * Rule can also be combined with a case sensitivity flag.
     *
     * @param matchRule one of following match rule
     * <ul>
     * <li>{@link #R_EXACT_MATCH}</li>
     * <li>{@link #R_PREFIX_MATCH}</li>
     * <li>{@link #R_PATTERN_MATCH}</li>
     * <li>{@link #R_REGEXP_MATCH}</li>
     * <li>{@link #R_CAMELCASE_MATCH}</li>
     * <li>{@link #R_CAMELCASE_SAME_PART_COUNT_MATCH}</li>
     * </ul>
     * which may be also combined with one of following flag:
     * <ul>
     * <li>{@link #R_CASE_SENSITIVE}</li>
     * <li>{@link #R_ERASURE_MATCH}</li>
     * <li>{@link #R_EQUIVALENT_MATCH}</li>
     * </ul>
     * For example,
     * <ul>
     * <li>{@link #R_EXACT_MATCH} | {@link #R_CASE_SENSITIVE}: if an exact
     * and case sensitive match is requested,</li>
     * <li>{@link #R_PREFIX_MATCH} if a case insensitive prefix match is requested</li>
     * <li>{@link #R_EXACT_MATCH} | {@link #R_ERASURE_MATCH}: if a case
     * insensitive and erasure match is requested.</li>
     * </ul>
     * Note that {@link #R_ERASURE_MATCH} or {@link #R_EQUIVALENT_MATCH} has no effect
     * on non-generic types/methods search.
     * <p>
     * Note also that default behavior for generic types/methods search is to find exact matches.
     */
    public SearchPattern(int matchRule) {
        int rule = matchRule;
        // Set full match implicit mode
        if ((matchRule & (R_EQUIVALENT_MATCH | R_ERASURE_MATCH)) == 0) {
            rule |= R_FULL_MATCH;
        }
        // reset other incompatible flags
        if ((matchRule & R_CAMELCASE_MATCH) != 0) {
            rule &= ~R_CAMELCASE_SAME_PART_COUNT_MATCH;
            rule &= ~R_PREFIX_MATCH;
        } else if ((matchRule & R_CAMELCASE_SAME_PART_COUNT_MATCH) != 0) {
            rule &= ~R_PREFIX_MATCH;
        }
        this.matchRule = rule;
    }

    /**
     * @noreference This method is not intended to be referenced by clients.
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     */
    public void acceptMatch(String relativePath, String containerPath, char separator, SearchPattern pattern,
        IndexQueryRequestor requestor, SearchParticipant participant, IJavaSearchScope scope,
        IProgressMonitor monitor) {

        if (scope instanceof JavaSearchScope) {
            JavaSearchScope javaSearchScope = (JavaSearchScope) scope;
            // Get document path access restriction from java search scope
            // Note that requestor has to verify if needed whether the document violates the access restriction or not
            AccessRuleSet access = javaSearchScope.getAccessRuleSet(relativePath, containerPath);
            if (access != JavaSearchScope.NOT_ENCLOSED) { // scope encloses the document path
                StringBuilder documentPath = new StringBuilder(containerPath.length() + 1 + relativePath.length());
                documentPath.append(containerPath);
                documentPath.append(separator);
                documentPath.append(relativePath);
                if (!requestor.acceptIndexMatch(documentPath.toString(), pattern, participant, access))
                    throw new OperationCanceledException();
            }
        } else {
            StringBuilder buffer = new StringBuilder(containerPath.length() + 1 + relativePath.length());
            buffer.append(containerPath);
            buffer.append(separator);
            buffer.append(relativePath);
            String documentPath = buffer.toString();
            boolean encloses = (scope instanceof HierarchyScope)
                ? ((HierarchyScope) scope).encloses(documentPath, monitor)
                : scope.encloses(documentPath);
            if (encloses)
                if (!requestor.acceptIndexMatch(documentPath, pattern, participant, null))
                    throw new OperationCanceledException();

        }
    }

    /**
     * @noreference This method is not intended to be referenced by clients.
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     */
    public SearchPattern currentPattern() {
        return this;
    }

    /**
     * Decode the given index key in this pattern. The decoded index key is used by
     * {@link #matchesDecodedKey(SearchPattern)} to find out if the corresponding index entry
     * should be considered.
     * <p>
     * This method should be re-implemented in subclasses that need to decode an index key.
     * </p>
     *
     * @param key the given index key
     */
    public void decodeIndexKey(char[] key) {
        // called from findIndexMatches(), override as necessary
    }

    /**
     * Query a given index for matching entries. Assumes the sender has opened the index and will close when finished.
     *
     * @noreference This method is not intended to be referenced by clients.
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     */
    public void findIndexMatches(Index index, IndexQueryRequestor requestor, SearchParticipant participant,
        IJavaSearchScope scope, IProgressMonitor monitor) throws IOException {
        findIndexMatches(index, requestor, participant, scope, true, monitor);
    }

    /**
     * Query a given index for matching entries. Assumes the sender has
     * opened the index and will close when finished.
     *
     * This API provides a flag to control whether to skip resolving
     * document name for the matching entries. If a SearchPattern subclass
     * has a different implementation of index matching, they have to
     * override this API to support document name resolving feature.
     *
     * @param index the target index to query
     * @param requestor the search requestor
     * @param participant the search participant
     * @param scope the search scope where the search results should be found
     * @param resolveDocumentName whether to skip the document name resolving
     * for the matching entries
     * @param monitor a progress monitor
     *
     * @noreference This method is not intended to be referenced by clients.
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     */
    public void findIndexMatches(Index index, IndexQueryRequestor requestor, SearchParticipant participant,
        IJavaSearchScope scope, boolean resolveDocumentName, IProgressMonitor monitor) throws IOException {
        if (monitor != null && monitor.isCanceled())
            throw new OperationCanceledException();
        try {
            index.startQuery();
            SearchPattern pattern = currentPattern();
            EntryResult[] entries = pattern.queryIn(index);
            if (entries == null)
                return;

            String containerPath = index.containerPath;
            char separator = index.separator;
            for (EntryResult entry : entries) {
                if (monitor != null && monitor.isCanceled())
                    throw new OperationCanceledException();

                SearchPattern decodedResult = pattern.getBlankPattern();
                decodedResult.decodeIndexKey(entry.getWord());
                if (pattern.matchesDecodedKey(decodedResult)) {
                    // Since resolve document name is expensive, leave the decision to the search client
                    // to decide whether to do so.
                    if (resolveDocumentName) {
                        String[] names = entry.getDocumentNames(index);
                        for (String name : names)
                            acceptMatch(name, containerPath, separator, decodedResult, requestor, participant, scope,
                                monitor);
                    } else {
                        acceptMatch("", containerPath, separator, decodedResult, requestor, participant, scope, //$NON-NLS-1$
                            monitor);
                    }
                }
            }
        } finally {
            index.stopQuery();
        }
    }

    /**
     * Returns a blank pattern that can be used as a record to decode an index key.
     * <p>
     * Implementors of this method should return a new search pattern that is going to be used
     * to decode index keys.
     * </p>
     *
     * @return a new blank pattern
     * @see #decodeIndexKey(char[])
     */
    public abstract SearchPattern getBlankPattern();

    /**
     * Returns a key to find in relevant index categories, if null then all index entries are matched.
     * The key will be matched according to some match rule. These potential matches
     * will be further narrowed by the match locator, but precise match locating can be expensive,
     * and index query should be as accurate as possible so as to eliminate obvious false hits.
     * <p>
     * This method should be re-implemented in subclasses that need to narrow down the
     * index query.
     * </p>
     *
     * @return an index key from this pattern, or <code>null</code> if all index entries are matched.
     */
    public char[] getIndexKey() {
        return null; // called from queryIn(), override as necessary
    }

    /**
     * Returns an array of index categories to consider for this index query.
     * These potential matches will be further narrowed by the match locator, but precise
     * match locating can be expensive, and index query should be as accurate as possible
     * so as to eliminate obvious false hits.
     * <p>
     * This method should be re-implemented in subclasses that need to narrow down the
     * index query.
     * </p>
     *
     * @return an array of index categories
     */
    public char[][] getIndexCategories() {
        return CharOperation.NO_CHAR_CHAR; // called from queryIn(), override as necessary
    }

    /**
     * Returns the rule to apply for matching index keys. Can be exact match, prefix match, pattern match or regexp
     * match.
     * Rule can also be combined with a case sensitivity flag.
     *
     * @return one of R_EXACT_MATCH, R_PREFIX_MATCH, R_PATTERN_MATCH, R_REGEXP_MATCH combined with R_CASE_SENSITIVE,
     * e.g. R_EXACT_MATCH | R_CASE_SENSITIVE if an exact and case sensitive match is requested,
     * or R_PREFIX_MATCH if a prefix non case sensitive match is requested.
     */
    public final int getMatchRule() {
        return this.matchRule;
    }

    /**
     * @noreference This method is not intended to be referenced by clients.
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     */
    public boolean isPolymorphicSearch() {
        return false;
    }

    /**
     * Returns whether this pattern matches the given pattern (representing a decoded index key).
     * <p>
     * This method should be re-implemented in subclasses that need to narrow down the
     * index query.
     * </p>
     *
     * @param decodedPattern a pattern representing a decoded index key
     * @return whether this pattern matches the given pattern
     */
    public boolean matchesDecodedKey(SearchPattern decodedPattern) {
        return true; // called from findIndexMatches(), override as necessary if index key is encoded
    }

    /**
     * Returns whether the given name matches the given pattern.
     * <p>
     * This method should be re-implemented in subclasses that need to define how
     * a name matches a pattern.
     * </p>
     *
     * @param pattern the given pattern, or <code>null</code> to represent "*"
     * @param name the given name
     * @return whether the given name matches the given pattern
     */
    public boolean matchesName(char[] pattern, char[] name) {
        if (pattern == null)
            return true; // null is as if it was "*"
        if (name != null) {
            boolean isCaseSensitive = (this.matchRule & R_CASE_SENSITIVE) != 0;
            int matchMode = this.matchRule & MODE_MASK;
            boolean emptyPattern = pattern.length == 0;
            if (emptyPattern && (this.matchRule & R_PREFIX_MATCH) != 0)
                return true;
            boolean sameLength = pattern.length == name.length;
            boolean canBePrefix = name.length >= pattern.length;
            boolean matchFirstChar = !isCaseSensitive || emptyPattern || (name.length > 0 && pattern[0] == name[0]);

            if ((matchMode & R_SUBSTRING_MATCH) != 0) {
                if (CharOperation.substringMatch(pattern, name))
                    return true;
                matchMode &= ~R_SUBSTRING_MATCH;
            }
            if ((matchMode & SearchPattern.R_SUBWORD_MATCH) != 0) {
                if (CharOperation.subWordMatch(pattern, name))
                    return true;
                matchMode &= ~SearchPattern.R_SUBWORD_MATCH;
            }

            switch (matchMode) {
                case R_EXACT_MATCH:
                    if (sameLength && matchFirstChar) {
                        return CharOperation.equals(pattern, name, isCaseSensitive);
                    }
                    break;

                case R_PREFIX_MATCH:
                    if (canBePrefix && matchFirstChar) {
                        return CharOperation.prefixEquals(pattern, name, isCaseSensitive);
                    }
                    break;

                case R_PATTERN_MATCH:
                    if (!isCaseSensitive)
                        pattern = CharOperation.toLowerCase(pattern);
                    return CharOperation.match(pattern, name, isCaseSensitive);

                case SearchPattern.R_CAMELCASE_MATCH:
                    if (matchFirstChar && CharOperation.camelCaseMatch(pattern, name, false)) {
                        return true;
                    }
                    // only test case insensitive as CamelCase already verified prefix case sensitive
                    if (!isCaseSensitive && CharOperation.prefixEquals(pattern, name, false)) {
                        return true;
                    }
                    break;

                case SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH:
                    return matchFirstChar && CharOperation.camelCaseMatch(pattern, name, true);

                case R_REGEXP_MATCH:
                    return Pattern.matches(new String(pattern), new String(name));
            }
        }
        return false;
    }

    /**
     * @noreference This method is not intended to be referenced by clients.
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     */
    public EntryResult[] queryIn(Index index) throws IOException {
        return index.query(getIndexCategories(), getIndexKey(), getMatchRule());
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "SearchPattern"; //$NON-NLS-1$
    }

    /**
     * @since 3.25
     */
    @Override
    public SearchPattern clone() throws CloneNotSupportedException {
        return (SearchPattern) super.clone();
    }
}
