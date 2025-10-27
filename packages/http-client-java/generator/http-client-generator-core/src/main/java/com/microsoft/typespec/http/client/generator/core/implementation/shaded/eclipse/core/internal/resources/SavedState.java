/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
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
 *     Christoph Läubrich - Issue #77 - SaveManager access the ResourcesPlugin.getWorkspace at init phase
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.events.ResourceDelta;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.events.ResourceDeltaFactory;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.utils.Policy;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.watson.ElementTree;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResourceChangeEvent;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.IResourceChangeListener;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources.ISavedState;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * Standard implementation of the ISavedState interface.
 */
public class SavedState implements ISavedState {
	ElementTree oldTree;
	ElementTree newTree;
	SafeFileTable fileTable;
	String pluginId;
	Workspace workspace;

	SavedState(Workspace workspace, String pluginId, ElementTree oldTree, ElementTree newTree) throws CoreException {
		this.workspace = workspace;
		this.pluginId = pluginId;
		this.newTree = newTree;
		this.oldTree = oldTree;
		this.fileTable = restoreFileTable();
	}

	void forgetTrees() {
		newTree = null;
		oldTree = null;
		workspace.saveManager.clearDeltaExpiration(pluginId);
	}

    protected SafeFileTable getFileTable() {
		return fileTable;
	}

	protected SafeFileTable restoreFileTable() throws CoreException {
		if (fileTable == null) {
			fileTable = new SafeFileTable(pluginId, workspace);
		}
		return fileTable;
	}

	@Override
	public IPath lookup(IPath file) {
		return getFileTable().lookup(file);
	}

	@Override
	public IPath[] getFiles() {
		return getFileTable().getFiles();
	}

	@Override
	public void processResourceChangeEvents(IResourceChangeListener listener) {
		try {
			final ISchedulingRule rule = workspace.getRoot();
			try {
				workspace.prepareOperation(rule, null);
				if (oldTree == null || newTree == null) {
					return;
				}
				workspace.beginOperation(true);
				ResourceDelta delta = ResourceDeltaFactory.computeDelta(workspace, oldTree, newTree, IPath.ROOT, -1);
				forgetTrees(); // free trees to prevent memory leak
				workspace.getNotificationManager().broadcastChanges(listener, IResourceChangeEvent.POST_BUILD, delta);
			} finally {
				workspace.endOperation(rule, false);
			}
		} catch (CoreException e) {
			// this is unlikely to happen, so, just log it
			Policy.log(e);
		}
	}
}
