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
package pascani.lang.util.dsl;

import com.google.common.base.Function;

import pascani.lang.Event;
import pascani.lang.events.ChangeEvent;
import pascani.lang.infrastructure.ProbeProxy;

/**
 * <b>Note</b>: DSL-only intended use
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public abstract class NonPeriodicEvent<T extends Event<?>>
		extends ManagedEvent {

	public abstract Class<? extends Event<?>> getType();

	public abstract Object getEmitter();

	/**
	 * @return A proxy pointing to the monitor probe producing this event
	 */
	public abstract ProbeProxy getProbe();

	public Function<ChangeEvent, Boolean> getSpecifier() {
		return new Function<ChangeEvent, Boolean>() {
			public Boolean apply(ChangeEvent event) {
				return true;
			}
		};
	}

	public void subscribe(final EventObserver<T> eventObserver) {
		addObserver(eventObserver);
	}

	public void unsubscribe(final EventObserver<T> eventObserver) {
		deleteObserver(eventObserver);
	}

}
