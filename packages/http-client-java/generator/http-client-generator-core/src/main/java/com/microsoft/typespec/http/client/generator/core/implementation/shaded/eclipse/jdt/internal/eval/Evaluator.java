/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.eval;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.IProblemFactory;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.INameEnvironment;
import java.util.Map;

/**
 * A evaluator builds a compilation unit and compiles it into class files.
 * If the compilation unit has problems, reports the problems using the
 * requestor.
 */
public abstract class Evaluator {
    EvaluationContext context;
    INameEnvironment environment;
    Map<String, String> options;
    IRequestor requestor;
    IProblemFactory problemFactory;

    /**
     * Creates a new evaluator.
     */
    Evaluator(EvaluationContext context, INameEnvironment environment, Map<String, String> options,
        IRequestor requestor, IProblemFactory problemFactory) {
        this.context = context;
        this.environment = environment;
        this.options = options;
        this.requestor = requestor;
        this.problemFactory = problemFactory;
    }

    /**
     * Returns the name of the current class. This is the simple name of the class.
     * This doesn't include the extension ".java" nor the name of the package.
     */
    protected abstract char[] getClassName();
}
