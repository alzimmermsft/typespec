/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
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
 * Anton Leherbauer (Wind River) - [198591] Allow Builder to specify scheduling rule
 * Anton Leherbauer (Wind River) - [305858] Allow Builder to return null rule
 * James Blackburn (Broadcom) - [306822] Provide Context for Builder getRule()
 * Broadcom Corporation - build configurations and references
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.events.InternalBuilder;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.*;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.jobs.ISchedulingRule;
import java.util.Collection;
import java.util.Map;

/**
 * The abstract base class for all incremental project builders. This class
 * provides the infrastructure for defining a builder and fulfills the contract
 * specified by the
 * <code>com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.builders</code>
 * standard
 * extension point.
 * <p>
 * All builders must subclass this class according to the following guidelines:
 * <ul>
 * <li>must re-implement at least <code>build</code></li>
 * <li>may implement other methods</li>
 * <li>must supply a public, no-argument constructor</li>
 * </ul>
 * On creation, the <code>setInitializationData</code> method is called with
 * any parameter data specified in the declaring plug-in's manifest.
 *
 */
public abstract class IncrementalProjectBuilder extends InternalBuilder implements IExecutableExtension {
    /**
     * Build kind constant (value 9) indicating an automatic build request. When
     * autobuild is turned on, these builds are triggered automatically whenever
     * resources change. Apart from the method by which autobuilds are triggered,
     * they otherwise operate like an incremental build.
     *
     */
    public static final int AUTO_BUILD = 9;

    /**
     * Requests that this builder forget any state it may be retaining regarding
     * previously built states. Typically this means that the next time the
     * builder runs, it will have to do a full build since it does not have any
     * state upon which to base an incremental build.
     * This supersedes a call to {@link #rememberLastBuiltState()}.
     */
    @Override
    public final void forgetLastBuiltState() {
        super.forgetLastBuiltState();
    }

    /**
     * Requests that this builder remember any build invocation specific state.
     * This means that the next time the builder runs, it will receive a delta
     * which includes changes reported in the current {@link #getDelta(IProject)}.
     * <p>
     * This can be used to indicate that a builder didn't run, even though there
     * are changes, and the builder wishes that the delta be preserved until its
     * next invocation.
     * </p>
     * This is superseded by a call to {@link #forgetLastBuiltState()}.
     * 
     * @since 3.7
     */
    @Override
    public final void rememberLastBuiltState() {
        super.rememberLastBuiltState();
    }

    /**
     * Returns the build command associated with this builder. The returned
     * command may or may not be in the build specification for the project
     * on which this builder operates.
     * <p>
     * Any changes made to the returned command will only take effect if
     * the modified command is installed on a project build spec.
     * </p>
     *
     * @since 3.1
     */
    @Override
    public final ICommand getCommand() {
        return super.getCommand();
    }

    /**
     * Returns the project for which this builder is defined.
     *
     * @return the project
     */
    @Override
    public final IProject getProject() {
        return super.getProject();
    }

    /**
     * Returns the build configuration for which this build was invoked.
     * 
     * @return the build configuration
     * @since 3.7
     */
    @Override
    public final IBuildConfiguration getBuildConfig() {
        return super.getBuildConfig();
    }

    /**
     * Returns whether the given project has already been built during this
     * build iteration.
     * <p>
     * When the entire workspace is being built, the projects are built in
     * linear sequence. This method can be used to determine if another project
     * precedes this builder's project in that build sequence. If only a single
     * project is being built, then there is no build order and this method will
     * always return <code>false</code>.
     * </p>
     *
     * @param project the project to check against in the current build order
     * @return <code>true</code> if the given project has been built in this
     * iteration, and <code>false</code> otherwise.
     * @see #needRebuild()
     * @since 2.1
     */
    @Override
    public final boolean hasBeenBuilt(IProject project) {
        return super.hasBeenBuilt(project);
    }

    /**
     * Indicates that this builder made changes that affect a build configuration
     * that precedes this build configuration in the currently executing build
     * order, and thus a rebuild will be necessary.
     * <p>
     * <b>Note:</b> this method will schedule rebuild for all projects involved in
     * the current build cycle!
     * </p>
     * <ul>
     * <li>If concrete projects that require rebuild are known, it is better to use
     * {@link #requestProjectsRebuild(Collection)} instead to avoid overhead.</li>
     * <li>If only one project should be rebuilt, it is better to use
     * {@link #requestProjectRebuild(boolean)} where additionally one could avoid
     * extra work by skipping not yet executed builders.</li>
     * </ul>
     * <p>
     * This is an advanced feature that builders should use with caution. This can
     * cause workspace builds to iterate until no more builders require rebuilds.
     * </p>
     *
     * @see #hasBeenBuilt(IProject)
     * @see #requestProjectsRebuild(Collection)
     * @see #requestProjectRebuild(boolean)
     * @since 2.1
     */
    @Override
    public final void needRebuild() {
        super.needRebuild();
    }

    /**
     * Indicates that this builder generated or detected new input for currently
     * running project build, and thus a project rebuild will be necessary in the
     * current build round.
     * <p>
     * The builders configured to run after the current one will be still processed
     * if {@code processOtherBuilders} is set to {@code true}. To force an immediate
     * rebuild of a project {@code processOtherBuilders} argument should be set to
     * {@code false}.
     * </p>
     * <p>
     * <b>Note</b> if {@code processOtherBuilders} is set to {@code false}, the
     * project that is built with current builder will be only rebuilt again, if
     * this builder is not the first one configured to run.
     * </p>
     * <p>
     * This is an advanced feature that builders should use with caution. This can
     * cause workspace builds to iterate until no more builders require rebuilds.
     * </p>
     *
     * @param processOtherBuilders to continue building project with other builders
     * and not start project build from beginning
     * immediately
     *
     * @see #needRebuild()
     * @see #requestProjectsRebuild(Collection)
     * @since 3.17
     */
    @Override
    public final void requestProjectRebuild(boolean processOtherBuilders) {
        super.requestProjectRebuild(processOtherBuilders);
    }

    /**
     * Indicates that this builder generated or detected new input for given
     * projects and thus a rebuild for given projects will be necessary in the next
     * build round.
     * <p>
     * <b>Note</b> if the given collection contains the current project (that is
     * currently built with current builder), the current project will be not
     * rebuilt immediately, but scheduled for rebuild on next round. To perform
     * immediate rebuild of the current project, use
     * {@link #requestProjectRebuild(boolean)}.
     * </p>
     * <p>
     * This is an advanced feature that builders should use with caution. This can
     * cause workspace builds to iterate until no more builders require rebuilds.
     * </p>
     *
     * @see #needRebuild()
     * @see #requestProjectRebuild(boolean)
     * @since 3.17
     */
    @Override
    public final void requestProjectsRebuild(Collection<IProject> projects) {
        super.requestProjectsRebuild(projects);
    }

    /**
     * Sets initialization data for this builder.
     * <p>
     * This method is part of the {@link IExecutableExtension} interface.
     * </p>
     * <p>
     * Subclasses are free to extend this method to pick up initialization
     * parameters from the plug-in plug-in manifest (<code>plugin.xml</code>)
     * file, but should be sure to invoke this method on their superclass.
     * </p><p>
     * For example, the following method looks for a boolean-valued parameter
     * named "trace":
     * </p>
     *
     * <pre>
     * public void setInitializationData(IConfigurationElement cfig, String propertyName, Object data)
     *     throws CoreException {
     *     super.setInitializationData(cfig, propertyName, data);
     *     if (data instanceof Hashtable) {
     *         Hashtable args = (Hashtable) data;
     *         String traceValue = (String) args.get(&quot;trace&quot;);
     *         TRACING = (traceValue != null &amp;&amp; traceValue.equals(&quot;true&quot;));
     *     }
     * }
     * </pre>
     * 
     * @throws CoreException if fails.
     */
    @Override
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
        throws CoreException {
        // default implementation does nothing
        // thwart compiler warning
    }

    /**
     * Returns the scheduling rule that is required for building
     * the project build configuration for which this builder is defined. The default
     * is the workspace root rule.
     * <p>
     * The scheduling rule determines which resources in the workspace are
     * protected from being modified by other threads while the builder is running. Up until
     * Eclipse 3.5, the entire workspace was always locked during a build;
     * since Eclipse 3.6, builders can allow resources outside their scheduling
     * rule to be modified.
     * <p>
     * <strong>Notes:</strong>
     * </p>
     * <ul>
     * <li>
     * The rule may be <i>relaxed</i> and in some cases let the builder be scheduled in
     * parallel of any other operation using a rule based on {@link IResource}). A relaxed
     * rule is a scheduling rule which does not contain the workspace root rule.
     * </li>
     * <li>
     * The rule returned here may have no effect if the build is invoked within the
     * scope of another operation that locks the entire workspace.
     * </li>
     * <li>
     * If this method returns any rule other than the workspace root,
     * resources outside of the rule scope can be modified concurrently with the build.
     * The delta returned by {@link #getDelta(IProject)} for any project
     * outside the scope of the builder's rule may not contain changes that occurred
     * concurrently with the build.
     * </li>
     * </ul>
     * <p>
     * Subclasses may override this method.
     * </p>
     * 
     * @noreference This method is not intended to be referenced by clients.
     *
     * @param kind the kind of build being requested. Valid values include:
     * <ul>
     * <li>{@link #AUTO_BUILD} - indicates an automatically triggered
     * incremental build (autobuilding on).</li>
     * <li>{@link #CLEAN_BUILD} - indicates a clean request.</li>
     * </ul>
     * @param args a table of builder-specific arguments keyed by argument name
     * (key type: <code>String</code>, value type: <code>String</code>);
     * <code>null</code> is equivalent to an empty map.
     * @return a scheduling rule which is contained in the workspace root rule
     * or <code>null</code> to indicate that no protection against resource
     * modification during the build is needed.
     *
     * @since 3.6
     */
    public ISchedulingRule getRule(int kind, Map<String, String> args) {
        return ResourcesPlugin.getWorkspace().getRoot();
    }
}
