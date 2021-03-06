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
package org.pascani.dsl.lib.util;

import java.text.ParseException;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.osoa.sca.annotations.Service;

/**
 * @author Miguel Jiménez - Initial contribution and API
 */
@Service
@Path("/events")
public interface MonitorEventsService {

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
	public void updateCronExpression(@PathParam("event") String eventName,
			@PathParam("expression") String cronExpression) throws ParseException;

	/**
	 * Gets the chronological expression of a periodic event
	 * 
	 * @param eventName
	 *            The name of the periodic event
	 * @return The chronological expression of the specified event
	 */
	@GET
	@Path("{event}/expression")
	@Produces({ MediaType.TEXT_PLAIN })
	public String getExpression(@PathParam("event") String eventName);

}
