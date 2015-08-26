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
package pascani.lang.infrastructure.rabbitmq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Basic end point representation containing common factors from Consumer and
 * Producer roles, i.e., required information in both cases.
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class EndPoint {

	private final Channel channel;
	private final Connection connection;

	/**
	 * Creates a RabbitMQ end point; that is, a connection to the RabbitMQ
	 * server. This is commonly used by all RabbitMQ consumers and producers.
	 * 
	 * @param uri
	 *            The RabbitMQ connection URI
	 * @throws Exception
	 *             If something bat happens. Check exceptions in
	 *             {@link ConnectionFactory#setUri(String)},
	 *             {@link ConnectionFactory#newConnection()}, and
	 *             {@link Connection#createChannel()}
	 */
	public EndPoint(String uri) throws Exception {

		ConnectionFactory factory = new ConnectionFactory();
		factory.setAutomaticRecoveryEnabled(true);
		factory.setUri(uri);

		this.connection = factory.newConnection();
		this.channel = this.connection.createChannel();
	}

	public void close() throws IOException, TimeoutException {
		this.channel.close();
		this.connection.close();
	}

	public Channel channel() {
		return this.channel;
	}
}
