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
 * along with The SLR Support Tools. If not, see <http://www.gnu.org/licenses/>.
 */
package pascani.lang.infrastructure;

import java.util.List;

import pascani.lang.Event;
import pascani.lang.Probe;
import pascani.lang.monitors.AbstractMonitor;
import pascani.lang.util.events.EventSet;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;

/**
 * Implementation of {@link Probe} for basic handling of events.
 * 
 * <p>
 * This {@link Probe} supports attending RPCs from monitors, through an RPC
 * queue, by providing a unique routing key; this routing key is not configured
 * directly in here, but in the RPC server instead.
 * </p>
 * 
 * @param <T>
 *            The type of events this probe handles
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class BasicProbe<T extends Event<?>> implements Probe<T> {

	/**
	 * An RPC server configured to serve external requests, for instance, from
	 * {@link AbstractMonitor} objects
	 */
	private final RpcServer server;

	/**
	 * A set holding the events as they are raised
	 */
	private final EventSet<T> events;

	/**
	 * Creates an instance of {@link Probe} with an empty set of events, and
	 * with a unique identifier within the RPC queue (hidden by the
	 * {@link RpcServer}).
	 * 
	 * @param server
	 *            A configured {@link RpcServer} instance
	 */
	public BasicProbe(final RpcServer server) {
		this.server = server;
		this.events = new EventSet<T>();

		// Start serving RPC requests
		this.server.setProbe(this);
		this.server.start();
	}

	/**
	 * Listens for events and records them into an {@link EventSet}
	 * 
	 * @param event
	 *            The event to record
	 */
	@Subscribe public void recordEvent(T event) {
		this.events.add(event);
	}

	public boolean cleanData(long timestamp) {
		return this.events.clean(timestamp).size() > 1;
	}

	public int count(long timestamp) {
		return this.events.filter(timestamp).size();
	}

	public int countAndClean(long timestamp) {
		return this.events.clean(timestamp).size();
	}

	public List<T> fetch(long timestamp) {
		return Lists.newArrayList(this.events.filter(timestamp));
	}

	public List<T> fetchAndClean(long timestamp) {
		return Lists.newArrayList(this.events.clean(timestamp));
	}

}
