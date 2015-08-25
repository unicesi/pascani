package pruebas;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public interface Prueba {
	
	public void sayHi(String name, List<Hashtable<Map<String, String>, Class<String>>> event);
	
	public String sayGoodBye(String message);

}
