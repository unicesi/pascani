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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pascani.dsl.lib.Event;
import org.pascani.dsl.lib.PascaniRuntime;
import org.pascani.dsl.lib.Probe;
import org.pascani.dsl.lib.infrastructure.rabbitmq.RabbitMQRpcClient;

/**
 * An implementation of {@link Probe} that makes communication transparent for
 * {@link Monitor} instances with remote {@link Probe} objects.
 *
 * @param <T>
 *            The type of events the actual probe handles
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class ProbeProxy implements Probe {

	/**
	 * The logger
	 */
	protected final Logger logger = LogManager.getLogger(ProbeProxy.class);

	/**
	 * An RPC client configured to make requests to a specific {@link Probe}
	 */
	private final RpcClient client;

	/**
	 * Creates a ProbeProxy instance from a routing key, and the default RPC
	 * exchange.
	 * 
	 * @param routingKey
	 *            The probe's routing key
	 * @throws Exception
	 */
	public ProbeProxy(String routingKey) throws Exception {
		this(new RabbitMQRpcClient(PascaniRuntime.getEnvironment().get(
				"rpc_exchange"), routingKey));
	}

	/**
	 * @param client
	 *            An already configured RPC client, i.e., an initialized client
	 *            that knows a routing key
	 */
	public ProbeProxy(RpcClient client) {
		this.client = client;
	}

	/**
	 * Performs an RPC call to a remote probe
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
			this.logger.error("Error performing an RPC call to monitor probe "
					+ this.client.routingKey(), e.getCause());
			throw new RuntimeException(e);
		}
		return response;
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

		RpcRequest request = new RpcRequest(RpcOperation.PROBE_CLEAN, start,
				end, new ArrayList<Class<? extends Event<?>>>(eventTypes));
		byte[] response = makeActualCall(request, false);
		return SerializationUtils.deserialize(response);
	}

	public int count(final long start, final long end) {
		return count(start, end, new ArrayList<Class<? extends Event<?>>>());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.Probe#count(long, long, java.lang.Class[])
	 */
	public int count(final long start, final long end,
			final List<Class<? extends Event<?>>> eventTypes) {

		RpcRequest request = new RpcRequest(RpcOperation.PROBE_COUNT, start,
				end, new ArrayList<Class<? extends Event<?>>>(eventTypes));
		byte[] response = makeActualCall(request, 0);
		return SerializationUtils.deserialize(response);
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

		RpcRequest request = new RpcRequest(RpcOperation.PROBE_COUNT_AND_CLEAN,
				start, end,
				new ArrayList<Class<? extends Event<?>>>(eventTypes));
		byte[] response = makeActualCall(request, 0);
		return SerializationUtils.deserialize(response);
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

		RpcRequest request = new RpcRequest(RpcOperation.PROBE_FETCH, start,
				end, new ArrayList<Class<? extends Event<?>>>(eventTypes));
		byte[] response = makeActualCall(request, new ArrayList<Event<?>>());
		return SerializationUtils.deserialize(response);
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

		RpcRequest request = new RpcRequest(RpcOperation.PROBE_FETCH_AND_CLEAN,
				start, end,
				new ArrayList<Class<? extends Event<?>>>(eventTypes));
		byte[] response = makeActualCall(request, new ArrayList<Event<?>>());
		return SerializationUtils.deserialize(response);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.util.Resumable#pause()
	 */
	public void pause() {
		RpcRequest request = new RpcRequest(RpcOperation.PAUSE);
		makeActualCall(request, false); // true should be returned
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.util.Resumable#resume()
	 */
	public void resume() {
		RpcRequest request = new RpcRequest(RpcOperation.RESUME);
		makeActualCall(request, false); // true should be returned
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.util.Resumable#isPaused()
	 */
	public boolean isPaused() {
		RpcRequest request = new RpcRequest(RpcOperation.IS_PAUSED);
		byte[] response = makeActualCall(request, false);
		return SerializationUtils.deserialize(response);
	}
	
	/**
	 * @return the routing key to which this proxy points
	 */
	public String routingKey() {
		return this.client.routingKey();
	}
	
	/**
	 * Shutdowns connections
	 * 
	 * @throws Exception
	 *             If something bad happens!
	 */
	public void shutdown() throws Exception {
		this.client.shutdown();
	}

}
