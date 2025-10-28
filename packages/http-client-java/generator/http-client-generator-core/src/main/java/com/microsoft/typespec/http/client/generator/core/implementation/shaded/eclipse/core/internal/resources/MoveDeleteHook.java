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
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.*;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.team.IMoveDeleteHook;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.team.IResourceTree;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;

/**
 * @since 2.0
 */
public class MoveDeleteHook implements IMoveDeleteHook {

    @Override
    public boolean deleteFile(IResourceTree tree, IFile file, int updateFlags, IProgressMonitor monitor) {
        // Let someone else do the work.
        return false;
    }

    /**
     * @see IMoveDeleteHook#deleteFolder(IResourceTree, IFolder, int, IProgressMonitor)
     */
    @Override
    public boolean deleteFolder(IResourceTree tree, IFolder folder, int updateFlags, IProgressMonitor monitor) {
        // Let someone else do the work.
        return false;
    }

    /**
     * @see IMoveDeleteHook#deleteProject(IResourceTree, IProject, int, IProgressMonitor)
     */
    @Override
    public boolean deleteProject(IResourceTree tree, IProject project, int updateFlags, IProgressMonitor monitor) {
        // Let someone else do the work.
        return false;
    }

    /**
     * @see IMoveDeleteHook#moveFile(IResourceTree, IFile, IFile, int, IProgressMonitor)
     */
    @Override
    public boolean moveFile(IResourceTree tree, IFile source, IFile destination, int updateFlags,
        IProgressMonitor monitor) {
        // Let someone else do the work.
        return false;
    }

    /**
     * @see IMoveDeleteHook#moveFolder(IResourceTree, IFolder, IFolder, int, IProgressMonitor)
     */
    @Override
    public boolean moveFolder(final IResourceTree tree, IFolder source, IFolder destination, int updateFlags,
        IProgressMonitor monitor) {
        // Let someone else do the work.
        return false;
    }

    /**
     * @see IMoveDeleteHook#moveProject(IResourceTree, IProject, IProjectDescription, int, IProgressMonitor)
     */
    @Override
    public boolean moveProject(IResourceTree tree, IProject source, IProjectDescription description, int updateFlags,
        IProgressMonitor monitor) {
        // Let someone else do the work.
        return false;
    }

}
