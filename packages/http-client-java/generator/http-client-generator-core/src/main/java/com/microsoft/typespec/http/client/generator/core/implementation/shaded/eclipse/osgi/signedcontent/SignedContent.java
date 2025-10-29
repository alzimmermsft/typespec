/*******************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.signedcontent;

/**
 * A <code>SignedContent</code> object represents content which may be signed. A
 * {@link SignedContentFactory} is used to create signed content objects.
 * <p>
 * A <code>SignedContent</code> object is intended to provide information about
 * the signers of the content, and cannot be used to access the actual data of
 * the content.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @since 3.4
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface SignedContent {

    /**
     * Returns all the signer infos for this <code>SignedContent</code>. If the
     * content is not signed then an empty array is returned.
     * 
     * @return all the signer infos for this <code>SignedContent</code>
     */
    SignerInfo[] getSignerInfos();

    /**
     * Returns true if the content is signed; false otherwise. This is a convenience
     * method equivalent to calling
     * <code>{@link #getSignerInfos()}.length &gt; 0</code>
     * 
     * @return true if the content is signed
     */
    boolean isSigned();

    /**
     * Returns the TSA signer info used to authenticate the signer time of a signer
     * info.
     * 
     * @param signerInfo the signer info to get the TSA signer for
     * @return the TSA signer info
     */
    SignerInfo getTSASignerInfo(SignerInfo signerInfo);

}
