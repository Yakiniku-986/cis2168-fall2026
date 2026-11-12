# 1. Recursion

Code for this chapter (every file has a `main` you can run):

| File | What's in it |
|---|---|
| [`RecursionBasics.java`](../src/tuj/cis2168/study/recursion/RecursionBasics.java) | factorial, power, Fibonacci (3 ways), strings, arrays, binary search, gcd. Each one recursive **and** iterative. |
| [`RecursionOnStructures.java`](../src/tuj/cis2168/study/recursion/RecursionOnStructures.java) | The **same** problems (size, sum, contains, max, print reversed, reverse list) on an **array**, a **linked list**, an **iterator**, and a **loop**. |
| [`Backtracking.java`](../src/tuj/cis2168/study/recursion/Backtracking.java) | Permutations, subsets, Towers of Hanoi, grid paths. |

Lecture 12 notes in the project files (`lecture12/`) go with this chapter.

---

## 1.1 The recipe

Every recursive method needs two things:

1. **Base case**: a small input you can answer directly, with no recursive call.
2. **Recursive case**: shrink the problem, call yourself on the smaller piece, and combine.

Each call must get **closer** to the base case. If it doesn't (like `factorial(-1)`), you get infinite recursion and a `StackOverflowError`.

The professor's picture is **peel off one layer**. What a "layer" is depends on the structure:

| Structure | Peel one layer | "Nothing left" (base case) |
|---|---|---|
| String | `s.substring(1)` | `s.isEmpty()` |
| Array | `i + 1` (pass an index) | `i == a.length` |
| Linked list | `node.next` | `node == null` |
| Iterator | `it.next()` | `!it.hasNext()` |
| Number | `n - 1` or `n / 10` | `n == 0` |
| Tree | `t.left`, `t.right` | `t == null` |
| Range (divide in half) | `lo..mid`, `mid+1..hi` | `lo > hi` |

That table is the whole "permutation" idea for recursion: **the algorithm doesn't change, only how you peel**. `RecursionOnStructures.java` prints this proof:

```
problem   | array | linked list | iterator | loop
----------+-------+-------------+----------+-----
size      |     6 |           6 |        6 |    6
sum       |   108 |         108 |      108 |  108
has 23?   |  true |        true |     true | true
max       |     - |          42 |       42 |    -
```

Here is `sum` four ways, side by side:

```java
// array: move an index
static int sumArray(int[] a, int i) {
  if (i == a.length) { return 0; }
  return a[i] + sumArray(a, i + 1);
}

// linked list: move a node
static int sumList(Node<Integer> node) {
  if (node == null) { return 0; }
  return node.data + sumList(node.next);
}

// iterator: consume one element
static int sumIter(Iterator<Integer> it) {
  if (!it.hasNext()) { return 0; }
  int first = it.next();
  return first + sumIter(it);
}

// loop: works on ANY Iterable (ArrayList, LinkedList, HashSet, ...)
static int sumLoop(Iterable<Integer> things) {
  int total = 0;
  for (int x : things) { total += x; }
  return total;
}
```

---

## 1.2 Unwinding, and the run-time stack

Each call gets an **activation frame** (arguments, locals, return address) pushed on the **run-time stack**. Frames pop off in reverse order when calls return. Work written **after** the recursive call happens on the way back up ("unwinding").

```
sum({2,10,8}, 0)
= 2 + sum(.., 1)
      = 10 + sum(.., 2)
             = 8 + sum(.., 3)
                   = 0            <- base case, now unwind
             = 8 + 0  = 8
      = 10 + 8 = 18
= 2 + 18 = 20
```

**Order matters.** Print before the call, you get forward order; print after, you get reverse:

```java
static void printBackward(String s) {
  if (s.isEmpty()) { return; }
  printBackward(s.substring(1));   // go all the way down first
  System.out.print(s.charAt(0));   // print on the way back up
}
// printBackward("cat") prints "tac"
```

The iterative version of "print a linked list backwards" needs an **explicit stack**, which is exactly what the run-time stack was doing for free (`printReverseListLoop` in `RecursionOnStructures.java`).

> **Rule:** anything recursive can be written with a loop, and vice versa. If the recursion makes **one** call at the very end (tail call), the loop is easy. If it does work **after** the call, or makes **two** calls (trees, merge sort), the loop needs an explicit stack.

---

## 1.3 Three shapes of recursion

**(a) Peel one off (linear).** `n * fact(n-1)`, `a[i] + sum(a, i+1)`, `1 + size(node.next)`. Depth `n`, time `O(n)`.

**(b) Divide in half.** Split the input into two halves, solve each, combine. Depth `log n`.
- One half only: binary search, `fastPower` → `O(log n)`.
- Both halves with `O(n)` combine: merge sort → `O(n log n)`.
- Both halves with `O(1)` combine: `sumHalves` → `O(n)`.

**(c) Branching (tree recursion).** Several calls per call. `fib(n-1) + fib(n-2)`, permutations, subsets, Hanoi → exponential.

---

## 1.4 Fibonacci three ways (the cost of naive recursion)

```java
// Naive: two calls per call -> O(2^n)
static long fib(int n) {
  if (n <= 1) { return n; }
  return fib(n - 1) + fib(n - 2);
}

// Memoized with a HASH MAP: each fib(k) computed once -> O(n)
static long fibMemo(int n, Map<Integer, Long> memo) {
  if (n <= 1) { return n; }
  if (memo.containsKey(n)) { return memo.get(n); }
  long answer = fibMemo(n - 1, memo) + fibMemo(n - 2, memo);
  memo.put(n, answer);
  return answer;
}

// Iterative: only keep the last two -> O(n) time, O(1) space
static long fibLoop(int n) {
  long prev = 0, curr = 1;
  if (n == 0) { return 0; }
  for (int i = 2; i <= n; i++) { long next = prev + curr; prev = curr; curr = next; }
  return curr;
}
```

Real output:

```
fib(25)     = 75025   (242785 calls!)
fibMemo(25) = 75025   (memo now holds 24 entries)
fibLoop(25) = 75025
```

Call tree for `fib(5)`. Notice `fib(3)` is computed twice and `fib(2)` three times:

```
                    fib(5)
                 /          \
            fib(4)          fib(3)
           /     \          /    \
       fib(3)   fib(2)   fib(2) fib(1)
       /   \     /  \     /  \
   fib(2) fib(1) f1  f0  f1  f0
    /  \
   f1  f0
```

This is **recursion + a hash table**: the memo map turns an exponential algorithm into a linear one.

---

## 1.5 Recursion on linked lists: reverse the list

A favourite exam question. Both versions are in `RecursionOnStructures.java`.

```java
// Recursive: reverse the rest, then hook me onto the end.
static <T> Node<T> reverseList(Node<T> head) {
  if (head == null || head.next == null) { return head; }
  Node<T> newHead = reverseList(head.next);
  head.next.next = head;   // the node after me now points back at me
  head.next = null;
  return newHead;
}

// Iterative: three pointers.
static <T> Node<T> reverseListLoop(Node<T> head) {
  Node<T> prev = null, curr = head;
  while (curr != null) {
    Node<T> next = curr.next;
    curr.next = prev;
    prev = curr;
    curr = next;
  }
  return prev;
}
```

Trace of the iterative version on `1 -> 2 -> 3`:

| step | prev | curr | list so far (reversed part) |
|---|---|---|---|
| start | null | 1 | |
| 1 | 1 | 2 | `1` |
| 2 | 2 | 3 | `2 -> 1` |
| 3 | 3 | null | `3 -> 2 -> 1` ← return prev |

---

## 1.6 Backtracking: "choose, explore, un-choose"

When you need **every** arrangement, you don't know how many nested loops to write, so recursion is the natural tool.

```java
static void permutations(String soFar, String remaining, List<String> out) {
  if (remaining.isEmpty()) { out.add(soFar); return; }
  for (int i = 0; i < remaining.length(); i++) {
    String rest = remaining.substring(0, i) + remaining.substring(i + 1);
    permutations(soFar + remaining.charAt(i), rest, out);
  }
}
// permutations("", "abc") -> [abc, acb, bac, bca, cab, cba]   (3! = 6)
```

The in-place array version swaps, recurses, then **swaps back** (the "un-choose" step). Subsets: each item is either **in** or **out**, giving `2^n` results. Hanoi: move `n-1` out of the way, move the big disk, move `n-1` back, giving `2^n - 1` moves.

```
hanoi(3):
  move disk 1 A -> C
  move disk 2 A -> B
  move disk 1 C -> B
  move disk 3 A -> C
  move disk 1 B -> A
  move disk 2 B -> C
  move disk 1 A -> C
  total moves = 7 (2^3 - 1)
```

---

## 1.7 Recursion cheat sheet

| Method | Recurrence | Time | Stack depth |
|---|---|---|---|
| factorial, sum, size, contains | T(n) = T(n-1) + O(1) | O(n) | O(n) |
| binary search, fastPower | T(n) = T(n/2) + O(1) | O(log n) | O(log n) |
| sumHalves, tree size | T(n) = 2T(n/2) + O(1) | O(n) | O(log n) (balanced) |
| merge sort | T(n) = 2T(n/2) + O(n) | O(n log n) | O(log n) |
| naive fib | T(n) = T(n-1) + T(n-2) | O(2^n) | O(n) |
| permutations | n · T(n-1) | O(n · n!) | O(n) |
| subsets | 2T(n-1) | O(2^n) | O(n) |
| Hanoi | 2T(n-1) + 1 | O(2^n) | O(n) |

## 1.8 Common mistakes

- Forgetting the base case, or a base case the input can skip past (`factorial(-1)`, `n == 1` when `n` can be 0).
- Checking `str.isEmpty()` before `str == null` (NullPointerException).
- Not **returning** the recursive call's result: `contains(node.next, x);` instead of `return contains(node.next, x);`.
- For trees: searching only one side in a plain binary tree (only a **BST** lets you pick a side).
- Calling the expensive recursion twice instead of saving it: `fastPower` must store `half` and reuse it, otherwise it's back to `O(n)`.
