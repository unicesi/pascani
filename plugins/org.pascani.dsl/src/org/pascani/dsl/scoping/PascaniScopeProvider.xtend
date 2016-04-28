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

import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.scoping.Scopes
import org.pascani.dsl.pascani.Event
import org.pascani.dsl.pascani.ImportEventDeclaration
import org.pascani.dsl.pascani.PascaniPackage

/**
 * This class contains custom scoping description.
 * 
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#scoping
 * on how and when to use it.
 */
class PascaniScopeProvider extends AbstractPascaniScopeProvider {

	override getScope(EObject context, EReference reference) {
		if (context instanceof ImportEventDeclaration) {
			if (reference == PascaniPackage.Literals.IMPORT_EVENT_DECLARATION__EVENTS && context.monitor != null) {
				val candidates = EcoreUtil2.getAllContentsOfType(context.monitor, Event);
				return Scopes.scopeFor(candidates)
			}
		}
		return super.getScope(context, reference);
	}

}
