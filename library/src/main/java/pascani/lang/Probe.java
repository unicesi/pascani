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
package pascani.lang;

import java.util.List;

/**
 * Standard probe interface
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public interface Probe {

	/**
	 * Removes all of the event objects raised from {@code start} until
	 * {@code end}.
	 * 
	 * @param start
	 *            The initial date of search in epoch format
	 * @param end
	 *            The final date of search in epoch format
	 * @param eventTypes
	 *            An optional filter to only affect events by their type
	 * @return whether at least one element was removed or not
	 */
	public boolean cleanData(long start, long end,
			List<Class<? extends Event<?>>> eventTypes);

	/**
	 * Counts the number of event objects raised from {@code start} until
	 * {@code end}.
	 * 
	 * @param start
	 *            The initial date of search in epoch format
	 * @param end
	 *            The final date of search in epoch format
	 * @param eventTypes
	 *            An optional filter to only affect events by their type
	 * 
	 * @return the number of events raised within the given time window
	 */
	public int count(long start, long end,
			List<Class<? extends Event<?>>> eventTypes);

	/**
	 * Counts the number of event objects raised from {@code start} until
	 * {@code end}, while at the same time removes them.
	 * 
	 * @see Probe#count(long, long, List)
	 * @see Probe#cleanData(long, long, List)
	 * 
	 * @param start
	 *            The initial date of search in epoch format
	 * @param end
	 *            The final date of search in epoch format
	 * @param eventTypes
	 *            An optional filter to only affect events by their type
	 * 
	 * @return the number of events raised after {@code timestamp}
	 */
	public int countAndClean(long start, long end,
			List<Class<? extends Event<?>>> eventTypes);

	/**
	 * Fetches the event objects raised from {@code start} until {@code end}.
	 * 
	 * @param start
	 *            The initial date of search in epoch format
	 * @param end
	 *            The final date of search in epoch format
	 * @param eventTypes
	 *            An optional filter to only affect events by their type
	 * 
	 * @return a {@link List} containing the event objects
	 */
	public List<Event<?>> fetch(long start, long end,
			List<Class<? extends Event<?>>> eventTypes);

	/**
	 * Fetches the event objects raised from {@code start} until {@code end},
	 * while at the same time removes them.
	 * 
	 * @see Probe#fetch(long, long, List)
	 * @see Probe#cleanData(long, long, List)
	 * 
	 * @param start
	 *            The initial date of search in epoch format
	 * @param end
	 *            The final date of search in epoch format
	 * @param eventTypes
	 *            An optional filter to only affect events by their type
	 * 
	 * @return a {@link List} containing the event objects
	 */
	public List<Event<?>> fetchAndClean(long start, long end,
			List<Class<? extends Event<?>>> eventTypes);

}