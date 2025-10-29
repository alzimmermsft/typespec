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
 *     Francis Lynch (Wind River) - [301563] Save and load tree snapshots
 *     Broadcom Corporation - ongoing development
 *     Sergey Prigogin (Google) - [437005] Out-of-date .snap file prevents Eclipse from running
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 473427
 *     Mickael Istria (Red Hat Inc.) - Bug 488937
 *     Christoph Läubrich - Issue #77 - SaveManager access the ResourcesPlugin.getWorkspace at init phase
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.utils.Messages;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResourceStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.ResourcesPlugin;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Assert;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.util.NLS;

public class LocalMetaArea implements ICoreConstants {
    /* package */static final String F_BACKUP_FILE_EXTENSION = ".bak"; //$NON-NLS-1$

    /* package */static final String F_MARKERS = ".markers"; //$NON-NLS-1$
    /* package */static final String F_OLD_PROJECT = ".prj"; //$NON-NLS-1$
    /* package */static final String F_PROJECTS = ".projects"; //$NON-NLS-1$
    /* package */static final String F_REFRESH = ".refresh"; //$NON-NLS-1$
    /* package */static final String F_ROOT = ".root"; //$NON-NLS-1$
    /* package */static final String F_SNAP_EXTENSION = "snap"; //$NON-NLS-1$
    /* package */static final String F_SYNCINFO = ".syncinfo"; //$NON-NLS-1$
    /* package */static final String URI_PREFIX = "URI//"; //$NON-NLS-1$

    protected final IPath metaAreaLocation;

    /**
     * The project location is just stored as an optimization, to avoid recomputing it.
     */
    protected final IPath projectMetaLocation;
    private final Workspace workspace;

    public LocalMetaArea(Workspace workspace) {
        this.workspace = workspace;
        metaAreaLocation = ResourcesPlugin.getPlugin().getStateLocation();
        projectMetaLocation = metaAreaLocation.append(F_PROJECTS);
    }

    public void create(IProject target) {
        java.io.File file = locationFor(target).toFile();
        // make sure area is empty
        Workspace.clear(file);
        file.mkdirs();
    }

    /**
     * The project is being deleted. Delete all meta-data associated with the
     * project.
     */
    public void delete(IProject target) throws CoreException {
        IPath path = locationFor(target);
        if (!Workspace.clear(path.toFile()) && path.toFile().exists()) {
            String message = NLS.bind(Messages.resources_deleteMeta, target.getFullPath());
            throw new ResourceException(IResourceStatus.FAILED_DELETE_METADATA, target.getFullPath(), message, null);
        }
    }

    public IPath getBackupLocationFor(IPath file) {
        return file.removeLastSegments(1).append(file.lastSegment() + F_BACKUP_FILE_EXTENSION);
    }

    /**
     * Returns the local file system location which contains the META data for
     * the resources plugin (i.e., the entire workspace).
     */
    public IPath getLocation() {
        return metaAreaLocation;
    }

    /**
     * Returns the path of the file in which to save markers for the given
     * resource. Should only be called for the workspace root and projects.
     */
    public IPath getMarkersLocationFor(IResource resource) {
        Assert.isNotNull(resource);
        Assert.isLegal(resource.getType() == IResource.ROOT || resource.getType() == IResource.PROJECT);
        return locationFor(resource).append(F_MARKERS);
    }

    /**
     * Returns the path of the file in which to snapshot markers for the given
     * resource. Should only be called for the workspace root and projects.
     */
    public IPath getMarkersSnapshotLocationFor(IResource resource) {
        return getMarkersLocationFor(resource).addFileExtension(F_SNAP_EXTENSION);
    }

    /**
     * Returns the path of the file in which to save the refresh snapshot for
     * the given project.
     */
    public IPath getRefreshLocationFor(IProject project) {
        Assert.isNotNull(project);
        return locationFor(project).append(F_REFRESH);
    }

    /**
     * Returns the path of the file in which to save the sync information for
     * the given resource. Should only be called for the workspace root and
     * projects.
     */
    public IPath getSyncInfoLocationFor(IResource resource) {
        Assert.isNotNull(resource);
        Assert.isLegal(resource.getType() == IResource.ROOT || resource.getType() == IResource.PROJECT);
        return locationFor(resource).append(F_SYNCINFO);
    }

    /**
     * Returns the path of the file in which to snapshot the sync information
     * for the given resource. Should only be called for the workspace root and
     * projects.
     */
    public IPath getSyncInfoSnapshotLocationFor(IResource resource) {
        return getSyncInfoLocationFor(resource).addFileExtension(F_SNAP_EXTENSION);
    }

    public IPath getWorkingLocation(IResource resource, String id) {
        return locationFor(resource).append(id);
    }

    protected Workspace getWorkspace() {
        return workspace;
    }

    /**
     * Returns the local file system location in which the meta data for the
     * resource with the given path is stored.
     */
    public IPath locationFor(IPath resourcePath) {
        if (IPath.ROOT.equals(resourcePath)) {
            return metaAreaLocation.append(F_ROOT);
        }
        return projectMetaLocation.append(resourcePath.segment(0));
    }

    /**
     * Returns the local file system location in which the meta data for the
     * given resource is stored.
     */
    public IPath locationFor(IResource resource) {
        if (resource.getType() == IResource.ROOT) {
            return metaAreaLocation.append(F_ROOT);
        }
        return projectMetaLocation.append(resource.getProject().getName());
    }

}
