package tuj.cis2168.study.hashing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/// Study guide: hash table with SEPARATE CHAINING (see study/04-hash-tables.md).
///
/// The table is an ARRAY of buckets. Each bucket is a LINKED LIST (a "chain")
/// of all the entries whose key hashes to that index.
///
///   index(key) = Math.floorMod(key.hashCode(), table.length)
///
///   put / get / remove: hash to a bucket, then walk that one chain.
///   load factor  L = size / table.length   (can go ABOVE 1 with chaining)
///   average O(1 + L); we resize (double + rehash) when L > 0.75 to keep it O(1)
///   worst case O(n): every key in the same bucket
///
/// Also here:
///   * an Iterator over all keys (walk bucket by bucket, node by node)
///   * a recursive and an iterative chain search, side by side
///   * ChainingHashSet: a SET is just a map where you ignore the values
///   * a version using java.util.LinkedList buckets instead of our own nodes
///
/// Run:  java src/tuj/cis2168/study/hashing/ChainingHashMap.java
public class ChainingHashMap<K, V> implements Iterable<K> {

  private static class Node<K, V> {
    final K key;
    V value;
    Node<K, V> next;

    Node(K key, V value, Node<K, V> next) {
      this.key = key;
      this.value = value;
      this.next = next;
    }
  }

  private Node<K, V>[] table;
  private int size;
  private static final double MAX_LOAD = 0.75;
  boolean trace = false;

  @SuppressWarnings("unchecked")
  public ChainingHashMap(int capacity) {
    table = (Node<K, V>[]) new Node[capacity];   // generic array creation workaround
  }

  public int size() { return size; }

  /// hashCode() can be negative, so use floorMod (not %) to get 0..length-1.
  private int indexFor(Object key, int length) {
    return Math.floorMod(key.hashCode(), length);
  }

  // ------------------------------------------------------------------ get
  public V get(K key) {
    Node<K, V> n = find(table[indexFor(key, table.length)], key);
    return n == null ? null : n.value;
  }

  /// Iterative chain search.
  private Node<K, V> find(Node<K, V> chain, K key) {
    for (Node<K, V> n = chain; n != null; n = n.next) {
      if (n.key.equals(key)) { return n; }      // equals(), NOT ==
    }
    return null;
  }

  /// The same search, recursively (it's just "contains" on a linked list).
  private Node<K, V> findRec(Node<K, V> chain, K key) {
    if (chain == null) { return null; }
    if (chain.key.equals(key)) { return chain; }
    return findRec(chain.next, key);
  }

  public boolean containsKey(K key) {
    return findRec(table[indexFor(key, table.length)], key) != null;
  }

  // ------------------------------------------------------------------ put
  public V put(K key, V value) {
    int i = indexFor(key, table.length);
    Node<K, V> existing = find(table[i], key);
    if (existing != null) {                     // key already there: replace value
      V old = existing.value;
      existing.value = value;
      return old;
    }
    table[i] = new Node<>(key, value, table[i]);  // add at FRONT of the chain: O(1)
    size++;
    if (trace) {
      System.out.printf("  put %-6s hash=%-11d index=%d  %s%n", key, key.hashCode(), i,
          table[i].next != null ? "<- COLLISION, chained" : "");
    }
    if ((double) size / table.length > MAX_LOAD) { resize(); }
    return null;
  }

  // --------------------------------------------------------------- remove
  /// Unlinking from a singly linked list: keep track of the previous node.
  public V remove(K key) {
    int i = indexFor(key, table.length);
    Node<K, V> prev = null;
    for (Node<K, V> n = table[i]; n != null; prev = n, n = n.next) {
      if (n.key.equals(key)) {
        if (prev == null) { table[i] = n.next; } else { prev.next = n.next; }
        size--;
        return n.value;
      }
    }
    return null;
  }

  // --------------------------------------------------------------- resize
  /// Double the array and RE-HASH every entry: indices depend on table.length,
  /// so entries can't just be copied to the same index.
  @SuppressWarnings("unchecked")
  private void resize() {
    Node<K, V>[] old = table;
    table = (Node<K, V>[]) new Node[old.length * 2];
    for (Node<K, V> chain : old) {
      for (Node<K, V> n = chain; n != null; n = n.next) {
        int i = indexFor(n.key, table.length);
        table[i] = new Node<>(n.key, n.value, table[i]);
      }
    }
    if (trace) { System.out.println("  ** load > " + MAX_LOAD + ": resized " + old.length + " -> " + table.length + " and rehashed"); }
  }

  // ------------------------------------------------------------- iterator
  /// Walk bucket 0's chain, then bucket 1's chain, ... skipping empty buckets.
  @Override
  public Iterator<K> iterator() {
    return new Iterator<K>() {
      int bucket = -1;
      Node<K, V> next = advance(null);

      private Node<K, V> advance(Node<K, V> from) {
        if (from != null && from.next != null) { return from.next; }  // rest of this chain
        while (++bucket < table.length) {                             // next non-empty bucket
          if (table[bucket] != null) { return table[bucket]; }
        }
        return null;
      }

      public boolean hasNext() { return next != null; }

      public K next() {
        if (next == null) { throw new NoSuchElementException(); }
        K k = next.key;
        next = advance(next);
        return k;
      }
    };
  }

  /// Show the actual buckets.
  public String dump() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < table.length; i++) {
      sb.append(String.format("  [%2d] ", i));
      for (Node<K, V> n = table[i]; n != null; n = n.next) {
        sb.append(n.key).append('=').append(n.value).append(n.next != null ? " -> " : "");
      }
      sb.append('\n');
    }
    return sb.toString();
  }

  // =========================================================================
  // A SET on top of the map: the value is just a placeholder.
  // (java.util.HashSet is literally implemented this way, on a HashMap.)
  // =========================================================================

  static class ChainingHashSet<E> implements Iterable<E> {
    private final ChainingHashMap<E, Boolean> map = new ChainingHashMap<>(8);

    boolean add(E e) { return map.put(e, Boolean.TRUE) == null; }  // false if already there
    boolean contains(E e) { return map.containsKey(e); }
    boolean remove(E e) { return map.remove(e) != null; }
    int size() { return map.size(); }
    public Iterator<E> iterator() { return map.iterator(); }
  }

  // =========================================================================
  // Same structure, using java.util.LinkedList as the bucket type.
  // Shorter code; the chain operations come from the library.
  // =========================================================================

  static class LibraryChainingMap<K, V> {
    private record Entry<K, V>(K key, V value) { }

    private final List<LinkedList<Entry<K, V>>> buckets = new ArrayList<>();

    LibraryChainingMap(int capacity) {
      for (int i = 0; i < capacity; i++) { buckets.add(new LinkedList<>()); }
    }

    private LinkedList<Entry<K, V>> bucketFor(K key) {
      return buckets.get(Math.floorMod(key.hashCode(), buckets.size()));
    }

    void put(K key, V value) {
      LinkedList<Entry<K, V>> b = bucketFor(key);
      b.removeIf(e -> e.key().equals(key));       // Iterator.remove under the hood
      b.add(new Entry<>(key, value));
    }

    V get(K key) {
      for (Entry<K, V> e : bucketFor(key)) {
        if (e.key().equals(key)) { return e.value(); }
      }
      return null;
    }
  }

  // =========================================================================

  public static void main(String[] args) {
    ChainingHashMap<String, Integer> m = new ChainingHashMap<>(5);
    m.trace = true;
    System.out.println("capacity 5; put 6 keys (watch the collisions and the resize):");
    String[] keys = {"cat", "dog", "emu", "fox", "gnu", "hen"};
    for (int i = 0; i < keys.length; i++) { m.put(keys[i], i); }
    System.out.println("\nbuckets after inserts:");
    System.out.print(m.dump());

    System.out.println("get(\"emu\") = " + m.get("emu") + ", get(\"yak\") = " + m.get("yak"));
    System.out.println("put(\"emu\", 99) returns old value " + m.put("emu", 99) + "; size still " + m.size());
    System.out.println("remove(\"cat\") = " + m.remove("cat") + ", containsKey(\"cat\") = " + m.containsKey("cat"));
    System.out.print("iterator: ");
    for (String k : m) { System.out.print(k + " "); }
    System.out.println("\n");

    ChainingHashSet<Integer> set = new ChainingHashSet<>();
    int[] nums = {4, 8, 15, 16, 23, 42, 8, 15};
    StringBuilder dups = new StringBuilder();
    for (int x : nums) { if (!set.add(x)) { dups.append(x).append(' '); } }
    System.out.println("set of {4,8,15,16,23,42,8,15}: size " + set.size() + ", duplicates rejected: " + dups);

    LibraryChainingMap<String, String> lib = new LibraryChainingMap<>(4);
    lib.put("JP", "Tokyo");
    lib.put("US", "Washington");
    lib.put("JP", "Tokyo (updated)");
    System.out.println("LinkedList-bucket map: JP -> " + lib.get("JP") + ", US -> " + lib.get("US"));
  }
}
