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
import org.ow2.frascati.mojo.ContributionUtil;
import org.ow2.frascati.remote.introspection.Deployment;
import org.ow2.frascati.remote.introspection.FileUtil;
import org.ow2.frascati.remote.introspection.Reconfiguration;
import org.ow2.frascati.remote.introspection.RemoteScaDomain;
import org.ow2.frascati.remote.introspection.resources.Component;
import org.ow2.frascati.remote.introspection.resources.Node;
import org.ow2.frascati.remote.introspection.resources.Port;
import org.ow2.frascati.remote.introspection.resources.Property;
import org.ow2.scesame.qoscare.core.scaspec.FraSCAti2QoSCAre;
import org.ow2.scesame.qoscare.core.scaspec.SCAComponent;
import org.ow2.scesame.qoscare.core.scaspec.SCADomain;
import org.ow2.scesame.qoscare.core.scaspec.SCANamedNode;
import org.ow2.scesame.qoscare.core.scaspec.SCAPort;
import org.ow2.scesame.qoscare.core.scaspec.SCAProperty;

/**
 * This implementation provides utility methods to ease introspection,
 * reconfiguration and deployment of SCA components
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class ComponentManager {
	
	public static class DeploymentBuilder {
		private final Collection<File> jars = new ArrayList<File>();
		private final Collection<String> deployables = new ArrayList<String>();
		private final String contributionName;
		
		public DeploymentBuilder(String contributionName) {
			this.contributionName = contributionName;
		}
		
		public DeploymentBuilder withJars(String... files) {
			for(String file : files)
				jars.add(new File(file));
			return this;
		}
		
		public DeploymentBuilder withDeployables(String... composites) {
			for(String composite : composites)
				deployables.add(composite);
			return this;
		}
		
		public boolean deploy() {
			return deploy(DEFAULT_BINDING_URI);
		}
		
		public boolean deploy(URI bindingUri) {
			Deployment instance = getDeploymentInstance(bindingUri);
			File workingDir = new File("./target");
			File contribFile = ContributionUtil.makeContribution(jars, deployables,
					contributionName, workingDir);
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

	protected static final Map<URI, RemoteScaDomain> introspection = new HashMap<URI, RemoteScaDomain>();
	protected static final Map<URI, Reconfiguration> reconfiguration = new HashMap<URI, Reconfiguration>();
	protected static final Map<URI, Deployment> deployment = new HashMap<URI, Deployment>();
	protected static URI DEFAULT_BINDING_URI = initializeDefaultUri();

	/**
	 * Looks up the given component name in the default FraSCAti runtime
	 * 
	 * @param componentName
	 *            The name of the running SCA component
	 * @return the Java class representing the Component complex type
	 */
	public static SCAComponent lookup(String componentName) {
		return lookup(componentName, DEFAULT_BINDING_URI);
	}

	/**
	 * Looks up the given component name in the specified FraSCAti runtime
	 * 
	 * @param componentName
	 *            The name of the running SCA component
	 * @param bindingUri
	 *            The URI where the FraSCAti runtime is running
	 * @return the Java class representing the Component complex type
	 */
	public static SCAComponent lookup(String componentName, URI bindingUri) {
		SCAComponent component = null;
		for (SCAComponent c : getRemoteScaDomain(bindingUri).getComposites()) {
			if (c.getName().equals(componentName)) {
				component = c;
				break;
			}
		}
		return component;
	}

	/**
	 * Searches the given service name in the collection of services of the
	 * given component
	 * 
	 * @param component
	 *            The component containing the service
	 * @param serviceName
	 *            The name of the service to search
	 * @return the Java class representing the Port complex type
	 */
	public static SCAPort service(SCAComponent component, String serviceName) {
		return port(component.getServices(), serviceName);
	}

	/**
	 * Searches the given reference name in the collection of references of the
	 * given component
	 * 
	 * @param component
	 *            The component containing the reference
	 * @param referenceName
	 *            The name of the reference to search
	 * @return the Java class representing the Port complex type
	 */
	public static SCAPort reference(SCAComponent component,
			String referenceName) {
		return port(component.getServices(), referenceName);
	}

	/**
	 * Searches the given property name in the collection of properties of the
	 * given component
	 * 
	 * @param component
	 *            The component containing the property
	 * @param propertyName
	 *            The name of the property to search
	 * @return the Java class representing the Property complex type
	 */
	public static SCAProperty property(SCAComponent component,
			String propertyName) {
		SCAProperty property = null;
		for (SCAProperty p : component.getProperties()) {
			if (p.getName().equals(propertyName)) {
				property = p;
				break;
			}
		}
		return property;
	}

	private static SCAPort port(Collection<SCAPort> ports, String portName) {
		SCAPort service = null;
		for (SCAPort port : ports) {
			if (port.getName().equals(portName)) {
				service = port;
				break;
			}
		}
		return service;
	}

	/**
	 * Execute a FraSCAti Script statement in the default FraSCAti runtime
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
	 * Execute a FraSCAti Script statement in the specified FraSCAti runtime
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
	
	public static DeploymentBuilder deploy(String contributionName) {
		return new DeploymentBuilder(contributionName);
	}

	private static URI initializeDefaultUri() {
		URI uri = null;
		try {
			uri = new URI("http://localhost:8090");
		} catch (URISyntaxException e) {
			// TODO: log the exception
			e.printStackTrace();
		}
		return uri;
	}

	private static SCADomain getRemoteScaDomain(URI bindingUri) {
		RemoteScaDomain domain = getInstance(bindingUri, "introspection",
				introspection, RemoteScaDomain.class);
		return FraSCAti2QoSCAre.convert(bindingUri.toString(),
				domain.getDomainComposites());
	}

	private static Reconfiguration getReconfigurationInstance(URI bindingUri) {
		return getInstance(bindingUri, "reconfig", reconfiguration,
				Reconfiguration.class);
	}
	
	private static Deployment getDeploymentInstance(URI bindingUri) {
		return getInstance(bindingUri, "deploy", deployment,
				Deployment.class);
	}
	
	private static <T> T getInstance(URI bindingUri, String path,
			Map<URI, T> map, Class<T> clazz) {
		T instance = map.get(bindingUri);
		if (instance == null) {
			instance = JAXRSClientFactory.create(bindingUri + "/" + path,
					clazz);
			map.put(bindingUri, instance);
		}
		return instance;
	}

}
