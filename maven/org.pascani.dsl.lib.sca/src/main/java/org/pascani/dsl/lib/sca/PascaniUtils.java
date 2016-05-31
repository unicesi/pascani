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
package org.pascani.dsl.lib.sca;

import static org.pascani.dsl.lib.sca.FrascatiUtils.eval;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptException;

import org.pascani.dsl.lib.Event;
import org.pascani.dsl.lib.PascaniRuntime;
import org.pascani.dsl.lib.Probe;
import org.pascani.dsl.lib.events.ExceptionEvent;
import org.pascani.dsl.lib.events.InvokeEvent;
import org.pascani.dsl.lib.events.ReturnEvent;
import org.pascani.dsl.lib.events.TimeLapseEvent;
import org.pascani.dsl.lib.infrastructure.ProbeProxy;
import org.pascani.dsl.lib.util.Exceptions;

import com.google.common.base.MoreObjects;
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
	 * Updates the given probe property, in the specified FraSCAti runtime.
	 * 
	 * @param target
	 *            A FPath selector
	 * @param routingKey
	 *            The probe's routing key
	 * @param propertyName
	 *            The name of the property to update
	 * @param propertyValue
	 *            The new property value
	 * @param bindingUri
	 *            The URI where the FraSCAti runtime is running
	 * @throws IOException
	 *             If there is a problem loading the Pascani FScript procedures
	 *             from the resources
	 * @throws ScriptException
	 *             If there is a problem executing any of the scripts
	 * @see FrascatiUtils#DEFAULT_BINDING_URI
	 */
	public static void setProbeProperty(String target, String routingKey, 
			String propertyName, String propertyValue, URI bindingUri) 
					throws IOException, ScriptException {
		registerPascaniScripts(bindingUri);
		String[] data = target.split("/");
		String parent = data[0] + "/" + data[1];
		String keyValue = propertyName + "=" + propertyValue;
		eval("pascani-probe-set(" + parent + ", \"" + routingKey + "\", \"" 
				+ keyValue + "\")", bindingUri);
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
		for (String key: PascaniRuntime.getEnvironment().keySet()) {
			setProbeProperty(target, routingKey, "pascani." + key, 
					PascaniRuntime.getEnvironment().get(key), bindingUri);
		}
		setProbeProperty(target, routingKey, "routingkey", routingKey, bindingUri);
		setProbeProperty(target, routingKey, "probe", Boolean.TRUE.toString(), bindingUri);
		if (activateProducer)
			setProbeProperty(target, routingKey, "producer", 
					Boolean.TRUE.toString(), bindingUri);
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
		String intentName = intentName(eventType);
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
		// A random name is necessary because FraSCAti 1.4 cannot stop a top
		// level component. This is necessary to remove child components. See
		// comments in pascani.fscript.
		String randomName = "\"removed-intent-" + System.nanoTime() + "\"";
		eval("pascani-remove-intent(" + parent + ", " + target + ", \""
				+ routingKey + "\", " + randomName + ");", bindingUri);
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
	 * Selects the SCA intent corresponding to the given {@link Event} type
	 * 
	 * @param eventType
	 *            The type of Event
	 * @return the name of the corresponding Pascani intent
	 */
	public static String intentName(Class<? extends Event<?>> eventType) {
		String intentName = null;
		if (eventType.equals(TimeLapseEvent.class))
			intentName = "pascani-performance-intent";
		else if (eventType.equals(ExceptionEvent.class))
			intentName = "pascani-exception-intent";
		else if (eventType.equals(InvokeEvent.class))
			intentName = "pascani-invoke-intent";
		else if (eventType.equals(ReturnEvent.class))
			intentName = "pascani-return-intent";
		return intentName;
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
			ClassLoader loader = MoreObjects.firstNonNull(
					Thread.currentThread().getContextClassLoader(),
					PascaniUtils.class.getClassLoader());
			URL url = loader.getResource("pascani.fscript");
			if (url == null)
				url = ClassLoader.getSystemResource("pascani.fscript");
			String fscript = "";
			if (url != null) {
				fscript = Resources.toString(url, Charset.defaultCharset());
				System.out.println("------ pascani.fscript LOADED FROM RESOURCES ------");
			} else {
				System.out.println("------ pascani.fscript NOT FOUND. USING BACKUP ------");
				fscript = "" +
						"function pascani-element-exists(selector) {\n" +
						"	return size($selector) > 0;\n" +
						"}\n" +
						"action pascani-add-intent(parent, target, intentName, routingKey) {\n" +
						"	-- 1. Create a new instance of the intent composite\n" +
						"	clone = sca-new($intentName);\n" +
						"	-- 2. Add the intent instance as a child of the target's parent\n" +
						"	add-scachild($parent, $clone);\n" +
						"	intent = $parent/scachild::$intentName;\n" +
						"	-- 3. Change the name of the intent component to be the routing key\n" +
						"	set-name($intent, $routingKey);\n" +
						"	intent = $parent/scachild::$routingKey;\n" +
						"	-- 4. Sets the routing key\n" +
						"	property = $intent/scachild::probe/scaproperty::property;\n" +
						"	set-value($property, concat(\"routingkey=\", $routingKey));\n" +
						"	-- 5. Add the REST binding to the Resumable interface, and then add the SCA intent\n" +
						"	add-scaintent($target, $intent);\n" +
						"	-- 6. Wire the event handler service\n" +
						"	service = $intent/scachild::probe/scaservice::handler;\n" +
						"	reference = $intent/scachild::primitiveIntentHandler/scareference::handler;\n" +
						"	add-scawire($reference, $service);\n" +
						"	-- 7. Clean things up\n" +
						"	sca-remove($intentName); \n" +
						"	set-state($intent, \"STARTED\");\n" +
						"	set-state($parent, \"STARTED\");\n" +
						"}\n" +
						"action pascani-remove-intent(parent, target, routingKey, randomName) {\n" +
						"	-- 1. Remove the SCA intent\n" +
						"	intent = $parent/scachild::$routingKey;\n" +
						"	remove-scaintent($target, $intent);\n" +
						"	-- 2. Remove the intent component from the target's parent\n" +
						"	set-state($intent, \"STOPPED\");\n" +
						"	-- FraSCAti freezes when stopping a top level component.\n" +
						"	-- This is a requirement for removing child components though.\n" +
						"	-- set-state($parent, \"STOPPED\");\n" +
						"	-- remove-scachild($parent, $intent);\n" +
						"	-- set-state($parent, \"STARTED\");\n" +
						"	-- Instead of that, rename the component to a random name and shutdown probe & producer\n" +
						"	pascani-probe-set($parent, $routingKey, \"shutdown=both\");" +
						"	set-name($intent, $randomName);\n" +
						"}\n" +
						"action pascani-probe-set(parent, routingKey, key_value) {\n" +
						"	intent = $parent/scachild::$routingKey;\n" +
						"	property = $intent/scachild::probe/scaproperty::property;\n" +
						"	set-value($property, $key_value);\n" +
						"}";
			}
			List<String> scripts = FrascatiUtils.registerScript(fscript, bindingUri);
			registeredScripts.put(bindingUri, scripts.size() > 0);
		}
	}

}
