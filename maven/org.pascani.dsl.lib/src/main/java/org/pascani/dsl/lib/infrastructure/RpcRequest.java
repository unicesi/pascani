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
package org.pascani.dsl.lib.infrastructure;

import java.io.Serializable;

/**
 * A POJO containing the necessary information to serve RPC requests
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public final class RpcRequest implements Serializable {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 6621641760153675622L;

	/**
	 * The type of operation requested
	 */
	private final RpcOperation operation;

	/**
	 * The parameters of the requested operation
	 */
	private final Serializable[] parameters;

	/**
	 * Creates an RPC request
	 * 
	 * @param operation
	 *            The type of requested operation
	 * @param parameters
	 *            The parameters of the requested operation
	 */
	public RpcRequest(final RpcOperation operation,
			final Serializable... parameters) {
		this.operation = operation;
		this.parameters = parameters;
	}

	public RpcOperation operation() {
		return this.operation;
	}

	public Serializable getParameter(int index) {
		return this.parameters[index];
	}
}
