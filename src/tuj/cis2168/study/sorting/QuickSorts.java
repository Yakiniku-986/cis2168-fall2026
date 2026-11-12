package tuj.cis2168.study.sorting;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/// Study guide: QUICK SORT and partition(), every way (see study/03-sorting.md).
///
/// Idea (divide and conquer, but the work happens BEFORE recursing):
///   1. pick a PIVOT
///   2. PARTITION: smaller items to the left of the pivot, bigger to the right.
///      The pivot is now in its FINAL position.
///   3. quick sort the left part and the right part
///
///   average O(n log n), WORST O(n^2) (pivot is always the min or max, e.g.
///   already-sorted input with "last element" pivot). Space O(log n) stack on
///   average. In place. NOT stable.
///
/// Versions:
///   1. LOMUTO partition (pivot = last), recursive          + trace
///   2. HOARE partition (pivot = middle), recursive
///   3. ITERATIVE with an explicit stack of (lo, hi) ranges
///   4. median-of-three pivot (fixes the sorted-input worst case)
///   5. LINKED LIST: partition nodes into < / == / > lists, then concatenate
///   6. java.util.List with ITERATORS (for-each builds less / equal / greater)
///   7. QUICKSELECT: k-th smallest using the same partition() in O(n) average
///
/// Run:  java src/tuj/cis2168/study/sorting/QuickSorts.java
public class QuickSorts {

  static boolean trace = false;

  // =========================================================================
  // 1. Lomuto partition: pivot = a[hi].
  //    i marks the end of the "<= pivot" zone. Scan j; when a[j] <= pivot,
  //    grow the zone and swap a[j] into it. Finally put the pivot after the zone.
  // =========================================================================

  static void quickSort(int[] a, int lo, int hi) {
    if (lo >= hi) { return; }                 // 0 or 1 items
    int p = partitionLomuto(a, lo, hi);
    quickSort(a, lo, p - 1);                  // pivot at p is DONE: exclude it
    quickSort(a, p + 1, hi);
  }

  static int partitionLomuto(int[] a, int lo, int hi) {
    int pivot = a[hi];
    int i = lo - 1;                           // zone a[lo..i] is <= pivot (empty so far)
    for (int j = lo; j < hi; j++) {
      if (a[j] <= pivot) {
        i++;
        swap(a, i, j);
      }
    }
    swap(a, i + 1, hi);                       // pivot goes right after the zone
    if (trace) {
      System.out.println("  partition [" + lo + ".." + hi + "] pivot " + pivot + " -> "
          + showRange(a, lo, hi, i + 1));
    }
    return i + 1;
  }

  // =========================================================================
  // 2. Hoare partition: two indices walk toward each other and swap pairs
  //    that are on the wrong side. Fewer swaps than Lomuto. Returns a split
  //    point j: a[lo..j] <= pivot <= a[j+1..hi] (pivot NOT necessarily at j).
  // =========================================================================

  static void quickSortHoare(int[] a, int lo, int hi) {
    if (lo >= hi) { return; }
    int j = partitionHoare(a, lo, hi);
    quickSortHoare(a, lo, j);                 // NOTE: j, not j-1
    quickSortHoare(a, j + 1, hi);
  }

  static int partitionHoare(int[] a, int lo, int hi) {
    int pivot = a[(lo + hi) / 2];
    int i = lo - 1, j = hi + 1;
    while (true) {
      do { i++; } while (a[i] < pivot);
      do { j--; } while (a[j] > pivot);
      if (i >= j) { return j; }
      swap(a, i, j);
    }
  }

  // =========================================================================
  // 3. Iterative: push (lo, hi) ranges onto a stack instead of recursing
  // =========================================================================

  static void quickSortLoop(int[] a) {
    Deque<int[]> stack = new ArrayDeque<>();
    stack.push(new int[] {0, a.length - 1});
    while (!stack.isEmpty()) {
      int[] r = stack.pop();
      int lo = r[0], hi = r[1];
      if (lo >= hi) { continue; }
      int p = partitionLomuto(a, lo, hi);
      stack.push(new int[] {lo, p - 1});
      stack.push(new int[] {p + 1, hi});
    }
  }

  // =========================================================================
  // 4. Median-of-three: look at a[lo], a[mid], a[hi]; move the median to a[hi]
  //    and then use Lomuto as usual. Sorted input is no longer a worst case.
  // =========================================================================

  static int comparisons = 0;

  static void quickSortMedian3(int[] a, int lo, int hi, boolean useMedian) {
    if (lo >= hi) { return; }
    if (useMedian) {
      int mid = (lo + hi) / 2;
      if (a[mid] < a[lo]) { swap(a, mid, lo); }
      if (a[hi] < a[lo]) { swap(a, hi, lo); }
      if (a[mid] < a[hi]) { swap(a, mid, hi); }     // now a[hi] is the median
    }
    int pivot = a[hi], i = lo - 1;
    for (int j = lo; j < hi; j++) {
      comparisons++;
      if (a[j] <= pivot) { swap(a, ++i, j); }
    }
    swap(a, i + 1, hi);
    quickSortMedian3(a, lo, i, useMedian);
    quickSortMedian3(a, i + 2, hi, useMedian);
  }

  // =========================================================================
  // 5. Linked list: pivot = head. Split the rest into three lists
  //    (less / equal / greater), sort less and greater, then concatenate.
  //    No swapping, no indices: just relinking nodes.
  // =========================================================================

  static class Node {
    int data;
    Node next;

    Node(int data, Node next) { this.data = data; this.next = next; }
  }

  static Node quickSortList(Node head) {
    if (head == null || head.next == null) { return head; }
    int pivot = head.data;
    Node less = null, equal = null, greater = null;
    Node curr = head;
    while (curr != null) {
      Node next = curr.next;
      if (curr.data < pivot) { curr.next = less; less = curr; }          // push onto front
      else if (curr.data == pivot) { curr.next = equal; equal = curr; }
      else { curr.next = greater; greater = curr; }
      curr = next;
    }
    less = quickSortList(less);
    greater = quickSortList(greater);
    return concat(concat(less, equal), greater);
  }

  static Node concat(Node x, Node y) {
    if (x == null) { return y; }
    Node t = x;
    while (t.next != null) { t = t.next; }
    t.next = y;
    return x;
  }

  // =========================================================================
  // 6. java.util.List with iterators (for-each). Easy to write, uses O(n) extra
  //    space per level. Same 3-way idea as the linked-list version.
  // =========================================================================

  static <T extends Comparable<T>> List<T> quickSortList(List<T> items) {
    if (items.size() <= 1) { return items; }
    T pivot = items.get(items.size() / 2);
    List<T> less = new ArrayList<>(), equal = new ArrayList<>(), greater = new ArrayList<>();
    for (T x : items) {
      int c = x.compareTo(pivot);
      if (c < 0) { less.add(x); } else if (c == 0) { equal.add(x); } else { greater.add(x); }
    }
    List<T> out = new ArrayList<>(quickSortList(less));
    out.addAll(equal);
    out.addAll(quickSortList(greater));
    return out;
  }

  // =========================================================================
  // 7. Quickselect: k-th smallest (k = 0 is the min). Partition, then recurse
  //    into ONLY the side that contains index k. O(n) average.
  // =========================================================================

  static int quickSelect(int[] a, int lo, int hi, int k) {
    if (lo == hi) { return a[lo]; }
    int p = partitionLomuto(a, lo, hi);
    if (k == p) { return a[p]; }
    if (k < p) { return quickSelect(a, lo, p - 1, k); }
    return quickSelect(a, p + 1, hi, k);
  }

  // =========================================================================

  static void swap(int[] a, int i, int j) { int t = a[i]; a[i] = a[j]; a[j] = t; }

  static String showRange(int[] a, int lo, int hi, int p) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < a.length; i++) {
      if (i == lo) { sb.append("{ "); }
      sb.append(i == p ? "[" + a[i] + "]" : String.valueOf(a[i])).append(' ');
      if (i == hi) { sb.append("} "); }
    }
    return sb.toString().trim();
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
    int[] data = {7, 2, 1, 6, 8, 5, 3, 4};

    System.out.println("1. Lomuto, recursive. { } = range being partitioned, [x] = pivot's final spot");
    trace = true;
    int[] a = data.clone();
    quickSort(a, 0, a.length - 1);
    trace = false;
    System.out.println("   result: " + Arrays.toString(a));

    int[] b = data.clone();
    quickSortHoare(b, 0, b.length - 1);
    System.out.println("2. Hoare             : " + Arrays.toString(b));

    int[] c = data.clone();
    quickSortLoop(c);
    System.out.println("3. iterative + stack : " + Arrays.toString(c));

    int n = 2000;
    int[] sorted1 = new int[n], sorted2 = new int[n];
    for (int i = 0; i < n; i++) { sorted1[i] = i; sorted2[i] = i; }
    comparisons = 0;
    quickSortMedian3(sorted1, 0, n - 1, false);
    int plain = comparisons;
    comparisons = 0;
    quickSortMedian3(sorted2, 0, n - 1, true);
    System.out.println("4. already-sorted n=" + n + ": last-pivot comparisons = " + plain
        + " (~n^2/2),  median-of-3 = " + comparisons + " (~n log n)");

    System.out.println("5. linked list       : " + show(quickSortList(listOf(7, 2, 1, 6, 8, 5, 3, 4, 5))));
    System.out.println("6. List + iterators  : " + quickSortList(List.of("kiwi", "apple", "pear", "fig", "apple")));

    int[] d = data.clone();
    System.out.println("7. quickSelect k=2 (3rd smallest) of " + Arrays.toString(data) + " = "
        + quickSelect(d, 0, d.length - 1, 2));
  }
}
