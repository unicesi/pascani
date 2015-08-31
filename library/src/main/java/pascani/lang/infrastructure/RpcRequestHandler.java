package pascani.lang.infrastructure;

import java.io.Serializable;

public interface RpcRequestHandler {

	public Serializable handle(RpcRequest request);
	
}
