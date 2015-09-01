package pascani.lang.events;

import java.io.Serializable;
import java.util.UUID;

import com.google.common.collect.Range;

import pascani.lang.Event;

public class ChangeEvent implements Event<Serializable> {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -4543044781516991891L;

	/**
	 * The universally unique identifier of this event
	 */
	private final UUID id;

	/**
	 * The universally unique identifier of the transaction of which this event
	 * is part
	 */
	private final UUID transactionId;

	/**
	 * The timestamp when the exception is caught, in nanoseconds
	 */
	private final long timestamp;

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

		this.timestamp = System.nanoTime();
		this.id = UUID.randomUUID();
		this.transactionId = transactionId;
		this.previousValue = previousValue;
		this.newValue = newValue;
		this.variable = variable;
	}

	public UUID identifier() {
		return this.id;
	}

	public UUID transactionId() {
		return this.transactionId;
	}
	
	public String variable(){
		return this.variable;
	}
	
	public Serializable previousValue(){
		return this.previousValue;
	}

	public Serializable value() {
		return this.newValue;
	}

	public boolean isInTimeWindow(final long start, final long end) {
		return Range.closed(start, end).contains(this.timestamp);
	}
	
	/**
	 * Returns the string representation of this event for logging purposes.
	 */
	@Override public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getCanonicalName() + "\t");
		sb.append(this.transactionId + "\t");
		sb.append(this.id + "\t");
		sb.append(this.variable + "\t");
		sb.append(this.previousValue + "\t");
		sb.append(this.newValue + "\t");
		sb.append(this.timestamp + "\t");

		return sb.toString();
	}

	/**
	 * The result is {@code -1} if {@code this} event was raised before
	 * {@code o}. If {@code this} was raised after {@code o}, the result is
	 * {@code 1}. Otherwise, the result is {@code 0}.
	 *
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(final Event<Serializable> o) {
		if (o != null && o instanceof ChangeEvent) {
			ChangeEvent other = (ChangeEvent) o;

			if (this.timestamp < other.timestamp) {
				return -1;
			} else if (this.timestamp > other.timestamp) {
				return 1;
			} else {
				return 0;
			}
		}

		return 0;
	}

}
