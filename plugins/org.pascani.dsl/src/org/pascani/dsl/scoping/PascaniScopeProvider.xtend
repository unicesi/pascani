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

import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference
import org.eclipse.xtext.EcoreUtil2
import org.pascani.dsl.pascani.Handler
import org.pascani.dsl.pascani.Monitor
import org.pascani.dsl.scoping.AbstractPascaniScopeProvider

/**
 * This class contains custom scoping description.
 * 
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#scoping
 * on how and when to use it.
 */
class PascaniScopeProvider extends AbstractPascaniScopeProvider {

	def scope_Monitor_usings(Monitor context, EReference reference) {
		val EObject rootElement = EcoreUtil2.getRootContainer(context);
		return rootElement.getScope(reference)
	}

	def scope_Handler_body(Handler context, EReference reference) {
		val EObject rootElement = EcoreUtil2.getRootContainer(context);
		return rootElement.getScope(reference)
	}

}
