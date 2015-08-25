package pruebas;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;

import pascani.compiler.NetworkLatencyGenerator;

public class TestNetworkLatencyMeasurement {

	public static void main(String[] args) throws InstantiationException,
			IllegalAccessException, IOException {
		
		String path = "/Users/migueljimenez/Desarrollo/eclipse-dsl/Pascani/monitors/compiler/src/main/java/pruebas/Prueba.java";
		String modified = "/Users/migueljimenez/Desarrollo/eclipse-dsl/Pascani/monitors/compiler/src/main/java/pruebas/Prueba0.java";
		String adapter1 = "/Users/migueljimenez/Desarrollo/eclipse-dsl/Pascani/monitors/compiler/src/main/java/pruebas/Prueba0InitialImpl.java";
		String adapter2 = "/Users/migueljimenez/Desarrollo/eclipse-dsl/Pascani/monitors/compiler/src/main/java/pruebas/Prueba0FinalImpl.java";

		// Avoid naming problems
		new File(modified).delete();
		new File(adapter1).delete();
		new File(adapter2).delete();

		NetworkLatencyGenerator g = new NetworkLatencyGenerator(path,
				"localhost", 5672, "/", "probes_exchange",
				"MonitorX.networkProbe", true);

		// Generate files
		JavaInterfaceSource duplicated = g.modifiedInterface();
		JavaClassSource initialAdapter = g.initialAdapter(duplicated);
		JavaClassSource finalAdapter = g.finalAdapter(duplicated);

		FileUtils.writeLines(new File(modified),
				Arrays.asList(duplicated.toString().split("\n")));

		FileUtils.writeLines(new File(adapter1),
				Arrays.asList(initialAdapter.toString().split("\n")));

		FileUtils.writeLines(new File(adapter2),
				Arrays.asList(finalAdapter.toString().split("\n")));
	}

}
