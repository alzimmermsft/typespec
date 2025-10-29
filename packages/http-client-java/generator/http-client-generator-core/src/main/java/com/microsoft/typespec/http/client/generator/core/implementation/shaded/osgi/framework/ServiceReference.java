/*
 * Copyright (c) OSGi Alliance (2000, 2019). All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.annotation.versioning.ProviderType;
import java.util.Dictionary;

/**
 * A reference to a service.
 * 
 * <p>
 * The Framework returns {@code ServiceReference} objects from the
 * {@code BundleContext.getServiceReference} and
 * {@code BundleContext.getServiceReferences} methods.
 * <p>
 * A {@code ServiceReference} object may be shared between bundles and can be
 * used to examine the properties of the service and to get the service object.
 * <p>
 * Every service registered in the Framework has a unique
 * {@code ServiceRegistration} object and may have multiple, distinct
 * {@code ServiceReference} objects referring to it. {@code ServiceReference}
 * objects associated with a {@code ServiceRegistration} object have the same
 * {@code hashCode} and are considered equal (more specifically, their
 * {@code equals()} method will return {@code true} when compared).
 * <p>
 * If the same service object is registered multiple times,
 * {@code ServiceReference} objects associated with different
 * {@code ServiceRegistration} objects are not equal.
 * 
 * @param <S> Type of Service.
 * @see BundleContext#getServiceReference(Class)
 * @see BundleContext#getServiceReference(String)
 * @see BundleContext#getServiceReferences(Class, String)
 * @see BundleContext#getServiceReferences(String, String)
 * @see BundleContext#getService(ServiceReference)
 * @see BundleContext#getServiceObjects(ServiceReference)
 * @ThreadSafe
 * @author $Id: adb91d7f0922417180e901dc6ec447f467b34921 $
 */
@ProviderType
public interface ServiceReference<S> extends Comparable<Object>, BundleReference {
    /**
     * Returns the property value to which the specified property key is mapped
     * in the properties {@code Dictionary} object of the service referenced by
     * this {@code ServiceReference} object.
     * 
     * <p>
     * Property keys are case-insensitive.
     * 
     * <p>
     * This method must continue to return property values after the service has
     * been unregistered. This is so references to unregistered services (for
     * example, {@code ServiceReference} objects stored in the log) can still be
     * interrogated.
     * 
     * @param key The property key.
     * @return The property value to which the key is mapped; {@code null} if
     * there is no property named after the key.
     */
    Object getProperty(String key);

    /**
     * Returns an array of the keys in the properties {@code Dictionary} object
     * of the service referenced by this {@code ServiceReference} object.
     * 
     * <p>
     * This method will continue to return the keys after the service has been
     * unregistered. This is so references to unregistered services (for
     * example, {@code ServiceReference} objects stored in the log) can still be
     * interrogated.
     * 
     * <p>
     * This method is <i>case-preserving </i>; this means that every key in the
     * returned array must have the same case as the corresponding key in the
     * properties {@code Dictionary} that was passed to the
     * {@link BundleContext#registerService(String[],Object,Dictionary)} or
     * {@link ServiceRegistration#setProperties(Dictionary)} methods.
     * 
     * @return An array of property keys.
     */
    String[] getPropertyKeys();

    /**
     * Returns the bundle that registered the service referenced by this
     * {@code ServiceReference} object.
     * 
     * <p>
     * This method must return {@code null} when the service has been
     * unregistered. This can be used to determine if the service has been
     * unregistered.
     * 
     * @return The bundle that registered the service referenced by this
     * {@code ServiceReference} object; {@code null} if that service has
     * already been unregistered.
     * @see BundleContext#registerService(String[],Object,Dictionary)
     */
    @Override
    Bundle getBundle();

    /**
     * Returns the bundles that are using the service referenced by this
     * {@code ServiceReference} object. Specifically, this method returns the
     * bundles whose usage count for that service is greater than zero.
     * 
     * @return An array of bundles whose usage count for the service referenced
     * by this {@code ServiceReference} object is greater than zero;
     * {@code null} if no bundles are currently using that service.
     * 
     * @since 1.1
     */
    Bundle[] getUsingBundles();

    /**
     * Compares this {@code ServiceReference} with the specified
     * {@code ServiceReference} for order.
     * 
     * <p>
     * If this {@code ServiceReference} and the specified
     * {@code ServiceReference} have the same {@link Constants#SERVICE_ID
     * service id} they are equal. This {@code ServiceReference} is less than
     * the specified {@code ServiceReference} if it has a lower
     * {@link Constants#SERVICE_RANKING service ranking} and greater if it has a
     * higher service ranking. Otherwise, if this {@code ServiceReference} and
     * the specified {@code ServiceReference} have the same
     * {@link Constants#SERVICE_RANKING service ranking}, this
     * {@code ServiceReference} is less than the specified
     * {@code ServiceReference} if it has a higher {@link Constants#SERVICE_ID
     * service id} and greater if it has a lower service id.
     * 
     * @param reference The {@code ServiceReference} to be compared.
     * @return Returns a negative integer, zero, or a positive integer if this
     * {@code ServiceReference} is less than, equal to, or greater than
     * the specified {@code ServiceReference}.
     * @throws IllegalArgumentException If the specified
     * {@code ServiceReference} was not created by the same framework
     * instance as this {@code ServiceReference}.
     * @since 1.4
     */
    @Override
    int compareTo(Object reference);

    /**
     * Returns a copy of the properties of the service referenced by this
     * {@code ServiceReference} object.
     * <p>
     * This method will continue to return the properties after the service has
     * been unregistered. This is so references to unregistered services (for
     * example, {@code ServiceReference} objects stored in the log) can still be
     * interrogated.
     * <p>
     * The returned {@code Dictionary} object:
     * <ul>
     * <li>Must map property values by using property keys in a
     * <i>case-insensitive manner</i>.</li>
     * <li>Must return property keys is a <i>case-preserving</i> manner. This
     * means that the keys must have the same case as the corresponding key in
     * the properties {@code Dictionary} that was passed to the
     * {@link BundleContext#registerService(String[],Object,Dictionary)} or
     * {@link ServiceRegistration#setProperties(Dictionary)} methods.</li>
     * <li>Is the property of the caller and can be modified by the caller but
     * any changes are not reflected in the properties of the service.
     * {@link ServiceRegistration#setProperties(Dictionary)} must be called to
     * modify the properties of the service.</li>
     * </ul>
     * 
     * @return A copy of the properties of the service referenced by this
     * {@code ServiceReference} object
     * @since 1.9
     */
    Dictionary<String, Object> getProperties();

    /**
     * Adapt this {@code ServiceReference} object to the specified type.
     * <p>
     * Adapting this {@code ServiceReference} object to the specified type may
     * require certain checks, including security checks, to succeed. If a check
     * does not succeed, then this {@code ServiceReference} object cannot be
     * adapted and {@code null} is returned.
     * 
     * @param <A> The type to which this {@code ServiceReference} object is to
     * be adapted.
     * @param type Class object for the type to which this
     * {@code ServiceReference} object is to be adapted.
     * @return The object, of the specified type, to which this
     * {@code ServiceReference} object has been adapted or {@code null}
     * if this {@code ServiceReference} object cannot be adapted to the
     * specified type.
     * @throws SecurityException If the caller does not have the appropriate
     * {@code AdaptPermission[type,this,ADAPT]}, and the Java
     * Runtime Environment supports permissions.
     * @since 1.10
     */
    <A> A adapt(Class<A> type);
}
