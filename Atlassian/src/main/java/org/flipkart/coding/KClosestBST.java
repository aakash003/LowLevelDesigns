package org.flipkart.coding;

import java.util.*;

public class KClosestBST {
    static PriorityQueue<Pair> queue;

    static class Node {
        int key;
        Node left, right;
    }

    static class Pair {
        int diff;
        int nodeVal;

        public Pair(int diff, int nodeVal) {
            this.diff = diff;
            this.nodeVal = nodeVal;
        }
    }


    static Node newnode(int key) {

        Node node = new Node();
        node.key = key;
        node.left = node.right = null;
        return (node);
    }

    static void maxDiffUtil(Node ptr, int k, int target) {
        if (ptr == null)
            return;

        queue.offer(new Pair(Math.abs(ptr.key - target), ptr.key));
        //System.out.println("queue : " + Math.abs(ptr.key - target) + " " + ptr.key);
        if (queue.size() > k) {
            queue.poll();
        }
        if (target < ptr.key)
            maxDiffUtil(ptr.left, k, target);
        else
            maxDiffUtil(ptr.right, k, target);
    }

    static void maxDiff(Node root, int k, int target) {
        queue = new PriorityQueue<Pair>((a, b) -> {
            if (a.diff < b.diff) {
                return 1;
            } else {
                return -1;
            }
        });

        maxDiffUtil(root, k, target);
    }

    public static void main(String args[]) {

        Node root = newnode(10);
        root.left = newnode(5);
        root.right = newnode(20);
        root.left.left = newnode(1);
        root.left.right = newnode(6);
        root.left.right.right = newnode(7);
        root.right.right = newnode(35);
        root.right.left = newnode(19);
        int k = 3;
        int target = 8;
        maxDiff(root, k, target);
        for (int i = 0; i < k; i++) {
            Pair p = queue.poll();
            System.out.println(p.nodeVal);
        }
    }
}