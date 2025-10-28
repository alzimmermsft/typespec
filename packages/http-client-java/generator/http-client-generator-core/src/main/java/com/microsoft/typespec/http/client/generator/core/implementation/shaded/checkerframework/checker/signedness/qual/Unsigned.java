package com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.checker.signedness.qual;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.framework.qual.DefaultFor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.framework.qual.SubtypeOf;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.framework.qual.TypeKind;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.framework.qual.UpperBoundFor;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The value is to be interpreted as unsigned. That is, if the most significant bit in the bitwise
 * representation is set, then the bits should be interpreted as a large positive number rather than
 * as a negative number.
 *
 * @checker_framework.manual #signedness-checker Signedness Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
@SubtypeOf({ UnknownSignedness.class })
@DefaultFor(typeKinds = { TypeKind.CHAR }, types = { Character.class })
@UpperBoundFor(typeKinds = { TypeKind.CHAR }, types = { Character.class })
public @interface Unsigned {
}
