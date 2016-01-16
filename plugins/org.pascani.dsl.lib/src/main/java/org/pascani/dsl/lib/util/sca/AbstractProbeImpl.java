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
package org.pascani.dsl.lib.util.sca;

import org.osoa.sca.annotations.Property;
import org.pascani.dsl.lib.Event;
import org.pascani.dsl.lib.PascaniRuntime;
import org.pascani.dsl.lib.PascaniRuntime.Context;
import org.pascani.dsl.lib.infrastructure.AbstractProducer;
import org.pascani.dsl.lib.infrastructure.LocalProbe;
import org.pascani.dsl.lib.infrastructure.rabbitmq.RabbitMQProducer;
import org.pascani.dsl.lib.util.Resumable;

/**
 * @author Miguel Jiménez - Initial contribution and API
 */
public abstract class AbstractProbeImpl implements Resumable {

	@Property
	protected String routingKey = null;

	@Property
	protected Boolean resetProbe = false;

	@Property
	protected Boolean resetProducer = false;

	protected String exchange;

	protected AbstractProducer producer;

	protected LocalProbe probe;

	protected final Class<? extends Event<?>>[] acceptedTypes;

	private volatile boolean paused = false;

	public AbstractProbeImpl(final Class<? extends Event<?>>... acceptedTypes) {
		this.acceptedTypes = acceptedTypes;
		this.exchange = PascaniRuntime.getEnvironment().get("probes_exchange");
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
			// TODO: handle the exception
			e.printStackTrace();
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
			PascaniRuntime.getRuntimeInstance(Context.PROBE)
					.registerEventListener(producer);
		} catch (Exception e) {
			// TODO: handle the exception
			e.printStackTrace();
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

	public void setRoutingKey(final String routingKey) {
		this.routingKey = routingKey;
	}

	public void setResetProbe(final Boolean resetProbe) {
		this.resetProbe = resetProbe;
		if (this.resetProbe) {
			resetProbe();
		}
	}

	public void setResetProducer(final Boolean resetProducer) {
		this.resetProducer = resetProducer;
		if (this.resetProducer) {
			resetProducer();
		}
	}

}
