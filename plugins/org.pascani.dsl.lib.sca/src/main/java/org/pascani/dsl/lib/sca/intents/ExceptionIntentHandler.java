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
package org.pascani.dsl.lib.sca.intents;

import java.util.UUID;

import org.ow2.frascati.tinfi.api.IntentJoinPoint;
import org.pascani.dsl.lib.events.ExceptionEvent;

/**
 * @author Miguel Jiménez - Initial contribution and API
 */
public class ExceptionIntentHandler extends AbstractIntentHandler {

	public Object invoke(IntentJoinPoint ijp) throws Throwable {
		UUID transactionId = UUID.randomUUID();
		String[] parameterTypes = 
				new String[ijp.getMethod().getParameterTypes().length];
		for (int i = 0; i < parameterTypes.length; i++) {
			parameterTypes[i] = 
					ijp.getMethod().getParameterTypes()[i].getCanonicalName();
		}
		Object _return = null;
		try {
			_return = ijp.proceed();
		} catch (Throwable cause) {
			ExceptionEvent exceptionEvent = new ExceptionEvent(transactionId,
					new Exception(cause),
					ijp.getMethod().getDeclaringClass().getCanonicalName(),
					ijp.getMethod().getName(), parameterTypes);
			super.handler.handle(exceptionEvent);
			throw new Throwable(cause);
		}
		return _return;
	}

}
