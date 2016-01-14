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

class PascaniImportedNamespaceAwareLocalScopeProvider extends XImportSectionNamespaceScopeProvider {

	public static final QualifiedName LIB = QualifiedName.create("org", "pascani", "dsl", "lib");
	public static final QualifiedName LIB_EVENTS = QualifiedName.create("org", "pascani", "dsl", "lib", "events");
	public static final QualifiedName LIB_UTIL = QualifiedName.create("org", "pascani", "dsl", "lib", "util");
	public static final QualifiedName LIB_UTIL_EVENTS = QualifiedName.create("org", "pascani", "dsl", "lib", "util", "events");
	public static final QualifiedName LIB_INFRASTRUCTURE = QualifiedName.create("org", "pascani", "dsl", "lib", "infrastructure");

	override List<ImportNormalizer> getImplicitImports(boolean ignoreCase) {
		val imports = newArrayList(
			doCreateImportNormalizer(LIB, true, false),
			doCreateImportNormalizer(LIB_EVENTS, true, false),
			doCreateImportNormalizer(LIB_UTIL, true, false),
			doCreateImportNormalizer(LIB_UTIL_EVENTS, true, false),
			doCreateImportNormalizer(LIB_INFRASTRUCTURE, true, false)
		)
		imports.addAll(super.getImplicitImports(ignoreCase))
		return imports
	}

}
