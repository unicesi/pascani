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

import org.pascani.dsl.dbmapper.DbInterface;
import org.pascani.dsl.lib.Event;
import org.pascani.dsl.lib.events.ChangeEvent;
import org.pascani.dsl.lib.util.ConfigProperties;
import org.pascani.dsl.lib.util.TaggedValue;

import com.google.common.collect.Range;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.HttpRequestWithBody;

/**
 * This implementation supports only {@link ChangeEvent} events, as FNordMetric
 * is not a suitable dashboard for visualizing/searching/filtering logs.
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class FNordMetric implements DbInterface {

	/**
	 * The FNordMetric configuration properties
	 */
	private final Map<String, String> props;

	public FNordMetric() {
		this.props = readProperties();
	}

	private Map<String, String> readProperties() {
		Map<String, String> defaultProps = new HashMap<String, String>();
		defaultProps.put("uri", "http://localhost:8080");
		// TODO: add authentication support
		// defaultProps.put("user", "");
		// defaultProps.put("password", "");
		defaultProps.put("output_responses", "true");
		return new ConfigProperties("fnordmetric.properties", "fnordmetric",
				defaultProps).readProperties();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pascani.dsl.dbmapper.DbInterface#save(org.pascani.dsl.lib.Event)
	 */
	@Override public <T extends Event<?>> void save(T event) throws Exception {
		Map<String, String> data = null;
		if (event instanceof ChangeEvent) {
			ChangeEvent e = (ChangeEvent) event;
			data = handle(e);
			if (data != null) {
				String uri = this.props.get("uri") + "/metrics";
				HttpRequestWithBody request = Unirest.post(uri);
				request.queryString("metric", e.variable());
				request.queryString("value", data.get("value"));
				data.remove("value");
				for (String tag : data.keySet()) {
					request.queryString("label[" + tag + "]", data.get(tag));
				}
				HttpResponse<String> response = request.asString();
				if (Boolean.valueOf(this.props.get("output_responses"))) {
					System.out.println(response.getStatusText() + "\t"
							+ response.getBody());
				}
			}
		}

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
