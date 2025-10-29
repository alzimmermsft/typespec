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
 *     James Blackburn (Broadcom Corp.) - Custom trigger builder #equals
 *     Broadcom Corporation - ongoing development
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 473427
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.events;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources.ModelObject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.*;

import java.util.*;

/**
 * The concrete implementation of {@link ICommand}. This object
 * stores information about a particular type of builder.
 *
 * If the builder has been instantiated, a reference to the builder is held.
 * If the builder supports multiple build configurations, a reference to the
 * builder for each configuration is held.
 */
public class BuildCommand extends ModelObject implements ICommand {
    /**
     * Internal flag masks for different build triggers.
     */
    private static final int MASK_AUTO = 0x01;
    private static final int MASK_INCREMENTAL = 0x02;
    private static final int MASK_FULL = 0x04;
    private static final int MASK_CLEAN = 0x08;

    private static final int ALL_TRIGGERS = MASK_AUTO | MASK_CLEAN | MASK_FULL | MASK_INCREMENTAL;

    protected HashMap<String, String> arguments = new HashMap<>(0);

    /**
     * The builder instance for this command. Null if the builder has
     * not yet been instantiated.
     */
    private IncrementalProjectBuilder builder;
    /**
     * The builders for this command if the builder supports multiple configurations
     */
    private HashMap<IBuildConfiguration, IncrementalProjectBuilder> builders;

    /**
     * The triggers that this builder will respond to. Since build triggers are not
     * bit-maskable, we use internal bit masks to represent each
     * trigger (MASK_* constants). By default, a command responds to all
     * build triggers.
     */
    private int triggers = ALL_TRIGGERS;

    /**
     * Lock used to synchronize access to {@link #builder} and {@link #builders}.
     */
    private final Object builderLock = new Object();

    public BuildCommand() {
        super(""); //$NON-NLS-1$
    }

    @Override
    public Object clone() {
        BuildCommand result = (BuildCommand) super.clone();
        if (result == null) {
            return null;
        }
        result.setArguments(getArguments());
        // don't let references to builder instances leak out because they reference trees
        result.setBuilders(null);
        return result;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof BuildCommand command)) {
            return false;
        }
        // equal if same builder name, arguments, and triggers
        return getBuilderName().equals(command.getBuilderName())
            && getArguments(false).equals(command.getArguments(false))
            && (triggers & ALL_TRIGGERS) == (command.triggers & ALL_TRIGGERS);
    }

    @Override
    public Map<String, String> getArguments() {
        return getArguments(true);
    }

    @SuppressWarnings({ "unchecked" })
    public Map<String, String> getArguments(boolean makeCopy) {
        return arguments == null ? null : (makeCopy ? (Map<String, String>) arguments.clone() : arguments);
    }

    @Override
    public String getBuilderName() {
        return getName();
    }

    @Override
    public int hashCode() {
        // hash on name and trigger
        return 37 * getName().hashCode() + (ALL_TRIGGERS & triggers);
    }

    @Override
    public void setArguments(Map<String, String> value) {
        // copy parameter for safety's sake
        arguments = value == null ? null : new HashMap<>(value);
    }

    /**
     * Set the IncrementalProjectBuilder(s) for this command
     *
     * @param value a single {@link IncrementalProjectBuilder} or a {@link Map} of
     * {@link IncrementalProjectBuilder} indexed by
     * {@link IBuildConfiguration}
     */
    @SuppressWarnings("unchecked")
    public void setBuilders(Object value) {
        synchronized (builderLock) {
            if (value == null) {
                builder = null;
                builders = null;
            } else {
                if (value instanceof IncrementalProjectBuilder) {
                    builder = (IncrementalProjectBuilder) value;
                } else {
                    builders = new HashMap<>((Map<IBuildConfiguration, IncrementalProjectBuilder>) value);
                }
            }
        }
    }

    /**
     * For debugging purposes only
     */
    @Override
    public String toString() {
        return "BuildCommand(" + getName() + ")";//$NON-NLS-1$ //$NON-NLS-2$
    }
}
