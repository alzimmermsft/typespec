/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
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
 *     Serge Beauchamp (Freescale Semiconductor) - [229633] Project Path Variable Support
 *     Anton Leherbauer (Wind River) - [198591] Allow Builder to specify scheduling rule
 *     Francis Lynch (Wind River) - [301563] Save and load tree snapshots
 *     Markus Schorn (Wind River) - [306575] Save snapshot location with project
 *     Broadcom Corporation - ongoing development
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 473427
 *     Christoph Läubrich - Issue #80 - CharsetManager access the ResourcesPlugin.getWorkspace before init
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.utils.Messages;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IContainer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IProjectDescription;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResourceStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.util.NLS;

public class Project extends Container implements IProject {

    protected Project(IPath path, Workspace container) {
        super(path, container);
    }

    /**
     * Checks that this resource is accessible. Typically this means that it
     * exists. In the case of projects, they must also be open.
     * If phantom is true, phantom resources are considered.
     *
     * @exception CoreException if this resource is not accessible
     */
    @Override
    public void checkAccessible(int flags) throws CoreException {
        super.checkAccessible(flags);
        if (!isOpen(flags)) {
            String message = NLS.bind(Messages.resources_mustBeOpen, getName());
            throw new ResourceException(IResourceStatus.PROJECT_NOT_OPEN, getFullPath(), message, null);
        }
    }

    @Override
    public IProjectDescription getDescription() throws CoreException {
        ResourceInfo info = getResourceInfo(false, false);
        checkAccessible(getFlags(info));
        ProjectDescription description = ((ProjectInfo) info).getDescription();
        // if the project is currently in the middle of being created, the description might not be available yet
        if (description == null) {
            checkAccessible(NULL_FLAG);
        }
        return (IProjectDescription) description.clone();
    }

    @Override
    public IContainer getParent() {
        return workspace.getRoot();
    }

    @Override
    public IProject getProject() {
        return this;
    }

    @Override
    public IPath getProjectRelativePath() {
        return IPath.EMPTY;
    }

    @Override
    public int getType() {
        return PROJECT;
    }

    @Override
    public IPath getWorkingLocation(String id) {
        if (id == null || !exists()) {
            return null;
        }
        IPath result = workspace.getMetaArea().getWorkingLocation(this, id);
        result.toFile().mkdirs();
        return result;
    }

    @Override
    public boolean hasNature(String natureID) throws CoreException {
        checkAccessible(getFlags(getResourceInfo(false, false)));
        // use #internal method to avoid copy but still throw an
        // exception if the resource doesn't exist.
        IProjectDescription desc = internalGetDescription();
        if (desc == null) {
            checkAccessible(NULL_FLAG);
        }
        return desc.hasNature(natureID);
    }

    /**
     * This is an internal helper method. This implementation is different from the API
     * method getDescription(). This one does not check the project accessibility. It exists
     * in order to prevent "chicken and egg" problems in places like the project creation.
     * It may return null.
     */
    public ProjectDescription internalGetDescription() {
        ProjectInfo info = (ProjectInfo) getResourceInfo(false, false);
        if (info == null) {
            return null;
        }
        return info.getDescription();
    }

    @Override
    public void internalSetLocal(boolean flag, int depth) throws CoreException {
        // do nothing for projects, but call for its children
        if (depth == IResource.DEPTH_ZERO) {
            return;
        }
        if (depth == IResource.DEPTH_ONE) {
            depth = IResource.DEPTH_ZERO;
        }
        // get the children via the workspace since we know that this
        // resource exists (it is local).
        IResource[] children = getChildren(IResource.NONE);
        for (IResource element : children) {
            ((Resource) element).internalSetLocal(flag, depth);
        }
    }

    @Override
    public boolean isAccessible() {
        return isOpen();
    }

    @Override
    public boolean isDerived(int options) {
        // projects are never derived
        return false;
    }

    @Override
    public boolean isLinked(int options) {
        return false;// projects are never linked
    }

    @Override
    public boolean isVirtual() {
        return false; // projects are never virtual
    }

    @Deprecated
    @Override
    public boolean isLocal(int depth) {
        // the flags parameter is ignored for projects so pass anything
        return isLocal(-1, depth);
    }

    @Deprecated
    @Override
    public boolean isLocal(int flags, int depth) {
        // don't check the flags....projects are always local
        if (depth == DEPTH_ZERO) {
            return true;
        }
        if (depth == DEPTH_ONE) {
            depth = DEPTH_ZERO;
        }
        // get the children via the workspace since we know that this
        // resource exists (it is local).
        IResource[] children = getChildren(IResource.NONE);
        for (IResource element : children) {
            if (!element.isLocal(depth)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isOpen() {
        ResourceInfo info = getResourceInfo(false, false);
        return isOpen(getFlags(info));
    }

    public boolean isOpen(int flags) {
        return flags != NULL_FLAG && ResourceInfo.isSet(flags, M_OPEN);
    }

}
