package pascani.lang.util;


/**
 * This enumeration facilitates the use of cron expressions by allowing to
 * define them from a set of constants.
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public enum CronConstant {
	YEARLY("0 0 0 1 1 *"), ANNUALLY("0 0 0 1 1 *"), MONTHLY("0 0 0 1 * *"), WEEKLY(
			"0 0 0 * * 0"), DAILY("0 0 0 * * *"), HOURLY("0 0 * * * *"), MINUTELY(
			"0 * * * * *"), SECONDLY("* * * * * *");

	private String expression;

	CronConstant(String expression) {
		this.expression = expression;
	}

	public String expression() {
		return this.expression;
	}
}
