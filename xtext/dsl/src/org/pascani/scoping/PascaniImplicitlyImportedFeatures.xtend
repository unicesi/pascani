package org.pascani.scoping

import org.eclipse.xtext.xbase.scoping.batch.ImplicitlyImportedFeatures
import pascani.lang.util.ComponentExtensions
import pascani.lang.util.ServiceManager
import pascani.lang.util.FrascatiUtils

class PascaniImplicitlyImportedFeatures extends ImplicitlyImportedFeatures {
	
	override protected getStaticImportClasses() {
		return (super.getStaticImportClasses() + #[ServiceManager, FrascatiUtils]).toList
	}
	
	override protected getExtensionClasses() {
		return (super.getExtensionClasses() + #[ComponentExtensions]).toList
	}
	
}