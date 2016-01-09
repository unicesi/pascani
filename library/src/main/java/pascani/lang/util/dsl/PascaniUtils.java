package pascani.lang.util.dsl;

import pascani.lang.events.ExceptionEvent;
import pascani.lang.events.InvokeEvent;
import pascani.lang.events.ReturnEvent;
import pascani.lang.events.TimeLapseEvent;
import pascani.lang.infrastructure.ProbeProxy;

/**
 * @author Miguel Jiménez - Initial contribution and API
 */
public class PascaniUtils {
	
	/**
	 * <b>Note</b>: DSL-only intended use
	 * <p>
	 * Introduces a new monitor probe, and returns a proxy pointing to it. The
	 * probe created manages events of type {@link ExceptionEvent}.
	 * 
	 * @param uniqueName
	 *            A unique name representing the monitor probe
	 * @return a {@link ProbeProxy} instance pointing to an exception probe
	 */
	public static ProbeProxy newExceptionProbe(String uniqueName) {
		return createProbeProxy(uniqueName);
	}

	/**
	 * <b>Note</b>: DSL-only intended use
	 * <p>
	 * Introduces a new monitor probe, and returns a proxy pointing to it. The
	 * probe created manages event of type {@link TimeLapseEvent},
	 * {@link InvokeEvent}, and {@link ReturnEvent}
	 * 
	 * @param uniqueName
	 *            A unique name representing the monitor probe
	 * @return a {@link ProbeProxy} instance pointing to a performance probe
	 */
	public static ProbeProxy newPerformanceProbe(String uniqueName) {
		return createProbeProxy(uniqueName);
	}
	
	private static ProbeProxy createProbeProxy(String routingKey) {
		try {
			return new ProbeProxy(routingKey);
		} catch (Exception e) {
			// TODO: log the exception
			e.printStackTrace();
		}
		return null;
	}

}
