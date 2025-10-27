/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.internal.core.search.processing;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IProgressMonitor;

public interface IJob {

	/* Waiting policies */
	int ForceImmediate = 1;
	int CancelIfNotReady = 2;
	int WaitUntilReady = 3;

	/* Job's result */
	boolean FAILED = false;
	boolean COMPLETE = true;

	/**
	 * Answer true if the job belongs to a given family (tag)
	 */
    boolean belongsTo(String jobFamily);
	/**
	 * Asks this job to cancel its execution. The cancellation
	 * can take an undertermined amount of time.
	 */
    void cancel();
	/**
	 * Ensures that this job is ready to run.
	 */
    void ensureReadyToRun();
	/**
	 * Execute the current job, answer whether it was successful.
	 */
    boolean execute(IProgressMonitor progress);

	/**
	 * Returns this job's family
	 */
    String getJobFamily();

	/**
	 * Answers if we need some sleep after index write operations. Default implementation returns {@code false}.
	 *
	 * @return true if the job manager should sleep a bit after this job is done to avoid IO tasks overloading OS (which
	 *         could cause UI freezes etc).
	 */
	default boolean waitNeeded() {
		return false;
	}
}
