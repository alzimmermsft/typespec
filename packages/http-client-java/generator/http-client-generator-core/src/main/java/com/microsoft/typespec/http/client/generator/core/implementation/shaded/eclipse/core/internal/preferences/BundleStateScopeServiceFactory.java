/*******************************************************************************
 * Copyright (c) 2022 Christoph Läubrich and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christoph Läubrich - initial API and implementation
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.preferences;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.preferences.IScopeContext;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.*;

public class BundleStateScopeServiceFactory implements ServiceFactory<IScopeContext> {

	@Override
	public IScopeContext getService(Bundle bundle, ServiceRegistration<IScopeContext> registration) {
		return new BundleStateScope(bundle);
	}

	@Override
	public void ungetService(Bundle bundle, ServiceRegistration<IScopeContext> registration, IScopeContext service) {
		// nothing to do here...
	}

}
