/*******************************************************************************
 *  Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  IBM - Initial API and implementation
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.runtime;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.util.NLS;

// Runtime plugin message catalog
public class Messages extends NLS {
    private static final String BUNDLE_NAME
        = "com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.runtime.messages"; //$NON-NLS-1$

    // metadata
    public static String meta_appNotInit;

    static {
        // load message values from bundle file
        reloadMessages();
    }

    public static void reloadMessages() {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
}
