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
 * Implementation of {@link Event} for measuring method calls' latency,
 * specially useful to measure network latency in remote methods execution.
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class NetworkLatencyEvent extends Event<Double> {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 8016321382976600130L;

	/**
	 * The actual initial time in epoch format
	 */
	private final long start;

	/**
	 * The actual final time in epoch format
	 */
	private final long end;

	/**
	 * The actual latency between method invocation and reception of the
	 * parameters
	 */
	private final double latency;

	/**
	 * The class performing the method call
	 */
	private final Class<?> caller;

	/**
	 * The class providing the method
	 */
	private final Class<?> callee;

	/**
	 * The actual method return
	 */
	private final Object _return;

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
	 * @param start
	 *            The initial timestamp
	 * @param end
	 *            The final timestamp
	 * @param caller
	 *            The class performing the method call
	 * @param callee
	 *            The class from which the method is member
	 * @param _return
	 *            The actual method return
	 * @param methodName
	 *            The name of the method
	 * @param parameters
	 *            The formal parameters of the method
	 * @param arguments
	 *            The arguments with which the method was called
	 */
	public NetworkLatencyEvent(final UUID transactionId, final long start,
			final long end, final Class<?> caller, final Class<?> callee,
			final Object _return, final String methodName,
			final Class<?>[] parameters, Object... arguments) {
		super(transactionId);
		this.start = start;
		this.end = end;
		this.latency = this.end - this.start;
		this.caller = caller;
		this.callee = callee;
		this._return = _return;
		this.methodName = methodName;
		this.parameters = parameters;
		this.arguments = arguments;
	}

	/**
	 * Creates an instance having all the parameters except for the final
	 * timestamp. The final timestamp is set to {@code 0}; to re-create the
	 * event with the final timestamp (when this is captured) a new instance
	 * must be created from the previous one, by calling constructor
	 * {@link NetworkLatencyEvent#NetworkLatencyEvent(NetworkLatencyEvent, long)}
	 * .
	 * 
	 * @param transactionId
	 *            The transaction of which this event makes part
	 * @param start
	 *            The initial timestamp
	 * @param caller
	 *            The class performing the method call
	 * @param callee
	 *            The class from which the method is member
	 * @param _return
	 *            The actual method return
	 * @param methodName
	 *            The name of the method
	 * @param parameters
	 *            The formal parameters of the method
	 * @param arguments
	 *            The arguments with which the method was called
	 */
	public NetworkLatencyEvent(final UUID transactionId, final long start,
			final Class<?> caller, final Class<?> callee, final Object _return,
			final String methodName, final Class<?>[] parameters,
			Object... arguments) {
		this(transactionId, start, 0, caller, callee, _return, methodName,
				parameters, arguments);
	}

	/**
	 * Creates an instance based on a previous instance. This is intended to be
	 * used when the final timestamp is captured.
	 * 
	 * @param event
	 *            The previous instance
	 * @param end
	 *            The final timestamp
	 */
	public NetworkLatencyEvent(final NetworkLatencyEvent event, final long end) {
		this(event.transactionId, event.start, end, event.caller, event.callee,
				event._return, event.methodName, event.parameters,
				event.arguments);
	}

	@Override public Double value() {
		return this.latency;
	}

	public Class<?> methodCaller() {
		return this.caller;
	}

	public Class<?> methodProvider() {
		return this.callee;
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

	public Object methodReturn() {
		return this._return;
	}

	/**
	 * Checks whether this {@link NetworkLatencyEvent} instance was finished
	 * measuring within a given time window, i.e., {@code this.end} is contained
	 * in [{@code start} , {@code end}].
	 * 
	 * @param start
	 *            The initial timestamp of the time window
	 * @param end
	 *            The final timestamp of the time window
	 * @return Whether the range [{@code start}, {@code end}] contains the final
	 *         timestamp this object
	 */
	@Override public boolean isInTimeWindow(long start, long end) {
		return Range.closed(start, end).contains(this.end);
	}

	/**
	 * Returns the string representation of this event for logging purposes.
	 */
	@Override public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getCanonicalName() + "\t");
		sb.append(this.transactionId + "\t");
		sb.append(this.identifier + "\t");
		sb.append(this.caller.getCanonicalName() + "\t");
		sb.append(this.callee.getCanonicalName() + "\t");
		sb.append(this.methodName + "\t");
		sb.append(Arrays.toString(parameters) + "\t");
		sb.append(this.start + "\t");
		sb.append(this.end + "\t");
		sb.append(this.value());

		return sb.toString();
	}

	/**
	 * The result is {@code -1} if {@code this} event was started before
	 * {@code o}, otherwise is {@code 1}. {@code 0} is returned when the
	 * argument is null or is not a {@link NetworkLatencyEvent}.
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(final Event<Double> o) {
		if (o != null && o instanceof NetworkLatencyEvent) {
			NetworkLatencyEvent other = (NetworkLatencyEvent) o;

			if (this.start < other.start) {
				return -1;
			} else {
				return 1;
			}
		}

		return 0;
	}

}
