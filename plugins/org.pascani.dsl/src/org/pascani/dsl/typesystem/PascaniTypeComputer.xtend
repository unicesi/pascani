package org.pascani.dsl.typesystem

import org.eclipse.xtext.xbase.XExpression
import org.eclipse.xtext.xbase.typesystem.computation.ITypeComputationState
import org.eclipse.xtext.xbase.typesystem.computation.XbaseTypeComputer
import org.pascani.dsl.pascani.CronExpression

class PascaniTypeComputer extends XbaseTypeComputer {

	override computeTypes(XExpression expression, ITypeComputationState state) {
		switch (expression) {
			CronExpression: _computeTypes(expression, state)
			default: super.computeTypes(expression, state)
		}
	}
	
	def protected _computeTypes(CronExpression expression, ITypeComputationState state) {
		val result = getRawTypeForName(String, state);
		state.acceptActualType(result);
	}

}
