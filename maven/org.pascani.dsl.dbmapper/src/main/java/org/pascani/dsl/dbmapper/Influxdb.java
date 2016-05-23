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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.pascani.dsl.lib.Event;
import org.pascani.dsl.lib.events.ChangeEvent;
import org.pascani.dsl.lib.events.LogEvent;
import org.pascani.dsl.lib.events.NewMonitorEvent;
import org.pascani.dsl.lib.events.NewNamespaceEvent;
import org.pascani.dsl.lib.util.ConfigProperties;
import org.pascani.dsl.lib.util.TaggedValue;

import com.google.common.collect.Range;

/**
 * @author Miguel Jiménez - Initial contribution and API
 */
public class Influxdb implements DbInterface {

	/**
	 * The logger
	 */
	private static final Logger logger = LogManager.getLogger(Influxdb.class);

	/**
	 * The Influxdb configuration properties
	 */
	private final Map<String, String> props;

	/**
	 * A REST interface to the influxDB database
	 */
	private InfluxDB influxDB;

	public Influxdb() {
		this.props = readProperties();
	}

	private Map<String, String> readProperties() {
		Map<String, String> defaultProps = new HashMap<String, String>();
		defaultProps.put("uri", "http://127.0.0.1:8086");
		defaultProps.put("database", "test");
		defaultProps.put("user", "");
		defaultProps.put("password", "");
		defaultProps.put("flush_points", "100");
		defaultProps.put("flush_interval", "100"); // milliseconds
		return new ConfigProperties("influxdb.properties", "influxdb",
				defaultProps).readProperties();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pascani.dsl.dbmapper.DbInterface#save(org.pascani.dsl.lib.Event)
	 */
	@Override public <T extends Event<?>> void save(T event) throws Exception {
		Point point = null;
		
		if (event instanceof ChangeEvent)
			point = handle((ChangeEvent) event);
		else if (event instanceof NewMonitorEvent)
			point = handle((NewMonitorEvent) event);
		else if (event instanceof NewNamespaceEvent)
			point = handle((NewNamespaceEvent) event);
		else if (event instanceof LogEvent)
			point = handle((LogEvent) event);
		
		if (point != null) {
			this.influxDB.write(this.props.get("database"), "default", point);
		}
	}
	
	private Point handle(LogEvent e) {
		Builder builder = Point.measurement("log")
				.tag("type", "log")
				.addField("level", e.level())
				.addField("logger", e.logger())
				.addField("message", e.value() + "")
				.time(e.timestamp(), TimeUnit.MILLISECONDS);
		return builder.build();
	}
	
	private Point handle(NewMonitorEvent e) {
		Builder builder = Point.measurement("log")
				.tag("type", "monitor")
				.addField("level", Level.CONFIG.getName())
				.addField("logger", e.value() + "")
				.addField("message", "Monitor " + e.value() + " has been deployed")
				.time(e.timestamp(), TimeUnit.MILLISECONDS);
		return builder.build();
	}
	
	private Point handle(NewNamespaceEvent e) {
		Builder builder = Point.measurement("log")
				.tag("type", "namespace")
				.addField("level", Level.CONFIG.getName())
				.addField("logger", e.value() + "")
				.addField("message", "Namespace " + e.value() + " has been deployed")
				.time(e.timestamp(), TimeUnit.MILLISECONDS);
		return builder.build();
	}
	
	private Point handle(ChangeEvent e) {
		Point point = null;
		TaggedValue<Serializable> taggedValue = TaggedValue
				.instanceFrom(e.value(), Serializable.class);
		if (taggedValue.value() instanceof Number
				|| taggedValue.value() instanceof Boolean
				|| taggedValue.value() instanceof String) {
			point = makeRequestString(e, taggedValue.value(),
					taggedValue.tags());
		} else if (taggedValue.value() instanceof Range<?>) {
			Range<?> range = (Range<?>) taggedValue.value();
			Class<?> clazz = range.hasLowerBound()
					? range.lowerEndpoint().getClass()
					: range.upperEndpoint().getClass();
			if (Number.class.isAssignableFrom(clazz)) {
				point = makeRequestString(e, taggedValue.value(),
						taggedValue.tags());
			} else {
				logger.warn(
						"Not supported type " + clazz.getCanonicalName());
			}
		} else {
			logger.warn("Not supported type "
					+ taggedValue.value().getClass().getCanonicalName());
		}
		return point;
	}

	private Point makeRequestString(ChangeEvent e, Serializable value,
			Map<String, String> tags) {
		Builder point = Point.measurement(e.variable()).tag(tags)
				.time(e.timestamp(), TimeUnit.MILLISECONDS);
		if (value instanceof Range<?>) {
			Range<?> range = (Range<?>) value;
			point.addField("start", (Number) range.lowerEndpoint());
			point.addField("end", (Number) range.upperEndpoint());
		} else if (value instanceof Number) {
			point.addField("value", (Number) value);
		} else if (value instanceof Boolean) {
			point.addField("value", (Boolean) value);
		} else if (value instanceof String) {
			point.addField("value", (String) value);
		}
		return point.build();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pascani.dsl.dbmapper.DbInterface#openConnection()
	 */
	@Override public void openConnection() throws Exception {
		this.influxDB = InfluxDBFactory.connect(this.props.get("uri"),
				this.props.get("user"), this.props.get("password"));
		// Flush every ${flush_points} Points, at least every
		// ${flush_interval}ms
		this.influxDB.enableBatch(
				Integer.parseInt(this.props.get("flush_points")),
				Integer.parseInt(this.props.get("flush_interval")),
				TimeUnit.MILLISECONDS);
		if (!this.influxDB.describeDatabases()
				.contains(this.props.get("database")))
			influxDB.createDatabase(this.props.get("database"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pascani.dsl.dbmapper.DbInterface#closeConnection()
	 */
	@Override public void closeConnection() throws Exception {
		// Nothing to do (REST interface)
	}

}
