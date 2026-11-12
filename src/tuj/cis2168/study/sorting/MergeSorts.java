package tuj.cis2168.study.sorting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/// Study guide: MERGE SORT and merge(), every way (see study/03-sorting.md).
///
/// Idea (divide and conquer):
///   1. split the list in half
///   2. sort each half (recursively)
///   3. MERGE the two sorted halves: repeatedly take the smaller front item
///
///   ALWAYS O(n log n): log n levels of splitting, O(n) merge work per level
///   space O(n) extra for arrays (the temp array); STABLE (take from LEFT on ties)
///
/// Versions:
///   1. array, top-down RECURSIVE with merge(a, lo, mid, hi, temp)   + trace
///   2. array, "return a new array" version (simplest to write on an exam)
///   3. array, bottom-up ITERATIVE (widths 1, 2, 4, 8 ...) - no recursion at all
///   4. LINKED LIST: split with slow/fast pointers; merge RECURSIVE and ITERATIVE
///      (no temp array needed: just relink nodes)
///   5. ITERATORS: merge any two sorted Iterables; merge sort a java.util.List
///   6. HASH MAP entries with a Comparator, showing why STABILITY matters
///
/// Run:  java src/tuj/cis2168/study/sorting/MergeSorts.java
public class MergeSorts {

  static boolean trace = false;

  // =========================================================================
  // 1. Top-down recursive on an array
  // =========================================================================

  static void mergeSort(int[] a) {
    int[] temp = new int[a.length];      // allocate ONCE, reuse in every merge
    mergeSort(a, 0, a.length - 1, temp, "");
  }

  /// Sort a[lo..hi] (inclusive).
  static void mergeSort(int[] a, int lo, int hi, int[] temp, String indent) {
    if (lo >= hi) { return; }            // base case: 0 or 1 item
    int mid = (lo + hi) / 2;
    if (trace) { System.out.println(indent + "split " + slice(a, lo, mid) + " " + slice(a, mid + 1, hi)); }
    mergeSort(a, lo, mid, temp, indent + "  ");
    mergeSort(a, mid + 1, hi, temp, indent + "  ");
    merge(a, lo, mid, hi, temp);
    if (trace) { System.out.println(indent + "merge -> " + slice(a, lo, hi)); }
  }

  /// merge(): a[lo..mid] and a[mid+1..hi] are each sorted. Combine into a[lo..hi].
  static void merge(int[] a, int lo, int mid, int hi, int[] temp) {
    int i = lo, j = mid + 1, k = lo;
    while (i <= mid && j <= hi) {
      if (a[i] <= a[j]) { temp[k++] = a[i++]; }   // <= : take LEFT on ties -> stable
      else { temp[k++] = a[j++]; }
    }
    while (i <= mid) { temp[k++] = a[i++]; }       // leftovers (only ONE of these
    while (j <= hi) { temp[k++] = a[j++]; }        // two loops actually runs)
    for (k = lo; k <= hi; k++) { a[k] = temp[k]; } // copy back
  }

  // =========================================================================
  // 2. "Return a new array" version
  // =========================================================================

  static int[] mergeSortCopy(int[] a) {
    if (a.length <= 1) { return a; }
    int mid = a.length / 2;
    int[] left = mergeSortCopy(Arrays.copyOfRange(a, 0, mid));
    int[] right = mergeSortCopy(Arrays.copyOfRange(a, mid, a.length));
    return merge(left, right);
  }

  /// The classic standalone merge(): two sorted arrays in, one sorted array out.
  static int[] merge(int[] x, int[] y) {
    int[] out = new int[x.length + y.length];
    int i = 0, j = 0, k = 0;
    while (i < x.length && j < y.length) { out[k++] = (x[i] <= y[j]) ? x[i++] : y[j++]; }
    while (i < x.length) { out[k++] = x[i++]; }
    while (j < y.length) { out[k++] = y[j++]; }
    return out;
  }

  // =========================================================================
  // 3. Bottom-up, iterative: merge runs of width 1, then 2, then 4, ...
  // =========================================================================

  static void mergeSortBottomUp(int[] a) {
    int n = a.length;
    int[] temp = new int[n];
    for (int width = 1; width < n; width *= 2) {
      for (int lo = 0; lo < n - width; lo += 2 * width) {
        int mid = lo + width - 1;
        int hi = Math.min(lo + 2 * width - 1, n - 1);
        merge(a, lo, mid, hi, temp);
      }
      if (trace) { System.out.println("  width " + width + " -> " + Arrays.toString(a)); }
    }
  }

  // =========================================================================
  // 4. Linked list: the BEST sort for linked lists (no random access needed,
  //    O(1) extra space for merging, stable).
  // =========================================================================

  static class Node {
    int data;
    Node next;

    Node(int data, Node next) { this.data = data; this.next = next; }
  }

  static Node mergeSortList(Node head) {
    if (head == null || head.next == null) { return head; }
    Node secondHalf = split(head);
    return mergeLists(mergeSortList(head), mergeSortList(secondHalf));
  }

  /// slow moves 1, fast moves 2: when fast hits the end, slow is in the middle.
  /// Cut after slow and return the start of the second half.
  static Node split(Node head) {
    Node slow = head, fast = head.next;
    while (fast != null && fast.next != null) {
      slow = slow.next;
      fast = fast.next.next;
    }
    Node second = slow.next;
    slow.next = null;
    return second;
  }

  /// Recursive merge: the smaller head goes first, followed by merge(the rest).
  static Node mergeLists(Node x, Node y) {
    if (x == null) { return y; }
    if (y == null) { return x; }
    if (x.data <= y.data) {
      x.next = mergeLists(x.next, y);
      return x;
    } else {
      y.next = mergeLists(x, y.next);
      return y;
    }
  }

  /// Iterative merge with a DUMMY head node (avoids special-casing the first node).
  static Node mergeListsLoop(Node x, Node y) {
    Node dummy = new Node(0, null), tail = dummy;
    while (x != null && y != null) {
      if (x.data <= y.data) { tail.next = x; x = x.next; }
      else { tail.next = y; y = y.next; }
      tail = tail.next;
    }
    tail.next = (x != null) ? x : y;   // attach the leftovers in one step
    return dummy.next;
  }

  // =========================================================================
  // 5. Iterators: merge two sorted Iterables. An Iterator can't "peek", so we
  //    hold the current front item of each side in a variable.
  // =========================================================================

  static <T extends Comparable<T>> List<T> mergeIter(Iterable<T> xs, Iterable<T> ys) {
    List<T> out = new ArrayList<>();
    Iterator<T> i = xs.iterator(), j = ys.iterator();
    T x = i.hasNext() ? i.next() : null;
    T y = j.hasNext() ? j.next() : null;
    while (x != null && y != null) {
      if (x.compareTo(y) <= 0) { out.add(x); x = i.hasNext() ? i.next() : null; }
      else { out.add(y); y = j.hasNext() ? j.next() : null; }
    }
    while (x != null) { out.add(x); x = i.hasNext() ? i.next() : null; }
    while (y != null) { out.add(y); y = j.hasNext() ? j.next() : null; }
    return out;
  }

  static <T extends Comparable<T>> List<T> mergeSortList(List<T> list) {
    if (list.size() <= 1) { return list; }
    int mid = list.size() / 2;
    List<T> left = mergeSortList(list.subList(0, mid));
    List<T> right = mergeSortList(list.subList(mid, list.size()));
    return mergeIter(left, right);
  }

  // =========================================================================
  // 6. Generic merge sort with a Comparator, used on HashMap entries.
  //    Sort by key, THEN (stable) by count: equal counts stay alphabetical.
  // =========================================================================

  static <T> void mergeSort(List<T> a, Comparator<T> cmp) {
    if (a.size() <= 1) { return; }
    int mid = a.size() / 2;
    List<T> left = new ArrayList<>(a.subList(0, mid));
    List<T> right = new ArrayList<>(a.subList(mid, a.size()));
    mergeSort(left, cmp);
    mergeSort(right, cmp);
    int i = 0, j = 0, k = 0;
    while (i < left.size() && j < right.size()) {
      a.set(k++, cmp.compare(left.get(i), right.get(j)) <= 0 ? left.get(i++) : right.get(j++));
    }
    while (i < left.size()) { a.set(k++, left.get(i++)); }
    while (j < right.size()) { a.set(k++, right.get(j++)); }
  }

  // =========================================================================

  static String slice(int[] a, int lo, int hi) {
    return Arrays.toString(Arrays.copyOfRange(a, lo, hi + 1));
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
    int[] data = {38, 27, 43, 3, 9, 82, 10};

    System.out.println("1. top-down recursive, trace:");
    trace = true;
    int[] a = data.clone();
    mergeSort(a);

    System.out.println("\n   merge([3,27,38,43], [9,10,82]) = " + Arrays.toString(merge(new int[] {3, 27, 38, 43}, new int[] {9, 10, 82})));
    System.out.println("2. return-new-array : " + Arrays.toString(mergeSortCopy(data.clone())));

    System.out.println("3. bottom-up iterative, trace:");
    int[] c = data.clone();
    mergeSortBottomUp(c);
    trace = false;

    System.out.println("4. linked list           : " + show(mergeSortList(listOf(data))));
    System.out.println("   mergeLists (recursive): " + show(mergeLists(listOf(1, 4, 9), listOf(2, 3, 10))));
    System.out.println("   mergeListsLoop        : " + show(mergeListsLoop(listOf(1, 4, 9), listOf(2, 3, 10))));

    System.out.println("5. mergeIter(List, Set)  : " + mergeIter(List.of(1, 4, 9), new java.util.TreeSet<>(List.of(10, 2, 3))));
    System.out.println("   mergeSortList(List)   : " + mergeSortList(List.of("kiwi", "apple", "pear", "fig")));

    Map<String, Integer> counts = new HashMap<>();
    for (String w : "b a c b d a e b".split(" ")) { counts.merge(w, 1, Integer::sum); }
    List<Map.Entry<String, Integer>> entries = new ArrayList<>(counts.entrySet());
    mergeSort(entries, Map.Entry.comparingByKey());                                  // pass 1: by key
    mergeSort(entries, (x, y) -> y.getValue() - x.getValue());                       // pass 2: by count desc
    System.out.println("6. HashMap " + counts + " sorted by count, ties alphabetical: " + entries);
  }
}
