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
 *     Hannes Wellmann - Bug 576643: Clean up and unify Bundle resource classes
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.ModuleContainer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.ModuleRevision;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.framework.internal.reliablefile.ReliableFile;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.framework.util.CaseInsensitiveDictionaryMap;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.connect.ConnectBundleFile;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.framework.EquinoxConfiguration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.hookregistry.StorageHookFactory;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.hookregistry.StorageHookFactory.StorageHook;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.messages.Msg;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage.ContentProvider.Type;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage.Storage.StorageException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage.bundlefile.BundleEntry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage.bundlefile.BundleFile;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage.bundlefile.BundleFileWrapperChain;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage.url.BundleResourceHandler;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage.url.bundleentry.Handler;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.util.ManifestElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.util.NLS;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Constants;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;

public final class BundleInfo {
    public static final String OSGI_BUNDLE_MANIFEST = JarFile.MANIFEST_NAME;
    public static final String MULTI_RELEASE_HEADER = "Multi-Release"; //$NON-NLS-1$
    public static final String MULTI_RELEASE_VERSIONS = "META-INF/versions/"; //$NON-NLS-1$
    public static final Collection<String> MULTI_RELEASE_FILTER_PREFIXES = Collections.singleton("META-INF/"); //$NON-NLS-1$

    public final class Generation {
        private final long generationId;
        private final Object genMonitor = new Object();
        private final Dictionary<String, String> cachedHeaders;
        private final File content;
        private final boolean isDirectory;
        private final boolean hasPackageInfo;
        private BundleFile bundleFile;
        private Map<String, String> rawHeaders;
        private ModuleRevision revision;
        private ManifestLocalization headerLocalization;
        private ProtectionDomain domain;
        private List<StorageHook<?, ?>> storageHooks;
        private final long lastModified;
        private final boolean isMRJar;
        private final Type contentType;

        Generation(long generationId, File content, boolean isDirectory, Type contentType, boolean hasPackageInfo,
            Map<String, String> cached, long lastModified, boolean isMRJar) {
            this.generationId = generationId;
            this.content = content;
            this.isDirectory = isDirectory;
            this.contentType = contentType;
            this.hasPackageInfo = hasPackageInfo;
            this.cachedHeaders = new CachedManifest(this, cached);
            this.lastModified = lastModified;
            this.isMRJar = isMRJar;
        }

        public BundleFile getBundleFile() {
            synchronized (genMonitor) {
                if (bundleFile == null) {
                    if (getBundleId() == 0 && content == null && contentType != Type.CONNECT) {
                        bundleFile = new SystemBundleFile();
                    } else {
                        bundleFile = getStorage().createBundleFile(content, this, isDirectory, true);
                    }
                }
                return bundleFile;
            }
        }

        public void close() {
            synchronized (genMonitor) {
                if (bundleFile != null) {
                    try {
                        bundleFile.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }

        public Dictionary<String, String> getHeaders() {
            return cachedHeaders;
        }

        Map<String, String> getRawHeaders() {
            synchronized (genMonitor) {
                if (rawHeaders == null) {
                    BundleFile bFile = getBundleFile();

                    if (this.contentType == Type.CONNECT) {
                        ConnectBundleFile connectContent = bFile instanceof BundleFileWrapperChain ? //
                            ((BundleFileWrapperChain) bFile).getWrappedType(ConnectBundleFile.class) : //
                            (ConnectBundleFile) bFile;

                        Map<String, String> connectHeaders = connectContent.getConnectHeaders();
                        if (connectHeaders != null) {
                            return rawHeaders = connectHeaders;
                        }
                    }

                    BundleEntry manifest = bFile.getEntry(OSGI_BUNDLE_MANIFEST);
                    if (manifest == null) {
                        rawHeaders = Collections.emptyMap();
                    } else {
                        try {
                            Map<String, String> merged = ManifestElement.parseBundleManifest(manifest.getInputStream(),
                                new CaseInsensitiveDictionaryMap<>());
                            // For MRJARs only replace Import-Package and Require-Capability if the
                            // versioned values are non-null
                            if (Boolean.parseBoolean(merged.get(MULTI_RELEASE_HEADER))) {
                                for (int i = getStorage().getRuntimeVersion().getMajor(); i > 8; i--) {
                                    String versionManifest = MULTI_RELEASE_VERSIONS + i + "/OSGI-INF/MANIFEST.MF"; //$NON-NLS-1$
                                    BundleEntry versionEntry = getBundleFile().getEntry(versionManifest);
                                    if (versionEntry != null) {
                                        Map<String, String> versioned = ManifestElement.parseBundleManifest(
                                            versionEntry.getInputStream(), new CaseInsensitiveDictionaryMap<>());
                                        String versionedImport = versioned.get(Constants.IMPORT_PACKAGE);
                                        String versionedRequireCap = versioned.get(Constants.REQUIRE_CAPABILITY);
                                        if (versionedImport != null) {
                                            merged.put(Constants.IMPORT_PACKAGE, versionedImport);
                                        }
                                        if (versionedRequireCap != null) {
                                            merged.put(Constants.REQUIRE_CAPABILITY, versionedRequireCap);
                                        }
                                        // found a versioned entry; stop searching for more versions
                                        break;
                                    }
                                }
                            }
                            rawHeaders = Collections.unmodifiableMap(merged);
                        } catch (RuntimeException e) {
                            throw e;
                        } catch (Exception e) {
                            throw new RuntimeException("Error occurred getting the bundle manifest.", e); //$NON-NLS-1$
                        }
                    }
                }
                return rawHeaders;
            }
        }

        public Dictionary<String, String> getHeaders(String locale) {
            ManifestLocalization current = getManifestLocalization();
            return current.getHeaders(locale);
        }

        private ManifestLocalization getManifestLocalization() {
            synchronized (genMonitor) {
                if (headerLocalization == null) {
                    headerLocalization = new ManifestLocalization(this, getHeaders(),
                        getStorage().getConfiguration().getConfiguration(EquinoxConfiguration.PROP_ROOT_LOCALE, "en")); //$NON-NLS-1$
                }
                return headerLocalization;
            }
        }

        public void clearManifestCache() {
            synchronized (genMonitor) {
                if (headerLocalization != null) {
                    headerLocalization.clearCache();
                }
            }
        }

        public long getGenerationId() {
            return this.generationId;
        }

        public long getLastModified() {
            return lastModified;
        }

        public boolean isDirectory() {
            synchronized (this.genMonitor) {
                return this.isDirectory;
            }
        }

        public boolean hasPackageInfo() {
            synchronized (this.genMonitor) {
                return this.hasPackageInfo;
            }
        }

        public boolean isMRJar() {
            synchronized (this.genMonitor) {
                return this.isMRJar;
            }
        }

        public File getContent() {
            synchronized (this.genMonitor) {
                return this.content;
            }
        }

        public Type getContentType() {
            synchronized (this.genMonitor) {
                return this.contentType;
            }
        }

        @SuppressWarnings("unchecked")
        public <S, L, H extends StorageHook<S, L>> H
            getStorageHook(Class<? extends StorageHookFactory<S, L, H>> factoryClass) {
            synchronized (this.genMonitor) {
                if (this.storageHooks == null)
                    return null;
                for (StorageHook<?, ?> hook : storageHooks) {
                    if (hook.getFactoryClass().equals(factoryClass)) {
                        return (H) hook;
                    }
                }
            }
            return null;
        }

        public ModuleRevision getRevision() {
            synchronized (this.genMonitor) {
                return this.revision;
            }
        }

        public void setRevision(ModuleRevision revision) {
            synchronized (this.genMonitor) {
                this.revision = revision;
            }
        }

        public ProtectionDomain getDomain() {
            return getDomain(true);
        }

        public ProtectionDomain getDomain(boolean create) {
            if (getBundleId() == 0 || System.getSecurityManager() == null) {
                return null;
            }
            synchronized (this.genMonitor) {
                if (domain == null && create) {
                    if (revision == null) {
                        throw new IllegalStateException("The revision is not yet set for this generation."); //$NON-NLS-1$
                    }
                    domain = getStorage().getSecurityAdmin().createProtectionDomain(revision.getBundle());
                }
                return domain;
            }
        }

        /**
         * Gets called by BundleFile during {@link BundleFile#getFile(String, boolean)}.
         * This method will allocate a File object where content of the specified path
         * may be stored for this generation. The returned File object may not exist if
         * the content has not previously been stored.
         * 
         * @param path the path to the content to extract from the generation
         * @param base the base path that is prepended to the path, may be null
         * @return a file object where content of the specified path may be stored.
         * @throws StorageException if the path will escape the persistent storage of
         * the generation starting at the specified base
         */
        public File getExtractFile(String base, String path) {
            StringBuilder baseBuilder = new StringBuilder();
            baseBuilder.append(getBundleId()).append('/').append(getGenerationId());
            if (base != null) {
                baseBuilder.append('/').append(base);
            }

            return getStorage().getFile(baseBuilder.toString(), path, true);
        }

        public void storeContent(File destination, InputStream in, boolean nativeCode) throws IOException {
            /* the entry has not been cached */
            /* create the necessary directories */
            File dir = new File(destination.getParent());
            if (!dir.mkdirs() && !dir.isDirectory()) {
                throw new IOException(NLS.bind(Msg.ADAPTOR_DIRECTORY_CREATE_EXCEPTION, dir.getAbsolutePath()));
            }
            /* copy the entry to the cache */
            File tempDest = ReliableFile.createTempFile("staged", ".tmp", dir); //$NON-NLS-1$ //$NON-NLS-2$
            StorageUtil.readFile(in, tempDest);
            if (destination.exists()) {
                // maybe because some other thread already beat us there.
                // just delete our staged copy
                tempDest.delete();
            } else {
                StorageUtil.move(tempDest, destination);
            }
            if (nativeCode) {
                getBundleInfo().getStorage().setPermissions(destination);
            }
        }

        public BundleInfo getBundleInfo() {
            return BundleInfo.this;
        }

        public URL getEntry(String path) {
            BundleEntry entry = getBundleFile().getEntry(path);
            if (entry == null) {
                return null;
            }
            // use the constant string for the protocol to prevent duplication
            String protocol = BundleResourceHandler.OSGI_ENTRY_URL_PROTOCOL;
            ModuleContainer container = getStorage().getModuleContainer();
            Handler handler = new Handler(container, entry);
            return BundleFile.createURL(protocol, getBundleId(), container, entry, 0, path, handler);
        }

    }

    private final Storage storage;
    private final long bundleId;
    private final String location;
    private final long nextGenerationId;
    private final Object infoMonitor = new Object();

    public BundleInfo(Storage storage, long bundleId, String location, long nextGenerationId) {
        this.storage = storage;
        this.bundleId = bundleId;
        this.location = location;
        this.nextGenerationId = nextGenerationId;
    }

    public long getBundleId() {
        return bundleId;
    }

    public String getLocation() {
        return location;
    }

    public Storage getStorage() {
        return storage;
    }

    public long getNextGenerationId() {
        synchronized (this.infoMonitor) {
            return nextGenerationId;
        }
    }

    public File getDataFile(String path) {
        File dataRoot = getStorage().getFile(getBundleId() + "/" + Storage.BUNDLE_DATA_DIR, false); //$NON-NLS-1$
        if (!Storage.secureAction.isDirectory(dataRoot)
            && (storage.isReadOnly()
                || !(Storage.secureAction.mkdirs(dataRoot) || Storage.secureAction.isDirectory(dataRoot)))) {
            return null;
        }
        return path == null ? dataRoot : new File(dataRoot, path);
    }

    static class CachedManifest extends Dictionary<String, String> implements Map<String, String> {
        private final Map<String, String> cached;
        private final Generation generation;

        CachedManifest(Generation generation, Map<String, String> cached) {
            this.generation = generation;
            this.cached = cached;
        }

        @Override
        public Enumeration<String> elements() {
            return Collections.enumeration(generation.getRawHeaders().values());
        }

        @Override
        public String get(Object key) {
            if (cached.containsKey(key)) {
                return cached.get(key);
            }
            return generation.getRawHeaders().get(key);
        }

        @Override
        public boolean isEmpty() {
            return generation.getRawHeaders().isEmpty();
        }

        @Override
        public Enumeration<String> keys() {
            return Collections.enumeration(generation.getRawHeaders().keySet());
        }

        @Override
        public String put(String key, String value) {
            return generation.getRawHeaders().put(key, value);
        }

        @Override
        public String remove(Object key) {
            return generation.getRawHeaders().remove(key);
        }

        @Override
        public int size() {
            return generation.getRawHeaders().size();
        }

        @Override
        public boolean containsKey(Object key) {
            return cached.containsKey(key) || generation.getRawHeaders().containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return cached.containsValue(value) || generation.getRawHeaders().containsValue(value);
        }

        @Override
        public void putAll(Map<? extends String, ? extends String> m) {
            generation.getRawHeaders().putAll(m);
        }

        @Override
        public void clear() {
            generation.getRawHeaders().clear();
        }

        @Override
        public Set<String> keySet() {
            return generation.getRawHeaders().keySet();
        }

        @Override
        public Collection<String> values() {
            return generation.getRawHeaders().values();
        }

        @Override
        public Set<Entry<String, String>> entrySet() {
            return generation.getRawHeaders().entrySet();
        }
    }

}
