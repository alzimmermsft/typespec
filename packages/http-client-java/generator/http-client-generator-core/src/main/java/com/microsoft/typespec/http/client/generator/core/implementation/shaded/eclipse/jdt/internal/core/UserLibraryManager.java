/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.preferences.IEclipsePreferences;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaCore;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.Util;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.prefs.BackingStoreException;

@SuppressWarnings({"rawtypes", "unchecked"})
public class UserLibraryManager {

	public final static String CP_USERLIBRARY_PREFERENCES_PREFIX = JavaCore.PLUGIN_ID+".userLibrary."; //$NON-NLS-1$

	private final Map<String, UserLibrary> userLibraries = new ConcurrentHashMap<>();

	/*
	 * Gets the library for a given name or <code>null</code> if no such library exists.
	 */
	public synchronized UserLibrary getUserLibrary(String libName) {
		return this.userLibraries.get(libName);
	}

    public UserLibraryManager() {
		IEclipsePreferences instancePreferences = JavaModelManager.getJavaModelManager().getInstancePreferences();
		String[] propertyNames;
		try {
			propertyNames = instancePreferences.keys();
		} catch (BackingStoreException e) {
			Util.log(e, "Exception while initializing user libraries"); //$NON-NLS-1$
			return;
		}

		boolean preferencesNeedFlush = false;
		for (String propertyName : propertyNames) {
			if (propertyName.startsWith(CP_USERLIBRARY_PREFERENCES_PREFIX)) {
				String propertyValue = instancePreferences.get(propertyName, null);
				if (propertyValue != null) {
					String libName= propertyName.substring(CP_USERLIBRARY_PREFERENCES_PREFIX.length());
					StringReader reader = new StringReader(propertyValue);
					UserLibrary library;
					try {
						library = UserLibrary.createFromString(reader);
					} catch (IOException | ClasspathEntry.AssertionFailedException e) {
						Util.log(e, "Exception while initializing user library " + libName); //$NON-NLS-1$
						instancePreferences.remove(propertyName);
						preferencesNeedFlush = true;
						continue;
					}
					this.userLibraries.put(libName, library);
				}
			}
		}
		if (preferencesNeedFlush) {
			try {
				instancePreferences.flush();
			} catch (BackingStoreException e) {
				Util.log(e, "Exception while flusing instance preferences"); //$NON-NLS-1$
			}
		}
	}

}
