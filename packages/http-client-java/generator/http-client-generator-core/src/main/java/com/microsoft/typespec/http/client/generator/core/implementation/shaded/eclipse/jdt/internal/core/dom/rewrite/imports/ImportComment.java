/*******************************************************************************
 * Copyright (c) 2014 Google Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Glassmyer <jogl@google.com> - import group sorting is broken - https://bugs.eclipse.org/430303
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.dom.rewrite.imports;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jface.text.IRegion;

final class ImportComment {
	/** The original location of this comment in the compilation unit. */
	final IRegion region;

	/**
	 * The number of line delimiters following this comment and preceding the next comment or the
	 * associated import declaration. Used to preserve blank lines between comments and/or import
	 * declarations. Will be 0 for a trailing comment.
	 */
	final int succeedingLineDelimiters;

	ImportComment(IRegion region, int succeedingLineDelims) {
		this.region = region;
		this.succeedingLineDelimiters = succeedingLineDelims;
	}
}