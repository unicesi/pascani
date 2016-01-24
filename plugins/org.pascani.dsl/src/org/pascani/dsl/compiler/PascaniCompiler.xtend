package org.pascani.dsl.compiler

import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import org.eclipse.xtext.xbase.XExpression
import org.eclipse.xtext.xbase.compiler.XbaseCompiler
import org.eclipse.xtext.xbase.compiler.output.ITreeAppendable
import org.pascani.dsl.lib.util.Exceptions
import org.pascani.dsl.pascani.CronExpression

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
		val seconds = NodeModelUtils.getNode(exp.seconds).text
		val minutes = NodeModelUtils.getNode(exp.minutes).text
		val hours = NodeModelUtils.getNode(exp.hours).text
		val dayOfMonth = NodeModelUtils.getNode(exp.dayOfMonth).text
		val month = NodeModelUtils.getNode(exp.month).text
		val dayOfWeek = NodeModelUtils.getNode(exp.dayOfWeek).text
		var year = ""
		if (exp.year != null)
			year = NodeModelUtils.getNode(exp.year).text
		val _expr = seconds + minutes + hours + dayOfMonth + month + dayOfWeek + year
		appendable.append(Exceptions)
		appendable.append('''.sneakyInitializer(''')
		appendable.append(org.quartz.CronExpression)
		appendable.append('''.class, "«_expr»")''')
	}

}
