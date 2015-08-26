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
package pascani.lang.sca;

import java.util.Map;

public class ServiceManager {

	/**
	 * Registers the necessary properties to bind a SCA service
	 * 
	 * @param properties
	 * @param clazz
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static <T> T bind(Map<String, Object> properties, Class<T> clazz) {

		// TODO: how to register these properties? (this method is not actually
		// called at runtime)

		/*
		 * At runtime, inside Pascani, it is not important to have the actual
		 * instance; it is enough just thinking it is there, for code completion
		 * and content assist purposes.
		 */
		return null;
	}

}
