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
import pascani.lang.infrastructure.rabbitmq.EndPoint;
import pascani.lang.infrastructure.rabbitmq.RabbitMQRpcServer;

/**
 * A simple implementation of {@link Probe} hiding technical details of the
 * connection to the RabbitMQ server.
 * 
 * <p>
 * This class acts as a proxy of a {@link BasicProbe} instance, delegating the
 * recording of events and the processing of RPC requests.
 * </p>
 * 
 * @param <T>
 *            The type of events this probe handles
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class CustomProbe<T extends Event<?>> implements Probe<T> {

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
	protected final BasicProbe<T> probe;

	/**
	 * The context in which this probe is used
	 */
	protected final pascani.lang.Runtime.Context context;

	/**
	 * Creates an instance connected to a RabbitMQ server, making the recorded
	 * events reachable for external components.
	 * 
	 * <p>
	 * External components may use a {@link ProbeProxy} instance to access
	 * recorded events from this probe.
	 * </p>
	 * 
	 * @param uri
	 *            The RabbitMQ connection URI
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
	public CustomProbe(final String uri, final String routingKey,
			final pascani.lang.Runtime.Context context) throws Exception {

		this.context = context;
		this.endPoint = new EndPoint(uri);

		// Create the corresponding queue, and then create a binding between the
		// queue and the probes exchange
		String queue = routingKey;
		String exchange = pascani.lang.Runtime.getRuntimeInstance(this.context)
				.getEnvironment().get("probes_exchange");

		this.endPoint.channel().queueDeclare(queue, false, true, true, null);
		this.endPoint.channel().queueBind(queue, exchange, routingKey);

		this.server = new RabbitMQRpcServer(endPoint, routingKey, this.context);
		this.probe = new BasicProbe<T>(server);

		registerProbeAsListener();
	}

	/**
	 * Registers the probe as an event listener
	 */
	private void registerProbeAsListener() {
		pascani.lang.Runtime.getRuntimeInstance(this.context)
				.registerEventListener(this.probe);
	}

	/**
	 * Records an event
	 * 
	 * @param event
	 *            The event to record
	 */
	public void recordEvent(T event) {
		this.probe.recordEvent(event);
	}
	
	public boolean cleanData(final long start, final long end) {
		return cleanData(start, end, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.Probe#cleanData(long, long, java.util.List)
	 */
	public boolean cleanData(final long start, final long end,
			final List<Class<T>> eventTypes) {
		return this.probe.cleanData(start, end, eventTypes);
	}
	
	public int count(final long start, final long end) {
		return count(start, end, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.Probe#count(long, long, java.util.List)
	 */
	public int count(final long start, final long end,
			final List<Class<T>> eventTypes) {
		return this.probe.count(start, end, eventTypes);
	}
	
	public int countAndClean(final long start, final long end) {
		return countAndClean(start, end, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.Probe#countAndClean(long, long, java.util.List)
	 */
	public int countAndClean(final long start, final long end,
			final List<Class<T>> eventTypes) {
		return this.probe.countAndClean(start, end, eventTypes);
	}
	
	public List<T> fetch(final long start, final long end) {
		return fetch(start, end, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.Probe#fetch(long, long, java.util.List)
	 */
	public List<T> fetch(final long start, final long end,
			final List<Class<T>> eventTypes) {
		return this.probe.fetch(start, end, eventTypes);
	}
	
	public List<T> fetchAndClean(final long start, final long end) {
		return fetchAndClean(start, end, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.Probe#fetchAndClean(long, long, java.util.List)
	 */
	public List<T> fetchAndClean(final long start, final long end,
			final List<Class<T>> eventTypes) {
		return this.probe.fetchAndClean(start, end, eventTypes);
	}

}
