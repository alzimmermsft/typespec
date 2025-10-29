/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     James Blackburn (Broadcom Corp.) - ongoing development
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 473427
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import java.util.*;

/**
 * The notification mechanism can request marker deltas for several overlapping intervals
 * of time. This class maintains a history of marker deltas, and upon request can
 * generate a map of marker deltas for any interval. This is done by maintaining
 * batches of marker deltas keyed by the change Id at the start of that batch.
 * When the delta factory requests a delta, it specifies the start generation, and
 * this class assembles the deltas for all generations between then and the most
 * recent delta.
 */
class MarkerDeltaManager {
    private static final int DEFAULT_SIZE = 10;
    private long[] startIds = new long[DEFAULT_SIZE];
    @SuppressWarnings("unchecked")
    private Map<IPath, MarkerSet>[] batches = new Map[DEFAULT_SIZE];
    private int nextFree = 0;

    /**
     * Returns the deltas from the given start id up until the present. Returns null
     * if there are no deltas for that interval.
     */
    protected Map<IPath, MarkerSet> assembleDeltas(long start) {
        Map<IPath, MarkerSet> result = null;
        for (int i = 0; i < nextFree; i++) {
            if (startIds[i] >= start) {
                result = MarkerDelta.merge(result, batches[i]);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    protected Map<IPath, MarkerSet> newGeneration(long start) {
        int len = startIds.length;
        if (nextFree >= len) {
            long[] newIds = new long[len * 2];
            Map<IPath, MarkerSet>[] newBatches = new Map[len * 2];
            System.arraycopy(startIds, 0, newIds, 0, len);
            System.arraycopy(batches, 0, newBatches, 0, len);
            startIds = newIds;
            batches = newBatches;
        }
        startIds[nextFree] = start;
        batches[nextFree] = new HashMap<>(11);
        return batches[nextFree++];
    }
}
