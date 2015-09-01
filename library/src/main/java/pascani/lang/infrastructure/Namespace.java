package pascani.lang.infrastructure;

import java.io.Serializable;

/**
 * TODO: documentation
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public interface Namespace {
	
	/**
	 * TODO: documentation - abstract?
	 * 
	 * @param variable
	 * @return
	 */
	public Serializable getVariable(String variable);
	
	/**
	 * TODO: documentation - abstract?
	 * 
	 * @param variable
	 * @param value
	 * @return
	 */
	public Serializable setVariable(String variable, Serializable value);

}
