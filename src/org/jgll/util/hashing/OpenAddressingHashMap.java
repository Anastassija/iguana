package org.jgll.util.hashing;

import java.util.Iterator;

import org.jgll.util.hashing.hashfunction.HashFunction;

/**
 * 
 * A hash map based on open addressing.
 * 
 * 
 * @author Ali Afroozeh
 *
 */
public class OpenAddressingHashMap<K, V> implements MultiHashMap<K, V> {
	
	private static final long serialVersionUID = 1L;
	
	private OpenAddressingHashSet<MapEntry<K, V>> set;

	public OpenAddressingHashMap(ExternalHashEquals<K> hasher) {
		set = new OpenAddressingHashSet<>(new MapEntryExternalHasher(hasher));
	}
	
	public OpenAddressingHashMap(int initialCapacity, ExternalHashEquals<K> hasher) {
		set = new OpenAddressingHashSet<>(initialCapacity, new MapEntryExternalHasher(hasher));
	}
	
	public V get(K key) {
		MapEntry<K, V> entry = set.get(new MapEntry<K, V>(key, null));
		if(entry != null) {
			return entry.getValue();
		}
		
		return null;
	}
	
	public V put(K key, V value) {
		MapEntry<K, V> add = set.add(new MapEntry<K, V>(key, value));
		if(add == null) {
			return null;
		}
		
		return add.v;
	}
	
	public int size() {
		return set.size();
	}
	
	public void clear() {
		set.clear();
	}
	
	public Iterator<K> keyIterator() {
		
		final Iterator<MapEntry<K, V>> it = set.iterator();
		
		return new Iterator<K>() {

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public K next() {
				return it.next().k;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	public Iterator<V> valueIterator() {
		
		final Iterator<MapEntry<K, V>> it = set.iterator();
		
		return new Iterator<V>() {

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public V next() {
				return it.next().v;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	public static class MapEntry<K, V> {
		
		private K k;
		private V v;
		
		public MapEntry(K k, V v) {
			this.k = k;
			this.v = v;
		}

		public K getKey() {
			return k;
		}

		public V getValue() {
			return v;
		}

		public V setValue(V value) {
			this.v = value;
			return v;
		}
				
		@Override
		public String toString() {
			return "(" + k.toString() + ", " + (v == null ? "" : v.toString()) + ")";
		}
	}
	
	public class MapEntryExternalHasher implements ExternalHashEquals<MapEntry<K, V>> {

		private static final long serialVersionUID = 1L;
		
		private ExternalHashEquals<K> hasher;

		public MapEntryExternalHasher(ExternalHashEquals<K> hasher) {
			this.hasher = hasher;
		}
		
		@Override
		public int hash(MapEntry<K, V> t, HashFunction f) {
			return hasher.hash(t.k, f);
		}

		@Override
		public boolean equals(MapEntry<K, V> e1, MapEntry<K, V> e2) {
			return hasher.equals(e1.k, e2.k);
		}
	}

	@Override
	public int getInitialCapacity() {
		return set.getInitialCapacity();
	}

	@Override
	public int getEnlargeCount() {
		return set.getEnlargeCount();
	}

	@Override
	public boolean isEmpty() {
		return set.isEmpty();
	}
}
