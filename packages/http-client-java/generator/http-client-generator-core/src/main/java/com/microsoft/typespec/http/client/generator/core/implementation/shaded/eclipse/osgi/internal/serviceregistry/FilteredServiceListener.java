/*******************************************************************************
 * Copyright (c) 2003, 2012 IBM Corporation and others.
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

package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.serviceregistry;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.framework.BundleContextImpl;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.framework.FilterImpl;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.BundleContext;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Constants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.InvalidSyntaxException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.ServiceListener;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.UnfilteredServiceListener;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.hooks.service.ListenerHook;

/**
 * Service Listener delegate.
 */
class FilteredServiceListener implements ServiceListener, ListenerHook.ListenerInfo {
	/** Filter for listener. */
	private final FilterImpl filter;
	/** Real listener. */
	private final ServiceListener listener;
	/** The bundle context */
	private final BundleContextImpl context;
    /** an objectClass required by the filter */
	private final String objectClass;
	/** indicates whether the listener has been removed */
	private volatile boolean removed;

    /**
	 * Constructor.
	 *
	 * @param context      The bundle context of the bundle which added the
	 *                     specified service listener.
	 * @param filterstring The filter string specified when this service listener
	 *                     was added.
	 * @param listener     The service listener object.
	 * @exception InvalidSyntaxException if the filter is invalid.
	 */
	FilteredServiceListener(final BundleContextImpl context, final ServiceListener listener, final String filterstring)
			throws InvalidSyntaxException {
        /** is this an UnfilteredServiceListener */
        boolean unfiltered = (listener instanceof UnfilteredServiceListener);
		if (filterstring == null) {
			this.filter = null;
			this.objectClass = null;
		} else {
			FilterImpl filterImpl = FilterImpl.newInstance(filterstring);
			String clazz = filterImpl.getRequiredObjectClass();
			if (unfiltered || (clazz == null)) {
				this.objectClass = null;
				this.filter = filterImpl;
			} else {
				this.objectClass = clazz.intern(); /* intern the name for future identity comparison */
				// a filter with no children and non-null requiredObjectClass is a simple
				// filter;
				// e.g. (objectClass=SomeService)
				this.filter = filterImpl.getChildren().isEmpty() ? null : filterImpl;
			}
		}
		this.removed = false;
		this.listener = listener;
		this.context = context;
    }

    /**
	 * The string representation of this Filtered listener.
	 *
	 * @return The string representation of this listener.
	 */
	@Override
	public String toString() {
		String filterString = getFilter();
		if (filterString == null) {
			filterString = ""; //$NON-NLS-1$
		}
		return listener.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(listener)) //$NON-NLS-1$
				+ filterString;
	}

	/**
	 * Return the bundle context for the ListenerHook.
	 * 
	 * @return The context of the bundle which added the service listener.
	 * @see org.osgi.framework.hooks.service.ListenerHook.ListenerInfo#getBundleContext()
	 */
	@Override
	public BundleContext getBundleContext() {
		return context;
	}

	/**
	 * Return the filter string for the ListenerHook.
	 * 
	 * @return The filter string with which the listener was added. This may be
	 *         <code>null</code> if the listener was added without a filter.
	 * @see org.osgi.framework.hooks.service.ListenerHook.ListenerInfo#getFilter()
	 */
	@Override
	public String getFilter() {
		if (filter != null) {
			return filter.toString();
		}
		return getObjectClassFilterString(objectClass);
	}

	/**
	 * Return the state of the listener for this addition and removal life cycle.
	 * Initially this method will return <code>false</code> indicating the listener
	 * has been added but has not been removed. After the listener has been removed,
	 * this method must always return <code>true</code>.
	 *
	 * @return <code>false</code> if the listener has not been been removed,
	 *         <code>true</code> otherwise.
	 */
	@Override
	public boolean isRemoved() {
		return removed;
	}

	/**
	 * Mark the service listener registration as removed.
	 */
	void markRemoved() {
		removed = true;
	}

	/**
	 * Returns an objectClass filter string for the specified class name.
	 * 
	 * @return A filter string for the specified class name or <code>null</code> if
	 *         the specified class name is <code>null</code>.
	 */
	private static String getObjectClassFilterString(String className) {
		if (className == null) {
			return null;
		}
		return "(" + Constants.OBJECTCLASS + "=" + className + ")"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	}
}
