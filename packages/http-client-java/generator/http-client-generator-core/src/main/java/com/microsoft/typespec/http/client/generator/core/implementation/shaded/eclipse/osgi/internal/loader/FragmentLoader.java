/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.loader;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.ModuleLoader;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.ModuleRevision;
import java.net.URL;
import java.util.*;

public class FragmentLoader extends ModuleLoader {

    @Override
    protected List<URL> findEntries(String path, String filePattern, int options) {
        return Collections.emptyList();
    }

    @Override
    protected ClassLoader getClassLoader() {
        return null;
    }

    @Override
    protected boolean getAndSetTrigger() {
        // nothing to do here
        return false;
    }

    @Override
    public boolean isTriggerSet() {
        // nothing to do here
        return false;
    }

    @Override
    protected void loadFragments(Collection<ModuleRevision> fragments) {
        // do nothing
    }

}
