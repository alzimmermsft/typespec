/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Platform;
import java.util.Arrays;

/**
 * Captures platform specific attributes relevant to the core resources plugin. This
 * class is not intended to be instantiated.
 */
public abstract class OS {
    private static final String INSTALLED_PLATFORM;

    public static final char[] INVALID_RESOURCE_CHARACTERS;
    private static final String[] INVALID_RESOURCE_BASENAMES;
    private static final String[] INVALID_RESOURCE_FULLNAMES;

    static {
        // find out the OS being used
        // setup the invalid names
        INSTALLED_PLATFORM = Platform.getOS();
        if (INSTALLED_PLATFORM.equals(Platform.OS_WIN32)) {
            // valid names and characters taken from
            // http://msdn.microsoft.com/library/default.asp?url=/library/en-us/fileio/fs/naming_a_file.asp
            INVALID_RESOURCE_CHARACTERS = new char[] {
                '\\',
                '/',
                ':',
                '*',
                '?',
                '"',
                '<',
                '>',
                '|',
                // Characters 0..31 are forbidden in windows
                // See https://docs.microsoft.com/en-us/windows/win32/fileio/naming-a-file
                '\0',
                '\u0001',
                '\u0002',
                '\u0003',
                '\u0004',
                '\u0005',
                '\u0006',
                '\u0007',
                '\u0008',
                '\u0009',
                '\n',
                '\u000b',
                '\u000c',
                '\r',
                '\u000e',
                '\u000f',
                '\u0010',
                '\u0011',
                '\u0012',
                '\u0013',
                '\u0014',
                '\u0015',
                '\u0016',
                '\u0017',
                '\u0018',
                '\u0019',
                '\u001a',
                '\u001b',
                '\u001c',
                '\u001d',
                '\u001e',
                '\u001f' };
            INVALID_RESOURCE_BASENAMES = new String[] {
                "aux", //$NON-NLS-1$
                "com1", //$NON-NLS-1$
                "com2", //$NON-NLS-1$
                "com3", //$NON-NLS-1$
                "com4",  //$NON-NLS-1$
                "com5", //$NON-NLS-1$
                "com6", //$NON-NLS-1$
                "com7", //$NON-NLS-1$
                "com8", //$NON-NLS-1$
                "com9", //$NON-NLS-1$
                "con", //$NON-NLS-1$
                "lpt1", //$NON-NLS-1$
                "lpt2",  //$NON-NLS-1$
                "lpt3", //$NON-NLS-1$
                "lpt4", //$NON-NLS-1$
                "lpt5", //$NON-NLS-1$
                "lpt6", //$NON-NLS-1$
                "lpt7", //$NON-NLS-1$
                "lpt8", //$NON-NLS-1$
                "lpt9", //$NON-NLS-1$
                "nul", //$NON-NLS-1$
                "prn" };  //$NON-NLS-1$
            Arrays.sort(INVALID_RESOURCE_BASENAMES);
            // CLOCK$ may be used if an extension is provided
            INVALID_RESOURCE_FULLNAMES = new String[] { "clock$" }; //$NON-NLS-1$
        } else {
            // only front slash and null char are invalid on UNIXes
            // taken from http://www.faqs.org/faqs/unix-faq/faq/part2/section-2.html
            INVALID_RESOURCE_CHARACTERS = new char[] { '/', '\0', };
            INVALID_RESOURCE_BASENAMES = null;
            INVALID_RESOURCE_FULLNAMES = null;
        }
    }

    /**
     * Returns true if the given name is a valid resource name on this operating system,
     * and false otherwise.
     */
    public static boolean isNameValid(String name) {
        // . and .. have special meaning on all platforms
        if (name.equals(".") || name.equals("..")) { //$NON-NLS-1$ //$NON-NLS-2$
            return false;
        }
        if (INSTALLED_PLATFORM.equals(Platform.OS_WIN32)) {
            // empty names are not valid
            final int length = name.length();
            if (length == 0) {
                return false;
            }
            final char lastChar = name.charAt(length - 1);
            // filenames ending in dot are not valid
            if (lastChar == '.') {
                return false;
            }
            // file names ending with whitespace are truncated (bug 118997)
            if (Character.isWhitespace(lastChar)) {
                return false;
            }
            int dot = name.indexOf('.');
            // on windows, filename suffixes are not relevant to name validity
            String basename = dot == -1 ? name : name.substring(0, dot);
            if (Arrays.binarySearch(INVALID_RESOURCE_BASENAMES, basename.toLowerCase()) >= 0) {
                return false;
            }
            return Arrays.binarySearch(INVALID_RESOURCE_FULLNAMES, name.toLowerCase()) < 0;
        }
        return true;
    }
}
