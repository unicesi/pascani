package pruebas;

import pruebas.Prueba;
import java.util.UUID;
import pascani.lang.events.NetworkLatencyEvent;
import pascani.lang.infrastructure.rabbitmq.EndPoint;
import java.util.List;
import java.util.ArrayList;
import pascani.lang.Event;
import pascani.lang.infrastructure.MessageProducer;
import pascani.lang.infrastructure.rabbitmq.RabbitMQProducer;
import java.util.Hashtable;
import java.util.Map;
public class Prueba0InitialImpl implements Prueba {

	private Prueba0 reference;
	private MessageProducer producer;

	Prueba0InitialImpl() {
		try {
			EndPoint endPoint = new EndPoint.Builder("localhost", 5672, "/")
					.build();
			List<Class<? extends Event<?>>> classes = new ArrayList<Class<? extends Event<?>>>();
			classes.add(NetworkLatencyEvent.class);
			this.producer = new RabbitMQProducer(endPoint, classes,
					"probes_exchange", "MonitorX.networkProbe", true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void sayHi(String name,
			List<Hashtable<Map<String, String>, Class<String>>> event) {
		long start = System.nanoTime();
		NetworkLatencyEvent event0 = null;
		try {
			event0 = new NetworkLatencyEvent(UUID.randomUUID(), start,
					Prueba.class, Prueba.class, null, this.getClass()
							.getMethod("sayHi", name.getClass(),
									event.getClass()), name, event);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		this.reference.sayHi(event0, name, event);
	}

	public String sayGoodBye(String message) {
		long start = System.nanoTime();
		NetworkLatencyEvent event = null;
		try {
			event = new NetworkLatencyEvent(UUID.randomUUID(), start,
					Prueba.class, Prueba.class, null, this.getClass()
							.getMethod("sayGoodBye", message.getClass()),
					message);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		NetworkLatencyEvent _return = new NetworkLatencyEvent(
				this.reference.sayGoodBye(event, message), System.nanoTime());
		producer.produce(_return);
		return (String) _return.getActualMethodReturn();
	}
}
