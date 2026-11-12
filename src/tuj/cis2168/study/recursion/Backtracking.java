package tuj.cis2168.study.recursion;

import java.util.ArrayList;
import java.util.List;

/// Study guide: recursion that makes CHOICES (see study/01-recursion.md).
///
/// "Generate every permutation / subset / move" problems are where recursion
/// beats loops: you don't know how many nested loops you'd need.
///
/// Template ("choose, explore, un-choose"):
///     if (no choices left) { record the answer; return; }
///     for each choice:
///         make the choice
///         recurse on the smaller problem
///         undo the choice
///
/// Run:  java src/tuj/cis2168/study/recursion/Backtracking.java
public class Backtracking {

  // =========================================================================
  // Permutations of a string: n! results
  // =========================================================================

  /// Version 1 (string building): pick each remaining char to go next.
  ///   perms("", "abc")
  ///     -> perms("a", "bc") -> perms("ab","c") -> perms("abc","")  record
  ///                          -> perms("ac","b") -> perms("acb","")  record
  ///     -> perms("b", "ac") ...
  ///     -> perms("c", "ab") ...
  static void permutations(String soFar, String remaining, List<String> out) {
    if (remaining.isEmpty()) {          // base case: every char placed
      out.add(soFar);
      return;
    }
    for (int i = 0; i < remaining.length(); i++) {
      char choice = remaining.charAt(i);
      String rest = remaining.substring(0, i) + remaining.substring(i + 1);
      permutations(soFar + choice, rest, out);   // no "un-choose" needed: strings are immutable
    }
  }

  /// Version 2 (in-place swapping on an array): the classic backtracking form.
  /// Positions [0, k) are fixed; try every element in position k.
  static void permuteArray(int[] a, int k, List<String> out) {
    if (k == a.length) {
      out.add(java.util.Arrays.toString(a));
      return;
    }
    for (int i = k; i < a.length; i++) {
      swap(a, k, i);            // choose: put a[i] in position k
      permuteArray(a, k + 1, out);
      swap(a, k, i);            // un-choose: put it back
    }
  }

  static void swap(int[] a, int i, int j) {
    int t = a[i];
    a[i] = a[j];
    a[j] = t;
  }

  // =========================================================================
  // Subsets (power set): 2^n results. Each element: IN or OUT.
  // =========================================================================

  static void subsets(List<Character> items, int i, List<Character> current, List<String> out) {
    if (i == items.size()) {
      out.add(current.toString());
      return;
    }
    // choice 1: leave items[i] out
    subsets(items, i + 1, current, out);
    // choice 2: put items[i] in
    current.add(items.get(i));
    subsets(items, i + 1, current, out);
    current.remove(current.size() - 1);          // un-choose
  }

  /// Iterative version of subsets: use the bits of a counter 0 .. 2^n - 1.
  static List<String> subsetsLoop(List<Character> items) {
    List<String> out = new ArrayList<>();
    int n = items.size();
    for (int mask = 0; mask < (1 << n); mask++) {
      List<Character> s = new ArrayList<>();
      for (int bit = 0; bit < n; bit++) {
        if ((mask & (1 << bit)) != 0) { s.add(items.get(bit)); }
      }
      out.add(s.toString());
    }
    return out;
  }

  // =========================================================================
  // Towers of Hanoi: 2^n - 1 moves
  // =========================================================================

  /// To move n disks from `from` to `to`:
  ///   1. move the top n-1 out of the way (to `via`)
  ///   2. move the biggest disk
  ///   3. move the n-1 back on top of it
  static int hanoi(int n, char from, char to, char via, boolean print) {
    if (n == 0) { return 0; }
    int moves = hanoi(n - 1, from, via, to, print);
    if (print) { System.out.println("  move disk " + n + " " + from + " -> " + to); }
    moves++;
    moves += hanoi(n - 1, via, to, from, print);
    return moves;
  }

  // =========================================================================
  // Counting paths in a grid (only right / down): recursion + base cases on edges
  // =========================================================================

  static long gridPaths(int rows, int cols) {
    if (rows == 1 || cols == 1) { return 1; }
    return gridPaths(rows - 1, cols) + gridPaths(rows, cols - 1);
  }

  // =========================================================================

  public static void main(String[] args) {
    List<String> perms = new ArrayList<>();
    permutations("", "abc", perms);
    System.out.println("permutations(\"abc\") = " + perms + "  (3! = " + perms.size() + ")");

    List<String> permsArr = new ArrayList<>();
    permuteArray(new int[] {1, 2, 3}, 0, permsArr);
    System.out.println("permuteArray({1,2,3}) = " + permsArr);

    List<String> subs = new ArrayList<>();
    subsets(List.of('a', 'b', 'c'), 0, new ArrayList<>(), subs);
    System.out.println("subsets(abc)     = " + subs + "  (2^3 = " + subs.size() + ")");
    System.out.println("subsetsLoop(abc) = " + subsetsLoop(List.of('a', 'b', 'c')));

    System.out.println("hanoi(3):");
    int moves = hanoi(3, 'A', 'C', 'B', true);
    System.out.println("  total moves = " + moves + " (2^3 - 1)");

    System.out.println("gridPaths(3,3) = " + gridPaths(3, 3));
  }
}
