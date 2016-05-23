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
package org.pascani.dsl.lib.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides a way to tag serializable values.
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class TaggedValue<T extends Serializable> implements Serializable {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -2580330614196690636L;

	/**
	 * The value being tagged
	 */
	private final T value;

	/**
	 * The map containing key-value tags
	 */
	private final Map<String, String> tags;

	/**
	 * Creates a tagged value
	 * 
	 * @param value
	 *            The value being tagged
	 * @param tags
	 *            The map containing key-value tags
	 */
	public TaggedValue(T value, Map<String, String> tags) {
		this.value = value;
		this.tags = tags;
	}

	/**
	 * Creates a tagged value initialized with an empty list of tags
	 * 
	 * @param value
	 *            The value being tagged
	 */
	public TaggedValue(T value) {
		this(value, new HashMap<String, String>());
	}
	
	/**
	 * Casts or create an instance from the given presumed tagged value
	 * 
	 * @param value
	 *            The presumed tagged value
	 * @param type
	 *            The value's type
	 * @return a tagged value created from the given presumed tagged value
	 */
	@SuppressWarnings("unchecked") 
	public static <T extends Serializable> TaggedValue<T> instanceFrom(
			Serializable value, Class<T> type) {
		if (value instanceof TaggedValue<?>) {
			return (TaggedValue<T>) value;
		} else {
			return new TaggedValue<T>(type.cast(value));
		}
	}

	/**
	 * Adds a tag
	 * 
	 * @param key
	 *            The tag name
	 * @param value
	 *            The tag value
	 * @return {@code this} tagged value
	 */
	public TaggedValue<T> with(String key, String value) {
		this.tags.put(key, value);
		return this;
	}

	/**
	 * @return The value being tagged
	 */
	public T value() {
		return this.value;
	}

	/**
	 * @return The map containing the tags
	 */
	public Map<String, String> tags() {
		return this.tags;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return value + (tags.isEmpty() ? "" : " " + tags);
	}

}
