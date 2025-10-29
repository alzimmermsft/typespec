/*******************************************************************************
 * Copyright (c) 2003, 2022 IBM Corporation and others.
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

package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.serviceregistry;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.framework.BundleContextImpl;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.messages.Msg;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.util.NLS;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Bundle;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.FrameworkEvent;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.ServiceException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.ServiceFactory;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.ServiceRegistration;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * This class represents the use of a service by a bundle. One is created for
 * each service acquired by a bundle.
 *
 * <p>
 * This class manages a service factory.
 *
 * @ThreadSafe
 */
public class ServiceFactoryUse<S> extends ServiceUse<S> {
    /** ServiceFactory object */
    final ServiceFactory<S> factory;

    /** Service object returned by ServiceFactory.getService() */
    /* @GuardedBy("getLock()") */
    private S cachedService;
    /**
     * true if we are calling the factory getService method. Used to detect
     * recursion.
     */
    /* @GuardedBy("getLock()") */
    private boolean factoryInUse;

    /**
     * Constructs a service use encapsulating the service factory.
     *
     * @param context bundle getting the service
     * @param registration ServiceRegistration of the service
     */
    ServiceFactoryUse(BundleContextImpl context, ServiceRegistrationImpl<S> registration) {
        super(context, registration);
        this.factoryInUse = false;
        this.cachedService = null;
        @SuppressWarnings("unchecked")
        ServiceFactory<S> f = (ServiceFactory<S>) registration.getServiceObject();
        this.factory = f;
    }

    /**
     * Get a service's service object and increment the use count.
     *
     * <p>
     * The following steps are followed to get the service object:
     * <ol>
     * <li>The use count is incremented by one.
     * <li>If the use count is now one, the
     * {@link ServiceFactory#getService(Bundle, ServiceRegistration)} method is
     * called to create a service object for the context bundle. This service object
     * is cached. While the use count is greater than zero, subsequent calls to get
     * the service object will return the cached service object. <br>
     * If the service object returned by the {@link ServiceFactory} is not an
     * <code>instanceof</code> all the classes named when the service was registered
     * or the {@link ServiceFactory} throws an exception, <code>null</code> is
     * returned and a {@link FrameworkEvent} of type {@link FrameworkEvent#ERROR} is
     * broadcast.
     * <li>The service object is returned.
     * </ol>
     *
     * @return The service object.
     */
    /* @GuardedBy("getLock()") */
    @Override
    S getService() {
        assert isLocked();
        if (inUse()) {
            incrementUse();
            return cachedService;
        }
        // check for recursive call on this thread
        if (factoryInUse) {
            ServiceException se = new ServiceException(
                NLS.bind(Msg.SERVICE_FACTORY_RECURSION, factory.getClass().getName(), "getService"), //$NON-NLS-1$
                ServiceException.FACTORY_RECURSION);
            context.getContainer()
                .getEventPublisher()
                .publishFrameworkEvent(FrameworkEvent.WARNING, registration.getBundle(), se);
            return null;
        }
        factoryInUse = true;
        final S service;
        try {
            service = factoryGetService();
            if (service == null) {
                return null;
            }
        } finally {
            factoryInUse = false;
        }

        this.cachedService = service;
        incrementUse();

        return service;
    }

    /**
     * Unget a service's service object.
     *
     * <p>
     * Decrements the use count if the service was being used.
     *
     * <p>
     * The following steps are followed to unget the service object:
     * <ol>
     * <li>If the use count is zero, return.
     * <li>The use count is decremented by one.
     * <li>If the use count is non zero, return.
     * <li>The
     * {@link ServiceFactory#ungetService(Bundle, ServiceRegistration, Object)}
     * method is called to release the service object for the context bundle.
     * </ol>
     * 
     * @return true if the service was ungotten; otherwise false.
     */
    /* @GuardedBy("getLock()") */
    @Override
    boolean ungetService() {
        assert isLocked();
        if (!inUse()) {
            return false;
        }

        decrementUse();
        if (inUse()) {
            return true;
        }

        final S service = cachedService;
        cachedService = null;
        factoryUngetService(service);
        return true;
    }

    /**
     * Release all uses of the service and reset the use count to zero.
     *
     * <ol>
     * <li>The bundle's use count for this service is set to zero.
     * <li>The
     * {@link ServiceFactory#ungetService(Bundle, ServiceRegistration, Object)}
     * method is called to release the service object for the bundle.
     * </ol>
     */
    /* @GuardedBy("getLock()") */
    @Override
    void release() {
        super.release();

        final S service = cachedService;
        if (service == null) {
            return;
        }
        cachedService = null;
        factoryUngetService(service);
    }

    /**
     * Return the service object for this service use.
     *
     * @return The service object.
     */
    /* @GuardedBy("getLock()") */
    @Override
    S getCachedService() {
        return cachedService;
    }

    /**
     * Call the service factory to get the service.
     *
     * @return The service returned by the factory or null if there was an error.
     */
    /* @GuardedBy("getLock()") */
    S factoryGetService() {
        final S service;
        try {
            service = AccessController.doPrivileged(new PrivilegedAction<S>() {
                @Override
                public S run() {
                    return factory.getService(context.getBundleImpl(), registration);
                }
            });
        } catch (Throwable t) {
            ServiceException se = new ServiceException(
                NLS.bind(Msg.SERVICE_FACTORY_EXCEPTION, factory.getClass().getName(), "getService"), //$NON-NLS-1$
                ServiceException.FACTORY_EXCEPTION, t);
            context.getContainer()
                .getEventPublisher()
                .publishFrameworkEvent(FrameworkEvent.ERROR, registration.getBundle(), se);
            return null;
        }

        if (service == null) {
            ServiceException se
                = new ServiceException(NLS.bind(Msg.SERVICE_OBJECT_NULL_EXCEPTION, factory.getClass().getName()),
                    ServiceException.FACTORY_ERROR);
            context.getContainer()
                .getEventPublisher()
                .publishFrameworkEvent(FrameworkEvent.WARNING, registration.getBundle(), se);
            return null;
        }

        String[] clazzes = registration.getClasses();
        String invalidService = ServiceRegistry.checkServiceClass(clazzes, service);
        if (invalidService != null) {
            ServiceException se = new ServiceException(NLS.bind(Msg.SERVICE_FACTORY_NOT_INSTANCEOF_CLASS_EXCEPTION,
                factory.getClass().getName(), service.getClass().getName(), invalidService),
                ServiceException.FACTORY_ERROR);
            context.getContainer()
                .getEventPublisher()
                .publishFrameworkEvent(FrameworkEvent.ERROR, registration.getBundle(), se);
            return null;
        }
        return service;
    }

    /**
     * Call the service factory to unget the service.
     *
     * @param service The service object to pass to the factory.
     */
    /* @GuardedBy("getLock()") */
    void factoryUngetService(final S service) {
        try {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    factory.ungetService(context.getBundleImpl(), registration, service);
                    return null;
                }
            });
        } catch (Throwable t) {
            ServiceException se = new ServiceException(
                NLS.bind(Msg.SERVICE_FACTORY_EXCEPTION, factory.getClass().getName(), "ungetService"), //$NON-NLS-1$
                ServiceException.FACTORY_EXCEPTION, t);
            context.getContainer()
                .getEventPublisher()
                .publishFrameworkEvent(FrameworkEvent.ERROR, registration.getBundle(), se);
        }
    }
}
