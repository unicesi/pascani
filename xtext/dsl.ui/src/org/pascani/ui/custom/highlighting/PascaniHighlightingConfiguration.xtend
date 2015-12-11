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
package org.pascani.ui.custom.highlighting

import org.eclipse.xtext.ui.editor.syntaxcoloring.DefaultHighlightingConfiguration
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfigurationAcceptor
import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.RGB

class PascaniHighlightingConfiguration extends DefaultHighlightingConfiguration {

	public static val String RELATIONAL_OP_ID = "relational_op";
	public static val String EVENT_TYPE_ID = "event_type";
	public static val String CRON_EXPRESSION_ID = "cron_expression_constant";

	override configure(IHighlightingConfigurationAcceptor acceptor) {

		acceptor.acceptDefaultHighlighting(RELATIONAL_OP_ID, "Relational operators (and, or)", relationalOpTextStyle());
		acceptor.acceptDefaultHighlighting(EVENT_TYPE_ID, "Event types", eventTypeTextStyle());
		acceptor.acceptDefaultHighlighting(CRON_EXPRESSION_ID, "Cron expressions", cronExpressionTextStyle());

		super.configure(acceptor);
	}

	def relationalOpTextStyle() {
		val textStyle = stringTextStyle().copy();
		textStyle.setStyle(SWT.ITALIC);
		return textStyle;
	}

	def eventTypeTextStyle() {
		val textStyle = stringTextStyle().copy();
		textStyle.setStyle(SWT.ITALIC);
		return textStyle;
	}

	def cronExpressionTextStyle() {
		val textStyle = defaultTextStyle().copy();
		textStyle.setColor(new RGB(125, 125, 125));
		return textStyle;
	}

}
