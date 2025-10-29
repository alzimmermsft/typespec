package com.microsoft.typespec.http.client.generator.core.implementation.shaded.javax.annotation;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.javax.annotation.meta.TypeQualifier;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.javax.annotation.meta.When;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@TypeQualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface PropertyKey {
    When when() default When.ALWAYS;
}
