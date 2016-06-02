/*
 * Copyright © 2015 Universidad Icesi
 * 
 * This file is part of the Pascani project.
 * 
 * The Pascani project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * The Pascani project is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with The Pascani project. If not, see <http://www.gnu.org/licenses/>.
 */
package org.pascani.dsl.lib.util.events;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.pascani.dsl.lib.events.IntervalEvent;
import org.pascani.dsl.lib.util.CronConstant;
import org.pascani.dsl.lib.util.Exceptions;
import org.pascani.dsl.lib.util.JobScheduler;
import org.quartz.CronExpression;
import org.quartz.Job;
import org.quartz.JobDataMap;

/**
 * <b>Note</b>: DSL-only intended use
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class PeriodicEvent extends ManagedEvent<IntervalEvent> {

	protected CronExpression expression;
	protected List<Class<? extends Job>> classes;
	private List<Class<? extends Job>> temporal;

	public PeriodicEvent(String cronExpression) throws ParseException {
		this(new CronExpression(cronExpression));
	}
	
	public PeriodicEvent(CronConstant cronConstant) {
		this(cronConstant.expression());
	}
	
	public PeriodicEvent(CronExpression cronExpression) {
		super();
		this.classes = new ArrayList<Class<? extends Job>>();
		this.temporal = new ArrayList<Class<? extends Job>>();
		this.expression = cronExpression;
	}

	public CronExpression getExpression() {
		return this.expression;
	}

	@SuppressWarnings("unchecked")
	public void updateExpression(CronExpression expression) {
		List<Class<? extends Job>> tmp = this.classes;
		this.classes = new ArrayList<Class<? extends Job>>();
		this.expression = expression;
		for (Class<? extends Job> clazz : tmp) {
			unsubscribe(clazz);
			subscribe(clazz);
		}
	}
	
	public void subscribe(final Class<? extends Job>... jobClasses) {
		for (Class<? extends Job> jobClass : jobClasses) {
			if (!this.classes.contains(jobClass)) {
				this.classes.add(jobClass);
				JobDataMap data = new JobDataMap();
				data.put("expression", getExpression().getCronExpression());
				try {
					JobScheduler.schedule(jobClass, new CronExpression(
							getExpression().getCronExpression()), data);
				} catch (Exception e) {
					Exceptions.sneakyThrow(e);
				}
			}
		}
	}
	
	public boolean unsubscribe(final Class<? extends Job>... jobClasses) {
		boolean unsubscribed = false;
		for (Class<? extends Job> jobClass : jobClasses) {
			try {
				unsubscribed &= JobScheduler.unschedule(jobClass);
			} catch (Exception e) {
				Exceptions.sneakyThrow(e);
			}
		}
		return unsubscribed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.util.dsl.ManagedEvent#pause()
	 */
	@SuppressWarnings("unchecked")
	@Override public synchronized void pause() {
		if (isPaused())
			return;
		this.temporal = this.classes;
		this.classes = new ArrayList<Class<? extends Job>>();
		for (Class<? extends Job> clazz : this.temporal) {
			unsubscribe(clazz);
		}
		super.pause();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.util.dsl.ManagedEvent#resume()
	 */
	@SuppressWarnings("unchecked")
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
