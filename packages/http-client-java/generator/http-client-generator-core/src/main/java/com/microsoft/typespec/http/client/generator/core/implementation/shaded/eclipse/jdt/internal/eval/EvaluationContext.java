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

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.CharOperation;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.IBinaryType;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.util.SuffixConstants;

/**
 * @see org.eclipse.jdt.core.eval.IEvaluationContext
 */
public class EvaluationContext implements EvaluationConstants, SuffixConstants {
	/**
	 * Global counters so that several evaluation context can deploy on the same runtime.
	 */
	static int VAR_CLASS_COUNTER = 0;
	static int CODE_SNIPPET_COUNTER = 0;

	GlobalVariable[] variables;
	int variableCount;
	char[][] imports;
	char[] packageName;
	boolean varsChanged;
	VariablesInfo installedVars;
	IBinaryType codeSnippetBinary;
	String lineSeparator;

	/* do names implicitly refer to a given type */
	char[] declaringTypeName;
	int[] localVariableModifiers;
	char[][] localVariableTypeNames;
	char[][] localVariableNames;

	/* can 'this' be used in this context */
	boolean isStatic;
	boolean isConstructorCall;
/**
 * Creates a new evaluation context.
 */
public EvaluationContext() {
	this.variables = new GlobalVariable[5];
	this.variableCount = 0;
	this.imports = CharOperation.NO_CHAR_CHAR;
	this.packageName = CharOperation.NO_CHAR;
	this.varsChanged = true;
	this.isStatic = true;
	this.isConstructorCall = false;
	this.lineSeparator = com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.util.Util.LINE_SEPARATOR; // default value
}

    /**
 * Returns the imports of this evaluation context. An import is the name of a package
 * or the fully qualified name of a type as defined in the import statement of
 * a compilation unit.
 */
public char[][] getImports() {
	return this.imports;
}
/**
 * Returns the dot-separated name of the package code snippets are run into.
 * Returns an empty array for the default package. This is the default if
 * the package name has never been set.
 */
public char[] getPackageName() {
	return this.packageName;
}
/**
 * Return the binary for the root code snippet class (i.e. org.eclipse.jdt.internal.eval.target.CodeSnippet).
 */
IBinaryType getRootCodeSnippetBinary() {
	if (this.codeSnippetBinary == null) {
		this.codeSnippetBinary = new CodeSnippetSkeleton();
	}
	return this.codeSnippetBinary;
}

    /**
 * Sets the imports of this evaluation context. An import is the name of a package
 * or the fully qualified name of a type as defined in the import statement of
 * a compilation unit (see the Java Language Specifications for more details).
 */
public void setImports(char[][] imports) {
	this.imports = imports;
	this.varsChanged = true; // this may change the visibility of the variable's types
}
/**
 * Sets the line separator used by this evaluation context.
 */
public void setLineSeparator(String lineSeparator) {
	this.lineSeparator = lineSeparator;
}
/**
 * Sets the dot-separated name of the package code snippets are ran into.
 * The default package name is an empty array.
 */
public void setPackageName(char[] packageName) {
	this.packageName = packageName;
	this.varsChanged = true; // this may change the visibility of the variable's types
}
}
