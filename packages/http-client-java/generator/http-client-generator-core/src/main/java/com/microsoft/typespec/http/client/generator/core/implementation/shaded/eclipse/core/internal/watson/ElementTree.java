/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *     James Blackburn (Broadcom Corp.) - ongoing development
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 473427
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.watson;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.dtree.DataTreeLookup;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.dtree.DeltaDataTree;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.dtree.ObjectNotFoundException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.utils.Messages;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Assert;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.util.NLS;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * An ElementTree can be viewed as a generic rooted tree that stores a hierarchy
 * of elements. An element in the tree consists of a (name, data, children)
 * 3-tuple. The name ({@link #getNamesOfChildren(IPath)}) can be any String, and
 * the data ({@link #getElementData(IPath)}) can be any Object. The children
 * ({@link #getChildren(IPath)}) are a collection of zero or more elements that
 * logically fall below their parent in the tree. The implementation makes no
 * guarantees about the ordering of children.
 * </p>
 *
 * <p>
 * Elements in the tree are referenced by a key that consists of the names of
 * all elements on the path from the root to that element in the tree. For
 * example, if root node "a" has child "b", which has child "c", element "c" can
 * be referenced in the tree using the key (/a/b/c). Keys are represented using
 * {@link IPath} objects, where the Paths are relative to the root element of
 * the tree.
 * </p>
 *
 * <p>
 * Each ElementTree has a single root element that is created implicitly and is
 * always present in any tree. This root corresponds to the key (/), or the
 * singleton <code>Path.ROOT</code>. The root element cannot be created or
 * deleted, and its data and name cannot be set. The root element's children
 * however can be modified (added, deleted, etc). The root path can be obtained
 * using the <code>getRoot()</code> method.
 * </p>
 *
 * <p>
 * ElementTrees are modified in generations. The method {@link #newEmptyDelta()}
 * returns a new tree generation that can be modified arbitrarily by the user.
 * For the purpose of explanation, we call such a tree "active". When the method
 * {@link #immutable()} is called, that tree generation is frozen, and can never
 * again be modified. A tree must be immutable before a new tree generation can
 * start. Since all ancestor trees are immutable, different active trees can
 * have ancestors in common without fear of thread corruption problems.
 * </p>
 *
 * <p>
 * Internally, any single tree generation is simply stored as the set of changes
 * between itself and its most recent ancestor (its parent). This compact delta
 * representation allows chains of element trees to be created at relatively low
 * cost. Clients of the ElementTree can instantaneously "undo" sets of changes
 * by navigating up to the parent tree using the {@link #getParent()} method.
 * </p>
 *
 * <p>
 * Although the delta representation is compact, extremely long delta chains
 * make for a large structure that is potentially slow to query. For this
 * reason, the client is encouraged to minimize delta chain lengths using the
 * {@link #collapseTo(ElementTree)} and {@link DeltaDataTree#makeComplete()}
 * methods. The entire delta chain can also be re-oriented in terms of the
 * current element tree using the {@link DeltaDataTree#reroot()} operation.
 * </p>
 *
 * <p>
 * Classes are also available for tree serialization and navigation (
 * {@link ElementTreeIterator} )
 * </p>
 *
 * Finally, the package name is called "watson" because - (misquoting Sherlock
 * Holmes)
 *
 * <pre>
 * "It's ElementTree my dear Watson, ElementTree."
 * </pre>
 *
 * @see DeltaDataTree
 */
public class ElementTree {
    protected final DeltaDataTree tree;
    protected volatile IElementTreeData userData;

    private static final class ChildIDsCache {
        ChildIDsCache(IPath path, IPath[] childPaths) {
            this.path = path;
            this.childPaths = childPaths;
        }

        final IPath path;
        final IPath[] childPaths;
    }

    /** synchronized access **/
    private volatile ChildIDsCache childIDsCache = null;

    /** synchronized access **/
    private volatile DataTreeLookup lookupCache = null;

    private final static AtomicInteger treeCounter = new AtomicInteger();
    private final int treeStamp;

    /**
     * Creates a new empty element tree.
     */
    public ElementTree() {
        this(new DeltaDataTree());
    }

    /**
     * Creates a new element tree with the given data tree as its representation.
     */
    protected ElementTree(DeltaDataTree newTree) {
        // Keep this element tree as the data of the root node.
        // Useful for canonical results for ElementTree.getParent().
        // see getParent().
        treeStamp = treeCounter.incrementAndGet();
        newTree.setRootData(this);
        this.tree = newTree;
    }

    /**
     * Creates a new empty delta element tree having the
     * given tree as its parent.
     */
    protected ElementTree(ElementTree parent) {
        this(parent.tree.newEmptyDeltaTree());

        /* copy the user data forward */
        IElementTreeData data = parent.getTreeData();
        if (data != null) {
            userData = (IElementTreeData) data.clone();
        }
    }

    /**
     * Creates the indicated element and sets its element info.
     * The parent element must be present, otherwise an IllegalArgumentException
     * is thrown. If the indicated element is already present in the tree,
     * its element info is replaced and any existing children are
     * deleted.
     *
     * @param key element key
     * @param data element data, or <code>null</code>
     */
    public synchronized void createElement(IPath key, Object data) {
        /* don't allow modification of the implicit root */
        if (key.isRoot()) {
            return;
        }

        // Clear the child IDs cache in case it's referring to this parent. This is conservative.
        childIDsCache = null;

        IPath parent = key.removeLastSegments(1);
        try {
            tree.createChild(parent, key.lastSegment(), data);
        } catch (ObjectNotFoundException e) {
            throw createElementNotFoundException(parent);
        }
        // Set the lookup to be this newly created object.
        lookupCache = DataTreeLookup.newLookup(key, true, data, true);
    }

    private IllegalArgumentException createElementNotFoundException(IPath key) {
        return new IllegalArgumentException(NLS.bind(Messages.watson_elementNotFound, key));
    }

    /**
     * Returns the IDs of the children of the specified element.
     * If the specified element is null, returns the root element path.
     */
    protected IPath[] getChildIDs(IPath key) {
        ChildIDsCache cache = childIDsCache; // Grab it in case it's replaced concurrently.
        if (cache != null && cache.path == key) {
            return cache.childPaths;
        }
        if (key == null) {
            return tree.rootPaths();
        }
        try {
            IPath[] children = tree.getChildren(key);
            childIDsCache = new ChildIDsCache(key, children); // Cache the result
            return children;
        } catch (ObjectNotFoundException e) {
            throw createElementNotFoundException(key);
        }
    }

    /**
     * Returns the paths of the children of the element
     * specified by the given path.
     * The given element must be present in this tree.
     */
    public synchronized IPath[] getChildren(IPath key) {
        Assert.isNotNull(key);
        return getChildIDs(key);
    }

    /**
     * Returns the internal data tree.
     */
    public DeltaDataTree getDataTree() {
        return tree;
    }

    /**
     * Returns the element data for the given element identifier.
     * The given element must be present in this tree.
     */
    public Object getElementData(IPath key) {
        /* don't allow modification of the implicit root */
        if (key.isRoot()) {
            return null;
        }
        synchronized (this) {
            DataTreeLookup lookup = lookupCache; // Grab it in case it's replaced concurrently.
            if (lookup == null || lookup.key != key) {
                lookupCache = lookup = tree.lookup(key);
            }
            if (lookup.isPresent) {
                return lookup.data;
            }
        }
        throw createElementNotFoundException(key);
    }

    /**
     * Returns the parent tree, or <code>null</code> if there is no parent.
     */
    public ElementTree getParent() {
        DeltaDataTree parentTree = tree.getParent();
        if (parentTree == null) {
            return null;
        }
        // The parent ElementTree is stored as the node data of the parent DeltaDataTree,
        // to simplify canonicalization in the presence of rerooting.
        return (ElementTree) parentTree.getRootData();
    }

    /**
     * Returns the root node of this tree.
     */
    public IPath getRoot() {
        return getChildIDs(null)[0];
    }

    /**
     * Returns the user data associated with this tree.
     */
    public IElementTreeData getTreeData() {
        return userData;
    }

    /**
     * Returns true if there have been changes in the tree between the two
     * given layers. The two must be related and new must be newer than old.
     * That is, new must be an ancestor of old.
     */
    public static boolean hasChanges(ElementTree newLayer, ElementTree oldLayer, IElementComparator comparator,
        boolean inclusive) {
        // if any of the layers are null, assume that things have changed
        if (newLayer == null || oldLayer == null) {
            return true;
        }
        if (newLayer == oldLayer) {
            return false;
        }
        // if the tree data has changed, then the tree has changed
        if (comparator.compare(newLayer.getTreeData(), oldLayer.getTreeData()) != IElementComparator.K_NO_CHANGE) {
            return true;
        }

        // The tree structure has the top layer(s) (i.e., tree) parentage pointing down to a complete
        // layer whose parent is null. The bottom layers (i.e., operationTree) point up to the
        // common complete layer whose parent is null. The complete layer moves up as
        // changes happen. To see if any changes have happened, we should consider only
        // layers whose parent is not null. That is, skip the complete layer as it will clearly not be
        // empty.

        // look down from the current layer (always inclusive) if the top layer is mutable
        ElementTree stopLayer = null;
        if (newLayer.isImmutable()) {
            // if the newLayer is immutable, the tree structure all points up so ensure that
            // when searching up, we stop at newLayer (inclusive)
            stopLayer = newLayer.getParent();
        } else {
            ElementTree layer = newLayer;
            while (layer != null && layer.getParent() != null) {
                if (!layer.getDataTree().isEmptyDelta()) {
                    return true;
                }
                layer = layer.getParent();
            }
        }

        // look up from the layer at which we started to null or newLayer's parent (variably inclusive)
        // depending on whether newLayer is mutable.
        ElementTree layer = inclusive ? oldLayer : oldLayer.getParent();
        while (layer != null && layer.getParent() != stopLayer) {
            if (!layer.getDataTree().isEmptyDelta()) {
                return true;
            }
            layer = layer.getParent();
        }
        // didn't find anything that changed
        return false;
    }

    /**
     * Makes this tree immutable (read-only); ignored if it is already
     * immutable.
     */
    public synchronized void immutable() {
        if (!tree.isImmutable()) {
            tree.immutable();
            /*
             * need to clear the lookup cache since it reports whether results were found
             * in the topmost delta, and the order of deltas is changing
             */
            lookupCache = null;
            /* reroot the delta chain at this tree */
            tree.reroot();
        }
    }

    /**
     * Returns true if this element tree includes an element with the given
     * key, false otherwise.
     */
    public boolean includes(IPath key) {
        DataTreeLookup lookup = lookupCache; // Grab it in case it's replaced concurrently.
        if (lookup == null || lookup.key != key) {
            synchronized (this) {
                lookupCache = lookup = tree.lookup(key);
            }
        }
        return lookup.isPresent;
    }

    /**
     * Returns whether this tree is immutable.
     */
    public boolean isImmutable() {
        return tree.isImmutable();
    }

    /**
     * Creates a new element tree which is represented as a delta on this one.
     * Initially they have the same content. Subsequent changes to the new
     * tree will not affect this one.
     */
    public synchronized ElementTree newEmptyDelta() {
        // Don't want old trees hanging onto cached infos.
        lookupCache = null;
        // reclaim memory of childIDsCache - which is only used on the current tree:
        childIDsCache = null;
        if (!this.isImmutable()) {
            this.immutable();
        }
        return new ElementTree(this);
    }

    /**
     * Returns a mutable copy of the element data for the given path.
     * This copy will be held onto in the most recent delta.
     * ElementTree data MUST implement the IElementTreeData interface
     * for this method to work. If the data does not define that interface
     * this method will fail.
     */
    public synchronized Object openElementData(IPath key) {
        Assert.isTrue(!isImmutable());

        /* don't allow modification of the implicit root */
        if (key.isRoot()) {
            return null;
        }
        DataTreeLookup lookup = lookupCache; // Grab it in case it's replaced concurrently.
        if (lookup == null || lookup.key != key) {
            lookupCache = lookup = tree.lookup(key);
        }
        if (lookup.isPresent) {
            if (lookup.foundInFirstDelta) {
                return lookup.data;
            }
            /**
             * The node has no data in the most recent delta.
             * Pull it up to the present delta by setting its data with a clone.
             */
            IElementTreeData oldData = (IElementTreeData) lookup.data;
            if (oldData != null) {
                try {
                    Object newData = oldData.clone();
                    tree.setData(key, newData);
                    lookupCache = null;
                    return newData;
                } catch (ObjectNotFoundException e) {
                    throw createElementNotFoundException(key);
                }
            }
        } else {
            throw createElementNotFoundException(key);
        }
        return null;
    }

    /**
     * Sets the element for the given element identifier.
     * The given element must be present in this tree.
     * 
     * @param key element identifier
     * @param data element info, or <code>null</code>
     */
    public synchronized void setElementData(IPath key, Object data) {
        /* don't allow modification of the implicit root */
        if (key.isRoot()) {
            return;
        }

        Assert.isNotNull(key);
        // Clear the lookup cache, in case the element being modified is the same
        // as for the last lookup.
        lookupCache = null;
        try {
            tree.setData(key, data);
        } catch (ObjectNotFoundException e) {
            throw createElementNotFoundException(key);
        }
    }

    /**
     * Sets the user data associated with this tree.
     */
    public void setTreeData(IElementTreeData data) {
        userData = data;
    }

    /** for debugging purposes only */
    @Override
    public String toString() {
        return "ElementTree(" + treeStamp + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
