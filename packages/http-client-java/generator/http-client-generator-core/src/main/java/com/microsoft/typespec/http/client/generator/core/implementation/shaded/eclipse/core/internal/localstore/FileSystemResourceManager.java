/*******************************************************************************
 * Copyright (c) 2000, 2025 IBM Corporation and others.
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
 *     Martin Oberhuber (Wind River) - [210664] descriptionChanged(): ignore LF style
 *     Martin Oberhuber (Wind River) - [233939] findFilesForLocation() with symlinks
 *     James Blackburn (Broadcom Corp.) - ongoing development
 *     Sergey Prigogin (Google) - [338010] Resource.createLink() does not preserve symbolic links
 *                              - [462440] IFile#getContents methods should specify the status codes for its exceptions
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 473427
 *     Karsten Thoms <karsten.thoms@itemis.de> - Bug 521500
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.localstore;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.filesystem.EFS;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.filesystem.IFileInfo;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.filesystem.IFileStore;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.filesystem.URIUtil;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources.ICoreConstants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources.Project;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources.ProjectDescription;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources.Resource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources.ResourceException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources.ResourceInfo;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources.Workspace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.utils.FileUtil;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.utils.Messages;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IContainer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IFile;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResourceStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IWorkspaceRoot;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.ResourceAttributes;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.ResourcesPlugin;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.util.NLS;

import java.io.InputStream;
import java.net.URI;

/**
 * Manages the synchronization between the workspace's view and the file system.
 */
public class FileSystemResourceManager implements ICoreConstants {

    protected Workspace workspace;

    public FileSystemResourceManager(Workspace workspace) {
        this.workspace = workspace;
    }

    /**
     * Asynchronously auto-refresh the requested resource if {@link ResourcesPlugin#PREF_LIGHTWEIGHT_AUTO_REFRESH} is
     * enabled.
     */
    private void asyncRefresh() {
    }

    /*
     * (non-javadoc)
     * 
     * @see IResource.getResourceAttributes
     */
    public ResourceAttributes attributes(IResource resource) {
        IFileStore store = getStore(resource);
        IFileInfo fileInfo = store.fetchInfo();
        if (!fileInfo.exists()) {
            return null;
        }
        return FileUtil.fileInfoToAttributes(fileInfo);
    }

    /**
     * Never returns null.
     *
     * @param target the resource to get a store for
     * @return The file store for this resource
     */
    public IFileStore getStore(IResource target) {
        try {
            return getStoreRoot(target).createStore(target.getFullPath(), target);
        } catch (CoreException e) {
            // callers aren't expecting failure here, so return null file system
            return EFS.getNullFileSystem().getStore(target.getFullPath());
        }
    }

    /**
     * Returns the file store root for the provided resource. Never returns null.
     */
    private FileStoreRoot getStoreRoot(IResource target) {
        ResourceInfo info = workspace.getResourceInfo(target.getFullPath(), true, false);
        FileStoreRoot root;
        if (info != null) {
            root = info.getFileStoreRoot();
            if (root != null && root.isValid()) {
                return root;
            }
            if (info.isSet(ICoreConstants.M_VIRTUAL)) {
                ProjectDescription description = ((Project) target.getProject()).internalGetDescription();
                if (description != null) {
                    setLocation(target, info, description.getGroupLocationURI(target.getProjectRelativePath()));
                    return info.getFileStoreRoot();
                }
                return info.getFileStoreRoot();
            }
            if (info.isSet(ICoreConstants.M_LINK)) {
                ProjectDescription description = ((Project) target.getProject()).internalGetDescription();
                if (description != null) {
                    final URI linkLocation = description.getLinkLocationURI(target.getProjectRelativePath());
                    // if we can't determine the link location, fall through to parent resource
                    if (linkLocation != null) {
                        setLocation(target, info, linkLocation);
                        return info.getFileStoreRoot();
                    }
                }
            }
        }
        final IContainer parent = target.getParent();
        if (parent == null) {
            // this is the root, so we know where this must be located
            // initialize root location
            info = workspace.getResourceInfo(IPath.ROOT, false, true);
            final IWorkspaceRoot rootResource = workspace.getRoot();
            setLocation(rootResource, info, URIUtil.toURI(rootResource.getLocation()));
            return info.getFileStoreRoot();
        }
        root = getStoreRoot(parent);
        if (info != null) {
            info.setFileStoreRoot(root);
        }
        return root;
    }

    protected Workspace getWorkspace() {
        return workspace;
    }

    /**
     * Initializes the file store for a resource.
     *
     * @param target The resource to initialize the file store for.
     * @param location the File system location of this resource on disk
     * @return The file store for the provided resource
     */
    private IFileStore initializeStore(IResource target, URI location, boolean locationAlreadyNormalized)
        throws CoreException {
        ResourceInfo info = ((Resource) target).getResourceInfo(false, true);
        if (locationAlreadyNormalized) {
            setNormalizedLocation(target, info, location);
        } else {
            setLocation(target, info, location);
        }
        FileStoreRoot root = getStoreRoot(target);
        return root.createStore(target.getFullPath(), target);
    }

    public void link(Resource target, URI location, IFileInfo fileInfo) throws CoreException {
        initializeStore(target, location, false);
        ResourceInfo info = target.getResourceInfo(false, true);
        long lastModified = fileInfo == null ? 0 : fileInfo.getLastModified();
        if (lastModified == 0) {
            info.clearModificationStamp();
        }
        updateLocalSync(info, lastModified);
    }

    /**
     * Returns the resolved, absolute file system location of the given resource.
     * Returns null if the location could not be resolved.
     *
     * @param target the resource to get the location for
     */
    public IPath locationFor(IResource target) {
        return getStoreRoot(target).localLocation(target.getFullPath(), target, false);
    }

    /**
     * Returns the resolved, absolute file system location of the given resource.
     * Returns null if the location could not be resolved.
     *
     * @param target the resource to get the location URI for
     * @param canonical if {@code true}, the prefix of the path of the returned URI
     * corresponding to resource's file store root will be canonicalized
     */
    public URI locationURIFor(IResource target, boolean canonical) {
        return getStoreRoot(target).computeURI(target.getFullPath(), canonical);
    }

    public InputStream read(IFile target, boolean force, IProgressMonitor monitor) throws CoreException {
        IFileStore store = getFileStore(target, force);
        try {
            return store.openInputStream(EFS.NONE, monitor);
        } catch (CoreException e) {
            asyncRefresh();
            if (e.getStatus().getCode() == EFS.ERROR_NOT_EXISTS) {
                String message = NLS.bind(Messages.localstore_fileNotFound, store.toString());
                throw new ResourceException(IResourceStatus.RESOURCE_NOT_FOUND, target.getFullPath(), message, e);
            }
            throw e;
        }
    }

    private IFileStore getFileStore(IFile target, boolean force) throws ResourceException, CoreException {
        IFileStore store = getStore(target);
        if (!force) {
            final IFileInfo fileInfo = store.fetchInfo();
            Resource resource = (Resource) target;
            ResourceInfo info = resource.getResourceInfo(true, false);
            if (fileInfo.getLastModified() != info.getLocalSyncInfo()) {
                asyncRefresh();
                String message = NLS.bind(Messages.localstore_resourceIsOutOfSync, target.getFullPath());
                throw new ResourceException(IResourceStatus.OUT_OF_SYNC_LOCAL, target.getFullPath(), message, null);
            }
        }
        return store;
    }

    /** @see #read(IFile, boolean, IProgressMonitor) **/
    public byte[] readAllBytes(IFile target, boolean force, IProgressMonitor monitor) throws CoreException {
        IFileStore store = getFileStore(target, force);
        try {
            return store.readAllBytes(EFS.NONE, monitor);
        } catch (CoreException e) {
            asyncRefresh();
            if (e.getStatus().getCode() == EFS.ERROR_NOT_EXISTS) {
                String message = NLS.bind(Messages.localstore_fileNotFound, store.toString());
                throw new ResourceException(IResourceStatus.RESOURCE_NOT_FOUND, target.getFullPath(), message, e);
            }
            throw e;
        }
    }

    /*
     * (non-javadoc)
     * 
     * @see IResouce.setLocalTimeStamp
     */
    public long setLocalTimeStamp(IResource target, ResourceInfo info, long value) throws CoreException {
        IFileStore store = getStore(target);
        IFileInfo fileInfo = store.fetchInfo();
        fileInfo.setLastModified(value);
        store.putInfo(fileInfo, EFS.SET_LAST_MODIFIED, null);
        // actual value may be different depending on file system granularity
        fileInfo = store.fetchInfo();
        long actualValue = fileInfo.getLastModified();
        updateLocalSync(info, actualValue);
        return actualValue;
    }

    /**
     * The storage location for a resource has changed; update the location.
     *
     * @param target the changed resource
     * @param info the resource info to update
     * @param location the new storage location
     */
    public void setLocation(IResource target, ResourceInfo info, URI location) {
        // Normalize case as it exists on the file system.
        setNormalizedLocation(target, info, FileUtil.realURI(location));
    }

    public void setNormalizedLocation(IResource target, ResourceInfo info, URI location) {
        FileStoreRoot oldRoot = info.getFileStoreRoot();
        if (location != null) {
            info.setFileStoreRoot(new FileStoreRoot(location, target.getFullPath()));
        } else {
            // project is in default location so clear the store root
            info.setFileStoreRoot(null);
        }
        if (oldRoot != null) {
            oldRoot.setValid(false);
        }
    }

    /*
     * (non-javadoc)
     * 
     * @see IResource.setResourceAttributes
     */
    public void setResourceAttributes(IResource resource, ResourceAttributes attributes) throws CoreException {
        IFileStore store = getStore(resource);
        // when the executable bit is changed on a folder a refresh is required
        store.putInfo(FileUtil.attributesToFileInfo(attributes), EFS.SET_ATTRIBUTES, null);
    }

    /**
     * The ResourceInfo must be mutable.
     */
    public void updateLocalSync(ResourceInfo info, long localSyncInfo) {
        info.setLocalSyncInfo(localSyncInfo);
        if (localSyncInfo == I_NULL_SYNC_INFO) {
            info.clear(M_LOCAL_EXISTS);
        } else {
            info.set(M_LOCAL_EXISTS);
        }
    }

}
