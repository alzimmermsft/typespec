/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.team;

/**
 * A context that is used in conjunction with the {@link FileModificationValidator}
 * to indicate that UI-based validation is desired.
 * <p>
 * This class is not intended to be instantiated or subclassed by clients.
 *
 * @see FileModificationValidator
 * @since 3.3
 */
public class FileModificationValidationContext {

    private final Object shell;

    /**
     * Create a context with the given shell.
     *
     * @param shell the shell
     */
    FileModificationValidationContext(Object shell) {
        this.shell = shell;
    }

    /**
     * Return the
     * <code>com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.swt.widgets.Shell</code>
     * that is to be used to
     * parent any dialogs with the user, or <code>null</code> if there is no UI context
     * available (declared as an <code>Object</code> to avoid any direct references on the SWT component).
     * If there is no shell, the {@link FileModificationValidator} may still perform
     * UI-based validation if they can obtain a Shell from another source.
     * 
     * @return the
     * <code>com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.swt.widgets.Shell</code>
     * that is to be used to
     * parent any dialogs with the user, or <code>null</code>
     */
    public Object getShell() {
        return shell;
    }
}
