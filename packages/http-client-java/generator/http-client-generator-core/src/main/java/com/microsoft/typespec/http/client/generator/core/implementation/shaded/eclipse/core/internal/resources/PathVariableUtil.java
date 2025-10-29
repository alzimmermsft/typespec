/*******************************************************************************
 * Copyright (c) 2008, 2016 Freescale Semiconductor and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Serge Beauchamp (Freescale Semiconductor) - initial API and implementation
 *     James Blackburn (Broadcom Corp.) - ongoing development
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 473427
 *     Mickael Istria (Red Hat Inc.) - Bug 488937
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources;

public class PathVariableUtil {

    // getMatchingBrace("${FOO}/something") returns 5
    // getMatchingBrace("${${OTHER}}/something") returns 10
    // getMatchingBrace("${FOO") returns 5
    static int getMatchingBrace(String value, int index) {
        int scope = 0;
        for (int i = index + 1; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '}') {
                if (scope == 0) {
                    return i;
                }
                scope--;
            }
            if (c == '$') {
                if ((i + 1 < value.length()) && (value.charAt(i + 1) == '{')) {
                    scope++;
                }
            }
        }
        return value.length();
    }

}
