package com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.checker.index.qual;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.framework.qual.InvisibleQualifier;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.framework.qual.SubtypeOf;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The top type for the SearchIndex type system. This indicates that the Index checker does not know
 * any arrays that this integer is a {@link SearchIndexFor search index} for.
 *
 * @checker_framework.manual #index-checker Index Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
@SubtypeOf({ })
@DefaultQualifierInHierarchy
@InvisibleQualifier
public @interface SearchIndexUnknown {
}
