/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *     James Blackburn (Broadcom Corp.) - ongoing development
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 473427
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.filesystem.URIUtil;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.utils.Messages;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IPathVariableManager;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResourceStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.ResourcesPlugin;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Preferences;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Status;

import java.net.URI;

/**
 * Core's implementation of IPathVariableManager.
 */
public class PathVariableManager implements IPathVariableManager {

    static final String VARIABLE_PREFIX = "pathvariable."; //$NON-NLS-1$

    private final Preferences preferences;

    /**
     * Constructor for the class.
     */
    public PathVariableManager() {
        this.preferences = ResourcesPlugin.getPlugin().getPluginPreferences();
    }

    /**
     * Return a key to use in the Preferences.
     */
    private String getKeyForName(String varName) {
        return VARIABLE_PREFIX + varName;
    }

    /**
     * Note that if a user changes the key in the preferences file to be invalid
     * and then calls #getValue using that key, they will get the value back for
     * that. But then if they try and call #setValue using the same key it will throw
     * an exception. We may want to revisit this behaviour in the future.
     *
     * @see org.eclipse.core.resources.IPathVariableManager#getValue(String)
     */
    @Deprecated
    @Override
    public IPath getValue(String varName) {
        String key = getKeyForName(varName);
        String value = preferences.getString(key);
        return value.isEmpty() ? null : IPath.fromPortableString(value);
    }

    @Deprecated
    @Override
    public IPath resolvePath(IPath path) {
        if (path == null || path.segmentCount() == 0 || path.isAbsolute() || path.getDevice() != null) {
            return path;
        }
        IPath value = getValue(path.segment(0));
        return value == null ? path : value.append(path.removeFirstSegments(1));
    }

    @Override
    public URI resolveURI(URI uri) {
        if (uri == null || uri.isAbsolute()) {
            return uri;
        }
        String schemeSpecificPart = uri.getSchemeSpecificPart();
        if (schemeSpecificPart == null || schemeSpecificPart.isEmpty()) {
            return uri;
        }
        IPath raw = IPath.fromOSString(schemeSpecificPart);
        IPath resolved = resolvePath(raw);
        return raw == resolved ? uri : URIUtil.toURI(resolved);
    }

    /**
     * @see IPathVariableManager#validateValue(IPath)
     */
    @Override
    public IStatus validateValue(IPath value) {
        if (value != null && (!value.isValidPath(value.toString()) || !value.isAbsolute())) {
            String message = Messages.pathvar_invalidValue;
            return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
        }
        return Status.OK_STATUS;
    }

    /**
     * see IPathVariableManager#getURIValue(String)
     */
    @Override
    public URI getURIValue(String name) {
        IPath path = getValue(name);
        if (path != null) {
            return URIUtil.toURI(path);
        }
        return null;
    }

}
