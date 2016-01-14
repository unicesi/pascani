package org.pascani.dsl.lib.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;

/**
 * Provides facilities to deserialize objects. This implementation is a fork of
 * {@link SerializationUtils}, removing incompatible use of generics for JDK 1.6
 * (<T> T).
 * 
 * @author Miguel Jiménez
 */
public class SerializationUtils6 {

	/**
	 * <p>
	 * Deserializes a single {@code Object} from an array of bytes.
	 * </p>
	 * 
	 * <p>
	 * If the call site incorrectly types the return value, a
	 * {@link ClassCastException} is thrown from the call site. Without Generics
	 * in this declaration, the call site must type cast and can cause the same
	 * ClassCastException. Note that in both cases, the ClassCastException is in
	 * the call site, not in this method.
	 * </p>
	 * 
	 * @param <T>
	 *            the object type to be deserialized
	 * @param objectData
	 *            the serialized object, must not be null
	 * @return the deserialized object
	 * @throws IllegalArgumentException
	 *             if {@code objectData} is {@code null}
	 * @throws SerializationException
	 *             (runtime) if the serialization fails
	 */
	public static Object deserialize(final byte[] objectData) {
		if (objectData == null) {
			throw new IllegalArgumentException("The byte[] must not be null");
		}
		return SerializationUtils6
				.deserialize(new ByteArrayInputStream(objectData));
	}

	/**
	 * <p>
	 * Deserializes an {@code Object} from the specified stream.
	 * </p>
	 * 
	 * <p>
	 * The stream will be closed once the object is written. This avoids the
	 * need for a finally clause, and maybe also exception handling, in the
	 * application code.
	 * </p>
	 * 
	 * <p>
	 * The stream passed in is not buffered internally within this method. This
	 * is the responsibility of your application if desired.
	 * </p>
	 * 
	 * <p>
	 * If the call site incorrectly types the return value, a
	 * {@link ClassCastException} is thrown from the call site. Without Generics
	 * in this declaration, the call site must type cast and can cause the same
	 * ClassCastException. Note that in both cases, the ClassCastException is in
	 * the call site, not in this method.
	 * </p>
	 * 
	 * @param inputStream
	 *            the serialized object input stream, must not be null
	 * @return the deserialized object
	 * @throws IllegalArgumentException
	 *             if {@code inputStream} is {@code null}
	 * @throws SerializationException
	 *             (runtime) if the serialization fails
	 */
	public static Object deserialize(final InputStream inputStream) {
		if (inputStream == null) {
			throw new IllegalArgumentException(
					"The InputStream must not be null");
		}
		ObjectInputStream in = null;
		try {
			// stream closed in the finally
			in = new ObjectInputStream(inputStream);
			return in.readObject();

		} catch (final ClassCastException ex) {
			throw new SerializationException(ex);
		} catch (final ClassNotFoundException ex) {
			throw new SerializationException(ex);
		} catch (final IOException ex) {
			throw new SerializationException(ex);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (final IOException ex) { // NOPMD
				// ignore close exception
			}
		}
	}

}
