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
package pascani.lang.util;

import java.lang.reflect.Proxy;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pascani.lang.PascaniRuntime;
import pascani.lang.infrastructure.ProbeProxy;
import pascani.lang.infrastructure.RpcClient;
import pascani.lang.infrastructure.rabbitmq.EndPoint;
import pascani.lang.infrastructure.rabbitmq.RabbitMQRpcClient;

/**
 * This implementation provides utility methods to bind external services
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class ServiceManager {

	/**
	 * The logger
	 */
	private final static Logger logger = LogManager
			.getLogger(ServiceManager.class);

	/**
	 * Registers the necessary properties to bind a SCA service
	 * 
	 * @param properties
	 * @param clazz
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static <T> T bindService(Map<String, Object> properties,
			Class<T> clazz) {

		// TODO: how to register these properties? (this method is not actually
		// called at runtime) -> Create production rules in the Pascani grammar

		/*
		 * At runtime, inside Pascani, it is not important to have the actual
		 * instance; it is enough just thinking it is there, for code completion
		 * and content assist purposes.
		 */
		return null;
	}

	/**
	 * Binds a {@link ProbeProxy} to a remote {@link Proxy} instance
	 * 
	 * @param routingKey
	 *            The probe routing key
	 * @return a probe proxy pointing to the specified routing key
	 */
	public static ProbeProxy bindProbe(final String routingKey) {
		ProbeProxy proxy = null;

		String uri = PascaniRuntime.getEnvironment().get("uri");
		String exchange = PascaniRuntime.getEnvironment()
				.get("rpc_exchange");

		try {
			EndPoint endPoint = new EndPoint(uri);
			RpcClient client = new RabbitMQRpcClient(endPoint, exchange,
					routingKey);
			proxy = new ProbeProxy(client);
		} catch (Exception e) {
			logger.error("Error binding probe " + routingKey, e);
		}

		return proxy;
	}

}
