package tuj.cis2168.study.trees;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

/// Study guide: heaps / priority queues (see study/02-trees.md).
///
/// A (min-)heap is a binary tree with TWO rules:
///   1. SHAPE: it is COMPLETE (all levels full except the last, filled left to right)
///   2. ORDER: every parent <= its children     (max-heap: parent >= children)
///
/// Because it is complete, we store it in an ARRAY with no gaps:
///   children of i: 2i+1, 2i+2        parent of i: (i-1)/2
///
/// Shown here:
///   * add      -> siftUp   (iterative AND recursive)
///   * removeMin-> siftDown (iterative AND recursive)
///   * build a heap two ways: n adds (O(n log n)) vs bottom-up heapify (O(n))
///   * isHeap on the array form (recursive and loop) and on linked nodes
///   * java.util.PriorityQueue does the same thing for you
///
/// Run:  java src/tuj/cis2168/study/trees/HeapDemo.java
public class HeapDemo {

  /// A min-heap backed by an ArrayList (an elastic array).
  static class MinHeap<E extends Comparable<E>> {
    final List<E> a = new ArrayList<>();
    boolean recursive;     // choose which sift implementation to use
    boolean trace;

    MinHeap(boolean recursive) { this.recursive = recursive; }

    int size() { return a.size(); }

    E peek() { return a.get(0); }

    // ---------------------------------------------------------------- add
    /// Put the new item at the end (keeps the SHAPE), then swap it up while
    /// it is smaller than its parent (fixes the ORDER). O(log n).
    void add(E item) {
      a.add(item);
      if (recursive) { siftUpRec(a.size() - 1); } else { siftUp(a.size() - 1); }
      if (trace) { System.out.println("  add " + item + " -> " + a); }
    }

    void siftUp(int i) {
      while (i > 0) {
        int parent = (i - 1) / 2;
        if (a.get(i).compareTo(a.get(parent)) >= 0) { break; }  // order OK, stop
        Collections.swap(a, i, parent);
        i = parent;
      }
    }

    void siftUpRec(int i) {
      if (i == 0) { return; }                                    // reached the root
      int parent = (i - 1) / 2;
      if (a.get(i).compareTo(a.get(parent)) >= 0) { return; }    // order OK
      Collections.swap(a, i, parent);
      siftUpRec(parent);
    }

    // ---------------------------------------------------------- removeMin
    /// The min is at the root. Move the LAST item to the root (keeps the SHAPE),
    /// then swap it down with its SMALLER child until order is fixed. O(log n).
    E removeMin() {
      E min = a.get(0);
      E last = a.remove(a.size() - 1);
      if (!a.isEmpty()) {
        a.set(0, last);
        if (recursive) { siftDownRec(0); } else { siftDown(0, a.size()); }
      }
      if (trace) { System.out.println("  removeMin " + min + " -> " + a); }
      return min;
    }

    void siftDown(int i, int n) {
      while (true) {
        int left = 2 * i + 1, right = 2 * i + 2, smallest = i;
        if (left < n && a.get(left).compareTo(a.get(smallest)) < 0) { smallest = left; }
        if (right < n && a.get(right).compareTo(a.get(smallest)) < 0) { smallest = right; }
        if (smallest == i) { return; }                           // both children bigger
        Collections.swap(a, i, smallest);
        i = smallest;
      }
    }

    void siftDownRec(int i) {
      int n = a.size();
      int left = 2 * i + 1, right = 2 * i + 2, smallest = i;
      if (left < n && a.get(left).compareTo(a.get(smallest)) < 0) { smallest = left; }
      if (right < n && a.get(right).compareTo(a.get(smallest)) < 0) { smallest = right; }
      if (smallest == i) { return; }
      Collections.swap(a, i, smallest);
      siftDownRec(smallest);
    }

    // ------------------------------------------------ bottom-up heapify O(n)
    /// Leaves are already heaps. Start at the LAST PARENT (n/2 - 1) and sift
    /// each node down, moving backwards to the root.
    static <E extends Comparable<E>> MinHeap<E> heapify(List<E> items) {
      MinHeap<E> h = new MinHeap<>(false);
      h.a.addAll(items);
      for (int i = h.a.size() / 2 - 1; i >= 0; i--) { h.siftDown(i, h.a.size()); }
      return h;
    }
  }

  // =========================================================================
  // IS IT A HEAP?
  // =========================================================================

  /// Array form, recursive: node i is OK if each existing child is >= it and
  /// each child's subtree is a heap. (Shape is automatic in an array with no gaps.)
  static <E extends Comparable<E>> boolean isMinHeap(List<E> a, int i) {
    int left = 2 * i + 1, right = 2 * i + 2;
    if (left < a.size()) {
      if (a.get(left).compareTo(a.get(i)) < 0 || !isMinHeap(a, left)) { return false; }
    }
    if (right < a.size()) {
      if (a.get(right).compareTo(a.get(i)) < 0 || !isMinHeap(a, right)) { return false; }
    }
    return true;
  }

  /// Array form, loop: every non-root i must be >= its parent.
  static <E extends Comparable<E>> boolean isMinHeapLoop(List<E> a) {
    for (int i = 1; i < a.size(); i++) {
      if (a.get(i).compareTo(a.get((i - 1) / 2)) < 0) { return false; }
    }
    return true;
  }

  /// Linked-node form (lab06 style). Must check SHAPE too: number nodes like
  /// the array would, and make sure no index is >= size (that means a gap).
  static class TNode {
    int data;
    TNode left, right;

    TNode(int d, TNode l, TNode r) { data = d; left = l; right = r; }
  }

  static int count(TNode t) { return t == null ? 0 : 1 + count(t.left) + count(t.right); }

  static boolean isComplete(TNode t, int index, int size) {
    if (t == null) { return true; }
    if (index >= size) { return false; }    // a node sits where the array would have a gap
    return isComplete(t.left, 2 * index + 1, size) && isComplete(t.right, 2 * index + 2, size);
  }

  static boolean hasMaxHeapOrder(TNode t) {
    if (t == null) { return true; }
    if (t.left != null && t.left.data > t.data) { return false; }
    if (t.right != null && t.right.data > t.data) { return false; }
    return hasMaxHeapOrder(t.left) && hasMaxHeapOrder(t.right);
  }

  static boolean isMaxHeap(TNode t) {
    return isComplete(t, 0, count(t)) && hasMaxHeapOrder(t);
  }

  // =========================================================================

  public static void main(String[] args) {
    int[] input = {40, 20, 50, 10, 30, 5};

    System.out.println("== add one at a time (iterative siftUp) ==");
    MinHeap<Integer> h = new MinHeap<>(false);
    h.trace = true;
    for (int x : input) { h.add(x); }

    System.out.println("\n== removeMin repeatedly (iterative siftDown) -> sorted order ==");
    while (h.size() > 0) { h.removeMin(); }

    System.out.println("\n== same thing with the RECURSIVE sift methods ==");
    MinHeap<Integer> hr = new MinHeap<>(true);
    for (int x : input) { hr.add(x); }
    System.out.println("  heap array: " + hr.a);
    StringBuilder out = new StringBuilder();
    while (hr.size() > 0) { out.append(hr.removeMin()).append(' '); }
    System.out.println("  removeMin order: " + out);

    System.out.println("\n== build: n adds vs bottom-up heapify (both valid, may differ) ==");
    List<Integer> list = new ArrayList<>(List.of(9, 8, 7, 6, 5, 4, 3, 2, 1));
    MinHeap<Integer> byAdds = new MinHeap<>(false);
    for (int x : list) { byAdds.add(x); }
    MinHeap<Integer> byHeapify = MinHeap.heapify(list);
    System.out.println("  input     : " + list);
    System.out.println("  by adds   : " + byAdds.a + "  isMinHeap=" + isMinHeap(byAdds.a, 0));
    System.out.println("  heapify   : " + byHeapify.a + "  isMinHeap=" + isMinHeapLoop(byHeapify.a));
    System.out.println("  input is a heap? " + isMinHeapLoop(list));

    System.out.println("\n== java.util.PriorityQueue (a binary min-heap) ==");
    PriorityQueue<Integer> pq = new PriorityQueue<>(list);
    StringBuilder s = new StringBuilder();
    while (!pq.isEmpty()) { s.append(pq.poll()).append(' '); }
    System.out.println("  poll order: " + s);
    PriorityQueue<Integer> maxPq = new PriorityQueue<>(Collections.reverseOrder());
    maxPq.addAll(list);
    System.out.println("  max-heap peek: " + maxPq.peek());

    System.out.println("\n== isMaxHeap on linked nodes (shape AND order) ==");
    //      9            9
    //     / \          / \
    //    5   7        5   7
    //   / \            \
    //  1   3            3     <- gap: not complete
    TNode good = new TNode(9, new TNode(5, new TNode(1, null, null), new TNode(3, null, null)),
        new TNode(7, null, null));
    TNode gap = new TNode(9, new TNode(5, null, new TNode(3, null, null)), new TNode(7, null, null));
    System.out.println("  good tree: " + isMaxHeap(good) + ",  tree with gap: " + isMaxHeap(gap));
  }
}
