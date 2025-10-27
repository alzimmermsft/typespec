/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
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
 *     Microsoft Corporation - adapt to the new index match API
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.search.matching;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.OperationCanceledException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IMember;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IMethod;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IModuleDescription;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IPackageFragmentRoot;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IType;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.ITypeParameter;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.Signature;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.search.IJavaSearchScope;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.search.SearchParticipant;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.util.SuffixConstants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.index.Index;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.search.IndexQueryRequestor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.search.JavaSearchScope;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.Util;

/**
 * Pattern to search type parameters.
 *
 * @since 3.1
 */
public class TypeParameterPattern extends JavaSearchPattern {

	protected boolean findDeclarations;
	protected boolean findReferences;
	protected char[] name;
	protected ITypeParameter typeParameter;
	protected char[] declaringMemberName;
	protected char[] methodDeclaringClassName;
	protected char[][] methodArgumentTypes;

	public TypeParameterPattern(boolean findDeclarations, boolean findReferences, ITypeParameter typeParameter, int matchRule) {
		super(TYPE_PARAM_PATTERN, matchRule);

		this.findDeclarations = findDeclarations; // set to find declarations & all occurences
		this.findReferences = findReferences; // set to find references & all occurences
		this.typeParameter = typeParameter;
		this.name = typeParameter.getElementName().toCharArray(); // store type parameter name
		IMember member = typeParameter.getDeclaringMember();
		this.declaringMemberName = member.getElementName().toCharArray(); // store type parameter declaring member name

		// For method type parameter, store also declaring class name and parameters type names
		if (member instanceof IMethod) {
			IMethod method = (IMethod) member;
			this.methodDeclaringClassName = method.getParent().getElementName().toCharArray();
			String[] parameters = method.getParameterTypes();
			int length = parameters.length;
			this.methodArgumentTypes = new char[length][];
			for (int i=0; i<length; i++) {
				this.methodArgumentTypes[i] = Signature.toCharArray(parameters[i].toCharArray());
			}
		}
	}

	/*
	 * Same than LocalVariablePattern.
	 */
	@Override
	public void findIndexMatches(Index index, IndexQueryRequestor requestor, SearchParticipant participant, IJavaSearchScope scope, IProgressMonitor progressMonitor) {
	    IPackageFragmentRoot root = (IPackageFragmentRoot) this.typeParameter.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
		String documentPath;
		String relativePath;
	    if (root.isArchive()) {
 	    	IType type = (IType) this.typeParameter.getAncestor(IJavaElement.TYPE);
    	    relativePath = (type.getFullyQualifiedName('$')).replace('.', '/') + SuffixConstants.SUFFIX_STRING_class;
    	    IModuleDescription md = root.getModuleDescription();
            if(md != null) {
            	String module = md.getElementName();
            	documentPath = root.getPath() + IJavaSearchScope.JAR_FILE_ENTRY_SEPARATOR
            			+ module + IJavaSearchScope.JAR_FILE_ENTRY_SEPARATOR + relativePath;
            } else {
            	documentPath = root.getPath() + IJavaSearchScope.JAR_FILE_ENTRY_SEPARATOR + relativePath;
            }
	    } else {
			IPath path = this.typeParameter.getPath();
	        documentPath = path.toString();
			relativePath = Util.relativePath(path, 1/*remove project segment*/);
	    }

		if (scope instanceof JavaSearchScope) {
			JavaSearchScope javaSearchScope = (JavaSearchScope) scope;
			// Get document path access restriction from java search scope
			// Note that requestor has to verify if needed whether the document violates the access restriction or not
			AccessRuleSet access = javaSearchScope.getAccessRuleSet(relativePath, index.containerPath);
			if (access != JavaSearchScope.NOT_ENCLOSED) { // scope encloses the path
				if (!requestor.acceptIndexMatch(documentPath, this, participant, access))
					throw new OperationCanceledException();
			}
		} else if (scope.encloses(documentPath)) {
			if (!requestor.acceptIndexMatch(documentPath, this, participant, null))
				throw new OperationCanceledException();
		}
	}

	@Override
	public void findIndexMatches(Index index, IndexQueryRequestor requestor, SearchParticipant participant, IJavaSearchScope scope, boolean resolveDocumentName, IProgressMonitor progressMonitor) {
		findIndexMatches(index, requestor, participant, scope, progressMonitor);
	}

	@Override
	protected StringBuilder print(StringBuilder output) {
		if (this.findDeclarations) {
			output.append(this.findReferences
				? "TypeParamCombinedPattern: " //$NON-NLS-1$
				: "TypeParamDeclarationPattern: "); //$NON-NLS-1$
		} else {
			output.append("TypeParamReferencePattern: "); //$NON-NLS-1$
		}
		output.append(this.typeParameter.toString());
		return super.print(output);
	}

}
