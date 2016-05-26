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
package org.pascani.dsl.dbmapper.databases;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.json.JSONObject;
import org.pascani.dsl.dbmapper.DbInterface;
import org.pascani.dsl.lib.Event;
import org.pascani.dsl.lib.events.ChangeEvent;
import org.pascani.dsl.lib.events.LogEvent;
import org.pascani.dsl.lib.events.NewMonitorEvent;
import org.pascani.dsl.lib.events.NewNamespaceEvent;
import org.pascani.dsl.lib.util.ConfigProperties;
import org.pascani.dsl.lib.util.TaggedValue;

import com.google.common.collect.Range;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;

/**
 * @author Miguel Jiménez - Initial contribution and API
 */
public class ElasticSearch implements DbInterface {

	/**
	 * The ElasticSearch configuration properties
	 */
	private final Map<String, String> props;

	public ElasticSearch() {
		this.props = readProperties();
	}

	private Map<String, String> readProperties() {
		Map<String, String> defaultProps = new HashMap<String, String>();
		defaultProps.put("uri", "http://localhost:9200");
		// TODO: add authentication support
		// defaultProps.put("user", "");
		// defaultProps.put("password", "");
		defaultProps.put("output_responses", "true");
		return new ConfigProperties("elasticsearch.properties", "elasticsearch",
				defaultProps).readProperties();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pascani.dsl.dbmapper.DbInterface#save(org.pascani.dsl.lib.Event)
	 */
	@Override public <T extends Event<?>> void save(T event) throws Exception {
		Map<String, String> data = null;
		String index = "", type = "";
		if (event instanceof ChangeEvent) {
			ChangeEvent e = (ChangeEvent) event;
			data = handle(e);
			index = "variables";
			type = e.variable();
		} else if (event instanceof NewMonitorEvent) {
			data = handle((NewMonitorEvent) event);
			index = "logs";
			type = "deployment";
		} else if (event instanceof NewNamespaceEvent) {
			data = handle((NewNamespaceEvent) event);
			index = "logs";
			type = "deployment";
		} else if (event instanceof LogEvent) {
			data = handle((LogEvent) event);
			index = "logs";
			type = "execution";
		}
		if (data != null) {
			String uri = this.props.get("uri") + "/" + index + "/" + type + "/" + event.identifier();
			HttpResponse<JsonNode> response = Unirest.put(uri)
					.body(new JSONObject(data)).asJson();
			if (Boolean.valueOf(this.props.get("output_responses"))) {
				System.out.println(
						response.getStatusText() + "\t" + response.getBody());
			}
		}
	}

	private Map<String, String> handle(LogEvent e) {
		Map<String, String> data = new HashMap<String, String>();
		data.put("level", e.level());
		data.put("logger", e.logger());
		data.put("message", e.value() + "");
		data.put("cause", e.cause());
		data.put("source", e.source());
		data.put("timestamp", e.timestamp() + "");
		return data;
	}

	private Map<String, String> handle(NewMonitorEvent e) {
		Map<String, String> data = new HashMap<String, String>();
		data.put("level", Level.CONFIG.getName());
		data.put("logger", e.value() + "");
		data.put("message", "Monitor " + e.value() + " has been deployed");
		data.put("source", e.value() + "");
		data.put("timestamp", e.timestamp() + "");
		return data;
	}

	private Map<String, String> handle(NewNamespaceEvent e) {
		Map<String, String> data = new HashMap<String, String>();
		data.put("level", Level.CONFIG.getName());
		data.put("logger", e.value() + "");
		data.put("message", "Namespace " + e.value() + " has been deployed");
		data.put("source", e.value() + "");
		data.put("timestamp", e.timestamp() + "");
		return data;
	}

	private Map<String, String> handle(ChangeEvent e) {
		Map<String, String> data = null;
		TaggedValue<Serializable> taggedValue = TaggedValue
				.instanceFrom(e.value(), Serializable.class);
		if (taggedValue.value() instanceof Number
				|| taggedValue.value() instanceof Boolean
				|| taggedValue.value() instanceof String) {
			data = toData(e, taggedValue.value(), taggedValue.tags());
		} else if (taggedValue.value() instanceof Range<?>) {
			Range<?> range = (Range<?>) taggedValue.value();
			Class<?> clazz = range.hasLowerBound()
					? range.lowerEndpoint().getClass()
					: range.upperEndpoint().getClass();
			if (Number.class.isAssignableFrom(clazz)) {
				data = toData(e, taggedValue.value(), taggedValue.tags());
			} else {
				System.out.println(
						"Not supported type " + clazz.getCanonicalName());
			}
		} else {
			System.out.println("Not supported type "
					+ taggedValue.value().getClass().getCanonicalName());
		}
		return data;
	}

	private Map<String, String> toData(ChangeEvent e, Serializable value,
			Map<String, String> tags) {
		Map<String, String> data = new HashMap<String, String>();
		renameTags(tags, "value", "start", "end", "timestamp");
		data.putAll(tags);
		if (value instanceof Range<?>) {
			Range<?> range = (Range<?>) value;
			Number start = (Number) range.lowerEndpoint();
			Number end = (Number) range.upperEndpoint();
			data.put("start", start + "");
			data.put("end", end + "");
			data.put("value", (end.doubleValue() - start.doubleValue()) + "");
		} else if (value instanceof Number) {
			data.put("value", (Number) value + "");
		} else if (value instanceof Boolean) {
			data.put("value", (Boolean) value + "");
		} else if (value instanceof String) {
			data.put("value", (String) value);
		}
		data.put("timestamp", e.timestamp() + "");
		return data;
	}
	
	private void renameTags(Map<String, String> tags, String... reserved) {
		for (String key : reserved) {
			if (tags.containsKey(key)) {
				String value = tags.remove(key);
				int i = 1;
				String proposal = key + i;
				while (tags.containsKey(proposal))
					proposal = key + ++i;
				tags.put(proposal, value);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pascani.dsl.dbmapper.DbInterface#openConnection()
	 */
	@Override public void openConnection() throws Exception {
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pascani.dsl.dbmapper.DbInterface#closeConnection()
	 */
	@Override public void closeConnection() throws Exception {
		// Nothing to do
	}

}
