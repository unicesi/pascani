package org.pascani.scoping

import org.eclipse.xtext.xbase.scoping.batch.ImplicitlyImportedFeatures
import pascani.lang.util.ComponentManager
import pascani.lang.util.ServiceManager

class PascaniImplicitlyImportedFeatures extends ImplicitlyImportedFeatures {
	
	override protected getStaticImportClasses() {
		return (super.getStaticImportClasses() + #[ServiceManager]).toList
	}
	
	override protected getExtensionClasses() {
		return (super.getExtensionClasses() + #[ComponentManager]).toList
	}
	
}