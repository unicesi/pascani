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

import com.google.common.base.Function
import com.google.inject.Inject
import java.io.Serializable
import java.math.BigDecimal
import java.util.ArrayList
import java.util.List
import java.util.UUID
import org.eclipse.xtext.common.types.JvmGenericType
import org.eclipse.xtext.common.types.JvmMember
import org.eclipse.xtext.common.types.JvmOperation
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.naming.IQualifiedNameProvider
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import org.eclipse.xtext.xbase.XVariableDeclaration
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
import pascani.lang.Probe
import pascani.lang.events.ChangeEvent
import pascani.lang.events.IntervalEvent
import pascani.lang.infrastructure.BasicNamespace
import pascani.lang.infrastructure.NamespaceProxy
import pascani.lang.util.CronConstant
import pascani.lang.util.EventHandler
import pascani.lang.util.JobScheduler
import pascani.lang.util.NonPeriodicEvent
import pascani.lang.util.PeriodicEvent
import org.eclipse.xtext.xbase.XBlockExpression

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

	def dispatch void infer(Monitor monitor, IJvmDeclaredTypeAcceptor acceptor, boolean isPreIndexingPhase) {
		val monitorImpl = monitor.toClass(monitor.fullyQualifiedName)

		monitorImpl.eAdapters.add(new OutputConfigurationAdapter(
			PascaniOutputConfigurationProvider::PASCANI_OUTPUT
		))

		acceptor.accept(monitorImpl) [ m |
			val blocks = new ArrayList
			val subscriptions = new ArrayList

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
						m.members += e.createPeriodicClass(monitor)
						m.members += e.toField(e.name, typeRef(monitor.name + "_" + e.name)) [
							^final = true
							^static = true
							initializer = '''new «monitor.name + "_" + e.name»()'''
						]
					}
					Event case e.emitter != null && e.emitter.cronExpression == null: {
						m.members += e.createNonPeriodicClass(monitor)
						m.members += e.toField(e.name, typeRef(monitor.name + "_" + e.name)) [
							^final = true
							^static = true
							initializer = '''new «monitor.name + "_" + e.name»()'''
						]
					}
					Handler: {
						if (e.param.parameterType.type.qualifiedName.equals(IntervalEvent.canonicalName)) {
							m.members += e.createJobClass(monitor)
						} else {
							m.members += e.createNonPeriodicClass(monitor);
						}
						m.members += e.toField(e.name, typeRef(monitor.name + "_" + e.name)) [
							^final = true
							^static = true
							initializer = '''new «monitor.name + "_" + e.name»()'''
						]
					}
					XBlockExpression: {
						blocks += e
					}
				}
			}
			// TODO: handle and log the exception
			m.members += monitor.toConstructor [
				body = '''
					try {
						initialize();
						«IF(blocks.size > 0)»
							«FOR i : 0..blocks.size - 1»
								applyCustomCode«i»();
							«ENDFOR»
						«ENDIF»
					} catch(Exception e) {
						e.printStackTrace();
					}
				'''
			]
			m.members += monitor.toMethod("initialize", typeRef(void)) [
				visibility = JvmVisibility::PRIVATE
				body = '''
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
			for(var i = 0; i < blocks.size; i++) {
				val ic = i;
				m.members += monitor.toMethod("applyCustomCode" + ic, typeRef(void)) [
					visibility = JvmVisibility::PRIVATE
					body = blocks.get(ic)
				]
			}
			if (monitor.usings != null) {
				for (namespace : monitor.usings.filter[n|n.name != null]) {
					m.members += namespace.toField(namespace.name, typeRef(namespace.fullyQualifiedName.toString)) [
						^static = true
					]
				}
			}
		]
	}

	def dispatch void infer(Namespace namespace, IJvmDeclaredTypeAcceptor acceptor, boolean isPreIndexingPhase) {
		namespace.createProxy(isPreIndexingPhase, acceptor, true)
		namespace.createClass(isPreIndexingPhase, acceptor)
	}

	def String parseSpecifier(String changeEvent, RelationalEventSpecifier specifier, List<JvmMember> members) {
		var left = ""
		var right = ""

		if (specifier.left instanceof RelationalEventSpecifier)
			left = parseSpecifier(changeEvent, specifier.left as RelationalEventSpecifier, members)
		else
			left = parseSpecifier(changeEvent, specifier.left, members)

		if (specifier.right instanceof RelationalEventSpecifier)
			right = parseSpecifier(changeEvent, specifier.right as RelationalEventSpecifier, members)
		else
			right = parseSpecifier(changeEvent, specifier.right, members)

		'''
			«left» «parseSpecifierLogOp(specifier.operator)»
			«right»
		'''
	}

	// FIXME: reproduce explicit parentheses
	def String parseSpecifier(String changeEvent, EventSpecifier specifier, List<JvmMember> members) {
		val op = parseSpecifierRelOp(specifier)
		val suffix = System.nanoTime()
		val typeRef = typeRef(BigDecimal)
		members += specifier.value.toField("value" + suffix, specifier.value.inferredType) [
			initializer = specifier.value
		]
		if (specifier.
			isPercentage) {
			'''
				(new «typeRef.qualifiedName»(«changeEvent».previousValue().toString()).subtract(
				 new «typeRef.qualifiedName»(«changeEvent».value().toString())
				)).abs().doubleValue() «op» new «typeRef.qualifiedName»(«changeEvent».previousValue().toString()).doubleValue() * (this.value«suffix» / 100.0)
			'''
		} else {
			'''
				new «typeRef.qualifiedName»(«changeEvent».value().toString()).doubleValue() «op» this.value«suffix»
			'''
		}
	}

	def parseSpecifierRelOp(EventSpecifier specifier) {
		if (specifier.isAbove) '''>''' else if (specifier.isBelow) '''<''' else if (specifier.isEqual) '''=='''
	}

	def parseSpecifierLogOp(RelationalOperator op) {
		if (op.equals(RelationalOperator.OR)) '''||''' else if (op.equals(RelationalOperator.AND)) '''&&'''
	}

	def JvmGenericType createJobClass(Handler handler, Monitor monitor) {
		val clazz = createNonPeriodicClass(handler, monitor)
		val expressionVar = "expression" + System.nanoTime()
		val contextVar = "context" + System.nanoTime()

		clazz.superTypes += typeRef(Job)
		clazz.members += handler.toField(expressionVar, typeRef(String))
		clazz.members += handler.toSetter(expressionVar, typeRef(String))
		clazz.members += handler.toMethod("execute", typeRef(void)) [
			exceptions += typeRef(JobExecutionException)
			parameters += handler.toParameter(contextVar, typeRef(JobExecutionContext))
			body = '''
				«contextVar».getMergedJobDataMap();
				handle(new «typeRef(IntervalEvent)»(«typeRef(UUID)».randomUUID(), this.«expressionVar»));
			'''
		]
		return clazz
	}

	def JvmGenericType createNonPeriodicClass(Handler handler, Monitor monitor) {
		val eventVar = "event"
		handler.toClass(monitor.name + "_" + handler.name) [
			^static = true
			superTypes += typeRef(EventHandler)
			members += handler.toMethod("handle", typeRef(void)) [
				parameters += handler.toParameter(eventVar, typeRef(pascani.lang.Event))
				body = '''
					«handler.name»((«typeRef(handler.param.parameterType.type.qualifiedName)») «eventVar»);
				'''
			]
			members += createMethod(handler)
		]
	}

	def JvmOperation createMethod(Handler handler) {
		handler.toMethod(handler.name, typeRef(void)) [
			documentation = handler.documentation
			parameters +=
				handler.toParameter(handler.param.name, typeRef(handler.param.parameterType.type.qualifiedName))
			body = handler.body
		]
	}
	
	def JvmGenericType createPeriodicClass(Event e, Monitor monitor) {
		e.toClass(monitor.fullyQualifiedName + "_" + e.name) [
			documentation = e.documentation
			^static = true
			superTypes += typeRef(PeriodicEvent)
			members += e.emitter.toField("expression", typeRef(CronExpression))
			members += e.emitter.toGetter("expression", typeRef(CronExpression))
			// TODO: handle the exception (idea: Xtend's sneaky throw with logging capabilities)
			members += e.toConstructor [
				body = '''
					try {
						initialize();
						«IF (e.emitter.cronExpression.constant != null)»
							this.expression = new «typeRef(CronExpression)»(«typeRef(CronConstant)».valueOf("«e.emitter.cronExpression.constant.toUpperCase»").expression());
						«ELSE»
							this.expression = new «typeRef(CronExpression)»("«NodeModelUtils.getNode(e.emitter.cronExpression).text.trim()»");
						«ENDIF»
					} catch(Exception e) {
						e.printStackTrace();
					}
				'''
			]
			members += e.emitter.toMethod("updateExpression", typeRef(void)) [
				parameters += e.emitter.toParameter("expression", typeRef(CronExpression))
				body = '''
					this.expression = expression;
				'''
			]
			members += managedEventMembers(e)
		]
	}

	def JvmGenericType createNonPeriodicClass(Event e, Monitor monitor) {
		e.toClass(monitor.fullyQualifiedName + "_" + e.name) [
			val varSuffix = System.nanoTime()
			val specifierTypeRef = typeRef(Function, typeRef(ChangeEvent), typeRef(Boolean))
			val eventTypeRef = typeRef(Class, wildcardExtends(typeRef(pascani.lang.Event, wildcard())))
			val eventTypeRefName = '''pascani.lang.events.«e.emitter.eventType.toString.toLowerCase.toFirstUpper»Event'''

			documentation = e.documentation
			^static = true
			superTypes += typeRef(NonPeriodicEvent)
			members += e.toConstructor[
				body = '''
					initialize();
				'''
			]
			members += e.emitter.toField("type" + varSuffix, eventTypeRef) [
				initializer = '''«eventTypeRefName».class'''
			]
			members += e.emitter.toMethod("getType", eventTypeRef) [
				body = '''return this.type«varSuffix»;'''
			]
			members += e.emitter.toField("emitter" + varSuffix, e.emitter.emitter.inferredType) [
				initializer = e.emitter.emitter
			]
			members += e.emitter.toMethod("getEmitter", typeRef(Object)) [
				body = '''return this.emitter«varSuffix»;'''
			]
			members += managedEventMembers(e)

			if (e.emitter.specifier != null) {
				members += e.emitter.specifier.toClass("Specifier" + varSuffix) [
					val fields = new ArrayList<JvmMember>
					val code = new ArrayList
					if (e.emitter.specifier instanceof RelationalEventSpecifier)
						code.add(parseSpecifier("changeEvent" + varSuffix,
								e.emitter.specifier as RelationalEventSpecifier, fields))
					else
						code.add(parseSpecifier("changeEvent" + varSuffix, e.emitter.specifier, fields))

					superTypes += specifierTypeRef
					members += fields
					members += e.emitter.specifier.toMethod("apply", typeRef(Boolean)) [
						parameters += e.emitter.specifier.toParameter("changeEvent" + varSuffix, typeRef(ChangeEvent))
						body = '''return «code.get(0)»;'''
					]
					members += e.emitter.specifier.toMethod("equals", typeRef(boolean)) [
						parameters += e.emitter.specifier.toParameter("object", typeRef(Object))
						body = '''return false;''' // Don't care
					]
				]
				members += e.emitter.specifier.toMethod("getSpecifier", specifierTypeRef) [
					body = '''return new Specifier«varSuffix»();'''
				]

			} else {
				members += e.toMethod("getSpecifier", specifierTypeRef) [
					// There is no validation to perform, all events are welcomed
					body = '''
						return new «specifierTypeRef»() {
							public «Boolean.simpleName» apply(«typeRef(ChangeEvent).simpleName» event) {
								return true;
							}
						};
					'''
				]
			}

			if (e.emitter.probe != null) {
				members += e.emitter.probe.toField("probe" + varSuffix, typeRef(Probe)) [
					initializer = e.emitter.probe
				]
				members += e.emitter.probe.toMethod("getProbe", typeRef(Probe)) [
					body = '''return this.probe«varSuffix»;'''
				]
			} else {
				members += e.toMethod("getProbe", typeRef(Probe)) [
					body = '''return null;'''
				]
			}
		]
	}
	
	def List<JvmMember> managedEventMembers(Event e) {
		val members = new ArrayList<JvmMember>
		members += e.emitter.toField("paused", typeRef(boolean)) [
			^volatile = true
		]
		members += e.emitter.toField("subscribers", typeRef(List, typeRef(EventHandler)))
		members += e.emitter.toMethod("initialize", typeRef(void)) [
			visibility = JvmVisibility::PRIVATE
			body = '''
				this.paused = false;
				this.subscribers = new «typeRef(ArrayList)»<«typeRef(EventHandler)»>();
			'''
		]
		members += e.emitter.toMethod("pause", typeRef(void)) [
			body = '''
				this.paused = true;
			'''
		]
		members += e.emitter.toMethod("resume", typeRef(void)) [
			body = '''
				this.paused = false;
			'''
		]
		members += e.emitter.toMethod("subscribe", typeRef(void)) [
			parameters += e.emitter.toParameter("handler", typeRef(EventHandler))
			body = '''
				
			'''
		]
		
		return members
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
			PascaniOutputConfigurationProvider::PASCANI_OUTPUT
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

				// TODO: Handle the exception by logging it
				if (isParentNamespace) {
					members += namespace.toField(namespace.name + "Proxy", typeRef(NamespaceProxy))
					members += namespace.toConstructor [
						body = '''
							try {
								this.«namespace.name»Proxy = new «NamespaceProxy»("«namespace.fullyQualifiedName»");
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
			val output = PascaniOutputConfigurationProvider::PASCANI_OUTPUT
			namespaceProxyImpl.eAdapters.add(new OutputConfigurationAdapter(output))
			acceptor.accept(namespaceProxyImpl)
		}

		return namespaceProxyImpl;
	}

}
