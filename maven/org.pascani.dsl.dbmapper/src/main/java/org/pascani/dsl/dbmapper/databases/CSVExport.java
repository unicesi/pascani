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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.pascani.dsl.dbmapper.DbInterface;
import org.pascani.dsl.lib.Event;
import org.pascani.dsl.lib.events.ChangeEvent;
import org.pascani.dsl.lib.events.LogEvent;
import org.pascani.dsl.lib.events.NewMonitorEvent;
import org.pascani.dsl.lib.events.NewNamespaceEvent;
import org.pascani.dsl.lib.util.TaggedValue;

import com.google.common.collect.Range;

/**
 * Basic implementation of a CSV export mechanism. As columns may change over
 * time, the file is overwritten each time a new column is found (only the
 * header is changed). While columns remain the same, new records are appended
 * using a {@link FileWriter} instance; this instance is replaced when the file
 * is overwritten.
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class CSVExport implements DbInterface {

	/**
	 * The directory in which the CSV files are stored
	 */
	private final String outputDirectory;

	/**
	 * The different "database" collections
	 */
	private final String[] collections = { "variables", "logs" };

	/**
	 * Headers per collection
	 */
	private final Map<String, List<String>> columns;

	/**
	 * Backup of headers per collection. Useful to identify if the file needs to
	 * be rewritten (because headers has changed)
	 */
	private final Map<String, Integer> lastColumns;

	/**
	 * The files corresponding to the "database" collections
	 */
	private final Map<String, File> collectionFiles;

	/**
	 * A in-memory list of records awaiting to be appended to the corresponding
	 * collection
	 */
	private final Map<String, List<Map<String, String>>> tmp;

	/**
	 * The number of events to store in memory before appending to the
	 * corresponding files
	 */
	private final int memorySize;

	/**
	 * The file writers associated to each collection
	 */
	private final Map<String, FileWriter> writers;

	/**
	 * @param outputDirectory
	 *            The directory in which the CSV files are stored
	 * @param memorySize
	 *            The number of events to store in memory before appending to
	 *            the corresponding files
	 */
	public CSVExport(final String outputDirectory, final int memorySize) {
		this.outputDirectory = outputDirectory;
		this.memorySize = memorySize;
		this.columns = new HashMap<String, List<String>>();
		this.lastColumns = new HashMap<String, Integer>();
		this.collectionFiles = new HashMap<String, File>();
		this.tmp = new HashMap<String, List<Map<String, String>>>();
		this.writers = new HashMap<String, FileWriter>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pascani.dsl.dbmapper.DbInterface#save(org.pascani.dsl.lib.Event)
	 */
	@Override public <T extends Event<?>> void save(T event) throws Exception {
		String collection = "logs";
		Map<String, String> data = null;
		if (event instanceof ChangeEvent) {
			collection = "variables";
			data = handle((ChangeEvent) event, collection);
		} else if (event instanceof LogEvent) {
			data = handle((LogEvent) event);
		} else if (event instanceof NewMonitorEvent) {
			data = handle((NewMonitorEvent) event);
		} else if (event instanceof NewNamespaceEvent) {
			data = handle((NewNamespaceEvent) event);
		}
		if (data != null) {
			this.tmp.get(collection).add(data);
			if (this.tmp.get(collection).size() == memorySize)
				write(collection);
		}
	}

	private synchronized void write(String collection) throws IOException {
		List<String> columns = this.columns.get(collection);
		String body = body(collection);
		// Overwrite the file's content (if needed)
		if (this.lastColumns.get(collection) != columns.size()) {
			this.lastColumns.put(collection, columns.size());
			this.writers.get(collection).close();
			this.writers.put(collection,
					new FileWriter(this.collectionFiles.get(collection), true));
			// Read
			BufferedReader file = new BufferedReader(
					new FileReader(this.collectionFiles.get(collection)));
			String previousContents = "";
			String line = file.readLine(); // skip previous header
			while ((line = file.readLine()) != null)
				previousContents += line + '\n';
			file.close();

			// Overwrite
			String contents = header(collection) + previousContents + body;
			FileOutputStream fileOut = new FileOutputStream(
					this.collectionFiles.get(collection));
			fileOut.write(contents.getBytes());
			fileOut.close();
		} else {
			FileWriter writer = this.writers.get(collection);
			writer.append(body);
			writer.flush();
		}
		this.tmp.get(collection).clear();
	}

	private Map<String, String> handle(ChangeEvent e, String collection) {
		Map<String, String> data = null;
		TaggedValue<Serializable> taggedValue = TaggedValue
				.instanceFrom(e.value(), Serializable.class);
		if (taggedValue.value() instanceof Number
				|| taggedValue.value() instanceof Boolean
				|| taggedValue.value() instanceof String) {
			data = toData(collection, e, taggedValue.value(),
					taggedValue.tags());
		} else if (taggedValue.value() instanceof Range<?>) {
			Range<?> range = (Range<?>) taggedValue.value();
			Class<?> clazz = range.hasLowerBound()
					? range.lowerEndpoint().getClass()
					: range.upperEndpoint().getClass();
			if (Number.class.isAssignableFrom(clazz)) {
				data = toData(collection, e, taggedValue.value(),
						taggedValue.tags());
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

	private Map<String, String> toData(String collection, ChangeEvent e,
			Serializable value, Map<String, String> tags) {
		for (String tag : tags.keySet()) {
			if (!this.columns.get(collection).contains(tag))
				this.columns.get(collection).add(tag);
		}
		Map<String, String> data = new HashMap<String, String>();
		data.putAll(tags);
		data.put("variable", e.variable());
		if (value instanceof Range<?>) {
			Range<?> range = (Range<?>) value;
			data.put("start", (Number) range.lowerEndpoint() + "");
			data.put("end", (Number) range.upperEndpoint() + "");
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

	private Map<String, String> handle(LogEvent e) {
		Map<String, String> data = new HashMap<String, String>();
		data.put("type", "execution");
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
		data.put("type", "deployment");
		data.put("level", Level.CONFIG.getName());
		data.put("logger", e.value() + "");
		data.put("message", "Monitor " + e.value() + " has been deployed");
		data.put("source", e.value() + "");
		data.put("timestamp", e.timestamp() + "");
		return data;
	}

	private Map<String, String> handle(NewNamespaceEvent e) {
		Map<String, String> data = new HashMap<String, String>();
		data.put("type", "deployment");
		data.put("level", Level.CONFIG.getName());
		data.put("logger", e.value() + "");
		data.put("message", "Namespace " + e.value() + " has been deployed");
		data.put("source", e.value() + "");
		data.put("timestamp", e.timestamp() + "");
		return data;
	}

	private String header(String collection) {
		StringBuilder header = new StringBuilder();
		List<String> columns = this.columns.get(collection);
		for (int i = 0; i < columns.size(); i++) {
			if (i > 0)
				header.append(",");
			header.append(columns.get(i));
		}
		header.append("\n");
		return header.toString();
	}

	private String body(String collection) {
		StringBuilder body = new StringBuilder();
		List<String> columns = this.columns.get(collection);
		List<Map<String, String>> data = this.tmp.get(collection);
		for (Map<String, String> record : data) {
			for (int i = 0; i < columns.size(); i++) {
				if (i > 0)
					body.append(",");
				String column = columns.get(i);
				body.append(
						record.containsKey(column) ? record.get(column) : "");
			}
			body.append("\n");
		}
		return body.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pascani.dsl.dbmapper.DbInterface#openConnection()
	 */
	@Override public void openConnection() throws Exception {
		initialize();
		for (String collection : this.collections) {
			FileWriter writer = new FileWriter(
					this.collectionFiles.get(collection), true);
			this.writers.put(collection, writer);
			writer.append(header(collection));
		}
	}

	private void initialize() throws IOException {
		File output = new File(this.outputDirectory);
		String suffix = new SimpleDateFormat("_MM-dd-yyyy_HH-mm-ss")
				.format(new Date());
		if (!output.exists()) {
			output.mkdirs();
		}
		for (String collection : this.collections) {
			File file = new File(output, collection + suffix + ".csv");
			if (!file.exists()) {
				file.createNewFile();
			}
			this.collectionFiles.put(collection, file);
			this.tmp.put(collection, new ArrayList<Map<String, String>>());
			this.columns.put(collection, new ArrayList<String>());
		}

		// manually add columns
		this.columns.get("logs").addAll(Arrays.asList("type", "level", "logger",
				"message", "cause", "source", "timestamp"));
		this.columns.get("variables").addAll(Arrays.asList("variable", "value",
				"start", "end", "timestamp"));

		this.lastColumns.put("logs", this.columns.get("logs").size());
		this.lastColumns.put("variables", this.columns.get("variables").size());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pascani.dsl.dbmapper.DbInterface#closeConnection()
	 */
	@Override public void closeConnection() throws Exception {
		// First, write records to the corresponding files, if any
		for (String collection : this.collections) {
			if (!this.tmp.get(collection).isEmpty())
				write(collection);
		}
		for (String collection : this.collections) {
			this.writers.get(collection).close();
		}
	}

}
