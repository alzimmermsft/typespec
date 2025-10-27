/*******************************************************************************
 * Copyright (c) 2012, 2015 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.filesystem.local.unix;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.filesystem.IFileInfo;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.filesystem.provider.FileInfo;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.filesystem.local.NativeHandler;

/**
 * Native handler that delegates to UnixFileNatives
 */
public class UnixFileHandler extends NativeHandler {
	@Override
	public int getSupportedAttributes() {
		return UnixFileNatives.getSupportedAttributes();
	}

	@Override
	public FileInfo fetchFileInfo(String fileName) {
		return UnixFileNatives.fetchFileInfo(fileName);
	}

	@Override
	public boolean putFileInfo(String fileName, IFileInfo info, int options) {
		return UnixFileNatives.putFileInfo(fileName, info, options);
	}
}
