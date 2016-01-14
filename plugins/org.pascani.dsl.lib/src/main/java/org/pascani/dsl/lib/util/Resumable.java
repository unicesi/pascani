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
package org.pascani.dsl.lib.util;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.osoa.sca.annotations.Service;
import org.pascani.dsl.lib.Probe;
import org.pascani.dsl.lib.infrastructure.Monitor;
import org.pascani.dsl.lib.infrastructure.Namespace;

/**
 * Utility interface to define resumable behavior in classes within the
 * monitoring infrastructure, such as instances of {@link Probe},
 * {@link Namespace} and {@link Monitor}.
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
@Service
public interface Resumable {

	/**
	 * Temporarily stops the regular behavior of this object
	 */
	@POST
	@Path("/pause")
	public void pause();

	/**
	 * Resumes the regular behavior of this object
	 */
	@POST
	@Path("/resume")
	public void resume();

	/**
	 * 
	 * @return {@code true} in case this object is in stopped state, or
	 *         {@code false} in the opposite case.
	 */
	@POST
	@Path("/paused")
	@Produces("text/plain")
	public boolean isPaused();

}
