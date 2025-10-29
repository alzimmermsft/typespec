/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.search;

/**
 * <p>
 * This interface defines the constants used by the search engine.
 * </p>
 * <p>
 * This interface declares constants only.
 * </p>
 * 
 * @see org.eclipse.jdt.core.search.SearchEngine
 * &#064;noimplement  This interface is not intended to be implemented by clients.
 */
public interface IJavaSearchConstants {

    /**
     * The nature of searched element or the nature
     * of match in unknown.
     */
    int UNKNOWN = -1;

    /* Nature of searched element */

    /**
     * The searched element is a type, which may include classes, interfaces,
     * enums, and annotation types.
     *
     * &#064;category  searchFor
     */
    int TYPE = 0;

    /**
     * The searched element is a method.
     *
     * &#064;category  searchFor
     */
    int METHOD = 1;

    /**
     * The searched element is a package.
     *
     * &#064;category  searchFor
     */
    int PACKAGE = 2;

    /**
     * The searched element is a constructor.
     *
     * &#064;category  searchFor
     */
    int CONSTRUCTOR = 3;

    /**
     * The searched element is a field.
     *
     * &#064;category  searchFor
     */
    int FIELD = 4;

    /**
     * The searched element is a class.
     * More selective than using {@link #TYPE}.
     *
     * &#064;category  searchFor
     */
    int CLASS = 5;

    /**
     * The searched element is an interface.
     * More selective than using {@link #TYPE}.
     *
     * &#064;category  searchFor
     */
    int INTERFACE = 6;

    /**
     * The searched element is an enum.
     * More selective than using {@link #TYPE}.
     *
     * @since 3.1
     * &#064;category  searchFor
     */
    int ENUM = 7;

    /**
     * The searched element is an annotation type.
     * More selective than using {@link #TYPE}.
     *
     * @since 3.1
     * &#064;category  searchFor
     */
    int ANNOTATION_TYPE = 8;

    /**
     * The searched element is a module.
     * 
     * @since 3.14
     * &#064;category  searchFor
     */
    int MODULE = 12;
    /* Nature of match */

    /**
     * Return only type references used as the type of a field declaration.
     * <p>
     * When this flag is set, only {@link TypeReferenceMatch} matches will be
     * returned.
     * </p>
     * 
     * @since 3.4
     * &#064;category  limitTo
     */
    int FIELD_DECLARATION_TYPE_REFERENCE = 0x40;

    /**
     * Return only type references used as the type of a local variable declaration.
     * <p>
     * When this flag is set, only {@link TypeReferenceMatch} matches will be
     * returned.
     * </p>
     * 
     * @since 3.4
     * &#064;category  limitTo
     */
    int LOCAL_VARIABLE_DECLARATION_TYPE_REFERENCE = 0x80;

    /**
     * Return only type references used as the type of a method parameter
     * declaration.
     * <p>
     * When this flag is set, only {@link TypeReferenceMatch} matches will be
     * returned.
     * </p>
     * 
     * @since 3.4
     * &#064;category  limitTo
     */
    int PARAMETER_DECLARATION_TYPE_REFERENCE = 0x100;

    /**
     * Return only type references used as a super type or as a super interface.
     * <p>
     * When this flag is set, only {@link TypeReferenceMatch} matches will be
     * returned.
     * </p>
     * 
     * @since 3.4
     * &#064;category  limitTo
     */
    int SUPERTYPE_TYPE_REFERENCE = 0x200;

    /**
     * Return only type references used in a throws clause.
     * <p>
     * When this flag is set, only {@link TypeReferenceMatch} matches will be
     * returned.
     * </p>
     * 
     * @since 3.4
     * &#064;category  limitTo
     */
    int THROWS_CLAUSE_TYPE_REFERENCE = 0x400;

    /**
     * Return only type references used in a cast expression.
     * <p>
     * When this flag is set, only {@link TypeReferenceMatch} matches will be
     * returned.
     * </p>
     * 
     * @since 3.4
     * &#064;category  limitTo
     */
    int CAST_TYPE_REFERENCE = 0x800;

    /**
     * Return only type references used in a catch header.
     * <p>
     * When this flag is set, only {@link TypeReferenceMatch} matches will be
     * returned.
     * </p>
     * 
     * @since 3.4
     * &#064;category  limitTo
     */
    int CATCH_TYPE_REFERENCE = 0x1000;

    /**
     * Return only type references used in class instance creation.
     * <p>
     * When this flag is set, only {@link TypeReferenceMatch} matches will be
     * returned.
     * </p><p>
     * Example:
     * 
     * <pre>
     * public class Test {
     * 	Test() {}
     * 	static Test bar()  {
     * 		return new <i>Test</i>();
     * 	}
     * }
     * </pre>
     * 
     * Searching references to the type <code>Test</code> using this flag in the
     * above snippet will match only the reference in italic.
     * <p>
     * Note that array creations are not returned when using this flag.
     * </p>
     * 
     * @since 3.4
     * &#064;category  limitTo
     */
    int CLASS_INSTANCE_CREATION_TYPE_REFERENCE = 0x2000;

    /**
     * Return only type references used as a method return type.
     * <p>
     * When this flag is set, only {@link TypeReferenceMatch} matches will be
     * returned.
     * </p>
     * 
     * @since 3.4
     * &#064;category  limitTo
     */
    int RETURN_TYPE_REFERENCE = 0x4000;

    /**
     * Return only type references used in an import declaration.
     * <p>
     * When this flag is set, only {@link TypeReferenceMatch} matches will be
     * returned.
     * </p>
     * 
     * @since 3.4
     * &#064;category  limitTo
     */
    int IMPORT_DECLARATION_TYPE_REFERENCE = 0x8000;

    /**
     * Return only type references used as an annotation.
     * <p>
     * When this flag is set, only {@link TypeReferenceMatch} matches will be
     * returned.
     * </p>
     * 
     * @since 3.4
     * &#064;category  limitTo
     */
    int ANNOTATION_TYPE_REFERENCE = 0x10000;

    /**
     * Return only type references used as a type argument in a parameterized
     * type or a parameterized method.
     * <p>
     * When this flag is set, only {@link TypeReferenceMatch} matches will be
     * returned.
     * </p>
     * 
     * @since 3.4
     * &#064;category  limitTo
     */
    int TYPE_ARGUMENT_TYPE_REFERENCE = 0x20000;

    /**
     * Return only type references used as a type variable bound.
     * <p>
     * When this flag is set, only {@link TypeReferenceMatch} matches will be
     * returned.
     * </p>
     * 
     * @since 3.4
     * &#064;category  limitTo
     */
    int TYPE_VARIABLE_BOUND_TYPE_REFERENCE = 0x40000;

    /**
     * Return only type references used as a wildcard bound.
     * <p>
     * When this flag is set, only {@link TypeReferenceMatch} matches will be
     * returned.
     * </p>
     * 
     * @since 3.4
     * &#064;category  limitTo
     */
    int WILDCARD_BOUND_TYPE_REFERENCE = 0x80000;

    /**
     * Return only type references used as a type of an <code>instanceof</code>
     * expression.
     * <p>
     * When this flag is set, only {@link TypeReferenceMatch} matches will be
     * returned.
     * </p>
     * 
     * @since 3.4
     * &#064;category  limitTo
     */
    int INSTANCEOF_TYPE_REFERENCE = 0x100000;

    /**
     * Return only super field accesses or super method invocations (e.g. using the
     * <code>super</code> qualifier).
     * <p>
     * When this flag is set, the kind of returned matches will depend on the
     * specified nature of the searched element:
     * <ul>
     * <li>for the {@link #FIELD} nature, only {@link FieldReferenceMatch}
     * matches will be returned,</li>
     * <li>for the {@link #METHOD} nature, only {@link MethodReferenceMatch}
     * matches will be returned.</li>
     * </ul>
     * 
     * @since 3.4
     * &#064;category  limitTo
     */
    int SUPER_REFERENCE = 0x1000000;

    /**
     * Return only qualified field accesses or qualified method invocations.
     * <p>
     * When this flag is set, the kind of returned matches will depend on the
     * specified nature of the searched element:
     * <ul>
     * <li>for the {@link #FIELD} nature, only {@link FieldReferenceMatch}
     * matches will be returned,</li>
     * <li>for the {@link #METHOD} nature, only {@link MethodReferenceMatch}
     * matches will be returned.</li>
     * </ul>
     * 
     * @since 3.4
     * &#064;category  limitTo
     */
    int QUALIFIED_REFERENCE = 0x2000000;

    /**
     * Return only primary field accesses or primary method invocations (e.g. using
     * the <code>this</code> qualifier).
     * <p>
     * When this flag is set, the kind of returned matches will depend on the
     * specified nature of the searched element:
     * <ul>
     * <li>for the {@link #FIELD} nature, only {@link FieldReferenceMatch}
     * matches will be returned,</li>
     * <li>for the {@link #METHOD} nature, only {@link MethodReferenceMatch}
     * matches will be returned.</li>
     * </ul>
     * 
     * @since 3.4
     * &#064;category  limitTo
     */
    int THIS_REFERENCE = 0x4000000;

    /**
     * Return only field accesses or method invocations without any qualification.
     * <p>
     * When this flag is set, the kind of returned matches will depend on the
     * specified nature of the searched element:
     * <ul>
     * <li>for the {@link #FIELD} nature, only {@link FieldReferenceMatch}
     * matches will be returned,</li>
     * <li>for the {@link #METHOD} nature, only {@link MethodReferenceMatch}
     * matches will be returned.</li>
     * </ul>
     * 
     * @since 3.4
     * &#064;category  limitTo
     */
    int IMPLICIT_THIS_REFERENCE = 0x8000000;

    /**
     * Return only method reference expressions, e.g. <code>A::foo</code>.
     * <p>
     * When this flag is set, only {@link MethodReferenceMatch} matches will be
     * returned.
     * </p>
     * 
     * @since 3.10
     * &#064;category  limitTo
     */
    int METHOD_REFERENCE_EXPRESSION = 0x10000000;

    /**
     * Return only type references used as a permit type (Java 17)
     * <p>
     * When this flag is set, only {@link TypeReferenceMatch} matches will be
     * returned.
     * </p>
     * 
     * @since 3.24
     * &#064;category  limitTo
     */
    int PERMITTYPE_TYPE_REFERENCE = 0x20000000;

    /* Syntactic match modes */

    /**
     * The search pattern matches exactly the search result,
     * that is, the source of the search result equals the search pattern.
     *
     * @deprecated Use {@link SearchPattern#R_EXACT_MATCH} instead.
     * &#064;category  matchRule
     */
    int EXACT_MATCH = 0;
    /**
     * The search pattern is a prefix of the search result.
     *
     * @deprecated Use {@link SearchPattern#R_PREFIX_MATCH} instead.
     * &#064;category  matchRule
     */
    int PREFIX_MATCH = 1;
    /**
     * The search pattern contains one or more wild cards ('*') where a
     * wild-card can replace 0 or more characters in the search result.
     *
     * @deprecated Use {@link SearchPattern#R_PATTERN_MATCH} instead.
     * &#064;category  matchRule
     */
    int PATTERN_MATCH = 2;

    /* Case sensitivity */

    /**
     * The search pattern matches the search result only
     * if cases are the same.
     *
     * @deprecated Use the methods that take the matchMode
     * with {@link SearchPattern#R_CASE_SENSITIVE} as a matchRule instead.
     * &#064;category  matchRule
     */
    boolean CASE_SENSITIVE = true;
    /**
     * The search pattern ignores cases in the search result.
     *
     * @deprecated Use the methods that take the matchMode
     * without {@link SearchPattern#R_CASE_SENSITIVE} as a matchRule instead.
     * &#064;category  matchRule
     */
    boolean CASE_INSENSITIVE = false;

}
