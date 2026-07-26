package com.dcb.tree;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

public class QueueDSA {

  public static void main(String[] args) {
    standardQueue();
    standardStack();
    standardDeque();
    standardPriorityQueue(); // min-Heap
    standardMaxHeapPriorityQueue();

  }

  private static void standardMaxHeapPriorityQueue() {
    PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder());
    maxHeap.offer(31);
    maxHeap.offer(43);
    maxHeap.offer(9763);
    maxHeap.offer(300);

    System.out.println(maxHeap.peek()); // will show max data
    System.out.println(maxHeap.poll());  //will remove max data
    System.out.println(maxHeap.poll());
  }

  private static void standardPriorityQueue() {
    PriorityQueue<Integer> minHeap = new PriorityQueue<>();
    minHeap.offer(31);
    minHeap.offer(43);
    minHeap.offer(9763);

    System.out.println(minHeap.poll());
    System.out.println(minHeap.poll());
  }

  private static void standardDeque() {
    Deque<String> dq = new ArrayDeque<>();
    dq.add("Customer 1"); //same as addLast
    dq.addLast("Customer 2");
    dq.addFirst("VIP person");

    System.out.println(dq.removeFirst());
    System.out.println(dq.removeLast());
  }

  private static void standardStack() {
//    Stack<String> stack = new Stack<>(); //this is old Java method
    Deque<String> browserHistory = new ArrayDeque<>(); //new method to use Stack
    //See deque is double ended queue and that means you can do operations like
    //offerFirst() , offerLast(), popFirst() , popLast();
    //ArrayDeque is circular Array where front and tail pointers are there to check front and
//    back positions which is better alternative than LinkedList for queues

//    In Stack only push and pop operation are used so we also should use the same
//     Although Deque gives us all other operations to perform

    browserHistory.push("google");
    browserHistory.push("Microsoft");
    browserHistory.push("yahoo"); //it adds elements from front

    System.out.println(browserHistory.pop()); // it removes elements from front
    System.out.println(browserHistory.pop());
    System.out.println(browserHistory.pop());
    //This is the best implementation for Stack as it removes

  }

  private static void standardQueue() {
    //Standard Queue
    Queue<String> checkoutLine = new LinkedList<>();



    checkoutLine.offer("first");
    checkoutLine.add("second"); // this is not standard practice for Queue
    checkoutLine.offer("third");

    System.out.println(checkoutLine.poll());
    System.out.println(checkoutLine.remove());
  }

}
