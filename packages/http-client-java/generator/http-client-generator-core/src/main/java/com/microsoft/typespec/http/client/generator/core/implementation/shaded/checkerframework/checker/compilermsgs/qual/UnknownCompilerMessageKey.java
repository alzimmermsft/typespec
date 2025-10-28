package com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.checker.compilermsgs.qual;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.framework.qual.InvisibleQualifier;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.framework.qual.SubtypeOf;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A {@code String} that might or might not be a compiler message key.
 *
 * @checker_framework.manual #compilermsgs-checker Compiler Message Key Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
@InvisibleQualifier
@SubtypeOf({ })
@DefaultQualifierInHierarchy
public @interface UnknownCompilerMessageKey {
}
