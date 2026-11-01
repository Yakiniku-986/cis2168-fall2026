package tuj.cis2168.lab06;

// Lab 6: the BinaryTree class given in the assignment.
// Shared by Problem1, Problem2 and Problem3 (same package), so it lives in
// its own file instead of being copied into each one.
class BinaryTree<E> {
  public E data;
  public BinaryTree<E> left;  // null if no left child
  public BinaryTree<E> right; // null if no right child

  BinaryTree(E data, BinaryTree<E> left, BinaryTree<E> right) {
    this.data = data;
    this.left = left;
    this.right = right;
  }
}
