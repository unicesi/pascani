package org.pascani.compiler

import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import org.eclipse.xtext.xbase.XExpression
import org.eclipse.xtext.xbase.compiler.XbaseCompiler
import org.eclipse.xtext.xbase.compiler.output.ITreeAppendable
import org.pascani.pascani.CronExpression
import pascani.lang.util.CronConstant

class PascaniCompiler extends XbaseCompiler {

	override internalToConvertedExpression(XExpression obj, ITreeAppendable appendable) {
		switch (obj) {
			CronExpression: _toJavaExpression(obj, appendable)
			default: super.internalToConvertedExpression(obj, appendable)
		}
	}

	override doInternalToJavaStatement(XExpression obj, ITreeAppendable appendable, boolean isReferenced) {
		switch (obj) {
			CronExpression: generateComment(obj, appendable, isReferenced)
			default: super.doInternalToJavaStatement(obj, appendable, isReferenced)
		}
	}

	def void _toJavaExpression(CronExpression exp, ITreeAppendable appendable) {
		appendable.append("\"");
		if (exp.constant != null) {
			val constant = exp.constant.toUpperCase
			if (CronConstant.values.map[v|v.toString].contains(constant))
				appendable.append(CronConstant.valueOf(constant).expression())
		} else {
			val seconds = NodeModelUtils.getNode(exp.seconds).text
			val minutes = NodeModelUtils.getNode(exp.minutes).text
			val hours = NodeModelUtils.getNode(exp.hours).text
			val dayOfMonth = NodeModelUtils.getNode(exp.dayOfMonth).text
			val month = NodeModelUtils.getNode(exp.month).text
			val dayOfWeek = NodeModelUtils.getNode(exp.dayOfWeek).text
			val year = if(exp.year != null) NodeModelUtils.getNode(exp.year).text
			appendable.append('''«seconds»«minutes»«hours»«dayOfMonth»«month»«dayOfWeek»«year»''')
		}
		appendable.append("\"");
	}

}
