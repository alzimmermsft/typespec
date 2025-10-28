package com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.checker.lock.qual;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.framework.qual.DefaultFor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.framework.qual.InvisibleQualifier;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.framework.qual.LiteralKind;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.framework.qual.QualifierForLiterals;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.framework.qual.SubtypeOf;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.framework.qual.TypeUseLocation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that an expression is not known to be {@link LockHeld}.
 *
 * <p>This annotation may not be written in source code; it is an implementation detail of the
 * checker.
 *
 * @see LockHeld
 * @checker_framework.manual #lock-checker Lock Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ })
@InvisibleQualifier
@SubtypeOf({ })
@DefaultQualifierInHierarchy
@DefaultFor(value = TypeUseLocation.LOWER_BOUND, types = Void.class)
@QualifierForLiterals(LiteralKind.NULL)
public @interface LockPossiblyHeld {
}
