package org.pascani.dsl.scoping

import org.eclipse.xtext.xbase.scoping.batch.ImplicitlyImportedFeatures
import org.pascani.dsl.lib.util.ComponentExtensions
import org.pascani.dsl.lib.util.FrascatiUtils
import org.pascani.dsl.lib.util.dsl.PascaniUtils

class PascaniImplicitlyImportedFeatures extends ImplicitlyImportedFeatures {
	
	override protected getStaticImportClasses() {
		return (super.getStaticImportClasses() + #[PascaniUtils, FrascatiUtils]).toList
	}
	
	override protected getExtensionClasses() {
		return (super.getExtensionClasses() + #[ComponentExtensions]).toList
	}
	
}