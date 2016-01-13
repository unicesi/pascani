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
package org.pascani.dsl.lib.infrastructure;

import org.pascani.dsl.lib.util.Resumable;

/**
 * @author Miguel Jiménez - Initial contribution and API
 */
public abstract class Monitor implements Resumable {

	private volatile boolean paused = false;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pascani.dsl.lib.util.Resumable#pause()
	 */
	public void pause() {
		this.paused = true;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pascani.dsl.lib.util.Resumable#resume()
	 */
	public void resume() {
		this.paused = false;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pascani.dsl.lib.util.Resumable#isPaused()
	 */
	public boolean isPaused() {
		return this.paused;
	}

}
