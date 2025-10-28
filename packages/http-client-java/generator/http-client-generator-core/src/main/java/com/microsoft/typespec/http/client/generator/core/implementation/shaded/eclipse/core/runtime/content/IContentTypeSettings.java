/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.content;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.preferences.IScopeContext;

/**
 * Gives access to the user settings for a content type.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see IContentType
 * @see IContentType#getSettings(IScopeContext)
 * @since 3.1
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IContentTypeSettings {
    /**
     * File spec type constant, indicating a file extension specification.
     */
    int FILE_EXTENSION_SPEC = 0b1000;
    /**
     * File spec type constant, indicating a file name specification.
     */
    int FILE_NAME_SPEC = 0b100;

    /**
     * File spec type constant, indicating a file pattern specification
     *
     * @since 3.7
     */
    int FILE_PATTERN_SPEC = 0b10000;

    /**
     * Returns the default charset for the corresponding content type if
     * it has been set, or
     * <code>null</code> otherwise.
     *
     * @return the default charset, or <code>null</code>
     */
    String getDefaultCharset();

    /**
     * Returns the file specifications for the corresponding content type. The type mask
     * is a bit-wise or of file specification type constants indicating the
     * file specification types of interest.
     *
     * @param type a bit-wise or of file specification type constants. Valid
     * flags are one of <code>FILE_EXTENSION_SPEC</code> or
     * <code>FILE_NAME_SPEC</code>
     * @return the file specification
     * @see #FILE_NAME_SPEC
     * @see #FILE_EXTENSION_SPEC
     */
    String[] getFileSpecs(int type);

    /**
     * Returns the corresponding content type's unique identifier. Each content
     * type has an identifier by which they can be retrieved from the content
     * type catalog.
     *
     * @return the content type unique identifier
     */
    String getId();

    /**
     * @return whether the content-type was defined via user action
     * @since 3.6
     */
    boolean isUserDefined();
}
