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
package pascani.lang.events;

import java.util.UUID;

import pascani.lang.Event;

import com.google.common.collect.Range;

/**
 * Implementation of {@link Event} for periodic events based on chronological
 * expressions
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class IntervalEvent implements Event<String> {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -9029356694184256904L;

	/**
	 * The universally unique identifier of this event
	 */
	private final UUID id;

	/**
	 * The universally unique identifier of the transaction of which this event
	 * is part
	 */
	private final UUID transactionId;

	/**
	 * The timestamp when this event was raised, in nanoseconds
	 */
	private final long timestamp;

	/**
	 * The scheduler expression
	 */
	private final String expression;

	/**
	 * Creates an instance having all of the parameters
	 * 
	 * @param transactionId
	 *            The transaction of which this event makes part
	 * @param expression
	 *            The scheduler expression
	 */
	public IntervalEvent(final UUID transactionId, final String expression) {
		this.timestamp = System.nanoTime();
		this.id = UUID.randomUUID();
		this.transactionId = transactionId;
		this.expression = expression;
	}

	public UUID identifier() {
		return this.id;
	}

	public UUID transactionId() {
		return this.transactionId;
	}

	public String value() {
		return this.expression;
	}

	public boolean isInTimeWindow(final long start, final long end) {
		return Range.closed(start, end).contains(this.timestamp);
	}

	/**
	 * Returns the string representation of this event for logging purposes.
	 */
	@Override public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getCanonicalName() + "\t");
		sb.append(this.transactionId + "\t");
		sb.append(this.id + "\t");
		sb.append(this.timestamp + "\t");
		sb.append(value().toString());

		return sb.toString();
	}

	/**
	 * The result is {@code true} if and only if the argument is not
	 * {@code null}, is a {@Link IntervalEvent} object and has the same
	 * identifier as {@code this} {@Link IntervalEvent}.
	 */
	@Override public boolean equals(final Object obj) {
		if ((null == obj) || (obj.getClass() != IntervalEvent.class))
			return false;

		IntervalEvent other = (IntervalEvent) obj;
		return this.id.equals(other.id);
	}

	/**
	 * The result is {@code -1} if {@code this} event was started before
	 * {@code o}, otherwise is {@code 1}. {@code 0} is returned when the
	 * argument is null or is not a {@link IntervalEvent}.
	 *
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(final Event<String> o) {
		if (o != null && o instanceof IntervalEvent) {
			IntervalEvent other = (IntervalEvent) o;

			if (this.timestamp < other.timestamp) {
				return -1;
			} else {
				return 1;
			}
		}

		return 0;
	}

}
