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
package org.pascani.ui

import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor
import org.eclipse.xtext.ide.editor.syntaxcoloring.ISemanticHighlightingCalculator
import org.eclipse.xtext.ui.editor.folding.IFoldingRegionProvider
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfiguration
import org.pascani.ui.custom.highlighting.PascaniHighlightingConfiguration
import org.pascani.ui.custom.highlighting.PascaniSemanticHighlightingCalculator
import org.pascani.ui.editor.PascaniFoldingRegionProvider

/**
 * Use this class to register components to be used within the Eclipse IDE.
 */
@FinalFieldsConstructor
class PascaniUiModule extends AbstractPascaniUiModule {

	override Class<? extends ISemanticHighlightingCalculator> bindIdeSemanticHighlightingCalculator() {
		return PascaniSemanticHighlightingCalculator;
	}

	override Class<? extends IHighlightingConfiguration> bindIHighlightingConfiguration() {
		return PascaniHighlightingConfiguration;
	}

	def Class<? extends IFoldingRegionProvider> bindIFoldingRegionProvider() {
		return PascaniFoldingRegionProvider;
	}

}
