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
package org.pascani.dsl.typesystem

import org.eclipse.xtext.xbase.XExpression
import org.eclipse.xtext.xbase.typesystem.computation.ITypeComputationState
import org.eclipse.xtext.xbase.typesystem.computation.XbaseTypeComputer
import org.pascani.dsl.pascani.CronExpression

/**
 * @author Miguel Jiménez - Initial contribution and API
 */
class PascaniTypeComputer extends XbaseTypeComputer {

	override computeTypes(XExpression expression, ITypeComputationState state) {
		switch (expression) {
			CronExpression: _computeTypes(expression, state)
			default: super.computeTypes(expression, state)
		}
	}
	
	def protected _computeTypes(CronExpression expression, ITypeComputationState state) {
		val result = getRawTypeForName(org.quartz.CronExpression, state);
		state.acceptActualType(result);
	}

}
