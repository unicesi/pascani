package pascani.lang.infrastructure;

import java.io.Serializable;

/**
 * TODO: documentation
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public interface RpcRequestHandler {

	/**
	 * TODO: documentation
	 * 
	 * @param request
	 * @return
	 */
	public Serializable handle(RpcRequest request);
	
}
