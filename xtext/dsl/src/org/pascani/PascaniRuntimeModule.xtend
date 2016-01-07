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
package org.pascani

import com.google.inject.Binder
import com.google.inject.Singleton
import org.eclipse.xtext.generator.IGenerator
import org.eclipse.xtext.generator.IOutputConfigurationProvider
import org.eclipse.xtext.naming.IQualifiedNameProvider
import org.eclipse.xtext.scoping.IScopeProvider
import org.pascani.outputconfiguration.OutputConfigurationAwaredGenerator
import org.pascani.outputconfiguration.PascaniOutputConfigurationProvider
import org.pascani.runtime.PascaniQualifiedNameProvider
import org.pascani.scoping.PascaniScopeProvider

/**
 * Use this class to register components to be used at runtime / without the Equinox extension registry.
 */
class PascaniRuntimeModule extends AbstractPascaniRuntimeModule {

	override Class<? extends IQualifiedNameProvider> bindIQualifiedNameProvider() {
		return PascaniQualifiedNameProvider;
	}

	override Class<? extends IScopeProvider> bindIScopeProvider() {
		return PascaniScopeProvider;
	}

	override void configure(Binder binder) {
		super.configure(binder);
		binder
			.bind(IOutputConfigurationProvider)
			.to(PascaniOutputConfigurationProvider)
			.in(Singleton);
	}

	override Class<? extends IGenerator> bindIGenerator() {
		return OutputConfigurationAwaredGenerator;
	}

}
