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
 * instances of an specific class implementing {@link Event}. This is done by
 * performing a non safe cast in a safe way, that is:
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

	private final Class<? extends Event<?>> subType;

	public EventFilter(Collection<Event<?>> events,
			Class<? extends Event<?>> subType) {
		this.events = events;
		this.subType = subType;
	}

	public EventFilter(Class<? extends Event<?>> subType, Event<?>... events) {
		this(Lists.newArrayList(events), subType);
	}

	/**
	 * @return a list containing only instances of the specified class
	 */
	public <T> List<T> filter() {
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
