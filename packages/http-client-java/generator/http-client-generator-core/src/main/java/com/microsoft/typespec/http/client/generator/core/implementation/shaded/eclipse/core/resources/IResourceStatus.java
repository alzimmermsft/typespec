/*******************************************************************************
 *  Copyright (c) 2000, 2010 IBM Corporation and others.
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

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.*;

/**
 * Represents status related to resources in the Resources plug-in and
 * defines the relevant status code constants.
 * Status objects created by the Resources plug-in bear its unique id
 * (<code>ResourcesPlugin.PI_RESOURCES</code>) and one of
 * these status codes.
 *
 * @see org.eclipse.core.runtime.IStatus
 * @see ResourcesPlugin#PI_RESOURCES
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IResourceStatus extends IStatus {

    /*
     * Status code definitions
     */

    // General constants [0-98]
    // Information Only [0-32]
    // Warnings [33-65]
    /**
     * Status code constant (value 35) indicating that a given
     * nature set does not satisfy its constraints.
     * Severity: warning. Category: general.
     */
    int INVALID_NATURE_SET = 35;

    // Errors [66-98]

    /**
     * Status code constant (value 76) indicating that an operation failed.
     * Severity: error. Category: general.
     */
    int OPERATION_FAILED = 76;

    /**
     * Status code constant (value 77) indicating an invalid value.
     * Severity: error. Category: general.
     */
    int INVALID_VALUE = 77;

    // Local file system constants [200-298]
    // Information Only [200-232]

    // Warnings [233-265]

    /**
     * Status code constant (value 271) indicating an error occurred while
     * reading part of a resource from the local file system.
     * Severity: error. Category: local file system.
     */
    int FAILED_READ_LOCAL = 271;

    /**
     * Status code constant (value 273) indicating an error occurred while
     * deleting a resource from the local file system.
     * Severity: error. Category: local file system.
     */
    int FAILED_DELETE_LOCAL = 273;

    /**
     * Status code constant (value 274) indicating the workspace view of
     * the resource differs from that of the local file system. The requested
     * operation has been aborted to prevent the possible loss of data.
     * Severity: error. Category: local file system.
     */
    int OUT_OF_SYNC_LOCAL = 274;

    // Errors [366-398]

    /**
     * Status code constant (value 366) indicating a resource exists in the
     * workspace but is not of the expected type.
     * Severity: error. Category: workspace.
     */
    int RESOURCE_WRONG_TYPE = 366;

    /**
     * Status code constant (value 367) indicating a resource unexpectedly
     * exists in the workspace.
     * Severity: error. Category: workspace.
     */
    int RESOURCE_EXISTS = 367;

    /**
     * Status code constant (value 368) indicating a resource unexpectedly
     * does not exist in the workspace.
     * Severity: error. Category: workspace.
     */
    int RESOURCE_NOT_FOUND = 368;

    /**
     * Status code constant (value 369) indicating a resource unexpectedly
     * does not have content local to the workspace.
     * Severity: error. Category: workspace.
     */
    int RESOURCE_NOT_LOCAL = 369;

    /**
     * Status code constant (value 372) indicating a project is
     * unexpectedly closed.
     * Severity: error. Category: workspace.
     */
    int PROJECT_NOT_OPEN = 372;

    /**
     * Status code constant (value 375) indicating that the sync partner
     * is not registered with the workspace synchronizer.
     * Severity: error. Category: workspace.
     */
    int PARTNER_NOT_REGISTERED = 375;

    /**
     * Status code constant (value 376) indicating a marker unexpectedly
     * does not exist in the workspace tree.
     * Severity: error. Category: workspace.
     */
    int MARKER_NOT_FOUND = 376;

    /**
     * Status code constant (value 380) indicating that an attempt was made to modify
     * the workspace while it was locked. Resource changes are disallowed
     * during certain types of resource change event notification.
     * Severity: error. Category: workspace.
     *
     * @since 2.1
     */
    int WORKSPACE_LOCKED = 380;

    // Errors [566-598]

    /**
     * Status code constant (value 566) indicating an error internal to the
     * platform has occurred.
     * Severity: error. Category: internal.
     */
    int INTERNAL_ERROR = 566;

    /**
     * Status code constant (value 567) indicating the platform could not read
     * some of its metadata.
     * Severity: error. Category: internal.
     */
    int FAILED_READ_METADATA = 567;

    /**
     * Status code constant (value 568) indicating the platform could not write
     * some of its metadata.
     * Severity: error. Category: internal.
     */
    int FAILED_WRITE_METADATA = 568;

    /**
     * Status code constant (value 569) indicating the platform could not delete
     * some of its metadata.
     * Severity: error. Category: internal.
     */
    int FAILED_DELETE_METADATA = 569;

    /**
     * Returns the path of the resource associated with this status.
     *
     * @return the path of the resource related to this status
     */
    IPath getPath();
}
