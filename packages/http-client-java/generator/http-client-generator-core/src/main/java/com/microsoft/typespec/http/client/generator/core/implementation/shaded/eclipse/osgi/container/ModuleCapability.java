/*******************************************************************************
 * Copyright (c) 2012, 2020 IBM Corporation and others.
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.namespace.NativeNamespace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.wiring.BundleCapability;

/**
 * An implementation of {@link BundleCapability}.
 * 
 * @since 3.10
 */
public final class ModuleCapability implements BundleCapability {
	private final String namespace;
	private final Map<String, String> directives;
	private final Map<String, Object> attributes;
	private final Map<String, Object> transientAttrs;
	private final ModuleRevision revision;

	ModuleCapability(String namespace, Map<String, String> directives, Map<String, Object> attributes,
			ModuleRevision revision) {
		this.namespace = namespace;
		this.directives = directives;
		this.attributes = attributes;
		this.transientAttrs = NativeNamespace.NATIVE_NAMESPACE.equals(namespace) ? new HashMap<>(0) : null;
		this.revision = revision;
	}

	@Override
	public ModuleRevision getRevision() {
		return revision;
	}

	@Override
	public String getNamespace() {
		return namespace;
	}

	@Override
	public Map<String, String> getDirectives() {
		return directives;
	}

	@Override
	public Map<String, Object> getAttributes() {
		if (transientAttrs == null)
			return attributes;
		Map<String, Object> result = new HashMap<>(transientAttrs);
		result.putAll(attributes);
		return Collections.unmodifiableMap(result);
	}

	Map<String, Object> getPersistentAttributes() {
		return attributes;
	}

    @Override
	public ModuleRevision getResource() {
		return revision;
	}

	@Override
	public String toString() {
		return namespace + ModuleContainer.toString(attributes, false) + ModuleContainer.toString(directives, true);
	}
}
