/*******************************************************************************
 * Copyright (c) 2008, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Serge Beauchamp (Freescale Semiconductor) - initial API and implementation
 *     IBM Corporation - ongoing implementation
 *     James Blackburn (Broadcom Corp.) - ongoing development
 *     Sergey Prigogin (Google) - bug 424972
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.*;

/**
 * Class that instantiate IResourceFilter's that are stored in the project description.
 */
public class Filter {

    FilterDescription description;
    IProject project;

    public Filter(IProject project, FilterDescription description) {
        this.description = description;
        this.project = project;
    }


    public Object getArguments() {
        return description.getFileInfoMatcherDescription().getArguments();
    }

    public String getId() {
        return description.getFileInfoMatcherDescription().getId();
    }

    public int getType() {
        return description.getType();
    }

}
