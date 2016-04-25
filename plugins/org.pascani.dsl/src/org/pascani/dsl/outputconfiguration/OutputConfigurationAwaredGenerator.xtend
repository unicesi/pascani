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
package org.pascani.dsl.outputconfiguration

import org.eclipse.xtext.xbase.compiler.JvmModelGenerator
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.IFileSystemAccess
import com.google.inject.Inject
import org.pascani.dsl.generator.PascaniGenerator

/**
 * @author Miguel Jiménez - Initial contribution and API
 */
class OutputConfigurationAwaredGenerator extends JvmModelGenerator {
	
	@Inject private PascaniGenerator generator

	/*
	 * More information on using both IGenerator and IJvmModelInferrer:
	 * https://www.eclipse.org/forums/index.php/t/486215/
	 */
	override void doGenerate(Resource input, IFileSystemAccess fsa) {
		val _contents = input.getContents()
		for (obj : _contents) {
			val adapters = obj.eAdapters.filter(OutputConfigurationAdapter)
			for (adapter : adapters) {
				var outputConfiguration = adapter.getOutputConfigurationName()
				if (outputConfiguration == PascaniOutputConfigurationProvider::PASCANI_OUTPUT) {
					val sfsa = new SingleOutputConfigurationFileSystemAccess(fsa, outputConfiguration)
					this.internalDoGenerate(obj, sfsa) // PascaniJvmModelInferrer
				} else if (outputConfiguration == PascaniOutputConfigurationProvider::SCA_OUTPUT) {
					this.generator.doGenerate(input, fsa)
				}
			}
			if (adapters.isEmpty) {
				this.internalDoGenerate(obj, fsa)
			}
		}
	}

}
