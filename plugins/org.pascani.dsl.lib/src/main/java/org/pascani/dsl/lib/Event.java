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
package org.pascani.dsl.lib;

import java.io.Serializable;
import java.util.UUID;

import com.google.common.collect.Range;

/**
 * Standard abstract implementation for simple events
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public abstract class Event<T> implements Comparable<Event<T>>, Serializable {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 4932084076805806298L;

	/**
	 * The universally unique identifier of this event
	 */
	protected final UUID identifier;

	/**
	 * The universally unique identifier of the transaction of which this event
	 * is part
	 */
	protected final UUID transactionId;

	/**
	 * The timestamp when this event is raised, in nanoseconds
	 */
	protected final long timestamp;

	public Event(final UUID transactionId) {
		this.timestamp = System.nanoTime();
		this.identifier = UUID.randomUUID();
		this.transactionId = transactionId;
	}

	/**
	 * @return the universal unique identifier of this event
	 */
	public UUID identifier() {
		return this.identifier;
	}

	/**
	 * @return The universally unique identifier of the transaction of which
	 *         this event is part
	 */
	public UUID transactionId() {
		return this.transactionId;
	}
	
	/**
	 * @return the timestamp when this event is raised, in nanoseconds
	 */
	public long timestamp() {
		return this.timestamp;
	}

	/**
	 * @return the value of this event
	 */
	public abstract T value();

	/**
	 * Checks whether this {@link Event} was raised within a given time window,
	 * i.e., {@code this.timestamp} is contained in [ {@code start} ,
	 * {@code end}].
	 * 
	 * @param start
	 *            The initial timestamp of the time window
	 * @param end
	 *            The final timestamp of the time window
	 * @return Whether the range [{@code start}, {@code end}] contains the final
	 *         timestamp this object
	 */
	public boolean isInTimeWindow(long start, long end) {
		return Range.closed(start, end).contains(this.timestamp);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.identifier == null) ? 0 : this.identifier.hashCode());
		return result;
	}
	
	/**
	 * The result is {@code true} if and only if the argument is not
	 * {@code null}, is an {@link Event} object with the same identifier
	 * as {@code this} instance.
	 */
	@Override public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		else if ((null == obj) || (obj.getClass() != this.getClass()))
			return false;

		@SuppressWarnings("unchecked")
		Event<T> other = (Event<T>) obj;
		return this.compareTo(other) == 0;
	}
	
	protected int compareInt(long i1, long i2) {
		if (i1 < i2) {
			return -1;
		} else if (i1 > i2) {
			return +1;
		} else {
			return 0;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see pascani.lang.Event#compareTo(pascani.lang.Event)
	 */
	public int compareTo(final Event<T> o) {
		int v = 0;
		
		v = compareInt(this.timestamp, o.timestamp);
		if (v != 0) return v;

		v = this.identifier.compareTo(o.identifier);
		if (v != 0) return v;
		
		return v;
	}
	
}
