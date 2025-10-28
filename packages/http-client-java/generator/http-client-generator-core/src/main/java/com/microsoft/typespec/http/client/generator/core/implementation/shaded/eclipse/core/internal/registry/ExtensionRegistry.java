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
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IContributor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IExtension;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IExtensionDelta;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IExtensionPoint;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IExtensionRegistry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IRegistryChangeListener;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IRegistryEventListener;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.ISafeRunnable;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.ListenerList;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.MultiStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.SafeRunner;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Status;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.spi.IDynamicExtensionRegistry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.spi.RegistryContributor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.spi.RegistryStrategy;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.util.NLS;
import java.io.IOException;
import java.io.InputStream;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * An implementation for the extension registry API.
 */
public class ExtensionRegistry implements IExtensionRegistry, IDynamicExtensionRegistry {

    protected class ListenerInfo {
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
    private final transient Map<String, Object> deltas = new HashMap<>(11);

    // all registry change listeners
    private final transient ListenerList<ListenerInfo> listeners = new ListenerList<>();

    private RegistryObjectManager registryObjects;

    // Table reader associated with this extension registry
    protected TableReader theTableReader = new TableReader(this);

    private Object masterToken; // use to get full control of the registry; objects created as "static"
    private Object userToken; // use to modify non-persisted registry elements

    protected RegistryStrategy strategy; // overridable portions of the registry functionality

    private final RegistryTimestamp aggregatedTimestamp = new RegistryTimestamp(); // tracks current contents of the
                                                                                   // registry

    // encapsulates processing of new registry deltas
    private CombinedEventDelta eventDelta = null;
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

    /**
     * Adds and resolves all extensions and extension points provided by the
     * plug-in.
     * <p>
     * A corresponding IRegistryChangeEvent will be broadcast to all listeners
     * interested on changes in the given plug-in.
     * </p>
     */
    private void add(Contribution element) {
        access.enterWrite();
        try {
            eventDelta = CombinedEventDelta.recordAddition();
            basicAdd(element);
            fireRegistryChangeEvent();
            eventDelta = null;
        } finally {
            access.exitWrite();
        }
    }

    private String addExtension(int extension) {
        Extension addedExtension = (Extension) registryObjects.getObject(extension, RegistryObjectManager.EXTENSION);
        String extensionPointToAddTo = addedExtension.getExtensionPointIdentifier();
        ExtensionPoint extPoint = registryObjects.getExtensionPointObject(extensionPointToAddTo);
        // orphan extension
        if (extPoint == null) {
            registryObjects.addOrphan(extensionPointToAddTo, extension);
            return null;
        }
        // otherwise, link them
        int[] newExtensions;
        int[] existingExtensions = extPoint.getRawChildren();
        newExtensions = new int[existingExtensions.length + 1];
        System.arraycopy(existingExtensions, 0, newExtensions, 0, existingExtensions.length);
        newExtensions[newExtensions.length - 1] = extension;
        link(extPoint, newExtensions);
        if (eventDelta != null) {
            eventDelta.rememberExtension(extPoint, extension);
        }
        return recordChange(extPoint, extension, IExtensionDelta.ADDED);
    }

    /**
     * Looks for existing orphan extensions to connect to the given extension point.
     * If none is found, there is nothing to do. Otherwise, link them.
     */
    private String addExtensionPoint(int extPoint) {
        ExtensionPoint extensionPoint
            = (ExtensionPoint) registryObjects.getObject(extPoint, RegistryObjectManager.EXTENSION_POINT);
        if (eventDelta != null) {
            eventDelta.rememberExtensionPoint(extensionPoint);
        }
        int[] orphans = registryObjects.removeOrphans(extensionPoint.getUniqueIdentifier());
        if (orphans == null) {
            return null;
        }
        link(extensionPoint, orphans);
        if (eventDelta != null) {
            eventDelta.rememberExtensions(extensionPoint, orphans);
        }
        return recordChange(extensionPoint, orphans, IExtensionDelta.ADDED);
    }

    private Set<String> addExtensionsAndExtensionPoints(Contribution element) {
        // now add and resolve extensions and extension points
        Set<String> affectedNamespaces = new HashSet<>();
        for (int extPoint : element.getExtensionPoints()) {
            String namespace = this.addExtensionPoint(extPoint);
            if (namespace != null) {
                affectedNamespaces.add(namespace);
            }
        }
        for (int extension : element.getExtensions()) {
            String namespace = this.addExtension(extension);
            if (namespace != null) {
                affectedNamespaces.add(namespace);
            }
        }
        return affectedNamespaces;
    }

    @Override
    public void addListener(IRegistryEventListener listener) {
        addListenerInternal(listener, null);
    }

    @Override
    public void addListener(IRegistryEventListener listener, String extensionPointId) {
        addListenerInternal(listener, extensionPointId);
    }

    private void addListenerInternal(EventListener listener, String filter) {
        synchronized (listeners) {
            listeners.add(new ListenerInfo(listener, filter));
        }
    }

    @Override
    public void addRegistryChangeListener(IRegistryChangeListener listener) {
        // this is just a convenience API - no need to do any sync'ing here
        addListenerInternal(listener, null);
    }

    @Override
    public void addRegistryChangeListener(IRegistryChangeListener listener, String filter) {
        addListenerInternal(listener, filter);
    }

    private void basicAdd(Contribution element) {
        registryObjects.addContribution(element);
        Set<String> affectedNamespaces = addExtensionsAndExtensionPoints(element);
        setObjectManagers(affectedNamespaces, registryObjects
            .createDelegatingObjectManager(registryObjects.getAssociatedObjects(element.getContributorId())));
    }

    private void setObjectManagers(Set<String> affectedNamespaces, IObjectManager manager) {
        for (String namespace : affectedNamespaces) {
            getDelta(namespace).setObjectManager(manager);
        }
        if (eventDelta != null) {
            eventDelta.setObjectManager(manager);
        }
    }

    private void basicRemove(String contributorId) {
        // ignore anonymous namespaces
        Set<String> affectedNamespaces = removeExtensionsAndExtensionPoints(contributorId);
        Map<Integer, RegistryObject> associatedObjects = registryObjects.getAssociatedObjects(contributorId);
        registryObjects.removeObjects(associatedObjects);
        registryObjects.addNavigableObjects(associatedObjects); // put the complete set of navigable objects
        setObjectManagers(affectedNamespaces, registryObjects.createDelegatingObjectManager(associatedObjects));

        registryObjects.removeContribution(contributorId);
        registryObjects.removeContributor(contributorId);
    }

    /**
     * Broadcasts (asynchronously) the event to all interested parties.
     */
    private void fireRegistryChangeEvent() {
        // pack new extended delta together with the rest of deltas using invalid
        // namespace
        deltas.put(notNamespace, eventDelta);
        // if there is nothing to say, just bail out
        if (listeners.isEmpty()) {
            deltas.clear();
            return;
        }
        // for thread safety, create tmp collections
        Object[] tmpListeners = listeners.getListeners();
        Map<String, Object> tmpDeltas = new HashMap<>(this.deltas);
        // the deltas have been saved for notification - we can clear them now
        deltas.clear();
        // do the notification asynchronously
        strategy.scheduleChangeEvent(tmpListeners, tmpDeltas, this);
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
     * @see
     * org.eclipse.core.runtime.IExtensionRegistry#getConfigurationElementsFor(java.
     * lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public IConfigurationElement[] getConfigurationElementsFor(String pluginId, String extensionPointName,
        String extensionId) {
        // this is just a convenience API - no need to do any sync'ing here
        IExtension extension = this.getExtension(pluginId, extensionPointName, extensionId);
        if (extension == null) {
            return new IConfigurationElement[0];
        }
        return extension.getConfigurationElements();
    }

    private RegistryDelta getDelta(String namespace) {
        // is there a delta for the plug-in?
        RegistryDelta existingDelta = (RegistryDelta) deltas.get(namespace);
        if (existingDelta != null) {
            return existingDelta;
        }

        // if not, create one
        RegistryDelta delta = new RegistryDelta();
        deltas.put(namespace, delta);
        return delta;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.core.runtime.IExtensionRegistry#getExtension(java.lang.String)
     */
    @Override
    public IExtension getExtension(String extensionId) {
        if (extensionId == null) {
            return null;
        }
        int lastdot = extensionId.lastIndexOf('.');
        if (lastdot == -1) {
            return null;
        }
        String namespace = extensionId.substring(0, lastdot);

        ExtensionHandle[] extensions;
        access.enterRead();
        try {
            extensions = registryObjects.getExtensionsFromNamespace(namespace);
        } finally {
            access.exitRead();
        }
        for (ExtensionHandle suspect : extensions) {
            if (extensionId.equals(suspect.getUniqueIdentifier())) {
                return suspect;
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.core.runtime.IExtensionRegistry#getExtension(java.lang.String,
     * java.lang.String)
     */
    @Override
    public IExtension getExtension(String extensionPointId, String extensionId) {
        // this is just a convenience API - no need to do any sync'ing here
        int lastdot = extensionPointId.lastIndexOf('.');
        if (lastdot == -1) {
            return null;
        }
        return getExtension(extensionPointId.substring(0, lastdot), extensionPointId.substring(lastdot + 1),
            extensionId);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.core.runtime.IExtensionRegistry#getExtension(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public IExtension getExtension(String pluginId, String extensionPointName, String extensionId) {
        // this is just a convenience API - no need to do any sync'ing here
        IExtensionPoint extPoint = getExtensionPoint(pluginId, extensionPointName);
        if (extPoint != null) {
            return extPoint.getExtension(extensionId);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.core.runtime.IExtensionRegistry#getExtensionPoint(java.lang.
     * String)
     */
    @Override
    public IExtensionPoint getExtensionPoint(String xptUniqueId) {
        access.enterRead();
        try {
            return registryObjects.getExtensionPointHandle(xptUniqueId);
        } finally {
            access.exitRead();
        }
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
     * @see org.eclipse.core.runtime.IExtensionRegistry#getExtensionPoints()
     */
    @Override
    public IExtensionPoint[] getExtensionPoints() {
        access.enterRead();
        try {
            return registryObjects.getExtensionPointsHandles();
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

    @Override
    public IExtension[] getExtensions(IContributor contributor) {
        if (!(contributor instanceof RegistryContributor)) {
            throw new IllegalArgumentException(); // should never happen
        }
        String contributorId = ((RegistryContributor) contributor).getActualId();
        access.enterRead();
        try {
            return registryObjects.getExtensionsFromContributor(contributorId);
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
                namespaceNames[i] = (String) ((RegistryIndexElement) namespaceElements[i]).getKey();
            }
            return namespaceNames;
        } finally {
            access.exitRead();
        }
    }

    @Override
    public boolean hasContributor(IContributor contributor) {
        if (!(contributor instanceof RegistryContributor)) {
            throw new IllegalArgumentException(); // should never happen
        }
        String contributorId = ((RegistryContributor) contributor).getActualId();
        return hasContributor(contributorId);
    }

    public boolean hasContributor(String contributorId) {
        access.enterRead();
        try {
            return registryObjects.hasContribution(contributorId);
        } finally {
            access.exitRead();
        }
    }

    private void link(ExtensionPoint extPoint, int[] extensions) {
        extPoint.setRawChildren(extensions);
        registryObjects.add(extPoint, true);
    }

    /*
     * Records an extension addition/removal.
     */
    private String recordChange(ExtensionPoint extPoint, int extension, int kind) {
        // avoid computing deltas when there are no listeners
        if (listeners.isEmpty()) {
            return null;
        }
        ExtensionDelta extensionDelta = new ExtensionDelta();
        extensionDelta.setExtension(extension);
        extensionDelta.setExtensionPoint(extPoint.getObjectId());
        extensionDelta.setKind(kind);
        getDelta(extPoint.getNamespace()).addExtensionDelta(extensionDelta);
        return extPoint.getNamespace();
    }

    /*
     * Records a set of extension additions/removals.
     */
    private String recordChange(ExtensionPoint extPoint, int[] extensions, int kind) {
        if (listeners.isEmpty()) {
            return null;
        }
        String namespace = extPoint.getNamespace();
        if (extensions == null || extensions.length == 0) {
            return namespace;
        }
        RegistryDelta pluginDelta = getDelta(extPoint.getNamespace());
        for (int extension : extensions) {
            ExtensionDelta extensionDelta = new ExtensionDelta();
            extensionDelta.setExtension(extension);
            extensionDelta.setExtensionPoint(extPoint.getObjectId());
            extensionDelta.setKind(kind);
            pluginDelta.addExtensionDelta(extensionDelta);
        }
        return namespace;
    }

    public void remove(String removedContributorId, long timestamp) {
        remove(removedContributorId);
        if (timestamp != 0) {
            aggregatedTimestamp.remove(timestamp);
        }
    }

    @Override
    public void removeContributor(IContributor contributor, Object key) {
        if (!(contributor instanceof RegistryContributor)) {
            throw new IllegalArgumentException(); // should never happen
        }
        if (!checkReadWriteAccess(key, true)) {
            throw new IllegalArgumentException(
                "Unauthorized access to the ExtensionRegistry.removeContributor() method. Check if proper access token is supplied."); //$NON-NLS-1$
        }
        String contributorId = ((RegistryContributor) contributor).getActualId();
        remove(contributorId);
    }

    /**
     * Unresolves and removes all extensions and extension points provided by the
     * plug-in.
     * <p>
     * A corresponding IRegistryChangeEvent will be broadcast to all listeners
     * interested on changes in the given plug-in.
     * </p>
     */
    public void remove(String removedContributorId) {
        access.enterWrite();
        try {
            eventDelta = CombinedEventDelta.recordRemoval();
            basicRemove(removedContributorId);
            fireRegistryChangeEvent();
            eventDelta = null;
        } finally {
            access.exitWrite();
        }
    }

    // Return the affected namespace
    private String removeExtension(int extensionId) {
        Extension extension = (Extension) registryObjects.getObject(extensionId, RegistryObjectManager.EXTENSION);
        registryObjects.removeExtensionFromNamespaceIndex(extensionId, extension.getNamespaceIdentifier());
        String xptName = extension.getExtensionPointIdentifier();
        ExtensionPoint extPoint = registryObjects.getExtensionPointObject(xptName);
        if (extPoint == null) {
            registryObjects.removeOrphan(xptName, extensionId);
            return null;
        }
        // otherwise, unlink the extension from the extension point
        int[] existingExtensions = extPoint.getRawChildren();
        int[] newExtensions = RegistryObjectManager.EMPTY_INT_ARRAY;
        if (existingExtensions.length > 1) {
            newExtensions = new int[existingExtensions.length - 1];
            for (int i = 0, j = 0; i < existingExtensions.length; i++) {
                if (existingExtensions[i] != extension.getObjectId()) {
                    newExtensions[j++] = existingExtensions[i];
                }
            }
        }
        link(extPoint, newExtensions);
        if (eventDelta != null) {
            eventDelta.rememberExtension(extPoint, extensionId);
        }
        return recordChange(extPoint, extension.getObjectId(), IExtensionDelta.REMOVED);
    }

    private String removeExtensionPoint(int extPoint) {
        ExtensionPoint extensionPoint
            = (ExtensionPoint) registryObjects.getObject(extPoint, RegistryObjectManager.EXTENSION_POINT);
        registryObjects.removeExtensionPointFromNamespaceIndex(extPoint, extensionPoint.getNamespace());
        int[] existingExtensions = extensionPoint.getRawChildren();
        if (existingExtensions != null && existingExtensions.length != 0) {
            registryObjects.addOrphans(extensionPoint.getUniqueIdentifier(), existingExtensions);
            link(extensionPoint, RegistryObjectManager.EMPTY_INT_ARRAY);
        }
        if (eventDelta != null) {
            eventDelta.rememberExtensionPoint(extensionPoint);
            eventDelta.rememberExtensions(extensionPoint, existingExtensions);
        }
        return recordChange(extensionPoint, existingExtensions, IExtensionDelta.REMOVED);
    }

    private Set<String> removeExtensionsAndExtensionPoints(String contributorId) {
        Set<String> affectedNamespaces = new HashSet<>();
        for (int extension : registryObjects.getExtensionsFrom(contributorId)) {
            String namespace = this.removeExtension(extension);
            if (namespace != null) {
                affectedNamespaces.add(namespace);
            }
        }

        // remove extension points
        for (int extPoint : registryObjects.getExtensionPointsFrom(contributorId)) {
            String namespace = this.removeExtensionPoint(extPoint);
            if (namespace != null) {
                affectedNamespaces.add(namespace);
            }
        }
        return affectedNamespaces;
    }

    @Override
    public void removeRegistryChangeListener(IRegistryChangeListener listener) {
        synchronized (listeners) {
            listeners.remove(new ListenerInfo(listener, null));
        }
    }

    @Override
    public void removeListener(IRegistryEventListener listener) {
        synchronized (listeners) {
            listeners.remove(new ListenerInfo(listener, null));
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    // Registry Object Factory
    // The factory produces contributions, extension points, extensions, and
    ///////////////////////////////////////////////////////////////////////////////////////////////// configuration
    ///////////////////////////////////////////////////////////////////////////////////////////////// elements
    // to be stored in the extension registry.
    protected RegistryObjectFactory theRegistryObjectFactory = null;

    // Override to provide domain-specific elements to be stored in the extension
    // registry
    protected void setElementFactory() {
        theRegistryObjectFactory = new RegistryObjectFactory(this);
    }

    // Lazy initialization.
    public RegistryObjectFactory getElementFactory() {
        if (theRegistryObjectFactory == null) {
            setElementFactory();
        }
        return theRegistryObjectFactory;
    }

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

    //////////////////////////////////////////////////////////////////////////////////////////
    // Registry change events processing

    public IStatus processChangeEvent(Object[] listenerInfos, final Map<String, ?> scheduledDeltas) {
        // Separate new event delta from the pack
        final CombinedEventDelta extendedDelta = (CombinedEventDelta) scheduledDeltas.remove(notNamespace);

        final MultiStatus result = new MultiStatus(RegistryMessages.OWNER_NAME, IStatus.OK,
            RegistryMessages.plugin_eventListenerError, null);
        for (Object info : listenerInfos) {
            final ListenerInfo listenerInfo = (ListenerInfo) info;
            if ((listenerInfo.listener instanceof IRegistryChangeListener) && scheduledDeltas.size() != 0) {
                if (listenerInfo.filter == null || scheduledDeltas.containsKey(listenerInfo.filter)) {
                    SafeRunner.run(new ISafeRunnable() {
                        @Override
                        public void run() throws Exception {
                            ((IRegistryChangeListener) listenerInfo.listener)
                                .registryChanged(new RegistryChangeEvent(scheduledDeltas, listenerInfo.filter));
                        }

                        @Override
                        public void handleException(Throwable exception) {
                            result.add(new Status(IStatus.ERROR, RegistryMessages.OWNER_NAME,
                                RegistryMessages.plugin_eventListenerError, exception));
                        }
                    });
                }
            }
            if (listenerInfo.listener instanceof IRegistryEventListener extensionListener) {
                IExtension[] extensions = extendedDelta.getExtensions(listenerInfo.filter);
                IExtensionPoint[] extensionPoints = extendedDelta.getExtensionPoints(listenerInfo.filter);

                // notification order - on addition: extension points; then extensions
                if (extendedDelta.isAddition()) {
                    if (extensionPoints != null) {
                        extensionListener.added(extensionPoints);
                    }
                    if (extensions != null) {
                        extensionListener.added(extensions);
                    }
                } else { // on removal: extensions; then extension points
                    if (extensions != null) {
                        extensionListener.removed(extensions);
                    }
                    if (extensionPoints != null) {
                        extensionListener.removed(extensionPoints);
                    }
                }
            }
        }
        for (Object delta : scheduledDeltas.values()) {
            ((RegistryDelta) delta).getObjectManager().close();
        }
        IObjectManager manager = extendedDelta.getObjectManager();
        if (manager != null) {
            manager.close();
        }
        return result;
    }

    private RegistryEventThread eventThread = null; // registry event loop
    protected final List<QueueElement> queue = new LinkedList<>(); // stores registry events info

    // Registry events notifications are done on a separate thread in a sequential
    // manner
    // (first in - first processed)
    public void scheduleChangeEvent(Object[] listenerInfos, Map<String, ?> scheduledDeltas) {
        QueueElement newElement = new QueueElement(listenerInfos, scheduledDeltas);
        if (eventThread == null) {
            eventThread = new RegistryEventThread(this);
            eventThread.start();
        }
        synchronized (queue) {
            queue.add(newElement);
            queue.notify();
        }
    }

    // The pair of values we store in the event queue
    private static class QueueElement {
        Object[] listenerInfos;
        Map<String, ?> scheduledDeltas;

        QueueElement(Object[] infos, Map<String, ?> deltas) {
            this.scheduledDeltas = deltas;
            listenerInfos = infos;
        }
    }

    private class RegistryEventThread extends Thread {
        private final ExtensionRegistry registry;

        public RegistryEventThread(ExtensionRegistry registry) {
            super("Extension Registry Event Dispatcher"); //$NON-NLS-1$
            setDaemon(true);
            this.registry = registry;
        }

        @Override
        public void run() {
            while (true) {
                QueueElement element;
                synchronized (queue) {
                    try {
                        while (queue.isEmpty()) {
                            queue.wait();
                        }
                    } catch (InterruptedException e) {
                        return;
                    }
                    element = queue.remove(0);
                }
                registry.processChangeEvent(element.listenerInfos, element.scheduledDeltas);
            }
        }
    }

    /**
     * Access check for add/remove operations: - Master key allows all operations -
     * User key allows modifications of non-persisted elements
     *
     * @param key key to the registry supplied by the user
     * @param persist true if operation affects persisted elements
     * @return true is the key grants read/write access to the registry
     */
    private boolean checkReadWriteAccess(Object key, boolean persist) {
        if (masterToken == key) {
            return true;
        }
        if (userToken == key && !persist) {
            return true;
        }
        return false;
    }

    public boolean addContribution(InputStream is, IContributor contributor, boolean persist, String contributionName,
        ResourceBundle translationBundle, Object key, long timestamp) {
        boolean result = addContribution(is, contributor, persist, contributionName, translationBundle, key);
        if (timestamp != 0) {
            aggregatedTimestamp.add(timestamp);
        }
        return result;
    }

    @Override
    public boolean addContribution(InputStream is, IContributor contributor, boolean persist, String contributionName,
        ResourceBundle translationBundle, Object key) {
        if (!checkReadWriteAccess(key, persist)) {
            throw new IllegalArgumentException(
                "Unauthorized access to the ExtensionRegistry.addContribution() method. Check if proper access token is supplied."); //$NON-NLS-1$
        }
        if (contributionName == null) {
            contributionName = ""; //$NON-NLS-1$
        }

        RegistryContributor internalContributor = (RegistryContributor) contributor;
        registryObjects.addContributor(internalContributor); // only adds a contributor if it is not already present

        String ownerName = internalContributor.getActualName();
        String message = NLS.bind(RegistryMessages.parse_problems, ownerName);
        MultiStatus problems
            = new MultiStatus(RegistryMessages.OWNER_NAME, ExtensionsParser.PARSE_PROBLEM, message, null);
        ExtensionsParser parser = new ExtensionsParser(problems, this);
        Contribution contribution = getElementFactory().createContribution(internalContributor.getActualId(), persist);

        try {
            parser.parseManifest(strategy.getXMLParser(), new InputSource(is), contributionName, getObjectManager(),
                contribution, translationBundle);
            int status = problems.getSeverity();
            if (status != IStatus.OK) {
                log(problems);
                if (status == IStatus.ERROR || status == IStatus.CANCEL) {
                    return false;
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            logError(ownerName, contributionName, e);
            return false;
        } finally {
            try {
                is.close();
            } catch (IOException ioe) {
                // nothing to do
            }
        }
        add(contribution); // the add() method does synchronization
        return true;
    }

    private void logError(String owner, String contributionName, Exception e) {
        String message = NLS.bind(RegistryMessages.parse_failedParsingManifest, owner + "/" + contributionName); //$NON-NLS-1$
        log(new Status(IStatus.ERROR, RegistryMessages.OWNER_NAME, 0, message, e));
    }

    @Override
    public IContributor[] getAllContributors() {
        access.enterRead();
        try {
            return registryObjects.getContributorsSync();
        } finally {
            access.exitRead();
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
