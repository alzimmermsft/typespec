/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
 *     Mickael Istria (Red Hat Inc.) - Bug 488937
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.utils.Messages;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.util.NLS;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * This class is used to read markers from disk. Subclasses implement
 * version specific reading code.
 */
public class MarkerReader {
    protected Workspace workspace;

    public MarkerReader(Workspace workspace) {
        super();
        this.workspace = workspace;
    }

    /**
     * Returns the appropriate reader for the given version.
     */
    protected MarkerReader getReader(int formatVersion) throws IOException {
        switch (formatVersion) {
            case 1:
                return new MarkerReader_1(workspace);

            case 2:
                return new MarkerReader_2(workspace);

            case 3:
                return new MarkerReader_3(workspace);

            default:
                throw new IOException(NLS.bind(Messages.resources_format, formatVersion));
        }
    }

    public void read(DataInputStream input, boolean generateDeltas) throws IOException, CoreException {
        int formatVersion = readVersionNumber(input);
        MarkerReader reader = getReader(formatVersion);
        reader.read(input, generateDeltas);
    }

    protected static int readVersionNumber(DataInputStream input) throws IOException {
        return input.readInt();
    }
}
