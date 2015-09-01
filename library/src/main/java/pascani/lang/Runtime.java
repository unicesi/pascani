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
 * along with The SLR Support Tools. If not, see <http://www.gnu.org/licenses/>.
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

import pascani.lang.infrastructure.MessageProducer;
import pascani.lang.infrastructure.Monitor;
import pascani.lang.infrastructure.BasicNamespace;
import pascani.lang.util.EventProducer;

import com.google.common.eventbus.EventBus;

/**
 * This class serves as event bus for all measurement components generating
 * events, such as {@link EventProducer} and {@link Probe} instances.
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class Runtime {

	/**
	 * Specifies whether this runtime resides in the context of a
	 * {@link Monitor}, a {@link Probe}, a {@link BasicNamespace} or a {@link Probe}
	 * in the context of the measurement library.
	 * 
	 * @author Miguel Jiménez - Initial contribution and API
	 */
	public static enum Context {
		MONITOR, PROBE, NAMESPACE, LIBRARY
	}

	/**
	 * A Map storing {@link pascani.lang.Runtime} instances. Useful when a
	 * system is being measured by {@link Probe} instances and custom
	 * measurement mechanisms
	 */
	private final static Map<String, pascani.lang.Runtime> runtimes = new HashMap<String, Runtime>();

	/**
	 * The context in which this runtime resides
	 */
	private final pascani.lang.Runtime.Context context;

	/**
	 * The event bus for {@link Event} objects
	 */
	private final EventBus eventBus;

	/**
	 * A map containing configuration variables (e.g., default queue and
	 * exchange names)
	 */
	private final Map<String, String> environment;

	/**
	 * The logger
	 */
	private final Logger logger = LogManager.getLogger(getClass());

	/**
	 * @param context
	 *            The context in which this runtime resides
	 */
	private Runtime(Context context) {
		this.context = context;
		this.eventBus = new EventBus(this.context.toString());
		this.environment = new HashMap<String, String>();

		readProperties();
	}

	/**
	 * @param context
	 *            The context in which this runtime resides
	 * @return a runtime singleton
	 */
	public static Runtime getRuntimeInstance(Context context) {
		if (!runtimes.containsKey(context.toString())) {
			runtimes.put(context.toString(), new Runtime(context));
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
	 * Event listeners may be {@link Probe} or {@link MessageProducer}
	 * instances.
	 * </p>
	 * 
	 * @param listener
	 *            The event listener
	 */
	public void registerEventListener(Object listener) {
		this.eventBus.register(listener);
	}

	public Map<String, String> getEnvironment() {
		return this.environment;
	}

	/**
	 * Reads configuration properties
	 */
	private void readProperties() {
		Properties config = new Properties();
		InputStream input = null;
		boolean ok = false;

		try {
			input = getClass().getClassLoader().getResourceAsStream(
					"pascani.properties");
			config.load(input);
			ok = true;
		} catch (FileNotFoundException e) {
			logger.warn("No configuration file was found. Execution is started with default values");
		} catch (IOException e) {
			logger.error("Error loading configuration file. Execution is started with default values");
		} finally {

			if (!ok) {
				// Set defaults
				config.put("uri", "amqp://guest:guest@localhost:5672");
				config.put("probes_exchange", "probes_exchange");
				config.put("namespace_exchange", "namespace_exchange");
				config.put("rpc_exchange", "rpc_exchange");
				config.put("rpc_queue_prefix", "rpc_");
			}

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
	public final pascani.lang.Runtime.Context context() {
		return this.context;
	}

}
