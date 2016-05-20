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

import org.osoa.sca.annotations.Scope;
import org.pascani.dsl.lib.PascaniRuntime;
import org.pascani.dsl.lib.events.ChangeEvent;

/**
 * @author Miguel Jiménez - Initial contribution and API
 */
@Scope("COMPOSITE")
public class ChangeEventDbMapper implements Runnable {

	/**
	 * The event (database) serializer
	 */
	private final EventSerializer serializer;

	/**
	 * Creates an instance using Rethinkdb
	 * 
	 * @throws Exception
	 * @see {@link EventSerializer#EventSerializer(String, String, Class, DbInterface)}
	 */
	public ChangeEventDbMapper() throws Exception {
		String exchange = PascaniRuntime.getEnvironment()
				.get("namespaces_exchange");
		String routingKey = "#"; // Accept all ChangeEvent events
		DbInterface db = new RethinkdbMapper();
		this.serializer = new EventSerializer(exchange, routingKey,
				ChangeEvent.class, db);
	}

	/**
	 * Adds a shutdown hook in order to shutdown the event serializer
	 */
	public void run() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override public void run() {
				try {
					serializer.shutdown();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
