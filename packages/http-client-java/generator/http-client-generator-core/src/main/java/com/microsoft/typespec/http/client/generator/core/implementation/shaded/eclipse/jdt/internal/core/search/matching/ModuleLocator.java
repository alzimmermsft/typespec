package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.search.matching;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.CharOperation;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.search.SearchMatch;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.ASTNode;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.ImportReference;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.ModuleDeclaration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.ModuleReference;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.Binding;

/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
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
 *
 *******************************************************************************/
public class ModuleLocator extends PatternLocator {

	private final ModulePattern pattern;
	/* package */ boolean target = false;

	public ModuleLocator(ModulePattern pattern) {
		super(pattern);
		this.pattern = pattern;
	}
	@Override
	public int match(ModuleDeclaration node, MatchingNodeSet nodeSet) {
		if (!this.pattern.findDeclarations) return IMPOSSIBLE_MATCH;
		if (!matchesName(this.pattern.name, node.moduleName)) return IMPOSSIBLE_MATCH;
		nodeSet.mustResolve = true;
		return nodeSet.addMatch(node, POSSIBLE_MATCH);
	}
	@Override
	protected int match(ModuleReference node, MatchingNodeSet nodeSet) {
		if (!this.pattern.findReferences) return IMPOSSIBLE_MATCH;
		if (!matchesName(this.pattern.name, node.moduleName)) return IMPOSSIBLE_MATCH;
		if (this.target) {
			return nodeSet.addMatch(node, ACCURATE_MATCH);
		}
		nodeSet.mustResolve = true;
		return nodeSet.addMatch(node, POSSIBLE_MATCH);
	}
	@Override
	public int match(ASTNode node, MatchingNodeSet nodeSet) {
		if (node instanceof ImportReference impt && (impt.modifiers & ClassFileConstants.AccModule) != 0) {
			if (!this.pattern.findReferences) return IMPOSSIBLE_MATCH;
			char[] moduleName = CharOperation.concatWith(impt.tokens, '.');
			if (!matchesName(this.pattern.name, moduleName)) return IMPOSSIBLE_MATCH;
			nodeSet.mustResolve = true;
			return nodeSet.addMatch(node, POSSIBLE_MATCH);
		}
		return IMPOSSIBLE_MATCH;
	}

	@Override
	protected int referenceType() {
		return IJavaElement.JAVA_MODULE;
	}
}
