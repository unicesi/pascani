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
package org.pascani.dsl.lib.sca.probes;

import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Scope;
import org.pascani.dsl.lib.Event;
import org.pascani.dsl.lib.PascaniRuntime;
import org.pascani.dsl.lib.Probe;
import org.pascani.dsl.lib.infrastructure.AbstractProducer;
import org.pascani.dsl.lib.infrastructure.LocalProbe;
import org.pascani.dsl.lib.infrastructure.rabbitmq.RabbitMQProducer;
import org.pascani.dsl.lib.sca.EventHandler;
import org.pascani.dsl.lib.util.Exceptions;
import org.pascani.dsl.lib.util.Resumable;

/**
 * @author Miguel Jiménez - Initial contribution and API
 */
@Scope("COMPOSITE")
public abstract class AbstractProbeImpl implements EventHandler, Resumable {
	
	/**
	 * A SCA property used to set key-value pairs useful for initialization of
	 * this probe.
	 * <p>
	 * The key-value pairs must follow this notation: key=value, without spaces.
	 * <p>
	 * Possible keys are:
	 * <ul>
	 * <li>probe: whether to use or not a local probe
	 * <li>producer: whether to use or not an event producer
	 * <li>routingkey: the routing key belonging to this probe
	 * <li>shutdown: shutdowns probe, producer or both. Values are: probe,
	 * producer, both
	 * <li>pascani.*: Pascani properties. Where * can be replaced for a property
	 * name
	 */
	protected String property = null;

	/**
	 * The unique name among all the {@link Probe} instances
	 */
	protected String routingKey = null;

	/**
	 * Utility variable to reset the {@link Probe} instance
	 */
	protected Boolean useProbe = false;

	/**
	 * Utility variable to reset the {@link AbstractProducer} instance
	 */
	protected Boolean useProducer = false;

	/**
	 * The event producer sending events to the exchange as event occur
	 */
	protected AbstractProducer producer;

	/**
	 * The monitor probe storing events
	 */
	protected LocalProbe probe;

	/**
	 * The accepted events handled by this monitor probe
	 */
	protected final Class<? extends Event<?>>[] acceptedTypes;

	/**
	 * The variable representing the current state (paused or not)
	 */
	private volatile boolean paused = false;

	public AbstractProbeImpl(final Class<? extends Event<?>>... acceptedTypes) {
		this.acceptedTypes = acceptedTypes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.pascani.dsl.lib.sca.EventHandler#handle(org.pascani.dsl.lib.Event)
	 */
	public void handle(Event<?> event) {
		if (this.probe != null)
			this.probe.recordEvent(event);
		if (this.producer != null)
			this.producer.produce(event);
	}
	
	@Property public void setProperty(final String property) {
		String[] data = property.split("=");
		String name = data[0].trim();
		String value = data[0].trim();
		if (name.startsWith("pascani.")) {
			System.setProperty(name, value);
		} else if (name.equals("routingkey")) {
			this.routingKey = value;
			// This is done only the first time the routing key is set
			// and in case it was set after updating properties
			// resetProbe/resetProducer
			if (this.useProbe && this.probe == null)
				resetProbe();
			if (this.useProducer && this.producer == null)
				resetProducer();
		} else if (name.equals("probe")) {
			this.useProbe = Boolean.valueOf(value);
			resetProbe();
		} else if (name.equals("producer")) {
			this.useProducer = Boolean.valueOf(value);
			resetProducer();
		} else if (name.equals("shutdown")) {
			shutdown(value.equals("probe") || value.equals("both"),
					value.equals("producer") || value.equals("both"));
		}
	}
	
	public void shutdown(final boolean probe, final boolean producer) {
		try {
			if (producer && this.producer != null) {
				this.producer.shutdown();
				this.producer = null;
			}
			if (probe && this.probe != null) {
				this.probe.shutdown();
				this.probe = null;
			}
		} catch (Exception e) {
			Exceptions.sneakyThrow(e);
		}
	}

	private void resetProbe() {
		if (this.routingKey == null)
			return;
		try {
			// shutdown in case it is already initialized
			if (this.probe != null)
				this.probe.shutdown();
			this.probe = new LocalProbe(this.routingKey,
					PascaniRuntime.Context.PROBE);
			this.probe.acceptOnly(this.acceptedTypes);
		} catch (Exception e) {
			Exceptions.sneakyThrow(e);
		}
	}

	private void resetProducer() {
		if (this.routingKey == null)
			return;
		try {
			// shutdown in case it is already initialized
			if (this.producer != null)
				this.producer.shutdown();
			this.producer = new RabbitMQProducer(
					PascaniRuntime.getEnvironment().get("probes_exchange"),
					this.routingKey);
			this.producer.acceptOnly(this.acceptedTypes);
		} catch (Exception e) {
			Exceptions.sneakyThrow(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.pascani.dsl.lib.util.Resumable#pause()
	 */
	public void pause() {
		if (paused)
			return;
		this.paused = true;
		if (this.probe != null)
			this.probe.pause();
		if (this.producer != null)
			this.producer.pause();
	}

	/*
	 * (non-Javadoc)
	 * @see org.pascani.dsl.lib.util.Resumable#unpause()
	 */
	public void unpause() {
		if (!this.paused)
			return;
		this.paused = false;
		if (this.probe != null)
			this.probe.unpause();
		if (this.producer != null)
			this.producer.unpause();
	}

	/*
	 * (non-Javadoc)
	 * @see org.pascani.dsl.lib.util.Resumable#isPaused()
	 */
	public boolean isPaused() {
		return this.paused;
	}

	public LocalProbe probe() {
		return this.probe;
	}

	public AbstractProducer producer() {
		return this.producer;
	}

	public String routingKey() {
		return this.routingKey;
	}

}
