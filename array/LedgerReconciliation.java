package com.dcb;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LedgerReconciliation {

  static class Entry {

    String id;
    BigDecimal amount;

    Entry(String id, BigDecimal amount) {
      this.id = id;
      this.amount = amount;
    }

    String getId() {
      return id;
    }

    BigDecimal getAmount() {
      return amount;
    }
  }

  public static void reconcile(List<Entry> ledger1, List<Entry> ledger2) {
    System.out.println("--- Reconciliation Report ---");

    // 1. Group/sum entries by ID for both ledgers
    Map<String, BigDecimal> ledger1Map = new HashMap<>();
    for (Entry e : ledger1) {
      ledger1Map.merge(e.getId(), e.getAmount(), BigDecimal::add);
    }

    Map<String, BigDecimal> ledger2Map = new HashMap<>();
    for (Entry e : ledger2) {
      ledger2Map.merge(e.getId(), e.getAmount(), BigDecimal::add);
    }

    // 2. Identify missing entries in Ledger 1 (Exists in 2 but not 1)
    for (Map.Entry<String, BigDecimal> kv : ledger2Map.entrySet()) {
      if (!ledger1Map.containsKey(kv.getKey())) {
        System.out.println("Missing in Ledger1: ID " + kv.getKey() + ", Amount " + kv.getValue());
      }
    }

    // 3. Identify missing entries in Ledger 2 (Exists in 1 but not 2)
    for (Map.Entry<String, BigDecimal> kv : ledger1Map.entrySet()) {
      if (!ledger2Map.containsKey(kv.getKey())) {
        System.out.println("Missing in Ledger2: ID " + kv.getKey() + ", Amount " + kv.getValue());
      }
    }

    // 4. Identify amount deltas for matching IDs
    for (Map.Entry<String, BigDecimal> kv : ledger1Map.entrySet()) {
      if (ledger2Map.containsKey(kv.getKey())) {
        BigDecimal val1 = kv.getValue();
        BigDecimal val2 = ledger2Map.get(kv.getKey());

        // compareTo safely ignores scale (e.g. 100.0 == 100.00)
        if (val1.compareTo(val2) != 0) {
          System.out.println("Delta: ID " + kv.getKey() +
              ", Ledger1=" + val1 +
              ", Ledger2=" + val2 +
              ", Diff=" + val1.subtract(val2));
        }
      }
    }
  }

  public static void main(String[] args) {
    List<Entry> l1 = Arrays.asList(
        new Entry("A", new BigDecimal("100.00")),
        new Entry("B", new BigDecimal("200.00"))
    );

    List<Entry> l2 = Arrays.asList(
        new Entry("A", new BigDecimal("100.00")),
        new Entry("C", new BigDecimal("300.00")),
        new Entry("B", new BigDecimal("200.01"))
    );

    reconcile(l1, l2);
  }
}