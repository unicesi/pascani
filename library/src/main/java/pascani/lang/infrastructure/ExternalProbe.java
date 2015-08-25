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

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import pascani.lang.Event;
import pascani.lang.Probe;
import pascani.lang.infrastructure.rabbitmq.RabbitMQConsumer;
import pascani.lang.monitors.AbstractMonitor;

/**
 * This {@link Probe} implementation is specially made for grouping distributed
 * measurements into one single probe. Measurements are sent to the probes
 * exchange with the routing key set to this probe's unique name.
 * 
 * <p>
 * In the same way, {@link AbstractMonitor} instances and other components can
 * request data from the RPC queue, by using the routing key.
 * </p>
 * 
 * @param <T>
 *            The type of events this probe handles
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class ExternalProbe<T extends Event<?>> extends CustomProbe<T> {

	/**
	 * The message consumer listening for external events (i.e., events
	 * generated elsewhere and concentrated here)
	 */
	protected final MessageConsumer consumer;

	/**
	 * Creates an instance connected to a RabbitMQ server, making the recorded
	 * events reachable for external components.
	 * 
	 * <p>
	 * External components may use a {@link ProbeProxy} instance to access
	 * recorded events from this probe.
	 * </p>
	 * 
	 * @param host
	 *            The RabbitMQ server's host
	 * @param port
	 *            The RabbitMQ server's AQMP port
	 * @param virtualHost
	 *            The RabbitMQ server's virtual host
	 * @param username
	 *            The RabbitMQ user
	 * @param password
	 *            The RabbitMQ password
	 * @param routingKey
	 *            A unique name among all the {@link Probe} instances. This name
	 *            is necessary for external components to send RPC requests to
	 *            this probe.
	 * @throws IOException
	 *             If an I/O problem is encountered in the initialization of the
	 *             RabbitMQ RPC server
	 * @throws TimeoutException
	 *             If there is a connection time out when connecting to the
	 *             RabbitMQ server
	 */
	public ExternalProbe(final String host, final int port,
			final String virtualHost, final String username,
			final String password, String routingKey) throws IOException,
			TimeoutException {

		super(host, port, virtualHost, username, password, routingKey);

		String queue = super.endPoint.channel().queueDeclare().getQueue();
		this.consumer = new RabbitMQConsumer(super.endPoint, queue, routingKey);
		this.consumer.start();
	}

}
