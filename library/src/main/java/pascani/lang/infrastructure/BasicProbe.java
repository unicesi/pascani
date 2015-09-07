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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pascani.lang.Event;
import pascani.lang.Probe;
import pascani.lang.util.EventSet;

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
	 * A map holding the events as they are raised, grouped by event type
	 */
	private final Map<Class<? extends Event<?>>, EventSet<T>> events;

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

		/*
		 * A type restriction on the map is not necessary, as the only method
		 * that adds new events it type safe (restricted to T). Also, it could
		 * cause cast errors, see http://stackoverflow.com/a/13974262
		 */
		this.events = new HashMap<Class<? extends Event<?>>, EventSet<T>>();

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
	@Subscribe
	public boolean recordEvent(T event) {

		@SuppressWarnings("unchecked")
		Class<? extends Event<?>> clazz = (Class<? extends Event<?>>) event
				.getClass();

		synchronized(this.events) {
			if (this.events.get(clazz) == null)
				this.events.put(clazz, new EventSet<T>());

			return this.events.get(clazz).add(event);
		}
	}

	private List<Class<? extends Event<?>>> types(
			List<Class<? extends Event<?>>> eventTypes) {
		List<Class<? extends Event<?>>> types = eventTypes;

		if (types == null || types.isEmpty()) {
			types = new ArrayList<Class<? extends Event<?>>>();
			for (Class<? extends Event<?>> t : this.events.keySet()) {
				types.add(t);
			}
		}

		return types;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.Probe#cleanData(long, long, java.util.List)
	 */
	public boolean cleanData(final long start, final long end,
			List<Class<? extends Event<?>>> eventTypes) {

		boolean removed = false;

		for (Class<? extends Event<?>> clazz : types(eventTypes)) {
			if (this.events.containsKey(clazz)) {
				removed = removed
						|| this.events.get(clazz).clean(start, end).size() > 1;
			}
		}

		return removed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.Probe#count(long, long, java.util.List)
	 */
	public int count(final long start, final long end,
			List<Class<? extends Event<?>>> eventTypes) {

		int count = 0;

		for (Class<? extends Event<?>> clazz : types(eventTypes)) {
			if (this.events.containsKey(clazz)) {
				count += this.events.get(clazz).filter(start, end).size();
			}
		}

		return count;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.Probe#countAndClean(long, long, java.util.List)
	 */
	public int countAndClean(final long start, final long end,
			List<Class<? extends Event<?>>> eventTypes) {

		int count = 0;

		for (Class<? extends Event<?>> clazz : types(eventTypes)) {
			if (this.events.containsKey(clazz)) {
				count += this.events.get(clazz).clean(start, end).size();
			}
		}

		return count;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.Probe#fetch(long, long, java.util.List)
	 */
	public List<T> fetch(final long start, final long end,
			List<Class<? extends Event<?>>> eventTypes) {

		List<T> fetched = new ArrayList<T>();

		for (Class<? extends Event<?>> clazz : types(eventTypes)) {
			if (this.events.containsKey(clazz)) {
				fetched.addAll(this.events.get(clazz).filter(start, end));
			}
		}

		return fetched;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.Probe#fetchAndClean(long, long, java.util.List)
	 */
	public List<T> fetchAndClean(final long start, final long end,
			List<Class<? extends Event<?>>> eventTypes) {
		List<T> fetched = new ArrayList<T>();

		for (Class<? extends Event<?>> clazz : types(eventTypes)) {
			if (this.events.containsKey(clazz)) {
				fetched.addAll(this.events.get(clazz).clean(start, end));
			}
		}

		return fetched;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.infrastructure.RpcRequestHandler#handle(pascani.lang.
	 * infrastructure.RpcRequest)
	 */
	public Serializable handle(RpcRequest request) {
		Serializable response = null;

		long start = (Long) request.getParameter(0);
		long end = (Long) request.getParameter(1);

		@SuppressWarnings("unchecked")
		List<Class<? extends Event<?>>> eventTypes = (List<Class<? extends Event<?>>>) request
				.getParameter(2);

		if (request.operation().equals(RpcOperation.PROBE_CLEAN))
			response = this.cleanData(start, end, eventTypes);
		else if (request.operation().equals(RpcOperation.PROBE_COUNT))
			response = this.count(start, end, eventTypes);
		else if (request.operation().equals(RpcOperation.PROBE_COUNT_AND_CLEAN))
			response = this.countAndClean(start, end, eventTypes);
		else if (request.operation().equals(RpcOperation.PROBE_FETCH))
			response = (Serializable) this.fetch(start, end, eventTypes);
		else if (request.operation().equals(RpcOperation.PROBE_FETCH_AND_CLEAN))
			response = (Serializable) this
					.fetchAndClean(start, end, eventTypes);

		return response;
	}

}
