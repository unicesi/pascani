package pascani.lang.util;

import java.util.Observer;

import pascani.lang.Event;

public abstract class EventObserver<T extends Event<?>> implements Observer {
	
	public abstract void execute(T e);
	
}
