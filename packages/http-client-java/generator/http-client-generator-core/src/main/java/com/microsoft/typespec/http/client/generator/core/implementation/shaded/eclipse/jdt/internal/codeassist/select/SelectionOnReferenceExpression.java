/*******************************************************************************
 * Copyright (c) 2014, 2017 IBM Corporation and others.
 *
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
 */

package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.codeassist.select;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.ReferenceExpression;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.BlockScope;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.parser.Scanner;

public class SelectionOnReferenceExpression extends ReferenceExpression {

	public SelectionOnReferenceExpression(ReferenceExpression referenceExpression, Scanner scanner) {
		super(scanner);
		initialize(referenceExpression.compilationResult, referenceExpression.lhs, referenceExpression.typeArguments, referenceExpression.selector, referenceExpression.sourceEnd);
	}

	@Override
	public TypeBinding resolveType(BlockScope scope) {
		TypeBinding resolveType = super.resolveType(scope);
		if (this.expectedType != null && this.original == this)
			throw new SelectionNodeFound(this.descriptor);
		return resolveType;
	}
}