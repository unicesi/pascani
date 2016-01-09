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
package pascani.lang.infrastructure;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pascani.lang.Event;
import pascani.lang.util.LocalEventProducer;

import com.google.common.eventbus.Subscribe;

/**
 * TODO: Check if producing events blocks the execution. If so, make
 * {@link AbstractProducer} a {@link Thread}
 * 
 * Abstract implementation of a message queue producer, containing only one
 * public method for posting {@link Event} objects to the infrastructure.
 * <p>
 * Subclasses must implement the actual code to post the event, by implementing
 * the communication with a message queuing system. The implementation of method
 * {@link #publish(Event)} should not be {@code public}, given that the entry
 * point for events should be always {@link #produce(Event)}; this allows to
 * filter messages not supposed to be produced.
 * </p>
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public abstract class AbstractProducer {

	/**
	 * The list of event classes configured to be produced
	 */
	private Class<? extends Event<?>>[] acceptedTypes;

	/**
	 * The logger
	 */
	protected final Logger logger = LogManager.getLogger(getClass());

	/**
	 * Listens for {@link Event}s produced by {@link LocalEventProducer}
	 * objects.
	 * 
	 * @param event
	 *            The {@link LocalEventProducer}-produced {@link Event} object
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
	 *            The {@link LocalEventProducer}-produced {@link Event} object
	 * @throws Exception
	 *             in case something bad happens!
	 */
	protected abstract void publish(Event<?> event) throws Exception;

	/**
	 * Shutdowns connections
	 * 
	 * @throws Exception
	 *             If something bad happens!
	 */
	public abstract void shutdown() throws Exception;
	
	/**
	 * Establishes a set of event classes to be produced
	 * 
	 * @param acceptedTypes
	 *            The array of classes implementing {@link Event}
	 */
	public void acceptOnly(final Class<? extends Event<?>>... acceptedTypes) {
		this.acceptedTypes = acceptedTypes;
	}

	/**
	 * Decides whether an {@link Event} instance must be or not posted to the
	 * infrastructure
	 * 
	 * @param event
	 *            The event under judgment
	 * @return whether the event is a direct instance of any of the configured
	 *         classes or not
	 */
	protected boolean isAcceptedEvent(final Event<?> event) {
		boolean accepted = this.acceptedTypes == null;

		if (!accepted) {
			for (int i = 0; i < this.acceptedTypes.length && !accepted; i++) {
				accepted = this.acceptedTypes[i].isInstance(event);
			}
		}

		return accepted;
	}

}
