# 2. Trees

| File | What's in it |
|---|---|
| [`BinaryTreeDemo.java`](../src/tuj/cis2168/study/trees/BinaryTreeDemo.java) | size/height/leaves recursive **and** iterative; pre/in/post/level order recursive, with a **stack**, with a **queue**, as an **Iterator** class, and on the **array** form |
| [`BSTDemo.java`](../src/tuj/cis2168/study/trees/BSTDemo.java) | contains/insert/min/max/remove recursive and iterative; isBST (wrong vs right); balanced build; BST as a map; tree sort |
| [`HeapDemo.java`](../src/tuj/cis2168/study/trees/HeapDemo.java) | array min-heap; siftUp/siftDown iterative and recursive; heapify vs n adds; isHeap on array and on linked nodes; `PriorityQueue` |
| [`ExpressionTreeDemo.java`](../src/tuj/cis2168/study/trees/ExpressionTreeDemo.java) | build from postfix (stack), prefix (recursion), infix (recursion); evaluate recursively, with a stack, and with no tree at all |

---

## 2.1 Vocabulary

```
          A          <- root (depth 0)
         / \
        B   E        <- B and E are children of A, siblings of each other
       / \   \
      C   D   F      <- leaves (no children). depth 2
```

- **Height** of a tree: edges on the longest root-to-leaf path. This tree: 2. Some books count **nodes** instead (this tree: 3). Empty tree is -1 (edges) or 0 (nodes). **Check your professor's convention.**
- **Full**: every node has 0 or 2 children.
- **Complete**: every level full except possibly the last, which is filled **left to right**. (Heaps must be complete.)
- **Perfect**: all levels completely full. Has `2^(h+1) - 1` nodes.
- A binary tree with `n` nodes has height between `floor(log2 n)` (bushy) and `n - 1` (a chain).

---

## 2.2 Binary tree operations: recursion is the natural fit

A tree is a recursive structure: a tree is either **empty** (`null`) or a **node with a left tree and a right tree**. So the base case is almost always `t == null`.

```java
static <E> int size(BinaryTree<E> t) {
  if (t == null) { return 0; }
  return 1 + size(t.left) + size(t.right);
}

static <E> int height(BinaryTree<E> t) {
  if (t == null) { return -1; }
  return 1 + Math.max(height(t.left), height(t.right));
}

static <E> boolean contains(BinaryTree<E> t, E target) {
  if (t == null) { return false; }
  if (t.data.equals(target)) { return true; }
  return contains(t.left, target) || contains(t.right, target);   // BOTH sides
}
```

Iterative versions replace the run-time stack with your own:
- **size** with a stack (any depth-first order works for counting)
- **height** with a queue, counting levels (breadth-first)

---

## 2.3 Traversals: four orders, four implementations

| Order | Rule | Example tree above |
|---|---|---|
| Pre-order | **N**ode, Left, Right | A B C D E F |
| In-order | Left, **N**ode, Right | C B D A E F |
| Post-order | Left, Right, **N**ode | C D B F E A |
| Level-order | top to bottom, left to right | A B E C D F |

Memory trick: the name says **when you visit the node** relative to its children (pre = before, in = between, post = after).

**Recursive** (all three differ only in where the `add` line goes):

```java
static <E> void inOrder(BinaryTree<E> t, List<E> out) {
  if (t == null) { return; }
  inOrder(t.left, out);
  out.add(t.data);          // move this line up for pre-order, down for post-order
  inOrder(t.right, out);
}
```

**Iterative with a stack** (depth-first):

```java
// Pre-order: pop, visit, push RIGHT then LEFT (so left comes off first)
while (!stack.isEmpty()) {
  BinaryTree<E> node = stack.pop();
  out.add(node.data);
  if (node.right != null) { stack.push(node.right); }
  if (node.left != null) { stack.push(node.left); }
}

// In-order: slide down the left spine pushing; pop, visit, go right
while (curr != null || !stack.isEmpty()) {
  while (curr != null) { stack.push(curr); curr = curr.left; }
  curr = stack.pop();
  out.add(curr.data);
  curr = curr.right;
}

// Post-order trick: do Node-Right-Left (mirror of pre-order), then reverse
```

**Iterative with a queue** (breadth-first = level order). Same loop as pre-order, but a `Queue` instead of a `Stack`, and add **left before right**:

```java
while (!queue.isEmpty()) {
  BinaryTree<E> node = queue.remove();
  out.add(node.data);
  if (node.left != null) { queue.add(node.left); }
  if (node.right != null) { queue.add(node.right); }
}
```

> **Stack ⇒ depth-first. Queue ⇒ breadth-first.** That one swap is the difference.

**As an Iterator** (lab06 style). Take the loop above and split it so each `next()` does one step. The in-order iterator keeps a stack of the "left spine":

```java
public E next() {
  BinaryTree<E> node = stack.pop();
  pushLeftSpine(node.right);   // next node = leftmost in my right subtree
  return node.data;
}
```

**On the array form.** Store the root at index 0; children of `i` are `2i+1` and `2i+2`; parent is `(i-1)/2`. The recursion is identical, with indices instead of links:

```
array form: [A, B, E, C, D, null, F]
             0  1  2  3  4   5    6
pre-order on array form : [A, B, C, D, E, F]
```

Lopsided trees waste array slots on `null`s, which is why only **complete** trees (heaps) are normally stored this way.

### Reconstructing a tree from traversals

Given **pre-order + in-order** (or post-order + in-order), you can rebuild the tree:
1. The **first** of pre-order (or **last** of post-order) is the root.
2. Find the root in in-order: everything left of it is the left subtree, everything right is the right subtree.
3. Recurse on each side.

Example: pre `A B C D E F`, in `C B D A E F`. Root A. In-order left of A: `C B D` (left subtree), right: `E F`. Next in pre-order is B, which splits `C B D` into C and D. Then E, with F on its right. That gives the tree above, so post-order is `C D B F E A`.

---

## 2.4 Binary Search Trees

**BST property:** for every node, **all** keys in the left subtree are smaller and **all** keys in the right subtree are larger. (Not just the direct children.)

Consequences:
- **In-order traversal of a BST is sorted.** (Tree sort = insert everything, then in-order.)
- Search goes **one** way at each node, so it costs `O(h)`.
- `h = O(log n)` if balanced, `O(n)` if you insert already-sorted data:

```
insert 1..15 in order -> height 14 (a linked list!)
buildBalanced(1..15)  -> height 3 (log2 16 - 1)
```

### Insert, recursive vs iterative

```java
// Recursive: "t = insert(t, key)" re-links the path on the way back up
static <K extends Comparable<K>> Node<K> insert(Node<K> t, K key) {
  if (t == null) { return new Node<>(key); }
  int cmp = key.compareTo(t.key);
  if (cmp < 0) { t.left = insert(t.left, key); }
  else if (cmp > 0) { t.right = insert(t.right, key); }
  return t;                                  // duplicates ignored (a set)
}

// Iterative: walk down remembering the parent, then attach
Node<K> parent = null, curr = root;
while (curr != null) {
  parent = curr;
  curr = key.compareTo(curr.key) < 0 ? curr.left : curr.right;
}
if (key.compareTo(parent.key) < 0) { parent.left = fresh; } else { parent.right = fresh; }
```

Search is a **tail call** (one call, nothing after it), so its loop version needs no stack at all.

### Insert trace

Insert `50 30 70 20 40 60 80 35 45`:

```
            50
          /    \
        30      70
       /  \    /  \
     20   40  60   80
         /  \
        35   45
```

### Remove: three cases

1. **Leaf**: just remove it.
2. **One child**: replace the node with its child.
3. **Two children**: copy in the **in-order successor** (smallest key in the right subtree), then remove that successor from the right subtree. (Using the in-order **predecessor**, the largest in the left subtree, is equally correct.)

```java
if (t.left == null) { return t.right; }     // cases 1 and 2
if (t.right == null) { return t.left; }     // case 2
K successor = min(t.right);                 // case 3
t.key = successor;
t.right = remove(t.right, successor);
```

After removing 30 (two children: successor 35 moves up), then 20 (leaf), then 70 (two children: successor 80 moves up):

```
        50
      /    \
    35      80
      \    /
      40  60
        \
        45
```

### "Is this a BST?" trap

Checking only `left.key < key < right.key` at each node is **wrong**:

```
      50
     /
   30
     \
      60     <- 60 > 30 is fine locally, but it's in 50's LEFT subtree
```

Correct ways: pass down an allowed `(lo, hi)` range, or check that in-order is strictly increasing.

```
sneaky tree: isBSTWrong = true, isBST(range) = false, isBSTInOrder = false
```

### BST as a map

Add a `value` field and you have `TreeMap`: `put` updates the value when the key already exists. `java.util.TreeSet`/`TreeMap` are self-balancing BSTs (red-black trees), so they guarantee `O(log n)`.

---

## 2.5 Heaps

Two rules:
1. **Shape**: complete binary tree.
2. **Order**: min-heap: parent ≤ children (min at the root). Max-heap: parent ≥ children.

A heap is **not** sorted, and **not** a BST: siblings have no order relative to each other. Only "every path from the root down is sorted".

Because it's complete, store it in an **array** (an `ArrayList`) with no gaps:

```
            5                index: 0  1   2   3   4   5
          /   \                     [5, 20, 10, 40, 30, 50]
        20     10
       /  \    /             children of i: 2i+1, 2i+2
      40  30  50             parent of i:   (i-1)/2
```

### add → siftUp (O(log n))
Put the item at the **end** (keeps the shape), then swap it **up** while it's smaller than its parent.

### removeMin → siftDown (O(log n))
Save the root. Move the **last** item to the root (keeps the shape), then swap it **down** with its **smaller** child (max-heap: larger child) until it's ≤ both children.

Trace from `HeapDemo.java`:

```
  add 40 -> [40]
  add 20 -> [20, 40]
  add 50 -> [20, 40, 50]
  add 10 -> [10, 20, 50, 40]        10 went in at index 3, swapped with 40, then with 20
  add 30 -> [10, 20, 50, 40, 30]
  add 5 -> [5, 20, 10, 40, 30, 50]  5 went in at index 5, swapped with 50, then with 10

  removeMin 5 -> [10, 20, 50, 40, 30]   50 moved to root, swapped down with 10
  removeMin 10 -> [20, 30, 50, 40]
  removeMin 20 -> [30, 40, 50]
  ...                                   out comes 5 10 20 30 40 50: SORTED (that's heap sort)
```

siftUp and siftDown are both written **iteratively and recursively** in the file. The recursive siftDown:

```java
void siftDownRec(int i) {
  int left = 2 * i + 1, right = 2 * i + 2, smallest = i;
  if (left < n && a.get(left).compareTo(a.get(smallest)) < 0) { smallest = left; }
  if (right < n && a.get(right).compareTo(a.get(smallest)) < 0) { smallest = right; }
  if (smallest == i) { return; }        // base case: order is fine here
  Collections.swap(a, i, smallest);
  siftDownRec(smallest);
}
```

### Building a heap: two ways

| Way | How | Cost |
|---|---|---|
| n adds | `add` each item | O(n log n) |
| **bottom-up heapify** | leaves are already heaps; siftDown every node from the last parent `n/2 - 1` back to 0 | **O(n)** |

They can give **different, equally valid** heaps:

```
  input     : [9, 8, 7, 6, 5, 4, 3, 2, 1]
  by adds   : [1, 2, 4, 3, 7, 8, 5, 9, 6]  isMinHeap=true
  heapify   : [1, 2, 3, 6, 5, 4, 7, 8, 9]  isMinHeap=true
```

### Is it a heap?
- **Array form**: for every `i ≥ 1`, `a[i] ≥ a[(i-1)/2]`. (Shape is automatic.)
- **Linked nodes** (lab06): you must check **shape** too. Number the nodes like the array would (root 0, children `2i+1`, `2i+2`); the tree is complete iff no node's number is ≥ the node count. Then check the order rule recursively.

`java.util.PriorityQueue` is a binary min-heap. Use `new PriorityQueue<>(Collections.reverseOrder())` for a max-heap.

---

## 2.6 Expression trees

Leaves are numbers, internal nodes are operators. The three traversals **are** the three notations:

```
(((3 + 4) * 2) / (7 - 1))

            /
          /   \
         *     -
        / \   / \
       +   2 7   1
      / \
     3   4

pre-order  (prefix)  : / * + 3 4 2 - 7 1
in-order   (infix)   : (((3 + 4) * 2) / (7 - 1))   <- print parens around every operator
post-order (postfix) : 3 4 + 2 * 7 1 - /
```

### Build from postfix with a stack (lab05 Part B)
- number → push a leaf
- operator → pop **right** first, then **left**, push `new Node(op, left, right)`
- at the end the stack holds exactly one tree

### Build from prefix with recursion
Read a token. If it's a number, return a leaf. If it's an operator, its left child is "the next whole expression" (recursive call) and its right child is "the expression after that" (another call).

### Evaluate, three ways

```java
// 1. Recursive = post-order: children first, then combine
static int evaluate(Node t) {
  if (t.isLeaf()) { return Integer.parseInt(t.token); }
  return apply(t.token, evaluate(t.left), evaluate(t.right));
}
// 2. Iterative: get post-order with a stack, then run the postfix evaluator
// 3. No tree: evaluate the postfix string directly with a stack (lecture10)
```

Stack trace for `3 4 + 2 * 7 1 - /`:

```
    3   stack(top first) = [3]
    4   stack(top first) = [4, 3]
    +   stack(top first) = [7]
    2   stack(top first) = [2, 7]
    *   stack(top first) = [14]
    7   stack(top first) = [7, 14]
    1   stack(top first) = [1, 7, 14]
    -   stack(top first) = [6, 14]
    /   stack(top first) = [2]
```

Watch the order for `-` and `/`: the **first** pop is the **right** operand. `7 1 -` is `7 - 1`, not `1 - 7`.

lab05 modelled this with polymorphism (`Expr` interface, `LiteralExpr`, `BinaryExpr`) where `evaluate()` is a method on each class. Same recursion: `LiteralExpr.evaluate()` is the base case, `BinaryExpr.evaluate()` is the recursive case.

---

## 2.7 Trees cheat sheet

| Operation | Binary tree | BST (balanced / worst) | Heap |
|---|---|---|---|
| search | O(n) | O(log n) / O(n) | O(n) |
| insert | n/a | O(log n) / O(n) | O(log n) |
| delete | n/a | O(log n) / O(n) | removeMin/Max: O(log n) |
| find min | O(n) | O(log n) / O(n) | **O(1)** |
| traversal | O(n) | O(n) (in-order is sorted) | O(n) |
| build from n items | | O(n log n) / O(n²) | **O(n)** heapify |
