package tuj.cis2168.study.sorting;

import java.util.Arrays;
import java.util.Random;

/// Study guide: see the Big-O table come true (see study/03-sorting.md).
///
/// Counts COMPARISONS for each of the five sorts on random, already-sorted and
/// reverse-sorted input. Watch for:
///   * insertion sort: ~n on sorted input (best case), ~n^2/2 on reversed
///   * selection sort: ~n^2/2 no matter what
///   * heap / merge  : ~n log n no matter what
///   * quick (last-element pivot): ~n log n on random, ~n^2/2 on sorted/reversed
///
/// Run:  java src/tuj/cis2168/study/sorting/SortComparison.java
public class SortComparison {

  static long cmp;

  static boolean less(int x, int y) { cmp++; return x < y; }

  static void swap(int[] a, int i, int j) { int t = a[i]; a[i] = a[j]; a[j] = t; }

  static void insertion(int[] a) {
    for (int i = 1; i < a.length; i++) {
      int key = a[i], j = i - 1;
      while (j >= 0 && less(key, a[j])) { a[j + 1] = a[j]; j--; }
      a[j + 1] = key;
    }
  }

  static void selection(int[] a) {
    for (int i = 0; i < a.length - 1; i++) {
      int m = i;
      for (int j = i + 1; j < a.length; j++) { if (less(a[j], a[m])) { m = j; } }
      swap(a, i, m);
    }
  }

  static void heap(int[] a) {
    int n = a.length;
    for (int i = n / 2 - 1; i >= 0; i--) { sift(a, i, n); }
    for (int end = n - 1; end > 0; end--) { swap(a, 0, end); sift(a, 0, end); }
  }

  static void sift(int[] a, int i, int n) {
    while (2 * i + 1 < n) {
      int c = 2 * i + 1;
      if (c + 1 < n && less(a[c], a[c + 1])) { c++; }
      if (!less(a[i], a[c])) { return; }
      swap(a, i, c);
      i = c;
    }
  }

  static void merge(int[] a) { mergeSort(a, 0, a.length - 1, new int[a.length]); }

  static void mergeSort(int[] a, int lo, int hi, int[] t) {
    if (lo >= hi) { return; }
    int mid = (lo + hi) / 2;
    mergeSort(a, lo, mid, t);
    mergeSort(a, mid + 1, hi, t);
    int i = lo, j = mid + 1, k = lo;
    while (i <= mid && j <= hi) { t[k++] = less(a[j], a[i]) ? a[j++] : a[i++]; }
    while (i <= mid) { t[k++] = a[i++]; }
    while (j <= hi) { t[k++] = a[j++]; }
    System.arraycopy(t, lo, a, lo, hi - lo + 1);
  }

  static void quick(int[] a) { quickSort(a, 0, a.length - 1); }

  static void quickSort(int[] a, int lo, int hi) {
    if (lo >= hi) { return; }
    int pivot = a[hi], i = lo - 1;
    for (int j = lo; j < hi; j++) { if (!less(pivot, a[j])) { swap(a, ++i, j); } }
    swap(a, i + 1, hi);
    quickSort(a, lo, i);
    quickSort(a, i + 2, hi);
  }

  interface Sorter { void sort(int[] a); }

  public static void main(String[] args) {
    int n = 1000;
    int[] random = new Random(2168).ints(n, 0, 1_000_000).toArray();
    int[] sorted = random.clone();
    Arrays.sort(sorted);
    int[] reversed = new int[n];
    for (int i = 0; i < n; i++) { reversed[i] = sorted[n - 1 - i]; }

    String[] names = {"insertion", "selection", "heap", "merge", "quick (last pivot)"};
    Sorter[] sorters = {SortComparison::insertion, SortComparison::selection,
        SortComparison::heap, SortComparison::merge, SortComparison::quick};

    System.out.printf("comparisons for n = %d   (n^2/2 = %d,  n log2 n = %d)%n%n",
        n, n * n / 2, (int) (n * Math.log(n) / Math.log(2)));
    System.out.printf("%-20s %10s %10s %10s%n", "sort", "random", "sorted", "reversed");
    for (int s = 0; s < sorters.length; s++) {
      long[] r = new long[3];
      int[][] inputs = {random, sorted, reversed};
      for (int k = 0; k < 3; k++) {
        int[] a = inputs[k].clone();
        cmp = 0;
        sorters[s].sort(a);
        for (int i = 1; i < n; i++) { if (a[i - 1] > a[i]) { throw new AssertionError(names[s]); } }
        r[k] = cmp;
      }
      System.out.printf("%-20s %10d %10d %10d%n", names[s], r[0], r[1], r[2]);
    }
  }
}
