/*
 * Copyright © 2015 Universidad Icesi
 * 
 * This file is part of the Pascani library.
 * 
 * The Pascani library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * The Pascani library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with The Pascani library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.pascani.dsl.lib.compiler.templates

import com.google.common.collect.Lists
import java.util.List
import org.pascani.dsl.lib.Event
import org.pascani.dsl.lib.PascaniRuntime
import org.pascani.dsl.lib.compiler.util.NameProposal
import org.pascani.dsl.lib.events.ExceptionEvent
import org.pascani.dsl.lib.events.InvokeEvent
import org.pascani.dsl.lib.events.ReturnEvent
import org.pascani.dsl.lib.events.TimeLapseEvent
import org.pascani.dsl.lib.infrastructure.AbstractProducer
import org.pascani.dsl.lib.infrastructure.rabbitmq.RabbitMQProducer
import org.pascani.dsl.lib.util.LocalEventProducer

class ProbeTemplates {

	/**
	 * Initializes the {@link EventProducer} within the service interceptor
	 */
	def static String getProducerInitialization(String producerVar) {
		'''
			this.«producerVar» = new «LocalEventProducer.simpleName»<«Event.simpleName»<?>>(PascaniRuntime.Context.PROBE);
		'''
	}

	def static String getInitializationContrib(String probeClass, String probesExchange, String probeRoutingKey,
		boolean addProducer, List<Class<? extends Event<?>>> events) {
		'''
			try {
				«probeClass» probe = new «probeClass»();
				«IF addProducer»
					«AbstractProducer.simpleName» producer = 
						new «RabbitMQProducer.simpleName»("«probesExchange»", "«probeRoutingKey»");
					
					producer.acceptOnly(
						«FOR clazz : events SEPARATOR ", "»
							«clazz.canonicalName».class
						«ENDFOR»
					);
					«PascaniRuntime.canonicalName»
						.getRuntimeInstance(«PascaniRuntime.canonicalName».Context.PROBE)
						.registerEventListener(producer);
				«ENDIF»
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
		'''
	}

	/**
	 * Generates the code to intercept service executions and measure the execution time
	 */
	def static String getInterceptorMethodBody(String producerVar, String ijpVar,
		List<Class<? extends Event<?>>> events) {

		var names = Lists.newArrayList(producerVar, ijpVar);
		val varNames = new NameProposal(names)

		val transactionVar = varNames.getNewName("transactionId");
		val returnVar = varNames.getNewName("_return");

		val invokeEVar = varNames.getNewName("invokeEvent");
		val exceptionEVar = varNames.getNewName("exceptionEvent");
		val timeLapseEVar = varNames.getNewName("timeLapseEvent");
		val returnEVar = varNames.getNewName("returnEvent");

		val causeVar = varNames.getNewName("cause");
		val startVar = varNames.getNewName("start");
		val endVar = varNames.getNewName("end");

		'''
			«IF !events.isEmpty»
				UUID «transactionVar» = UUID.randomUUID();
			«ENDIF»
			
			«IF events.contains(InvokeEvent)»
				«getInvokeEventContrib(ijpVar, invokeEVar, transactionVar, producerVar)»
			«ENDIF»
			
			«IF events.contains(TimeLapseEvent)»
				long «startVar» = System.nanoTime();
			«ENDIF»
			
			«IF events.contains(ExceptionEvent)»
				«getExceptionEventContrib(ijpVar, returnVar, exceptionEVar, causeVar, transactionVar, producerVar)»
			«ELSE»
				Object «returnVar» = «ijpVar».proceed();
			«ENDIF»
			
			«IF events.contains(TimeLapseEvent)»
				long «endVar» = System.nanoTime();
				«getTimeLapseEventContrib(startVar, endVar, timeLapseEVar, transactionVar, producerVar)»
			«ENDIF»
			
			«IF events.contains(ReturnEvent)»
				«getReturnEventContrib(ijpVar, returnVar, returnEVar, transactionVar, producerVar)»
			«ENDIF»
			
			return «returnVar»;
		'''
	}

	def private static String getExceptionEventContrib(String ijpVar, String returnVar, String exceptionEVar,
		String causeVar, String transactionVar, String producerVar) {
		'''
			Object «returnVar» = null;
			try {
				«returnVar» = «ijpVar».proceed();
			} catch(Throwable «causeVar») {
				«ExceptionEvent.simpleName» «exceptionEVar» = new «ExceptionEvent.simpleName»(
					«transactionVar»,
					new Exception(«causeVar»),
					«ijpVar».getMethod().getDeclaringClass(),
					«ijpVar».getMethod().getName(),
					«ijpVar».getMethod().getParameterTypes(),
					«ijpVar».getArguments()
				);
				
				«producerVar».post(«exceptionEVar»);
				throw new Throwable(«causeVar»);
			}
		'''
	}

	def private static getTimeLapseEventContrib(String startVar, String endVar, String timeLapseEVar,
		String transactionVar, String producerVar) {
		'''
			«TimeLapseEvent.simpleName» «timeLapseEVar» = new «TimeLapseEvent.simpleName»(«transactionVar», «startVar», «endVar»);
			«producerVar».post(«timeLapseEVar»);
		'''
	}

	def private static getInvokeEventContrib(String ijpVar, String invokeEVar, String transactionVar,
		String producerVar) {
		'''
			«InvokeEvent.simpleName» «invokeEVar» = new «InvokeEvent.simpleName»(
				«transactionVar», 
				«ijpVar».getMethod().getDeclaringClass(),
				«ijpVar».getMethod().getName(),
				«ijpVar».getMethod().getParameterTypes(),
				«ijpVar».getArguments()
			);
			«producerVar».post(«invokeEVar»);
		'''
	}

	def private static getReturnEventContrib(String ijpVar, String returnVar, String returnEVar, String transactionVar,
		String producerVar) {
		'''
			«ReturnEvent.simpleName» «returnEVar» = new «ReturnEvent.simpleName»(
				«transactionVar», 
				«ijpVar».getMethod().getDeclaringClass(),
				«ijpVar».getMethod().getName(),
				«ijpVar».getMethod().getParameterTypes(),
				«returnVar»
			);
			«producerVar».post(«returnEVar»);
		'''
	}

}