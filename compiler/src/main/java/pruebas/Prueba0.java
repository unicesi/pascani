package pruebas;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import pascani.lang.events.NetworkLatencyEvent;

public interface Prueba0 {

	public void sayHi(NetworkLatencyEvent event0, String name,
			List<Hashtable<Map<String, String>, Class<String>>> event);

	public NetworkLatencyEvent sayGoodBye(NetworkLatencyEvent event,
			String message);

}
