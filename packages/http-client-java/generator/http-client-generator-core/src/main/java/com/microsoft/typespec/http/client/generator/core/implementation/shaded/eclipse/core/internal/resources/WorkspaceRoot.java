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
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.utils.FileUtil;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.utils.Policy;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IContainer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IWorkspaceRoot;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.ResourcesPlugin;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Assert;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Path;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Platform;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.util.NLS;
import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorkspaceRoot extends Container implements IWorkspaceRoot {
    /**
     * As an optimization, we store a table of project handles
     * that have been requested from this root. This maps project
     * name strings to project handles.
     */
    private final Map<String, Project> projectTable = new ConcurrentHashMap<>(16);

    /**
     * Cache of the canonicalized platform location.
     */
    private final IPath workspaceLocation;

    protected WorkspaceRoot(IPath path, Workspace container) {
        super(path, container);
        Assert.isTrue(path.equals(IPath.ROOT));
        workspaceLocation = FileUtil.canonicalPath(Platform.getLocation());
        Assert.isNotNull(workspaceLocation);
    }

    @Override
    public boolean exists(int flags, boolean checkType) {
        return true;
    }

    @Override
    public IPath getLocation() {
        return workspaceLocation;
    }

    @Override
    public String getName() {
        return ""; //$NON-NLS-1$
    }

    @Override
    public IContainer getParent() {
        return null;
    }

    @Override
    public IProject getProject() {
        return null;
    }

    @Override
    public IProject getProject(String name) {
        // first check our project cache
        Project result = projectTable.get(name);
        if (result == null) {
            IPath projectPath = new Path(null, name).makeAbsolute();
            int segmentCount = projectPath.segmentCount();
            String message = MessageFormat.format("Path for project must have only one segment but has {0}: {1}", //$NON-NLS-1$
                segmentCount, projectPath);
            Assert.isLegal(segmentCount == ICoreConstants.PROJECT_SEGMENT_LENGTH, message);
            // try to get the project using a canonical name
            String canonicalName = projectPath.lastSegment();
            result = projectTable.computeIfAbsent(canonicalName, n -> new Project(projectPath, workspace));
        }
        return result;
    }

    @Override
    public IPath getProjectRelativePath() {
        return IPath.EMPTY;
    }

    @Override
    public IProject[] getProjects() {
        return getProjects(IResource.NONE);
    }

    @Override
    public IProject[] getProjects(int memberFlags) {
        IResource[] roots = getChildren(memberFlags);
        IProject[] result = new IProject[roots.length];
        try {
            System.arraycopy(roots, 0, result, 0, roots.length);
        } catch (ArrayStoreException ex) {
            // Shouldn't happen since only projects should be children of the workspace root
            for (IResource root2 : roots) {
                if (root2.getType() != IResource.PROJECT) {
                    Policy.log(IStatus.ERROR, NLS.bind("{0} is an invalid child of the workspace root.", //$NON-NLS-1$
                        root2), null);
                }

            }
            throw ex;
        }
        return result;
    }

    @Override
    public int getType() {
        return IResource.ROOT;
    }

    @Override
    public void internalSetLocal(boolean flag, int depth) throws CoreException {
        // do nothing for the root, but call for its children
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
    public boolean isDerived(int options) {
        return false;// the root is never derived
    }

    @Override
    public boolean isHidden() {
        return false;// the root is never hidden
    }

    @Override
    public boolean isHidden(int options) {
        return false;// the root is never hidden
    }

    @Override
    public boolean isLinked(int options) {
        return false;// the root is never linked
    }

    @Deprecated
    @Override
    public boolean isLocal(int depth) {
        // the flags parameter is ignored for the workspace root so pass anything
        return isLocal(-1, depth);
    }

    @Deprecated
    @Override
    public boolean isLocal(int flags, int depth) {
        // don't check the flags....workspace root is always local
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
    public boolean isPhantom() {
        return false;
    }

    @Override
    public void setHidden(boolean isHidden) {
        // workspace root cannot be set hidden
    }

    @Override
    public long setLocalTimeStamp(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Illegal time stamp: " + value); //$NON-NLS-1$
        }
        // can't set local time for root
        return value;
    }
}
