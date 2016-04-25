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

import org.pascani.dsl.lib.Probe;

/**
 * An enumeration containing the types of operations provided by {@link Probe}
 * and {@link BasicNamespace} instances.
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public enum RpcOperation {
	// Probe operations
	PROBE_CLEAN, PROBE_COUNT, PROBE_COUNT_AND_CLEAN, PROBE_FETCH, PROBE_FETCH_AND_CLEAN,

	// Namespace operations
	NAMESPACE_GET_VARIABLE, NAMESPACE_SET_VARIABLE,
	
	// Common operations
	PAUSE, RESUME, IS_PAUSED
}
