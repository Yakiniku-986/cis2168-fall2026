# 4. Hash Tables (Sets and Maps)

| File | What's in it |
|---|---|
| [`ChainingHashMap.java`](../src/tuj/cis2168/study/hashing/ChainingHashMap.java) | separate chaining with our own nodes: put/get/remove/resize, an Iterator, recursive and iterative chain search, a **Set** built on it, and a version with `java.util.LinkedList` buckets |
| [`LinearProbingHashMap.java`](../src/tuj/cis2168/study/hashing/LinearProbingHashMap.java) | open addressing with linear probing: tombstones, the **broken** remove, recursive get, resize, Iterator, a **Set** version |
| [`HashSetsAndMaps.java`](../src/tuj/cis2168/study/hashing/HashSetsAndMaps.java) | the same problems solved with arrays, sorting, a BST (`TreeSet`/`TreeMap`) and a hash table; `hashCode`/`equals`; `String.hashCode` by hand |

---

## 4.1 Sets vs maps

| | Set | Map |
|---|---|---|
| Stores | keys only | key → value pairs |
| Duplicates | no (`add` returns `false`) | keys unique; `put` on an existing key **replaces** the value |
| Main ops | `add`, `contains`, `remove` | `put`, `get`, `containsKey`, `remove` |
| Hash version | `HashSet` | `HashMap` |
| BST version | `TreeSet` (sorted) | `TreeMap` (sorted keys) |

A set is just a map where you ignore the values. `java.util.HashSet` is literally a `HashMap` inside, and so are our `ChainingHashSet` and `LinearProbingHashSet`.

---

## 4.2 How hashing works

1. Turn the key into an int: `key.hashCode()`.
2. Turn that into an array index: `Math.floorMod(hashCode, table.length)`.
   - Use `floorMod`, not `%`: `hashCode()` can be **negative**, and `-7 % 10` is `-7` in Java.
3. Go to that slot. If two keys land in the same slot, that's a **collision**. Collisions are normal and must be handled.

**`String.hashCode()`** is `s[0]·31^(n-1) + s[1]·31^(n-2) + ... + s[n-1]`, computed with Horner's rule:

```java
int h = 0;
for (int i = 0; i < s.length(); i++) { h = 31 * h + s.charAt(i); }
// "cat" = 99*961 + 97*31 + 116 = 98262
```

**Load factor** `λ = size / table.length`. Keep it low by **resizing**: allocate a bigger array (usually double) and **rehash** every entry. You can't copy entries to the same index, because the index depends on `table.length`.

### The `hashCode` / `equals` contract
- If `a.equals(b)`, then `a.hashCode() == b.hashCode()`. **Must** hold.
- Equal hash codes do **not** mean equal objects (that's just a collision).
- Override `equals` without `hashCode` and two "equal" objects go to different buckets, so `HashSet.contains` returns `false`:

```
equals without hashCode: contains(new BadPoint(1,2))  = false   <- broken
equals AND hashCode   : contains(new GoodPoint(1,2)) = true
```

Always compare keys with `.equals()`, never `==` (except for identity, like the linked-list cycle check where you **want** "same node object").

---

## 4.3 Collision strategy 1: separate chaining

Each array slot holds a **linked list** ("chain") of every entry that hashes there.

```java
public V put(K key, V value) {
  int i = Math.floorMod(key.hashCode(), table.length);
  for (Node<K, V> n = table[i]; n != null; n = n.next) {
    if (n.key.equals(key)) { V old = n.value; n.value = value; return old; }  // update
  }
  table[i] = new Node<>(key, value, table[i]);   // new: add at FRONT of chain, O(1)
  size++;
  if ((double) size / table.length > 0.75) { resize(); }
  return null;
}
```

- **get**: hash, then walk that one chain (a linked-list `contains`, which can be written recursively or with a loop; both are in the file).
- **remove**: hash, then unlink from a singly linked list (track `prev`, or special-case the head).
- **λ can exceed 1** (chains just get longer). Average cost O(1 + λ). Resize around 0.75.
- Worst case: all keys in one chain → O(n).

Trace from `ChainingHashMap.java` (capacity 5):

```
  put cat    hash=98262       index=2
  put dog    hash=99644       index=4
  put emu    hash=100557      index=2  <- COLLISION, chained
  put fox    hash=101583      index=3
  ** load > 0.75: resized 5 -> 10 and rehashed
  put gnu    hash=102510      index=0
  put hen    hash=103185      index=5
```

Before the resize, bucket 2 was `emu -> cat`. After resizing to 10, `cat` stays at 98262 % 10 = 2 but `emu` moves to 100557 % 10 = 7. That's why you must rehash.

**Iterator:** walk bucket 0's chain, then bucket 1's chain, and so on, skipping empty buckets. The order looks random because it follows hash codes, not insertion.

**Permutations of the chain:** our own `Node` class, or `java.util.LinkedList<Entry>` per bucket (`LibraryChainingMap` in the file). Java 8+ `HashMap` even turns long chains into small balanced BSTs.

---

## 4.4 Collision strategy 2: linear probing (open addressing)

Everything lives **in the array itself**. On a collision, try the next slot, then the next, wrapping around: `h, h+1, h+2, ...` mod length.

```java
public V get(K key) {
  int i = hash(key);
  while (keys[i] != null) {                     // stop only at a truly EMPTY slot
    if (keys[i] != TOMBSTONE && keys[i].equals(key)) { return values[i]; }
    i = (i + 1) % keys.length;
  }
  return null;
}
```

Trace from `LinearProbingHashMap.java`, capacity 23, `hash(k) = k % 23`:

```
  put 5    hash= 5  -> slot 5
  put 28   hash= 5  occupied: 5 -> slot 6
  put 51   hash= 5  occupied: 5 6 -> slot 7
  put 7    hash= 7  occupied: 7 -> slot 8
  put 6    hash= 6  occupied: 6 7 8 -> slot 9
  put 74   hash= 5  occupied: 5 6 7 8 9 -> slot 10
  0:_  1:_  2:_  3:_  4:_  5:5  6:28  7:51  8:7  9:6  10:74  11:_  ...
```

Notice **primary clustering**: 7 and 6 didn't even collide with each other's keys, but they got pushed along because the run starting at slot 5 grew. Long runs make every operation slower, so keep **λ < 0.5**-ish (λ can never exceed 1: the array fills up).

### Removing: why you need tombstones

If you just set the slot to `null`, you punch a **hole** in the run, and searches stop at holes:

```
removeBroken: the bug
  0:_  1:_  2:_  3:_  4:_  5:5  6:_  7:51  8:_  ...
  get(51) = null   <- WRONG! 51 is still in slot 7, but the search stopped at the hole in slot 6
```

Fix: mark the slot with a **tombstone** (`X`). Searches **skip over** tombstones; inserts may **reuse** them.

```
remove(28) with a TOMBSTONE:
  0:_  1:_  2:_  3:_  4:_  5:5  6:X  7:51  8:7  9:6  10:74  11:_  ...
  get(51) = v51  (search walks over X and keeps going)
  put 97 (hash 5) walks the whole cluster to be sure 97 isn't already there,
  then goes back and reuses the tombstone at slot 6
```

Tombstones still take space and slow searches, so resize counts them, and a resize drops them.

### Other probe sequences

| Probe | Sequence | Notes |
|---|---|---|
| Linear | h, h+1, h+2, ... | simple, cache-friendly, primary clustering |
| Quadratic | h, h+1, h+4, h+9, ... | less clustering; use a prime table size and λ < 0.5 to always find a free slot |
| Double hashing | h, h+d, h+2d, ... with d = h2(key) | step depends on the key; d must never be 0 |

---

## 4.5 Chaining vs linear probing

| | Separate chaining | Linear probing |
|---|---|---|
| Storage | array of linked lists | one flat array (or parallel key/value arrays) |
| Load factor | can go above 1; resize ~0.75 | must stay below 1; resize ~0.5 |
| Collision | add to the chain | step to the next free slot |
| Delete | unlink the node, easy | needs **tombstones** |
| Problem | long chains | clustering |
| Average get/put/remove | O(1) | O(1) |
| Worst case | O(n) | O(n) |

---

## 4.6 The same problem, many structures

This is the hash table version of the "permutations" idea. All of these are in `HashSetsAndMaps.java`.

### Does the array contain a duplicate?

| Approach | Time | How |
|---|---|---|
| Nested loops | O(n²) | compare every pair |
| Sort, then scan | O(n log n) | duplicates end up next to each other |
| `TreeSet` (BST) | O(n log n) | `if (!seen.add(x)) return true;` |
| **`HashSet`** | **O(n)** avg | same code, just `new HashSet<>()` |
| Recursive + `HashSet` | O(n) | index helper, base case `i == a.length` |

### Word frequency

| Approach | Time | Order of output |
|---|---|---|
| Two parallel `ArrayList`s + `indexOf` | O(n · distinct) | first seen |
| Sort, then count runs | O(n log n) | sorted |
| `TreeMap` | O(n log n) | sorted |
| **`HashMap`** | **O(n)** avg | no order |
| `LinkedHashMap` | O(n) avg | first seen |

```
   parallel lists : the=3 cat=1 and=2 hat=1 bat=1
   sort + runs    : and=2 bat=1 cat=1 hat=1 the=3
   TreeMap        : {and=2, bat=1, cat=1, hat=1, the=3}   <- sorted keys
   HashMap        : {the=3, bat=1, and=2, cat=1, hat=1}   <- any order
   LinkedHashMap  : {the=3, cat=1, and=2, hat=1, bat=1}   <- first-seen order
```

The `HashMap` counting idiom, three ways:

```java
if (m.containsKey(w)) { m.put(w, m.get(w) + 1); } else { m.put(w, 1); }
m.put(w, m.getOrDefault(w, 0) + 1);
m.merge(w, 1, Integer::sum);
```

### Two-sum (is there a pair adding to target?)

| Approach | Time |
|---|---|
| Nested loops | O(n²) |
| Sort + two pointers from both ends | O(n log n) |
| **HashSet**: for each `x`, "have I already seen `target - x`?" | **O(n)** |

### Set operations
`HashSet`: `addAll` (union), `retainAll` (intersection), `removeAll` (difference). Without hashing, two **sorted** arrays can be intersected with a merge-style walk in O(n + m), the same loop shape as `merge()` in merge sort.

### Linked list cycle
`HashSet<Node>` of visited nodes (O(n) space), or Floyd's slow/fast pointers (O(1) space). The slow/fast trick is the same one merge sort uses to find the middle of a linked list.

### Recursion + hash table
Memoized Fibonacci (chapter 1) stores answers in a `HashMap` so each sub-problem is solved once: O(2ⁿ) → O(n).

---

## 4.7 Hash table cheat sheet

| Operation | HashSet/HashMap avg | worst | TreeSet/TreeMap | sorted array | unsorted array / linked list |
|---|---|---|---|---|---|
| contains / get | **O(1)** | O(n) | O(log n) | O(log n) | O(n) |
| add / put | **O(1)** | O(n) | O(log n) | O(n) | O(1) at end |
| remove | **O(1)** | O(n) | O(log n) | O(n) | O(n) |
| min / max | O(n) | O(n) | O(log n) | O(1) | O(n) |
| iterate in sorted order | O(n log n) (sort a copy) | | **O(n)** | O(n) | O(n log n) |

Pick a **hash table** when you only need lookup by key. Pick a **BST** (`TreeMap`) when you also need order (min, max, sorted iteration, ranges). Pick a **heap** when you only need the min (or max) over and over.
