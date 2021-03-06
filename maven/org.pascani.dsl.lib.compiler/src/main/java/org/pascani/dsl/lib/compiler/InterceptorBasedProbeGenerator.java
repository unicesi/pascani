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
package org.pascani.dsl.lib.compiler;

import java.io.File;
import java.util.List;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.pascani.dsl.lib.Event;
import org.pascani.dsl.lib.PascaniRuntime.Context;
import org.pascani.dsl.lib.Probe;
import org.pascani.dsl.lib.compiler.templates.InterceptorBasedProbeTemplates;
import org.pascani.dsl.lib.compiler.templates.ProbeTemplates;
import org.pascani.dsl.lib.compiler.util.NameProposal;
import org.pascani.dsl.lib.events.ExceptionEvent;
import org.pascani.dsl.lib.infrastructure.LocalProbe;

/** 
 * Generates the source code of a {@link Probe} for interceptor-based
 * measurements. For example, (i) generation of {@link ExceptionEvent} objects
 * from intercepting service executions, or (ii) execution time measurement.
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public abstract class InterceptorBasedProbeGenerator {

	/**
	 * The directory in which the java files will be written
	 */
	protected final String path;

	/**
	 * The intended probe name; for instance "ExceptionProbe". It may change if
	 * an existing file under the same parent directory has the same name.
	 */
	protected final String probeName;

	/**
	 * @param directoryPath
	 *            The directory in which the java files will be written
	 * @param probeName
	 *            The intended probe name; for instance "ExceptionProbe". It may
	 *            change if an existing file under the same parent directory has
	 *            the same name.
	 */
	public InterceptorBasedProbeGenerator(final String directoryPath,
			final String probeName) {
		this.path = directoryPath;
		this.probeName = probeName;
	}

	/**
	 * Generates the necessary code to intercept service executions and generate
	 * events of interest.
	 * 
	 * @param packageName
	 *            The package of the generated java class
	 * @param events
	 *            A list of event types to be caught by the interceptor. Not all
	 *            types of events are supported
	 * @return an object encapsulating the java source code
	 */
	public abstract JavaClassSource interceptor(String packageName,
			List<Class<? extends Event<?>>> events);

	/**
	 * Generates the (String) source code of the probe initialization and the
	 * event producer
	 * 
	 * @param exchange
	 *            The exchange of the external component to which events are
	 *            sent
	 * @param routingKey
	 *            The routingKey of the external component to which events are
	 *            sent
	 * @param events
	 *            A list of event types to be accepted by the event producer
	 * @return
	 */
	public String probeInitialization(String exchange, String routingKey,
			List<Class<? extends Event<?>>> events) {

		return ProbeTemplates.getInitializationContrib(getProbeClassName(),
				exchange, routingKey, true, events);
	}

	/**
	 * Generates the (String) source code of the probe initialization
	 */
	public String probeInitialization() {

		return ProbeTemplates.getInitializationContrib(getProbeClassName(), "",
				"", false, null);
	}

	/**
	 * Generates a class extending {@link LocalProbe} with the corresponding
	 * connection information.
	 * 
	 * @param packageName
	 *            The package of the generated java class
	 * @param routingKey
	 *            The routing key designated to the probe
	 * @return an object encapsulating the java source code
	 */
	public JavaClassSource probe(final String packageName,
			final String routingKey) {

		File directory = new File(this.path);
		String className = new NameProposal(getProbeClassName(), directory)
				.getNewName();

		JavaClassSource javaClass = Roaster.create(JavaClassSource.class);

		// Add imports
		javaClass.addImport(LocalProbe.class);
		javaClass.addImport(Context.class);

		// Set general properties
		javaClass.setPackage(packageName);
		javaClass.setName(className);
		javaClass.setSuperType(LocalProbe.class);

		String constructorBody = InterceptorBasedProbeTemplates
				.getProbeConstructor(routingKey, Context.PROBE);

		MethodSource<?> constructor = javaClass.addMethod();
		constructor.setConstructor(true);
		constructor.setBody(constructorBody);
		constructor.addThrows(Exception.class);

		return javaClass;
	}

	protected String getProbeClassName() {
		return this.probeName + "Probe";
	}

}
