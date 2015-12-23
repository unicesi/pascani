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
package org.pascani.jvmmodel

import com.google.inject.Inject
import java.io.Serializable
import java.util.ArrayList
import java.util.List
import org.eclipse.xtext.common.types.JvmGenericType
import org.eclipse.xtext.naming.IQualifiedNameProvider
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import org.eclipse.xtext.xbase.XVariableDeclaration
import org.eclipse.xtext.xbase.compiler.ImportManager
import org.eclipse.xtext.xbase.compiler.XbaseCompiler
import org.eclipse.xtext.xbase.compiler.output.FakeTreeAppendable
import org.eclipse.xtext.xbase.jvmmodel.AbstractModelInferrer
import org.eclipse.xtext.xbase.jvmmodel.IJvmDeclaredTypeAcceptor
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.pascani.outputconfiguration.OutputConfigurationAdapter
import org.pascani.outputconfiguration.PascaniOutputConfigurationProvider
import org.pascani.pascani.Event
import org.pascani.pascani.EventSpecifier
import org.pascani.pascani.Handler
import org.pascani.pascani.Monitor
import org.pascani.pascani.Namespace
import org.pascani.pascani.RelationalEventSpecifier
import org.pascani.pascani.RelationalOperator
import org.pascani.pascani.TypeDeclaration
import org.quartz.CronExpression
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import pascani.lang.infrastructure.BasicNamespace
import pascani.lang.infrastructure.NamespaceProxy
import pascani.lang.util.CronConstant
import pascani.lang.util.JobScheduler

/**
 * <p>Infers a JVM model from the source model.</p> 
 * 
 * <p>The JVM model should contain all elements that would appear in the Java code 
 * which is generated from the source model. Other models link against the JVM model rather than the source model.</p>     
 */
class PascaniJvmModelInferrer extends AbstractModelInferrer {

	/**
	 * convenience API to build and initialize JVM types and their members.
	 */
	@Inject extension JvmTypesBuilder

	@Inject extension IQualifiedNameProvider
	
	@Inject extension XbaseCompiler

	def dispatch void infer(Monitor monitor, IJvmDeclaredTypeAcceptor acceptor, boolean isPreIndexingPhase) {
		val monitorImpl = monitor.toClass(monitor.fullyQualifiedName)

		monitorImpl.eAdapters.add(new OutputConfigurationAdapter(
			PascaniOutputConfigurationProvider::MONITORS_OUTPUT
		))

		acceptor.accept(monitorImpl) [ m |
			val subscriptions = new ArrayList
			val cronEvents = new ArrayList
			val importManager = new ImportManager(true, m)

			for (e : monitor.body.expressions) {
				switch (e) {
					XVariableDeclaration: {
						m.members += e.toField(e.name, e.type) [
							documentation = e.documentation
							initializer = e.right
							^final = !e.isWriteable
							^static = true
						]
					}
					Event case e.emitter != null && e.emitter.cronExpression != null: {
						// TODO: add subscription to subscriptions
						// TODO: change the subscription: subscription = job scheduling
						cronEvents.add(e)
						m.members += e.toField(e.name, typeRef(CronExpression)) [
							documentation = e.documentation
						]
					}
					Event case e.emitter != null && e.emitter.cronExpression == null: {
						m.members += e.toField(e.name, typeRef(String)) [
							documentation = e.documentation
							// TODO: must be an Event with parameters corresponding to the grammar (values are instances of Serializable)
							initializer = '''"demo"'''
							if (e.emitter.specifier != null) {
								var code = ""
								if (e.emitter.specifier instanceof RelationalEventSpecifier)
									code = parseSpecifier(importManager, "changeEvent", e.emitter.specifier as RelationalEventSpecifier)
								else
									code = parseSpecifier(importManager, "changeEvent", e.emitter.specifier)
							}
						]
					}
					Handler: {
						m.members += e.createClass(isPreIndexingPhase)
					}
					default: {
						// subscriptions...
					}
				}
			}

			// TODO: handle and log the exception
			m.members += monitor.toConstructor [
				body = '''
					try {
						initialize();
					} catch(Exception e) {
						e.printStackTrace();
					}
				'''
			]

			m.members += monitor.toMethod("initialize", typeRef(void)) [
				body = '''
					«FOR cronEvent : cronEvents»
						«IF(cronEvent.emitter.cronExpression.constant != null)»
							this.«cronEvent.name» = new «typeRef(CronExpression)»(«typeRef(CronConstant)».valueOf("«cronEvent
								.emitter.cronExpression.constant.toUpperCase»").expression());
						«ELSE»
							this.«cronEvent.name» = new «typeRef(CronExpression)»("«NodeModelUtils
								.getNode(cronEvent.emitter.cronExpression).text.trim()»");
						«ENDIF»
					«ENDFOR»
					«FOR subscription : subscriptions»
						«JobScheduler».schedule(null, new «CronExpression»(""), null);
					«ENDFOR»
					«IF monitor.usings != null»
						«FOR namespace : monitor.usings»
							«namespace.name» = new «namespace.name»();
						«ENDFOR»
					«ENDIF»
				'''
				exceptions += typeRef(Exception)
			]

			if (monitor.usings != null) {
				for (namespace : monitor.usings.filter[n|n.name != null]) {
					m.members += namespace.toField(namespace.name, typeRef(namespace.fullyQualifiedName.toString)) [
						^static = true
					]
				}
			}
		]
	}

	def String parseSpecifier(ImportManager importManager, String changeEvent, RelationalEventSpecifier specifier) {
		var left = ""
		var right = ""

		if (specifier.left instanceof RelationalEventSpecifier)
			left = parseSpecifier(importManager, changeEvent, specifier.left as RelationalEventSpecifier)
		else
			left = parseSpecifier(importManager, changeEvent, specifier.left)

		if (specifier.right instanceof RelationalEventSpecifier)
			right = parseSpecifier(importManager, changeEvent, specifier.right as RelationalEventSpecifier)
		else
			right = parseSpecifier(importManager, changeEvent, specifier.right)

		'''
			«left» «parseSpecifierLogOp(specifier.operator)» «right»
		'''
	}

	// FIXME: reproduce explicit parentheses
	def String parseSpecifier(ImportManager importManager, String changeEvent, EventSpecifier specifier) {
		val op = parseSpecifierRelOp(specifier)
		val result = new FakeTreeAppendable(importManager)
		// FIXME: this is not compiling
		val value = specifier.value.compileAsJavaExpression(result, specifier.value.inferredType)

		if (specifier.isPercentage) {
			'''«typeRef(Math)».abs(«changeEvent».previousValue()-«changeEvent».value()) «op» «changeEvent».previousValue()*(«value»/100.0)'''
		} else {
			'''«changeEvent».value() «op» «value»'''
		}
	}

	def parseSpecifierRelOp(EventSpecifier specifier) {
		if (specifier.isAbove) '''>''' 
		else if (specifier.isBelow) '''<''' 
		else if (specifier.isEqual) '''=='''
	}

	def parseSpecifierLogOp(RelationalOperator op) {
		if (op.equals(RelationalOperator.OR)) '''||''' 
		else if (op.equals(RelationalOperator.AND)) '''&&'''
	}

	def dispatch void infer(Namespace namespace, IJvmDeclaredTypeAcceptor acceptor, boolean isPreIndexingPhase) {
		namespace.createProxy(isPreIndexingPhase, acceptor, true)
		namespace.createClass(isPreIndexingPhase, acceptor)
	}

	def JvmGenericType createClass(Handler handler, boolean isPreIndexingPhase) {
		val handlerImpl = handler.toClass(handler.name) [
			if (!isPreIndexingPhase) {
				^static = true
				superTypes += typeRef(Job)

				members += handler.toMethod("execute", typeRef(void)) [
					documentation = handler.documentation
					exceptions += typeRef(JobExecutionException)
					parameters += handler.toParameter("context" + System.nanoTime(), typeRef(JobExecutionContext))
					body = handler.body
				]
			}
		]

		return handlerImpl
	}

	def JvmGenericType createClass(Namespace namespace, boolean isPreIndexingPhase, IJvmDeclaredTypeAcceptor acceptor) {

		val namespaceImpl = namespace.toClass(namespace.fullyQualifiedName + "Namespace") [
			if (!isPreIndexingPhase) {

				val List<XVariableDeclaration> declarations = getVariableDeclarations(namespace)
				superTypes += typeRef(BasicNamespace)

				for (decl : declarations) {
					val name = decl.fullyQualifiedName.toString.replace(".", "_")
					val type = decl.type // ?: inferredType(decl.right)
					members += decl.toField(name, type) [
						initializer = decl.right
					]
				}

				members += namespace.toConstructor [
					exceptions += typeRef(Exception)
					body = '''
						super("«namespace.fullyQualifiedName»");
						«FOR decl : declarations»
							registerVariable("«decl.fullyQualifiedName»", «decl.fullyQualifiedName.toString.replace(".", "_")», false);
						«ENDFOR»
					'''
				]
			}
		]

		namespaceImpl.eAdapters.add(new OutputConfigurationAdapter(
			PascaniOutputConfigurationProvider::NAMESPACES_OUTPUT
		))
		acceptor.accept(namespaceImpl)

		return namespaceImpl
	}

	def List<XVariableDeclaration> getVariableDeclarations(TypeDeclaration typeDecl) {
		val List<XVariableDeclaration> variables = new ArrayList<XVariableDeclaration>()

		for (e : typeDecl.body.expressions) {
			switch (e) {
				TypeDeclaration: {
					variables.addAll(getVariableDeclarations(e))
				}
				XVariableDeclaration: {
					variables.add(e)
				}
			}
		}

		return variables
	}

	def JvmGenericType createProxy(Namespace namespace, boolean isPreIndexingPhase, IJvmDeclaredTypeAcceptor acceptor,
		boolean isParentNamespace) {

		val namespaceProxyImpl = namespace.toClass(namespace.fullyQualifiedName) [

			if (!isPreIndexingPhase) {
				documentation = namespace.documentation

				for (e : namespace.body.expressions) {
					switch (e) {
						Namespace: {
							val internalClass = createProxy(e, isPreIndexingPhase, acceptor, false)
							members += internalClass
							members += e.toField(e.name, typeRef(internalClass)) [
								initializer = '''new «internalClass.simpleName»()'''
							]
							members += e.toMethod(e.name, typeRef(internalClass)) [
								body = '''return this.«e.name»;'''
							]
						}
					}
				}

				for (e : namespace.body.expressions) {
					switch (e) {
						XVariableDeclaration: {
							val name = e.fullyQualifiedName.toString
							val type = e.type // ?: inferredType(e.right)
							val cast = if(type != null) "(" + type.simpleName + ")"

							members += e.toMethod(e.name, type) [
								body = '''return «cast» getVariable("«name»");'''
							]

							if (e.isWriteable) {
								members += e.toMethod(e.name, typeRef(Void.TYPE)) [
									parameters += e.toParameter(e.name, type)
									body = '''setVariable("«name»", «e.name»);'''
								]
							}
						}
					}
				}

				// TODO: Handle the exception by loggging it
				if (isParentNamespace) {
					members += namespace.toField(namespace.name + "Proxy", typeRef(NamespaceProxy))
					members += namespace.toConstructor [
						body = '''
							try {
								String routingKey = "«namespace.fullyQualifiedName»";
								this.«namespace.name»Proxy = new «NamespaceProxy»(routingKey);
							} catch(«Exception» e) {
								e.printStackTrace();
							}
						'''
					]

					members += namespace.toMethod("getVariable", typeRef(Serializable)) [
						parameters += namespace.toParameter("variable", typeRef(String))
						body = '''return this.«namespace.name»Proxy.getVariable(variable);'''
					]

					members += namespace.toMethod("setVariable", typeRef(void)) [
						parameters += namespace.toParameter("variable", typeRef(String))
						parameters += namespace.toParameter("value", typeRef(Serializable))
						body = '''this.«namespace.name»Proxy.setVariable(variable, value);'''
					]
				}
			}
		]

		if (isParentNamespace) {
			val output = PascaniOutputConfigurationProvider::MONITORS_OUTPUT

			namespaceProxyImpl.eAdapters.add(new OutputConfigurationAdapter(output))
			acceptor.accept(namespaceProxyImpl)
		}

		return namespaceProxyImpl;
	}

}
