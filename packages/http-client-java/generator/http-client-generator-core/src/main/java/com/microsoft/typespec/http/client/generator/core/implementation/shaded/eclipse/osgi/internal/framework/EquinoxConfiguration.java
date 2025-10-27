/*******************************************************************************
 * Copyright (c) 2003, 2021 IBM Corporation and others.
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
 *     Hannes Wellmann - Bug 577497 - Support version-specific entries in dev-classPath file
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.framework;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.hookregistry.HookRegistry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.location.EquinoxLocations;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.service.environment.EnvironmentInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Internal class.
 */
public class EquinoxConfiguration implements EnvironmentInfo {

    private final AliasMapper aliasMapper = new AliasMapper();

    public boolean DEFINE_PACKAGE_ATTRIBUTES;

    public static final String PROP_SETPERMS_CMD = "osgi.filepermissions.command"; //$NON-NLS-1$
	public static final String PROP_USE_SYSTEM_PROPERTIES = "osgi.framework.useSystemProperties"; //$NON-NLS-1$

    public static final String PROP_OSGI_OS = "osgi.os"; //$NON-NLS-1$
	public static final String PROP_OSGI_WS = "osgi.ws"; //$NON-NLS-1$
	public static final String PROP_OSGI_ARCH = "osgi.arch"; //$NON-NLS-1$
	public static final String PROP_OSGI_NL = "osgi.nl"; //$NON-NLS-1$

    public static final String PROP_ROOT_LOCALE = "equinox.root.locale"; //$NON-NLS-1$

	public static final String PROP_PARENT_CLASSLOADER = "osgi.parentClassloader"; //$NON-NLS-1$
	// A parent classloader type that specifies the framework classlaoder
	public static final String PARENT_CLASSLOADER_FWK = "fwk"; //$NON-NLS-1$

    public static final String PROPERTY_STRICT_BUNDLE_ENTRY_PATH = "osgi.strictBundleEntryPath";//$NON-NLS-1$
	public static final String PROP_STATE_SAVE_DELAY_INTERVAL = "eclipse.stateSaveDelayInterval"; //$NON-NLS-1$

	public static final String PROP_MODULE_LOCK_TIMEOUT = "osgi.module.lock.timeout"; //$NON-NLS-1$
	public static final String PROP_MODULE_AUTO_START_ON_RESOLVE = "osgi.module.auto.start.on.resolve"; //$NON-NLS-1$
    public static final String PROP_LOG_CAPTURE_ENTRY_LOCATION = "equinox.log.capture.entry.location"; //$NON-NLS-1$

	@Deprecated
	public static final String PROP_RESOLVER_THREAD_COUNT = "equinox.resolver.thead.count"; //$NON-NLS-1$
	public static final String PROP_EQUINOX_RESOLVER_THREAD_COUNT = "equinox.resolver.thread.count"; //$NON-NLS-1$
	public static final String PROP_EQUINOX_START_LEVEL_THREAD_COUNT = "equinox.start.level.thread.count"; //$NON-NLS-1$
	public static final String PROP_EQUINOX_START_LEVEL_RESTRICT_PARALLEL = "equinox.start.level.restrict.parallel"; //$NON-NLS-1$
	public static final String PROP_RESOLVER_REVISION_BATCH_SIZE = "equinox.resolver.revision.batch.size"; //$NON-NLS-1$
	public static final String PROP_RESOLVER_BATCH_TIMEOUT = "equinox.resolver.batch.timeout"; //$NON-NLS-1$

    public static final int SIGNED_CONTENT_VERIFY_TRUST = 0x02;
    public boolean runtimeVerifySignedBundles;

    public boolean THROW_ISE_UNREGISTER;

	public static final class ConfigValues {
		/**
		 * Value of {@link #localConfig} properties that should be considered
		 * <code>null</code> and for which access should not fall back to
		 * {@link System#getProperty(String)}. The instance must be compared by identity
		 * (==, not equals) and must not be leaked outside this class.
		 */
		private final static String NULL_CONFIG = "com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.equinox.configuration.null.value"; //$NON-NLS-1$

        private final boolean useSystemProperties;
        private final Properties localConfig;

        public ConfigValues(Map<String, ?> initialConfiguration) {
            Map<String, Object> initialConfig = initialConfiguration == null
                ? new HashMap<>(0)
                : new HashMap<>(initialConfiguration);
			Object useSystemPropsValue = initialConfig.get(PROP_USE_SYSTEM_PROPERTIES);
			this.useSystemProperties = useSystemPropsValue != null && Boolean.parseBoolean(
                useSystemPropsValue.toString());
			Properties tempConfiguration = useSystemProperties ? EquinoxContainer.secureAction.getProperties()
					: new Properties();
			// do this the hard way to handle null values
			for (Map.Entry<String, ?> initialEntry : initialConfig.entrySet()) {
				if (initialEntry.getValue() == null) {
					if (useSystemProperties) {
						tempConfiguration.remove(initialEntry.getKey());
					} else {
						tempConfiguration.put(initialEntry.getKey(), NULL_CONFIG);
					}
				} else {
					tempConfiguration.put(initialEntry.getKey(), initialEntry.getValue());
				}
			}
			localConfig = useSystemProperties ? null : tempConfiguration;
		}

        public String getProperty(String key) {
			// have to access configuration directly instead of using getConfiguration to
			// get access to NULL_CONFIG values
			String result = internalGet(key);
			if (result == NULL_CONFIG) {
				return null;
			}
			return result == null ? EquinoxContainer.secureAction.getProperty(key) : result;
		}

		public String setProperty(String key, String value) {
			if (value == null) {
				return clearConfiguration(key);
			}
			return setConfiguration(key, value);
		}

		public String getConfiguration(String key) {
			String result = internalGet(key);
			return result == NULL_CONFIG ? null : result;
		}

		private String internalGet(String key) {
			if (useSystemProperties) {
				return EquinoxContainer.secureAction.getProperty(key);
			}
			return localConfig.getProperty(key);
		}

		public String getConfiguration(String key, String defaultValue) {
			String result = getConfiguration(key);
			return result == null ? defaultValue : result;
		}

		public String setConfiguration(String key, String value) {
			Object result = internalPut(key, value);
			return result instanceof String && result != NULL_CONFIG ? (String) result : null;
		}

		public String clearConfiguration(String key) {
			Object result = internalRemove(key);
			if (!useSystemProperties) {
				internalPut(key, NULL_CONFIG);
			}
			return result instanceof String && result != NULL_CONFIG ? (String) result : null;
		}

		private Object internalPut(String key, String value) {
			if (useSystemProperties) {
				return EquinoxContainer.secureAction.getProperties().put(key, value);
			}
			return localConfig.put(key, value);
		}

		private Object internalRemove(String key) {
			if (useSystemProperties) {
				return EquinoxContainer.secureAction.getProperties().remove(key);
			}
			return localConfig.remove(key);
		}

		public Map<String, String> getConfiguration() {
			Properties props = useSystemProperties ? EquinoxContainer.secureAction.getProperties() : localConfig;
			// must sync on props to avoid concurrent modification exception
			synchronized (props) {
				Map<String, String> result = new HashMap<>(props.size());
				for (Object key : props.keySet()) {
					if (key instanceof String) {
						String skey = (String) key;
						String sValue = props.getProperty(skey);
						if (sValue != NULL_CONFIG) {
							result.put(skey, sValue);
						}
					}
				}
				return result;
			}
		}

    }

    @Override
	public String[] getCommandLineArgs() {
		return allArgs;
	}

    @Override
	public String[] getNonFrameworkArgs() {
		return appArgs;
	}

	@Override
	public String getOSArch() {
		return getConfiguration(PROP_OSGI_ARCH);
	}

	@Override
	public String getNL() {
		return getConfiguration(PROP_OSGI_NL);
	}

	@Override
	public String getOS() {
		return getConfiguration(PROP_OSGI_OS);
	}

	@Override
	public String getWS() {
		return getConfiguration(PROP_OSGI_WS);
	}

    public String getConfiguration(String key) {
		return this.configValues.getConfiguration(key);
	}

	public String getConfiguration(String key, String defaultValue) {
		return this.configValues.getConfiguration(key, defaultValue);
	}

	public String setConfiguration(String key, String value) {
		return this.configValues.setConfiguration(key, value);
	}

    public Map<String, String> getConfiguration() {
		return this.configValues.getConfiguration();
	}

    public HookRegistry getHookRegistry() {
		return hookRegistry;
	}

	@Override
	public String getProperty(String key) {
		return this.configValues.getProperty(key);
	}

	@Override
	public String setProperty(String key, String value) {
		return this.configValues.setProperty(key, value);
	}

	public AliasMapper getAliasMapper() {
		return aliasMapper;
	}

    public EquinoxLocations getEquinoxLocations() {
		return equinoxLocations;
	}

}
