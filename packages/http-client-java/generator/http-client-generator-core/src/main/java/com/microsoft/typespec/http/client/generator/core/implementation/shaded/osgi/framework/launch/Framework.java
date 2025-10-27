/*
 * Copyright (c) OSGi Alliance (2008, 2020). All Rights Reserved.
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

package com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.launch;

import java.net.URL;
import java.util.Enumeration;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.annotation.versioning.ProviderType;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Bundle;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Constants;

/**
 * A Framework instance. A Framework is also known as a System Bundle.
 * 
 * <p>
 * Framework instances are created using a {@link FrameworkFactory}. The methods
 * of this interface can be used to manage and control the created framework
 * instance.
 * 
 * @ThreadSafe
 * @author $Id: bf960bdc39d19a780694a8cab5a555b3e0dc0fde $
 */
@ProviderType
public interface Framework extends Bundle {

    /**
	 * Returns the Framework unique identifier. This Framework is assigned the
	 * unique identifier zero (0) since this Framework is also a System Bundle.
	 * 
	 * @return 0.
	 * @see Bundle#getBundleId()
	 */
	@Override
	long getBundleId();

	/**
	 * Returns the Framework location identifier. This Framework is assigned the
	 * unique location &quot;{@code System Bundle}&quot; since this Framework is
	 * also a System Bundle.
	 * 
	 * @return The string &quot;{@code System Bundle}&quot;.
	 * @throws SecurityException If the caller does not have the appropriate
	 *         {@code AdminPermission[this,METADATA]}, and the Java Runtime
	 *         Environment supports permissions.
	 * @see Bundle#getLocation()
	 * @see Constants#SYSTEM_BUNDLE_LOCATION
	 */
	@Override
	String getLocation();

	/**
	 * Returns the symbolic name of this Framework. The symbolic name is unique
	 * for the implementation of the framework. However, the symbolic name
	 * &quot;{@code system.bundle}&quot; must be recognized as an alias to the
	 * implementation-defined symbolic name since this Framework is also a
	 * System Bundle.
	 * 
	 * @return The symbolic name of this Framework.
	 * @see Bundle#getSymbolicName()
	 * @see Constants#SYSTEM_BUNDLE_SYMBOLICNAME
	 */
	@Override
	String getSymbolicName();

	/**
	 * Returns {@code null} as a framework implementation does not have a proper
	 * bundle from which to return an entry.
	 * 
	 * @param path Ignored.
	 * @return {@code null} as a framework implementation does not have a proper
	 *         bundle from which to return an entry.
	 */
	@Override
	URL getEntry(String path);

	/**
	 * Returns the time when the set of bundles in this framework was last
	 * modified. The set of bundles is considered to be modified when a bundle
	 * is installed, updated or uninstalled.
	 * 
	 * <p>
	 * The time value is the number of milliseconds since January 1, 1970,
	 * 00:00:00 UTC.
	 * 
	 * @return The time when the set of bundles in this framework was last
	 *         modified.
	 */
	@Override
	long getLastModified();

	/**
	 * Returns {@code null} as a framework implementation does not have a proper
	 * bundle from which to return entries.
	 * 
	 * @param path Ignored.
	 * @param filePattern Ignored.
	 * @param recurse Ignored.
	 * @return {@code null} as a framework implementation does not have a proper
	 *         bundle from which to return entries.
	 */
	@Override
	Enumeration<URL> findEntries(String path, String filePattern, boolean recurse);

	/**
	 * Adapt this Framework to the specified type.
	 * 
	 * <p>
	 * Adapting this Framework to the specified type may require certain checks,
	 * including security checks, to succeed. If a check does not succeed, then
	 * this Framework cannot be adapted and {@code null} is returned. If this
	 * Framework is not {@link #init() initialized}, then {@code null} is
	 * returned if the specified type is one of the OSGi defined types to which
	 * a system bundle can be adapted.
	 * 
	 * @param <A> The type to which this Framework is to be adapted.
	 * @param type Class object for the type to which this Framework is to be
	 *        adapted.
	 * @return The object, of the specified type, to which this Framework has
	 *         been adapted or {@code null} if this Framework cannot be adapted
	 */
	@Override
	<A> A adapt(Class<A> type);
}
