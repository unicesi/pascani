/***
 * OW2 SCeSAME QoS-CARE Core 
 * Copyright (C) 2010-2011 G. Tamura
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Authors: Gabriel Tamura
 */
package org.ow2.scesame.qoscare.core.scaspec;

import java.util.ArrayList;
import java.util.Collection;

public class SCADomain extends SCANamedNode {
	
	protected Collection<SCAComponent> composites;

	public SCADomain(String dname) {
		super("Component Domain Container");
		composites = new ArrayList<SCAComponent>();
	}

	public Collection<SCAComponent> getComposites() {
		return this.composites;
	}

	public void setComposites(Collection<SCAComponent> composites) {
		this.composites = composites;
	}

	public void addComposite(SCAComponent composite) {
		this.composites.add(composite);
	}
	
	public String getName() {
		return name;
	}

}
