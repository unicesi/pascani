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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import pascani.lang.Event;

import com.google.common.collect.Range;

/**
 * Implementation of {@link Event} for exceptions registry
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class ExceptionEvent implements Event<Exception> {

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
	 * The timestamp when the exception is caught, in nanoseconds
	 */
	private final long timestamp;

	/**
	 * The thrown exception
	 */
	private final Exception exception;

	public ExceptionEvent(final UUID transactionId, final Exception exception) {
		this.id = UUID.randomUUID();
		this.transactionId = transactionId;
		this.exception = exception;
		this.timestamp = System.nanoTime();
	}

	public UUID identifier() {
		return this.id;
	}

	public UUID transactionId() {
		return this.transactionId;
	}

	public Exception value() {
		return this.exception;
	}

	public List<Event<Exception>> children() {
		return Collections.emptyList();
	}

	public boolean isInTimeWindow(final long start, final long end) {
		return Range.closed(start, end).contains(this.timestamp);
	}

	/**
	 * Returns the string representation of this event for logging purposes. The
	 * string contains [this class name, id, timestamp, end, value] separated by
	 * a tab character.
	 */
	@Override public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getCanonicalName() + "\t");
		sb.append(identifier().toString() + "\t");
		sb.append(this.timestamp + "\t");
		sb.append(value().toString());

		return sb.toString();
	}

	/**
	 * The result is {@code -1} if {@code this} event was raised before
	 * {@code o}. If {@code this} was raised after {@code o}, the result is
	 * {@code 1}. Otherwise, the result is {@code 0}.
	 *
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(final Event<Exception> o) {
		if (o != null && o instanceof ExceptionEvent) {
			ExceptionEvent other = (ExceptionEvent) o;

			if (this.timestamp < other.timestamp) {
				return -1;
			} else if (this.timestamp > other.timestamp) {
				return 1;
			} else {
				return 0;
			}
		}

		return 0;
	}

}
