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

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IFile;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IProjectDescription;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Status;

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

}
