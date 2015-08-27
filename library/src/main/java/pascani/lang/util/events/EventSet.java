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

import java.util.Collection;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import pascani.lang.Event;
import pascani.lang.util.LoggingSortedSet;

/**
 * A sorted set of {@link Event} objects with logging capabilities (by means of
 * {@link LoggingSortedSet}) and filter methods based on the raising time of
 * each {@link Event}.
 * 
 * @param <T>
 *            The type of events
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public final class EventSet<T extends Event<?>> extends LoggingSortedSet<T> {

	/**
	 * Creates an instance setting the logging format to a {@link String},
	 * delegating the trace format to {@link Event#toString()}
	 */
	public EventSet() {
		super("%s");
	}

	/**
	 * Filters this {@link EventSet} according to a time window, by checking if
	 * the events were measured (including the final timestamp) within the range
	 * [{@code timestamp}, now]; by means of
	 * {@link Event#isInTimeWindow(long, long)}.
	 * 
	 * <p>
	 * <b>Note</b>: The "now" time is in nanoseconds.
	 * </p>
	 * 
	 * @param timestamp
	 *            The initial timestamp of the filtering criteria, in
	 *            nanoseconds
	 * @return an {@link EventSet} filtered according to the specified timestamp
	 */
	public EventSet<T> filter(final long timestamp) {
		Collection<T> filtered = Collections2.filter(this, new Predicate<T>() {
			public boolean apply(T event) {
				return event.isInTimeWindow(timestamp, System.nanoTime());
			}
		});

		EventSet<T> filteredSet = new EventSet<T>();
		filteredSet.addAll(filtered);

		return filteredSet;
	}

	/**
	 * Removes the {@link Event} objects raised from {@code timestamp} until
	 * now.
	 * 
	 * @param timestamp
	 *            The initial timestamp of the filtering criteria
	 * @return the removed {@link Event} objects
	 */
	public synchronized EventSet<T> clean(final long timestamp) {
		Collection<T> toRemove = filter(timestamp);
		this.removeAll(toRemove);

		return (EventSet<T>) toRemove;
	}

}
