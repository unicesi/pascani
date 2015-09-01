package pascani.lang.infrastructure;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import pascani.lang.Event;
import pascani.lang.events.ChangeEvent;
import pascani.lang.infrastructure.rabbitmq.EndPoint;
import pascani.lang.infrastructure.rabbitmq.RabbitMQProducer;
import pascani.lang.infrastructure.rabbitmq.RabbitMQRpcServer;

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
	private final MessageProducer producer;

	/**
	 * The context in which this namespace is used
	 */
	private final pascani.lang.Runtime.Context context;

	/**
	 * A map containing the variables defined in this namespace, with their
	 * corresponding current values
	 */
	private final Map<String, Serializable> variables;

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
	 * @param uri
	 *            The RabbitMQ connection URI
	 * @param routingKey
	 *            The routing key identifying this namespace within the
	 *            namespaces exchange
	 * @throws Exception
	 *             If something bad happens. Check out
	 *             {@link RabbitMQRpcServer#RabbitMQRpcServer(EndPoint, String, pascani.lang.Runtime.Context)}
	 *             for more information.
	 */
	public BasicNamespace(final String uri, final String routingKey)
			throws Exception {

		this.variables = new HashMap<String, Serializable>();
		this.context = pascani.lang.Runtime.Context.NAMESPACE;
		this.endPoint = new EndPoint(uri);
		this.producer = new RabbitMQProducer(endPoint, getAcceptedClasses(),
				declareQueue(routingKey), routingKey);

		this.server = new RabbitMQRpcServer(endPoint, routingKey,
				pascani.lang.Runtime.Context.NAMESPACE);

		startRpcServer();
	}

	private String declareQueue(final String routingKey) throws IOException {
		// Create the corresponding namespace queue for the event producer, and
		// then create a binding between the queue and the configured namespace
		// exchange.
		String queue = routingKey;
		String exchange = pascani.lang.Runtime.getRuntimeInstance(this.context)
				.getEnvironment().get("namespace_exchange");

		this.endPoint.channel().queueDeclare(queue, false, true, true, null);
		this.endPoint.channel().queueBind(queue, exchange, routingKey);

		return exchange;
	}

	private List<Class<? extends Event<?>>> getAcceptedClasses() {
		List<Class<? extends Event<?>>> classes = new ArrayList<Class<? extends Event<?>>>();
		classes.add(ChangeEvent.class);

		return classes;
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

		if (request.operation().equals(RpcOperation.NAMESPACE_GET_VARIABLE)) {
			response = getVariable(variable);
		} else if (request.operation().equals(
				RpcOperation.NAMESPACE_SET_VARIABLE)) {

			Serializable value = request.getParameter(1);
			response = setVariable(variable, value);
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
		if(!this.variables.containsKey(variable))
			return null;
		
		synchronized (this.variables) {
			Serializable previousValue = this.variables.get(variable);
			ChangeEvent event = new ChangeEvent(UUID.randomUUID(),
					previousValue, value, variable);

			this.variables.put(variable, value);
			this.producer.produce(event);
		}
		return getVariable(variable);
	}

}
