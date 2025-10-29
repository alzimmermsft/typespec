/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
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
 *     Sergey Prigogin (Google) - Bug 421375
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.expressions;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IConfigurationElement;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class Expressions {

    private Expressions() {
        // no instance
    }

    public static void checkAttribute(String name, String value) throws CoreException {
        if (value == null) {
            throw new CoreException(new ExpressionStatus(ExpressionStatus.MISSING_ATTRIBUTE,
                Messages.format(ExpressionMessages.Expression_attribute_missing, name)));
        }
    }

    // ---- Argument parsing --------------------------------------------

    public static final Object[] EMPTY_ARGS = new Object[0];

    public static Object[] getArguments(IConfigurationElement element, String attributeName) throws CoreException {
        String args = element.getAttribute(attributeName);
        if (args != null) {
            return parseArguments(args);
        } else {
            return EMPTY_ARGS;
        }
    }

    public static Object[] getArguments(Element element, String attributeName) throws CoreException {
        String args = element.getAttribute(attributeName);
        if (!args.isEmpty()) {
            return parseArguments(args);
        } else {
            return EMPTY_ARGS;
        }
    }

    public static Object[] parseArguments(String args) throws CoreException {
        List<Object> result = new ArrayList<>();
        int start = 0;
        int comma;
        while ((comma = findNextComma(args, start)) != -1) {
            result.add(convertArgument(args.substring(start, comma).trim()));
            start = comma + 1;
        }
        result.add(convertArgument(args.substring(start).trim()));
        return result.toArray();
    }

    private static int findNextComma(String str, int start) throws CoreException {
        boolean inString = false;
        for (int i = start; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch == ',' && !inString) {
                return i;
            }
            if (ch == '\'') {
                if (!inString) {
                    inString = true;
                } else if (i + 1 < str.length() && str.charAt(i + 1) == '\'') {
                    i++;
                } else {
                    inString = false;
                }
            } else if (ch == ',' && !inString) {
                return i;
            }
        }
        if (inString) {
            throw new CoreException(new ExpressionStatus(ExpressionStatus.STRING_NOT_TERMINATED,
                Messages.format(ExpressionMessages.Expression_string_not_terminated, str)));
        }

        return -1;
    }

    public static Object convertArgument(String arg) throws CoreException {
        if (arg == null) {
            return null;
        } else if (arg.isEmpty()) {
            return arg;
        } else if (arg.charAt(0) == '\'' && arg.charAt(arg.length() - 1) == '\'') {
            return unEscapeString(arg.substring(1, arg.length() - 1));
        } else if ("true".equals(arg)) { //$NON-NLS-1$
            return Boolean.TRUE;
        } else if ("false".equals(arg)) { //$NON-NLS-1$
            return Boolean.FALSE;
        } else if (arg.indexOf('.') != -1) {
            try {
                return Float.valueOf(arg);
            } catch (NumberFormatException e) {
                return arg;
            }
        } else {
            try {
                return Integer.valueOf(arg);
            } catch (NumberFormatException e) {
                return arg;
            }
        }
    }

    public static String unEscapeString(String str) throws CoreException {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch == '\'') {
                if (i == str.length() - 1 || str.charAt(i + 1) != '\'') {
                    throw new CoreException(new ExpressionStatus(ExpressionStatus.STRING_NOT_CORRECT_ESCAPED,
                        Messages.format(ExpressionMessages.Expression_string_not_correctly_escaped, str)));
                }
                result.append('\'');
                i++;
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }
}
