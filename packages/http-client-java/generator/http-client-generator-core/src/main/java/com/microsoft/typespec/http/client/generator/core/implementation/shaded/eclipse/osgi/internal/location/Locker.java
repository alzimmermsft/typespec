/*******************************************************************************
 * Copyright (c) 2004, 2013 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.location;

import java.io.IOException;

/**
 * Internal class.
 */
public interface Locker {
    boolean lock() throws IOException;

    boolean isLocked() throws IOException;

    void release();

    class MockLocker implements Locker {
        @Override
        public boolean lock() throws IOException {
            // locking always successful
            return true;
        }

        @Override
        public boolean isLocked() {
            // this lock is never locked
            return false;
        }

        @Override
        public void release() {
            // nothing to release
        }

    }
}
