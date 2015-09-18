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

/**
 * Implementation of {@link Event} for raising events each time a non-void
 * method finishes its execution.
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class ReturnEvent extends Event<Long> {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 8016321382976600130L;

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
	 * The actual method return
	 */
	private final Object _return;

	/**
	 * Creates an instance having all of the parameters
	 * 
	 * @param transactionId
	 *            The transaction of which this event makes part
	 * @param clazz
	 *            The class from which the method is member
	 * @param methodName
	 *            The name of the method
	 * @param parameters
	 *            The formal parameters of the method
	 * @param _return
	 *            The actual method return
	 */
	public ReturnEvent(final UUID transactionId, final Class<?> clazz,
			final String methodName, final Class<?>[] parameters, Object _return) {
		super(transactionId);
		this.clazz = clazz;
		this.methodName = methodName;
		this.parameters = parameters;
		this._return = _return;
	}

	@Override public Long value() {
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

	public Object methodReturn() {
		return this._return;
	}

	/**
	 * Returns the string representation of this event for logging purposes.
	 */
	@Override public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getCanonicalName() + "\t");
		sb.append(this.transactionId + "\t");
		sb.append(this.identifier + "\t");
		sb.append(this.clazz.getCanonicalName() + "\t");
		sb.append(this.methodName + "\t");
		sb.append(Arrays.toString(parameters) + "\t");
		sb.append(this.value());

		return sb.toString();
	}

}
