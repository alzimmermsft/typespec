/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
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

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.OperationCanceledException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.CategorizedProblem;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;

public class CancelableProblemFactory extends DefaultProblemFactory {
    public IProgressMonitor monitor;

    public CancelableProblemFactory(IProgressMonitor monitor) {
        super();
        this.monitor = monitor;
    }

    @Override
    public CategorizedProblem createProblem(char[] originatingFileName, int problemId, String[] problemArguments,
        String[] messageArguments, int severity, int startPosition, int endPosition, int lineNumber, int columnNumber) {
        if (this.monitor != null && this.monitor.isCanceled())
            throw new AbortCompilation(true/* silent */, new OperationCanceledException());
        return super.createProblem(originatingFileName, problemId, problemArguments, messageArguments, severity,
            startPosition, endPosition, lineNumber, columnNumber);
    }

    @Override
    public CategorizedProblem createProblem(char[] originatingFileName, int problemId, String[] problemArguments,
        int elaborationId, String[] messageArguments, int severity, int startPosition, int endPosition, int lineNumber,
        int columnNumber) {
        if (this.monitor != null && this.monitor.isCanceled())
            throw new AbortCompilation(true/* silent */, new OperationCanceledException());
        return super.createProblem(originatingFileName, problemId, problemArguments, elaborationId, messageArguments,
            severity, startPosition, endPosition, lineNumber, columnNumber);
    }
}
