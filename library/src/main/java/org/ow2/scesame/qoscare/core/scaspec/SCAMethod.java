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
 * 			Christophe Demarey
 */
package org.ow2.scesame.qoscare.core.scaspec;

import java.util.ArrayList;
import java.util.Collection;

public class SCAMethod extends SCANamedNode {
	public String name;
	public String resultat;
	public Collection<String> exceptions = new ArrayList<String>();
	public Collection<SCAComponent> intents = new ArrayList<SCAComponent>();
	public Collection<SCAParameter> parameters = new ArrayList<SCAParameter>();

	public SCAMethod(String name) {
		super(name);
	}

	public SCAMethod(String name, String resultat) {
		super(name);
		this.resultat = resultat;
	}

	public SCAMethod(String name, String resultat,
			Collection<SCAParameter> parameters) {
		super(name);
		this.resultat = resultat;
		this.parameters.addAll(parameters);
	}

	public Collection<String> getExceptions() {
		return this.exceptions;
	}

	public Collection<SCAComponent> getIntents() {
		return this.intents;
	}

	public Collection<SCAParameter> getParameters() {
		return this.parameters;
	}
}
