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
 *     Stephan Herrmann - Contribution for
 *								Bug 440477 - [null] Infrastructure for feeding external annotations into compilation
 *     Karsten Thoms - Bug 532505
 *     Sebastian Zarnekow - Contribution for
 *								Bug 545491 - Poor performance of ReferenceCollection with many source files
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.builder;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IContainer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IWorkspaceRoot;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Path;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.IProblem;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.AccessRule;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.IUpdatableModule;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.IUpdatableModule.AddExports;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.IUpdatableModule.AddReads;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.IUpdatableModule.UpdateKind;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.util.Util;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.JavaModelManager;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.DeduplicationUtil;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class State {
    // NOTE: this state cannot contain types that are not defined in this project

    String javaProjectName;
    public ClasspathMultiDirectory[] sourceLocations;
    public ClasspathMultiDirectory[] testSourceLocations;
    public ClasspathLocation[] binaryLocations;
    public ClasspathLocation[] testBinaryLocations;
    // keyed by the project relative path of the type (i.e. "src1/p1/p2/A.java"), value is a ReferenceCollection or an AdditionalTypeCollection
    Map<String, ReferenceCollection> references;
    // Holds a mapping of types to a path to detect duplicate type definitions (possibly depending on the release for multi-release types)
    public TypeLocators typeLocators;

    int buildNumber;
    long lastStructuralBuildTime;
    HashMap<String, Long> structuralBuildTimes;

    // keep track of ? structurally changed types, otherwise consider all to be changed

    public static final byte VERSION = 0x0027;

    static final byte SOURCE_FOLDER = 1;
    static final byte BINARY_FOLDER = 2;
    static final byte EXTERNAL_JAR = 3;
    static final byte INTERNAL_JAR = 4;

    /** typical values of accessRule.pattern for encoding hint */
    private static final int[] PROBLEM_IDS = new int[] {
        0,
        IProblem.ForbiddenReference,
        IProblem.DiscouragedReference,
        IProblem.ForbiddenReference | AccessRule.IgnoreIfBetter,
        IProblem.DiscouragedReference | AccessRule.IgnoreIfBetter };

    State() {
        // constructor with no argument
        this.typeLocators = new TypeLocators();
    }

    protected State(JavaBuilder javaBuilder) {
        this.javaProjectName = javaBuilder.currentProject.getName();
        this.sourceLocations = javaBuilder.nameEnvironment.sourceLocations;
        this.binaryLocations = javaBuilder.nameEnvironment.binaryLocations;
        this.testSourceLocations = javaBuilder.testNameEnvironment.sourceLocations;
        this.testBinaryLocations = javaBuilder.testNameEnvironment.binaryLocations;
        this.references = new LinkedHashMap<>(7);
        this.typeLocators = new TypeLocators();

        this.buildNumber = 0; // indicates a full build
        this.lastStructuralBuildTime = computeStructuralBuildTime(
            javaBuilder.lastState == null ? 0 : javaBuilder.lastState.lastStructuralBuildTime);
        this.structuralBuildTimes = new HashMap<>();
    }

    long computeStructuralBuildTime(long previousTime) {
        long newTime = System.currentTimeMillis();
        if (newTime <= previousTime)
            newTime = previousTime + 1;
        return newTime;
    }

    /**
     * Compares this build state with other one in terms of persistence (transient data is ignored)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof State other)) {
            return false;
        }
        return this.buildNumber == other.buildNumber && this.lastStructuralBuildTime == other.lastStructuralBuildTime
            && Objects.equals(this.javaProjectName, other.javaProjectName) && Arrays.equals(this.sourceLocations,
            other.sourceLocations) && Arrays.equals(this.binaryLocations, other.binaryLocations) && Arrays.equals(
            this.testSourceLocations, other.testSourceLocations) && Arrays.equals(this.testBinaryLocations,
            other.testBinaryLocations) && Objects.equals(this.typeLocators, other.typeLocators) && Objects.equals(
            this.references, other.references);
        // Below fields aren't persisted
        //			&& this.previousStructuralBuildTime == other.previousStructuralBuildTime
        //			&& Arrays.equals(this.knownPackageNames, other.knownPackageNames)
        //			&& Objects.equals(this.structurallyChangedTypes, other.structurallyChangedTypes)
        //			&& Objects.equals(this.structuralBuildTimes, other.structuralBuildTimes)
    }

    @Override
    public int hashCode() {
        return 31 + Objects.hash(this.javaProjectName);
    }

    public Map<String, ReferenceCollection> getReferences() {
        return this.references;
    }

    static State read(IProject project, DataInputStream input) throws IOException, CoreException {
        CompressedReader in = new CompressedReader(input);
        if (VERSION != in.readByte()) {
            return null;
        }

        State newState = new State();
        newState.javaProjectName = in.readStringUsingDictionary();
        if (!project.getName().equals(newState.javaProjectName)) {
            return null;
        }
        newState.buildNumber = in.readInt();
        newState.lastStructuralBuildTime = in.readLong();

        newState.sourceLocations = readSourceLocations(project, in);
        newState.binaryLocations = readBinaryLocations(project, in, newState.sourceLocations);

        newState.testSourceLocations = readSourceLocations(project, in);
        newState.testBinaryLocations = readBinaryLocations(project, in, newState.testSourceLocations);

        int length;
        newState.structuralBuildTimes = new HashMap<>(length = in.readInt());
        for (int i = 0; i < length; i++)
            newState.structuralBuildTimes.put(in.readStringUsingDictionary(), in.readLong());

        String[] internedTypeLocators = new String[length = in.readInt()];
        for (int i = 0; i < length; i++)
            internedTypeLocators[i] = in.readStringUsingLast();
        newState.typeLocators.read(in, internedTypeLocators);

        /*
         * Here we read global arrays of names for the entire project - do not mess up the ordering while interning
         */
        char[][] internedRootNames = ReferenceCollection.internSimpleNames(readNames(in), false /* keep well known */,
            false /* do not sort */);
        char[][] internedSimpleNames = ReferenceCollection.internSimpleNames(readNames(in), false /* keep well known */,
            false /* do not sort */);
        char[][][] internedQualifiedNames = new char[length = in.readInt()][][];
        for (int i = 0; i < length; i++) {
            int qLength = in.readInt();
            char[][] qName = new char[qLength][];
            for (int j = 0; j < qLength; j++)
                qName[j] = internedSimpleNames[in.readIntInRange(internedSimpleNames.length)];
            internedQualifiedNames[i] = qName;
        }
        internedQualifiedNames = ReferenceCollection.internQualifiedNames(internedQualifiedNames,
            false /* drop well known */, false /* do not sort */);

        length = in.readInt();
        newState.references = new LinkedHashMap((int) (length / 0.75 + 1));
        for (int i = 0; i < length; i++) {
            String typeLocator = internedTypeLocators[in.readInt()];
            ReferenceCollection collection = null;
            switch (in.readByte()) {
                case 1:
                    char[][] additionalTypeNames = readNames(in);
                    char[][][] qualifiedNames = new char[in.readInt()][][];
                    for (int j = 0, m = qualifiedNames.length; j < m; j++)
                        qualifiedNames[j] = internedQualifiedNames[in.readIntInRange(internedQualifiedNames.length)];
                    char[][] simpleNames = new char[in.readInt()][];
                    for (int j = 0, m = simpleNames.length; j < m; j++)
                        simpleNames[j] = internedSimpleNames[in.readIntInRange(internedSimpleNames.length)];
                    char[][] rootNames = new char[in.readInt()][];
                    for (int j = 0, m = rootNames.length; j < m; j++)
                        rootNames[j] = internedRootNames[in.readIntInRange(internedRootNames.length)];
                    collection = new AdditionalTypeCollection(additionalTypeNames, qualifiedNames, simpleNames,
                        rootNames);
                    break;
                case 2:
                    char[][][] qNames = new char[in.readInt()][][];
                    for (int j = 0, m = qNames.length; j < m; j++)
                        qNames[j] = internedQualifiedNames[in.readIntInRange(internedQualifiedNames.length)];
                    char[][] sNames = new char[in.readInt()][];
                    for (int j = 0, m = sNames.length; j < m; j++)
                        sNames[j] = internedSimpleNames[in.readIntInRange(internedSimpleNames.length)];
                    char[][] rNames = new char[in.readInt()][];
                    for (int j = 0, m = rNames.length; j < m; j++)
                        rNames[j] = internedRootNames[in.readIntInRange(internedRootNames.length)];
                    collection = new ReferenceCollection(qNames, sNames, rNames);
            }
            newState.references.put(typeLocator, collection);
        }
        return newState;
    }

    private static ClasspathMultiDirectory[] readSourceLocations(IProject project, CompressedReader in) throws IOException {
        int length = in.readInt();
        ClasspathMultiDirectory[] sourceLocations = new ClasspathMultiDirectory[length];
        for (int i = 0; i < length; i++) {
            IContainer sourceFolder = project, outputFolder = project;
            String folderName;
            if (!(folderName = in.readStringUsingDictionary()).isEmpty())
                sourceFolder = project.getFolder(folderName);
            if (!(folderName = in.readStringUsingDictionary()).isEmpty())
                outputFolder = project.getFolder(folderName);
            char[][] inclusionPatterns = readNames(in);
            char[][] exclusionPatterns = readNames(in);
            boolean ignoreOptionalProblems = in.readBoolean();
            IPath path = readNullablePath(in);
            boolean hasIndependentOutputFolder = in.readBoolean();
            int release = in.readInt();
            ClasspathMultiDirectory md = (ClasspathMultiDirectory) ClasspathLocation.forSourceFolder(sourceFolder,
                outputFolder, inclusionPatterns, exclusionPatterns, ignoreOptionalProblems, path, release);
            if (hasIndependentOutputFolder)
                md.hasIndependentOutputFolder = true;
            sourceLocations[i] = md;
        }
        return sourceLocations;
    }

    private static ClasspathLocation[] readBinaryLocations(IProject project, CompressedReader in,
        ClasspathMultiDirectory[] sourceLocations)
        throws IOException, CoreException {
        int length = in.readInt();
        ClasspathLocation[] locations = new ClasspathLocation[length];
        IWorkspaceRoot root = project.getWorkspace().getRoot();
        for (int i = 0; i < length; i++) {
            byte kind = in.readByte();
            switch (kind) {
                case SOURCE_FOLDER:
                    locations[i] = sourceLocations[in.readInt()];
                    break;
                case BINARY_FOLDER:
                    IPath path = new Path(in.readStringUsingDictionary());
                    IContainer outputFolder = path.segmentCount() == 1
                        ? root.getProject(path.toString())
                        : root.getFolder(path);
                    locations[i] = ClasspathLocation.forBinaryFolder(outputFolder, in.readBoolean(),
                        readRestriction(in), new Path(in.readStringUsingDictionary()), in.readBoolean());
                    break;
                case EXTERNAL_JAR:
                    String jarPath = in.readStringUsingDictionary();
                    if (Util.isJrt(jarPath)) {
                        locations[i] = ClasspathLocation.forJrtSystem(jarPath, readRestriction(in),
                            new Path(in.readStringUsingDictionary()), in.readStringUsingDictionary());
                    } else {
                        locations[i] = ClasspathLocation.forLibrary(jarPath, in.readLong(), readRestriction(in),
                            new Path(in.readStringUsingDictionary()), in.readBoolean(), in.readStringUsingDictionary());
                    }
                    break;
                case INTERNAL_JAR:
                    locations[i] = ClasspathLocation.forLibrary(root.getFile(new Path(in.readStringUsingDictionary())),
                        readRestriction(in), new Path(in.readStringUsingDictionary()), in.readBoolean(),
                        in.readStringUsingDictionary());
                    break;
            }
            ClasspathLocation loc = locations[i];
            char[] patchName = in.readChars();
            loc.patchModuleName = patchName.length > 0 ? new String(patchName) : null;
            int limitSize = in.readInt();
            if (limitSize != 0) {
                loc.limitModuleNames = new LinkedHashSet<>(limitSize);
                for (int j = 0; j < limitSize; j++) {
                    loc.limitModuleNames.add(in.readStringUsingDictionary());
                }
            } else {
                loc.limitModuleNames = null;
            }
            IUpdatableModule.UpdatesByKind updates = new IUpdatableModule.UpdatesByKind();
            List<Consumer<IUpdatableModule>> packageUpdates = null;
            int packageUpdatesSize = in.readInt();
            if (packageUpdatesSize != 0) {
                packageUpdates = updates.getList(UpdateKind.PACKAGE, true);
                for (int j = 0; j < packageUpdatesSize; j++) {
                    char[] pkgName = in.readChars();
                    char[][] targets = readNames(in);
                    packageUpdates.add(new AddExports(pkgName, targets));
                }
            }
            List<Consumer<IUpdatableModule>> moduleUpdates = null;
            int moduleUpdatesSize = in.readInt();
            if (moduleUpdatesSize != 0) {
                moduleUpdates = updates.getList(UpdateKind.MODULE, true);
                char[] modName = in.readChars();
                moduleUpdates.add(new AddReads(modName));
            }
            if (packageUpdates != null || moduleUpdates != null)
                loc.updates = updates;
        }
        return locations;
    }

    private static IPath readNullablePath(CompressedReader in) throws IOException {
        String path = in.readStringUsingDictionary();
        if (!path.isEmpty())
            return new Path(path);
        return null;
    }

    private static AccessRuleSet readRestriction(CompressedReader in) throws IOException {
        int length = in.readInt();
        if (length == 0)
            return null; // no restriction specified
        AccessRule[] accessRules = new AccessRule[length];
        JavaModelManager manager = JavaModelManager.getJavaModelManager();
        for (int i = 0; i < length; i++) {
            char[] pattern = in.readCharsUsingLast();
            int problemId = in.readIntWithHint(PROBLEM_IDS);
            accessRules[i] = manager.getAccessRuleForProblemId(pattern, problemId);
        }
        return new AccessRuleSet(accessRules, in.readByte(), DeduplicationUtil.intern(in.readStringUsingDictionary()));
    }

    private static char[][] readNames(CompressedReader in) throws IOException {
        int length = in.readInt();
        char[][] names = new char[length][];
        for (int i = 0; i < length; i++)
            names[i] = in.readChars();
        return names;
    }

    /**
     * Returns a string representation of the receiver.
     */
    @Override
    public String toString() {
        return "State for " + this.javaProjectName //$NON-NLS-1$
            + " (#" + this.buildNumber //$NON-NLS-1$
            + " @ " + new Date(this.lastStructuralBuildTime) //$NON-NLS-1$
            + ")"; //$NON-NLS-1$
    }

}
