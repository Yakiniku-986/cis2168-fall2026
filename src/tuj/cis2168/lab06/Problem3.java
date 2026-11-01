package tuj.cis2168.lab06;

// Lab 6, Problem 3 (Tree Traversals)
//
// Part C answer: the post-order traversal of the example tree is
//
//     C D B F E A
//
// The example tree, worked out from the pre-order (A B C D E F), in-order
// (C B D A E F) and level-order (A B E C D F) given in the assignment:
//
//         A
//        / \
//       B   E
//      / \   \
//     C   D   F
//
// Post-order visits left subtree, right subtree, then the node:
//   left of A:  C, D, then B
//   right of A: F, then E
//   finally A

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

////////////////////////////////////////////////////////////
// Part A (Pre-Order Traversal)
//
// The stack holds the subtrees we still have to visit, with the next one on
// top. To visit a node: pop it, return its data, then push its right child
// and then its left child. The left child is pushed last so it comes off
// first, which gives node, left subtree, right subtree.

class PreOrderIterator<E> implements Iterator<E> {
  private Deque<BinaryTree<E>> stack;

  PreOrderIterator(BinaryTree<E> tree) {
    stack = new ArrayDeque<>();
    if (tree != null) { stack.push(tree); }
  }

  @Override
  public boolean hasNext() {
    return !stack.isEmpty();
  }

  @Override
  public E next() {
    if (!hasNext()) { throw new NoSuchElementException(); }
    BinaryTree<E> node = stack.pop();
    if (node.right != null) { stack.push(node.right); }
    if (node.left != null) { stack.push(node.left); }
    return node.data;
  }
}

////////////////////////////////////////////////////////////
// Part B (In-Order Traversal)
//
// A node can only be visited after its whole left subtree. So we push the
// node and then keep walking left, pushing everything on the way. The top of
// the stack is then the leftmost unvisited node. When we pop and visit it,
// its right subtree comes next, so we push the left spine of that subtree.

class InOrderIterator<E> implements Iterator<E> {
  private Deque<BinaryTree<E>> stack;

  InOrderIterator(BinaryTree<E> tree) {
    stack = new ArrayDeque<>();
    pushLeftSpine(tree);
  }

  // Push tree, tree.left, tree.left.left, ... onto the stack.
  private void pushLeftSpine(BinaryTree<E> tree) {
    while (tree != null) {
      stack.push(tree);
      tree = tree.left;
    }
  }

  @Override
  public boolean hasNext() {
    return !stack.isEmpty();
  }

  @Override
  public E next() {
    if (!hasNext()) { throw new NoSuchElementException(); }
    BinaryTree<E> node = stack.pop();
    pushLeftSpine(node.right);
    return node.data;
  }
}

////////////////////////////////////////////////////////////
// Part D (Level-Order Traversal)
//
// Same idea as pre-order, but with a queue instead of a stack. A queue is
// first-in first-out, so all nodes on level k come out before any of their
// children (level k+1), and left children are queued before right children.
//
// The assignment calls this class LevelIterator in Part D and
// LevelOrderIterator in the submit line, so both names are provided.

class LevelOrderIterator<E> implements Iterator<E> {
  private Queue<BinaryTree<E>> storage;

  LevelOrderIterator(BinaryTree<E> tree) {
    storage = new ArrayDeque<>();
    if (tree != null) { storage.add(tree); }
  }

  @Override
  public boolean hasNext() {
    return !storage.isEmpty();
  }

  @Override
  public E next() {
    if (!hasNext()) { throw new NoSuchElementException(); }
    BinaryTree<E> node = storage.remove();
    if (node.left != null) { storage.add(node.left); }
    if (node.right != null) { storage.add(node.right); }
    return node.data;
  }
}

class LevelIterator<E> extends LevelOrderIterator<E> {
  LevelIterator(BinaryTree<E> tree) {
    super(tree);
  }
}

////////////////////////////////////////////////////////////

public class Problem3 {
}
