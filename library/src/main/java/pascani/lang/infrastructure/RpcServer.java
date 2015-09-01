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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pascani.lang.Probe;

/**
 * An abstract implementation of an RPC server. This server is setup with a
 * specific queue name, where {@link RpcRequest} instances are put. A
 * {@link Probe} instance must be given in order to process each request; once
 * the probe has been set, this server can start processing requests. To do so,
 * a call to the {@link RpcServer#run()} method must be performed.
 * 
 * <p>
 * Once the server has started, the {@link RpcServer#startProcessingRequests()}
 * method is invoked. For each {@link RpcRequest} object received, the
 * processing must be delegated to the {@link Probe} instance by means of
 * {@link RpcServer#delegateHandling(RpcRequest)}.
 * </p>
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public abstract class RpcServer extends Thread {

	/**
	 * The queue designated for RPC requests
	 */
	protected final String RPC_REQUEST_QUEUE_NAME;

	/**
	 * An RPC request handler. Could be either a {@link Monitor}, a
	 * {@link Probe} or a {@link BasicNamespace}
	 */
	protected RpcRequestHandler handler;

	/**
	 * The logger
	 */
	protected final Logger logger = LogManager.getLogger(getClass());

	public RpcServer(final String rpcRequestQueueName) {
		this.RPC_REQUEST_QUEUE_NAME = rpcRequestQueueName;
	}

	/**
	 * Sets the RPC requests handler
	 * 
	 * @param handler
	 *            The RPC handler to which requests are delegated
	 */
	public void setHandler(RpcRequestHandler handler) {
		this.handler = handler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override public final void run() {
		try {
			startProcessingRequests();
		} catch (Exception e) {
			logger.error("Error starting request processing from RPC server",
					e.getCause());
			throw new RuntimeException(e);
		}
	}

	/**
	 * Starts the RPC server, i.e., creates the connection and starts consuming
	 * {@link RpcRequest} objects from the specified queue.
	 * 
	 * @throws Exception
	 *             If something bad happens!
	 */
	protected abstract void startProcessingRequests() throws Exception;

	/**
	 * Delegates the request processing to the handler.
	 * 
	 * @param request
	 *            The RPC request
	 * @return the corresponding response
	 */
	public Serializable delegateHandling(RpcRequest request) {
		return this.handler.handle(request);
	}

}
