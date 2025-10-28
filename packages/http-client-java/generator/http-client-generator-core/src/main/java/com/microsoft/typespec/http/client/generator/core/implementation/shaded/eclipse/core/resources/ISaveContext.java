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

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;

/**
 * A context for workspace <code>save</code> operations.
 * <p>
 * Note that <code>IWorkspace.save</code> uses a
 * different save context for each registered participant,
 * allowing each to declare whether they have actively
 * participated and decide whether to receive a resource
 * delta on reactivation.
 * </p>
 *
 * @see IWorkspace#save(boolean,
 * com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor)
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISaveContext {

    /*
     * ====================================================================
     * Constants related to save kind
     * ====================================================================
     */

    /**
     * Type constant which identifies a full save.
     *
     * @see ISaveContext#getKind()
     */
    int FULL_SAVE = 1;

    /**
     * Type constant which identifies a snapshot.
     *
     * @see ISaveContext#getKind()
     */
    int SNAPSHOT = 2;

    /**
     * Type constant which identifies a project save.
     *
     * @see ISaveContext#getKind()
     */
    int PROJECT_SAVE = 3;

    /**
     * Returns current files mapped with the <code>ISaveContext.map</code>
     * facility or an empty array if there are no mapped files.
     *
     * @return the files currently mapped by the participant
     *
     * @see #map(IPath, IPath)
     */
    IPath[] getFiles();

    /**
     * Returns the type of this save. The types can be:
     * <ul>
     * <li> <code>ISaveContext.FULL_SAVE</code></li>
     * <li> <code>ISaveContext.SNAPSHOT</code></li>
     * <li> <code>ISaveContext.PROJECT_SAVE</code></li>
     * </ul>
     *
     * @return the type of the current save
     */
    int getKind();

    /**
     * Returns the number for the previous save in
     * which the plug-in actively participated, or <code>0</code>
     * if the plug-in has never actively participated in a save before.
     * <p>
     * In the event of an unsuccessful save, this is the value to
     * <code>rollback</code> to.
     * </p>
     *
     * @return the previous save number if positive, or <code>0</code>
     * if never saved before
     */
    int getPreviousSaveNumber();

    /**
     * If the current save is a project save, this method returns the project
     * being saved.
     *
     * @return the project being saved or <code>null</code> if this is not
     * project save
     *
     * @see #getKind()
     */
    IProject getProject();

    /**
     * Returns the number for this save. This number is
     * guaranteed to be <code>1</code> more than the
     * previous save number.
     * <p>
     * This is the value to use when, for example, creating files
     * in which a participant will save its data.
     * </p>
     *
     * @return the save number
     */
    int getSaveNumber();

    /**
     * Indicates that the saved workspace tree should be remembered so that a delta
     * will be available in a subsequent session when the plug-in re-registers
     * to participate in saves. If this method is not called, no resource delta will
     * be made available. This facility is not available for marker deltas.
     * Plug-ins must assume that all markers may have changed when they are activated.
     * <p>
     * Note that this is orthogonal to <code>needSaveNumber</code>. That is,
     * one can ask for a delta regardless of whether or not one is an active participant.
     * </p>
     * <p>
     * Note that deltas are not guaranteed to be saved even if saving is requested.
     * Deltas cannot be supplied where the previous state is too old or has become invalid.
     * </p>
     * <p>
     * This method is only valid for full saves. It is ignored during snapshots
     * or project saves.
     * </p>
     *
     */
    void needDelta();

}
