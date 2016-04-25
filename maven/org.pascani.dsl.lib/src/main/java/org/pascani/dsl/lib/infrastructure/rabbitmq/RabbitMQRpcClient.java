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
package org.pascani.dsl.lib.infrastructure.rabbitmq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.pascani.dsl.lib.infrastructure.RpcClient;

import com.rabbitmq.client.ShutdownSignalException;

/**
 * An implementation of {@link RpcClient} for the RabbitMQ queuing system
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class RabbitMQRpcClient extends RpcClient {

	/**
	 * An end point connected to a RabbitMQ server
	 */
	private final EndPoint endPoint;

	/**
	 * The actual RPC RabbitMQ client
	 */
	private com.rabbitmq.client.RpcClient client;

	/**
	 * Creates an instance of a RabbitMQ RPC client, setting the RPC exchange
	 * and a routing key.
	 * 
	 * @param exchange
	 *            The exchange to which messages are sent
	 * @param routingKey
	 *            The queue name of the RPC server
	 * @throws Exception
	 */
	public RabbitMQRpcClient(String exchange,
			String routingKey) throws Exception {
		this(new EndPoint(), exchange, routingKey);
	}
	
	/**
	 * Creates an instance of a RabbitMQ RPC client, setting the RPC exchange
	 * and a routing key.
	 * 
	 * @param endPoint
	 *            The configured RabbitMQ end point
	 * @param exchange
	 *            The exchange to which messages are sent
	 * @param routingKey
	 *            The queue name of the RPC server
	 * @throws IOException
	 *             Is thrown if an I/O problem is encountered in the
	 *             initialization of the actual RabbitMQ RPC client
	 * @throws TimeoutException
	 *             Is thrown if there is a connection time out when connecting
	 *             to the RabbitMQ server
	 */
	public RabbitMQRpcClient(final EndPoint endPoint, String exchange,
			String routingKey) throws IOException {

		super(routingKey);

		this.endPoint = endPoint;
		this.client = new com.rabbitmq.client.RpcClient(
				this.endPoint.channel(), exchange, routingKey);
	}

	@Override public byte[] makeRequest(final byte[] message)
			throws ShutdownSignalException, IOException, TimeoutException {

		// Automatically sets reply-to and correlationId
		return this.client.primitiveCall(message);
	}
	
	@Override public void shutdown() throws IOException, TimeoutException {
		this.endPoint.close();
	}

}
