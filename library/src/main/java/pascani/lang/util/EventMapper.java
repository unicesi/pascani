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
package pascani.lang.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pascani.lang.Event;
import pascani.lang.events.NamedEventDecorator;

import com.google.common.collect.Lists;

/**
 * This class maps a collection of {@link NamedEventDecorator} instances by
 * grouping instances with the same key.
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class EventMapper {

	private final Collection<NamedEventDecorator> events;

	public EventMapper(Collection<NamedEventDecorator> namedEvents) {
		this.events = namedEvents;
	}

	public EventMapper(NamedEventDecorator... namedEvents) {
		this(Lists.newArrayList(namedEvents));
	}

	/**
	 * @return a collection containing the unmapped events
	 */
	public Collection<NamedEventDecorator> unmappedEvents() {
		return this.events;
	}

	/**
	 * @return a map containing {@link Event} instances grouped by key
	 */
	public Map<String, List<Event<?>>> map() {
		Map<String, List<Event<?>>> map = new HashMap<String, List<Event<?>>>();

		for (NamedEventDecorator event : this.events) {
			if (!map.containsKey(event.key()))
				map.put(event.key(), new ArrayList<Event<?>>());

			Event<?> decoratedEvent = event.decoratedEvent();
			map.get(event.key()).add(decoratedEvent);
		}

		return map;
	}

}
