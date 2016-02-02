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
package org.pascani.dsl

import com.google.inject.Binder
import com.google.inject.Singleton
import com.google.inject.name.Names
import org.eclipse.xtext.generator.IGenerator
import org.eclipse.xtext.generator.IOutputConfigurationProvider
import org.eclipse.xtext.linking.LinkingScopeProviderBinding
import org.eclipse.xtext.naming.IQualifiedNameProvider
import org.eclipse.xtext.scoping.IScopeProvider
import org.eclipse.xtext.scoping.impl.AbstractDeclarativeScopeProvider
import org.eclipse.xtext.xbase.compiler.XbaseCompiler
import org.eclipse.xtext.xbase.scoping.batch.ImplicitlyImportedFeatures
import org.eclipse.xtext.xbase.typesystem.computation.ITypeComputer
import org.pascani.dsl.compiler.PascaniCompiler
import org.pascani.dsl.outputconfiguration.OutputConfigurationAwaredGenerator
import org.pascani.dsl.outputconfiguration.PascaniOutputConfigurationProvider
import org.pascani.dsl.runtime.PascaniQualifiedNameProvider
import org.pascani.dsl.scoping.PascaniImplicitlyImportedFeatures
import org.pascani.dsl.scoping.PascaniScopeProvider
import org.pascani.dsl.typesystem.PascaniTypeComputer

/**
 * Use this class to register components to be used at runtime / without the Equinox extension registry.
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
class PascaniRuntimeModule extends AbstractPascaniRuntimeModule {
	
	override void configureIScopeProviderDelegate(Binder binder) {
		binder
			.bind(IScopeProvider)
			.annotatedWith(Names.named(AbstractDeclarativeScopeProvider.NAMED_DELEGATE))
			.to(PascaniScopeProvider)
	}
	
	override void configureLinkingIScopeProvider(Binder binder) {
		binder
			.bind(IScopeProvider)
			.annotatedWith(LinkingScopeProviderBinding)
			.to(PascaniScopeProvider);
	}

	override Class<? extends IQualifiedNameProvider> bindIQualifiedNameProvider() {
		return PascaniQualifiedNameProvider
	}

	override Class<? extends IScopeProvider> bindIScopeProvider() {
		return PascaniScopeProvider
	}

	override void configure(Binder binder) {
		super.configure(binder)
		binder
			.bind(IOutputConfigurationProvider)
			.to(PascaniOutputConfigurationProvider)
			.in(Singleton)
		binder
			.bind(ImplicitlyImportedFeatures)
			.to(PascaniImplicitlyImportedFeatures)
	}

	override Class<? extends IGenerator> bindIGenerator() {
		return OutputConfigurationAwaredGenerator
	}
	
	def Class<? extends ITypeComputer> bindITypeComputer() {
		return PascaniTypeComputer
	}

	def Class<? extends XbaseCompiler> bindXbaseCompiler() {
		return PascaniCompiler
	}
	
}
