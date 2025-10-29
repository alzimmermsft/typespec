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

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.*;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.*;

public abstract class Container extends Resource implements IContainer {
    protected Container(IPath path, Workspace container) {
        super(path, container);
    }

    /**
     * Converts this resource and all its children into phantoms by modifying
     * their resource infos in-place.
     */
    @Override
    public void convertToPhantom() throws CoreException {
        if (isPhantom()) {
            return;
        }
        super.convertToPhantom();
        IResource[] members = members(
            IContainer.INCLUDE_PHANTOMS | IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS | IContainer.INCLUDE_HIDDEN);
        for (IResource member : members) {
            ((Resource) member).convertToPhantom();
        }
    }

    @Override
    public boolean exists(IPath childPath) {
        return workspace.getResourceInfo(getFullPath().append(childPath), false, false) != null;
    }

    @Override
    public IResource findMember(String memberPath) {
        return findMember(memberPath, false);
    }

    @Override
    public IResource findMember(String memberPath, boolean phantom) {
        IPath childPath = getFullPath().append(memberPath);
        ResourceInfo info = workspace.getResourceInfo(childPath, phantom, false);
        return info == null ? null : workspace.newResource(childPath, info.getType());
    }

    @Override
    public IResource findMember(IPath childPath) {
        return findMember(childPath, false);
    }

    @Override
    public IResource findMember(IPath childPath, boolean phantom) {
        childPath = getFullPath().append(childPath);
        ResourceInfo info = workspace.getResourceInfo(childPath, phantom, false);
        return (info == null) ? null : workspace.newResource(childPath, info.getType());
    }

    protected IResource[] getChildren(int memberFlags) {
        IPath[] children = null;
        try {
            children = workspace.tree.getChildren(path);
        } catch (IllegalArgumentException e) {
            // concurrency problem: the container has been deleted by another
            // thread during this call. Just return empty children set
        }
        if (children == null || children.length == 0) {
            return ICoreConstants.EMPTY_RESOURCE_ARRAY;
        }
        Resource[] result = new Resource[children.length];
        int found = 0;
        for (IPath child : children) {
            ResourceInfo info = workspace.getResourceInfo(child, true, false);
            if (info != null && isMember(info.getFlags(), memberFlags)) {
                result[found++] = workspace.newResource(child, info.getType());
            }
        }
        if (found == result.length) {
            return result;
        }
        Resource[] trimmedResult = new Resource[found];
        System.arraycopy(result, 0, trimmedResult, 0, found);
        return trimmedResult;
    }

    public IFile getFile(String name) {
        return (IFile) workspace.newResource(getFullPath().append(name), FILE);
    }

    @Override
    public IFile getFile(IPath childPath) {
        return (IFile) workspace.newResource(getFullPath().append(childPath), FILE);
    }

    public IFolder getFolder(String name) {
        return (IFolder) workspace.newResource(getFullPath().append(name), FOLDER);
    }

    @Override
    public IFolder getFolder(IPath childPath) {
        return (IFolder) workspace.newResource(getFullPath().append(childPath), FOLDER);
    }

    @Deprecated
    @Override
    public boolean isLocal(int flags, int depth) {
        if (!super.isLocal(flags, depth)) {
            return false;
        }
        if (depth == DEPTH_ZERO) {
            return true;
        }
        if (depth == DEPTH_ONE) {
            depth = DEPTH_ZERO;
        }
        // get the children via the workspace since we know that this
        // resource exists (it is local).
        IResource[] children = getChildren(IResource.NONE);
        for (IResource c : children) {
            if (!c.isLocal(depth)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public IResource[] members() throws CoreException {
        // forward to central method
        return members(IResource.NONE);
    }

    @Override
    public IResource[] members(int memberFlags) throws CoreException {
        final boolean phantom = (memberFlags & INCLUDE_PHANTOMS) != 0;
        ResourceInfo info = getResourceInfo(phantom, false);
        checkAccessible(getFlags(info));
        return getChildren(memberFlags);
    }
}
