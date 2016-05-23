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

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.osoa.sca.annotations.Scope;
import org.pascani.dsl.lib.PascaniRuntime;
import org.pascani.dsl.lib.events.ChangeEvent;
import org.pascani.dsl.lib.events.LogEvent;
import org.pascani.dsl.lib.events.NewMonitorEvent;
import org.pascani.dsl.lib.events.NewNamespaceEvent;

/**
 * @author Miguel Jiménez - Initial contribution and API
 */
@Scope("COMPOSITE")
public class Main implements Runnable {

	private static final Logger logger = LogManager.getLogger(Main.class);

	public static void main(String[] args) {
		Main main = new Main();
		main.run();
	}

	public void run() {
		Map<String, String> env = PascaniRuntime.getEnvironment();
		String namespaces = env.get("namespaces_exchange");
		String monitors = env.get("monitors_exchange");
		String logs = env.get("logs_exchange");
		DbInterface db = new Influxdb();
		try {
			EventSerializer[] serializers = {
				new EventSerializer(namespaces, "#", ChangeEvent.class, db),
				new EventSerializer(namespaces, "org.pascani.deployment", NewNamespaceEvent.class, db),
				new EventSerializer(monitors, "org.pascani.deployment", NewMonitorEvent.class, db),
				new EventSerializer(logs, "", LogEvent.class, db)
			};
			addShutdownHook(serializers);
		} catch (Exception e) {
			logger.error("Error initializing class " + this.getClass().getCanonicalName(), e);
			e.printStackTrace();
		}
	}

	private void addShutdownHook(final EventSerializer... serializers) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override public void run() {
				try {
					for (EventSerializer serializer : serializers) {
						serializer.shutdown();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
