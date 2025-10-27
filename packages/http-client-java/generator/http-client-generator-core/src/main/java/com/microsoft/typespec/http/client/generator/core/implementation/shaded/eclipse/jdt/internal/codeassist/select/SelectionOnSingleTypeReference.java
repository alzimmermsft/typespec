/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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

/*
 * Selection node build by the parser in any case it was intending to
 * reduce a type reference containing the selection identifier as a single
 * name reference.
 * e.g.
 *
 *	class X extends [start]Object[end]
 *
 *	---> class X extends <SelectOnType:Object>
 */
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.Binding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.BlockScope;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.Scope;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class SelectionOnSingleTypeReference extends SingleTypeReference {
public SelectionOnSingleTypeReference(char[] source, long pos) {
	super(source, pos);
}
@Override
public void aboutToResolve(Scope scope) {
	getTypeBinding(scope.parent); // step up from the ClassScope
}
@Override
protected TypeBinding getTypeBinding(Scope scope) {
	// it can be a package, type or member type
	Binding binding = scope.getTypeOrPackage(new char[][] {this.token});
	if (!binding.isValidBinding()) {
		if(binding instanceof ProblemReferenceBinding && binding.problemId() == ProblemReasons.NotVisible) {
			ProblemReferenceBinding problemReferenceBinding = (ProblemReferenceBinding) binding;
			throw new SelectionNodeFound(problemReferenceBinding.closestMatch());
		} else if (binding instanceof TypeBinding) {
			scope.problemReporter().invalidType(this, (TypeBinding) binding);
		} else if (binding instanceof PackageBinding) {
			ProblemReferenceBinding problemBinding = new ProblemReferenceBinding(((PackageBinding)binding).compoundName, null, binding.problemId());
			scope.problemReporter().invalidType(this, problemBinding);
		}
		throw new SelectionNodeFound();
	}
	throw new SelectionNodeFound(binding);
}
@Override
public StringBuilder printExpression(int indent, StringBuilder output) {

	return output.append("<SelectOnType:").append(this.token).append('>');//$NON-NLS-1$
}
@Override
public TypeBinding resolveTypeEnclosing(BlockScope scope, ReferenceBinding enclosingType) {
	super.resolveTypeEnclosing(scope, enclosingType);

		// tolerate some error cases
		if (this.resolvedType == null ||
				!(this.resolvedType.isValidBinding() ||
					this.resolvedType.problemId() == ProblemReasons.NotVisible))
		throw new SelectionNodeFound();
	else
		throw new SelectionNodeFound(this.resolvedType);
}
}
