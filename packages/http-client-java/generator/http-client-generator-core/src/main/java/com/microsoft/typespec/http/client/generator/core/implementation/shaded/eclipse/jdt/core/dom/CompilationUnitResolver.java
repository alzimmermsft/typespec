/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
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
 *								bug 363858 - [dom] early throwing of AbortCompilation causes NPE in CompilationUnitResolver
 *								Bug 466279 - [hovering] IAE on hover when annotation-based null analysis is enabled
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.dom;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.SubMonitor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IClasspathEntry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.ICompilationUnit;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IJavaProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaCore;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaModelException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.WorkingCopyOwner;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.CategorizedProblem;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.CompilationResult;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.Compiler;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ICompilerRequestor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.IProblemFactory;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.AccessRestriction;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.INameEnvironment;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.ISourceType;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.Binding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.parser.Parser;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.util.HashtableOfObject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.util.Util;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.CancelableNameEnvironment;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.CancelableProblemFactory;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.ClasspathEntry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.INameEnvironmentWithProgress;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.JavaProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.NameLookup;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.SourceTypeElementInfo;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.dom.ICompilationUnitResolver;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.BindingKeyResolver;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.CommentRecorderParser;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings({ "rawtypes", "unchecked" })
class CompilationUnitResolver extends Compiler {

    private static final class ECJCompilationUnitResolver implements ICompilationUnitResolver {

        @Override
        public void parse(ICompilationUnit[] compilationUnits, ASTRequestor requestor, int apiLevel,
            Map<String, String> compilerOptions, int flags, IProgressMonitor monitor) {
            CompilationUnitResolver.parse(compilationUnits, requestor, apiLevel, compilerOptions, flags, monitor);
        }

        @Override
        public void parse(String[] sourceFilePaths, String[] encodings, FileASTRequestor requestor, int apiLevel,
            Map<String, String> compilerOptions, int flags, IProgressMonitor monitor) {
            CompilationUnitResolver.parse(sourceFilePaths, encodings, requestor, apiLevel, compilerOptions, flags,
                monitor);
        }

        @Override
        public CompilationUnit toCompilationUnit(
            com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit,
            final boolean initialNeedsToResolveBinding, IJavaProject project, List<Classpath> classpaths,
            int focalPosition, int apiLevel, Map<String, String> compilerOptions,
            WorkingCopyOwner parsedUnitWorkingCopyOwner, WorkingCopyOwner typeRootWorkingCopyOwner, int flags,
            IProgressMonitor monitor) {
            return CompilationUnitResolver.toCompilationUnit(sourceUnit, initialNeedsToResolveBinding, project,
                classpaths, focalPosition == -1 ? null : new NodeSearcher(focalPosition), apiLevel, compilerOptions,
                parsedUnitWorkingCopyOwner, typeRootWorkingCopyOwner, flags, monitor);
        }
    }

    private static ECJCompilationUnitResolver FACADE;

    public static synchronized ICompilationUnitResolver getInstance() {
        if (FACADE == null) {
            FACADE = new ECJCompilationUnitResolver();
        }
        return FACADE;
    }

    public static final int RESOLVE_BINDING = 0x1;
    public static final int PARTIAL = 0x2;
    public static final int STATEMENT_RECOVERY = 0x4;
    public static final int IGNORE_METHOD_BODIES = 0x8;
    public static final int BINDING_RECOVERY = 0x10;
    public static final int INCLUDE_RUNNING_VM_BOOTCLASSPATH = 0x20;

    /* A list of int */
    static class IntArrayList {
        public int[] list = new int[5];
        public int length = 0;

        public void add(int i) {
            if (this.list.length == this.length) {
                System.arraycopy(this.list, 0, this.list = new int[this.length * 2], 0, this.length);
            }
            this.list[this.length++] = i;
        }
    }

    /*
     * The sources that were requested.
     * Map from file name (char[]) to org.eclipse.jdt.internal.compiler.env.ICompilationUnit.
     */
    HashtableOfObject requestedSources;

    /*
     * The binding keys that were requested.
     * Map from file name (char[]) to BindingKey (or ArrayList if multiple keys in the same file).
     */
    HashtableOfObject requestedKeys;

    DefaultBindingResolver.BindingTables bindingTables;

    boolean hasCompilationAborted;
    CategorizedProblem abortProblem;

    private final IProgressMonitor monitor;

    /**
     * Set to <code>true</code> if the receiver was initialized using a java project name environment
     */
    boolean fromJavaProject;

    /**
     * Answer a new CompilationUnitVisitor using the given name environment and compiler options.
     * The environment and options will be in effect for the lifetime of the compiler.
     * When the compiler is run, compilation results are sent to the given requestor.
     *
     * @param environment org.eclipse.jdt.internal.compiler.api.env.INameEnvironment
     * Environment used by the compiler in order to resolve type and package
     * names. The name environment implements the actual connection of the compiler
     * to the outside world (for example, in batch mode the name environment is performing
     * pure file accesses, reuse previous build state or connection to repositories).
     * Note: the name environment is responsible for implementing the actual classpath
     * rules.
     *
     * @param policy org.eclipse.jdt.internal.compiler.api.problem.IErrorHandlingPolicy
     * Configurable part for problem handling, allowing the compiler client to
     * specify the rules for handling problems (stop on first error or accumulate
     * them all) and at the same time perform some actions such as opening a dialog
     * in UI when compiling interactively.
     * @see org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies
     *
     * @param compilerOptions The compiler options to use for the resolution.
     *
     * @param requestor org.eclipse.jdt.internal.compiler.api.ICompilerRequestor
     * Component which will receive and persist all compilation results and is intended
     * to consume them as they are produced. Typically, in a batch compiler, it is
     * responsible for writing out the actual .class files to the file system.
     * @see org.eclipse.jdt.internal.compiler.CompilationResult
     *
     * @param problemFactory org.eclipse.jdt.internal.compiler.api.problem.IProblemFactory
     * Factory used inside the compiler to create problem descriptors. It allows the
     * compiler client to supply its own representation of compilation problems in
     * order to avoid object conversions. Note that the factory is not supposed
     * to accumulate the created problems, the compiler will gather them all and hand
     * them back as part of the compilation unit result.
     */
    public CompilationUnitResolver(INameEnvironment environment, IErrorHandlingPolicy policy,
        CompilerOptions compilerOptions, ICompilerRequestor requestor, IProblemFactory problemFactory,
        IProgressMonitor monitor, boolean fromJavaProject) {

        super(environment, policy, compilerOptions, requestor, problemFactory);
        this.hasCompilationAborted = false;
        this.monitor = monitor;
        this.fromJavaProject = fromJavaProject;
    }

    /*
     * Add additional source types
     */
    @Override
    public void accept(ISourceType[] sourceTypes, PackageBinding packageBinding, AccessRestriction accessRestriction) {
        // Need to reparse the entire source of the compilation unit so as to get source positions
        // (case of processing a source that was not known by beginToCompile (e.g. when asking to createBinding))
        SourceTypeElementInfo sourceType = (SourceTypeElementInfo) sourceTypes[0];
        accept(
            (com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.ICompilationUnit) sourceType
                .getHandle()
                .getCompilationUnit(),
            accessRestriction);
    }

    @Override
    public synchronized void accept(
        com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit,
        AccessRestriction accessRestriction) {
        super.accept(sourceUnit, accessRestriction);
    }

    IBinding createBinding(String key) {
        if (this.bindingTables == null)
            throw new RuntimeException("Cannot be called outside ASTParser#createASTs(...)"); //$NON-NLS-1$
        BindingKeyResolver keyResolver = new BindingKeyResolver(key, this, this.lookupEnvironment);
        Binding compilerBinding = keyResolver.getCompilerBinding();
        if (compilerBinding == null)
            return null;
        DefaultBindingResolver resolver = new DefaultBindingResolver(this.lookupEnvironment, null/* no owner */,
            this.bindingTables, false, this.fromJavaProject);
        return resolver.getBinding(compilerBinding);
    }

    public static CompilationUnit convert(CompilationUnitDeclaration compilationUnitDeclaration, char[] source,
        int apiLevel, Map options, boolean needToResolveBindings, WorkingCopyOwner owner,
        DefaultBindingResolver.BindingTables bindingTables, int flags, IProgressMonitor monitor,
        boolean fromJavaProject) {
        return convert(compilationUnitDeclaration, source, apiLevel, options, needToResolveBindings, owner,
            bindingTables, flags, monitor, fromJavaProject, null);
    }

    public static CompilationUnit convert(CompilationUnitDeclaration compilationUnitDeclaration, char[] source,
        int apiLevel, Map options, boolean needToResolveBindings, WorkingCopyOwner owner,
        DefaultBindingResolver.BindingTables bindingTables, int flags, IProgressMonitor monitor,
        boolean fromJavaProject, IJavaProject project) {
        BindingResolver resolver;
        AST ast
            = AST.newAST(apiLevel, JavaCore.ENABLED.equals(options.get(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES)));
        String sourceModeSetting = (String) options.get(JavaCore.COMPILER_SOURCE);
        long sourceLevel = CompilerOptions.versionToJdkLevel(sourceModeSetting);
        if (sourceLevel == 0) {
            // unknown sourceModeSetting
            sourceLevel = CompilerOptions.getFirstSupportedJdkLevel();
        }
        ast.scanner.sourceLevel = sourceLevel;
        String compliance = (String) options.get(JavaCore.COMPILER_COMPLIANCE);
        long complianceLevel = CompilerOptions.versionToJdkLevel(compliance);
        if (complianceLevel == 0) {
            // unknown sourceModeSetting
            complianceLevel = sourceLevel;
        }
        ast.scanner.complianceLevel = complianceLevel;
        ast.setDefaultNodeFlag(ASTNode.ORIGINAL);
        CompilationUnit compilationUnit;
        ASTConverter converter = new ASTConverter(options, needToResolveBindings, monitor);
        if (needToResolveBindings) {
            resolver = new DefaultBindingResolver(compilationUnitDeclaration.scope, owner, bindingTables,
                (flags & ICompilationUnit.ENABLE_BINDINGS_RECOVERY) != 0, fromJavaProject);
            ast.setFlag(flags | AST.RESOLVED_BINDINGS);
        } else {
            resolver = new BindingResolver();
            ast.setFlag(flags);
        }
        ast.setBindingResolver(resolver);
        converter.setAST(ast);
        converter.docParser.setProjectPath(CompilationUnitResolver.getProjectPath(project));
        converter.docParser.setProjectSrcClasspath(CompilationUnitResolver.getSourceClassPaths(project));
        compilationUnit = converter.convert(compilationUnitDeclaration, source);
        compilationUnit.setLineEndTable(compilationUnitDeclaration.compilationResult.getLineSeparatorPositions());
        ast.setDefaultNodeFlag(0);
        ast.setOriginalModificationCount(ast.modificationCount());
        return compilationUnit;
    }

    /**
     * @return absolute path in local file system, may return {@code null}
     */
    private static String getProjectPath(IJavaProject project) {
        if (project == null) {
            return null;
        }
        IProject rp = project.getProject();
        if (rp == null) {
            return null;
        }
        IPath location = rp.getLocation();
        if (location == null) {
            return null;
        }
        return location.toOSString();
    }

    private static ArrayList<String> getSourceClassPaths(IJavaProject project) {
        ArrayList<String> srcClassPath = new ArrayList<>();
        if (project == null)
            return srcClassPath;
        if (project.getProject() == null)
            return srcClassPath;
        IClasspathEntry[] resolvedClasspath = null;
        try {
            resolvedClasspath = project.getResolvedClasspath(true);
        } catch (JavaModelException e) {
            // do nothing
        }
        if (resolvedClasspath == null)
            return srcClassPath;

        for (IClasspathEntry entry : resolvedClasspath) {
            if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                if (entry instanceof ClasspathEntry) {
                    IPath path = ((ClasspathEntry) entry).getPath();
                    srcClassPath.add(path.removeFirstSegments(1).toString());
                }
            }
        }
        return srcClassPath;
    }

    protected static CompilerOptions getCompilerOptions(Map options, boolean statementsRecovery) {
        CompilerOptions compilerOptions = new CompilerOptions(options);
        compilerOptions.performMethodsFullRecovery = statementsRecovery;
        compilerOptions.performStatementsRecovery = statementsRecovery;
        compilerOptions.parseLiteralExpressionsAsConstants = false;
        compilerOptions.storeAnnotations = true /* store annotations in the bindings */;
        compilerOptions.ignoreSourceFolderWarningOption = true;
        return compilerOptions;
    }

    /*
     * Low-level API performing the actual compilation
     */
    protected static IErrorHandlingPolicy getHandlingPolicy() {

        // passes the initial set of files to the batch oracle (to avoid finding more than once the same units when case
        // insensitive match)
        return new IErrorHandlingPolicy() {
            @Override
            public boolean stopOnFirstError() {
                return false;
            }

            @Override
            public boolean proceedOnErrors() {
                return false; // stop if there are some errors
            }

            @Override
            public boolean ignoreAllErrors() {
                return false;
            }
        };
    }

    /*
     * Answer the component to which will be handed back compilation results from the compiler
     */
    protected static ICompilerRequestor getRequestor() {
        return new ICompilerRequestor() {
            @Override
            public void acceptResult(CompilationResult compilationResult) {
                // do nothing
            }
        };
    }

    @Override
    public void initializeParser() {
        this.parser = new CommentRecorderParser(this.problemReporter, false);
    }

    @Override
    public void process(CompilationUnitDeclaration unit, int i) {
        // don't resolve a second time the same unit (this would create the same binding twice)
        char[] fileName = unit.compilationResult.getFileName();
        if (this.requestedKeys.get(fileName) == null && this.requestedSources.get(fileName) == null)
            super.process(unit, i);
    }

    /*
     * Compiler crash recovery in case of unexpected runtime exceptions
     */
    @Override
    protected void handleInternalException(Throwable internalException, CompilationUnitDeclaration unit,
        CompilationResult result) {
        super.handleInternalException(internalException, unit, result);
        if (unit != null) {
            removeUnresolvedBindings(unit);
        }
    }

    /*
     * Compiler recovery in case of internal AbortCompilation event
     */
    @Override
    protected void handleInternalException(AbortCompilation abortException, CompilationUnitDeclaration unit) {
        super.handleInternalException(abortException, unit);
        if (unit != null) {
            removeUnresolvedBindings(unit);
        }
        this.hasCompilationAborted = true;
        this.abortProblem = abortException.problem;
    }

    public static void parse(ICompilationUnit[] compilationUnits, ASTRequestor astRequestor, int apiLevel, Map options,
        int flags, IProgressMonitor monitor) {
        CompilerOptions compilerOptions = new CompilerOptions(options);
        compilerOptions.ignoreMethodBodies = (flags & ICompilationUnit.IGNORE_METHOD_BODIES) != 0;
        Parser parser
            = new CommentRecorderParser(new ProblemReporter(DefaultErrorHandlingPolicies.proceedWithAllProblems(),
                compilerOptions, new DefaultProblemFactory()), false);
        int unitLength = compilationUnits.length;
        SubMonitor subMonitor = SubMonitor.convert(monitor);
        for (int i = 0; i < unitLength; i++) {
            subMonitor.setWorkRemaining(unitLength - i);
            com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit
                = (com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.ICompilationUnit) compilationUnits[i];
            CompilationResult compilationResult
                = new CompilationResult(sourceUnit, 0, 0, compilerOptions.maxProblemsPerUnit);
            CompilationUnitDeclaration compilationUnitDeclaration = parser.dietParse(sourceUnit, compilationResult);

            if (compilationUnitDeclaration.ignoreMethodBodies) {
                compilationUnitDeclaration.ignoreFurtherInvestigation = true;
                // if initial diet parse did not work, no need to dig into method bodies.
                continue;
            }

            // fill the methods bodies in order for the code to be generated
            // real parse of the method....
            TypeDeclaration[] types = compilationUnitDeclaration.types;
            if (types != null) {
                for (TypeDeclaration type : types) {
                    type.parseMethods(parser, compilationUnitDeclaration);
                }
            }

            // convert AST
            CompilationUnit node = convert(compilationUnitDeclaration, parser.scanner.getSource(), apiLevel, options,
                false/* don't resolve binding */, null/* no owner needed */, null/* no binding table needed */,
                flags /* flags */, subMonitor.split(1), true);
            node.setTypeRoot(compilationUnits[i]);

            // accept AST
            astRequestor.acceptAST(compilationUnits[i], node);
        }
    }

    public static void parse(String[] sourceUnits, String[] encodings, FileASTRequestor astRequestor, int apiLevel,
        Map options, int flags, IProgressMonitor monitor) {
        CompilerOptions compilerOptions = new CompilerOptions(options);
        compilerOptions.ignoreMethodBodies = (flags & ICompilationUnit.IGNORE_METHOD_BODIES) != 0;
        Parser parser
            = new CommentRecorderParser(new ProblemReporter(DefaultErrorHandlingPolicies.proceedWithAllProblems(),
                compilerOptions, new DefaultProblemFactory()), false);
        int unitLength = sourceUnits.length;
        SubMonitor subMonitor = SubMonitor.convert(monitor, unitLength);
        for (int i = 0; i < unitLength; i++) {
            SubMonitor iterationMonitor = subMonitor.split(1);
            char[] contents;
            String encoding = encodings != null ? encodings[i] : null;
            try {
                contents = Util.getFileCharContent(new File(sourceUnits[i]), encoding);
            } catch (IOException e) {
                // go to the next unit
                continue;
            }
            if (contents == null) {
                // go to the next unit
                continue;
            }
            com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.batch.CompilationUnit compilationUnit
                = new com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.batch.CompilationUnit(contents, sourceUnits[i], encoding);
            com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit
                = compilationUnit;
            CompilationResult compilationResult
                = new CompilationResult(sourceUnit, 0, 0, compilerOptions.maxProblemsPerUnit);
            CompilationUnitDeclaration compilationUnitDeclaration = parser.dietParse(sourceUnit, compilationResult);

            if (compilationUnitDeclaration.ignoreMethodBodies) {
                compilationUnitDeclaration.ignoreFurtherInvestigation = true;
                // if initial diet parse did not work, no need to dig into method bodies.
                continue;
            }

            // fill the methods bodies in order for the code to be generated
            // real parse of the method....
            com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.TypeDeclaration[] types = compilationUnitDeclaration.types;
            if (types != null) {
                for (TypeDeclaration type : types) {
                    type.parseMethods(parser, compilationUnitDeclaration);
                }
            }

            // convert AST
            CompilationUnit node = convert(compilationUnitDeclaration, parser.scanner.getSource(), apiLevel, options,
                false/* don't resolve binding */, null/* no owner needed */, null/* no binding table needed */,
                flags /* flags */, iterationMonitor, true);
            node.setTypeRoot(null);

            // accept AST
            astRequestor.acceptAST(sourceUnits[i], node);
        }
    }

    public static CompilationUnitDeclaration parse(
        com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit,
        NodeSearcher nodeSearcher, Map settings, int flags) {
        return parse(sourceUnit, nodeSearcher, settings, flags, null);
    }

    public static CompilationUnitDeclaration parse(
        com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit,
        NodeSearcher nodeSearcher, Map settings, int flags, IJavaProject project) {
        if (sourceUnit == null) {
            throw new IllegalStateException();
        }
        CompilerOptions compilerOptions = new CompilerOptions(settings);
        boolean statementsRecovery = (flags & ICompilationUnit.ENABLE_STATEMENTS_RECOVERY) != 0;
        compilerOptions.performMethodsFullRecovery = statementsRecovery;
        compilerOptions.performStatementsRecovery = statementsRecovery;
        compilerOptions.ignoreMethodBodies = (flags & ICompilationUnit.IGNORE_METHOD_BODIES) != 0;
        Parser parser = new CommentRecorderParser(
            new ProblemReporter(DefaultErrorHandlingPolicies.proceedWithAllProblems(), compilerOptions,
                new DefaultProblemFactory()), false);
        if (project != null) {
            parser.javadocParser.setProjectPath(getProjectPath(project));
            parser.javadocParser.setProjectSrcClasspath(getSourceClassPaths(project));

        }
        CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0,
            compilerOptions.maxProblemsPerUnit);
        CompilationUnitDeclaration compilationUnitDeclaration = parser.dietParse(sourceUnit, compilationResult);

        if (compilationUnitDeclaration.ignoreMethodBodies) {
            compilationUnitDeclaration.ignoreFurtherInvestigation = true;
            // if initial diet parse did not work, no need to dig into method bodies.
            return compilationUnitDeclaration;
        }

        if (nodeSearcher != null) {
            char[] source = parser.scanner.getSource();
            int searchPosition = nodeSearcher.position;
            if (searchPosition < 0 || searchPosition > source.length) {
                // the position is out of range. There is no need to search for a node.
                return compilationUnitDeclaration;
            }

            compilationUnitDeclaration.traverse(nodeSearcher, compilationUnitDeclaration.scope);

            com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.ASTNode
                node = nodeSearcher.found;
            if (node == null) {
                return compilationUnitDeclaration;
            }

            TypeDeclaration enclosingTypeDeclaration = nodeSearcher.enclosingType;

            if (node instanceof AbstractMethodDeclaration) {
                ((AbstractMethodDeclaration) node).parseStatements(parser, compilationUnitDeclaration);
            } else if (enclosingTypeDeclaration != null) {
                if (node instanceof com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.Initializer) {
                    ((com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.Initializer) node).parseStatements(
                        parser, enclosingTypeDeclaration, compilationUnitDeclaration);
                } else if (node instanceof com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.TypeDeclaration) {
                    ((TypeDeclaration) node).parseMethods(parser, compilationUnitDeclaration);
                }
            }
        } else {
            // fill the methods bodies in order for the code to be generated
            // real parse of the method....
            TypeDeclaration[] types = compilationUnitDeclaration.types;
            if (types != null) {
                for (TypeDeclaration type : types) {
                    type.parseMethods(parser, compilationUnitDeclaration);
                }
            }
        }
        return compilationUnitDeclaration;
    }

    public static CompilationUnitDeclaration resolve(
        com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit,
        IJavaProject javaProject, List classpaths, NodeSearcher nodeSearcher, Map options, WorkingCopyOwner owner,
        int flags, IProgressMonitor monitor) throws JavaModelException {

        CompilationUnitDeclaration unit;
        INameEnvironmentWithProgress environment = null;
        CancelableProblemFactory problemFactory = null;
        CompilationUnitResolver resolver;
        try {
            if (javaProject == null) {
                Classpath[] allEntries = new Classpath[classpaths.size()];
                classpaths.toArray(allEntries);
                environment = new NameEnvironmentWithProgress(allEntries, null, monitor);
            } else {
                environment = new CancelableNameEnvironment((JavaProject) javaProject, owner, monitor);
            }
            problemFactory = new CancelableProblemFactory(monitor);
            CompilerOptions compilerOptions
                = getCompilerOptions(options, (flags & ICompilationUnit.ENABLE_STATEMENTS_RECOVERY) != 0);
            boolean ignoreMethodBodies = (flags & ICompilationUnit.IGNORE_METHOD_BODIES) != 0;
            compilerOptions.ignoreMethodBodies = ignoreMethodBodies;
            resolver = new CompilationUnitResolver(environment, getHandlingPolicy(), compilerOptions, getRequestor(),
                problemFactory, monitor, javaProject != null);
            boolean analyzeAndGenerateCode = !ignoreMethodBodies;
            unit = resolver.resolve(null, // no existing compilation unit declaration
                sourceUnit, nodeSearcher, true, // method verification
                analyzeAndGenerateCode, // analyze code
                analyzeAndGenerateCode); // generate code
            if (resolver.hasCompilationAborted) {
                // the bindings could not be resolved due to missing types in name environment
                // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=86541
                CompilationUnitDeclaration unitDeclaration = parse(sourceUnit, nodeSearcher, options, flags);
                if (unit != null) {
                    final int problemCount = unit.compilationResult.problemCount;
                    if (problemCount != 0) {
                        unitDeclaration.compilationResult.problems = new CategorizedProblem[problemCount];
                        System.arraycopy(unit.compilationResult.problems, 0, unitDeclaration.compilationResult.problems,
                            0, problemCount);
                        unitDeclaration.compilationResult.problemCount = problemCount;
                    }
                } else if (resolver.abortProblem != null) {
                    unitDeclaration.compilationResult.problemCount = 1;
                    unitDeclaration.compilationResult.problems = new CategorizedProblem[] { resolver.abortProblem };
                }
                return unitDeclaration;
            }
            if (NameLookup.VERBOSE && environment instanceof CancelableNameEnvironment) {
                CancelableNameEnvironment cancelableNameEnvironment = (CancelableNameEnvironment) environment;
                cancelableNameEnvironment.printTimeSpent();
            }
            if (unit != null
                && unit.scope != null
                && unit.scope.environment != null
                && unit.scope.environment.unitBeingCompleted == null) {
                unit.scope.environment.unitBeingCompleted = unit;
            }
            return unit;
        } finally {
            if (environment != null) {
                // don't hold a reference to this external object
                environment.setMonitor(null);
            }
            if (problemFactory != null) {
                problemFactory.monitor = null; // don't hold a reference to this external object
            }
        }
    }

    /*
     * When unit result is about to be accepted, removed back pointers
     * to unresolved bindings
     */
    public void removeUnresolvedBindings(CompilationUnitDeclaration compilationUnitDeclaration) {
        final TypeDeclaration[] types = compilationUnitDeclaration.types;
        if (types != null) {
            for (TypeDeclaration type : types) {
                removeUnresolvedBindings(type);
            }
        }
    }

    private void removeUnresolvedBindings(TypeDeclaration type) {
        final TypeDeclaration[] memberTypes = type.memberTypes;
        if (memberTypes != null) {
            for (TypeDeclaration memberType : memberTypes) {
                removeUnresolvedBindings(memberType);
            }
        }
        if (type.binding != null && (type.binding.modifiers & ExtraCompilerModifiers.AccUnresolved) != 0) {
            type.binding = null;
        }

        final com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.FieldDeclaration[] fields
            = type.fields;
        if (fields != null) {
            for (com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.FieldDeclaration field : fields) {
                if (field.binding != null && (field.binding.modifiers & ExtraCompilerModifiers.AccUnresolved) != 0) {
                    field.binding = null;
                }
            }
        }

        final AbstractMethodDeclaration[] methods = type.methods;
        if (methods != null) {
            for (AbstractMethodDeclaration method : methods) {
                if (method.binding != null && (method.binding.modifiers & ExtraCompilerModifiers.AccUnresolved) != 0) {
                    method.binding = null;
                }
            }
        }
    }

    private CompilationUnitDeclaration resolve(CompilationUnitDeclaration unit,
        com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit,
        NodeSearcher nodeSearcher, boolean verifyMethods, boolean analyzeCode, boolean generateCode) {

        try {

            if (unit == null) {
                // build and record parsed units
                this.parseThreshold = 0; // will request a full parse
                beginToCompile(new com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.ICompilationUnit[] { sourceUnit });
                // find the right unit from what was injected via accept(ICompilationUnit,..):
                for (int i = 0, max = this.totalUnits; i < max; i++) {
                    CompilationUnitDeclaration currentCompilationUnitDeclaration = this.unitsToProcess[i];
                    if (currentCompilationUnitDeclaration != null
                        && currentCompilationUnitDeclaration.compilationResult.compilationUnit == sourceUnit) {
                        unit = currentCompilationUnitDeclaration;
                        break;
                    }
                }
                if (unit == null) {
                    unit = this.unitsToProcess[0]; // fall back to old behavior
                }
            } else {
                // initial type binding creation
                this.lookupEnvironment.buildTypeBindings(unit, null /* no access restriction */);

                // binding resolution
                this.lookupEnvironment.completeTypeBindings();
            }

            if (nodeSearcher == null) {
                this.parser.getMethodBodies(unit); // no-op if method bodies have already been parsed
            } else {
                int searchPosition = nodeSearcher.position;
                char[] source = sourceUnit.getContents();
                int length = source.length;
                if (searchPosition >= 0 && searchPosition <= length) {
                    unit.traverse(nodeSearcher, unit.scope);

                    com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.ASTNode node
                        = nodeSearcher.found;

                    if (node != null) {
                        // save existing values to restore them at the end of the parsing process
                        // see bug 47079 for more details
                        int[] oldLineEnds = this.parser.scanner.lineEnds;
                        int oldLinePtr = this.parser.scanner.linePtr;

                        this.parser.scanner.setSource(source, unit.compilationResult);

                        TypeDeclaration enclosingTypeDeclaration = nodeSearcher.enclosingType;
                        if (node instanceof AbstractMethodDeclaration) {
                            ((AbstractMethodDeclaration) node).parseStatements(this.parser, unit);
                        } else if (enclosingTypeDeclaration != null) {
                            if (node instanceof com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.Initializer) {
                                ((com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.Initializer) node)
                                    .parseStatements(this.parser, enclosingTypeDeclaration, unit);
                            } else if (node instanceof com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.ast.TypeDeclaration) {
                                ((TypeDeclaration) node).parseMethods(this.parser, unit);
                            }
                        }
                        // this is done to prevent any side effects on the compilation unit result
                        // line separator positions array.
                        this.parser.scanner.lineEnds = oldLineEnds;
                        this.parser.scanner.linePtr = oldLinePtr;
                    }
                }
            }

            if (unit.scope != null) {
                CompilationUnitDeclaration previousUnit = this.lookupEnvironment.unitBeingCompleted;
                this.lookupEnvironment.unitBeingCompleted = unit;
                try {
                    // fault in fields & methods
                    unit.scope.faultInTypes();
                    if (unit.scope != null && verifyMethods) {
                        // http://dev.eclipse.org/bugs/show_bug.cgi?id=23117
                        // verify inherited methods
                        unit.scope.verifyMethods(this.lookupEnvironment.methodVerifier());
                    }
                    // type checking
                    unit.resolve();

                    // flow analysis
                    if (analyzeCode)
                        unit.analyseCode();

                    // code generation
                    if (generateCode)
                        unit.generateCode();

                    // finalize problems (suppressWarnings)
                    unit.finalizeProblems();
                } finally {
                    this.lookupEnvironment.unitBeingCompleted = previousUnit; // paranoia, always null in
                                                                              // org.eclipse.jdt.core.tests.dom.RunAllTests
                }
            }
            if (this.unitsToProcess != null)
                this.unitsToProcess[0] = null; // release reference to processed unit declaration
            this.requestor.acceptResult(unit.compilationResult.tagAsAccepted());
            return unit;
        } catch (AbortCompilation e) {
            this.handleInternalException(e, unit);
            return unit == null ? this.unitsToProcess[0] : unit;
        } catch (Error | RuntimeException e) {
            this.handleInternalException(e, unit, null);
            throw e; // rethrow
        }
    }

    /*
     * Internal API used to resolve a given compilation unit. Can run a subset of the compilation process
     */
    @Override
    public CompilationUnitDeclaration resolve(
        com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit,
        boolean verifyMethods, boolean analyzeCode, boolean generateCode) {

        return resolve(null, /* no existing compilation unit declaration */
            sourceUnit, null/* no node searcher */, verifyMethods, analyzeCode, generateCode);
    }

    /*
     * Internal API used to resolve a given compilation unit. Can run a subset of the compilation process
     */
    @Override
    public CompilationUnitDeclaration resolve(CompilationUnitDeclaration unit,
        com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit,
        boolean verifyMethods, boolean analyzeCode, boolean generateCode) {

        return resolve(unit, sourceUnit, null/* no node searcher */, verifyMethods, analyzeCode, generateCode);
    }

    public static CompilationUnit toCompilationUnit(
        com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit,
        final boolean initialNeedsToResolveBinding, IJavaProject project, List<Classpath> classpaths,
        NodeSearcher nodeSearcher, int apiLevel, Map<String, String> compilerOptions,
        WorkingCopyOwner parsedUnitWorkingCopyOwner, WorkingCopyOwner typeRootWorkingCopyOwner, int flags,
        IProgressMonitor monitor) {
        // this -> astParser, pass as args
        CompilationUnitDeclaration compilationUnitDeclaration = null;
        boolean needsToResolveBindingsState = initialNeedsToResolveBinding;
        try {
            if (initialNeedsToResolveBinding) {
                try {
                    // parse and resolve
                    compilationUnitDeclaration = CompilationUnitResolver.resolve(sourceUnit, project, classpaths,
                        nodeSearcher, compilerOptions, parsedUnitWorkingCopyOwner, flags, monitor);
                } catch (JavaModelException e) {
                    flags &= ~ICompilationUnit.ENABLE_BINDINGS_RECOVERY;
                    compilationUnitDeclaration
                        = CompilationUnitResolver.parse(sourceUnit, nodeSearcher, compilerOptions, flags);
                    needsToResolveBindingsState = false;
                }
            } else {
                compilationUnitDeclaration
                    = CompilationUnitResolver.parse(sourceUnit, nodeSearcher, compilerOptions, flags, project);
            }
            return CompilationUnitResolver.convert(compilationUnitDeclaration, sourceUnit.getContents(), apiLevel,
                compilerOptions, needsToResolveBindingsState, typeRootWorkingCopyOwner,
                needsToResolveBindingsState ? new DefaultBindingResolver.BindingTables() : null, flags, monitor,
                project != null, project);
        } finally {
            if (compilationUnitDeclaration != null && initialNeedsToResolveBinding) {
                compilationUnitDeclaration.cleanUp();
            }
        }
    }

}
