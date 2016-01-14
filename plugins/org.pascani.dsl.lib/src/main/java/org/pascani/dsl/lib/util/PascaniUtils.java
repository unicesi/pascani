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

import static org.pascani.dsl.lib.util.FrascatiUtils.eval;

import java.io.File;
import java.util.Map;

import javax.script.ScriptException;

import org.pascani.dsl.lib.events.ExceptionEvent;
import org.pascani.dsl.lib.events.InvokeEvent;
import org.pascani.dsl.lib.events.ReturnEvent;
import org.pascani.dsl.lib.events.TimeLapseEvent;
import org.pascani.dsl.lib.infrastructure.ProbeProxy;

import com.google.common.io.Resources;

/**
 * @author Miguel Jiménez - Initial contribution and API
 */
public class PascaniUtils {
	
	public static boolean contributionDeployed = false;
	
	/**
	 * Introduces a new monitor probe, and returns a proxy pointing to it. The
	 * probe created manages events of type {@link ExceptionEvent}.
	 * 
	 * @param target
	 *            A FPath selector
	 * @return a {@link ProbeProxy} instance pointing to an exception probe
	 * @throws ScriptException
	 *             if something bad happens. See
	 *             {@link FrascatiUtils#eval(String)}
	 */
	public static ProbeProxy probe(String target) throws ScriptException {
		if (!contributionDeployed) {
			File zipfile = new File(Resources.getResource("probes.zip").getFile());
			contributionDeployed = FrascatiUtils.deploy("probes")
					.withContribution(zipfile).deploy();
		}
		if (contributionDeployed) {
			eval("target = " + target + (target.endsWith(";") ? "" : ";"));
			eval("intent = $domain/scachild::probe;");
			eval("add-scaintent($target, $intent);");
			return createProbeProxy(target);
		} else {
			return null;
		}
	}

	/**
	 * <b>Note</b>: DSL-only intended use
	 * <p>
	 * Introduces a new monitor probe, and returns a proxy pointing to it. The
	 * probe created manages event of type {@link TimeLapseEvent},
	 * {@link InvokeEvent}, and {@link ReturnEvent}
	 * 
	 * @param uniqueName
	 *            A unique name representing the monitor probe
	 * @return a {@link ProbeProxy} instance pointing to a performance probe
	 */
	public static ProbeProxy newPerformanceProbe(String uniqueName) {
		return createProbeProxy(uniqueName);
	}

	/**
	 * Registers the necessary properties to bind a SCA service
	 * 
	 * @param properties
	 *            A map containing the necessary data to bind the service
	 * @param clazz
	 *            The service interface
	 * @return null
	 */
	public static <T> T bindService(Map<String, Object> properties,
			Class<T> clazz) {
		/*
		 * At runtime, inside Pascani, it is not important to have the actual
		 * instance.
		 */
		return null;
	}
	
	private static ProbeProxy createProbeProxy(String routingKey) {
		try {
			return new ProbeProxy(routingKey);
		} catch (Exception e) {
			// TODO: log the exception
			e.printStackTrace();
		}
		return null;
	}

}
