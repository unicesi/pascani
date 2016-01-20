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
package org.pascani.dsl.lib.sca.intents;

import org.osoa.sca.annotations.Scope;
import org.ow2.frascati.tinfi.api.IntentHandler;
import org.pascani.dsl.lib.Event;
import org.pascani.dsl.lib.PascaniRuntime;
import org.pascani.dsl.lib.Probe;
import org.pascani.dsl.lib.infrastructure.AbstractProducer;
import org.pascani.dsl.lib.util.LocalEventProducer;

/**
 * Abstract implementation of a simple {@link IntentHandler} containing a
 * {@link LocalEventProducer} to produce events related to service executions;
 * these events are handled automatically by {@link Probe}s and
 * {@link AbstractProducer}s.
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
@Scope("COMPOSITE")
public abstract class AbstractIntentHandler implements IntentHandler {

	/**
	 * The events producer in charge of posting events to the Pascani runtime
	 */
	protected final LocalEventProducer<Event<?>> producer;

	/**
	 * Initializes the {@link LocalEventProducer} in the PROBE context
	 */
	protected AbstractIntentHandler() {
		this.producer = new LocalEventProducer<Event<?>>(
				PascaniRuntime.Context.PROBE);
	}

}
