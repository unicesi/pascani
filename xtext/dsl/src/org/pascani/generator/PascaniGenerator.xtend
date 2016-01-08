package org.pascani.generator

import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.IFileSystemAccess
import org.eclipse.xtext.generator.IGenerator
import org.eclipse.xtext.xbase.XAbstractFeatureCall
import org.eclipse.xtext.xbase.XVariableDeclaration
import org.ow2.scesame.qoscare.core.scaspec.SCAComponent
import org.ow2.scesame.qoscare.core.scaspec.SCAPort
import org.pascani.pascani.Model
import pascani.lang.util.ComponentManager

class PascaniGenerator implements IGenerator {
	
	val newProbeId = ComponentManager.canonicalName + ".newProbe"
	val cNewProbe = newProbeId + "(" + SCAComponent.canonicalName + "," + Class.canonicalName + "[])"
	val pNewProbe = newProbeId + "(" + SCAPort.canonicalName + "," + Class.canonicalName + "[])"
	
	override doGenerate(Resource resource, IFileSystemAccess fsa) {
		resource.allContents.forEach [element|
			switch (element) {
				Model: {
					for(e : element.typeDeclaration.body.expressions) {
						switch (e) {
							XVariableDeclaration: {
								if (e.right instanceof XAbstractFeatureCall) {
									val featureCall = e.right as XAbstractFeatureCall
									val isProbeDeclaration = featureCall.feature.identifier.equals(cNewProbe) ||
										featureCall.feature.identifier.equals(pNewProbe)
									if (isProbeDeclaration) {
										println(featureCall + " --> " + featureCall.feature.identifier)
									}
								}
							}
						}
					}
				}
			}
		]
	}
	
}