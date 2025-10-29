/*******************************************************************************
 * Copyright (c) 2003, 2021 IBM Corporation and others.
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

package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.framework;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.Module;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.framework.eventmgr.EventDispatcher;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.messages.Msg;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.serviceregistry.ServiceReferenceImpl;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.serviceregistry.ServiceRegistrationImpl;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.serviceregistry.ServiceRegistry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.serviceregistry.ServiceUse;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.serviceregistry.ShrinkableCollection;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Bundle;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.BundleContext;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.BundleEvent;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.BundleListener;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Constants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Filter;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.FrameworkEvent;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.FrameworkListener;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.InvalidSyntaxException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.ServiceEvent;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.ServiceFactory;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.ServiceListener;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.ServiceObjects;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.ServicePermission;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.ServiceReference;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.ServiceRegistration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.hooks.bundle.FindHook;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bundle's execution context.
 *
 * This object is given out to bundles and provides the implementation to the
 * BundleContext for a host bundle. It is destroyed when a bundle is stopped.
 */

public class BundleContextImpl implements BundleContext, EventDispatcher<Object, Object, Object> {
    /** true if the bundle context is still valid */
    private volatile boolean valid;

    /** Bundle object this context is associated with. */
    // This slot is accessed directly by the Framework instead of using
    // the getBundle() method because the Framework needs access to the bundle
    // even when the context is invalid while the close method is being called.
    final EquinoxBundle bundle;

    /** Internal equinox container object. */
    final EquinoxContainer container;

    /**
     * Services that bundle is using. Key is ServiceRegistrationImpl, Value is
     * ServiceUse
     */
    /* @GuardedBy("contextLock") */
    private HashMap<ServiceRegistrationImpl<?>, ServiceUse<?>> servicesInUse;

    /** private object for locking */
    private final Object contextLock = new Object();

    /**
     * Construct a BundleContext which wrappers the framework for a bundle
     *
     * @param bundle The bundle we are wrapping.
     */
    public BundleContextImpl(EquinoxBundle bundle, EquinoxContainer container) {
        this.bundle = bundle;
        this.container = container;
        valid = true;
        synchronized (contextLock) {
            servicesInUse = null;
        }
    }

    /**
     * Destroy the wrapper. This is called when the bundle is stopped.
     */
    protected void close() {
        valid = false; /* invalidate context */

        final ServiceRegistry registry = container.getServiceRegistry();

        registry.removeAllServiceListeners(this);
        container.getEventPublisher().removeAllListeners(this);

        /* service's registered by the bundle, if any, are unregistered. */
        registry.unregisterServices(this);

        /* service's used by the bundle, if any, are released. */
        registry.releaseServicesInUse(this);

        synchronized (contextLock) {
            servicesInUse = null;
        }
    }

    /**
     * Retrieve the value of the named environment property.
     *
     * @param key The name of the requested property.
     * @return The value of the requested property, or <code>null</code> if the
     * property is undefined.
     */
    @Override
    public String getProperty(String key) {
        SecurityManager sm = System.getSecurityManager();

        if (sm != null) {
            sm.checkPropertyAccess(key);
        }

        return (container.getConfiguration().getProperty(key));
    }

    /**
     * Retrieve the Bundle object for the context bundle.
     *
     * @return The context bundle's Bundle object.
     */
    @Override
    public Bundle getBundle() {
        return getBundleImpl();
    }

    public EquinoxBundle getBundleImpl() {
        return bundle;
    }

    /**
     * Retrieve the bundle that has the given unique identifier.
     *
     * @param id The identifier of the bundle to retrieve.
     * @return A Bundle object, or <code>null</code> if the identifier doesn't match
     * any installed bundle.
     */
    @Override
    public Bundle getBundle(long id) {
        Module m = container.getStorage().getModuleContainer().getModule(id);
        if (m == null) {
            return null;
        }

        List<Bundle> bundles = new ArrayList<>(1);
        bundles.add(m.getBundle());
        notifyFindHooks(this, bundles);
        if (bundles.isEmpty()) {
            return null;
        }
        return m.getBundle();
    }

    @Override
    public Bundle getBundle(String location) {
        Module m = container.getStorage().getModuleContainer().getModule(location);
        return m == null ? null : m.getBundle();
    }

    /**
     * Retrieve a list of all installed bundles. The list is valid at the time of
     * the call to getBundles, but the framework is a very dynamic environment and
     * bundles can be installed or uninstalled at anytime.
     *
     * @return An array of {@link Bundle} objects, one object per installed bundle.
     */
    @Override
    public Bundle[] getBundles() {
        List<Module> modules = container.getStorage().getModuleContainer().getModules();
        List<Bundle> bundles = new ArrayList<>(modules.size());
        for (Module module : modules) {
            bundles.add(module.getBundle());
        }

        notifyFindHooks(this, bundles);
        return bundles.toArray(new Bundle[bundles.size()]);
    }

    private void notifyFindHooks(final BundleContextImpl context, List<Bundle> allBundles) {
        if (context.getBundleImpl().getBundleId() == 0) {
            // Make a copy for the purposes of calling the hooks;
            // The the removals from the hooks are ignored
            allBundles = new ArrayList<>(allBundles);
        }
        final Collection<Bundle> shrinkable = new ShrinkableCollection<>(allBundles);
        if (System.getSecurityManager() == null) {
            notifyFindHooksPriviledged(context, shrinkable);
        } else {
            AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
                notifyFindHooksPriviledged(context, shrinkable);
                return null;
            });
        }
    }

    void notifyFindHooksPriviledged(final BundleContextImpl context, final Collection<Bundle> allBundles) {
        ServiceRegistry sr = container.getServiceRegistry();
        if (sr == null) {
            allBundles.clear();
        } else {
            sr.notifyHooksPrivileged(FindHook.class, "find", //$NON-NLS-1$
                (hook, hookRegistration) -> hook.find(context, allBundles));
        }
    }

    @Override
    public void addServiceListener(ServiceListener listener, String filter) throws InvalidSyntaxException {
        checkValid();

        if (listener == null) {
            throw new IllegalArgumentException();
        }
        container.getServiceRegistry().addServiceListener(this, listener, filter);
    }

    /**
     * Remove a service listener. The listener is removed from the context bundle's
     * list of listeners. See {@link #getBundle() getBundle()} for a definition of
     * context bundle.
     *
     * <p>
     * If this method is called with a listener which is not registered, then this
     * method does nothing.
     *
     * @param listener The service listener to remove.
     */
    @Override
    public void removeServiceListener(ServiceListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException();
        }
        container.getServiceRegistry().removeServiceListener(this, listener);
    }

    /**
     * Add a bundle listener. {@link BundleListener}s are notified when a bundle has
     * a lifecycle state change. The listener is added to the context bundle's list
     * of listeners. See {@link #getBundle() getBundle()} for a definition of
     * context bundle.
     *
     * @param listener The bundle listener to add.
     * @exception IllegalStateException If the bundle context has stopped.
     * @see BundleEvent
     * @see BundleListener
     */
    @Override
    public void addBundleListener(BundleListener listener) {
        checkValid();
        if (listener == null) {
            throw new IllegalArgumentException();
        }

        container.getEventPublisher().addBundleListener(listener, this);
    }

    /**
     * Remove a bundle listener. The listener is removed from the context bundle's
     * list of listeners. See {@link #getBundle() getBundle()} for a definition of
     * context bundle.
     *
     * <p>
     * If this method is called with a listener which is not registered, then this
     * method does nothing.
     *
     * @param listener The bundle listener to remove.
     */
    @Override
    public void removeBundleListener(BundleListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException();
        }

        container.getEventPublisher().removeBundleListener(listener, this);
    }

    /**
     * Add a general framework listener. {@link FrameworkListener}s are notified of
     * general framework events. The listener is added to the context bundle's list
     * of listeners. See {@link #getBundle() getBundle()} for a definition of
     * context bundle.
     *
     * @param listener The framework listener to add.
     * @exception IllegalStateException If the bundle context has stopped.
     * @see FrameworkEvent
     * @see FrameworkListener
     */
    @Override
    public void addFrameworkListener(FrameworkListener listener) {
        checkValid();
        if (listener == null) {
            throw new IllegalArgumentException();
        }

        container.getEventPublisher().addFrameworkListener(listener, this);
    }

    /**
     * Register a service with multiple names. This method registers the given
     * service object with the given properties under the given class names. A
     * {@link ServiceRegistration} object is returned. The
     * {@link ServiceRegistration} object is for the private use of the bundle
     * registering the service and should not be shared with other bundles. The
     * registering bundle is defined to be the context bundle. See
     * {@link #getBundle()} for a definition of context bundle. Other bundles can
     * locate the service by using either the {@link #getServiceReferences
     * getServiceReferences} or {@link #getServiceReference getServiceReference}
     * method.
     *
     * <p>
     * A bundle can register a service object that implements the
     * {@link ServiceFactory} interface to have more flexiblity in providing service
     * objects to different bundles.
     *
     * <p>
     * The following steps are followed to register a service:
     * <ol>
     * <li>If the service parameter is not a {@link ServiceFactory}, an
     * <code>IllegalArgumentException</code> is thrown if the service parameter is
     * not an <code>instanceof</code> all the classes named.
     * <li>The service is added to the framework's service registry and may now be
     * used by other bundles.
     * <li>A {@link ServiceEvent} of type {@link ServiceEvent#REGISTERED} is
     * synchronously sent.
     * <li>A {@link ServiceRegistration} object for this registration is returned.
     * </ol>
     *
     * @param clazzes The class names under which the service can be located. The
     * class names in this array will be stored in the service's
     * properties under the key "objectClass".
     * @param service The service object or a {@link ServiceFactory} object.
     * @param properties The properties for this service. The keys in the properties
     * object must all be Strings. Changes should not be made to
     * this object after calling this method. To update the
     * service's properties call the
     * {@link ServiceRegistration#setProperties
     * ServiceRegistration.setProperties} method. This parameter
     * may be <code>null</code> if the service has no properties.
     * @return A {@link ServiceRegistration} object for use by the bundle
     * registering the service to update the service's properties or to
     * unregister the service.
     * @exception IllegalArgumentException If one of the following is
     * true:
     * <ul>
     * <li>The service parameter is
     * null.
     * <li>The service parameter is
     * not a {@link ServiceFactory}
     * and is not an
     * <code>instanceof</code> all the
     * named classes in the clazzes
     * parameter.
     * </ul>
     * @exception SecurityException If the caller does not have
     * {@link ServicePermission}
     * permission to "register" the
     * service for all the named
     * classes and the Java runtime
     * environment supports
     * permissions.
     * @exception IllegalStateException If the bundle context has
     * stopped.
     * @see ServiceRegistration
     * @see ServiceFactory
     */
    @Override
    public ServiceRegistration<?> registerService(String[] clazzes, Object service, Dictionary<String, ?> properties) {
        checkValid();
        return container.getServiceRegistry().registerService(this, clazzes, service, properties);
    }

    /**
     * Register a service with a single name. This method registers the given
     * service object with the given properties under the given class name.
     *
     * <p>
     * This method is otherwise identical to
     * {@link #registerService(String[], Object, Dictionary)}
     * and is provided as a convenience when the service parameter will only be
     * registered under a single class name.
     *
     * @see #registerService(String[], Object,
     * Dictionary)
     */
    @Override
    public ServiceRegistration<?> registerService(String clazz, Object service, Dictionary<String, ?> properties) {
        String[] clazzes = new String[] { clazz };

        return registerService(clazzes, service, properties);
    }

    /**
     * Returns a list of <code>ServiceReference</code> objects. This method returns
     * a list of <code>ServiceReference</code> objects for services which implement
     * and were registered under the specified class and match the specified filter
     * criteria.
     *
     * <p>
     * The list is valid at the time of the call to this method, however as the
     * Framework is a very dynamic environment, services can be modified or
     * unregistered at anytime.
     *
     * <p>
     * <code>filter</code> is used to select the registered service whose properties
     * objects contain keys and values which satisfy the filter. See
     * {@link Filter}for a description of the filter string syntax.
     *
     * <p>
     * If <code>filter</code> is <code>null</code>, all registered services are
     * considered to match the filter.
     * <p>
     * If <code>filter</code> cannot be parsed, an {@link InvalidSyntaxException}
     * will be thrown with a human readable message where the filter became
     * unparsable.
     *
     * <p>
     * The following steps are required to select a service:
     * <ol>
     * <li>If the Java Runtime Environment supports permissions, the caller is
     * checked for the <code>ServicePermission</code> to get the service with the
     * specified class. If the caller does not have the correct permission,
     * <code>null</code> is returned.
     * <li>If the filter string is not <code>null</code>, the filter string is
     * parsed and the set of registered services which satisfy the filter is
     * produced. If the filter string is <code>null</code>, then all registered
     * services are considered to satisfy the filter.
     * <li>If <code>clazz</code> is not <code>null</code>, the set is further
     * reduced to those services which are an <code>instanceof</code> and were
     * registered under the specified class. The complete list of classes of which a
     * service is an instance and which were specified when the service was
     * registered is available from the service's
     * {@link Constants#OBJECTCLASS}property.
     * <li>An array of <code>ServiceReference</code> to the selected services is
     * returned.
     * </ol>
     *
     * @param clazz The class name with which the service was registered, or
     * <code>null</code> for all services.
     * @param filter The filter criteria.
     * @return An array of <code>ServiceReference</code> objects, or
     * <code>null</code> if no services are registered which satisfy the
     * search.
     * @exception InvalidSyntaxException If <code>filter</code> contains an invalid
     * filter string which cannot be parsed.
     */
    @Override
    public ServiceReference<?>[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
        checkValid();
        return container.getServiceRegistry().getServiceReferences(this, clazz, filter, false);
    }

    @Override
    public ServiceReference<?>[] getAllServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
        checkValid();
        return container.getServiceRegistry().getServiceReferences(this, clazz, filter, true);
    }

    /**
     * Get a service reference. Retrieves a {@link ServiceReference} for a service
     * which implements the named class.
     *
     * <p>
     * This reference is valid at the time of the call to this method, but since the
     * framework is a very dynamic environment, services can be modified or
     * unregistered at anytime.
     *
     * <p>
     * This method is provided as a convenience for when the caller is interested in
     * any service which implements a named class. This method is the same as
     * calling {@link #getServiceReferences getServiceReferences} with a
     * <code>null</code> filter string but only a single {@link ServiceReference} is
     * returned.
     *
     * @param clazz The class name which the service must implement.
     * @return A {@link ServiceReference} object, or <code>null</code> if no
     * services are registered which implement the named class.
     * @see #getServiceReferences
     */
    @Override
    public ServiceReference<?> getServiceReference(String clazz) {
        checkValid();

        return container.getServiceRegistry().getServiceReference(this, clazz);
    }

    /**
     * Get a service's service object. Retrieves the service object for a service. A
     * bundle's use of a service is tracked by a use count. Each time a service's
     * service object is returned by {@link #getService}, the context bundle's use
     * count for the service is incremented by one. Each time the service is release
     * by {@link #ungetService}, the context bundle's use count for the service is
     * decremented by one. When a bundle's use count for a service drops to zero,
     * the bundle should no longer use the service. See {@link #getBundle()} for a
     * definition of context bundle.
     *
     * <p>
     * This method will always return <code>null</code> when the service associated
     * with this reference has been unregistered.
     *
     * <p>
     * The following steps are followed to get the service object:
     * <ol>
     * <li>If the service has been unregistered, <code>null</code> is returned.
     * <li>The context bundle's use count for this service is incremented by one.
     * <li>If the context bundle's use count for the service is now one and the
     * service was registered with a {@link ServiceFactory}, the
     * {@link ServiceFactory#getService ServiceFactory.getService} method is called
     * to create a service object for the context bundle. This service object is
     * cached by the framework. While the context bundle's use count for the service
     * is greater than zero, subsequent calls to get the services's service object
     * for the context bundle will return the cached service object. <br>
     * If the service object returned by the {@link ServiceFactory} is not an
     * <code>instanceof</code> all the classes named when the service was registered
     * or the {@link ServiceFactory} throws an exception, <code>null</code> is
     * returned and a {@link FrameworkEvent} of type {@link FrameworkEvent#ERROR} is
     * broadcast.
     * <li>The service object for the service is returned.
     * </ol>
     *
     * @param reference A reference to the service whose service object is desired.
     * @return A service object for the service associated with this reference, or
     * <code>null</code> if the service is not registered.
     * @exception SecurityException If the caller does not have
     * {@link ServicePermission}
     * permission to "get" the service
     * using at least one of the named
     * classes the service was registered
     * under and the Java runtime
     * environment supports permissions.
     * @exception IllegalStateException If the bundle context has stopped.
     * @see #ungetService
     * @see ServiceFactory
     */
    @Override
    public <S> S getService(ServiceReference<S> reference) {
        checkValid();
        if (reference == null)
            throw new NullPointerException("A null service reference is not allowed."); //$NON-NLS-1$
        provisionServicesInUseMap();
        S service = container.getServiceRegistry().getService(this, (ServiceReferenceImpl<S>) reference);
        return service;
    }

    /**
     * Unget a service's service object. Releases the service object for a service.
     * If the context bundle's use count for the service is zero, this method
     * returns <code>false</code>. Otherwise, the context bundle's use count for the
     * service is decremented by one. See {@link #getBundle()} for a definition of
     * context bundle.
     *
     * <p>
     * The service's service object should no longer be used and all references to
     * it should be destroyed when a bundle's use count for the service drops to
     * zero.
     *
     * <p>
     * The following steps are followed to unget the service object:
     * <ol>
     * <li>If the context bundle's use count for the service is zero or the service
     * has been unregistered, <code>false</code> is returned.
     * <li>The context bundle's use count for this service is decremented by one.
     * <li>If the context bundle's use count for the service is now zero and the
     * service was registered with a {@link ServiceFactory}, the
     * {@link ServiceFactory#ungetService ServiceFactory.ungetService} method is
     * called to release the service object for the context bundle.
     * <li><code>true</code> is returned.
     * </ol>
     *
     * @param reference A reference to the service to be released.
     * @return <code>false</code> if the context bundle's use count for the service
     * is zero or if the service has been unregistered, otherwise
     * <code>true</code>.
     * @exception IllegalStateException If the bundle context has stopped.
     * @see #getService
     * @see ServiceFactory
     */
    @Override
    public boolean ungetService(ServiceReference<?> reference) {
        return container.getServiceRegistry().ungetService(this, (ServiceReferenceImpl<?>) reference);
    }

    /**
     * Return the map of ServiceRegistrationImpl to ServiceUse for services being
     * used by this context.
     * 
     * @return A map of ServiceRegistrationImpl to ServiceUse for services in use by
     * this context.
     */
    public Map<ServiceRegistrationImpl<?>, ServiceUse<?>> getServicesInUseMap() {
        synchronized (contextLock) {
            return servicesInUse;
        }
    }

    /**
     * Provision the map of ServiceRegistrationImpl to ServiceUse for services being
     * used by this context.
     */
    public void provisionServicesInUseMap() {
        synchronized (contextLock) {
            if (servicesInUse == null)
                // Cannot predict how many services a bundle will use, start with a small table.
                servicesInUse = new HashMap<>(10);
        }
    }

    /**
     * Construct a Filter object. This filter object may be used to match a
     * ServiceReference or a Dictionary. See Filter for a description of the filter
     * string syntax.
     *
     * @param filter The filter string.
     * @return A Filter object encapsulating the filter string.
     */
    @Override
    public Filter createFilter(String filter) throws InvalidSyntaxException {
        return FilterImpl.newInstance(filter);
    }

    /**
     * This method checks that the context is still valid. If the context is no
     * longer valid, an IllegalStateException is thrown.
     *
     * @exception IllegalStateException If the context bundle has stopped.
     */
    public void checkValid() {
        if (!isValid()) {
            throw new IllegalStateException(Msg.BUNDLE_CONTEXT_INVALID_EXCEPTION + ' ' + bundle);
        }
    }

    /**
     * This method checks that the context is still valid.
     *
     * @return true if the context is still valid; false otherwise
     */
    public boolean isValid() {
        return valid;
    }

    @Override
    public <S> ServiceRegistration<S> registerService(Class<S> clazz, S service, Dictionary<String, ?> properties) {
        @SuppressWarnings("unchecked")
        ServiceRegistration<S> registration
            = (ServiceRegistration<S>) registerService(clazz.getName(), service, properties);
        return registration;
    }

    @Override
    public <S> ServiceRegistration<S> registerService(Class<S> clazz, ServiceFactory<S> factory,
        Dictionary<String, ?> properties) {
        @SuppressWarnings("unchecked")
        ServiceRegistration<S> registration
            = (ServiceRegistration<S>) registerService(clazz.getName(), factory, properties);
        return registration;
    }

    @Override
    public <S> ServiceReference<S> getServiceReference(Class<S> clazz) {
        @SuppressWarnings("unchecked")
        ServiceReference<S> reference = (ServiceReference<S>) getServiceReference(clazz.getName());
        return reference;
    }

    @Override
    public <S> Collection<ServiceReference<S>> getServiceReferences(Class<S> clazz, String filter)
        throws InvalidSyntaxException {
        @SuppressWarnings("unchecked")
        ServiceReference<S>[] refs = (ServiceReference<S>[]) getServiceReferences(clazz.getName(), filter);
        if (refs == null) {
            return Collections.emptyList();
        }
        List<ServiceReference<S>> result = new ArrayList<>(refs.length);
        Collections.addAll(result, refs);
        return result;
    }

    public EquinoxContainer getContainer() {
        return container;
    }

    @Override
    public <S> ServiceObjects<S> getServiceObjects(ServiceReference<S> reference) {
        checkValid();
        if (reference == null)
            throw new NullPointerException("A null service reference is not allowed."); //$NON-NLS-1$
        provisionServicesInUseMap();
        ServiceObjects<S> serviceObjects
            = container.getServiceRegistry().getServiceObjects(this, (ServiceReferenceImpl<S>) reference);
        return serviceObjects;
    }
}
