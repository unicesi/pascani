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
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ForwardingSortedSet;

/**
 * A {@link SortedSet} that further provides <i>logging capabilities</i> when a
 * new element is added, either by {@link #add(E)} or
 * {@link #addAll(Collection)}.
 *
 * @param <E>
 *            The type of elements maintained by this set
 *
 * @author Miguel Jiménez - Initial contribution and API
 */
public class LoggingSortedSet<E> extends ForwardingSortedSet<E> {

	private final Logger logger = LogManager
			.getLogger(LoggingSortedSet.class);
	
	private final String format;
	private final SortedSet<E> delegate;

	public LoggingSortedSet(String format) {
		this.format = format;
		this.delegate = new TreeSet<E>();
	}

	@Override protected SortedSet<E> delegate() {
		return this.delegate;
	}

	@Override public boolean add(E element) {
		logger.info(String.format(format, element));
		return this.delegate.add(element);
	}

	@Override public boolean addAll(Collection<? extends E> collection) {
		return super.standardAddAll(collection);
	}
}
