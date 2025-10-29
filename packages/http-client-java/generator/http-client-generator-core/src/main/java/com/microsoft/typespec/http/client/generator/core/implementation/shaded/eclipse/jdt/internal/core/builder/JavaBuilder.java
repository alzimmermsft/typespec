/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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
 *     Terry Parker <tparker@google.com> - [performance] Low hit rates in JavaModel caches - https://bugs.eclipse.org/421165
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.builder;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IncrementalProjectBuilder;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaModelMarker;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.JavaModelManager;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Set;

public class JavaBuilder extends IncrementalProjectBuilder {

    IProject currentProject;
    NameEnvironment nameEnvironment;
    NameEnvironment testNameEnvironment;
    // maps a project to its binary resources (output folders, class folders, zip/jar files)
    public State lastState;
    public static final String SOURCE_ID = "JDT"; //$NON-NLS-1$

    public static boolean DEBUG = false;

    public static void removeProblemsAndTasksFor(IResource resource) {
        try {
            if (resource != null && resource.exists()) {
                resource.deleteMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
                resource.deleteMarkers(IJavaModelMarker.TASK_MARKER, false, IResource.DEPTH_INFINITE);

                // delete managed markers
                Set<String> markerTypes
                    = JavaModelManager.getJavaModelManager().compilationParticipants.managedMarkerTypes();
                if (markerTypes.isEmpty())
                    return;
                for (String markerType : markerTypes) {
                    resource.deleteMarkers(markerType, false, IResource.DEPTH_INFINITE);
                }
            }
        } catch (CoreException e) {
            // assume there were no problems
        }
    }

    public static State readState(IProject project, DataInputStream in) throws IOException, CoreException {
        return State.read(project, in);
    }

    /**
     * String representation for debugging purposes
     */
    @Override
    public String toString() {
        return this.currentProject == null
            ? "JavaBuilder for unknown project" //$NON-NLS-1$
            : "JavaBuilder for " + this.currentProject.getName(); //$NON-NLS-1$
    }
}
