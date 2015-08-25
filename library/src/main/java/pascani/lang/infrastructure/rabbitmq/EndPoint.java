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

	/**
	 * @author Miguel Jiménez - Initial contribution and API
	 */
	public static class Builder {

		private final String host;
		private final int port;
		private final String virtualHost;
		private String user;
		private String pass;

		public Builder(String host, int port, String virtualHost) {
			this.host = host;
			this.port = port;
			this.virtualHost = virtualHost;
		}

		public Builder withAuthentication(String username, String password) {
			this.user = username;
			this.pass = password;
			return this;
		}

		public EndPoint build() throws IOException, TimeoutException {
			return new EndPoint(this);
		}
	}

	private final Channel channel;
	private final Connection connection;

	/**
	 * Creates a RabbitMQ end point; that is, a connection to the RabbitMQ
	 * server. This is commonly used by all RabbitMQ consumers and producers.
	 * 
	 * @param b
	 *            The {@link Builder} instance
	 * @throws IOException
	 *             Is thrown if an I/O problem is encountered
	 * @throws TimeoutException
	 *             Is thrown if there is a connection timeout when connecting to
	 *             the RabbitMQ server
	 */
	public EndPoint(final Builder b) throws IOException,
			TimeoutException {
		this(b.host, b.port, b.virtualHost, b.user, b.pass);
	}

	/**
	 * Creates a RabbitMQ end point; that is, a connection to the RabbitMQ
	 * server. This is commonly used by all RabbitMQ consumers and producers.
	 * 
	 * @param host
	 *            The RabbitMQ server's host
	 * @param port
	 *            The RabbitMQ server's port
	 * @param virtualHost
	 *            The RabbitMQ server's virtual host. By default this is: "/"
	 * @param username
	 *            The RabbitMQ server username
	 * @param password
	 *            The RabbitMQ server password
	 * @throws IOException
	 *             Is thrown if an I/O problem is encountered
	 * @throws TimeoutException
	 *             Is thrown if there is a connection timeout when connecting to
	 *             the RabbitMQ server
	 */
	private EndPoint(String host, int port, String virtualHost,
			String username, String password) throws IOException,
			TimeoutException {

		ConnectionFactory factory = new ConnectionFactory();

		factory.setAutomaticRecoveryEnabled(true);
		factory.setHost(host);
		factory.setPort(port);
		factory.setVirtualHost(virtualHost);
		factory.setConnectionTimeout(0);

		if (username != null && password != null) {
			// Notice that he default "guest" user can only access the loopback
			// address (i.e., localhost). More info:
			// http://www.rabbitmq.com/access-control.html
			factory.setUsername(username);
			factory.setPassword(password);
		}

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
