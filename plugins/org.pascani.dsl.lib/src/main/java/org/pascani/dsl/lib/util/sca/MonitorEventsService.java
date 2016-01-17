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
package org.pascani.dsl.lib.util.sca;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.osoa.sca.annotations.Service;
import org.pascani.dsl.lib.Event;
import org.pascani.dsl.lib.util.events.EventObserver;
import org.quartz.Job;

/**
 * @author Miguel Jiménez - Initial contribution and API
 */
@Service
@Path("/events")
public interface MonitorEventsService {

	/**
	 * Subscribes the given observer to the specified event
	 * 
	 * @param eventName
	 *            The name of the event
	 * @param eventObserver
	 *            An instance of {@link EventObserver}
	 */
	public <T extends Event<?>> void subscribe(String eventName,
			EventObserver<T> eventObserver);

	/**
	 * Subscribes the given {@link Job} class to the specified periodic event
	 * 
	 * @param eventName
	 *            The name of the periodic event
	 * @param jobClass
	 *            A class to handle the occurrence of the periodic event
	 */
	public void subscribe(String eventName, Class<? extends Job> jobClass);

	/**
	 * Updates the chronological expression of a periodic event
	 * 
	 * @param eventName
	 *            The name of the periodic event
	 * @param cronExpression
	 *            The new chronological expression
	 */
	@PUT 
	@Path("{event}/expression/{expression}")
	public void updateCronExpression(
			@PathParam("event") String eventName,
			@PathParam("expression") String cronExpression);

}
