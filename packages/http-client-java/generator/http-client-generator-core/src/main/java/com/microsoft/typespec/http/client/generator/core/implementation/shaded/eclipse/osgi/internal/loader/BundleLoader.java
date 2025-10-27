/*******************************************************************************
 * Copyright (c) 2004, 2021 IBM Corporation and others.
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

package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.loader;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.Module;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.ModuleCapability;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.ModuleLoader;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.ModuleRequirement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.ModuleRevision;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.ModuleWire;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.ModuleWiring;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.framework.EquinoxConfiguration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.framework.EquinoxContainer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.hookregistry.ClassLoaderHook;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.loader.sources.MultiSourcePackage;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.loader.sources.NullPackageSource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.loader.sources.PackageSource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.loader.sources.SingleSourcePackage;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage.BundleInfo.Generation;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.BundleContext;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.BundleException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.FrameworkEvent;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.namespace.BundleNamespace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.namespace.HostNamespace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.namespace.PackageNamespace;

import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This object is responsible for all classloader delegation for a bundle. It
 * represents the loaded state of the bundle. BundleLoader objects are created
 * lazily; care should be taken not to force the creation of a BundleLoader
 * unless it is necessary.
 * 
 * @see org.eclipse.osgi.internal.loader.BundleLoaderSources
 */
public class BundleLoader extends ModuleLoader {
	public final static String DEFAULT_PACKAGE = "."; //$NON-NLS-1$

	private static final Pattern PACKAGENAME_FILTER = Pattern.compile("\\(osgi.wiring.package\\s*=\\s*([^)]+)\\)"); //$NON-NLS-1$

	private final ModuleWiring wiring;
	private final EquinoxContainer container;

    /* List of package names that are exported by this BundleLoader */
	private final Collection<String> exportedPackages;
	private final BundleLoaderSources exportSources;

	/*
	 * cache of required package sources. Key is packagename, value is PackageSource
	 */
	private final Map<String, PackageSource> requiredSources = new HashMap<>();
	/* cache of imported packages. Key is packagename, Value is PackageSource */
	private final Map<String, PackageSource> importedSources = new HashMap<>();
	private final List<ModuleWire> requiredBundleWires;

	/* @GuardedBy("importedSources") */
	private boolean importsInitialized = false;
	/* @GuardedBy("importedSources") */
	private boolean dynamicAllPackages;
	/* If not null, list of package stems to import dynamically. */
	/* @GuardedBy("importedSources") */
	private String[] dynamicImportPackageStems;
	/* @GuardedBy("importedSources") */
	/* If not null, list of package names to import dynamically. */
	private String[] dynamicImportPackages;

	private final Object classLoaderCreatedMonitor = new Object();
	/* @GuardedBy("classLoaderCreatedMonitor") */
	private ModuleClassLoader classLoaderCreated;
	private volatile ModuleClassLoader classloader;
	private final ClassLoader parent;
	private final AtomicBoolean triggerClassLoaded = new AtomicBoolean(false);
	private final AtomicBoolean firstUseOfInvalidLoader = new AtomicBoolean(false);

	/**
	 * Returns the package name from the specified class name. The returned package
	 * is dot seperated.
	 *
	 * @param name Name of a class.
	 * @return Dot separated package name or null if the class has no package name.
	 */
	public final static String getPackageName(String name) {
		if (name != null) {
			int index = name.lastIndexOf('.'); /* find last period in class name */
			if (index > 0)
				return name.substring(0, index);
		}
		return DEFAULT_PACKAGE;
	}

    public BundleLoader(ModuleWiring wiring, EquinoxContainer container, ClassLoader parent) {
		this.wiring = wiring;
		this.container = container;
        this.parent = parent;

		// init the provided packages set
		exportSources = new BundleLoaderSources(this);
		List<ModuleCapability> exports = wiring.getModuleCapabilities(PackageNamespace.PACKAGE_NAMESPACE);
		exports = exports == null ? Collections.emptyList() : exports;
		exportedPackages = Collections.synchronizedCollection(
				exports.size() > 10 ? new HashSet<String>(exports.size()) : new ArrayList<String>(exports.size()));
		initializeExports(exports, exportSources, exportedPackages);

		// init the dynamic imports tables
		addDynamicImportPackage(wiring.getModuleRequirements(PackageNamespace.PACKAGE_NAMESPACE));

		// initialize the required bundle wires
		List<ModuleWire> currentRequireBundleWires = wiring.getRequiredModuleWires(BundleNamespace.BUNDLE_NAMESPACE);
		requiredBundleWires = currentRequireBundleWires == null || currentRequireBundleWires.isEmpty()
				? Collections.emptyList()
				: Collections.unmodifiableList(currentRequireBundleWires);
    }

	public ModuleWiring getWiring() {
		return wiring;
	}

	public void addFragmentExports(List<ModuleCapability> exports) {
		initializeExports(exports, exportSources, exportedPackages);
	}

	private static void initializeExports(List<ModuleCapability> exports, BundleLoaderSources sources,
			Collection<String> exportNames) {
		if (exports != null) {
			for (ModuleCapability export : exports) {
				String name = (String) export.getAttributes().get(PackageNamespace.PACKAGE_NAMESPACE);
				if (sources.forceSourceCreation(export)) {
					if (!exportNames.contains(name)) {
						// must force filtered and reexport sources to be created early
						// to prevent lazy normal package source creation.
						// We only do this for the first export of a package name.
						sources.createPackageSource(export, true);
					}
				}
				exportNames.add(name);
			}
		}
	}

	final PackageSource createExportPackageSource(ModuleWire importWire, Collection<BundleLoader> visited) {
		String name = (String) importWire.getCapability().getAttributes().get(PackageNamespace.PACKAGE_NAMESPACE);
		BundleLoader providerLoader = getProviderLoader(importWire);
		if (providerLoader == null) {
			return createMultiSource(name, new PackageSource[0]);
		}
		PackageSource requiredSource = providerLoader.findRequiredSource(name, visited);
		PackageSource exportSource = providerLoader.exportSources.createPackageSource(importWire.getCapability(),
				false);
		if (requiredSource == null)
			return exportSource;
		return createMultiSource(name, new PackageSource[] { requiredSource, exportSource });
	}

	private static PackageSource createMultiSource(String packageName, PackageSource[] sources) {
		if (sources.length == 1)
			return sources[0];
		List<SingleSourcePackage> sourceList = new ArrayList<>(sources.length);
		for (PackageSource source : sources) {
			SingleSourcePackage[] innerSources = source.getSuppliers();
			for (SingleSourcePackage innerSource : innerSources) {
				if (!sourceList.contains(innerSource)) {
					sourceList.add(innerSource);
				}
			}
		}
		return new MultiSourcePackage(packageName, sourceList.toArray(new SingleSourcePackage[0]));
	}

	public ModuleClassLoader getModuleClassLoader() {
		ModuleClassLoader result = classloader;
		if (result != null) {
			return result;
		}
		// doing optimistic class loader creating here to avoid holding any locks,
		// this may result in multiple classloaders being constructed but only one will
		// be used.
		final List<ClassLoaderHook> hooks = container.getConfiguration().getHookRegistry().getClassLoaderHooks();
		final Generation generation = (Generation) wiring.getRevision().getRevisionInfo();
		if (System.getSecurityManager() == null) {
			result = createClassLoaderPrivledged(parent, generation.getBundleInfo().getStorage().getConfiguration(),
					this, generation, hooks);
		} else {
			final ClassLoader cl = parent;
			result = AccessController.doPrivileged(
                (PrivilegedAction<ModuleClassLoader>) () -> createClassLoaderPrivledged(cl, generation.getBundleInfo().getStorage().getConfiguration(),
                        BundleLoader.this, generation, hooks));
		}

		// Synchronize on classLoaderCreatedMonitor in order to ensure hooks are called
		// before returning.
		// Note that we do hold a lock here while calling hooks.
		// Not ideal, but hooks really should do little work from classLoaderCreated
		// method.
		synchronized (classLoaderCreatedMonitor) {
			if (classLoaderCreated == null) {
				// must set createdClassloader before calling hooks; otherwise we could enter
				// and endless loop if the hook causes re-entry (that would be a bad hook impl)
				classLoaderCreated = result;
				// only send to hooks if this thread wins in creating the class loader.
				final ModuleClassLoader cl = result;
				// protect with doPriv to avoid bubbling up permission checks that hooks may
				// require
				AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                    for (ClassLoaderHook hook : hooks) {
                        hook.classLoaderCreated(cl);
                    }
                    return null;
                });
				// finally set the class loader for use after calling hooks
				classloader = classLoaderCreated;
			} else {
				// return the classLoaderCreated here; not the final classloader
				// this is necessary in case re-entry by a hook.classLoaderCreated method
				result = classLoaderCreated;
			}
		}
		return result;
	}

	@Override
	protected void loadFragments(Collection<ModuleRevision> fragments) {
		addFragmentExports(wiring.getModuleCapabilities(PackageNamespace.PACKAGE_NAMESPACE));
		loadClassLoaderFragments(fragments);
		clearManifestLocalizationCache();
	}

	protected void clearManifestLocalizationCache() {
		Generation hostGen = (Generation) wiring.getRevision().getRevisionInfo();
		hostGen.clearManifestCache();
		List<ModuleWire> hostWires = wiring.getProvidedModuleWires(HostNamespace.HOST_NAMESPACE);
		// doing a null check because there is no lock on the module database here
		if (hostWires != null) {
			for (ModuleWire fragmentWire : hostWires) {
				Generation fragGen = (Generation) fragmentWire.getRequirer().getRevisionInfo();
				fragGen.clearManifestCache();
			}
		}
	}

	void loadClassLoaderFragments(Collection<ModuleRevision> fragments) {
		ModuleClassLoader current = classloader;
		if (current != null) {
			current.loadFragments(fragments);
		}
	}

	static ModuleClassLoader createClassLoaderPrivledged(ClassLoader parent, EquinoxConfiguration configuration,
			BundleLoader delegate, Generation generation, List<ClassLoaderHook> hooks) {
		// allow hooks to extend the ModuleClassLoader implementation
		for (ClassLoaderHook hook : hooks) {
			ModuleClassLoader hookClassLoader = hook.createClassLoader(parent, configuration, delegate, generation);
			if (hookClassLoader != null) {
				// first one to return non-null wins.
				return hookClassLoader;
			}
		}
		// just use the default one
		return new EquinoxClassLoader(parent, configuration);
	}

	public void close() {
		if (policy != null) {
			Module systemModule = container.getStorage().getModuleContainer().getModule(0);
			BundleContext context = systemModule.getBundle().getBundleContext();
			if (context != null) {
				policy.close(context);
			}
		}
		ModuleClassLoader current = classloader;
		if (current != null) {
			current.close();
		}
	}

	@Override
	protected ClassLoader getClassLoader() {
		return getModuleClassLoader();
	}

    /**
	 * Finds a class local to this bundle. Only the classloader for this bundle is
	 * searched.
	 * 
	 * @param name The name of the class to find.
	 * @return The loaded Class or null if the class is not found.
	 */
	public Class<?> findLocalClass(String name) throws ClassNotFoundException {
		try {
			Class<?> clazz = getModuleClassLoader().findLocalClass(name);
			return clazz;
		} catch (ClassNotFoundException e) {
			if (e.getCause() instanceof BundleException) {
				// Here we assume this is because of a lazy activation error
				throw e;
			}
			return null;
		}
	}

    @Override
	protected List<URL> findEntries(String path, String filePattern, int options) {
		return getModuleClassLoader().findEntries(path, filePattern, options);
	}

	public static <E> Enumeration<E> compoundEnumerations(Enumeration<E> list1, Enumeration<E> list2) {
		if (list2 == null || !list2.hasMoreElements())
			return list1 == null ? Collections.emptyEnumeration() : list1;
		if (list1 == null || !list1.hasMoreElements())
			return list2;
		List<E> compoundResults = Collections.list(list1);
		while (list2.hasMoreElements()) {
			E item = list2.nextElement();
			if (!compoundResults.contains(item)) // don't add duplicates
				compoundResults.add(item);
		}
		return Collections.enumeration(compoundResults);
	}

	/**
	 * Finds a resource local to this bundle. Only the classloader for this bundle
	 * is searched.
	 * 
	 * @param name The name of the resource to find.
	 * @return The URL to the resource or null if the resource is not found.
	 */
	public URL findLocalResource(final String name) {
		return getModuleClassLoader().findLocalResource(name);
	}

	/**
	 * Returns an Enumeration of URLs representing all the resources with the given
	 * name. Only the classloader for this bundle is searched.
	 *
	 * @param name the resource name
	 * @return an Enumeration of URLs for the resources
	 */
	public Enumeration<URL> findLocalResources(String name) {
		return getModuleClassLoader().findLocalResources(name);
	}

	/**
	 * Return a string representation of this loader.
	 * 
	 * @return String
	 */
	@Override
	public final String toString() {
		ModuleRevision revision = wiring.getRevision();
		String name = revision.getSymbolicName();
		if (name == null)
			name = "unknown"; //$NON-NLS-1$
		return name + '_' + revision.getVersion();
	}

	/**
	 * Return true if the target package name matches a name in the
	 * DynamicImport-Package manifest header.
	 *
	 * @param pkgname The name of the requested class' package.
	 * @return true if the package should be imported.
	 */
	private boolean isDynamicallyImported(String pkgname) {
		if (this instanceof SystemBundleLoader)
			return false; // system bundle cannot dynamically import
		// must check for startsWith("java.") to satisfy R3 section 4.7.2
		if (pkgname.startsWith("java.")) //$NON-NLS-1$
			return true;

		synchronized (importedSources) {
			/* "*" shortcut */
			if (dynamicAllPackages)
				return true;

			/* match against specific names */
			if (dynamicImportPackages != null)
				for (String dynamicImportPackage : dynamicImportPackages) {
					if (pkgname.equals(dynamicImportPackage)) {
						return true;
					}
				}

			/* match against names with trailing wildcards */
			if (dynamicImportPackageStems != null)
				for (String dynamicImportPackageStem : dynamicImportPackageStems) {
					if (pkgname.startsWith(dynamicImportPackageStem)) {
						return true;
					}
				}
		}
		return false;
	}

	final void addExportedProvidersFor(String packageName, List<PackageSource> result,
			Collection<BundleLoader> visited) {
		if (visited.contains(this))
			return;
		visited.add(this);
		// See if we locally provide the package.
		PackageSource local = null;
		if (isExportedPackage(packageName))
			local = exportSources.getPackageSource(packageName);
		else if (isSubstitutedExport(packageName)) {
			result.add(findImportedSource(packageName, visited));
			return; // should not continue to required bundles in this case
		}
		// Must search required bundles that are exported first.
		for (ModuleWire bundleWire : requiredBundleWires) {
			if (local != null || BundleNamespace.VISIBILITY_REEXPORT.equals(bundleWire.getRequirement().getDirectives()
					.get(BundleNamespace.REQUIREMENT_VISIBILITY_DIRECTIVE))) {
				// always add required bundles first if we locally provide the package
				// This allows a bundle to provide a package from a required bundle without
				// re-exporting the whole required bundle.
				BundleLoader loader = getProviderLoader(bundleWire);
				if (loader != null) {
					loader.addExportedProvidersFor(packageName, result, visited);
				}
			}
		}
		// now add the locally provided package.
		if (local != null)
			result.add(local);
	}

	private BundleLoader getProviderLoader(ModuleWire wire) {
		ModuleWiring provider = wire.getProviderWiring();
		if (provider == null) {
			if (firstUseOfInvalidLoader.getAndSet(true)) {
				// publish a framework event once per loader, include an exception to show the
				// stack
				String message = "Invalid class loader from a refreshed bundle is being used: " + toString(); //$NON-NLS-1$
				container.getEventPublisher().publishFrameworkEvent(FrameworkEvent.ERROR, wiring.getBundle(),
						new IllegalStateException(message));
			}
			return null;
		}
		return (BundleLoader) provider.getModuleLoader();
	}

    public final boolean isExportedPackage(String name) {
		return exportedPackages.contains(name);
	}

	final boolean isSubstitutedExport(String name) {
		return wiring.isSubstitutedPackage(name);
	}

	private void addDynamicImportPackage(List<ModuleRequirement> packageImports) {
		if (packageImports == null || packageImports.isEmpty()) {
			return;
		}
		List<String> dynamicImports = new ArrayList<>(packageImports.size());
		for (ModuleRequirement packageImport : packageImports) {
			if (PackageNamespace.RESOLUTION_DYNAMIC
					.equals(packageImport.getDirectives().get(PackageNamespace.REQUIREMENT_RESOLUTION_DIRECTIVE))) {
				Matcher matcher = PACKAGENAME_FILTER
						.matcher(packageImport.getDirectives().get(PackageNamespace.REQUIREMENT_FILTER_DIRECTIVE));
				if (matcher.find()) {
					String dynamicName = matcher.group(1);
					if (dynamicName != null) {
						dynamicImports.add(dynamicName);
					}
				}
			}
		}
		if (!dynamicImports.isEmpty())
			addDynamicImportPackage(dynamicImports.toArray(new String[0]));
	}

	/**
	 * Adds a list of DynamicImport-Package manifest elements to the dynamic import
	 * tables of this BundleLoader. Duplicate packages are checked and not added
	 * again. This method is not thread safe. Callers should ensure synchronization
	 * when calling this method.
	 * 
	 * @param packages the DynamicImport-Package elements to add.
	 */
	private void addDynamicImportPackage(String[] packages) {
		if (packages == null)
			return;

		synchronized (importedSources) {
			int size = packages.length;
			List<String> stems;
			if (dynamicImportPackageStems == null) {
				stems = new ArrayList<>(size);
			} else {
				stems = new ArrayList<>(size + dynamicImportPackageStems.length);
                Collections.addAll(stems, dynamicImportPackageStems);
			}

			List<String> names;
			if (dynamicImportPackages == null) {
				names = new ArrayList<>(size);
			} else {
				names = new ArrayList<>(size + dynamicImportPackages.length);
                Collections.addAll(names, dynamicImportPackages);
			}

			for (int i = 0; i < size; i++) {
				String name = packages[i];
				if (isDynamicallyImported(name))
					continue;
				if (name.equals("*")) { //$NON-NLS-1$
					// shortcut
					dynamicAllPackages = true;
					return;
				}

				if (name.endsWith(".*")) //$NON-NLS-1$
					stems.add(name.substring(0, name.length() - 1));
				else
					names.add(name);
			}

			size = stems.size();
			if (size > 0)
				dynamicImportPackageStems = stems.toArray(new String[size]);

			size = names.size();
			if (size > 0)
				dynamicImportPackages = names.toArray(new String[size]);
		}
	}

    /*
	 * Finds a packagesource that is either imported or required from another
	 * bundle. This will not include an local package source
	 */
	private PackageSource findSource(String pkgName) {
		if (pkgName == null)
			return null;
		PackageSource result = findImportedSource(pkgName, null);
		if (result != null)
			return result;
		// Note that dynamic imports are not checked to avoid aggressive wiring (bug
		// 105779)
		return findRequiredSource(pkgName, null);
	}

	private PackageSource findImportedSource(String pkgName, Collection<BundleLoader> visited) {
		Map<String, PackageSource> imports = getImportedSources(visited);
		synchronized (imports) {
			return imports.get(pkgName);
		}
	}

	private Map<String, PackageSource> getImportedSources(Collection<BundleLoader> visited) {
		synchronized (importedSources) {
			if (importsInitialized) {
				return importedSources;
			}
			List<ModuleWire> importWires = wiring.getRequiredModuleWires(PackageNamespace.PACKAGE_NAMESPACE);
			if (importWires != null) {
				for (ModuleWire importWire : importWires) {
					PackageSource source = createExportPackageSource(importWire, visited);
					if (source != null) {
						importedSources.put(source.getId(), source);
					}
				}
			}
			importsInitialized = true;
			return importedSources;
		}
	}

    private PackageSource findRequiredSource(String pkgName, Collection<BundleLoader> visited) {
		if (requiredBundleWires.isEmpty()) {
			return null;
		}
		synchronized (requiredSources) {
			PackageSource result = requiredSources.get(pkgName);
			if (result != null)
				return result.isNullSource() ? null : result;
		}
		if (visited == null)
			visited = new ArrayList<>();
		if (!visited.contains(this))
			visited.add(this); // always add ourselves so we do not recurse back to ourselves
		List<PackageSource> result = new ArrayList<>(3);
		for (ModuleWire bundleWire : requiredBundleWires) {
			BundleLoader loader = getProviderLoader(bundleWire);
			if (loader != null) {
				loader.addExportedProvidersFor(pkgName, result, visited);
			}
		}
		// found some so cache the result for next time and return
		PackageSource source;
		if (result.isEmpty()) {
			// did not find it in our required bundles lets record the failure
			// so we do not have to do the search again for this package.
			source = NullPackageSource.getNullPackageSource(pkgName);
		} else if (result.size() == 1) {
			// if there is just one source, remember just the single source
			source = result.get(0);
		} else {
			// if there was more than one source, build a multisource and cache that.
			PackageSource[] srcs = result.toArray(new PackageSource[0]);
			source = createMultiSource(pkgName, srcs);
		}
		synchronized (requiredSources) {
			requiredSources.put(source.getId(), source);
		}
		return source.isNullSource() ? null : source;
	}

	/*
	 * Gets the package source for the pkgName. This will include the local package
	 * source if the bundle exports the package. This is used to compare the
	 * PackageSource of a package from two different bundles.
	 */
	public final PackageSource getPackageSource(String pkgName) {
		PackageSource result = findSource(pkgName);
		if (!isExportedPackage(pkgName))
			return result;
		// if the package is exported then we need to get the local source
		PackageSource localSource = exportSources.getPackageSource(pkgName);
		if (result == null)
			return localSource;
		if (localSource == null)
			return result;
		return createMultiSource(pkgName, new PackageSource[] { result, localSource });
	}

    @Override
	protected boolean getAndSetTrigger() {
		return triggerClassLoaded.getAndSet(true);
	}

	@Override
	public boolean isTriggerSet() {
		return triggerClassLoaded.get();
	}
}
