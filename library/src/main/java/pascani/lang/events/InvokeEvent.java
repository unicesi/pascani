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

import java.util.Arrays;
import java.util.UUID;

import pascani.lang.Event;

import com.google.common.collect.Range;

/**
 * Implementation of {@link Event} for raising events each time a method is
 * invoked.
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class InvokeEvent implements Event<Long> {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 8016321382976600130L;

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
	 * The timestamp when the method is invoked, in nanoseconds
	 */
	private final long timestamp;

	/**
	 * The class providing the method
	 */
	private final Class<?> clazz;

	/**
	 * The name of the method
	 */
	private final String methodName;

	/**
	 * The formal parameters of the method
	 */
	private final Class<?>[] parameters;

	/**
	 * The arguments with which the method was called
	 */
	private final Object[] arguments;

	/**
	 * Creates an instance having all of the parameters
	 * 
	 * @param transactionId
	 *            The transaction of which this event makes part
	 * @param timestamp
	 *            The timestamp when the method is invoked, in nanoseconds
	 * @param clazz
	 *            The class from which the method is member
	 * @param methodName
	 *            The name of the method
	 * @param parameters
	 *            The formal parameters of the method
	 * @param arguments
	 *            The arguments with which the method was called
	 */
	public InvokeEvent(final UUID transactionId, final long timestamp,
			final Class<?> clazz, final String methodName,
			final Class<?>[] parameters, Object... arguments) {
		this.id = UUID.randomUUID();
		this.transactionId = transactionId;
		this.timestamp = timestamp;
		this.clazz = clazz;
		this.methodName = methodName;
		this.parameters = parameters;
		this.arguments = arguments;
	}

	public UUID identifier() {
		return this.id;
	}

	public UUID transactionId() {
		return this.transactionId;
	}

	public Long value() {
		return this.timestamp;
	}

	public Class<?> methodProvider() {
		return this.clazz;
	}

	public String methodName() {
		return this.methodName;
	}

	public Class<?>[] methodParameters() {
		return this.parameters;
	}

	public Object[] methodArguments() {
		return this.arguments;
	}

	/**
	 * Checks whether this {@link InvokeEvent} instance was finished measuring
	 * within a given time window, i.e., {@code this.end} is contained in [
	 * {@code start} , {@code end}].
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
	 * Returns the string representation of this event for logging purposes.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getCanonicalName() + "\t");
		sb.append(this.transactionId + "\t");
		sb.append(this.id + "\t");
		sb.append(this.clazz.getCanonicalName() + "\t");
		sb.append(this.methodName + "\t");
		sb.append(Arrays.toString(parameters) + "\t");
		sb.append(value());

		return sb.toString();
	}

	/**
	 * The result is {@code true} if and only if the argument is not
	 * {@code null}, is a {@Link InvokeEvent} object and has the same
	 * identifier as {@code this} {@Link InvokeEvent}.
	 */
	@Override
	public boolean equals(final Object obj) {
		if ((null == obj) || (obj.getClass() != InvokeEvent.class))
			return false;

		InvokeEvent other = (InvokeEvent) obj;
		return this.id.equals(other.id);
	}

	/**
	 * The result is {@code -1} if {@code this} event was started before
	 * {@code o}. If {@code this} was started after {@code o}, the result is
	 * {@code 1}. Otherwise, the result is {@code 0}.
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(final Event<Long> o) {
		if (o != null && o instanceof InvokeEvent) {
			InvokeEvent other = (InvokeEvent) o;

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
