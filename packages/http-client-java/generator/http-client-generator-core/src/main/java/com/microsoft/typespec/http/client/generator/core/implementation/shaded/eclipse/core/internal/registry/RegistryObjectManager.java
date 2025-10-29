/*******************************************************************************
 * Copyright (c) 2004, 2018 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.registry;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.InvalidRegistryObjectException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.spi.RegistryContributor;

import java.util.*;

/**
 * This class manage all the object from the registry but does not deal with
 * their dependencies. It serves the objects which are either directly obtained
 * from memory or read from a cache. It also returns handles for objects.
 */
public class RegistryObjectManager implements IObjectManager {
    // Constants used to get the objects and their handles
    static public final byte CONFIGURATION_ELEMENT = 1;
    static public final byte EXTENSION = 2;
    static public final byte EXTENSION_POINT = 3;
    static public final byte THIRDLEVEL_CONFIGURATION_ELEMENT = 4;

    static final int CACHE_INITIAL_SIZE = 512; // This value has been picked because it is the minimal size required to
                                               // startup an RCP app. (FYI, eclipse requires 3 growths).
    static final float DEFAULT_LOADFACTOR = 0.75f; // This is the default factor used in reference map.

    static final int[] EMPTY_INT_ARRAY = new int[0];
    static final String[] EMPTY_STRING_ARRAY = new String[0];

    static final ExtensionHandle[] EMPTY_EXTENSIONS_ARRAY = new ExtensionHandle[0];

    static int UNKNOWN = -1;

    // key: extensionPointName, value: object id
    private final HashtableOfStringAndInt extensionPoints; // This is loaded on startup. Then entries can be added when
                                                           // loading a new plugin from the xml.
    // key: object id, value: an object
    private final ReferenceMap cache; // Entries are added by getter. The structure is not thread safe.
    // key: int, value: int
    // This is read once on startup when loading from the cache. Entries
    // are
    // never added here. They are only removed to prevent "removed" objects to
    // be reloaded.

    private int nextId = 1; // This is only used to get the next number available.

    // Those two data structures are only used when the addition or the removal of a
    // plugin occurs.
    // They are used to keep track on a contributor basis of the extension being
    // added or removed
    // represents the contributers added during this session.
    // lazily.

    private HashMap<String, RegistryContributor> contributors; // key: contributor ID; value: contributor name
    private HashMap<String, RegistryContributor> removedContributors; // key: contributor ID; value: contributor name
    private KeyedHashSet namespacesIndex; // registry elements (extension & extensionpoints) indexed by namespaces

    // Map key: extensionPointFullyQualifiedName, value int[] of orphan extensions.
    // The orphan access does not need to be synchronized because the it is
    // protected by the lock in extension registry.

    private final KeyedHashSet heldObjects = new KeyedHashSet(); // strong reference to the objects that must be hold on
                                                                 // to

    // Indicate if objects have been removed or added from the table. This only
    // needs to be set in a couple of places (addNamespace and removeNamespace)
    private boolean isDirty = false;

    private final boolean fromCache = false;

    private final ExtensionRegistry registry;

    // TODO this option is not used
    // OSGI system properties. Copied from EclipseStarter
    public static final String PROP_NO_REGISTRY_FLUSHING = "eclipse.noRegistryFlushing"; //$NON-NLS-1$

    public RegistryObjectManager(ExtensionRegistry registry) {
        extensionPoints = new HashtableOfStringAndInt();
        if ("true".equalsIgnoreCase(RegistryProperties.getProperty(PROP_NO_REGISTRY_FLUSHING))) { //$NON-NLS-1$
            cache = new ReferenceMap(ReferenceMap.HARD, CACHE_INITIAL_SIZE, DEFAULT_LOADFACTOR);
        } else {
            cache = new ReferenceMap(ReferenceMap.SOFT, CACHE_INITIAL_SIZE, DEFAULT_LOADFACTOR);
        }

        this.registry = registry;
    }

    synchronized public void add(RegistryObject registryObject, boolean hold) {
        if (registryObject.getObjectId() == UNKNOWN) {
            int id = nextId++;
            registryObject.setObjectId(id);
        }
        cache.put(registryObject.getObjectId(), registryObject);
        if (hold) {
            hold(registryObject);
        }
    }

    private void hold(RegistryObject toHold) {
        heldObjects.add(toHold);
    }

    @Override
    public synchronized Object getObject(int id, byte type) {
        return basicGetObject(id, type);
    }

    private Object basicGetObject(int id, byte type) {
        Object result = cache.get(id);
        if (result != null) {
            return result;
        }
        if (fromCache) {
            load(id, type);
        }
        throw new InvalidRegistryObjectException();
    }

    // The current impementation of this method assumes that we don't cache dynamic
    // extension. In this case all extensions not yet loaded (i.e. not in the memory
    // cache)
    // are "not dynamic" and we actually check memory objects to see if they are
    // dynamic.
    //
    // If we decide to allow caching of dynamic objects, the implementation
    // of this method would have to retrieved the object from disk and check
    // its "dynamic" status. The problem is that id alone is not enough to get the
    // object
    // from the disk; object type is needed as well.
    public boolean shouldPersist(int id) {
        Object result = cache.get(id);
        if (result != null) {
            return ((RegistryObject) result).shouldPersist();
        }
        return true;
    }

    @Override
    public synchronized RegistryObject[] getObjects(int[] values, byte type) {
        if (values.length == 0) {
            switch (type) {
                case EXTENSION_POINT:
                    return ExtensionPoint.EMPTY_ARRAY;

                case EXTENSION:
                    return Extension.EMPTY_ARRAY;

                case CONFIGURATION_ELEMENT:
                case THIRDLEVEL_CONFIGURATION_ELEMENT:
                    return ConfigurationElement.EMPTY_ARRAY;
            }
        }

        RegistryObject[] results = switch (type) {
            case EXTENSION_POINT -> new ExtensionPoint[values.length];
            case EXTENSION -> new Extension[values.length];
            case CONFIGURATION_ELEMENT, THIRDLEVEL_CONFIGURATION_ELEMENT -> new ConfigurationElement[values.length];
            default -> null;
        };
        for (int i = 0; i < values.length; i++) {
            results[i] = (RegistryObject) basicGetObject(values[i], type);
        }
        return results;
    }

    synchronized ExtensionPoint getExtensionPointObject(String xptUniqueId) {
        int id;
        if ((id = extensionPoints.get(xptUniqueId)) == HashtableOfStringAndInt.MISSING_ELEMENT) {
            return null;
        }
        return (ExtensionPoint) getObject(id, EXTENSION_POINT);
    }

    @Override
    public Handle getHandle(int id, byte type) {
        return switch (type) {
            case EXTENSION_POINT -> new ExtensionPointHandle(this, id);
            case EXTENSION -> new ExtensionHandle(this, id);
            case CONFIGURATION_ELEMENT -> new ConfigurationElementHandle(this, id);
            default -> // avoid compiler error, type should always be known
                new ThirdLevelConfigurationElementHandle(this, id);
        };
    }

    @Override
    public Handle[] getHandles(int[] ids, byte type) {
        Handle[] results = null;
        int nbrId = ids.length;
        switch (type) {
            case EXTENSION_POINT:
                if (nbrId == 0) {
                    return ExtensionPointHandle.EMPTY_ARRAY;
                }
                results = new ExtensionPointHandle[nbrId];
                for (int i = 0; i < nbrId; i++) {
                    results[i] = new ExtensionPointHandle(this, ids[i]);
                }
                break;

            case EXTENSION:
                if (nbrId == 0) {
                    return ExtensionHandle.EMPTY_ARRAY;
                }
                results = new ExtensionHandle[nbrId];
                for (int i = 0; i < nbrId; i++) {
                    results[i] = new ExtensionHandle(this, ids[i]);
                }
                break;

            case CONFIGURATION_ELEMENT:
                if (nbrId == 0) {
                    return ConfigurationElementHandle.EMPTY_ARRAY;
                }
                results = new ConfigurationElementHandle[nbrId];
                for (int i = 0; i < nbrId; i++) {
                    results[i] = new ConfigurationElementHandle(this, ids[i]);
                }
                break;

            case THIRDLEVEL_CONFIGURATION_ELEMENT:
                if (nbrId == 0) {
                    return ConfigurationElementHandle.EMPTY_ARRAY;
                }
                results = new ThirdLevelConfigurationElementHandle[nbrId];
                for (int i = 0; i < nbrId; i++) {
                    results[i] = new ThirdLevelConfigurationElementHandle(this, ids[i]);
                }
                break;
        }
        return results;
    }

    synchronized ExtensionPointHandle getExtensionPointHandle(String xptUniqueId) {
        int id = extensionPoints.get(xptUniqueId);
        if (id == HashtableOfStringAndInt.MISSING_ELEMENT) {
            return null;
        }
        return (ExtensionPointHandle) getHandle(id, EXTENSION_POINT);
    }

    private Object load(int id, byte type) {
        return null;
    }

    public boolean isDirty() {
        return isDirty;
    }

    // This method is used internally and by the writer to reach in. Notice that it
    // doesn't
    // return contributors marked as removed.
    HashMap<String, RegistryContributor> getContributors() {
        if (contributors == null) {
            if (!fromCache) {
                contributors = new HashMap<>();
            } else {
                contributors = registry.getTableReader().loadContributors();
            }
        }
        return contributors;
    }

    synchronized RegistryContributor getContributor(String id) {
        RegistryContributor contributor = getContributors().get(id);
        if (contributor != null) {
            return contributor;
        }
        // check if we have it among removed contributors - potentially
        // notification of removals might be processed after the contributor
        // marked as removed:
        if (removedContributors != null) {
            return removedContributors.get(id);
        }
        return null;
    }

    KeyedHashSet getNamespacesIndex() {
        if (namespacesIndex == null) {
            if (!fromCache) {
                namespacesIndex = new KeyedHashSet(0);
            } else {
                namespacesIndex = registry.getTableReader().loadNamespaces();
            }
        }
        return namespacesIndex;
    }

    // Find or create required index element
    private RegistryIndexElement getNamespaceIndex(String namespaceName) {
        RegistryIndexElement indexElement = (RegistryIndexElement) getNamespacesIndex().getByKey(namespaceName);
        if (indexElement == null) {
            indexElement = new RegistryIndexElement(namespaceName);
            namespacesIndex.add(indexElement);
        }
        return indexElement;
    }

    @Override
    public void close() {
        // do nothing.
    }

    public ExtensionRegistry getRegistry() {
        return registry;
    }

    // This method filters out extensions with no extension point
    synchronized public ExtensionHandle[] getExtensionsFromNamespace(String namespaceName) {
        RegistryIndexElement indexElement = getNamespaceIndex(namespaceName);
        int[] namespaceExtensions = indexElement.getExtensions();

        // filter extensions with no extension point (orphan extensions)
        List<Handle> tmp = new ArrayList<>();
        Extension[] exts = (Extension[]) getObjects(namespaceExtensions, EXTENSION);
        for (Extension ext : exts) {
            if (getExtensionPointObject(ext.getExtensionPointIdentifier()) != null) {
                tmp.add(getHandle(ext.getObjectId(), EXTENSION));
            }
        }
        if (tmp.size() == 0) {
            return EMPTY_EXTENSIONS_ARRAY;
        }
        ExtensionHandle[] result = new ExtensionHandle[tmp.size()];
        return tmp.toArray(result);
    }

}
