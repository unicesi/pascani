/*
 * Copyright © 2015 Universidad Icesi
 * 
 * This file is part of the Pascani DSL.
 * 
 * The Pascani DSL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * The Pascani DSL is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with The Pascani DSL. If not, see <http://www.gnu.org/licenses/>.
 */
package org.pascani.dsl.scoping

import java.util.List
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.scoping.impl.ImportNormalizer
import org.eclipse.xtext.xbase.scoping.XImportSectionNamespaceScopeProvider

/**
 * @author Miguel Jiménez - Initial contribution and API
 */
class PascaniImportedNamespaceAwareLocalScopeProvider extends XImportSectionNamespaceScopeProvider {

	public static final QualifiedName LIB = QualifiedName.create("org", "pascani", "dsl", "lib");

	override List<ImportNormalizer> getImplicitImports(boolean ignoreCase) {
		return (
			super.getImplicitImports(ignoreCase) + #[doCreateImportNormalizer(LIB, true, false)]
		).toList
	}

}
