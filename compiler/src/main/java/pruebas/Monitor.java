package pruebas;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import pascani.lang.events.ExceptionEvent;
import pascani.lang.infrastructure.ProbeProxy;
import pascani.lang.infrastructure.RpcClient;
import pascani.lang.infrastructure.rabbitmq.EndPoint;
import pascani.lang.infrastructure.rabbitmq.RabbitMQRpcClient;
import pascani.lang.monitors.AbstractMonitor;

public class Monitor extends AbstractMonitor {
	
	private ProbeProxy<ExceptionEvent> customProbe;
	
	public Monitor() throws IOException, TimeoutException {
		EndPoint endPoint = new EndPoint.Builder("localhost", 5672, "/").build();
		RpcClient client = new RabbitMQRpcClient(endPoint, "rpc_exchange", "custom-performance-1");
		customProbe = new ProbeProxy<ExceptionEvent>(client);
	}
	
	public List<ExceptionEvent> fetch(long timestamp){
		return this.customProbe.fetch(timestamp);
	}

}
