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
package org.pascani.dsl.lib.util;

import java.util.Collection;
import java.util.List;

import org.pascani.dsl.lib.Event;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

/**
 * This class filters a collection of {@link Event} instances by keeping only
 * instances of an specific class. This is done by performing a non safe cast in
 * a safe way, that is:
 * 
 * <ol>
 * <li>The collection is filtered keeping the elements of interest (instances of
 * the specific class)</li>
 * <li>Once the collection is filtered, a cast is done from {@link Event} to the
 * specified class.</li>
 * </ol>
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class EventFilter {

	private final Collection<Event<?>> events;

	public EventFilter(Collection<Event<?>> events) {
		this.events = events;
	}

	public EventFilter(Event<?>... events) {
		this(Lists.newArrayList(events));
	}
	
	/**
	 * @return a collection containing the unfiltered events
	 */
	public Collection<Event<?>> unfilteredEvents() {
		return this.events;
	}

	/**
	 * @return a list containing only instances of the specified class
	 */
	public <T extends Event<?>> List<T> filter(final Class<T> subType) {
		Collection<Event<?>> filtered = Collections2.filter(this.events,
				new Predicate<Event<?>>() {
					public boolean apply(Event<?> event) {
						return subType.isInstance(event);
					}
				});

		Collection<T> transformed = Collections2.transform(filtered,
				new Function<Event<?>, T>() {
					@SuppressWarnings("unchecked") public T apply(Event<?> event) {
						return (T) event;
					}
				});

		return Lists.newArrayList(transformed);
	}

}
