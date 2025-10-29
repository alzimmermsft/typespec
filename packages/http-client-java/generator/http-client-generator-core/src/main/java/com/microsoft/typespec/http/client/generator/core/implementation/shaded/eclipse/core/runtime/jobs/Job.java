/*******************************************************************************
 * Copyright (c) 2003, 2018 IBM Corporation and others.
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
 *     Thirumala Reddy Mutchukota (thirumala@google.com) -
 *     		Bug 432049, JobGroup API and implementation
 *     		Bug 105821, Support for Job#join with timeout and progress monitor
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.jobs;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.jobs.InternalJob;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.jobs.JobManager;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.*;

/**
 * Jobs are units of runnable work that can be scheduled to be run with the job
 * manager. Once a job has completed, it can be scheduled to run again (jobs are
 * reusable).
 * <p>
 * Jobs have a state that indicates what they are currently doing. When constructed,
 * jobs start with a state value of <code>NONE</code>. When a job is scheduled
 * to be run, it moves into the <code>WAITING</code> state. When a job starts
 * running, it moves into the <code>RUNNING</code> state. When execution finishes
 * (either normally or through cancelation), the state changes back to
 * <code>NONE</code>.
 * </p><p>
 * A job can also be in the <code>SLEEPING</code> state. This happens if a user
 * calls Job.sleep() on a waiting job, or if a job is scheduled to run after a specified
 * delay. Only jobs in the <code>WAITING</code> state can be put to sleep.
 * Sleeping jobs can be woken at any time using Job.wakeUp(), which will put the
 * job back into the <code>WAITING</code> state.
 * </p><p>
 * Jobs can be assigned a priority that is used as a hint about how the job should
 * be scheduled. There is no guarantee that jobs of one priority will be run before
 * all jobs of lower priority. The javadoc for the various priority constants provide
 * more detail about what each priority means. By default, jobs start in the
 * <code>LONG</code> priority class.
 *
 * @see IJobManager
 * @since 3.0
 */
public abstract class Job extends InternalJob {

    /**
     * Job status return value that is used to indicate asynchronous job completion.
     * 
     * @see Job#run(IProgressMonitor)
     * @see Job#done(IStatus)
     */
    public static final IStatus ASYNC_FINISH = new Status(IStatus.OK, JobManager.PI_JOBS, 1, "", null);//$NON-NLS-1$

    /* Job priorities */
    /**
     * Job priority constant (value 10) for interactive jobs.
     * Interactive jobs generally have priority over all other jobs.
     * Interactive jobs should be either fast running or very low on CPU
     * usage to avoid blocking other interactive jobs from running.
     *
     * @see #getPriority()
     * @see #setPriority(int)
     * @see #run(IProgressMonitor)
     */
    public static final int INTERACTIVE = 10;
    /**
     * Job priority constant (value 20) for short background jobs.
     * Short background jobs are jobs that typically complete within a second,
     * but may take longer in some cases. Short jobs are given priority
     * over all other jobs except interactive jobs.
     *
     * @see #getPriority()
     * @see #setPriority(int)
     * @see #run(IProgressMonitor)
     */
    public static final int SHORT = 20;
    /**
     * Job priority constant (value 30) for long-running background jobs.
     *
     * @see #getPriority()
     * @see #setPriority(int)
     * @see #run(IProgressMonitor)
     */
    public static final int LONG = 30;
    /**
     * Job priority constant (value 40) for build jobs. Build jobs are
     * generally run after all other background jobs complete.
     *
     * @see #getPriority()
     * @see #setPriority(int)
     * @see #run(IProgressMonitor)
     */
    public static final int BUILD = 40;

    /**
     * Job priority constant (value 50) for decoration jobs.
     * Decoration jobs have lowest priority. Decoration jobs generally
     * compute extra information that the user may be interested in seeing
     * but is generally not waiting for.
     *
     * @see #getPriority()
     * @see #setPriority(int)
     * @see #run(IProgressMonitor)
     */
    public static final int DECORATE = 50;
    /**
     * Job state code (value 0) indicating that a job is not
     * currently sleeping, waiting, or running (i.e., the job manager doesn't know
     * anything about the job).
     *
     * @see #getState()
     */
    public static final int NONE = 0;
    /**
     * Job state code (value 1) indicating that a job is sleeping.
     *
     * @see #run(IProgressMonitor)
     * @see #getState()
     */
    public static final int SLEEPING = 0x01;
    /**
     * Job state code (value 2) indicating that a job is waiting to run.
     *
     * @see #getState()
     */
    public static final int WAITING = 0x02;
    /**
     * Job state code (value 4) indicating that a job is currently running
     *
     * @see #getState()
     */
    public static final int RUNNING = 0x04;

    /**
     * Returns the job manager.
     *
     * @return the job manager
     * @since org.eclipse.core.jobs 3.2
     */
    public static final IJobManager getJobManager() {
        return manager;
    }

    /**
     * Creates a new Job that will execute the provided function when it runs.
     *
     * Prefer using {@link Job#create(String, ICoreRunnable)} as this does not
     * require to call done on the monitor and relies on OperationCanceledException
     *
     * @param name The name of the job
     * @param function The function to execute
     * @return A job that encapsulates the provided function
     * @see IJobFunction
     * @since 3.6
     */
    public static Job create(String name, final IJobFunction function) {
        return new Job(name) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                return function.run(monitor);
            }
        };
    }

    /**
     * Creates a new Job that will execute the provided runnable when it runs.
     *
     * @param name
     * the name of the job
     * @param runnable
     * the runnable to execute
     * @return a job that encapsulates the provided runnable
     * @see ICoreRunnable
     * @since 3.8
     */
    public static Job create(String name, final ICoreRunnable runnable) {
        return new Job(name) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    runnable.run(monitor);
                } catch (CoreException e) {
                    IStatus st = e.getStatus();
                    return new Status(st.getSeverity(), st.getPlugin(), st.getCode(), st.getMessage(), e);
                }
                return Status.OK_STATUS;
            }
        };
    }

    /**
     * Creates a new job with the specified name. The job name is a
     * human-readable value that is displayed to users. The name does not need
     * to be unique, but it must not be <code>null</code>.
     *
     * @param name the name of the job.
     */
    public Job(String name) {
        super(name);
    }

    /**
     * Returns whether this job belongs to the given family. Job families are
     * represented as objects that are not interpreted or specified in any way
     * by the job manager. Thus, a job can choose to belong to any number of
     * families.
     * <p>
     * Clients may override this method. This default implementation always returns
     * <code>false</code>. Overriding implementations must return <code>false</code>
     * for families they do not recognize.
     * </p>
     *
     * @param family the job family identifier
     * @return <code>true</code> if this job belongs to the given family, and
     * <code>false</code> otherwise.
     */
    @Override
    public boolean belongsTo(Object family) {
        return false;
    }

    /**
     * A hook method indicating that this job is running and {@link #cancel()}
     * is being called for the first time.
     * <p>
     * Subclasses may override this method to perform additional work when
     * a cancelation request is made. This default implementation does nothing.
     * 
     * @since 3.3
     */
    @Override
    protected void canceling() {
        // default implementation does nothing
    }

    /**
     * Executes this job. Returns the result of the execution.
     * <p>
     * The provided monitor can be used to report progress and respond to
     * cancellation. If the progress monitor has been canceled, the job
     * should finish its execution at the earliest convenience and return a result
     * status of severity {@link IStatus#CANCEL}. The singleton
     * cancel status {@link Status#CANCEL_STATUS} can be used for
     * this purpose. The monitor is only valid for the duration of the invocation
     * of this method.
     * <p>
     * This method must not be called directly by clients. Clients should call
     * <code>schedule</code>, which will in turn cause this method to be called.
     * <p>
     * Jobs can optionally finish their execution asynchronously (in another thread) by
     * returning a result status of {@link #ASYNC_FINISH}. Jobs that finish
     * asynchronously <b>must</b> specify the execution thread by calling
     * <code>setThread</code>, and must indicate when they are finished by calling
     * the method <code>done</code>.
     *
     * @param monitor the monitor to be used for reporting progress and
     * responding to cancelation. The monitor is never <code>null</code>
     * @return resulting status of the run. The result must not be <code>null</code>
     * @see #ASYNC_FINISH
     * @see #done(IStatus)
     */
    @Override
    protected abstract IStatus run(IProgressMonitor monitor);

    /**
     * Returns whether this job should be run.
     * If <code>false</code> is returned, this job will be discarded by the job manager
     * without running.
     * <p>
     * This method is called immediately prior to calling the job's
     * run method, so it can be used for last minute precondition checking before
     * a job is run. This method must not attempt to schedule or change the
     * state of any other job.
     * </p><p>
     * Clients may override this method. This default implementation always returns
     * <code>true</code>.
     * </p>
     *
     * @return <code>true</code> if this job should be run
     * and <code>false</code> otherwise
     */
    public boolean shouldRun() {
        return true;
    }

    /**
     * Returns whether this job should be scheduled.
     * If <code>false</code> is returned, this job will be discarded by the job manager
     * without being added to the queue.
     * <p>
     * This method is called immediately prior to adding the job to the waiting job
     * queue.,so it can be used for last minute precondition checking before
     * a job is scheduled.
     * </p><p>
     * Clients may override this method. This default implementation always returns
     * <code>true</code>.
     * </p>
     *
     * @return <code>true</code> if the job manager should schedule this job
     * and <code>false</code> otherwise
     */
    @Override
    public boolean shouldSchedule() {
        return true;
    }
}
