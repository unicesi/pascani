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
package org.pascani.dsl.dbmapper;

import java.util.Map;

import org.pascani.dsl.lib.Event;

/**
 * @author Miguel Jiménez - Initial contribution and API
 */
public interface DbInterface {

	/**
	 * Stores the given event in the database
	 * 
	 * @param event
	 *            The {@link Event} to store
	 * @throws Exception
	 *             If something bad happens!
	 */
	public <T extends Event<?>> void save(T event) throws Exception;

	/**
	 * Retrieves an event from the database
	 * 
	 * @param params
	 *            An array of parameters necessary to retrieve the event from
	 *            the database
	 * @return the retrieved event or null if it is not found
	 * @throws Exception
	 *             If something bad happens!
	 */
	public <T extends Event<?>> T retrieve(Map<String, String> params)
			throws Exception;

	/**
	 * Opens connection with the database
	 * 
	 * @throws Exception
	 *             If something bad happens!
	 */
	public void openConnection() throws Exception;

	/**
	 * Closes connection with the database
	 * 
	 * @throws Exception
	 *             If something bad happens!
	 */
	public void closeConnection() throws Exception;

}
