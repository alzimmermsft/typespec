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
 *     Stephan Herrmann - contribution for bug 337868 - [compiler][model] incomplete support for package-info.java when using SearchableEnvironment
 *     Microsoft Corporation - contribution for bug 575562 - improve completion search performance
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IClasspathEntry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IModuleDescription;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IPackageFragment;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IPackageFragmentRoot;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IType;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaCore;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaModelException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.WorkingCopyOwner;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.CharOperation;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.search.IJavaSearchConstants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.codeassist.ISearchRequestor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.IBinaryType;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.IModule;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.IModuleAwareNameEnvironment;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.ISourceType;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.IUpdatableModule;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.IUpdatableModule.UpdateKind;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.ModuleBinding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.NameLookup.Answer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.DeduplicationUtil;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 *	This class provides a <code>SearchableBuilderEnvironment</code> for code assist which
 *	uses the Java model as a search tool.
 */
public class SearchableEnvironment
	implements IModuleAwareNameEnvironment, IJavaSearchConstants {

	public NameLookup nameLookup;
    protected com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.ICompilationUnit[] workingCopies;
	protected WorkingCopyOwner owner;

	protected JavaProject project;

    protected boolean checkAccessRestrictions;
	// moduleName -> IPackageFragmentRoot[](lazily populated)
	private Map<String,IPackageFragmentRoot[]> knownModuleLocations; // null indicates: not using JPMS
	private final boolean excludeTestCode;

	private ModuleUpdater moduleUpdater;
	private Map<IPackageFragmentRoot,IModuleDescription> rootToModule;

	private long timeSpentInGetModulesDeclaringPackage;
	private long timeSpentInFindTypes;

	private List<IPackageFragmentRoot> unnamedModulePackageFragmentRoots;

	private final int release;

	@Deprecated
	public SearchableEnvironment(JavaProject project, com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.ICompilationUnit[] workingCopies) throws JavaModelException {
		this(project, workingCopies, false, JavaProject.NO_RELEASE);
	}
	/**
	 * Creates a SearchableEnvironment on the given project
	 * <p>
	 * When {@code release} is not {@link JavaProject#NO_RELEASE} then this SearchableEnvironment
	 * will define a view that prefers to search in locations that best match the given release.
	 * </p>
	 */
	public SearchableEnvironment(JavaProject project, com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.ICompilationUnit[] workingCopies, boolean excludeTestCode, int release) throws JavaModelException {
		this.project = project;
		this.excludeTestCode = excludeTestCode;
		this.release = release;
        this.checkAccessRestrictions = true;
		this.workingCopies = workingCopies;
		this.nameLookup = project.newNameLookup(workingCopies, excludeTestCode);
    }

	/**
	 * Note: this is required for (abandoned) Scala-IDE
	 */
	@Deprecated
	public SearchableEnvironment(JavaProject project, WorkingCopyOwner owner) throws JavaModelException {
		this(project, owner, false, JavaProject.NO_RELEASE);
	}

	/**
	 * Creates a SearchableEnvironment on the given project
	 */
	public SearchableEnvironment(JavaProject project, WorkingCopyOwner owner, boolean excludeTestCode, int release) throws JavaModelException {
		this(project, owner == null ? null : JavaModelManager.getJavaModelManager().getWorkingCopies(owner, true/*add primary WCs*/), excludeTestCode, release);
		this.owner = owner;
	}

    /**
	 * Returns the given type in the the given package if it exists,
	 * otherwise <code>null</code>.
	 */
	protected NameEnvironmentAnswer find(String typeName, String packageName, IPackageFragmentRoot[] moduleContext) {
		if (packageName == null)
			packageName = IPackageFragment.DEFAULT_PACKAGE_NAME;
		if (this.owner != null) {
			String source = this.owner.findSource(typeName, packageName);
			if (source != null) {
				IJavaElement moduleElement = (moduleContext != null && moduleContext.length > 0) ? moduleContext[0] : null;
				ICompilationUnit cu = new BasicCompilationUnit(
						source.toCharArray(),
						CharOperation.splitOn('.', packageName.toCharArray()),
						typeName + Util.defaultJavaExtension(),
						moduleElement);
				return new NameEnvironmentAnswer(cu, null);
			}
		}
		Answer answer =
			this.nameLookup.findType(
				typeName,
				packageName,
				false/*exact match*/,
				NameLookup.ACCEPT_ALL,
				this.checkAccessRestrictions,
				moduleContext,
				this.release);
		if (answer != null) {
			// construct name env answer
			if (answer.type instanceof BinaryType) { // BinaryType
				return createAnswer(answer, (BinaryType) answer.type);
			} else { //SourceType
				try {
					// retrieve the requested type
					SourceTypeElementInfo sourceType = (SourceTypeElementInfo)((SourceType) answer.type).getElementInfo();
					ISourceType topLevelType = sourceType;
					while (topLevelType.getEnclosingType() != null) {
						topLevelType = topLevelType.getEnclosingType();
					}
					// find all siblings (other types declared in same unit, since may be used for name resolution)
					IType[] types = sourceType.getHandle().getCompilationUnit().getTypes();
					ISourceType[] sourceTypes = new ISourceType[types.length];

					// in the resulting collection, ensure the requested type is the first one
					sourceTypes[0] = sourceType;
					int length = types.length;
					for (int i = 0, index = 1; i < length; i++) {
						ISourceType otherType =
							(ISourceType) ((JavaElement) types[i]).getElementInfo();
						if (!otherType.equals(topLevelType) && index < length) // check that the index is in bounds (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=62861)
							sourceTypes[index++] = otherType;
					}
					char[] moduleName = answer.module != null ? answer.module.getElementName().toCharArray() : null;
					return new NameEnvironmentAnswer(sourceTypes, answer.restriction, getExternalAnnotationPath(answer.entry), moduleName);
				} catch (JavaModelException jme) {
					if (jme.isDoesNotExist() && String.valueOf(TypeConstants.PACKAGE_INFO_NAME).equals(typeName)) {
						// in case of package-info.java the type doesn't exist in the model,
						// but the CU may still help in order to fetch package level annotations.
						return new NameEnvironmentAnswer((ICompilationUnit)answer.type.getParent(), answer.restriction);
					}
					// no usable answer
				}
			}
		}
		return null;
	}

	private String getExternalAnnotationPath(IClasspathEntry entry) {
		if (entry == null)
			return null;
		IPath path = entry.getExternalAnnotationPath(this.project.getProject(), true);
		if (path == null)
			return null;
		return path.toOSString();
	}

	private NameEnvironmentAnswer createAnswer(Answer lookupAnswer, BinaryType binaryType) {
		char[] moduleName = lookupAnswer.module != null ? lookupAnswer.module.getElementName().toCharArray() : null;
		try {
			IBinaryType iBinaryType = binaryType.getElementInfo();
            iBinaryType.getExternalAnnotationStatus();
            return new NameEnvironmentAnswer(iBinaryType, lookupAnswer.restriction, moduleName);
		} catch (JavaModelException e) {
			// fallback to null
		}
		return null;
	}

    @Override
	public boolean isOnModulePath(ICompilationUnit unit) {
		if (unit instanceof CompilationUnit cUnit) {
			IPackageFragmentRoot root = cUnit.originalFromClone().getPackageFragmentRoot();
			if (Objects.equals(root.getJavaProject(), this.project))
				return true; // current project: modular if it contains module-info :)
			IClasspathEntry entry = this.nameLookup.rootToResolvedEntries.get(root);
			if (entry instanceof ClasspathEntry cpEntry)
				return cpEntry.isModular();
			return true; // out-of-band resolution / transitive dependency?
		} else if (unit instanceof BasicCompilationUnit bUnit) {
			return bUnit.moduleName != null;
		}
		return false;
	}

	/**
	 * Find the packages that start with the given prefix.
	 * A valid prefix is a qualified name separated by periods
	 * (ex. java.util).
	 * The packages found are passed to:
	 *    ISearchRequestor.acceptPackage(char[][] packageName)
	 */
	public void findPackages(char[] prefix, ISearchRequestor requestor) {
		this.nameLookup.seekPackageFragments(
			new String(prefix),
			true,
			new SearchableEnvironmentRequestor(requestor));
	}

    /**
	 * @see org.eclipse.jdt.internal.compiler.env.IModuleAwareNameEnvironment#findType(char[][],char[])
	 */
	@Override
	public NameEnvironmentAnswer findType(char[][] compoundTypeName, char[] moduleName) {
		if (compoundTypeName == null) return null;

		boolean isNamedStrategy = LookupStrategy.get(moduleName) == LookupStrategy.Named;
		IPackageFragmentRoot[] moduleLocations = isNamedStrategy ? findModuleContext(moduleName) : null;

		int length = compoundTypeName.length;
		if (length <= 1) {
			if (length == 0) return null;
			return find(new String(compoundTypeName[0]), null, moduleLocations);
		}

		int lengthM1 = length - 1;
		char[][] packageName = new char[lengthM1][];
		System.arraycopy(compoundTypeName, 0, packageName, 0, lengthM1);

		return find(
			DeduplicationUtil.toString(compoundTypeName[lengthM1]),
			CharOperation.toString(packageName),
			moduleLocations);
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.env.IModuleAwareNameEnvironment#findType(char[],char[][],char[])
	 */
	@Override
	public NameEnvironmentAnswer findType(char[] name, char[][] packageName, char[] moduleName) {
		if (name == null) return null;

		boolean isNamedStrategy = LookupStrategy.get(moduleName) == LookupStrategy.Named;
		IPackageFragmentRoot[] moduleLocations = isNamedStrategy ? findModuleContext(moduleName) : null;
		return find(
				DeduplicationUtil.toString(name),
				packageName == null || packageName.length == 0 ? null : CharOperation.toString(packageName),
			moduleLocations);
	}

    /**
	 * @see org.eclipse.jdt.internal.compiler.env.IModuleAwareNameEnvironment#getModulesDeclaringPackage(char[][], char[])
	 */
	@Override
	public char[][] getModulesDeclaringPackage(char[][] packageName, char[] moduleName) {
		long start = -1;
		if (NameLookup.VERBOSE)
			start = System.currentTimeMillis();
		try {
			String[] pkgName = Arrays.stream(packageName).map(String::new).toArray(String[]::new);
			LookupStrategy strategy = LookupStrategy.get(moduleName);
			switch (strategy) {
				case Named:
					if (this.knownModuleLocations != null) {
						IPackageFragmentRoot[] moduleContext = findModuleContext(moduleName);
						if (moduleContext != null) {
							// (this.owner != null && this.owner.isPackage(pkgName)) // TODO(SHMOD) see old isPackage
							if (this.nameLookup.isPackage(pkgName, moduleContext)) {
								return new char[][] { moduleName };
							}
						}
					}
					return null;
				case Unnamed:
				case Any:
					// if in pre-9 mode we may still search the unnamed module
					if (this.knownModuleLocations == null) {
						if ((this.owner != null && this.owner.isPackage(pkgName))
								|| this.nameLookup.isPackage(pkgName))
							return new char[][] { ModuleBinding.UNNAMED };
						return null;
					}
					//$FALL-THROUGH$
				case AnyNamed:
					char[][] names = CharOperation.NO_CHAR_CHAR;
					// narrow down candidates of roots (https://bugs.eclipse.org/566498)
					IPackageFragmentRoot[] matchingRoots = this.nameLookup.findPackageFragementRoots(pkgName);
					if(matchingRoots != null) {
						boolean containsUnnamed = false;
						for (IPackageFragmentRoot packageRoot : matchingRoots) {
							IPackageFragmentRoot[] singleton = { packageRoot };
							if (strategy.matches(singleton, locs -> locs[0] instanceof JrtPackageFragmentRoot || getModuleDescription(locs) != null)) {
								if (this.nameLookup.isPackage(pkgName, singleton)) {
									IModuleDescription moduleDescription = getModuleDescription(singleton);
									char[] aName;
									if (moduleDescription != null) {
										aName = moduleDescription.getElementName().toCharArray();
									} else {
										if (containsUnnamed)
											continue;
										containsUnnamed = true;
										aName = ModuleBinding.UNNAMED;
									}
									names = CharOperation.arrayConcat(names, aName);
								}
							}
						}
						/*
						 * Check if we have a sub-package in the unnamed module,
						 * since classpath filters can result in top level packages
						 * not listed by nameLookup.findPackageFragementRoots().
						 * See: https://github.com/eclipse-jdt/eclipse.jdt.core/issues/485
						 * and https://github.com/eclipse-jdt/eclipse.jdt.core/issues/646
						 */
						if (!containsUnnamed && hasSubPackageInUnnamedModule(pkgName)) {
							names = CharOperation.arrayConcat(names, ModuleBinding.UNNAMED);
						}
					}
					return names == CharOperation.NO_CHAR_CHAR ? null : names;
				default:
					throw new IllegalArgumentException("Unexpected LookupStrategy "+strategy); //$NON-NLS-1$
			}
		} finally {
			if (NameLookup.VERBOSE)
				this.timeSpentInGetModulesDeclaringPackage += System.currentTimeMillis()-start;
		}
	}
	@Override
	public boolean hasCompilationUnit(char[][] pkgName, char[] moduleName, boolean checkCUs) {
		LookupStrategy strategy = LookupStrategy.get(moduleName);
		switch (strategy) {
			case Named:
				if (this.knownModuleLocations != null) {
					IPackageFragmentRoot[] moduleContext = findModuleContext(moduleName);
					if (moduleContext != null) {
						// (this.owner != null && this.owner.isPackage(pkgName)) // TODO(SHMOD) see old isPackage
						if (this.nameLookup.hasCompilationUnit(pkgName, moduleContext))
							return true;
					}
				}
				return false;
			case Unnamed:
			case Any:
				// if in pre-9 mode we may still search the unnamed module
				if (this.knownModuleLocations == null) {
					if (this.nameLookup.hasCompilationUnit(pkgName, null))
						return true;
				}
				//$FALL-THROUGH$
			case AnyNamed:
				// narrow down candidates of roots (https://bugs.eclipse.org/566498)
				String[] splittedName = Util.toStrings(pkgName);
				IPackageFragmentRoot[] packageRoots = this.nameLookup.findPackageFragementRoots(splittedName);
				if(packageRoots != null) {
					for (IPackageFragmentRoot packageRoot : packageRoots) {
						IPackageFragmentRoot[] singleton = { packageRoot };
						if (strategy.matches(singleton, locs -> locs[0] instanceof JrtPackageFragmentRoot || getModuleDescription(locs) != null)) {
							if (this.nameLookup.hasCompilationUnit(pkgName, singleton))
								return true;
						}
					}
				}
				return false;
			default:
				throw new IllegalArgumentException("Unexpected LookupStrategy "+strategy); //$NON-NLS-1$
		}
	}

	private IModuleDescription getModuleDescription(IPackageFragmentRoot[] roots) {
		if (this.rootToModule == null) {
			this.rootToModule = new HashMap<>();
		}
		for (IPackageFragmentRoot root : roots) {
			IModuleDescription moduleDescription = NameLookup.getModuleDescription(this.project, root, this.rootToModule, this.nameLookup.rootToResolvedEntries::get);
			if (moduleDescription != null)
				return moduleDescription;
		}
		return null;
	}

	private IPackageFragmentRoot[] findModuleContext(char[] moduleName) {
		IPackageFragmentRoot[] moduleContext = null;
		if (this.knownModuleLocations != null && moduleName != null && moduleName.length > 0) {
			moduleContext = this.knownModuleLocations.get(String.valueOf(moduleName));
			if (moduleContext == null) {
				Answer moduleAnswer = this.nameLookup.findModule(moduleName);
				if (moduleAnswer != null) {
					IProject currentProject = moduleAnswer.module.getJavaProject().getProject();
					IJavaElement current = moduleAnswer.module.getParent();
					while (moduleContext == null && current != null) {
						switch (current.getElementType()) {
							case IJavaElement.PACKAGE_FRAGMENT_ROOT:
								if (!((IPackageFragmentRoot) current).isExternal() && !(current instanceof JarPackageFragmentRoot)) {
									current = current.getJavaProject();
								} else {
									moduleContext = new IPackageFragmentRoot[] { (IPackageFragmentRoot) current }; // TODO: validate
									break;
								}
								//$FALL-THROUGH$
							case IJavaElement.JAVA_PROJECT:
								try {
									moduleContext = getOwnedPackageFragmentRoots((IJavaProject) current);
								} catch (JavaModelException e) {
									// silent?
								}
								break;
							default:
								current = current.getParent();
								if (current != null) {
									try {
										// detect when an element refers to a resource owned by another project:
										IResource resource = current.getUnderlyingResource();
										if (resource != null) {
											IProject otherProject = resource.getProject();
											if (otherProject != null && !otherProject.equals(currentProject)) {
												IJavaProject otherJavaProject = JavaCore.create(otherProject);
												if (otherJavaProject.exists())
													moduleContext = getRootsForOutputLocation(otherJavaProject, resource);
											}
										}
									} catch (JavaModelException e) {
										Util.log(e, "Failed to find package fragment root for " + current); //$NON-NLS-1$
									}
								}
						}
					}
					this.knownModuleLocations.put(String.valueOf(moduleName), moduleContext);
				}
			}
		}
		return moduleContext;
	}

    @Override
	public void cleanup() {
		// nothing to do
	}

	@Override
	public IModule getModule(char[] name) {
		Answer answer = this.nameLookup.findModule(name);
		IModule module = null;
		if (answer != null) {
			module = NameLookup.getModuleDescriptionInfo(answer.module);
		}
		return module;
	}

	@Override
	public char[][] getAllAutomaticModules() {
		return CharOperation.NO_CHAR_CHAR;
	}

	@Override
	public void applyModuleUpdates(IUpdatableModule module, UpdateKind kind) {
		if (this.moduleUpdater != null)
			this.moduleUpdater.applyModuleUpdates(module, kind);
	}

	private IPackageFragmentRoot[] getRootsForOutputLocation(IJavaProject otherJavaProject, IResource outputLocation) throws JavaModelException {
		IPath outputPath = outputLocation.getFullPath();
		List<IPackageFragmentRoot> result = new ArrayList<>();
		if (outputPath.equals(otherJavaProject.getOutputLocation())) {
			// collect roots reporting to the default output location:
			for (IClasspathEntry classpathEntry : otherJavaProject.getRawClasspath()) {
				if (classpathEntry.getOutputLocation() == null) {
					for (IPackageFragmentRoot root : otherJavaProject.findPackageFragmentRoots(classpathEntry)) {
						IResource rootResource = root.getResource();
						if (rootResource == null || !rootResource.getProject().equals(otherJavaProject.getProject()))
							continue; // outside this project
						result.add(root);
					}
				}
			}
		}
		if (!result.isEmpty())
			return result.toArray(new IPackageFragmentRoot[result.size()]);
		// search an entry that specifically (and exclusively) reports to the output location:
		for (IClasspathEntry classpathEntry : otherJavaProject.getRawClasspath()) {
			if (outputPath.equals(classpathEntry.getOutputLocation()))
				return otherJavaProject.findPackageFragmentRoots(classpathEntry);
		}
		return null;
	}

	public static IPackageFragmentRoot[] getOwnedPackageFragmentRoots(IJavaProject javaProject) throws JavaModelException {
		IPackageFragmentRoot[] allRoots = javaProject.getPackageFragmentRoots();
		IPackageFragmentRoot[] sourceRoots = Arrays.copyOf(allRoots, allRoots.length);
		int count = 0;
		for (IPackageFragmentRoot root : allRoots) {
			if (root.getKind() == IPackageFragmentRoot.K_BINARY) {
				if(root instanceof JarPackageFragmentRoot) {
					// don't treat jars in a project as part of the project's module
					continue;
				}
				IResource resource = root.getResource();
				if (resource == null || !resource.getProject().equals(javaProject.getProject()))
					continue; // outside this project
			}
			sourceRoots[count++] = root;
		}
		if (count < allRoots.length)
			return Arrays.copyOf(sourceRoots, count);
		return sourceRoots;
	}

	@Override
	public char[][] listPackages(char[] moduleName) {
		switch (LookupStrategy.get(moduleName)) {
			case Named:
				IPackageFragmentRoot[] packageRoots = findModuleContext(moduleName);
				Set<String> packages = new HashSet<>();
				if (packageRoots != null) {
					for (IPackageFragmentRoot packageRoot : packageRoots) {
						try {
							for (IJavaElement javaElement : packageRoot.getChildren()) {
								if (javaElement instanceof IPackageFragment && !((IPackageFragment) javaElement).isDefaultPackage())
									packages.add(javaElement.getElementName());
							}
						} catch (JavaModelException e) {
							Util.log(e, "Failed to retrieve packages from " + packageRoot); //$NON-NLS-1$
						}
					}
				}
				return packages.stream().map(String::toCharArray).toArray(char[][]::new);
			default:
				throw new UnsupportedOperationException("can list packages only of a named module"); //$NON-NLS-1$
		}
	}

	public void printTimeSpent() {
		if(!NameLookup.VERBOSE)
			return;

		JavaModelManager.trace(" TIME SPENT SearchableEnvironment");  //$NON-NLS-1$
		JavaModelManager.trace(" -> getModulesDeclaringPackage..." +  this.timeSpentInGetModulesDeclaringPackage + "ms");  //$NON-NLS-1$ //$NON-NLS-2$
		JavaModelManager.trace(" -> findTypes...................." +  this.timeSpentInFindTypes + "ms");  //$NON-NLS-1$ //$NON-NLS-2$

		this.nameLookup.printTimeSpent();
	}

	private boolean hasSubPackageInUnnamedModule(String[] pkgName) {
		List<IPackageFragmentRoot> packageFragmentRoots = this.unnamedModulePackageFragmentRoots;
		if (packageFragmentRoots != null) {
			String name = String.join(".", pkgName); //$NON-NLS-1$
			for (IPackageFragmentRoot packageFragmentRoot : packageFragmentRoots) {
				try {
					IJavaElement[] children = packageFragmentRoot.getChildren();
					for (IJavaElement child : children) {
						String childName = child.getElementName();
						if (childName.startsWith(name)) {
							return true;
						}
					}
				} catch (JavaModelException e) {
					Util.log(e, "Failed to retrieve children for " + packageFragmentRoot); //$NON-NLS-1$
				}
			}
		}
		return false;
	}
}
