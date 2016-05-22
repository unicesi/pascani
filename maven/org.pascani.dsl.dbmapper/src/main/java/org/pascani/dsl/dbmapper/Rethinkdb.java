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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import org.pascani.dsl.lib.Event;
import org.pascani.dsl.lib.events.ChangeEvent;
import org.pascani.dsl.lib.events.NewMonitorEvent;
import org.pascani.dsl.lib.events.NewNamespaceEvent;
import org.pascani.dsl.lib.util.ConfigProperties;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;

/**
 * @author Miguel Jiménez - Initial contribution and API
 */
public class Rethinkdb implements DbInterface {

	/**
	 * The Singleton to use to interact with the RethinkDB Driver
	 */
	private static final RethinkDB r = RethinkDB.r;

	/**
	 * The connection to the Rethinkdb database
	 */
	private Connection connection;

	/**
	 * The Rethinkdb configuration properties
	 */
	private final Map<String, String> props;

	/**
	 * The Json<->Event mapper
	 */
	private final JsonUtility jsonUtility;

	public Rethinkdb() {
		this.props = readConfigProperties();
		this.jsonUtility = new JsonUtility();
	}

	/**
	 * @return the Rethinkdb configuration properties
	 */
	private Map<String, String> readConfigProperties() {
		Map<String, String> defaultProps = new HashMap<String, String>();
		defaultProps.put("hostname", "localhost");
		defaultProps.put("port", "28015");
		defaultProps.put("database", "test");
		ConfigProperties config = new ConfigProperties("rethinkdb.properties",
				"rethinkdb.", defaultProps);
		return config.readProperties();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pascani.dsl.dbmapper.DbInterface#save(org.pascani.dsl.lib.Event)
	 */
	public <T extends Event<?>> void save(T event) throws Exception {
		// TODO: do not insert duplicate monitors, namespaces, variables & usings
		long createdAt = new Date().getTime();
		Map<String, Object> eventData = null;
		String table = null;
		if (event instanceof ChangeEvent) {
			eventData = toJson((ChangeEvent) event);
			table = "values";
		} else if (event instanceof NewMonitorEvent) {
			NewMonitorEvent e = (NewMonitorEvent) event;
			eventData = toJson(e);
			table = "monitors";
			List<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();
			for (String namespace : e.namespaces()) {
				Map<String, Object> data = new HashMap<String, Object>();
				data.put("createdAt", createdAt);
				data.put("monitor", e.value());
				data.put("namespace", namespace);
				maps.add(data);
			}
			r.db(this.props.get("database"))
			 .table("monitors_namespaces")
			 .insert(maps)
			 .run(this.connection);
		} else if (event instanceof NewNamespaceEvent) {
			NewNamespaceEvent e = (NewNamespaceEvent) event;
			eventData = toJson(e);
			table = "namespaces";
			List<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();
			for (String variable : e.variables()) {
				Map<String, Object> data = new HashMap<String, Object>();
				data.put("createdAt", createdAt);
				data.put("name", variable);
				data.put("namespace", e.value());
				maps.add(data);
			}
			r.db(this.props.get("database"))
			 .table("variables")
			 .insert(maps)
			 .run(this.connection);
		}
		
		r.db(this.props.get("database"))
		 .table(table)
		 .insert(eventData)
		 .run(this.connection);
	}
	
	private Map<String, Object> toJson(ChangeEvent e) {
		Map<String, Object> vars = new HashMap<String, Object>();
		vars.put("createdAt", e.timestamp());
		vars.put("variable", e.variable());
		vars.put("value", jsonUtility.toJson(e.value()));
		return vars;
	}
	
	private Map<String, Object> toJson(NewMonitorEvent e) {
		Map<String, Object> vars = new HashMap<String, Object>();
		vars.put("createdAt", e.timestamp());
		vars.put("name", e.value());
		return vars;
	}
	
	private Map<String, Object> toJson(NewNamespaceEvent e) {
		Map<String, Object> vars = new HashMap<String, Object>();
		vars.put("createdAt", e.timestamp());
		vars.put("name", e.value());
		return vars;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pascani.dsl.dbmapper.DbInterface#retrieve(java.util.Map)
	 */
	public <T extends Event<?>> T retrieve(Map<String, String> params)
			throws Exception {
		throw new NotImplementedException("Not implemented yet!");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pascani.dsl.dbmapper.DbInterface#openConnection()
	 */
	public void openConnection() throws Exception {
		this.connection = r.connection().hostname(this.props.get("hostname"))
				.port(Integer.parseInt(this.props.get("port"))).connect();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pascani.dsl.dbmapper.DbInterface#closeConnection()
	 */
	public void closeConnection() throws Exception {
		this.connection.close();
	}

	public Map<String, String> configProps() {
		return this.props;
	}

}
