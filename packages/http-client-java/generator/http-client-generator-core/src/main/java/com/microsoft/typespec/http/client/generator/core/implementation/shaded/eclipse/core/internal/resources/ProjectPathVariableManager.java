/*******************************************************************************
 * Copyright (c) 2008, 2015 Freescale Semiconductor and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Serge Beauchamp (Freescale Semiconductor) - initial API and implementation
 *     IBM Corporation - ongoing development
 *     James Blackburn (Broadcom Corp.) - ongoing development
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 473427
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.filesystem.URIUtil;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources.ProjectVariableProviderManager.Descriptor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IPathVariableManager;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Status;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * The {@link IPathVariableManager} for a single project
 * 
 * @see IProject#getPathVariableManager()
 */
public class ProjectPathVariableManager implements IPathVariableManager {

    private final Resource resource;
    private final Descriptor variableProviders[];

    /**
     * Constructor for the class.
     */
    public ProjectPathVariableManager(Resource resource) {
        this.resource = resource;
        variableProviders = ProjectVariableProviderManager.getDefault().getDescriptors();
    }

    PathVariableManager getWorkspaceManager() {
        return (PathVariableManager) resource.getWorkspace().getPathVariableManager();
    }

    /**
     * @deprecated use {@link #getURIValue(String)} instead.
     */
    @Deprecated
    @Override
    public IPath getValue(String varName) {
        URI uri = getURIValue(varName);
        if (uri != null) {
            return URIUtil.toPath(uri);
        }
        return null;
    }

    public String internalGetValue(String varName) {
        HashMap<String, VariableDescription> map;
        try {
            map = ((ProjectDescription) resource.getProject().getDescription()).getVariables();
        } catch (CoreException e) {
            return null;
        }
        if (map != null && map.containsKey(varName)) {
            return map.get(varName).getValue();
        }

        String name;
        int index = varName.indexOf('-');
        if (index != -1) {
            name = varName.substring(0, index);
        } else {
            name = varName;
        }
        for (Descriptor variableProvider : variableProviders) {
            if (variableProvider.getName().equals(name)) {
                return variableProvider.getValue(varName, resource);
            }
        }
        for (Descriptor variableProvider : variableProviders) {
            if (name.startsWith(variableProvider.getName())) {
                return variableProvider.getValue(varName, resource);
            }
        }
        return null;
    }

    /**
     * @deprecated use {@link #resolveURI(URI)} instead.
     */
    @Override
    @Deprecated
    public IPath resolvePath(IPath path) {
        if (path == null || path.segmentCount() == 0 || path.isAbsolute() || path.getDevice() != null) {
            return path;
        }
        URI value = resolveURI(URIUtil.toURI(path));
        return value == null ? path : URIUtil.toPath(value);
    }

    public URI resolveVariable(String variable) {
        LinkedList<String> variableStack = new LinkedList<>();

        String value = resolveVariable(variable, variableStack);
        if (value != null) {
            try {
                return URI.create(value);
            } catch (IllegalArgumentException e) {
                return URIUtil.toURI(IPath.fromPortableString(value));
            }
        }
        return null;
    }

    public String resolveVariable(String value, LinkedList<String> variableStack) {
        if (variableStack == null) {
            variableStack = new LinkedList<>();
        }

        String tmp = internalGetValue(value);
        if (tmp == null) {
            URI result = getWorkspaceManager().getURIValue(value);
            if (result != null) {
                return result.toASCIIString();
            }
        } else {
            value = tmp;
        }

        while (true) {
            String stringValue;
            try {
                URI uri = URI.create(value);
                if (uri != null) {
                    IPath path = URIUtil.toPath(uri);
                    if (path != null) {
                        stringValue = path.toPortableString();
                    } else {
                        stringValue = value;
                    }
                } else {
                    stringValue = value;
                }
            } catch (IllegalArgumentException e) {
                stringValue = value;
            }
            // we check if the value contains referenced variables with ${VAR}
            int index = stringValue.indexOf("${"); //$NON-NLS-1$
            if (index != -1) {
                int endIndex = PathVariableUtil.getMatchingBrace(stringValue, index);
                String macro = stringValue.substring(index + 2, endIndex);
                String resolvedMacro = ""; //$NON-NLS-1$
                if (!variableStack.contains(macro)) {
                    variableStack.add(macro);
                    resolvedMacro = resolveVariable(macro, variableStack);
                    if (resolvedMacro == null) {
                        resolvedMacro = ""; //$NON-NLS-1$
                    }
                }
                if (stringValue.length() > endIndex) {
                    stringValue = stringValue.substring(0, index) + resolvedMacro + stringValue.substring(endIndex + 1);
                } else {
                    stringValue = resolvedMacro;
                }
                value = stringValue;
            } else {
                break;
            }
        }
        return value;
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
        if (raw == null || raw.segmentCount() == 0 || raw.isAbsolute() || raw.getDevice() != null) {
            return URIUtil.toURI(raw);
        }
        URI value = resolveVariable(raw.segment(0));
        if (value == null) {
            return uri;
        }

        String path = value.getPath();
        if (path != null) {
            IPath p = IPath.fromPortableString(path);
            p = p.append(raw.removeFirstSegments(1));
            try {
                value = new URI(value.getScheme(), value.getHost(), p.toPortableString(), value.getFragment());
            } catch (URISyntaxException e) {
                return uri;
            }
            return value;
        }
        return uri;
    }

    /**
     * @see IPathVariableManager#validateValue(IPath)
     */
    @Override
    public IStatus validateValue(IPath value) {
        // accept any format
        return Status.OK_STATUS;
    }

    /*
     * Return the resource of this manager.
     */
    public IResource getResource() {
        return resource;
    }

}
