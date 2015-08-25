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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.eventbus.EventBus;

import pascani.lang.Event;

/**
 * Abstract implementation of a message queue consumer, containing only methods
 * for starting consuming messages from a queue (
 * {@link MessageConsumer#startConsuming()}) and delegating the handling of the
 * already consumed message (instance of {@link Event}) to an interested
 * listener ({@link MessageConsumer#delegateEventHandling(Event)}).
 * 
 * <p>
 * The delegating can be done by using an {@link EventBus}.
 * </p>
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public abstract class MessageConsumer extends Thread {

	/**
	 * The logger
	 */
	protected final Logger logger = LogManager.getLogger(getClass());

	/**
	 * Starts consuming messages from the queue
	 */
	protected abstract void startConsuming();

	/**
	 * Delegates the event handling to an interested component; this may be
	 * done, for instance, by using an {@link EventBus} object.
	 * 
	 * @param event
	 *            The event to be handled
	 */
	public abstract void delegateEventHandling(Event<?> event);

	@Override public final void run() {
		startConsuming();
	}

}
