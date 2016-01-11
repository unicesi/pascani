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
package org.pascani.dsl.lib.compiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.Type;
import org.jboss.forge.roaster.model.source.Import;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.jboss.forge.roaster.model.source.ParameterSource;
import org.jboss.forge.roaster.model.util.Types;
import org.pascani.dsl.lib.PascaniRuntime;
import org.pascani.dsl.lib.PascaniRuntime.Context;
import org.pascani.dsl.lib.compiler.util.NameProposal;
import org.pascani.dsl.lib.events.NetworkLatencyEvent;
import org.pascani.dsl.lib.infrastructure.AbstractProducer;
import org.pascani.dsl.lib.infrastructure.ExternalProbe;
import org.pascani.dsl.lib.infrastructure.rabbitmq.RabbitMQProducer;

import org.pascani.dsl.lib.compiler.templates.NetworkLatencyTemplates;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

/**
 * TODO: Generate the SCA composites
 * 
 * TODO: The probe must be initialized. The initialization code must be
 * generated
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
public class LatencyProbeGenerator {

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
	 * The probes' exchange
	 */
	private final String exchange;

	/**
	 * The network probe's unique identifier (among the monitor probes)
	 */
	private final String routingKey;

	/**
	 * Creates a network latency probe generator.
	 * 
	 * @param interfacePath
	 *            The file path of the original interface's source code
	 * @param exchange
	 *            The probes' exchange
	 * @param routingKey
	 *            The network probe's unique identifier (among the monitor
	 *            probes)
	 * @param durableExchange
	 *            Whether the exchange is durable or not
	 */
	public LatencyProbeGenerator(final String interfacePath,
			final String exchange, final String routingKey) {
		this.interfacePath = interfacePath;
		this.exchange = exchange;
		this.routingKey = routingKey;
	}

	/**
	 * Duplicates the service interface adding a {@link NetworkLatencyEvent}
	 * parameter to each declared method
	 * 
	 * @return The modified source code
	 * @throws FileNotFoundException
	 *             If the original service interface file is not found
	 */
	public JavaInterfaceSource modifiedInterface() throws FileNotFoundException {
		File file = new File(interfacePath);
		JavaInterfaceSource modifiedInterface = Roaster.parse(
				JavaInterfaceSource.class, file);

		// Choose a name
		String interfaceName = new NameProposal(file.getName(),
				file.getParentFile()).getNewName();

		modifiedInterface.setName(interfaceName);
		modifiedInterface.addImport(NetworkLatencyEvent.class);

		// For each method, add a parameter
		for (MethodSource<?> method : modifiedInterface.getMethods()) {
			List<?> parameters = method.getParameters();
			Collection<String> names = Collections2.transform(parameters,
					this.getName);

			String parameterName = new NameProposal(names)
					.getNewName(EVENT_PARAMETER_NAME);
			String parameter = NetworkLatencyEvent.class.getSimpleName() + " "
					+ parameterName;

			// Set the parameters, leaving the measurement parameter as first
			// parameter
			method.setParameters(parameter + ", "
					+ Joiner.on(", ").join(parameters));

			// If there is a return, change it to the event type
			if (!method.isReturnTypeVoid()) {
				method.setReturnType(NetworkLatencyEvent.class);
			}
		}

		return modifiedInterface;
	}

	/**
	 * Creates an adapter implementation of the modified service interface. Each
	 * method implementation contains the initial {@link NetworkLatencyEvent}
	 * measurement.
	 * 
	 * @param modified
	 *            The modified service interface
	 * @return The adapter's source code
	 * @throws FileNotFoundException
	 *             If the original service interface file is not found
	 */
	public JavaClassSource initialAdapter(final JavaInterfaceSource modified)
			throws FileNotFoundException {

		File file = new File(this.interfacePath);
		String className = new NameProposal(modified.getName()
				+ "InitialImpl.java", file.getParentFile()).getNewName();

		JavaInterfaceSource _interface = Roaster.parse(
				JavaInterfaceSource.class, file);

		JavaClassSource javaClass = Roaster.create(JavaClassSource.class);
		javaClass.setPackage(_interface.getPackage()).setName(className);
		javaClass.addInterface(_interface.getCanonicalName());
		javaClass.addImport(UUID.class);
		javaClass.addImport(NetworkLatencyEvent.class);
		javaClass.addImport(List.class);
		javaClass.addImport(AbstractProducer.class);
		javaClass.addImport(RabbitMQProducer.class);

		for (Import _import : _interface.getImports()) {
			javaClass.addImport(_import);
		}

		// Add a reference to the final adapter
		javaClass.addField().setName(REFERENCE_FIELD_NAME)
				.setType(modified.getCanonicalName()).setPrivate();

		// Add a producer
		javaClass.addField().setName(PRODUCER_FIELD_NAME)
				.setType(AbstractProducer.class).setPrivate().setFinal(true);

		// A constructor to initialize the producer
		String constructorBody = NetworkLatencyTemplates
				.getProducerInitialization(PRODUCER_FIELD_NAME, this.exchange,
						this.routingKey);

		MethodSource<JavaClassSource> constructor = javaClass.addMethod()
				.setName(javaClass.getName()).setBody(constructorBody)
				.setConstructor(true);
		constructor.addAnnotation(SuppressWarnings.class).setStringValue(
				"unchecked");

		Function<Object, String> getClass = new Function<Object, String>() {
			public String apply(Object parameter) {
				Type<?> type = ((ParameterSource<?>) parameter).getType();
				String name = type.toString();

				if (type.toString().indexOf("<") > -1) {
					name = name.substring(0, name.indexOf('<'))
							+ name.substring(name.lastIndexOf('>') + 1);
				}

				return Types.toSimpleName(name) + ".class";
			}
		};

		for (MethodSource<?> method : _interface.getMethods()) {

			// Add the method in the new class for further modification
			MethodSource<?> classMethod = javaClass
					.addMethod(method.toString());

			List<?> parameters = classMethod.getParameters();
			Collection<String> names = Collections2.transform(parameters,
					this.getName);
			NameProposal varNames = new NameProposal(names);

			// Method's body
			String eventParam = varNames.getNewName(EVENT_PARAMETER_NAME);
			List<String> eventParams = new ArrayList<String>();

			String startParam = varNames.getNewName("start");
			String exceptionParam = varNames.getNewName("e");

			String paramTypesArray = varNames.getNewName("paramTypes");
			Collection<String> paramTypes = Collections2.transform(parameters,
					getClass);

			eventParams.add("UUID.randomUUID()");
			eventParams.add(startParam);
			eventParams.add(_interface.getName() + ".class");
			eventParams.add(_interface.getName() + ".class");
			eventParams.add("null");
			eventParams.add("\"" + classMethod.getName() + "\"");
			eventParams.add(paramTypesArray);
			eventParams.add(Joiner.on(", ").join(names));

			String body = NetworkLatencyTemplates.initialAdapterMethod(
					startParam, eventParam, eventParams, exceptionParam,
					paramTypesArray, paramTypes,
					classMethod.isReturnTypeVoid(), REFERENCE_FIELD_NAME,
					PRODUCER_FIELD_NAME, classMethod.getName(), classMethod
							.getReturnType().getSimpleName(), names);

			classMethod.setBody(body);
		}

		return javaClass;
	}

	/**
	 * Creates an adapter implementation of the modified service interface. Each
	 * method implementation contains the final {@link NetworkLatencyEvent}
	 * measurement.
	 * 
	 * @param modified
	 *            The modified service interface
	 * @return The adapter's source code
	 * @throws FileNotFoundException
	 *             If the original service interface file is not found
	 */
	public JavaClassSource finalAdapter(final JavaInterfaceSource modified)
			throws FileNotFoundException {

		File file = new File(this.interfacePath);
		String className = new NameProposal(modified.getName()
				+ "FinalImpl.java", file.getParentFile()).getNewName();

		JavaInterfaceSource _interface = Roaster.parse(
				JavaInterfaceSource.class, file);

		JavaClassSource javaClass = Roaster.create(JavaClassSource.class);
		javaClass.setName(className);
		javaClass.setPackage(modified.getPackage());
		javaClass.addInterface(modified.getCanonicalName());
		javaClass.addImport(NetworkLatencyEvent.class);
		javaClass.addImport(List.class);
		javaClass.addImport(AbstractProducer.class);
		javaClass.addImport(RabbitMQProducer.class);

		for (Import _import : modified.getImports()) {
			javaClass.addImport(_import);
		}

		// Add a reference to the original service
		javaClass.addField().setName(REFERENCE_FIELD_NAME)
				.setType(_interface.getCanonicalName()).setPrivate();

		// Add a producer
		javaClass.addField().setName(PRODUCER_FIELD_NAME)
				.setType(AbstractProducer.class).setPrivate().setFinal(true);

		// A constructor to initialize the producer
		String constructorBody = NetworkLatencyTemplates
				.getProducerInitialization(PRODUCER_FIELD_NAME, this.exchange,
						this.routingKey);

		MethodSource<JavaClassSource> constructor = javaClass.addMethod()
				.setName(javaClass.getName()).setBody(constructorBody)
				.setConstructor(true);
		constructor.addAnnotation(SuppressWarnings.class).setStringValue(
				"unchecked");

		for (MethodSource<?> method : modified.getMethods()) {

			// Add the method in the new class for further modification
			MethodSource<?> classMethod = javaClass
					.addMethod(method.toString());

			List<?> parameters = classMethod.getParameters();
			ArrayList<String> names = Lists.newArrayList(Collections2
					.transform(parameters, this.getName));
			NameProposal varNames = new NameProposal(names);

			// Method's body
			String eventParam = names.get(0);
			String newEventParam = varNames.getNewName(eventParam);
			String endParam = varNames.getNewName("end");

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

	/**
	 * Creates an implementation of the network probe. The created class extends
	 * {@link ExternalProbe}, setting the queuing server's connection data.
	 * 
	 * @param modified
	 *            The modified service interface
	 * @return The created source code
	 */
	public JavaClassSource probe(final JavaInterfaceSource modified) {

		File file = new File(this.interfacePath);
		String className = new NameProposal("NetworkLatencyProbe.java",
				file.getParentFile()).getNewName();

		JavaClassSource javaClass = Roaster.create(JavaClassSource.class);

		// Set general properties
		javaClass.setName(className);
		javaClass.setPackage(modified.getPackage());
		javaClass.setSuperType(ExternalProbe.class);

		// Add imports
		javaClass.addImport(ExternalProbe.class);
		javaClass.addImport(PascaniRuntime.Context.class);

		Context context = Context.PROBE;
		String body = NetworkLatencyTemplates.getProbeInitialization(
				this.routingKey, context);
		javaClass.addMethod().setConstructor(true).setBody(body)
				.addThrows(Exception.class);

		return javaClass;
	}

}
