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

import java.lang.reflect.Method;
import java.util.UUID;

import pascani.lang.Event;

import com.google.common.collect.Range;

/**
 * 
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class NetworkLatencyEvent implements Event<Double> {

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
	 * The method being called
	 */
	private final Method method;

	/**
	 * The actual method return
	 */
	private final Object _return;

	/**
	 * The actual parameters within the method call
	 */
	private final Object[] parameters;

	/**
	 * Creates an instance having all of the parameters
	 * 
	 * @param start
	 *            The initial timestamp
	 * @param end
	 *            The final timestamp
	 * @param caller
	 *            The class performing the method call
	 * @param callee
	 *            The class providing the method
	 * @param _return
	 *            The actual method return
	 * @param method
	 *            The method being called
	 * @param parameters
	 *            The actual parameters within the method call
	 */
	public NetworkLatencyEvent(final UUID transactionId, final long start,
			final long end, final Class<?> caller, final Class<?> callee,
			final Object _return, final Method method, Object... parameters) {
		this.id = UUID.randomUUID();
		this.transactionId = transactionId;
		this.start = start;
		this.end = end;
		this.latency = this.start - this.end;
		this.caller = caller;
		this.callee = callee;
		this.method = method;
		this._return = _return;
		this.parameters = parameters;
	}

	/**
	 * Creates an instance having all the parameters except for the final
	 * timestamp. The final timestamp is set to {@code 0}; to re-create the
	 * event with the final timestamp (when this is captured) a new instance
	 * must be created from the previous one, by calling constructor
	 * {@link NetworkLatencyEvent#NetworkLatencyEvent(NetworkLatencyEvent, long)}
	 * .
	 * 
	 * @see NetworkLatencyEvent#NetworkLatencyEvent(NetworkLatencyEvent, long)
	 * 
	 * @param start
	 *            The initial timestamp
	 * @param caller
	 *            The class performing the method call
	 * @param callee
	 *            The class providing the method
	 * @param _return
	 *            The actual method return
	 * @param method
	 *            The method being called
	 * @param parameters
	 *            The actual parameters within the method call
	 */
	public NetworkLatencyEvent(final UUID transactionId, final long start,
			final Class<?> caller, final Class<?> callee, final Object _return,
			final Method method, Object... parameters) {
		this(transactionId, start, 0, caller, callee, _return, method,
				parameters);
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
				event._return, event.method, event.parameters);
	}

	public UUID identifier() {
		return this.id;
	}

	public UUID transactionId() {
		return this.transactionId;
	}

	public Double value() {
		return this.latency;
	}

	public Class<?> methodCaller() {
		return this.caller;
	}

	public Class<?> methodProvider() {
		return this.callee;
	}

	public Method getMethodInformation() {
		return this.method;
	}

	public Object[] getActualMethodParameters() {
		return this.parameters;
	}
	
	public Object getActualMethodReturn(){
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
	 * @return {@code true} if the range [{@code start}, {@code end}] contains
	 *         the final timestamp {@code this} object
	 */
	public boolean isInTimeWindow(long start, long end) {
		return Range.closed(start, end).contains(end);
	}

	/**
	 * Returns the string representation of this event for logging purposes. The
	 * string contains [this class name, id, caller, callee, method, start, end,
	 * value] separated by a tab character.
	 * 
	 */
	@Override public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getCanonicalName() + "\t");
		sb.append(identifier().toString() + "\t");
		sb.append(this.caller.getCanonicalName() + "\t");
		sb.append(this.callee.getCanonicalName() + "\t");
		sb.append(this.method.toString() + "\t");
		sb.append(this.start + "\t");
		sb.append(this.end + "\t");
		sb.append(value());

		return sb.toString();
	}

	/**
	 * The result is {@code true} if and only if the argument is not
	 * {@code null}, is a {@Link LatencyEvent} object and has the same
	 * identifier as {@code this} {@Link LatencyEvent}.
	 */
	@Override public boolean equals(final Object obj) {
		if ((null == obj) || (obj.getClass() != NetworkLatencyEvent.class))
			return false;

		NetworkLatencyEvent other = (NetworkLatencyEvent) obj;
		return this.id.equals(other.id);
	}

	/**
	 * The result is {@code -1} if {@code this} event was started before
	 * {@code o}. If {@code this} was started after {@code o}, the result is
	 * {@code 1}. Otherwise, the result is {@code 0}.
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(final Event<Double> o) {
		if (o != null && o instanceof NetworkLatencyEvent) {
			NetworkLatencyEvent other = (NetworkLatencyEvent) o;

			if (this.start < other.start) {
				return -1;
			} else if (this.start > other.start) {
				return 1;
			} else {
				return 0;
			}
		}

		return 0;
	}

}
