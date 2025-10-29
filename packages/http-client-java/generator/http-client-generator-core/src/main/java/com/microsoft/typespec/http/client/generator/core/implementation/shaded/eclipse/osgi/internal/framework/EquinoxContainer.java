/*******************************************************************************
 * Copyright (c) 2012, 2017 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.framework;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.framework.eventmgr.ListenerQueue;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.framework.util.SecureAction;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.connect.ConnectBundleFile;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.location.EquinoxLocations;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.log.EquinoxLogServices;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.serviceregistry.ServiceRegistry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.signedcontent.SignedContentFactory;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage.BundleInfo;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage.Storage;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage.bundlefile.MRUBundleFileList;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.AdminPermission;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Bundle;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.BundleException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.FrameworkUtil;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.connect.ConnectContent;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.connect.ConnectModule;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.connect.ModuleConnector;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

public class EquinoxContainer implements ThreadFactory, Runnable {
    public static final String NAME
        = "com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi"; //$NON-NLS-1$
    static final SecureAction secureAction = AccessController.doPrivileged(SecureAction.createSecureAction());

    public Storage getStorage() {
        return null;
    }

    public EquinoxConfiguration getConfiguration() {
        return null;
    }

    public EquinoxLocations getLocations() {
        return null;
    }

    public EquinoxLogServices getLogServices() {
        return null;
    }

    public Bundle getBundle(Class<?> clazz) {
        Bundle b = FrameworkUtil.getBundle(clazz);
        if (b != null) {
            return b;
        }
        // check if it is the system bundle
        return AccessController.doPrivileged((PrivilegedAction<Bundle>) () -> {
            if (clazz.getClassLoader() == EquinoxContainer.class.getClassLoader()) {
                return getStorage().getModuleContainer().getModule(0).getBundle();
            }
            return null;
        });
    }

    public SignedContentFactory getSignedContentFactory() {
        return null;
    }

    public boolean isBootDelegationPackage(String name) {
        return false;
    }

    public EquinoxEventPublisher getEventPublisher() {
        return null;
    }

    ScheduledExecutorService getScheduledExecutor() {
        return null;
    }

    public ServiceRegistry getServiceRegistry() {
        return null;
    }

    public <K, V, E> ListenerQueue<K, V, E> newListenerQueue() {
        return null;
    }

    void checkAdminPermission(Bundle bundle, String action) {
        if (bundle == null)
            return;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(new AdminPermission(bundle, action));
    }

    @Override
    public String toString() {
        return "Equinox Container: " + null; //$NON-NLS-1$
    }

    StorageSaver getStorageSaver() {
        return null;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, "Active Thread: " + toString()); //$NON-NLS-1$
        t.setDaemon(true);
        t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }

    @Override
    public void run() {
        // Do nothing; just used to ensure the active thread is created during init
    }

    public ClassLoader getBootLoader() {
        return null;
    }

    public ConnectModules getConnectModules() {
        return null;
    }

    public static class ConnectModules {
        final ModuleConnector moduleConnector;
        private final ConcurrentMap<String, ConnectModule> connectModules = new ConcurrentHashMap<>();
        private final WeakHashMap<ConnectContent, WeakReference<ConnectBundleFile>> contents = new WeakHashMap<>();

        public ConnectModules(ModuleConnector moduleConnector) {
            this.moduleConnector = moduleConnector;
        }

        public ConnectModule connect(String location) {
            if (moduleConnector == null) {
                return null;
            }
            ConnectModule result = connectModules.compute(location, (k, v) -> {
                try {
                    return moduleConnector.connect(location).orElse(null);
                } catch (BundleException e) {
                    throw new IllegalStateException(e);
                }
            });
            return result;
        }

        public ConnectBundleFile getConnectBundleFile(ConnectModule module, File basefile,
            BundleInfo.Generation generation, MRUBundleFileList mruList) throws IOException {
            ConnectContent content = module.getContent();
            synchronized (contents) {
                WeakReference<ConnectBundleFile> ref = contents.get(content);
                if (ref != null) {
                    ConnectBundleFile bundleFile = ref.get();
                    if (bundleFile != null) {
                        return bundleFile;
                    }
                }
                ConnectBundleFile bundleFile = new ConnectBundleFile(module, basefile, generation, mruList);
                contents.put(content, new WeakReference<>(bundleFile));
                return bundleFile;
            }
        }

        public ModuleConnector getModuleConnector() {
            return moduleConnector;
        }

        public ConnectModule getConnectModule(String location) {
            return connectModules.get(location);
        }
    }

    @SuppressWarnings("unchecked")
    public static <E extends Throwable> void sneakyThrow(Throwable e) throws E {
        throw (E) e;
    }
}
