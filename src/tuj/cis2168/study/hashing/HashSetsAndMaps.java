package tuj.cis2168.study.hashing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/// Study guide: using Sets and Maps, and the SAME PROBLEM solved with
/// arrays, sorting, a BST (TreeSet/TreeMap), and a hash table (HashSet/HashMap).
/// (see study/04-hash-tables.md)
///
///   Problem A: does an array contain a duplicate?
///   Problem B: count how often each word appears
///   Problem C: two-sum (is there a pair adding up to target?)
///   Problem D: set operations (union / intersection / difference)
///   Problem E: does a LINKED LIST have a cycle? (hash set of nodes vs two pointers)
///   Plus: the hashCode()/equals() contract, and what breaks if you forget it.
///
/// Run:  java src/tuj/cis2168/study/hashing/HashSetsAndMaps.java
public class HashSetsAndMaps {

  // =========================================================================
  // Problem A: contains a duplicate?
  // =========================================================================

  /// Nested loops: compare every pair. O(n^2) time, O(1) space.
  static boolean hasDupBrute(int[] a) {
    for (int i = 0; i < a.length; i++) {
      for (int j = i + 1; j < a.length; j++) {
        if (a[i] == a[j]) { return true; }
      }
    }
    return false;
  }

  /// Sort, then duplicates are NEIGHBOURS. O(n log n) time.
  static boolean hasDupSort(int[] a) {
    int[] s = a.clone();
    Arrays.sort(s);
    for (int i = 1; i < s.length; i++) { if (s[i] == s[i - 1]) { return true; } }
    return false;
  }

  /// BST-backed set (TreeSet is a balanced BST). O(n log n).
  static boolean hasDupTree(int[] a) {
    Set<Integer> seen = new TreeSet<>();
    for (int x : a) { if (!seen.add(x)) { return true; } }   // add() returns false if present
    return false;
  }

  /// Hash set. O(n) average. Same code as the TreeSet version: only the class changes!
  static boolean hasDupHash(int[] a) {
    Set<Integer> seen = new HashSet<>();
    for (int x : a) { if (!seen.add(x)) { return true; } }
    return false;
  }

  /// Recursive with a hash set (because we can).
  static boolean hasDupRec(int[] a, int i, Set<Integer> seen) {
    if (i == a.length) { return false; }
    if (!seen.add(a[i])) { return true; }
    return hasDupRec(a, i + 1, seen);
  }

  // =========================================================================
  // Problem B: word frequency
  // =========================================================================

  /// Two parallel lists, linear search for each word. O(n * distinct).
  static String countWithLists(String[] words) {
    List<String> keys = new ArrayList<>();
    List<Integer> counts = new ArrayList<>();
    for (String w : words) {
      int i = keys.indexOf(w);                 // O(distinct) scan
      if (i < 0) { keys.add(w); counts.add(1); } else { counts.set(i, counts.get(i) + 1); }
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < keys.size(); i++) { sb.append(keys.get(i)).append('=').append(counts.get(i)).append(' '); }
    return sb.toString().trim();
  }

  /// Sort, then count runs of equal words. O(n log n).
  static String countWithSort(String[] words) {
    String[] s = words.clone();
    Arrays.sort(s);
    StringBuilder sb = new StringBuilder();
    int run = 1;
    for (int i = 1; i <= s.length; i++) {
      if (i < s.length && s[i].equals(s[i - 1])) { run++; }
      else { sb.append(s[i - 1]).append('=').append(run).append(' '); run = 1; }
    }
    return sb.toString().trim();
  }

  /// TreeMap: O(n log n), keys come out SORTED.
  static Map<String, Integer> countWithTreeMap(String[] words) {
    Map<String, Integer> m = new TreeMap<>();
    for (String w : words) { m.put(w, m.getOrDefault(w, 0) + 1); }
    return m;
  }

  /// HashMap: O(n) average, keys come out in no particular order.
  static Map<String, Integer> countWithHashMap(String[] words) {
    Map<String, Integer> m = new HashMap<>();
    for (String w : words) {
      if (m.containsKey(w)) { m.put(w, m.get(w) + 1); }   // the long way
      else { m.put(w, 1); }
      // short ways: m.put(w, m.getOrDefault(w, 0) + 1);   or   m.merge(w, 1, Integer::sum);
    }
    return m;
  }

  /// LinkedHashMap: a hash table PLUS a linked list through the entries, so it
  /// iterates in INSERTION order (first time each word was seen). O(n) average.
  /// (Our own ChainingHashMap.java works the same way too: get, then put c + 1.)
  static Map<String, Integer> countWithLinkedHashMap(String[] words) {
    Map<String, Integer> m = new java.util.LinkedHashMap<>();
    for (String w : words) { m.merge(w, 1, Integer::sum); }
    return m;
  }

  // =========================================================================
  // Problem C: two-sum
  // =========================================================================

  static boolean twoSumBrute(int[] a, int target) {          // O(n^2)
    for (int i = 0; i < a.length; i++) {
      for (int j = i + 1; j < a.length; j++) { if (a[i] + a[j] == target) { return true; } }
    }
    return false;
  }

  static boolean twoSumSorted(int[] a, int target) {         // O(n log n): sort + two pointers
    int[] s = a.clone();
    Arrays.sort(s);
    int lo = 0, hi = s.length - 1;
    while (lo < hi) {
      int sum = s[lo] + s[hi];
      if (sum == target) { return true; }
      if (sum < target) { lo++; } else { hi--; }
    }
    return false;
  }

  static boolean twoSumHash(int[] a, int target) {           // O(n): "have I seen my partner?"
    Set<Integer> seen = new HashSet<>();
    for (int x : a) {
      if (seen.contains(target - x)) { return true; }
      seen.add(x);
    }
    return false;
  }

  // =========================================================================
  // Problem D: set operations
  // =========================================================================

  static <T> Set<T> union(Set<T> x, Set<T> y) { Set<T> r = new HashSet<>(x); r.addAll(y); return r; }
  static <T> Set<T> intersection(Set<T> x, Set<T> y) { Set<T> r = new HashSet<>(x); r.retainAll(y); return r; }
  static <T> Set<T> difference(Set<T> x, Set<T> y) { Set<T> r = new HashSet<>(x); r.removeAll(y); return r; }

  /// Intersection of two SORTED arrays without any hashing: merge-style walk. O(n + m).
  static List<Integer> intersectSorted(int[] x, int[] y) {
    List<Integer> out = new ArrayList<>();
    int i = 0, j = 0;
    while (i < x.length && j < y.length) {
      if (x[i] == y[j]) { out.add(x[i]); i++; j++; }
      else if (x[i] < y[j]) { i++; } else { j++; }
    }
    return out;
  }

  // =========================================================================
  // Problem E: linked list cycle. Hash set of NODES (identity) vs Floyd's two pointers.
  // =========================================================================

  static class Node {
    int data;
    Node next;

    Node(int d) { data = d; }
  }

  static boolean hasCycleHash(Node head) {                   // O(n) time, O(n) space
    Set<Node> visited = new HashSet<>();   // Node has no equals/hashCode -> identity, which is what we want
    for (Node n = head; n != null; n = n.next) {
      if (!visited.add(n)) { return true; }
    }
    return false;
  }

  static boolean hasCycleFloyd(Node head) {                  // O(n) time, O(1) space
    Node slow = head, fast = head;
    while (fast != null && fast.next != null) {
      slow = slow.next;
      fast = fast.next.next;
      if (slow == fast) { return true; }
    }
    return false;
  }

  // =========================================================================
  // The hashCode / equals contract
  //   If a.equals(b) then a.hashCode() == b.hashCode()   (MUST)
  //   Equal hash codes do NOT imply equal objects (collisions are allowed).
  // Override equals() without hashCode() -> two "equal" objects land in
  // different buckets, so HashSet.contains() says false.
  // =========================================================================

  static class BadPoint {
    final int x, y;

    BadPoint(int x, int y) { this.x = x; this.y = y; }

    @Override
    public boolean equals(Object o) {
      return o instanceof BadPoint p && p.x == x && p.y == y;
    }
    // no hashCode(): inherits Object.hashCode(), which is (usually) different per object
  }

  static class GoodPoint {
    final int x, y;

    GoodPoint(int x, int y) { this.x = x; this.y = y; }

    @Override
    public boolean equals(Object o) {
      return o instanceof GoodPoint p && p.x == x && p.y == y;
    }

    @Override
    public int hashCode() {
      return Objects.hash(x, y);            // = 31 * (31 + x) + y
    }
  }

  /// How String.hashCode works: s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]
  /// computed with Horner's rule (iteratively) and recursively.
  static int stringHash(String s) {
    int h = 0;
    for (int i = 0; i < s.length(); i++) { h = 31 * h + s.charAt(i); }
    return h;
  }

  static int stringHashRec(String s, int i, int acc) {
    if (i == s.length()) { return acc; }
    return stringHashRec(s, i + 1, 31 * acc + s.charAt(i));
  }

  // =========================================================================

  public static void main(String[] args) {
    int[] a = {3, 9, 4, 1, 9, 7};
    System.out.println("A. duplicate in " + Arrays.toString(a) + "?");
    System.out.println("   brute " + hasDupBrute(a) + " | sort " + hasDupSort(a) + " | TreeSet " + hasDupTree(a)
        + " | HashSet " + hasDupHash(a) + " | recursive+hash " + hasDupRec(a, 0, new HashSet<>()));

    String[] words = "the cat and the hat and the bat".split(" ");
    System.out.println("\nB. word counts");
    System.out.println("   parallel lists : " + countWithLists(words));
    System.out.println("   sort + runs    : " + countWithSort(words));
    System.out.println("   TreeMap        : " + countWithTreeMap(words) + "   <- sorted keys");
    System.out.println("   HashMap        : " + countWithHashMap(words) + "   <- any order");
    System.out.println("   LinkedHashMap  : " + countWithLinkedHashMap(words) + "   <- first-seen order");

    int[] nums = {8, 3, 11, 5, 2};
    System.out.println("\nC. two-sum to 13 in " + Arrays.toString(nums) + ": brute " + twoSumBrute(nums, 13)
        + " | sorted+2ptr " + twoSumSorted(nums, 13) + " | hash " + twoSumHash(nums, 13)
        + ";  to 4: " + twoSumHash(nums, 4));

    Set<String> x = new HashSet<>(List.of("a", "b", "c", "d"));
    Set<String> y = new HashSet<>(List.of("c", "d", "e"));
    System.out.println("\nD. x=" + x + " y=" + y);
    System.out.println("   union " + union(x, y) + "  intersection " + intersection(x, y) + "  x - y " + difference(x, y));
    System.out.println("   intersectSorted([1,3,5,7,9],[3,4,5,9]) = " + intersectSorted(new int[] {1, 3, 5, 7, 9}, new int[] {3, 4, 5, 9}));

    Node n1 = new Node(1), n2 = new Node(2), n3 = new Node(3), n4 = new Node(4);
    n1.next = n2; n2.next = n3; n3.next = n4;
    System.out.print("\nE. 1->2->3->4: hash " + hasCycleHash(n1) + ", floyd " + hasCycleFloyd(n1));
    n4.next = n2;
    System.out.println(";   1->2->3->4->(back to 2): hash " + hasCycleHash(n1) + ", floyd " + hasCycleFloyd(n1));

    Set<BadPoint> bad = new HashSet<>();
    bad.add(new BadPoint(1, 2));
    Set<GoodPoint> good = new HashSet<>();
    good.add(new GoodPoint(1, 2));
    System.out.println("\nequals without hashCode: contains(new BadPoint(1,2))  = " + bad.contains(new BadPoint(1, 2)) + "   <- broken");
    System.out.println("equals AND hashCode   : contains(new GoodPoint(1,2)) = " + good.contains(new GoodPoint(1, 2)));

    System.out.println("\n\"cat\".hashCode() = " + "cat".hashCode() + ", stringHash = " + stringHash("cat")
        + ", stringHashRec = " + stringHashRec("cat", 0, 0));
    System.out.println("   = 'c'*31^2 + 'a'*31 + 't' = 99*961 + 97*31 + 116 = " + (99 * 961 + 97 * 31 + 116));
  }
}
