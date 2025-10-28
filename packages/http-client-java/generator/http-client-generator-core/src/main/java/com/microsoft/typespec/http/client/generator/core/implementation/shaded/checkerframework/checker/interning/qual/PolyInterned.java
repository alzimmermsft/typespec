package com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.checker.interning.qual;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.framework.qual.PolymorphicQualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A polymorphic qualifier for the Interning type system.
 *
 * <p>Any method written using @PolyInterned conceptually has two versions: one in which every
 * instance of @PolyInterned has been replaced by @Interned, and one in which every instance
 * of @PolyInterned has been erased.
 *
 * @checker_framework.manual #interning-checker Interning Checker
 * @checker_framework.manual #qualifier-polymorphism Qualifier polymorphism
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
@PolymorphicQualifier(UnknownInterned.class)
public @interface PolyInterned {
}
