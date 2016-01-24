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
package org.pascani.dsl.lib.sca;

import static org.pascani.dsl.lib.sca.FrascatiUtils.eval;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptException;

import org.apache.commons.lang.NotImplementedException;
import org.pascani.dsl.lib.Event;
import org.pascani.dsl.lib.Probe;
import org.pascani.dsl.lib.events.ExceptionEvent;
import org.pascani.dsl.lib.events.InvokeEvent;
import org.pascani.dsl.lib.events.ReturnEvent;
import org.pascani.dsl.lib.events.TimeLapseEvent;
import org.pascani.dsl.lib.infrastructure.AbstractProducer;
import org.pascani.dsl.lib.infrastructure.ProbeProxy;
import org.pascani.dsl.lib.util.Exceptions;

import com.google.common.io.Resources;

/**
 * @author Miguel Jiménez - Initial contribution and API
 */
public class PascaniUtils {

	private static Map<URI, Boolean> registeredScripts = new HashMap<URI, Boolean>();
	
	/**
	 * Introduces a new SCA intent in the specified FraSCAti runtime, bound to
	 * the specified target.
	 * 
	 * @param target
	 *            A FPath selector
	 * @param routingKey
	 *            A unique name (within the
	 *            {@code org.pascani.dsl.lib.PascaniRuntime.Context#PROBE}
	 *            context) for the new probe
	 * @param intentName
	 *            The name of the Pascani intent
	 * @param bindingUri
	 *            The URI where the FraSCAti runtime is running
	 * @throws IOException
	 *             If there is a problem loading the Pascani FScript procedures
	 *             from the resources
	 * @throws ScriptException
	 *             If there is a problem executing any of the scripts
	 * @see FrascatiUtils#DEFAULT_BINDING_URI
	 */
	public static void newIntent(String target, String routingKey,
			String intentName, URI bindingUri) throws IOException, ScriptException {
		registerPascaniScripts(bindingUri);
		String[] data = target.split("/");
		String parent = data[0] + "/" + data[1];
		StringBuilder params = new StringBuilder();
		params.append(parent + ", ");
		params.append(target + ", ");
		params.append("\"" + intentName + "\", ");
		params.append("\"" + routingKey + "\"");
		eval("pascani-add-intent(" + params.toString() + ");", bindingUri);
	}
	
	/**
	 * Resets an existing {@link Probe} bound to the given target and routing
	 * key, in the specified FraSCAti runtime.
	 * 
	 * @param target
	 *            A FPath selector
	 * @param routingKey
	 *            A unique name (within the
	 *            {@code org.pascani.dsl.lib.PascaniRuntime.Context#PROBE}
	 *            context) for the new probe
	 * @param bindingUri
	 *            The URI where the FraSCAti runtime is running
	 * @throws IOException
	 *             If there is a problem loading the Pascani FScript procedures
	 *             from the resources
	 * @throws ScriptException
	 *             If there is a problem executing any of the scripts
	 * @see FrascatiUtils#DEFAULT_BINDING_URI
	 */
	public static void resetProbe(String target, String routingKey,
			URI bindingUri) throws IOException, ScriptException {
		registerPascaniScripts(bindingUri);
		String[] data = target.split("/");
		String parent = data[0] + "/" + data[1];
		eval("pascani-reset-probe(" + parent + ", \"" + routingKey + "\")",
				bindingUri);
	}

	/**
	 * Resets an existing {@link AbstractProducer} bound to the given target and
	 * routing key, in the specified FraSCAti runtime.
	 * 
	 * @param target
	 *            A FPath selector
	 * @param routingKey
	 *            A unique name (within the
	 *            {@code org.pascani.dsl.lib.PascaniRuntime.Context#PROBE}
	 *            context) for the new probe
	 * @param bindingUri
	 *            The URI where the FraSCAti runtime is running
	 * @throws IOException
	 *             If there is a problem loading the Pascani FScript procedures
	 *             from the resources
	 * @throws ScriptException
	 *             If there is a problem executing any of the scripts
	 * @see FrascatiUtils#DEFAULT_BINDING_URI
	 */
	public static void resetProducer(String target, String routingKey,
			URI bindingUri) throws IOException, ScriptException {
		registerPascaniScripts(bindingUri);
		String[] data = target.split("/");
		String parent = data[0] + "/" + data[1];
		eval("pascani-reset-producer(" + parent + ", \"" + routingKey + "\")",
				bindingUri);
	}

	/**
	 * Introduces a new SCA intent in the specified FraSCAti runtime, bound to
	 * the specified target and returns a proxy pointing to the corresponding
	 * {@link Probe}.
	 * 
	 * @param target
	 *            A FPath selector
	 * @param routingKey
	 *            A unique name (within the
	 *            {@code org.pascani.dsl.lib.PascaniRuntime.Context#PROBE}
	 *            context) for the new probe
	 * @param intentName
	 *            The name of the Pascani intent
	 * @param activateProducer
	 *            Whether or not the event producer must be activated
	 * @param bindingUri
	 *            The URI where the FraSCAti runtime is running
	 * @return a {@link ProbeProxy} instance pointing to the introduced
	 *         {@link Probe} or {@code null} if something bad happens while
	 *         evaluating the FScript commands.
	 * @throws IOException
	 *             If there is a problem loading the Pascani FScript procedures
	 *             from the resources
	 * @throws ScriptException
	 *             If there is a problem executing any of the scripts
	 */
	public static ProbeProxy newProbe(String target, String routingKey,
			String intentName, boolean activateProducer, URI bindingUri)
					throws IOException, ScriptException {
		newIntent(target, routingKey, intentName, bindingUri);
		resetProbe(target, routingKey, bindingUri);
		if (activateProducer)
			resetProducer(target, routingKey, bindingUri);
		return Exceptions.sneakyInitializer(ProbeProxy.class, routingKey);
	}
	
	/**
	 * Introduces a new SCA intent in the default FraSCAti runtime, bound to the
	 * specified target and returns a proxy pointing to the corresponding
	 * {@link Probe}. The probe created manages events of type
	 * {@link TimeLapseEvent}, {@link InvokeEvent}, {@link ExceptionEvent},
	 * {@link ReturnEvent}.
	 * 
	 * @param target
	 *            A FPath selector
	 * @param routingKey
	 *            A unique name (within the
	 *            {@code org.pascani.dsl.lib.PascaniRuntime.Context#PROBE}
	 *            context) for the new probe
	 * @param activateProducer
	 *            Whether or not the event producer must be activated
	 * @return a {@link ProbeProxy} instance pointing to the introduced
	 *         {@link Probe} or {@code null} if something bad happens while
	 *         evaluating the FScript commands.
	 * @throws IOException
	 *             If there is a problem loading the Pascani FScript procedures
	 *             from the resources
	 * @throws ScriptException
	 *             If there is a problem executing any of the scripts
	 */
	public static ProbeProxy newProbe(String target, String routingKey,
			boolean activateProducer) throws IOException, ScriptException {
		return newProbe(target, routingKey, "pascani-all-events-intent",
				activateProducer, FrascatiUtils.DEFAULT_BINDING_URI);
	}

	/**
	 * Introduces a new SCA intent in the specified FraSCAti runtime, bound to
	 * the specified target and returns a proxy pointing to the corresponding
	 * {@link Probe}. The probe created manages events of the specified type.
	 * 
	 * @param target
	 *            A FPath selector
	 * @param routingKey
	 *            A unique name (within the
	 *            {@code org.pascani.dsl.lib.PascaniRuntime.Context#PROBE}
	 *            context) for the new probe
	 * @param eventType
	 *            The type of events managed by the {@link Probe}
	 * @param activateProducer
	 *            Whether or not the event producer must be activated
	 * @param bindingUri
	 *            The URI where the FraSCAti runtime is running
	 * @return a {@link ProbeProxy} instance pointing to the introduced
	 *         {@link Probe} or {@code null} if something bad happens while
	 *         evaluating the FScript commands.
	 * @throws IOException
	 *             If there is a problem loading the Pascani FScript procedures
	 *             from the resources
	 * @throws ScriptException
	 *             If there is a problem executing any of the scripts
	 */
	public static ProbeProxy newProbe(String target, String routingKey,
			Class<? extends Event<?>> eventType, boolean activateProducer,
			URI bindingUri) throws IOException, ScriptException {
		String intentName = "";
		if (eventType.equals(TimeLapseEvent.class))
			intentName = "pascani-performance-intent";
		else if (eventType.equals(ExceptionEvent.class))
			intentName = "pascani-exception-intent";
		else if (eventType.equals(InvokeEvent.class))
			intentName = "pascani-invoke-intent";
		else if (eventType.equals(ReturnEvent.class))
			intentName = "pascani-return-intent";
		return newProbe(target, routingKey, intentName, activateProducer, bindingUri);
	}
	
	/**
	 * Introduces a new SCA intent in the default FraSCAti runtime, bound to the
	 * specified target and returns a proxy pointing to the corresponding
	 * {@link Probe}. The probe created manages events of the specified type.
	 * 
	 * @param target
	 *            A FPath selector
	 * @param routingKey
	 *            A unique name (within the
	 *            {@code org.pascani.dsl.lib.PascaniRuntime.Context#PROBE}
	 *            context) for the new probe
	 * @param eventType
	 *            The type of events managed by the {@link Probe}
	 * @param activateProducer
	 *            Whether or not the event producer must be activated
	 * @return a {@link ProbeProxy} instance pointing to the introduced
	 *         {@link Probe} or {@code null} if something bad happens while
	 *         evaluating the FScript commands.
	 * @throws IOException
	 *             If there is a problem loading the Pascani FScript procedures
	 *             from the resources
	 * @throws ScriptException
	 *             If there is a problem executing any of the scripts
	 */
	public static ProbeProxy newProbe(String target, String routingKey,
			Class<? extends Event<?>> eventType, boolean activateProducer)
					throws IOException, ScriptException {
		return newProbe(target, routingKey, eventType, activateProducer,
				FrascatiUtils.DEFAULT_BINDING_URI);
	}
	
	/**
	 * Removes the SCA intent containing the specified probe, in the specified
	 * FraSCAti runtime.
	 * 
	 * @param target
	 *            A FPath selector
	 * @param routingKey
	 *            The routing key of the probe to remove
	 * @param bindingUri
	 *            The URI where the FraSCAti runtime is running
	 * @throws IOException
	 *             If there is a problem loading the Pascani FScript procedures
	 *             from the resources
	 * @throws ScriptException
	 *             If there is a problem executing any of the scripts
	 */
	public static void removeProbe(String target, String routingKey,
			URI bindingUri) throws IOException, ScriptException {
		registerPascaniScripts(bindingUri);
		String[] data = target.split("/");
		String parent = data[0] + "/" + data[1];
		eval("pascani-remove-intent(" + parent + ", " + target + ", \""
				+ routingKey + "\");", bindingUri);
	}

	/**
	 * Removes the SCA intent containing the specified probe, in the default
	 * FraSCAti runtime.
	 * 
	 * @param target
	 *            A FPath selector
	 * @param routingKey
	 *            The routing key of the probe to remove
	 * @throws IOException
	 *             If there is a problem loading the Pascani FScript procedures
	 *             from the resources
	 * @throws ScriptException
	 *             If there is a problem executing any of the scripts
	 */
	public static void removeProbe(String target, String routingKey)
			throws IOException, ScriptException {
		removeProbe(target, routingKey, FrascatiUtils.DEFAULT_BINDING_URI);
	}
	
	/**
	 * Registers the Pascani FScript procedures in the specified FraSCAti
	 * runtime
	 * 
	 * @param bindingUri
	 *            The URI where the FraSCAti runtime is running
	 * @throws IOException
	 *             If there is a problem loading the Pascani FScript procedures
	 *             from the resources
	 * @throws ScriptException
	 *             If there is a problem executing any of the scripts
	 */
	private static void registerPascaniScripts(URI bindingUri)
			throws IOException, ScriptException {
		Boolean registered = registeredScripts.get(bindingUri);
		if (registered == null || !registered) {
			File fscript = new File(Resources.getResource("pascani.fscript").getFile());
			List<String> scripts = FrascatiUtils.registerScript(fscript, bindingUri);
			registeredScripts.put(bindingUri, scripts.size() > 0);
		}
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
		throw new NotImplementedException();
	}

}
