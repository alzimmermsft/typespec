/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IProject;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Path;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IClasspathEntry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IElementChangedListener;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.JavaCore;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.DeltaProcessor.RootInfo;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.Util;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Keep the global states used during Java element delta processing.
 */
public class DeltaProcessingState {

    /*
     * Collection of listeners for Java element deltas
     */
    public IElementChangedListener[] elementChangedListeners = new IElementChangedListener[5];
    public int[] elementChangedListenerMasks = new int[5];
    public int elementChangedListenerCount = 0;

    /* A table from IPath (from a classpath entry) to DeltaProcessor.RootInfo */
    public Map<IPath, RootInfo> roots = new LinkedHashMap<>();

    /*
     * A table from IPath (from a classpath entry) to ArrayList of DeltaProcessor.RootInfo
     * Used when an IPath corresponds to more than one root
     */
    public Map<IPath, List<RootInfo>> otherRoots = new HashMap<>();

    /* A table from file system absoulte path (String) to timestamp (Long) */
    public Hashtable<IPath, Long> externalTimeStamps;

    /*
     * Map from IProject to ClasspathChange
     * Note these changes need to be kept on the delta processing state to ensure we don't loose them
     * (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=271102 Java model corrupt after switching target platform)
     */
    private Map<IProject, ClasspathChange> classpathChanges = new LinkedHashMap<>();

    /* A table from JavaProject to ClasspathValidation */
    private final Map<JavaProject, ClasspathValidation> classpathValidations = new LinkedHashMap<>();

    private final Object mutex = new Object();

    /*
     * Need to clone defensively the listener information, in case some listener is reacting to some notification
     * iteration by adding/changing/removing
     * any of the other (for example, if it deregisters itself).
     */
    public synchronized void addElementChangedListener(IElementChangedListener listener, int eventMask) {
        for (int i = 0; i < this.elementChangedListenerCount; i++) {
            if (this.elementChangedListeners[i] == listener) {

                // only clone the masks, since we could be in the middle of notifications and one listener decide to
                // change
                // any event mask of another listeners (yet not notified).
                int cloneLength = this.elementChangedListenerMasks.length;
                System.arraycopy(this.elementChangedListenerMasks, 0,
                    this.elementChangedListenerMasks = new int[cloneLength], 0, cloneLength);
                this.elementChangedListenerMasks[i] |= eventMask; // could be different
                return;
            }
        }
        // may need to grow, no need to clone, since iterators will have cached original arrays and max boundary and we
        // only add to the end.
        int length;
        if ((length = this.elementChangedListeners.length) == this.elementChangedListenerCount) {
            System.arraycopy(this.elementChangedListeners, 0,
                this.elementChangedListeners = new IElementChangedListener[length * 2], 0, length);
            System.arraycopy(this.elementChangedListenerMasks, 0,
                this.elementChangedListenerMasks = new int[length * 2], 0, length);
        }
        this.elementChangedListeners[this.elementChangedListenerCount] = listener;
        this.elementChangedListenerMasks[this.elementChangedListenerCount] = eventMask;
        this.elementChangedListenerCount++;
    }

    public ClasspathChange addClasspathChange(IProject project, IClasspathEntry[] oldRawClasspath,
        IPath oldOutputLocation, IClasspathEntry[] oldResolvedClasspath) {
        synchronized (this.mutex) {
            ClasspathChange change = this.classpathChanges.get(project);
            if (change == null) {
                change = new ClasspathChange(
                    (JavaProject) JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject(project),
                    oldRawClasspath, oldOutputLocation, oldResolvedClasspath);
                this.classpathChanges.put(project, change);
            } else {
                if (change.oldRawClasspath == null)
                    change.oldRawClasspath = oldRawClasspath;
                if (change.oldOutputLocation == null)
                    change.oldOutputLocation = oldOutputLocation;
                if (change.oldResolvedClasspath == null)
                    change.oldResolvedClasspath = oldResolvedClasspath;
            }
            return change;
        }
    }

    public synchronized ClasspathValidation addClasspathValidation(JavaProject project) {
        return this.classpathValidations.computeIfAbsent(project, ClasspathValidation::new);
    }

    public Hashtable<IPath, Long> getExternalLibTimeStamps() {
        if (this.externalTimeStamps == null) {
            Hashtable<IPath, Long> timeStamps = new Hashtable<>();
            File timestampsFile = getTimeStampsFile();
            try (DataInputStream in
                = new DataInputStream(new BufferedInputStream(new FileInputStream(timestampsFile)))) {
                int size = in.readInt();
                while (size-- > 0) {
                    String key = in.readUTF();
                    long timestamp = in.readLong();
                    timeStamps.put(Path.fromPortableString(key), Long.valueOf(timestamp));
                }
            } catch (IOException e) {
                if (timestampsFile.exists())
                    Util.log(e, "Unable to read external time stamps"); //$NON-NLS-1$
            }
            this.externalTimeStamps = timeStamps;
        }
        return this.externalTimeStamps;
    }

    private File getTimeStampsFile() {
        return JavaCore.getPlugin().getStateLocation().append("externalLibsTimeStamps").toFile(); //$NON-NLS-1$
    }

}
