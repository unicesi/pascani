package org.pascani.dsl.lib.util;

import java.io.Serializable;

import com.google.common.base.Objects;

/**
 * @deprecated Use {@link org.eclipse.xtext.xbase.lib.Pair} instead
 * <p>
 * Adapted from {@link org.eclipse.xtext.xbase.lib.Pair}. This implementation
 * makes the original class serializable.
 * 
 * An immutable pair of {@link #getKey() key} and {@link #getValue() value}. A
 * pair is considered to be {@link #equals(Object) equal} to another pair if
 * both the key and the value are equal.
 * 
 * @param <K>
 *            The key-type of the pair.
 * @param <V>
 *            The value-type of the pair.
 * 
 * @license http://www.eclipse.org/legal/epl-v10.html
 * @copyright 2011 itemis AG (http://www.itemis.eu) and others
 * @author Sebastian Zarnekow - Initial contribution and API of XBase
 * 
 * @contributor Miguel Jiménez
 */
@Deprecated
public final class Pair<K, V> implements Serializable {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 4463967427771864536L;

	private final K k;
	private final V v;

	/**
	 * Creates a new instance with the given key and value. May be used instead
	 * of the constructor for convenience reasons.
	 * 
	 * @param k
	 *            The key. May be <code>null</code>.
	 * @param v
	 *            The value. May be <code>null</code>.
	 * @return a newly created pair. Never <code>null</code>.
	 */
	public static <K, V> Pair<K, V> of(K k, V v) {
		return new Pair<K, V>(k, v);
	}

	/**
	 * Creates a new instance with the given key and value.
	 * 
	 * @param k
	 *            The key. May be <code>null</code>.
	 * @param v
	 *            The value. May be <code>null</code>.
	 * 
	 */
	public Pair(K k, V v) {
		this.k = k;
		this.v = v;
	}

	/**
	 * Returns the key.
	 * 
	 * @return the key.
	 */
	public K getKey() {
		return k;
	}

	/**
	 * Returns the value.
	 * 
	 * @return the value.
	 */
	public V getValue() {
		return v;
	}

	@Override public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof Pair))
			return false;
		Pair<?, ?> e = (Pair<?, ?>) o;
		return Objects.equal(k, e.getKey()) && Objects.equal(v, e.getValue());
	}

	@Override public int hashCode() {
		return ((k == null) ? 0 : k.hashCode())
				^ ((v == null) ? 0 : v.hashCode());
	}

	@Override public String toString() {
		return k + "->" + v;
	}

}
