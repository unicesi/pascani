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
package org.pascani.dsl.lib.util.dsl;

import org.pascani.dsl.lib.Event;
import org.pascani.dsl.lib.PascaniRuntime;
import org.pascani.dsl.lib.PascaniRuntime.Context;
import org.pascani.dsl.lib.events.ChangeEvent;
import org.pascani.dsl.lib.infrastructure.AbstractConsumer;
import org.pascani.dsl.lib.infrastructure.ProbeProxy;
import org.pascani.dsl.lib.infrastructure.rabbitmq.RabbitMQConsumer;

import com.google.common.base.Function;

/**
 * <b>Note</b>: DSL-only intended use
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public abstract class NonPeriodicEvent<T extends Event<?>>
		extends ManagedEvent {

	public abstract Class<? extends Event<?>> getType();

	public abstract Object getEmitter();

	/**
	 * @return A proxy pointing to the monitor probe producing this event
	 */
	public abstract ProbeProxy getProbe();
	
	/**
	 * @return An event consumer consuming from the probes_exchange with the
	 *         routing key assigned to this event
	 */
	public abstract AbstractConsumer getConsumer();

	public Function<ChangeEvent, Boolean> getSpecifier() {
		return new Function<ChangeEvent, Boolean>() {
			public Boolean apply(ChangeEvent event) {
				return true;
			}
		};
	}

	public void subscribe(final EventObserver<T> eventObserver) {
		addObserver(eventObserver);
	}

	public void unsubscribe(final EventObserver<T> eventObserver) {
		deleteObserver(eventObserver);
	}
	
	protected AbstractConsumer initializeConsumer(final Context context,
			final String routingKey, String consumerTag) throws Exception {
		return initializeConsumer(context, routingKey, consumerTag, null);
	}

	protected AbstractConsumer initializeConsumer(final Context context,
			final String routingKey, final String consumerTag,
			final String variableName) throws Exception {
		String exchange = getType().equals(ChangeEvent.class)
				? PascaniRuntime.getEnvironment().get("namespaces_exchange")
				: PascaniRuntime.getEnvironment().get("probes_exchange");
		return new RabbitMQConsumer(exchange, routingKey, consumerTag,
				context) {
			@Override public void delegateEventHandling(final Event<?> event) {
				if (event.getClass().equals(getType())) {
					boolean notify = true;
					if (getType().equals(ChangeEvent.class)) {
						ChangeEvent changeEvent = (ChangeEvent) event;
						notify = changeEvent.variable().equals(variableName)
								&& getSpecifier().apply(changeEvent);
					}
					if (notify) {
						setChanged();
						notifyObservers(event);
					}
				}
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.util.dsl.ManagedEvent#pause()
	 */
	@Override public synchronized void pause() {
		if (isPaused())
			return;
		getConsumer().pause();
		getProbe().pause();
		super.pause();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.util.dsl.ManagedEvent#resume()
	 */
	@Override public synchronized void resume() {
		if (!isPaused())
			return;
		getConsumer().resume();
		getProbe().resume();
		super.resume();
	}

}
