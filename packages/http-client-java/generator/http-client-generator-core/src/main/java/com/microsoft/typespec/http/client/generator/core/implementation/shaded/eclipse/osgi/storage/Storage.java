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
 *     Christoph Laeubrich - Bug 527175 - Storage#getSystemContent() should first make the file absolute
 *     Hannes Wellmann - Bug 577432 - Speed up and improve file processing in Storage
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.adaptor.EclipseStarter;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.Module;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.ModuleContainer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.ModuleContainerAdaptor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.ModuleDatabase;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.ModuleRevision;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.namespaces.EclipsePlatformNamespace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.framework.log.FrameworkLogEntry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.framework.util.FilePath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.framework.util.SecureAction;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.container.InternalUtils;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.framework.EquinoxConfiguration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.framework.EquinoxContainer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.framework.EquinoxContainerAdaptor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.framework.FilterImpl;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.hookregistry.BundleFileWrapperFactoryHook;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.hookregistry.StorageHookFactory;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.hookregistry.StorageHookFactory.StorageHook;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.location.LocationHelper;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.log.EquinoxLogServices;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.messages.Msg;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.permadmin.SecurityAdmin;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.service.datalocation.Location;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage.BundleInfo.Generation;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage.ContentProvider.Type;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage.bundlefile.BundleFile;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage.bundlefile.BundleFileWrapper;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage.bundlefile.BundleFileWrapperChain;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage.bundlefile.DirBundleFile;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage.bundlefile.MRUBundleFileList;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage.bundlefile.NestedDirBundleFile;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage.bundlefile.ZipBundleFile;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storagemanager.ManagedOutputStream;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storagemanager.StorageManager;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.util.ManifestElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.util.NLS;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Constants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Filter;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.InvalidSyntaxException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Version;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.connect.ConnectModule;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.namespace.NativeNamespace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.namespace.PackageNamespace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.wiring.BundleCapability;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.wiring.BundleWiring;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.resource.Namespace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.resource.Requirement;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class Storage {
    public static class StorageException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public StorageException(String message) {
            super(message);
        }

    }

    public static final int VERSION = 6;
    public static final String BUNDLE_DATA_DIR = "data"; //$NON-NLS-1$
    public static final String BUNDLE_FILE_NAME = "bundleFile"; //$NON-NLS-1$
    public static final String FRAMEWORK_INFO = "framework.info"; //$NON-NLS-1$
    public static final String ECLIPSE_SYSTEMBUNDLE = "Eclipse-SystemBundle"; //$NON-NLS-1$
    public static final String DELETE_FLAG = ".delete"; //$NON-NLS-1$

    // $NON-NLS-1$
    private static final String NUL = new String(new byte[] { 0 });

    static final SecureAction secureAction = AccessController.doPrivileged(SecureAction.createSecureAction());

    private EquinoxContainer equinoxContainer;
    private String installPath;
    private Location osgiLocation;
    private File childRoot;
    private File parentRoot;
    private PermissionData permissionData;
    private SecurityAdmin securityAdmin;
    private EquinoxContainerAdaptor adaptor;
    private ModuleDatabase moduleDatabase;
    private ModuleContainer moduleContainer;
    private final Object saveMonitor = new Object();
    private long lastSavedTimestamp = -1;
    private MRUBundleFileList mruList;
    private final List<String> cachedHeaderKeys
        = Arrays.asList(Constants.BUNDLE_SYMBOLICNAME, Constants.BUNDLE_ACTIVATIONPOLICY, "Service-Component"); //$NON-NLS-1$
    private Version runtimeVersion;

    public Version getRuntimeVersion() {
        return runtimeVersion;
    }

    private Version findFrameworkVersion() {
        Requirement osgiPackageReq = ModuleContainer.createRequirement(PackageNamespace.PACKAGE_NAMESPACE,
            Collections.singletonMap(Namespace.REQUIREMENT_FILTER_DIRECTIVE, "(" + PackageNamespace.PACKAGE_NAMESPACE //$NON-NLS-1$
                + "=com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework)"),  //$NON-NLS-1$
            Collections.emptyMap());
        Collection<BundleCapability> osgiPackages = moduleContainer.getFrameworkWiring().findProviders(osgiPackageReq);
        for (BundleCapability packageCapability : osgiPackages) {
            if (packageCapability.getRevision().getBundle().getBundleId() == 0) {
                Version v
                    = (Version) packageCapability.getAttributes().get(PackageNamespace.CAPABILITY_VERSION_ATTRIBUTE);
                if (v != null) {
                    return v;
                }
            }
        }
        return null;
    }

    public ModuleDatabase getModuleDatabase() {
        return moduleDatabase;
    }

    public ModuleContainerAdaptor getAdaptor() {
        return adaptor;
    }

    public ModuleContainer getModuleContainer() {
        return moduleContainer;
    }

    public EquinoxConfiguration getConfiguration() {
        return equinoxContainer.getConfiguration();
    }

    public EquinoxLogServices getLogServices() {
        return equinoxContainer.getLogServices();
    }

    public boolean isReadOnly() {
        return osgiLocation.isReadOnly();
    }

    private String getSystemExtraCapabilities() {
        EquinoxConfiguration equinoxConfig = equinoxContainer.getConfiguration();
        StringBuilder result = new StringBuilder();

        String systemCapabilities = equinoxConfig.getConfiguration(Constants.FRAMEWORK_SYSTEMCAPABILITIES);
        if (systemCapabilities != null && !systemCapabilities.trim().isEmpty()) {
            result.append(systemCapabilities).append(", "); //$NON-NLS-1$
        }

        String extraSystemCapabilities = equinoxConfig.getConfiguration(Constants.FRAMEWORK_SYSTEMCAPABILITIES_EXTRA);
        if (extraSystemCapabilities != null && !extraSystemCapabilities.trim().isEmpty()) {
            result.append(extraSystemCapabilities).append(", "); //$NON-NLS-1$
        }

        result.append(EclipsePlatformNamespace.ECLIPSE_PLATFORM_NAMESPACE).append("; "); //$NON-NLS-1$
        result.append(EquinoxConfiguration.PROP_OSGI_OS).append("=").append(equinoxConfig.getOS()).append("; "); //$NON-NLS-1$ //$NON-NLS-2$
        result.append(EquinoxConfiguration.PROP_OSGI_WS).append("=").append(equinoxConfig.getWS()).append("; "); //$NON-NLS-1$ //$NON-NLS-2$
        result.append(EquinoxConfiguration.PROP_OSGI_ARCH).append("=").append(equinoxConfig.getOSArch()).append("; "); //$NON-NLS-1$ //$NON-NLS-2$
        result.append(EquinoxConfiguration.PROP_OSGI_NL).append("=").append(equinoxConfig.getNL()); //$NON-NLS-1$

        String osName = equinoxConfig.getConfiguration(Constants.FRAMEWORK_OS_NAME);
        osName = osName == null ? null : osName.toLowerCase();
        String processor = equinoxConfig.getConfiguration(Constants.FRAMEWORK_PROCESSOR);
        processor = processor == null ? null : processor.toLowerCase();
        String osVersion = equinoxConfig.getConfiguration(Constants.FRAMEWORK_OS_VERSION);
        osVersion = osVersion == null ? null : osVersion.toLowerCase();
        String language = equinoxConfig.getConfiguration(Constants.FRAMEWORK_LANGUAGE);
        language = language == null ? null : language.toLowerCase();

        result.append(", "); //$NON-NLS-1$
        result.append(NativeNamespace.NATIVE_NAMESPACE).append("; "); //$NON-NLS-1$
        if (osName != null) {
            osName = getAliasList(equinoxConfig.getAliasMapper().getOSNameAliases(osName));
            result.append(NativeNamespace.CAPABILITY_OSNAME_ATTRIBUTE)
                .append(":List<String>=") //$NON-NLS-1$
                .append(osName)
                .append("; "); //$NON-NLS-1$
        }
        if (processor != null) {
            processor = getAliasList(equinoxConfig.getAliasMapper().getProcessorAliases(processor));
            result.append(NativeNamespace.CAPABILITY_PROCESSOR_ATTRIBUTE)
                .append(":List<String>=") //$NON-NLS-1$
                .append(processor)
                .append("; "); //$NON-NLS-1$
        }
        result.append(NativeNamespace.CAPABILITY_OSVERSION_ATTRIBUTE)
            .append(":Version") //$NON-NLS-1$
            .append("=\"") //$NON-NLS-1$
            .append(osVersion)
            .append("\"; "); //$NON-NLS-1$
        result.append(NativeNamespace.CAPABILITY_LANGUAGE_ATTRIBUTE).append("=\"").append(language).append('\"'); //$NON-NLS-1$
        return result.toString();
    }

    String getAliasList(Collection<String> aliases) {
        if (aliases.isEmpty()) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        builder.append('"');
        for (String alias : aliases) {
            builder.append(alias).append(',');
        }
        builder.setLength(builder.length() - 1);
        builder.append('"');
        return builder.toString();
    }

    private String getSystemExtraPackages() {
        EquinoxConfiguration equinoxConfig = equinoxContainer.getConfiguration();
        StringBuilder result = new StringBuilder();

        String systemPackages = equinoxConfig.getConfiguration(Constants.FRAMEWORK_SYSTEMPACKAGES);
        if (systemPackages != null) {
            result.append(systemPackages);
        }

        String extraSystemPackages = equinoxConfig.getConfiguration(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA);
        if (extraSystemPackages != null && !extraSystemPackages.trim().isEmpty()) {
            if (!result.isEmpty()) {
                result.append(", "); //$NON-NLS-1$
            }
            result.append(extraSystemPackages);
        }

        return result.toString();
    }

    private static String getBundleFilePath(long bundleID, long generationID) {
        return bundleID + "/" + generationID + "/" + BUNDLE_FILE_NAME; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Gets a file from storage and conditionally checks the parent storage area if
     * the file does not exist in the child configuration. Note, this method does
     * not check for escaping of paths from the root storage area.
     * 
     * @param path the path relative to the root of the storage area
     * @param checkParent if true then check the parent storage (if any) when the
     * file does not exist in the child storage area
     * @return the file being requested. A {@code null} value is never returned. The
     * file returned may not exist.
     * @throws StorageException if there was an issue getting the file
     */
    public File getFile(String path, boolean checkParent) throws StorageException {
        return getFile(null, path, checkParent);
    }

    /**
     * Same as {@link #getFile(String, boolean)} except takes a base parameter which
     * is appended to the root storage area before looking for the path. If base is
     * not null then additional checks are done to make sure the path does not
     * escape out of the base path.
     * 
     * @param base the additional base path to append to the root storage
     * area. May be {@code null}, in which case no check is done
     * for escaping out of the base path.
     * @param path the path relative to the root + base storage area.
     * @param checkParent if true then check the parent storage (if any) when the
     * file does not exist in the child storage area
     * @return the file being requested. A {@code null} value is never returned. The
     * file returned may not exist.
     * @throws StorageException if there was an issue getting the file
     */
    public File getFile(String base, String path, boolean checkParent) throws StorageException {
        // first check the child location
        File childPath = getFile(childRoot, base, path);
        // now check the parent
        if (checkParent && parentRoot != null) {
            if (childPath.exists()) {
                return childPath;
            }
            File parentPath = getFile(parentRoot, base, path);
            if (parentPath.exists()) {
                // only use the parent file only if it exists;
                return parentPath;
            }
        }
        // did not exist in both locations; use the child path
        return childPath;
    }

    private static File getFile(File root, String base, String path) {
        if (base == null) {
            // return quick; no need to check for path traversal
            return new File(root, path);
        }

        // if base is not null then move root to include the base
        File rootBase = new File(root, base);
        File result = new File(rootBase, path);
        if (path.contains("..")) { //$NON-NLS-1$
            // do the extra check to make sure the path did not escape the root path
            Path resultNormalized = result.toPath().normalize();
            Path rootBaseNormalized = rootBase.toPath().normalize();
            if (!resultNormalized.startsWith(rootBaseNormalized)) {
                throw new StorageException("Invalid path: " + path); //$NON-NLS-1$
            }
        }
        // Additional check if it is a special device instead of a regular file.
        if (StorageUtil.isReservedFileName(result)) {
            throw new StorageException("Invalid filename: " + path); //$NON-NLS-1$
        }
        return result;
    }

    /**
     * Attempts to set the permissions of the file in a system dependent way.
     * 
     * @param file the file to set the permissions on
     */
    public void setPermissions(File file) {
        String commandProp = getConfiguration().getConfiguration(EquinoxConfiguration.PROP_SETPERMS_CMD);
        if (commandProp == null)
            commandProp = getConfiguration().getConfiguration(Constants.FRAMEWORK_EXECPERMISSION);
        if (commandProp == null)
            return;
        String[] commandComponents = ManifestElement.getArrayFromList(commandProp, " "); //$NON-NLS-1$
        List<String> command = new ArrayList<>(commandComponents.length + 1);
        boolean foundFullPath = false;
        for (String commandComponent : commandComponents) {
            if ("[fullpath]".equals(commandComponent) || "${abspath}".equals(commandComponent)) { //$NON-NLS-1$ //$NON-NLS-2$
                command.add(file.getAbsolutePath());
                foundFullPath = true;
            } else {
                command.add(commandComponent);
            }
        }
        if (!foundFullPath)
            command.add(file.getAbsolutePath());
        try {
            Runtime.getRuntime().exec(command.toArray(new String[command.size()])).waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BundleFile createBundleFile(File content, Generation generation, boolean isDirectory, boolean isBase) {
        BundleFile result = null;
        ConnectModule connectModule = null;
        if (generation.getContentType() == Type.CONNECT) {
            connectModule
                = equinoxContainer.getConnectModules().getConnectModule(generation.getBundleInfo().getLocation());
        }
        try {
            if (connectModule != null && isBase) {
                result = equinoxContainer.getConnectModules()
                    .getConnectBundleFile(connectModule, content, generation, mruList);
            } else if (isDirectory) {
                boolean strictPath = Boolean.parseBoolean(getConfiguration().getConfiguration(
                    EquinoxConfiguration.PROPERTY_STRICT_BUNDLE_ENTRY_PATH, Boolean.FALSE.toString()));
                result = new DirBundleFile(content, strictPath);
            } else {
                result = new ZipBundleFile(content, generation, mruList, getConfiguration().runtimeVerifySignedBundles);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create bundle file.", e); //$NON-NLS-1$
        }
        return wrapBundleFile(result, generation, isBase);
    }

    public BundleFile createNestedBundleFile(String nestedDir, BundleFile bundleFile, Generation generation,
        Collection<String> filterPrefixes) {
        // here we assume the content is a path offset into the base bundle file; create
        // a NestedDirBundleFile
        return wrapBundleFile(new NestedDirBundleFile(bundleFile, nestedDir, filterPrefixes), generation, false);
    }

    public BundleFile wrapBundleFile(BundleFile bundleFile, Generation generation, boolean isBase) {
        // try creating a wrapper bundlefile out of it.
        List<BundleFileWrapperFactoryHook> wrapperFactories
            = getConfiguration().getHookRegistry().getBundleFileWrapperFactoryHooks();
        BundleFileWrapperChain wrapped
            = wrapperFactories.isEmpty() ? null : new BundleFileWrapperChain(bundleFile, null);
        for (BundleFileWrapperFactoryHook wrapperFactory : wrapperFactories) {
            BundleFileWrapper wrapperBundle = wrapperFactory.wrapBundleFile(bundleFile, generation, isBase);
            if (wrapperBundle != null && wrapperBundle != bundleFile)
                bundleFile = wrapped = new BundleFileWrapperChain(wrapperBundle, wrapped);
        }

        return bundleFile;
    }

    public void compact() {
        if (!osgiLocation.isReadOnly()) {
            compact(childRoot);
        }
    }

    private void compact(File directory) {
        String list[] = directory.list();
        if (list == null)
            return;

        int len = list.length;
        for (int i = 0; i < len; i++) {
            if (BUNDLE_DATA_DIR.equals(list[i]))
                continue; /* do not examine the bundles data dir. */
            File target = new File(directory, list[i]);
            // if the file is a directory
            if (!target.isDirectory())
                continue;
            File delete = new File(target, DELETE_FLAG);
            // and the directory is marked for delete
            if (delete.exists()) {
                try {
                    deleteFlaggedDirectory(target);
                } catch (IOException e) {
                }
            } else {
                compact(target); /* descend into directory */
            }
        }
    }

    private void deleteFlaggedDirectory(File delete) throws IOException {
        if (!StorageUtil.rm(delete)) {
            ensureDeleteFlagFileExists(delete.toPath());
        }
    }

    public void save() throws IOException {
        if (isReadOnly()) {
            return;
        }
        if (System.getSecurityManager() == null) {
            save0();
        } else {
            try {
                AccessController.doPrivileged((PrivilegedExceptionAction<Void>) () -> {
                    save0();
                    return null;
                });
            } catch (PrivilegedActionException e) {
                if (e.getException() instanceof IOException)
                    throw (IOException) e.getException();
                throw (RuntimeException) e.getException();
            }
        }
    }

    void save0() throws IOException {
        StorageManager childStorageManager = null;
        ManagedOutputStream mos = null;
        DataOutputStream out = null;
        boolean success = false;
        moduleDatabase.readLock();
        synchronized (this.saveMonitor) {
            try {
                if (lastSavedTimestamp == moduleDatabase.getTimestamp())
                    return;
                childStorageManager = getChildStorageManager();
                mos = childStorageManager.getOutputStream(FRAMEWORK_INFO);
                out = new DataOutputStream(new BufferedOutputStream(mos));
                saveGenerations(out);
                savePermissionData(out);
                moduleDatabase.store(out, true);
                lastSavedTimestamp = moduleDatabase.getTimestamp();
                success = true;
            } finally {
                moduleDatabase.readUnlock();
                if (!success) {
                    if (mos != null) {
                        mos.abort();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        // tried our best
                    }
                }
                if (childStorageManager != null) {
                    childStorageManager.close();
                }
            }
        }
    }

    private void savePermissionData(DataOutputStream out) throws IOException {
        permissionData.savePermissionData(out);
    }

    private void saveGenerations(DataOutputStream out) throws IOException {
        List<Module> modules = moduleContainer.getModules();
        List<Generation> generations = new ArrayList<>();
        for (Module module : modules) {
            ModuleRevision revision = module.getCurrentRevision();
            if (revision != null) {
                Generation generation = (Generation) revision.getRevisionInfo();
                if (generation != null) {
                    generations.add(generation);
                }
            }
        }
        out.writeInt(VERSION);

        out.writeUTF(runtimeVersion.toString());

        Version curFrameworkVersion = findFrameworkVersion();
        out.writeUTF(curFrameworkVersion == null ? Version.emptyVersion.toString() : curFrameworkVersion.toString());

        saveLongString(out, getSystemExtraCapabilities());
        saveLongString(out, getSystemExtraPackages());

        out.writeInt(cachedHeaderKeys.size());
        for (String headerKey : cachedHeaderKeys) {
            out.writeUTF(headerKey);
        }

        out.writeInt(generations.size());
        for (Generation generation : generations) {
            BundleInfo bundleInfo = generation.getBundleInfo();
            out.writeLong(bundleInfo.getBundleId());
            out.writeUTF(bundleInfo.getLocation());
            out.writeLong(bundleInfo.getNextGenerationId());
            out.writeLong(generation.getGenerationId());
            out.writeBoolean(generation.isDirectory());
            Type contentType = generation.getContentType();
            out.writeInt(contentType.ordinal());
            out.writeBoolean(generation.hasPackageInfo());
            if (bundleInfo.getBundleId() == 0 || contentType == Type.CONNECT) {
                // just write empty string for system bundle content and connect content in this
                // case
                out.writeUTF(""); //$NON-NLS-1$
            } else {
                if (contentType == Type.REFERENCE) {
                    // make reference installs relative to the install path
                    out.writeUTF(new FilePath(installPath)
                        .makeRelative(new FilePath(generation.getContent().getAbsolutePath())));
                } else {
                    // make normal installs relative to the storage area
                    out.writeUTF(Storage.getBundleFilePath(bundleInfo.getBundleId(), generation.getGenerationId()));
                }
            }
            out.writeLong(generation.getLastModified());

            Dictionary<String, String> headers = generation.getHeaders();
            for (String headerKey : cachedHeaderKeys) {
                String value = headers.get(headerKey);
                if (value != null) {
                    out.writeUTF(value);
                } else {
                    out.writeUTF(NUL);
                }
            }

            out.writeBoolean(generation.isMRJar());
        }

        saveStorageHookData(out, generations);
    }

    private void saveLongString(DataOutputStream out, String value) throws IOException {
        if (value == null) {
            out.writeInt(0);
        } else {
            // don't use out.writeUTF because it has a hard string limit
            byte[] data = value.getBytes(StandardCharsets.UTF_8);
            out.writeInt(data.length);
            out.write(data);
        }
    }

    private void saveStorageHookData(DataOutputStream out, List<Generation> generations) throws IOException {
        List<StorageHookFactory<?, ?, ?>> factories = getConfiguration().getHookRegistry().getStorageHookFactories();
        out.writeInt(factories.size());
        for (StorageHookFactory<?, ?, ?> factory : factories) {
            out.writeUTF(factory.getKey());
            out.writeInt(factory.getStorageVersion());

            // create a temporary in memory stream so we can figure out the length
            ByteArrayOutputStream tempBytes = new ByteArrayOutputStream();
            try (DataOutputStream temp = new DataOutputStream(tempBytes)) {
                Object saveContext = factory.createSaveContext();
                for (Generation generation : generations) {
                    if (generation.getBundleInfo().getBundleId() == 0) {
                        continue; // ignore system bundle
                    }
                    @SuppressWarnings({ "rawtypes", "unchecked" })
                    StorageHook<Object, Object> hook = generation.getStorageHook((Class) factory.getClass());
                    if (hook != null) {
                        hook.save(saveContext, temp);
                    }
                }
            }
            out.writeInt(tempBytes.size());
            out.write(tempBytes.toByteArray());
        }
    }

    public static Enumeration<URL> findEntries(List<Generation> generations, String path, String filePattern,
        int options) {
        List<BundleFile> bundleFiles = new ArrayList<>(generations.size());
        for (Generation generation : generations)
            bundleFiles.add(generation.getBundleFile());
        // search all the bundle files
        List<String> pathList = listEntryPaths(bundleFiles, path, filePattern, options);
        // return null if no entries found
        if (pathList.isEmpty())
            return null;
        // create an enumeration to enumerate the pathList (generations must not change)
        Stream<URL> entries
            = pathList.stream().flatMap(p -> generations.stream().map(g -> g.getEntry(p))).filter(Objects::nonNull);
        return InternalUtils.asEnumeration(entries.iterator());
    }

    /**
     * Returns the names of resources available from a list of bundle files. No
     * duplicate resource names are returned, each name is unique.
     * 
     * @param bundleFiles the list of bundle files to search in
     * @param path The path name in which to look.
     * @param filePattern The file name pattern for selecting resource names in the
     * specified path.
     * @param options The options for listing resource names.
     * @return a list of resource names. If no resources are found then the empty
     * list is returned.
     * @see BundleWiring#listResources(String, String, int)
     */
    public static List<String> listEntryPaths(List<BundleFile> bundleFiles, String path, String filePattern,
        int options) {
        // Use LinkedHashSet for optimized performance of contains() plus
        // ordering guarantees.
        LinkedHashSet<String> pathList = new LinkedHashSet<>();
        Filter patternFilter = null;
        Hashtable<String, String> patternProps = null;
        if (filePattern != null) {
            // Optimization: If the file pattern does not include a wildcard or escape char
            // then it must represent a single file.
            // Avoid pattern matching and use BundleFile.getEntry() if recursion was not
            // requested.
            if ((options & BundleWiring.FINDENTRIES_RECURSE) == 0
                && filePattern.indexOf('*') == -1
                && filePattern.indexOf('\\') == -1) {
                if (path.isEmpty())
                    path = filePattern;
                else
                    path += path.charAt(path.length() - 1) == '/' ? filePattern : '/' + filePattern;
                for (BundleFile bundleFile : bundleFiles) {
                    if (bundleFile.getEntry(path) != null)
                        pathList.add(path);
                }
                return new ArrayList<>(pathList);
            }
            // For when the file pattern includes a wildcard.
            try {
                // create a file pattern filter with 'filename' as the key
                patternFilter = FilterImpl.newInstance("(filename=" + sanitizeFilterInput(filePattern) + ")"); //$NON-NLS-1$ //$NON-NLS-2$
                // create a single hashtable to be shared during the recursive search
                patternProps = new Hashtable<>(2);
            } catch (InvalidSyntaxException e) {
                // TODO something unexpected happened; log error and return nothing
                // Bundle b = context == null ? null : context.getBundle();
                // eventPublisher.publishFrameworkEvent(FrameworkEvent.ERROR, b, e);
                return new ArrayList<>(pathList);
            }
        }
        // find the entry paths for the datas
        for (BundleFile bundleFile : bundleFiles) {
            listEntryPaths(bundleFile, path, patternFilter, patternProps, options, pathList);
        }
        return new ArrayList<>(pathList);
    }

    public static String sanitizeFilterInput(String filePattern) throws InvalidSyntaxException {
        StringBuilder buffer = null;
        boolean foundEscape = false;
        for (int i = 0; i < filePattern.length(); i++) {
            char c = filePattern.charAt(i);
            switch (c) {
                case '\\':
                    // we either used the escape found or found a new escape.
                    foundEscape = !foundEscape;
                    if (buffer != null)
                        buffer.append(c);
                    break;

                case '(':
                case ')':
                    if (!foundEscape) {
                        if (buffer == null) {
                            buffer = new StringBuilder(filePattern.length() + 16);
                            buffer.append(filePattern.substring(0, i));
                        }
                        // must escape with '\'
                        buffer.append('\\');
                    } else {
                        foundEscape = false; // used the escape found
                    }
                    if (buffer != null)
                        buffer.append(c);
                    break;

                default:
                    // if we found an escape it has been used
                    foundEscape = false;
                    if (buffer != null)
                        buffer.append(c);
                    break;
            }
        }
        if (foundEscape)
            throw new InvalidSyntaxException("Trailing escape characters must be escaped.", filePattern); //$NON-NLS-1$
        return buffer == null ? filePattern : buffer.toString();
    }

    // Use LinkedHashSet for optimized performance of contains() plus ordering
    // guarantees.
    private static LinkedHashSet<String> listEntryPaths(BundleFile bundleFile, String path, Filter patternFilter,
        Hashtable<String, String> patternProps, int options, LinkedHashSet<String> pathList) {
        if (pathList == null)
            pathList = new LinkedHashSet<>();
        boolean recurse = (options & BundleWiring.FINDENTRIES_RECURSE) != 0;
        Enumeration<String> entryPaths = bundleFile.getEntryPaths(path, recurse);
        if (entryPaths == null)
            return pathList;
        while (entryPaths.hasMoreElements()) {
            String entry = entryPaths.nextElement();
            int lastSlash = entry.lastIndexOf('/');
            if (patternProps != null) {
                int secondToLastSlash = entry.lastIndexOf('/', lastSlash - 1);
                int fileStart;
                int fileEnd = entry.length();
                if (lastSlash < 0)
                    fileStart = 0;
                else if (lastSlash != entry.length() - 1)
                    fileStart = lastSlash + 1;
                else {
                    fileEnd = lastSlash; // leave the lastSlash out
                    if (secondToLastSlash < 0)
                        fileStart = 0;
                    else
                        fileStart = secondToLastSlash + 1;
                }
                String fileName = entry.substring(fileStart, fileEnd);
                // set the filename to the current entry
                patternProps.put("filename", fileName); //$NON-NLS-1$
            }
            // prevent duplicates and match on the patternFilter
            if (!pathList.contains(entry) && (patternFilter == null || patternFilter.matchCase(patternProps)))
                pathList.add(entry);
        }
        return pathList;
    }

    private static void ensureDeleteFlagFileExists(Path directory) throws IOException {
        Path deleteFlag = directory.resolve(DELETE_FLAG);
        if (!Files.exists(deleteFlag)) {
            Files.createFile(deleteFlag);
        }
    }

    public SecurityAdmin getSecurityAdmin() {
        return securityAdmin;
    }

    protected StorageManager getChildStorageManager() throws IOException {
        String locking
            = getConfiguration().getConfiguration(LocationHelper.PROP_OSGI_LOCKING, LocationHelper.LOCKING_NIO);
        StorageManager sManager
            = new StorageManager(childRoot, isReadOnly() ? LocationHelper.LOCKING_NONE : locking, isReadOnly());
        try {
            sManager.open(!isReadOnly());
        } catch (IOException ex) {
            String message = NLS.bind(Msg.ECLIPSE_STARTUP_FILEMANAGER_OPEN_ERROR, ex.getMessage());
            equinoxContainer.getLogServices().log(EquinoxContainer.NAME, FrameworkLogEntry.ERROR, message, ex);
            getConfiguration().setProperty(EclipseStarter.PROP_EXITCODE, "15"); //$NON-NLS-1$
            String errorDialog = "<title>" + Msg.ADAPTOR_STORAGE_INIT_FAILED_TITLE + "</title>" //$NON-NLS-1$ //$NON-NLS-2$
                + NLS.bind(Msg.ADAPTOR_STORAGE_INIT_FAILED_MSG, childRoot) + "\n" + ex.getMessage(); //$NON-NLS-1$
            getConfiguration().setProperty(EclipseStarter.PROP_EXITDATA, errorDialog);
            throw ex;
        }
        return sManager;
    }

}
