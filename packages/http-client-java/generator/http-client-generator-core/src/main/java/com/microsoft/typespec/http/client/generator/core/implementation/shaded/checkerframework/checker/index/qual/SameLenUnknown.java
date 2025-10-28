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
 * This type represents any variable that isn't known to have the same length as another sequence.
 * This is the top type of the Same Length type system. Programmers should not need to write this
 * type.
 *
 * @checker_framework.manual #index-checker Index Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
@SubtypeOf({ })
@DefaultQualifierInHierarchy
@InvisibleQualifier
public @interface SameLenUnknown {
}
