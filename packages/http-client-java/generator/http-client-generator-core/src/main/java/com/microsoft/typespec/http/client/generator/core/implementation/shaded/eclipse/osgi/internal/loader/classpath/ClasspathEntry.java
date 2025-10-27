/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
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

package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.loader.classpath;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.Manifest;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage.BundleInfo;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage.BundleInfo.Generation;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage.Storage;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage.bundlefile.BundleEntry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage.bundlefile.BundleFile;

/**
 * A ClasspathEntry contains a single <code>BundleFile</code> which is used as a
 * source to load classes and resources from, and a single
 * <code>ProtectionDomain</code> which is used as the domain to define classes
 * loaded from this ClasspathEntry.
 * 
 * @since 3.2
 */
public class ClasspathEntry {
	static final class PDEData {
		final String fileName;
		final String symbolicName;

		PDEData(File baseFile, String symbolicName) {
			this.fileName = baseFile == null ? null : baseFile.getAbsolutePath();
			this.symbolicName = symbolicName;
		}
	}

	private final BundleFile bundlefile;
	private final ProtectionDomain domain;
    private final List<BundleFile> mrBundleFiles;

    // TODO Note that PDE has internal dependency on this field type/name (bug
	// 267238)
	@SuppressWarnings("unused")
	private final PDEData data;

	/**
	 * Constructs a ClasspathElement with the specified bundlefile and domain
	 * 
	 * @param bundlefile A BundleFile object which acts as a source
	 * @param domain     the protection domain
	 */
	public ClasspathEntry(BundleFile bundlefile, ProtectionDomain domain, Generation generation) {
		this.bundlefile = bundlefile;
		this.domain = domain;
		this.data = new PDEData(generation.getBundleFile().getBaseFile(), generation.getRevision().getSymbolicName());
		final Manifest manifest = loadManifest(bundlefile, generation);
		if (manifest != null && generation.getBundleInfo().getStorage().getConfiguration().DEFINE_PACKAGE_ATTRIBUTES) {
        } else {
        }

		boolean isMRJar;
		if (bundlefile == generation.getBundleFile()) {
			// this is the root bundle file
			isMRJar = generation.isMRJar();
		} else {
			isMRJar = manifest != null
					? Boolean.parseBoolean(manifest.getMainAttributes().getValue(BundleInfo.MULTI_RELEASE_HEADER))
					: false;
		}
		if (isMRJar) {
			mrBundleFiles = getMRBundleFiles(bundlefile, generation);
		} else {
			mrBundleFiles = Collections.emptyList();
		}
	}

	private static List<BundleFile> getMRBundleFiles(BundleFile bundlefile, Generation generation) {
		Storage storage = generation.getBundleInfo().getStorage();
		if (storage.getRuntimeVersion().getMajor() < 9) {
			return Collections.emptyList();
		}
		List<BundleFile> mrBundleFiles = new ArrayList<>();
		for (int i = storage.getRuntimeVersion().getMajor(); i > 8; i--) {
			String versionPath = BundleInfo.MULTI_RELEASE_VERSIONS + i + '/';
			BundleEntry versionEntry = bundlefile.getEntry(versionPath);
			if (versionEntry != null) {
				mrBundleFiles.add(storage.createNestedBundleFile(versionPath, bundlefile, generation,
						BundleInfo.MULTI_RELEASE_FILTER_PREFIXES));
			}
		}
		return Collections.unmodifiableList(mrBundleFiles);
	}

    /**
	 * Returns the source BundleFile for this classpath entry
	 * 
	 * @return the source BundleFile for this classpath entry
	 */
	public BundleFile getBundleFile() {
		return bundlefile;
	}

	/**
	 * Returns the ProtectionDomain for this classpath entry
	 * 
	 * @return the ProtectionDomain for this classpath entry
	 */
	public ProtectionDomain getDomain() {
		return domain;
	}

    private static Manifest loadManifest(BundleFile cpBundleFile, Generation generation) {
		if (!generation.hasPackageInfo() && generation.getBundleFile() == cpBundleFile) {
			return null;
		}
		BundleEntry mfEntry = cpBundleFile.getEntry(BundleInfo.OSGI_BUNDLE_MANIFEST);
		if (mfEntry != null) {
			InputStream manIn = null;
			try {
				try {
					manIn = mfEntry.getInputStream();
					return new Manifest(manIn);
				} finally {
					if (manIn != null)
						manIn.close();
				}
			} catch (IOException e) {
				// do nothing
			}
		}
		return null;
	}

    public void close() throws IOException {
		bundlefile.close();
		for (BundleFile bf : mrBundleFiles) {
			bf.close();
		}
	}
}
