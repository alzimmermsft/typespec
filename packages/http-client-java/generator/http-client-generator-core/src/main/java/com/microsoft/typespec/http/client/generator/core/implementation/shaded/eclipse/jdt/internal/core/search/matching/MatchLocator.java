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
 *     Stephan Herrmann - Contribution for
 *								Bug 377883 - NPE on open Call Hierarchy
 *     Microsoft Corporation - Contribution for bug 575562 - improve completion search performance
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.search.matching;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IMethod;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IOrdinaryClassFile;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IPackageFragmentRoot;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IType;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.CharOperation;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.search.IJavaSearchScope;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.search.MethodReferenceMatch;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.search.ReferenceMatch;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.search.SearchMatch;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.search.SearchParticipant;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.search.SearchPattern;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.search.SearchRequestor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.search.TypeReferenceMatch;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.CompilationResult;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.AccessRestriction;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.IBinaryType;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.INameEnvironment;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.ISourceType;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.impl.ITypeRequestor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.Binding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.ClassScope;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.ProblemMethodBinding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.parser.Parser;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.parser.SourceTypeConverter;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.problem.AbortCompilationUnit;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.BinaryMember;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.BinaryMethod;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.BinaryType;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.JavaElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.JavaModelManager;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.PackageFragment;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.SourceMethod;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.SourceTypeElementInfo;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.index.Index;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.search.BasicSearchEngine;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.search.IndexQueryRequestor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.search.indexing.QualifierQuery;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.ASTNodeFinder;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

import static com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.JavaModelManager.trace;

public class MatchLocator implements ITypeRequestor {

    public static final int MAX_AT_ONCE;

    static {
        long maxMemory = Runtime.getRuntime().maxMemory();
        int ratio = (int) Math.round(((double) maxMemory) / (64 * 0x100000));
        switch (ratio) {
            case 0:
            case 1:
                MAX_AT_ONCE = 100;
                break;
            case 2:
                MAX_AT_ONCE = 200;
                break;
            case 3:
                MAX_AT_ONCE = 300;
                break;
            default:
                MAX_AT_ONCE = 400;
                break;
        }
    }

    // permanent state
    public SearchPattern pattern;
    public PatternLocator patternLocator;
    public int matchContainer;
    public SearchRequestor requestor;
    public IJavaSearchScope scope;
    public IProgressMonitor progressMonitor;

    public com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.ICompilationUnit[]
        workingCopies;

    // the following is valid for the current project
    public MatchLocatorParser parser;
    private Parser basicParser;
    public INameEnvironment nameEnvironment;
    public LookupEnvironment lookupEnvironment;

    public CompilerOptions options;

    /*
     * Time spent in the IJavaSearchResultCollector
     */
    public long resultCollectorTime = 0;

    // Binding resolution and cache
    CompilationUnitScope unitScope;
    Map<JavaSearchPattern, Binding> bindingsByPattern;
    Map<String, Binding> bindingsByName;

    private TypeBinding unitScopeTypeBinding = null; // cached

    public static void setFocus(SearchPattern pattern, IJavaElement focus) {
        pattern.focus = focus;
    }

    /**
     * Sets the qualifier queries into pattern.
     *
     * @see QualifierQuery#encodeQuery(QualifierQuery.QueryCategory[], char[], char[])
     */
    public static void setIndexQualifierQuery(SearchPattern pattern, char[] queries) {
        pattern.indexQualifierQuery = queries;
    }

    public static IBinaryType classFileReader(IType type) {
        IOrdinaryClassFile classFile = type.getClassFile();
        JavaModelManager manager = JavaModelManager.getJavaModelManager();
        if (classFile.isOpen())
            return (IBinaryType) manager.getInfo(type);

        PackageFragment pkg = (PackageFragment) type.getPackageFragment();
        IPackageFragmentRoot root = (IPackageFragmentRoot) pkg.getParent();
        try {
            if (!root.isArchive())
                return Util.newClassFileReader(((JavaElement) type).resource());

            String rootPath = root.getPath().toOSString();
            if (com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.util.Util.isJrt(
                rootPath)) {
                String classFileName = classFile.getElementName();
                String path = Util.concatWith(pkg.names, classFileName, '/');
                return ClassFileReader.readFromJrt(new File(rootPath), null, path);
            } else {
                ZipFile zipFile = null;
                try {
                    IPath zipPath = root.getPath();
                    if (JavaModelManager.ZIP_ACCESS_VERBOSE) {
                        trace("(" + Thread.currentThread() + ") [MatchLocator.classFileReader()] Creating ZipFile on "
                            + zipPath); //$NON-NLS-1$	//$NON-NLS-2$
                    }
                    zipFile = manager.getZipFile(zipPath);
                    String classFileName = classFile.getElementName();
                    String path = Util.concatWith(pkg.names, classFileName, '/');
                    return ClassFileReader.read(zipFile, path);
                } finally {
                    manager.closeZipFile(zipFile);
                }
            }
        } catch (ClassFormatException | CoreException | IOException e) {
            // invalid class file: return null
        }
        return null;
    }

    /**
     * Query a given index for matching entries. Assumes the sender has opened the index and will close when finished.
     */
    public static void findIndexMatches(SearchPattern pattern, Index index, IndexQueryRequestor requestor,
        SearchParticipant participant, IJavaSearchScope scope, IProgressMonitor monitor) throws IOException {
        pattern.findIndexMatches(index, requestor, participant, scope, monitor);
    }

    /**
     * Query a given index for matching entries. Assumes the sender has opened the index and will close when finished.
     */
    public static void findIndexMatches(SearchPattern pattern, Index index, IndexQueryRequestor requestor,
        SearchParticipant participant, IJavaSearchScope scope, boolean resolveDocumentName, IProgressMonitor monitor)
        throws IOException {
        pattern.findIndexMatches(index, requestor, participant, scope, resolveDocumentName, monitor);
    }

    public static IJavaElement getProjectOrJar(IJavaElement element) {
        while (!(element instanceof IJavaProject) && !(element instanceof JarPackageFragmentRoot)) {
            element = element.getParent();
        }
        return element;
    }

    public static IJavaElement projectOrJarFocus(SearchPattern pattern) {
        return pattern == null || pattern.focus == null ? null : getProjectOrJar(pattern.focus);
    }

    /**
     * Add an additional binary type
     */
    @Override
    public void accept(IBinaryType binaryType, PackageBinding packageBinding, AccessRestriction accessRestriction) {
        this.lookupEnvironment.createBinaryTypeFrom(binaryType, packageBinding, accessRestriction);
    }

    /**
     * Add an additional compilation unit into the loop
     * ->  build compilation unit declarations, their bindings and record their results.
     */
    @Override
    public void accept(ICompilationUnit sourceUnit, AccessRestriction accessRestriction) {
        // Switch the current policy and compilation result for this unit to the requested one.
        CompilationResult unitResult = new CompilationResult(sourceUnit, 1, 1, this.options.maxProblemsPerUnit);
        try {
            CompilationUnitDeclaration parsedUnit = basicParser().dietParse(sourceUnit, unitResult);
            this.lookupEnvironment.buildTypeBindings(parsedUnit, accessRestriction);
            this.lookupEnvironment.completeTypeBindings(parsedUnit, true);
        } catch (AbortCompilationUnit e) {
            // at this point, currentCompilationUnitResult may not be sourceUnit, but some other
            // one requested further along to resolve sourceUnit.
            if (unitResult.compilationUnit == sourceUnit) { // only report once
                //requestor.acceptResult(unitResult.tagAsAccepted());
            } else {
                throw e; // want to abort enclosing request to compile
            }
        }
        // Display unit error in debug mode
        if (BasicSearchEngine.VERBOSE) {
            if (unitResult.problemCount > 0) {
                trace(unitResult.toString());
            }
        }
    }

    /**
     * Add additional source types
     */
    @Override
    public void accept(ISourceType[] sourceTypes, PackageBinding packageBinding, AccessRestriction accessRestriction) {
        // case of SearchableEnvironment of an IJavaProject is used
        ISourceType sourceType = sourceTypes[0];
        while (sourceType.getEnclosingType() != null)
            sourceType = sourceType.getEnclosingType();
        if (sourceType instanceof SourceTypeElementInfo elementInfo) {
            // get source
            IType type = elementInfo.getHandle();
            ICompilationUnit sourceUnit = (ICompilationUnit) type.getCompilationUnit();
            accept(sourceUnit, accessRestriction);
        } else {
            CompilationResult result = new CompilationResult(sourceType.getFileName(), 1, 1, 0);
            CompilationUnitDeclaration unit = SourceTypeConverter.buildCompilationUnit(sourceTypes,
                SourceTypeConverter.FIELD_AND_METHOD // need field and methods
                    | SourceTypeConverter.MEMBER_TYPE, // need member types
                // no need for field initialization
                this.lookupEnvironment.problemReporter, result);
            if (unit != null) {
                this.lookupEnvironment.buildTypeBindings(unit, accessRestriction);
                this.lookupEnvironment.completeTypeBindings(unit, true);
            }
        }
    }

    protected Parser basicParser() {
        if (this.basicParser == null) {
            ProblemReporter problemReporter = new ProblemReporter(DefaultErrorHandlingPolicies.proceedWithAllProblems(),
                this.options, new DefaultProblemFactory());
            this.basicParser = new Parser(problemReporter, false);
            this.basicParser.reportOnlyOneSyntaxError = true;
        }
        return this.basicParser;
    }

    private boolean filterEnum(SearchMatch match) {

        // filter org.apache.commons.lang.enum package for projects above 1.5
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=317264
        IJavaElement element = (IJavaElement) match.getElement();
        if (element == null)
            return false;

        PackageFragment pkg = (PackageFragment) element.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
        if (pkg != null) {
            // enum was found in org.apache.commons.lang.enum at index 5
            //$NON-NLS-1$
            return pkg.names.length == 5 && pkg.names[4].equals("enum");
        }
        return false;
    }

    protected void getMethodBodies(CompilationUnitDeclaration unit, MatchingNodeSet nodeSet) {
        if (unit.ignoreMethodBodies) {
            unit.ignoreFurtherInvestigation = true;
            return; // if initial diet parse did not work, no need to dig into method bodies.
        }

        // save existing values to restore them at the end of the parsing process
        // see bug 47079 for more details
        int[] oldLineEnds = this.parser.scanner.lineEnds;
        int oldLinePtr = this.parser.scanner.linePtr;

        try {
            CompilationResult compilationResult = unit.compilationResult;
            this.parser.scanner.setSource(compilationResult);

            if (this.parser.javadocParser.checkDocComment) {
                char[] contents = compilationResult.compilationUnit.getContents();
                this.parser.javadocParser.scanner.setSource(contents);
            }
            this.parser.nodeSet = nodeSet;
            this.parser.parseBodies(unit);
        } finally {
            this.parser.nodeSet = null;
            // this is done to prevent any side effects on the compilation unit result
            // line separator positions array.
            this.parser.scanner.lineEnds = oldLineEnds;
            this.parser.scanner.linePtr = oldLinePtr;
        }
    }

    protected TypeBinding getType(char[] typeKey, char[] typeName) {
        if (this.unitScope == null || typeName == null || typeName.length == 0)
            return null;
        // Try to get binding from cache
        Binding binding = this.bindingsByName.get(new String(typeKey));
        if (binding != null) {
            if (binding instanceof TypeBinding && binding.isValidBinding())
                return (TypeBinding) binding;
            return null;
        }
        // Get binding from unit scope
        char[][] compoundName = CharOperation.splitOn('.', typeName);
        TypeBinding typeBinding = this.unitScope.getType(compoundName, compoundName.length);
        this.unitScopeTypeBinding = typeBinding; //cache.
        if (typeBinding == null || !typeBinding.isValidBinding()) {
            typeBinding = this.lookupEnvironment.getType(compoundName, this.unitScope.module());
        }
        this.bindingsByName.put(new String(typeKey), typeBinding);
        return typeBinding != null && typeBinding.isValidBinding() ? typeBinding : null;
    }

    public MethodBinding getMethodBinding(MethodPattern methodPattern) {
        this.unitScopeTypeBinding = null;
        MethodBinding methodBinding = getMethodBinding0(methodPattern);
        if (methodBinding != null)
            return methodBinding; // known to be valid.
        // special handling for methods of anonymous/local types. Since these cannot be looked up in the environment the usual way ...
        if (methodPattern.focus instanceof SourceMethod) {
            MethodBinding binding = getClosestMatchMethodBinding(methodPattern);
            if (binding != null) {
                return binding;
            }
            char[] typeName = PatternLocator.qualifiedPattern(methodPattern.declaringSimpleName,
                methodPattern.declaringQualification);
            if (typeName != null) {
                IType type = methodPattern.declaringType;
                IType enclosingType = type.getDeclaringType();
                while (enclosingType != null) {
                    type = enclosingType;
                    enclosingType = type.getDeclaringType();
                }
                typeName = type.getFullyQualifiedName().toCharArray();
                TypeBinding declaringTypeBinding = getType(typeName, typeName);
                if (declaringTypeBinding instanceof SourceTypeBinding sourceTypeBinding) {
                    ClassScope skope = sourceTypeBinding.scope;
                    if (skope != null) {
                        CompilationUnitDeclaration unit = skope.referenceCompilationUnit();
                        if (unit != null) {
                            AbstractMethodDeclaration amd = new ASTNodeFinder(unit).findMethod(
                                (IMethod) methodPattern.focus);
                            if (amd != null && amd.binding != null && amd.binding.isValidBinding()) {
                                this.bindingsByPattern.put(methodPattern, amd.binding);
                                return amd.binding;
                            }
                        }
                    }
                }
            }
        } else if (methodPattern.focus instanceof BinaryMethod && methodPattern.declaringType instanceof BinaryType
            && this.unitScopeTypeBinding instanceof ProblemReferenceBinding) {//Get binding from unit scope for non-visible member of binary type
            return getClosestMatchMethodBinding(methodPattern);
        }
        return null;
    }

    private MethodBinding getClosestMatchMethodBinding(MethodPattern methodPattern) {
        TypeBinding typeBinding = this.unitScopeTypeBinding;
        if (typeBinding instanceof ProblemReferenceBinding) {
            ProblemReferenceBinding problemReferenceBinding = (ProblemReferenceBinding) this.unitScopeTypeBinding;
            if (problemReferenceBinding.problemId() == ProblemReasons.NotVisible) {
                ReferenceBinding closestMatch = problemReferenceBinding.closestReferenceMatch();
                if (closestMatch != null) {
                    return getMethodBinding(methodPattern, closestMatch);
                }
            }
        }
        return null;
    }

    private List<String> getInverseFullName(char[] qualifier, char[] simpleName) {
        List<String> result = new ArrayList<>();
        if (qualifier != null && qualifier.length > 0) {
            result.addAll(Arrays.asList(new String(qualifier).split("\\.")));//$NON-NLS-1$
            Collections.reverse(result);
        }
        if (simpleName != null)
            result.add(0, new String(simpleName));
        return result;
    }

    /**
     * returns the row index which has the highest column entry.
     * TODO: rewrite this code with list when (if) we move to 1.8 [with FP constructs].
     */
    private int getMaxResult(int[][] resultsMap) {
        int rows = resultsMap.length;
        int cols = resultsMap[0].length;
        List<Integer> candidates = new ArrayList<>();
        candidates.add(0); //default row

        for (int j = 0; j < cols; ++j) {
            int current = resultsMap[0][j];
            for (int i = 1; i < rows; ++i) {
                int tmp = resultsMap[i][j];
                if (tmp < current)
                    continue;
                if (tmp > current) {
                    current = tmp;
                    candidates.clear();
                }
                candidates.add(i);// there is atleast one element always.
            }
            if (candidates.size() <= 1)
                break; // found
        }
        return candidates.get(0);
    }

    /**
     * apply the function to map the parameter full name to an index
     */
    private int mapParameter(List<String> patternParameterFullName, List<String> methodParameterFullName) {
        int patternLen = patternParameterFullName.size();
        int methodLen = methodParameterFullName.size();
        int size = Math.min(patternLen, methodLen);
        int result = -1;
        for (int i = 0; i < size; i++) {
            if (!patternParameterFullName.get(i).equals(methodParameterFullName.get(i)))
                break;
            ++result;
        }
        return patternLen == methodLen && result + 1 == patternLen ? Integer.MAX_VALUE : result;
    }

    /**
     * returns an array of integers whose elements are matching indices.
     * As a special case, full match would have max value as the index.
     */
    private int[] getResultMap(Map<Integer, List<String>> patternMap, Map<Integer, List<String>> methodMap) {
        int paramLength = methodMap.size();
        int[] result = new int[paramLength];
        for (int p = 0; p < paramLength; p++) {
            result[p] = mapParameter(patternMap.get(p), methodMap.get(p));
        }
        return result;
    }

    private Map<Integer, List<String>> getSplitNames(char[][] qualifiedNames, char[][] simpleNames) {
        int paramLength = simpleNames.length;
        Map<Integer, List<String>> result = new HashMap<>();
        for (int p = 0; p < paramLength; p++)
            result.put(p, getInverseFullName(qualifiedNames[p], simpleNames[p]));
        return result;
    }

    private Map<Integer, List<String>> getSplitNames(MethodBinding method) {
        TypeBinding[] methodParameters = method.parameters;
        int paramLength = methodParameters == null ? 0 : methodParameters.length;
        Map<Integer, List<String>> result = new HashMap<>();
        for (int p = 0; p < paramLength; p++)
            result.put(p, getInverseFullName(methodParameters[p].qualifiedSourceName(),
                null)); // source is part of qualifiedSourceName here);
        return result;
    }

    /**
     * Selects the most applicable method (though similar but not to be confused with its namesake in jls)
     * All this machinery for that elusive uncommon case referred in bug 431357.
     */
    private MethodBinding getMostApplicableMethod(List<MethodBinding> possibleMethods, MethodPattern methodPattern) {
        int size = possibleMethods.size();
        MethodBinding result = size != 0 ? possibleMethods.get(0) : null;
        if (size > 1) {
            // can cache but may not be worth since this is not a common case
            Map<Integer, List<String>> methodPatternReverseNames = getSplitNames(methodPattern.parameterQualifications,
                methodPattern.parameterSimpleNames);
            int len = possibleMethods.size();
            int[][] resultMaps = new int[len][];
            for (int i = 0; i < len; ++i)
                resultMaps[i] = getResultMap(methodPatternReverseNames, getSplitNames(possibleMethods.get(i)));
            result = possibleMethods.get(getMaxResult(resultMaps));
        }
        return result;
    }

    private MethodBinding getMethodBinding0(MethodPattern methodPattern) {
        if (this.unitScope == null)
            return null;
        // Try to get binding from cache
        Binding binding = this.bindingsByPattern.get(methodPattern);
        if (binding != null) {
            if (binding instanceof MethodBinding && binding.isValidBinding())
                return (MethodBinding) binding;
        }
        //	Get binding from unit scope
        char[] typeName = PatternLocator.qualifiedPattern(methodPattern.declaringSimpleName,
            methodPattern.declaringQualification);
        if (typeName == null) {
            if (methodPattern.declaringType == null)
                return null;
            typeName = methodPattern.declaringType.getFullyQualifiedName().toCharArray();
        }
        TypeBinding declaringTypeBinding = getType(typeName, typeName);
        MethodBinding result = null;
        if (declaringTypeBinding != null) {
            if (declaringTypeBinding.isArrayType()) {
                declaringTypeBinding = declaringTypeBinding.leafComponentType();
            }
            if (!declaringTypeBinding.isBaseType()) {
                result = getMethodBinding(methodPattern, declaringTypeBinding);
            }
        }
        this.bindingsByPattern.put(methodPattern,
            result != null ? result : new ProblemMethodBinding(methodPattern.selector, null, ProblemReasons.NotFound));
        return result;
    }

    private boolean matchParams(MethodPattern methodPattern, int index, TypeBinding binding) {
        char[] qualifier = CharOperation.concat(methodPattern.parameterQualifications[index],
            methodPattern.parameterSimpleNames[index], '.');
        int offset = (qualifier.length > 0 && qualifier[0] == '*') ? 1 : 0;
        String s1 = new String(qualifier, offset, qualifier.length - offset);
        char[] s2 = CharOperation.concat(binding.qualifiedPackageName(), binding.qualifiedSourceName(), '.');
        return new String(s2).endsWith(s1);
    }

    private MethodBinding getMethodBinding(MethodPattern methodPattern, TypeBinding declaringTypeBinding) {
        MethodBinding result;
        char[][] parameterTypes = methodPattern.parameterSimpleNames;
        if (parameterTypes == null)
            return null;
        int paramTypeslength = parameterTypes.length;
        ReferenceBinding referenceBinding = (ReferenceBinding) declaringTypeBinding;
        MethodBinding[] methods = referenceBinding.getMethods(methodPattern.selector);
        int methodsLength = methods.length;
        TypeVariableBinding[] refTypeVariables = referenceBinding.typeVariables();
        int typeVarLength = refTypeVariables == null ? 0 : refTypeVariables.length;
        List<MethodBinding> possibleMethods = new ArrayList<>(methodsLength);
        for (MethodBinding method : methods) {
            TypeBinding[] methodParameters = method.parameters;
            int paramLength = methodParameters == null ? 0 : methodParameters.length;
            TypeVariableBinding[] methodTypeVariables = method.typeVariables;
            int methTypeVarLength = methodTypeVariables == null ? 0 : methodTypeVariables.length;
            boolean found = false;
            if (methodParameters != null && paramLength == paramTypeslength) {
                for (int p = 0; p < paramLength; p++) {
                    TypeBinding parameter = methodParameters[p];
                    if (matchParams(methodPattern, p, parameter)) {
                        // param erasure match
                        found = true;
                    } else {
                        // type variable
                        found = false;
                        if (refTypeVariables != null) {
                            for (int v = 0; v < typeVarLength; v++) {
                                if (!CharOperation.equals(refTypeVariables[v].sourceName, parameterTypes[p])) {
                                    found = false;
                                    break;
                                }
                                found = true;
                            }
                        }
                        if (!found && methodTypeVariables != null) {
                            for (int v = 0; v < methTypeVarLength; v++) {
                                if (!CharOperation.equals(methodTypeVariables[v].sourceName, parameterTypes[p])) {
                                    found = false;
                                    break;
                                }
                                found = true;
                            }
                        }
                        if (!found)
                            break;
                    }
                }
            }
            if (found) {
                possibleMethods.add(method);
            }
        }
        result = getMostApplicableMethod(possibleMethods, methodPattern);
        return result;
    }

    protected void report(SearchMatch match) throws CoreException {
        if (match == null) {
            if (BasicSearchEngine.VERBOSE) {
                trace("Cannot report a null match!!!"); //$NON-NLS-1$
            }
            return;
        }
        if (filterEnum(match)) {
            if (BasicSearchEngine.VERBOSE) {
                trace("Filtered package with name enum"); //$NON-NLS-1$
            }
            return;
        }
        long start = -1;
        if (BasicSearchEngine.VERBOSE) {
            start = System.currentTimeMillis();
            trace("Reporting match"); //$NON-NLS-1$
            trace("\tResource: " + match.getResource());//$NON-NLS-1$
            trace("\tPositions: [offset=" + match.getOffset() + ", length=" + match.getLength()
                + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            try {
                if (this.parser != null && match.getOffset() > 0 && match.getLength() > 0
                    && !(match.getElement() instanceof BinaryMember)) {
                    String selection = new String(this.parser.scanner.source, match.getOffset(), match.getLength());
                    trace("\tSelection: -->" + selection + "<--"); //$NON-NLS-1$ //$NON-NLS-2$
                }
            } catch (Exception e) {
                // it's just for debug purposes... ignore all exceptions in this area
            }
            try {
                JavaElement javaElement = (JavaElement) match.getElement();
                trace("\tJava element: " + javaElement.toStringWithAncestors()); //$NON-NLS-1$
                if (!javaElement.exists()) {
                    trace("\t\tWARNING: this element does NOT exist!"); //$NON-NLS-1$
                }
            } catch (Exception e) {
                // it's just for debug purposes... ignore all exceptions in this area
            }
            if (match instanceof ReferenceMatch refMatch) {
                try {
                    JavaElement local = (JavaElement) refMatch.getLocalElement();
                    if (local != null) {
                        trace("\tLocal element: " + local.toStringWithAncestors()); //$NON-NLS-1$
                    }
                    if (match instanceof TypeReferenceMatch) {
                        IJavaElement[] others = ((TypeReferenceMatch) refMatch).getOtherElements();
                        if (others != null) {
                            int length = others.length;
                            if (length > 0) {
                                trace("\tOther elements:"); //$NON-NLS-1$
                                for (IJavaElement iJavaElement : others) {
                                    JavaElement other = (JavaElement) iJavaElement;
                                    trace("\t\t- " + other.toStringWithAncestors()); //$NON-NLS-1$
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    // it's just for debug purposes... ignore all exceptions in this area
                }
            }
            trace(match.getAccuracy() == SearchMatch.A_ACCURATE ? "\tAccuracy: EXACT_MATCH" //$NON-NLS-1$
                : "\tAccuracy: POTENTIAL_MATCH"); //$NON-NLS-1$
            trace("\tRule: "); //$NON-NLS-1$
            if (match.isExact()) {
                trace("EXACT"); //$NON-NLS-1$
            } else if (match.isEquivalent()) {
                trace("EQUIVALENT"); //$NON-NLS-1$
            } else if (match.isErasure()) {
                trace("ERASURE"); //$NON-NLS-1$
            } else {
                trace("INVALID RULE"); //$NON-NLS-1$
            }
            if (match instanceof MethodReferenceMatch methodReferenceMatch) {
                if (methodReferenceMatch.isSuperInvocation()) {
                    trace("+SUPER INVOCATION"); //$NON-NLS-1$
                }
                if (methodReferenceMatch.isImplicit()) {
                    trace("+IMPLICIT"); //$NON-NLS-1$
                }
                if (methodReferenceMatch.isSynthetic()) {
                    trace("+SYNTHETIC"); //$NON-NLS-1$
                }
            }
            trace("\n\tRaw: " + match.isRaw()); //$NON-NLS-1$
        }
        this.requestor.acceptSearchMatch(match);
        if (BasicSearchEngine.VERBOSE)
            this.resultCollectorTime += System.currentTimeMillis() - start;
    }

}
