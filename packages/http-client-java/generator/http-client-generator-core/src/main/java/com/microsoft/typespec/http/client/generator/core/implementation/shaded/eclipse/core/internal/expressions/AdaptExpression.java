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

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.expressions.CompositeExpression;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.expressions.EvaluationResult;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.expressions.Expression;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.expressions.ExpressionInfo;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.expressions.IEvaluationContext;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IAdaptable;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IAdapterManager;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IConfigurationElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Platform;
import java.util.Arrays;
import org.w3c.dom.Element;

public class AdaptExpression extends CompositeExpression {

    private static final String PREFERENCE_NAMESPACE
        = "com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.expressions"; //$NON-NLS-1$

    private static final String ATT_TYPE = "type"; //$NON-NLS-1$

    /**
     * The seed for the hash code for all adapt expressions.
     */
    private static final int HASH_INITIAL = AdaptExpression.class.getName().hashCode();

    private final String fTypeName;

    public AdaptExpression(IConfigurationElement configElement) throws CoreException {
        fTypeName = configElement.getAttribute(ATT_TYPE);
        Expressions.checkAttribute(ATT_TYPE, fTypeName);
    }

    public AdaptExpression(Element element) throws CoreException {
        fTypeName = element.getAttribute(ATT_TYPE);
        Expressions.checkAttribute(ATT_TYPE, fTypeName.isEmpty() ? null : fTypeName);
    }

    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof final AdaptExpression that)) {
            return false;
        }

        return this.fTypeName.equals(that.fTypeName) && equals(this.fExpressions, that.fExpressions);
    }

    @Override
    protected int computeHashCode() {
        return HASH_INITIAL * HASH_FACTOR + hashCode(fExpressions) * HASH_FACTOR + fTypeName.hashCode();
    }

    @Override
    public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {
        if (fTypeName == null) {
            return EvaluationResult.FALSE;
        }
        Object var = context.getDefaultVariable();
        if (var == null) {
            return EvaluationResult.FALSE;
        }
        Object adapted = null;
        IAdapterManager manager = Platform.getAdapterManager();
        if (Expressions.isInstanceOf(var, fTypeName)) {
            adapted = var;
        } else {
            // if the adapter manager doesn't have an adapter contributed,
            // try to see if the variable itself implements IAdaptable
            if (var instanceof IAdaptable) {
                Class<?> typeClazz = Expressions.loadClass(var.getClass().getClassLoader(), fTypeName);
                if (typeClazz != null) {
                    adapted = ((IAdaptable) var).getAdapter(typeClazz);
                }
            }

            if (adapted == null) {
                if (forceLoadEnabled()) {
                    adapted = manager.loadAdapter(var, fTypeName);
                } else {
                    adapted = manager.getAdapter(var, fTypeName);
                    if (adapted == null) {
                        if (manager.queryAdapter(var, fTypeName) == IAdapterManager.NOT_LOADED) {
                            return EvaluationResult.NOT_LOADED;
                        } else {
                            return EvaluationResult.FALSE;
                        }
                    }
                }
                if (adapted == null) {
                    // all attempts failed, return false
                    return EvaluationResult.FALSE;
                }
            }
        }
        // the adapted result is null but hasAdapter returned true check
        // if the adapter is loaded.
        return evaluateAnd(new DefaultVariable(context, adapted));
    }

    private boolean forceLoadEnabled() {
        return true;
    }

    @Override
    public void collectExpressionInfo(ExpressionInfo info) {
        // Although the default variable is passed to the children of this
        // expression as an instance of the adapted type it is OK to only
        // mark a default variable access.
        info.markDefaultVariableAccessed();
        super.collectExpressionInfo(info);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getClass().getSimpleName());
        builder.append(" [type=").append(fTypeName); //$NON-NLS-1$
        Expression[] children = getChildren();
        if (children.length > 0) {
            builder.append(", children="); //$NON-NLS-1$
            builder.append(Arrays.toString(children));
        }
        builder.append("]"); //$NON-NLS-1$
        return builder.toString();
    }

}
