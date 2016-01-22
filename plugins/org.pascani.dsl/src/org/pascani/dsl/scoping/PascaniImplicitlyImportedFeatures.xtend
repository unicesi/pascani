package org.pascani.dsl.scoping

import org.eclipse.xtext.xbase.scoping.batch.ImplicitlyImportedFeatures
import org.pascani.dsl.lib.sca.ComponentExtensions
import org.pascani.dsl.lib.sca.FrascatiUtils
import org.pascani.dsl.lib.sca.PascaniUtils
import org.pascani.dsl.lib.sca.FluentFPath

class PascaniImplicitlyImportedFeatures extends ImplicitlyImportedFeatures {
	
	override protected getStaticImportClasses() {
		return (super.getStaticImportClasses() + #[PascaniUtils, FrascatiUtils]).toList
	}
	
	override protected getExtensionClasses() {
		return (super.getExtensionClasses() + #[ComponentExtensions, FluentFPath]).toList
	}
	
}