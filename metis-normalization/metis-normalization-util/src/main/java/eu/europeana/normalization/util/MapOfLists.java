package eu.europeana.normalization.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 11 de Abr de 2013
 */
public class MapOfLists<K, O> implements Serializable {

  private static final long serialVersionUID = 1;

  // Hashtable<K,Object[]> hashtable;
  private final Hashtable<K, ArrayList<O>> hashtable;
  private int listInitialCapacity = -1;

  /**
   * Creates a new instance of this class.
   */
  public MapOfLists() {
    hashtable = new Hashtable<>();
  }

  /**
   * Creates a new instance of this class.
   */
  public MapOfLists(int initialCapacity) {
    hashtable = new Hashtable<>(initialCapacity);
  }

  /**
   * Creates a new instance of this class.
   */
  public MapOfLists(int initialCapacity, int listInitialCapacity) {
    hashtable = new Hashtable<>(initialCapacity);
    this.listInitialCapacity = listInitialCapacity;
  }

// public MapOfObjectArrays(int initialCapacity, int loadFactor){
// hashtable=new IntHashtable<Object[]>(initialCapacity,loadFactor);
// }

  /**
   * @param key
   * @param value
   */
  public void put(K key, O value) {
    ArrayList<O> recs = hashtable.get(key);
    if (recs == null) {
      recs = new ArrayList<>(listInitialCapacity == -1 ? 1 : listInitialCapacity);
      recs.add(value);
      hashtable.put(key, recs);
    } else {
      recs.add(value);
    }
  }

  /**
   * @param key
   * @param values
   */
  public void putAll(K key, O... values) {
    ArrayList<O> recs = hashtable.get(key);
    if (recs == null) {
      recs = new ArrayList<>(listInitialCapacity == -1 ? 1 : listInitialCapacity);
      Collections.addAll(recs, values);
      hashtable.put(key, recs);
    } else {
      Collections.addAll(recs, values);
    }
  }

  /**
   * @param key
   * @param values
   */
  public void putAll(K key, Collection<O> values) {
    ArrayList<O> recs = hashtable.get(key);
    if (recs == null) {
      recs = new ArrayList<>(listInitialCapacity == -1 ? 1 : listInitialCapacity);
      recs.addAll(values);
      hashtable.put(key, recs);
    } else {
      recs.addAll(values);
    }
  }

  /**
   * @param map
   */
  public void putAll(Map<K, O> map) {
    for (Map.Entry<K, O> entry : map.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
  }

  /**
   * @param map
   */
  public void putAll(MapOfLists<K, O> map) {
    for (K entry : map.keySet()) {
      putAll(entry, map.get(entry));
    }
  }

  /**
   * @param key
   * @param value
   */
  public void remove(K key, O value) {
    ArrayList<O> recs = hashtable.get(key);
    if (recs != null) {
      recs.remove(value);
      if (recs.size() == 0) {
        hashtable.remove(key);
      }
    }
  }

  /**
   * @param key
   */
  public void remove(K key) {
    hashtable.remove(key);
  }

  /**
   * @return bool
   */
  public boolean containsKey(K key) {
    return hashtable.containsKey(key);
  }

  /**
   * @return list
   */
  public List<O> get(K key) {
    return hashtable.get(key);
  }

  /**
   * @return value
   */
  public O get(K key, int idx) {
    return hashtable.get(key).get(idx);
  }

  /**
   * @return keySet
   */
  public Set<K> keySet() {
    return hashtable.keySet();
  }

  /**
   * @return size
   */
  public int size() {
    return hashtable.size();
  }

  /**
   * @return size Of All Lists
   */
  public int sizeOfAllLists() {
    int total = 0;
    for (K key : hashtable.keySet()) {
      total += get(key).size();
    }
    return total;
  }

  /**
   * @return values Of All Lists
   */
  public List<O> valuesOfAllLists() {
    ArrayList<O> ret = new ArrayList<>(sizeOfAllLists());
    for (K key : hashtable.keySet()) {
      ret.addAll(get(key));
    }
    return ret;
  }

  /**
   * @param c
   */
  public void sortLists(Comparator<O> c) {
    for (K k : hashtable.keySet()) {
      hashtable.get(k).sort(c);
    }
  }

  /**
   *
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void sortLists() {
    for (K k : hashtable.keySet()) {
      Collections.sort((ArrayList) hashtable.get(k));
    }
  }

  /**
   *
   */
  @SuppressWarnings("unchecked")
  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder();
    @SuppressWarnings("rawtypes")
    ArrayList keys = new ArrayList(hashtable.keySet());
    Collections.sort(keys);
    for (K key : (List<K>) keys) {
      List<O> vals = get(key);
      buffer.append(key.toString()).append("(").append(vals.size()).append(")").append(":\n");
      for (O val : vals) {
        buffer.append("\t").append(val.toString()).append("\n");
      }
    }
    return buffer.toString();
  }

  /**
   * @param key
   * @param value
   */
  public void putIfNotExists(K key, O value) {
    ArrayList<O> recs = hashtable.get(key);
    if (recs == null) {
      recs = new ArrayList<>(listInitialCapacity == -1 ? 1 : listInitialCapacity);
      recs.add(value);
      hashtable.put(key, recs);
    } else if (!recs.contains(value)) {
      recs.add(value);
    }
  }

  public boolean contains(K key, O value) {
    ArrayList<O> recs = hashtable.get(key);
    return recs != null && recs.contains(value);
  }
}
