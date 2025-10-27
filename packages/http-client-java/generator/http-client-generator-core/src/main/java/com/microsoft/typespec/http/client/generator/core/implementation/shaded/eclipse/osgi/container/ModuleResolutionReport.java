/*******************************************************************************
 * Copyright (c) 2013, 2021 IBM Corporation and others.
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

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.messages.Msg;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.report.resolution.ResolutionReport;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.wiring.BundleRevision;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.resource.Capability;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.resource.Requirement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.resource.Resource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.resource.Wire;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.resolver.ResolutionException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A resolution report implementation used by the container for resolution
 * operations.
 * 
 * @since 3.10
 */
class ModuleResolutionReport implements ResolutionReport {

	static class Builder {
		private final Map<Resource, List<Entry>> resourceToEntries = new HashMap<>();

		public void addEntry(Resource resource, Entry.Type type, Object data) {
            List<Entry> entries = resourceToEntries.computeIfAbsent(resource, k -> new ArrayList<>());
            entries.add(new EntryImpl(type, data));
		}

		public ModuleResolutionReport build(Map<Resource, List<Wire>> resolutionResult, ResolutionException cause,
            ModuleContainerAdaptor adaptor) {
			return new ModuleResolutionReport(resolutionResult, resourceToEntries, cause, adaptor);
		}

	}

	static class EntryImpl implements Entry {
		private final Object data;
		private final Type type;

		EntryImpl(Type type, Object data) {
			this.type = type;
			this.data = data;
		}

		@Override
		public Object getData() {
			return data;
		}

		@Override
		public Type getType() {
			return type;
		}
	}

	private final Map<Resource, List<Entry>> entries;
	private final ResolutionException resolutionException;
	private final Map<Resource, List<Wire>> resolutionResult;
    private final boolean printOptional;

	ModuleResolutionReport(Map<Resource, List<Wire>> resolutionResult, Map<Resource, List<Entry>> entries,
			ResolutionException cause, ModuleContainerAdaptor adaptor) {
        this.printOptional = Boolean.parseBoolean(adaptor.getProperty("equinox.resolver.report.printOptional")); //$NON-NLS-1$
		this.entries = entries == null ? Collections.emptyMap() : Collections.unmodifiableMap(new HashMap<>(entries));
		this.resolutionResult = resolutionResult == null ? Collections.emptyMap()
				: Collections.unmodifiableMap(resolutionResult);
		this.resolutionException = cause;
	}

	@Override
	public Map<Resource, List<Entry>> getEntries() {
		return entries;
	}

	@Override
	public ResolutionException getResolutionException() {
		return resolutionException;
	}

	Map<Resource, List<Wire>> getResolutionResult() {
		return resolutionResult;
	}

	private static String getResolutionReport0(String prepend, ModuleRevision revision,
			Map<Resource, List<Entry>> reportEntries, Set<BundleRevision> visited,
			boolean printOptional) {
		if (prepend == null) {
			prepend = ""; //$NON-NLS-1$
		}
		if (visited == null) {
			visited = new HashSet<>();
		}
		if (visited.contains(revision)) {
			return ""; //$NON-NLS-1$
		}
		visited.add(revision);
		StringBuilder result = new StringBuilder();
		String id = revision.getRevisions().getModule().getId().toString();
		result.append(prepend).append(revision.getSymbolicName()).append(" [").append(id).append("]").append('\n'); //$NON-NLS-1$ //$NON-NLS-2$

		List<Entry> revisionEntries = reportEntries.get(revision);
		if (revisionEntries == null) {
			result.append(prepend).append("  ").append(Msg.ModuleResolutionReport_NoReport); //$NON-NLS-1$
		} else {
			for (Entry entry : revisionEntries) {
				printResolutionEntry(result, prepend + "  ", entry, reportEntries, visited, printOptional); //$NON-NLS-1$
			}
		}
		return result.toString();
	}

	private static void printResolutionEntry(StringBuilder result, String prepend, Entry entry,
			Map<Resource, List<Entry>> reportEntries, Set<BundleRevision> visited,
			boolean printOptional) {
		switch (entry.getType()) {
		case MISSING_CAPABILITY: {
			Requirement requirement = (Requirement) entry.getData();
			if (!printOptional && ModuleRequirement.isOptional(requirement)) {
				return;
			}
			result.append(prepend).append(Msg.ModuleResolutionReport_UnresolvedReq)
					.append(ModuleContainer.toString(requirement)).append('\n');
		}
			break;
		case SINGLETON_SELECTION:
			result.append(prepend).append(Msg.ModuleResolutionReport_AnotherSingleton).append(entry.getData())
					.append('\n');
			break;
		case UNRESOLVED_PROVIDER: {
			@SuppressWarnings("unchecked")
			Map<Requirement, Set<Capability>> unresolvedProviders = (Map<Requirement, Set<Capability>>) entry.getData();
			for (Map.Entry<Requirement, Set<Capability>> unresolvedRequirement : unresolvedProviders.entrySet()) {
				// for now only printing the first possible unresolved candidates
				Set<Capability> unresolvedCapabilities = unresolvedRequirement.getValue();
				Requirement requirement = unresolvedRequirement.getKey();
				if (!printOptional && ModuleRequirement.isOptional(requirement)) {
					continue;
				}
				if (!unresolvedCapabilities.isEmpty()) {
					Capability unresolvedCapability = unresolvedCapabilities.iterator().next();
					// make sure this is not a case of importing and exporting the same package
					if (!unresolvedRequirement.getKey().getResource().equals(unresolvedCapability.getResource())) {
						result.append(prepend).append(Msg.ModuleResolutionReport_UnresolvedReq)
								.append(ModuleContainer.toString(requirement)).append('\n');
						result.append(prepend).append("  -> ") //$NON-NLS-1$
								.append(ModuleContainer.toString(unresolvedCapability)).append('\n');
						result.append(getResolutionReport0(prepend + "     ", //$NON-NLS-1$
								(ModuleRevision) unresolvedCapability.getResource(), reportEntries, visited,
								printOptional));
					}
				}
			}
		}
			break;
		case FILTERED_BY_RESOLVER_HOOK:
			result.append(Msg.ModuleResolutionReport_FilteredByHook).append('\n');
			break;
		case USES_CONSTRAINT_VIOLATION:
			result.append(prepend).append(Msg.ModuleResolutionReport_UsesConstraintError).append('\n');
			result.append("  ").append(entry.getData()); //$NON-NLS-1$
			break;
		default:
			result.append(Msg.ModuleResolutionReport_Unknown).append("type=").append(entry.getType()).append(" data=") //$NON-NLS-1$ //$NON-NLS-2$
					.append(entry.getData()).append('\n');
			break;
		}
	}

	@Override
	public String getResolutionReportMessage(Resource resource) {
		return getResolutionReport0(null, (ModuleRevision) resource, getEntries(), null, printOptional);
	}

}
