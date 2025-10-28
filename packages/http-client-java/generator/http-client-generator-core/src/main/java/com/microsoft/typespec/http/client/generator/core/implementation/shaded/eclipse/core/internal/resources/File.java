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
 *     Serge Beauchamp (Freescale Semiconductor) - [229633] Group and Project Path Variable Support
 *     James Blackburn (Broadcom Corp.) - ongoing development
 *     Sergey Prigogin (Google) - [464072] Refresh on Access ignored during text search
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.preferences.EclipsePreferences;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IFile;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IFolder;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IProjectDescription;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResourceStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Status;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.content.IContentDescription;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * The standard implementation of {@link IFile}.
 */
public class File extends Resource implements IFile {

    protected File(IPath path, Workspace container) {
        super(path, container);
    }

    /**
     * Changes this file to be a folder in the resource tree and returns
     * the newly created folder. All related
     * properties are deleted. It is assumed that on disk the resource is
     * already a folder/directory so no action is taken to delete the disk
     * contents.
     * <p>
     * <b>This method is for the exclusive use of the local resource manager</b>
     */
    public IFolder changeToFolder() throws CoreException {
        getPropertyManager().deleteProperties(this, IResource.DEPTH_ZERO);
        IFolder result = workspace.getRoot().getFolder(path);
        if (isLinked()) {
            IPath location = getRawLocation();
            delete(IResource.NONE, null);
            result.createLink(location, IResource.ALLOW_MISSING_LOCAL, null);
        } else {
            workspace.deleteResource(this);
            workspace.createResource(result, false);
        }
        return result;
    }

    @Override
    public String getCharset() throws CoreException {
        return getCharset(true);
    }

    @Override
    public String getCharset(boolean checkImplicit) throws CoreException {
        // non-existing resources default to parent's charset
        ResourceInfo info = getResourceInfo(false, false);
        int flags = getFlags(info);
        if (!exists(flags, false)) {
            return checkImplicit
                ? workspace.getCharsetManager().getCharsetFor(getFullPath().removeLastSegments(1), true)
                : null;
        }
        checkLocal(flags, DEPTH_ZERO);
        try {
            return internalGetCharset(checkImplicit, info);
        } catch (CoreException e) {
            if (e.getStatus().getCode() == IResourceStatus.RESOURCE_NOT_FOUND) {
                return checkImplicit
                    ? workspace.getCharsetManager().getCharsetFor(getFullPath().removeLastSegments(1), true)
                    : null;
            }
            throw e;
        }
    }

    private String internalGetCharset(boolean checkImplicit, ResourceInfo info) throws CoreException {
        // if there is a file-specific user setting, use it
        String charset = workspace.getCharsetManager().getCharsetFor(getFullPath(), false);
        if (charset != null || !checkImplicit) {
            return charset;
        }
        // tries to obtain a description for the file contents
        IContentDescription description = workspace.getContentDescriptionManager().getDescriptionFor(this, info, true);
        if (description != null) {
            String contentCharset = description.getCharset();
            if (contentCharset != null) {
                return contentCharset;
            }
        }
        // could not find out the encoding based on the contents... default to parent's
        return workspace.getCharsetManager().getCharsetFor(getFullPath().removeLastSegments(1), true);
    }

    @Override
    public InputStream getContents() throws CoreException {
        return getContents(false);
    }

    /** like {@link #readAllBytes()} **/
    @Override
    public InputStream getContents(boolean force) throws CoreException {
        ResourceInfo info = getResourceInfo(false, false);
        int flags = getFlags(info);
        checkAccessible(flags);
        checkLocal(flags, DEPTH_ZERO);
        return getLocalManager().read(this, force, null);
    }

    /** like {@link #getContents(boolean)} with parameter force=true **/
    @Override
    public byte[] readAllBytes() throws CoreException {
        boolean force = true;
        ResourceInfo info = getResourceInfo(false, false);
        int flags = getFlags(info);
        checkAccessible(flags);
        checkLocal(flags, DEPTH_ZERO);
        return getLocalManager().readAllBytes(this, force, null);
    }

    @Override
    public int getType() {
        return FILE;
    }

    /**
     * Optimized refreshLocal for files. This implementation does not block the workspace
     * for the common case where the file exists both locally and on the file system, and
     * is in sync. For all other cases, it defers to the super implementation.
     */
    @Override
    public void refreshLocal(int depth, IProgressMonitor monitor) throws CoreException {
        if (!getLocalManager().fastIsSynchronized(this)) {
            super.refreshLocal(IResource.DEPTH_ZERO, monitor);
        }
    }

    @Override
    public long setLocalTimeStamp(long value) throws CoreException {
        // override to handle changing timestamp on project description file
        long result = super.setLocalTimeStamp(value);
        if (path.segmentCount() == 2 && path.segment(1).equals(IProjectDescription.DESCRIPTION_FILE_NAME)) {
            // handle concurrent project deletion
            ResourceInfo projectInfo = ((Project) getProject()).getResourceInfo(false, false);
            if (projectInfo != null) {
                getLocalManager().updateLocalSync(projectInfo, result);
            }
        }
        return result;
    }

    /**
     * Treat the file specially if it represents a metadata file, which includes:
     * - project description file (.project)
     * - project preferences files (*.prefs)
     *
     * This method is called whenever it is discovered that a file has
     * been modified (added, removed, or changed).
     */
    public void updateMetadataFiles() throws CoreException {
        int count = path.segmentCount();
        String name = path.segment(1);
        // is this a project description file?
        if (count == 2 && name.equals(IProjectDescription.DESCRIPTION_FILE_NAME)) {
            Project project = (Project) getProject();
            project.updateDescription();
            // Discard stale project natures on ProjectInfo
            ProjectInfo projectInfo = (ProjectInfo) project.getResourceInfo(false, true);
            projectInfo.discardNatures();
            return;
        }
        // check to see if we are in the .settings directory
        if (count == 3 && EclipsePreferences.DEFAULT_PREFERENCES_DIRNAME.equals(name)) {
            ProjectPreferences.updatePreferences(this);
        }
    }

    @Override
    public String getLineSeparator(boolean checkParent) throws CoreException {
        if (exists()) {
            try (
                // for performance reasons the buffer size should
                // reflect the average length of the first Line:
                InputStream input = new BufferedInputStream(getContents(), 128);) {
                int c = input.read();
                while (c != -1 && c != '\r' && c != '\n') {
                    c = input.read();
                }
                if (c == '\n') {
                    return "\n"; //$NON-NLS-1$
                }
                if (c == '\r') {
                    if (input.read() == '\n') {
                        return "\r\n"; //$NON-NLS-1$
                    }
                    return "\r"; //$NON-NLS-1$
                }
            } catch (CoreException core) {
                if (!checkParent) {
                    throw core;
                }
            } catch (IOException io) {
                if (!checkParent) {
                    throw new CoreException(Status.error(io.getMessage(), io));
                }
            }
        }
        return checkParent ? getProject().getDefaultLineSeparator() : null;
    }

}
