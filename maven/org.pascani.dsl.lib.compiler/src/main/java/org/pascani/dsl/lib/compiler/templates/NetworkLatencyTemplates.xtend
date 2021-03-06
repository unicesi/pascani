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
package org.pascani.dsl.lib.compiler.templates

import com.google.common.base.Joiner
import java.util.Collection
import java.util.List
import org.pascani.dsl.lib.PascaniRuntime.Context
import org.pascani.dsl.lib.compiler.util.NameProposal
import org.pascani.dsl.lib.events.NetworkLatencyEvent
import org.pascani.dsl.lib.infrastructure.rabbitmq.RabbitMQProducer

/**
 * @author Miguel Jiménez - Initial contribution and API
 */
class NetworkLatencyTemplates {

	/**
	 * Generates the body for methods within the initial adapter (where the measurement starts, and the 
	 * return measurement ends) for a modified interface, measuring network latency data.
	 */
	def static String initialAdapterMethod(String startVar, String eventVar, List<String> eventParams, String eVar,
		String paramTypesVar, Collection<String> paramTypes, boolean isVoid, String referenceVar, String producerVar,
		String methodName, String methodReturn, Collection<String> paramNames) {

		var params = if(paramNames.size > 0) ", " + Joiner.on(", ").join(paramNames) else "";
		val _return = if(!isVoid) new NameProposal(paramNames).getNewName("_return");

		'''
			long «startVar» = System.nanoTime();
			«getParameterTypesArray(paramTypesVar, paramTypes)»
			
			«NetworkLatencyEvent.simpleName» «eventVar» = null;
			try {
				«eventVar» = new «NetworkLatencyEvent.simpleName»(«Joiner.on(", ").join(eventParams)»);
			} catch(Exception «eVar») { 
				throw new RuntimeException(«eVar»);
			}
			«IF (isVoid)»
				this.«referenceVar».«methodName»(«eventVar»«params»);
			«ELSE»
				«NetworkLatencyEvent.simpleName» «_return» = new «NetworkLatencyEvent.simpleName»(
					this.«referenceVar».«methodName»(«eventVar»«params»), 
					System.nanoTime()
				);
				«producerVar».produce(«_return»);
				
				return («methodReturn») «_return».methodReturn();
			«ENDIF»
		'''
	}

	/**
	 * Generates the body for methods within the final adapter (where the measurement ends, and the return 
	 * measurement starts) for a modified interface, measuring network latency data.
	 */
	def static String finalAdapterMethod(String endVar, String eventVar, String newEventVar, boolean isVoid,
		String referenceVar, String producerVar, String methodName, Collection<String> paramNames) {
		
		val varNames = new NameProposal(paramNames)
		
		val _return = if(!isVoid) varNames.getNewName("_return");
		val _returnEvent = if(!isVoid) varNames.getNewName("_returnEvent");

		'''
			long «endVar» = System.nanoTime();
			«NetworkLatencyEvent.simpleName» «newEventVar» = 
				new «NetworkLatencyEvent.simpleName» («eventVar», «endVar»);
			«producerVar».produce(«newEventVar»);
			
			«IF (isVoid)»
				this.«referenceVar».«methodName»(«Joiner.on(", ").join(paramNames)»);
			«ELSE»
				Object «_return» = this.«referenceVar».«methodName»(«Joiner.on(", ").join(paramNames)»);
				
				«NetworkLatencyEvent.simpleName» «_returnEvent» = new «NetworkLatencyEvent.simpleName»(
					«eventVar».transactionId(),
					System.nanoTime(),
					«eventVar».methodCaller(),
					«eventVar».methodProvider(),
					«eventVar».method(),
					«eventVar».methodParameters(),
					«_return»
				);
				
				return «_returnEvent»;
			«ENDIF»
		'''
	}

	/**
	 * Produces a code block for initializing the message producer inside each adapter
	 */
	def static String getProducerInitialization(String producerVar, String exchange, String routingKey) {
		'''
			try {
				this.«producerVar» = new «RabbitMQProducer.simpleName»("«exchange»", "«routingKey»");
				this.«producerVar».acceptOnly(«NetworkLatencyEvent.simpleName».class);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		'''
	}

	/**
	 * Produces a class array initialization containing a given list of class objects
	 */
	def static String getParameterTypesArray(String arrayVar, Collection<String> paramTypes) {
		'''
			String[] «arrayVar» = new String[«paramTypes.size»];
			«FOR index : 0 ..< paramTypes.size»
				«arrayVar»[«index»] = «paramTypes.get(index)»;
			«ENDFOR»
		'''
	}

	/**
	 * Producer a code block for initializing the network probe
	 */
	def static String getProbeInitialization(String routingKey, Context context) {
		'''
			super("«routingKey»", «Context.simpleName».«context.name»);
		'''
	}

}