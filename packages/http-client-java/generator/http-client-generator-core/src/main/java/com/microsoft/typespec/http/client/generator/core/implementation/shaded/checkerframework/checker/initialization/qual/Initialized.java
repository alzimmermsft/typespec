package com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.checker.initialization.qual;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.checker.nullness.qual.NonNull;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.framework.qual.DefaultFor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.framework.qual.SubtypeOf;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.framework.qual.TypeUseLocation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This type qualifier belongs to the freedom-before-commitment initialization tracking type-system.
 * This type-system is not used on its own, but in conjunction with some other type-system that
 * wants to ensure safe initialization. For instance, {@link
 * com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.checker.nullness.NullnessChecker}
 * uses freedom-before-commitment to track
 * initialization of {@link NonNull} fields.
 *
 * <p>This type qualifier indicates that the object has been fully initialized; reading fields from
 * such objects is fully safe and yields objects of the correct type.
 *
 * @checker_framework.manual #initialization-checker Initialization Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
@SubtypeOf(UnknownInitialization.class)
@DefaultQualifierInHierarchy
@DefaultFor({
    TypeUseLocation.IMPLICIT_UPPER_BOUND,
    TypeUseLocation.IMPLICIT_LOWER_BOUND,
    TypeUseLocation.EXCEPTION_PARAMETER })
public @interface Initialized {
}
