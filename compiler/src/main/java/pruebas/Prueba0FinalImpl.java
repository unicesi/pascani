package pruebas;

import pruebas.Prueba0;
import pascani.lang.events.NetworkLatencyEvent;
import pascani.lang.infrastructure.rabbitmq.EndPoint;
import java.util.List;
import java.util.ArrayList;
import pascani.lang.Event;
import pascani.lang.infrastructure.MessageProducer;
import pascani.lang.infrastructure.rabbitmq.RabbitMQProducer;
import java.util.Hashtable;
import java.util.Map;
public class Prueba0FinalImpl implements Prueba0 {

	private Prueba reference;
	private MessageProducer producer;

	Prueba0FinalImpl() {
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

	public void sayHi(NetworkLatencyEvent event0, String name,
			List<Hashtable<Map<String, String>, Class<String>>> event) {
		long end = System.nanoTime();
		NetworkLatencyEvent event00 = new NetworkLatencyEvent(event0, end);
		producer.produce(event00);
		this.reference.sayHi(name, event);
	}

	public NetworkLatencyEvent sayGoodBye(NetworkLatencyEvent event,
			String message) {
		long end = System.nanoTime();
		NetworkLatencyEvent event0 = new NetworkLatencyEvent(event, end);
		producer.produce(event0);
		Object _return = this.reference.sayGoodBye(message);
		NetworkLatencyEvent _returnEvent = new NetworkLatencyEvent(
				event.transactionId(), System.nanoTime(), event.methodCaller(),
				event.methodProvider(), _return, event.getMethodInformation(),
				event.getActualMethodParameters());
		return _returnEvent;
	}
}
