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
package org.pascani.dsl.lib.events;

import java.io.Serializable;

import org.pascani.dsl.lib.Event;
import org.pascani.dsl.lib.util.EventMapper;

/**
 * A concrete implementation of the decorator pattern. This class associates the
 * decorated event with a name (key); this is useful for mapping certain events
 * under the same name.
 * 
 * @see EventMapper
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class NamedEventDecorator extends
		EventDecorator<Event<? extends Serializable>> {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 178748327925632482L;

	/**
	 * The given key to the named event
	 */
	private final String key;

	/**
	 * @param key
	 *            The given key to the named event
	 * @param event
	 *            The event being named
	 */
	public NamedEventDecorator(final String key,
			final Event<? extends Serializable> event) {
		super(event);
		this.key = key;
	}

	/**
	 * @return the given key to the named event
	 */
	public String key() {
		return this.key;
	}

	/**
	 * Returns the string representation of this event for logging purposes.
	 */
	@Override public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getCanonicalName() + "\t");
		sb.append(this.key + "\t");
		sb.append("[" + this.event.toString() + "]");

		return sb.toString();
	}

}
