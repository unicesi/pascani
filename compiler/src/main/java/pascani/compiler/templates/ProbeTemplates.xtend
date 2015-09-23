package pascani.compiler.templates

import pascani.compiler.util.NameProposal
import com.google.common.collect.Lists
import pascani.lang.events.TimeLapseEvent
import pascani.lang.util.LocalEventProducer
import pascani.lang.Event
import java.util.List
import pascani.lang.events.ExceptionEvent
import pascani.lang.events.InvokeEvent
import pascani.lang.events.ReturnEvent
import pascani.lang.infrastructure.AbstractProducer
import pascani.lang.infrastructure.rabbitmq.RabbitMQProducer
import pascani.lang.infrastructure.rabbitmq.EndPoint
import pascani.lang.PascaniRuntime

class ProbeTemplates {

	/**
	 * Initializes the {@link EventProducer} within the service interceptor
	 */
	def static String getProducerInitialization(String producerVar) {
		'''
			this.«producerVar» = new «LocalEventProducer.simpleName»<«Event.simpleName»<?>>(PascaniRuntime.Context.PROBE);
		'''
	}

	def static String getInitializationContrib(String probeClass, String connectionURI, String probesExchange,
		String probeRoutingKey, boolean addProducer, List<Class<? extends Event<?>>> events) {
		'''
			try {
				«probeClass» probe = new «probeClass»();
				«IF addProducer»
					«EndPoint.simpleName» endPoint = new «EndPoint.simpleName»("«connectionURI»");
					«AbstractProducer.simpleName» producer = 
						new «RabbitMQProducer.simpleName»(endPoint, "«probesExchange»", "«probeRoutingKey»");
					
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

		val transactionVar = new NameProposal("transactionId", names).newName;
		val returnVar = new NameProposal("_return", names).newName;

		val invokeEVar = new NameProposal("invokeEvent", names).newName;
		val exceptionEVar = new NameProposal("exceptionEvent", names).newName;
		val timeLapseEVar = new NameProposal("timeLapseEvent", names).newName;
		val returnEVar = new NameProposal("returnEvent", names).newName;

		val causeVar = new NameProposal("cause", names).newName;
		val startVar = new NameProposal("start", names).newName;
		val endVar = new NameProposal("end", names).newName;

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