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
package pascani.lang;

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

	/**
	 * The result is {@code true} if and only if the argument is not
	 * {@code null}, is an {@Link Event} object with the same identifier
	 * as {@code this} instance.
	 */
	@Override public boolean equals(final Object obj) {
		if ((null == obj) || (obj.getClass() != this.getClass()))
			return false;

		Event<?> other = (Event<?>) obj;
		return this.identifier.equals(other.identifier);
	}

	/**
	 * The result is {@code -1} if {@code this} event was started before
	 * {@code o}, otherwise is {@code 1}. {@code 0} is returned when the object
	 * is null, or when the objects are not instances of the same class.
	 *
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(final Event<T> o) {

		if (o != null && this.getClass() == o.getClass()) {
			if (this.timestamp < o.timestamp)
				return -1;
			else
				return 1;
		}

		return 0;
	}
}
