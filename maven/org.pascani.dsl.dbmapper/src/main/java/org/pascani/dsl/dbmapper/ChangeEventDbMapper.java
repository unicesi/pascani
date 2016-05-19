package org.pascani.dsl.dbmapper;

import org.osoa.sca.annotations.Scope;
import org.pascani.dsl.lib.PascaniRuntime;
import org.pascani.dsl.lib.events.ChangeEvent;

@Scope("COMPOSITE")
public class ChangeEventDbMapper implements Runnable {

	private final EventSerializer serializer;

	public ChangeEventDbMapper() throws Exception {
		String exchange = PascaniRuntime.getEnvironment()
				.get("namespaces_exchange");
		String routingKey = "#"; // Accept all ChangeEvent events
		DbInterface db = new RethinkdbMapper();
		this.serializer = new EventSerializer(exchange, routingKey,
				ChangeEvent.class, db);
	}

	public void run() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override public void run() {
				try {
					serializer.shutdown();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
