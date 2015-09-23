package pascani.compiler;

import java.io.File;
import java.util.List;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import pascani.compiler.templates.InterceptorBasedProbeTemplates;
import pascani.compiler.templates.ProbeTemplates;
import pascani.compiler.util.NameProposal;
import pascani.lang.Event;
import pascani.lang.PascaniRuntime;
import pascani.lang.Probe;
import pascani.lang.events.ExceptionEvent;
import pascani.lang.infrastructure.CustomProbe;

/**
 * 
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
	 * The RabbitMQ connection URI
	 */
	protected final String connectionURI;

	/**
	 * @param directoryPath
	 *            The directory in which the java files will be written
	 * @param probeName
	 *            The intended probe name; for instance "ExceptionProbe". It may
	 *            change if an existing file under the same parent directory has
	 *            the same name.
	 * @param connectionURI
	 *            The RabbitMQ connection URI
	 */
	public InterceptorBasedProbeGenerator(final String directoryPath,
			final String probeName, final String connectionURI) {
		this.path = directoryPath;
		this.probeName = probeName;
		this.connectionURI = connectionURI;
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
				connectionURI, exchange, routingKey, true, events);
	}

	/**
	 * Generates the (String) source code of the probe initialization
	 */
	public String probeInitialization() {

		return ProbeTemplates.getInitializationContrib(getProbeClassName(),
				connectionURI, "", "", false, null);
	}

	/**
	 * Generates a class extending {@link CustomProbe} with the corresponding
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
		javaClass.addImport(PascaniRuntime.class);
		javaClass.addImport(CustomProbe.class);

		// Set general properties
		javaClass.setPackage(packageName);
		javaClass.setName(className);
		javaClass.setSuperType(CustomProbe.class);

		String constructorBody = InterceptorBasedProbeTemplates
				.getProbeConstructor(this.connectionURI, routingKey);

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
