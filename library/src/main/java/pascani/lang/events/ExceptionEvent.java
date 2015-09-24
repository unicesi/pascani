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
package pascani.lang.events;

import java.util.UUID;

import pascani.lang.Event;

/**
 * Implementation of {@link Event} for exceptions registry
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class ExceptionEvent extends Event<Exception> {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -9029356694184256904L;

	/**
	 * The thrown exception
	 */
	private final Exception exception;

	/**
	 * The class in which the exception was raised
	 */
	private final Class<?> clazz;

	/**
	 * The method that raised the exception
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
	 * @param exception
	 *            The actual Exception
	 * @param clazz
	 *            The class in which the exception was raised
	 * @param methodName
	 *            The method that raised the exception
	 * @param parameters
	 *            The formal parameters of the method
	 * @param arguments
	 *            The arguments with which the method was called
	 */
	public ExceptionEvent(final UUID transactionId, final Exception exception,
			final Class<?> clazz, final String methodName,
			final Class<?>[] parameters, final Object... arguments) {
		super(transactionId);
		this.exception = exception;
		this.clazz = clazz;
		this.methodName = methodName;
		this.parameters = parameters;
		this.arguments = arguments;
	}

	@Override public Exception value() {
		return this.exception;
	}

	public Class<?> clazz() {
		return this.clazz;
	}

	public String methodName() {
		return this.methodName;
	}

	public Class<?>[] parameters() {
		return this.parameters;
	}

	public Object[] arguments() {
		return this.arguments;
	}

	/**
	 * Returns the string representation of this event for logging purposes.
	 */
	@Override public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getCanonicalName() + "\t");
		sb.append(this.transactionId + "\t");
		sb.append(this.identifier + "\t");
		sb.append(this.clazz + "\t");
		sb.append(this.methodName + "\t");
		sb.append(this.parameters + "\t");
		sb.append(this.timestamp + "\t");
		sb.append(value().toString());

		return sb.toString();
	}

}
