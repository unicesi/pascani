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
 * along with The Pascani library. If not, see <http://www.gnu.org/licenses/>.
 */
package pascani.lang;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pascani.lang.infrastructure.AbstractProducer;
import pascani.lang.infrastructure.Monitor;
import pascani.lang.infrastructure.BasicNamespace;
import pascani.lang.util.LocalEventProducer;

import com.google.common.eventbus.EventBus;

/**
 * This class serves as event bus for all measurement components generating
 * events, such as {@link LocalEventProducer}.
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class PascaniRuntime {

	/**
	 * Specifies whether this runtime resides in the context of a
	 * {@link Monitor}, a {@link Probe}, a {@link BasicNamespace} or a
	 * {@link Probe} in the context of the measurement library.
	 * 
	 * @author Miguel Jiménez - Initial contribution and API
	 */
	public static enum Context {
		MONITOR, PROBE, NAMESPACE, LIBRARY
	}

	/**
	 * A Map storing {@link PascaniRuntime} instances. Useful when a
	 * system is being measured by {@link Probe} instances and custom
	 * measurement mechanisms
	 */
	private final static Map<String, PascaniRuntime> runtimes = new HashMap<String, PascaniRuntime>();

	/**
	 * The context in which this runtime resides
	 */
	private final PascaniRuntime.Context context;

	/**
	 * The event bus for {@link Event} objects
	 */
	private final EventBus eventBus;

	/**
	 * A map containing configuration variables (e.g., default queue and
	 * exchange names)
	 */
	private static final Map<String, String> environment = new HashMap<String, String>();

	/**
	 * The logger
	 */
	private final static Logger logger = LogManager.getLogger(PascaniRuntime.class);

	static {
		readProperties();
	}
	
	/**
	 * @param context
	 *            The context in which this runtime resides
	 */
	private PascaniRuntime(Context context) {
		this.context = context;
		this.eventBus = new EventBus(this.context.toString());
	}

	/**
	 * @param context
	 *            The context in which this runtime resides
	 * @return a runtime singleton
	 */
	public static PascaniRuntime getRuntimeInstance(Context context) {
		if (!runtimes.containsKey(context.toString())) {
			runtimes.put(context.toString(), new PascaniRuntime(context));
		}

		return runtimes.get(context.toString());
	}

	/**
	 * A simple wrapper of {@link EventBus#post(Object)} ensuring that only
	 * known {@link Event} objects are posted to event listeners.
	 * 
	 * @param event
	 *            The event to be posted
	 */
	public void postEvent(final Event<?> event) {
		this.eventBus.post(event);
	}

	/**
	 * A simple wrapper of {@link EventBus#register(Object)} for listening for
	 * new events.
	 * 
	 * <p>
	 * Event listeners may be {@link Probe} or {@link AbstractProducer}
	 * instances.
	 * </p>
	 * 
	 * @param listener
	 *            The event listener
	 */
	public void registerEventListener(Object listener) {
		this.eventBus.register(listener);
	}

	public static Map<String, String> getEnvironment() {
		return environment;
	}

	/**
	 * Reads configuration properties
	 */
	private static void readProperties() {
		Properties config = new Properties();
		InputStream input = null;

		try {
			input = PascaniRuntime.class.getClassLoader().getResourceAsStream("pascani.properties");
			if (input != null)
				config.load(input);
		} catch (FileNotFoundException e) {
			logger.warn("No configuration file was found. Execution is started with default values");
		} catch (IOException e) {
			logger.error("Error loading configuration file. Execution is started with default values");
		} finally {

			// Set defaults
			if(!config.containsKey("uri"))
				config.put("uri", "amqp://guest:guest@localhost:5672");
			if(!config.containsKey("probes_exchange"))
				config.put("probes_exchange", "probes_exchange");
			if(!config.containsKey("namespaces_exchange"))
				config.put("namespaces_exchange", "namespaces_exchange");
			if(!config.containsKey("monitors_exchange"))
				config.put("monitors_exchange", "monitors_exchange");
			if(!config.containsKey("rpc_exchange"))
				config.put("rpc_exchange", "rpc_exchange");
			if(!config.containsKey("rpc_queue_prefix"))
				config.put("rpc_queue_prefix", "rpc_");

			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					logger.error("Error closing stream of configuration file",
							e);
				}
			}
		}

		for (Object key : config.keySet()) {
			String name = (String) key;
			environment.put(name, config.getProperty(name));
		}
	}

	/**
	 * @return the context in which this runtime resides
	 */
	public final PascaniRuntime.Context context() {
		return this.context;
	}

}
