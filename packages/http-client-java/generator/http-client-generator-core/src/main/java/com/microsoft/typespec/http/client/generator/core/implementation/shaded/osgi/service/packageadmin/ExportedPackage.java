/*
 * Copyright (c) OSGi Alliance (2001, 2014). All Rights Reserved.
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

package com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.packageadmin;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Version;

/**
 * An exported package.
 * 
 * Objects implementing this interface are created by the Package Admin service.
 * 
 * <p>
 * The term <i>exported package</i> refers to a package that has been exported
 * from a resolved bundle. This package may or may not be currently wired to
 * other bundles.
 * 
 * <p>
 * The information about an exported package provided by this object may change.
 * An {@code ExportedPackage} object becomes stale if the package it
 * references has been updated or removed as a result of calling
 * {@code PackageAdmin.refreshPackages()}.
 * 
 * If this object becomes stale, its {@code getName()} and
 * {@code getVersion()} methods continue to return their original values,
 * {@code isRemovalPending()} returns {@code true}, and
 * {@code getExportingBundle()} and {@code getImportingBundles()}
 * return {@code null}.
 * 
 * @ThreadSafe
 * @noimplement
 * @deprecated The PackageAdmin service has been replaced by the
 *             <code>com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.wiring</code> package.
 * @author $Id: f4cdb9e84ce788c16d5304166a2b9eeecb5fabf3 $
 */
public interface ExportedPackage {
	/**
	 * Returns the name of the package associated with this exported package.
	 * 
	 * @return The name of this exported package.
	 */
    String getName();

	/**
	 * Returns the version of this exported package.
	 * 
	 * @return The version of this exported package, or
	 *         {@link Version#emptyVersion} if no version information is
	 *         available.
	 * @since 1.2
	 */
    Version getVersion();

}
