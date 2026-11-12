package tuj.cis2168.study.sorting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/// Study guide: SELECTION SORT, every way (see study/03-sorting.md).
///
/// Idea: find the smallest remaining item and put it next. Repeat.
///
///   ALWAYS O(n^2) comparisons (even if already sorted: it can't know without looking)
///   at most n-1 swaps (fewest writes of the simple sorts)
///   space O(1), NOT stable (a long-distance swap can jump over an equal item)
///
/// Versions:
///   1. array, iterative                                   + trace
///   2. array, RECURSIVE (min to the front, sort the rest; min found recursively too)
///   3. array, select the MAX and put it at the back
///   4. LINKED LIST: (a) swap data values  (b) unlink min node, append to new list
///   5. ITERATORS on any Collection: find min, remove it with Iterator.remove()
///   6. HASH SET: the same code as 5 on a HashSet (repeatedly "select" the min)
///
/// Run:  java src/tuj/cis2168/study/sorting/SelectionSorts.java
public class SelectionSorts {

  // =========================================================================
  // 1. Array, iterative
  // =========================================================================

  static void selectionSort(int[] a, boolean trace) {
    for (int i = 0; i < a.length - 1; i++) {
      int minIdx = i;
      for (int j = i + 1; j < a.length; j++) {
        if (a[j] < a[minIdx]) { minIdx = j; }
      }
      swap(a, i, minIdx);
      if (trace) { System.out.println("  pass " + i + ": min " + a[i] + " -> slot " + i + "  " + show(a, i)); }
    }
  }

  static void swap(int[] a, int i, int j) {
    int t = a[i];
    a[i] = a[j];
    a[j] = t;
  }

  // =========================================================================
  // 2. Array, recursive
  // =========================================================================

  static void selectionSortRec(int[] a, int start) {
    if (start >= a.length - 1) { return; }          // 0 or 1 items left
    int m = indexOfMin(a, start);
    swap(a, start, m);
    selectionSortRec(a, start + 1);                 // sort the rest
  }

  /// Index of the minimum in a[i..end], recursively (lab05 listArgMax style).
  static int indexOfMin(int[] a, int i) {
    if (i == a.length - 1) { return i; }
    int restMin = indexOfMin(a, i + 1);
    return a[i] <= a[restMin] ? i : restMin;
  }

  // =========================================================================
  // 3. Max version: grow the sorted part from the BACK
  // =========================================================================

  static void selectionSortMax(int[] a) {
    for (int end = a.length - 1; end > 0; end--) {
      int maxIdx = 0;
      for (int j = 1; j <= end; j++) {
        if (a[j] > a[maxIdx]) { maxIdx = j; }
      }
      swap(a, maxIdx, end);
    }
  }

  // =========================================================================
  // 4. Linked list
  // =========================================================================

  static class Node {
    int data;
    Node next;

    Node(int data, Node next) { this.data = data; this.next = next; }
  }

  /// (a) Same algorithm as the array: `i` and `j` become node pointers,
  /// and we swap the DATA, not the nodes.
  static void selectionSortListData(Node head) {
    for (Node i = head; i != null; i = i.next) {
      Node min = i;
      for (Node j = i.next; j != null; j = j.next) {
        if (j.data < min.data) { min = j; }
      }
      int t = i.data; i.data = min.data; min.data = t;
    }
  }

  /// (b) Relink nodes: repeatedly unlink the min node and append it to an output
  /// list. Needs the node BEFORE the min to unlink it (singly linked!).
  static Node selectionSortListNodes(Node head) {
    Node dummy = new Node(0, head);      // dummy in front makes "unlink the first node" easy
    Node outHead = null, outTail = null;
    while (dummy.next != null) {
      Node beforeMin = dummy;
      for (Node p = dummy; p.next != null; p = p.next) {
        if (p.next.data < beforeMin.next.data) { beforeMin = p; }
      }
      Node min = beforeMin.next;
      beforeMin.next = min.next;         // unlink
      min.next = null;
      if (outHead == null) { outHead = outTail = min; } else { outTail.next = min; outTail = min; }
    }
    return outHead;
  }

  /// Recursive linked-list selection sort: min to the front, then sort the rest.
  static void selectionSortListRec(Node head) {
    if (head == null || head.next == null) { return; }
    Node min = head;
    for (Node j = head.next; j != null; j = j.next) { if (j.data < min.data) { min = j; } }
    int t = head.data; head.data = min.data; min.data = t;
    selectionSortListRec(head.next);
  }

  // =========================================================================
  // 5 & 6. Iterators on ANY Collection (ArrayList, LinkedList, HashSet ...)
  //   Repeat: scan for the min (for-each), then scan again with an explicit
  //   Iterator and call it.remove() on it. Output collects the mins in order.
  //   The input collection is emptied (copy first if you need it).
  // =========================================================================

  static <T extends Comparable<T>> List<T> selectionSortCollection(Collection<T> items) {
    List<T> out = new ArrayList<>();
    while (!items.isEmpty()) {
      T min = null;
      for (T x : items) {                               // pass 1: SELECT the min
        if (min == null || x.compareTo(min) < 0) { min = x; }
      }
      Iterator<T> it = items.iterator();                // pass 2: remove it
      while (it.hasNext()) {
        if (it.next().equals(min)) { it.remove(); break; }
      }
      out.add(min);
    }
    return out;
  }
  // Note for a HashSet: items.remove(min) is O(1) on average, so pass 2 can be
  // skipped. But finding the min is still a full O(n) scan (hash tables don't
  // know about order) -> O(n^2) total. A HEAP finds the min in O(log n);
  // swapping the set for a heap turns this into HEAP SORT.

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
    int[] data = {29, 10, 14, 37, 13, 5};

    System.out.println("1. array, iterative (| = end of sorted part):");
    int[] a = data.clone();
    selectionSort(a, true);

    int[] b = data.clone();
    selectionSortRec(b, 0);
    System.out.println("2. array, recursive     : " + Arrays.toString(b));

    int[] c = data.clone();
    selectionSortMax(c);
    System.out.println("3. array, max to back   : " + Arrays.toString(c));

    Node l1 = listOf(data);
    selectionSortListData(l1);
    System.out.println("4a. linked list (data)  : " + show(l1));
    System.out.println("4b. linked list (nodes) : " + show(selectionSortListNodes(listOf(data))));
    Node l3 = listOf(data);
    selectionSortListRec(l3);
    System.out.println("    linked list (rec)   : " + show(l3));

    System.out.println("5. Iterator on ArrayList : " + selectionSortCollection(new ArrayList<>(List.of(29, 10, 14, 37, 13, 5))));
    System.out.println("   Iterator on LinkedList: " + selectionSortCollection(new LinkedList<>(List.of(29, 10, 14, 37, 13, 5))));
    HashSet<String> set = new HashSet<>(List.of("pear", "apple", "fig", "kiwi", "banana"));
    System.out.println("6. HashSet as stored     : " + set);
    System.out.println("   selection from HashSet: " + selectionSortCollection(set));

    // Not stable: 5a and 5b are equal keys; the first swap moves 5a behind 5b.
    System.out.println("\nStability demo (sort by number only):");
    String[] cards = {"5a", "5b", "3"};
    for (int i = 0; i < cards.length - 1; i++) {
      int m = i;
      for (int j = i + 1; j < cards.length; j++) {
        if (cards[j].charAt(0) < cards[m].charAt(0)) { m = j; }
      }
      String t = cards[i]; cards[i] = cards[m]; cards[m] = t;
    }
    System.out.println("   [5a, 5b, 3] -> " + Arrays.toString(cards) + "   (5b now before 5a: NOT stable)");
  }
}
