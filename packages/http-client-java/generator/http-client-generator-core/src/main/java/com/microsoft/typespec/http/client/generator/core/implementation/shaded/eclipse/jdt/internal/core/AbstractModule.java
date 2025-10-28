/*******************************************************************************
 * Copyright (c) 2017, 2019 IBM Corporation and others.
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

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IModuleDescription;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.ITypeRoot;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaModelException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.IModule;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.IModule.IModuleReference;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.IModule.IPackageExport;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.IModule.IService;

public interface AbstractModule extends IModuleDescription {

	/**
	 * Handle for an automatic module.
	 *
	 * <p>Note, that by definition this is mostly a fake, only {@link #getElementName()} provides a useful value.</p>
	 */
	static class AutoModule extends NamedMember implements AbstractModule {

		private final boolean nameFromManifest;

		public AutoModule(JavaElement parent, String name, boolean nameFromManifest) {
			super(parent, name);
			this.nameFromManifest = nameFromManifest;
		}
		@Override
		public IJavaElement[] getChildren() throws JavaModelException {
			return JavaElement.NO_ELEMENTS; // may later answer computed details
		}
		@Override
		public int getFlags() throws JavaModelException {
			return 0;
		}

        public boolean isAutoNameFromManifest() {
			return this.nameFromManifest;
		}
		@Override
		public char getHandleMementoDelimiter() {
			return JavaElement.JEM_MODULE;
		}
		@Override
		public ITypeRoot getTypeRoot() {
			return null; // has no real CompilationUnit nor ClassFile
		}
		@Override
		public IModuleReference[] getRequiredModules() throws JavaModelException {
			return ModuleDescriptionInfo.NO_REQUIRES;
		}
		@Override
		public void toStringContent(StringBuilder buffer, String lineDelimiter) throws JavaModelException {
			buffer.append("automatic module "); //$NON-NLS-1$
			buffer.append(this.name);
		}
	}

	// "forward declaration" for a method from JavaElement:
	abstract Object getElementInfo() throws JavaModelException;

	default IModule getModuleInfo() throws JavaModelException {
		return (IModule) getElementInfo();
	}

    default IModuleReference[] getRequiredModules() throws JavaModelException {
		return getModuleInfo().requires();
	}
	default IPackageExport[] getExportedPackages() throws JavaModelException {
		return getModuleInfo().exports();
	}
	default IService[] getProvidedServices() throws JavaModelException {
		return getModuleInfo().provides();
	}

    default char[][] getUsedServices() throws JavaModelException {
		return getModuleInfo().uses();
	}

    default IPackageExport[] getOpenedPackages() throws JavaModelException {
		return getModuleInfo().opens();
	}

    default String toString(String lineDelimiter) {
		StringBuilder buffer = new StringBuilder();
		try {
			toStringContent(buffer, lineDelimiter);
		} catch (JavaModelException e) {
			if (JavaModelManager.VERBOSE) {
				JavaModelManager.trace("", e); //$NON-NLS-1$
			}
		}
		return buffer.toString();
	}
	default void toStringContent(StringBuilder buffer, String lineDelimiter) throws JavaModelException {
		IPackageExport[] exports = getExportedPackages();
		IModuleReference[] requires = getRequiredModules();
		buffer.append("module "); //$NON-NLS-1$
		buffer.append(getElementName()).append(' ');
		buffer.append('{').append(lineDelimiter);
		if (exports != null) {
			for (IPackageExport export : exports) {
				buffer.append("\texports "); //$NON-NLS-1$
				buffer.append(export.toString());
				buffer.append(lineDelimiter);
			}
		}
		buffer.append(lineDelimiter);
		if (requires != null) {
			for (IModuleReference require : requires) {
				buffer.append("\trequires "); //$NON-NLS-1$
				if (require.isTransitive()) {
					buffer.append(" public "); //$NON-NLS-1$
				}
				buffer.append(require.name());
				buffer.append(';').append(lineDelimiter);
			}
		}
		buffer.append(lineDelimiter).append('}').toString();
	}

	@Override
	default int getElementType() {
		return JAVA_MODULE;
	}
}
