/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.equinox.plurl;

import java.net.URL;

/**
 * The {@code PlurlStreamHandler} interface has public versions of the protected
 * {@link java.net.URLStreamHandler} methods.
 * <p>
 * The important differences between this interface and the
 * {@code URLStreamHandler} class are that the {@code setURL} method is absent
 * and the {@code parseURL} method takes a {@link PlurlSetter} object as the
 * first argument. Classes implementing this interface must call the
 * {@code setURL} method on the {@code PlurlSetter} object received in the
 * {@code parseURL} method instead of {@code URLStreamHandler.setURL} to avoid a
 * {@code SecurityException}.
 * 
 * @see PlurlStreamHandlerBase
 * 
 */
public interface PlurlStreamHandler {
    /**
     * Interface used by {@code PlurlStreamHandler} objects to call the
     * {@code setURL} method on the plurl proxy {@code URLStreamHandler} object.
     */
    interface PlurlSetter {
        /**
         * @see "java.net.URLStreamHandler.setURL(URL,String,String,int,String,String,String,String)"
         */
        void setURL(URL u, String protocol, String host, int port, String authority, String userInfo, String path,
            String query, String ref);
    }

    /**
     * @see "java.net.URLStreamHandler.equals(URL, URL)"
     */
    boolean equals(URL u1, URL u2);

    /**
     * @see "java.net.URLStreamHandler.hashCode(URL)"
     */
    int hashCode(URL u);

    /**
     * @see "java.net.URLStreamHandler.hostsEqual(URL, URL)"
     */
    boolean hostsEqual(URL u1, URL u2);

    /**
     * @see "java.net.URLStreamHandler.sameFile(URL, URL)"
     */
    boolean sameFile(URL u1, URL u2);

    /**
     * @see "java.net.URLStreamHandler.toExternalForm(URL)"
     */
    String toExternalForm(URL u);

    /**
     * If the plurlSetter is not {@code null}
     * 
     * @see "java.net.URLStreamHandler.setURL"
     */
    void setURL(URL u, String proto, String host, int port, String auth, String user, String path, String query,
        String ref);
}
