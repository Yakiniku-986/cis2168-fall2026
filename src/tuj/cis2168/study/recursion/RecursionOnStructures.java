package tuj.cis2168.study.recursion;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/// Study guide: ONE problem, MANY structures (see study/01-recursion.md).
///
/// Problems: size, sum, contains, max, print-in-reverse, reverse-the-list.
/// Each one is solved on:
///   (A) an ARRAY            - recursion moves an index:   i -> i + 1
///   (B) a LINKED LIST       - recursion moves a node:     node -> node.next
///   (C) an ITERATOR         - recursion consumes:         it.next()
///   (D) a LOOP              - the iterative version of each
///
/// The pattern is always the same:
///     base case      : "nothing left"   (i == a.length / node == null / !it.hasNext())
///     recursive case : first item  (+)  answer for the rest
///
/// Run:  java src/tuj/cis2168/study/recursion/RecursionOnStructures.java
public class RecursionOnStructures {

  /// A bare singly linked list node, same idea as Node<T> in lecture07/10.
  static class Node<T> {
    T data;
    Node<T> next;

    Node(T data, Node<T> next) {
      this.data = data;
      this.next = next;
    }
  }

  @SafeVarargs
  static <T> Node<T> listOf(T... items) {
    Node<T> head = null;
    for (int i = items.length - 1; i >= 0; i--) { head = new Node<>(items[i], head); }
    return head;
  }

  static <T> String show(Node<T> head) {
    StringBuilder sb = new StringBuilder("[");
    for (Node<T> n = head; n != null; n = n.next) {
      sb.append(n.data);
      if (n.next != null) { sb.append(" -> "); }
    }
    return sb.append("]").toString();
  }

  // =========================================================================
  // SIZE
  // =========================================================================

  static int sizeArray(int[] a, int i) {           // (A) recursion on array
    if (i == a.length) { return 0; }
    return 1 + sizeArray(a, i + 1);
  }

  static <T> int sizeList(Node<T> node) {          // (B) recursion on linked list
    if (node == null) { return 0; }
    return 1 + sizeList(node.next);
  }

  static <T> int sizeIter(Iterator<T> it) {        // (C) recursion on an iterator
    if (!it.hasNext()) { return 0; }
    it.next();                                     // consume one element = peel one layer
    return 1 + sizeIter(it);
  }

  static <T> int sizeListLoop(Node<T> head) {      // (D) loop on linked list
    int count = 0;
    for (Node<T> n = head; n != null; n = n.next) { count++; }
    return count;
  }

  // =========================================================================
  // SUM
  // =========================================================================

  static int sumArray(int[] a, int i) {
    if (i == a.length) { return 0; }
    return a[i] + sumArray(a, i + 1);
  }

  static int sumList(Node<Integer> node) {
    if (node == null) { return 0; }
    return node.data + sumList(node.next);
  }

  static int sumIter(Iterator<Integer> it) {
    if (!it.hasNext()) { return 0; }
    int first = it.next();
    return first + sumIter(it);
  }

  static int sumLoopIterable(Iterable<Integer> things) {   // works for ANY Iterable:
    int total = 0;                                         // ArrayList, LinkedList,
    for (int x : things) { total += x; }                   // HashSet, your own lists...
    return total;
  }

  // =========================================================================
  // CONTAINS (early exit: two base cases)
  // =========================================================================

  static boolean containsArray(int[] a, int i, int target) {
    if (i == a.length) { return false; }        // base case 1: ran out
    if (a[i] == target) { return true; }        // base case 2: found it
    return containsArray(a, i + 1, target);
  }

  static <T> boolean containsList(Node<T> node, T target) {
    if (node == null) { return false; }
    if (node.data.equals(target)) { return true; }
    return containsList(node.next, target);
  }

  static <T> boolean containsIter(Iterator<T> it, T target) {
    if (!it.hasNext()) { return false; }
    if (it.next().equals(target)) { return true; }
    return containsIter(it, target);
  }

  static <T> boolean containsListLoop(Node<T> head, T target) {
    for (Node<T> n = head; n != null; n = n.next) {
      if (n.data.equals(target)) { return true; }
    }
    return false;
  }

  // =========================================================================
  // MAX (generic, uses Comparable)
  // =========================================================================

  static <T extends Comparable<T>> T maxList(Node<T> node) {
    if (node.next == null) { return node.data; }        // base: one node
    T restMax = maxList(node.next);
    return node.data.compareTo(restMax) >= 0 ? node.data : restMax;
  }

  static <T extends Comparable<T>> T maxIter(Iterator<T> it, T bestSoFar) {
    if (!it.hasNext()) { return bestSoFar; }            // accumulator style
    T x = it.next();
    return maxIter(it, x.compareTo(bestSoFar) > 0 ? x : bestSoFar);
  }

  // =========================================================================
  // PRINT IN REVERSE: recursion gives you a FREE STACK
  // =========================================================================

  /// Recursion: print AFTER the call -> reverse order. O(n) stack frames.
  static <T> void printReverseList(Node<T> node) {
    if (node == null) { return; }
    printReverseList(node.next);
    System.out.print(node.data + " ");
  }

  /// Iterative equivalent needs an EXPLICIT stack (that's what the run-time
  /// stack was doing for us).
  static <T> void printReverseListLoop(Node<T> head) {
    java.util.ArrayDeque<T> stack = new java.util.ArrayDeque<>();
    for (Node<T> n = head; n != null; n = n.next) { stack.push(n.data); }
    while (!stack.isEmpty()) { System.out.print(stack.pop() + " "); }
  }

  // =========================================================================
  // REVERSE THE LINKED LIST ITSELF (a favourite exam question)
  // =========================================================================

  /// Recursive: reverse the rest, then hook the current node on the end.
  ///   1 -> 2 -> 3
  ///   reverse(2 -> 3) gives 3 -> 2, and node 2 is now the tail
  ///   so: 2.next = 1, 1.next = null   -> 3 -> 2 -> 1
  static <T> Node<T> reverseList(Node<T> head) {
    if (head == null || head.next == null) { return head; }
    Node<T> newHead = reverseList(head.next);
    head.next.next = head;   // the node after me now points back at me
    head.next = null;        // I'm the new tail (for now)
    return newHead;
  }

  /// Iterative: three pointers (prev, curr, next).
  static <T> Node<T> reverseListLoop(Node<T> head) {
    Node<T> prev = null, curr = head;
    while (curr != null) {
      Node<T> next = curr.next;
      curr.next = prev;
      prev = curr;
      curr = next;
    }
    return prev;
  }

  // =========================================================================
  // GET LAST / GET k-th
  // =========================================================================

  static <T> T getRecursive(Node<T> node, int k) {
    if (node == null) { throw new IndexOutOfBoundsException(); }
    if (k == 0) { return node.data; }
    return getRecursive(node.next, k - 1);
  }

  // =========================================================================

  public static void main(String[] args) {
    int[] arr = {4, 8, 15, 16, 23, 42};
    Node<Integer> list = listOf(4, 8, 15, 16, 23, 42);
    List<Integer> arrayList = new ArrayList<>(List.of(4, 8, 15, 16, 23, 42));

    System.out.println("data: " + show(list));
    System.out.println();
    System.out.println("problem   | array | linked list | iterator | loop");
    System.out.println("----------+-------+-------------+----------+-----");
    System.out.printf("size      | %5d | %11d | %8d | %4d%n",
        sizeArray(arr, 0), sizeList(list), sizeIter(arrayList.iterator()), sizeListLoop(list));
    System.out.printf("sum       | %5d | %11d | %8d | %4d%n",
        sumArray(arr, 0), sumList(list), sumIter(arrayList.iterator()), sumLoopIterable(arrayList));
    System.out.printf("has 23?   | %5b | %11b | %8b | %4b%n",
        containsArray(arr, 0, 23), containsList(list, 23),
        containsIter(arrayList.iterator(), 23), containsListLoop(list, 23));
    System.out.printf("max       |     - | %11d | %8d |    -%n",
        maxList(list), maxIter(arrayList.iterator(), Integer.MIN_VALUE));
    System.out.println("get(2)    = " + getRecursive(list, 2));

    System.out.print("\nprintReverseList     : "); printReverseList(list); System.out.println();
    System.out.print("printReverseListLoop : "); printReverseListLoop(list); System.out.println();

    Node<Integer> r = reverseList(listOf(1, 2, 3, 4));
    System.out.println("\nreverseList(1..4)     = " + show(r));
    System.out.println("reverseListLoop(back) = " + show(reverseListLoop(r)));
  }
}
