package pascani.lang.util;

import java.util.Collection;
import java.util.List;

import pascani.lang.Event;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

/**
 * This class filters a collection of {@link Event} instances by keeping only
 * instances of an specific class. This is done by performing a non safe cast in
 * a safe way, that is:
 * 
 * <ol>
 * <li>The collection is filtered keeping the elements of interest (instances of
 * the specific class)</li>
 * <li>Once the collection is filtered, a cast is done from {@link Event} to the
 * specified class.</li>
 * </ol>
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class EventFilter {

	private final Collection<Event<?>> events;

	public EventFilter(Collection<Event<?>> events) {
		this.events = events;
	}

	public EventFilter(Event<?>... events) {
		this(Lists.newArrayList(events));
	}
	
	/**
	 * @return a collection containing the unfiltered events
	 */
	public Collection<Event<?>> unfilteredEvents() {
		return this.events;
	}

	/**
	 * @return a list containing only instances of the specified class
	 */
	public <T extends Event<?>> List<T> filter(final Class<T> subType) {
		Collection<Event<?>> filtered = Collections2.filter(this.events,
				new Predicate<Event<?>>() {
					public boolean apply(Event<?> event) {
						return subType.isInstance(event);
					}
				});

		Collection<T> transformed = Collections2.transform(filtered,
				new Function<Event<?>, T>() {
					@SuppressWarnings("unchecked") public T apply(Event<?> event) {
						return (T) event;
					}
				});

		return Lists.newArrayList(transformed);
	}

}
