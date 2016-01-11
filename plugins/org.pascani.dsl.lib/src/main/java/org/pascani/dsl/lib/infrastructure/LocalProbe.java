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
package org.pascani.dsl.lib.infrastructure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.pascani.dsl.lib.Event;
import org.pascani.dsl.lib.PascaniRuntime;
import org.pascani.dsl.lib.Probe;
import org.pascani.dsl.lib.infrastructure.rabbitmq.EndPoint;
import org.pascani.dsl.lib.infrastructure.rabbitmq.RabbitMQRpcServer;

/**
 * A simple implementation of {@link Probe} hiding technical details of the
 * connection to the RabbitMQ server.
 * 
 * <p>
 * This class acts as a proxy of a {@link BasicProbe} instance, delegating the
 * recording of events and the processing of RPC requests.
 * </p>
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class LocalProbe implements Probe {

	/**
	 * A RabbitMQ end point (a connection to the server)
	 */
	protected final EndPoint endPoint;

	/**
	 * The RPC server listening for RPC requests
	 */
	protected final RpcServer server;

	/**
	 * The {@link Probe} instance to record events and process RPC requests
	 */
	protected final BasicProbe probe;

	/**
	 * The context in which this probe is used
	 */
	protected final PascaniRuntime.Context context;

	/**
	 * Creates an instance connected to a RabbitMQ server, making the recorded
	 * events reachable for external components.
	 * 
	 * <p>
	 * External components may use a {@link ProbeProxy} instance to access
	 * recorded events from this probe.
	 * </p>
	 * 
	 * @param routingKey
	 *            A unique name among all the {@link Probe} instances. This name
	 *            is necessary for external components to send RPC requests to
	 *            this probe.
	 * @param context
	 *            The context in which this probe is used
	 * @throws Exception
	 *             If something bad happens. Check exceptions in
	 *             {@link EndPoint#EndPoint(String)}
	 */
	public LocalProbe(final String routingKey,
			final PascaniRuntime.Context context) throws Exception {
		
		this.context = context;
		this.endPoint = new EndPoint();
		createQueue(routingKey);
		
		this.server = new RabbitMQRpcServer(endPoint, routingKey, this.context);
		this.probe = new BasicProbe(server);

		registerProbeAsListener();
	}

	/**
	 * Create the corresponding queue, and then create a binding between the
	 * queue and the probes exchange
	 */
	private void createQueue(final String routingKey) throws IOException {
		String queue = routingKey;
		String exchange = PascaniRuntime.getEnvironment()
				.get("probes_exchange");

		this.endPoint.channel().queueDeclare(queue, false, true, true, null);
		this.endPoint.channel().queueBind(queue, exchange, routingKey);
	}

	/**
	 * Registers the probe as an event listener
	 */
	private void registerProbeAsListener() {
		PascaniRuntime.getRuntimeInstance(this.context).registerEventListener(
				this.probe);
	}

	/**
	 * Establishes a set of event classes to be recorded by this probe
	 * 
	 * @param acceptedTypes
	 *            The array of classes implementing {@link Event}
	 */
	public void acceptOnly(final Class<? extends Event<?>>... acceptedTypes) {
		this.probe.acceptOnly(acceptedTypes);
	}

	/**
	 * Records an event
	 * 
	 * @param event
	 *            The event to record
	 */
	public boolean recordEvent(final Event<?> event) {
		return this.probe.recordEvent(event);
	}

	public boolean cleanData(final long start, final long end) {
		return cleanData(start, end, new ArrayList<Class<? extends Event<?>>>());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.Probe#cleanData(long, long, java.util.List)
	 */
	public boolean cleanData(final long start, final long end,
			final List<Class<? extends Event<?>>> eventTypes) {
		return this.probe.cleanData(start, end, eventTypes);
	}

	public int count(final long start, final long end) {
		return count(start, end, new ArrayList<Class<? extends Event<?>>>());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.Probe#count(long, long, java.util.List)
	 */
	public int count(final long start, final long end,
			final List<Class<? extends Event<?>>> eventTypes) {
		return this.probe.count(start, end, eventTypes);
	}

	public int countAndClean(final long start, final long end) {
		return countAndClean(start, end,
				new ArrayList<Class<? extends Event<?>>>());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.Probe#countAndClean(long, long, java.util.List)
	 */
	public int countAndClean(final long start, final long end,
			final List<Class<? extends Event<?>>> eventTypes) {
		return this.probe.countAndClean(start, end, eventTypes);
	}

	public List<Event<?>> fetch(final long start, final long end) {
		return fetch(start, end, new ArrayList<Class<? extends Event<?>>>());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.Probe#fetch(long, long, java.util.List)
	 */
	public List<Event<?>> fetch(final long start, final long end,
			final List<Class<? extends Event<?>>> eventTypes) {
		return this.probe.fetch(start, end, eventTypes);
	}

	public List<Event<?>> fetchAndClean(final long start, final long end) {
		return fetchAndClean(start, end,
				new ArrayList<Class<? extends Event<?>>>());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.Probe#fetchAndClean(long, long, java.util.List)
	 */
	public List<Event<?>> fetchAndClean(final long start, final long end,
			final List<Class<? extends Event<?>>> eventTypes) {
		return this.probe.fetchAndClean(start, end, eventTypes);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.util.Resumable#pause()
	 */
	public void pause() {
		this.probe.pause();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.util.Resumable#resume()
	 */
	public void resume() {
		this.probe.resume();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.util.Resumable#isPaused()
	 */
	public boolean isPaused() {
		return this.probe.isPaused();
	}

	/**
	 * Shutdowns connections
	 * 
	 * @throws Exception
	 *             If something bad happens!
	 */
	public void shutdown() throws Exception {
		// The end point is shared among all connections to RabbitMQ, shutting
		// it down does all the job
		this.endPoint.close();
	}

}
