/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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
 *     Broadcom Corporation - build configurations and references
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.events;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IBuildConfiguration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.ICommand;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IncrementalProjectBuilder;

import java.util.Collection;

/**
 * This class is the internal basis for all builders. Plugin developers should not
 * subclass this class.
 *
 * @see IncrementalProjectBuilder
 */
public abstract class InternalBuilder {
    /**
     * Hold a direct reference to the build manager as an optimization.
     * This will be initialized by BuildManager when it is constructed.
     */
    static BuildManager buildManager;
    private ICommand command;
    /**
     * The build configuration that this builder is to build.
     */
    private IBuildConfiguration buildConfiguration;

    /*
     * @see IncrementalProjectBuilder#forgetLastBuiltState
     */
    protected void forgetLastBuiltState() {
    }

    /*
     * @see IncrementalProjectBuilder#rememberLastBuiltState
     */
    protected void rememberLastBuiltState() {
    }

    /*
     * @see IncrementalProjectBuilder#getCommand
     */
    protected ICommand getCommand() {
        return (ICommand) ((BuildCommand) command).clone();
    }

    /**
     * Returns the project for this builder
     */
    protected IProject getProject() {
        return buildConfiguration.getProject();
    }

    /**
     * @see IncrementalProjectBuilder#getBuildConfig()
     */
    protected IBuildConfiguration getBuildConfig() {
        return buildConfiguration;
    }

    /*
     * @see IncrementalProjectBuilder#hasBeenBuilt
     */
    protected boolean hasBeenBuilt(IProject aProject) {
        return buildManager.hasBeenBuilt(aProject);
    }

    /*
     * @see IncrementalProjectBuilder#needRebuild
     */
    protected void needRebuild() {
        buildManager.requestRebuild();
    }

    /*
     * @see IncrementalProjectBuilder#requestProjectRebuild
     */
    public void requestProjectRebuild(boolean processOtherBuilders) {
        buildManager.requestRebuild(getProject(), processOtherBuilders);
    }

    /*
     * @see IncrementalProjectBuilder#requestProjectsRebuild
     */
    public void requestProjectsRebuild(Collection<IProject> projects) {
        buildManager.requestRebuild(projects, getProject());
    }

}
