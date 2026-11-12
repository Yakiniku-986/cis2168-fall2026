package tuj.cis2168.study.trees;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Arrays;

/// Study guide: expression trees (see study/02-trees.md).
///
/// Leaves are NUMBERS, internal nodes are OPERATORS (+ - * /).
///     (3 + 4) * 2       is          *
///                                  / \
///                                 +   2
///                                / \
///                               3   4
///
/// The three traversals ARE the three notations:
///   pre-order  -> prefix  (Polish)          * + 3 4 2
///   in-order   -> infix   (needs parens)    ((3 + 4) * 2)
///   post-order -> postfix (Reverse Polish)  3 4 + 2 *
///
/// Shown here:
///   BUILD    : from postfix with a STACK (lab05)  /  from prefix with RECURSION  /
///              from fully-parenthesized infix with RECURSION
///   EVALUATE : recursive (post-order)  /  iterative with explicit stack  /
///              straight from a postfix string, no tree at all (lecture10 PostfixEvaluator)
///   PRINT    : prefix, infix, postfix
///
/// lab05 used polymorphism (Expr interface, LiteralExpr, BinaryExpr). Here we use
/// one node class with a String token to keep the traversal code side by side.
/// Both designs are fine; know both.
///
/// Run:  java src/tuj/cis2168/study/trees/ExpressionTreeDemo.java
public class ExpressionTreeDemo {

  static class Node {
    String token;      // "+", "-", "*", "/" or a number
    Node left, right;

    Node(String token, Node left, Node right) {
      this.token = token;
      this.left = left;
      this.right = right;
    }

    boolean isLeaf() { return left == null && right == null; }
  }

  static boolean isOperator(String s) {
    return s.equals("+") || s.equals("-") || s.equals("*") || s.equals("/");
  }

  static int apply(String op, int x, int y) {
    switch (op) {
      case "+": return x + y;
      case "-": return x - y;
      case "*": return x * y;
      case "/": return x / y;
      default: throw new IllegalArgumentException("unknown operator " + op);
    }
  }

  // =========================================================================
  // BUILD 1: from POSTFIX with a stack
  //   number   -> push a leaf
  //   operator -> pop RIGHT first, then LEFT, push new node(op, left, right)
  // =========================================================================

  static Node fromPostfix(String postfix) {
    Deque<Node> stack = new ArrayDeque<>();
    for (String tok : postfix.trim().split("\\s+")) {
      if (isOperator(tok)) {
        Node right = stack.pop();         // ORDER MATTERS: right comes off first
        Node left = stack.pop();
        stack.push(new Node(tok, left, right));
      } else {
        stack.push(new Node(tok, null, null));
      }
    }
    if (stack.size() != 1) { throw new IllegalArgumentException("malformed postfix"); }
    return stack.pop();
  }

  // =========================================================================
  // BUILD 2: from PREFIX with recursion (consume tokens left to right)
  //   read a token; if it's an operator, its left operand is "the next whole
  //   expression" and its right operand is "the expression after that".
  // =========================================================================

  static Node fromPrefix(Iterator<String> tokens) {
    String tok = tokens.next();
    if (!isOperator(tok)) { return new Node(tok, null, null); }   // base case
    Node left = fromPrefix(tokens);
    Node right = fromPrefix(tokens);
    return new Node(tok, left, right);
  }

  // =========================================================================
  // BUILD 3: from FULLY PARENTHESIZED infix with recursion: "( A op B )"
  // =========================================================================

  static Node fromInfix(Iterator<String> tokens) {
    String tok = tokens.next();
    if (!tok.equals("(")) { return new Node(tok, null, null); }   // a number
    Node left = fromInfix(tokens);
    String op = tokens.next();
    Node right = fromInfix(tokens);
    tokens.next();                                                 // skip ")"
    return new Node(op, left, right);
  }

  // =========================================================================
  // EVALUATE
  // =========================================================================

  /// Recursive: a post-order traversal (children first, then combine).
  static int evaluate(Node t) {
    if (t.isLeaf()) { return Integer.parseInt(t.token); }   // base case
    int x = evaluate(t.left);
    int y = evaluate(t.right);
    return apply(t.token, x, y);
  }

  /// Iterative: produce the postfix order with a stack (reverse of
  /// node-right-left), then run the postfix evaluator over it.
  static int evaluateLoop(Node root) {
    Deque<Node> order = new ArrayDeque<>();
    Deque<Node> stack = new ArrayDeque<>();
    stack.push(root);
    while (!stack.isEmpty()) {
      Node n = stack.pop();
      order.push(n);
      if (n.left != null) { stack.push(n.left); }
      if (n.right != null) { stack.push(n.right); }
    }
    Deque<Integer> values = new ArrayDeque<>();
    for (Node n : order) {                      // order is now post-order
      if (n.isLeaf()) {
        values.push(Integer.parseInt(n.token));
      } else {
        int y = values.pop(), x = values.pop();
        values.push(apply(n.token, x, y));
      }
    }
    return values.pop();
  }

  /// No tree at all: evaluate a postfix string directly (lecture10).
  static int evaluatePostfix(String postfix, boolean trace) {
    Deque<Integer> stack = new ArrayDeque<>();
    for (String tok : postfix.trim().split("\\s+")) {
      if (isOperator(tok)) {
        int y = stack.pop(), x = stack.pop();
        stack.push(apply(tok, x, y));
      } else {
        stack.push(Integer.parseInt(tok));
      }
      if (trace) { System.out.printf("    %-3s stack(top first) = %s%n", tok, stack); }
    }
    return stack.pop();
  }

  // =========================================================================
  // PRINT
  // =========================================================================

  static String prefix(Node t) {
    if (t.isLeaf()) { return t.token; }
    return t.token + " " + prefix(t.left) + " " + prefix(t.right);
  }

  static String infix(Node t) {
    if (t.isLeaf()) { return t.token; }
    return "(" + infix(t.left) + " " + t.token + " " + infix(t.right) + ")";
  }

  static String postfix(Node t) {
    if (t.isLeaf()) { return t.token; }
    return postfix(t.left) + " " + postfix(t.right) + " " + t.token;
  }

  static int height(Node t) { return t == null ? -1 : 1 + Math.max(height(t.left), height(t.right)); }

  static void printSideways(Node t, String indent) {
    if (t == null) { return; }
    printSideways(t.right, indent + "    ");
    System.out.println(indent + t.token);
    printSideways(t.left, indent + "    ");
  }

  // =========================================================================

  public static void main(String[] args) {
    String post = "3 4 + 2 * 7 1 - /";      // ((3 + 4) * 2) / (7 - 1)
    Node tree = fromPostfix(post);

    System.out.println("fromPostfix(\"" + post + "\"):");
    printSideways(tree, "  ");
    System.out.println("prefix  : " + prefix(tree));
    System.out.println("infix   : " + infix(tree));
    System.out.println("postfix : " + postfix(tree));
    System.out.println("height  : " + height(tree));

    System.out.println("\nevaluate (recursive)       = " + evaluate(tree));
    System.out.println("evaluateLoop (stack)       = " + evaluateLoop(tree));
    System.out.println("evaluatePostfix (no tree), trace:");
    int v = evaluatePostfix(post, true);
    System.out.println("  = " + v);

    List<String> pre = Arrays.asList(prefix(tree).split(" "));
    Node fromPre = fromPrefix(pre.iterator());
    System.out.println("\nfromPrefix(" + pre + ") -> infix " + infix(fromPre) + " = " + evaluate(fromPre));

    List<String> in = Arrays.asList("( ( 3 + 4 ) * ( 10 - 4 ) )".split(" "));
    Node fromIn = fromInfix(in.iterator());
    System.out.println("fromInfix(\"( ( 3 + 4 ) * ( 10 - 4 ) )\") -> postfix " + postfix(fromIn)
        + " = " + evaluate(fromIn));
  }
}
