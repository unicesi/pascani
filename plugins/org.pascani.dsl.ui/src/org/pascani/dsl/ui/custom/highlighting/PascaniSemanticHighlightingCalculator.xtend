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
package org.pascani.dsl.ui.custom.highlighting

import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EStructuralFeature
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.xtext.ide.editor.syntaxcoloring.IHighlightedPositionAcceptor
import org.eclipse.xtext.nodemodel.ILeafNode
import org.eclipse.xtext.nodemodel.INode
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.util.CancelIndicator
import org.eclipse.xtext.xbase.ide.highlighting.XbaseHighlightingCalculator
import org.pascani.dsl.pascani.CronExpression
import org.pascani.dsl.pascani.EventEmitter
import org.pascani.dsl.pascani.PascaniPackage
import org.pascani.dsl.pascani.RelationalEventSpecifier
import org.pascani.dsl.pascani.impl.CronExpressionImpl
import org.pascani.dsl.pascani.impl.EventEmitterImpl
import org.pascani.dsl.pascani.impl.RelationalEventSpecifierImpl
import org.pascani.dsl.pascani.util.PascaniSwitch

class PascaniSemanticHighlightingCalculator extends XbaseHighlightingCalculator {

	override void provideHighlightingFor(XtextResource resource, IHighlightedPositionAcceptor acceptor,
		CancelIndicator cancelIndicator) {
		if (resource == null || resource.getParseResult() == null)
			return;

		val supported = newArrayList(RelationalEventSpecifierImpl, EventEmitterImpl, CronExpressionImpl)
		val switcher = new HighlightingSwitch(acceptor);
		val iterator = EcoreUtil.getAllContents(resource, true);

		while (iterator.hasNext()) {
			val current = iterator.next();
			if (supported.contains(current.class)) {
				switcher.doSwitch(current);
			} else {
				super.highlightElement(current, acceptor, cancelIndicator)
			}
		}
	}

	/*
	 * Adapted from:
	 * http://stackoverflow.com/questions/5008773/xtext-using-the-grammar
	 * -classes-in-isemantichighlightingcalculator (Xtext 1.x)
	 * https://blogs.itemis.de/leipzig/archives/467
	 */
	private static class HighlightingSwitch extends PascaniSwitch<Void> {

		private val IHighlightedPositionAcceptor acceptor;

		new(IHighlightedPositionAcceptor acceptor) {
			this.acceptor = acceptor;
		}

		override Void caseRelationalEventSpecifier(RelationalEventSpecifier object) {
			val node = getFirstFeatureNode(object, PascaniPackage.eINSTANCE.getRelationalEventSpecifier_Operator());
			highlightNode(node, PascaniHighlightingConfiguration.RELATIONAL_OP_ID);
			return null;
		}

		override Void caseEventEmitter(EventEmitter object) {
			val node = getFirstFeatureNode(object, PascaniPackage.eINSTANCE.getEventEmitter_EventType());
			highlightNode(node, PascaniHighlightingConfiguration.EVENT_TYPE_ID);
			return null;
		}

		override Void caseCronExpression(CronExpression object) {
			// Constants
			val constant = getFirstFeatureNode(object, PascaniPackage.eINSTANCE.getCronExpression_Constant());
			highlightNode(constant, PascaniHighlightingConfiguration.STRING_ID);

			// Expression
			val leftSymbol = getFirstFeatureNode(object, PascaniPackage.eINSTANCE.getCronExpression_Lsymbol())
			val rightSymbol = getFirstFeatureNode(object, PascaniPackage.eINSTANCE.getCronExpression_Rsymbol())
			
			val seconds = getFirstFeatureNode(object, PascaniPackage.eINSTANCE.getCronExpression_Seconds());
			val minutes = getFirstFeatureNode(object, PascaniPackage.eINSTANCE.getCronExpression_Minutes());
			val hours = getFirstFeatureNode(object, PascaniPackage.eINSTANCE.getCronExpression_Hours());
			val dayOfMonth = getFirstFeatureNode(object, PascaniPackage.eINSTANCE.getCronExpression_DayOfMonth());
			val month = getFirstFeatureNode(object, PascaniPackage.eINSTANCE.getCronExpression_Month());
			val daysOfWeek = getFirstFeatureNode(object, PascaniPackage.eINSTANCE.getCronExpression_DayOfWeek());
			val year = getFirstFeatureNode(object, PascaniPackage.eINSTANCE.getCronExpression_Year());

			highlightNodes(PascaniHighlightingConfiguration.CRON_EXPRESSION_ID, leftSymbol, rightSymbol);
			highlightNodes(PascaniHighlightingConfiguration.STRING_ID, seconds, minutes, hours, 
				dayOfMonth, month, daysOfWeek);

			if (year != null)
				highlightNode(year, PascaniHighlightingConfiguration.STRING_ID);
				
			return null;
		}

		def highlightNodes(String id, INode... nodes) {
			for (INode node : nodes) {
				highlightNode(node, id);
			}
		}

		def highlightNode(INode node, String id) {
			if (node == null)
				return;
			if (node instanceof ILeafNode) {
				acceptor.addPosition(node.getOffset(), node.getLength(), id);
			} else {
				for (ILeafNode leaf : node.getLeafNodes()) {
					if (!leaf.isHidden()) {
						acceptor.addPosition(leaf.getOffset(), leaf.getLength(), id);
					}
				}
			}
		}
	}

	def static INode getFirstFeatureNode(EObject semantic, EStructuralFeature feature) {
		if (feature == null)
			return NodeModelUtils.findActualNodeFor(semantic);
		val nodes = NodeModelUtils.findNodesForFeature(semantic, feature);

		if (!nodes.isEmpty())
			return nodes.get(0);

		return null;
	}

}
