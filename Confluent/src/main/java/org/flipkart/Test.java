package org.flipkart;

import java.util.HashMap;
import java.util.Map;

public class Test {

    class Node{
        String key;
        int value;
        long expiryTime;
        Node prev, next;
    }

    Map<String,Node> map = new HashMap<>();
    long windowMillis;
    long sum = 0;
    int count = 0;
    Node head = null, tail = null;


    public Test(long windowMillis){
        this.windowMillis = windowMillis;
    }

    public void put(String key, int value){
        removeExpired();
        long expiryTime = System.currentTimeMillis() + windowMillis;
        if(map.containsKey(key)){
            Node n = map.get(key);
            sum = sum - n.value + value;
            n.value = value;
            n.expiryTime = expiryTime;
            removeNode(n);
            addNodeAtTail(n);
        }else{
            Node n = new Node();
            n.key = key;
            n.value = value;
            n.expiryTime = expiryTime;
            addNodeAtTail(n);
            map.put(key, n);
            sum += value;
            count++;
        }
    }

    private void removeNode(Node n) {
        if(n.prev != null){
            n.prev.next = n.next;
        }else{
            head = n.next;
        }
        if(n.next != null){
            n.next.prev = n.prev;
        }else{
            tail = n.prev;
        }
    }

    private void addNodeAtTail(Node n) {
        if(tail != null){
            tail.next = n;
            n.prev = tail;
        }else{
            head = n;
        }
        tail = n;
    }

    public int getKey(String key){
        removeExpired();
        Node n = map.get(key);
        if(n == null){
            return -1;
        }
        return n.value;
    }

    public double avg(){
        removeExpired();
        if(count == 0) return 0.0;
        return (double)sum / count;
    }


    public void delete(String key){

    }

    public void removeExpired(){
        long now = System.currentTimeMillis();
        while(head != null && head.expiryTime <= now){
            Node n = head;
            removeNode(n);
            map.remove(n.key);
            sum -= n.value;
            count--;
        }
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
