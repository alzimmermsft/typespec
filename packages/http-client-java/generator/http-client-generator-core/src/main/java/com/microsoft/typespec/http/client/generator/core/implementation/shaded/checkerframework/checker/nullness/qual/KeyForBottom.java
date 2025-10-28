package com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.checker.nullness.qual;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.framework.qual.InvisibleQualifier;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.framework.qual.SubtypeOf;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.framework.qual.TargetLocations;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.framework.qual.TypeUseLocation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The bottom type in the Map Key type system. Programmers should rarely write this type.
 *
 * <p>There are no values of this type (not even {@code null}).
 *
 * @checker_framework.manual #map-key-checker Map Key Checker
 * @checker_framework.manual #bottom-type the bottom type
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
@TargetLocations({ TypeUseLocation.EXPLICIT_LOWER_BOUND, TypeUseLocation.EXPLICIT_UPPER_BOUND })
@InvisibleQualifier
@SubtypeOf(KeyFor.class)
public @interface KeyForBottom {
}
