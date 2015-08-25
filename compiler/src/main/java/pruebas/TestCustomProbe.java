package pruebas;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import pascani.lang.events.ExceptionEvent;
import pascani.lang.infrastructure.CustomProbe;

public class TestCustomProbe {

	public static void main(String[] args) throws IOException, TimeoutException {

		CustomProbe<ExceptionEvent> probe = new CustomProbe<ExceptionEvent>(
				"localhost", 5672, "/", "guest", "guest",
				"custom-performance-1");
		
		UUID transactionId = UUID.randomUUID();

		probe.recordEvent(new ExceptionEvent(transactionId, new Exception("Error 1")));
		probe.recordEvent(new ExceptionEvent(transactionId, new Exception("Error 2")));
		probe.recordEvent(new ExceptionEvent(transactionId, new Exception("Error 3")));

		long timestamp = System.nanoTime();

		probe.recordEvent(new ExceptionEvent(transactionId, new Exception("Error 4")));
		probe.recordEvent(new ExceptionEvent(transactionId, new Exception("Error 5")));
		probe.recordEvent(new ExceptionEvent(transactionId, new Exception("Error 6")));

		probe.fetch(timestamp);
		
		Monitor monitor = new Monitor();
		
		System.out.println(monitor.fetch(timestamp));
	}

}
