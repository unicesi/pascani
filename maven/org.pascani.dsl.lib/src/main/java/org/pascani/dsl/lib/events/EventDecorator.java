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
package org.pascani.dsl.lib.events;

import java.io.Serializable;

import org.pascani.dsl.lib.Event;

/**
 * An abstract implementation of the decorator pattern for {@link Event}
 * instances.
 * 
 * @param <T>
 *            The type of event to decorate
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public abstract class EventDecorator<T extends Event<? extends Serializable>>
		extends Event<Serializable> {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -7430259741904004568L;

	/**
	 * The event being decorated
	 */
	protected final T event;

	/**
	 * @param event
	 *            The event being decorated
	 */
	public EventDecorator(final T event) {
		super(event.transactionId());
		this.event = event;
	}

	/**
	 * @return the event being decorated
	 */
	public T decoratedEvent() {
		return this.event;
	}

	@Override public Serializable value() {
		return this.event.value();
	}

	/**
	 * Returns the string representation of the decorated event for logging
	 * purposes.
	 */
	@Override public String toString() {
		return this.event.toString();
	}

}
