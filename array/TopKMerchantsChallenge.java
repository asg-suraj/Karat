package com.dcb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;

public class TopKMerchantsChallenge {

  // 1. The Data Model
  public static class Transaction {

    public String merchant;
    public int amount;

    public Transaction(String merchant, int amount) {
      this.merchant = merchant;
      this.amount = amount;
    }
  }

  // 2. The Method to Implement
  public static List<String> getTopKMerchants(List<Transaction> transactions, int k) {
    // TODO: 1. Group by merchant and sum the amounts
    Map<String, Integer> merchantTotal = new HashMap<>();
    for (Transaction t : transactions) {
      merchantTotal.merge(t.merchant, t.amount, Integer::sum);
    }

    System.out.println(merchantTotal);
    // TODO: 2. Use a bounded Min-Heap to find the top K merchants
    //use Queue of Map.Entry and sort based on Value
    PriorityQueue<Map.Entry<String, Integer>> minHeap =
        new PriorityQueue<>((o1, o2) -> o1.getValue().compareTo(o2.getValue()));

    for (Map.Entry<String, Integer> entry : merchantTotal.entrySet()) {
      minHeap.offer(entry);

      if (minHeap.size() > k) {
        minHeap.poll();
      }
    }

    // TODO: 3. Return the names of the top K merchants, sorted highest to lowest

    List<String> ans = new ArrayList<>();

    while (!minHeap.isEmpty()) {
      //minheap will return smallest element everytime so to avoid we will add in front
      Entry<String, Integer> polled = minHeap.poll();
      ans.add(0, polled.getKey()); //adding at start so that highest value will come first
    }

    return ans;
  }

  // 3. Sample Data
  public static void main(String[] args) {
    List<Transaction> transactions = Arrays.asList(
        new Transaction("Amazon", 150),
        new Transaction("Netflix", 15),
        new Transaction("Amazon", 50),
        new Transaction("Uber", 30),
        new Transaction("Uber", 40),
        new Transaction("Whole Foods", 120),
        new Transaction("Amazon", 20)
    );

    int k = 2;
    List<String> result = getTopKMerchants(transactions, k);

    // Expected Output: [Amazon, Whole Foods]
    // (Because Amazon = 220, Whole Foods = 120, Uber = 70, Netflix = 15)
    System.out.println("Top " + k + " Merchants: " + result);
  }
}