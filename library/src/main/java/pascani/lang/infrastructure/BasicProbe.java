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

import java.io.Serializable;
import java.util.List;

import pascani.lang.Event;
import pascani.lang.Probe;
import pascani.lang.util.EventSet;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;

/**
 * Implementation of {@link Probe} for basic handling of events.
 * 
 * <p>
 * This {@link Probe} supports attending RPCs from external components, such as
 * {@link Monitor} instances, through an RPC queue, by providing a unique
 * routing key; this routing key is not configured directly in here, but in the
 * RPC server instead.
 * </p>
 * 
 * @param <T>
 *            The type of events this probe handles
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class BasicProbe<T extends Event<?>> implements Probe<T>,
		RpcRequestHandler {

	/**
	 * An RPC server configured to serve external requests, for instance, from
	 * {@link Monitor} objects
	 */
	private final RpcServer server;

	/**
	 * A set holding the events as they are raised
	 */
	private final EventSet<T> events;

	/**
	 * Creates an instance of {@link Probe} with an empty set of events, and
	 * with a unique identifier within the RPC queue (contained in the
	 * {@link RpcServer}).
	 * 
	 * @param server
	 *            A configured {@link RpcServer} instance
	 */
	public BasicProbe(final RpcServer server) {
		this.server = server;
		this.events = new EventSet<T>();

		// Start serving RPC requests
		this.server.setHandler(this);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.Probe#cleanData(long)
	 */
	public boolean cleanData(long timestamp) {
		return this.events.clean(timestamp).size() > 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.Probe#count(long)
	 */
	public int count(long timestamp) {
		return this.events.filter(timestamp).size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.Probe#countAndClean(long)
	 */
	public int countAndClean(long timestamp) {
		return this.events.clean(timestamp).size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.Probe#fetch(long)
	 */
	public List<T> fetch(long timestamp) {
		return Lists.newArrayList(this.events.filter(timestamp));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.Probe#fetchAndClean(long)
	 */
	public List<T> fetchAndClean(long timestamp) {
		return Lists.newArrayList(this.events.clean(timestamp));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.infrastructure.RpcRequestHandler#handle(pascani.lang.
	 * infrastructure.RpcRequest)
	 */
	public Serializable handle(RpcRequest request) {
		Serializable response = null;
		long timestamp = (Long) request.getParameter(0);

		if (request.operation().equals(RpcOperation.PROBE_CLEAN))
			response = this.cleanData(timestamp);
		else if (request.operation().equals(RpcOperation.PROBE_COUNT))
			response = this.count(timestamp);
		else if (request.operation().equals(RpcOperation.PROBE_COUNT_AND_CLEAN))
			response = this.countAndClean(timestamp);
		else if (request.operation().equals(RpcOperation.PROBE_FETCH))
			response = (Serializable) this.fetch(timestamp);
		else if (request.operation().equals(RpcOperation.PROBE_FETCH_AND_CLEAN))
			response = (Serializable) this.fetchAndClean(timestamp);

		return response;
	}

}
