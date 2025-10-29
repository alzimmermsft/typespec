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
 *     Terry Parker <tparker@google.com>
 *           - Contribution for https://bugs.eclipse.org/bugs/show_bug.cgi?id=372418
 *           -  Another problem with inner classes referenced from jars or class folders: "The type ... cannot be resolved"
 *     Stephan Herrmann - Contribution for
 *								Bug 392727 - Cannot compile project when a java file contains $ in its file name
 *								Bug 440477 - [null] Infrastructure for feeding external annotations into compilation
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.builder;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.CharOperation;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.IModule;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.IModuleAwareNameEnvironment;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.IModulePathEntry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.IMultiModuleEntry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.IUpdatableModule;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.IUpdatableModule.UpdateKind;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.util.SimpleSet;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.util.SuffixConstants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.util.Util;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.ModuleUpdater;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class NameEnvironment implements IModuleAwareNameEnvironment, SuffixConstants {

    boolean isIncrementalBuild;
    ClasspathMultiDirectory[] sourceLocations;
    ClasspathLocation[] binaryLocations;
    Map<String, IModulePathEntry> modulePathEntries; // is null when performing a non-modular compilation
    BuildNotifier notifier;

    SimpleSet initialTypeNames;
    // assumed that each name is of the form "a/b/ClassName", or, if a module is given: "my.mod:a/b/ClassName"
    Map<String, SourceFile> additionalUnits;
    /** Tasks resulting from add-reads or add-exports classpath attributes. */
    ModuleUpdater moduleUpdater;

    @Override
    public void cleanup() {
        this.initialTypeNames = null;
        this.additionalUnits = null;
        for (ClasspathMultiDirectory sourceLocation : this.sourceLocations)
            sourceLocation.cleanup();
        for (ClasspathLocation binaryLocation : this.binaryLocations)
            binaryLocation.cleanup();
        // assume modulePathEntries are cleaned-up via the corresponding source/binaryLocations
    }

    private NameEnvironmentAnswer findClass(String qualifiedTypeName, char[] typeName, LookupStrategy strategy,
        String moduleName) {
        if (this.notifier != null)
            this.notifier.checkCancelWithinCompiler();

        String moduleQualifiedName = moduleName != null ? moduleName + ':' + qualifiedTypeName : qualifiedTypeName;
        if (this.initialTypeNames != null && this.initialTypeNames.includes(moduleQualifiedName)) {
            if (this.isIncrementalBuild)
                // catch the case that a type inside a source file has been renamed but other class files are looking
                // for it
                throw new AbortCompilation(true, new AbortIncrementalBuildException(qualifiedTypeName));
            return null; // looking for a file which we know was provided at the beginning of the compilation
        }

        if (this.additionalUnits != null && this.sourceLocations.length > 0) {
            // if an additional source file is waiting to be compiled, answer it BUT not if this is a secondary type
            // search
            // if we answer X.java & it no longer defines Y then the binary type looking for Y will think the class path
            // is wrong
            // let the recompile loop fix up dependents when the secondary type Y has been deleted from X.java
            // Only enclosing type names are present in the additional units table, so strip off inner class
            // specifications
            // when doing the lookup (https://bugs.eclipse.org/372418).
            // Also take care of $ in the name of the class (https://bugs.eclipse.org/377401)
            // and prefer name with '$' if unit exists rather than failing to search for nested class
            // (https://bugs.eclipse.org/392727)
            SourceFile unit = this.additionalUnits.get(qualifiedTypeName); // doesn't have file extension
            if (unit != null)
                return new NameEnvironmentAnswer(unit, null /* no access restriction */);
            int index = qualifiedTypeName.indexOf('$');
            if (index > 0) {
                String enclosingTypeName = qualifiedTypeName.substring(0, index);
                unit = this.additionalUnits.get(enclosingTypeName); // doesn't have file extension
                if (unit != null)
                    return new NameEnvironmentAnswer(unit, null /* no access restriction */);
            }
        }

        String qBinaryFileName = qualifiedTypeName + SUFFIX_STRING_class;
        String qPackageName = (qualifiedTypeName.length() == typeName.length)
            ? Util.EMPTY_STRING
            : qBinaryFileName.substring(0, qBinaryFileName.length() - typeName.length - 7);
        char[] binaryFileName = CharOperation.concat(typeName, SUFFIX_class);

        ClasspathLocation[] relevantLocations;
        if (moduleName != null && this.modulePathEntries != null) {
            IModulePathEntry modulePathEntry = this.modulePathEntries.get(moduleName);
            if (modulePathEntry instanceof ModulePathEntry) {
                relevantLocations = ((ModulePathEntry) modulePathEntry).getClasspathLocations();
            } else if (modulePathEntry instanceof ClasspathLocation) {
                return ((ClasspathLocation) modulePathEntry).findClass(typeName, qPackageName, moduleName,
                    qBinaryFileName, false, null/* module already checked */);
            } else {
                return null;
            }
        } else {
            relevantLocations = this.binaryLocations;
        }
        NameEnvironmentAnswer suggestedAnswer = null;
        for (ClasspathLocation classpathLocation : relevantLocations) {
            if (!strategy.matches(classpathLocation, ClasspathLocation::hasModule)) {
                continue;
            }
            NameEnvironmentAnswer answer = classpathLocation.findClass(binaryFileName, qPackageName, moduleName,
                qBinaryFileName, false, this.modulePathEntries != null ? this.modulePathEntries::containsKey : null);
            if (answer != null) {
                char[] answerMod = answer.moduleName();
                if (answerMod != null && this.modulePathEntries != null) {
                    if (!this.modulePathEntries.containsKey(String.valueOf(answerMod)))
                        continue; // assumed to be filtered out by --limit-modules
                }
                if (!answer.ignoreIfBetter()) {
                    if (answer.isBetter(suggestedAnswer))
                        return answer;
                } else if (answer.isBetter(suggestedAnswer))
                    // remember suggestion and keep looking
                    suggestedAnswer = answer;
            }
        }
        return suggestedAnswer;
    }

    @Override
    public NameEnvironmentAnswer findType(char[][] compoundName, char[] moduleName) {
        if (compoundName != null)
            return findClass(String.valueOf(CharOperation.concatWith(compoundName, '/')),
                compoundName[compoundName.length - 1], LookupStrategy.get(moduleName),
                LookupStrategy.getStringName(moduleName));
        return null;
    }

    @Override
    public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName, char[] moduleName) {
        return findClass(String.valueOf(CharOperation.concatWith(packageName, typeName, '/')), typeName,
            LookupStrategy.get(moduleName), LookupStrategy.getStringName(moduleName));
    }

    @Override
    public char[][] getModulesDeclaringPackage(char[][] packageName, char[] moduleName) {
        String pkgName = new String(CharOperation.concatWith(packageName, '/'));
        String modName = new String(moduleName);
        LookupStrategy strategy = LookupStrategy.get(moduleName);
        switch (strategy) {
            // include unnamed (search all locations):
            case Any:
            case Unnamed:
                char[][] names = CharOperation.NO_CHAR_CHAR;
                for (ClasspathLocation location : this.binaryLocations) {
                    if (strategy.matches(location, ClasspathLocation::hasModule)) {
                        char[][] declaringModules = location.getModulesDeclaringPackage(pkgName, null);
                        if (declaringModules != null)
                            names = CharOperation.arrayConcat(names, declaringModules);
                    }
                }
                for (ClasspathLocation location : this.sourceLocations) {
                    if (strategy.matches(location, ClasspathLocation::hasModule)) {
                        char[][] declaringModules = location.getModulesDeclaringPackage(pkgName, null);
                        if (declaringModules != null)
                            names = CharOperation.arrayConcat(names, declaringModules);
                    }
                }
                return names == CharOperation.NO_CHAR_CHAR ? null : names;

            // only named (rely on modulePathEntries):
            case AnyNamed:
                modName = null;
                //$FALL-THROUGH$
            default:
                if (this.modulePathEntries != null) {
                    names = CharOperation.NO_CHAR_CHAR;
                    Collection<IModulePathEntry> entries = new HashSet<>(this.modulePathEntries.values());
                    for (IModulePathEntry modulePathEntry : entries) {
                        char[][] declaringModules = modulePathEntry.getModulesDeclaringPackage(pkgName, modName);
                        if (declaringModules != null)
                            names = CharOperation.arrayConcat(names, declaringModules);
                    }
                    return names == CharOperation.NO_CHAR_CHAR ? null : names;
                }
        }
        return null;
    }

    @Override
    public boolean hasCompilationUnit(char[][] qualifiedPackageName, char[] moduleName, boolean checkCUs) {
        String pkgName = String.valueOf(CharOperation.concatWith(qualifiedPackageName, '/'));
        LookupStrategy strategy = LookupStrategy.get(moduleName);
        String modName = LookupStrategy.getStringName(moduleName);
        switch (strategy) {
            // include unnamed (search all locations):
            case Any:
            case Unnamed:
                for (ClasspathLocation location : this.binaryLocations) {
                    if (strategy.matches(location, ClasspathLocation::hasModule))
                        if (location.hasCompilationUnit(pkgName, null))
                            return true;
                }
                for (ClasspathLocation location : this.sourceLocations) {
                    if (strategy.matches(location, ClasspathLocation::hasModule))
                        if (location.hasCompilationUnit(pkgName, null))
                            return true;
                }
                return false;

            // only named (rely on modulePathEntries):
            case Named:
                if (this.modulePathEntries != null) {
                    IModulePathEntry modulePathEntry = this.modulePathEntries.get(modName);
                    return modulePathEntry != null && modulePathEntry.hasCompilationUnit(pkgName, modName);
                }
                return false;

            case AnyNamed:
                if (this.modulePathEntries != null) {
                    for (IModulePathEntry modulePathEntry : this.modulePathEntries.values())
                        if (modulePathEntry.hasCompilationUnit(pkgName, modName))
                            return true;
                }
                return false;

            default:
                throw new IllegalArgumentException("Unexpected LookupStrategy " + strategy); //$NON-NLS-1$
        }
    }

    public boolean isPackage(String qualifiedPackageName, char[] moduleName) {
        String stringModuleName = null;

        LookupStrategy strategy = LookupStrategy.get(moduleName);
        Collection<IModulePathEntry> entries = null;
        switch (strategy) {
            case Any:
            case Unnamed:
                // NOTE: the output folders are added at the beginning of the binaryLocations
                for (ClasspathLocation binaryLocation : this.binaryLocations) {
                    if (strategy.matches(binaryLocation, ClasspathLocation::hasModule))
                        if (binaryLocation.isPackage(qualifiedPackageName, null))
                            return true;
                }
                for (ClasspathMultiDirectory sourceLocation : this.sourceLocations) {
                    if (strategy.matches(sourceLocation, ClasspathLocation::hasModule))
                        if (sourceLocation.isPackage(qualifiedPackageName, null))
                            return true;
                }
                return false;

            case AnyNamed:
                entries = this.modulePathEntries.values();
                break;

            default:
                stringModuleName = String.valueOf(moduleName);
                IModulePathEntry entry = this.modulePathEntries.get(stringModuleName);
                if (entry == null)
                    return false;
                entries = Collections.singletonList(entry);
        }
        for (IModulePathEntry modulePathEntry : entries) {
            if (modulePathEntry instanceof ModulePathEntry) {
                for (ClasspathLocation classpathLocation : ((ModulePathEntry) modulePathEntry)
                    .getClasspathLocations()) {
                    if (classpathLocation.isPackage(qualifiedPackageName, stringModuleName))
                        return true;
                }
            } else if (modulePathEntry instanceof ClasspathLocation) {
                return ((ClasspathLocation) modulePathEntry).isPackage(qualifiedPackageName, stringModuleName);
            }
        }
        return false;
    }

    @Override
    public char[][] listPackages(char[] moduleName) {
        LookupStrategy strategy = LookupStrategy.get(moduleName);
        switch (strategy) {
            case Named:
                IModulePathEntry entry = this.modulePathEntries.get(String.valueOf(moduleName));
                if (entry == null)
                    return CharOperation.NO_CHAR_CHAR;
                return entry.listPackages();

            default:
                throw new UnsupportedOperationException("can list packages only of a named module"); //$NON-NLS-1$
        }
    }

    @Override
    public IModule getModule(char[] name) {
        if (this.modulePathEntries != null) {
            IModulePathEntry modulePathEntry = this.modulePathEntries.get(String.valueOf(name));
            if (modulePathEntry instanceof IMultiModuleEntry)
                return modulePathEntry.getModule(name);
            else if (modulePathEntry != null)
                return modulePathEntry.getModule();
        }
        return null;
    }

    @Override
    public char[][] getAllAutomaticModules() {
        if (this.modulePathEntries == null)
            return CharOperation.NO_CHAR_CHAR;
        Set<char[]> set = this.modulePathEntries.values()
            .stream()
            .filter(IModulePathEntry::isAutomaticModule)
            .map(e -> e.getModule().name())
            .collect(Collectors.toSet());
        return set.toArray(new char[set.size()][]);
    }

    @Override
    public void applyModuleUpdates(IUpdatableModule compilerModule, UpdateKind kind) {
        if (this.moduleUpdater != null)
            this.moduleUpdater.applyModuleUpdates(compilerModule, kind);
    }
}
