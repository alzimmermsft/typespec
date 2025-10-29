/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Path;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.IAccessRule;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.compiler.IProblem;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.compiler.env.AccessRule;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.util.DeduplicationUtil;

public class ClasspathAccessRule extends AccessRule implements IAccessRule {

    private IPath path;

    public ClasspathAccessRule(IPath pattern, int kind) {
        this(pattern.toString().toCharArray(), toProblemId(kind));
        // avoid IPath creation (Bug 571159):
        this.path = pattern;
    }

    public ClasspathAccessRule(char[] pattern, int problemId) {
        super(DeduplicationUtil.intern(pattern), problemId);
    }

    private static int toProblemId(int kind) {
        boolean ignoreIfBetter = (kind & IAccessRule.IGNORE_IF_BETTER) != 0;
        switch (kind & ~IAccessRule.IGNORE_IF_BETTER) {
            case K_NON_ACCESSIBLE:
                return ignoreIfBetter
                    ? IProblem.ForbiddenReference | AccessRule.IgnoreIfBetter
                    : IProblem.ForbiddenReference;

            case K_DISCOURAGED:
                return ignoreIfBetter
                    ? IProblem.DiscouragedReference | AccessRule.IgnoreIfBetter
                    : IProblem.DiscouragedReference;

            default:
                return ignoreIfBetter ? AccessRule.IgnoreIfBetter : 0;
        }
    }

    @Override
    public IPath getPattern() {
        if (this.path == null) {
            // cache the IPath (Bug 571159):
            this.path = new Path(new String(this.pattern));
        }
        return this.path;
    }

    @Override
    public int getKind() {
        switch (getProblemId()) {
            case IProblem.ForbiddenReference:
                return K_NON_ACCESSIBLE;

            case IProblem.DiscouragedReference:
                return K_DISCOURAGED;

            default:
                return K_ACCESSIBLE;
        }
    }

}
