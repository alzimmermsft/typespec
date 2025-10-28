/*******************************************************************************
 * Copyright (c) 2025 Christoph Läubrich and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christoph Läubrich - initial API and implementation
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.builder;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.JavaProject;

/**
 * The {@link TypeLocators} maintain a mapping of type names (in the form <code>p1/p2/A</code>) to code locations (in the
 * form <code>src1/p1/p2/A.java</code>) to detect duplicate type definitions in different source folders. In the case of
 * multi-release compilation, this might allow for duplicate types if they are in distinct release folders.
 */
public class TypeLocators {

    // holds data when no release is used
	private final Map<String, String> defaultMap;

	// holds data when a release version is used:
	// 		type name -> release -> location
	private Map<String, Map<Integer, String>> releaseMap;

	TypeLocators() {
		this.defaultMap = new LinkedHashMap<>(7);
	}

    void write(CompressedWriter out, Map<String, Integer> internedTypeLocators) throws IOException {
		if (this.defaultMap.isEmpty()) {
			out.writeInt(0);
		} else {
			out.writeInt(this.defaultMap.size());
			for (Entry<String, String> entry : this.defaultMap.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				out.writeStringUsingLast(key);
				Integer index = internedTypeLocators.get(value);
				out.writeIntInRange(index.intValue(), internedTypeLocators.size());
			}
		}
		if (this.releaseMap == null || this.releaseMap.isEmpty()) {
			out.writeInt(0);
		} else {
			out.writeInt(this.releaseMap.size());
			for (var entry : this.releaseMap.entrySet()) {
				String key = entry.getKey();
				out.writeStringUsingLast(key);
				Map<Integer, String> map = entry.getValue();
				out.writeInt(map.size());
				for (var releaseEntry : map.entrySet()) {
					out.writeInt(releaseEntry.getKey());
					Integer index = internedTypeLocators.get(releaseEntry.getValue());
					out.writeIntInRange(index.intValue(), internedTypeLocators.size());
				}
			}
		}

	}

	void read(CompressedReader in, String[] internedTypeLocators) throws IOException {
		int length = in.readInt();
		this.defaultMap.clear();
		if (length > 0) {
			for (int i = 0; i < length; i++) {
				recordLocatorForType(in.readStringUsingLast(),
						internedTypeLocators[in.readIntInRange(internedTypeLocators.length)],
						JavaProject.NO_RELEASE);
			}
		}
		length = in.readInt();
		if (length == 0) {
			this.releaseMap = null;
		} else {
			this.releaseMap = new LinkedHashMap<>((int) (length / 0.75 + 1));
			for (int i = 0; i < length; i++) {
				String key = in.readStringUsingLast();
				int mapSize = in.readInt();
				for (int j = 0; j < mapSize; j++) {
					int release = in.readInt();
					String locator = internedTypeLocators[in.readIntInRange(internedTypeLocators.length)];
					recordLocatorForType(key, locator, release);
				}
			}
		}
	}

    void recordLocatorForType(String qualifiedTypeName, String typeLocator, int release) {
        int start = typeLocator.indexOf(qualifiedTypeName, 0);
		if (start > 0) {
			// in the common case, the qualifiedTypeName is a substring of the typeLocator so share the char[] by using
			// String.substring()
			qualifiedTypeName = typeLocator.substring(start, start + qualifiedTypeName.length());
		}
		if (release > JavaProject.NO_RELEASE) {
			if (this.releaseMap == null) {
				this.releaseMap = new LinkedHashMap<>(7);
			}
			this.releaseMap.computeIfAbsent(qualifiedTypeName, nil -> new TreeMap<>()).put(release, typeLocator);
		} else {
			this.defaultMap.put(qualifiedTypeName, typeLocator);
		}
	}

    boolean isDuplicateLocator(String qualifiedTypeName, String typeLocator, int release) {
		String string;
		if (release > JavaProject.NO_RELEASE) {
			if (this.releaseMap == null) {
				return false;
			}
			Map<Integer, String> existing = this.releaseMap.get(qualifiedTypeName);
			if (existing == null) {
				return false;
			}
			string = existing.get(release);
		} else {
			string = this.defaultMap.get(qualifiedTypeName);
		}
		return string != null && !string.equals(typeLocator);
	}

    /**
	 * Only implemented for StateTest! one usually won't use {@link TypeLocators} in a way where equals/hashCode really
	 * matters
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TypeLocators other) {
			if (!this.defaultMap.equals(other.defaultMap)) {
				return false;
			}
			if (!Objects.requireNonNullElse(this.releaseMap, Map.of())
					.equals(Objects.requireNonNullElse(other.releaseMap, Map.of()))) {
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * Only implemented for StateTest! one usually won't use {@link TypeLocators} in a way where equals/hashCode really
	 * matters
	 */
	@Override
	public int hashCode() {
		return 0;
	}
}
