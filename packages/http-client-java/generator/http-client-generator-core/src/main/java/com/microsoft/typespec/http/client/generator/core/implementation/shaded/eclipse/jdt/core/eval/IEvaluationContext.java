/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.eval;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.CompletionRequestor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.ICompletionRequestor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaModelException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.WorkingCopyOwner;

/**
 * An evaluation context supports evaluating code snippets.
 * <p>
 * A code snippet is pretty much any valid piece of Java code that could be
 * pasted into the body of a method and compiled. However, there are two
 * areas where the rules are slightly more liberal.
 * </p>
 * <p>
 * First, a code snippet can return heterogeneous types. Inside the same code
 * snippet an <code>int</code> could be returned on one line, and a
 * <code>String</code> on the next, etc. For example, the following would be
 * considered a valid code snippet:
 * 
 * <pre>
 * <code>
 * char c = '3';
 * switch (c) {
 *   case '1': return 1;
 *   case '2': return '2';
 *   case '3': return "3";
 *   default: return null;
 * }
 * </code>
 * </pre>
 * 
 * <p>
 * Second, if the last statement is only an expression, the <code>return</code>
 * keyword is implied. For example, the following returns <code>false</code>:
 * 
 * <pre>
 * <code>
 * int i = 1;
 * i == 2
 * </code>
 * </pre>
 * 
 * <p>
 * Global variables are an additional feature of evaluation contexts. Within an
 * evaluation context, global variables maintain their value across evaluations.
 * These variables are particularly useful for storing the result of an
 * evaluation for use in subsequent evaluations.
 * </p>
 * <p>
 * The evaluation context remembers the name of the package in which code
 * snippets are run. The user can set this to any package, thereby gaining
 * access to types that are normally only visible within that package.
 * </p>
 * <p>
 * Finally, the evaluation context remembers a list of import declarations. The
 * user can import any packages and types so that the code snippets may refer
 * to types by their shorter simple names.
 * </p>
 * <p>
 * Example of use:
 * 
 * <pre>
 * <code>
 * IJavaProject project = getJavaProject();
 * IEvaluationContext context = project.newEvaluationContext();
 * String codeSnippet = "int i= 0; i++";
 * ICodeSnippetRequestor requestor = ...;
 * context.evaluateCodeSnippet(codeSnippet, requestor, progressMonitor);
 * </code>
 * </pre>
 * 
 * <p>
 * <code>IJavaProject.newEvaluationContext</code> can be used to obtain an
 * instance.
 * </p>
 *
 * @see IJavaProject#newEvaluationContext()
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IEvaluationContext {
}
