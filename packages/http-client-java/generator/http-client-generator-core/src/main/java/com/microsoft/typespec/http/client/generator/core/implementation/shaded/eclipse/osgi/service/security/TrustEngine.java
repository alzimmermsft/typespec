/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.service.security;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.signedcontent.TrustEngineListener;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;

/**
 * A <code>TrustEngine</code> is used to establish the authenticity of a
 * {@link Certificate} chain.
 * <p>
 * Clients may implement this interface.
 * </p>
 * 
 * @since 3.4
 */
public abstract class TrustEngine {
    /**
     * Returns the certificate trust anchor contained in the specified chain which
     * was used to establish the authenticity of the chain. If no trust anchor is
     * found in the chain then <code>null</code> is returned.
     * 
     * @param chain - a complete or incomplete certificate chain, implementations
     * *MAY* complete chains
     * @return - the certificate trust anchor used to establish authenticity
     * @throws IOException if there is a problem connecting to the backing store
     */
    public abstract Certificate findTrustAnchor(Certificate[] chain) throws IOException;

    /**
     * Remove a trust anchor point from the engine, based on the human readable
     * "friendly name"
     * 
     * @param alias - the name of the trust anchor
     * @throws IOException if there is a problem connecting to the
     * backing store
     * @throws GeneralSecurityException if there is a certificate problem
     */
    public void removeTrustAnchor(String alias) throws IOException, GeneralSecurityException {
        Certificate existing = getTrustAnchor(alias);
        doRemoveTrustAnchor(alias);
        if (existing != null) {
            TrustEngineListener listener = trustEngineListener;
            if (listener != null)
                listener.removedTrustAnchor(existing);
        }
    }

    /**
     * Remove a trust anchor point from the engine, based on the human readable
     * "friendly name"
     * 
     * @param alias - the name of the trust anchor
     * @throws IOException if there is a problem connecting to the
     * backing store
     * @throws GeneralSecurityException if there is a certificate problem
     */
    protected abstract void doRemoveTrustAnchor(String alias) throws IOException, GeneralSecurityException;

    /**
     * Return the certificate associated with the unique "friendly name" in the
     * engine.
     * 
     * @param alias - the friendly name
     * @return the associated trust anchor
     * @throws IOException if there is a problem connecting to the
     * backing store
     * @throws GeneralSecurityException if there is a certificate problem
     */
    public abstract Certificate getTrustAnchor(String alias) throws IOException, GeneralSecurityException;

    /**
     * Return the list of friendly name aliases for the TrustAnchors installed in
     * the engine.
     * 
     * @return string[] - the list of friendly name aliases
     * @throws IOException if there is a problem connecting to the
     * backing store
     * @throws GeneralSecurityException if there is a certificate problem
     */
    public abstract String[] getAliases() throws IOException, GeneralSecurityException;

    /**
     * Return a value indicate whether this trust engine is read-only.
     *
     * @return true if this trust engine is read-only false otherwise.
     */
    public abstract boolean isReadOnly();

    /**
     * Return a representation string of this trust engine
     *
     * @return a string
     */
    public abstract String getName();

    private volatile TrustEngineListener trustEngineListener;
}
