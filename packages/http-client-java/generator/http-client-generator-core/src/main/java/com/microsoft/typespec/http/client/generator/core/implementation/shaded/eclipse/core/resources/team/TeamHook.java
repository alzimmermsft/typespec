/*******************************************************************************
 *  Copyright (c) 2000, 2014 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.team;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResourceRuleFactory;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IWorkspace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.ResourcesPlugin;

/**
 * A general hook class for operations that team providers may be
 * interested in participating in. Implementors of the hook should provide
 * a concrete subclass, and override any methods they are interested in.
 * <p>
 * This class is intended to be subclassed by the team component in
 * conjunction with the
 * <code>com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.teamHook</code>
 * standard extension point. Individual team providers may also subclass this
 * class. It is not intended to be subclassed by other clients. The methods
 * defined on this class are called from within the implementations of
 * workspace API methods and must not be invoked directly by clients.
 * </p>
 *
 * @since 2.1
 */
public abstract class TeamHook {
    /**
     * The default resource scheduling rule factory. This factory can be used for projects
     * that the team hook methods do not participate in.
     *
     * @see #getRuleFactory(IProject)
     * @since 3.0
     */
    protected final IResourceRuleFactory defaultFactory;

    /**
     * Creates a new team hook. Default constructor for use by subclasses and the
     * resources plug-in only.
     *
     * @deprecated this constructor relies on the workspace already initialized and
     * accessible via static access, instead the
     * {@link #TeamHook(IWorkspace)} constructor should be used.
     */
    @Deprecated
    protected TeamHook() {
        this(ResourcesPlugin.getWorkspace());
    }

    /**
     * Creates a new team hook for the given workspace.
     *
     * @since 3.17
     */
    protected TeamHook(IWorkspace workspace) {
        defaultFactory = new ResourceRuleFactory(workspace);
    }

    /**
     * Returns the resource scheduling rule factory that should be used when workspace
     * operations are invoked on resources in that project. The workspace will ask the
     * team hook this question only once per project, per session. The workspace will
     * assume the returned result is valid for the rest of that session, unless the rule
     * is changed by calling <code>setRuleFactory</code>.
     * <p>
     * This method must not return <code>null</code>. If no special rules are required
     * by the team hook for the given project, the value of the <code>defaultFactory</code>
     * field should be returned.
     * <p>
     * This default implementation always returns the value of the <code>defaultFactory</code>
     * field. Subclasses may override and provide a subclass of <code>ResourceRuleFactory</code>.
     *
     * @param project the project to return scheduling rules for
     * @return the resource scheduling rules for a project
     * @see #setRuleFactory(IProject, IResourceRuleFactory)
     * @see ResourceRuleFactory
     * @since 3.0
     */
    public IResourceRuleFactory getRuleFactory(IProject project) {
        return defaultFactory;
    }

}
