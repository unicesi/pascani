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

import java.util.UUID;

import org.pascani.dsl.lib.Event;

import com.google.common.collect.Range;

/**
 * Implementation of {@link Event} for time-based data
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class TimeLapseEvent extends Event<Double> {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 6620284496813795698L;

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
		super(transactionId);
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
	 * @param end
	 *            The final time
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
	 * @param transactionId
	 *            The transaction of which this event makes part
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
	@Override public boolean isInTimeWindow(final long start, final long end) {
		return Range.closed(start, end).contains(this.end);
	}

	@Override public Double value() {
		return this.value;
	}
	
	public long start() {
		return this.start;
	}
	
	public long end() {
		return this.end;
	}

	/**
	 * Returns the string representation of this event for logging purposes.
	 */
	@Override public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getCanonicalName() + "\t");
		sb.append(this.transactionId + "\t");
		sb.append(this.identifier + "\t");
		sb.append(this.start + "\t");
		sb.append(this.end + "\t");
		sb.append(this.value());

		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see pascani.lang.Event#compareTo(pascani.lang.Event)
	 */
	@Override public int compareTo(final Event<Double> o) {
		int v = 0;
		TimeLapseEvent other = (TimeLapseEvent) o;
		
		v = compareInt(this.start, other.start);
		if (v != 0) return v;

		v = compareInt(this.end, other.end);
		if (v != 0) return v;

		v = this.identifier.compareTo(other.identifier);
		if (v != 0) return v;
		
		return v;
	}

}
