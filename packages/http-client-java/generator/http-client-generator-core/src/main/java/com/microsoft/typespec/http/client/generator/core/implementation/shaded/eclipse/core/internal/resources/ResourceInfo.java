/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *     Oakland Software Incorporated - added getSessionProperties and getPersistentProperties
 *     James Blackburn (Broadcom Corp.) - ongoing development
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 473427
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.localstore.FileStoreRoot;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.utils.IStringPoolParticipant;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.utils.ObjectMap;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.utils.StringPool;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.watson.IElementTreeData;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResource;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.QualifiedName;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeMap;

/**
 * A data structure containing the in-memory state of a resource in the workspace.
 */
public class ResourceInfo implements IElementTreeData, ICoreConstants, IStringPoolParticipant {
    protected static final int LOWER = 0xFFFF;
    protected static final int UPPER = 0xFFFF0000;

    /**
     * This field stores the resource modification stamp in the lower two bytes,
     * and the character set generation count in the higher two bytes.
     */
    protected volatile int charsetAndContentId;

    /**
     * The file system root that this resource is stored in
     */
    protected FileStoreRoot fileStoreRoot;

    /** Set of flags which reflect various states of the info (used, derived, ...). */
    protected int flags;

    /** Local sync info */
    // thread safety: (Concurrency004)
    protected volatile long localInfo = I_NULL_SYNC_INFO;

    /**
     * This field stores the sync info generation in the lower two bytes, and
     * the marker generation count in the upper two bytes.
     */
    protected volatile int markerAndSyncStamp;

    /** The collection of markers for this resource. */
    protected MarkerSet markers;

    /** Modification stamp */
    protected long modStamp;

    /** Unique node identifier */
    // thread safety: (Concurrency004)
    protected volatile long nodeId;

    /**
     * The properties which are maintained for the lifecycle of the workspace.
     * <p>
     * This field is declared as the implementing class rather than the
     * interface so we ensure that we get it right since we are making certain
     * assumptions about the object type w.r.t. casting.
     */
    protected ObjectMap<QualifiedName, Object> sessionProperties;

    /**
     * The table of sync information.
     * <p>
     * This field is declared as the implementing class rather than the
     * interface so we ensure that we get it right since we are making certain
     * assumptions about the object type w.r.t. casting.
     */
    protected ObjectMap<QualifiedName, Object> syncInfo;

    /**
     * Default constructor (for easier debugging)
     */
    public ResourceInfo() {
        super();
    }

    /**
     * Returns the integer value stored in the indicated part of this info's flags.
     */
    protected static int getBits(int flags, int mask, int start) {
        return (flags & mask) >> start;
    }

    /**
     * Returns the type setting for this info. Valid values are
     * FILE, FOLDER, PROJECT,
     */
    public static int getType(int flags) {
        return getBits(flags, M_TYPE, M_TYPE_START);
    }

    /**
     * Returns true if all of the bits indicated by the mask are set.
     */
    public static boolean isSet(int flags, int mask) {
        return (flags & mask) == mask;
    }

    /**
     * Clears all of the bits indicated by the mask.
     */
    public void clear(int mask) {
        flags &= ~mask;
    }

    public void clearModificationStamp() {
        modStamp = IResource.NULL_STAMP;
    }

    public synchronized void clearSessionProperties() {
        sessionProperties = null;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null; // never gets here.
        }
    }

    public int getCharsetGenerationCount() {
        return charsetAndContentId >> 16;
    }

    public int getContentId() {
        return charsetAndContentId & LOWER;
    }

    public FileStoreRoot getFileStoreRoot() {
        return fileStoreRoot;
    }

    /**
     * Returns the set of flags for this info.
     */
    public int getFlags() {
        return flags;
    }

    /**
     * Gets the local-relative sync information.
     */
    public long getLocalSyncInfo() {
        return localInfo;
    }

    /**
     * Returns the marker generation count.
     * The count is incremented whenever markers on the resource change.
     */
    public int getMarkerGenerationCount() {
        return markerAndSyncStamp >> 16;
    }

    /**
     * Returns a copy of the collection of makers on this resource.
     * <code>null</code> is returned if there are none.
     */
    public MarkerSet getMarkers() {
        return getMarkers(true);
    }

    /**
     * Returns the collection of makers on this resource.
     * <code>null</code> is returned if there are none.
     */
    public MarkerSet getMarkers(boolean makeCopy) {
        if (markers == null) {
            return null;
        }
        return makeCopy ? (MarkerSet) markers.clone() : markers;
    }

    public long getModificationStamp() {
        return modStamp;
    }

    public long getNodeId() {
        return nodeId;
    }

    /**
     * Returns the value of the identified session property
     */
    public Object getSessionProperty(QualifiedName name) {
        // thread safety: (Concurrency001)
        Map<QualifiedName, Object> temp = sessionProperties;
        if (temp == null) {
            return null;
        }
        return temp.get(name);
    }

    /**
     * The parameter to this method is the implementing class rather than the
     * interface so we ensure that we get it right since we are making certain
     * assumptions about the object type w.r.t. casting.
     */
    @SuppressWarnings({ "unchecked" })
    public synchronized ObjectMap<QualifiedName, Object> getSyncInfo(boolean makeCopy) {
        if (syncInfo == null) {
            return null;
        }
        return makeCopy ? (ObjectMap<QualifiedName, Object>) syncInfo.clone() : syncInfo;
    }

    public synchronized byte[] getSyncInfo(QualifiedName id, boolean makeCopy) {
        // thread safety: (Concurrency001)
        byte[] b;
        if (syncInfo == null) {
            return null;
        }
        b = (byte[]) syncInfo.get(id);
        return b == null ? null : (makeCopy ? (byte[]) b.clone() : b);
    }

    /**
     * Returns the sync information generation count.
     * The count is incremented whenever sync info on the resource changes.
     */
    public int getSyncInfoGenerationCount() {
        return markerAndSyncStamp & LOWER;
    }

    /**
     * Returns the type setting for this info. Valid values are
     * FILE, FOLDER, PROJECT,
     */
    public int getType() {
        return getType(flags);
    }

    /**
     * Increments the marker generation count.
     * The count is incremented whenever markers on the resource change.
     */
    public void incrementMarkerGenerationCount() {
        // increment high order bits
        markerAndSyncStamp = ((markerAndSyncStamp + LOWER + 1) & UPPER) + (markerAndSyncStamp & LOWER);
    }

    /**
     * Change the modification stamp to indicate that this resource has changed.
     * The exact value of the stamp doesn't matter, as long as it can be used to
     * distinguish two arbitrary resource generations.
     */
    public void incrementModificationStamp() {
        modStamp++;
    }

    /**
     * Returns true if all of the bits indicated by the mask are set.
     */
    public boolean isSet(int mask) {
        return (flags & mask) == mask;
    }

    /**
     * Sets all of the bits indicated by the mask.
     */
    public void set(int mask) {
        flags |= mask;
    }

    /**
     * Sets the value of the indicated bits to be the given value.
     */
    protected void setBits(int mask, int start, int value) {
        int baseMask = mask >> start;
        int newValue = (value & baseMask) << start;
        // thread safety: (guarantee atomic assignment)
        int temp = flags;
        temp &= ~mask;
        temp |= newValue;
        flags = temp;
    }

    public void setFileStoreRoot(FileStoreRoot fileStoreRoot) {
        this.fileStoreRoot = fileStoreRoot;
    }

    /**
     * Sets the flags for this info.
     */
    protected void setFlags(int value) {
        flags = value;
    }

    /**
     * Sets the local-relative sync information.
     */
    public void setLocalSyncInfo(long info) {
        localInfo = info;
    }

    /**
     * Sets the collection of makers for this resource.
     * <code>null</code> is passed in if there are no markers.
     */
    public void setMarkers(MarkerSet value) {
        markers = value;
    }

    public void setNodeId(long id) {
        nodeId = id;
        // Resource modification stamp starts from current nodeId
        // so future generations are distinguishable (bug 160728)
        if (modStamp == 0) {
            modStamp = nodeId;
        }
    }

    /**
     * Sets the identified session property to the given value. If
     * the value is null, the property is removed.
     */
    @SuppressWarnings({ "unchecked" })
    public synchronized void setSessionProperty(QualifiedName name, Object value) {
        // thread safety: (Concurrency001)
        if (value == null) {
            if (sessionProperties == null) {
                return;
            }
            ObjectMap<QualifiedName, Object> temp = (ObjectMap<QualifiedName, Object>) sessionProperties.clone();
            temp.remove(name);
            if (temp.isEmpty()) {
                sessionProperties = null;
            } else {
                sessionProperties = temp;
            }
        } else {
            ObjectMap<QualifiedName, Object> temp = sessionProperties;
            if (temp == null) {
                temp = new ObjectMap<>(5);
            } else {
                temp = (ObjectMap<QualifiedName, Object>) sessionProperties.clone();
            }
            temp.put(name, value);
            sessionProperties = temp;
        }
    }

    /**
     * The parameter to this method is the implementing class rather than the
     * interface so we ensure that we get it right since we are making certain
     * assumptions about the object type w.r.t. casting.
     */
    protected void setSyncInfo(ObjectMap<QualifiedName, Object> syncInfo) {
        this.syncInfo = syncInfo;
    }

    /**
     * Sets the type for this info to the given value. Valid values are
     * FILE, FOLDER, PROJECT
     */
    public void setType(int value) {
        setBits(M_TYPE, M_TYPE_START, value);
    }

    /*
     * (non-Javadoc
     * Method declared on IStringPoolParticipant
     */
    @Override
    public void shareStrings(StringPool set) {
        ObjectMap<QualifiedName, Object> map = syncInfo;
        if (map != null) {
            map.shareStrings(set);
        }
        map = sessionProperties;
        if (map != null) {
            map.shareStrings(set);
        }
        MarkerSet markerSet = markers;
        if (markerSet != null) {
            markerSet.shareStrings(set);
        }
    }

    /** for debugging only **/
    @Override
    public String toString() {
        Map<String, Integer> flagsMap = new TreeMap<>();
        Field[] fields = ICoreConstants.class.getFields();
        for (Field field : fields) {
            String name = field.getName();
            if (name.startsWith("M_")) { //$NON-NLS-1$
                try {
                    flagsMap.put(name, field.getInt(null));
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    // don't care
                }
            }
        }
        StringBuilder sb = new StringBuilder(fileStoreRoot + " modStamp=" + modStamp); //$NON-NLS-1$
        sb.append(", exists="); //$NON-NLS-1$
        sb.append(flags != NULL_FLAG);
        sb.append(", syncInfo="); //$NON-NLS-1$
        sb.append(localInfo);
        sb.append(", flags ["); //$NON-NLS-1$
        for (Map.Entry<String, Integer> entry : flagsMap.entrySet()) {
            String flag = entry.getKey();
            Integer val = entry.getValue();
            if (isSet(flags, val)) {
                sb.append(flag).append(',');
            }
        }
        sb.append("]"); //$NON-NLS-1$
        return sb.toString();
    }
}
