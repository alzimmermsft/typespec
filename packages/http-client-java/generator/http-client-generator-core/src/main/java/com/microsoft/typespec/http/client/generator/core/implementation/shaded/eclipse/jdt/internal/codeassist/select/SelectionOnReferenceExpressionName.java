/*******************************************************************************
 * Copyright (c) 2013, 2017 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.codeassist.select;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.CharOperation;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.ReferenceExpression;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.BlockScope;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.PolyTypeBinding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.parser.Scanner;

public class SelectionOnReferenceExpressionName extends ReferenceExpression {

	public SelectionOnReferenceExpressionName(Scanner scanner) {
		super(scanner);
	}

	@Override
	public StringBuilder printExpression(int indent, StringBuilder output) {
		output.append("<SelectionOnReferenceExpressionName:"); //$NON-NLS-1$
		super.printExpression(indent, output);
		return output.append('>');
	}

	// See SelectionScanner#scanIdentifierOrKeyword
	@Override
	public boolean isConstructorReference() {
		return CharOperation.equals(this.selector, "new".toCharArray()); //$NON-NLS-1$
	}

	// See SelectionScanner#scanIdentifierOrKeyword
	@Override
	public boolean isMethodReference() {
		return !CharOperation.equals(this.selector, "new".toCharArray()); //$NON-NLS-1$
	}

	@Override
	public TypeBinding resolveType(BlockScope scope) {
		TypeBinding type = super.resolveType(scope);
		if (type == null || type instanceof ProblemReferenceBinding || type instanceof PolyTypeBinding)
			return type;
		MethodBinding method = getMethodBinding();
		if (method != null && method.isValidBinding() && !method.isSynthetic())
			throw new SelectionNodeFound(this.actualMethodBinding);
		throw new SelectionNodeFound();
	}
}
