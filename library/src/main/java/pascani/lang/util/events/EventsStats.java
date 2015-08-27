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

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import pascani.lang.Event;

import com.google.common.collect.Lists;

/**
 * This class stores the value of numeric-based events (i.e., events whose value
 * is a {@link Number}) into a {@link DescriptiveStatistics} instance, to easily
 * calculate statistics on a list or array of events.
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public final class EventsStats {

	private final List<? extends Event<? extends Number>> events;

	public EventsStats(List<? extends Event<? extends Number>> events) {
		this.events = events;
	}

	public EventsStats(Event<? extends Number>... events) {
		this(Lists.newArrayList(events));
	}

	/**
	 * Adds the events' (numerical) values to a {@link DescriptiveStatistics}
	 * instance.
	 * 
	 * @return an object maintaining a dataset of values of a single statistical
	 *         variable to compute descriptive statistics based on the events'
	 *         values
	 */
	public DescriptiveStatistics statistics() {
		DescriptiveStatistics stats = new DescriptiveStatistics();

		for (Event<? extends Number> event : this.events) {
			stats.addValue(event.value().doubleValue());
		}

		return stats;
	}

}
