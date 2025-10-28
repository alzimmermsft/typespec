/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.expressions;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.expressions.Expression;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.expressions.ExpressionConverter;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IConfigurationElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IExtensionDelta;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IExtensionRegistry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IRegistryChangeEvent;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IRegistryChangeListener;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.InvalidRegistryObjectException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Platform;
import java.util.HashMap;
import java.util.Map;

/**
 * This manages the extension point that allows core expression reuse.
 *
 * @since 3.3
 */
public class DefinitionRegistry implements IRegistryChangeListener {
    private Map<String, Expression> cache = null;

    private Map<String, Expression> getCache() {
        if (cache == null) {
            cache = new HashMap<>();
        }
        return cache;
    }

    public DefinitionRegistry() {
        Platform.getExtensionRegistry()
            .addRegistryChangeListener(this,
                "com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.expressions"); //$NON-NLS-1$
    }

    /**
     * Get the expression with the id defined by an extension. This class will
     * cache the expressions when appropriate, so it's OK to always ask the
     * registry.
     *
     * @param id The unique ID of the expression definition
     * @return the expression
     * @throws CoreException If the expression cannot be found.
     */
    public Expression getExpression(String id) throws CoreException {
        Expression cachedExpression = getCache().get(id);
        if (cachedExpression != null) {
            return cachedExpression;
        }

        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IConfigurationElement[] ces = registry.getConfigurationElementsFor(
            "com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.expressions", //$NON-NLS-1$
            "definitions");  //$NON-NLS-1$

        Expression foundExpression = null;
        for (IConfigurationElement ce : ces) {
            String cid = ce.getAttribute("id"); //$NON-NLS-1$
            if (cid != null && cid.equals(id)) {
                try {
                    foundExpression = getExpression(id, ce);
                    break;
                } catch (InvalidRegistryObjectException e) {
                    throw new CoreException(new ExpressionStatus(ExpressionStatus.MISSING_EXPRESSION,
                        Messages.format(ExpressionMessages.Missing_Expression, id)));
                }
            }
        }
        if (foundExpression == null) {
            throw new CoreException(new ExpressionStatus(ExpressionStatus.MISSING_EXPRESSION,
                Messages.format(ExpressionMessages.Missing_Expression, id)));
        }
        return foundExpression;
    }

    private Expression getExpression(String id, IConfigurationElement element)
        throws InvalidRegistryObjectException, CoreException {
        Expression expr = ExpressionConverter.getDefault().perform(element.getChildren()[0]);
        if (expr != null) {
            getCache().put(id, expr);
        }
        return expr;
    }

    @Override
    public void registryChanged(IRegistryChangeEvent event) {
        IExtensionDelta[] extensionDeltas = event.getExtensionDeltas(
            "com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.expressions", //$NON-NLS-1$
            "definitions"); //$NON-NLS-1$
        for (IExtensionDelta extensionDelta : extensionDeltas) {
            if (extensionDelta.getKind() == IExtensionDelta.REMOVED) {
                IConfigurationElement[] ces = extensionDelta.getExtension().getConfigurationElements();
                for (IConfigurationElement ce : ces) {
                    String id = ce.getAttribute("id"); //$NON-NLS-1$
                    if (id != null) {
                        getCache().remove(id);
                    }
                }
            }
        }
    }
}
