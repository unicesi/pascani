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
package org.pascani.dsl.lib.util.log4j2;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.pascani.dsl.lib.PascaniRuntime;
import org.pascani.dsl.lib.infrastructure.AbstractProducer;
import org.pascani.dsl.lib.infrastructure.rabbitmq.RabbitMQProducer;

/**
 * @author Miguel Jiménez - Initial contribution and API
 */
@Plugin(name = "Pascani", category = "Core", elementType = "appender", printObject = true)
public final class PascaniAppender extends AbstractAppender {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 8954342145473153314L;

	/**
	 * The read lock for appending new loggin events
	 */
	private final Lock readLock;

	/**
	 * The event producer connected to RabbitMQ
	 */
	private final AbstractProducer producer;

	private PascaniAppender(String name, Filter filter,
			Layout<? extends Serializable> layout, boolean ignoreExceptions)
			throws Exception {
		super(name, filter, layout, ignoreExceptions);
		this.readLock = new ReentrantReadWriteLock().readLock();
		this.producer = new RabbitMQProducer(
				PascaniRuntime.getEnvironment().get("logs_exchange"),
				this.getClass().getCanonicalName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.logging.log4j.core.Appender#append(org.apache.logging.log4j.
	 * core.LogEvent)
	 */
	@Override public void append(LogEvent event) {
		readLock.lock();
		try {
			org.pascani.dsl.lib.events.LogEvent e = new org.pascani.dsl.lib.events.LogEvent(
					UUID.randomUUID(), event.getLoggerName(),
					event.getLevel().name(),
					event.getMessage().getFormattedMessage());
			producer.produce(e);
		} catch (Exception ex) {
			if (!ignoreExceptions()) {
				throw new AppenderLoggingException(ex);
			}
		} finally {
			readLock.unlock();
		}
	}

	@PluginFactory public static PascaniAppender createAppender(
			@PluginAttribute("name") String name,
			@PluginElement("Layout") Layout<? extends Serializable> layout,
			@PluginElement("Filter") final Filter filter,
			@PluginAttribute("otherAttribute") String otherAttribute)
			throws Exception {
		if (name == null) {
			LOGGER.error("No name provided for the Pascani Appender");
			return null;
		}
		if (layout == null) {
			layout = PatternLayout.createDefaultLayout();
		}
		return new PascaniAppender(name, filter, layout, true);
	}

}
