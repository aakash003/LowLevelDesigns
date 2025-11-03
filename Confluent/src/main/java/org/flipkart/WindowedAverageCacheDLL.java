package org.flipkart;
import java.util.*;

public class WindowedAverageCacheDLL {

    class Node {
        String key;
        int value;
        long expiryTime;
        Node prev, next;
        Node(String k, int v, long exp) {
            key = k; value = v; expiryTime = exp;
        }
    }

    private final Map<String, Node> map = new HashMap<>();
    private final long windowMillis;
    private Node head = null, tail = null;
    private long sum = 0;
    private int count = 0;

    public WindowedAverageCacheDLL(long windowMillis) {
        this.windowMillis = windowMillis;
    }

    private void removeExpired() {
        long now = System.currentTimeMillis();
        while (head != null && head.expiryTime <= now) {
            Node n = head;
            map.remove(n.key);
            sum -= n.value;
            count--;
            head = head.next;
            if (head != null) head.prev = null;
            if (tail == n) tail = null; // if single element
        }
    }

    private void removeNode(Node n) {
        if (n.prev != null) n.prev.next = n.next;
        else head = n.next;
        if (n.next != null) n.next.prev = n.prev;
        else tail = n.prev;
    }

    private void addNodeAtTail(Node n) {
        if (tail != null) {
            tail.next = n;
            n.prev = tail;
        } else {
            head = n;
        }
        tail = n;
    }

    public void put(String key, int value) {
        removeExpired();
        long expiryTime = System.currentTimeMillis() + windowMillis;

        if (map.containsKey(key)) {
            Node node = map.get(key);
            sum -= node.value;
            node.value = value;
            node.expiryTime = expiryTime;

            // move to tail since it's newest
            removeNode(node);
            addNodeAtTail(node);
            sum += value;
        } else {
            Node n = new Node(key, value, expiryTime);
            map.put(key, n);
            addNodeAtTail(n);
            sum += value;
            count++;
        }
    }

    public int get(String key) {
        removeExpired();
        if (!map.containsKey(key)) return -1;
        Node n = map.get(key);
        if (n.expiryTime < System.currentTimeMillis()) {
            removeNode(n);
            map.remove(key);
            sum -= n.value;
            count--;
            return -1;
        }
        return n.value;
    }

    public double avg() {
        removeExpired();
        return count == 0 ? 0.0 : (double) sum / count;
    }

    // Simple demo
    public static void main(String[] args) throws InterruptedException {
        WindowedAverageCacheDLL cache = new WindowedAverageCacheDLL(3000); // 3 sec window
        cache.put("foo", 10);
        Thread.sleep(1000);
        cache.put("bar", 20);
        System.out.println("Get foo: " + cache.get("foo")); // 10
        System.out.println("Average: " + cache.avg());      // 15.0

        Thread.sleep(3000); // Let expire
        System.out.println("Get foo after expiry: " + cache.get("foo")); // -1
        System.out.println("Average after expiry: " + cache.avg()); // 0.0
    }
}
