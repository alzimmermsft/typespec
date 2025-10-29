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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.Module.StartOptions;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.Module.State;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.ModuleContainer.ResolutionLock.Permits;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.ModuleContainerAdaptor.ContainerEvent;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.ModuleContainerAdaptor.ModuleEvent;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.ModuleDatabase.Sort;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.framework.eventmgr.CopyOnWriteIdentityMap;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.framework.eventmgr.EventDispatcher;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.framework.eventmgr.EventManager;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.framework.eventmgr.ListenerQueue;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.framework.util.SecureAction;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.container.InternalUtils;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.framework.EquinoxConfiguration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.framework.FilterImpl;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.messages.Msg;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.report.resolution.ResolutionReport;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.AdminPermission;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Bundle;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.BundleException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Constants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.FrameworkListener;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.InvalidSyntaxException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Version;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.namespace.BundleNamespace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.namespace.HostNamespace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.namespace.IdentityNamespace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.namespace.PackageNamespace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.startlevel.FrameworkStartLevel;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.wiring.BundleCapability;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.wiring.BundleRevision;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.wiring.FrameworkWiring;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.resource.Capability;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.resource.Namespace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.resource.Requirement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.resource.Resource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.resource.Wire;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.resolver.ResolutionException;
import java.io.Closeable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A container for installing, updating, uninstalling and resolve modules.
 * 
 * @since 3.10
 */
public final class ModuleContainer {
    private final static SecureAction secureAction = AccessController.doPrivileged(SecureAction.createSecureAction());

    /**
     * An implementation of FrameworkWiring for this container
     */
    private final ContainerWiring frameworkWiring;

    /**
     * An implementation of FrameworkStartLevel for this container
     */
    private final ContainerStartLevel frameworkStartLevel;

    /**
     * The module database for this container.
     */
    final ModuleDatabase moduleDatabase;

    /**
     * The module adaptor for this container.
     */
    final ModuleContainerAdaptor adaptor;

    /**
     * The module resolver which implements the ResolverContext and handles calling
     * the resolver service.
     */
    private final ModuleResolver moduleResolver;

    /**
     * Holds the system module while it is being refreshed
     */
    private final AtomicReference<SystemModule> refreshingSystemModule = new AtomicReference<>();

    private final long moduleLockTimeout;

    private final boolean autoStartOnResolve;

    final boolean restrictParallelStart;

    /**
     * Constructs a new container with the specified adaptor, module database.
     * 
     * @param adaptor the adaptor for the container
     * @param moduledataBase the module database
     */
    public ModuleContainer(ModuleContainerAdaptor adaptor, ModuleDatabase moduledataBase) {
        this.adaptor = adaptor;
        this.moduleResolver = new ModuleResolver(adaptor);
        this.moduleDatabase = moduledataBase;
        this.frameworkWiring = new ContainerWiring();
        this.frameworkStartLevel = new ContainerStartLevel();
        long tempModuleLockTimeout = 30;
        String moduleLockTimeoutProp = adaptor.getProperty(EquinoxConfiguration.PROP_MODULE_LOCK_TIMEOUT);
        if (moduleLockTimeoutProp != null) {
            try {
                tempModuleLockTimeout = Long.parseLong(moduleLockTimeoutProp);
                // don't do anything less than one second
                if (tempModuleLockTimeout < 1) {
                    tempModuleLockTimeout = 1;
                }
            } catch (NumberFormatException e) {
                // will default to 30
            }
        }
        this.moduleLockTimeout = tempModuleLockTimeout;

        String autoStartOnResolveProp = adaptor.getProperty(EquinoxConfiguration.PROP_MODULE_AUTO_START_ON_RESOLVE);
        if (autoStartOnResolveProp == null) {
            autoStartOnResolveProp = Boolean.toString(true);
        }
        this.autoStartOnResolve = Boolean.parseBoolean(autoStartOnResolveProp);
        this.restrictParallelStart = Boolean
            .parseBoolean(adaptor.getProperty(EquinoxConfiguration.PROP_EQUINOX_START_LEVEL_RESTRICT_PARALLEL));
    }

    /**
     * Returns the adaptor for this container
     * 
     * @return the adaptor for this container
     */
    public ModuleContainerAdaptor getAdaptor() {
        return adaptor;
    }

    /**
     * Returns the list of currently installed modules sorted by module id.
     * 
     * @return the list of currently installed modules sorted by module id.
     */
    public List<Module> getModules() {
        return moduleDatabase.getModules();
    }

    /**
     * Returns the module installed with the specified id, or null if no such module
     * is installed.
     * 
     * @param id the id of the module
     * @return the module with the specified id, or null of no such module is
     * installed.
     */
    public Module getModule(long id) {
        return moduleDatabase.getModule(id);
    }

    /**
     * Returns the module installed with the specified location, or null if no such
     * module is installed.
     * 
     * @param location the location of the module
     * @return the module with the specified location, or null of no such module is
     * installed.
     */
    public Module getModule(String location) {
        return moduleDatabase.getModule(location);
    }

    /**
     * Creates a synthetic requirement that is not associated with any module
     * revision. This is useful for calling
     * {@link FrameworkWiring#findProviders(Requirement)}.
     * 
     * @param namespace the requirement namespace
     * @param directives the requirement directives
     * @param attributes the requirement attributes
     * @return a synthetic requirement
     */
    public static Requirement createRequirement(String namespace, Map<String, String> directives,
        Map<String, ?> attributes) {
        return new ModuleRequirement(namespace, directives, attributes, null);
    }

    /**
     * Generates a human readable string representation of the the given capability,
     * mapping the namespace to well-known header names.
     * 
     * @param capability the {@link Capability} for which a string representation is
     * desired
     * @since 3.19
     */
    public static String toString(Capability capability) {
        if (PackageNamespace.PACKAGE_NAMESPACE.equals(capability.getNamespace())) {
            return Constants.EXPORT_PACKAGE + ": " + createOSGiCapability(capability); //$NON-NLS-1$
        } else if (BundleNamespace.BUNDLE_NAMESPACE.equals(capability.getNamespace())) {
            return Constants.BUNDLE_SYMBOLICNAME + ": " + createOSGiCapability(capability); //$NON-NLS-1$
        } else if (HostNamespace.HOST_NAMESPACE.equals(capability.getNamespace())) {
            return Constants.BUNDLE_SYMBOLICNAME + ": " + createOSGiCapability(capability); //$NON-NLS-1$
        }
        return Constants.PROVIDE_CAPABILITY + ": " + capability; //$NON-NLS-1$
    }

    /**
     * Generates a human readable string representation of the the given
     * {@link Resource} using the IDENTITY_NAMESPACE
     * 
     * @param resource the {@link Resource} for which a string representation is
     * desired
     * 
     * @since 3.22
     */
    public static String toString(Resource resource) {
        String id = null;
        Version version = null;
        List<Capability> caps = resource.getCapabilities(null);
        for (Capability cap : caps) {
            if (cap.getNamespace().equals(IdentityNamespace.IDENTITY_NAMESPACE)) {
                id = cap.getAttributes().get(IdentityNamespace.IDENTITY_NAMESPACE).toString();
                version = (Version) cap.getAttributes().get(IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE);
            }
        }
        if (id != null) {
            if (version != null) {
                return String.format("%s %s", id, version); //$NON-NLS-1$
            }
            return id;
        }
        return resource.toString();
    }

    private static String createOSGiCapability(Capability cap) {
        Map<String, Object> attributes = new HashMap<>(cap.getAttributes());
        Map<String, String> directives = cap.getDirectives();
        String name = String.valueOf(attributes.remove(cap.getNamespace()));
        return name + toString(attributes, false, true) + toString(directives, true, true);
    }

    /**
     * Generates a human readable string representation of the the given
     * requirement, mapping the namespace to well-known header names.
     * 
     * @param requirement the {@link Requirement} for which a string representation
     * is desired
     * @since 3.19
     */
    public static String toString(Requirement requirement) {
        if (PackageNamespace.PACKAGE_NAMESPACE.equals(requirement.getNamespace())) {
            return Constants.IMPORT_PACKAGE + ": " //$NON-NLS-1$
                + createOSGiRequirement(requirement, PackageNamespace.CAPABILITY_VERSION_ATTRIBUTE,
                    PackageNamespace.CAPABILITY_BUNDLE_VERSION_ATTRIBUTE);
        } else if (BundleNamespace.BUNDLE_NAMESPACE.equals(requirement.getNamespace())) {
            return Constants.REQUIRE_BUNDLE + ": " //$NON-NLS-1$
                + createOSGiRequirement(requirement, BundleNamespace.CAPABILITY_BUNDLE_VERSION_ATTRIBUTE);
        } else if (HostNamespace.HOST_NAMESPACE.equals(requirement.getNamespace())) {
            return Constants.FRAGMENT_HOST + ": " //$NON-NLS-1$
                + createOSGiRequirement(requirement, HostNamespace.CAPABILITY_BUNDLE_VERSION_ATTRIBUTE);
        }
        return Constants.REQUIRE_CAPABILITY + ": " + requirement; //$NON-NLS-1$
    }

    private static String createOSGiRequirement(Requirement requirement, String... versions) {
        Map<String, String> directives = new HashMap<>(requirement.getDirectives());
        String filter = directives.remove(Namespace.REQUIREMENT_FILTER_DIRECTIVE);
        if (filter == null)
            throw new IllegalArgumentException("No filter directive found:" + requirement); //$NON-NLS-1$
        FilterImpl filterImpl;
        try {
            filterImpl = FilterImpl.newInstance(filter);
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException("Invalid filter directive", e); //$NON-NLS-1$
        }
        Map<String, String> matchingAttributes = filterImpl.getStandardOSGiAttributes(versions);
        String name = matchingAttributes.remove(requirement.getNamespace());
        if (name == null)
            throw new IllegalArgumentException("Invalid requirement: " + requirement); //$NON-NLS-1$
        return name + toString(matchingAttributes, false, true) + toString(directives, true, true);
    }

    static <V> String toString(Map<String, V> map, boolean directives) {
        return toString(map, directives, false);
    }

    static <V> String toString(Map<String, V> map, boolean directives, boolean stringsOnly) {
        if (map.isEmpty())
            return ""; //$NON-NLS-1$
        String assignment = directives ? ":=" : "="; //$NON-NLS-1$ //$NON-NLS-2$
        Set<Map.Entry<String, V>> set = map.entrySet();
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, V> entry : set) {
            sb.append("; "); //$NON-NLS-1$
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) value;
                if (list.isEmpty())
                    continue;
                Object component = list.get(0);
                String className = component.getClass().getName();
                String type = className.substring(className.lastIndexOf('.') + 1);
                sb.append(key).append(':').append("List<").append(type).append(">").append(assignment).append('"'); //$NON-NLS-1$ //$NON-NLS-2$
                for (Object object : list)
                    sb.append(object).append(',');
                sb.setLength(sb.length() - 1);
                sb.append('"');
            } else {
                String type = ""; //$NON-NLS-1$
                if (!(value instanceof String) && !stringsOnly) {
                    String className = value.getClass().getName();
                    type = ":" + className.substring(className.lastIndexOf('.') + 1); //$NON-NLS-1$
                }
                sb.append(key).append(type).append(assignment).append('"').append(value).append('"');
            }
        }
        return sb.toString();
    }

    ModuleWiring getWiring(ModuleRevision revision) {
        return moduleDatabase.getWiring(revision);
    }

    /**
     * Returns the {@link FrameworkWiring} for this container
     * 
     * @return the framework wiring for this container.
     */
    public FrameworkWiring getFrameworkWiring() {
        return frameworkWiring;
    }

    /**
     * Returns the {@link FrameworkStartLevel} for this container
     * 
     * @return the framework start level for this container
     */
    public FrameworkStartLevel getFrameworkStartLevel() {
        return frameworkStartLevel;
    }

    /**
     * Attempts to resolve the current revisions of the specified modules.
     * 
     * @param triggers the modules to resolve or {@code null} to resolve
     * all unresolved current revisions.
     * @param triggersMandatory true if the triggers must be resolved. This will
     * result in a {@link ResolutionException} if set to
     * true and one of the triggers could not be resolved.
     * @see FrameworkWiring#resolveBundles(Collection)
     * @return A resolution report for the resolve operation
     */
    public ResolutionReport resolve(Collection<Module> triggers, boolean triggersMandatory) {
        if (isRefreshingSystemModule()) {
            return new ModuleResolutionReport(null, Collections.emptyMap(),
                new ResolutionException("Unable to resolve while shutting down the framework."),  //$NON-NLS-1$
                adaptor);
        }
        ResolutionReport report;
        try (Permits resolutionPermits = _resolutionLock.acquire()) {
            do {
                try {
                    report = resolveAndApply(triggers, triggersMandatory, resolutionPermits);
                } catch (RuntimeException e) {
                    if (e.getCause()instanceof BundleException be) {
                        if (be.getType() == BundleException.REJECTED_BY_HOOK
                            || be.getType() == BundleException.STATECHANGE_ERROR) {
                            return new ModuleResolutionReport(null, Collections.emptyMap(), new ResolutionException(be),
                                adaptor);
                        }
                    }
                    throw e;
                }
            } while (report == null);
        } catch (ResolutionLockException e) {
            return new ModuleResolutionReport(null, Collections.emptyMap(),
                new ResolutionException("Timeout acquiring lock for resolution", e, Collections.emptyList()),
                // $NON-NLS-1$
                adaptor);
        }
        return report;
    }

    private ResolutionReport resolveAndApply(Collection<Module> triggers, boolean triggersMandatory,
        Permits resolutionPermits) {
        if (triggers == null) {
            triggers = new ArrayList<>(0);
        }
        if (triggers.isEmpty()) {
            // if there are no triggers then they cannot be mandatory
            triggersMandatory = false;
        }
        Collection<ModuleRevision> triggerRevisions = new ArrayList<>(triggers.size());
        Collection<ModuleRevision> unresolved = new ArrayList<>();
        Map<ModuleRevision, ModuleWiring> wiringClone;
        long timestamp;
        moduleDatabase.readLock();
        try {
            timestamp = moduleDatabase.getRevisionsTimestamp();
            wiringClone = moduleDatabase.getWiringsClone();
            for (Module module : triggers) {
                if (!State.UNINSTALLED.equals(module.getState())) {
                    ModuleRevision current = module.getCurrentRevision();
                    if (current != null)
                        triggerRevisions.add(current);
                }
            }
            Collection<Module> allModules = moduleDatabase.getModules();
            for (Module module : allModules) {
                ModuleRevision revision = module.getCurrentRevision();
                if (revision != null && !wiringClone.containsKey(revision))
                    unresolved.add(revision);
            }
        } finally {
            moduleDatabase.readUnlock();
        }

        ModuleResolutionReport report
            = moduleResolver.resolveDelta(triggerRevisions, triggersMandatory, unresolved, wiringClone, moduleDatabase);
        Map<Resource, List<Wire>> resolutionResult = report.getResolutionResult();
        Map<ModuleRevision, ModuleWiring> deltaWiring = resolutionResult == null
            ? Collections.emptyMap()
            : moduleResolver.generateDelta(resolutionResult, wiringClone);
        if (deltaWiring.isEmpty())
            return report; // nothing to do

        Collection<Module> modulesResolved = new ArrayList<>();
        for (ModuleRevision deltaRevision : deltaWiring.keySet()) {
            if (!wiringClone.containsKey(deltaRevision))
                modulesResolved.add(deltaRevision.getRevisions().getModule());
        }

        return applyDelta(deltaWiring, modulesResolved, timestamp, resolutionPermits) ? report : null;
    }

    // The resolution algorithm uses optimistic locking approach;
    // This involves taking a snapshot of the state and performing an
    // operation on the snapshot while holding no locks and then
    // obtaining the write lock to apply the results. If we
    // detect the state has changed since the snapshot taken then
    // the process is started over. If we allow too many threads
    // to try to do this at the same time it causes thrashing
    // between taking the snapshot and successfully applying the
    // results. Instead of resorting to single threaded operations
    // we choose to limit the number of concurrent resolves
    final ResolutionLock _resolutionLock = new ResolutionLock();
    final ReentrantLock _bundleStateLock = new ReentrantLock();

    static class ResolutionLockException extends Exception {
        private static final long serialVersionUID = 1L;

        public ResolutionLockException() {
            super();
        }

        public ResolutionLockException(Throwable cause) {
            super(cause);
        }
    }

    /**
     * A resolution hook allows for a max of 10 threads to do resolution operations
     * at the same time. The implementation uses a semaphore to grant the max number
     * of permits (threads) but a reentrant read lock is also used to detect
     * reentry. If a thread reenters then no extra permits are required by the
     * thread. This lock returns a Permits object that implements closeable for use
     * in try->with. If permits is closed multiple times then the additional close
     * operations are a no-op.
     */
    static class ResolutionLock {
        final static int MAX_RESOLUTION_PERMITS = 10;
        final Semaphore permitPool = new Semaphore(MAX_RESOLUTION_PERMITS);
        final ReentrantReadWriteLock reenterLock = new ReentrantReadWriteLock();

        class Permits implements Closeable {
            private final int aquiredPermits;
            private final AtomicBoolean closed = new AtomicBoolean();

            Permits(int requestedPermits) throws ResolutionLockException {
                if (reenterLock.getReadHoldCount() > 0) {
                    // thread is already holding permits; don't request more
                    requestedPermits = 0;
                }
                this.aquiredPermits = requestedPermits;
                boolean previousInterruption = Thread.interrupted();
                try {
                    if (!permitPool.tryAcquire(requestedPermits, 30, TimeUnit.SECONDS)) {
                        throw new ResolutionLockException();
                    }
                } catch (InterruptedException e) {
                    throw new ResolutionLockException(e);
                } finally {
                    if (previousInterruption) {
                        Thread.currentThread().interrupt();
                    }
                }
                // mark this thread as holding permits
                reenterLock.readLock().lock();
            }

            @Override
            public void close() {
                if (closed.compareAndSet(false, true)) {
                    permitPool.release(aquiredPermits);
                    reenterLock.readLock().unlock();
                }
            }
        }

        Permits acquire() throws ResolutionLockException {
            return new Permits(1);
        }
    }

    private boolean applyDelta(Map<ModuleRevision, ModuleWiring> deltaWiring, Collection<Module> modulesResolved,
        long timestamp, Permits resolutionPermits) {
        List<Module> modulesLocked = new ArrayList<>(modulesResolved.size());
        // now attempt to apply the delta
        try {
            // Acquire the necessary RESOLVED state change lock.
            // Note this is done while holding a global lock to avoid multiple threads
            // trying to compete over
            // locking multiple modules; otherwise out of order locks between modules can
            // happen
            // NOTE this MUST be done outside of holding the moduleDatabase lock also to
            // avoid
            // introducing out of order locks between the bundle state change lock and the
            // moduleDatabase
            // lock.
            _bundleStateLock.lock();
            try {
                for (Module module : modulesResolved) {
                    try {
                        // avoid grabbing the lock if the timestamp has changed
                        if (timestamp != moduleDatabase.getRevisionsTimestamp()) {
                            return false; // need to try again
                        }
                        module.lockStateChange(ModuleEvent.RESOLVED);
                        modulesLocked.add(module);
                    } catch (BundleException e) {
                        // before throwing an exception here, see if the timestamp has changed
                        if (timestamp != moduleDatabase.getRevisionsTimestamp()) {
                            return false; // need to try again
                        }
                        // TODO throw some appropriate exception
                        throw new IllegalStateException(Msg.ModuleContainer_StateLockError, e);
                    }
                }
            } finally {
                _bundleStateLock.unlock();
            }

            Map<ModuleWiring, Collection<ModuleRevision>> hostsWithDynamicFrags = new HashMap<>(0);
            moduleDatabase.writeLock();
            try {
                if (timestamp != moduleDatabase.getRevisionsTimestamp())
                    return false; // need to try again

                Map<ModuleRevision, ModuleWiring> wiringCopy = moduleDatabase.getWiringsCopy();
                for (Map.Entry<ModuleRevision, ModuleWiring> deltaEntry : deltaWiring.entrySet()) {
                    ModuleWiring current = wiringCopy.get(deltaEntry.getKey());
                    if (current != null) {
                        // need to update the provided capabilities, provided and required wires for
                        // currently resolved
                        current.setCapabilities(deltaEntry.getValue().getCapabilities());
                        current.setProvidedWires(deltaEntry.getValue().getProvidedWires());
                        current.setRequirements(deltaEntry.getValue().getRequirements());
                        current.setRequiredWires(deltaEntry.getValue().getRequiredWires());
                        deltaEntry.setValue(current); // set the real wiring into the delta
                    } else {
                        ModuleRevision revision = deltaEntry.getValue().getRevision();
                        modulesResolved.add(revision.getRevisions().getModule());
                        if ((revision.getTypes() & BundleRevision.TYPE_FRAGMENT) != 0) {
                            for (ModuleWire hostWire : deltaEntry.getValue()
                                .getRequiredModuleWires(HostNamespace.HOST_NAMESPACE)) {
                                // check to see if the host revision has a wiring
                                ModuleWiring hostWiring = hostWire.getProvider().getWiring();
                                if (hostWiring != null) {
                                    Collection<ModuleRevision> dynamicFragments
                                        = hostsWithDynamicFrags.computeIfAbsent(hostWiring, k -> new ArrayList<>());
                                    dynamicFragments.add(hostWire.getRequirer());
                                }
                            }
                        }
                    }
                }
                moduleDatabase.mergeWiring(deltaWiring);
                moduleDatabase.sortModules(modulesLocked, Sort.BY_DEPENDENCY, Sort.BY_START_LEVEL);
            } finally {
                moduleDatabase.writeUnlock();
            }
            // set the modules state to resolved
            for (Module module : modulesLocked) {
                module.setState(State.RESOLVED);
            }
            // attach fragments to already resolved hosts that have
            // dynamically attached fragments
            for (Map.Entry<ModuleWiring, Collection<ModuleRevision>> dynamicFragments : hostsWithDynamicFrags
                .entrySet()) {
                dynamicFragments.getKey().loadFragments(dynamicFragments.getValue());
            }
        } finally {
            for (Module module : modulesLocked) {
                module.unlockStateChange(ModuleEvent.RESOLVED);
            }
        }

        // release resolution permits before firing events
        resolutionPermits.close();

        for (Module module : modulesLocked) {
            adaptor.publishModuleEvent(ModuleEvent.RESOLVED, module, module);
        }

        // If there are any triggers re-start them now if requested
        if (autoStartOnResolve) {
            // This is questionable behavior according to the spec but this was the way
            // equinox previously behaved
            // Need to auto-start any persistently started bundles that got resolved
            for (Module module : modulesLocked) {
                // Note that we check inStart here. There is still a timing issue that is
                // impossible to avoid.
                // Another thread could attempt to start the module but we could check inStart()
                // before that thread
                // increments inStart. One thread will win the race to grab the module STARTED
                // lock. That thread
                // will end up actually starting the module and the other thread will block. If
                // a timeout occurs
                // the blocking thread will get an exception.
                if (!module.inStart() && module.getId() != 0) {
                    start(module, StartOptions.TRANSIENT_IF_AUTO_START, StartOptions.TRANSIENT_RESUME);
                }
            }
        }
        return true;
    }

    private void start(Module module, StartOptions... options) {
        try {
            secureAction.start(module, options);
        } catch (BundleException e) {
            if (e.getType() == BundleException.STATECHANGE_ERROR) {
                if (Module.ACTIVE_SET.contains(module.getState())) {
                    // There is still a timing issue here;
                    // but at least try to detect that another thread is starting the module
                    return;
                }
            }
            adaptor.publishContainerEvent(ContainerEvent.ERROR, module, e);
        } catch (IllegalStateException e) {
            // been uninstalled
            return;
        }
    }

    /**
     * Returns the revisions that have {@link ModuleWiring#isCurrent() non-current},
     * {@link ModuleWiring#isInUse() in use} module wirings.
     * 
     * @return A collection containing a snapshot of the revisions which have
     * non-current, in use ModuleWirings, or an empty collection if there
     * are no such revisions.
     */
    public Collection<ModuleRevision> getRemovalPending() {
        return moduleDatabase.getRemovalPending();
    }

    /**
     * Return the active start level value of this container.
     *
     * If the container is in the process of changing the start level this method
     * must return the active start level if this differs from the requested start
     * level.
     *
     * @return The active start level value of the Framework.
     */
    public int getStartLevel() {
        return frameworkStartLevel.getStartLevel();
    }

    void setStartLevel(Module module, int startlevel) {
        frameworkStartLevel.setStartLevel(module, startlevel);
    }

    long getModuleLockTimeout() {
        return this.moduleLockTimeout;
    }

    Set<Module> getRefreshClosure(Collection<Module> initial, Map<ModuleRevision, ModuleWiring> wiringCopy) {
        Set<Module> refreshClosure = new HashSet<>();
        if (initial == null) {
            initial = new HashSet<>();
            Collection<ModuleRevision> removalPending = moduleDatabase.getRemovalPending();
            for (ModuleRevision revision : removalPending) {
                initial.add(revision.getRevisions().getModule());
            }
        }
        for (Module module : initial)
            addDependents(module, wiringCopy, refreshClosure);
        return refreshClosure;
    }

    private static void addDependents(Module module, Map<ModuleRevision, ModuleWiring> wiringCopy,
        Set<Module> refreshClosure) {
        if (refreshClosure.contains(module))
            return;
        refreshClosure.add(module);
        List<ModuleRevision> revisions = module.getRevisions().getModuleRevisions();
        for (ModuleRevision revision : revisions) {
            ModuleWiring wiring = wiringCopy.get(revision);
            if (wiring == null)
                continue;
            List<ModuleWire> provided = wiring.getProvidedModuleWires(null);
            // No null checks; we are holding the read lock here.
            // Add all requirers of the provided wires
            for (ModuleWire providedWire : provided) {
                addDependents(providedWire.getRequirer().getRevisions().getModule(), wiringCopy, refreshClosure);
            }
            // add all hosts of a fragment
            if (revision.getTypes() == BundleRevision.TYPE_FRAGMENT) {
                List<ModuleWire> hosts = wiring.getRequiredModuleWires(HostNamespace.HOST_NAMESPACE);
                for (ModuleWire hostWire : hosts) {
                    addDependents(hostWire.getProvider().getRevisions().getModule(), wiringCopy, refreshClosure);
                }
            }
        }
    }

    Bundle getSystemBundle() {
        Module systemModule = moduleDatabase.getModule(0);
        return systemModule == null ? null : systemModule.getBundle();
    }

    void checkAdminPermission(Bundle bundle, String action) {
        if (bundle == null)
            return;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(new AdminPermission(bundle, action));
    }

    boolean isRefreshingSystemModule() {
        return refreshingSystemModule.get() != null;
    }

    class ContainerWiring
        implements FrameworkWiring, EventDispatcher<ContainerWiring, FrameworkListener[], Collection<Module>> {
        private final Object monitor = new Object();
        private EventManager refreshThread = null;

        @Override
        public Bundle getBundle() {
            return getSystemBundle();
        }

        @Override
        public void refreshBundles(Collection<Bundle> bundles, FrameworkListener... listeners) {
            checkAdminPermission(getBundle(), AdminPermission.RESOLVE);
            Collection<Module> modules = getModules(bundles);

            // queue to refresh in the background
            // notice that we only do one refresh operation at a time
            CopyOnWriteIdentityMap<ContainerWiring, FrameworkListener[]> dispatchListeners
                = new CopyOnWriteIdentityMap<>();
            dispatchListeners.put(this, listeners);
            ListenerQueue<ContainerWiring, FrameworkListener[], Collection<Module>> queue
                = new ListenerQueue<>(getManager());
            queue.queueListeners(dispatchListeners.entrySet(), this);

            // dispatch the refresh job
            queue.dispatchEventAsynchronous(0, modules);
        }

        @Override
        public boolean resolveBundles(Collection<Bundle> bundles) {
            checkAdminPermission(getBundle(), AdminPermission.RESOLVE);
            Collection<Module> modules = getModules(bundles);
            resolve(modules, false);

            if (modules == null) {
                modules = ModuleContainer.this.getModules();
            }
            for (Module module : modules) {
                if (getWiring(module.getCurrentRevision()) == null)
                    return false;
            }
            return true;
        }

        @Override
        public Collection<Bundle> getRemovalPendingBundles() {
            moduleDatabase.readLock();
            try {
                Collection<Bundle> removalPendingBundles = new HashSet<>();
                Collection<ModuleRevision> removalPending = moduleDatabase.getRemovalPending();
                for (ModuleRevision moduleRevision : removalPending) {
                    removalPendingBundles.add(moduleRevision.getBundle());
                }
                return removalPendingBundles;
            } finally {
                moduleDatabase.readUnlock();
            }
        }

        @Override
        public Collection<Bundle> getDependencyClosure(Collection<Bundle> bundles) {
            Collection<Module> modules = getModules(bundles);
            moduleDatabase.readLock();
            try {
                Collection<Module> closure = getRefreshClosure(modules, moduleDatabase.getWiringsCopy());
                Collection<Bundle> result = new ArrayList<>(closure.size());
                for (Module module : closure) {
                    result.add(module.getBundle());
                }
                return result;
            } finally {
                moduleDatabase.readUnlock();
            }
        }

        @Override
        public Collection<BundleCapability> findProviders(Requirement requirement) {
            return InternalUtils.asList(moduleDatabase.findCapabilities(requirement));
        }

        private Collection<Module> getModules(final Collection<Bundle> bundles) {
            if (bundles == null)
                return null;
            return AccessController.doPrivileged((PrivilegedAction<Collection<Module>>) () -> {
                Collection<Module> result = new ArrayList<>(bundles.size());
                for (Bundle bundle : bundles) {
                    Module module = bundle.adapt(Module.class);
                    if (module == null)
                        throw new IllegalStateException("Could not adapt a bundle to a module. " + bundle); //$NON-NLS-1$
                    result.add(module);
                }
                return result;
            });
        }

        private EventManager getManager() {
            synchronized (monitor) {
                if (refreshThread == null) {
                    refreshThread = new EventManager("Refresh Thread: " + adaptor.toString()); //$NON-NLS-1$
                }
                return refreshThread;
            }
        }

    }

    class ContainerStartLevel implements FrameworkStartLevel, EventDispatcher<Module, FrameworkListener[], Integer> {
        private static final int FRAMEWORK_STARTLEVEL = 1;
        private static final int MODULE_STARTLEVEL = 2;
        private final AtomicInteger activeStartLevel = new AtomicInteger(0);
        private final Object eventManagerLock = new Object();
        private EventManager startLevelThread = null;

        @Override
        public Bundle getBundle() {
            return getSystemBundle();
        }

        @Override
        public int getStartLevel() {
            return activeStartLevel.get();
        }

        void setStartLevel(Module module, int startlevel) {
            checkAdminPermission(module.getBundle(), AdminPermission.EXECUTE);
            if (module.getId() == 0) {
                throw new IllegalArgumentException(Msg.ModuleContainer_SystemStartLevelError);
            }
            if (startlevel < 1) {
                throw new IllegalArgumentException(Msg.ModuleContainer_NegativeStartLevelError + startlevel);
            }
            int currentLevel = module.getStartLevel();
            if (currentLevel == startlevel) {
                return; // do nothing
            }
            moduleDatabase.setStartLevel(module, startlevel);
            // only queue the start level if
            // 1) the current level is less than the new startlevel, may need to stop or
            // 2) the module is marked for persistent activation, may need to start
            if (currentLevel < startlevel || module.isPersistentlyStarted()) {
                // queue start level operation in the background
                // notice that we only do one start level operation at a time
                CopyOnWriteIdentityMap<Module, FrameworkListener[]> dispatchListeners = new CopyOnWriteIdentityMap<>();
                dispatchListeners.put(module, new FrameworkListener[0]);
                ListenerQueue<Module, FrameworkListener[], Integer> queue = new ListenerQueue<>(getManager());
                queue.queueListeners(dispatchListeners.entrySet(), this);

                // dispatch the start level job
                queue.dispatchEventAsynchronous(MODULE_STARTLEVEL, startlevel);
            }
        }

        @Override
        public void setStartLevel(int startlevel, FrameworkListener... listeners) {
            checkAdminPermission(getBundle(), AdminPermission.STARTLEVEL);
            if (startlevel < 1) {
                throw new IllegalArgumentException(Msg.ModuleContainer_NegativeStartLevelError + startlevel);
            }

            if (activeStartLevel.get() == 0) {
                throw new IllegalStateException(Msg.ModuleContainer_SystemNotActiveError);
            }
            // queue start level operation in the background
            // notice that we only do one start level operation at a time
            CopyOnWriteIdentityMap<Module, FrameworkListener[]> dispatchListeners = new CopyOnWriteIdentityMap<>();
            dispatchListeners.put(moduleDatabase.getModule(0), listeners);
            ListenerQueue<Module, FrameworkListener[], Integer> queue = new ListenerQueue<>(getManager());
            queue.queueListeners(dispatchListeners.entrySet(), this);

            // dispatch the start level job
            queue.dispatchEventAsynchronous(FRAMEWORK_STARTLEVEL, startlevel);
        }

        @Override
        public int getInitialBundleStartLevel() {
            return moduleDatabase.getInitialModuleStartLevel();
        }

        @Override
        public void setInitialBundleStartLevel(int startlevel) {
            checkAdminPermission(getBundle(), AdminPermission.STARTLEVEL);
            if (startlevel < 1) {
                throw new IllegalArgumentException(Msg.ModuleContainer_NegativeStartLevelError + startlevel);
            }
            moduleDatabase.setInitialModuleStartLevel(startlevel);
        }

        private EventManager getManager() {
            synchronized (eventManagerLock) {
                if (startLevelThread == null) {
                    startLevelThread = new EventManager("Start Level: " + adaptor.toString()); //$NON-NLS-1$
                }
                return startLevelThread;
            }
        }

    }
}
