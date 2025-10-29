/*******************************************************************************
 *  Copyright (c) 2000, 2009 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources;

/**
 * A project nature descriptor contains information about a project nature
 * obtained from the plug-in manifest (<code>plugin.xml</code>) file.
 * <p>
 * Nature descriptors are platform-defined objects that exist
 * independent of whether that nature's plug-in has been started.
 * In contrast, a project nature's runtime object (<code>IProjectNature</code>)
 * generally runs plug-in-defined code.
 * </p>
 *
 * @since 2.0
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IProjectNatureDescriptor {
    /**
     * Returns the unique identifier of this nature.
     * <p>
     * The nature identifier is composed of the nature's plug-in id and the simple
     * id of the nature extension. For example, if plug-in <code>"com.xyz"</code>
     * defines a nature extension with id <code>"myNature"</code>, the unique
     * nature identifier will be <code>"com.xyz.myNature"</code>.
     * </p>
     * 
     * @return the unique nature identifier
     */
    String getNatureId();

    /**
     * Returns the unique identifiers of the natures required by this nature.
     * Nature requirements are specified by the <code>"requires-nature"</code>
     * element on a nature extension.
     * Returns an empty array if no natures are required by this nature.
     *
     * @return an array of nature ids that this nature requires,
     * possibly an empty array.
     */
    String[] getRequiredNatureIds();

}
