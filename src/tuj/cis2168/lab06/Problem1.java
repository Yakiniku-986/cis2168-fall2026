package tuj.cis2168.lab06;

// Lab 6, Problem 1 (Measuring Trees)

public class Problem1 {

  // Part A (Size)
  // Base case:      the empty tree (null) has no nodes.
  // Recursive case: one for the root, plus the sizes of both subtrees.
  // Runtime:        O(n), every node is visited once.
  public static <E> int size(BinaryTree<E> tree) {
    if (tree == null) { return 0; }
    return 1 + size(tree.left) + size(tree.right);
  }

  // Part B (Height)
  // Height = number of edges on the longest root-to-leaf path, so a single
  // leaf has height 0. The empty tree gets height -1, which makes the
  // recursive case work out: a leaf is 1 + max(-1, -1) = 0.
  // Runtime:        O(n), every node is visited once.
  public static <E> int height(BinaryTree<E> tree) {
    if (tree == null) { return -1; }
    return 1 + Math.max(height(tree.left), height(tree.right));
  }
}
