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
import java.util.UUID;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.osoa.sca.annotations.Scope;
import org.ow2.frascati.tinfi.api.IntentHandler;
import org.ow2.frascati.tinfi.api.IntentJoinPoint;

import pascani.compiler.templates.ExceptionProbeTemplates;
import pascani.compiler.util.NameProposal;
import pascani.lang.events.ExceptionEvent;
import pascani.lang.infrastructure.CustomProbe;
import pascani.lang.util.EventProducer;

/**
 * TODO: The probe must be initialized. The initialization code must be
 * generated
 * 
 * TODO: documentation
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class ExceptionProbeScaGenerator {

	/**
	 * The directory in which the java files will be written
	 */
	private final String path;

	/**
	 * The name of the event producer within the intercepter class
	 */
	public static final String PRODUCER_FIELD_NAME = "producer";

	public ExceptionProbeScaGenerator(final String directoryPath) {
		this.path = directoryPath;
	}

	public JavaClassSource interceptor(String packageName) {
		File directory = new File(this.path);
		String className = new NameProposal("ExceptionInterceptor.java",
				directory).getNewName();

		JavaClassSource javaClass = Roaster.create(JavaClassSource.class);

		// Add imports
		javaClass.addImport(EventProducer.class);
		javaClass.addImport(ExceptionEvent.class);
		javaClass.addImport(UUID.class);

		// Set general properties
		javaClass.setPackage(packageName);
		javaClass.addAnnotation(Scope.class).setStringValue("value",
				"COMPOSITE");
		javaClass.setName(className);
		javaClass.addInterface(IntentHandler.class);

		// Add an event producer
		FieldSource<?> field = javaClass.addField();
		field.setType(EventProducer.class.getSimpleName() + "<"
				+ ExceptionEvent.class.getSimpleName() + ">");
		field.setName(PRODUCER_FIELD_NAME).setPrivate().setFinal(true);

		String constructor = ExceptionProbeTemplates
				.getProducerInitialization(PRODUCER_FIELD_NAME);
		javaClass.addMethod().setConstructor(true).setBody(constructor);

		// Override the invoke method
		String paramName = "ijp";
		String invokeBody = ExceptionProbeTemplates.getInterceptorMethodBody(
				PRODUCER_FIELD_NAME, paramName);

		MethodSource<?> invoke = javaClass.addMethod();

		invoke.setReturnType(Object.class);
		invoke.setName("invoke");
		invoke.addThrows("Throwable");
		invoke.addParameter(IntentJoinPoint.class, paramName);
		invoke.setBody(invokeBody);
		invoke.setPublic();

		return javaClass;
	}

	public JavaClassSource probe(String packageName, final String uri,
			final String routingKey) {
		File directory = new File(this.path);
		String className = new NameProposal("ExceptionProbe.java", directory)
				.getNewName();

		JavaClassSource javaClass = Roaster.create(JavaClassSource.class);

		// Add imports
		javaClass.addImport(CustomProbe.class);
		javaClass.addImport(ExceptionEvent.class);

		// Set general properties
		javaClass.setPackage(packageName);
		javaClass.setName(className);
		javaClass.setSuperType(CustomProbe.class.getSimpleName() + "<"
				+ ExceptionEvent.class.getSimpleName() + ">");

		String constructorBody = ExceptionProbeTemplates.getProbeConstructor(
				uri, routingKey);

		MethodSource<?> constructor = javaClass.addMethod();
		constructor.setConstructor(true);
		constructor.setBody(constructorBody);
		constructor.addThrows(Exception.class);

		return javaClass;
	}

}
