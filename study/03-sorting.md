# 3. Sorting

| File | Versions inside |
|---|---|
| [`InsertionSorts.java`](../src/tuj/cis2168/study/sorting/InsertionSorts.java) | array shift, array swap, **recursive**, binary insertion, **linked list** (loop + recursive), **Iterator/ListIterator**, **HashMap** keys / entries / LinkedHashMap |
| [`SelectionSorts.java`](../src/tuj/cis2168/study/sorting/SelectionSorts.java) | array, **recursive** (with recursive indexOfMin), max-to-back, **linked list** (swap data / relink nodes / recursive), **Iterator.remove()** on any Collection, **HashSet** |
| [`HeapSorts.java`](../src/tuj/cis2168/study/sorting/HeapSorts.java) | in-place array (iterative + **recursive** siftDown), **PriorityQueue** on any Iterable, **linked list**, **HashMap + heap** top-k |
| [`MergeSorts.java`](../src/tuj/cis2168/study/sorting/MergeSorts.java) | top-down **recursive** with `merge()`, return-new-array, bottom-up **iterative**, **linked list** (recursive + iterative merge), **Iterator** merge, **HashMap** entries with a Comparator (stability) |
| [`QuickSorts.java`](../src/tuj/cis2168/study/sorting/QuickSorts.java) | Lomuto `partition()`, Hoare `partition()`, **iterative** with a stack, median-of-three, **linked list** 3-way, **List + iterators**, quickselect |
| [`SortComparison.java`](../src/tuj/cis2168/study/sorting/SortComparison.java) | counts comparisons on random / sorted / reversed input |

---

## 3.0 The big table (memorize this)

| Sort | Best | Average | Worst | Extra space | Stable? | In place? |
|---|---|---|---|---|---|---|
| Insertion | **O(n)** (sorted) | O(n²) | O(n²) (reversed) | O(1) | **yes** | yes |
| Selection | O(n²) | O(n²) | O(n²) | O(1) | no | yes |
| Heap | O(n log n) | O(n log n) | O(n log n) | O(1) | no | yes |
| Merge | O(n log n) | O(n log n) | O(n log n) | **O(n)** | **yes** | no (arrays) |
| Quick | O(n log n) | O(n log n) | **O(n²)** (bad pivots) | O(log n) stack | no | yes |

**Stable** = equal keys keep their original relative order. Matters when you sort by one field after another (see 3.4).

Measured on 1000 items by `SortComparison.java` (number of comparisons):

```
comparisons for n = 1000   (n^2/2 = 500000,  n log2 n = 9965)

sort                     random     sorted   reversed
insertion                260632        999     499500   <- best case n, worst n^2/2
selection                499500     499500     499500   <- always n^2/2
heap                      16882      17583      15965   <- always ~n log n
merge                      8703       5044       4932   <- always ~n log n
quick (last pivot)        10577     499500     499500   <- sorted input = worst case!
```

---

## 3.1 Insertion sort

**Idea:** like sorting a hand of cards. The left part is sorted. Take the next card and slide it left until it fits.

```java
for (int i = 1; i < a.length; i++) {
  int key = a[i];
  int j = i - 1;
  while (j >= 0 && a[j] > key) {   // strict > keeps it stable
    a[j + 1] = a[j];               // shift right
    j--;
  }
  a[j + 1] = key;
}
```

Trace on `[7, 3, 5, 1, 9, 2]` (`|` = end of the sorted part):

```
  i=1 insert 3: [3 7 | 5 1 9 2]
  i=2 insert 5: [3 5 7 | 1 9 2]
  i=3 insert 1: [1 3 5 7 | 9 2]
  i=4 insert 9: [1 3 5 7 9 | 2]     9 compares once with 7 and stays (why sorted input is O(n))
  i=5 insert 2: [1 2 3 5 7 9 |]
```

### The permutations

| Version | Key idea |
|---|---|
| **Recursive** | To sort `a[0..n-1]`: sort `a[0..n-2]`, then insert `a[n-1]` into it. Base case `n <= 1`. The inner "slide left" can be recursive too (`insertLast`). |
| **Binary insertion** | Binary-search the spot: O(n log n) comparisons, but still O(n²) shifts. |
| **Linked list** | Take nodes off the input one at a time and insert each into a growing sorted list. No shifting needed, just relinking. Still O(n²) because finding the spot is a walk. Recursive version: sort the rest, then insert the head into it. |
| **Iterator / ListIterator** | Read input with any `Iterator`; walk the output `LinkedList` with a `ListIterator`, step back with `previous()`, and insert with `it.add(x)` (O(1) at the cursor). Works on any `Iterable` input, even a `HashSet`. |
| **Hash table** | A `HashMap`/`HashSet` has **no order**, so you can't sort it in place. Pull the keys out with an iterator into a list and sort that; or sort the `entrySet()` by value; or re-insert in sorted order into a `LinkedHashMap` (which remembers insertion order). |

```java
// Linked list: relink nodes into a sorted list
static Node insertionSortList(Node head) {
  Node sorted = null;
  while (head != null) {
    Node curr = head;
    head = head.next;                               // detach from input
    if (sorted == null || curr.data < sorted.data) {
      curr.next = sorted;                           // new front
      sorted = curr;
    } else {
      Node p = sorted;
      while (p.next != null && p.next.data <= curr.data) { p = p.next; }
      curr.next = p.next;                           // splice in after p
      p.next = curr;
    }
  }
  return sorted;
}
```

```
7. HashMap as stored     : {the=3, bat=1, and=2, cat=1, hat=1}   <- no useful order
   sortedKeys            : [and, bat, cat, hat, the]
   sortByValue           : [the=3, and=2, bat=1, cat=1, hat=1]
   sorted LinkedHashMap  : {and=2, bat=1, cat=1, hat=1, the=3}
```

---

## 3.2 Selection sort

**Idea:** find the smallest remaining item, swap it into the next slot. Repeat.

```java
for (int i = 0; i < a.length - 1; i++) {
  int minIdx = i;
  for (int j = i + 1; j < a.length; j++) {
    if (a[j] < a[minIdx]) { minIdx = j; }
  }
  swap(a, i, minIdx);
}
```

Trace on `[29, 10, 14, 37, 13, 5]`:

```
  pass 0: min 5 -> slot 0  [5 | 10 14 37 13 29]     (29 and 5 swapped)
  pass 1: min 10 -> slot 1  [5 10 | 14 37 13 29]
  pass 2: min 13 -> slot 2  [5 10 13 | 37 14 29]
  pass 3: min 14 -> slot 3  [5 10 13 14 | 37 29]
  pass 4: min 29 -> slot 4  [5 10 13 14 29 | 37]
```

Always `n(n-1)/2` comparisons, but at most `n - 1` swaps (fewest writes of any sort here). **Not stable**: the long swap can jump an item over an equal one (`[5a, 5b, 3] -> [3, 5b, 5a]`).

### The permutations

| Version | Key idea |
|---|---|
| **Recursive** | Swap the min into `a[start]`, then `selectionSortRec(a, start + 1)`. The min can be found recursively too (`indexOfMin`, the lab05 `listArgMax` pattern). |
| **Max to the back** | Same thing mirrored: find the max, swap it to the end, shrink. |
| **Linked list (swap data)** | `i` and `j` become node pointers; swap the `data` fields. Same code shape as the array. |
| **Linked list (relink nodes)** | Repeatedly unlink the min node and append it to an output list. You need the node **before** the min to unlink it, so a dummy head helps. |
| **Iterator on any Collection** | Loop: find the min with for-each, then remove it with an explicit `Iterator` and `it.remove()`. Works on `ArrayList`, `LinkedList`, **`HashSet`**. |
| **Hash set** | Same as above. Removing from a `HashSet` is O(1), but **finding** the min is still an O(n) scan (hash tables don't know about order), so the whole thing is O(n²). Replace the set with a **heap** and finding the min becomes O(log n): that's heap sort. |

---

## 3.3 Heap sort

**Idea:** selection sort, but use a **max-heap** to find the max in O(log n).

1. **Heapify** the array into a max-heap, bottom-up: for `i = n/2 - 1` down to `0`, siftDown(i). O(n).
2. Repeat: swap the root (the max) with the last unsorted slot; shrink the heap by one; siftDown the new root. O(n log n).

```java
for (int i = n / 2 - 1; i >= 0; i--) { siftDown(a, i, n); }
for (int end = n - 1; end > 0; end--) {
  swap(a, 0, end);
  siftDown(a, 0, end);       // heap is now a[0 .. end-1]
}
```

Trace on `[4, 10, 3, 5, 1, 8]` (`|` = start of the sorted part):

```
  after heapify (max-heap): [10, 5, 8, 4, 1, 3]
  moved 10 to slot 5: [8 5 3 4 1 | 10]
  moved 8 to slot 4: [5 4 3 1 | 8 10]
  moved 5 to slot 3: [4 1 3 | 5 8 10]
  moved 4 to slot 2: [3 1 | 4 5 8 10]
  moved 3 to slot 1: [1 | 3 4 5 8 10]
```

Heapify step by step: last parent is index `6/2 - 1 = 2` (value 3, child 8) → swap → `[4,10,8,5,1,3]`. Index 1 (10, children 5 and 1) is fine. Index 0 (4, children 10 and 8) → swap with 10 → `[10,4,8,5,1,3]`; 4 now at index 1 has children 5 and 1 → swap with 5 → `[10,5,8,4,1,3]`.

### The permutations

| Version | Key idea |
|---|---|
| **Recursive siftDown** | Find the larger child; if it beats you, swap and recurse on that child. Base case: you're bigger than both children. |
| **Separate heap (PriorityQueue)** | `add` everything, then `poll` everything. O(n log n) time, O(n) extra space. Works on **any Iterable** (`List`, `Set`, `map.keySet()`). |
| **Linked list** | Heaps need random access (`2i+1`), which linked lists don't have. Move the values into a heap, then write them back into the nodes in order. |
| **HashMap + heap (top-k)** | Count with a `HashMap`, then keep a **min-heap of size k**: add each entry, and if the heap grows past k, poll the smallest. What's left is the top k. O(n + m log k). |

```
5. HashMap + heap, top 3 : [a=5, is=3, rose=3]
```

---

## 3.4 Merge sort and `merge()`

**Idea (divide and conquer):** split in half, sort each half recursively, **merge** the two sorted halves. The work happens **after** the recursive calls (in `merge`).

```java
static void mergeSort(int[] a, int lo, int hi, int[] temp) {
  if (lo >= hi) { return; }               // 0 or 1 items
  int mid = (lo + hi) / 2;
  mergeSort(a, lo, mid, temp);
  mergeSort(a, mid + 1, hi, temp);
  merge(a, lo, mid, hi, temp);
}

static void merge(int[] a, int lo, int mid, int hi, int[] temp) {
  int i = lo, j = mid + 1, k = lo;
  while (i <= mid && j <= hi) {
    if (a[i] <= a[j]) { temp[k++] = a[i++]; }   // <= takes LEFT on ties: stable
    else              { temp[k++] = a[j++]; }
  }
  while (i <= mid) { temp[k++] = a[i++]; }      // copy leftovers
  while (j <= hi)  { temp[k++] = a[j++]; }
  for (k = lo; k <= hi; k++) { a[k] = temp[k]; }
}
```

Trace on `[38, 27, 43, 3, 9, 82, 10]`:

```
split [38, 27, 43, 3] [9, 82, 10]
  split [38, 27] [43, 3]
    split [38] [27]
    merge -> [27, 38]
    split [43] [3]
    merge -> [3, 43]
  merge -> [3, 27, 38, 43]
  split [9, 82] [10]
    split [9] [82]
    merge -> [9, 82]
  merge -> [9, 10, 82]
merge -> [3, 9, 10, 27, 38, 43, 82]
```

Why O(n log n): there are `log n` levels of splitting, and the merges on each level touch all `n` items once.

Hand-merging `[3, 27, 38, 43]` and `[9, 10, 82]`: compare fronts 3 vs 9 → 3; 27 vs 9 → 9; 27 vs 10 → 10; 27 vs 82 → 27; 38 → 38; 43 → 43; left side empty → copy 82.

### The permutations

| Version | Key idea |
|---|---|
| **Return a new array** | `merge(int[] x, int[] y)` returns a new array; `mergeSort` copies halves with `Arrays.copyOfRange`. Easiest to write on paper. |
| **Bottom-up iterative** | No recursion. Merge runs of width 1, then 2, then 4... `width 1 -> [27,38, 3,43, 9,82, 10]`, `width 2 -> [3,27,38,43, 9,10,82]`, `width 4 -> sorted`. |
| **Linked list** | The best sort for linked lists. Find the middle with **slow/fast pointers**, cut, sort both halves, merge by **relinking** (no temp array). Merge can be recursive (smaller head first, then `head.next = merge(rest)`) or iterative with a **dummy head** node. |
| **Iterators** | Merge any two sorted `Iterable`s. An `Iterator` can't peek, so hold each side's current front item in a variable. |
| **HashMap entries + Comparator** | Sort `entrySet()` by key, then **stably** by count: equal counts stay alphabetical. That only works because merge sort is stable. |

```java
// Linked list merge, recursive
static Node mergeLists(Node x, Node y) {
  if (x == null) { return y; }
  if (y == null) { return x; }
  if (x.data <= y.data) { x.next = mergeLists(x.next, y); return x; }
  else                  { y.next = mergeLists(x, y.next); return y; }
}

// Linked list merge, iterative with a dummy head
Node dummy = new Node(0, null), tail = dummy;
while (x != null && y != null) {
  if (x.data <= y.data) { tail.next = x; x = x.next; } else { tail.next = y; y = y.next; }
  tail = tail.next;
}
tail.next = (x != null) ? x : y;
return dummy.next;
```

```
6. HashMap {a=2, b=3, c=1, d=1, e=1} sorted by count, ties alphabetical: [b=3, a=2, c=1, d=1, e=1]
```

---

## 3.5 Quick sort and `partition()`

**Idea:** pick a **pivot**; **partition** so smaller items are left of it and larger items are right of it. The pivot is now in its **final** position. Recursively sort both sides. The work happens **before** the recursive calls (in `partition`), the opposite of merge sort.

### Lomuto partition (pivot = last element)

```java
static int partition(int[] a, int lo, int hi) {
  int pivot = a[hi];
  int i = lo - 1;                      // a[lo..i] holds items <= pivot
  for (int j = lo; j < hi; j++) {
    if (a[j] <= pivot) { i++; swap(a, i, j); }
  }
  swap(a, i + 1, hi);                  // pivot goes right after that zone
  return i + 1;
}

static void quickSort(int[] a, int lo, int hi) {
  if (lo >= hi) { return; }
  int p = partition(a, lo, hi);
  quickSort(a, lo, p - 1);             // p itself is done
  quickSort(a, p + 1, hi);
}
```

Trace on `[7, 2, 1, 6, 8, 5, 3, 4]` (`{ }` = range being partitioned, `[x]` = pivot's final spot):

```
  partition [0..7] pivot 4 -> { 2 1 3 [4] 8 5 7 6 }
  partition [0..2] pivot 3 -> { 2 1 [3] } 4 8 5 7 6
  partition [0..1] pivot 1 -> { [1] 2 } 3 4 8 5 7 6
  partition [4..7] pivot 6 -> 1 2 3 4 { 5 [6] 7 8 }
  partition [6..7] pivot 8 -> 1 2 3 4 5 6 { 7 [8] }
```

First partition by hand (pivot 4, `i` starts at -1):

| j | a[j] | ≤ 4? | action | array |
|---|---|---|---|---|
| 0 | 7 | no | | 7 2 1 6 8 5 3 4 |
| 1 | 2 | yes | i=0, swap a[0],a[1] | 2 7 1 6 8 5 3 4 |
| 2 | 1 | yes | i=1, swap a[1],a[2] | 2 1 7 6 8 5 3 4 |
| 3 | 6 | no | | |
| 4 | 8 | no | | |
| 5 | 5 | no | | |
| 6 | 3 | yes | i=2, swap a[2],a[6] | 2 1 3 6 8 5 7 4 |
| end | | | swap a[3],a[7] (pivot) | 2 1 3 **4** 8 5 7 6 |

### Worst case

If the pivot is always the smallest or largest item, one side is empty and the other has `n - 1` items: O(n²). With "last element" as pivot, **already-sorted input** does exactly this. Fixes: **median-of-three** (median of first, middle, last) or a random pivot.

```
4. already-sorted n=2000: last-pivot comparisons = 1999000 (~n^2/2),  median-of-3 = 17964 (~n log n)
```

### The permutations

| Version | Key idea |
|---|---|
| **Hoare partition** | Two indices walk inward from both ends and swap pairs that are on the wrong side. Fewer swaps. Returns a split point `j`; recurse on `lo..j` and `j+1..hi` (note: **not** `j-1`). |
| **Iterative** | Push `(lo, hi)` ranges onto an explicit stack instead of recursing. |
| **Linked list** | Pivot = head. Walk the list and move each node onto a `less`, `equal` or `greater` list. Sort `less` and `greater` recursively, concatenate. No swapping, no indices. |
| **List + iterators** | Same three-way split using for-each into three `ArrayList`s. Short to write, uses extra space. |
| **Quickselect** | Find the k-th smallest: partition, then recurse into **only** the side containing index k. O(n) average. |

---

## 3.6 Merge sort vs quick sort, side by side

| | Merge sort | Quick sort |
|---|---|---|
| Where the work is | **after** the recursive calls (merge) | **before** the recursive calls (partition) |
| Split | always exactly in half | depends on the pivot |
| Worst case | O(n log n) | O(n²) |
| Extra space | O(n) temp array | O(log n) stack |
| Stable | yes | no |
| Best for | linked lists, stability, guaranteed time | arrays in memory (fast in practice) |

---

## 3.7 Which structure for which sort?

| | Array | Linked list | Iterator only | Hash table |
|---|---|---|---|---|
| Insertion | ✔ natural | ✔ relink into sorted list | ✔ ListIterator insert | sort the keys/entries you pull out |
| Selection | ✔ natural | ✔ swap data or relink | ✔ find min + `it.remove()` | ✔ repeatedly select + remove min from a set (O(n²)) |
| Heap | ✔ in place | copy into heap and back | ✔ feed a PriorityQueue | ✔ HashMap counts + heap for top-k |
| Merge | ✔ needs temp array | ✔✔ **best fit** (no temp) | ✔ merge two iterators | ✔ stable sort of entries |
| Quick | ✔✔ **best fit** | ✔ 3-way list partition | ✔ 3 lists via for-each | sort keys you pull out |

**Key exam point:** hash tables scatter keys by `hashCode`, so they have **no order** to sort. You always sort a **copy** of the keys or entries, or use a `TreeMap` (BST) if you need sorted order all the time, or a `LinkedHashMap` to keep the order you inserted.
