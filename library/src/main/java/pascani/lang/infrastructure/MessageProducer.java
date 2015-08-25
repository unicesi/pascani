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
package pascani.lang.infrastructure;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pascani.lang.Event;
import pascani.lang.util.EventProducer;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.eventbus.Subscribe;

/**
 * Abstract implementation of a message queue producer, containing only one
 * public method for posting {@link Event} objects to the infrastructure.
 * <p>
 * Subclasses must implement the actual code to post the event, by implementing
 * the communication with a message queuing system. The implementation of method
 * {@link MessageProducer#publish(Event)} should not be {@code public}, given
 * that the entry point for events should be always
 * {@link MessageProducer#produce(Event)}; this allows to filter messages not
 * supposed to be produced.
 * </p>
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public abstract class MessageProducer {

	/**
	 * The list of event classes configured to be produced
	 */
	protected final List<Class<? extends Event<?>>> configuredEventClasses;

	/**
	 * The logger
	 */
	protected final Logger logger = LogManager.getLogger(getClass());

	/**
	 * <p>
	 * <b>Note</b>: For the optional parameter {@code routingKey} DO NOT USE
	 * null, use empty string instead.
	 * </p>
	 * 
	 * @param classes
	 *            The list of event types of interest
	 */
	protected MessageProducer(final List<Class<? extends Event<?>>> classes) {
		this.configuredEventClasses = classes;
	}

	/**
	 * Listens for {@link Event}s produced by {@link EventProducer} objects.
	 * 
	 * @param event
	 *            The {@link EventProducer}-produced {@link Event} object
	 */
	@Subscribe public final void produce(Event<?> event) {
		if (isAcceptedEvent(event)) {
			try {
				publish(event);
			} catch (Exception e) {
				logger.error("Message " + event.identifier()
						+ " could not be published", e.getCause());
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Posts an Event object to the infrastructure.
	 * <p>
	 * <b>Note</b>: For the optional parameter {@code routingKey} DO NOT USE
	 * null, use empty string instead.
	 * </p>
	 * 
	 * @param event
	 *            The {@link EventProducer}-produced {@link Event} object
	 */
	protected abstract void publish(Event<?> event) throws Exception;

	/**
	 * Decides whether an {@link Event} instance must be or not posted to the
	 * infrastructure.
	 * 
	 * @param event
	 *            The event under judgment
	 * @return {@code true} if the event is a direct instance of any of the
	 *         configured classes, and {@code false} otherwise
	 */
	protected final boolean isAcceptedEvent(final Event<?> event) {
		Predicate<Class<?>> isInstance = new Predicate<Class<?>>() {
			public boolean apply(Class<?> clazz) {
				return event.getClass().isAssignableFrom(clazz);
			}
		};
		return Collections2.filter(this.configuredEventClasses, isInstance)
				.size() > 0;
	}

}
