/*******************************************************************************
 * Copyright (c) 2006, 2016 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.signedcontent;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.framework.log.FrameworkLogEntry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.framework.util.SecureAction;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.framework.EquinoxBundle;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.framework.EquinoxConfiguration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.framework.EquinoxContainer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.hookregistry.HookConfigurator;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.signedcontent.SignedContentFromBundleFile.BaseSignerInfo;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.service.security.TrustEngine;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.signedcontent.SignedContent;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.signedcontent.SignedContentFactory;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.signedcontent.SignerInfo;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage.BundleInfo.Generation;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Bundle;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.BundleContext;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Constants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Filter;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.InvalidSyntaxException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.ServiceReference;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.util.tracker.ServiceTracker;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.util.tracker.ServiceTrackerCustomizer;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.cert.Certificate;

/**
 * Implements signed bundle hook support for the framework
 */
public class SignedBundleHook implements HookConfigurator, SignedContentFactory {
    static final SecureAction secureAction = AccessController.doPrivileged(SecureAction.createSecureAction());

    TrustEngineListener trustEngineListener;
    private String trustEngineNameProp;
    private ServiceTracker<TrustEngine, TrustEngine> trustEngineTracker;
    private BundleContext context;
    private EquinoxContainer container;

    BundleContext getContext() {
        return context;
    }

    @Override
    public SignedContent getSignedContent(Bundle bundle) throws IOException {
        Generation generation
            = (Generation) ((EquinoxBundle) bundle).getModule().getCurrentRevision().getRevisionInfo();
        SignedContentFromBundleFile signedContent = new SignedContentFromBundleFile(generation.getBundleFile());
        determineTrust(signedContent);
        return signedContent;
    }

    public void log(String msg, int severity, Throwable t) {
        container.getLogServices().log(EquinoxContainer.NAME, severity, msg, t);
    }

    private TrustEngine[] getTrustEngines() {
        // find all the trust engines available
        if (context == null)
            return new TrustEngine[0];
        if (trustEngineTracker == null) {
            // read the trust provider security property
            Filter filter = null;
            if (trustEngineNameProp != null)
                try {
                    filter = context.createFilter("(&(" + Constants.OBJECTCLASS + "=" + TrustEngine.class.getName() //$NON-NLS-1$ //$NON-NLS-2$
                        + ")(" + SignedContentConstants.TRUST_ENGINE + "=" + trustEngineNameProp + "))"); //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
                } catch (InvalidSyntaxException e) {
                    log("Invalid trust engine filter", FrameworkLogEntry.WARNING, e); //$NON-NLS-1$
                }
            if (filter != null) {
                trustEngineTracker = new ServiceTracker<>(context, filter, new TrustEngineCustomizer());
            } else
                trustEngineTracker
                    = new ServiceTracker<>(context, TrustEngine.class.getName(), new TrustEngineCustomizer());
            trustEngineTracker.open();
        }
        Object[] services = trustEngineTracker.getServices();
        if (services != null) {
            TrustEngine[] engines = new TrustEngine[services.length];
            System.arraycopy(services, 0, engines, 0, services.length);
            return engines;
        }
        return new TrustEngine[0];
    }

    class TrustEngineCustomizer implements ServiceTrackerCustomizer<TrustEngine, TrustEngine> {

        @Override
        public TrustEngine addingService(ServiceReference<TrustEngine> reference) {
            TrustEngine engine = getContext().getService(reference);
            if (engine != null) {
                try {
                    Field trustEngineListenerField = TrustEngine.class.getDeclaredField("trustEngineListener"); //$NON-NLS-1$
                    trustEngineListenerField.setAccessible(true);
                    trustEngineListenerField.set(engine, SignedBundleHook.this.trustEngineListener);
                } catch (Exception e) {
                    log("Unable to set the trust engine listener.", FrameworkLogEntry.ERROR, e); //$NON-NLS-1$
                }

            }
            return engine;
        }

        @Override
        public void removedService(ServiceReference<TrustEngine> reference, TrustEngine service) {
            // nothing
        }

    }

    void determineTrust(SignedContentFromBundleFile trustedContent) {
        TrustEngine[] engines = null;
        SignerInfo[] signers = trustedContent.getSignerInfos();
        for (SignerInfo signer : signers) {
            // first check if we need to find an anchor
            if (signer.getTrustAnchor() == null) {
                // no anchor set ask the trust engines
                if (engines == null)
                    engines = getTrustEngines();
                // check trust of singer certs
                Certificate[] signerCerts = signer.getCertificateChain();
                ((BaseSignerInfo) signer).setTrustAnchor(findTrustAnchor(signerCerts, engines));
                // if signer has a tsa check trust of tsa certs
                SignerInfo tsaSignerInfo = trustedContent.getTSASignerInfo(signer);
                if (tsaSignerInfo != null) {
                    Certificate[] tsaCerts = tsaSignerInfo.getCertificateChain();
                    ((BaseSignerInfo) tsaSignerInfo).setTrustAnchor(findTrustAnchor(tsaCerts, engines));
                }
            }
        }
    }

    private Certificate findTrustAnchor(Certificate[] certs, TrustEngine[] engines) {
        if ((EquinoxConfiguration.SIGNED_CONTENT_VERIFY_TRUST) == 0)
            // we are not searching the engines; in this case we just assume the root cert
            // is trusted
            return certs != null && certs.length > 0 ? certs[certs.length - 1] : null;
        for (TrustEngine engine : engines) {
            try {
                Certificate anchor = engine.findTrustAnchor(certs);
                if (anchor != null)
                    // found an anchor
                    return anchor;
            } catch (IOException e) {
                // log the exception and continue
                log("TrustEngine failure: " + engine.getName(), FrameworkLogEntry.WARNING, e); //$NON-NLS-1$
            }
        }
        return null;
    }
}
