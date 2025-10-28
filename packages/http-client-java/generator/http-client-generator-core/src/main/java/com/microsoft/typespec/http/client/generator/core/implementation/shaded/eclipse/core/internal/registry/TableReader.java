/*******************************************************************************
 * Copyright (c) 2004, 2018 IBM Corporation and others.
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

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Status;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.spi.RegistryContributor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.util.NLS;
import java.io.*;
import java.lang.ref.SoftReference;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class TableReader {
    // Markers in the cache
    static final int NULL = 0;
    static final int OBJECT = 1;
    static final int LOBJECT = 2;

    // The version of the cache
    static final int CACHE_VERSION = 8;
    // Version 1 -> 2: the contributor Ids changed from "long" to "String"
    // Version 2 -> 3: added namespace index and the table of contributors
    // Version 3 -> 4: offset table saved in a binary form (performance)
    // Version 4 -> 5: remove support added in version 4 to save offset table in a
    // binary form (performance)
    // Version 5 -> 6: replace HashtableOfInt with OffsetTable (memory usage
    // optimization)
    // Version 6 -> 7: added option for multi-language support
    // Version 7 -> 8: added support for large UTF-8 strings

    // Informations representing the MAIN file
    static final String MAIN = ".mainData"; //$NON-NLS-1$
    DataInputStream mainInput = null;

    // Informations representing the EXTRA file
    static final String EXTRA = ".extraData"; //$NON-NLS-1$
    BufferedRandomInputStream extraDataFile = null;
    DataInputStream extraInput = null;

    // The contributions file
    static final String CONTRIBUTIONS = ".contributions"; //$NON-NLS-1$
    File contributionsFile;

    // The contributor file
    static final String CONTRIBUTORS = ".contributors"; //$NON-NLS-1$
    File contributorsFile;

    // The namespace file
    static final String NAMESPACES = ".namespaces"; //$NON-NLS-1$
    File namespacesFile;

    // The orphan file
    static final String ORPHANS = ".orphans"; //$NON-NLS-1$
    File orphansFile;

    // Status code
    private static final byte fileError = 0;
    private static final boolean DEBUG = false; // TODO need to change

    private final ExtensionRegistry registry;

    private SoftReference<Map<String, String>> stringPool;

    public TableReader(ExtensionRegistry registry) {
        this.registry = registry;
    }

    private int[] readArray(DataInputStream in) throws IOException {
        int arraySize = in.readInt();
        if (arraySize == 0) {
            return RegistryObjectManager.EMPTY_INT_ARRAY;
        }
        int[] result = new int[arraySize];
        for (int i = 0; i < arraySize; i++) {
            result[i] = in.readInt();
        }
        return result;
    }

    private void goToExtraFile(int offset) throws IOException {
        extraDataFile.seek(offset);
    }

    private String readStringOrNull(DataInputStream in) throws IOException {
        byte type = in.readByte();
        if (type == NULL) {
            return null;
        }
        return readUTF(in, type);
    }

    public String[] loadExtensionExtraData(int dataPosition) {
        try {
            synchronized (extraDataFile) {
                goToExtraFile(dataPosition);
                return basicLoadExtensionExtraData();
            }
        } catch (IOException e) {
            String message = NLS.bind(RegistryMessages.meta_regCacheIOExceptionReading, extraDataFile);
            log(new Status(IStatus.ERROR, RegistryMessages.OWNER_NAME, fileError, message, e));
            if (DEBUG) {
                log(new Status(IStatus.ERROR, RegistryMessages.OWNER_NAME, fileError,
                    "Error reading extension label (" + dataPosition + ") from the registry cache", e)); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return null;
        }
    }

    private String[] basicLoadExtensionExtraData() throws IOException {
        return new String[] {
            readStringOrNull(extraInput),
            readStringOrNull(extraInput),
            readStringOrNull(extraInput) };
    }

    public String[] loadExtensionPointExtraData(int offset) {
        try {
            synchronized (extraDataFile) {
                goToExtraFile(offset);
                return basicLoadExtensionPointExtraData();
            }
        } catch (IOException e) {
            String message = NLS.bind(RegistryMessages.meta_regCacheIOExceptionReading, extraDataFile);
            log(new Status(IStatus.ERROR, RegistryMessages.OWNER_NAME, fileError, message, e));
            if (DEBUG) {
                log(new Status(IStatus.ERROR, RegistryMessages.OWNER_NAME, fileError,
                    "Error reading extension point data (" + offset + ") from the registry cache", e)); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return null;
        }
    }

    private String[] basicLoadExtensionPointExtraData() throws IOException {
        String[] result = new String[5];
        result[0] = readStringOrNull(extraInput); // the label
        result[1] = readStringOrNull(extraInput); // the schema
        result[2] = readStringOrNull(extraInput); // the fully qualified name
        result[3] = readStringOrNull(extraInput); // the namespace
        result[4] = readStringOrNull(extraInput); // the contributor Id
        return result;
    }

    public KeyedHashSet loadContributions() {
        DataInputStream namespaceInput = null;
        try {
            synchronized (contributionsFile) {
                namespaceInput = new DataInputStream(new BufferedInputStream(new FileInputStream(contributionsFile)));
                int size = namespaceInput.readInt();
                KeyedHashSet result = new KeyedHashSet(size);
                for (int i = 0; i < size; i++) {
                    String contributorId = readStringOrNull(namespaceInput);
                    Contribution n = getObjectFactory().createContribution(contributorId, true);
                    n.setRawChildren(readArray(namespaceInput));
                    result.add(n);
                }
                return result;
            }
        } catch (IOException e) {
            String message = NLS.bind(RegistryMessages.meta_regCacheIOExceptionReading, contributionsFile);
            log(new Status(IStatus.ERROR, RegistryMessages.OWNER_NAME, fileError, message, e));
            return null;
        } finally {
            if (namespaceInput != null) {
                try {
                    namespaceInput.close();
                } catch (IOException e1) {
                    // Ignore
                }
            }
        }
    }

    final static float contributorsLoadFactor = 1.2f; // allocate more memory to avoid resizing

    public HashMap<String, RegistryContributor> loadContributors() {
        HashMap<String, RegistryContributor> result = null;
        DataInputStream contributorsInput = null;
        try {
            synchronized (contributorsFile) {
                contributorsInput = new DataInputStream(new BufferedInputStream(new FileInputStream(contributorsFile)));
                int size = contributorsInput.readInt();
                result = new HashMap<>((int) (size * contributorsLoadFactor));
                for (int i = 0; i < size; i++) {
                    String id = readStringOrNull(contributorsInput);
                    String name = readStringOrNull(contributorsInput);
                    String hostId = readStringOrNull(contributorsInput);
                    String hostName = readStringOrNull(contributorsInput);
                    result.put(id, new RegistryContributor(id, name, hostId, hostName));
                }
            }
            return result;
        } catch (IOException e) {
            String message = NLS.bind(RegistryMessages.meta_regCacheIOExceptionReading, contributorsFile);
            log(new Status(IStatus.ERROR, RegistryMessages.OWNER_NAME, fileError, message, e));
            return null;
        } finally {
            if (contributorsInput != null) {
                try {
                    contributorsInput.close();
                } catch (IOException e1) {
                    // Ignore
                }
            }
        }
    }

    public KeyedHashSet loadNamespaces() {
        DataInputStream namespaceInput = null;
        try {
            synchronized (namespacesFile) {
                namespaceInput = new DataInputStream(new BufferedInputStream(new FileInputStream(namespacesFile)));
                int size = namespaceInput.readInt();
                KeyedHashSet result = new KeyedHashSet(size);
                for (int i = 0; i < size; i++) {
                    String key = readStringOrNull(namespaceInput);
                    RegistryIndexElement indexElement = new RegistryIndexElement(key);
                    indexElement.updateExtensionPoints(readArray(namespaceInput), true);
                    indexElement.updateExtensions(readArray(namespaceInput), true);
                    result.add(indexElement);
                }
                return result;
            }
        } catch (IOException e) {
            String message = NLS.bind(RegistryMessages.meta_regCacheIOExceptionReading, namespacesFile);
            log(new Status(IStatus.ERROR, RegistryMessages.OWNER_NAME, fileError, message, e));
            return null;
        } finally {
            if (namespaceInput != null) {
                try {
                    namespaceInput.close();
                } catch (IOException e1) {
                    // Ignore
                }
            }
        }
    }

    public HashMap<String, int[]> loadOrphans() {
        DataInputStream orphanInput = null;
        try {
            synchronized (orphansFile) {
                orphanInput = new DataInputStream(new BufferedInputStream(new FileInputStream(orphansFile)));
                int size = orphanInput.readInt();
                HashMap<String, int[]> result = new HashMap<>(size);
                for (int i = 0; i < size; i++) {
                    String key = readUTF(orphanInput, OBJECT);
                    int[] value = readArray(orphanInput);
                    result.put(key, value);
                }
                return result;
            }
        } catch (IOException e) {
            return null;
        } finally {
            if (orphanInput != null) {
                try {
                    orphanInput.close();
                } catch (IOException e1) {
                    // ignore
                }
            }
        }
    }

    private void log(Status status) {
        registry.log(status);
    }

    private RegistryObjectFactory getObjectFactory() {
        return registry.getElementFactory();
    }

    public void close() {
        try {
            if (mainInput != null) {
                mainInput.close();
            }
            if (extraInput != null) {
                extraInput.close();
            }
        } catch (IOException e) {
            log(new Status(IStatus.ERROR, RegistryMessages.OWNER_NAME, fileError,
                RegistryMessages.meta_registryCacheReadProblems, e));
        }
    }

    private String readUTF(DataInputStream in, int type) throws IOException {
        String value;
        if (type == LOBJECT) {
            int length = in.readInt();
            byte[] data = new byte[length];
            in.readFully(data);
            value = new String(data, StandardCharsets.UTF_8);
        } else {
            value = in.readUTF();
        }

        Map<String, String> map = null;
        if (stringPool != null) {
            map = stringPool.get();
        }
        if (map == null) {
            map = new HashMap<>();
            stringPool = new SoftReference<>(map);
        }

        String pooledString = map.get(value);
        if (pooledString == null) {
            map.put(value, value);
            return value;
        }

        return pooledString;
    }
}
