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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.framework;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.Module;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.Module.State;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.ModuleRevision;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.ModuleWire;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.ModuleWiring;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.permadmin.EquinoxSecurityManager;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.report.resolution.ResolutionReport;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.signedcontent.SignedContent;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.signedcontent.SignedContentFactory;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.signedcontent.SignerInfo;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage.BundleInfo.Generation;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage.Storage;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.AdaptPermission;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.AdminPermission;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Bundle;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.BundleContext;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.BundleReference;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.ServiceReference;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Version;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.dto.BundleDTO;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.dto.FrameworkDTO;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.dto.ServiceReferenceDTO;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.launch.Framework;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.namespace.HostNamespace;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.startlevel.BundleStartLevel;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.startlevel.FrameworkStartLevel;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.startlevel.dto.BundleStartLevelDTO;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.startlevel.dto.FrameworkStartLevelDTO;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.wiring.BundleRevision;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.wiring.BundleRevisions;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.wiring.BundleWiring;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.wiring.FrameworkWiring;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.wiring.dto.BundleRevisionDTO;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.wiring.dto.BundleWiringDTO;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.wiring.dto.FrameworkWiringDTO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Permission;
import java.security.ProtectionDomain;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EquinoxBundle implements Bundle, BundleReference {

    private EquinoxContainer equinoxContainer;
    private Module module;
    private final Object monitor = new Object();
    private BundleContextImpl context;
    private volatile SignedContent signedContent;

    @Override
    public int compareTo(Bundle bundle) {
        long idcomp = getBundleId() - bundle.getBundleId();
        return (idcomp < 0L) ? -1 : ((idcomp > 0L) ? 1 : 0);
    }

    @Override
    public int getState() {
        switch (module.getState()) {
            case INSTALLED:
                return Bundle.INSTALLED;

            case RESOLVED:
                return Bundle.RESOLVED;

            case STARTING:
            case LAZY_STARTING:
                return Bundle.STARTING;

            case ACTIVE:
                return Bundle.ACTIVE;

            case STOPPING:
                return Bundle.STOPPING;

            case UNINSTALLED:
                return Bundle.UNINSTALLED;

            default:
                throw new IllegalStateException("No valid bundle state for module state: " + module.getState()); //$NON-NLS-1$
        }
    }

    @Override
    public Dictionary<String, String> getHeaders(String locale) {
        equinoxContainer.checkAdminPermission(this, AdminPermission.METADATA);
        return privGetHeaders(locale);
    }

    private Dictionary<String, String> privGetHeaders(String locale) {
        Generation current = (Generation) module.getCurrentRevision().getRevisionInfo();
        return current.getHeaders(locale);
    }

    @Override
    public long getBundleId() {
        return module.getId();
    }

    @Override
    public String getLocation() {
        equinoxContainer.checkAdminPermission(getBundle(), AdminPermission.METADATA);
        return module.getLocation();
    }

    @Override
    public boolean hasPermission(Object permission) {
        Generation current = (Generation) module.getCurrentRevision().getRevisionInfo();
        ProtectionDomain domain = current.getDomain();
        if (domain != null) {
            if (permission instanceof Permission) {
                SecurityManager sm = System.getSecurityManager();
                if (sm instanceof EquinoxSecurityManager) {
                    /*
                     * If the FrameworkSecurityManager is active, we need to do checks the "right"
                     * way. We can exploit our knowledge that the security context of
                     * FrameworkSecurityManager is an AccessControlContext to invoke it properly
                     * with the ProtectionDomain.
                     */
                    AccessControlContext acc = new AccessControlContext(new ProtectionDomain[] { domain });
                    try {
                        sm.checkPermission((Permission) permission, acc);
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                }
                return domain.implies((Permission) permission);
            }
            return false;
        }
        return true;
    }

    @Override
    public String getSymbolicName() {
        return module.getCurrentRevision().getSymbolicName();
    }

    @Override
    public Version getVersion() {
        return module.getCurrentRevision().getVersion();
    }

    @Override
    public URL getEntry(String path) {
        try {
            equinoxContainer.checkAdminPermission(this, AdminPermission.RESOURCE);
        } catch (SecurityException e) {
            return null;
        }
        checkValid();
        Generation current = (Generation) getModule().getCurrentRevision().getRevisionInfo();
        return current.getEntry(path);
    }

    @Override
    public long getLastModified() {
        return module.getLastModified();
    }

    @Override
    public Enumeration<URL> findEntries(String path, String filePattern, boolean recurse) {
        try {
            equinoxContainer.checkAdminPermission(this, AdminPermission.RESOURCE);
        } catch (SecurityException e) {
            return null;
        }
        checkValid();
        resolve();
        return Storage.findEntries(getGenerations(), path, filePattern, recurse ? BundleWiring.FINDENTRIES_RECURSE : 0);
    }

    @Override
    public BundleContext getBundleContext() {
        equinoxContainer.checkAdminPermission(this, AdminPermission.CONTEXT);
        return createBundleContext();
    }

    BundleContextImpl createBundleContext() {
        if (isFragment()) {
            // fragments cannot have contexts
            return null;
        }
        synchronized (this.monitor) {
            if (context == null) {
                // only create the context if we are starting, active or stopping
                // this is so that SCR can get the context for lazy-start bundles
                if (Module.ACTIVE_SET.contains(module.getState())) {
                    context = new BundleContextImpl(this, equinoxContainer);
                }
            }
            return context;
        }
    }

    private BundleContextImpl getBundleContextImpl() {
        synchronized (this.monitor) {
            return context;
        }
    }

    @Override
    public Map<X509Certificate, List<X509Certificate>> getSignerCertificates(int signersType) {
        try {
            SignedContent current = getSignedContent();
            SignerInfo[] infos = current == null ? null : current.getSignerInfos();
            if (infos.length == 0)
                return Collections.emptyMap();
            Map<X509Certificate, List<X509Certificate>> results = new HashMap<>(infos.length);
            for (SignerInfo info : infos) {
                if (signersType == SIGNERS_TRUSTED && !info.isTrusted()) {
                    continue;
                }
                Certificate[] certs = info.getCertificateChain();
                if (certs == null || certs.length == 0)
                    continue;
                List<X509Certificate> certChain = new ArrayList<>();
                for (Certificate cert : certs) {
                    certChain.add((X509Certificate) cert);
                }
                results.put((X509Certificate) certs[0], certChain);
            }
            return results;
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    @Override
    public final <A> A adapt(Class<A> adapterType) {
        checkAdaptPermission(adapterType);
        return adapt0(adapterType);
    }

    private void readLock() {
        equinoxContainer.getStorage().getModuleDatabase().readLock();
    }

    private void readUnlock() {
        equinoxContainer.getStorage().getModuleDatabase().readUnlock();
    }

    @SuppressWarnings("unchecked")
    private <A> A adapt0(Class<A> adapterType) {
        if (AccessControlContext.class.equals(adapterType)) {
            Generation current = (Generation) module.getCurrentRevision().getRevisionInfo();
            ProtectionDomain domain = current.getDomain();
            return (A) (domain == null ? null : new AccessControlContext(new ProtectionDomain[] { domain }));
        }

        if (BundleContext.class.equals(adapterType)) {
            try {
                return (A) getBundleContext();
            } catch (SecurityException e) {
                return null;
            }
        }

        if (BundleRevision.class.equals(adapterType)) {
            if (module.getState().equals(State.UNINSTALLED)) {
                return null;
            }
            return (A) module.getCurrentRevision();
        }

        if (BundleRevisions.class.equals(adapterType)) {
            return (A) module.getRevisions();
        }

        if (BundleStartLevel.class.equals(adapterType)) {
            return (A) module;
        }

        if (BundleWiring.class.equals(adapterType)) {
            if (module.getState().equals(State.UNINSTALLED)) {
                return null;
            }
            ModuleRevision revision = module.getCurrentRevision();
            if (revision == null) {
                return null;
            }
            return (A) revision.getWiring();
        }

        if (BundleDTO.class.equals(adapterType)) {
            // Unfortunately we need to lock here to make sure the BSN and version
            // are consistent in case of updates
            readLock();
            try {
                return (A) DTOBuilder.newBundleDTO(this);
            } finally {
                readUnlock();
            }
        }

        if (BundleStartLevelDTO.class.equals(adapterType)) {
            if (module.getState().equals(State.UNINSTALLED)) {
                return null;
            }
            return (A) DTOBuilder.newBundleStartLevelDTO(this, module);
        }

        if (BundleRevisionDTO.class.equals(adapterType)) {
            if (module.getState().equals(State.UNINSTALLED)) {
                return null;
            }
            return (A) DTOBuilder.newBundleRevisionDTO(module.getCurrentRevision());
        }

        if (BundleRevisionDTO[].class.equals(adapterType)) {
            if (module.getState().equals(State.UNINSTALLED)) {
                return null;
            }
            // No need to lock the database here since the ModuleRevisions object does the
            // proper locking for us.
            return (A) DTOBuilder.newArrayBundleRevisionDTO(module.getRevisions());
        }

        if (BundleWiringDTO.class.equals(adapterType)) {
            if (module.getState().equals(State.UNINSTALLED)) {
                return null;
            }
            readLock();
            try {
                return (A) DTOBuilder.newBundleWiringDTO(module.getCurrentRevision());
            } finally {
                readUnlock();
            }
        }

        if (BundleWiringDTO[].class.equals(adapterType)) {
            if (module.getState().equals(State.UNINSTALLED)) {
                return null;
            }
            readLock();
            try {
                return (A) DTOBuilder.newArrayBundleWiringDTO(module.getRevisions());
            } finally {
                readUnlock();
            }
        }

        if (ServiceReferenceDTO[].class.equals(adapterType)) {
            if (module.getState().equals(State.UNINSTALLED)) {
                return null;
            }
            BundleContextImpl current = getBundleContextImpl();
            ServiceReference<?>[] references
                = (current == null) ? null : equinoxContainer.getServiceRegistry().getRegisteredServices(current);
            return (A) DTOBuilder.newArrayServiceReferenceDTO(references);
        }

        if (getBundleId() == 0) {
            if (Framework.class.equals(adapterType)) {
                return (A) this;
            }

            if (FrameworkStartLevel.class.equals(adapterType)) {
                return (A) module.getContainer().getFrameworkStartLevel();
            }

            if (FrameworkWiring.class.equals(adapterType)) {
                return (A) module.getContainer().getFrameworkWiring();
            }

            if (FrameworkDTO.class.equals(adapterType)) {
                BundleContextImpl current = getBundleContextImpl();
                Map<String, String> configuration = equinoxContainer.getConfiguration().getConfiguration();
                readLock();
                try {
                    return (A) DTOBuilder.newFrameworkDTO(current, configuration);
                } finally {
                    readUnlock();
                }
            }

            if (FrameworkStartLevelDTO.class.equals(adapterType)) {
                return (A) DTOBuilder.newFrameworkStartLevelDTO(module.getContainer().getFrameworkStartLevel());
            }

            if (FrameworkWiringDTO.class.equals(adapterType)) {
                readLock();
                try {
                    Set<ModuleWiring> allWirings = new HashSet<>();
                    for (Module m : module.getContainer().getModules()) {
                        for (ModuleRevision revision : m.getRevisions().getModuleRevisions()) {
                            ModuleWiring wiring = revision.getWiring();
                            if (wiring != null) {
                                allWirings.add(wiring);
                            }
                        }
                    }
                    for (ModuleRevision revision : module.getContainer().getRemovalPending()) {
                        ModuleWiring wiring = revision.getWiring();
                        if (wiring != null) {
                            allWirings.add(wiring);
                        }
                    }
                    return (A) DTOBuilder.newFrameworkWiringDTO(allWirings);
                } finally {
                    readUnlock();
                }
            }
        }

        // Equinox extras
        if (Module.class.equals(adapterType)) {
            return (A) module;
        }
        if (ProtectionDomain.class.equals(adapterType)) {
            Generation current = (Generation) module.getCurrentRevision().getRevisionInfo();
            return (A) current.getDomain();
        }
        if (SignedContent.class.equals(adapterType)) {
            return (A) getSignedContent();
        }
        if (File.class.equals(adapterType)) {
            Generation current = (Generation) module.getCurrentRevision().getRevisionInfo();
            return (A) current.getContent();
        }
        return null;
    }

    private SignedContent getSignedContent() {
        SignedContent current = signedContent;
        if (current == null) {
            SignedContentFactory factory = equinoxContainer.getSignedContentFactory();
            if (factory == null) {
                return null;
            }
            try {
                signedContent = current = factory.getSignedContent(this);
            } catch (InvalidKeyException | SignatureException | CertificateException | NoSuchAlgorithmException
                | NoSuchProviderException | IOException e) {
                return null;
            }
        }
        return current;
    }

    /**
     * Check for permission to adapt.
     */
    private <A> void checkAdaptPermission(Class<A> adapterType) {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            return;
        }
        sm.checkPermission(new AdaptPermission(adapterType.getName(), this, AdaptPermission.ADAPT));
    }

    @Override
    public File getDataFile(String filename) {
        checkValid();
        Generation current = (Generation) module.getCurrentRevision().getRevisionInfo();
        return current.getBundleInfo().getDataFile(filename);
    }

    @Override
    public Bundle getBundle() {
        return this;
    }

    public Module getModule() {
        return module;
    }

    private void checkValid() {
        if (module.getState().equals(State.UNINSTALLED))
            throw new IllegalStateException("Bundle has been uninstalled: " + this); //$NON-NLS-1$
    }

    public boolean isFragment() {
        return (getModule().getCurrentRevision().getTypes() & BundleRevision.TYPE_FRAGMENT) != 0;
    }

    ResolutionReport resolve() {
        if (!Module.RESOLVED_SET.contains(module.getState())) {
            return module.getContainer().resolve(Collections.singletonList(module), true);
        }
        return null;
    }

    List<Generation> getGenerations() {
        List<Generation> result = new ArrayList<>();
        ModuleRevision current = getModule().getCurrentRevision();
        result.add((Generation) current.getRevisionInfo());
        ModuleWiring wiring = current.getWiring();
        if (wiring != null) {
            List<ModuleWire> hostWires = wiring.getProvidedModuleWires(HostNamespace.HOST_NAMESPACE);
            if (hostWires != null) {
                for (ModuleWire hostWire : hostWires) {
                    result.add((Generation) hostWire.getRequirer().getRevisionInfo());
                }
            }
        }
        return result;
    }

    @Override
    public String toString() {
        String name = getSymbolicName();
        if (name == null)
            name = "unknown"; //$NON-NLS-1$
        return (name + '_' + getVersion() + " [" + getBundleId() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
