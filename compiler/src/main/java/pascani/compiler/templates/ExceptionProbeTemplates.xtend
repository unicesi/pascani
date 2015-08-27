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

import com.google.common.collect.Lists
import pascani.compiler.util.NameProposal
import pascani.lang.events.ExceptionEvent
import pascani.lang.util.EventProducer

class ExceptionProbeTemplates {
	
	def static String getProducerInitialization(String producerVar) {
		'''
			this.«producerVar» = new «EventProducer.simpleName»<«ExceptionEvent.simpleName»>();
		'''
	}
	
	def static String getInterceptorMethodBody(String producerVar, String intentJointPointVar) {
		
		var names = Lists.newArrayList(producerVar, intentJointPointVar);
		val _return = new NameProposal("_return", names).newName;
		names.add(_return);
		
		val cause = new NameProposal("cause", names).newName;
		names.add(cause);
		
		val event = new NameProposal("event", names).newName;
		names.add(event);
		
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