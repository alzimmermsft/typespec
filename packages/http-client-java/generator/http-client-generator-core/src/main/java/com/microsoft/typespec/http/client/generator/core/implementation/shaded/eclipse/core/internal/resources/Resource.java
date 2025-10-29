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
 *     Dan Rubel <dan_rubel@instantiations.com> - Implementation of getLocalTimeStamp
 *     Red Hat Incorporated - get/setResourceAttribute code
 *     Oakland Software Incorporated - added getSessionProperties and getPersistentProperties
 *     Holger Oehm <holger.oehm@sap.com> - [226264] race condition in Workspace.isTreeLocked()/setTreeLocked()
 *     Martin Oberhuber (Wind River) -  [245937] ProjectDescription#setLinkLocation() detects non-change
 *     Serge Beauchamp (Freescale Semiconductor) - [252996] add resource filtering
 *     Serge Beauchamp (Freescale Semiconductor) - [229633] Project Path Variable Support
 *     James Blackburn (Broadcom Corp.) - ongoing development
 *     Sergey Prigogin (Google) - [338010] Resource.createLink() does not preserve symbolic links
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 473427
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.filesystem.IFileStore;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.localstore.FileSystemResourceManager;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.properties.IPropertyManager;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.utils.Messages;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.utils.WrappedRuntimeException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.watson.ElementTreeIterator;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.watson.IElementContentVisitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.watson.IPathRequestor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IContainer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IFolder;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IMarker;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IPathVariableManager;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResourceProxyVisitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResourceStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResourceVisitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IWorkspace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.ResourceAttributes;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Assert;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.PlatformObject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.QualifiedName;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.jobs.ISchedulingRule;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.jobs.MultiRule;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.util.NLS;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class Resource extends PlatformObject implements IResource, ICoreConstants, Cloneable, IPathRequestor {
    final IPath path;
    final Workspace workspace;

    protected Resource(IPath path, Workspace workspace) {
        this.path = path.removeTrailingSeparator();
        this.workspace = workspace;
    }

    @Override
    public void accept(IResourceProxyVisitor visitor, int memberFlags) throws CoreException {
        accept(visitor, IResource.DEPTH_INFINITE, memberFlags);
    }

    @Override
    public void accept(final IResourceProxyVisitor visitor, final int depth, final int memberFlags)
        throws CoreException {
        // It is invalid to call accept on a phantom when INCLUDE_PHANTOMS is not specified.
        final boolean includePhantoms = (memberFlags & IContainer.INCLUDE_PHANTOMS) != 0;
        if ((memberFlags & IContainer.DO_NOT_CHECK_EXISTENCE) == 0) {
            checkAccessible(getFlags(getResourceInfo(includePhantoms, false)));
        }

        final ResourceProxy proxy = new ResourceProxy();
        IElementContentVisitor elementVisitor = (tree, requestor, contents) -> {
            ResourceInfo info = (ResourceInfo) contents;
            if (!isMember(getFlags(info), memberFlags)) {
                return false;
            }
            proxy.requestor = requestor;
            proxy.info = info;
            try {
                boolean shouldContinue = true;
                switch (depth) {
                    case DEPTH_ZERO:
                        shouldContinue = false;
                        break;

                    case DEPTH_ONE:
                        shouldContinue = !path.equals(requestor.requestPath().removeLastSegments(1));
                        break;

                    case DEPTH_INFINITE:
                        shouldContinue = true;
                        break;
                }
                return visitor.visit(proxy) && shouldContinue;
            } catch (CoreException e) {
                // Throw an exception to bail out of the traversal.
                throw new WrappedRuntimeException(e);
            } finally {
                proxy.reset();
            }
        };
        try {
            new ElementTreeIterator(workspace.getElementTree(), getFullPath()).iterate(elementVisitor);
        } catch (WrappedRuntimeException e) {
            throw (CoreException) e.getTargetException();
        } finally {
            proxy.requestor = null;
            proxy.info = null;
        }
    }

    @Override
    public void accept(IResourceVisitor visitor) throws CoreException {
        accept(visitor, IResource.DEPTH_INFINITE, 0);
    }

    @Override
    public void accept(IResourceVisitor visitor, int depth, boolean includePhantoms) throws CoreException {
        accept(visitor, depth, includePhantoms ? IContainer.INCLUDE_PHANTOMS : 0);
    }

    @Override
    public void accept(final IResourceVisitor visitor, int depth, int memberFlags) throws CoreException {
        // Use the fast visitor if visiting to infinite depth.
        if (depth == IResource.DEPTH_INFINITE) {
            accept(proxy -> visitor.visit(proxy.requestResource()), memberFlags);
            return;
        }
        // It is invalid to call accept on a phantom when INCLUDE_PHANTOMS is not specified.
        final boolean includePhantoms = (memberFlags & IContainer.INCLUDE_PHANTOMS) != 0;
        ResourceInfo info = getResourceInfo(includePhantoms, false);
        int flags = getFlags(info);
        if ((memberFlags & IContainer.DO_NOT_CHECK_EXISTENCE) == 0) {
            checkAccessible(flags);
        }

        // Check that this resource matches the member flags
        // Visit this resource.
        if (!isMember(flags, memberFlags) || !visitor.visit(this) || depth == DEPTH_ZERO) {
            return;
        }
        // Get the info again because it might have been changed by the visitor.
        info = getResourceInfo(includePhantoms, false);
        if (info == null) {
            return;
        }
        // Thread safety: (cache the type to avoid changes -- we might not be inside an operation).
        int type = info.getType();
        if (type == FILE) {
            return;
        }
        // If we had a gender change we need to fix up the resource before asking for its members.
        IContainer resource
            = getType() != type ? (IContainer) workspace.newResource(getFullPath(), type) : (IContainer) this;
        IResource[] members = resource.members(memberFlags);
        for (IResource member : members) {
            member.accept(visitor, DEPTH_ZERO, memberFlags | IContainer.DO_NOT_CHECK_EXISTENCE);
        }
    }

    public void checkAccessible(int flags) throws CoreException {
        checkExists(flags, true);
    }

    private ResourceInfo checkAccessibleAndLocal(int depth) throws CoreException {
        ResourceInfo info = getResourceInfo(false, false);
        int flags = getFlags(info);
        checkAccessible(flags);
        checkLocal(flags, depth);
        return info;
    }

    /**
     * Checks that this resource exists.
     * If checkType is true, the type of this resource and the one in the tree must match.
     *
     * @exception CoreException if this resource does not exist
     */
    public void checkExists(int flags, boolean checkType) throws CoreException {
        if (!exists(flags, checkType)) {
            String message = NLS.bind(Messages.resources_mustExist, getFullPath());
            throw new ResourceException(IResourceStatus.RESOURCE_NOT_FOUND, getFullPath(), message, null);
        }
    }

    /**
     * Checks that this resource is local to the given depth.
     *
     * @exception CoreException if this resource is not local
     */
    public void checkLocal(int flags, int depth) throws CoreException {
        if (!isLocal(flags, depth)) {
            String message = NLS.bind(Messages.resources_mustBeLocal, getFullPath());
            throw new ResourceException(IResourceStatus.RESOURCE_NOT_LOCAL, getFullPath(), message, null);
        }
    }

    @Override
    public boolean contains(ISchedulingRule rule) {
        // Must allow notifications to nest in all resource rules.
        if ((this == rule) || rule.getClass().equals(WorkManager.NotifyRule.class)) {
            return true;
        }
        if (rule instanceof MultiRule multi) {
            ISchedulingRule[] children = multi.getChildren();
            for (ISchedulingRule c : children) {
                if (!contains(c)) {
                    return false;
                }
            }
            return true;
        }
        if (!(rule instanceof IResource resource)) {
            return false;
        }
        if (!workspace.equals(resource.getWorkspace())) {
            return false;
        }
        return path.isPrefixOf(resource.getFullPath());
    }

    /**
     * @throws CoreException in overloads
     */
    public void convertToPhantom() throws CoreException {
        ResourceInfo info = getResourceInfo(false, true);
        if (info == null || isPhantom(getFlags(info))) {
            return;
        }
        info.clearSessionProperties();
        info.set(M_PHANTOM);
        getLocalManager().updateLocalSync(info, I_NULL_SYNC_INFO);
        info.clearModificationStamp();
        // Should already be done by the #deleteResource call but left in
        // just to be safe and for code clarity.
        info.setMarkers(null);
    }

    @Override
    public IMarker createMarker(String type) throws CoreException {
        return createMarker(type, Collections.emptyMap());
    }

    @Override
    public IMarker createMarker(String type, Map<String, ? extends Object> attributes) throws CoreException {
        Assert.isNotNull(type);
        final ISchedulingRule rule = workspace.getRuleFactory().markerRule(this);
        try {
            workspace.prepareOperation(rule, null);
            checkAccessible(getFlags(getResourceInfo(false, false)));
            workspace.beginOperation(true);
            long id = workspace.nextMarkerId();
            MarkerManager manager = workspace.getMarkerManager();
            boolean validate = manager.isPersistentType(type);
            MarkerInfo markerInfo = new MarkerInfo(attributes, validate, type, id);
            manager.add(this, markerInfo);
            if (attributes != null && !attributes.isEmpty()) {
                if (manager.isPersistent(markerInfo)) {
                    this.getResourceInfo(false, true).set(ICoreConstants.M_MARKERS_SNAP_DIRTY);
                }
            }
            return new Marker(this, markerInfo.getId());
        } finally {
            workspace.endOperation(rule, false);
        }
    }

    @Override
    public void deleteMarkers(String type, boolean includeSubtypes, int depth) throws CoreException {
        final ISchedulingRule rule = workspace.getRuleFactory().markerRule(this);
        try {
            workspace.prepareOperation(rule, null);
            ResourceInfo info = getResourceInfo(false, false);
            checkAccessible(getFlags(info));

            workspace.beginOperation(true);
            workspace.getMarkerManager().removeMarkers(this, type, includeSubtypes, depth);
        } finally {
            workspace.endOperation(rule, false);
        }
    }

    @Override
    public boolean equals(Object target) {
        if (this == target) {
            return true;
        }
        if (!(target instanceof Resource resource)) {
            return false;
        }
        return getType() == resource.getType() && path.equals(resource.path) && workspace.equals(resource.workspace);
    }

    @Override
    public boolean exists() {
        ResourceInfo info = getResourceInfo(false, false);
        return exists(getFlags(info), true);
    }

    public boolean exists(int flags, boolean checkType) {
        return flags != NULL_FLAG && !(checkType && ResourceInfo.getType(flags) != getType());
    }

    @Override
    public IMarker[] findMarkers(String type, boolean includeSubtypes, int depth) throws CoreException {
        ResourceInfo info = getResourceInfo(false, false);
        checkAccessible(getFlags(info));
        // It might happen that from this point the resource is not accessible anymore.
        // But markers have the #exists method that callers can use to check if it is still valid.
        return workspace.getMarkerManager().findMarkers(this, type, includeSubtypes, depth);
    }

    @Override
    public String getFileExtension() {
        String name = getName();
        int index = name.lastIndexOf('.');
        if (index == -1) {
            return null;
        }
        if (index == (name.length() - 1)) {
            return ""; //$NON-NLS-1$
        }
        return name.substring(index + 1);
    }

    public int getFlags(ResourceInfo info) {
        return (info == null) ? NULL_FLAG : info.getFlags();
    }

    @Override
    public IPath getFullPath() {
        return path;
    }

    public FileSystemResourceManager getLocalManager() {
        return workspace.getFileSystemManager();
    }

    @Override
    public IPath getLocation() {
        IProject project = getProject();
        if (project != null && !project.exists()) {
            return null;
        }
        return getLocalManager().locationFor(this);
    }

    @Override
    public URI getLocationURI() {
        IProject project = getProject();
        if (project != null && !project.exists()) {
            return null;
        }
        return getLocalManager().locationURIFor(this, false);
    }

    @Override
    public long getModificationStamp() {
        ResourceInfo info = getResourceInfo(false, false);
        return info == null ? IResource.NULL_STAMP : info.getModificationStamp();
    }

    @Override
    public String getName() {
        return path.lastSegment();
    }

    @Override
    public IContainer getParent() {
        int segments = path.segmentCount();
        // Zero and one segments handled by subclasses.
        if (segments < 2) {
            Assert.isLegal(false, path.toString());
        }
        if (segments == 2) {
            return workspace.getRoot().getProject(path.segment(0));
        }
        return (IFolder) workspace.newResource(path.removeLastSegments(1), IResource.FOLDER);
    }

    @Override
    public String getPersistentProperty(QualifiedName key) throws CoreException {
        checkAccessibleAndLocal(DEPTH_ZERO);
        return getPropertyManager().getProperty(this, key);
    }

    @Override
    public IProject getProject() {
        return workspace.getRoot().getProject(path.segment(0));
    }

    @Override
    public IPath getProjectRelativePath() {
        return getFullPath().removeFirstSegments(ICoreConstants.PROJECT_SEGMENT_LENGTH);
    }

    public IPropertyManager getPropertyManager() {
        return workspace.getPropertyManager();
    }

    @Override
    public ResourceAttributes getResourceAttributes() {
        if (!isAccessible() || isVirtual()) {
            return null;
        }
        FileSystemResourceManager manager = getLocalManager();
        if (manager == null) {
            return null;
        }
        return manager.attributes(this);
    }

    /**
     * Returns the resource info. Returns null if the resource doesn't exist.
     * If the phantom flag is true, phantom resources are considered.
     * If the mutable flag is true, a mutable info is returned.
     */
    public ResourceInfo getResourceInfo(boolean phantom, boolean mutable) {
        return workspace.getResourceInfo(getFullPath(), phantom, mutable);
    }

    public IFileStore getStore() {
        return getLocalManager().getStore(this);
    }

    @Override
    public abstract int getType();

    public String getTypeString() {
        switch (getType()) {
            case FILE:
                return "L"; //$NON-NLS-1$

            case FOLDER:
                return "F"; //$NON-NLS-1$

            case PROJECT:
                return "P"; //$NON-NLS-1$

            case ROOT:
                return "R"; //$NON-NLS-1$
        }
        return ""; //$NON-NLS-1$
    }

    @Override
    public IWorkspace getWorkspace() {
        return workspace;
    }

    @Override
    public int hashCode() {
        // The container may be null if the identified resource
        // does not exist so don't bother with it in the hash
        return getFullPath().hashCode();
    }

    /**
     * Sets the M_LOCAL_EXISTS flag. Is internal so we don't have
     * to begin an operation.
     */
    protected void internalSetLocal(boolean flag, int depth) throws CoreException {
        ResourceInfo info = getResourceInfo(true, true);
        // Only make the change if it's not already in desired state.
        if (info.isSet(M_LOCAL_EXISTS) != flag) {
            if (flag && !isPhantom(getFlags(info))) {
                info.set(M_LOCAL_EXISTS);
                workspace.updateModificationStamp(info);
            } else {
                info.clear(M_LOCAL_EXISTS);
                info.clearModificationStamp();
            }
        }
        if (getType() == IResource.FILE || depth == IResource.DEPTH_ZERO) {
            return;
        }
        if (depth == IResource.DEPTH_ONE) {
            depth = IResource.DEPTH_ZERO;
        }
        IResource[] children = ((IContainer) this).members();
        for (IResource element : children) {
            ((Resource) element).internalSetLocal(flag, depth);
        }
    }

    @Override
    public boolean isAccessible() {
        return exists();
    }

    @Override
    public boolean isConflicting(ISchedulingRule rule) {
        // Must not schedule at same time as notification.
        if ((this == rule) || rule.getClass().equals(WorkManager.NotifyRule.class)) {
            return true;
        }
        if (rule instanceof MultiRule multi) {
            ISchedulingRule[] children = multi.getChildren();
            for (ISchedulingRule element : children) {
                if (isConflicting(element)) {
                    return true;
                }
            }
            return false;
        }
        if (!(rule instanceof IResource resource)) {
            return false;
        }
        if (!workspace.equals(resource.getWorkspace())) {
            return false;
        }
        IPath otherPath = resource.getFullPath();
        return path.isPrefixOf(otherPath) || otherPath.isPrefixOf(path);
    }

    @Override
    public boolean isDerived() {
        return isDerived(IResource.NONE);
    }

    @Override
    public boolean isDerived(int options) {
        ResourceInfo info = getResourceInfo(false, false);
        int flags = getFlags(info);
        if (flags != NULL_FLAG && ResourceInfo.isSet(flags, ICoreConstants.M_DERIVED)) {
            return true;
        }
        // Check ancestors if the appropriate option is set.
        if ((options & CHECK_ANCESTORS) != 0) {
            return getParent().isDerived(options);
        }
        return false;
    }

    @Override
    public boolean isHidden() {
        ResourceInfo info = getResourceInfo(false, false);
        int flags = getFlags(info);
        return flags != NULL_FLAG && ResourceInfo.isSet(flags, ICoreConstants.M_HIDDEN);
    }

    @Override
    public boolean isHidden(int options) {
        ResourceInfo info = getResourceInfo(false, false);
        int flags = getFlags(info);
        if (flags != NULL_FLAG && ResourceInfo.isSet(flags, ICoreConstants.M_HIDDEN)) {
            return true;
        }
        // Check ancestors if the appropriate option is set.
        if ((options & CHECK_ANCESTORS) != 0) {
            return getParent().isHidden(options);
        }
        return false;
    }

    @Override
    public boolean isLinked() {
        return isLinked(NONE);
    }

    @Override
    public boolean isLinked(int options) {
        if ((options & CHECK_ANCESTORS) != 0) {
            IProject project = getProject();
            if (project == null) {
                return false;
            }
            ProjectDescription desc = ((Project) project).internalGetDescription();
            if (desc == null) {
                return false;
            }
            HashMap<IPath, LinkDescription> links = desc.getLinks();
            if (links == null) {
                return false;
            }
            IPath myPath = getProjectRelativePath();
            for (LinkDescription linkDescription : links.values()) {
                if (linkDescription.getProjectRelativePath().isPrefixOf(myPath)) {
                    return true;
                }
            }
            return false;
        }
        // The no ancestor checking case.
        ResourceInfo info = getResourceInfo(false, false);
        return info != null && info.isSet(M_LINK);
    }

    @Override
    public boolean isVirtual() {
        ResourceInfo info = getResourceInfo(false, false);
        return info != null && info.isSet(M_VIRTUAL);
    }

    @Override
    @Deprecated
    public boolean isLocal(int depth) {
        ResourceInfo info = getResourceInfo(false, false);
        return isLocal(getFlags(info), depth);
    }

    /**
     * Note the depth parameter is intentionally ignored because
     * this method is over-ridden by {@link Container#isLocal(int)}.
     * 
     * @deprecated
     */
    @Deprecated
    public boolean isLocal(int flags, int depth) {
        return flags != NULL_FLAG && ResourceInfo.isSet(flags, M_LOCAL_EXISTS);
    }

    /**
     * Returns whether a resource should be included in a traversal
     * based on the provided member flags.
     *
     * @param flags The resource info flags
     * @param memberFlags The member flag mask
     * @return Whether the resource is included
     */
    protected boolean isMember(int flags, int memberFlags) {
        int excludeMask = 0;
        if ((memberFlags & IContainer.INCLUDE_PHANTOMS) == 0) {
            excludeMask |= M_PHANTOM;
        }
        if ((memberFlags & IContainer.INCLUDE_HIDDEN) == 0) {
            excludeMask |= M_HIDDEN;
        }
        if ((memberFlags & IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS) == 0) {
            excludeMask |= M_TEAM_PRIVATE_MEMBER;
        }
        if ((memberFlags & IContainer.EXCLUDE_DERIVED) != 0) {
            excludeMask |= M_DERIVED;
        }
        // The resource is a matching member if it matches none of the exclude flags.
        return flags != NULL_FLAG && (flags & excludeMask) == 0;
    }

    @Override
    public boolean isPhantom() {
        ResourceInfo info = getResourceInfo(true, false);
        return isPhantom(getFlags(info));
    }

    public boolean isPhantom(int flags) {
        return flags != NULL_FLAG && ResourceInfo.isSet(flags, M_PHANTOM);
    }

    @Override
    public String requestName() {
        return getName();
    }

    @Override
    public IPath requestPath() {
        return getFullPath();
    }

    @Override
    public void setHidden(boolean isHidden) throws CoreException {
        // Fetch the info but don't bother making it mutable even though we are going
        // to modify it. We don't know whether or not the tree is open and it really doesn't
        // matter as the change we are making does not show up in deltas.
        ResourceInfo info = getResourceInfo(false, false);
        int flags = getFlags(info);
        checkAccessible(flags);
        if (isHidden) {
            info.set(ICoreConstants.M_HIDDEN);
        } else {
            info.clear(ICoreConstants.M_HIDDEN);
        }
    }

    @Override
    public long setLocalTimeStamp(long value) throws CoreException {
        if (value < 0) {
            throw new IllegalArgumentException("Illegal value: " + value); //$NON-NLS-1$
        }
        // Fetch the info but don't bother making it mutable even though we are going to modify it.
        // It really doesn't matter as the change we are making does not show up in deltas.
        ResourceInfo info = checkAccessibleAndLocal(DEPTH_ZERO);
        return getLocalManager().setLocalTimeStamp(this, info, value);
    }

    @Override
    public void setResourceAttributes(ResourceAttributes attributes) throws CoreException {
        checkAccessibleAndLocal(DEPTH_ZERO);
        getLocalManager().setResourceAttributes(this, attributes);
    }

    @Override
    public String toString() {
        return getTypeString() + getFullPath().toString();
    }

    @Override
    public IPathVariableManager getPathVariableManager() {
        if (getProject() == null) {
            return workspace.getPathVariableManager();
        }
        return new ProjectPathVariableManager(this);
    }

}
