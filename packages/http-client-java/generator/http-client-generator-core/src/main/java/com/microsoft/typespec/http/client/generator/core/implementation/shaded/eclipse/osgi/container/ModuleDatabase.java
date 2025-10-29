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

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.Module.Settings;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.ModuleContainerAdaptor.ContainerEvent;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.container.Capabilities;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.container.ComputeNodeOrder;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.BundleException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Version;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.namespace.PackageNamespace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.resource.Namespace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.resource.Requirement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.resource.Wire;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.resolver.Resolver;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * A database for storing modules, their revisions and wiring states. The
 * database is responsible for assigning ids and providing access to the
 * capabilities provided by the revisions currently installed as well as the
 * wiring states.
 * <p>
 * <strong>Concurrent Semantics</strong><br />
 *
 * Implementations must be thread safe. The database allows for concurrent read
 * operations and all read operations are protected by the {@link #readLock()
 * read} lock. All write operations are protected by the {@link #writeLock()
 * write} lock. The read and write locks are reentrant and follow the semantics
 * of the {@link ReentrantReadWriteLock}. Just like the
 * {@code ReentrantReadWriteLock} the lock on a database can not be upgraded
 * from a read to a write. Doing so will result in an
 * {@link IllegalMonitorStateException} being thrown. This is behavior is
 * different from the {@code ReentrantReadWriteLock} which results in a deadlock
 * if an attempt is made to upgrade from a read to a write lock.
 * <p>
 * A database is associated with a {@link ModuleContainer container}. The
 * container associated with a database provides public API for manipulating the
 * modules and their wiring states. For example, installing, updating,
 * uninstalling, resolving and unresolving modules. Except for the
 * {@link #load(DataInputStream)}, all other methods that perform write
 * operations are intended to be used by the associated container.
 * 
 * @since 3.10
 */
public class ModuleDatabase {
    /**
     * The adaptor for this database
     */
    final ModuleContainerAdaptor adaptor;

    /**
     * A map of modules by location.
     */
    private final Map<String, Module> modulesByLocations;

    /**
     * A map of modules by id.
     */
    private final Map<Long, Module> modulesById;

    /**
     * A map of revision wiring objects.
     */
    final Map<ModuleRevision, ModuleWiring> wirings;

    /**
     * Holds the next id to be assigned to a module when it is installed
     */
    final AtomicLong nextId;

    /**
     * Holds the current timestamp for revisions of this database.
     */
    final AtomicLong revisionsTimeStamp;

    /**
     * Holds the current timestamp for all changes to this database. This includes
     * changes to revisions and changes to module settings.
     */
    final AtomicLong allTimeStamp;

    /**
     * Holds the construction time which is used to check for empty database on
     * load. This is necessary to ensure the loaded database is consistent with what
     * was persisted.
     */
    final long constructionTime;

    private final Capabilities capabilities;

    /**
     * A map of module settings keyed by module id.
     */
    final Map<Long, EnumSet<Settings>> moduleSettings;

    /**
     * The initial module start level.
     */
    private int initialModuleStartLevel = 1;

    /**
     * Monitors read and write access to this database
     */
    private final ReentrantReadWriteLock monitor = new ReentrantReadWriteLock(false);

    static enum Sort {
        BY_DEPENDENCY, BY_START_LEVEL, BY_ID;

        /**
         * Tests if this option is contained in the specified options
         */
        public boolean isContained(Sort... options) {
            for (Sort option : options) {
                if (equals(option)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Constructs a new empty database.
     * 
     * @param adaptor the module container adaptor
     */
    public ModuleDatabase(ModuleContainerAdaptor adaptor) {
        this.adaptor = adaptor;
        this.modulesByLocations = new HashMap<>();
        this.modulesById = new HashMap<>();
        this.wirings = new HashMap<>();
        // Start at id 1 because 0 is reserved for the system bundle
        this.nextId = new AtomicLong(1);
        // seed with current time to avoid duplicate timestamps after using -clean
        this.constructionTime = System.currentTimeMillis();
        this.revisionsTimeStamp = new AtomicLong(constructionTime);
        this.allTimeStamp = new AtomicLong(constructionTime);
        this.moduleSettings = new HashMap<>();
        this.capabilities = new Capabilities();
    }

    /**
     * Returns the module at the given location or null if no module exists at the
     * given location.
     * <p>
     * A read operation protected by the {@link #readLock() read} lock.
     * 
     * @param location the location of the module.
     * @return the module at the given location or null.
     */
    final Module getModule(String location) {
        readLock();
        try {
            return modulesByLocations.get(location);
        } finally {
            readUnlock();
        }
    }

    /**
     * Returns the module at the given id or null if no module exists at the given
     * location.
     * <p>
     * A read operation protected by the {@link #readLock() read} lock.
     * 
     * @param id the id of the module.
     * @return the module at the given id or null.
     */
    final Module getModule(long id) {
        readLock();
        try {
            return modulesById.get(id);
        } finally {
            readUnlock();
        }
    }

    /**
     * Gets all revisions with a removal pending wiring.
     * <p>
     * A read operation protected by the {@link #readLock() read} lock.
     * 
     * @return all revisions with a removal pending wiring.
     */
    final Collection<ModuleRevision> getRemovalPending() {
        Collection<ModuleRevision> removalPending = new ArrayList<>();
        readLock();
        try {
            for (ModuleWiring wiring : wirings.values()) {
                if (!wiring.isCurrent())
                    removalPending.add(wiring.getRevision());
            }
        } finally {
            readUnlock();
        }
        return removalPending;
    }

    /**
     * Returns the current wiring for the specified revision or null of no wiring
     * exists for the revision.
     * <p>
     * A read operation protected by the {@link #readLock() read} lock.
     * 
     * @param revision the revision to get the wiring for
     * @return the current wiring for the specified revision.
     */
    final ModuleWiring getWiring(ModuleRevision revision) {
        readLock();
        try {
            return wirings.get(revision);
        } finally {
            readUnlock();
        }
    }

    /**
     * Returns a snapshot of the wirings for all revisions. This performs a shallow
     * copy of each entry in the wirings map.
     * <p>
     * A read operation protected by the {@link #readLock() read} lock.
     * 
     * @return a snapshot of the wirings for all revisions.
     */
    final Map<ModuleRevision, ModuleWiring> getWiringsCopy() {
        readLock();
        try {
            return new HashMap<>(wirings);
        } finally {
            readUnlock();
        }
    }

    /**
     * Returns a cloned snapshot of the wirings of all revisions. This performs a
     * clone of each {@link ModuleWiring}. The {@link ModuleWiring#getRevision()
     * revision}, {@link ModuleWiring#getModuleCapabilities(String) capabilities},
     * {@link ModuleWiring#getModuleRequirements(String) requirements},
     * {@link ModuleWiring#getProvidedModuleWires(String) provided wires},
     * {@link ModuleWiring#getRequiredModuleWires(String) required wires}, and
     * {@link ModuleWiring#getSubstitutedNames()} of each wiring are copied into a
     * cloned copy of the wiring.
     * <p>
     * The returned map of wirings may be safely read from while not holding any
     * read or write locks on this database. This is useful for doing
     * {@link Resolver#resolve(com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.resolver.ResolveContext)
     * resolve}
     * operations without holding the read or write lock on this database.
     * <p>
     * A read operation protected by the {@link #readLock() read} lock.
     * 
     * @return a cloned snapshot of the wirings of all revisions.
     */
    final Map<ModuleRevision, ModuleWiring> getWiringsClone() {
        readLock();
        try {
            Map<ModuleRevision, ModuleWiring> clonedWirings = new HashMap<>(wirings);
            clonedWirings.replaceAll((r, w) -> new ModuleWiring(r, w.getCapabilities(), w.getRequirements(),
                w.getProvidedWires(), w.getRequiredWires(), w.getSubstitutedNames()));
            return clonedWirings;
        } finally {
            readUnlock();
        }
    }

    /**
     * Adds all the values from the specified delta wirings to the wirings current
     * wirings
     * <p>
     * A write operation protected by the {@link #writeLock() write} lock.
     * 
     * @param deltaWiring the new wiring values to take effect. The values from the
     * delta wiring are copied.
     */
    final void mergeWiring(Map<ModuleRevision, ModuleWiring> deltaWiring) {
        writeLock();
        try {
            wirings.putAll(deltaWiring);
            incrementTimestamps(true);
        } finally {
            writeUnlock();
        }
    }

    /**
     * Returns a snapshot of all modules ordered by module ID.
     * <p>
     * A read operation protected by the {@link #readLock() read} lock.
     * 
     * @return a snapshot of all modules.
     */
    final List<Module> getModules() {
        return getSortedModules();
    }

    /**
     * Returns a snapshot of all modules ordered according to the sort options
     * 
     * @param sortOptions options for sorting
     * @return a snapshot of all modules ordered according to the sort options
     */
    final List<Module> getSortedModules(Sort... sortOptions) {
        readLock();
        try {
            List<Module> modules = new ArrayList<>(modulesByLocations.values());
            sortModules(modules, sortOptions);
            return modules;
        } finally {
            readUnlock();
        }
    }

    final void sortModules(List<Module> modules, Sort... sortOptions) {
        if (modules.size() < 2)
            return;
        if (sortOptions == null || Sort.BY_ID.isContained(sortOptions) || sortOptions.length == 0) {
            modules.sort(Comparator.comparing(Module::getId));
            return;
        }
        // first sort by start-level
        if (Sort.BY_START_LEVEL.isContained(sortOptions)) {
            Collections.sort(modules);
        }
        if (Sort.BY_DEPENDENCY.isContained(sortOptions)) {
            if (Sort.BY_START_LEVEL.isContained(sortOptions)) {
                // sort each sublist that has modules of the same start level
                int currentSL = modules.get(0).getStartLevel();
                int currentSLindex = 0;
                boolean lazy = false;
                for (int i = 0; i < modules.size(); i++) {
                    Module module = modules.get(i);
                    if (currentSL != module.getStartLevel()) {
                        if (lazy)
                            sortByDependencies(modules.subList(currentSLindex, i));
                        currentSL = module.getStartLevel();
                        currentSLindex = i;
                        lazy = false;
                    }
                    lazy |= module.isLazyActivate();
                }
                // sort the last set of bundles
                if (lazy)
                    sortByDependencies(modules.subList(currentSLindex, modules.size()));
            } else {
                // sort the whole list by dependency
                sortByDependencies(modules);
            }
        }
    }

    private Collection<List<Module>> sortByDependencies(List<Module> toSort) {
        // Build references so we can sort
        List<Module[]> references = new ArrayList<>(toSort.size());
        for (Module module : toSort) {
            ModuleRevision current = module.getCurrentRevision();
            if (current == null) {
                continue;
            }
            ModuleWiring wiring = current.getWiring();
            if (wiring == null) {
                continue;
            }
            // No null check; we are holding the database lock here.
            for (ModuleWire wire : wiring.getRequiredModuleWires(null)) {
                ModuleRequirement req = wire.getRequirement();
                // Add all requirements that are not package requirements.
                // Only add package requirements that are not dynamic
                // TODO may want to consider only adding package, bundle and host requirements,
                // other generic requirement are not that interesting
                if (!PackageNamespace.PACKAGE_NAMESPACE.equals(req.getNamespace())
                    || !PackageNamespace.RESOLUTION_DYNAMIC
                        .equals(req.getDirectives().get(Namespace.REQUIREMENT_RESOLUTION_DIRECTIVE))) {
                    references.add(new Module[] {
                        wire.getRequirer().getRevisions().getModule(),
                        wire.getProvider().getRevisions().getModule() });
                }
            }
        }

        // Sort an array using the references
        Module[] sorted = toSort.toArray(new Module[toSort.size()]);
        Object[][] cycles
            = ComputeNodeOrder.computeNodeOrder(sorted, references.toArray(new Module[references.size()][]));

        // Apply the sorted array to the list
        toSort.clear();
        toSort.addAll(Arrays.asList(sorted));

        if (cycles.length == 0)
            return Collections.emptyList();

        Collection<List<Module>> moduleCycles = new ArrayList<>(cycles.length);
        for (Object[] cycle : cycles) {
            List<Module> moduleCycle = new ArrayList<>(cycle.length);
            for (Object module : cycle) {
                moduleCycle.add((Module) module);
            }
            moduleCycles.add(moduleCycle);
        }
        return moduleCycles;
    }

    private void checkWrite() {
        if (monitor.getWriteHoldCount() == 0)
            throw new IllegalMonitorStateException("Must hold the write lock."); //$NON-NLS-1$
    }

    /**
     * returns the next module ID.
     * <p>
     * A read operation protected by the {@link #readLock() read} lock.
     * 
     * @return the next module ID
     */
    public final long getNextId() {
        readLock();
        try {
            return nextId.get();
        } finally {
            readUnlock();
        }
    }

    /**
     * Returns the current timestamp for the revisions of this database. The
     * timestamp is incremented any time a modification is made to the revisions in
     * this database. For example:
     * <ul>
     * <li>installing a module
     * <li>updating a module
     * <li>uninstalling a module
     * <li>modifying the wirings
     * </ul>
     * <p>
     * A read operation protected by the {@link #readLock() read} lock.
     * 
     * @return the current timestamp of this database.
     */
    final public long getRevisionsTimestamp() {
        readLock();
        try {
            return revisionsTimeStamp.get();
        } finally {
            readUnlock();
        }
    }

    /**
     * Returns the current timestamp for this database. The timestamp is incremented
     * any time a modification is made to this database. This includes the
     * modifications described in {@link #getRevisionsTimestamp() revisions
     * timestamp} and the following modifications related to modules:
     * <ul>
     * <li>modifying the initial module start level
     * <li>modifying a module start level
     * <li>modifying a module settings
     * </ul>
     * <p>
     * A read operation protected by the {@link #readLock() read} lock.
     * 
     * @return the current timestamp of this database.
     */
    final public long getTimestamp() {
        readLock();
        try {
            return allTimeStamp.get();
        } finally {
            readUnlock();
        }
    }

    /**
     * Increments the timestamps of this database.
     * 
     * @param incrementRevision indicates if the revision timestamp should change
     */
    private void incrementTimestamps(boolean incrementRevision) {
        // sanity check
        checkWrite();
        if (incrementRevision) {
            revisionsTimeStamp.incrementAndGet();
        }
        allTimeStamp.incrementAndGet();
        adaptor.updatedDatabase();
    }

    /**
     * Acquires the read lock for this database.
     * 
     * @see ReadLock#lock()
     */
    public final void readLock() {
        monitor.readLock().lock();
    }

    /**
     * Acquires the write lock for this database. Same as {@link WriteLock#lock()}
     * except an illegal state exception is thrown if the current thread holds one
     * or more read locks.
     * 
     * @see WriteLock#lock()
     * @throws IllegalMonitorStateException if the current thread holds one or more
     * read locks.
     */
    public final void writeLock() {
        if (monitor.getReadHoldCount() > 0) {
            // this is not supported and will cause deadlock if allowed to proceed.
            // fail fast instead of deadlocking
            throw new IllegalMonitorStateException("Requesting upgrade to write lock."); //$NON-NLS-1$
        }
        monitor.writeLock().lock();
    }

    /**
     * Attempts to release the read lock for this database.
     * 
     * @see ReadLock#unlock()
     */
    public final void readUnlock() {
        monitor.readLock().unlock();
    }

    /**
     * Attempts to release the write lock for this database.
     * 
     * @see WriteLock#unlock()
     */
    public final void writeUnlock() {
        monitor.writeLock().unlock();
    }

    /**
     * Returns a mutable snapshot of capabilities that are candidates for satisfying
     * the specified requirement.
     * <p>
     * A read operation protected by the {@link #readLock() read} lock. Implementers
     * of this method should acquire the read lock while finding capabilities.
     * 
     * @param requirement the requirement
     * @return the candidates for the requirement
     */
    final List<ModuleCapability> findCapabilities(Requirement requirement) {
        readLock();
        try {
            return capabilities.findCapabilities(requirement);
        } finally {
            readUnlock();
        }
    }

    /**
     * Writes this database in a format suitable for using the
     * {@link #load(DataInputStream)} method. All modules are stored which have a
     * current {@link ModuleRevision revision}. Only the current revision of each
     * module is stored (no removal pending revisions are stored). Optionally the
     * {@link ModuleWiring wiring} of each current revision may be stored. Wiring
     * can only be stored if there are no {@link #getRemovalPending() removal
     * pending} revisions.
     * <p>
     * This method acquires the {@link #readLock() read} lock while writing this
     * database.
     * <p>
     * After this database have been written, the output stream is flushed. The
     * output stream remains open after this method returns.
     * 
     * @param out the data output steam.
     * @param persistWirings true if wirings should be persisted. This option will
     * be ignored if there are {@link #getRemovalPending()
     * removal pending} revisions.
     * @throws IOException if writing this database to the specified output stream
     * throws an IOException
     */
    public final void store(DataOutputStream out, boolean persistWirings) throws IOException {
        readLock();
        try {
            Persistence.store(this, out, persistWirings);
        } finally {
            readUnlock();
        }
    }

    final void persistSettings(EnumSet<Settings> settings, Module module) {
        writeLock();
        try {
            EnumSet<Settings> existing = moduleSettings.get(module.getId());
            if (!settings.equals(existing)) {
                moduleSettings.put(module.getId(), EnumSet.copyOf(settings));
                incrementTimestamps(false);
            }
        } finally {
            writeUnlock();
        }
    }

    final void setStartLevel(Module module, int startlevel) {
        writeLock();
        try {
            module.checkValid();
            module.storeStartLevel(startlevel);
            incrementTimestamps(false);
        } finally {
            writeUnlock();
        }
    }

    final int getInitialModuleStartLevel() {
        readLock();
        try {
            return this.initialModuleStartLevel;
        } finally {
            readUnlock();
        }
    }

    final void setInitialModuleStartLevel(int initialStartlevel) {
        writeLock();
        try {
            this.initialModuleStartLevel = initialStartlevel;
            incrementTimestamps(false);
        } finally {
            writeUnlock();
        }
    }

    private static class Persistence {
        private static final int VERSION = 3;
        private static final byte NULL = 0;
        private static final byte OBJECT = 1;
        private static final byte INDEX = 2;
        private static final byte LONG_STRING = 3;

        private static final byte VALUE_STRING = 0;
        // REMOVED treated as List<String> - private static final byte
        // VALUE_STRING_ARRAY = 1;
        // REMOVED never was really supported by the OSGi builder - private static final
        // byte VAlUE_BOOLEAN = 2;
        // REMOVED never was really supported by the OSGi builder - private static final
        // byte VALUE_INTEGER = 3;
        private static final byte VALUE_LONG = 4;
        private static final byte VALUE_DOUBLE = 5;
        private static final byte VALUE_VERSION = 6;
        // REMOVED treated as type String - private static final byte VALUE_URI = 7;
        private static final byte VALUE_LIST = 8;

        private static int addToWriteTable(Object object, Map<Object, Integer> objectTable) {
            if (object == null)
                throw new NullPointerException();
            Integer cur = objectTable.get(object);
            if (cur != null)
                throw new IllegalStateException("Object is already in the write table: " + object); //$NON-NLS-1$
            objectTable.put(object, objectTable.size());
            // return the index of the object just added (i.e. size - 1)
            return (objectTable.size() - 1);
        }

        public static void store(ModuleDatabase moduleDatabase, DataOutputStream out, boolean persistWirings)
            throws IOException {
            out.writeInt(VERSION);
            out.writeLong(moduleDatabase.getRevisionsTimestamp());
            out.writeLong(moduleDatabase.getTimestamp());
            out.writeLong(moduleDatabase.getNextId());
            out.writeInt(moduleDatabase.getInitialModuleStartLevel());

            // prime the object table with all the strings, versions and maps
            Set<String> allStrings = new HashSet<>();
            Set<Version> allVersions = new HashSet<>();
            Set<Map<String, ?>> allMaps = new HashSet<>();

            // first gather all the strings, versions and maps from the modules
            List<Module> modules = moduleDatabase.getModules();
            for (Module module : modules) {
                getStringsVersionsAndMaps(module, moduleDatabase, allStrings, allVersions, allMaps);
            }
            // outside of the modules the wirings have 'substituted' packages strings
            Map<ModuleRevision, ModuleWiring> wirings = moduleDatabase.wirings;
            for (ModuleWiring wiring : wirings.values()) {
                Collection<String> substituted = wiring.getSubstitutedNames();
                allStrings.addAll(substituted);
            }

            // Now persist all the Strings
            Map<Object, Integer> objectTable = new HashMap<>();
            allStrings.remove(null);
            out.writeInt(allStrings.size());
            for (String string : allStrings) {
                writeString(string, out, objectTable);
                out.writeInt(addToWriteTable(string, objectTable));
            }
            // Followed by versions which may reference strings with their qualifier
            out.writeInt(allVersions.size());
            for (Version version : allVersions) {
                writeVersion(version, out, objectTable);
                out.writeInt(addToWriteTable(version, objectTable));
            }
            // Followed by maps which may reference the strings and versions
            out.writeInt(allMaps.size());
            for (Map<String, ?> map : allMaps) {
                writeMap(map, out, objectTable, moduleDatabase);
                out.writeInt(addToWriteTable(map, objectTable));
            }

            // Followed by modules which reference the strings, versions, and maps
            out.writeInt(modules.size());
            for (Module module : modules) {
                writeModule(module, moduleDatabase, out, objectTable);
            }

            Collection<ModuleRevision> removalPendings = moduleDatabase.getRemovalPending();
            // only persist wirings if there are no removals pending
            persistWirings &= removalPendings.isEmpty();
            out.writeBoolean(persistWirings);
            if (!persistWirings) {
                return;
            }

            // prime the object table with all the required wires which reference the
            // modules
            out.writeInt(wirings.size());
            for (ModuleWiring wiring : wirings.values()) {
                List<ModuleWire> requiredWires = wiring.getPersistentRequiredWires();
                out.writeInt(requiredWires.size());
                for (ModuleWire wire : requiredWires) {
                    writeWire(wire, out, objectTable);
                }
            }

            // now write all the info about each wiring using only indexes from the
            // objectTable
            for (ModuleWiring wiring : wirings.values()) {
                writeWiring(wiring, out, objectTable);
            }

            out.flush();
        }

        private static void getStringsVersionsAndMaps(Module module, ModuleDatabase moduleDatabase,
            Set<String> allStrings, Set<Version> allVersions, Set<Map<String, ?>> allMaps) {
            ModuleRevision current = module.getCurrentRevision();
            if (current == null)
                return;

            allStrings.add(module.getLocation());
            allStrings.add(current.getSymbolicName());
            allStrings.add(current.getVersion().getQualifier());
            allVersions.add(current.getVersion());
            EnumSet<Settings> settings = moduleDatabase.moduleSettings.get(module.getId());
            if (settings != null) {
                for (Settings setting : settings) {
                    allStrings.add(setting.toString());
                }
            }

            List<ModuleCapability> capabilities = current.getModuleCapabilities(null);
            for (ModuleCapability capability : capabilities) {
                allStrings.add(capability.getNamespace());
                addMap(capability.getPersistentAttributes(), allStrings, allVersions, allMaps);
                addMap(capability.getDirectives(), allStrings, allVersions, allMaps);
            }

            List<ModuleRequirement> requirements = current.getModuleRequirements(null);
            for (ModuleRequirement requirement : requirements) {
                allStrings.add(requirement.getNamespace());
                addMap(requirement.getAttributes(), allStrings, allVersions, allMaps);
                addMap(requirement.getDirectives(), allStrings, allVersions, allMaps);
            }
        }

        private static void addMap(Map<String, ?> map, Set<String> allStrings, Set<Version> allVersions,
            Set<Map<String, ?>> allMaps) {
            if (!allMaps.add(map)) {
                // map was already added
                return;
            }
            for (Map.Entry<String, ?> entry : map.entrySet()) {
                allStrings.add(entry.getKey());
                Object value = entry.getValue();
                if (value instanceof String) {
                    allStrings.add((String) value);
                } else if (value instanceof Version) {
                    allStrings.add(((Version) value).getQualifier());
                    allVersions.add((Version) value);
                } else if (value instanceof List) {
                    switch (getListType((List<?>) value)) {
                        case VALUE_STRING:
                            for (Object string : (List<?>) value) {
                                allStrings.add((String) string);
                            }
                            break;

                        case VALUE_VERSION:
                            for (Object version : (List<?>) value) {
                                allStrings.add(((Version) version).getQualifier());
                                allVersions.add((Version) version);
                            }
                            break;
                    }
                }
            }
        }

        private static void writeModule(Module module, ModuleDatabase moduleDatabase, DataOutputStream out,
            Map<Object, Integer> objectTable) throws IOException {
            ModuleRevision current = module.getCurrentRevision();
            if (current == null)
                return;
            out.writeInt(addToWriteTable(current, objectTable));

            writeString(module.getLocation(), out, objectTable);
            out.writeLong(module.getId());

            writeString(current.getSymbolicName(), out, objectTable);
            writeVersion(current.getVersion(), out, objectTable);
            out.writeInt(current.getTypes());

            List<ModuleCapability> capabilities = current.getModuleCapabilities(null);
            out.writeInt(capabilities.size());
            for (ModuleCapability capability : capabilities) {
                out.writeInt(addToWriteTable(capability, objectTable));
                writeGenericInfo(capability.getNamespace(), capability.getPersistentAttributes(),
                    capability.getDirectives(), out, objectTable);
            }

            List<Requirement> requirements = current.getRequirements(null);
            out.writeInt(requirements.size());
            for (Requirement requirement : requirements) {
                out.writeInt(addToWriteTable(requirement, objectTable));
                writeGenericInfo(requirement.getNamespace(), requirement.getAttributes(), requirement.getDirectives(),
                    out, objectTable);
            }

            // settings
            EnumSet<Settings> settings = moduleDatabase.moduleSettings.get(module.getId());
            out.writeInt(settings == null ? 0 : settings.size());
            if (settings != null) {
                for (Settings setting : settings) {
                    writeString(setting.name(), out, objectTable);
                }
            }

            // startlevel
            out.writeInt(module.getStartLevel());

            // last modified
            out.writeLong(module.getLastModified());
        }

        private static void writeWire(ModuleWire wire, DataOutputStream out, Map<Object, Integer> objectTable)
            throws IOException {
            Wire w = wire;
            Integer capability = objectTable.get(w.getCapability());
            Integer provider = objectTable.get(w.getProvider());
            Integer requirement = objectTable.get(w.getRequirement());
            Integer requirer = objectTable.get(w.getRequirer());

            if (capability == null || provider == null || requirement == null || requirer == null)
                throw new NullPointerException("Could not find the expected indexes"); //$NON-NLS-1$

            out.writeInt(addToWriteTable(wire, objectTable));

            out.writeInt(capability);
            out.writeInt(provider);
            out.writeInt(requirement);
            out.writeInt(requirer);
        }

        private static void writeWiring(ModuleWiring wiring, DataOutputStream out, Map<Object, Integer> objectTable)
            throws IOException {
            Integer revisionIndex = objectTable.get(wiring.getRevision());
            if (revisionIndex == null)
                throw new NullPointerException("Could not find revision for wiring."); //$NON-NLS-1$
            out.writeInt(revisionIndex);

            List<ModuleCapability> capabilities = wiring.getModuleCapabilities(null);
            out.writeInt(capabilities.size());
            for (ModuleCapability capability : capabilities) {
                Integer capabilityIndex = objectTable.get(capability);
                if (capabilityIndex == null)
                    throw new NullPointerException("Could not find capability for wiring."); //$NON-NLS-1$
                out.writeInt(capabilityIndex);
            }

            List<ModuleRequirement> requirements = wiring.getPersistentRequirements();
            out.writeInt(requirements.size());
            for (ModuleRequirement requirement : requirements) {
                Integer requirementIndex = objectTable.get(requirement);
                if (requirementIndex == null)
                    throw new NullPointerException("Could not find requirement for wiring."); //$NON-NLS-1$
                out.writeInt(requirementIndex);
            }

            List<ModuleWire> providedWires = wiring.getPersistentProvidedWires();
            out.writeInt(providedWires.size());
            for (ModuleWire wire : providedWires) {
                Integer wireIndex = objectTable.get(wire);
                if (wireIndex == null)
                    throw new NullPointerException("Could not find provided wire for wiring."); //$NON-NLS-1$
                out.writeInt(wireIndex);
            }

            List<ModuleWire> requiredWires = wiring.getPersistentRequiredWires();
            out.writeInt(requiredWires.size());
            for (ModuleWire wire : requiredWires) {
                Integer wireIndex = objectTable.get(wire);
                if (wireIndex == null)
                    throw new NullPointerException("Could not find required wire for wiring."); //$NON-NLS-1$
                out.writeInt(wireIndex);
            }

            Collection<String> substituted = wiring.getSubstitutedNames();
            out.writeInt(substituted.size());
            for (String pkgName : substituted) {
                writeString(pkgName, out, objectTable);
            }
        }

        private static void writeGenericInfo(String namespace, Map<String, ?> attributes,
            Map<String, String> directives, DataOutputStream out, Map<Object, Integer> objectTable) throws IOException {
            writeString(namespace, out, objectTable);

            Integer attributesIndex = objectTable.get(attributes);
            Integer directivesIndex = objectTable.get(directives);
            if (attributesIndex == null || directivesIndex == null)
                throw new NullPointerException("Could not find the expected indexes"); //$NON-NLS-1$
            out.writeInt(attributesIndex);
            out.writeInt(directivesIndex);
        }

        private static void writeMap(Map<String, ?> source, DataOutputStream out, Map<Object, Integer> objectTable,
            ModuleDatabase moduleDatabase) throws IOException {
            if (source == null) {
                out.writeInt(0);
            } else {
                out.writeInt(source.size());
                for (String key : source.keySet()) {
                    Object value = source.get(key);
                    writeString(key, out, objectTable);
                    if (value instanceof String) {
                        out.writeByte(VALUE_STRING);
                        writeString((String) value, out, objectTable);
                    } else if (value instanceof Long) {
                        out.writeByte(VALUE_LONG);
                        out.writeLong((Long) value);
                    } else if (value instanceof Double) {
                        out.writeByte(VALUE_DOUBLE);
                        out.writeDouble((Double) value);
                    } else if (value instanceof Version) {
                        out.writeByte(VALUE_VERSION);
                        writeVersion((Version) value, out, objectTable);
                    } else if (value instanceof List) {
                        out.writeByte(VALUE_LIST);
                        writeList(out, key, (List<?>) value, objectTable, moduleDatabase);
                    } else {
                        // do our best and write a string; post an error.
                        // This will be difficult to debug because we don't know which module it is
                        // coming from, but it is better than being silent
                        moduleDatabase.adaptor.publishContainerEvent(ContainerEvent.ERROR, moduleDatabase.getModule(0),
                            new BundleException("Invalid map value: " + key + " = " + value.getClass().getName() //$NON-NLS-1$ //$NON-NLS-2$
                                + '[' + value + ']'));
                        out.writeByte(VALUE_STRING);
                        writeString(String.valueOf(value), out, objectTable);
                    }
                }
            }
        }

        private static void writeList(DataOutputStream out, String key, List<?> list, Map<Object, Integer> objectTable,
            ModuleDatabase moduleDatabase) throws IOException {
            if (list.isEmpty()) {
                out.writeInt(0);
                return;
            }
            byte type = getListType(list);
            if (type == -1) {
                out.writeInt(0);
                return; // don't understand the list type
            }
            out.writeInt(list.size());
            out.writeByte(type == -2 ? VALUE_STRING : type);
            for (Object value : list) {
                switch (type) {
                    case VALUE_STRING:
                        writeString((String) value, out, objectTable);
                        break;

                    case VALUE_LONG:
                        out.writeLong((Long) value);
                        break;

                    case VALUE_DOUBLE:
                        out.writeDouble((Double) value);
                        break;

                    case VALUE_VERSION:
                        writeVersion((Version) value, out, objectTable);
                        break;

                    default:
                        // do our best and write a string; post an error.
                        // This will be difficult to debug because we don't know which module it is
                        // coming from, but it is better than being silent
                        moduleDatabase.adaptor.publishContainerEvent(ContainerEvent.ERROR, moduleDatabase.getModule(0),
                            new BundleException("Invalid list element in map: " + key + " = " //$NON-NLS-1$ //$NON-NLS-2$
                                + value.getClass().getName() + '[' + value + ']'));
                        writeString(String.valueOf(value), out, objectTable);
                        break;
                }
            }
        }

        private static byte getListType(List<?> list) {
            if (list.isEmpty())
                return -1;
            Object type = list.get(0);
            if (type instanceof String)
                return VALUE_STRING;
            if (type instanceof Long)
                return VALUE_LONG;
            if (type instanceof Double)
                return VALUE_DOUBLE;
            if (type instanceof Version)
                return VALUE_VERSION;
            return -2;
        }

        private static void writeVersion(Version version, DataOutputStream out, Map<Object, Integer> objectTable)
            throws IOException {
            if (version == null || version.equals(Version.emptyVersion)) {
                out.writeByte(NULL);
                return;
            }
            Integer index = objectTable.get(version);
            if (index != null) {
                out.writeByte(INDEX);
                out.writeInt(index);
                return;
            }
            out.writeByte(OBJECT);
            out.writeInt(version.getMajor());
            out.writeInt(version.getMinor());
            out.writeInt(version.getMicro());
            writeQualifier(version.getQualifier(), out, objectTable);
        }

        private static void writeQualifier(String string, DataOutputStream out, Map<Object, Integer> objectTable)
            throws IOException {
            if (string != null && string.isEmpty())
                string = null;
            writeString(string, out, objectTable);
        }

        private static void writeString(String string, DataOutputStream out, Map<Object, Integer> objectTable)
            throws IOException {
            Integer index = string != null ? objectTable.get(string) : null;
            if (index != null) {
                out.writeByte(INDEX);
                out.writeInt(index);
                return;
            }

            if (string == null)
                out.writeByte(NULL);
            else {
                byte[] data = string.getBytes(StandardCharsets.UTF_8);

                if (data.length > 65535) {
                    out.writeByte(LONG_STRING);
                    out.writeInt(data.length);
                    out.write(data);
                } else {
                    out.writeByte(OBJECT);
                    out.writeUTF(string);
                }
            }
        }

    }
}
