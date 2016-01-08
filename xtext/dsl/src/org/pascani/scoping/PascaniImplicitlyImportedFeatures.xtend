package org.pascani.scoping

import org.eclipse.xtext.xbase.scoping.batch.ImplicitlyImportedFeatures
import pascani.lang.util.ComponentManager
import pascani.lang.util.ServiceManager

class PascaniImplicitlyImportedFeatures extends ImplicitlyImportedFeatures {
	
	override protected getStaticImportClasses() {
		val staticImportClasses = super.getStaticImportClasses()
		staticImportClasses.addAll(newArrayList(
			ServiceManager
		))
		return staticImportClasses
	}
	
	override protected getExtensionClasses() {
		val extensionClasses = super.getExtensionClasses()
		extensionClasses.addAll(newArrayList(
			ComponentManager
		))
		return extensionClasses
	}	
	
}