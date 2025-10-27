/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IMarker;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.Util;

/**
 * This class is the entry point for source corrections.
 *
 * This class is intended to be instantiated by clients.
 *
 * @since 2.0
 * @noextend This class is not intended to be subclassed by clients.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class CorrectionEngine {
	/**
	 * This field is not intended to be used by client.
	 */
	protected ICompilationUnit compilationUnit;
	/**
	 * This field is not intended to be used by client.
	 */
	protected static final int IMPORT = 0x00000004;
	/**
	 * This field is not intended to be used by client.
	 */
	protected static final int METHOD = 0x00000008;
	/**
	 * This field is not intended to be used by client.
	 */
	protected static final int FIELD = 0x00000010;
	/**
	 * This field is not intended to be used by client.
	 */
	protected static final int LOCAL = 0x00000020;
	/**
	 * This field is not intended to be used by client.
	 */
	protected int filter;

	/**
	 * The CorrectionEngine is responsible for computing problem corrections.
	 *
     */
	public CorrectionEngine() {
		// settings ignored for now
	}

    /**
	 * Helper method for decoding problem marker attributes. Returns an array of String arguments
	 * extracted from the problem marker "arguments" attribute, or <code>null</code> if the marker
	 * "arguments" attribute is missing or ill-formed.
	 *
	 * @param problemMarker
	 * 		the problem marker to decode arguments from.
	 * @return an array of String arguments, or <code>null</code> if unable to extract arguments
	 * @since 2.1
	 */
	public static String[] getProblemArguments(IMarker problemMarker){
		String argumentsString = problemMarker.getAttribute(IJavaModelMarker.ARGUMENTS, null);
		return Util.getProblemArgumentsFromMarker(argumentsString);
	}

}
