/*
 * Copyright © 2015 Universidad Icesi
 * 
 * This file is part of the Pascani project.
 * 
 * The Pascani project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * The Pascani project is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with The Pascani project. If not, see <http://www.gnu.org/licenses/>.
 */
package org.pascani.dsl.jvmmodel

import com.google.common.base.Function
import com.google.inject.Inject
import java.io.Serializable
import java.math.BigDecimal
import java.util.ArrayList
import java.util.List
import java.util.Observable
import java.util.UUID
import org.eclipse.xtext.common.types.JvmGenericType
import org.eclipse.xtext.common.types.JvmMember
import org.eclipse.xtext.common.types.JvmOperation
import org.eclipse.xtext.common.types.JvmTypeReference
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.naming.IQualifiedNameProvider
import org.eclipse.xtext.xbase.XAbstractFeatureCall
import org.eclipse.xtext.xbase.XExpression
import org.eclipse.xtext.xbase.XVariableDeclaration
import org.eclipse.xtext.xbase.jvmmodel.AbstractModelInferrer
import org.eclipse.xtext.xbase.jvmmodel.IJvmDeclaredTypeAcceptor
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.osoa.sca.annotations.Scope
import org.pascani.dsl.lib.PascaniRuntime.Context
import org.pascani.dsl.lib.events.ChangeEvent
import org.pascani.dsl.lib.events.IntervalEvent
import org.pascani.dsl.lib.infrastructure.AbstractConsumer
import org.pascani.dsl.lib.infrastructure.BasicNamespace
import org.pascani.dsl.lib.infrastructure.NamespaceProxy
import org.pascani.dsl.lib.infrastructure.ProbeProxy
import org.pascani.dsl.lib.sca.FrascatiUtils
import org.pascani.dsl.lib.sca.PascaniUtils
import org.pascani.dsl.lib.util.events.EventObserver
import org.pascani.dsl.lib.util.events.NonPeriodicEvent
import org.pascani.dsl.lib.util.events.PeriodicEvent
import org.pascani.dsl.outputconfiguration.OutputConfigurationAdapter
import org.pascani.dsl.outputconfiguration.PascaniOutputConfigurationProvider
import org.pascani.dsl.pascani.ConfigBlockExpression
import org.pascani.dsl.pascani.Event
import org.pascani.dsl.pascani.EventSpecifier
import org.pascani.dsl.pascani.EventType
import org.pascani.dsl.pascani.Handler
import org.pascani.dsl.pascani.Monitor
import org.pascani.dsl.pascani.Namespace
import org.pascani.dsl.pascani.RelationalEventSpecifier
import org.pascani.dsl.pascani.RelationalOperator
import org.pascani.dsl.pascani.TypeDeclaration
import org.quartz.Job
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException

/**
 * <p>Infers a JVM model from the source model.</p> 
 * 
 * <p>The JVM model should contain all elements that would appear in the Java code 
 * which is generated from the source model. Other models link against the JVM model rather than the source model.</p>
 * 
 * @author Miguel Jiménez - Initial contribution and API  
 */
class PascaniJvmModelInferrer extends AbstractModelInferrer {

	@Inject extension JvmTypesBuilder
	
	@Inject extension IQualifiedNameProvider
	
	static val prefix = "＿" 

	def dispatch void infer(Monitor monitor, IJvmDeclaredTypeAcceptor acceptor, boolean isPreIndexingPhase) {
		monitor.createClass(isPreIndexingPhase, acceptor)
	}

	def dispatch void infer(Namespace namespace, IJvmDeclaredTypeAcceptor acceptor, boolean isPreIndexingPhase) {
		namespace.createProxy(isPreIndexingPhase, acceptor, true)
		namespace.createClass(isPreIndexingPhase, acceptor)
	}
	
	def JvmTypeReference toEventType(EventType type) {
		return typeRef("org.pascani.dsl.lib.events." + type.toString.toLowerCase.toFirstUpper + "Event")
	}
	
	def void createClass(Monitor monitor, boolean isPreIndexingPhase, IJvmDeclaredTypeAcceptor acceptor) {
		val monitorImpl = monitor.toClass(monitor.fullyQualifiedName)
		if (monitorImpl == null)
			return;
		monitorImpl.eAdapters.add(new OutputConfigurationAdapter(PascaniOutputConfigurationProvider::PASCANI_OUTPUT))
		monitorImpl.eAdapters.add(new OutputConfigurationAdapter(PascaniOutputConfigurationProvider::SCA_OUTPUT))
		acceptor.accept(monitorImpl) [
			val nestedTypes = new ArrayList
			val fields = new ArrayList
			val constructors = new ArrayList
			val methods = new ArrayList
			val getters = new ArrayList
			
			// utility variables
			var nblocks = 0
			val events = new ArrayList

			// More information on the Scope annotation: http://mail-archive.ow2.org/frascati/2011-02/msg00001.html
			annotations += annotationRef(Scope, "COMPOSITE")
			superTypes += typeRef(org.pascani.dsl.lib.infrastructure.Monitor)
			
			if (monitor.usings != null) {
				for (namespace : monitor.usings.filter[n|n.name != null]) {
					fields += namespace.toField(namespace.name, typeRef(namespace.fullyQualifiedName.toString)) [
						^static = true
					]
				}
			}
			
			if (monitor.eventImports != null) {
				for (^import : monitor.eventImports.importDeclarations) {
					for (event : ^import.events) {
						if (event != null && event.emitter != null) {
							val eventTypeRef = event.emitter.eventType.toEventType
							val innerClass = event.createNonPeriodicClass(^import.monitor, eventTypeRef, true)
							nestedTypes += innerClass
							fields += event.toField(event.name, typeRef(NonPeriodicEvent, eventTypeRef)) [
								^final = true
								^static = true
								initializer = '''new «innerClass.simpleName»()'''
							]
							events += event	
						}
					}
				}
			}
			
			for (e : monitor.body.expressions) {
				switch (e) {
					XVariableDeclaration: {
						fields += e.toField(e.name, e.type) [
							documentation = e.documentation
							initializer = e.right
							^final = !e.isWriteable
							^static = true
						]
					}
					
					Event case e.emitter != null && e.emitter.cronExpression != null: {
						getters += e.toMethod("init" + e.name.toFirstUpper, inferredType) [
							^static = true
							visibility = JvmVisibility::PRIVATE
							body = e.emitter.cronExpression
						]
						fields += e.toField(e.name, typeRef(PeriodicEvent)) [
							^final = true
							^static = true
							initializer = '''
								new «PeriodicEvent»(init«e.name.toFirstUpper»())
							'''
						]
						events += e
					}
					
					Event case e.emitter != null && e.emitter.cronExpression == null: {
						val eventTypeRef = e.emitter.eventType.toEventType
						val innerClass = e.createNonPeriodicClass(monitor, eventTypeRef, false)
						nestedTypes += innerClass
						fields += e.toField(e.name, typeRef(NonPeriodicEvent, eventTypeRef)) [
							^final = true
							^static = true
							initializer = '''new «innerClass.simpleName»()'''
						]
						events += e
					}
					
					Handler: {
						if (e.param !== null && e.param.parameterType != null) {
							if (e.param.parameterType.type.qualifiedName.equals(IntervalEvent.canonicalName)) {
								nestedTypes += e.createJobClass
							} else {
								val innerClass = e.createNonPeriodicClass(monitor.name + "_")
								nestedTypes += innerClass
								fields +=
									e.toField(e.name,
										typeRef(EventObserver, typeRef(e.param.parameterType.type.qualifiedName))) [
										^final = true
										^static = true
										initializer = '''new «innerClass.simpleName»()'''
									]
							}	
						}
					}
					
					ConfigBlockExpression case !e.expressions.isEmpty: {
						methods += monitor.toMethod("applyCustomCode" + nblocks++, typeRef(void)) [
							visibility = JvmVisibility::PRIVATE
							documentation = e.documentation
							body = e
						]
					}
				}
			}
			val fblocks = nblocks
			constructors += monitor.toConstructor [
				body = '''
					try {
						initialize();
						«IF(fblocks > 0)»
							«FOR i : 0..fblocks - 1»
								applyCustomCode«i»();
							«ENDFOR»
						«ENDIF»
					} catch(Exception e) {
						«org.pascani.dsl.lib.util.Exceptions.canonicalName».sneakyThrow(e);
					}
				'''
			]
			
			methods += monitor.toMethod("initialize", typeRef(void)) [
				visibility = JvmVisibility::PRIVATE
				body = '''
					«IF monitor.usings != null»
						«FOR namespace : monitor.usings»
							this.«namespace.name» = new «namespace.name»();
						«ENDFOR»
					«ENDIF»
					«FOR event : events.filter[e|e.emitter.cronExpression != null]»
						super.periodicEvents.put("«event.name»", this.«event.name»);
					«ENDFOR»
				'''
				exceptions += typeRef(Exception)
			]
			
			// Methods from the super type
			methods += monitor.toMethod("pause", typeRef(void)) [
				annotations += annotationRef(Override)
				body = '''
					if (isPaused())
						return;
					super.pause();
					«events.map[e|e.name].join("\n", [e|e + ".pause();"])»
				'''
			]

			methods += monitor.toMethod("resume", typeRef(void)) [
				annotations += annotationRef(Override)
				body = '''
					if (!isPaused())
						return;
					super.resume();
					«events.map[e|e.name].join("\n", [e|e + ".resume();"])»
				'''
			]
			
			// Add members in an organized way
			members += fields
			members += constructors
			members += methods
			members += nestedTypes
			members += getters
		]
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
		val suffix = System.nanoTime
		val op = parseSpecifierRelOp(specifier)
		val typeRef = typeRef(BigDecimal)
		members += specifier.value.toField("value" + suffix, specifier.value.inferredType) [
			initializer = specifier.value
		]
		if (specifier.isPercentage) {
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

	def JvmGenericType createJobClass(Handler handler) {
		val clazz = createNonPeriodicClass(handler, "")
		clazz.visibility = JvmVisibility::PUBLIC
		clazz.superTypes += typeRef(Job)
		clazz.members += handler.toMethod("execute", typeRef(void)) [
			exceptions += typeRef(JobExecutionException)
			parameters += handler.toParameter("context", typeRef(JobExecutionContext))
			body = '''
				«typeRef(JobDataMap)» data = context.getJobDetail().getJobDataMap();
				execute(new «typeRef(IntervalEvent)»(«typeRef(UUID)».randomUUID(), (String) data.get("expression")));
			'''
		]
		return clazz
	}

	def JvmGenericType createNonPeriodicClass(Handler handler, String classPrefix) {
		handler.toClass(classPrefix + handler.name) [
			^static = true
			visibility = JvmVisibility::PRIVATE
			superTypes += typeRef(EventObserver, typeRef(handler.param.parameterType.type.qualifiedName))
			members += handler.toMethod("update", typeRef(void)) [
				parameters += handler.toParameter("observable", typeRef(Observable))
				parameters += handler.toParameter("argument", typeRef(Object))
				body = '''
					if (argument instanceof «typeRef(handler.param.parameterType.type.qualifiedName)») {
						execute((«typeRef(handler.param.parameterType.type.qualifiedName)») argument);
					}
				'''
			]
			members += createMethod(handler)
		]
	}

	def JvmOperation createMethod(Handler handler) {
		handler.toMethod("execute", typeRef(void)) [
			documentation = handler.documentation
			annotations += annotationRef(Override)
			parameters +=
				handler.toParameter(handler.param.name, typeRef(handler.param.parameterType.type.qualifiedName))
			body = handler.body
		]
	}

	def JvmGenericType createNonPeriodicClass(Event e, Monitor monitor, JvmTypeReference eventTypeRef, boolean isProxy) {
		e.toClass(monitor.fullyQualifiedName + "_" + e.name) [
			val names = #{
				"probe" -> prefix + "probe", 
				"type" -> prefix + "type", 
				"emitter" -> prefix + "emitter",
				"consumer" -> prefix + "consumer",
				"Specifier" -> "Specifier_" + e.name,
				"changeEvent" -> prefix + "changeEvent"
			}
			val specifierTypeRef = typeRef(Function, typeRef(ChangeEvent), typeRef(Boolean))
			val eventClassRef = typeRef(Class, wildcardExtends(typeRef(org.pascani.dsl.lib.Event, wildcard())))
			val isChangeEvent = e.emitter.eventType.equals(EventType.CHANGE)
			val routingKey = new ArrayList
			
			documentation = e.documentation
			^static = true
			visibility = JvmVisibility::PRIVATE
			superTypes += typeRef(NonPeriodicEvent, eventTypeRef)
			
			members += e.emitter.toField(names.get("type"), typeRef(Class, eventTypeRef)) [
				initializer = '''«eventTypeRef».class'''
			]
			
			if (!isProxy && e.emitter.emitter != null) {
				members += e.emitter.toField(names.get("emitter"), e.emitter.emitter.inferredType) [
					initializer = e.emitter.emitter
				]
			}
			
			members += e.toField(names.get("consumer"), typeRef(AbstractConsumer))

			if (isChangeEvent) {
				routingKey += monitor.name + "." + getEmitterFQN(e.emitter.emitter).last + ".getClass().getCanonicalName()"
			} else {
				routingKey += "\"" + monitor.fullyQualifiedName + "." + e.name + "\""
				members += e.emitter.toField(names.get("probe"), typeRef(ProbeProxy))
			}
			
			members += e.toConstructor[
				body = '''
					«IF isProxy»
						super.isProxyEvent = «isProxy»;
					«ENDIF»
					initialize();
				'''
			]
			members += e.emitter.toMethod("initialize", typeRef(void)) [
				visibility = JvmVisibility::PRIVATE
				body = '''
					final «typeRef(Context)» context = «typeRef(Context)».«Context.MONITOR.toString»;
					final String routingKey = «routingKey.get(0)»;
					final String consumerTag = "«monitor.fullyQualifiedName».«e.name»";
					«IF isChangeEvent»
						final String variable = routingKey + ".«getEmitterFQN(e.emitter.emitter).toList.reverseView.drop(1).join(".")»";
					«ENDIF»
					try {
						«IF !isChangeEvent»
							«IF !isProxy»
								if (bindingUri == null)
									this.bindingUri = «typeRef(FrascatiUtils)».DEFAULT_BINDING_URI;
								final String intentName = «typeRef(PascaniUtils)».intentName(this.«names.get("type")»);
								«typeRef(PascaniUtils)».newIntent(this.«names.get("emitter")», routingKey, intentName, bindingUri);
								if (useProbe) {
									«typeRef(PascaniUtils)».resetProbe(this.«names.get("emitter")», routingKey, bindingUri);
									this.«names.get("probe")» = new «typeRef(ProbeProxy)»(routingKey);
								}
								«typeRef(PascaniUtils)».resetProducer(this.«names.get("emitter")», routingKey, bindingUri);
							«ELSE»
								if (useProbe)
									this.«names.get("probe")» = new «typeRef(ProbeProxy)»(routingKey);
							«ENDIF»
						«ENDIF»
						this.«names.get("consumer")» = initializeConsumer(context, routingKey, consumerTag«IF isChangeEvent», variable«ENDIF»);
						this.«names.get("consumer")».start();
					} catch(Exception e) {
						«org.pascani.dsl.lib.util.Exceptions.canonicalName».sneakyThrow(e);
					}
				'''
			]
			members += e.emitter.toMethod("getType", eventClassRef) [
				annotations += annotationRef(Override)
				body = '''return this.«names.get("type")»;'''
			]
			
			members += e.emitter.toMethod("getProbe", typeRef(ProbeProxy)) [
					annotations += annotationRef(Override)
					body = '''return «IF(isChangeEvent)»null«ELSE»this.«names.get("probe")»«ENDIF»;'''
			]
			
			members += e.emitter.toMethod("getConsumer", typeRef(AbstractConsumer)) [
					annotations += annotationRef(Override)
					body = '''return this.«names.get("consumer")»;'''
			]

			if (e.emitter.specifier != null) {
				members += e.emitter.specifier.toClass(names.get("Specifier")) [
					val fields = new ArrayList<JvmMember>
					val code = new ArrayList
					
					if (e.emitter.specifier instanceof RelationalEventSpecifier)
						code.add(parseSpecifier(names.get("changeEvent"),
								e.emitter.specifier as RelationalEventSpecifier, fields))
					else
						code.add(parseSpecifier(names.get("changeEvent"), e.emitter.specifier, fields))

					superTypes += specifierTypeRef
					
					members += fields
					
					members += e.emitter.specifier.toMethod("apply", typeRef(Boolean)) [
						parameters += e.emitter.specifier.toParameter(names.get("changeEvent"), typeRef(ChangeEvent))
						body = '''return «code.get(0)»;'''
					]
					
					members += e.emitter.specifier.toMethod("equals", typeRef(boolean)) [
						parameters += e.emitter.specifier.toParameter("object", typeRef(Object))
						body = '''return false;''' // Don't care
					]
				]
				
				members += e.emitter.specifier.toMethod("getSpecifier", specifierTypeRef) [
					annotations += annotationRef(Override)
					body = '''return new «names.get("Specifier")»();'''
				]
			}
		]
	}
	
	def Iterable<String> getEmitterFQN(XExpression expression) {
		var segments = new ArrayList
		if (expression instanceof XAbstractFeatureCall) {
			segments += expression.concreteSyntaxFeatureName
			segments += getEmitterFQN(expression.actualReceiver)
			return segments.filter[l|!l.isEmpty]
		}
		return segments
	}

	def JvmGenericType createClass(Namespace namespace, boolean isPreIndexingPhase, IJvmDeclaredTypeAcceptor acceptor) {
		val namespaceImpl = namespace.toClass(namespace.fullyQualifiedName + "Namespace") [
			if (!isPreIndexingPhase) {
				val List<XVariableDeclaration> declarations = getVariableDeclarations(namespace)
				
				// More information on the Scope annotation: http://mail-archive.ow2.org/frascati/2011-02/msg00001.html
				annotations += annotationRef(Scope, "COMPOSITE")
				superTypes += typeRef(BasicNamespace)

				for (decl : declarations) {
					if (decl.name != null) {
						val name = decl.fullyQualifiedName.toString.replace(".", "_")
						val type = decl.type // ?: inferredType(decl.right)
						members += decl.toField(name, type) [
							initializer = decl.right
						]
					}
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
		namespaceImpl.eAdapters.add(new OutputConfigurationAdapter(PascaniOutputConfigurationProvider::PASCANI_OUTPUT))
		namespaceImpl.eAdapters.add(new OutputConfigurationAdapter(PascaniOutputConfigurationProvider::SCA_OUTPUT))
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
				val fields = new ArrayList
				val constructors = new ArrayList
				val methods = new ArrayList
				val nestedTypes = new ArrayList
				documentation = namespace.documentation
				
				for (e : namespace.body.expressions) {
					switch (e) {
						Namespace case e.name != null: {
							val internalClass = createProxy(e, isPreIndexingPhase, acceptor, false)
							nestedTypes += internalClass
							fields += e.toField(e.name, typeRef(internalClass)) [
								initializer = '''new «internalClass.simpleName»()'''
							]
							methods += e.toMethod(e.name, typeRef(internalClass)) [
								body = '''return this.«e.name»;'''
							]
						}
					}
				}
				for (e : namespace.body.expressions) {
					switch (e) {
						XVariableDeclaration case e.name != null: {
							val name = e.fullyQualifiedName.toString
							val type = e.type // ?: inferredType(e.right)
							val cast = if(type != null) "(" + type.simpleName + ")"

							methods += e.toMethod(e.name, type) [
								body = '''return «cast» getVariable("«name»");'''
							]

							if (e.isWriteable) {
								methods += e.toMethod(e.name, typeRef(Void.TYPE)) [
									parameters += e.toParameter(e.name, type)
									body = '''setVariable("«name»", «e.name»);'''
								]
							}
						}
					}
				}
				if (isParentNamespace) {
					fields += namespace.toField(namespace.name + "Proxy", typeRef(NamespaceProxy))
					constructors += namespace.toConstructor [
						body = '''
							try {
								this.«namespace.name»Proxy = new «NamespaceProxy»("«namespace.fullyQualifiedName»");
							} catch(«Exception» e) {
								«org.pascani.dsl.lib.util.Exceptions.canonicalName».sneakyThrow(e);
							}
						'''
					]
					methods += namespace.toMethod("getVariable", typeRef(Serializable)) [
						parameters += namespace.toParameter("variable", typeRef(String))
						body = '''return this.«namespace.name»Proxy.getVariable(variable);'''
					]
					methods += namespace.toMethod("setVariable", typeRef(void)) [
						parameters += namespace.toParameter("variable", typeRef(String))
						parameters += namespace.toParameter("value", typeRef(Serializable))
						body = '''this.«namespace.name»Proxy.setVariable(variable, value);'''
					]
				}
				// Add members in an organized way
				members += fields
				members += constructors
				members += methods
				members += nestedTypes
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
