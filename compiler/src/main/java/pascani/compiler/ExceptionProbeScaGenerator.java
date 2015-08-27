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
import pascani.lang.util.EventProducer;

/**
 * This class generates the necessary source code to automatically caught
 * {@link Exception} objects in service executions. As the generated code is
 * compliant with the SCA specification, and the FraSCAti middleware, the
 * mechanism for intercepting the service execution is an Intent composite.
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class ExceptionProbeScaGenerator extends InterceptorBasedProbeGenerator {

	public ExceptionProbeScaGenerator(String directoryPath) {
		super(directoryPath, "ExceptionProbe");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pascani.compiler.InterceptorBasedProbeGenerator#interceptor(java.lang
	 * .String)
	 */
	@Override public JavaClassSource interceptor(final String packageName) {
		File directory = new File(path);
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
		String producerVar = "producer";

		FieldSource<?> field = javaClass.addField();
		field.setType(EventProducer.class.getSimpleName() + "<"
				+ ExceptionEvent.class.getSimpleName() + ">");
		field.setName(producerVar).setPrivate().setFinal(true);

		String constructor = ExceptionProbeTemplates
				.getProducerInitialization(producerVar);
		javaClass.addMethod().setConstructor(true).setBody(constructor);

		// Override the invoke method
		String paramName = "ijp";
		String invokeBody = ExceptionProbeTemplates.getInterceptorMethodBody(
				producerVar, paramName);

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
