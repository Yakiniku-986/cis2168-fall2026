package tuj.cis2168.study.recursion;

import java.util.HashMap;
import java.util.Map;

/// Study guide: recursion basics (see study/01-recursion.md).
///
/// Every problem here is written at least two ways:
///   * RECURSIVE  (base case + recursive case, "peel off one layer")
///   * ITERATIVE  (a loop that does the same work)
/// and some get a third version (memoized with a HashMap, or "divide in half").
///
/// Run:  java src/tuj/cis2168/study/recursion/RecursionBasics.java
public class RecursionBasics {

  // =========================================================================
  // 1. Factorial   n! = 1 if n == 0,   n * (n-1)! otherwise
  // =========================================================================

  /// Recursive. O(n) time, O(n) stack frames.
  public static long factorial(int n) {
    if (n < 0) { throw new IllegalArgumentException("n < 0 never reaches the base case"); }
    if (n == 0) { return 1; }               // base case
    return n * factorial(n - 1);            // recursive case: n * (smaller problem)
  }

  /// Iterative. O(n) time, O(1) extra space.
  public static long factorialLoop(int n) {
    long result = 1;
    for (int k = 2; k <= n; k++) { result *= k; }
    return result;
  }

  /// Tail-recursive version: the recursive call is the LAST thing done, and
  /// the answer is carried in an "accumulator". This is exactly what a loop
  /// does: `acc` is the loop variable `result`.
  public static long factorialTail(int n, long acc) {
    if (n <= 1) { return acc; }
    return factorialTail(n - 1, acc * n);
  }

  // =========================================================================
  // 2. Power  x^n
  // =========================================================================

  /// Recursive, peel one layer: x^n = x * x^(n-1).  O(n) calls.
  public static double power(double x, int n) {
    if (n == 0) { return 1; }
    return x * power(x, n - 1);
  }

  /// Recursive, DIVIDE IN HALF: x^n = (x^(n/2))^2  (times x if n is odd).
  /// O(log n) calls. Same idea as binary search / merge sort: halve the problem.
  public static double fastPower(double x, int n) {
    if (n == 0) { return 1; }
    double half = fastPower(x, n / 2);       // ONE recursive call, reused twice
    return (n % 2 == 0) ? half * half : half * half * x;
  }

  /// Iterative.
  public static double powerLoop(double x, int n) {
    double result = 1;
    for (int i = 0; i < n; i++) { result *= x; }
    return result;
  }

  // =========================================================================
  // 3. Fibonacci: three versions with VERY different running times
  // =========================================================================

  public static int naiveCalls = 0;

  /// Naive recursion: TWO recursive calls per call -> O(2^n) time.
  /// fib(5) calls fib(3) twice, fib(2) three times, ... (see the call tree
  /// in 01-recursion.md).
  public static long fib(int n) {
    naiveCalls++;
    if (n <= 1) { return n; }                // two base cases: fib(0)=0, fib(1)=1
    return fib(n - 1) + fib(n - 2);
  }

  /// Memoized recursion: remember answers in a HASH MAP so each fib(k) is
  /// computed once.  O(n) time, O(n) space.  (Recursion + hash table!)
  public static long fibMemo(int n, Map<Integer, Long> memo) {
    if (n <= 1) { return n; }
    if (memo.containsKey(n)) { return memo.get(n); }   // already solved
    long answer = fibMemo(n - 1, memo) + fibMemo(n - 2, memo);
    memo.put(n, answer);
    return answer;
  }

  /// Iterative: keep only the last two values. O(n) time, O(1) space.
  public static long fibLoop(int n) {
    long prev = 0, curr = 1;
    if (n == 0) { return 0; }
    for (int i = 2; i <= n; i++) {
      long next = prev + curr;
      prev = curr;
      curr = next;
    }
    return curr;
  }

  // =========================================================================
  // 4. Strings: length, reverse, palindrome, print order
  // =========================================================================

  /// From the slides: length = 1 + length(rest).  (null check FIRST.)
  public static int length(String str) {
    if (str == null || str.isEmpty()) { return 0; }
    return 1 + length(str.substring(1));
  }

  /// Reverse: reverse(rest) + first char.  "abc" -> reverse("bc") + "a"
  public static String reverse(String s) {
    if (s.length() <= 1) { return s; }
    return reverse(s.substring(1)) + s.charAt(0);
  }

  /// Reverse, iterative.
  public static String reverseLoop(String s) {
    StringBuilder sb = new StringBuilder();
    for (int i = s.length() - 1; i >= 0; i--) { sb.append(s.charAt(i)); }
    return sb.toString();
  }

  /// Palindrome, recursive with two indices (no substring copies).
  /// Peels off BOTH ends at once.
  public static boolean isPalindrome(String s, int lo, int hi) {
    if (lo >= hi) { return true; }                     // 0 or 1 chars left
    if (s.charAt(lo) != s.charAt(hi)) { return false; } // early exit
    return isPalindrome(s, lo + 1, hi - 1);
  }

  /// Palindrome, iterative: the same two indices in a while loop.
  public static boolean isPalindromeLoop(String s) {
    int lo = 0, hi = s.length() - 1;
    while (lo < hi) {
      if (s.charAt(lo) != s.charAt(hi)) { return false; }
      lo++;
      hi--;
    }
    return true;
  }

  /// Work BEFORE the call happens on the way down; AFTER the call on the way up.
  public static void printForward(String s) {
    if (s.isEmpty()) { return; }
    System.out.print(s.charAt(0));   // before -> forward order
    printForward(s.substring(1));
  }

  public static void printBackward(String s) {
    if (s.isEmpty()) { return; }
    printBackward(s.substring(1));
    System.out.print(s.charAt(0));   // after -> reversed order
  }

  // =========================================================================
  // 5. Arrays: sum / max with an index helper (lab05 style)
  // =========================================================================

  /// Peel one element off the front: sum(a, i) = a[i] + sum(a, i+1).
  public static int sum(int[] a, int i) {
    if (i == a.length) { return 0; }
    return a[i] + sum(a, i + 1);
  }

  /// Divide in half: sum(lo..hi) = sum(left half) + sum(right half).
  /// Same O(n) work, but only O(log n) stack depth. This is the SHAPE of
  /// merge sort.
  public static int sumHalves(int[] a, int lo, int hi) {  // [lo, hi)
    if (hi - lo == 0) { return 0; }
    if (hi - lo == 1) { return a[lo]; }
    int mid = (lo + hi) / 2;
    return sumHalves(a, lo, mid) + sumHalves(a, mid, hi);
  }

  public static int max(int[] a, int i) {
    if (i == a.length - 1) { return a[i]; }  // base case: last element
    int restMax = max(a, i + 1);
    return Math.max(a[i], restMax);
  }

  // =========================================================================
  // 6. Binary search: recursive vs iterative (array must be SORTED)
  // =========================================================================

  public static int binarySearch(int[] a, int target, int lo, int hi) { // [lo, hi]
    if (lo > hi) { return -1; }                      // base case: empty range
    int mid = (lo + hi) / 2;
    if (a[mid] == target) { return mid; }            // base case: found
    if (target < a[mid]) { return binarySearch(a, target, lo, mid - 1); }
    return binarySearch(a, target, mid + 1, hi);
  }

  public static int binarySearchLoop(int[] a, int target) {
    int lo = 0, hi = a.length - 1;
    while (lo <= hi) {
      int mid = (lo + hi) / 2;
      if (a[mid] == target) { return mid; }
      if (target < a[mid]) { hi = mid - 1; } else { lo = mid + 1; }
    }
    return -1;
  }

  // =========================================================================
  // 7. Classic math recursions
  // =========================================================================

  /// Euclid: gcd(a, b) = gcd(b, a % b), gcd(a, 0) = a.
  public static int gcd(int a, int b) {
    if (b == 0) { return a; }
    return gcd(b, a % b);
  }

  /// From the slides: a * b using only +.
  public static int multiply(int a, int b) {
    if (b < 0) { return -multiply(a, -b); }
    if (b == 0) { return 0; }
    return a + multiply(a, b - 1);
  }

  /// Sum of digits: 1234 -> 1 + 2 + 3 + 4.
  public static int digitSum(int n) {
    if (n < 10) { return n; }
    return n % 10 + digitSum(n / 10);
  }

  /// Print n in binary: recurse first, then print the last bit.
  public static String toBinary(int n) {
    if (n < 2) { return Integer.toString(n); }
    return toBinary(n / 2) + (n % 2);
  }

  // =========================================================================

  public static void main(String[] args) {
    System.out.println("== factorial ==");
    System.out.println("factorial(5)       = " + factorial(5));
    System.out.println("factorialLoop(5)   = " + factorialLoop(5));
    System.out.println("factorialTail(5,1) = " + factorialTail(5, 1));

    System.out.println("\n== power ==");
    System.out.println("power(2,10)     = " + power(2, 10));
    System.out.println("fastPower(2,10) = " + fastPower(2, 10));
    System.out.println("powerLoop(2,10) = " + powerLoop(2, 10));

    System.out.println("\n== fibonacci: same answer, very different cost ==");
    naiveCalls = 0;
    long f = fib(25);
    System.out.println("fib(25)     = " + f + "   (" + naiveCalls + " calls!)");
    Map<Integer, Long> memo = new HashMap<>();
    System.out.println("fibMemo(25) = " + fibMemo(25, memo) + "   (memo now holds " + memo.size() + " entries)");
    System.out.println("fibLoop(25) = " + fibLoop(25));
    System.out.println("fibMemo(90) = " + fibMemo(90, memo) + "   (naive would take ~10^19 calls)");

    System.out.println("\n== strings ==");
    System.out.println("length(\"ace\")      = " + length("ace"));
    System.out.println("reverse(\"stack\")     = " + reverse("stack"));
    System.out.println("reverseLoop(\"stack\") = " + reverseLoop("stack"));
    System.out.println("isPalindrome(racecar) = " + isPalindrome("racecar", 0, 6));
    System.out.println("isPalindromeLoop(abca) = " + isPalindromeLoop("abca"));
    System.out.print("printForward(\"cat\")  -> "); printForward("cat"); System.out.println();
    System.out.print("printBackward(\"cat\") -> "); printBackward("cat"); System.out.println();

    System.out.println("\n== arrays ==");
    int[] a = {2, 10, 8, 5};
    System.out.println("sum        = " + sum(a, 0));
    System.out.println("sumHalves  = " + sumHalves(a, 0, a.length));
    System.out.println("max        = " + max(a, 0));

    int[] sorted = {1, 3, 5, 7, 9, 11, 13};
    System.out.println("binarySearch(7)     = " + binarySearch(sorted, 7, 0, sorted.length - 1));
    System.out.println("binarySearchLoop(4) = " + binarySearchLoop(sorted, 4));

    System.out.println("\n== math ==");
    System.out.println("gcd(48,18)     = " + gcd(48, 18));
    System.out.println("multiply(3,-4) = " + multiply(3, -4));
    System.out.println("digitSum(1234) = " + digitSum(1234));
    System.out.println("toBinary(13)   = " + toBinary(13));
  }
}
