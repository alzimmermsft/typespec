package com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.checker.index.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.framework.qual.InvisibleQualifier;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.framework.qual.SubtypeOf;

/**
 * The top qualifier for the LessThan type hierarchy. It indicates that no other expression is known
 * to be larger than the annotated one.
 *
 * @checker_framework.manual #index-inequalities Index Checker Inequalities
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})
@SubtypeOf({})
@DefaultQualifierInHierarchy
@InvisibleQualifier
public @interface LessThanUnknown {}
