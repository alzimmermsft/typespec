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
 *     Martin Oberhuber (Wind River) - [245937] setLinkLocation() detects non-change
 *     Serge Beauchamp (Freescale Semiconductor) - [229633] Project Path Variable Support
 *     Markus Schorn (Wind River) - [306575] Save snapshot location with project
 *     Broadcom Corporation - build configurations and references
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 473427
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.events.BuildCommand;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IBuildConfiguration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.ICommand;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IProjectDescription;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ProjectDescription extends ModelObject implements IProjectDescription {
    // constants
    private static final ICommand[] EMPTY_COMMAND_ARRAY = new ICommand[0];
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final String EMPTY_STR = ""; //$NON-NLS-1$

    protected ICommand[] buildSpec = EMPTY_COMMAND_ARRAY;
    protected String comment = EMPTY_STR;

    // Build configuration + References state
    /**
     * The 'real' build configuration names set on this project.
     * This doesn't contain the generated 'default' build configuration with name
     * {@link IBuildConfiguration#DEFAULT_CONFIG_NAME}
     * when no build configurations have been defined.
     */
    protected String[] configNames = EMPTY_STRING_ARRAY;
    /** Map from config name in this project -&gt; build configurations in other projects */
    protected HashMap<String, IBuildConfiguration[]> dynamicConfigRefs = new HashMap<>(1);

    // Cache of the build configurations
    protected volatile IBuildConfiguration[] cachedBuildConfigs;
    // Cached build configuration references. Not persisted.
    protected Map<String, IBuildConfiguration[]> cachedConfigRefs = Collections.synchronizedMap(new HashMap<>(1));
    /**
     * Cached project level references. Synchronize on {@link #cachedRefsMutex} before reading or writing. Increment
     * {@link #cachedRefsDirtyCount} whenever this is dirtied.
     */
    protected IProject[] cachedRefs;
    /**
     * Counts the number of times {@link #cachedRefs} has been dirtied. Can be used to determine if dynamic dependencies
     * have
     * changed during an operation that is intended to be atomic with respect to dynamic dependencies. Synchronize on
     * {@link #cachedRefsMutex} before accessing.
     */
    protected int cachedRefsDirtyCount;
    /**
     * Mutex used to protect {@link #cachedRefs} and {@link #cachedRefsDirtyCount}.
     */
    protected final Object cachedRefsMutex = new Object();

    /**
     * Map of (IPath -&gt; LinkDescription) pairs for each linked resource
     * in this project, where IPath is the project relative path of the resource.
     */
    protected HashMap<IPath, LinkDescription> linkDescriptions = null;

    /**
     * Map of {@literal (IPath -> LinkedList<FilterDescription>)} pairs for each filtered resource
     * in this project, where IPath is the project relative path of the resource.
     */
    protected HashMap<IPath, LinkedList<FilterDescription>> filterDescriptions = null;

    /**
     * Map of (String -&gt; VariableDescription) pairs for each variable in this
     * project, where String is the name of the variable.
     */
    protected HashMap<String, VariableDescription> variableDescriptions = null;

    // fields
    protected URI location = null;
    protected volatile String[] natures = EMPTY_STRING_ARRAY;

    public ProjectDescription() {
        super();
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public Object clone() {
        ProjectDescription clone = (ProjectDescription) super.clone();
        // don't want the clone to have access to our internal link locations table or builders
        clone.linkDescriptions = null;
        clone.filterDescriptions = null;
        if (variableDescriptions != null) {
            clone.variableDescriptions = (HashMap<String, VariableDescription>) variableDescriptions.clone();
        }
        clone.buildSpec = getBuildSpec(true);
        clone.dynamicConfigRefs = (HashMap<String, IBuildConfiguration[]>) dynamicConfigRefs.clone();
        clone.cachedConfigRefs = Collections.synchronizedMap(new HashMap<>(1));
        clone.clearCachedDynamicReferences(null);
        return clone;
    }

    /**
     * Clear cached references for the specified build config name
     * or all if configName is null.
     */
    public void clearCachedDynamicReferences(String configName) {
        synchronized (cachedRefsMutex) {
            if (configName == null) {
                cachedConfigRefs.clear();
            } else {
                cachedConfigRefs.remove(configName);
            }
            cachedRefs = null;
            cachedRefsDirtyCount++;
        }
    }

    public ICommand[] getBuildSpec(boolean makeCopy) {
        // thread safety: copy reference in case of concurrent write
        ICommand[] oldCommands = this.buildSpec;
        if (oldCommands == null) {
            return EMPTY_COMMAND_ARRAY;
        }
        if (!makeCopy) {
            return oldCommands;
        }
        ICommand[] result = new ICommand[oldCommands.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (ICommand) ((BuildCommand) oldCommands[i]).clone();
        }
        return result;
    }

    /**
     * Returns the link location for the given resource name. Returns null if
     * no such link exists.
     */
    public URI getLinkLocationURI(IPath aPath) {
        if (linkDescriptions == null) {
            return null;
        }
        LinkDescription desc = linkDescriptions.get(aPath);
        return desc == null ? null : desc.getLocationURI();
    }

    /**
     * Returns the filter for the given resource name. Returns null if
     * no such filter exists.
     */
    synchronized public LinkedList<FilterDescription> getFilter(IPath aPath) {
        if (filterDescriptions == null) {
            return null;
        }
        return filterDescriptions.get(aPath);
    }

    /**
     * Returns the map of link descriptions (IPath (project relative path) -&gt; LinkDescription).
     * Since this method is only used internally, it never creates a copy.
     * Returns null if the project does not have any linked resources.
     */
    public HashMap<IPath, LinkDescription> getLinks() {
        return linkDescriptions;
    }

    /**
     * Returns the map of filter descriptions (IPath (project relative path) -&gt;
     * {@literal LinkedList<FilterDescription>}). Since this method is only used
     * internally, it never creates a copy. Returns null if the project does not
     * have any filtered resources.
     */
    public HashMap<IPath, LinkedList<FilterDescription>> getFilters() {
        return filterDescriptions;
    }

    /**
     * Returns the map of variable descriptions (String (variable name) -&gt;
     * VariableDescription). Since this method is only used internally, it never
     * creates a copy. Returns null if the project does not have any variables.
     */
    public HashMap<String, VariableDescription> getVariables() {
        return variableDescriptions;
    }

    public String[] getNatureIds(boolean makeCopy) {
        if (natures == null) {
            return EMPTY_STRING_ARRAY;
        }
        return makeCopy ? natures.clone() : natures;
    }

    @Override
    public boolean hasNature(String natureID) {
        String[] natureIDs = getNatureIds(false);
        for (String natureID2 : natureIDs) {
            if (natureID2.equals(natureID)) {
                return true;
            }
        }
        return false;
    }

    public URI getGroupLocationURI(IPath projectRelativePath) {
        return LinkDescription.VIRTUAL_LOCATION;
    }

}
