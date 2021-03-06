package org.pascani.dsl.lib.util;

import java.lang.reflect.Constructor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Useful helper methods when dealing with exceptions.
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class Exceptions {
	
	/**
	 * The logger
	 */
	protected final static Logger logger = LogManager.getLogger(Exceptions.class);

	/**
	 * Creates an instance of the given class T. This is useful to initialize
	 * objects without worrying about unhandled exceptions.
	 * 
	 * @param clazz
	 *            The class of the object to initialize
	 * @param arguments
	 *            The arguments that are passed to the constructor (also their
	 *            types are used to select the adequate constructor)
	 * @return a new instance of type T, initialized with the given arguments
	 */
	public static <T> T sneakyInitializer(Class<T> clazz, Object... arguments) {
		Class<?>[] parameterTypes = new Class<?>[arguments.length];
		for (int i = 0; i < arguments.length; i++) {
			parameterTypes[i] = arguments[i].getClass();
		}
		try {
			Constructor<T> constructor = clazz.getConstructor(parameterTypes);
			return constructor.newInstance(arguments);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * Shamelessly taken from Project Lombok
	 * https://github.com/rzwitserloot/lombok/blob/master/src/core/lombok/Lombok.java
	 */
	/**
	 * Throws the given exception and sneaks it through any compiler checks.
	 * This allows to throw checked exceptions without the need to declare it.
	 * Clients should use the following idiom to trick static analysis and dead
	 * code checks:
	 * 
	 * <pre>
	 * throw sneakyThrow(new CheckedException("Catch me if you can ;-)")).
	 * </pre>
	 * 
	 * This method is heavily inspired by project 
	 * <a href="https://github.com/rzwitserloot/lombok/blob/master/src/core/lombok/Lombok.java">Lombok</a>.
	 * 
	 * @param t
	 *            The throwable that should be sneaked through compiler checks.
	 *            May not be <code>null</code>.
	 * @return never returns anything since {@code t} is always thrown.
	 * @throws NullPointerException
	 *             if {@code t} is <code>null</code>.
	 */
	public static RuntimeException sneakyThrow(Throwable t) {
		if (t == null)
			throw new NullPointerException("t");
		logger.error(t.getMessage(), t);
		Exceptions.<RuntimeException> sneakyThrow0(t);
		return null;
	}

	@SuppressWarnings("unchecked") 
	private static <T extends Throwable> void sneakyThrow0(Throwable t) throws T {
		throw (T) t;
	}

}
