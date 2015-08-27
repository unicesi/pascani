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
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.osoa.sca.annotations.Scope;
import org.ow2.frascati.tinfi.api.IntentHandler;
import org.ow2.frascati.tinfi.api.IntentJoinPoint;

import pascani.compiler.templates.ExceptionProbeTemplates;
import pascani.compiler.util.NameProposal;
import pascani.lang.events.ExceptionEvent;
import pascani.lang.util.EventProducer;

/**
 * TODO: Generate the probe source code
 * 
 * TODO: documentation
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class ExceptionProbeScaGenerator {

	/**
	 * The directory in which the java file will be written
	 */
	private final String path;

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
		javaClass.addAnnotation(Scope.class).setStringValue("value", "COMPOSITE");
		javaClass.setName(className);
		javaClass.addInterface(IntentHandler.class);
		
		// Add an event producer
		javaClass.addField().setType(EventProducer.class)
				.setName(PRODUCER_FIELD_NAME).setPrivate().setFinal(true);

		String constructor = ExceptionProbeTemplates
				.getProducerInitialization(PRODUCER_FIELD_NAME);
		javaClass.addMethod().setConstructor(true).setBody(constructor);

		// Override the invoke method
		String paramName = "ijp";
		String invokeBody = ExceptionProbeTemplates.getInterceptorMethodBody(
				PRODUCER_FIELD_NAME, paramName);

		MethodSource<?> invoke = javaClass.addMethod();

		invoke.addAnnotation(Override.class);
		invoke.setReturnType(Object.class);
		invoke.setName("invoke");
		invoke.addThrows("Throwable");
		invoke.addParameter(IntentJoinPoint.class, paramName);
		invoke.setBody(invokeBody);

		return javaClass;
	}

}
