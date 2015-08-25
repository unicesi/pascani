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
package pascani.lang.util.events;

import java.util.List;

import pascani.lang.Event;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.math.DoubleMath;

/**
 * This class encapsulates the average calculation for a {@link List} or array
 * of {@link Event} objects with numerical value.
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public final class EventsAverage {

	private final List<? extends Event<? extends Number>> events;

	public EventsAverage(List<? extends Event<? extends Number>> events) {
		this.events = events;
	}

	public EventsAverage(Event<? extends Number>... events) {
		this(Lists.newArrayList(events));
	}

	/**
	 * Calculates the average among the events' values.
	 * 
	 * @return the average for the numerical value events
	 */
	public double value() {
		return DoubleMath.mean(Collections2.transform(events,
				new Function<Event<? extends Number>, Double>() {
					public Double apply(Event<? extends Number> event) {
						return event.value().doubleValue();
					}
				}));
	}

}
