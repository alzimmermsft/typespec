/*******************************************************************************
 * Copyright (c) 2004, 2023 IBM Corporation and others.
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
 *     Julian Chen - fix for bug #92572, jclRM
 *     Jan-Ove Weichel (janove.weichel@vogella.com) - bug 474359
 *     InterSystems Corporation - bug 444188
 *     Hannes Wellmann - Leverage Java-NIO to write preferences
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.preferences;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.runtime.RuntimeLog;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.*;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.preferences.*;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.util.NLS;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.prefs.BackingStoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.prefs.Preferences;
import java.io.*;
import java.util.*;

/**
 * Represents a node in the Eclipse preference node hierarchy. This class is
 * used as a default implementation/super class for those nodes which belong to
 * scopes which are contributed by the Platform.
 *
 * Implementation notes:
 *
 * - For thread safety, we always synchronize on <code>writeLock</code> when writing
 * the children or properties fields. Must ensure we don't synchronize when
 * calling client code such as listeners.
 *
 * @since 3.0
 */
public class EclipsePreferences implements IEclipsePreferences, IScope {

    public static final String DEFAULT_PREFERENCES_DIRNAME = ".settings"; //$NON-NLS-1$
    protected static final String[] EMPTY_STRING_ARRAY = new String[0];
    //$NON-NLS-1$
    private static final String TRUE = "true"; //$NON-NLS-1$
    protected static final String VERSION_KEY = "eclipse.preferences.version"; //$NON-NLS-1$
    protected static final String PATH_SEPARATOR = String.valueOf(IPath.SEPARATOR);
    protected static final String DOUBLE_SLASH = "//"; //$NON-NLS-1$
    protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
    private static final String BACKUP_FILE_EXTENSION = ".bak"; //$NON-NLS-1$

    /** not synchronized, but each thread would create the same result **/
    private String cachedPath;
    /** synchronized by childAndPropertyLock */
    private ImmutableMap properties = ImmutableMap.EMPTY;
    /** synchronized by childAndPropertyLock */
    private Map<String, Object> children;
    /**
     * Protects write access to properties and children.
     */
    private final Object childAndPropertyLock = new Object();
    protected volatile boolean dirty;
    protected volatile boolean loading;
    protected final String name;
    // the parent of an EclipsePreference node is always an EclipsePreference node.
    // (or null)
    protected final EclipsePreferences parent;
    protected volatile boolean removed;
    private final ListenerList<INodeChangeListener> nodeChangeListeners = new ListenerList<>();
    private final ListenerList<IPreferenceChangeListener> preferenceChangeListeners = new ListenerList<>();
    private final ScopeDescriptor descriptor;

    EclipsePreferences(EclipsePreferences parent, String name, ScopeDescriptor descriptor) {
        this.parent = parent;
        this.name = name;
        this.cachedPath = null; // make sure the cached path is cleared after setting the parent
        this.descriptor = descriptor;
    }

    @Override
    public String absolutePath() {
        if (cachedPath == null) {
            if (parent == null) {
                cachedPath = PATH_SEPARATOR;
            } else {
                String parentPath = parent.absolutePath();
                // if the parent is the root then we don't have to add a separator
                // between the parent path and our path
                if (parentPath.length() == 1) {
                    cachedPath = parentPath + name();
                } else {
                    cachedPath = parentPath + PATH_SEPARATOR + name();
                }
            }
        }
        return cachedPath;
    }

    @Override
    public void accept(IPreferenceNodeVisitor visitor) throws BackingStoreException {
        if (!visitor.visit(this)) {
            return;
        }
        for (IEclipsePreferences p : getChildren()) {
            p.accept(visitor);
        }
    }

    protected IEclipsePreferences addChild(String childName, IEclipsePreferences child) {
        synchronized (childAndPropertyLock) {
            if (children == null) {
                children = new HashMap<>();
            }
            children.put(childName, child == null ? childName : child);
            return child;
        }
    }

    @Override
    public void addNodeChangeListener(INodeChangeListener listener) {
        checkRemoved();
        nodeChangeListeners.add(listener);
    }

    @Override
    public void addPreferenceChangeListener(IPreferenceChangeListener listener) {
        checkRemoved();
        preferenceChangeListeners.add(listener);
    }

    private IEclipsePreferences calculateRoot() {
        IEclipsePreferences result = this;
        while (result.parent() != null) {
            result = (IEclipsePreferences) result.parent();
        }
        return result;
    }

    /*
     * Convenience method for throwing an exception when methods are called on a
     * removed node.
     */
    protected void checkRemoved() {
        if (removed) {
            throw new IllegalStateException(NLS.bind(PrefsMessages.preferences_removedNode, name));
        }
    }

    @Override
    public String[] childrenNames() throws BackingStoreException {
        // illegal state if this node has been removed
        checkRemoved();
        String[] internal = internalChildNames();
        // if we are != 0 then we have already been initialized
        if (internal.length != 0) {
            return internal;
        }
        // we only want to query the descriptor for the child names if
        // this node is the scope root
        if (descriptor != null && getSegmentCount(absolutePath()) == 1) {
            return descriptor.childrenNames(absolutePath());
        }
        return internal;
    }

    protected String[] internalChildNames() {
        synchronized (childAndPropertyLock) {
            if (children == null || children.isEmpty()) {
                return EMPTY_STRING_ARRAY;
            }
            return children.keySet().toArray(String[]::new);
        }
    }

    /*
     * Version 1 (current version) path/key=value
     */
    protected static void convertFromProperties(EclipsePreferences node, Properties table) {
        table.remove(VERSION_KEY);
        for (Object propName : table.keySet()) {
            String fullKey = (String) propName;
            String value = table.getProperty(fullKey);
            if (value != null) {
                String[] splitPath = decodePath(fullKey);
                String path = splitPath[0];
                path = makeRelative(path);
                String key = splitPath[1];
                // use internal methods to avoid notifying listeners
                EclipsePreferences childNode = (EclipsePreferences) node.internalNode(path, false);
                childNode.internalPut(key, value);
                // notify listeners if applicable
            }
        }
    }

    protected boolean isLoading() {
        return loading;
    }

    protected void setLoading(boolean isLoading) {
        loading = isLoading;
    }

    public IEclipsePreferences create(EclipsePreferences nodeParent, String nodeName, Object context) {
        EclipsePreferences result = internalCreate(nodeParent, nodeName, context);
        nodeParent.addChild(nodeName, result);
        IEclipsePreferences loadLevel = result.getLoadLevel();

        // if this node or a parent node is not the load level then return
        // if the result node is not a load level, then a child must be
        if (loadLevel == null || result != loadLevel || isAlreadyLoaded(result) || result.isLoading()) {
            return result;
        }
        try {
            result.setLoading(true);
            result.load();
            result.loaded();
            result.flush();
        } catch (BackingStoreException e) {
            IPath location = result.getLocation();
            String message = NLS.bind(PrefsMessages.preferences_loadException,
                location == null ? EMPTY_STRING : location.toString());
            IStatus status = Status.error(message, e);
            RuntimeLog.log(status);
        } finally {
            result.setLoading(false);
        }
        return result;
    }

    @Override
    public void flush() throws BackingStoreException {
        IEclipsePreferences toFlush;
        synchronized (childAndPropertyLock) {
            toFlush = internalFlush();
        }
        // if we aren't at the right level, then flush the appropriate node
        if (toFlush != null) {
            toFlush.flush();
        }
    }

    /*
     * Do the real flushing in a non-synchronized internal method so sub-classes
     * (mainly ProjectPreferences and ProfilePreferences) don't cause deadlocks.
     *
     * If this node is not responsible for persistence (a load level), then this
     * method returns the node that should be flushed. Returns null if this method
     * performed the flush.
     */
    protected IEclipsePreferences internalFlush() throws BackingStoreException {
        // illegal state if this node has been removed
        checkRemoved();

        IEclipsePreferences loadLevel = getLoadLevel();

        // if this node or a parent is not the load level, then flush the children
        if (loadLevel == null) {
            for (String childrenName : childrenNames()) {
                node(childrenName).flush();
            }
            return null;
        }
        // a parent is the load level for this node
        if (this != loadLevel) {
            return loadLevel;
        }
        // this node is a load level
        // any work to do?
        return null;
    }

    @Override
    public String get(String key, String defaultValue) {
        String value = internalGet(key);
        return value == null ? defaultValue : value;
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = internalGet(key);
        return value == null ? defaultValue : TRUE.equalsIgnoreCase(value);
    }

    /*
     * Return a boolean value indicating whether or not a child with the given name
     * is known to this node.
     */
    protected boolean childExists(String childName) {
        synchronized (childAndPropertyLock) {
            if (children == null) {
                return false;
            }
            return children.containsKey(childName);
        }
    }

    /**
     * Thread safe way to obtain a child for a given key. Returns the child that
     * matches the given key, or null if there is no matching child.
     */
    protected IEclipsePreferences getChild(String key, Object context, boolean create) {
        synchronized (childAndPropertyLock) {
            if (children == null) {
                return null;
            }
            Object value = children.get(key);
            if (value == null) {
                return null;
            } else if (value instanceof IEclipsePreferences eclipsePreferences) {
                return eclipsePreferences;
            }
            // if we aren't supposed to create this node, then
            // just return null
            if (!create) {
                return null;
            }
            return addChild(key, create(this, key, context));
        }
    }

    /**
     * Thread safe way to obtain all children of this node. Never returns null.
     */
    private List<IEclipsePreferences> getChildren() {
        List<IEclipsePreferences> result = new ArrayList<>();
        for (String n : internalChildNames()) {
            IEclipsePreferences child = getChild(n, null, true);
            if (child != null) {
                result.add(child);
            }
        }
        return result;
    }

    @Override
    public int getInt(String key, int defaultValue) {
        String value = internalGet(key);
        int result = defaultValue;
        if (value != null) {
            try {
                result = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                // use default
            }
        }
        return result;
    }

    protected IEclipsePreferences getLoadLevel() {
        return descriptor == null ? null : descriptor.getLoadLevel(this);
    }

    /*
     * Subclasses to over-ride
     */
    protected IPath getLocation() {
        return null;
    }

    @Override
    public long getLong(String key, long defaultValue) {
        String value = internalGet(key);
        long result = defaultValue;
        if (value != null) {
            try {
                result = Long.parseLong(value);
            } catch (NumberFormatException e) {
                // use default
            }
        }
        return result;
    }

    protected EclipsePreferences internalCreate(EclipsePreferences nodeParent, String nodeName, Object context) {
        EclipsePreferences result = new EclipsePreferences(nodeParent, nodeName, descriptor);
        return result;
    }

    /**
     * Returns the existing value at the given key, or null if no such value exists.
     */
    protected String internalGet(String key) {
        // throw NPE if key is null
        if (key == null) {
            throw new NullPointerException();
        }
        // illegal state if this node has been removed
        checkRemoved();
        String result;
        synchronized (childAndPropertyLock) {
            result = properties.get(key);
        }
        return result;
    }

    /**
     * Implements the node(String) method, and optionally notifies listeners.
     */
    protected IEclipsePreferences internalNode(String path, boolean notify) {

        // illegal state if this node has been removed
        checkRemoved();

        // short circuit this node
        if (path.isEmpty()) {
            return this;
        }
        // if we have an absolute path use the root relative to
        // this node instead of the global root
        // in case we have a different hierarchy. (e.g. export)
        if (path.charAt(0) == IPath.SEPARATOR) {
            return (IEclipsePreferences) calculateRoot().node(path.substring(1));
        }
        int index = path.indexOf(IPath.SEPARATOR);
        String key = index == -1 ? path : path.substring(0, index);
        boolean added = false;
        IEclipsePreferences child = getChild(key, null, true);
        if (child == null) {
            child = create(this, key, null);
            added = true;
        }
        // notify listeners if a child was added
        if (added && notify) {
            fireNodeEvent(new NodeChangeEvent(this, child));
        }
        return (IEclipsePreferences) child.node(index == -1 ? EMPTY_STRING : path.substring(index + 1));
    }

    /**
     * Stores the given (key,value) pair, performing lazy initialization of the
     * properties field if necessary. Returns the old value for the given key, or
     * null if no value existed.
     */
    protected String internalPut(String key, String newValue) {
        synchronized (childAndPropertyLock) {
            // illegal state if this node has been removed
            checkRemoved();
            String oldValue = properties.get(key);
            if (oldValue != null && oldValue.equals(newValue)) {
                return oldValue;
            }
            properties = properties.put(key.intern(), newValue.intern());
            return oldValue;
        }
    }

    /*
     * Subclasses to over-ride.
     */
    protected boolean isAlreadyLoaded(IEclipsePreferences node) {
        return descriptor == null || descriptor.isAlreadyLoaded(node.absolutePath());
    }

    @Override
    public String[] keys() {
        // illegal state if this node has been removed
        synchronized (childAndPropertyLock) {
            checkRemoved();
            return properties.keys();
        }
    }

    /**
     * Loads the preference node. This method returns silently if the node does not
     * exist in the backing store (for example non-existent project).
     *
     * @throws BackingStoreException if the node exists in the backing store but it
     * could not be loaded
     */
    protected void load() throws BackingStoreException {
        if (descriptor == null) {
            load(getLocation());
        } else {
            // load the properties then set them without sending out change events
            Properties props = descriptor.load(absolutePath());
            if (props == null || props.isEmpty()) {
                return;
            }
            convertFromProperties(this, props);
        }
    }

    protected static Properties loadProperties(IPath location) throws BackingStoreException {
        Properties result = new Properties();
        try (InputStream input = getSaveInputStream(location)) {
            result.load(input);
        } catch (FileNotFoundException e) {
            // file doesn't exist but that's ok.
        } catch (IOException | IllegalArgumentException e) {
            String message = NLS.bind(PrefsMessages.preferences_loadException, location);
            log(new Status(IStatus.INFO, PrefsMessages.OWNER_NAME, IStatus.INFO, message, e));
            throw new BackingStoreException(message, e);
        }
        return result;
    }

    private static InputStream getSaveInputStream(IPath location) throws IOException {
        File target = location.toFile().getAbsoluteFile();
        if (!target.exists()) {
            target = new File(target + BACKUP_FILE_EXTENSION);
        }
        return new FileInputStream(target);
    }

    protected void load(IPath location) throws BackingStoreException {
        if (location == null) {
            return;
        }
        Properties fromDisk = loadProperties(location);
        convertFromProperties(this, fromDisk);
    }

    protected void loaded() {
        if (descriptor == null) {
            // do nothing
        } else {
            descriptor.loaded(absolutePath());
        }
    }

    public static void log(IStatus status) {
        RuntimeLog.log(status);
    }

    protected void makeDirty() {
        EclipsePreferences node = this;
        while (node != null && !node.removed) {
            node.dirty = true;
            node = (EclipsePreferences) node.parent();
        }
    }

    public boolean isDirty() {
        return dirty;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Preferences node(String pathName) {
        return internalNode(pathName, true);
    }

    protected void fireNodeEvent(final NodeChangeEvent event) {
        for (final INodeChangeListener listener : nodeChangeListeners) {
            SafeRunner.run(() -> listener.added(event));
        }
    }

    @Override
    public boolean nodeExists(String path) throws BackingStoreException {
        // short circuit for checking this node
        if (path.isEmpty()) {
            return !removed;
        }
        // illegal state if this node has been removed.
        // do this AFTER checking for the empty string.
        checkRemoved();

        // use the root relative to this node instead of the global root
        // in case we have a different hierarchy. (e.g. export)
        if (path.charAt(0) == IPath.SEPARATOR) {
            return calculateRoot().nodeExists(path.substring(1));
        }
        int index = path.indexOf(IPath.SEPARATOR);
        boolean noSlash = index == -1;

        // if we are looking for a simple child then just look in the table and return
        if (noSlash) {
            return childExists(path);
        }
        // otherwise load the parent of the child and then recursively ask
        String childName = path.substring(0, index);
        if (!childExists(childName)) {
            return false;
        }
        IEclipsePreferences child = getChild(childName, null, true);
        if (child == null) {
            return false;
        }
        return child.nodeExists(path.substring(index + 1));
    }

    @Override
    public Preferences parent() {
        // illegal state if this node has been removed
        checkRemoved();
        return parent;
    }

    /*
     * Convenience method for notifying preference change listeners.
     */
    protected void firePreferenceEvent(String key, Object oldValue, Object newValue) {
        final PreferenceChangeEvent event = new PreferenceChangeEvent(this, key, oldValue, newValue);
        for (final IPreferenceChangeListener listener : preferenceChangeListeners) {
            SafeRunner.run(() -> listener.preferenceChange(event));
        }
    }

    @Override
    public void put(String key, String newValue) {
        if (key == null || newValue == null) {
            throw new NullPointerException();
        }
        String oldValue = internalPut(key, newValue);
        if (!newValue.equals(oldValue)) {
            makeDirty();
            firePreferenceEvent(key, oldValue, newValue);
        }
    }

    @Override
    public void putInt(String key, int value) {
        put(key, Integer.toString(value));
    }

    @Override
    public void putLong(String key, long value) {
        put(key, Long.toString(value));
    }

    @Override
    public void remove(String key) {
        String oldValue;
        synchronized (childAndPropertyLock) {
            // illegal state if this node has been removed
            checkRemoved();
            oldValue = properties.get(key);
            if (oldValue == null) {
                return;
            }
            properties = properties.removeKey(key);
        }
        makeDirty();
        firePreferenceEvent(key, oldValue, null);
    }

    @Override
    public void removeNodeChangeListener(INodeChangeListener listener) {
        checkRemoved();
        nodeChangeListeners.remove(listener);
    }

    @Override
    public void removePreferenceChangeListener(IPreferenceChangeListener listener) {
        checkRemoved();
        preferenceChangeListeners.remove(listener);
    }

    public static int getSegmentCount(String path) {
        StringTokenizer tokenizer = new StringTokenizer(path, String.valueOf(IPath.SEPARATOR));
        return tokenizer.countTokens();
    }

    /*
     * Return a relative path
     */
    public static String makeRelative(String path) {
        if (path == null) {
            return EMPTY_STRING;
        }
        if (path.length() > 0 && path.charAt(0) == IPath.SEPARATOR) {
            return path.substring(1);
        }
        return path;
    }

    /*
     * Return a 2 element String array. element 0 - the path element 1 - the key The
     * path may be null. The key is never null.
     */
    public static String[] decodePath(String fullPath) {
        String key;
        String path = null;

        // check to see if we have an indicator which tells us where the path ends
        int index = fullPath.indexOf(DOUBLE_SLASH);
        if (index == -1) {
            // we don't have a double-slash telling us where the path ends
            // so the path is up to the last slash character
            int lastIndex = fullPath.lastIndexOf(IPath.SEPARATOR);
            if (lastIndex == -1) {
                key = fullPath;
            } else {
                path = fullPath.substring(0, lastIndex);
                key = fullPath.substring(lastIndex + 1);
            }
        } else {
            // the child path is up to the double-slash and the key
            // is the string after it
            path = fullPath.substring(0, index);
            key = fullPath.substring(index + 2);
        }
        // adjust if we have an absolute path
        if (path != null) {
            if (path.isEmpty()) {
                path = null;
            } else if (path.charAt(0) == IPath.SEPARATOR) {
                path = path.substring(1);
            }
        }
        return new String[] { path, key };
    }

    @Override
    public String toString() {
        return absolutePath();
    }

}
