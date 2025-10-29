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
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.dom.rewrite;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.formatter.IndentManipulation;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.text.edits.ISourceModifier;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.text.edits.ReplaceEdit;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class SourceModifier implements ISourceModifier {

    private final String destinationIndent;
    private final int sourceIndentLevel;
    private final int tabWidth;
    private final int indentWidth;

    public SourceModifier(int sourceIndentLevel, String destinationIndent, int tabWidth, int indentWidth) {
        this.destinationIndent = destinationIndent;
        this.sourceIndentLevel = sourceIndentLevel;
        this.tabWidth = tabWidth;
        this.indentWidth = indentWidth;
    }

    @Override
    public ISourceModifier copy() {
        // We are state less
        return this;
    }

    @Override
    public ReplaceEdit[] getModifications(String source) {
        List result = new ArrayList();
        int destIndentLevel
            = IndentManipulation.measureIndentUnits(this.destinationIndent, this.tabWidth, this.indentWidth);
        if (destIndentLevel == this.sourceIndentLevel) {
            return (ReplaceEdit[]) result.toArray(new ReplaceEdit[result.size()]);
        }
        return IndentManipulation.getChangeIndentEdits(source, this.sourceIndentLevel, this.tabWidth, this.indentWidth,
            this.destinationIndent);
    }
}
