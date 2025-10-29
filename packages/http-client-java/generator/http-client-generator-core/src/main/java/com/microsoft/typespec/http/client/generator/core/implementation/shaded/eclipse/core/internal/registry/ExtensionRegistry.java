/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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
 *     Danail Nachev - exception handling for registry listeners (bug 188369)
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.registry;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IConfigurationElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IExtension;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IExtensionPoint;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IExtensionRegistry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.ListenerList;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Status;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.spi.RegistryStrategy;

import java.util.EventListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * An implementation for the extension registry API.
 */
public class ExtensionRegistry implements IExtensionRegistry {

    protected static class ListenerInfo {
        public String filter;
        public EventListener listener;

        public ListenerInfo(EventListener listener, String filter) {
            this.listener = listener;
            this.filter = filter;
        }

        /**
         * Used by ListenerList to ensure uniqueness.
         */
        @Override
        public boolean equals(Object another) {
            return another instanceof ListenerInfo && ((ListenerInfo) another).listener == this.listener;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return listener == null ? 0 : listener.hashCode();
        }
    }

    // used to enforce concurrent access policy for readers/writers
    private final ReadWriteMonitor access = new ReadWriteMonitor();

    // deltas not broadcasted yet. Deltas are kept organized by the namespace name
    // (objects with the same namespace are grouped together)

    // all registry change listeners
    private final transient ListenerList<ListenerInfo> listeners = new ListenerList<>();

    private RegistryObjectManager registryObjects;

    // Table reader associated with this extension registry
    protected TableReader theTableReader = new TableReader(this);

    protected RegistryStrategy strategy; // overridable portions of the registry functionality

    // tracks current contents of the
    // registry

    // marks a new extended delta. The namespace that normally would not exists is
    // used for this purpose
    private final static String notNamespace = ""; //$NON-NLS-1$

    // does this instance of the extension registry has multiple language support
    // enabled?

    // have we already logged a error on usage of an unsupported multi-language
    // method?
    private boolean mlErrorLogged = false;

    public RegistryObjectManager getObjectManager() {
        return registryObjects;
    }

    private void addListenerInternal(EventListener listener, String filter) {
        synchronized (listeners) {
            listeners.add(new ListenerInfo(listener, filter));
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.core.runtime.IExtensionRegistry#getConfigurationElementsFor(java.
     * lang.String, java.lang.String)
     */
    @Override
    public IConfigurationElement[] getConfigurationElementsFor(String pluginId, String extensionPointSimpleId) {
        // this is just a convenience API - no need to do any sync'ing here
        IExtensionPoint extPoint = this.getExtensionPoint(pluginId, extensionPointSimpleId);
        if (extPoint == null) {
            return new IConfigurationElement[0];
        }
        return extPoint.getConfigurationElements();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.core.runtime.IExtensionRegistry#getExtensionPoint(java.lang.
     * String, java.lang.String)
     */
    @Override
    public IExtensionPoint getExtensionPoint(String elementName, String xpt) {
        access.enterRead();
        try {
            return registryObjects.getExtensionPointHandle(elementName + '.' + xpt);
        } finally {
            access.exitRead();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.core.runtime.IExtensionRegistry#getExtensions(java.lang.String)
     */
    @Override
    public IExtension[] getExtensions(String namespaceName) {
        access.enterRead();
        try {
            return registryObjects.getExtensionsFromNamespace(namespaceName);
        } finally {
            access.exitRead();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.core.runtime.IExtensionRegistry#getNamespaces()
     */
    @Override
    public String[] getNamespaces() {
        access.enterRead();
        try {
            KeyedElement[] namespaceElements = registryObjects.getNamespacesIndex().elements();
            String[] namespaceNames = new String[namespaceElements.length];
            for (int i = 0; i < namespaceElements.length; i++) {
                namespaceNames[i] = (String) namespaceElements[i].getKey();
            }
            return namespaceNames;
        } finally {
            access.exitRead();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    // Registry Object Factory
    // The factory produces contributions, extension points, extensions, and
    ///////////////////////////////////////////////////////////////////////////////////////////////// configuration
    ///////////////////////////////////////////////////////////////////////////////////////////////// elements
    // to be stored in the extension registry.
    protected RegistryObjectFactory theRegistryObjectFactory = null;

    TableReader getTableReader() {
        return theTableReader;
    }

    public void log(IStatus status) {
        strategy.log(status);
    }

    /**
     * With multi-locale support enabled this method returns the non-translated key
     * so that they can be cached and translated later into desired languages. In
     * the absence of the multi-locale support the key gets translated immediately
     * and only translated values is cached.
     */
    public String translate(String key, ResourceBundle resources) {
        return strategy.translate(key, resources);
    }

    public boolean debug() {
        return strategy.debug();
    }

    protected final List<QueueElement> queue = new LinkedList<>(); // stores registry events info

    // The pair of values we store in the event queue
    private static class QueueElement {
        Object[] listenerInfos;
        Map<String, ?> scheduledDeltas;

        QueueElement(Object[] infos, Map<String, ?> deltas) {
            this.scheduledDeltas = deltas;
            listenerInfos = infos;
        }
    }

    public String getLocale() {
        return strategy.getLocale();
    }

    public void logMultiLangError() {
        if (mlErrorLogged) { // only log this error ones
            return;
        }
        log(new Status(IStatus.ERROR, RegistryMessages.OWNER_NAME, 0, RegistryMessages.registry_non_multi_lang,
            new IllegalArgumentException()));
        mlErrorLogged = true;
    }
}
