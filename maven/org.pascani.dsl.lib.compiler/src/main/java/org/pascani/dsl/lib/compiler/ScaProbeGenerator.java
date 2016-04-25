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
import java.util.UUID;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.osoa.sca.annotations.Scope;
import org.ow2.frascati.tinfi.api.IntentHandler;
import org.ow2.frascati.tinfi.api.IntentJoinPoint;
import org.pascani.dsl.lib.Event;
import org.pascani.dsl.lib.PascaniRuntime;
import org.pascani.dsl.lib.compiler.templates.ProbeTemplates;
import org.pascani.dsl.lib.compiler.util.NameProposal;
import org.pascani.dsl.lib.events.ExceptionEvent;
import org.pascani.dsl.lib.events.InvokeEvent;
import org.pascani.dsl.lib.events.ReturnEvent;
import org.pascani.dsl.lib.events.TimeLapseEvent;
import org.pascani.dsl.lib.util.LocalEventProducer;

/**
 * This class generates the necessary source code to automatically raise
 * instances of {@link ExceptionEvent}, {@link TimeLapseEvent},
 * {@link InvokeEvent} and {@link ReturnEvent}. As the generated code is
 * compliant with the SCA specification, and the FraSCAti middleware, the
 * mechanism for intercepting the service execution is an Intent composite.
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class ScaProbeGenerator extends InterceptorBasedProbeGenerator {

	public ScaProbeGenerator(String directoryPath, final String probeName) {
		super(directoryPath, probeName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pascani.compiler.InterceptorBasedProbeGenerator#interceptor(java.lang
	 * .String, java.util.List)
	 */
	@Override public JavaClassSource interceptor(final String packageName,
			final List<Class<? extends Event<?>>> events) {

		File directory = new File(path);
		String className = new NameProposal(
				this.probeName + "Interceptor.java", directory).getNewName();

		JavaClassSource javaClass = Roaster.create(JavaClassSource.class);

		// Add imports
		javaClass.addImport(PascaniRuntime.class);
		javaClass.addImport(LocalEventProducer.class);
		javaClass.addImport(Event.class);
		javaClass.addImport(UUID.class);

		for (Class<? extends Event<?>> clazz : events)
			javaClass.addImport(clazz);

		// Set general properties
		javaClass.setPackage(packageName);
		javaClass.addAnnotation(Scope.class).setStringValue("value",
				"COMPOSITE");
		javaClass.setName(className);
		javaClass.addInterface(IntentHandler.class);

		// Add an event producer
		String producerVar = "producer";

		FieldSource<?> field = javaClass.addField();
		field.setType(LocalEventProducer.class.getSimpleName() + "<"
				+ Event.class.getSimpleName() + "<?>" + ">");
		field.setName(producerVar).setPrivate().setFinal(true);

		String constructor = ProbeTemplates
				.getProducerInitialization(producerVar);
		javaClass.addMethod().setConstructor(true).setBody(constructor);

		// Override the invoke method
		String paramName = "ijp";
		String invokeBody = ProbeTemplates.getInterceptorMethodBody(
				producerVar, paramName, events);

		MethodSource<?> invoke = javaClass.addMethod();

		invoke.setReturnType(Object.class);
		invoke.setName("invoke");
		invoke.addThrows("Throwable");
		invoke.addParameter(IntentJoinPoint.class, paramName);
		invoke.setBody(invokeBody);
		invoke.setPublic();

		return javaClass;
	}

}
