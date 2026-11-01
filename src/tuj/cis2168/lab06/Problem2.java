package tuj.cis2168.lab06;

// Lab 6, Problem 2 (Invariants)
// The empty tree (null) satisfies both invariants, since it has no nodes
// that could break them.

public class Problem2 {

  // Part A (Binary Search Tree Invariant)
  // Checking only "left child < node < right child" is not enough: a node deep
  // in the left subtree could still be bigger than the root. So we pass down
  // the range (low, high) that every node in the current subtree must fall
  // strictly inside. null means "no bound on that side".
  //   * Going left, the current node becomes the new upper bound.
  //   * Going right, the current node becomes the new lower bound.
  // Runtime: O(n).
  public static <E extends Comparable<E>> boolean isBinarySearchTree(BinaryTree<E> tree) {
    return isBinarySearchTree(tree, null, null);
  }

  private static <E extends Comparable<E>> boolean isBinarySearchTree(
      BinaryTree<E> tree, E low, E high) {
    if (tree == null) { return true; }
    if (low != null && tree.data.compareTo(low) <= 0) { return false; }
    if (high != null && tree.data.compareTo(high) >= 0) { return false; }
    return isBinarySearchTree(tree.left, low, tree.data)
        && isBinarySearchTree(tree.right, tree.data, high);
  }

  // Part B (Heap Invariant)
  // ">= every descendant" follows from ">= each child" applied at every node,
  // because >= is transitive: if root >= child and child >= grandchild, then
  // root >= grandchild. So it is enough to compare each node with its
  // children and recurse.
  // Runtime: O(n).
  public static <E extends Comparable<E>> boolean isMaxHeap(BinaryTree<E> tree) {
    if (tree == null) { return true; }
    if (tree.left != null && tree.data.compareTo(tree.left.data) < 0) { return false; }
    if (tree.right != null && tree.data.compareTo(tree.right.data) < 0) { return false; }
    return isMaxHeap(tree.left) && isMaxHeap(tree.right);
  }
}
