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

import pascani.compiler.templates.TimeLapseProbeTemplates;
import pascani.compiler.util.NameProposal;
import pascani.lang.PascaniRuntime;
import pascani.lang.events.TimeLapseEvent;
import pascani.lang.util.EventProducer;

/**
 * This class generates the necessary source code to automatically measure
 * execution time in service executions. As the generated code is compliant with
 * the SCA specification, and the FraSCAti middleware, the mechanism for
 * intercepting the service execution is an Intent composite.
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class TimeLapseProbeScaGenerator extends InterceptorBasedProbeGenerator {

	public TimeLapseProbeScaGenerator(String directoryPath) {
		super(directoryPath, "TimeLapseProbe");
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
		String className = new NameProposal("TimeLapseInterceptor.java",
				directory).getNewName();

		JavaClassSource javaClass = Roaster.create(JavaClassSource.class);

		// Add imports
		javaClass.addImport(PascaniRuntime.class);
		javaClass.addImport(EventProducer.class);
		javaClass.addImport(TimeLapseEvent.class);
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
				+ TimeLapseEvent.class.getSimpleName() + ">");
		field.setName(producerVar).setPrivate().setFinal(true);

		String constructor = TimeLapseProbeTemplates
				.getProducerInitialization(producerVar);
		javaClass.addMethod().setConstructor(true).setBody(constructor);

		// Override the invoke method
		String paramName = "ijp";
		String invokeBody = TimeLapseProbeTemplates.getInterceptorMethodBody(
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
