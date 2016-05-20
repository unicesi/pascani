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
package org.pascani.dsl.dbmapper.typeadapters;

import java.lang.reflect.Type;
import java.util.UUID;

import com.google.gson.InstanceCreator;

/**
 * Custom Gson instance creator for the UUID class
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class UUIDInstanceCreator implements InstanceCreator<UUID> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.gson.InstanceCreator#createInstance(java.lang.reflect.Type)
	 */
	@Override public UUID createInstance(Type type) {
		return UUID.randomUUID();
	}

}
