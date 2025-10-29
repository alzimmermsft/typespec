/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.*;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.spi.RegistryContributor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.util.NLS;
import java.util.Hashtable;

/**
 * An object which represents the user-defined contents of an extension in a
 * plug-in manifest.
 */
public class ConfigurationElement extends RegistryObject {

    static final ConfigurationElement[] EMPTY_ARRAY = new ConfigurationElement[0];

    // The id of the parent element. It can be a configuration element or an
    // extension
    int parentId;
    byte parentType; // This value is only interesting when running from cache.

    // Store the properties and the value of the configuration element.
    // The format is the following:
    // [p1, v1, p2, v2, configurationElementValue]
    // If the array size is even, there is no "configurationElementValue (ie
    // getValue returns null)".
    // The properties and their values are alternated (v1 is the value of p1).
    protected String[] propertiesAndValue;

    // The name of the configuration element
    private String name;

    // ID of the actual contributor of this element
    // This value can be null when the element is loaded from disk and the owner has
    // been uninstalled.
    // This happens when the configuration is obtained from a delta containing
    // removed extension.
    private String contributorId;

    void throwException(String message, Throwable exception) throws CoreException {
        throw new CoreException(new Status(IStatus.ERROR, RegistryMessages.OWNER_NAME, IRegistryConstants.PLUGIN_ERROR,
            message, exception));
    }

    protected String getValue() {
        return getValueAsIs();
    }

    String getValueAsIs() {
        if (propertiesAndValue.length != 0 && propertiesAndValue.length % 2 == 1) {
            return propertiesAndValue[propertiesAndValue.length - 1];
        }
        return null;
    }

    public String getAttribute(String attrName) {
        return getAttributeAsIs(attrName);
    }

    String getAttributeAsIs(String attrName) {
        if (propertiesAndValue.length <= 1) {
            return null;
        }
        int size = propertiesAndValue.length - (propertiesAndValue.length % 2);
        for (int i = 0; i < size; i += 2) {
            if (propertiesAndValue[i].equals(attrName)) {
                return propertiesAndValue[i + 1];
            }
        }
        return null;
    }

    protected String[] getAttributeNames() {
        if (propertiesAndValue.length <= 1) {
            return RegistryObjectManager.EMPTY_STRING_ARRAY;
        }

        int size = propertiesAndValue.length / 2;
        String[] result = new String[size];
        for (int i = 0; i < size; i++) {
            result[i] = propertiesAndValue[i * 2];
        }
        return result;
    }

    public ConfigurationElement[] getChildren(String childrenName) {
        if (getRawChildren().length == 0) {
            return ConfigurationElement.EMPTY_ARRAY;
        }

        ConfigurationElement[] result = new ConfigurationElement[1]; // Most of the time there is only one match
        int idx = 0;
        RegistryObjectManager objectManager = registry.getObjectManager();
        for (int child : children) {
            ConfigurationElement toTest = (ConfigurationElement) objectManager.getObject(child,
                noExtraData()
                    ? RegistryObjectManager.CONFIGURATION_ELEMENT
                    : RegistryObjectManager.THIRDLEVEL_CONFIGURATION_ELEMENT);
            if (toTest.name.equals(childrenName)) {
                if (idx != 0) {
                    ConfigurationElement[] copy = new ConfigurationElement[result.length + 1];
                    System.arraycopy(result, 0, copy, 0, result.length);
                    result = copy;
                }
                result[idx++] = toTest;
            }
        }
        if (idx == 0) {
            result = ConfigurationElement.EMPTY_ARRAY;
        }
        return result;
    }

    protected String getName() {
        return name;
    }

    public IContributor getContributor() {
        return registry.getObjectManager().getContributor(contributorId);
    }

    protected Object createExecutableExtension(String attributeName) throws CoreException {
        String prop = null;
        String executable;
        String contributorName = null;
        String className = null;
        Object initData = null;
        int i;

        if (attributeName != null) {
            prop = getAttribute(attributeName);
        } else {
            // property not specified, try as element value
            prop = getValue();
            if (prop != null) {
                prop = prop.trim();
                if (prop.equals("")) { //$NON-NLS-1$
                    prop = null;
                }
            }
        }

        if (prop == null) {
            // property not defined, try as a child element
            ConfigurationElement[] exec;
            ConfigurationElement[] parms;
            ConfigurationElement element;
            Hashtable<String, String> initParms;
            String pname;

            exec = getChildren(attributeName);
            if (exec.length != 0) {
                element = exec[0]; // assumes single definition
                contributorName = element.getAttribute("plugin"); //$NON-NLS-1$
                className = element.getAttribute("class"); //$NON-NLS-1$
                parms = element.getChildren("parameter"); //$NON-NLS-1$
                if (parms.length != 0) {
                    initParms = new Hashtable<>(parms.length + 1);
                    for (i = 0; i < parms.length; i++) {
                        pname = parms[i].getAttribute("name"); //$NON-NLS-1$
                        if (pname != null) {
                            initParms.put(pname, parms[i].getAttribute("value")); //$NON-NLS-1$
                        }
                    }
                    if (!initParms.isEmpty()) {
                        initData = initParms;
                    }
                }
            } else {
                // specified name is not a simple attribute nor child element
                throwException(NLS.bind(RegistryMessages.exExt_extDefNotFound, attributeName), null);
            }
        } else {
            // simple property or element value, parse it into its components
            i = prop.indexOf(':');
            if (i != -1) {
                executable = prop.substring(0, i).trim();
                initData = prop.substring(i + 1).trim();
            } else {
                executable = prop;
            }

            i = executable.indexOf('/');
            if (i != -1) {
                contributorName = executable.substring(0, i).trim();
                className = executable.substring(i + 1).trim();
            } else {
                className = executable;
            }
        }

        // create a new instance
        RegistryContributor defaultContributor = registry.getObjectManager().getContributor(contributorId);
        Object result = registry.createExecutableExtension(defaultContributor, className, contributorName);

        // Check if we have extension adapter and initialize;
        // Make the call even if the initialization string is null
        try {
            // We need to take into account both "old" and "new" style executable extensions
            ConfigurationElementHandle confElementHandle
                = new ConfigurationElementHandle(registry.getObjectManager(), getObjectId());
            if (result instanceof IExecutableExtension) {
                ((IExecutableExtension) result).setInitializationData(confElementHandle, attributeName, initData);
            }
        } catch (CoreException ce) {
            // user code threw exception
            throw ce;
        } catch (Exception te) {
            // user code caused exception
            throwException(NLS.bind(RegistryMessages.plugin_initObjectError, getContributor().getName(), className),
                te);
        }

        return result;
    }

    String getAttribute(String attrName, String locale) {
        registry.logMultiLangError();
        return getAttribute(attrName);
    }

    String getValue(String locale) {
        registry.logMultiLangError();
        return getValue();
    }
}
