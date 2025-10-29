/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *     James Blackburn (Broadcom Corp.) - ongoing development
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 473427
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources;

public class ProjectInfo extends ResourceInfo {

    /** The description of this object */
    protected ProjectDescription description;


    /**
     * Default constructor (for easier debugging)
     */
    public ProjectInfo() {
        super();
    }

    /**
     * Returns the description associated with this info. The return value may be null.
     */
    public ProjectDescription getDescription() {
        return description;
    }

}
