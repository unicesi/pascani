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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Miguel Jiménez - Initial contribution and API
 */
public class ConfigProperties {

	/**
	 * The file containing the configuration properties
	 */
	private final String propertiesFile;

	/**
	 * The prefix used to set properties using system properties
	 */
	private final String systemPrefix;

	/**
	 * The properties and their corresponding default values
	 */
	private final Map<String, String> defaultProps;

	/**
	 * The logger
	 */
	private static final Logger logger = LogManager
			.getLogger(ConfigProperties.class);

	public ConfigProperties(final String propertiesFile,
			final String systemPrefix, final Map<String, String> defaultProps) {
		this.propertiesFile = propertiesFile;
		this.systemPrefix = systemPrefix;
		this.defaultProps = defaultProps;
	}

	/**
	 * Reads properties from file, system properties and map of default
	 * properties.
	 * 
	 * <p>
	 * <b>System properties have priority</b>
	 * </p>
	 * 
	 * @return A map containing all configuration properties with their
	 *         corresponding values
	 */
	public Map<String, String> readProperties() {
		Map<String, String> properties = new HashMap<String, String>();
		Properties config = new Properties();
		InputStream input = null;
		try {
			input = ConfigProperties.class.getClassLoader()
					.getResourceAsStream(this.propertiesFile);
			if (input != null)
				config.load(input);
		} catch (FileNotFoundException e) {
			logger.warn("No configuration file was found", e);
		} catch (IOException e) {
			logger.error("Error loading configuration file", e);
		} finally {
			setPropertiesFromSystem(config);
			setDefaultProperties(config);
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
			properties.put(name, config.getProperty(name));
		}

		return properties;
	}

	private void setPropertiesFromSystem(Properties props) {
		Properties systemProps = System.getProperties();
		for (String key : this.defaultProps.keySet()) {
			if (systemProps.containsKey(this.systemPrefix + key))
				props.put(key,
						systemProps.getProperty(this.systemPrefix + key));
		}
	}

	private void setDefaultProperties(Properties props) {
		for (String key : this.defaultProps.keySet()) {
			if (!props.containsKey(key))
				props.put(key, this.defaultProps.get(key));
		}
	}

}
