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
package org.pascani.dsl.generator

import com.google.inject.Inject
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.Date
import java.util.List
import java.util.Properties
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.IFileSystemAccess
import org.eclipse.xtext.generator.IGenerator
import org.eclipse.xtext.naming.IQualifiedNameProvider
import org.eclipse.xtext.resource.IContainer
import org.eclipse.xtext.resource.IEObjectDescription
import org.eclipse.xtext.resource.impl.ResourceDescriptionsProvider
import org.ow2.scesame.qoscare.core.scaspec.SCAAttribute
import org.ow2.scesame.qoscare.core.scaspec.SCABinding
import org.ow2.scesame.qoscare.core.scaspec.SCAComponent
import org.ow2.scesame.qoscare.core.scaspec.SCAInterface
import org.ow2.scesame.qoscare.core.scaspec.SCAPort
import org.pascani.dsl.lib.compiler.templates.DeploymentTemplates
import org.pascani.dsl.lib.compiler.templates.ScaCompositeTemplates
import org.pascani.dsl.lib.util.MonitorEventsService
import org.pascani.dsl.lib.util.Resumable
import org.pascani.dsl.outputconfiguration.PascaniOutputConfigurationProvider
import org.pascani.dsl.pascani.Model
import org.pascani.dsl.pascani.Monitor
import org.pascani.dsl.pascani.Namespace
import org.pascani.dsl.pascani.PascaniPackage
import org.pascani.dsl.pascani.TypeDeclaration

/**
 * @author Miguel Jiménez - Initial contribution and API
 */
class PascaniGenerator implements IGenerator {
	
	@Inject ResourceDescriptionsProvider resourceDescriptionsProvider
	
	@Inject IContainer.Manager containerManager

	@Inject extension IQualifiedNameProvider

	/**
	 * The path to the file containing port assignments (FQN=initialPort)
	 */
	static val PORTS_FILE = "ports.properties"
	
	/**
	 * The port from which service ports are assigned
	 */
	static val DEFAULT_STARTING_PORT = 10000
	
	/**
	 * The table containing past and new port assignments
	 */
	var ports = new Properties()

	override doGenerate(Resource resource, IFileSystemAccess fsa) {
		// FIXME: This is Eclipse-dependent (dependency: org.eclipse.core.resources)
		val currentProject = ResourcesPlugin.workspace.root.getProject(resource.URI.segmentsList.get(1))
		val filePath = currentProject.getFile("pascani-gen/" + PORTS_FILE).locationURI.rawPath
		
		// Generate composite files
		readPorts(filePath, fsa)
		resource.allContents.forEach [ element |
			switch (element) {
				Model: {
					val declaration = element.typeDeclaration
					var port = getPort(declaration)
					switch (declaration) {
						Monitor: declaration.infer(port, fsa)
						Namespace: declaration.infer(port, fsa)
					}
				}
			}
		]
		savePorts(fsa)
		
		// Generate deployment descriptors
		val projectPath = currentProject.locationURI.toURL.file
		val namespaces = getEObjectDescriptions(resource, PascaniPackage.eINSTANCE.namespace).map [ d |
			d.getEObject(resource) as TypeDeclaration
		].filter [ n |
			n.eContainer instanceof Model
		]
		val monitors = getEObjectDescriptions(resource, PascaniPackage.eINSTANCE.monitor).map [ d |
			d.getEObject(resource) as TypeDeclaration
		]
		(namespaces + monitors).toList.generateDeploymentArtifacts(projectPath, currentProject.name, fsa)
	}
	
	def void generateDeploymentArtifacts(List<TypeDeclaration> decls, String projectPath, String projectName,
		IFileSystemAccess fsa) {
		var packageName = "deployment"
		val comps = decls.toMap[m|m.name].mapValues[m|m.port]
		// Contents
		val deployment = DeploymentTemplates.deployment("^" + packageName, #["Execution"], "Deployment")
		val prerequisites = DeploymentTemplates.prerequisites("^" + packageName, projectPath, projectName)
		val subsystems = DeploymentTemplates.subsystems("^" + packageName, "Execution", comps)
		// Generate files
		fsa.generateFile("Deployment".prepareFileName(packageName), 
			PascaniOutputConfigurationProvider::PASCANI_OUTPUT, deployment)
		fsa.generateFile("Prerequisites".prepareFileName(packageName),
			PascaniOutputConfigurationProvider::PASCANI_OUTPUT, prerequisites)
		fsa.generateFile("Execution".prepareFileName(packageName),
			PascaniOutputConfigurationProvider::PASCANI_OUTPUT, subsystems)
	}
	
	def prepareFileName(String fileName, String packageName) {
		return (packageName + "." + fileName).replaceAll("\\.", File.separator) + ".amelia"
	}
	
	def void infer(Monitor declaration, int initialPort, IFileSystemAccess fsa) {
		var port = initialPort + 1
		val component = new SCAComponent(declaration.name)
		val runnablePromote = new SCAPort("r")
		runnablePromote.implement = new SCAInterface("r", Runnable.canonicalName)
		runnablePromote.wiredTo = "monitor/r"
		val child = new SCAComponent("monitor", declaration.fullyQualifiedName.toString)

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
		
		// Runnable service
		val runnable = new SCAPort("r")
		runnable.implement = new SCAInterface("r", Runnable.canonicalName)

		child.services += #[resumable, events, runnable]
		component.children += child
		component.services += #[runnablePromote]

		val contents = ScaCompositeTemplates.parseComponent(component)
		fsa.generateFile(declaration.fullyQualifiedName.toString(File.separator) + ".composite",
			PascaniOutputConfigurationProvider::PASCANI_OUTPUT, contents)
	}
	
	def void infer(Namespace declaration, int initialPort, IFileSystemAccess fsa) {
		var port = initialPort + 1
		val component = new SCAComponent(declaration.name)
		val runnablePromote = new SCAPort("r")
		runnablePromote.implement = new SCAInterface("r", Runnable.canonicalName)
		runnablePromote.wiredTo = "namespace/r"
		val child = new SCAComponent("namespace", declaration.fullyQualifiedName.toString + "Namespace")

		// Resumable service
		val resumable = new SCAPort("resumable")
		resumable.implement = new SCAInterface("resumable", Resumable.canonicalName)
		resumable.bindings +=
			new SCABinding(SCABinding.Kind.REST, 
				newArrayList(new SCAAttribute("uri", "http://localhost:" + port++)))
		
		// Runnable service
		val runnable = new SCAPort("r")
		runnable.implement = new SCAInterface("r", Runnable.canonicalName)

		child.services = #[resumable, runnable]
		component.children += child
		component.services += #[runnablePromote]

		val contents = ScaCompositeTemplates.parseComponent(component)
		fsa.generateFile(declaration.fullyQualifiedName.toString(File.separator) + ".composite",
			PascaniOutputConfigurationProvider::PASCANI_OUTPUT, contents)
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
		fsa.generateFile(PORTS_FILE, PascaniOutputConfigurationProvider::PASCANI_OUTPUT, sb.toString)
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
	
	def List<IEObjectDescription> getEObjectDescriptions(Resource resource, EClass eClass) {
	    val descriptions = newArrayList;
	    val resourceDescriptions = resourceDescriptionsProvider.getResourceDescriptions(resource);
	    val resourceDescription = resourceDescriptions.getResourceDescription(resource.getURI());
	    for (c : containerManager.getVisibleContainers(resourceDescription, resourceDescriptions)) {
	        for (ob : c.getExportedObjectsByType(eClass)) {
	            descriptions.add(ob);
	        }
	    }
	    return descriptions;
	}
	
	def EObject getEObject(IEObjectDescription description, Resource resource) {
		val resourceSet = resource.getResourceSet()
		return resourceSet.getEObject(description.getEObjectURI(), true)
	}

}
