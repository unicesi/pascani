package org.pascani.dsl.generator

import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.IFileSystemAccess
import org.eclipse.xtext.generator.IGenerator
import org.pascani.dsl.pascani.Model
import org.pascani.dsl.pascani.Monitor
import org.pascani.dsl.pascani.Namespace
import org.ow2.scesame.qoscare.core.scaspec.SCAComponent
import org.eclipse.xtext.naming.IQualifiedNameProvider
import com.google.inject.Inject
import org.ow2.scesame.qoscare.core.scaspec.SCAPort
import org.ow2.scesame.qoscare.core.scaspec.SCAInterface
import org.pascani.dsl.lib.util.Resumable
import org.pascani.dsl.outputconfiguration.PascaniOutputConfigurationProvider
import org.pascani.dsl.lib.compiler.templates.ScaCompositeTemplates
import java.io.File

class PascaniGenerator implements IGenerator {
	
	@Inject extension IQualifiedNameProvider
	
	override doGenerate(Resource resource, IFileSystemAccess fsa) {
		resource.allContents.forEach [element|
			switch (element) {
				Model: {
					val declaration = element.typeDeclaration
					switch (declaration) {
						Monitor: {
							val component = new SCAComponent(declaration.name)
							val child = new SCAComponent("monitor", declaration.fullyQualifiedName.lastSegment)
							
							// Resumable service
							val resumable = new SCAPort("resumable")
							resumable.implement = new SCAInterface("resumable", Resumable.canonicalName)
							
							// Events service
							val events = new SCAPort("events")
							events.implement = new SCAInterface("events", declaration.fullyQualifiedName + "EventsService")
							
							child.services += #[resumable, events]
							component.children += child

							val contents = ScaCompositeTemplates.parseComponent(component)
							fsa.generateFile(declaration.fullyQualifiedName.segments.join(File.separator) + ".composite",
								PascaniOutputConfigurationProvider::SCA_OUTPUT, contents)
						}
						
						Namespace: {
							
						}
					}
				}
			}
		]
	}
	
}