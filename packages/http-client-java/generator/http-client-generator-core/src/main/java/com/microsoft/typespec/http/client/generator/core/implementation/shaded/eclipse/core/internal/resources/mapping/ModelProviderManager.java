/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
 *     James Blackburn (Broadcom Corp.) - ongoing development
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 473427
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources.mapping;

import java.util.HashMap;
import java.util.Map;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.utils.Policy;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.ResourcesPlugin;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.mapping.IModelProviderDescriptor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.mapping.ModelProvider;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.*;

public class ModelProviderManager {

	private static Map<String, IModelProviderDescriptor> descriptors;
	private static ModelProviderManager instance;

	public synchronized static ModelProviderManager getDefault() {
		if (instance == null) {
			instance = new ModelProviderManager();
		}
		return instance;
	}

	private void detectCycles() {
		// TODO Auto-generated method stub

	}

	public IModelProviderDescriptor getDescriptor(String id) {
		lazyInitialize();
		return descriptors.get(id);
	}

	public IModelProviderDescriptor[] getDescriptors() {
		lazyInitialize();
		return descriptors.values().toArray(new IModelProviderDescriptor[descriptors.size()]);
	}

	public ModelProvider getModelProvider(String modelProviderId) throws CoreException {
		IModelProviderDescriptor desc = getDescriptor(modelProviderId);
		if (desc == null) {
			return null;
		}
		return desc.getModelProvider();
	}

	protected void lazyInitialize() {
		if (descriptors != null) {
			return;
		}
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PT_MODEL_PROVIDERS);
		IExtension[] extensions = point.getExtensions();
		descriptors = new HashMap<>(extensions.length * 2 + 1);
		for (IExtension extension : extensions) {
			IModelProviderDescriptor desc = null;
			try {
				desc = new ModelProviderDescriptor(extension);
			} catch (CoreException e) {
				Policy.log(e);
			}
			if (desc != null) {
				descriptors.put(desc.getId(), desc);
			}
		}
		//do cycle detection now so it only has to be done once
		//cycle detection on a graph subset is a pain
		detectCycles();
	}

}
