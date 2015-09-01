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

import org.apache.commons.lang3.SerializationUtils;

import pascani.lang.Event;
import pascani.lang.infrastructure.MessageConsumer;
import pascani.lang.util.EventProducer;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * Implementation of {@link MessageConsumer} to work with the RabbitMQ queuing
 * system.
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class RabbitMQConsumer extends MessageConsumer implements Consumer {

	/**
	 * An end point connected to a RabbitMQ queue
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
	private final EventProducer<Event<?>> eventProducer;

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
	 *             Is thrown if an I/O problem is encountered
	 * @throws TimeoutException
	 *             Is thrown if there is a connection time out when connecting
	 *             to the RabbitMQ server
	 */
	public RabbitMQConsumer(final EndPoint endPoint, final String queue,
			final String tag, final pascani.lang.Runtime.Context context)
			throws IOException, TimeoutException {

		this.endPoint = endPoint;
		this.queueName = queue;
		this.consumerTag = tag;
		this.eventProducer = new EventProducer<Event<?>>(context);
	}

	@Override protected void startConsuming() {
		Channel channel = this.endPoint.channel();

		try {
			// start consuming (non auto-acknowledged) messages
			channel.basicConsume(this.queueName, false, this.consumerTag, this);
		} catch (IOException e) {
			logger.error("Error consuming message from RabbitMQ consumer", e);
		}
	}

	@Override public void delegateEventHandling(final Event<?> event) {
		this.eventProducer.post(event);
	}

	public void handleDelivery(final String consumerTag,
			final Envelope envelope, final BasicProperties props,
			final byte[] body) throws IOException {

		Event<?> event = (Event<?>) SerializationUtils.deserialize(body);
		delegateEventHandling(event);

		// Acknowledge the received message after it has been handled
		this.endPoint.channel().basicAck(envelope.getDeliveryTag(), false);
	}

	public void handleConsumeOk(final String consumerTag) {
		logger.info("The RabbitMQ consumer " + consumerTag
				+ " has started successfully");
	}

	public void handleCancelOk(final String consumerTag) {
		logger.info("The RabbitMQ consumer " + consumerTag
				+ " has been canceled");
	}

	public void handleCancel(final String consumerTag) throws IOException {
		logger.warn("The RabbitMQ consumer " + consumerTag
				+ " has been canceled by unknown reasons");
	}

	public void handleShutdownSignal(final String consumerTag,
			ShutdownSignalException sig) {
		logger.error("A channel or the connection of the RabbitMQ consumer "
				+ consumerTag + " has been shut down");
	}

	public void handleRecoverOk(final String consumerTag) {
		logger.info("The connection for the RabbitMQ consumer " + consumerTag
				+ " has been recovered successfully");
	}
}
