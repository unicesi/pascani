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
import java.util.Map;

import org.pascani.dsl.lib.util.Resumable;

/**
 * @author Miguel Jiménez - Initial contribution and API
 */
public interface Namespace extends Resumable {

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
	 * Gets the current value of the specified variable that matches the given
	 * tags. If the given variable name is not found, null is returned.
	 * 
	 * @param variable
	 *            The name of the variable
	 * @param tags
	 *            The map containing the value tags
	 * @return the current value of the specified variable
	 */
	public Serializable getVariable(String variable, Map<String, String> tags);

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
