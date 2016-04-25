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
	private final String clazz;

	/**
	 * The method that raised the exception
	 */
	private final String method;

	/**
	 * The formal parameters of the method
	 */
	private final String[] parameters;

	/**
	 * Creates an instance having all of the parameters
	 * 
	 * @param transactionId
	 *            The transaction of which this event makes part
	 * @param exception
	 *            The actual Exception
	 * @param clazz
	 *            The class in which the exception was raised
	 * @param method
	 *            The method that threw the exception
	 * @param parameters
	 *            The formal parameters of the method
	 */
	public ExceptionEvent(final UUID transactionId, final Exception exception,
			final String clazz, final String method,
			final String[] parameters) {
		super(transactionId);
		this.exception = exception;
		this.clazz = clazz;
		this.method = method;
		this.parameters = parameters;
	}

	@Override public Exception value() {
		return this.exception;
	}

	public String clazz() {
		return this.clazz;
	}

	public String method() {
		return this.method;
	}

	public String[] parameters() {
		return this.parameters;
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
		sb.append(this.method + "\t");
		sb.append(this.parameters + "\t");
		sb.append(this.timestamp + "\t");
		sb.append(value().toString());

		return sb.toString();
	}

}
