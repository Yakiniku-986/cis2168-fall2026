package tuj.cis2168.study.hashing;

import java.util.Iterator;
import java.util.NoSuchElementException;

/// Study guide: hash table with LINEAR PROBING (open addressing)
/// (see study/04-hash-tables.md).
///
/// No linked lists: everything lives directly in the array. On a collision,
/// try the NEXT slot, then the next, wrapping around:
///
///   probe sequence: h, h+1, h+2, ...   (all mod table.length)
///
///   load factor L = size / length MUST stay < 1 (the array can fill up).
///   Performance drops fast as L grows (clusters form), so we resize at 0.5.
///
/// REMOVE is the tricky part. If you just set a slot back to null, a later
/// search stops at that hole and misses keys that probed PAST it. Fix: leave
/// a TOMBSTONE ("deleted" marker): searches skip over it, inserts may reuse it.
///
/// Also here:
///   * removeBroken(): the WRONG remove, to see the bug happen
///   * get() written iteratively and recursively
///   * LinearProbingHashSet: the set version
///   * notes on quadratic probing and double hashing
///
/// Run:  java src/tuj/cis2168/study/hashing/LinearProbingHashMap.java
public class LinearProbingHashMap<K, V> implements Iterable<K> {

  private Object[] keys;
  private Object[] values;
  private int size;          // live entries
  private int used;          // live entries + tombstones (they also block probes)
  private static final Object TOMBSTONE = new Object();
  private static final double MAX_LOAD = 0.5;
  boolean trace = false;

  public LinearProbingHashMap(int capacity) {
    keys = new Object[capacity];
    values = new Object[capacity];
  }

  public int size() { return size; }

  private int hash(Object key) { return Math.floorMod(key.hashCode(), keys.length); }

  // ------------------------------------------------------------------ get
  @SuppressWarnings("unchecked")
  public V get(K key) {
    int i = hash(key);
    while (keys[i] != null) {                  // stop only at a truly EMPTY slot
      if (keys[i] != TOMBSTONE && keys[i].equals(key)) { return (V) values[i]; }
      i = (i + 1) % keys.length;               // linear probe: next slot, wrap around
    }
    return null;
  }

  /// Same search, recursively: "look here; if not here, look at the next slot".
  /// `steps` guards against looping forever in a completely full table.
  @SuppressWarnings("unchecked")
  public V getRec(K key, int i, int steps) {
    if (keys[i] == null || steps == keys.length) { return null; }      // base: empty slot
    if (keys[i] != TOMBSTONE && keys[i].equals(key)) { return (V) values[i]; } // base: found
    return getRec(key, (i + 1) % keys.length, steps + 1);
  }

  public boolean containsKey(K key) { return getRec(key, hash(key), 0) != null; }

  // ------------------------------------------------------------------ put
  @SuppressWarnings("unchecked")
  public V put(K key, V value) {
    if ((double) (used + 1) / keys.length > MAX_LOAD) { resize(); }
    int i = hash(key), home = i, firstTombstone = -1;
    StringBuilder path = new StringBuilder();
    while (keys[i] != null) {
      path.append(i).append(' ');
      if (keys[i] == TOMBSTONE) {
        if (firstTombstone < 0) { firstTombstone = i; }   // remember, keep looking
      } else if (keys[i].equals(key)) {                   // update existing key
        V old = (V) values[i];
        values[i] = value;
        return old;
      }
      i = (i + 1) % keys.length;
    }
    if (firstTombstone >= 0) { i = firstTombstone; } else { used++; }  // reuse a tombstone
    keys[i] = key;
    values[i] = value;
    size++;
    if (trace) {
      System.out.printf("  put %-4s hash=%2d  %s-> slot %d%n", key, home,
          path.length() > 0 ? "occupied: " + path : "", i);
    }
    return null;
  }

  // --------------------------------------------------------------- remove
  @SuppressWarnings("unchecked")
  public V remove(K key) {
    int i = hash(key);
    while (keys[i] != null) {
      if (keys[i] != TOMBSTONE && keys[i].equals(key)) {
        V old = (V) values[i];
        keys[i] = TOMBSTONE;                  // NOT null!
        values[i] = null;
        size--;
        return old;
      }
      i = (i + 1) % keys.length;
    }
    return null;
  }

  /// WRONG on purpose: sets the slot to null, which breaks later searches.
  public void removeBroken(K key) {
    int i = hash(key);
    while (keys[i] != null) {
      if (keys[i].equals(key)) {
        keys[i] = null;
        values[i] = null;
        size--;
        used--;
        return;
      }
      i = (i + 1) % keys.length;
    }
  }

  // --------------------------------------------------------------- resize
  /// Double and re-insert every LIVE entry (tombstones are dropped).
  @SuppressWarnings("unchecked")
  private void resize() {
    Object[] oldKeys = keys, oldValues = values;
    keys = new Object[oldKeys.length * 2];
    values = new Object[oldKeys.length * 2];
    size = 0;
    used = 0;
    boolean t = trace;
    trace = false;
    for (int i = 0; i < oldKeys.length; i++) {
      if (oldKeys[i] != null && oldKeys[i] != TOMBSTONE) { put((K) oldKeys[i], (V) oldValues[i]); }
    }
    trace = t;
    if (trace) { System.out.println("  ** load would pass " + MAX_LOAD + ": resized " + oldKeys.length + " -> " + keys.length + " and re-inserted"); }
  }

  // ------------------------------------------------------------- iterator
  /// Scan the array, skipping empty slots and tombstones.
  @SuppressWarnings("unchecked")
  @Override
  public Iterator<K> iterator() {
    return new Iterator<K>() {
      int i = skip(0);

      private int skip(int from) {
        while (from < keys.length && (keys[from] == null || keys[from] == TOMBSTONE)) { from++; }
        return from;
      }

      public boolean hasNext() { return i < keys.length; }

      public K next() {
        if (!hasNext()) { throw new NoSuchElementException(); }
        K k = (K) keys[i];
        i = skip(i + 1);
        return k;
      }
    };
  }

  public String dump() { return dump(keys.length); }

  /// Show slots 0 .. limit-1 ( _ = empty, X = tombstone ).
  public String dump(int limit) {
    StringBuilder sb = new StringBuilder("  ");
    for (int i = 0; i < Math.min(limit, keys.length); i++) {
      String s = keys[i] == null ? "_" : keys[i] == TOMBSTONE ? "X" : keys[i].toString();
      sb.append(i).append(':').append(s).append("  ");
    }
    if (limit < keys.length) { sb.append("..."); }
    return sb.toString();
  }

  // =========================================================================
  // Set version
  // =========================================================================

  static class LinearProbingHashSet<E> {
    private final LinearProbingHashMap<E, Boolean> map = new LinearProbingHashMap<>(8);

    boolean add(E e) { return map.put(e, Boolean.TRUE) == null; }
    boolean contains(E e) { return map.containsKey(e); }
    boolean remove(E e) { return map.remove(e) != null; }
    int size() { return map.size(); }
  }

  // =========================================================================
  // Other probe sequences (for reference; same put/get/remove shape):
  //   linear    : h + i               -> primary clustering (long runs)
  //   quadratic : h + i*i             -> jumps 1, 4, 9 ... ; table size should
  //                                      be prime and L < 0.5 to always find a slot
  //   double    : h + i * h2(key)     -> step size depends on the key,
  //               e.g. h2 = 7 - (hashCode % 7), never 0
  // =========================================================================

  public static void main(String[] args) {
    // Integer.hashCode() is the int itself, so hash = key % 23: easy to trace by hand.
    LinearProbingHashMap<Integer, String> m = new LinearProbingHashMap<>(23);
    m.trace = true;
    System.out.println("capacity 23, hash(k) = k % 23. Insert 5, 28, 51, 7, 6, 74:");
    int[] ks = {5, 28, 51, 7, 6, 74};
    for (int k : ks) { m.put(k, "v" + k); }
    System.out.println(m.dump(12));
    System.out.println("  (5, 28, 51, 74 all hash to 5 and form a CLUSTER; 6 and 7 got pushed along too)");

    System.out.println("\nget(74) = " + m.get(74) + "   getRec(51) = " + m.getRec(51, Math.floorMod(51, 23), 0));

    System.out.println("\nremove(28) with a TOMBSTONE:");
    m.remove(28);
    System.out.println(m.dump(12));
    System.out.println("  get(51) = " + m.get(51) + "  (search walks over X and keeps going)");
    System.out.println("  put 97 (hash 5) walks the whole cluster to be sure 97 isn't already there,\n  then goes back and reuses the tombstone at slot 6:");
    m.put(97, "v97");
    System.out.println(m.dump(12));

    System.out.println("\nremoveBroken: the bug");
    LinearProbingHashMap<Integer, String> bad = new LinearProbingHashMap<>(23);
    for (int k : new int[] {5, 28, 51}) { bad.put(k, "v" + k); }
    bad.removeBroken(28);
    System.out.println(bad.dump(9));
    System.out.println("  get(51) = " + bad.get(51) + "   <- WRONG! 51 is still in slot 7, but the search stopped at the hole in slot 6");

    System.out.println("\nresize demo (capacity 4, max load 0.5):");
    LinearProbingHashMap<String, Integer> r = new LinearProbingHashMap<>(4);
    r.trace = true;
    for (String s : new String[] {"a", "b", "c", "d"}) { r.put(s, 1); }
    System.out.print("  iterator: ");
    for (String k : r) { System.out.print(k + " "); }
    System.out.println();

    LinearProbingHashSet<String> set = new LinearProbingHashSet<>();
    for (String w : "red green red blue green".split(" ")) { set.add(w); }
    System.out.println("\nset of \"red green red blue green\": size " + set.size()
        + ", contains blue = " + set.contains("blue") + ", remove red = " + set.remove("red")
        + ", contains red = " + set.contains("red"));
  }
}
