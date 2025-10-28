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

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources.ICoreConstants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.watson.ElementTree;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IBuildConfiguration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.ICommand;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResourceDelta;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IncrementalProjectBuilder;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Assert;
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
    private IProject[] interestingProjects = ICoreConstants.EMPTY_PROJECT_ARRAY;
    private String natureId;
    private ElementTree oldState;
    /**
     * The build configuration that this builder is to build.
     */
    private IBuildConfiguration buildConfiguration;

    /*
     * @see IncrementalProjectBuilder#forgetLastBuiltState
     */
    protected void forgetLastBuiltState() {
        oldState = null;
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
     * @see IncrementalProjectBuilder#forgetLastBuiltState()
     * @see IncrementalProjectBuilder#rememberLastBuiltState()
     */
    protected IResourceDelta getDelta(IProject aProject) {
        return buildManager.getDelta(aProject);
    }

    final IProject[] getInterestingProjects() {
        return interestingProjects;
    }

    final ElementTree getLastBuiltTree() {
        return oldState;
    }

    /**
     * Returns the ID of the nature that owns this builder. Returns null if the
     * builder does not belong to a nature.
     */
    final String getNatureId() {
        return natureId;
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

    final void setCallOnEmptyDelta(boolean value) {
    }

    final void setCommand(ICommand value) {
        this.command = value;
    }

    final void setInterestingProjects(IProject[] value) {
        interestingProjects = value;
    }

    final void setLabel(String value) {
    }

    final void setLastBuiltTree(ElementTree value) {
        oldState = value;
    }

    final void setNatureId(String id) {
        this.natureId = id;
    }

    final void setPluginId(String value) {
    }

    /**
     * Sets the build configuration for which this builder operates.
     * 
     * @see #getBuildConfig()
     */
    final void setBuildConfig(IBuildConfiguration value) {
        Assert.isNotNull(value);
        buildConfiguration = value;
    }

    /*
     * @see IncrementalProjectBuilder#startupOnInitialize
     */
    protected abstract void startupOnInitialize();

}
