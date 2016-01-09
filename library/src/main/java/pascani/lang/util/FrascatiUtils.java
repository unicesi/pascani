package pascani.lang.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptException;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.ow2.frascati.binding.factory.AbstractBindingFactoryProcessor;
import org.ow2.frascati.mojo.ContributionUtil;
import org.ow2.frascati.remote.introspection.Deployment;
import org.ow2.frascati.remote.introspection.FileUtil;
import org.ow2.frascati.remote.introspection.Reconfiguration;
import org.ow2.frascati.remote.introspection.resources.Component;
import org.ow2.frascati.remote.introspection.resources.Node;
import org.ow2.frascati.remote.introspection.resources.Port;
import org.ow2.frascati.remote.introspection.resources.Property;
import org.ow2.scesame.qoscare.core.scaspec.FraSCAti2QoSCAre;
import org.ow2.scesame.qoscare.core.scaspec.SCANamedNode;

public class FrascatiUtils {

	public static class DeploymentBuilder {
		private final Collection<File> jars = new ArrayList<File>();
		private final Collection<String> deployables = new ArrayList<String>();
		private final String contributionName;

		public DeploymentBuilder(String contributionName) {
			this.contributionName = contributionName;
		}

		public DeploymentBuilder withJars(String... files) {
			for (String file : files)
				jars.add(new File(file));
			return this;
		}

		public DeploymentBuilder withDeployables(String... composites) {
			for (String composite : composites)
				deployables.add(composite);
			return this;
		}

		public boolean deploy() {
			return deploy(DEFAULT_BINDING_URI);
		}

		public boolean deploy(URI bindingUri) {
			Deployment instance = getDeploymentInstance(bindingUri);
			File workingDir = new File("./target");
			File contribFile = ContributionUtil.makeContribution(jars,
					deployables, contributionName, workingDir);
			String base64 = null;
			try {
				base64 = FileUtil.getStringFromFile(contribFile);
			} catch (IOException e) {
				// TODO: log the exception
				e.printStackTrace();
			}
			return instance.deploy(base64) == 0;
		}
	}

	protected static final Map<URI, Reconfiguration> reconfiguration = new HashMap<URI, Reconfiguration>();
	protected static final Map<URI, Deployment> deployment = new HashMap<URI, Deployment>();
	protected static URI DEFAULT_BINDING_URI = initializeDefaultUri();

	/**
	 * Executes a FraSCAti Script statement in the default FraSCAti runtime
	 * 
	 * @param script
	 *            The script to execute
	 * @param bindingUri
	 *            The URI where the FraSCAti runtime is running
	 * @return The result of evaluating the given script
	 * @throws ScriptException
	 *             if something bad happens!
	 */
	public static Collection<SCANamedNode> eval(String script)
			throws ScriptException {
		return eval(script, DEFAULT_BINDING_URI);
	}

	/**
	 * Executes a FraSCAti Script statement in the specified FraSCAti runtime.
	 * Examples of use can be found here: http://goo.gl/my3xck
	 * 
	 * @param script
	 *            The script to execute
	 * @param bindingUri
	 *            The URI where the FraSCAti runtime is running
	 * @return The result of evaluating the given script
	 * @throws ScriptException
	 *             if something bad happens!
	 */
	public static Collection<SCANamedNode> eval(String script, URI bindingUri)
			throws ScriptException {
		List<SCANamedNode> nodes = new ArrayList<SCANamedNode>();
		for (Node node : getReconfigurationInstance(bindingUri).eval(script)) {
			if (node instanceof Component)
				nodes.add(FraSCAti2QoSCAre.convertComponent((Component) node));
			else if (node instanceof Property)
				nodes.add(FraSCAti2QoSCAre.convertProperty((Property) node));
			else if (node instanceof Port)
				nodes.add(FraSCAti2QoSCAre.convertPort((Port) node));
		}
		return nodes;
	}

	/**
	 * Loads a FraSCAti Script procedure into the default FraSCAti runtime.
	 * 
	 * @param file
	 *            The file containing the FScript procedure to load
	 * @return A String containing the names of the registered procedures
	 * @throws ScriptException
	 *             if something bad happens!
	 */
	public static String registerScript(File file)
			throws IOException, ScriptException {
		return registerScript(file, DEFAULT_BINDING_URI);
	}

	/**
	 * Loads a FraSCAti Script procedure into the specified FraSCAti runtime.
	 * 
	 * @param file
	 *            The file containing the FScript procedure to load
	 * @param bindingUri
	 *            The URI where the FraSCAti runtime is running
	 * @return A String containing the names of the registered procedures
	 * @throws ScriptException
	 *             if something bad happens!
	 */
	public static String registerScript(File file, URI bindingUri)
			throws IOException, ScriptException {
		return registerScript(new String(FileUtil.getBytesFromFile(file)),
				bindingUri);
	}

	/**
	 * Loads a FraSCAti Script procedure into the default FraSCAti runtime.
	 * 
	 * @param script
	 *            The FScript procedure to load
	 * @return TODO
	 * @throws ScriptException
	 *             if something bad happens!
	 */
	public static String registerScript(String script) throws ScriptException {
		return registerScript(script, DEFAULT_BINDING_URI);
	}

	/**
	 * Loads a FraSCAti Script procedure into the specified FraSCAti runtime.
	 * 
	 * @param script
	 *            The FScript procedure to load
	 * @param bindingUri
	 *            The URI where the FraSCAti runtime is running
	 * @return A String containing the names of the registered procedures
	 * @throws ScriptException
	 *             if something bad happens!
	 */
	public static String registerScript(String script, URI bindingUri)
			throws ScriptException {
		return getReconfigurationInstance(bindingUri).register(script);
	}

	/**
	 * Deploys a SCA contribution
	 * 
	 * @param contributionName
	 *            The name of the zip file to be generated
	 * @return A builder to configure the contribution deployment
	 */
	public static DeploymentBuilder deploy(String contributionName) {
		return new DeploymentBuilder(contributionName);
	}

	private static URI initializeDefaultUri() {
		URI uri = null;
		try {
			uri = new URI(
					AbstractBindingFactoryProcessor.BINDING_URI_BASE_DEFAULT_VALUE);
		} catch (URISyntaxException e) {
			// TODO: log the exception
			e.printStackTrace();
		}
		return uri;
	}

	private static Reconfiguration getReconfigurationInstance(URI bindingUri) {
		return getInstance(bindingUri, "reconfig", reconfiguration,
				Reconfiguration.class);
	}

	private static Deployment getDeploymentInstance(URI bindingUri) {
		return getInstance(bindingUri, "deploy", deployment, Deployment.class);
	}

	private static <T> T getInstance(URI bindingUri, String path,
			Map<URI, T> map, Class<T> clazz) {
		T instance = map.get(bindingUri);
		if (instance == null) {
			instance = JAXRSClientFactory.create(bindingUri + "/" + path,
					clazz);
			map.put(bindingUri, instance);
		}
		try {
			/*
			 * Wait for 1 second to be sure that the FScript engine is
			 * initialized. From: http://goo.gl/my3xck
			 */
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO log the exception
			e.printStackTrace();
		}
		return instance;
	}

}
