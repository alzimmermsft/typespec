/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.preferences;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.prefs.BackingStoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.prefs.Preferences;
import java.util.EventObject;

/**
 * This interface describes Eclipse extensions to the preference story. It
 * provides means for both preference and node change listeners.
 * <p>
 * Clients may implement this interface.
 * </p>
 *
 * @see org.osgi.service.prefs.Preferences
 * @since 3.0
 */
public interface IEclipsePreferences extends Preferences {

    /**
     * An event object which describes the details of a change in the preference
     * node hierarchy. The child node is the one which was added or removed.
     *
     * @see INodeChangeListener
     * @since 3.0
     */
    final class NodeChangeEvent extends EventObject {
        /**
         * All serializable objects should have a stable serialVersionUID
         */
        private static final long serialVersionUID = 1L;

        private final Preferences child;

        /**
         * Constructor for a new node change event object.
         *
         * @param parent the parent node
         * @param child the child node
         */
        public NodeChangeEvent(Preferences parent, Preferences child) {
            super(parent);
            this.child = child;
        }

        /**
         * Return the parent node for this event. This is the parent of the node which
         * was added or removed.
         *
         * @return the parent node
         */
        public Preferences getParent() {
            return (Preferences) getSource();
        }

        /**
         * Return the child node for this event. This is the node which was added or
         * removed.
         * <p>
         * Note: The child node may have been removed as a result of the bundle
         * supplying its implementation being un-installed. In this case the only method
         * which can safely be called on the child is #name().
         * </p>
         *
         * @return the child node
         */
        public Preferences getChild() {
            return child;
        }
    }

    /**
     * A listener to be used to receive preference node change events.
     * <p>
     * Clients may implement this interface.
     * </p>
     *
     * @since 3.0
     */
    interface INodeChangeListener {

        /**
         * Notification that a child node was added to the preference hierarchy. The
         * given event must not be <code>null</code>.
         *
         * @param event an event specifying the details about the new node
         * @see NodeChangeEvent
         * @see IEclipsePreferences#addNodeChangeListener(INodeChangeListener)
         * @see IEclipsePreferences#removeNodeChangeListener(INodeChangeListener)
         */
        void added(NodeChangeEvent event);

        /**
         * Notification that a child node was removed from the preference hierarchy. The
         * given event must not be <code>null</code>.
         *
         * @param event an event specifying the details about the removed node
         * @see NodeChangeEvent
         * @see IEclipsePreferences#addNodeChangeListener(INodeChangeListener)
         * @see IEclipsePreferences#removeNodeChangeListener(INodeChangeListener)
         */
        void removed(NodeChangeEvent event);
    }

    /**
     * An event object describing the details of a change to a preference in the
     * preference store.
     *
     * @see IPreferenceChangeListener
     * @since 3.0
     */
    final class PreferenceChangeEvent extends EventObject {
        /**
         * All serializable objects should have a stable serialVersionUID
         */
        private static final long serialVersionUID = 1L;

        private final String key;
        private final Object newValue;
        private final Object oldValue;

        /**
         * Constructor for a new preference change event. The node and the key must not
         * be <code>null</code>. The old and new preference values must be either a
         * <code>String</code> or <code>null</code>.
         *
         * @param node the node on which the change occurred
         * @param key the preference key
         * @param oldValue the old preference value, as a <code>String</code> or
         * <code>null</code>
         * @param newValue the new preference value, as a <code>String</code> or
         * <code>null</code>
         */
        public PreferenceChangeEvent(Object node, String key, Object oldValue, Object newValue) {
            super(node);
            if (key == null || !(node instanceof Preferences)) {
                throw new IllegalArgumentException();
            }
            this.key = key;
            this.newValue = newValue;
            this.oldValue = oldValue;
        }

        /**
         * Return the preference node on which the change occurred. Must not be
         * <code>null</code>.
         *
         * @return the node
         */
        public Preferences getNode() {
            return (Preferences) source;
        }

        /**
         * Return the key of the preference which was changed. Must not be
         * <code>null</code>.
         *
         * @return the preference key
         */
        public String getKey() {
            return key;
        }

        /**
         * Return the new value for the preference encoded as a <code>String</code>, or
         * <code>null</code> if the preference was removed.
         *
         * @return the new value or <code>null</code>
         */
        public Object getNewValue() {
            return newValue;
        }

        /**
         * Return the old value for the preference encoded as a <code>String</code>, or
         * <code>null</code> if the preference was removed or if it cannot be
         * determined.
         *
         * @return the old value or <code>null</code>
         */
        public Object getOldValue() {
            return oldValue;
        }
    }

    /**
     * A listener used to receive changes to preference values in the preference
     * store.
     * <p>
     * Clients may implement this interface.
     * </p>
     *
     * @since 3.0
     */
    interface IPreferenceChangeListener {

        /**
         * Notification that a preference value has changed in the preference store. The
         * given event object describes the change details and must not be
         * <code>null</code>.
         *
         * @param event the event details
         * @see PreferenceChangeEvent
         * @see IEclipsePreferences#addPreferenceChangeListener(IPreferenceChangeListener)
         * @see IEclipsePreferences#removePreferenceChangeListener(IPreferenceChangeListener)
         */
        void preferenceChange(PreferenceChangeEvent event);
    }

    /**
     * Register the given listener for changes to this node. Duplicate calls to this
     * method with the same listener will have no effect. The given listener
     * argument must not be <code>null</code>.
     *
     * @param listener the node change listener to add
     * @throws IllegalStateException if this node or an ancestor has been removed
     * @see #removeNodeChangeListener(INodeChangeListener)
     * @see INodeChangeListener
     */
    void addNodeChangeListener(INodeChangeListener listener);

    /**
     * De-register the given listener from receiving event change notifications for
     * this node. Calling this method with a listener which is not registered has no
     * effect. The given listener argument must not be <code>null</code>.
     *
     * @param listener the node change listener to remove
     * @throws IllegalStateException if this node or an ancestor has been removed
     * @see #addNodeChangeListener(INodeChangeListener)
     * @see INodeChangeListener
     */
    void removeNodeChangeListener(INodeChangeListener listener);

    /**
     * Register the given listener for notification of preference changes to this
     * node. Calling this method multiple times with the same listener has no
     * effect. The given listener argument must not be <code>null</code>.
     *
     * @param listener the preference change listener to register
     * @throws IllegalStateException if this node or an ancestor has been removed
     * @see #removePreferenceChangeListener(IPreferenceChangeListener)
     * @see IPreferenceChangeListener
     */
    void addPreferenceChangeListener(IPreferenceChangeListener listener);

    /**
     * De-register the given listener from receiving notification of preference
     * changes to this node. Calling this method multiple times with the same
     * listener has no effect. The given listener argument must not be
     * <code>null</code>.
     *
     * @param listener the preference change listener to remove
     * @throws IllegalStateException if this node or an ancestor has been removed
     * @see #addPreferenceChangeListener(IPreferenceChangeListener)
     * @see IPreferenceChangeListener
     */
    void removePreferenceChangeListener(IPreferenceChangeListener listener);

    /**
     * Return the preferences node with the given path. The given path must not be
     * <code>null</code>.
     * <p>
     * See the spec of {@link Preferences#node(String)} for more details.
     * </p>
     * <p>
     * Note that if the node does not yet exist and is created, then the appropriate
     * {@link NodeChangeEvent} must be sent to listeners who are registered at this
     * node.
     * </p>
     *
     * @param path the path of the node
     * @return the node
     * @see org.osgi.service.prefs.Preferences#node(String)
     * @see NodeChangeEvent
     */
    @Override
    Preferences node(String path);

    /**
     * Accepts the given visitor. The visitor's <code>visit</code> method is called
     * with this node. If the visitor returns <code>true</code>, this method visits
     * this node's children.
     *
     * @param visitor the visitor
     * @see IPreferenceNodeVisitor#visit(IEclipsePreferences)
     * @throws BackingStoreException if this operation cannot be completed due to a
     * failure in the backing store, or inability to
     * communicate with it.
     */
    void accept(IPreferenceNodeVisitor visitor) throws BackingStoreException;
}
