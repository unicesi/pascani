/*
 * Copyright © 2015 Universidad Icesi
 * 
 * This file is part of the Pascani project.
 * 
 * The Pascani project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * The Pascani project is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with The Pascani project. If not, see <http://www.gnu.org/licenses/>.
 */
package org.pascani.dsl.lib.util.events;

import java.util.HashMap;
import java.util.Map;
import java.util.Observer;

import org.pascani.dsl.lib.Event;
import org.pascani.dsl.lib.util.Resumable;

/**
 * <b>Note</b>: DSL-only intended use
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public abstract class ManagedEvent<T extends Event<?>> extends Observable
		implements Resumable {

	/**
	 * The variable holding the event state: is it paused?
	 */
	private volatile boolean paused;
	
	/**
	 * Data bound to the subscription
	 */
	protected Map<Observer, Map<String, Object>> data;
	
	public ManagedEvent() {
		this.paused = false;
		this.data = new HashMap<Observer, Map<String,Object>>();
	}
	
	public void subscribe(final EventObserver<T>... eventObservers) {
		subscribe(eventObservers, new HashMap<String, Object>());
	}

	public void subscribe(final EventObserver<T>[] eventObservers,
			final Map<String, Object> data) {
		for (EventObserver<T> eventObserver : eventObservers) {
			subscribe(eventObserver, data);
		}
	}
	
	public void subscribe(final EventObserver<T> eventObserver,
			final Map<String, Object> data) {
		this.data.put(eventObserver, data);
		addObserver(eventObserver);
	}

	public void unsubscribe(final EventObserver<T>... eventObservers) {
		for (EventObserver<T> eventObserver : eventObservers)
			deleteObserver(eventObserver);
	}
	
	@Override public void notifyObservers(Object e) {
		Object[] localArray;
		synchronized (this) {
			if (!changed)
				return;
			localArray = obs.toArray();
			clearChanged();
		}
		for (int i = localArray.length - 1; i >= 0; i--) {
			Observer observer = (Observer) localArray[i];
			Map<String, Object> bindingData = data(observer) == null
					? new HashMap<String, Object>() : data(observer);
			observer.update(this, new Object[] { e, bindingData });
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.util.Resumable#pause()
	 */
	public void pause() {
		this.paused = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.util.Resumable#unpause()
	 */
	public void unpause() {
		this.paused = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pascani.lang.util.Resumable#isPaused()
	 */
	public boolean isPaused() {
		return this.paused;
	}
	
	public Map<String, Object> data(Observer observer) {
		return this.data.get(observer);
	}

}
