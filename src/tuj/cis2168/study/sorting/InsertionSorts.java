package tuj.cis2168.study.sorting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/// Study guide: INSERTION SORT, every way (see study/03-sorting.md).
///
/// Idea: like sorting cards in your hand. The left part is sorted; take the
/// next item and slide it left into its place.
///
///   best  O(n)   (already sorted: each item compares once and stays)
///   worst O(n^2) (reverse sorted: each item slides all the way left)
///   space O(1), STABLE, great for small or nearly-sorted data
///
/// Versions:
///   1. array, iterative, SHIFTING (the textbook version)      + trace
///   2. array, iterative, SWAPPING (simpler, more writes)
///   3. array, RECURSIVE  (sort first n-1, insert the last)
///   4. array, BINARY insertion (binary search for the spot)
///   5. LINKED LIST of nodes (relink nodes into a new sorted list)
///   6. ITERATORS / ListIterator on java.util.List
///   7. HASH TABLE: sort what comes out of a HashMap (keys, or entries by value)
///
/// Run:  java src/tuj/cis2168/study/sorting/InsertionSorts.java
public class InsertionSorts {

  // =========================================================================
  // 1. Array, iterative, shifting
  // =========================================================================

  static void insertionSort(int[] a, boolean trace) {
    for (int i = 1; i < a.length; i++) {
      int key = a[i];                 // the "card" we're inserting
      int j = i - 1;
      while (j >= 0 && a[j] > key) {  // strict > keeps it STABLE
        a[j + 1] = a[j];              // shift bigger items one to the right
        j--;
      }
      a[j + 1] = key;                 // drop the card into the gap
      if (trace) { System.out.println("  i=" + i + " insert " + key + ": " + show(a, i)); }
    }
  }

  // =========================================================================
  // 2. Array, swapping (generic)
  // =========================================================================

  static <T extends Comparable<T>> void insertionSortSwap(T[] a) {
    for (int i = 1; i < a.length; i++) {
      for (int j = i; j > 0 && a[j - 1].compareTo(a[j]) > 0; j--) {
        T t = a[j];
        a[j] = a[j - 1];
        a[j - 1] = t;
      }
    }
  }

  // =========================================================================
  // 3. Array, recursive: to sort a[0..n-1], sort a[0..n-2], then insert a[n-1]
  // =========================================================================

  static void insertionSortRec(int[] a, int n) {
    if (n <= 1) { return; }            // base case: 0 or 1 items are sorted
    insertionSortRec(a, n - 1);        // sort everything except the last
    insertLast(a, n - 1);              // insert a[n-1] into the sorted prefix
  }

  /// The inner loop can be recursive too.
  static void insertLast(int[] a, int i) {
    if (i == 0 || a[i - 1] <= a[i]) { return; }
    int t = a[i]; a[i] = a[i - 1]; a[i - 1] = t;
    insertLast(a, i - 1);
  }

  // =========================================================================
  // 4. Binary insertion sort: O(n log n) COMPARISONS, but still O(n^2) MOVES
  // =========================================================================

  static void binaryInsertionSort(int[] a) {
    for (int i = 1; i < a.length; i++) {
      int key = a[i];
      int lo = 0, hi = i;               // find first index in [0, i) with a[idx] > key
      while (lo < hi) {
        int mid = (lo + hi) / 2;
        if (a[mid] <= key) { lo = mid + 1; } else { hi = mid; }
      }
      System.arraycopy(a, lo, a, lo + 1, i - lo);   // shift a[lo..i-1] right
      a[lo] = key;
    }
  }

  // =========================================================================
  // 5. Linked list of nodes: take nodes off the input one by one and insert
  //    each into a growing SORTED list. No new nodes, just relinking. O(1) space.
  // =========================================================================

  static class Node {
    int data;
    Node next;

    Node(int data, Node next) { this.data = data; this.next = next; }
  }

  static Node insertionSortList(Node head) {
    Node sorted = null;                       // head of the sorted list
    while (head != null) {
      Node curr = head;
      head = head.next;                       // detach curr from the input
      if (sorted == null || curr.data < sorted.data) {
        curr.next = sorted;                   // new smallest: goes in front
        sorted = curr;
      } else {
        Node p = sorted;                      // walk to the last node <= curr
        while (p.next != null && p.next.data <= curr.data) { p = p.next; }
        curr.next = p.next;
        p.next = curr;
      }
    }
    return sorted;
  }

  /// Recursive linked-list insertion sort: sort the rest, then insert the head.
  static Node insertionSortListRec(Node head) {
    if (head == null || head.next == null) { return head; }
    Node sortedRest = insertionSortListRec(head.next);
    return insertSorted(sortedRest, head);
  }

  static Node insertSorted(Node sorted, Node n) {
    if (sorted == null || n.data <= sorted.data) {
      n.next = sorted;
      return n;
    }
    sorted.next = insertSorted(sorted.next, n);
    return sorted;
  }

  // =========================================================================
  // 6. Iterators: read the input with an Iterator, insert into an output
  //    java.util.LinkedList using a ListIterator to find the spot. ListIterator
  //    .add() inserts right where the cursor is, in O(1) on a LinkedList.
  //    Works for ANY Iterable input (ArrayList, LinkedList, HashSet, ...).
  // =========================================================================

  static <T extends Comparable<T>> LinkedList<T> insertionSortIter(Iterable<T> input) {
    LinkedList<T> out = new LinkedList<>();
    for (T x : input) {                               // outer loop: an Iterator
      ListIterator<T> it = out.listIterator();
      while (it.hasNext()) {
        if (it.next().compareTo(x) > 0) {             // passed the spot
          it.previous();                              // step back
          break;
        }
      }
      it.add(x);                                      // insert at the cursor
    }
    return out;
  }

  /// In-place on a java.util.List using ListIterators (walks backwards with
  /// previous(), the way the array version walks j down).
  static <T extends Comparable<T>> void insertionSortListInPlace(List<T> list) {
    for (int i = 1; i < list.size(); i++) {
      T key = list.get(i);
      ListIterator<T> it = list.listIterator(i);      // cursor just before index i
      int j = i;
      while (it.hasPrevious()) {
        T prev = it.previous();
        if (prev.compareTo(key) <= 0) { break; }
        list.set(j, prev);                            // shift right
        j--;
      }
      list.set(j, key);
    }
  }

  // =========================================================================
  // 7. HASH TABLES
  //   A HashMap/HashSet has NO ORDER (buckets are scattered by hashCode), so you
  //   can't "insertion sort a hash table" in place. What you CAN do:
  //     a) pull the keys out with an iterator, sort them, walk them in order
  //     b) sort the ENTRIES by value (e.g. word counts), with a comparator
  //     c) rebuild a LinkedHashMap in sorted order (it remembers insertion order)
  // =========================================================================

  static <K extends Comparable<K>, V> List<K> sortedKeys(Map<K, V> map) {
    List<K> keys = new ArrayList<>();
    Iterator<K> it = map.keySet().iterator();
    while (it.hasNext()) { keys.add(it.next()); }
    insertionSortListInPlace(keys);
    return keys;
  }

  /// Entries sorted by value, DESCENDING; ties keep their existing order (stable).
  static <K, V extends Comparable<V>> List<Map.Entry<K, V>> sortByValue(Map<K, V> map) {
    List<Map.Entry<K, V>> entries = new ArrayList<>(map.entrySet());
    for (int i = 1; i < entries.size(); i++) {
      Map.Entry<K, V> key = entries.get(i);
      int j = i - 1;
      while (j >= 0 && entries.get(j).getValue().compareTo(key.getValue()) < 0) {
        entries.set(j + 1, entries.get(j));
        j--;
      }
      entries.set(j + 1, key);
    }
    return entries;
  }

  static <K extends Comparable<K>, V> LinkedHashMap<K, V> toSortedLinkedHashMap(Map<K, V> map) {
    LinkedHashMap<K, V> out = new LinkedHashMap<>();
    for (K k : sortedKeys(map)) { out.put(k, map.get(k)); }
    return out;
  }

  // =========================================================================

  static String show(int[] a, int sortedUpTo) {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < a.length; i++) {
      sb.append(a[i]);
      if (i == sortedUpTo) { sb.append(" |"); }
      if (i < a.length - 1) { sb.append(' '); }
    }
    return sb.append("]").toString();
  }

  static Node listOf(int... xs) {
    Node h = null;
    for (int i = xs.length - 1; i >= 0; i--) { h = new Node(xs[i], h); }
    return h;
  }

  static String show(Node h) {
    StringBuilder sb = new StringBuilder();
    for (; h != null; h = h.next) { sb.append(h.data).append(h.next != null ? " -> " : ""); }
    return sb.toString();
  }

  public static void main(String[] args) {
    int[] data = {7, 3, 5, 1, 9, 2};

    System.out.println("1. array, shifting (| marks the end of the sorted part):");
    int[] a = data.clone();
    insertionSort(a, true);

    Integer[] b = {7, 3, 5, 1, 9, 2};
    insertionSortSwap(b);
    System.out.println("2. array, swapping      : " + Arrays.toString(b));

    int[] c = data.clone();
    insertionSortRec(c, c.length);
    System.out.println("3. array, recursive     : " + Arrays.toString(c));

    int[] d = data.clone();
    binaryInsertionSort(d);
    System.out.println("4. binary insertion     : " + Arrays.toString(d));

    System.out.println("5. linked list (loop)   : " + show(insertionSortList(listOf(data))));
    System.out.println("   linked list (rec)    : " + show(insertionSortListRec(listOf(data))));

    List<Integer> input = List.of(7, 3, 5, 1, 9, 2);
    System.out.println("6. iterator -> LinkedList: " + insertionSortIter(input));
    List<String> words = new ArrayList<>(List.of("pear", "apple", "fig", "kiwi"));
    insertionSortListInPlace(words);
    System.out.println("   ListIterator in place : " + words);

    Map<String, Integer> counts = new HashMap<>();
    for (String w : "the cat and the hat and the bat".split(" ")) { counts.merge(w, 1, Integer::sum); }
    System.out.println("7. HashMap as stored     : " + counts + "   <- no useful order");
    System.out.println("   sortedKeys            : " + sortedKeys(counts));
    System.out.println("   sortByValue           : " + sortByValue(toSortedLinkedHashMap(counts)));
    System.out.println("   sorted LinkedHashMap  : " + toSortedLinkedHashMap(counts));
  }
}
