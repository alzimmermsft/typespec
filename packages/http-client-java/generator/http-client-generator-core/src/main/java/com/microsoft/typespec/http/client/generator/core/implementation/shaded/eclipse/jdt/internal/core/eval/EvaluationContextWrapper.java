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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.eval;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.*;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.eval.IEvaluationContext;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.JavaProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.eval.EvaluationContext;

/**
 * A wrapper around the infrastructure evaluation context.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class EvaluationContextWrapper implements IEvaluationContext {
	protected EvaluationContext context;
	protected JavaProject project;
/**
 * Creates a new wrapper around the given infrastructure evaluation context
 * and project.
 */
public EvaluationContextWrapper(EvaluationContext context, JavaProject project) {
	this.context = context;
	this.project = project;
}
}
