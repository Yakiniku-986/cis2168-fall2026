package tuj.cis2168.study.trees;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

/// Study guide: general binary trees (see study/02-trees.md).
///
/// Every operation is shown more than one way:
///   * RECURSIVE on linked nodes  (the natural way: a tree is a recursive structure)
///   * ITERATIVE with a STACK     (depth-first: pre/in/post order)
///   * ITERATIVE with a QUEUE     (breadth-first: level order)
///   * as an ITERATOR class       (lazy, one node per next(), like lab06)
///   * stored in an ARRAY         (children of index i are 2i+1 and 2i+2)
///
/// The example tree (same one as lab06 Problem 3):
///
///         A
///        / \
///       B   E
///      / \   \
///     C   D   F
///
/// Run:  java src/tuj/cis2168/study/trees/BinaryTreeDemo.java
public class BinaryTreeDemo {

  /// Same shape as the lab06 BinaryTree class.
  static class BinaryTree<E> {
    E data;
    BinaryTree<E> left;   // null if no left child
    BinaryTree<E> right;  // null if no right child

    BinaryTree(E data, BinaryTree<E> left, BinaryTree<E> right) {
      this.data = data;
      this.left = left;
      this.right = right;
    }

    BinaryTree(E data) { this(data, null, null); }
  }

  // =========================================================================
  // SIZE / HEIGHT / LEAVES  (recursive: answer for me = combine answers for children)
  // =========================================================================

  /// size(t) = 0 if empty, else 1 + size(left) + size(right).  O(n)
  static <E> int size(BinaryTree<E> t) {
    if (t == null) { return 0; }
    return 1 + size(t.left) + size(t.right);
  }

  /// Height convention used here: empty tree = -1, single node = 0
  /// (counts EDGES on the longest root-to-leaf path).
  /// Some books use empty = 0, single node = 1 (counts NODES). Check which
  /// one your exam uses! The recursion is the same, only the base case changes.
  static <E> int height(BinaryTree<E> t) {
    if (t == null) { return -1; }
    return 1 + Math.max(height(t.left), height(t.right));
  }

  static <E> int countLeaves(BinaryTree<E> t) {
    if (t == null) { return 0; }
    if (t.left == null && t.right == null) { return 1; }   // a leaf
    return countLeaves(t.left) + countLeaves(t.right);
  }

  static <E> boolean contains(BinaryTree<E> t, E target) {
    if (t == null) { return false; }
    if (t.data.equals(target)) { return true; }
    return contains(t.left, target) || contains(t.right, target);  // must search BOTH sides
  }

  /// Size, iteratively, with an explicit stack (any traversal works for counting).
  static <E> int sizeLoop(BinaryTree<E> t) {
    if (t == null) { return 0; }
    Deque<BinaryTree<E>> stack = new ArrayDeque<>();
    stack.push(t);
    int count = 0;
    while (!stack.isEmpty()) {
      BinaryTree<E> node = stack.pop();
      count++;
      if (node.right != null) { stack.push(node.right); }
      if (node.left != null) { stack.push(node.left); }
    }
    return count;
  }

  /// Height, iteratively: count the LEVELS with a queue (BFS).
  static <E> int heightLoop(BinaryTree<E> t) {
    if (t == null) { return -1; }
    Queue<BinaryTree<E>> queue = new ArrayDeque<>();
    queue.add(t);
    int levels = 0;
    while (!queue.isEmpty()) {
      int levelSize = queue.size();       // everything currently queued is one level
      for (int i = 0; i < levelSize; i++) {
        BinaryTree<E> node = queue.remove();
        if (node.left != null) { queue.add(node.left); }
        if (node.right != null) { queue.add(node.right); }
      }
      levels++;
    }
    return levels - 1;                    // edges = levels - 1
  }

  // =========================================================================
  // TRAVERSALS, RECURSIVE
  //   pre-order : Node, Left, Right   (visit BEFORE children)
  //   in-order  : Left, Node, Right   (visit BETWEEN children)
  //   post-order: Left, Right, Node   (visit AFTER children)
  // =========================================================================

  static <E> void preOrder(BinaryTree<E> t, List<E> out) {
    if (t == null) { return; }
    out.add(t.data);
    preOrder(t.left, out);
    preOrder(t.right, out);
  }

  static <E> void inOrder(BinaryTree<E> t, List<E> out) {
    if (t == null) { return; }
    inOrder(t.left, out);
    out.add(t.data);
    inOrder(t.right, out);
  }

  static <E> void postOrder(BinaryTree<E> t, List<E> out) {
    if (t == null) { return; }
    postOrder(t.left, out);
    postOrder(t.right, out);
    out.add(t.data);
  }

  // =========================================================================
  // TRAVERSALS, ITERATIVE (explicit stack replaces the run-time stack)
  // =========================================================================

  /// Pre-order: pop, visit, push RIGHT then LEFT (so left pops first).
  static <E> List<E> preOrderLoop(BinaryTree<E> t) {
    List<E> out = new ArrayList<>();
    Deque<BinaryTree<E>> stack = new ArrayDeque<>();
    if (t != null) { stack.push(t); }
    while (!stack.isEmpty()) {
      BinaryTree<E> node = stack.pop();
      out.add(node.data);
      if (node.right != null) { stack.push(node.right); }
      if (node.left != null) { stack.push(node.left); }
    }
    return out;
  }

  /// In-order: go as far left as possible pushing nodes; pop one, visit it,
  /// then do the same thing starting from its right child.
  static <E> List<E> inOrderLoop(BinaryTree<E> t) {
    List<E> out = new ArrayList<>();
    Deque<BinaryTree<E>> stack = new ArrayDeque<>();
    BinaryTree<E> curr = t;
    while (curr != null || !stack.isEmpty()) {
      while (curr != null) {         // slide down the left spine
        stack.push(curr);
        curr = curr.left;
      }
      curr = stack.pop();
      out.add(curr.data);
      curr = curr.right;             // now handle the right subtree
    }
    return out;
  }

  /// Post-order, trick version: do "Node, Right, Left" with a stack (a mirrored
  /// pre-order), then REVERSE the result -> "Left, Right, Node".
  static <E> List<E> postOrderLoop(BinaryTree<E> t) {
    Deque<E> reversed = new ArrayDeque<>();
    Deque<BinaryTree<E>> stack = new ArrayDeque<>();
    if (t != null) { stack.push(t); }
    while (!stack.isEmpty()) {
      BinaryTree<E> node = stack.pop();
      reversed.push(node.data);                // pushing to the front reverses
      if (node.left != null) { stack.push(node.left); }
      if (node.right != null) { stack.push(node.right); }
    }
    return new ArrayList<>(reversed);
  }

  /// Level-order (breadth-first): a QUEUE instead of a stack.
  static <E> List<E> levelOrder(BinaryTree<E> t) {
    List<E> out = new ArrayList<>();
    Queue<BinaryTree<E>> queue = new ArrayDeque<>();
    if (t != null) { queue.add(t); }
    while (!queue.isEmpty()) {
      BinaryTree<E> node = queue.remove();
      out.add(node.data);
      if (node.left != null) { queue.add(node.left); }
      if (node.right != null) { queue.add(node.right); }
    }
    return out;
  }

  // =========================================================================
  // TRAVERSAL AS AN ITERATOR (lazy: the loop above, split across next() calls)
  // =========================================================================

  static class InOrderIterator<E> implements Iterator<E> {
    private final Deque<BinaryTree<E>> stack = new ArrayDeque<>();

    InOrderIterator(BinaryTree<E> root) { pushLeftSpine(root); }

    private void pushLeftSpine(BinaryTree<E> node) {
      while (node != null) {
        stack.push(node);
        node = node.left;
      }
    }

    @Override
    public boolean hasNext() { return !stack.isEmpty(); }

    @Override
    public E next() {
      if (!hasNext()) { throw new NoSuchElementException(); }
      BinaryTree<E> node = stack.pop();
      pushLeftSpine(node.right);       // the next node is the leftmost of my right subtree
      return node.data;
    }
  }

  // =========================================================================
  // ARRAY REPRESENTATION
  //   root at index 0
  //   left child of i  = 2i + 1
  //   right child of i = 2i + 2
  //   parent of i      = (i - 1) / 2
  // Missing nodes are null, so a lopsided tree wastes space. A COMPLETE tree
  // (every level full except the last, filled left to right) has no gaps,
  // which is why HEAPS are stored this way.
  // =========================================================================

  static <E> List<E> toArray(BinaryTree<E> t) {
    List<E> arr = new ArrayList<>();
    fill(t, 0, arr);
    return arr;
  }

  private static <E> void fill(BinaryTree<E> t, int i, List<E> arr) {
    if (t == null) { return; }
    while (arr.size() <= i) { arr.add(null); }
    arr.set(i, t.data);
    fill(t.left, 2 * i + 1, arr);
    fill(t.right, 2 * i + 2, arr);
  }

  /// Pre-order directly on the array form: same recursion, indices instead of links.
  static <E> void preOrderArray(List<E> arr, int i, List<E> out) {
    if (i >= arr.size() || arr.get(i) == null) { return; }  // "null child"
    out.add(arr.get(i));
    preOrderArray(arr, 2 * i + 1, out);
    preOrderArray(arr, 2 * i + 2, out);
  }

  static <E> int sizeArray(List<E> arr, int i) {
    if (i >= arr.size() || arr.get(i) == null) { return 0; }
    return 1 + sizeArray(arr, 2 * i + 1) + sizeArray(arr, 2 * i + 2);
  }

  // =========================================================================
  // Pretty printer (sideways: root on the left, right subtree on top)
  // =========================================================================

  static <E> void printSideways(BinaryTree<E> t, String indent) {
    if (t == null) { return; }
    printSideways(t.right, indent + "    ");
    System.out.println(indent + t.data);
    printSideways(t.left, indent + "    ");
  }

  // =========================================================================

  public static void main(String[] args) {
    BinaryTree<String> tree =
        new BinaryTree<>("A",
            new BinaryTree<>("B", new BinaryTree<>("C"), new BinaryTree<>("D")),
            new BinaryTree<>("E", null, new BinaryTree<>("F")));

    System.out.println("tree (sideways, root on the left):");
    printSideways(tree, "  ");

    System.out.println("\nsize       = " + size(tree) + "   sizeLoop   = " + sizeLoop(tree));
    System.out.println("height     = " + height(tree) + "   heightLoop = " + heightLoop(tree));
    System.out.println("leaves     = " + countLeaves(tree));
    System.out.println("contains D = " + contains(tree, "D") + ", contains Z = " + contains(tree, "Z"));

    List<String> pre = new ArrayList<>(), in = new ArrayList<>(), post = new ArrayList<>();
    preOrder(tree, pre);
    inOrder(tree, in);
    postOrder(tree, post);
    System.out.println("\n            recursive            iterative (stack/queue)");
    System.out.println("pre-order : " + pre + "   " + preOrderLoop(tree));
    System.out.println("in-order  : " + in + "   " + inOrderLoop(tree));
    System.out.println("post-order: " + post + "   " + postOrderLoop(tree));
    System.out.println("level     : (no natural recursion)  " + levelOrder(tree));

    System.out.print("\nInOrderIterator: ");
    Iterator<String> it = new InOrderIterator<>(tree);
    while (it.hasNext()) { System.out.print(it.next() + " "); }
    System.out.println();

    List<String> arr = toArray(tree);
    System.out.println("\narray form (index: 0..n): " + arr);
    List<String> preArr = new ArrayList<>();
    preOrderArray(arr, 0, preArr);
    System.out.println("pre-order on array form : " + preArr);
    System.out.println("size on array form      : " + sizeArray(arr, 0));
  }
}
