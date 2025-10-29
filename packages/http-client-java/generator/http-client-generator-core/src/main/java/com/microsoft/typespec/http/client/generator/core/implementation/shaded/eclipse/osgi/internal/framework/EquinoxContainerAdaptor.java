/*******************************************************************************
 * Copyright (c) 2012, 2021 IBM Corporation and others.
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

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.Module;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.ModuleContainerAdaptor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.ModuleLoader;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.ModuleWiring;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.container.AtomicLazyInitializer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.hookregistry.ClassLoaderHook;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.loader.BundleLoader;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.loader.FragmentLoader;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.loader.SystemBundleLoader;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage.Storage;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.BundleEvent;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Constants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.FrameworkEvent;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.FrameworkListener;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.hooks.resolver.ResolverHookFactory;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.wiring.BundleRevision;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class EquinoxContainerAdaptor extends ModuleContainerAdaptor {
    private final EquinoxContainer container;
    private final Storage storage;
    private final OSGiFrameworkHooks hooks;
    // The ClassLoader parent to use when creating ModuleClassLoaders.
    private final ClassLoader moduleClassLoaderParent;

    final AtomicLazyInitializer<Executor> resolverExecutor;
    final Callable<Executor> lazyResolverExecutorCreator;
    final AtomicLazyInitializer<Executor> startLevelExecutor;
    final Callable<Executor> lazyStartLevelExecutorCreator;

    public EquinoxContainerAdaptor(EquinoxContainer container, Storage storage) {
        this.container = container;
        this.storage = storage;
        this.hooks = new OSGiFrameworkHooks(container, storage);
        this.moduleClassLoaderParent
            = getModuleClassLoaderParent(container.getConfiguration(), container.getBootLoader());

        EquinoxConfiguration config = container.getConfiguration();
        @SuppressWarnings("deprecation")
        String resolverThreadCntProp = config.getConfiguration(EquinoxConfiguration.PROP_EQUINOX_RESOLVER_THREAD_COUNT,
            //
            config.getConfiguration(EquinoxConfiguration.PROP_RESOLVER_THREAD_COUNT));

        int resolverThreadCnt;
        try {
            // note that resolver thread count defaults to -1 (compute based on processor
            // number)
            resolverThreadCnt = resolverThreadCntProp == null ? -1 : Integer.parseInt(resolverThreadCntProp);
        } catch (NumberFormatException e) {
            resolverThreadCnt = -1;
        }
        String startLevelThreadCntProp
            = config.getConfiguration(EquinoxConfiguration.PROP_EQUINOX_START_LEVEL_THREAD_COUNT);
        int startLevelThreadCnt;
        try {
            // Note that start-level thread count defaults to 1 (synchronous start)
            startLevelThreadCnt = startLevelThreadCntProp == null ? 1 : Integer.parseInt(startLevelThreadCntProp);
        } catch (NumberFormatException e) {
            startLevelThreadCnt = 1;
        }

        // Use two different executors for resolver and start-level because of the
        // different queue requirements

        // For the resolver we must use a SynchronousQueue because multiple threads
        // can kick off a resolution operation and block one of the executor threads
        // per resolution operation.
        // If the number of concurrent resolution operations reaches the number of
        // executor threads then each executor thread may end up blocked causing the
        // executor to no longer accept work. A SynchronousQueue prevents that from
        // happening.
        this.resolverExecutor = new AtomicLazyInitializer<>();
        this.lazyResolverExecutorCreator = createLazyExecutorCreator( //
            "Equinox resolver thread - " + EquinoxContainerAdaptor.this, //$NON-NLS-1$
            resolverThreadCnt, new SynchronousQueue<>());

        // For the start-level we can safely use a growing queue because the thread
        // feeding the
        // start-level executor with work is a single thread and it can safely block
        // waiting
        // for the work of the executor threads to finish.
        this.startLevelExecutor = new AtomicLazyInitializer<>();
        this.lazyStartLevelExecutorCreator = createLazyExecutorCreator(//
            "Equinox start level thread - " + EquinoxContainerAdaptor.this, //$NON-NLS-1$
            startLevelThreadCnt, new LinkedBlockingQueue<>(1000));

    }

    private Callable<Executor> createLazyExecutorCreator(final String threadName, int threadCnt,
        final BlockingQueue<Runnable> queue) {
        // use the number of processors when configured value is <=0
        final int maxThreads = threadCnt <= 0 ? Runtime.getRuntime().availableProcessors() : threadCnt;
        return () -> {
            if (maxThreads == 1) {
                // just do synchronous execution with current thread
                return Runnable::run;
            }
            // Always want to create core threads until max size
            // idle timeout; make it short to get rid of threads quickly after use
            int idleTimeout = 10;
            // try to name the threads with useful name
            ThreadFactory threadFactory = r -> {
                Thread t = new Thread(r, threadName);
                t.setDaemon(true);
                return t;
            };
            // use a rejection policy that simply runs the task in the current thread once
            // the max pool size is reached
            RejectedExecutionHandler rejectHandler = new ThreadPoolExecutor.CallerRunsPolicy();

            ThreadPoolExecutor executor = new ThreadPoolExecutor(maxThreads, maxThreads, idleTimeout, TimeUnit.SECONDS,
                queue, threadFactory, rejectHandler);
            executor.allowCoreThreadTimeOut(true);
            return executor;
        };
    }

    private static ClassLoader getModuleClassLoaderParent(EquinoxConfiguration configuration, ClassLoader bootLoader) {
        // allow hooks to determine the parent class loader
        for (ClassLoaderHook hook : configuration.getHookRegistry().getClassLoaderHooks()) {
            ClassLoader parent = hook.getModuleClassLoaderParent(configuration);
            if (parent != null) {
                // first one to return non-null wins.
                return parent;
            }
        }
        // DEFAULT behavior:
        // check property for specified parent
        // check the osgi defined property first
        String type = configuration.getConfiguration(Constants.FRAMEWORK_BUNDLE_PARENT);
        if (type == null) {
            type = configuration.getConfiguration(EquinoxConfiguration.PROP_PARENT_CLASSLOADER,
                Constants.FRAMEWORK_BUNDLE_PARENT_BOOT);
        }

        if (Constants.FRAMEWORK_BUNDLE_PARENT_FRAMEWORK.equalsIgnoreCase(type)
            || EquinoxConfiguration.PARENT_CLASSLOADER_FWK.equalsIgnoreCase(type)) {
            ClassLoader cl = EquinoxContainer.class.getClassLoader();
            return cl == null ? bootLoader : cl;
        }
        if (Constants.FRAMEWORK_BUNDLE_PARENT_APP.equalsIgnoreCase(type))
            return ClassLoader.getSystemClassLoader();
        if (Constants.FRAMEWORK_BUNDLE_PARENT_EXT.equalsIgnoreCase(type)) {
            ClassLoader appCL = ClassLoader.getSystemClassLoader();
            if (appCL != null)
                return appCL.getParent();
        }
        return bootLoader;

    }

    @Override
    public ResolverHookFactory getResolverHookFactory() {
        return hooks.getResolverHookFactory();
    }

    @Override
    public void publishContainerEvent(ContainerEvent type, Module module, Throwable error,
        FrameworkListener... listeners) {
        EquinoxEventPublisher publisher = container.getEventPublisher();
        if (publisher != null) {
            publisher.publishFrameworkEvent(getType(type), module.getBundle(), error, listeners);
        }
    }

    @Override
    public void publishModuleEvent(ModuleEvent type, Module module, Module origin) {
        EquinoxEventPublisher publisher = container.getEventPublisher();
        if (publisher != null) {
            publisher.publishBundleEvent(getType(type), module.getBundle(), origin.getBundle());
        }
    }

    @Override
    public String getProperty(String key) {
        return storage.getConfiguration().getConfiguration(key);
    }

    @Override
    public ModuleLoader createModuleLoader(ModuleWiring wiring) {
        if (wiring.getBundle().getBundleId() == 0) {
            ClassLoader cl = EquinoxContainer.class.getClassLoader();
            cl = cl == null ? container.getBootLoader() : cl;
            return new SystemBundleLoader(wiring, container, cl);
        }
        if ((wiring.getRevision().getTypes() & BundleRevision.TYPE_FRAGMENT) != 0) {
            return new FragmentLoader();
        }
        return new BundleLoader(wiring, container, moduleClassLoaderParent);
    }

    static int getType(ContainerEvent type) {
        return switch (type) {
            case ERROR -> FrameworkEvent.ERROR;
            case INFO -> FrameworkEvent.INFO;
            case WARNING -> FrameworkEvent.WARNING;
            case REFRESH -> FrameworkEvent.PACKAGES_REFRESHED;
            case START_LEVEL -> FrameworkEvent.STARTLEVEL_CHANGED;
            case STARTED -> FrameworkEvent.STARTED;
            case STOPPED -> FrameworkEvent.STOPPED;
            case STOPPED_REFRESH -> FrameworkEvent.STOPPED_SYSTEM_REFRESHED;
            case STOPPED_UPDATE -> FrameworkEvent.STOPPED_UPDATE;
            case STOPPED_TIMEOUT -> FrameworkEvent.WAIT_TIMEDOUT;
            // default to error
        };
    }

    private int getType(ModuleEvent type) {
        return switch (type) {
            case INSTALLED -> BundleEvent.INSTALLED;
            case LAZY_ACTIVATION -> BundleEvent.LAZY_ACTIVATION;
            case RESOLVED -> BundleEvent.RESOLVED;
            case STARTED -> BundleEvent.STARTED;
            case STARTING -> BundleEvent.STARTING;
            case STOPPING -> BundleEvent.STOPPING;
            case STOPPED -> BundleEvent.STOPPED;
            case UNINSTALLED -> BundleEvent.UNINSTALLED;
            case UNRESOLVED -> BundleEvent.UNRESOLVED;
            case UPDATED -> BundleEvent.UPDATED;
            // TODO log error?
        };
    }

    @Override
    public String toString() {
        return container.toString();
    }

    @Override
    public void updatedDatabase() {
        StorageSaver saver = container.getStorageSaver();
        if (saver == null)
            return;
        saver.save();
    }

    @Override
    public Executor getResolverExecutor() {
        return resolverExecutor.getInitialized(lazyResolverExecutorCreator);
    }

    @Override
    public ScheduledExecutorService getScheduledExecutor() {
        return container.getScheduledExecutor();
    }

}
