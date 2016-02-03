/*
 * Copyright © 2015 Universidad Icesi
 * 
 * This file is part of the Pascani DSL.
 * 
 * The Pascani DSL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * The Pascani DSL is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with The Pascani DSL. If not, see <http://www.gnu.org/licenses/>.
 */
package org.pascani.dsl.generator

import com.google.inject.Inject
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.Date
import java.util.Properties
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.IFileSystemAccess
import org.eclipse.xtext.generator.IGenerator
import org.eclipse.xtext.naming.IQualifiedNameProvider
import org.ow2.scesame.qoscare.core.scaspec.SCAAttribute
import org.ow2.scesame.qoscare.core.scaspec.SCABinding
import org.ow2.scesame.qoscare.core.scaspec.SCAComponent
import org.ow2.scesame.qoscare.core.scaspec.SCAInterface
import org.ow2.scesame.qoscare.core.scaspec.SCAPort
import org.pascani.dsl.lib.compiler.templates.ScaCompositeTemplates
import org.pascani.dsl.lib.util.MonitorEventsService
import org.pascani.dsl.lib.util.Resumable
import org.pascani.dsl.outputconfiguration.PascaniOutputConfigurationProvider
import org.pascani.dsl.pascani.Model
import org.pascani.dsl.pascani.Monitor
import org.pascani.dsl.pascani.Namespace
import org.pascani.dsl.pascani.TypeDeclaration

/**
 * @author Miguel Jiménez - Initial contribution and API
 */
class PascaniGenerator implements IGenerator {

	@Inject extension IQualifiedNameProvider

	/**
	 * The path to the file containing port assignments (FQN=initialPort)
	 */
	static val PORTS_FILE = "ports.properties"
	
	/**
	 * The port from which service ports are assigned
	 */
	static val DEFAULT_STARTING_PORT = 9000
	
	/**
	 * The table containing past and new port assignments
	 */
	var ports = new Properties()

	override doGenerate(Resource resource, IFileSystemAccess fsa) {
		// FIXME: This is Eclipse-dependent (dependency: org.eclipse.core.resources)
		val currentProject = ResourcesPlugin.workspace.root.getProject(resource.URI.segmentsList.get(1))
		val filePath = currentProject.getFile("sca-gen/" + PORTS_FILE).locationURI.rawPath
		readPorts(filePath, fsa)
		
		resource.allContents.forEach [ element |
			switch (element) {
				Model: {
					val declaration = element.typeDeclaration
					var port = getPort(declaration)
					switch (declaration) {
						Monitor: {
							val component = new SCAComponent(declaration.name)
							val child = new SCAComponent("monitor", declaration.fullyQualifiedName.segments.join("."))

							// Resumable service
							val resumable = new SCAPort("resumable")
							resumable.implement = new SCAInterface("resumable", Resumable.canonicalName)
							resumable.bindings +=
								new SCABinding(SCABinding.Kind.REST,
									newArrayList(new SCAAttribute("uri", "http://localhost:" + port++)))

							// Events service
							val events = new SCAPort("events")
							events.implement = new SCAInterface("events", MonitorEventsService.canonicalName)
							events.bindings +=
								new SCABinding(SCABinding.Kind.REST,
									newArrayList(new SCAAttribute("uri", "http://localhost:" + port++)))

							child.services += #[resumable, events]
							component.children += child

							val contents = ScaCompositeTemplates.parseComponent(component)
							fsa.generateFile(declaration.fullyQualifiedName.segments.join(File.separator) +
								".composite", PascaniOutputConfigurationProvider::SCA_OUTPUT, contents)
						}
						Namespace: {
							val component = new SCAComponent(declaration.name)
							val child = new SCAComponent("namespace", declaration.fullyQualifiedName.segments.join("."))

							// Resumable service
							val resumable = new SCAPort("resumable")
							resumable.implement = new SCAInterface("resumable", Resumable.canonicalName)
							resumable.bindings +=
								new SCABinding(SCABinding.Kind.REST,
									newArrayList(new SCAAttribute("uri", "http://localhost:" + port++)))

							child.services += resumable
							component.children += child

							val contents = ScaCompositeTemplates.parseComponent(component)
							fsa.generateFile(declaration.fullyQualifiedName.segments.join(File.separator) +
								".composite", PascaniOutputConfigurationProvider::SCA_OUTPUT, contents)
						}
					}
				}
			}
		]
		savePorts(fsa)
	}

	def void readPorts(String filePath, IFileSystemAccess fsa) {
		val file = new File(filePath)
		ports = new Properties
		if (!file.exists)
			savePorts(fsa)
		try {
			ports.load(new FileInputStream(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	def void savePorts(IFileSystemAccess fsa) {
		val sb = new StringBuilder();
		sb.append("# This file was generated by Pascani")
		sb.append("\n");
		sb.append("# " + new Date().toString());
		sb.append("\n");
		for (key : ports.keySet) {
			val value = ports.get(key) as String
			sb.append(key)
			sb.append("=")
			sb.append(value)
			sb.append("\n")
		}
		fsa.generateFile(PORTS_FILE, PascaniOutputConfigurationProvider::SCA_OUTPUT, sb.toString)
	}

	def getPort(TypeDeclaration typeDeclaration) {
		var port = DEFAULT_STARTING_PORT
		var newPort = true
		if (!ports.keySet.empty) {
			val value = ports.get(typeDeclaration.fullyQualifiedName.toString)
			if (value != null) {
				port = Integer.parseInt(value.toString)
				newPort = false
			} else {
				val last = ports.values.map[v|Integer.parseInt(v + "")].max
				port = last + 1000;
			}
		}
		if(newPort)
			ports.put(typeDeclaration.fullyQualifiedName.toString, port + "")
		return port
	}

}
