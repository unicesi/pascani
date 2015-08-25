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

import java.util.List;

/**
 * Standard probe interface
 * 
 * @param <T>
 *            The type of events it is intended to handle
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public interface Probe<T extends Event<?>> {

	/**
	 * Removes all of the event objects raised from {@code timestamp} until now.
	 * 
	 * @param timestamp
	 *            The initial date of search in epoch format
	 * @return {@code true} if the measured data was removed; {@code false}
	 *         otherwise
	 */
	public boolean cleanData(long timestamp);

	/**
	 * Counts the number of event objects raised from {@code timestamp} until
	 * now.
	 * 
	 * @param timestamp
	 *            The initial date of search in epoch format
	 * @return the number of events raised after {@code timestamp}
	 */
	public int count(long timestamp);

	/**
	 * Counts the number of event objects raised from {@code timestamp} until
	 * now, while at the same time removes them.
	 * 
	 * @see Probe#count(String, long)
	 * @see Probe#cleanData(String, long)
	 * 
	 * @param timestamp
	 *            The initial date of search in epoch format
	 * @return the number of events raised after {@code timestamp}
	 */
	public int countAndClean(long timestamp);

	/**
	 * Fetches the event objects raised from {@code timestamp} until now.
	 * 
	 * @param timestamp
	 *            The initial date of search in epoch format
	 * @return a {@link List} containing the event objects
	 */
	public List<T> fetch(long timestamp);

	/**
	 * Fetches the event objects raised from {@code timestamp} until now, while
	 * at the same time removes them.
	 * 
	 * @see Probe#fetch(String, long)
	 * @see Probe#cleanData(String, long)
	 * 
	 * @param timestamp
	 *            The initial date of search in epoch format
	 * @return a {@link List} containing the event objects
	 */
	public List<T> fetchAndClean(long timestamp);

}