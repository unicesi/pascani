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
package org.pascani.dsl.lib.util;

import java.io.Serializable;
import java.net.URI;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;

/**
 * Language utilities
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class LanguageUtils {

	/**
	 * Tags a given value
	 * 
	 * @param value
	 *            The new variable value
	 * @param tags
	 *            The value tags
	 * @return The given value tagged
	 */
	public static <T extends Serializable> TaggedValue<T> tag(T value,
			Map<String, String> tags) {
		return new TaggedValue<T>(value, tags);
	}

	/**
	 * Creates a proxy to a remote REST service
	 * 
	 * @param baseUri
	 *            The service URI
	 * @param clazz
	 *            The service interface
	 * @return an instance of the specified class bound to the remote REST
	 *         service
	 */
	public static <T> T bindREST(URI baseUri, Class<T> clazz) {
		return JAXRSClientFactory.create((URI) baseUri, clazz);
	}

	/**
	 * Creates a proxy to a remote RMI service
	 * 
	 * @param host
	 *            The service URI
	 * @param port
	 *            The RMI registry port
	 * @param serviceName
	 *            The service name
	 * @param clazz
	 *            The service interface
	 * @return an instance of the specified class bound to the remote RMI
	 *         service
	 * @throws RemoteException
	 *             See {@link LocateRegistry#getRegistry(String)}
	 * @throws NotBoundException
	 *             See {@link Registry#lookup(String)}
	 */
	public static <T> T bindRMI(String host, int port, String serviceName,
			Class<T> clazz) throws RemoteException, NotBoundException {
		Registry registry = LocateRegistry.getRegistry(host, port);
		return clazz.cast(registry.lookup(serviceName));
	}

}
