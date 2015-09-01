package pascani.lang.infrastructure;

import java.io.Serializable;

/**
 * @author Miguel Jiménez - Initial contribution and API
 */
public interface Namespace {

	/**
	 * Gets the current value of the specified variable. If the given variable
	 * name is not found, null is returned.
	 * 
	 * @param variable
	 *            The name of the variable
	 * @return the current value of the specified variable
	 */
	public Serializable getVariable(String variable);

	/**
	 * Updates the current value of the specified variable. If the given
	 * variable name is not found, null is returned.
	 * 
	 * @param variable
	 *            The name of the variable
	 * @param value
	 *            The new value
	 * @return the current variable's value after updating it. Notice that it
	 *         may be different than {@code value}
	 */
	public Serializable setVariable(String variable, Serializable value);

}
