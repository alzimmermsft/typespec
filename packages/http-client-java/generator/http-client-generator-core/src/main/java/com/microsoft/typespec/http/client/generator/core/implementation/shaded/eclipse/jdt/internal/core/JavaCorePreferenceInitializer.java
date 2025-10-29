/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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
 *     Harry Terkelsen (het@google.com) - Bug 449262 - Allow the use of third-party Java formatters
 *     Gábor Kövesdán - Contribution for Bug 350000 - [content assist] Include non-prefix matches in auto-complete suggestions
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;

import java.util.Map;

/**
 * JavaCore eclipse preferences initializer.
 * Initially done in JavaCore.initializeDefaultPreferences which was deprecated
 * with new eclipse preferences mechanism.
 */
public class JavaCorePreferenceInitializer {

    /**
     * Note: For deprecated formatter options, you may also add migration to their replacement options in
     * {@link com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions#setDeprecatedOptions}.
     *
     * @deprecated As using deprecated options
     */
    private void initializeDeprecatedOptions() {
        Map<String, String[]> deprecatedOptions = JavaModelManager.getJavaModelManager().deprecatedOptions;
        deprecatedOptions.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_MEMBER,
            new String[] {
                DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_FIELD,
                DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_METHOD,
                DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_PACKAGE,
                DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_TYPE });
        deprecatedOptions.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION,
            new String[] {
                DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_FIELD,
                DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_METHOD,
                DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_PACKAGE,
                DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_TYPE,
                DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_LOCAL_VARIABLE,
                DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_PARAMETER });

        deprecatedOptions.put(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_BINARY_EXPRESSION,
            new String[] {
                DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_MULTIPLICATIVE_OPERATOR,
                DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ADDITIVE_OPERATOR,
                DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_STRING_CONCATENATION,
                DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_BITWISE_OPERATOR,
                DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_LOGICAL_OPERATOR, });
        deprecatedOptions.put(DefaultCodeFormatterConstants.FORMATTER_WRAP_BEFORE_BINARY_OPERATOR,
            new String[] {
                DefaultCodeFormatterConstants.FORMATTER_WRAP_BEFORE_MULTIPLICATIVE_OPERATOR,
                DefaultCodeFormatterConstants.FORMATTER_WRAP_BEFORE_ADDITIVE_OPERATOR,
                DefaultCodeFormatterConstants.FORMATTER_WRAP_BEFORE_STRING_CONCATENATION,
                DefaultCodeFormatterConstants.FORMATTER_WRAP_BEFORE_BITWISE_OPERATOR,
                DefaultCodeFormatterConstants.FORMATTER_WRAP_BEFORE_LOGICAL_OPERATOR, });
        deprecatedOptions.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_BINARY_OPERATOR,
            new String[] {
                DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_MULTIPLICATIVE_OPERATOR,
                DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_ADDITIVE_OPERATOR,
                DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_STRING_CONCATENATION,
                DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_SHIFT_OPERATOR,
                DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_RELATIONAL_OPERATOR,
                DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_BITWISE_OPERATOR,
                DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_LOGICAL_OPERATOR, });
        deprecatedOptions.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_BINARY_OPERATOR,
            new String[] {
                DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_MULTIPLICATIVE_OPERATOR,
                DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_ADDITIVE_OPERATOR,
                DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_STRING_CONCATENATION,
                DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_SHIFT_OPERATOR,
                DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_RELATIONAL_OPERATOR,
                DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_BITWISE_OPERATOR,
                DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_LOGICAL_OPERATOR, });
    }
}
