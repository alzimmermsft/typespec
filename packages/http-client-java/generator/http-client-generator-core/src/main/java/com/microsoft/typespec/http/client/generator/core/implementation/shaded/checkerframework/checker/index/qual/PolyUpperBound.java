package com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.checker.index.qual;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.checkerframework.framework.qual.PolymorphicQualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A polymorphic qualifier for the Upper Bound type system.
 *
 * @checker_framework.manual #index-checker Index Checker
 * @checker_framework.manual #qualifier-polymorphism Qualifier polymorphism
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
@PolymorphicQualifier(UpperBoundUnknown.class)
public @interface PolyUpperBound {
}
