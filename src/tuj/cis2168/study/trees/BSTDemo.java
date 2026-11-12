package tuj.cis2168.study.trees;

import java.util.ArrayList;
import java.util.List;

/// Study guide: Binary Search Trees (see study/02-trees.md).
///
/// BST rule: for EVERY node, everything in its left subtree is smaller and
/// everything in its right subtree is larger. (Not just the children!)
///
/// Each operation is shown RECURSIVE and ITERATIVE:
///   contains, insert, min, max, remove, plus isBST, buildBalanced, a TreeMap-style
///   key/value version, and "sort with a BST" (tree sort).
///
/// Running times: O(h) where h = height.
///   balanced tree: h = O(log n)      degenerate (inserted in sorted order): h = O(n)
///
/// Run:  java src/tuj/cis2168/study/trees/BSTDemo.java
public class BSTDemo {

  static class Node<K extends Comparable<K>> {
    K key;
    Node<K> left, right;

    Node(K key) { this.key = key; }
  }

  // =========================================================================
  // CONTAINS
  // =========================================================================

  /// Recursive: go left or right, never both. That is why it's O(h), not O(n).
  static <K extends Comparable<K>> boolean contains(Node<K> t, K key) {
    if (t == null) { return false; }
    int cmp = key.compareTo(t.key);
    if (cmp == 0) { return true; }
    if (cmp < 0) { return contains(t.left, key); }
    return contains(t.right, key);
  }

  /// Iterative: the recursion only ever makes ONE call at the end (tail call),
  /// so a simple loop replaces it with no stack.
  static <K extends Comparable<K>> boolean containsLoop(Node<K> t, K key) {
    while (t != null) {
      int cmp = key.compareTo(t.key);
      if (cmp == 0) { return true; }
      t = (cmp < 0) ? t.left : t.right;
    }
    return false;
  }

  // =========================================================================
  // INSERT  (returns the new root of the subtree: the "t = insert(t, key)" idiom)
  // =========================================================================

  static <K extends Comparable<K>> Node<K> insert(Node<K> t, K key) {
    if (t == null) { return new Node<>(key); }        // found the empty spot
    int cmp = key.compareTo(t.key);
    if (cmp < 0) { t.left = insert(t.left, key); }
    else if (cmp > 0) { t.right = insert(t.right, key); }
    // cmp == 0: duplicate, ignore (a SET)
    return t;
  }

  /// Iterative insert: walk down remembering the PARENT, then attach.
  static <K extends Comparable<K>> Node<K> insertLoop(Node<K> root, K key) {
    Node<K> fresh = new Node<>(key);
    if (root == null) { return fresh; }
    Node<K> parent = null, curr = root;
    while (curr != null) {
      parent = curr;
      int cmp = key.compareTo(curr.key);
      if (cmp == 0) { return root; }               // duplicate
      curr = (cmp < 0) ? curr.left : curr.right;
    }
    if (key.compareTo(parent.key) < 0) { parent.left = fresh; } else { parent.right = fresh; }
    return root;
  }

  // =========================================================================
  // MIN / MAX: keep going left / right
  // =========================================================================

  static <K extends Comparable<K>> K min(Node<K> t) {
    if (t.left == null) { return t.key; }
    return min(t.left);
  }

  static <K extends Comparable<K>> K maxLoop(Node<K> t) {
    while (t.right != null) { t = t.right; }
    return t.key;
  }

  // =========================================================================
  // REMOVE: the three cases
  //   1. leaf             -> just remove it (return null)
  //   2. one child        -> replace the node with that child
  //   3. two children     -> copy in the in-order SUCCESSOR (min of right subtree),
  //                          then remove the successor from the right subtree
  // =========================================================================

  static <K extends Comparable<K>> Node<K> remove(Node<K> t, K key) {
    if (t == null) { return null; }                  // not found
    int cmp = key.compareTo(t.key);
    if (cmp < 0) { t.left = remove(t.left, key); }
    else if (cmp > 0) { t.right = remove(t.right, key); }
    else {
      if (t.left == null) { return t.right; }        // cases 1 and 2
      if (t.right == null) { return t.left; }        // case 2
      K successor = min(t.right);                    // case 3
      t.key = successor;
      t.right = remove(t.right, successor);
    }
    return t;
  }

  // =========================================================================
  // IS IT A BST?  The classic WRONG answer only checks children.
  // =========================================================================

  /// WRONG: checks left.key < key < right.key at each node only.
  static <K extends Comparable<K>> boolean isBSTWrong(Node<K> t) {
    if (t == null) { return true; }
    if (t.left != null && t.left.key.compareTo(t.key) >= 0) { return false; }
    if (t.right != null && t.right.key.compareTo(t.key) <= 0) { return false; }
    return isBSTWrong(t.left) && isBSTWrong(t.right);
  }

  /// RIGHT: pass down the allowed range (lo, hi). null = unbounded.
  static <K extends Comparable<K>> boolean isBST(Node<K> t, K lo, K hi) {
    if (t == null) { return true; }
    if (lo != null && t.key.compareTo(lo) <= 0) { return false; }
    if (hi != null && t.key.compareTo(hi) >= 0) { return false; }
    return isBST(t.left, lo, t.key) && isBST(t.right, t.key, hi);
  }

  /// RIGHT, another way: in-order traversal must be strictly increasing.
  static <K extends Comparable<K>> boolean isBSTInOrder(Node<K> t) {
    List<K> keys = new ArrayList<>();
    inOrder(t, keys);
    for (int i = 1; i < keys.size(); i++) {
      if (keys.get(i - 1).compareTo(keys.get(i)) >= 0) { return false; }
    }
    return true;
  }

  static <K extends Comparable<K>> void inOrder(Node<K> t, List<K> out) {
    if (t == null) { return; }
    inOrder(t.left, out);
    out.add(t.key);
    inOrder(t.right, out);
  }

  // =========================================================================
  // BUILD A BALANCED BST FROM A SORTED ARRAY (divide in half, like binary search)
  // =========================================================================

  static <K extends Comparable<K>> Node<K> buildBalanced(List<K> sorted, int lo, int hi) {
    if (lo > hi) { return null; }
    int mid = (lo + hi) / 2;
    Node<K> t = new Node<>(sorted.get(mid));
    t.left = buildBalanced(sorted, lo, mid - 1);
    t.right = buildBalanced(sorted, mid + 1, hi);
    return t;
  }

  static <K extends Comparable<K>> int height(Node<K> t) {
    return t == null ? -1 : 1 + Math.max(height(t.left), height(t.right));
  }

  // =========================================================================
  // BST AS A MAP (key -> value), like java.util.TreeMap. Same code, extra field.
  // =========================================================================

  static class MapNode<K extends Comparable<K>, V> {
    K key;
    V value;
    MapNode<K, V> left, right;

    MapNode(K key, V value) {
      this.key = key;
      this.value = value;
    }
  }

  static <K extends Comparable<K>, V> MapNode<K, V> put(MapNode<K, V> t, K key, V value) {
    if (t == null) { return new MapNode<>(key, value); }
    int cmp = key.compareTo(t.key);
    if (cmp < 0) { t.left = put(t.left, key, value); }
    else if (cmp > 0) { t.right = put(t.right, key, value); }
    else { t.value = value; }                          // existing key: overwrite value
    return t;
  }

  static <K extends Comparable<K>, V> V get(MapNode<K, V> t, K key) {
    while (t != null) {
      int cmp = key.compareTo(t.key);
      if (cmp == 0) { return t.value; }
      t = cmp < 0 ? t.left : t.right;
    }
    return null;
  }

  // =========================================================================

  static <K extends Comparable<K>> void printSideways(Node<K> t, String indent) {
    if (t == null) { return; }
    printSideways(t.right, indent + "    ");
    System.out.println(indent + t.key);
    printSideways(t.left, indent + "    ");
  }

  public static void main(String[] args) {
    Node<Integer> root = null;
    int[] keys = {50, 30, 70, 20, 40, 60, 80, 35, 45};
    for (int k : keys) { root = insert(root, k); }
    System.out.println("insert 50 30 70 20 40 60 80 35 45 (recursive):");
    printSideways(root, "  ");

    Node<Integer> root2 = null;
    for (int k : keys) { root2 = insertLoop(root2, k); }
    List<Integer> a = new ArrayList<>(), b = new ArrayList<>();
    inOrder(root, a);
    inOrder(root2, b);
    System.out.println("in-order (recursive insert) = " + a);
    System.out.println("in-order (iterative insert) = " + b + "   <- in-order of a BST is SORTED");

    System.out.println("\ncontains(45) = " + contains(root, 45) + ", containsLoop(55) = " + containsLoop(root, 55));
    System.out.println("min = " + min(root) + ", max = " + maxLoop(root));

    root = remove(root, 30);   // two children (20, 40) -> successor 35 moves up
    root = remove(root, 20);   // leaf
    root = remove(root, 70);   // two children (60, 80) -> successor 80 moves up
    System.out.println("\nafter remove 30 (two children), 20 (leaf), 70 (two children):");
    printSideways(root, "  ");

    // A tree that passes the WRONG check but is NOT a BST:
    //        50
    //       /
    //     30
    //       \
    //        60   <- 60 > 30 is fine locally, but 60 is in 50's LEFT subtree
    Node<Integer> sneaky = new Node<>(50);
    sneaky.left = new Node<>(30);
    sneaky.left.right = new Node<>(60);
    System.out.println("\nsneaky tree: isBSTWrong = " + isBSTWrong(sneaky)
        + ", isBST(range) = " + isBST(sneaky, null, null)
        + ", isBSTInOrder = " + isBSTInOrder(sneaky));

    // Degenerate vs balanced
    Node<Integer> chain = null;
    List<Integer> sorted = new ArrayList<>();
    for (int i = 1; i <= 15; i++) { chain = insert(chain, i); sorted.add(i); }
    Node<Integer> balanced = buildBalanced(sorted, 0, sorted.size() - 1);
    System.out.println("\ninsert 1..15 in order -> height " + height(chain) + " (a linked list!)");
    System.out.println("buildBalanced(1..15)  -> height " + height(balanced) + " (log2 16 - 1)");

    // Tree sort: insert everything, read back in-order. O(n log n) average, O(n^2) worst.
    Node<String> words = null;
    for (String w : "pear apple fig kiwi banana".split(" ")) { words = insert(words, w); }
    List<String> sortedWords = new ArrayList<>();
    inOrder(words, sortedWords);
    System.out.println("\ntree sort of words: " + sortedWords);

    // BST as a map
    MapNode<String, Integer> counts = null;
    for (String w : "to be or not to be".split(" ")) {
      Integer c = get(counts, w);
      counts = put(counts, w, c == null ? 1 : c + 1);
    }
    System.out.println("word counts via BST map: to=" + get(counts, "to") + " be=" + get(counts, "be")
        + " or=" + get(counts, "or") + " not=" + get(counts, "not"));
  }
}
