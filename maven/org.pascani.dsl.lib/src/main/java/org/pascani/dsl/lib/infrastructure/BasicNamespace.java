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

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.pascani.dsl.lib.PascaniRuntime;
import org.pascani.dsl.lib.events.ChangeEvent;
import org.pascani.dsl.lib.infrastructure.rabbitmq.EndPoint;
import org.pascani.dsl.lib.infrastructure.rabbitmq.RabbitMQProducer;
import org.pascani.dsl.lib.infrastructure.rabbitmq.RabbitMQRpcServer;

/**
 * This implementation provides the basic functionality of a Namespace; that is,
 * getting a variable's current value, and setting a new value. In the latter
 * case, a new event of type {@link ChangeEvent} is published to the namespaces
 * exchange with the namespace name as routing key, thus allowing external
 * components to know when a variable's value has changed.
 * 
 * <p>
 * Variables are registered by means of
 * {@link BasicNamespace#registerVariable(String, Serializable, boolean)}
 * </p>
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class BasicNamespace implements Namespace, RpcRequestHandler {

	/**
	 * A RabbitMQ end point (a connection to the server)
	 */
	protected final EndPoint endPoint;

	/**
	 * An RPC server configured to serve external requests, for instance, from
	 * {@link Monitor} objects
	 */
	private final RpcServer server;

	/**
	 * The producer sending (variable) change events
	 */
	private final AbstractProducer producer;

	/**
	 * The context in which this namespace is used
	 */
	protected final PascaniRuntime.Context context;

	/**
	 * A map containing the variables defined in this namespace, with their
	 * corresponding current values
	 */
	private final Map<String, Serializable> variables;
	
	/**
	 * The variable representing the current state (stopped or not)
	 */
	private volatile boolean paused = false;

	/**
	 * Creates a basic namespace connected to the RabbitMQ server, identified by
	 * a unique routing key (the fully qualified name of the namespace).
	 * 
	 * <p>
	 * The producer does not need to be registered as event listener, as only
	 * this namespace is in charge of managing value changing for its variables
	 * (there is no local event propagation).
	 * </p>
	 * 
	 * @param routingKey
	 *            The routing key identifying this namespace within the
	 *            namespaces exchange
	 * @throws Exception
	 *             If something bad happens. Check out
	 *             {@link RabbitMQRpcServer#RabbitMQRpcServer(EndPoint, String, PascaniRuntime.Context)}
	 *             for more information.
	 */
	@SuppressWarnings("unchecked")
	public BasicNamespace(final String routingKey)
			throws Exception {

		this.variables = new HashMap<String, Serializable>();
		this.context = PascaniRuntime.Context.NAMESPACE;
		this.endPoint = new EndPoint();
		this.producer = new RabbitMQProducer(endPoint,
				declareQueue(routingKey), routingKey);
		this.producer.acceptOnly(ChangeEvent.class);

		this.server = new RabbitMQRpcServer(endPoint, routingKey,
				PascaniRuntime.Context.NAMESPACE);

		startRpcServer();
	}

	private String declareQueue(final String routingKey) throws IOException {
		// Create the corresponding namespace queue for the event producer, and
		// then create a binding between the queue and the configured namespace
		// exchange.
		String queue = routingKey;
		String exchange = PascaniRuntime.getEnvironment().get(
				"namespaces_exchange");

		this.endPoint.channel().queueDeclare(queue, false, true, true, null);
		this.endPoint.channel().queueBind(queue, exchange, routingKey);

		return exchange;
	}

	private void startRpcServer() {
		this.server.setHandler(this);
		this.server.start();
	}

	/**
	 * Registers a new variable with an initial value. If {@code overwrite} is
	 * {@code true} and the variable is already registered, its value is
	 * updated, otherwise no change is performed.
	 * 
	 * @param name
	 *            The name of the variable
	 * @param initialValue
	 *            The initial variable value
	 * @param overwrite
	 *            Whether to overwrite the current value in case the variable is
	 *            already registered
	 * @return either if the variable was registered or its value was updated
	 */
	protected boolean registerVariable(final String name,
			Serializable initialValue, boolean overwrite) {

		boolean registered = true;

		if (!overwrite && this.variables.containsKey(name)) {
			registered = false;
		} else {
			this.variables.put(name, initialValue);
		}

		return registered;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.infrastructure.RpcRequestHandler#handle(pascani.lang.
	 * infrastructure.RpcRequest)
	 */
	public Serializable handle(RpcRequest request) {
		Serializable response = null;
		String variable = (String) request.getParameter(0);
		
		// Namespace operations
		if (request.operation().equals(RpcOperation.NAMESPACE_GET_VARIABLE)) {
			response = getVariable(variable);
		} else if (request.operation().equals(
				RpcOperation.NAMESPACE_SET_VARIABLE)) {

			Serializable value = request.getParameter(1);
			response = setVariable(variable, value);
		}
		// Common operations
		else if (request.operation().equals(RpcOperation.PAUSE)) {
			pause();
			response = true;
		} else if (request.operation().equals(RpcOperation.RESUME)) {
			resume();
			response = true;
		} else if (request.operation().equals(RpcOperation.IS_PAUSED)) {
			response = isPaused();
		}

		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.infrastructure.Namespace#getVariable(java.lang.String)
	 */
	public Serializable getVariable(String variable) {
		return this.variables.get(variable);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.infrastructure.Namespace#setVariable(java.lang.String,
	 * java.io.Serializable)
	 */
	public Serializable setVariable(String variable, Serializable value) {
		if (!this.variables.containsKey(variable))
			return null;

		if (!isPaused()) {
			synchronized (this.variables) {
				Serializable previousValue = this.variables.get(variable);
				ChangeEvent event = new ChangeEvent(UUID.randomUUID(),
						previousValue, value, variable);

				this.variables.put(variable, value);
				this.producer.produce(event);
			}
		}
		return getVariable(variable);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.util.Resumable#pause()
	 */
	public void pause() {
		this.paused = true;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.util.Resumable#resume()
	 */
	public void resume() {
		this.paused = false;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.util.Resumable#isPaused()
	 */
	public boolean isPaused() {
		return this.paused;
	}
	
	/**
	 * Shutdowns connections
	 * 
	 * @throws Exception
	 *             If something bad happens!
	 */
	public void shutdown() throws Exception {
		this.server.shutdown();
	}

}
