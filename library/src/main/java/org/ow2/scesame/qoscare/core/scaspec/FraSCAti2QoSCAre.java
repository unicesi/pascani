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

import java.util.Collection;

import org.ow2.frascati.remote.introspection.resources.Attribute;
import org.ow2.frascati.remote.introspection.resources.Binding;
import org.ow2.frascati.remote.introspection.resources.BindingKind;
import org.ow2.frascati.remote.introspection.resources.Component;
import org.ow2.frascati.remote.introspection.resources.ComponentStatus;
import org.ow2.frascati.remote.introspection.resources.Interface;
import org.ow2.frascati.remote.introspection.resources.Method;
import org.ow2.frascati.remote.introspection.resources.Parameter;
import org.ow2.frascati.remote.introspection.resources.Port;
import org.ow2.frascati.remote.introspection.resources.Property;

public final class FraSCAti2QoSCAre {

	public final synchronized static SCADomain convert(String domainname,
			Collection<Component> frascatiComponents) {

		SCADomain qosCareDomain = new SCADomain(domainname);
		for (Component c : frascatiComponents) {
			qosCareDomain.composites.add(convertComponent(c));
		}
		return qosCareDomain;
	}

	public final static SCAComponent convertComponent(Component fc) {
		SCAComponent qc = new SCAComponent(fc.getName(),
				convertComponentStatus(fc.getStatus()));

		for (Component ch : fc.getComponents()) {
			qc.children.add(convertComponent(ch));
		}
		for (Property pr : fc.getProperties()) {
			qc.properties.add(convertProperty(pr));
		}
		for (Port svc : fc.getServices()) {
			qc.services.add(convertPort(svc));
		}
		for (Port ref : fc.getReferences()) {
			qc.references.add(convertPort(ref));
		}
		return qc;
	}

	public final static SCAAttribute convertAttribute(Attribute at) {
		return new SCAAttribute(at.getName(), at.getValue(), at.getType());
	}

	public final static SCAProperty convertProperty(Property pr) {
		return new SCAProperty(pr.getName(), pr.getValue(), pr.getType());
	}

	public final SCAParameter convertParameter(Parameter par) {
		return new SCAParameter(par.getName(), par.getType());
	}

	public final SCAInterface convertInterface(Interface ifc) {
		SCAInterface qifc = new SCAInterface(ifc.getName(), ifc.getClazz());
		for (Method m : ifc.getMethods()) {
			qifc.methods.add(convertMethod(m));
		}
		return qifc;
	}

	public final SCAMethod convertMethod(Method fm) {
		SCAMethod qm = new SCAMethod(fm.getName(), fm.getResult());

		for (Component c : fm.getIntents()) {
			qm.intents.add(convertComponent(c));
		}
		for (String s : fm.getExceptions()) {
			qm.exceptions.add(s);
		}
		for (Parameter p : fm.getParameters()) {
			qm.parameters.add(convertParameter(p));
		}

		return qm;
	}

	public final static SCAPort convertPort(Port fp) {
		SCAPort qp = new SCAPort(fp.getName(), fp.getWiredTo());
		// qp.implement = convertInterface(fp.implement);
		// qp.implement = null;
		for (Binding b : fp.getBindings()) {
			qp.bindings.add(convertBinding(b));
		}
		return qp;
	}

	public final static SCABinding convertBinding(Binding fb) {
		SCABinding qb = new SCABinding(convertBindingKind(fb.getKind()));
		for (Attribute a : fb.getAttributes()) {
			qb.attributs.add(convertAttribute(a));
		}
		return qb;
	}

	public final static SCAComponent.Status convertComponentStatus(
			ComponentStatus fs) {
		switch (fs) {
			case STARTED:
				return SCAComponent.Status.STARTED;
			case STOPPED:
				return SCAComponent.Status.STOPPED;
			default:
				return SCAComponent.Status.UNKNOWN;
		}
	}

	public final static SCABinding.Kind convertBindingKind(BindingKind fbk) {
		switch (fbk) {
			case WS:
				return SCABinding.Kind.WS;
			case REST:
				return SCABinding.Kind.REST;
			case RMI:
				return SCABinding.Kind.RMI;
			case JSON_RPC:
				return SCABinding.Kind.JSONRPC;
			case JMS:
				return SCABinding.Kind.JMS;
			default:
				return SCABinding.Kind.UNKNOWN;
		}
	}
}
