package pascani.lang.util;

import pascani.lang.Event;

/**
 * This interface is intended to be implemented by event handlers and external
 * services handling events
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public interface EventHandler {

	public void handle(Event<?> event);

}
