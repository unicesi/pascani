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
 * @author Miguel Jiménez - Initial contribution and API
 */
public class BasicProbe implements Probe<Event<?>>,
		RpcRequestHandler {

	/**
	 * An RPC server configured to serve external requests, for instance, from
	 * {@link Monitor} objects
	 */
	private final RpcServer server;

	/**
	 * A map holding the events as they are raised, grouped by event type
	 */
	private final Map<String, EventSet<Event<?>>> events;

	protected Class<? extends Event<?>>[] acceptedTypes;

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
		this.events = new HashMap<String, EventSet<Event<?>>>();

		// Start serving RPC requests
		this.server.setHandler(this);
		this.server.start();
	}

	/**
	 * Establishes a set of event classes to be recorded by this probe
	 * 
	 * @param acceptedTypes
	 *            The array of classes implementing {@link Event}
	 */
	public void acceptOnly(final Class<? extends Event<?>>... acceptedTypes) {
		this.acceptedTypes = acceptedTypes;
	}

	/**
	 * Listens for events and records them into an {@link EventSet}. When the
	 * {@link #acceptOnly(Class...)} method has been invoked, a check will be
	 * performed each time an event is received; if the event is instance of one
	 * of the accepted classes, it will be recorded and {@code true} is
	 * returned; otherwise, false is returned.
	 * 
	 * @param event
	 *            The event to record
	 */
	@Subscribe public boolean recordEvent(final Event<?> event) {
		boolean r = false;
		boolean accept = isAcceptedEvent(event);

		if (accept) {
			String key = event.getClass().getCanonicalName();

			synchronized (this.events) {
				if (this.events.get(key) == null)
					this.events.put(key, new EventSet<Event<?>>());

				r = this.events.get(key).add(event);
			}
		}

		return r;
	}

	protected boolean isAcceptedEvent(final Event<?> event) {
		boolean accepted = this.acceptedTypes == null;

		if (!accepted) {
			for (int i = 0; i < this.acceptedTypes.length && !accepted; i++) {
				accepted = this.acceptedTypes[i].isInstance(event);
			}
		}

		return accepted;
	}

	protected List<String> types(
			final List<Class<? extends Event<?>>> eventTypes) {

		List<String> types = new ArrayList<String>();

		if (eventTypes == null || eventTypes.isEmpty()) {
			types.addAll(this.events.keySet());
		} else {
			for (Class<? extends Event<?>> t : eventTypes)
				types.add(t.getCanonicalName());
		}

		return types;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.Probe#cleanData(long, long, java.util.List)
	 */
	public boolean cleanData(final long start, final long end,
			final List<Class<? extends Event<?>>> eventTypes) {

		boolean removed = false;

		for (String clazz : types(eventTypes)) {
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
			final List<Class<? extends Event<?>>> eventTypes) {

		int count = 0;

		for (String clazz : types(eventTypes)) {
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
			final List<Class<? extends Event<?>>> eventTypes) {

		int count = 0;

		for (String clazz : types(eventTypes)) {
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
	public List<Event<?>> fetch(final long start, final long end,
			final List<Class<? extends Event<?>>> eventTypes) {

		List<Event<?>> fetched = new ArrayList<Event<?>>();

		for (String clazz : types(eventTypes)) {
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
	public List<Event<?>> fetchAndClean(final long start, final long end,
			final List<Class<? extends Event<?>>> eventTypes) {
		List<Event<?>> fetched = new ArrayList<Event<?>>();

		for (String clazz : types(eventTypes)) {
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
	public Serializable handle(final RpcRequest request) {
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
	
	public void shutdown() throws Exception {
		this.server.shutdown();
	}

}
