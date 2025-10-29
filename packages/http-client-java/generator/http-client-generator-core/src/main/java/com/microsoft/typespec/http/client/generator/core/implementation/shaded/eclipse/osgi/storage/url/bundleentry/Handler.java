/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
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

package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage.url.bundleentry;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.ModuleContainer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage.bundlefile.BundleEntry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage.url.BundleResourceHandler;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * URLStreamHandler the bundleentry protocol.
 */

public class Handler extends BundleResourceHandler {

    public Handler(ModuleContainer container, BundleEntry bundleEntry) {
        super(container, bundleEntry);
    }

    @Override
    public URLConnection openConnection(URL u) throws IOException {
        return null;
    }
}
