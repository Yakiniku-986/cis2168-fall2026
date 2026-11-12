# CIS 2168 exam study guide: recursion, trees, sorting, hash tables

This folder is a study guide for recursion, trees (binary tree, BST, heap, expression tree), the five sorts (insertion, selection, heap, merge with `merge()`, quick with `partition()`), and hash tables (sets and maps, chaining, linear probing).

The main idea is **one problem, many implementations**. Each algorithm is written several ways: on an **array**, on a **linked list**, with **iterators**, **recursively and iteratively**, and **with a hash table**. Seeing the same algorithm in different forms shows which part is the algorithm itself and which part comes from the data structure.

## Chapters

1. [Recursion](01-recursion.md): the recipe, unwinding, three shapes of recursion, Fibonacci 3 ways, reversing a linked list, backtracking
2. [Trees](02-trees.md): traversals 4 ways, BST insert/remove/isBST, heaps (siftUp/siftDown, heapify), expression trees
3. [Sorting](03-sorting.md): the 5 sorts, each with traces and every variant, plus the Big-O table checked by experiment
4. [Hash tables](04-hash-tables.md): chaining vs linear probing, tombstones, hashCode/equals, the same problem solved with 4 structures
5. [Practice questions](05-practice-exam.md): exam-style questions with hidden answers

## The code

All code is in [`src/tuj/cis2168/study/`](../src/tuj/cis2168/study/). Every file has a `main` that prints a trace, so you can run it and compare with what you worked out by hand.

| Topic | Files |
|---|---|
| recursion | `RecursionBasics`, `RecursionOnStructures`, `Backtracking` |
| trees | `BinaryTreeDemo`, `BSTDemo`, `HeapDemo`, `ExpressionTreeDemo` |
| sorting | `InsertionSorts`, `SelectionSorts`, `HeapSorts`, `MergeSorts`, `QuickSorts`, `SortComparison` |
| hashing | `ChainingHashMap`, `LinearProbingHashMap`, `HashSetsAndMaps` |

Each file stands on its own (no imports from other study files), so any one of them runs directly from the repo root:

```bash
java src/tuj/cis2168/study/sorting/QuickSorts.java
```

You can also run them from your IDE (the `main` method), or compile everything with `javac -d out $(find src/tuj/cis2168/study -name '*.java')`.

## The full set of variations

Each ✔ is a working implementation in the code.

| | Array (loop) | Array (recursive) | Linked list | Iterator | Hash table | Other |
|---|---|---|---|---|---|---|
| **sum / size / contains / max** | ✔ | ✔ | ✔ loop + recursive | ✔ recursive on an `Iterator` | ✔ dup-check with `HashSet` | divide-in-half sum |
| **reverse** | ✔ string | ✔ string | ✔ loop + recursive | | | explicit stack |
| **Fibonacci** | ✔ | ✔ naive | | | ✔ memo `HashMap` | |
| **binary search** | ✔ | ✔ | | | | |
| **tree traversals** | ✔ array form | ✔ | ✔ linked nodes | ✔ `InOrderIterator` | | stack (DFS) and queue (BFS) |
| **BST ops** | | ✔ | ✔ loop versions | | ✔ BST as a map | balanced build |
| **heap sift up/down** | ✔ | ✔ | isHeap on linked nodes | | | `PriorityQueue` |
| **expression tree** | | ✔ evaluate / build | | ✔ prefix/infix parser over an `Iterator` | | stack build + stack evaluate |
| **insertion sort** | ✔ shift, swap, binary | ✔ | ✔ loop + recursive | ✔ `ListIterator` | ✔ sort keys / entries / `LinkedHashMap` | |
| **selection sort** | ✔ min-front, max-back | ✔ (+ recursive min) | ✔ swap data, relink, recursive | ✔ `Iterator.remove()` | ✔ select from a `HashSet` | |
| **heap sort** | ✔ in place | ✔ recursive siftDown | ✔ via heap | ✔ any `Iterable` into a `PriorityQueue` | ✔ `HashMap` + heap top-k | |
| **merge sort / merge()** | ✔ bottom-up | ✔ top-down, new-array | ✔ recursive + iterative merge | ✔ merge two iterators | ✔ stable sort of entries | |
| **quick sort / partition()** | ✔ explicit stack | ✔ Lomuto, Hoare, median-of-3 | ✔ 3-way node partition | ✔ for-each 3-way | | quickselect |
| **hash map** | ✔ probing arrays | ✔ recursive chain search, recursive probe | ✔ chaining nodes, `LinkedList` buckets | ✔ iterators for both tables | | `TreeMap` comparison |
| **hash set** | ✔ probing set | | ✔ chaining set | | ✔ | dup / two-sum / set ops / cycle |

## A 3-day plan

- **Day 1:** chapters 1 and 2. Trace every example by hand **before** running the file. Do R1-R4 and T1-T6.
- **Day 2:** chapter 3. For each sort, be able to (a) write the array version from memory, (b) trace it on 6 numbers, (c) state best/avg/worst/stable. Do S1-S6.
- **Day 3:** chapter 4, then H1-H5. Then redo every practice question you got wrong.

## What exams usually ask

1. **Trace** an algorithm on small input (sort passes, heap array after adds, BST after removes, hash table after inserts).
2. **Write** a short recursive method (count, sum, contains, height, isBST) on a linked list or tree.
3. **Big-O**, best and worst case, and **why**.
4. **Compare** structures: when to use a hash table, a BST or a heap.
5. **Spot the bug**: missing base case, checking only children for isBST, removing from linear probing without a tombstone, `equals` without `hashCode`.
