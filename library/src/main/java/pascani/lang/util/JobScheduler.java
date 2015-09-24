package pascani.lang.util;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import org.quartz.CronExpression;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;

/**
 * Simple wrapper implementation of the Quartz library to schedule jobs. This
 * class is intended to schedule event initializations using cron expressions,
 * however the method {@link #schedule(JobDetail, Trigger)} is provided in case
 * a more detailed scheduling is needed.
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class JobScheduler {

	private static Scheduler scheduler;

	protected static void initialize() throws SchedulerException {
		SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
		scheduler = schedFact.getScheduler();
		scheduler.start();
	}

	/**
	 * Add the given {@link JobDetail} to the Scheduler, and associate the given
	 * {@link Trigger} with it.
	 * 
	 * <p>
	 * If the given Trigger does not reference any <code>Job</code>, then it
	 * will be set to reference the Job passed with it into this method.
	 * </p>
	 * 
	 * @throws SchedulerException
	 *             if the Job or Trigger cannot be added to the Scheduler, or
	 *             there is an internal Scheduler error.
	 */
	public static void schedule(JobDetail jobDetail, Trigger trigger)
			throws SchedulerException {
		if (scheduler == null)
			initialize();

		scheduler.scheduleJob(jobDetail, trigger);
	}

	/**
	 * Creates an instance of {@link JobDetail} using the given {@link Job}
	 * class, and an instance of {@link Trigger} from the {@link CronExpression}
	 * object. Then, add the <code>JobDetail</code> to the scheduler, and
	 * associate the <code>Trigger</code> with it.
	 * 
	 * @throws SchedulerException
	 *             if the Job or Trigger cannot be added to the Scheduler, or
	 *             there is an internal Scheduler error.
	 */
	public static void schedule(Class<? extends Job> jobClass,
			CronExpression expression, JobDataMap data)
			throws SchedulerException {

		JobDetail jobDetail = newJob(jobClass).usingJobData(data).build();
		Trigger trigger = newTrigger().startNow()
				.withSchedule(cronSchedule(expression)).build();

		schedule(jobDetail, trigger);
	}

	/**
	 * Creates an instance of {@link JobDetail} using the given {@link Job}
	 * class, and an instance of {@link Trigger} from the {@link CronExpression}
	 * object. Then, add the <code>JobDetail</code> to the scheduler, and
	 * associate the <code>Trigger</code> with it.
	 * 
	 * @throws SchedulerException
	 *             if the Job or Trigger cannot be added to the Scheduler, or
	 *             there is an internal Scheduler error.
	 */
	public static void schedule(Class<? extends Job> jobClass,
			CronExpression expression) throws SchedulerException {

		JobDetail jobDetail = newJob(jobClass).build();
		Trigger trigger = newTrigger().startNow()
				.withSchedule(cronSchedule(expression)).build();

		schedule(jobDetail, trigger);
	}
	
	/**
	 * Halts the <code>Scheduler</code>'s firing of <code>{@link Trigger}s</code>,
     * and cleans up all resources associated with the Scheduler. Equivalent to
     * <code>shutdown(false)</code>.
     * 
     * <p>
     * The scheduler cannot be re-started.
     * </p>
     * 
     * @see Scheduler#shutdown(boolean)
	 */
	public static void shutdown() throws SchedulerException {
		scheduler.shutdown();
	}

}
