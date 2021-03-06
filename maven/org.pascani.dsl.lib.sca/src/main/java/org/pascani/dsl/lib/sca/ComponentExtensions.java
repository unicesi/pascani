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
package org.pascani.dsl.lib.sca;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.ow2.frascati.remote.introspection.RemoteScaDomain;
import org.ow2.scesame.qoscare.core.scaspec.FraSCAti2QoSCAre;
import org.ow2.scesame.qoscare.core.scaspec.SCAComponent;
import org.ow2.scesame.qoscare.core.scaspec.SCADomain;
import org.ow2.scesame.qoscare.core.scaspec.SCAPort;
import org.ow2.scesame.qoscare.core.scaspec.SCAProperty;
import org.pascani.dsl.lib.util.Exceptions;

/**
 * Extension methods to ease introspection of SCA components by means of the
 * FraSCAti middleware.
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class ComponentExtensions {

	protected static final Map<URI, RemoteScaDomain> introspection = new HashMap<URI, RemoteScaDomain>();
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

	private static SCADomain getRemoteScaDomain(URI bindingUri) {
		RemoteScaDomain domain = getInstance(bindingUri, "introspection",
				introspection, RemoteScaDomain.class);
		return FraSCAti2QoSCAre.convert(bindingUri.toString(),
				domain.getDomainComposites());
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

	private static URI initializeDefaultUri() {
		URI uri = null;
		try {
			// From: AbstractBindingFactoryProcessor.BINDING_URI_BASE_DEFAULT_VALUE
			uri = new URI("http://localhost:8090");
		} catch (URISyntaxException e) {
			Exceptions.sneakyThrow(e);
		}
		return uri;
	}

}
