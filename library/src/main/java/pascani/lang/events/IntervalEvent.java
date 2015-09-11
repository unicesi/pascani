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

/**
 * Implementation of {@link Event} for periodic events based on chronological
 * expressions
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class IntervalEvent extends Event<String> {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -9029356694184256904L;

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
		super(transactionId);
		this.expression = expression;
	}

	@Override public String value() {
		return this.expression;
	}

	/**
	 * Returns the string representation of this event for logging purposes.
	 */
	@Override public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getCanonicalName() + "\t");
		sb.append(this.transactionId + "\t");
		sb.append(this.identifier + "\t");
		sb.append(this.timestamp + "\t");
		sb.append(value().toString());

		return sb.toString();
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
