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
 *******************************************************************************/

package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.commands.util;

/**
 * A class providing utility functions for the commands plug-in.
 *
 * @since 3.1
 */
public final class Util {

    /**
     * A shared, zero-length string -- for avoiding non-externalized string
     * tags. This value is guaranteed to always be the same.
     */
    public static final String ZERO_LENGTH_STRING = ""; //$NON-NLS-1$

    /**
     * Compares two boolean values. <code>false</code> is considered to be
     * less than <code>true</code>.
     *
     * @param left
     * The left value to compare.
     * @param right
     * The right value to compare.
     * @return <code>-1</code> if <code>left</code> is <code>false</code>
     * and <code>right</code> is <code>true</code>;<code>0</code>
     * if they are equal; <code>1</code> if <code>left</code> is
     * <code>true</code> and <code>right</code> is
     * <code>false</code>
     */
    public static int compare(final boolean left, final boolean right) {
        return !left ? (right ? -1 : 0) : (right ? 0 : 1);
    }

    /**
     * Compares two comparable objects, but with protection against
     * <code>null</code>.
     *
     * @param left
     * The left value to compare; may be <code>null</code>.
     * @param right
     * The right value to compare; may be <code>null</code>.
     * @return <code>-1</code> if <code>left</code> is <code>null</code>
     * and <code>right</code> is not <code>null</code>;
     * <code>0</code> if they are both <code>null</code>;
     * <code>1</code> if <code>left</code> is not <code>null</code>
     * and <code>right</code> is <code>null</code>. Otherwise, the
     * result of <code>left.compareTo(right)</code>.
     */
    public static <T extends Comparable<? super T>> int compare(final T left, final T right) {
        if (left == null && right == null) {
            return 0;
        } else if (left == null) {
            return -1;
        } else if (right == null) {
            return 1;
        } else {
            return left.compareTo(right);
        }
    }

    /**
     * Compares two integer values. This method fails if the distance between
     * <code>left</code> and <code>right</code> is greater than
     * <code>Integer.MAX_VALUE</code>.
     *
     * @param left
     * The left value to compare.
     * @param right
     * The right value to compare.
     * @return <code>left - right</code>
     */
    public static int compare(final int left, final int right) {
        return left - right;
    }

    /**
     * Compares two objects that are not otherwise comparable. If neither object
     * is <code>null</code>, then the string representation of each object is
     * used.
     *
     * @param left
     * The left value to compare. The string representation of this
     * value must not be <code>null</code>.
     * @param right
     * The right value to compare. The string representation of this
     * value must not be <code>null</code>.
     * @return <code>-1</code> if <code>left</code> is <code>null</code>
     * and <code>right</code> is not <code>null</code>;
     * <code>0</code> if they are both <code>null</code>;
     * <code>1</code> if <code>left</code> is not <code>null</code>
     * and <code>right</code> is <code>null</code>. Otherwise, the
     * result of
     * <code>left.toString().compareTo(right.toString())</code>.
     */
    public static int compare(final Object left, final Object right) {
        if (left == null && right == null) {
            return 0;
        } else if (left == null) {
            return -1;
        } else if (right == null) {
            return 1;
        } else {
            return left.toString().compareTo(right.toString());
        }
    }

    /**
     * The utility class is meant to just provide static members.
     */
    private Util() {
        // Should not be called.
    }
}
