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
package org.pascani.dsl.dbmapper;

import org.pascani.dsl.lib.Event;
import org.pascani.dsl.lib.PascaniRuntime;
import org.pascani.dsl.lib.PascaniRuntime.Context;
import org.pascani.dsl.lib.infrastructure.AbstractConsumer;
import org.pascani.dsl.lib.infrastructure.rabbitmq.RabbitMQConsumer;

import com.google.common.eventbus.Subscribe;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Miguel Jiménez - Initial contribution and API
 */
public class EventSerializer {

	/**
	 * The event consumer connected to the RabbitMQ server
	 */
	private final AbstractConsumer consumer;

	/**
	 * The object mediating communication with the database
	 */
	private final DbInterface db;

	/**
	 * The type of event this listener subscribes to
	 */
	private final Class<? extends Event<?>> eventType;

	/**
	 * The logger
	 */
	private static final Logger logger = LogManager
			.getLogger(EventSerializer.class);

	/**
	 * Creates and start an event listener connected to a RabbitMQ exchange
	 * 
	 * @param exchange
	 *            The RabbitMQ exchange to which the event producer is sending
	 *            events
	 * @param routingKey
	 *            The RabbitMQ routingKey of the event producer
	 * @param eventType
	 *            The type of event this listener subscribes to
	 * @throws Exception
	 *             if something bad happens! @see {@link RabbitMQConsumer}
	 */
	public EventSerializer(final String exchange, final String routingKey,
			final Class<? extends Event<?>> eventType, final DbInterface db)
			throws Exception {
		PascaniRuntime.getRuntimeInstance(Context.LIBRARY)
				.registerEventListener(this);
		final String tag = "dbmapper-" + eventType.getCanonicalName();
		this.consumer = new RabbitMQConsumer(exchange, routingKey, tag,
				Context.LIBRARY);
		this.db = db;
		this.eventType = eventType;
		// Open connection and start consuming events
		this.db.openConnection();
		this.consumer.start();
	}

	/**
	 * Receives an event and saves it in the database
	 * 
	 * @param event
	 *            The event to save
	 */
	@Subscribe public void receiveEvent(final Event<?> event) {
		if (this.eventType.isInstance(event)) {
			try {
				this.db.save(event);
			} catch (Exception e) {
				logger.error("Error saving event in the database", e);
			}
		}
	}

	/**
	 * Shutdowns connections
	 * 
	 * @throws Exception
	 *             If something bad happens!
	 */
	public void shutdown() throws Exception {
		this.consumer.shutdown();
		this.db.closeConnection();
	}

}
