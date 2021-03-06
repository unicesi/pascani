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
package org.pascani.dsl.scoping

import org.eclipse.xtext.xbase.scoping.batch.ImplicitlyImportedFeatures
import org.pascani.dsl.lib.sca.ComponentExtensions
import org.pascani.dsl.lib.sca.FrascatiUtils
import org.pascani.dsl.lib.sca.PascaniUtils
import org.pascani.dsl.lib.sca.FluentFPath
import org.pascani.dsl.lib.util.LanguageUtils

/**
 * @author Miguel Jiménez - Initial contribution and API
 */
class PascaniImplicitlyImportedFeatures extends ImplicitlyImportedFeatures {

	override protected getStaticImportClasses() {
		return (super.getStaticImportClasses() + #[PascaniUtils, FrascatiUtils, LanguageUtils]).toList
	}

	override protected getExtensionClasses() {
		return (super.getExtensionClasses() + #[ComponentExtensions, FluentFPath]).toList
	}

}