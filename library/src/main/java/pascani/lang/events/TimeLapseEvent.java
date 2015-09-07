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
 * Implementation of {@link Event} for time-based data
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public final class TimeLapseEvent implements Event<Double> {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 6620284496813795698L;

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
	 * The actual initial time in epoch format
	 */
	private final long start;

	/**
	 * The actual final time in epoch format
	 */
	private final long end;

	/**
	 * The calculated value
	 */
	private final double value;

	/**
	 * Creates an instance having all the required data
	 * 
	 * @param transactionId
	 *            The transaction of which this event makes part
	 * @param start
	 *            The initial time
	 * @param end
	 *            The final time
	 */
	public TimeLapseEvent(final UUID transactionId, final long start,
			final long end) {
		this.id = UUID.randomUUID();
		this.transactionId = transactionId;
		this.start = start;
		this.end = end;
		this.value = this.end - this.start;
	}

	/**
	 * Creates an instance based on a previous instance. This is intended to be
	 * used when timestamps are taken at different places, i.e., the initial and
	 * final times are captured separately
	 * 
	 * @param event
	 *            A previous instance of {@code this} event
	 */
	public TimeLapseEvent(final TimeLapseEvent event, final long end) {
		this(event.transactionId, event.start, end);
	}

	/**
	 * Creates an instance having only the initial timestamp. The final
	 * timestamp is set to {@code 0}; to re-create the event with the final
	 * timestamp (when this is captured) a new instance must be created from the
	 * previous one, by calling constructor
	 * {@link TimeLapseEvent#TimeLapseEvent(TimeLapseEvent, long)}.
	 * 
	 * @see TimeLapseEvent#TimeLapseEvent(TimeLapseEvent, long)
	 * 
	 * @param start
	 *            The initial time
	 */
	public TimeLapseEvent(final UUID transactionId, final long start) {
		this(transactionId, start, 0);
	}

	/**
	 * Checks whether this {@link TimeLapseEvent} object was finished measuring
	 * within a given time window, i.e., {@code this.end} is contained in [
	 * {@code start} , {@code end}].
	 * 
	 * @param start
	 *            The initial timestamp of the time window
	 * @param end
	 *            The final timestamp of the time window
	 * @return {@code true} if the range [{@code start}, {@code end}] contains
	 *         the final timestamp {@code this} object
	 */
	public boolean isInTimeWindow(final long start, final long end) {
		return Range.closed(start, end).contains(this.end);
	}

	public UUID identifier() {
		return this.id;
	}

	public UUID transactionId() {
		return this.transactionId;
	}

	public Double value() {
		return this.value;
	}

	/**
	 * Returns the string representation of this event for logging purposes.
	 */
	@Override public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getCanonicalName() + "\t");
		sb.append(this.transactionId + "\t");
		sb.append(this.id + "\t");
		sb.append(this.start + "\t");
		sb.append(this.end + "\t");
		sb.append(value());

		return sb.toString();
	}

	/**
	 * The result is {@code true} if and only if the argument is not
	 * {@code null}, is a {@Link TimeLapseEvent} object and has the same
	 * identifier as {@code this} {@Link TimeLapseEvent}.
	 */
	@Override public boolean equals(final Object obj) {
		if ((null == obj) || (obj.getClass() != TimeLapseEvent.class))
			return false;

		TimeLapseEvent other = (TimeLapseEvent) obj;
		return this.id.equals(other.id);
	}

	/**
	 * The result is {@code -1} if {@code this} event was started before
	 * {@code o}, otherwise is {@code 1}. {@code 0} is returned when the
	 * argument is null or is not a {@link TimeLapseEvent}.
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(final Event<Double> o) {
		if (o != null && o instanceof TimeLapseEvent) {
			TimeLapseEvent other = (TimeLapseEvent) o;

			if (this.start < other.start) {
				return -1;
			} else {
				return 1;
			}
		}

		return 0;
	}

}
