/*******************************************************************************
 * Copyright (c) 2004, 2013 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.dom.rewrite;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.dom.ASTNode;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.dom.rewrite.ListRewriteEvent;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.dom.rewrite.RewriteEvent;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.dom.rewrite.RewriteEventStore;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.text.edits.TextEditGroup;
import java.util.Collections;
import java.util.List;

/**
 * For describing manipulations to a child list property of an AST node.
 * <p>
 * This class is not intended to be subclassed.
 * </p>
 * 
 * @see ASTRewrite#getListRewrite(ASTNode, ChildListPropertyDescriptor)
 * @since 3.0
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public final class ListRewrite {

    private final ASTNode parent;
    private final ChildListPropertyDescriptor childListProperty;
    private final ASTRewrite rewriter;

    /* package */ ListRewrite(ASTRewrite rewriter, ASTNode parent, ChildListPropertyDescriptor childProperty) {
        this.rewriter = rewriter;
        this.parent = parent;
        this.childListProperty = childProperty;
    }

    private RewriteEventStore getRewriteStore() {
        return this.rewriter.getRewriteEventStore();
    }

    private ListRewriteEvent getEvent() {
        return getRewriteStore().getListEvent(this.parent, this.childListProperty, true);
    }

    /**
     * Returns the parent of the list for which this list rewriter was created.
     * 
     * @return the node that contains the list for which this list rewriter was created
     * @since 3.1
     */
    public ASTNode getParent() {
        return this.parent;
    }

    /**
     * Removes the given node from its parent's list property in the rewriter.
     * The node must be contained in the list.
     * The AST itself is not actually modified in any way; rather, the rewriter
     * just records a note that this node has been removed from this list.
     *
     * @param node the node being removed. The node can either be an original node in this list
     * or (since 3.4) a new node already inserted or used as replacement in this AST rewriter.
     * @param editGroup the edit group in which to collect the corresponding
     * text edits, or <code>null</code> if ungrouped
     * @throws IllegalArgumentException if the node is null, or if the node is not
     * part of this rewriter's AST, or if the described modification is invalid
     * (not a member of this node's original list)
     */
    public void remove(ASTNode node, TextEditGroup editGroup) {
        if (node == null) {
            throw new IllegalArgumentException();
        }
        RewriteEvent event = getEvent().removeEntry(node);
        if (editGroup != null) {
            getRewriteStore().setEventEditGroup(event, editGroup);
        }
    }

    /**
     * Returns the ASTRewrite instance from which this ListRewriter has been created from.
     * 
     * @return the parent AST Rewriter instance.
     * @since 3.1
     */
    public ASTRewrite getASTRewrite() {
        return this.rewriter;
    }

    /**
     * Replaces the given node from its parent's list property in the rewriter.
     * The node must be contained in the list.
     * The replacement node must either be brand new (not part of the original AST)
     * or a placeholder node (for example, one created by
     * {@link ASTRewrite#createCopyTarget(ASTNode)},
     * {@link ASTRewrite#createMoveTarget(ASTNode)},
     * or {@link ASTRewrite#createStringPlaceholder(String, int)}). The AST itself
     * is not actually modified in any way; rather, the rewriter just records
     * a note that this node has been replaced in this list.
     *
     * @param node the node being removed. The node can either be an original node in this list
     * or (since 3.4) a new node already inserted or used as replacement in this AST rewriter.
     * @param replacement the replacement node, or <code>null</code> if no
     * replacement
     * @param editGroup the edit group in which to collect the corresponding
     * text edits, or <code>null</code> if ungrouped
     * @throws IllegalArgumentException if the node is null, or if the node is not part
     * of this rewriter's AST, or if the replacement node is not a new node (or
     * placeholder), or if the described modification is otherwise invalid
     * (not a member of this node's original list)
     */
    public void replace(ASTNode node, ASTNode replacement, TextEditGroup editGroup) {
        if (node == null) {
            throw new IllegalArgumentException();
        }
        validatePropertyType(node);
        RewriteEvent event = getEvent().replaceEntry(node, replacement);
        if (editGroup != null) {
            getRewriteStore().setEventEditGroup(event, editGroup);
        }
    }

    private void validatePropertyType(ASTNode node) {
        if (!RewriteEventStore.DEBUG) {
            return;
        }
        if (!this.childListProperty.getElementType().isAssignableFrom(node.getClass())) {
            String message = node.getClass().getName() + " is not a valid type for " //$NON-NLS-1$
                + this.childListProperty.getNodeClass().getName() + " property '" + this.childListProperty.getId() //$NON-NLS-1$
                + "'. Must be "  //$NON-NLS-1$
                + this.childListProperty.getElementType().getName();
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Returns the nodes in the revised list property managed by this
     * rewriter. The returned list is unmodifiable.
     *
     * @return a list of all nodes in the list taking into account
     * all the described changes
     */
    public List getRewrittenList() {
        List list = (List) getEvent().getNewValue();
        return Collections.unmodifiableList(list);
    }

}
