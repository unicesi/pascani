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
package org.pascani.dsl.lib.infrastructure.rabbitmq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.SerializationUtils;
import org.pascani.dsl.lib.Event;
import org.pascani.dsl.lib.PascaniRuntime;
import org.pascani.dsl.lib.infrastructure.AbstractConsumer;
import org.pascani.dsl.lib.util.LocalEventProducer;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * Implementation of {@link AbstractConsumer} to work with the RabbitMQ queuing
 * system.
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class RabbitMQConsumer extends AbstractConsumer implements Consumer {

	/**
	 * An end point connected to a RabbitMQ server
	 */
	private final EndPoint endPoint;

	/**
	 * The consumer tag
	 */
	private final String consumerTag;

	/**
	 * The queue from which messages are consumed
	 */
	private final String queueName;

	/**
	 * An event producer for delegating the event handling
	 */
	private final LocalEventProducer<Event<?>> eventProducer;

	/**
	 * Creates a RabbitMQ message consumer
	 * 
	 * @param queue
	 *            The queue from which messages are consumed
	 * @param tag
	 *            The consumer tag for this consumer (a unique name)
	 * @param context
	 *            The context in which this consumer is used
	 * @throws Exception
	 *             if something bad happens! @see {@link EndPoint#EndPoint()}
	 */
	public RabbitMQConsumer(final String queue, final String tag,
			final PascaniRuntime.Context context) throws Exception {
		this(new EndPoint(), queue, tag, context);
	}
	
	/**
	 * Creates a RabbitMQ message consumer
	 * 
	 * @param endPoint
	 *            The RabbitMQ end point
	 * @param queue
	 *            The queue from which messages are consumed
	 * @param tag
	 *            The consumer tag for this consumer
	 * @param context
	 *            The context in which this consumer is used
	 * @throws IOException
	 *             If an I/O problem is encountered
	 * @throws TimeoutException
	 *             If there is a connection time out when connecting to the
	 *             RabbitMQ server
	 */
	public RabbitMQConsumer(final EndPoint endPoint, final String queue,
			final String tag, final PascaniRuntime.Context context)
			throws IOException, TimeoutException {

		this.endPoint = endPoint;
		this.queueName = queue;
		this.consumerTag = tag;
		this.eventProducer = new LocalEventProducer<Event<?>>(context);
	}
	
	/**
	 * Creates a RabbitMQ message consumer consuming from an exchange. To do
	 * this, an anonymous non-durable queue is declared and bound to the
	 * exchange.
	 * 
	 * @param exchange
	 *            The exchange from which messages are consumed
	 * @param routingKey
	 *            The routing key of interest
	 * @param tag
	 *            The consumer tag for this consumer
	 * @param context
	 *            The context in which this consumer is used
	 * @throws Exception
	 *             if something bad happens! @see {@link EndPoint#EndPoint()}
	 */
	public RabbitMQConsumer(final String exchange,
			final String routingKey, final String tag,
			final PascaniRuntime.Context context) throws Exception {
		this(new EndPoint(), exchange, routingKey, tag, context);
	}

	/**
	 * Creates a RabbitMQ message consumer consuming from an exchange. To do
	 * this, an anonymous non-durable queue is declared and bound to the
	 * exchange.
	 * 
	 * @param endPoint
	 *            The RabbitMQ end point
	 * @param exchange
	 *            The exchange from which messages are consumed
	 * @param routingKey
	 *            The routing key of interest
	 * @param tag
	 *            The consumer tag for this consumer
	 * @param context
	 *            The context in which this consumer is used
	 * @throws IOException
	 *             If an I/O problem is encountered
	 * @throws TimeoutException
	 *             If there is a connection time out when connecting to the
	 *             RabbitMQ server
	 */
	public RabbitMQConsumer(final EndPoint endPoint, final String exchange,
			final String routingKey, final String tag,
			final PascaniRuntime.Context context) throws IOException,
			TimeoutException {

		this.endPoint = endPoint;
		Channel channel = this.endPoint.channel();

		this.queueName = channel.queueDeclare().getQueue();
		channel.queueBind(this.queueName, exchange, routingKey);

		this.consumerTag = tag;
		this.eventProducer = new LocalEventProducer<Event<?>>(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.infrastructure.AbstractConsumer#startConsuming()
	 */
	@Override protected void startConsuming() {
		Channel channel = this.endPoint.channel();
		try {
			// start consuming (non auto-acknowledged) messages
			channel.basicConsume(this.queueName, false, this.consumerTag, this);
		} catch (IOException e) {
			logger.error("Error consuming message from RabbitMQ consumer", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.infrastructure.AbstractConsumer#delegateEventHandling(
	 * pascani.lang.Event)
	 */
	@Override public void delegateEventHandling(final Event<?> event) {
		this.eventProducer.post(event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rabbitmq.client.Consumer#handleDelivery(java.lang.String,
	 * com.rabbitmq.client.Envelope, com.rabbitmq.client.AMQP.BasicProperties,
	 * byte[])
	 */
	public void handleDelivery(final String consumerTag,
			final Envelope envelope, final BasicProperties props,
			final byte[] body) throws IOException {

		Event<?> event = (Event<?>) SerializationUtils.deserialize(body);
		internalDelegateHandling(event);

		// Acknowledge the received message after it has been handled
		this.endPoint.channel().basicAck(envelope.getDeliveryTag(), false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rabbitmq.client.Consumer#handleConsumeOk(java.lang.String)
	 */
	public void handleConsumeOk(final String consumerTag) {
		logger.info("The RabbitMQ consumer " + consumerTag
				+ " has started successfully");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rabbitmq.client.Consumer#handleCancelOk(java.lang.String)
	 */
	public void handleCancelOk(final String consumerTag) {
		logger.info("The RabbitMQ consumer " + consumerTag
				+ " has been canceled");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rabbitmq.client.Consumer#handleCancel(java.lang.String)
	 */
	public void handleCancel(final String consumerTag) throws IOException {
		logger.warn("The RabbitMQ consumer " + consumerTag
				+ " has been canceled by unknown reasons");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rabbitmq.client.Consumer#handleShutdownSignal(java.lang.String,
	 * com.rabbitmq.client.ShutdownSignalException)
	 */
	public void handleShutdownSignal(final String consumerTag,
			ShutdownSignalException sig) {
		logger.info("A channel or the connection of the RabbitMQ consumer "
				+ consumerTag + " has been shut down");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rabbitmq.client.Consumer#handleRecoverOk(java.lang.String)
	 */
	public void handleRecoverOk(final String consumerTag) {
		logger.info("The connection for the RabbitMQ consumer " + consumerTag
				+ " has been recovered successfully");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.infrastructure.AbstractConsumer#shutdown()
	 */
	@Override public void shutdown() throws IOException, TimeoutException {
		this.endPoint.close();
	}
}
