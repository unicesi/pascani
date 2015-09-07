package pascani.compiler;

import java.io.File;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import pascani.compiler.templates.InterceptorBasedProbeTemplates;
import pascani.compiler.util.NameProposal;
import pascani.lang.PascaniRuntime;
import pascani.lang.Probe;
import pascani.lang.events.ExceptionEvent;
import pascani.lang.infrastructure.CustomProbe;

/**
 * TODO: The probe must be initialized. The initialization code must be
 * generated
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
	 * @return an object encapsulating the java source code
	 */
	public abstract JavaClassSource interceptor(String packageName);

	/**
	 * Generates a class extending {@link CustomProbe} with the corresponding
	 * connection information.
	 * 
	 * @param packageName
	 *            The package of the generated java class
	 * @param uri
	 *            The RabbitMQ connection URI
	 * @param routingKey
	 *            The routing key designated to this probe
	 * @return an object encapsulating the java source code
	 */
	public JavaClassSource probe(final String packageName, final String uri,
			final String routingKey) {

		File directory = new File(this.path);
		String className = new NameProposal(this.probeName + ".java", directory)
				.getNewName();

		JavaClassSource javaClass = Roaster.create(JavaClassSource.class);

		// Add imports
		javaClass.addImport(PascaniRuntime.class);
		javaClass.addImport(CustomProbe.class);
		javaClass.addImport(ExceptionEvent.class);

		// Set general properties
		javaClass.setPackage(packageName);
		javaClass.setName(className);
		javaClass.setSuperType(CustomProbe.class.getSimpleName() + "<"
				+ ExceptionEvent.class.getSimpleName() + ">");

		String constructorBody = InterceptorBasedProbeTemplates
				.getProbeConstructor(uri, routingKey);

		MethodSource<?> constructor = javaClass.addMethod();
		constructor.setConstructor(true);
		constructor.setBody(constructorBody);
		constructor.addThrows(Exception.class);

		return javaClass;
	}

}
