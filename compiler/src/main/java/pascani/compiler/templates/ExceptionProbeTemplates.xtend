package pascani.compiler.templates

import pascani.compiler.util.NameProposal
import com.google.common.collect.Lists
import pascani.lang.events.ExceptionEvent
import pascani.lang.util.EventProducer

class ExceptionProbeTemplates {

	/**
	 * Initializes the {@link EventProducer} within the service interceptor
	 */
	def static String getProducerInitialization(String producerVar) {
		'''
			this.«producerVar» = new «EventProducer.simpleName»<«ExceptionEvent.simpleName»>(PascaniRuntime.Context.PROBE);
		'''
	}

	/**
	 * Generates the code to intercept service executions and caught {@link Exception} raised in the execution
	 */
	def static String getInterceptorMethodBody(String producerVar, String intentJointPointVar) {

		var names = Lists.newArrayList(producerVar, intentJointPointVar);

		val _return = new NameProposal("_return", names).newName;
		val cause = new NameProposal("cause", names).newName;
		val event = new NameProposal("event", names).newName;

		'''
			Object «_return» = null;
			try {
				«_return» = «intentJointPointVar».proceed();
			} catch(Throwable «cause») {
				«ExceptionEvent.simpleName» «event» = new «ExceptionEvent.simpleName»(
					UUID.randomUUID(),
					new Exception(«cause»),
					«intentJointPointVar».getMethod().getDeclaringClass(),
					«intentJointPointVar».getMethod().getName(),
					«intentJointPointVar».getMethod().getParameterTypes(),
					«intentJointPointVar».getArguments()
				);
				
				«producerVar».post(«event»);
				
				throw new Throwable(«cause»);
			}
			
			return «_return»;
		'''
	}

}