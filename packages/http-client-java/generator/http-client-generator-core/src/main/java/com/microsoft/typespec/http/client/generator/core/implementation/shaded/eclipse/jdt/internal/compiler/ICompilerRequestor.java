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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler;

/**
 * A callback interface for receiving compilation results.
 */
public interface ICompilerRequestor {

    /**
     * Accept a compilation result.
     */
    void acceptResult(CompilationResult result);

    /**
     * Optionally called to start multiple {@link #acceptResult(CompilationResult)}
     */
    default void startBatch() {
        // nothing
    }

    /**
     * Optionally called after some {@link #acceptResult(CompilationResult)} to signal a good point in time
     */
    default void flushBatch() {
        // nothing
    }

    /**
     * if {@link #startBatch} was called then endBatch is called to finalize possibly multiple
     * {@link #acceptResult(CompilationResult)}
     */
    default void endBatch() {
        // nothing
    }
}
