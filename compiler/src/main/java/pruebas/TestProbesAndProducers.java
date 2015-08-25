package pruebas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import pascani.lang.Event;
import pascani.lang.Probe;
import pascani.lang.events.ExceptionEvent;
import pascani.lang.events.NetworkLatencyEvent;
import pascani.lang.events.TimeLapseEvent;
import pascani.lang.infrastructure.BasicProbe;
import pascani.lang.infrastructure.RpcServer;
import pascani.lang.infrastructure.rabbitmq.EndPoint;
import pascani.lang.infrastructure.rabbitmq.RabbitMQProducer;
import pascani.lang.infrastructure.rabbitmq.RabbitMQRpcServer;
import pascani.lang.util.EventProducer;

public class TestProbesAndProducers {

	public static void main(String[] args) throws InterruptedException,
			IOException, TimeoutException {

		System.out.println("Creating RabbitMQ end point");

		final EndPoint endPoint = new EndPoint.Builder("localhost", 5672, "/").build();

		System.out.println("Initializing RPC server for the probe");

		RpcServer server = new RabbitMQRpcServer(endPoint, "rpc_queue");
		final Probe<ExceptionEvent> probe = new BasicProbe<ExceptionEvent>(
				server);

		System.out.println("Subscribing listeners");

		pascani.lang.Runtime runtime = pascani.lang.Runtime
				.getRuntimeInstance(pascani.lang.Runtime.Context.LIBRARY);

		List<Class<? extends Event<?>>> classes = new ArrayList<Class<? extends Event<?>>>();
		classes.add(TimeLapseEvent.class);
		classes.add(NetworkLatencyEvent.class);
		classes.add(ExceptionEvent.class);

		// Register the RabbitMQ producer
		RabbitMQProducer RMQproducer = new RabbitMQProducer(endPoint, classes,
				"exchange", "", true);

		runtime.registerEventListener(RMQproducer);
		runtime.registerEventListener(probe);
		
		System.out.println("Sending messages");
		
		EventProducer<ExceptionEvent> producer = new EventProducer<ExceptionEvent>(
				pascani.lang.Runtime.Context.LIBRARY);
		
		UUID transactionId = UUID.randomUUID();
		
		producer.post(new ExceptionEvent(transactionId, new Exception("Prueba de exception 1")));
		producer.post(new ExceptionEvent(transactionId, new Exception("Prueba de exception 2")));

		long timestamp = System.nanoTime();

		producer.post(new ExceptionEvent(transactionId, new Exception("Prueba de exception 3")));
		producer.post(new ExceptionEvent(transactionId, new Exception("Prueba de exception 4")));
		producer.post(new ExceptionEvent(transactionId, new Exception("Prueba de exception 5")));
		producer.post(new ExceptionEvent(transactionId, new Exception("Prueba de exception 6")));

		System.out.println("Enviados! \nFILTRO:");

		probe.fetch(timestamp);
	}

}
