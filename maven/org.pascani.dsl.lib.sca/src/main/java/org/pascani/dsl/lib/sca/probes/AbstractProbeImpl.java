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
	 * The unique name among all the {@link Probe} instances
	 */
	protected String routingKey = null;

	/**
	 * Utility variable to reset the {@link Probe} instance
	 */
	protected Boolean resetProbe = false;

	/**
	 * Utility variable to reset the {@link AbstractProducer} instance
	 */
	protected Boolean resetProducer = false;

	/**
	 * The {@link Probe} exchange
	 */
	protected String exchange;

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
		this.exchange = PascaniRuntime.getEnvironment().get("probes_exchange");
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

	private void resetProbe() {
		try {
			// shutdown in case it is already initialized
			if (this.probe != null)
				this.probe.shutdown();
			this.probe = new LocalProbe(routingKey,
					PascaniRuntime.Context.PROBE);
			this.probe.acceptOnly(this.acceptedTypes);
		} catch (Exception e) {
			Exceptions.sneakyThrow(e);
		}
	}

	private void resetProducer() {
		try {
			// shutdown in case it is already initialized
			if (this.producer != null)
				this.producer.shutdown();
			this.producer = new RabbitMQProducer(this.exchange,
					this.routingKey);
			this.producer.acceptOnly(this.acceptedTypes);
		} catch (Exception e) {
			Exceptions.sneakyThrow(e);
		}
	}

	public void pause() {
		if (paused)
			return;
		this.paused = true;
		if (this.probe != null)
			this.probe.pause();
		if (this.producer != null)
			this.producer.pause();
	}

	public void resume() {
		if (!this.paused)
			return;
		this.paused = false;
		if (this.probe != null)
			this.probe.resume();
		if (this.producer != null)
			this.producer.resume();
	}

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

	@Property public void setRoutingKey(final String routingKey) {
		this.routingKey = routingKey;

		// This is done only the first time the routing key is set
		// and in case it was set after updating properties
		// resetProbe/resetProducer
		if (this.resetProbe && this.probe == null)
			resetProbe();
		if (this.resetProducer && this.producer == null)
			resetProducer();
	}

	@Property public void setResetProbe(final Boolean resetProbe) {
		this.resetProbe = resetProbe;
		if (this.resetProbe && this.routingKey != null)
			resetProbe();
	}

	@Property public void setResetProducer(final Boolean resetProducer) {
		this.resetProducer = resetProducer;
		if (this.resetProducer && this.routingKey != null)
			resetProducer();
	}

}
