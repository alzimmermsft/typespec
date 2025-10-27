/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.content;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.*;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.content.IContentDescription;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.content.IContentTypeSettings;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.preferences.IScopeContext;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.prefs.BackingStoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.prefs.Preferences;

public class ContentTypeSettings implements IContentTypeSettings, IContentTypeInfo {

	private final ContentType contentType;
	private final IScopeContext context;

    static String[] getFileSpecs(IScopeContext context, String contentTypeId, int type) {
		Preferences contentTypeNode = ContentTypeManager.getInstance().getPreferences(context).node(contentTypeId);
		return getFileSpecs(contentTypeNode, type);
	}

	static String[] getFileSpecs(Preferences contentTypeNode, int type) {
		String key = ContentType.getPreferenceKey(type);
		String existing = contentTypeNode.get(key, null);
		return Util.parseItems(existing);
	}

	public static String internalGetDefaultProperty(ContentType current, final Preferences contentTypePrefs, final QualifiedName key) throws BackingStoreException {
		String id = current.getId();
		if (contentTypePrefs.nodeExists(id)) {
			Preferences contentTypeNode = contentTypePrefs.node(id);
			String propertyValue = contentTypeNode.get(key.getLocalName(), null);
			if (propertyValue != null) {
				return propertyValue;
			}
		}
		// try built-in settings
		String propertyValue = current.basicGetDefaultProperty(key);
		if (propertyValue != null) {
			return propertyValue;
		}
		// try ancestor
		ContentType baseType = (ContentType) current.getBaseType();
		return baseType == null ? null : internalGetDefaultProperty(baseType, contentTypePrefs, key);
	}

    public ContentTypeSettings(ContentType contentType, IScopeContext context) {
		this.context = context;
		this.contentType = contentType;
	}

	@Override
	public ContentType getContentType() {
		return contentType;
	}

	@Override
	public String getDefaultCharset() {
		return getDefaultProperty(IContentDescription.CHARSET);
	}

	@Override
	public String getDefaultProperty(final QualifiedName key) {
		final Preferences contentTypePrefs = ContentTypeManager.getInstance().getPreferences(context);
		try {
			String propertyValue = internalGetDefaultProperty(contentType, contentTypePrefs, key);
			return "".equals(propertyValue) ? null : propertyValue; //$NON-NLS-1$
		} catch (BackingStoreException e) {
			return null;
		}
	}

	@Override
	public String[] getFileSpecs(int type) {
		return getFileSpecs(context, contentType.getId(), type);
	}

	@Override
	public String getId() {
		return contentType.getId();
	}

	@Override
	public boolean isUserDefined() {
		return getContentType().isUserDefined();
	}

}
