package pascani.compiler.templates

import pascani.compiler.util.NameProposal
import pascani.lang.util.EventProducer
import com.google.common.collect.Lists
import pascani.lang.events.TimeLapseEvent

class TimeLapseProbeTemplates {

	/**
	 * Initializes the {@link EventProducer} within the service interceptor
	 */
	def static String getProducerInitialization(String producerVar) {
		'''
			this.«producerVar» = new «EventProducer.simpleName»<«TimeLapseEvent.simpleName»>(PascaniRuntime.Context.PROBE);
		'''
	}

	/**
	 * Generates the code to intercept service executions and measure the execution time
	 */
	def static String getInterceptorMethodBody(String producerVar, String intentJointPointVar) {

		var names = Lists.newArrayList(producerVar, intentJointPointVar);

		val _return = new NameProposal("_return", names).newName;
		val event = new NameProposal("event", names).newName;
		val start = new NameProposal("start", names).newName;
		val end = new NameProposal("end", names).newName;

		'''
			long «start» = System.nanoTime();
			Object «_return» = «intentJointPointVar».proceed();
			long «end» = System.nanoTime();
			
			«TimeLapseEvent.simpleName» «event» = new «TimeLapseEvent.simpleName»(UUID.randomUUID(), «start», «end»);
			
			«producerVar».post(«event»);
			return «_return»;
		'''
	}

}