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
package org.pascani.dsl.lib.infrastructure;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pascani.dsl.lib.Event;
import org.pascani.dsl.lib.util.Resumable;

import com.google.common.eventbus.EventBus;

/**
 * Abstract implementation of a message queue consumer, containing only methods
 * for starting consuming messages from a queue ({@link #startConsuming()}) and
 * delegating the handling of the already consumed message (instance of
 * {@link Event}) to an interested listener (
 * {@link #delegateEventHandling(Event)}).
 * 
 * <p>
 * The delegating can be done by using an {@link EventBus}.
 * </p>
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public abstract class AbstractConsumer implements Resumable {

	/**
	 * The logger
	 */
	protected final Logger logger = LogManager.getLogger(getClass());

	/**
	 * Starts consuming messages from the queue
	 */
	protected abstract void startConsuming();

	/**
	 * The variable representing the current state (stopped or not)
	 */
	private volatile boolean paused = false;

	/**
	 * Delegates the event handling to an interested component; this may be
	 * done, for instance, by using an {@link EventBus} object.
	 * 
	 * @param event
	 *            The event to be handled
	 */
	public abstract void delegateEventHandling(Event<?> event);
	
	/**
	 * Shutdowns connections
	 * 
	 * @throws Exception
	 *             If something bad happens!
	 */
	public abstract void shutdown() throws Exception;

	/**
	 * Delegates the event handling to an interested component, applying some
	 * validations first.
	 * 
	 * @param event
	 *            The event to be handled
	 */
	protected final void internalDelegateHandling(Event<?> event) {
		if (!isPaused())
			delegateEventHandling(event);
	}
	
	/**
	 * Starts consuming elements from the queue in a new {@link Thread}
	 */
	public void start() {
		new Thread() {
			public void run() {
				startConsuming();
			}
		}.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.util.Resumable#pause()
	 */
	public void pause() {
		this.paused = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.util.Resumable#resume()
	 */
	public void resume() {
		this.paused = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.util.Resumable#isPaused()
	 */
	public boolean isPaused() {
		return this.paused;
	}

}
