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
package pascani.compiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.Import;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.jboss.forge.roaster.model.source.ParameterSource;

import pascani.compiler.templates.NetworkLatencyTemplates;
import pascani.compiler.util.FilenameProposal;
import pascani.lang.Event;
import pascani.lang.events.NetworkLatencyEvent;
import pascani.lang.infrastructure.MessageProducer;
import pascani.lang.infrastructure.rabbitmq.EndPoint;
import pascani.lang.infrastructure.rabbitmq.RabbitMQProducer;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

/**
 * TODO: remember to register interfaces to be duplicated, to generate adapters
 * only once
 * 
 * TODO: Check if producing events blocks the execution. If so, make
 * {@link MessageProducer} a {@link Thread}
 * 
 * TODO: Generate the SCA composites
 * 
 * This implementation generates several artifacts to measure network latency in
 * method executions. The artifacts created are:
 * 
 * <ul>
 * <li>Duplicates a service interface adding an additional parameter to each
 * method; this parameter is a partial measurement of type
 * {@link NetworkLatencyEvent},</li>
 * <li>Creates a class implementing the duplicated interface; within each method
 * creates an initial measurement of type {@link NetworkLatencyEvent} and return
 * (in case the method is not void) a call to the corresponding method in
 * another service, and</li>
 * <li>Creates a class implementing the duplicated interface; within each method
 * creates the final measurement of the received partial measurement.</li>
 * </ul>
 * 
 * <p>
 * For non-void methods, the measurement is also done backwards, meaning that
 * the final adapter starts a new measurement before returning. In these cases,
 * the return is encapsulated into a {@link NetworkLatencyEvent} instance.
 * </p>
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class NetworkLatencyGenerator {

	private final String interfacePath;
	public static final String REFERENCE_FIELD_NAME = "reference";
	public static final String PRODUCER_FIELD_NAME = "producer";
	public static final String EVENT_PARAMETER_NAME = "event";

	/**
	 * A function to get the name given a parameter
	 */
	private final Function<Object, String> getName = new Function<Object, String>() {
		public String apply(Object arg0) {
			return ((ParameterSource<?>) arg0).getName();
		}
	};

	/**
	 * The message queuing server's host
	 */
	private final String host;

	/**
	 * The message queuing server's port
	 */
	private final int port;

	/**
	 * The message queuing server's virtual host
	 */
	private final String virtualHost;

	/**
	 * The probes' exchange
	 */
	private final String exchange;

	/**
	 * The network probe's unique identifier (among the monitor probes)
	 */
	private final String routingKey;

	/**
	 * Whether the exchange is durable or not
	 */
	private final boolean durableExchange;

	/**
	 * TODO: documentation
	 * 
	 * @param interfacePath
	 *            The file path of the original interface's source code
	 * @param host
	 *            The message queuing server's host
	 * @param port
	 *            The message queuing server's port
	 * @param virtualHost
	 *            The message queuing server's virtual host
	 * @param exchange
	 *            The probes' exchange
	 * @param routingKey
	 *            The network probe's unique identifier (among the monitor
	 *            probes)
	 * @param durableExchange
	 *            Whether the exchange is durable or not
	 */
	public NetworkLatencyGenerator(final String interfacePath,
			final String host, final int port, final String virtualHost,
			final String exchange, final String routingKey,
			final boolean durableExchange) {
		this.interfacePath = interfacePath;
		this.host = host;
		this.port = port;
		this.virtualHost = virtualHost;
		this.exchange = exchange;
		this.routingKey = routingKey;
		this.durableExchange = durableExchange;
	}

	public JavaInterfaceSource modifiedInterface() throws FileNotFoundException {
		File file = new File(interfacePath);
		JavaInterfaceSource modifiedInterface = Roaster.parse(
				JavaInterfaceSource.class, file);

		// Choose a name
		String interfaceName = new FilenameProposal(file.getName(),
				file.getParentFile()).getNewName();

		modifiedInterface.setName(interfaceName);
		modifiedInterface.addImport(NetworkLatencyEvent.class);

		// For each method, add a parameter
		for (MethodSource<?> method : modifiedInterface.getMethods()) {
			List<?> parameters = method.getParameters();
			Collection<String> names = Collections2.transform(parameters,
					this.getName);

			String parameterName = new FilenameProposal(EVENT_PARAMETER_NAME,
					names).getNewName();
			String parameter = NetworkLatencyEvent.class.getSimpleName() + " "
					+ parameterName;

			// Set the parameters, leaving the measurement parameter as first
			// parameter
			method.setParameters(parameter + ", "
					+ Joiner.on(", ").join(parameters));

			// If return is not void, change it
			if (!method.isReturnTypeVoid()) {
				method.setReturnType(NetworkLatencyEvent.class);
			}
		}

		return modifiedInterface;
	}

	public JavaClassSource initialAdapter(final JavaInterfaceSource modified)
			throws FileNotFoundException {

		File file = new File(this.interfacePath);
		String className = new FilenameProposal(modified.getName()
				+ "InitialImpl.java", file.getParentFile()).getNewName();

		JavaInterfaceSource _interface = Roaster.parse(
				JavaInterfaceSource.class, file);

		JavaClassSource javaClass = Roaster.create(JavaClassSource.class);
		javaClass.setPackage(_interface.getPackage()).setName(className);
		javaClass.addInterface(_interface.getCanonicalName());
		javaClass.addImport(UUID.class);
		javaClass.addImport(NetworkLatencyEvent.class);
		javaClass.addImport(EndPoint.class);
		javaClass.addImport(List.class);
		javaClass.addImport(ArrayList.class);
		javaClass.addImport(Event.class);
		javaClass.addImport(MessageProducer.class);
		javaClass.addImport(RabbitMQProducer.class);

		for (Import _import : _interface.getImports()) {
			javaClass.addImport(_import);
		}

		// Add a reference to the final adapter
		javaClass.addField().setName(REFERENCE_FIELD_NAME)
				.setType(modified.getCanonicalName()).setPrivate();

		// Add a producer
		javaClass.addField().setName(PRODUCER_FIELD_NAME)
				.setType(MessageProducer.class).setPrivate();

		// A constructor to initialize the producer
		String constructorBody = NetworkLatencyTemplates
				.getProducerInitialization(PRODUCER_FIELD_NAME, host, port,
						virtualHost, exchange, routingKey, durableExchange);

		javaClass.addMethod().setName(javaClass.getName())
				.setBody(constructorBody).setConstructor(true);

		Function<String, String> getClass = new Function<String, String>() {
			public String apply(String input) {
				return input + ".getClass()";
			}
		};

		for (MethodSource<?> method : _interface.getMethods()) {

			// Add the method in the new class for further modification
			MethodSource<?> classMethod = javaClass
					.addMethod(method.toString());

			List<?> parameters = classMethod.getParameters();
			Collection<String> names = Collections2.transform(parameters,
					this.getName);

			// Method's body
			String eventParam = new FilenameProposal(EVENT_PARAMETER_NAME,
					names).getNewName();
			List<String> eventParams = new ArrayList<String>();

			String startParam = new FilenameProposal("start", names)
					.getNewName();
			String exceptionParam = new FilenameProposal("e", names)
					.getNewName();
			Collection<String> paramTypes = Collections2.transform(names,
					getClass);

			eventParams.add("UUID.randomUUID()");
			eventParams.add(startParam);
			eventParams.add(_interface.getName() + ".class");
			eventParams.add(_interface.getName() + ".class");
			eventParams.add("null");
			eventParams.add(NetworkLatencyTemplates.getMethod(
					classMethod.getName(), paramTypes));
			eventParams.add(Joiner.on(", ").join(names));

			String body = NetworkLatencyTemplates.initialAdapterMethod(
					startParam, eventParam, eventParams, exceptionParam,
					classMethod.isReturnTypeVoid(), REFERENCE_FIELD_NAME,
					PRODUCER_FIELD_NAME, classMethod.getName(), classMethod
							.getReturnType().getSimpleName(), names);

			classMethod.setBody(body);
		}

		return javaClass;
	}

	public JavaClassSource finalAdapter(final JavaInterfaceSource modified)
			throws FileNotFoundException {

		File file = new File(this.interfacePath);
		String className = new FilenameProposal(modified.getName()
				+ "FinalImpl.java", file.getParentFile()).getNewName();

		JavaInterfaceSource _interface = Roaster.parse(
				JavaInterfaceSource.class, file);

		JavaClassSource javaClass = Roaster.create(JavaClassSource.class);
		javaClass.setName(className);
		javaClass.setPackage(modified.getPackage());
		javaClass.addInterface(modified.getCanonicalName());
		javaClass.addImport(NetworkLatencyEvent.class);
		javaClass.addImport(EndPoint.class);
		javaClass.addImport(List.class);
		javaClass.addImport(ArrayList.class);
		javaClass.addImport(Event.class);
		javaClass.addImport(MessageProducer.class);
		javaClass.addImport(RabbitMQProducer.class);

		for (Import _import : modified.getImports()) {
			javaClass.addImport(_import);
		}

		// Add a reference to the original service
		javaClass.addField().setName(REFERENCE_FIELD_NAME)
				.setType(_interface.getCanonicalName()).setPrivate();

		// Add a producer
		javaClass.addField().setName(PRODUCER_FIELD_NAME)
				.setType(MessageProducer.class).setPrivate();

		// A constructor to initialize the producer
		String constructorBody = NetworkLatencyTemplates
				.getProducerInitialization(PRODUCER_FIELD_NAME, host, port,
						virtualHost, exchange, routingKey, durableExchange);

		javaClass.addMethod().setName(javaClass.getName())
				.setBody(constructorBody).setConstructor(true);

		for (MethodSource<?> method : modified.getMethods()) {

			// Add the method in the new class for further modification
			MethodSource<?> classMethod = javaClass
					.addMethod(method.toString());

			List<?> parameters = classMethod.getParameters();
			ArrayList<String> names = Lists.newArrayList(Collections2
					.transform(parameters, this.getName));

			// Method's body
			String eventParam = names.get(0);
			String newEventParam = new FilenameProposal(eventParam, names)
					.getNewName();
			String endParam = new FilenameProposal("end", names).getNewName();

			// Remove the event parameter
			names.remove(0);

			String body = NetworkLatencyTemplates.finalAdapterMethod(endParam,
					eventParam, newEventParam, classMethod.isReturnTypeVoid(),
					REFERENCE_FIELD_NAME, PRODUCER_FIELD_NAME,
					classMethod.getName(), names);

			classMethod.setBody(body);
		}

		return javaClass;
	}

}
