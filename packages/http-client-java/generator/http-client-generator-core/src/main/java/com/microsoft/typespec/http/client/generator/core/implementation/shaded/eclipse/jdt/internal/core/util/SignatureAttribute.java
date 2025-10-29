/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.util.ClassFormatException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.util.IConstantPool;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.util.IConstantPoolConstant;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.util.IConstantPoolEntry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.util.ISignatureAttribute;

/**
 * @since 3.0
 */
public class SignatureAttribute extends ClassFileAttribute implements ISignatureAttribute {

    private final int signatureIndex;
    private final char[] signature;

    SignatureAttribute(byte[] classFileBytes, IConstantPool constantPool, int offset) throws ClassFormatException {
        super(classFileBytes, constantPool, offset);
        final int index = u2At(classFileBytes, 6, offset);
        this.signatureIndex = index;
        IConstantPoolEntry constantPoolEntry = constantPool.decodeEntry(index);
        if (constantPoolEntry.getKind() != IConstantPoolConstant.CONSTANT_Utf8) {
            throw new ClassFormatException(ClassFormatException.INVALID_CONSTANT_POOL_ENTRY);
        }
        this.signature = constantPoolEntry.getUtf8Value();
    }

    @Override
    public int getSignatureIndex() {
        return this.signatureIndex;
    }

    @Override
    public char[] getSignature() {
        return this.signature;
    }
}
