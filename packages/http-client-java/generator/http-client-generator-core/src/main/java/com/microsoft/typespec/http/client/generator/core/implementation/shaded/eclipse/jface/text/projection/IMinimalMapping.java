/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jface.text.projection;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jface.text.BadLocationException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jface.text.IRegion;

/**
 * Internal interface for defining the exact subset of
 * {@link ProjectionMapping} that the
 * {@link ProjectionTextStore} is allowed to
 * access.
 *
 * @since 3.0
 */
interface IMinimalMapping {

    /*
     * @see org.eclipse.jface.text.IDocumentInformationMapping#getCoverage()
     */
    IRegion getCoverage();

    /*
     * @see org.eclipse.jface.text.IDocumentInformationMapping#toOriginRegion(IRegion)
     */
    IRegion toOriginRegion(IRegion region) throws BadLocationException;

    /*
     * @see org.eclipse.jface.text.IDocumentInformationMapping#toOriginOffset(int)
     */
    int toOriginOffset(int offset) throws BadLocationException;

    /*
     * @see org.eclipse.jface.text.IDocumentInformationMappingExtension#toExactOriginRegions(IRegion)
     */
    IRegion[] toExactOriginRegions(IRegion region) throws BadLocationException;

    /*
     * @see org.eclipse.jface.text.IDocumentInformationMappingExtension#getImageLength()
     */
    int getImageLength();
}
