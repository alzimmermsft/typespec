/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.dtree;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.utils.IStringPoolParticipant;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.utils.Messages;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.utils.StringPool;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Assert;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;

/**
 * <code>DataTreeNode</code>s are the nodes of a <code>DataTree</code>. Their
 * information and their subtrees are complete, and do not represent deltas on
 * another node or subtree.
 */
public class DataTreeNode extends AbstractDataTreeNode {
    protected Object data;

    /**
     * Creates a new node
     *
     * @param name name of node
     * @param data data for node
     */
    public DataTreeNode(String name, Object data) {
        super(name, AbstractDataTreeNode.NO_CHILDREN);
        this.data = data;
    }

    /**
     * Creates a new node
     *
     * @param name name of node
     * @param data data for node
     * @param children children for new node
     */
    public DataTreeNode(String name, Object data, AbstractDataTreeNode[] children) {
        super(name, children);
        this.data = data;
    }

    /**
     * @see AbstractDataTreeNode#asBackwardDelta(DeltaDataTree, DeltaDataTree, IPath)
     */
    @Override
    AbstractDataTreeNode asBackwardDelta(DeltaDataTree myTree, DeltaDataTree parentTree, IPath key) {
        if (parentTree.includes(key)) {
            return parentTree.copyCompleteSubtree(key);
        }
        return new DeletedNode(name);
    }

    /**
     * If this node is a node in a comparison tree, this method reverses
     * the comparison for this node and all children. Returns null
     * if this node should no longer be included in the comparison tree.
     */
    @Override
    AbstractDataTreeNode asReverseComparisonNode(IComparator comparator) {
        NodeComparison comparison = null;
        try {
            comparison = ((NodeComparison) data).asReverseComparison(comparator);
        } catch (ClassCastException e) {
            Assert.isTrue(false, Messages.dtree_reverse);
        }

        int nextChild = 0;
        for (AbstractDataTreeNode c : children) {
            AbstractDataTreeNode child = c.asReverseComparisonNode(comparator);
            if (child != null) {
                children[nextChild++] = child;
            }
        }

        if (nextChild == 0 && comparison.getUserComparison() == 0) {
            /* no children and no change */
            return null;
        }

        /* set the new data */
        data = comparison;

        /* shrink child array as necessary */
        if (nextChild < children.length) {
            AbstractDataTreeNode[] newChildren = new AbstractDataTreeNode[nextChild];
            System.arraycopy(children, 0, newChildren, 0, nextChild);
            children = newChildren;
        }

        return this;
    }

    AbstractDataTreeNode compareWith(DataTreeNode other, IComparator comparator) {
        AbstractDataTreeNode[] comparedChildren = compareWith(children, other.children, comparator);
        Object oldData = data;
        Object newData = other.data;

        /* don't allow comparison of implicit root node */
        int userComparison = 0;
        if (name != null) {
            userComparison = comparator.compare(oldData, newData);
        }

        return new DataTreeNode(name, new NodeComparison(oldData, newData, NodeComparison.K_CHANGED, userComparison),
            comparedChildren);
    }

    @Override
    AbstractDataTreeNode compareWithParent(IPath key, DeltaDataTree parent, IComparator comparator) {
        if (!parent.includes(key)) {
            return convertToAddedComparisonNode(this, NodeComparison.K_ADDED);
        }
        DataTreeNode inParent = (DataTreeNode) parent.copyCompleteSubtree(key);
        return inParent.compareWith(this, comparator);
    }

    /**
     * Creates and returns a new copy of the receiver.
     */
    @Override
    AbstractDataTreeNode copy() {
        if (children.length > 0) {
            AbstractDataTreeNode[] childrenCopy = new AbstractDataTreeNode[children.length];
            System.arraycopy(children, 0, childrenCopy, 0, children.length);
            return new DataTreeNode(name, data, childrenCopy);
        }
        return new DataTreeNode(name, data, children);
    }

    /**
     * Returns an array of delta nodes representing the forward delta between
     * the given two lists of nodes.
     * The given nodes must all be complete nodes.
     */
    protected static AbstractDataTreeNode[] forwardDeltaWith(AbstractDataTreeNode[] oldNodes,
        AbstractDataTreeNode[] newNodes, IComparator comparer) {
        if (oldNodes.length == 0 && newNodes.length == 0) {
            return NO_CHILDREN;
        }

        AbstractDataTreeNode[] childDeltas = null;
        int numChildDeltas = 0;
        int childDeltaMax = 0;

        // do a merge
        int oldIndex = 0;
        int newIndex = 0;
        while (oldIndex < oldNodes.length && newIndex < newNodes.length) {
            String oldName = oldNodes[oldIndex].name;
            String newName = newNodes[newIndex].name;
            int compare = oldName.compareTo(newName);
            if (compare == 0) {
                AbstractDataTreeNode deltaNode
                    = forwardDeltaWithOrNullIfEqual(oldNodes[oldIndex++], newNodes[newIndex++], comparer);
                if (deltaNode != null) {
                    if (numChildDeltas >= childDeltaMax) {
                        if (childDeltas == null) {
                            childDeltas = new AbstractDataTreeNode[childDeltaMax = 5];
                        } else {
                            System.arraycopy(childDeltas, 0,
                                childDeltas = new AbstractDataTreeNode[childDeltaMax = childDeltaMax * 2 + 1], 0,
                                numChildDeltas);
                        }
                    }
                    childDeltas[numChildDeltas++] = deltaNode;
                }
            } else if (compare < 0) {
                if (numChildDeltas >= childDeltaMax) {
                    if (childDeltas == null) {
                        childDeltas = new AbstractDataTreeNode[childDeltaMax = 5];
                    } else {
                        System.arraycopy(childDeltas, 0,
                            childDeltas = new AbstractDataTreeNode[childDeltaMax = childDeltaMax * 2 + 1], 0,
                            numChildDeltas);
                    }
                }
                childDeltas[numChildDeltas++] = new DeletedNode(oldName);
                oldIndex++;
            } else {
                if (numChildDeltas >= childDeltaMax) {
                    if (childDeltas == null) {
                        childDeltas = new AbstractDataTreeNode[childDeltaMax = 5];
                    } else {
                        System.arraycopy(childDeltas, 0,
                            childDeltas = new AbstractDataTreeNode[childDeltaMax = childDeltaMax * 2 + 1], 0,
                            numChildDeltas);
                    }
                }
                childDeltas[numChildDeltas++] = newNodes[newIndex++];
            }
        }
        while (oldIndex < oldNodes.length) {
            if (numChildDeltas >= childDeltaMax) {
                if (childDeltas == null) {
                    childDeltas = new AbstractDataTreeNode[childDeltaMax = 5];
                } else {
                    System.arraycopy(childDeltas, 0,
                        childDeltas = new AbstractDataTreeNode[childDeltaMax = childDeltaMax * 2 + 1], 0,
                        numChildDeltas);
                }
            }
            childDeltas[numChildDeltas++] = new DeletedNode(oldNodes[oldIndex++].name);
        }
        while (newIndex < newNodes.length) {
            if (numChildDeltas >= childDeltaMax) {
                if (childDeltas == null) {
                    childDeltas = new AbstractDataTreeNode[childDeltaMax = 5];
                } else {
                    System.arraycopy(childDeltas, 0,
                        childDeltas = new AbstractDataTreeNode[childDeltaMax = childDeltaMax * 2 + 1], 0,
                        numChildDeltas);
                }
            }
            childDeltas[numChildDeltas++] = newNodes[newIndex++];
        }

        // trim size of result
        if (numChildDeltas == 0) {
            return NO_CHILDREN;
        }
        if (numChildDeltas < childDeltaMax) {
            System.arraycopy(childDeltas, 0, childDeltas = new AbstractDataTreeNode[numChildDeltas], 0, numChildDeltas);
        }
        return childDeltas;
    }

    /**
     * Returns a node representing the forward delta between
     * the given two (complete) nodes.
     */
    protected AbstractDataTreeNode forwardDeltaWith(DataTreeNode other, IComparator comparer) {
        AbstractDataTreeNode deltaNode = forwardDeltaWithOrNullIfEqual(this, other, comparer);
        if (deltaNode == null) {
            return new NoDataDeltaNode(name, NO_CHILDREN);
        }
        return deltaNode;
    }

    /**
     * Returns a node representing the forward delta between
     * the given two (complete) nodes, or null if the two nodes are equal.
     * Although typed as abstract nodes, the given nodes must be complete.
     */
    protected static AbstractDataTreeNode forwardDeltaWithOrNullIfEqual(AbstractDataTreeNode oldNode,
        AbstractDataTreeNode newNode, IComparator comparer) {
        AbstractDataTreeNode[] childDeltas = forwardDeltaWith(oldNode.children, newNode.children, comparer);
        Object newData = newNode.getData();
        if (comparer.compare(oldNode.getData(), newData) == 0) {
            if (childDeltas.length == 0) {
                return null;
            }
            return new NoDataDeltaNode(newNode.name, childDeltas);
        }
        return new DataDeltaNode(newNode.name, newData, childDeltas);
    }

    /**
     * Returns the data for the node
     */
    @Override
    public Object getData() {
        return data;
    }

    /**
     * Returns true if the receiver can carry data, false otherwise.
     */
    @Override
    boolean hasData() {
        return true;
    }

    /**
     * Simplifies the given node, and answers its replacement.
     */
    @Override
    AbstractDataTreeNode simplifyWithParent(IPath key, DeltaDataTree parent, IComparator comparer) {
        /* If not in parent, can't be simplified */
        if (!parent.includes(key)) {
            return this;
        }
        /*
         * Can't just call simplify on children since this will miss the case
         * where a child exists in the parent but does not in this.
         * See PR 1FH5RYA.
         */
        DataTreeNode parentsNode = (DataTreeNode) parent.copyCompleteSubtree(key);
        return parentsNode.forwardDeltaWith(this, comparer);
    }

    @Override
    public void storeStrings(StringPool set) {
        super.storeStrings(set);
        // copy data for thread safety
        Object o = data;
        if (o instanceof IStringPoolParticipant) {
            ((IStringPoolParticipant) o).shareStrings(set);
        }
    }

    @Override
    int type() {
        return T_COMPLETE_NODE;
    }
}
