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

import org.apache.commons.lang3.SerializationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NamespaceProxy implements Namespace {

	/**
	 * The logger
	 */
	protected final Logger logger = LogManager.getLogger(NamespaceProxy.class);

	/**
	 * An RPC client configured to make requests to a specific {@link Namespace}
	 */
	private final RpcClient client;

	/**
	 * @param client
	 *            An already configured RPC client, i.e., an initialized client
	 *            that knows a routing key
	 */
	public NamespaceProxy(RpcClient client) {
		this.client = client;
	}

	/**
	 * Performs an RPC call to a remote namespace
	 * 
	 * @param message
	 *            The payload of the message
	 * @param defaultValue
	 *            A decent value to nicely return in case an {@link Exception}
	 *            is thrown
	 * @return The response from the RPC server (i.e., a remote component
	 *         processing RPC requests) configured with the routing key of the
	 *         {@link RpcClient} instance
	 */
	private byte[] makeActualCall(RpcRequest request, Serializable defaultValue) {
		byte[] message = SerializationUtils.serialize(request);
		byte[] response = SerializationUtils.serialize(defaultValue);
		try {
			response = client.makeRequest(message);
		} catch (Exception e) {
			this.logger.error("Error performing an RPC call to namespace "
					+ this.client.routingKey(), e.getCause());
			throw new RuntimeException(e);
		}
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.infrastructure.Namespace#getVariable(java.lang.String)
	 */
	public Serializable getVariable(String variable) {
		RpcRequest request = new RpcRequest(
				RpcOperation.NAMESPACE_GET_VARIABLE, variable);

		byte[] response = makeActualCall(request, null);
		return SerializationUtils.deserialize(response);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.infrastructure.Namespace#setVariable(java.lang.String,
	 * java.io.Serializable)
	 */
	public Serializable setVariable(String variable, Serializable value) {
		RpcRequest request = new RpcRequest(
				RpcOperation.NAMESPACE_SET_VARIABLE, variable, value);

		byte[] response = makeActualCall(request, null);
		return SerializationUtils.deserialize(response);
	}

}
