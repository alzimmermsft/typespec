/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
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
 *     Serge Beauchamp (Freescale Semiconductor) - [229633] Project Path Variable Support
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.utils.Messages;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResourceStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Status;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.util.NLS;

/**
 * This class implements the various path, URI, and name validation methods
 * in the workspace API
 */
public class LocationValidator {

    public LocationValidator() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see IWorkspace#validateName(String, int)
     */
    public IStatus validateName(String segment, int type) {
        String message;

        /* segment must not be null */
        if (segment == null) {
            message = Messages.resources_nameNull;
            return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
        }

        // cannot be an empty string
        if (segment.length() == 0) {
            message = Messages.resources_nameEmpty;
            return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
        }

        /* test invalid characters */
        char[] chars = OS.INVALID_RESOURCE_CHARACTERS;
        for (char c : chars) {
            if (segment.indexOf(c) != -1) {
                message = NLS.bind(Messages.resources_invalidCharInName, String.valueOf(c), segment);
                return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
            }
        }

        /* test invalid OS names */
        if (!OS.isNameValid(segment)) {
            message = NLS.bind(Messages.resources_invalidName, segment);
            return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
        }
        return Status.OK_STATUS;
    }

}
