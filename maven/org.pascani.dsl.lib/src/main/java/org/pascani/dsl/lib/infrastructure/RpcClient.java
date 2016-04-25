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
package org.pascani.dsl.lib.infrastructure;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pascani.dsl.lib.Probe;
import org.pascani.dsl.lib.infrastructure.rabbitmq.RabbitMQRpcClient;

/**
 * A simple implementation of an RPC client, abstracting the call to send a
 * request to a queuing system end point (
 * {@link RpcClient#makeCall(byte[], String)}). This request should be directly
 * sent by using an anonymous queue (a queue with generated-name) and a routing
 * key; the routing key generally points to an RPC server (i.e., a remote
 * component executing certain tasks such as {@link Probe} instances).
 * 
 * <p>
 * This class is intended to abstract different implementations of RPC clients,
 * such as {@link RabbitMQRpcClient}.
 * </p>
 * 
 * @see RabbitMQRpcClient
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public abstract class RpcClient {

	/**
	 * The routing key of the RPC server (i.e., a remote component processing
	 * RPC requests)
	 */
	protected final String ROUTING_KEY;

	/**
	 * The logger
	 */
	protected final Logger logger = LogManager.getLogger(getClass());

	public RpcClient(final String routingKey) {
		this.ROUTING_KEY = routingKey;
	}

	/**
	 * Sends a request (i.e., a message) to the RPC exchange, directly to a
	 * remote end point identified with a routing (binding) key.
	 * 
	 * @param message
	 *            The message to be sent
	 * @return the answer of the remote end point
	 * @throws Exception
	 *             If something bad happened
	 */
	public abstract byte[] makeRequest(byte[] message) throws Exception;
	
	/**
	 * Shutdowns connections
	 * 
	 * @throws Exception
	 *             If something bad happens!
	 */
	public abstract void shutdown() throws Exception;

	/**
	 * @return the routing key of the RPC server
	 */
	public final String routingKey() {
		return this.ROUTING_KEY;
	}

}
