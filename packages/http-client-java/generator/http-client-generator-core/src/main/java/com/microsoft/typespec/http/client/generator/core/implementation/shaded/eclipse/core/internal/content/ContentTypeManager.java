/*******************************************************************************
 *  Copyright (c) 2004, 2016 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Mickael Istria (Red Hat Inc.) - Bug 485227
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.content;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.InvalidRegistryObjectException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.ListenerList;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.content.IContentDescription;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.content.IContentType;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.content.IContentTypeManager;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.content.IContentTypeMatcher;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.preferences.IEclipsePreferences;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.preferences.IScopeContext;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.preferences.InstanceScope;
import java.io.InputStream;
import java.io.Reader;

public class ContentTypeManager extends ContentTypeMatcher implements IContentTypeManager {

    private static volatile ContentTypeManager instance;

    public static final int BLOCK_SIZE = 0x400;
    public static final String CONTENT_TYPE_PREF_NODE
        = IContentConstants.RUNTIME_NAME + IPath.SEPARATOR + "content-types"; //$NON-NLS-1$
    private ContentTypeCatalog catalog;
    private int catalogGeneration;

    /**
     * List of registered listeners (element type:
     * <code>IContentTypeChangeListener</code>).
     * These listeners are to be informed when
     * something in a content type changes.
     */
    protected final ListenerList<IContentTypeChangeListener> contentTypeListeners = new ListenerList<>();

    /**
     * Shuts down the platform's content type manager. After this call returns,
     * the content type manager will be closed for business.
     */
    public static void shutdown() {
        // there really is nothing left to do except null the instance.
        instance = null;
    }

    /**
     * Obtains this platform's content type manager. Lazyly creates and initializes
     * the platform's content type manager.
     *
     *
     * @return the content type manager
     */
    public static ContentTypeManager getInstance() {
        if (instance == null) {
            synchronized (ContentTypeManager.class) {
                if (instance == null) {
                    instance = new ContentTypeManager();
                }
            }
        }
        return instance;
    }

    /*
     * Returns the extension for a file name (omitting the leading '.').
     */
    static String getFileExtension(String fileName) {
        int dotPosition = fileName.lastIndexOf('.');
        return (dotPosition == -1 || dotPosition == fileName.length() - 1) ? "" : fileName.substring(dotPosition + 1); //$NON-NLS-1$
    }

    protected static ILazySource readBuffer(InputStream contents) {
        return new LazyInputStream(contents, BLOCK_SIZE);
    }

    protected static ILazySource readBuffer(Reader contents) {
        return new LazyReader(contents, BLOCK_SIZE);
    }

    public ContentTypeManager() {
        super(null, InstanceScope.INSTANCE);
        instance = this;
    }

    protected ContentTypeBuilder createBuilder(ContentTypeCatalog newCatalog) {
        return new ContentTypeBuilder(newCatalog);
    }

    @Override
    public IContentType[] getAllContentTypes() {
        ContentTypeCatalog currentCatalog = getCatalog();
        IContentType[] types = currentCatalog.getAllContentTypes();
        IContentType[] result = new IContentType[types.length];
        int generation = currentCatalog.getGeneration();
        for (int i = 0; i < result.length; i++) {
            result[i] = new ContentTypeHandler((ContentType) types[i], generation);
        }
        return result;
    }

    protected synchronized ContentTypeCatalog getCatalog() {
        if (catalog != null) {
            // already has one
            return catalog;
        }
        // create new catalog
        ContentTypeCatalog newCatalog = new ContentTypeCatalog(this, catalogGeneration++);
        // build catalog by parsing the extension registry
        ContentTypeBuilder builder = createBuilder(newCatalog);
        try {
            builder.buildCatalog(getContext());
            builder.applyProductPreferences();
            // only remember catalog if building it was successful
            catalog = newCatalog;
        } catch (InvalidRegistryObjectException e) {
            // the registry has stale objects... just don't remember the returned (incomplete) catalog
        }
        newCatalog.organize();
        return newCatalog;
    }

    @Override
    public IContentType getContentType(String contentTypeIdentifier) {
        ContentTypeCatalog currentCatalog = getCatalog();
        ContentType type = currentCatalog.getContentType(contentTypeIdentifier);
        return type == null ? null : new ContentTypeHandler(type, currentCatalog.getGeneration());
    }

    @Override
    public IContentTypeMatcher getMatcher(final ISelectionPolicy customPolicy, final IScopeContext context) {
        return new ContentTypeMatcher(customPolicy, context == null ? getContext() : context);
    }

    IEclipsePreferences getPreferences() {
        return getPreferences(getContext());
    }

    IEclipsePreferences getPreferences(IScopeContext context) {
        return context.getNode(CONTENT_TYPE_PREF_NODE);
    }

    /**
     * Causes a new catalog to be built afresh next time an API call is made.
     */
    synchronized void invalidate() {
        catalog = null;
    }

    @Override
    public void addContentTypeChangeListener(IContentTypeChangeListener listener) {
        contentTypeListeners.add(listener);
    }

    @Override
    public IContentDescription getSpecificDescription(BasicDescription description) {
        // this is the platform content type manager, no specificities
        return description;
    }

    static String[] getUserDefinedContentTypeIds(IScopeContext context) {
        String ids = context.getNode(ContentType.PREF_USER_DEFINED)
            .get(ContentType.PREF_USER_DEFINED, ContentType.EMPTY_STRING);
        if (ids.isEmpty()) {
            return new String[0];
        }
        return ids.split(ContentType.PREF_USER_DEFINED__SEPARATOR);
    }
}
