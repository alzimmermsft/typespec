/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IBuffer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jface.text.BadLocationException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jface.text.Document;

/*
 * Adapts an IBuffer to IDocument
 */
public class DocumentAdapter extends Document {

	private final IBuffer buffer;

	public DocumentAdapter(IBuffer buffer) {
		super(buffer.getContents());
		this.buffer = buffer;
	}

	@Override
	public void set(String text) {
		super.set(text);
		this.buffer.setContents(text);
	}

	@Override
	public void replace(int offset, int length, String text) throws BadLocationException {
		super.replace(offset, length, text);
		this.buffer.replace(offset, length, text);
	}

}
