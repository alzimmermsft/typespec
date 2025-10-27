/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.felix.resolver;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.felix.resolver.Candidates.FaultyResourcesReport;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.felix.resolver.util.ArrayMap;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.namespace.BundleNamespace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.namespace.ExecutionEnvironmentNamespace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.namespace.HostNamespace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.namespace.IdentityNamespace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.namespace.PackageNamespace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.resource.Capability;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.resource.Requirement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.resource.Resource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.resource.Wire;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.resource.Wiring;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.resolver.HostedCapability;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.resolver.ResolutionException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.resolver.ResolveContext;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.resolver.Resolver;

public class ResolverImpl implements Resolver
{
    private final AccessControlContext m_acc =
        System.getSecurityManager() != null ?
            AccessController.getContext() :
            null;

    private final int m_parallelism;

    private final Executor m_executor;

    public ResolverImpl(Executor executor)
    {
        this.m_parallelism = -1;
        this.m_executor = executor;
    }

    public Map<Resource, List<Wire>> resolve(ResolveContext rc) throws ResolutionException
    {
        if (m_executor != null)
        {
            return resolve(rc, m_executor);
        }
        else if (m_parallelism > 1)
        {
            final ExecutorService executor =
                System.getSecurityManager() != null ?
                    AccessController.doPrivileged(
                        (PrivilegedAction<ExecutorService>) () -> Executors.newFixedThreadPool(m_parallelism), m_acc)
                :
                    Executors.newFixedThreadPool(m_parallelism);
            try
            {
                return resolve(rc, executor);
            }
            finally
            {
                if (System.getSecurityManager() != null)
                {
                    AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
                        executor.shutdownNow();
                        return null;
                    }, m_acc);
                }
                else
                {
                    executor.shutdownNow();
                }
            }
        }
        else
        {
            return resolve(rc, new DumbExecutor());
        }
    }

    public Map<Resource, List<Wire>> resolve(ResolveContext rc, Executor executor) throws ResolutionException
    {
        ResolveSession session = ResolveSession.createSession(rc, executor, null, null, null);
        return doResolve(session);
    }

    private Map<Resource, List<Wire>> doResolve(ResolveSession session) throws ResolutionException {
        Map<Resource, List<Wire>> wireMap = new HashMap<>();
        boolean retry;
        do
        {
            retry = false;
            try
            {
                getInitialCandidates(session);

                Map<Resource, ResolutionError> faultyResources = new HashMap<>();
                Candidates allCandidates = findValidCandidates(session, faultyResources);
                session.checkForCancel();

                // If there is a resolve exception, then determine if an
                // optionally resolved resource is to blame (typically a fragment).
                // If so, then remove the optionally resolved resolved and try
                // again; otherwise, m_currentError the resolve exception.
                if (session.getCurrentError() != null)
                {
                    Set<Resource> resourceKeys = faultyResources.keySet();
                    retry = (session.getOptionalResources().removeAll(resourceKeys));
                    for (Resource faultyResource : resourceKeys)
                    {
                        if (session.invalidateRelatedResource(faultyResource))
                        {
                            retry = true;
                        }
                    }
                    // log all the resolution exceptions for the uses constraint violations
                    for (Entry<Resource, ResolutionError> usesError : faultyResources.entrySet())
                    {
                    }
                    if (!retry)
                    {
                        throw session.getCurrentError().toException();
                    }
                }
                // If there is no exception to m_currentError, then this was a clean
                // resolve, so populate the wire map.
                else
                {
                    if (session.getMultipleCardCandidates() != null)
                    {
                        // Candidates for multiple cardinality requirements were
                        // removed in order to provide a consistent class space.
                        // Use the consistent permutation
                        allCandidates = session.getMultipleCardCandidates();
                    }
                    if (session.isDynamic() )
                    {
                        wireMap = populateDynamicWireMap(session,
                            wireMap, allCandidates);
                    }
                    else
                    {
                        for (Resource resource : allCandidates.getRootHosts().keySet())
                        {
                            if (allCandidates.isPopulated(resource))
                            {
                                wireMap =
                                    populateWireMap(
                                        session, allCandidates.getWrappedHost(resource),
                                        wireMap, allCandidates);
                            }
                        }
                    }
                }
            }
            finally
            {
                // Always clear the state.
                session.clearPermutations();
            }
        }
        while (retry);

        return wireMap;
    }

    private void getInitialCandidates(ResolveSession session) throws ResolutionException {
        // Create object to hold all candidates.
        Candidates initialCandidates;
        if (session.isDynamic()) {
            // Create all candidates pre-populated with the single candidate set
            // for the resolving dynamic import of the host.
            initialCandidates = new Candidates(session);
            ResolutionError prepareError = initialCandidates.populateDynamic();
            if (prepareError != null) {
                throw prepareError.toException();
            }
        } else {
            List<Resource> toPopulate = new ArrayList<>();

            // Populate mandatory resources; since these are mandatory
            // resources, failure throws a resolve exception.
            for (Resource resource : session.getMandatoryResources())
            {
                if (Util.isFragment(resource) || (session.getContext().getWirings().get(resource) == null))
                {
                    toPopulate.add(resource);
                }
            }
            // Populate optional resources; since these are optional
            // resources, failure does not throw a resolve exception.
            for (Resource resource : session.getOptionalResources())
            {
                if (Util.isFragment(resource) || (session.getContext().getWirings().get(resource) == null))
                {
                    toPopulate.add(resource);
                }
            }

            initialCandidates = new Candidates(session);
            initialCandidates.populate(toPopulate);
        }

        // Merge any fragments into hosts.
        ResolutionError prepareError = initialCandidates.prepare();
        if (prepareError != null)
        {
            throw prepareError.toException();
        }
        else
        {
            // Record the initial candidate permutation.
            session.addPermutation(PermutationType.USES, initialCandidates);
        }
    }

    private Candidates findValidCandidates(ResolveSession session, Map<Resource, ResolutionError> faultyResources) {
        Backlog backlog = new Backlog(session);
        Candidates current = Objects.requireNonNull(backlog.getNext());
        boolean foundFaultyResources = false;
        Candidates bestCandidate = null;
        ResolutionError bestError = null;
        FaultyResourcesReport bestReport = null;

        while (!session.isCancelled()) {
            Map<Resource, ResolutionError> currentFaultyResources = new HashMap<>();
            ResolutionError consistency = PackageSpaces.checkConsistency(session, current, currentFaultyResources);
            session.setCurrentError(consistency);
            if (!currentFaultyResources.isEmpty()) {
                if (!foundFaultyResources) {
                    foundFaultyResources = true;
                    faultyResources.putAll(currentFaultyResources);
                } else if (faultyResources.size() > currentFaultyResources.size()) {
                    // save the optimal faultyResources which has less
                    faultyResources.clear();
                    faultyResources.putAll(currentFaultyResources);
                }
            }
            FaultyResourcesReport report = current.getFaultyResources(currentFaultyResources);
            if (consistency == null && report.getUnresolvedRequirements().isEmpty()) {
                // Success!
                break;
            }
            if (bestCandidate == null || report.isBetterThan(bestReport)) {
                bestCandidate = current;
                bestReport = report;
                if (consistency == null) {
                    if (report.isMissingMandatory()) {
                        bestError = report;
                    } else {
                        bestError = null;
                    }
                } else {
                    bestError = consistency;
                }
            }
            Candidates next = backlog.getNext();
            if (next == null)
            {
                // nothing more, return the best we found
                session.setCurrentError(bestError);
                return bestCandidate;
            }
            current = next;
        }
        return current;
    }



    public Map<Resource,List<Wire>> resolveDynamic(ResolveContext context,
            Wiring hostWiring, Requirement dynamicRequirement)
            throws ResolutionException
    {
        Resource host = hostWiring.getResource();
        List<Capability> matches = context.findProviders(dynamicRequirement);
        // We can only create a dynamic import if the following
        // conditions are met:
        // 1. The package in question is not already imported.
        // 2. The package in question is not accessible via require-bundle.
        // 3. The package in question is not exported by the resource.
        if (!matches.isEmpty())
        {
            // Make sure all matching candidates are packages.
            for (Capability cap : matches)
            {
                if (!cap.getNamespace().equals(PackageNamespace.PACKAGE_NAMESPACE))
                {
                    throw new IllegalArgumentException(
                        "Matching candidate does not provide a package name.");
                }
            }
            ResolveSession session = ResolveSession.createSession(context, new DumbExecutor(), host, dynamicRequirement,
                    matches);
            return doResolve(session);
        }

        throw new Candidates.MissingRequirementError(dynamicRequirement).toException();
    }

    private static Resource getDeclaredResource(Resource resource)
    {
        if (resource instanceof WrappedResource)
        {
            return ((WrappedResource) resource).getDeclaredResource();
        }
        return resource;
    }

    private static Capability getDeclaredCapability(Capability c)
    {
        if (c instanceof HostedCapability)
        {
            return ((HostedCapability) c).getDeclaredCapability();
        }
        return c;
    }

    static Requirement getDeclaredRequirement(Requirement r)
    {
        if (r instanceof WrappedRequirement)
        {
            return ((WrappedRequirement) r).getDeclaredRequirement();
        }
        return r;
    }

    private static Map<Resource, List<Wire>> populateWireMap(
        ResolveSession session, Resource resource,
        Map<Resource, List<Wire>> wireMap, Candidates allCandidates)
    {
        Resource unwrappedResource = getDeclaredResource(resource);
        if (!session.getContext().getWirings().containsKey(unwrappedResource)
            && !wireMap.containsKey(unwrappedResource))
        {
            wireMap.put(unwrappedResource, Collections.<Wire>emptyList());

            List<Wire> packageWires = new ArrayList<>();
            List<Wire> bundleWires = new ArrayList<>();
            List<Wire> capabilityWires = new ArrayList<>();

            for (Requirement req : resource.getRequirements(null))
            {
                List<Capability> cands = allCandidates.getCandidates(req);
                if ((cands != null) && (cands.size() > 0))
                {
                    for (Capability cand : cands)
                    {
                        // Do not create wires for the osgi.wiring.* namespaces
                        // if the provider and requirer are the same resource;
                        // allow such wires for non-OSGi wiring namespaces.
                        if (!cand.getNamespace().startsWith("osgi.wiring.")
                            || !resource.equals(cand.getResource()))
                        {
                            // Populate wires for the candidate
                            populateWireMap(session, cand.getResource(),
                                    wireMap, allCandidates);

                            Resource provider;
                            if (req.getNamespace().equals(IdentityNamespace.IDENTITY_NAMESPACE)) {
                                provider = getDeclaredCapability(cand).getResource();
                            } else {
                                provider = getDeclaredResource(cand.getResource());
                            }
                            Wire wire = new WireImpl(
                                unwrappedResource,
                                getDeclaredRequirement(req),
                                provider,
                                getDeclaredCapability(cand));
                            if (req.getNamespace().equals(PackageNamespace.PACKAGE_NAMESPACE))
                            {
                                packageWires.add(wire);
                            }
                            else if (req.getNamespace().equals(BundleNamespace.BUNDLE_NAMESPACE))
                            {
                                bundleWires.add(wire);
                            }
                            else
                            {
                                capabilityWires.add(wire);
                            }
                        }
                        if (!Util.isMultiple(req))
                        {
                            // If not multiple just create a wire for the first candidate.
                            break;
                        }
                    }
                }
            }

            // Combine package wires with require wires last.
            packageWires.addAll(bundleWires);
            packageWires.addAll(capabilityWires);
            wireMap.put(unwrappedResource, packageWires);

            // Add host wire for any fragments.
            if (resource instanceof WrappedResource)
            {
                List<Resource> fragments = ((WrappedResource) resource).getFragments();
                for (Resource fragment : fragments)
                {
                    // Get wire list for the fragment from the wire map.
                    // If there isn't one, then create one. Note that we won't
                    // add the wire list to the wire map until the end, so
                    // we can determine below if this is the first time we've
                    // seen the fragment while populating wires to avoid
                    // creating duplicate non-payload wires if the fragment
                    // is attached to more than one host.
                    List<Wire> fragmentWires = wireMap.get(fragment);
                    fragmentWires = (fragmentWires == null)
                        ? new ArrayList<>() : fragmentWires;

                    // Loop through all of the fragment's requirements and create
                    // any necessary wires for non-payload requirements.
                    for (Requirement req : fragment.getRequirements(null))
                    {
                        // Only look at non-payload requirements.
                        if (!isPayload(req))
                        {
                            // If this is the host requirement, then always create
                            // a wire for it to the current resource.
                            if (req.getNamespace().equals(HostNamespace.HOST_NAMESPACE))
                            {
                                fragmentWires.add(
                                    new WireImpl(
                                        getDeclaredResource(fragment),
                                        req,
                                        unwrappedResource,
                                        unwrappedResource.getCapabilities(
                                            HostNamespace.HOST_NAMESPACE).get(0)));
                            }
                            // Otherwise, if the fragment isn't already resolved and
                            // this is the first time we are seeing it, then create
                            // a wire for the non-payload requirement.
                            else if (!session.getContext().getWirings().containsKey(fragment)
                                && !wireMap.containsKey(fragment))
                            {
                                Wire wire = createWire(req, allCandidates);
                                if (wire != null)
                                {
                                    fragmentWires.add(wire);
                                }
                            }
                        }
                    }

                    // Finally, add the fragment's wire list to the wire map.
                    wireMap.put(fragment, fragmentWires);
                }
            }
            // now make sure any related resources are populated
            for (Resource related : session.getRelatedResources(unwrappedResource)) {
                if (allCandidates.isPopulated(related)) {
                    populateWireMap(session, related, wireMap, allCandidates);
                }
            }
        }

        return wireMap;
    }

    private static Wire createWire(Requirement requirement, Candidates allCandidates)
    {
        Capability cand = allCandidates.getFirstCandidate(requirement);
        if (cand == null) {
            return null;
        }
        return new WireImpl(
            getDeclaredResource(requirement.getResource()),
            getDeclaredRequirement(requirement),
            getDeclaredResource(cand.getResource()),
            getDeclaredCapability(cand));
    }

    private static boolean isPayload(Requirement fragmentReq)
    {
        // this is where we would add other non-payload namespaces
        if (ExecutionEnvironmentNamespace.EXECUTION_ENVIRONMENT_NAMESPACE
            .equals(fragmentReq.getNamespace()))
        {
            return false;
        }
        return !HostNamespace.HOST_NAMESPACE.equals(fragmentReq.getNamespace());
    }

    private static Map<Resource, List<Wire>> populateDynamicWireMap(
        ResolveSession session, Map<Resource,
        List<Wire>> wireMap, Candidates allCandidates)
    {
        wireMap.put(session.getDynamicHost(), Collections.<Wire>emptyList());

        List<Wire> packageWires = new ArrayList<>();

        // Get the candidates for the current dynamic requirement.
        // Record the dynamic candidate.
        Capability dynCand = allCandidates.getFirstCandidate(session.getDynamicRequirement());

        if (!session.getContext().getWirings().containsKey(dynCand.getResource()))
        {
            populateWireMap(session, dynCand.getResource(),
                wireMap, allCandidates);
        }

        packageWires.add(
            new WireImpl(
                session.getDynamicHost(),
                session.getDynamicRequirement(),
                getDeclaredResource(dynCand.getResource()),
                getDeclaredCapability(dynCand)));

        wireMap.put(session.getDynamicHost(), packageWires);

        return wireMap;
    }

    @SuppressWarnings("unused")
    private static void dumpResourcePkgMap(
        ResolveContext rc, Map<Resource, Packages> resourcePkgMap)
    {
        System.out.println("+++RESOURCE PKG MAP+++");
        for (Entry<Resource, Packages> entry : resourcePkgMap.entrySet())
        {
            dumpResourcePkgs(rc, entry.getKey(), entry.getValue());
        }
    }

    private static void dumpResourcePkgs(
        ResolveContext rc, Resource resource, Packages packages)
    {
        Wiring wiring = rc.getWirings().get(resource);
        System.out.println(resource
            + " (" + ((wiring != null) ? "RESOLVED)" : "UNRESOLVED)"));
        System.out.println("  EXPORTED");
        for (Entry<String, Blame> entry : packages.m_exportedPkgs.entrySet())
        {
            System.out.println("    " + entry.getKey() + " - " + entry.getValue());
        }
        System.out.println("  IMPORTED");
        for (Entry<String, List<Blame>> entry : packages.m_importedPkgs.entrySet())
        {
            System.out.println("    " + entry.getKey() + " - " + entry.getValue());
        }
        System.out.println("  REQUIRED");
        for (Entry<String, List<Blame>> entry : packages.m_requiredPkgs.entrySet())
        {
            System.out.println("    " + entry.getKey() + " - " + entry.getValue());
        }
        System.out.println("  USED");
        for (Entry<String, ArrayMap<Set<Capability>, UsedBlames>> entry : packages.m_usedPkgs.entrySet())
        {
            System.out.println("    " + entry.getKey() + " - " + entry.getValue().values());
        }
    }

}
