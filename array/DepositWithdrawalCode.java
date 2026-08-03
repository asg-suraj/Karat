package com.dcb.array;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

enum TransactionType {
  DEPOSIT,
  WITHDRAWAL
}

class Account {

  int accountId;
  String ownerName;

  Account(int accountId, String ownerName) {
    this.accountId = accountId;
    this.ownerName = ownerName;
  }
}

class Transaction {

  int transactionId;
  int accountId;
  TransactionType type;
  double amount;     // Always positive in inputs
  long timestampSec; // Unix-style seconds

  Transaction(int transactionId, int accountId, TransactionType type, double amount,
      long timestampSec) {
    this.transactionId = transactionId;
    this.accountId = accountId;
    this.type = type;
    this.amount = amount;
    this.timestampSec = timestampSec;
  }
}

class AccountManager {

  Map<Integer, Account> accounts = new HashMap<>();
  List<Transaction> transactions = new ArrayList<>();
  double currentBalance = 0.0; // Tracked balance state

  void addAccount(Account account) {
    accounts.put(account.accountId, account);
  }

  void addTransaction(Transaction tx) {
    transactions.add(tx);
    // Bug introduced: Updating a single shared state property during addTransaction
    if (tx.type == TransactionType.DEPOSIT) {
      currentBalance += tx.amount;
    } else if (tx.type == TransactionType.WITHDRAWAL) {
      currentBalance -= tx.amount;
    }
  }

  // TASK 1: Fix the bug in getBalance
  // Hint: Observe what happens when querying balance for different accountIds!
  double getBalance(int accountId) {
    double ans = 0.0;
    for (Transaction t : transactions) {
      if (t.accountId == accountId && t.type == TransactionType.DEPOSIT) {
        ans += t.amount;
      }
      if (t.accountId == accountId && t.type == TransactionType.WITHDRAWAL) {
        ans -= t.amount;
      }
    }
    return ans;
  }

  // TASK 2: Implement this method to satisfy testGetAverageTransactionAmountByAccount
  Map<Integer, Double> getAverageTransactionAmountByAccount() {
    // TODO: Implement this method
    Map<Integer, Double> averageTrans = new HashMap<>();
    //for(Map.Entry<Integer , Account> acc : accounts.entrySet()){
    //Calculate for Each Account ID

    // Not optimzed Solution
//      for(Integer accId  : accounts.keySet()){
//        double sum = 0.0;
//        int count=0;
//        for(Transaction t : transactions){
//          if(t.accountId == accId){
//            sum+=t.amount; //We do not need to check Transaction Type here
//            count++;
//          }
//        }
//        if(count!=0)
//          averageTrans.put(accId , (sum/count));
//
//
//      }
    //}

//    Java 8 options
//    return transactions.stream()
//        .collect(Collectors.groupingBy(
//            t -> t.accountId,
//            Collectors.averagingDouble(t -> t.amount)
//        ));
    Map<Integer, Double> sumMap = new HashMap<>();
    Map<Integer, Integer> countMap = new HashMap<>();
    for (Transaction t : transactions) {
      sumMap.merge(t.accountId, t.amount, Double::sum);
      countMap.put(t.accountId, countMap.getOrDefault(t.accountId, 0) + 1);
    }

    for (Integer accId : sumMap.keySet()) {
      averageTrans.put(accId, (sumMap.getOrDefault(accId, 0.0) / countMap.getOrDefault(accId, 1)));
    }

    return averageTrans;
  }
}

public class DepositWithdrawalCode {

  public static void main(String[] args) {
    System.out.println("--- Running Tests ---");
    testGetBalance_basic();
    testGetBalance_multipleAccounts();
    testGetAverageTransactionAmountByAccount();
    System.out.println("\nAll tests passed successfully!");
  }

  private static void assertAlmost(double expected, double actual, double eps) {
    if (Math.abs(expected - actual) > eps) {
      throw new AssertionError("Expected " + expected + " but got " + actual);
    }
  }

  private static void assertTrue(boolean condition, String message) {
    if (!condition) {
      throw new AssertionError(message);
    }
  }

  public static void testGetBalance_basic() {
    System.out.print("Running testGetBalance_basic... ");
    AccountManager mgr = new AccountManager();
    mgr.addAccount(new Account(1, "Alice"));

    mgr.addTransaction(new Transaction(101, 1, TransactionType.DEPOSIT, 100.0, 1000));
    mgr.addTransaction(new Transaction(102, 1, TransactionType.WITHDRAWAL, 30.0, 1010));
    mgr.addTransaction(new Transaction(103, 1, TransactionType.WITHDRAWAL, 20.0, 1020));
    mgr.addTransaction(new Transaction(104, 1, TransactionType.DEPOSIT, 10.0, 1030));

    // Expected balance: 100 - 30 - 20 + 10 = 60
    assertAlmost(60.0, mgr.getBalance(1), 0.0001);

    mgr.addTransaction(new Transaction(105, 1, TransactionType.WITHDRAWAL, 70.0, 1045));
    assertAlmost(-10.0, mgr.getBalance(1), 0.0001);
    System.out.println("PASSED");
  }

  public static void testGetBalance_multipleAccounts() {
    System.out.print("Running testGetBalance_multipleAccounts... ");
    AccountManager mgr = new AccountManager();
    mgr.addAccount(new Account(1, "Alice"));
    mgr.addAccount(new Account(2, "Bob"));

    mgr.addTransaction(new Transaction(201, 1, TransactionType.DEPOSIT, 50.0, 2000));
    mgr.addTransaction(new Transaction(202, 2, TransactionType.DEPOSIT, 80.0, 2005));
    mgr.addTransaction(new Transaction(203, 1, TransactionType.WITHDRAWAL, 10.0, 2010));
    mgr.addTransaction(new Transaction(204, 2, TransactionType.WITHDRAWAL, 5.5, 2015));
    mgr.addTransaction(new Transaction(205, 2, TransactionType.WITHDRAWAL, 14.5, 2020));

    // Account 1: 50 - 10 = 40
    assertAlmost(40.0, mgr.getBalance(1), 0.0001);
    // Account 2: 80 - 5.5 - 14.5 = 60
    assertAlmost(60.0, mgr.getBalance(2), 0.0001);
    System.out.println("PASSED");
  }

  public static void testGetAverageTransactionAmountByAccount() {
    System.out.print("Running testGetAverageTransactionAmountByAccount... ");
    AccountManager mgr = new AccountManager();

    mgr.addAccount(new Account(51, "Alice"));
    mgr.addAccount(new Account(72, "Bob"));
    mgr.addAccount(new Account(93, "Charlie")); // no transactions

    // Account 51: 100, 30, 20, 10 => avg = 160/4 = 40
    mgr.addTransaction(new Transaction(101, 51, TransactionType.DEPOSIT, 100.0, 1000));
    mgr.addTransaction(new Transaction(102, 51, TransactionType.WITHDRAWAL, 30.0, 1010));
    mgr.addTransaction(new Transaction(103, 51, TransactionType.WITHDRAWAL, 20.0, 1020));
    mgr.addTransaction(new Transaction(104, 51, TransactionType.DEPOSIT, 10.0, 1030));

    // Account 72: 80, 5.5, 14.5 => avg = 100/3 = 33.3333...
    mgr.addTransaction(new Transaction(201, 72, TransactionType.DEPOSIT, 80.0, 2005));
    mgr.addTransaction(new Transaction(202, 72, TransactionType.WITHDRAWAL, 5.5, 2015));
    mgr.addTransaction(new Transaction(203, 72, TransactionType.WITHDRAWAL, 14.5, 2020));

    Map<Integer, Double> avg = mgr.getAverageTransactionAmountByAccount();

    assertTrue(avg.containsKey(51), "Account 51 missing in output");
    assertAlmost(40.0, avg.get(51), 0.0001);

    assertTrue(avg.containsKey(72), "Account 72 missing in output");
    assertAlmost(33.3333, avg.get(72), 0.0001);

    // Account 93 has no transactions -> should not be present
    assertTrue(!avg.containsKey(93), "Account 93 should not be present in output");
    System.out.println("PASSED");
  }
}