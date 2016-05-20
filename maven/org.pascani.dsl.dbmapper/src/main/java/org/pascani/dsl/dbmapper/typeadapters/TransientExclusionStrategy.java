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

import org.pascani.dsl.lib.util.Transient;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

/**
 * Custom Gson exclusion strategy for fields annotated with {@link Transient}
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class TransientExclusionStrategy implements ExclusionStrategy {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gson.ExclusionStrategy#shouldSkipClass(java.lang.Class)
	 */
	@Override public boolean shouldSkipClass(Class<?> clazz) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gson.ExclusionStrategy#shouldSkipField(com.google.gson.
	 * FieldAttributes)
	 */
	@Override public boolean shouldSkipField(FieldAttributes field) {
		return field.getAnnotation(Transient.class) != null;
	}

}
