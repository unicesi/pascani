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
package org.pascani.dsl.lib.util;

import org.pascani.dsl.lib.util.Exceptions;
import org.quartz.CronExpression;

/**
 * This enumeration facilitates the use of cron expressions by allowing to
 * define them from a set of constants.
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public enum CronConstant {
	YEARLY("0 0 0 1 1 ?"), ANNUALLY("0 0 0 1 1 ?"), MONTHLY("0 0 0 1 * ?"), WEEKLY(
			"0 0 0 ? * 1"), DAILY("0 0 0 * * ?"), HOURLY("0 0 * * * ?"), MINUTELY(
			"0 * * * * ?"), SECONDLY("* * * * * ?");

	private String expression;

	CronConstant(String expression) {
		this.expression = expression;
	}

	public CronExpression expression() {
		return Exceptions.sneakyInitializer(CronExpression.class,
				this.expression);
	}
}
