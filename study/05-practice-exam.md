# 5. Practice questions (with answers)

Try each one on paper first, then open the answer. Every answer here was checked by running code.

---

## Recursion

**R1.** What does `mystery(482)` return?

```java
static int mystery(int n) {
  if (n <= 0) { return 0; }
  return n % 10 + mystery(n / 10);
}
```

<details><summary>Answer</summary>

`14`. It sums the digits: `2 + mystery(48) = 2 + 8 + mystery(4) = 2 + 8 + 4 + mystery(0) = 14`.
</details>

**R2.** What does `f(3)` print?

```java
static void f(int n) {
  if (n == 0) { return; }
  f(n - 1);
  System.out.print(n + " ");
  f(n - 1);
}
```

<details><summary>Answer</summary>

`1 2 1 3 1 2 1`. `f(1)` prints `1`. `f(2)` = f(1), 2, f(1) = `1 2 1`. `f(3)` = f(2), 3, f(2). It makes `2^n - 1` prints, the same shape as Towers of Hanoi.
</details>

**R3.** Write `countGreater(Node node, int x)`: the number of nodes in a singly linked list whose data is greater than `x`. Recursively, then with a loop.

<details><summary>Answer</summary>

```java
static int countGreater(Node node, int x) {
  if (node == null) { return 0; }
  return (node.data > x ? 1 : 0) + countGreater(node.next, x);
}

static int countGreaterLoop(Node head, int x) {
  int count = 0;
  for (Node n = head; n != null; n = n.next) { if (n.data > x) { count++; } }
  return count;
}
```
</details>

**R4.** Why is naive `fib(n)` O(2ⁿ), and how do you make it O(n) without a loop?

<details><summary>Answer</summary>

Each call makes two calls, and the same sub-problems are recomputed many times (`fib(25)` makes 242,785 calls). Store each answer in a `HashMap<Integer, Long>` the first time it is computed and return the stored value on later calls (memoization).
</details>

---

## Trees

**T1.** Pre-order is `A B D E C F`, in-order is `D B E A F C`. Draw the tree and give the post-order.

<details><summary>Answer</summary>

```
        A
       / \
      B   C
     / \  /
    D  E F
```

Post-order: `D E B F C A`. (Root A comes first in pre-order. In in-order, `D B E` is left of A and `F C` is right. Next in pre-order is B, which splits `D B E` into D and E. Then C, and F is left of C in in-order.)
</details>

**T2.** Insert `40 20 60 10 30 50 70 25` into an empty BST, then remove `20`. Draw the result.

<details><summary>Answer</summary>

Before: 20 has children 10 and 30, and 30 has left child 25. 20 has two children, so it is replaced by its in-order successor (the min of its right subtree), 25.

```
          40
        /    \
      25      60
     /  \    /  \
    10  30  50  70
```

In-order is still sorted: `10 25 30 40 50 60 70`.
</details>

**T3.** Insert `15 9 12 4 7 3` one at a time into an empty **min-heap**. Show the array. Then do one `removeMin` and show the array.

<details><summary>Answer</summary>

After the adds: `[3, 7, 4, 15, 9, 12]`.

- 4 enters at index 3, swaps with 15 then 9.
- 7 enters at index 4, swaps with 9.
- 3 enters at index 5, swaps with 12 then 4.

removeMin returns 3. The last item (12) moves to the root and sifts down by swapping with its **smaller** child, 4: `[4, 7, 12, 15, 9]`.
</details>

**T4.** Build a **max-heap** from `[3, 9, 2, 1, 4, 5]` using bottom-up heapify.

<details><summary>Answer</summary>

`n = 6`, so start at index `6/2 - 1 = 2`.
- i=2: 2 vs child 5 → swap → `[3, 9, 5, 1, 4, 2]`
- i=1: 9 vs children 1, 4 → fine
- i=0: 3 vs children 9, 5 → swap with 9 → `[9, 3, 5, 1, 4, 2]`; 3 now vs children 1, 4 → swap with 4 → `[9, 4, 5, 1, 3, 2]`
</details>

**T5.** Build the expression tree for postfix `5 1 2 + 4 * + 3 -`. Give the infix and prefix forms and the value.

<details><summary>Answer</summary>

```
            -
          /   \
         +     3
        / \
       5   *
          / \
         +   4
        / \
       1   2
```

Infix `((5 + ((1 + 2) * 4)) - 3)`, prefix `- + 5 * + 1 2 4 3`, value `14`.
</details>

**T6.** This "is it a BST?" method has a bug. Give a tree it gets wrong, and fix it.

```java
static boolean isBST(Node t) {
  if (t == null) { return true; }
  if (t.left != null && t.left.key > t.key) { return false; }
  if (t.right != null && t.right.key < t.key) { return false; }
  return isBST(t.left) && isBST(t.right);
}
```

<details><summary>Answer</summary>

It only checks direct children. The tree `50 → left 30 → right 60` passes, but 60 is in 50's **left** subtree. Fix: pass down an allowed range, `isBST(t, lo, hi)`, and recurse with `isBST(t.left, lo, t.key)` and `isBST(t.right, t.key, hi)`. (See `BSTDemo.java`.)
</details>

---

## Sorting

**S1.** Show the array after each pass of **insertion sort** on `[5, 2, 4, 6, 1, 3]`.

<details><summary>Answer</summary>

```
i=1: [2, 5, 4, 6, 1, 3]
i=2: [2, 4, 5, 6, 1, 3]
i=3: [2, 4, 5, 6, 1, 3]   (6 stays, one comparison)
i=4: [1, 2, 4, 5, 6, 3]
i=5: [1, 2, 3, 4, 5, 6]
```
</details>

**S2.** Show the array after the first two passes of **selection sort** on `[64, 25, 12, 22, 11]`.

<details><summary>Answer</summary>

Pass 0: min is 11, swap with 64 → `[11, 25, 12, 22, 64]`.
Pass 1: min of the rest is 12, swap with 25 → `[11, 12, 25, 22, 64]`.
</details>

**S3.** Run one **Lomuto partition** (pivot = last) on `[10, 80, 30, 90, 40, 50, 70]`. What array and what index come back?

<details><summary>Answer</summary>

Pivot 70. Items ≤ 70 (10, 30, 40, 50) are swapped forward into the zone, then the pivot goes right after them:

`[10, 30, 40, 50, 70, 90, 80]`, returns index `4`.
</details>

**S4.** Show the merges merge sort does on `[5, 1, 4, 2, 3]` (split as `lo..mid`, `mid+1..hi` with `mid = (lo+hi)/2`).

<details><summary>Answer</summary>

```
split [5, 1, 4] [2, 3]
  split [5, 1] [4]
    split [5] [1]
    merge -> [1, 5]
  merge -> [1, 4, 5]
  split [2] [3]
  merge -> [2, 3]
merge -> [1, 2, 3, 4, 5]
```
</details>

**S5.** Short answers:
1. Which sorts are stable?
2. Which sort is O(n) on already-sorted input?
3. Which sort is O(n²) on already-sorted input when the pivot is the last element?
4. Which sort always does about n²/2 comparisons?
5. Which O(n log n) sort uses O(1) extra space?
6. Which sort is the best fit for a linked list, and why?

<details><summary>Answer</summary>

1. Insertion and merge.
2. Insertion.
3. Quick sort.
4. Selection.
5. Heap sort.
6. Merge sort: it only needs sequential access (find the middle with slow/fast pointers), merging just relinks nodes so no temp array is needed, and it's always O(n log n).
</details>

**S6.** You have a `HashMap<String, Integer>` of word counts. Print the words from most to least frequent, ties alphabetical. Why can't you "sort the HashMap"?

<details><summary>Answer</summary>

A hash table places keys by `hashCode`, so it has no order to rearrange. Copy the entries out and sort the copy:

```java
List<Map.Entry<String, Integer>> entries = new ArrayList<>(counts.entrySet());
entries.sort(Map.Entry.comparingByKey());                     // 1. alphabetical
entries.sort((x, y) -> y.getValue() - x.getValue());          // 2. stable sort by count
```

Step 2 keeps step 1's alphabetical order among equal counts only because `List.sort` (a merge sort) is **stable**. `MergeSorts.java` part 6 does this with our own merge sort.
</details>

---

## Hash tables

**H1.** Table size 7, `h(k) = k % 7`, **separate chaining**, new entries go at the **front** of the chain, no resizing. Insert `10, 22, 31, 4, 15, 28, 17`. Draw the table.

<details><summary>Answer</summary>

```
[0] 28
[1] 15 -> 22
[2]
[3] 17 -> 31 -> 10
[4] 4
[5]
[6]
```

Load factor 7/7 = 1.0. Chaining allows that, but a real table would have resized at 0.75.
</details>

**H2.** Table size 11, `h(k) = k % 11`, **linear probing**, no resizing. Insert `10, 22, 31, 4, 15, 28, 17, 88`. Draw the table. Then `remove(4)` by setting the slot to `null`, and search for `15`. What goes wrong, and what is the fix?

<details><summary>Answer</summary>

```
slot: 0   1   2   3   4   5   6   7   8   9   10
key : 22  88  _   _   4   15  28  17  _   31  10
```

- 15 hashes to 4 (taken by 4) → slot 5.
- 17 hashes to 6 (taken by 28) → slot 7.
- 88 hashes to 0 (taken by 22) → slot 1.

After setting slot 4 to `null`, searching for 15 starts at slot 4, sees an empty slot and stops: "not found", even though 15 is in slot 5. Fix: mark slot 4 with a **tombstone**. Searches skip over tombstones; inserts may reuse them.
</details>

**H3.** A class overrides `equals` but not `hashCode`. What happens when you use it in a `HashSet`?

<details><summary>Answer</summary>

Two equal objects almost always get different `hashCode`s (the default is per-object), so they go to different buckets. `contains` looks in the wrong bucket and returns `false`, and the set can hold "duplicates". Rule: if `a.equals(b)` then `a.hashCode() == b.hashCode()`.
</details>

**H4.** Write `firstUnique(int[] a)`: the first value that appears exactly once, or -1. Do it in O(n²) without a hash table, then in O(n) with one.

<details><summary>Answer</summary>

```java
static int firstUniqueBrute(int[] a) {
  for (int i = 0; i < a.length; i++) {
    int count = 0;
    for (int x : a) { if (x == a[i]) { count++; } }
    if (count == 1) { return a[i]; }
  }
  return -1;
}

static int firstUniqueHash(int[] a) {
  Map<Integer, Integer> counts = new HashMap<>();
  for (int x : a) { counts.merge(x, 1, Integer::sum); }   // pass 1: count
  for (int x : a) { if (counts.get(x) == 1) { return x; } } // pass 2: in original order
  return -1;
}
```

Pass 2 walks the **array**, not the map, because the map has no order.
</details>

**H5.** Pick the structure: (a) "is this username taken?" (b) "give me the smallest deadline, again and again" (c) "list all students with GPA between 3.0 and 3.5, in order" (d) "undo the last action".

<details><summary>Answer</summary>

(a) `HashSet`: O(1) lookup, no order needed. (b) heap / `PriorityQueue`: O(1) peek, O(log n) remove. (c) BST / `TreeMap` keyed by GPA: ordered range query. (d) stack.
</details>
