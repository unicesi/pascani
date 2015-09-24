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
package pascani.lang.infrastructure;

import java.io.Serializable;

/**
 * @author Miguel Jiménez - Initial contribution and API
 */
public interface Namespace {

	/**
	 * Gets the current value of the specified variable. If the given variable
	 * name is not found, null is returned.
	 * 
	 * @param variable
	 *            The name of the variable
	 * @return the current value of the specified variable
	 */
	public Serializable getVariable(String variable);

	/**
	 * Updates the current value of the specified variable. If the given
	 * variable name is not found, null is returned.
	 * 
	 * @param variable
	 *            The name of the variable
	 * @param value
	 *            The new value
	 * @return the current variable's value after updating it. Notice that it
	 *         may be different than {@code value}
	 */
	public Serializable setVariable(String variable, Serializable value);

}
