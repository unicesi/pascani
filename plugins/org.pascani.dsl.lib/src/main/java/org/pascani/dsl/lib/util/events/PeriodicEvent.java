/*
 * Copyright © 2015 Universidad Icesi
 * 
 * This file is part of the Pascani library.
 * 
 * The Pascani library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * The Pascani library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with The Pascani library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.pascani.dsl.lib.util.events;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.pascani.dsl.lib.util.JobScheduler;
import org.quartz.CronExpression;
import org.quartz.Job;
import org.quartz.JobDataMap;

/**
 * <b>Note</b>: DSL-only intended use
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class PeriodicEvent extends ManagedEvent {

	protected CronExpression expression;
	protected List<Class<? extends Job>> classes;
	private List<Class<? extends Job>> temporal;

	public PeriodicEvent(String cronExpression) throws ParseException {
		this(new CronExpression(cronExpression));
	}
	
	public PeriodicEvent(CronExpression cronExpression) {
		this.classes = new ArrayList<Class<? extends Job>>();
		this.temporal = new ArrayList<Class<? extends Job>>();
		this.expression = cronExpression;
	}

	public CronExpression getExpression() {
		return this.expression;
	}

	public void updateExpression(CronExpression expression) {
		this.expression = expression;
		for (Class<? extends Job> clazz : this.classes) {
			unsubscribe(clazz);
			subscribe(clazz);
		}
	}

	public void subscribe(final Class<? extends Job> jobClass) {
		if (!this.classes.contains(jobClass)) {
			this.classes.add(jobClass);
			JobDataMap data = new JobDataMap();
			data.put("expression", getExpression().getCronExpression());
			try {
				JobScheduler.schedule(jobClass,
						new CronExpression(getExpression().getCronExpression()),
						data);
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}

	public boolean unsubscribe(final Class<? extends Job> jobClass) {
		this.classes.remove(jobClass);
		try {
			return JobScheduler.unschedule(jobClass);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.util.dsl.ManagedEvent#pause()
	 */
	@Override public synchronized void pause() {
		if (isPaused())
			return;
		for (Class<? extends Job> clazz : this.classes) {
			this.temporal.add(clazz);
			unsubscribe(clazz);
		}
		super.pause();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.util.dsl.ManagedEvent#resume()
	 */
	@Override public synchronized void resume() {
		if (!isPaused())
			return;
		for (Class<? extends Job> clazz : this.temporal) {
			subscribe(clazz);
		}
		this.temporal.clear();
		super.resume();
	}

}
