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

import java.util.HashMap;
import java.util.Map;

import pascani.lang.infrastructure.MessageProducer;
import pascani.lang.monitors.AbstractMonitor;

import com.google.common.eventbus.EventBus;

/**
 * TODO: class description
 * 
 * The default runtime for an independent subsystem; i.e., a SCA composite, an
 * EJB, an OSGi bundle, etc.
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class Runtime {

	/**
	 * Specifies whether this runtime resides in the context of a {@link Probe},
	 * an {@link AbstractMonitor} or in the measurement library.
	 * 
	 * TODO: specify the measurement library class
	 * 
	 * @author Miguel Jiménez - Initial contribution and API
	 */
	public static enum Context {
		MONITOR, PROBE, LIBRARY
	}

	/**
	 * TODO
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
	 * @param context
	 *            The context in which this runtime resides
	 */
	private Runtime(Context context) {
		this.context = context;
		eventBus = new EventBus(this.context.toString());
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

	/**
	 * @return the context in which this runtime resides
	 */
	public final pascani.lang.Runtime.Context context() {
		return this.context;
	}

}
