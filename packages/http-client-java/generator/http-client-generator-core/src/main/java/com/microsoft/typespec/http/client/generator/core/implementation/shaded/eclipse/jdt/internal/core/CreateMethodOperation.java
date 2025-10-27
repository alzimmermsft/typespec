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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core;

import java.util.Iterator;
import java.util.List;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.ICompilationUnit;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaModelStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaModelStatusConstants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IType;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaModelException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.Signature;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.dom.ASTNode;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.dom.MethodDeclaration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.dom.SimpleName;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.dom.SingleVariableDeclaration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.Messages;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.Util;

/**
 * <p>This operation creates an instance method.
 *
 * <p>Required Attributes:<ul>
 *  <li>Containing type
 *  <li>The source code for the method. No verification of the source is
 *      performed.
 * </ul>
 */
@SuppressWarnings("rawtypes")
public class CreateMethodOperation extends CreateTypeMemberOperation {

	protected String[] parameterTypes;

/**
 * When executed, this operation will create a method
 * in the given type with the specified source.
 */
public CreateMethodOperation(IType parentElement, String source, boolean force) {
	super(parentElement, source, force);
}
/**
 * Returns the type signatures of the parameter types of the
 * current <code>MethodDeclaration</code>
 */
protected String[] convertASTMethodTypesToSignatures() {
	if (this.parameterTypes == null) {
		if (this.createdNode != null) {
			MethodDeclaration methodDeclaration = (MethodDeclaration) this.createdNode;
			List parameters = methodDeclaration.parameters();
			int size = parameters.size();
			this.parameterTypes = new String[size];
			Iterator iterator = parameters.iterator();
			// convert the AST types to signatures
			for (int i = 0; i < size; i++) {
				SingleVariableDeclaration parameter = (SingleVariableDeclaration) iterator.next();
				String typeSig = Util.getSignature(parameter.getType());
				int extraDimensions = parameter.getExtraDimensions();
				if (methodDeclaration.isVarargs() && i == size-1)
					extraDimensions++;
				this.parameterTypes[i] = Signature.createArraySignature(typeSig, extraDimensions);
			}
		}
	}
	return this.parameterTypes;
}
@Override
protected ASTNode generateElementAST(ASTRewrite rewriter, ICompilationUnit cu) throws JavaModelException {
	ASTNode node = super.generateElementAST(rewriter, cu);
	if (node.getNodeType() != ASTNode.METHOD_DECLARATION)
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.INVALID_CONTENTS));
	return node;
}
/**
 * @see CreateElementInCUOperation#generateResultHandle
 */
@Override
protected IJavaElement generateResultHandle() {
	String[] types = convertASTMethodTypesToSignatures();
	String name = getASTNodeName();
	return getType().getMethod(name, types);
}
private String getASTNodeName() {
	return ((MethodDeclaration) this.createdNode).getName().getIdentifier();
}
/**
 * @see CreateElementInCUOperation#getMainTaskName()
 */
@Override
public String getMainTaskName(){
	return Messages.operation_createMethodProgress;
}
@Override
protected SimpleName rename(ASTNode node, SimpleName newName) {
	MethodDeclaration method = (MethodDeclaration) node;
	SimpleName oldName = method.getName();
	method.setName(newName);
	return oldName;
}
/**
 * @see CreateTypeMemberOperation#verifyNameCollision
 */
@Override
protected IJavaModelStatus verifyNameCollision() {
	if (this.createdNode != null) {
		IType type = getType();
		String name;
		if (((MethodDeclaration) this.createdNode).isConstructor())
			name = type.getElementName();
		else
			name = getASTNodeName();
		String[] types = convertASTMethodTypesToSignatures();
		if (type.getMethod(name, types).exists()) {
			return new JavaModelStatus(
				IJavaModelStatusConstants.NAME_COLLISION,
				Messages.bind(Messages.status_nameCollision, name));
		}
	}
	return JavaModelStatus.VERIFIED_OK;
}
}
