/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.flow.FlowContext;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.flow.FlowInfo;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.impl.Constant;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.BlockScope;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public abstract class Literal extends Expression {

	public Literal(int s, int e) {

		this.sourceStart = s;
		this.sourceEnd = e;
	}

	@Override
	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		return flowInfo;
	}

	public abstract void computeConstant();

	public abstract TypeBinding literalType(BlockScope scope);

	@Override
	public StringBuilder printExpression(int indent, StringBuilder output){

		return output.append(source());
	 }

	@Override
	public TypeBinding resolveType(BlockScope scope) {
		// compute the real value, which must range its type's range
		this.resolvedType = literalType(scope);

		// in case of error, constant did remain null
		computeConstant();
		if (this.constant == null) {
			scope.problemReporter().constantOutOfRange(this, this.resolvedType);
			this.constant = Constant.NotAConstant;
		}
		return this.resolvedType;
	}

	public abstract char[] source();
}
