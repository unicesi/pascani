package pascani.lang.events;

import java.io.Serializable;
import java.util.UUID;

import pascani.lang.Event;

public class ChangeEvent extends Event<Serializable> {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -4543044781516991891L;

	/**
	 * The value of the variable before the change
	 */
	private final Serializable previousValue;

	/**
	 * The new value of the variable
	 */
	private final Serializable newValue;

	/**
	 * The variable whose value has changed
	 */
	private final String variable;

	/**
	 * @param transactionId
	 *            The universally unique identifier of the transaction of which
	 *            this event is part
	 * @param previousValue
	 *            The value of the variable before the change
	 * @param newValue
	 *            The new value of the variable
	 * @param variable
	 *            The variable whose value has changed
	 */
	public ChangeEvent(final UUID transactionId,
			final Serializable previousValue, final Serializable newValue,
			final String variable) {
		super(transactionId);
		this.previousValue = previousValue;
		this.newValue = newValue;
		this.variable = variable;
	}

	public String variable() {
		return this.variable;
	}

	public Serializable previousValue() {
		return this.previousValue;
	}

	@Override public Serializable value() {
		return this.newValue;
	}

	/**
	 * Returns the string representation of this event for logging purposes.
	 */
	@Override public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getCanonicalName() + "\t");
		sb.append(this.transactionId + "\t");
		sb.append(this.identifier + "\t");
		sb.append(this.variable + "\t");
		sb.append(this.previousValue + "\t");
		sb.append(this.newValue + "\t");
		sb.append(this.timestamp + "\t");

		return sb.toString();
	}

}
