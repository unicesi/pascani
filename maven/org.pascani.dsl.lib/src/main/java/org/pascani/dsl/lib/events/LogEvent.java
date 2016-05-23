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

import java.io.Serializable;
import java.util.UUID;

import org.pascani.dsl.lib.Event;

/**
 * @author Miguel Jiménez - Initial contribution and API
 */
public class LogEvent extends Event<Serializable> {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -1984056623769373861L;

	/**
	 * The class emitting the log event
	 */
	private final String logger;

	/**
	 * The log level (ERROR, WARN, INFO, ...)
	 */
	private final String level;
	
	private final String cause;
	
	private final String source;

	/**
	 * The log message
	 */
	private final String message;

	public LogEvent(UUID transactionId, final String logger, final String level,
			final String message, final String cause, final String source) {
		super(transactionId);
		this.logger = logger;
		this.level = level;
		this.message = message;
		this.cause = cause;
		this.source = source;
	}

	@Override public Serializable value() {
		return this.message;
	}

	public final String logger() {
		return this.logger;
	}

	public final String level() {
		return this.level;
	}
	
	public final String cause() {
		return this.cause;
	}
	
	public final String source() {
		return this.source;
	}

	/**
	 * Returns the string representation of this event for logging purposes.
	 */
	@Override public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getCanonicalName() + "\t");
		sb.append(this.transactionId + "\t");
		sb.append(this.identifier + "\t");
		sb.append(this.logger + "\t");
		sb.append(this.level + "\t");
		sb.append("message=" + this.message + "\t");
		sb.append("cause=" + this.cause + "\t");
		sb.append("source=" + this.source + "\t");
		sb.append(value());
		return sb.toString();
	}

}
