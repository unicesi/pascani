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
package org.pascani.dsl.dbmapper;

import java.util.UUID;

import org.pascani.dsl.dbmapper.typeadapters.TransientExclusionStrategy;
import org.pascani.dsl.dbmapper.typeadapters.UUIDDeserializer;
import org.pascani.dsl.dbmapper.typeadapters.UUIDInstanceCreator;
import org.pascani.dsl.dbmapper.typeadapters.UUIDSerializer;
import org.pascani.dsl.lib.Event;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Miguel Jiménez - Initial contribution and API
 */
public class JsonEventMapper {

	/**
	 * The instance to convert from/to json
	 */
	private final Gson gson;

	public JsonEventMapper() {
		this.gson = buildGson();
	}

	private Gson buildGson() {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(UUID.class, new UUIDInstanceCreator());
		builder.registerTypeAdapter(UUID.class, new UUIDSerializer());
		builder.registerTypeAdapter(UUID.class, new UUIDDeserializer());
		builder.setExclusionStrategies(new TransientExclusionStrategy());
		return builder.create();
	}

	/**
	 * Converts an event to Json format
	 * 
	 * @param event
	 *            The event to convert to Json
	 * @return The Json representation of the given event
	 */
	public <T extends Event<?>> String toJson(T event) {
		return gson.toJson(event);
	}

	/**
	 * Converts an event from Json format to Object
	 * 
	 * @param json
	 *            The Json representation of the desired event
	 * @param eventType
	 *            The event type
	 * @return An Object representation of the given Json data
	 */
	public <T extends Event<?>> T fromJson(String json, Class<T> eventType) {
		return gson.fromJson(json, eventType);
	}

}
