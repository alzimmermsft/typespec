package com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.checker.tainting.qual;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.framework.qual.DefaultFor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.framework.qual.LiteralKind;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.framework.qual.QualifierForLiterals;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.framework.qual.SubtypeOf;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.framework.qual.TypeUseLocation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes a reference that is untainted, i.e. can be trusted.
 *
 * @checker_framework.manual #tainting-checker Tainting Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
@SubtypeOf(Tainted.class)
@QualifierForLiterals(LiteralKind.STRING)
@DefaultFor(TypeUseLocation.LOWER_BOUND)
public @interface Untainted {
}
