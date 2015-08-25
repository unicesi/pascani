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
import java.util.List;

import org.apache.commons.lang3.SerializationUtils;

import pascani.lang.Event;
import pascani.lang.infrastructure.MessageProducer;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;

/**
 * Implementation of {@link MessageProducer} to work with the RabbitMQ queuing
 * system.
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public final class RabbitMQProducer extends MessageProducer {

	/**
	 * An end point connected to a RabbitMQ queue
	 */
	private final EndPoint endPoint;

	/**
	 * The exchange to which messages are sent
	 */
	private String exchange;

	/**
	 * An optional routing key (queue name) for directly sending messages to
	 * certain consumers
	 */
	private String routingKey;

	/**
	 * Creates a RabbitMQ producer from an end point and a list of accepted
	 * events' classes. Additionally, declares an exchange for this producer's
	 * default channel. If it does not exist, it will be created on the server.
	 * 
	 * @param endPoint
	 *            The configured RabbitMQ end point
	 * @param classes
	 *            The list of event types of interest
	 * @param exchange
	 *            The exchange to which messages are sent
	 * @param routingKey
	 *            An optional queue name for directly sending the messages
	 * @param durableExchange
	 *            Whether the exchange is durable or not
	 * 
	 * @throws IOException
	 *             If an error with the exchange declaration is encountered
	 */
	public RabbitMQProducer(final EndPoint endPoint,
			final List<Class<? extends Event<?>>> classes,
			final String exchange, final String routingKey,
			final boolean durableExchange) throws IOException {
		super(classes);
		this.endPoint = endPoint;
		this.exchange = exchange == null ? "" : exchange;
		this.routingKey = routingKey == null ? "" : routingKey;
		this.endPoint.channel().exchangeDeclare(this.exchange, "direct",
				durableExchange);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pascani.runtime.infrastructure.MessageProducer#publish(pascani.lang.Event
	 * )
	 */
	@Override protected void publish(Event<?> event) throws IOException {
		byte[] data = SerializationUtils.serialize(event);
		BasicProperties props = new BasicProperties.Builder()
				.messageId(event.identifier().toString()).deliveryMode(1)
				.priority(0).type(event.getClass().getCanonicalName()).build();

		Channel c = endPoint.channel();
		c.basicPublish(this.exchange, this.routingKey, props, data);
	}

}
