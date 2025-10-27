/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.jobs;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Status;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.jobs.IJobStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.jobs.Job;

/**
 * Standard implementation of the IJobStatus interface.
 */
public class JobStatus extends Status implements IJobStatus {
	private final Job job;

	/**
	 * Creates a new job status with no interesting error code or exception.
	 */
	public JobStatus(int severity, Job job, String message) {
		super(severity, JobManager.PI_JOBS, 1, message, null);
		this.job = job;
	}

	@Override
	public Job getJob() {
		return job;
	}
}
