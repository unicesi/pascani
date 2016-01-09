package org.pascani.scoping

import org.eclipse.xtext.xbase.scoping.batch.ImplicitlyImportedFeatures
import pascani.lang.util.ComponentExtensions
import pascani.lang.util.FrascatiUtils
import pascani.lang.util.dsl.PascaniUtils

class PascaniImplicitlyImportedFeatures extends ImplicitlyImportedFeatures {
	
	override protected getStaticImportClasses() {
		return (super.getStaticImportClasses() + #[PascaniUtils, FrascatiUtils]).toList
	}
	
	override protected getExtensionClasses() {
		return (super.getExtensionClasses() + #[ComponentExtensions]).toList
	}
	
}