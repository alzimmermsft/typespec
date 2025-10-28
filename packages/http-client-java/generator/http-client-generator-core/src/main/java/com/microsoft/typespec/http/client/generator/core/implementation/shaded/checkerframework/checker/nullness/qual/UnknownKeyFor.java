package com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.checker.nullness.qual;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.framework.qual.DefaultFor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.framework.qual.InvisibleQualifier;
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
 * Used internally by the type system; should never be written by a programmer.
 *
 * <p>Indicates that the value assigned to the annotated variable is not known to be a key for any
 * map. It is the top type qualifier in the {@link KeyFor} hierarchy. It is also the default type
 * qualifier.
 *
 * @checker_framework.manual #map-key-checker Map Key Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
@InvisibleQualifier
@SubtypeOf({ })
@DefaultQualifierInHierarchy
@DefaultFor(value = TypeUseLocation.LOWER_BOUND, types = Void.class)
@QualifierForLiterals(LiteralKind.NULL)
public @interface UnknownKeyFor {
}
