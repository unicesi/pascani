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
package org.pascani.outputconfiguration

import org.eclipse.xtext.xbase.compiler.JvmModelGenerator
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.IFileSystemAccess

class OutputConfigurationAwaredGenerator extends JvmModelGenerator {

	override void doGenerate(Resource input, IFileSystemAccess fsa) {
		val _contents = input.getContents()
		for (obj : _contents) {
			var String outputConfiguration = null
			val adapters = obj.eAdapters.filter(OutputConfigurationAdapter)

			if (adapters.size == 1)
				outputConfiguration = adapters.get(0).getOutputConfigurationName()

			if (outputConfiguration == null)
				this.internalDoGenerate(obj, fsa)
			else
				this.internalDoGenerate(obj, new SingleOutputConfigurationFileSystemAccess(fsa, outputConfiguration))
		}
	}

}
