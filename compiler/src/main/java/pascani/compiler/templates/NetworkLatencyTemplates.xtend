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
 * along with The SLR Support Tools. If not, see <http://www.gnu.org/licenses/>.
 */
package pascani.compiler.templates

import pascani.lang.events.NetworkLatencyEvent
import com.google.common.base.Joiner
import java.util.List
import java.util.Collection
import pascani.compiler.util.FilenameProposal
import pascani.lang.infrastructure.rabbitmq.RabbitMQProducer
import pascani.lang.Event

class NetworkLatencyTemplates {

	/**
	 * Generates the body for methods within the initial adapter (where the measurement starts, and the 
	 * return measurement ends) for a modified interface, measuring network latency data.
	 */
	def static String initialAdapterMethod(String startVar, String eventVar, List<String> eventParams, String eVar,
		boolean isVoid, String referenceVar, String producerVar, String methodName, String methodReturn, 
		Collection<String> paramNames) {

		var params = if(paramNames.size > 0) ", " + Joiner.on(", ").join(paramNames) else "";
		val _return = if(!isVoid) new FilenameProposal("_return", paramNames).newName;

		'''
			long «startVar» = System.nanoTime();
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
				
				return («methodReturn») «_return».getActualMethodReturn();
			«ENDIF»
		'''
	}

	/**
	 * Generates the body for methods within the final adapter (where the measurement ends, and the return 
	 * measurement starts) for a modified interface, measuring network latency data.
	 */
	def static String finalAdapterMethod(String endVar, String eventVar, String newEventVar, boolean isVoid,
		String referenceVar, String producerVar, String methodName, Collection<String> paramNames) {

		val _return = if(!isVoid) new FilenameProposal("_return", paramNames).newName;
		val _returnEvent = if(!isVoid) new FilenameProposal("_returnEvent", paramNames).newName;

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
					«_return»,
					«eventVar».getMethodInformation(),
					«eventVar».getActualMethodParameters()
				);
				
				return «_returnEvent»;
			«ENDIF»
		'''
	}

	/**
	 * Simple template for getting a method based on the name and the parameters types
	 */
	def static String getMethod(String name, Collection<String> paramTypes) '''
		this.getClass().getMethod("«name»", «Joiner.on(", ").join(paramTypes)»)
	'''

	/**
	 * Produces a code block for initializing the message producer inside each adapter
	 */
	def static String getProducerInitialization(String producerVar, String host, int port, 
		String virtualHost, String exchange, String routingKey, boolean durableExchange) {
		'''
			try {
				EndPoint endPoint = new EndPoint.Builder("«host»", «port», "«virtualHost»").build();
				
				List<Class<? extends «Event.simpleName»<?>>> classes = 
					new ArrayList<Class<? extends «Event.simpleName»<?>>>();
				classes.add(«NetworkLatencyEvent.simpleName».class);
				
				this.«producerVar» = new «RabbitMQProducer.simpleName»(endPoint, classes, 
					"«exchange»", "«routingKey»", «durableExchange»);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		'''
	}

}