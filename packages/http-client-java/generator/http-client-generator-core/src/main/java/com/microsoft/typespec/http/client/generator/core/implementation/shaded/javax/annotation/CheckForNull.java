package com.microsoft.typespec.http.client.generator.core.implementation.shaded.javax.annotation;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.javax.annotation.meta.TypeQualifierNickname;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.javax.annotation.meta.When;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The annotated element might be null, and uses of the element should check for null.
 * <p>
 * When this annotation is applied to a method it applies to the method return value.
 */
@Documented
@TypeQualifierNickname
@Nonnull(when = When.MAYBE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckForNull {

}
