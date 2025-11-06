// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.typespec.http.client.generator.core.util;

import com.microsoft.typespec.http.client.generator.core.model.clientmodel.ClassType;
import com.microsoft.typespec.http.client.generator.core.model.clientmodel.GenericType;
import com.microsoft.typespec.http.client.generator.core.model.clientmodel.IType;
import java.util.Objects;

public class TypeUtil {
    private TypeUtil() {
    }

    /**
     * Whether the given type is GenericType and is subclass of either of the given classes.
     * 
     * @param type the type to check
     * @param parentClasses classes to match either one
     * @return whether the given type is GenericType and is subclass of either of the given classes
     */
    public static boolean isGenericTypeClassSubclassOf(IType type, ClassType... parentClasses) {
        if (!(type instanceof GenericType) || parentClasses == null || parentClasses.length == 0)
            return false;
        GenericType genericType = (GenericType) type;
        for (ClassType classType : parentClasses) {
            if (Objects.equals(genericType.getPackage(), classType.getPackage())
                && Objects.equals(genericType.getName(), classType.getName())) {
                return true;
            }
        }

        return false;
    }
}
