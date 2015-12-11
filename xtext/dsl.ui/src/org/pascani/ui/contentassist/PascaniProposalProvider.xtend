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
package org.pascani.ui.contentassist

import org.pascani.ui.contentassist.AbstractPascaniProposalProvider
import java.util.Set
import org.eclipse.xtext.Keyword
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext
import org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor
import com.google.inject.Inject
import org.pascani.services.PascaniGrammarAccess
import java.util.HashSet
import java.util.Arrays

/**
 * See https://www.eclipse.org/Xtext/documentation/304_ide_concepts.html#content-assist
 * on how to customize the content assistant.
 */
class PascaniProposalProvider extends AbstractPascaniProposalProvider {

	@Inject PascaniGrammarAccess grammarAccess;

	/**ƒ
	 * Keywords already proposed from templates
	 */
	private static Set<String> FILTERED_KEYWORDS = new HashSet(
		Arrays.asList("package", "monitor", "namespace", "handler", "event", "below", "above", "equal", "val", "var"))

	// From: https://kthoms.wordpress.com/2012/05/22/xtext-content-assist-filtering-keyword-proposals/
	override completeKeyword(Keyword keyword, ContentAssistContext contentAssistContext,
		ICompletionProposalAcceptor acceptor) {
		if (FILTERED_KEYWORDS.contains(keyword.getValue())) {
			// don't propose keyword
			return;
		}

		// Filter keywords at certain place
		if (!grammarAccess.eventEmitterAccess.usingKeyword_0_4_0.equals(keyword) && // using
		!grammarAccess.eventTypeAccess.exceptionExceptionKeyword_3_0.equals(keyword) && // exception
		!grammarAccess.eventTypeAccess.changeChangeKeyword_2_0.equals(keyword) && // change
		!grammarAccess.eventTypeAccess.invokeInvokeKeyword_0_0.equals(keyword) && // invoke
		!grammarAccess.eventTypeAccess.returnReturnKeyword_1_0.equals(keyword) && // return
		!grammarAccess.eventDeclarationAccess.periodicalPeriodicallyKeyword_3_0.equals(keyword) && // periodically
		!grammarAccess.eventDeclarationAccess.emittedIndicatedOnKeyword_4_0.equals(keyword)) // on
		{
			super.completeKeyword(keyword, contentAssistContext, acceptor);
		}
	}

}
