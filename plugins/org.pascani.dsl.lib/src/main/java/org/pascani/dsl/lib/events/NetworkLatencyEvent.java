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
package org.pascani.dsl.lib.events;

import java.util.Arrays;
import java.util.UUID;

import org.pascani.dsl.lib.Event;

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
	private final String caller;

	/**
	 * The class providing the method
	 */
	private final String callee;

	/**
	 * The invoked method
	 */
	private final String method;

	/**
	 * The formal parameters of the method
	 */
	private final String[] parameters;
	
	/**
	 * The actual method return
	 */
	private final Object _return;

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
	 * @param method
	 *            The name of the method
	 * @param parameters
	 *            The formal parameters of the method
	 */
	public NetworkLatencyEvent(final UUID transactionId, final long start,
			final long end, final String caller, final String callee,
			final String method, final String[] parameters, final Object _return) {
		super(transactionId);
		this.start = start;
		this.end = end;
		this.latency = this.end - this.start;
		this.caller = caller;
		this.callee = callee;
		this.method = method;
		this.parameters = parameters;
		this._return = _return;
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
	 * @param method
	 *            The name of the method
	 * @param parameters
	 *            The formal parameters of the method
	 */
	public NetworkLatencyEvent(final UUID transactionId, final long start,
			final String caller, final String callee, final String method,
			final String[] parameters, final Object _return) {
		this(transactionId, start, 0, caller, callee, method, parameters, _return);
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
	public NetworkLatencyEvent(final NetworkLatencyEvent event,
			final long end) {
		this(event.transactionId, event.start, end, event.caller, event.callee,
				event.method, event.parameters, event._return);
	}

	@Override public Double value() {
		return this.latency;
	}
	
	public long start() {
		return this.start;
	}
	
	public long end() {
		return this.end;
	}

	public String methodCaller() {
		return this.caller;
	}

	public String methodProvider() {
		return this.callee;
	}

	public String method() {
		return this.method;
	}

	public String[] methodParameters() {
		return this.parameters;
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
		sb.append(this.caller + "\t");
		sb.append(this.callee + "\t");
		sb.append(this.method + "\t");
		sb.append(Arrays.toString(parameters) + "\t");
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
		NetworkLatencyEvent other = (NetworkLatencyEvent) o;
		
		v = compareInt(this.start, other.start);
		if (v != 0) return v;

		v = compareInt(this.end, other.end);
		if (v != 0) return v;

		v = this.identifier.compareTo(other.identifier);
		if (v != 0) return v;
		
		return v;
	}

}
