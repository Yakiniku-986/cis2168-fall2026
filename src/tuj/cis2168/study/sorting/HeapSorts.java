package tuj.cis2168.study.sorting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/// Study guide: HEAP SORT, every way (see study/03-sorting.md and 02-trees.md).
///
/// Idea: selection sort, but find the max with a HEAP in O(log n) instead of an
/// O(n) scan.
///   Phase 1  build a MAX-heap inside the array (bottom-up heapify)   O(n)
///   Phase 2  repeat n-1 times: swap root (max) with the last unsorted slot,
///            shrink the heap by one, siftDown the new root            O(n log n)
///
///   ALWAYS O(n log n)  (best, average, worst)
///   space O(1) for the in-place version, NOT stable
///
/// Versions:
///   1. in-place array, iterative siftDown          + trace
///   2. in-place array, RECURSIVE siftDown
///   3. using a separate heap: java.util.PriorityQueue (any Iterable input)
///   4. LINKED LIST: drain nodes into a heap, write values back
///   5. HASH MAP + heap: top-k most frequent words (classic exam/interview combo)
///
/// Run:  java src/tuj/cis2168/study/sorting/HeapSorts.java
public class HeapSorts {

  // =========================================================================
  // 1. In-place, iterative
  // =========================================================================

  static void heapSort(int[] a, boolean trace) {
    int n = a.length;
    for (int i = n / 2 - 1; i >= 0; i--) { siftDown(a, i, n); }   // phase 1
    if (trace) { System.out.println("  after heapify (max-heap): " + Arrays.toString(a)); }
    for (int end = n - 1; end > 0; end--) {                         // phase 2
      swap(a, 0, end);            // max goes to its final place
      siftDown(a, 0, end);        // heap is now a[0 .. end-1]
      if (trace) { System.out.println("  moved " + a[end] + " to slot " + end + ": " + show(a, end)); }
    }
  }

  /// Sift a[i] down within a[0 .. n-1] (max-heap: swap with the LARGER child).
  static void siftDown(int[] a, int i, int n) {
    while (2 * i + 1 < n) {
      int child = 2 * i + 1;                                   // left
      if (child + 1 < n && a[child + 1] > a[child]) { child++; } // right is bigger
      if (a[i] >= a[child]) { return; }
      swap(a, i, child);
      i = child;
    }
  }

  // =========================================================================
  // 2. In-place, recursive siftDown
  // =========================================================================

  static void heapSortRec(int[] a) {
    int n = a.length;
    for (int i = n / 2 - 1; i >= 0; i--) { siftDownRec(a, i, n); }
    for (int end = n - 1; end > 0; end--) {
      swap(a, 0, end);
      siftDownRec(a, 0, end);
    }
  }

  static void siftDownRec(int[] a, int i, int n) {
    int left = 2 * i + 1, right = 2 * i + 2, largest = i;
    if (left < n && a[left] > a[largest]) { largest = left; }
    if (right < n && a[right] > a[largest]) { largest = right; }
    if (largest == i) { return; }       // base case: heap order holds here
    swap(a, i, largest);
    siftDownRec(a, largest, n);
  }

  // =========================================================================
  // 3. Separate heap object: add everything, poll everything. O(n) extra space.
  //    Works on ANY Iterable (ArrayList, LinkedList, HashSet, map.keySet() ...)
  // =========================================================================

  static <T extends Comparable<T>> List<T> heapSortWithPQ(Iterable<T> items) {
    PriorityQueue<T> pq = new PriorityQueue<>();    // a min-heap
    for (T x : items) { pq.add(x); }                // n * O(log n)
    List<T> out = new ArrayList<>();
    while (!pq.isEmpty()) { out.add(pq.poll()); }   // n * O(log n)
    return out;
  }

  // =========================================================================
  // 4. Linked list: heaps need random access (index 2i+1), which linked lists
  //    don't have. So: move values into a heap, then write them back in order.
  // =========================================================================

  static class Node {
    int data;
    Node next;

    Node(int data, Node next) { this.data = data; this.next = next; }
  }

  static void heapSortList(Node head) {
    PriorityQueue<Integer> pq = new PriorityQueue<>();
    for (Node n = head; n != null; n = n.next) { pq.add(n.data); }
    for (Node n = head; n != null; n = n.next) { n.data = pq.poll(); }
  }

  // =========================================================================
  // 5. HashMap (count) + heap (pick top k). O(n + m log k)
  //    Keep a MIN-heap of size k: the smallest of the current top-k is on top,
  //    so it's the one to kick out when something bigger shows up.
  // =========================================================================

  static List<String> topK(String text, int k) {
    Map<String, Integer> counts = new HashMap<>();
    for (String w : text.split("\\s+")) { counts.put(w, counts.getOrDefault(w, 0) + 1); }

    PriorityQueue<Map.Entry<String, Integer>> heap =
        new PriorityQueue<>((x, y) -> x.getValue().equals(y.getValue())
            ? y.getKey().compareTo(x.getKey())               // tie: alphabetical wins
            : x.getValue() - y.getValue());
    for (Map.Entry<String, Integer> e : counts.entrySet()) {
      heap.add(e);
      if (heap.size() > k) { heap.poll(); }               // drop the smallest
    }
    List<String> out = new ArrayList<>();
    while (!heap.isEmpty()) { Map.Entry<String, Integer> e = heap.poll(); out.add(e.getKey() + "=" + e.getValue()); }
    Collections.reverse(out);                             // biggest first
    return out;
  }

  // =========================================================================

  static void swap(int[] a, int i, int j) { int t = a[i]; a[i] = a[j]; a[j] = t; }

  static String show(int[] a, int sortedFrom) {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < a.length; i++) {
      if (i == sortedFrom) { sb.append("| "); }
      sb.append(a[i]);
      if (i < a.length - 1) { sb.append(' '); }
    }
    return sb.append("]").toString();
  }

  public static void main(String[] args) {
    int[] data = {4, 10, 3, 5, 1, 8};

    System.out.println("1. in-place, iterative (| = start of sorted part):");
    int[] a = data.clone();
    heapSort(a, true);

    int[] b = data.clone();
    heapSortRec(b);
    System.out.println("2. in-place, recursive   : " + Arrays.toString(b));

    System.out.println("3. PriorityQueue (list)  : " + heapSortWithPQ(List.of(4, 10, 3, 5, 1, 8)));
    System.out.println("   PriorityQueue (keySet): " + heapSortWithPQ(Map.of("pear", 1, "fig", 2, "apple", 3).keySet()));

    Node h = new Node(4, new Node(10, new Node(3, new Node(5, new Node(1, null)))));
    heapSortList(h);
    StringBuilder sb = new StringBuilder();
    for (Node n = h; n != null; n = n.next) { sb.append(n.data).append(' '); }
    System.out.println("4. linked list           : " + sb);

    System.out.println("5. HashMap + heap, top 3 : "
        + topK("a rose is a rose is a rose and a daisy is a daisy", 3));
  }
}
