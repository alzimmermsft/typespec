/*******************************************************************************
 * Copyright (c) 2004, 2024 IBM Corporation and others.
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
 *     Semion Chichelnitsky (semion@il.ibm.com) - bug 208564
 *     Jan-Ove Weichel (janove.weichel@vogella.com) - bug 474359
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.preferences;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.runtime.RuntimeLog;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.ListenerList;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.MultiStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.SafeRunner;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Status;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.preferences.BundleDefaultsScope;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.preferences.ConfigurationScope;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.preferences.DefaultScope;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.preferences.IEclipsePreferences;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.preferences.IExportedPreferences;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.preferences.IPreferenceFilter;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.preferences.IPreferencesService;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.preferences.IScope;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.preferences.InstanceScope;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.preferences.PreferenceFilterEntry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.preferences.PreferenceModifyListener;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.preferences.UserScope;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.prefs.BackingStoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.prefs.Preferences;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * @since 3.0
 */
public class PreferencesService implements IPreferencesService {
    private static final String MATCH_TYPE_PREFIX = "prefix"; //$NON-NLS-1$

    // the order of search scopes when people don't have a specific order set
    private static final char EXPORT_ROOT_PREFIX = '!';
    private static final char BUNDLE_VERSION_PREFIX = '@';
    private static final String VERSION_KEY = "file_export_version"; //$NON-NLS-1$
    private static final String EMPTY_STRING = ""; //$NON-NLS-1$

    private static PreferencesService instance;
    private static final RootPreferences root = new RootPreferences();
    private Object registryHelper = null;
    private final Map<String, EclipsePreferences> defaultScopes = new HashMap<>();

    /*
     * Return the instance.
     */
    public static PreferencesService getDefault() {
        if (instance == null) {
            instance = new PreferencesService();
        }
        return instance;
    }

    private PreferencesService() {
        initializeDefaultScope(BundleDefaultsScope.SCOPE, new BundleDefaultPreferences());
        initializeDefaultScope(DefaultScope.SCOPE, new DefaultPreferences());
        initializeDefaultScope(InstanceScope.SCOPE, new InstancePreferences());
        initializeDefaultScope(ConfigurationScope.SCOPE, new ConfigurationPreferences());
        initializeDefaultScope(UserScope.SCOPE, new UserPreferences());
    }

    @Override
    public IStatus applyPreferences(IExportedPreferences preferences) throws CoreException {
        // TODO investigate refactoring to merge with new #apply(IEclipsePreferences,
        // IPreferenceFilter[]) APIs
        if (preferences == null) {
            throw new IllegalArgumentException();
        }
        final MultiStatus result
            = new MultiStatus(PrefsMessages.OWNER_NAME, IStatus.OK, PrefsMessages.preferences_applyProblems, null);

        IEclipsePreferences modifiedNode = firePreApplyEvent(preferences);
        try {
            // start by visiting the root
            modifiedNode.accept(node -> { // create a visitor to apply the given set of preferences
                IEclipsePreferences globalNode;
                if (node.parent() == null) {
                    globalNode = root;
                } else {
                    globalNode = (IEclipsePreferences) root.node(node.absolutePath());
                }
                ExportedPreferences epNode = (ExportedPreferences) node;

                // if this node is an export root then we need to remove
                // it from the global preferences before continuing.
                boolean removed = false;
                if (epNode.isExportRoot()) {
                    // TODO should only have to do this if any of my children have properties to set
                    globalNode.removeNode();
                    removed = true;
                }

                // iterate over the preferences in this node and set them
                // in the global space.
                String[] keys = epNode.keys();

                // if this node was removed then we need to create a new one
                if (removed) {
                    globalNode = (IEclipsePreferences) root.node(node.absolutePath());
                }
                // the list for properties to remove
                List<String> propsToRemove = new ArrayList<>(Arrays.asList(globalNode.keys()));
                for (String key : keys) {
                    // preferences that are not in the applied node will be removed
                    propsToRemove.remove(key);
                    String value = node.get(key, null);
                    if (value != null) {
                        globalNode.put(key, value);
                    }
                }
                if (!propsToRemove.isEmpty() && !(globalNode instanceof EclipsePreferences)) {
                    // intern strings we import because some people in their property change
                    // listeners use identity instead of equals. See bug 20193 and 20534.
                    propsToRemove.replaceAll(String::intern);
                }
                for (String keyToRemove : propsToRemove) {
                    globalNode.remove(keyToRemove);
                }

                // keep visiting children
                return true;
            });
        } catch (BackingStoreException e) {
            throw new CoreException(Status.error(PrefsMessages.preferences_applyProblems, e));
        }

        // save the preferences
        try {
            getRootNode().node(modifiedNode.absolutePath()).flush();
        } catch (BackingStoreException e) {
            throw new CoreException(Status.error(PrefsMessages.preferences_saveProblems, e));
        }

        return result;
    }

    private boolean containsKeys(IEclipsePreferences aRoot) throws BackingStoreException {
        final boolean[] result = new boolean[] { false };
        aRoot.accept(node -> {
            if (node.keys().length != 0) {
                result[0] = true;
            }
            return !result[0];
        });
        return result[0];
    }

    /*
     * Convert the given properties file from legacy format to one which is Eclipse
     * 3.0 compliant.
     *
     * Convert the plug-in version indicator entries to export roots.
     */
    private Properties convertFromLegacy(Properties properties) {
        Properties result = new Properties();
        String prefix = IPath.SEPARATOR + InstanceScope.SCOPE + IPath.SEPARATOR;
        for (Entry<?, ?> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (value != null) {
                int index = key.indexOf(IPath.SEPARATOR);
                if (index == -1) {
                    result.put(BUNDLE_VERSION_PREFIX + key, value);
                    result.put(EXPORT_ROOT_PREFIX + prefix + key, EMPTY_STRING);
                } else {
                    String path = key.substring(0, index);
                    key = key.substring(index + 1);
                    result.put(EclipsePreferences.encodePath(prefix + path, key), value);
                }
            }
        }
        return result;
    }

    /*
     * Convert the given properties file into a node hierarchy suitable for
     * importing.
     */
    private IExportedPreferences convertFromProperties(Properties properties) {
        IExportedPreferences result = ExportedPreferences.newRoot();
        for (Entry<?, ?> entry : properties.entrySet()) {
            String path = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (path.charAt(0) == EXPORT_ROOT_PREFIX) {
                ExportedPreferences current = (ExportedPreferences) result.node(path.substring(1));
                current.setExportRoot();
            } else if (path.charAt(0) == BUNDLE_VERSION_PREFIX) {
                ExportedPreferences current
                    = (ExportedPreferences) result.node(InstanceScope.SCOPE).node(path.substring(1));
                current.setVersion(value);
            } else {
                String[] decoded = EclipsePreferences.decodePath(path);
                path = decoded[0] == null ? EMPTY_STRING : decoded[0];
                ExportedPreferences current = (ExportedPreferences) result.node(path);
                String key = decoded[1];
                current.put(key, value);
            }
        }
        return result;
    }

    public WeakReference<Object> applyRuntimeDefaults(String name, WeakReference<Object> pluginReference) {
        if (registryHelper == null) {
            return null;
        }
        return ((PreferenceServiceRegistryHelper) registryHelper).applyRuntimeDefaults(name, pluginReference);
    }

    private void initializeDefaultScope(String scope, EclipsePreferences preferences) {
        defaultScopes.put(scope, preferences);
        root.addChild(scope, null);
    }

    public IEclipsePreferences createNode(String key) {
        IScope scope = defaultScopes.get(key);
        if (scope == null) {
            if (registryHelper == null) {
                return new EclipsePreferences(root, key);
            }
            return ((PreferenceServiceRegistryHelper) registryHelper).createNode(root, key);
        }
        return scope.create(root, key);
    }

    /*
     * Give clients a chance to modify the tree before it is applied globally
     */
    private IEclipsePreferences firePreApplyEvent(IEclipsePreferences tree) {
        if (registryHelper == null) {
            return tree;
        }
        final IEclipsePreferences[] result = new IEclipsePreferences[] { tree };
        ListenerList<PreferenceModifyListener> listeners
            = ((PreferenceServiceRegistryHelper) registryHelper).getModifyListeners();
        for (final PreferenceModifyListener listener : listeners) {
            SafeRunner.run(() -> result[0] = listener.preApply(result[0]));
        }
        return result[0];
    }

    @Override
    public IEclipsePreferences getRootNode() {
        return root;
    }

    /*
     * Return true if the given tree contains information that the specified filter
     * is interested in, and false otherwise.
     */
    private boolean internalMatches(IEclipsePreferences tree, IPreferenceFilter filter) throws BackingStoreException {
        String[] scopes = filter.getScopes();
        if (scopes == null) {
            throw new IllegalArgumentException();
        }
        String treePath = tree.absolutePath();
        // see if this node is applicable by going over all our scopes
        for (String scope : scopes) {
            Map<String, PreferenceFilterEntry[]> mapping = filter.getMapping(scope);
            // if the mapping is null then we match everything
            if (mapping == null) {
                // if we are the root check to see if the scope exists
                if (tree.parent() == null
                    && tree.nodeExists(scope)
                    && containsKeys((IEclipsePreferences) tree.node(scope))) {
                    return true;
                }
                // otherwise check to see if we are in the right scope
                if (scopeMatches(scope, tree) && containsKeys(tree)) {
                    return true;
                }
                continue;
            }
            // iterate over the list of declared nodes
            for (String nodePath : mapping.keySet()) {
                String nodeFullPath = '/' + scope + '/' + nodePath;
                // if this subtree isn't in a hierarchy we are interested in, then go to the
                // next one
                if (!nodeFullPath.startsWith(treePath)) {
                    continue;
                }
                // get the child node
                String childPath = nodeFullPath.substring(treePath.length());
                childPath = EclipsePreferences.makeRelative(childPath);
                if (tree.nodeExists(childPath)) {
                    PreferenceFilterEntry[] entries;
                    // protect against wrong classes since this is user-code
                    try {
                        entries = mapping.get(nodePath);
                    } catch (ClassCastException e) {
                        RuntimeLog.log(Status.error(PrefsMessages.preferences_classCastFilterEntry, e));
                        continue;
                    }
                    // if there are no entries defined then we return false even if we
                    // are supposed to match on the existence of the node as a whole (bug 88820)
                    Preferences child = tree.node(childPath);
                    if (entries == null) {
                        if (child.keys().length != 0 || child.childrenNames().length != 0) {
                            return true;
                        }
                    } else {
                        // otherwise check to see if we have any applicable keys
                        for (PreferenceFilterEntry entry : entries) {
                            if (entry == null) {
                                continue;
                            }
                            if (entry.getMatchType() == null) {
                                if (child.get(entry.getKey(), null) != null) {
                                    return true;
                                }
                            } else if (internalMatchesWithMatchType(entry, child.keys())) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /*
     * Internal method that collects the matching filters for the given tree and
     * returns them.
     */
    private IPreferenceFilter[] internalMatches(IEclipsePreferences tree, IPreferenceFilter[] filters)
        throws BackingStoreException {
        List<IPreferenceFilter> result = new ArrayList<>();
        for (IPreferenceFilter filter : filters) {
            if (internalMatches(tree, filter)) {
                result.add(filter);
            }
        }
        return result.toArray(new IPreferenceFilter[0]);
    }

    /*
     * Internal method that check the matching preferences for entry with specific
     * match type.
     */
    private boolean internalMatchesWithMatchType(PreferenceFilterEntry entry, String[] keys) {
        if (keys == null || keys.length == 0) {
            return false;
        }
        String key = entry.getKey();
        String matchType = entry.getMatchType();
        if (!matchType.equalsIgnoreCase(MATCH_TYPE_PREFIX)) {
            return false;
        }
        for (String k : keys) {
            if (k.startsWith(key)) {
                return true;
            }
        }
        return false;
    }

    /*
     * Returns a boolean value indicating whether or not the given Properties object
     * is the result of a preference export previous to Eclipse 3.0.
     *
     * Check the contents of the file. In Eclipse 3.0 we printed out a file version
     * key.
     */
    private boolean isLegacy(Properties properties) {
        return properties.getProperty(VERSION_KEY) == null;
    }

    @Override
    public IPreferenceFilter[] matches(IEclipsePreferences tree, IPreferenceFilter[] filters) throws CoreException {
        if (filters == null || filters.length == 0) {
            return new IPreferenceFilter[0];
        }
        try {
            return internalMatches(tree, filters);
        } catch (BackingStoreException e) {
            throw new CoreException(Status.error(PrefsMessages.preferences_matching, e));
        }
    }

    @Override
    public IExportedPreferences readPreferences(InputStream input) throws CoreException {
        if (input == null) {
            throw new IllegalArgumentException();
        }
        // read the file into a properties object
        Properties properties = new Properties();
        try (input) {
            properties.load(input);
        } catch (IOException | IllegalArgumentException e) {
            throw new CoreException(Status.error(PrefsMessages.preferences_importProblems, e));
        }
        // an empty file is an invalid file format
        if (properties.isEmpty()) {
            throw new CoreException(Status.error(PrefsMessages.preferences_invalidFileFormat, null));
        }
        // manipulate the file if it from a legacy preference export
        if (isLegacy(properties)) {
            properties = convertFromLegacy(properties);
        } else {
            properties.remove(VERSION_KEY);
        }

        // convert the Properties object into an object to return
        return convertFromProperties(properties);
    }

    /**
     * Return true if the given node is in the specified scope and false otherwise.
     */
    private boolean scopeMatches(String scope, IEclipsePreferences tree) {
        // the root isn't in any scope
        if (tree.parent() == null) {
            return false;
        }
        // fancy math to get the first segment of the path
        String path = tree.absolutePath();
        int index = path.indexOf('/', 1);
        String sub = path.substring(1, index == -1 ? path.length() : index);
        return scope.equals(sub);
    }

    public void setRegistryHelper(Object registryHelper) {
        if (this.registryHelper != null && this.registryHelper != registryHelper) {
            ((PreferenceServiceRegistryHelper) this.registryHelper).stop();
        }
        this.registryHelper = registryHelper;
    }

}
