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
package org.pascani.dsl.formatting

import com.google.inject.Inject
import org.eclipse.xtext.formatting.impl.AbstractDeclarativeFormatter
import org.eclipse.xtext.formatting.impl.FormattingConfig
import org.pascani.dsl.services.PascaniGrammarAccess

/**
 * This class contains custom formatting declarations.
 * 
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#formatting
 * on how and when to use it.
 * 
 * Also see {@link org.eclipse.xtext.xtext.XtextFormattingTokenSerializer} as an example
 */
class PascaniFormatter extends AbstractDeclarativeFormatter {

	@Inject extension PascaniGrammarAccess

	override protected void configureFormatting(FormattingConfig c) {

		c.setLinewrap(0, 1, 1).before(SL_COMMENTRule)
		c.setLinewrap(2).before(ML_COMMENTRule)
		c.setLinewrap(1).after(ML_COMMENTRule)

		// set a maximum size of lines
		c.setAutoLinewrap(80);

		// set a line wrap after the import section and package declaration
		c.setLinewrap(2).between(modelAccess.nameAssignment_0_1, modelAccess.importsAssignment_1)
		c.setLinewrap(2).between(modelAccess.importsAssignment_1, modelAccess.typeDeclarationAssignment_2)
		c.setLinewrap(2).between(modelAccess.nameAssignment_0_1, modelAccess.typeDeclarationAssignment_2)

		c.setLinewrap().before(XVariableDeclarationAccess.valKeyword_1_1)

		// set indentation inside all curly brackets 
		// set line wrap after each left curly bracket
		// set line wrap around each right curly bracket
		for (p : findKeywordPairs("{", "}")) {
			c.setIndentationIncrement.after(p.first);
			c.setIndentationDecrement.before(p.second);
			c.setLinewrap.after(p.first);
			c.setLinewrap.around(p.second);
		}

		// set no space around all parentheses
		for (p : findKeywordPairs("(", ")")) {
			c.setNoSpace.around(p.first);
			c.setNoSpace.around(p.second);
		}

		// set no space before all commas
		for (comma : findKeywords(",")) {
			c.setNoSpace.before(comma);
		}

		// set empty line between two inner elements type declarations
		c.setLinewrap(2).between(namespaceBlockExpressionAccess.leftCurlyBracketKeyword_1,
			namespaceDeclarationAccess.namespaceKeyword_0)
		c.setLinewrap(2).between(monitorDeclarationAccess.bodyAssignment_4, handlerDeclarationAccess.bodyAssignment_5)
		c.setLinewrap(2).between(monitorDeclarationAccess.bodyAssignment_4, eventDeclarationAccess.emitterAssignment_5)
		c.setLinewrap(2).between(eventDeclarationAccess.emitterAssignment_5, handlerDeclarationAccess.bodyAssignment_5)

		c.setLinewrap(2).between(eventDeclarationAccess.emitterAssignment_5, eventDeclarationAccess.emitterAssignment_5)
		c.setLinewrap(2).between(handlerDeclarationAccess.bodyAssignment_5, handlerDeclarationAccess.bodyAssignment_5)

		// EVENTS
		c.setLinewrap(1).before(eventDeclarationAccess.eventKeyword_0)

		c.setLinewrap(1).after(eventEmitterAccess.alternatives)
		c.setLinewrap(1).before(eventEmitterAccess.specifierAssignment_0_3)

		c.setIndentationIncrement.before(eventDeclarationAccess.raisedKeyword_2)
		c.setIndentationDecrement.after(eventEmitterAccess.alternatives)

		c.setSpace(" ").after(cronElementAccess.alternatives)
		c.setLinewrap(1).after(cronExpressionAccess.alternatives)

	// TODO: continue working on the formatter rules for monitor declarations
	}
}
