/*******************************************************************************
 * Copyright (c) 2005, 2020 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core;

/**
 * Completion context.
 *
 * Represent the context in which the completion occurs.
 *
 * @see CompletionRequestor#acceptContext(CompletionContext)
 * @since 3.1
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class CompletionContext {

    /**
     * Returns whether this completion context is an extended context.
     * Some methods of this context can be used only if this context is an extended context but an extended context
     * consumes more memory.
     *
     * @return <code>true</code> if this completion context is an extended context.
     *
     * @since 3.4
     */
    public boolean isExtended() {
        return false; // default overridden by concrete implementation
    }

    /**
     * Returns the completed token.
     * This token is either the identifier or Java language keyword
     * or the string literal under, immediately preceding,
     * the original request offset. If the original request offset
     * is not within or immediately after an identifier or keyword or
     * a string literal then the returned value is <code>null</code>.
     *
     * @return completed token or <code>null</code>
     * @since 3.2
     */
    public char[] getToken() {
        return null; // default overridden by concrete implementation
    }

    /**
     * Returns the character index of the start of the
     * subrange in the source file buffer containing the
     * relevant token being completed. This
     * token is either the identifier or Java language keyword
     * under, or immediately preceding, the original request
     * offset. If the original request offset is not within
     * or immediately after an identifier or keyword, then the
     * position returned is original request offset and the
     * token range is empty.
     *
     * @return character index of token start position (inclusive)
     * @since 3.2
     */
    public int getTokenStart() {
        return -1; // default overridden by concrete implementation
    }

    /**
     * Returns the offset position in the source file buffer
     * after which code assist is requested.
     *
     * @return offset position in the source file buffer
     * @since 3.2
     */
    public int getOffset() {
        return -1; // default overridden by concrete implementation
    }

}
