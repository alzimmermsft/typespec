/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
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

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.ICompilationUnit;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IField;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IMember;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IMethod;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IModularClassFile;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IModuleDescription;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IPackageFragment;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IType;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.ITypeParameter;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaModelException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.Signature;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.CharOperation;

public abstract class NamedMember extends Member {

	/*
	 * This element's name, or an empty <code>String</code> if this
	 * element does not have a name.
	 */
	final protected String name;

	public NamedMember(JavaElement parent, String name) {
		super(parent);
		this.name = name;
	}
	public NamedMember(JavaElement parent, String name, int occurrenceCount) {
		super(parent, occurrenceCount);
		this.name = name;
	}

	private void appendTypeParameters(StringBuilder buffer) throws JavaModelException {
		ITypeParameter[] typeParameters = getTypeParameters();
		int length = typeParameters.length;
		if (length == 0) return;
		buffer.append('<');
		for (int i = 0; i < length; i++) {
			ITypeParameter typeParameter = typeParameters[i];
			buffer.append(typeParameter.getElementName());
			String[] bounds = typeParameter.getBounds();
			int boundsLength = bounds.length;
			if (boundsLength > 0) {
				buffer.append(" extends "); //$NON-NLS-1$
				for (int j = 0; j < boundsLength; j++) {
					buffer.append(bounds[j]);
					if (j < boundsLength-1)
						buffer.append(" & "); //$NON-NLS-1$
				}
			}
			if (i < length-1)
				buffer.append(", "); //$NON-NLS-1$
		}
		buffer.append('>');
	}

	@Override
	public String getElementName() {
		return this.name;
	}

	protected String getKey(IField field) throws JavaModelException {
		StringBuilder key = new StringBuilder();

		// declaring class
		String declaringKey = getKey((IType) field.getParent());
		key.append(declaringKey);

		// field name
		key.append('.');
		key.append(field.getElementName());

		return key.toString();
	}

	protected String getKey(IMethod method, boolean forceOpen) throws JavaModelException {
		StringBuilder key = new StringBuilder();

		// declaring class
		String declaringKey = getKey((IType) method.getParent());
		key.append(declaringKey);

		// selector
		key.append('.');
		if (!method.isConstructor()) { // empty selector for ctors, cf. BindingKeyResolver.consumeMethod()
			String selector = method.getElementName();
			key.append(selector);
		}

		// type parameters
		if (forceOpen) {
			ITypeParameter[] typeParameters = method.getTypeParameters();
			int length = typeParameters.length;
			if (length > 0) {
				key.append('<');
				for (int i = 0; i < length; i++) {
					ITypeParameter typeParameter = typeParameters[i];
					String[] bounds = typeParameter.getBounds();
					int boundsLength = bounds.length;
					char[][] boundSignatures = new char[boundsLength][];
					for (int j = 0; j < boundsLength; j++) {
						boundSignatures[j] = Signature.createCharArrayTypeSignature(bounds[j].toCharArray(), method.isBinary());
						CharOperation.replace(boundSignatures[j], '.', '/');
					}
					char[] sig = Signature.createTypeParameterSignature(typeParameter.getElementName().toCharArray(), boundSignatures);
					key.append(sig);
				}
				key.append('>');
			}
		}

		// parameters
		key.append('(');
		String[] parameters = method.getParameterTypes();
		for (String parameter : parameters)
			key.append(parameter.replace('.', '/'));
		key.append(')');

		// return type
		if (forceOpen)
			key.append(method.getReturnType().replace('.', '/'));
		else
			key.append('V');

		return key.toString();
	}

	protected String getKey(IType type) throws JavaModelException {
		StringBuilder key = new StringBuilder();
		key.append('L');
		String packageName = type.getPackageFragment().getElementName();
		key.append(packageName.replace('.', '/'));
		if (packageName.length() > 0)
			key.append('/');
		String typeQualifiedName = type.getTypeQualifiedName('$');
		ICompilationUnit cu = (ICompilationUnit) type.getAncestor(IJavaElement.COMPILATION_UNIT);
		if (cu != null) {
			String cuName = cu.getElementName();
			String mainTypeName = cuName.substring(0, cuName.lastIndexOf('.'));
			int end = typeQualifiedName.indexOf('$');
			if (end == -1)
				end = typeQualifiedName.length();
			String topLevelTypeName = typeQualifiedName.substring(0, end);
			if (!mainTypeName.equals(topLevelTypeName)) {
				key.append(mainTypeName);
				key.append('~');
			}
		}
		key.append(typeQualifiedName);
		key.append(';');
		return key.toString();
	}
	protected String getKey(IModuleDescription module, boolean forceOpen) throws JavaModelException {
		StringBuilder key = new StringBuilder();
		key.append('"');
		String modName = module.getElementName();
		key.append(modName);
		return key.toString();
	}

    protected IPackageFragment getPackageFragment() {
		return null;
	}

	public String getFullyQualifiedName(char enclosingTypeSeparator, boolean showParameters) throws JavaModelException {
		String packageName = getPackageFragment().getElementName();
		if (packageName.equals(IPackageFragment.DEFAULT_PACKAGE_NAME)) {
			return getTypeQualifiedName(enclosingTypeSeparator, showParameters);
		}
		return packageName + '.' + getTypeQualifiedName(enclosingTypeSeparator, showParameters);
	}

	public String getTypeQualifiedName(char enclosingTypeSeparator, boolean showParameters) throws JavaModelException {
		NamedMember declaringType;
		switch (this.getParent().getElementType()) {
			case IJavaElement.COMPILATION_UNIT:
				if (showParameters) {
					StringBuilder buffer = new StringBuilder(this.name);
					appendTypeParameters(buffer);
					return buffer.toString();
				}
				return this.name;
			case IJavaElement.CLASS_FILE:
				if (this.getParent() instanceof IModularClassFile)
					return null;
				String classFileName = this.getParent().getElementName();
				String typeName;
				if (classFileName.indexOf('$') == -1) {
					// top level class file: name of type is same as name of class file
					typeName = this.name;
				} else {
					// anonymous or local class file
					typeName = classFileName.substring(0, classFileName.lastIndexOf('.'))/*remove .class*/.replace('$', enclosingTypeSeparator);
				}
				if (showParameters) {
					StringBuilder buffer = new StringBuilder(typeName);
					appendTypeParameters(buffer);
					return buffer.toString();
				}
				return typeName;
			case IJavaElement.TYPE:
				declaringType = (NamedMember) this.getParent();
				break;
			case IJavaElement.FIELD:
			case IJavaElement.INITIALIZER:
			case IJavaElement.METHOD:
				declaringType = (NamedMember) ((IMember) this.getParent()).getDeclaringType();
				break;
			default:
				return null;
		}
		StringBuilder buffer = new StringBuilder(declaringType.getTypeQualifiedName(enclosingTypeSeparator, showParameters));
		buffer.append(enclosingTypeSeparator);
		String simpleName = this.name.length() == 0 ? getOccurrenceCountSignature() : this.name;
		buffer.append(simpleName);
		if (showParameters) {
			appendTypeParameters(buffer);
		}
		return buffer.toString();
	}
	/*
	 * Returns the String representation of the occurrence count for this element.
	 * The occurrence count is a unique number representation to identify the particular element.
	 *
	 * @return the occurrence count for this element in the form of String
	 */
	protected String getOccurrenceCountSignature() {
		return Integer.toString(this.getOccurrenceCount());
	}
	protected ITypeParameter[] getTypeParameters() throws JavaModelException {
		return null;
	}
}
