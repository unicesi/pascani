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
import java.util.List;
import java.util.UUID;

import org.pascani.dsl.lib.Event;

/**
 * Implementation of {@link Event} for raising events each time a new namespace
 * is deployed.
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class NewNamespaceEvent extends Event<Serializable> {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 1418636463507874973L;

	/**
	 * The name of the new namespace
	 */
	private final String namespaceName;

	/**
	 * Variables declared within the new namespace
	 */
	private final List<String> variables;

	public NewNamespaceEvent(final UUID transactionId,
			final String namespaceName, final List<String> variables) {
		super(transactionId);
		this.namespaceName = namespaceName;
		this.variables = variables;
	}

	@Override public Serializable value() {
		return this.namespaceName;
	}
	
	public List<String> variables() {
		return this.variables;
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
		sb.append(this.namespaceName + "\t");
		sb.append(value().toString());
		return sb.toString();
	}

}
