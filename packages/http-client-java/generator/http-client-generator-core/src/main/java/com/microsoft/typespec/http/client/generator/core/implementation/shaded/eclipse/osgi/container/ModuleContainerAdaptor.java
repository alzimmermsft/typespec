/*******************************************************************************
 * Copyright (c) 2012, 2021 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.FrameworkListener;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.hooks.resolver.ResolverHookFactory;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Adapts the behavior of a container.
 * 
 * @since 3.10
 */
public abstract class ModuleContainerAdaptor {
    private static final Executor defaultExecutor = Runnable::run;

    /**
     * Event types that may be
     * {@link #publishContainerEvent(ContainerEvent, Module, Throwable, FrameworkListener...)
     * published} for a container.
     */
    public enum ContainerEvent {
        REFRESH,

        /**
         * A container {@link ModuleContainer#getFrameworkStartLevel() start level}
         * change has completed.
         */
        START_LEVEL,

        /**
         * The container has been started.
         */
        STARTED,

        /**
         * This event is returned by {@link SystemModule#waitForStop(long)} to indicate
         * that the container has stopped.
         */
        STOPPED,

        /**
         * This event is returned by {@link SystemModule#waitForStop(long)} to indicate
         * that the container has stopped because of an update operation.
         */
        STOPPED_UPDATE,

        /**
         * This event is returned by {@link SystemModule#waitForStop(long)} to indicate
         * that the container has stopped because of an refresh operation.
         */
        STOPPED_REFRESH,

        /**
         * This event is returned by {@link SystemModule#waitForStop(long)} to indicate
         * that the wait operation has timed out..
         */
        STOPPED_TIMEOUT,

        /**
         * An event fired for an error condition.
         */
        ERROR,

        /**
         * An event fired for a warning condition.
         */
        WARNING,

        /**
         * An event fired for informational purposes only.
         */
        INFO
    }

    /**
     * Event types that may be
     * {@link #publishModuleEvent(ModuleEvent, Module, Module) published} for a
     * module indicating a {@link Module#getState() state} change has occurred for a
     * module.
     */
    public enum ModuleEvent {
        /**
         * The module has been installed
         */
        INSTALLED,
        /**
         * The module has been activated with the lazy activation policy and is waiting
         * a {@link Module.StartOptions#LAZY_TRIGGER trigger} class load.
         */
        LAZY_ACTIVATION,
        /**
         * The module has been resolved.
         */
        RESOLVED,
        /**
         * The module has beens started.
         */
        STARTED,
        /**
         * The module is about to be activated.
         */
        STARTING,
        /**
         * The module has been stopped.
         */
        STOPPED,
        /**
         * The module is about to be deactivated.
         */
        STOPPING,
        /**
         * The module has been uninstalled.
         */
        UNINSTALLED,
        /**
         * The module has been unresolved.
         */
        UNRESOLVED,
        /**
         * The module has been updated.
         */
        UPDATED
    }

    /**
     * Returns the resolver hook factory the container will use.
     * 
     * @return the resolver hook factory the container will use.
     */
    public abstract ResolverHookFactory getResolverHookFactory();

    /**
     * Publishes the specified container event. No locks are held by the container
     * when this method is called
     * 
     * @param type the type of event
     * @param module the module associated with the event
     * @param error the error associated with the event, may be {@code null}
     * @param listeners additional listeners to publish the event to synchronously
     */
    public abstract void publishContainerEvent(ContainerEvent type, Module module, Throwable error,
        FrameworkListener... listeners);

    /**
     * Publishes the specified module event type for the specified module. No locks
     * are held by the container when this method is called
     * 
     * @param type the event type to publish
     * @param module the module the event is associated with
     * @param origin the module which is the origin of the event. For the event type
     * {@link ModuleEvent#INSTALLED}, this is the module whose context
     * was used to install the module. Otherwise it is the module
     * itself. May be null only when the event is not of type
     * {@link ModuleEvent#INSTALLED}.
     */
    public abstract void publishModuleEvent(ModuleEvent type, Module module, Module origin);

    /**
     * Returns the specified configuration property value
     * 
     * @param key the key of the configuration property
     * @return the configuration property value
     */
    public String getProperty(String key) {
        return null;
    }

    /**
     * Creates a new {@link ModuleLoader} for the specified wiring.
     * 
     * @param wiring the module wiring to create a module loader for
     * @return a new {@link ModuleLoader} for the specified wiring.
     */
    public ModuleLoader createModuleLoader(ModuleWiring wiring) {
        throw new UnsupportedOperationException("Container adaptor does not support module class loaders."); //$NON-NLS-1$
    }

    /**
     * This is called whenever the module database has been updated.
     */
    public void updatedDatabase() {
        // do nothing by default
    }

    /**
     * Returns the executor used to perform resolve operations
     * 
     * @return the executor used to perform resolve operations
     * @since 3.11
     */
    public Executor getResolverExecutor() {
        return defaultExecutor;
    }

    /**
     * Returns the scheduled executor that may be used by the container to schedule
     * background tasks.
     * 
     * @return the scheduled executor, or null if background tasks are not supported
     * @since 3.13
     */
    public ScheduledExecutorService getScheduledExecutor() {
        return null;
    }
}
