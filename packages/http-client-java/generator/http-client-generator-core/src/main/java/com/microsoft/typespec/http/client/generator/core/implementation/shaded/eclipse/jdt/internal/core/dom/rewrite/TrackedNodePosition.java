/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.dom.rewrite;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.dom.ASTNode;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jface.text.IRegion;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.text.edits.TextEdit;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.text.edits.TextEditGroup;

public class TrackedNodePosition implements ITrackedNodePosition {

	private final TextEditGroup group;
	private final ASTNode node;

	public TrackedNodePosition(TextEditGroup group, ASTNode node) {
		this.group= group;
		this.node= node;
	}

	@Override
	public int getStartPosition() {
		if (this.group.isEmpty()) {
			return this.node.getStartPosition();
		}
		IRegion coverage= TextEdit.getCoverage(this.group.getTextEdits());
		if (coverage == null) {
			return this.node.getStartPosition();
		}
		return coverage.getOffset();
	}

	@Override
	public int getLength() {
		if (this.group.isEmpty()) {
			return this.node.getLength();
		}
		IRegion coverage= TextEdit.getCoverage(this.group.getTextEdits());
		if (coverage == null) {
			return this.node.getLength();
		}
		return coverage.getLength();
	}
}
