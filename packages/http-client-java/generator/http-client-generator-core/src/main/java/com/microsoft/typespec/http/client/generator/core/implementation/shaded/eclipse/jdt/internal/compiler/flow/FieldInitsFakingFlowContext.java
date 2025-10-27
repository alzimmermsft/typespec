/*******************************************************************************
 * Copyright (c) 2016 Till Brychcy and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Till Brychcy - initial API and implementation
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.flow;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.ASTNode;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.BlockScope;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

/**
 * For instances of this class,
 * {@link FlowContext#getInitsForFinalBlankInitializationCheck(com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.TypeBinding, FlowInfo)}
 * will returns a {@link FlowInfo#DEAD_END}, which for which
 * {@link FlowInfo#isDefinitelyAssigned(com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.FieldBinding)} returns true for all
 * fields.
 */

public class FieldInitsFakingFlowContext extends ExceptionHandlingFlowContext {
	public FieldInitsFakingFlowContext(
			FlowContext parent,
			ASTNode associatedNode,
			ReferenceBinding[] handledExceptions,
			FlowContext initializationParent,
			BlockScope scope,
			UnconditionalFlowInfo flowInfo) {
	super(parent, associatedNode, handledExceptions, initializationParent, scope, flowInfo);
}
}