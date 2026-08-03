//import java.math.BigDecimal;
//
////
////
////### 1. The Missing Entities (Account, Ledger, Outbox)
////
////To satisfy the double-entry ledger and outbox requirements from the hint, we need a few more lightweight entities.
////
////```java
//@Entity
//@Table(name = "accounts")
//public class Account {
//    @Id private UUID id;
//    private BigDecimal balance;
//    // getters/setters...
//}
//
//@Entity
//@Table(name = "outbox_events")
//public class OutboxEvent {
//    @Id @GeneratedValue private UUID id;
//    private String eventType;
//    private String payload; // JSON of the transfer details
//    private boolean processed = false;
//    // getters/setters...
//}
//
//```
//
//### 2. The Repositories (with Row-Level Locking)
//
//To prevent race conditions (e.g., double-spending if two requests hit the server simultaneously), we must lock the account rows at the database level using a `PESSIMISTIC_WRITE` lock (`SELECT ... FOR UPDATE`).
//
//```java
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Lock;
//import org.springframework.data.jpa.repository.Query;
//import jakarta.persistence.LockModeType;
//
//public interface AccountRepository extends JpaRepository<Account, UUID> {
//    // Acquires an exclusive row-level lock in the DB
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @Query("SELECT a FROM Account a WHERE a.id = :id")
//    Optional<Account> findByIdForUpdate(UUID id);
//}
//
//public interface TransferRepository extends JpaRepository<Transfer, UUID> {
//    Optional<Transfer> findByIdempotencyKey(String idempotencyKey);
//}
//
//```
//
//### 3. The Transfer Service Implementation
//
//This is where the magic happens. Everything executes within a single `@Transactional` boundary. If any step fails, the entire block rolls back.
//
//```java
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.dao.DataIntegrityViolationException;
//
//@Service
//public class TransferService {
//
//    private final AccountRepository accountRepo;
//    private final TransferRepository transferRepo;
//    private final OutboxRepository outboxRepo;
//
//    // constructor injection...
//
//    @Transactional
//    public Transfer transfer(UUID from, UUID to, BigDecimal amount, String idempotencyKey) {
//
//        // 1. Idempotency Check (Read)
//        // If the client retries a successful transfer, return the existing result.
//        Optional<Transfer> existingTransfer = transferRepo.findByIdempotencyKey(idempotencyKey);
//        if (existingTransfer.isPresent()) {
//            return existingTransfer.get();
//        }
//
//        // 2. Deadlock Prevention & Row-Level Locking
//        // Always lock rows in a deterministic order (e.g., alphabetical/UUID sort)
//        // so concurrent transfers between the same two accounts don't deadlock.
//        UUID firstLock = from.compareTo(to) < 0 ? from : to;
//        UUID secondLock = from.compareTo(to) < 0 ? to : from;
//
//        accountRepo.findByIdForUpdate(firstLock);
//        accountRepo.findByIdForUpdate(secondLock);
//
//        // Fetch the actual account objects
//        Account fromAccount = accountRepo.findById(from).orElseThrow();
//        Account toAccount = accountRepo.findById(to).orElseThrow();
//
//        // 3. Business Logic Validation
//        if (fromAccount.getBalance().compareTo(amount) < 0) {
//            throw new InsufficientFundsException("Not enough balance");
//        }
//
//        // 4. Update Balances (Double-entry logic)
//        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
//        toAccount.setBalance(toAccount.getBalance().add(amount));
//
//        // 5. Save the Transfer (Idempotency Check - Write)
//        Transfer transfer = new Transfer();
//        transfer.setId(UUID.randomUUID());
//        transfer.setFromAccountId(from);
//        transfer.setToAccountId(to);
//        transfer.setAmount(amount);
//        transfer.setIdempotencyKey(idempotencyKey);
//
//        try {
//            transferRepo.saveAndFlush(transfer);
//        } catch (DataIntegrityViolationException e) {
//            // Handles the edge case where a concurrent retry slipped past the read-check
//            // The DB UNIQUE constraint on idempotency_key rejects it.
//            return transferRepo.findByIdempotencyKey(idempotencyKey).orElseThrow();
//        }
//
//        // 6. Write to Outbox (in the same transaction)
//        OutboxEvent event = new OutboxEvent();
//        event.setEventType("TRANSFER_COMPLETED");
//        event.setPayload("{\"transferId\":\"" + transfer.getId() + "\"}");
//        outboxRepo.save(event);
//
//        return transfer;
//    }
//}
//
//```
//"Persist idempotency key in a table; write ledger + outbox in one DB transaction; emit events from outbox.
//
// with exactly-once effects under retries. It uses:
//
//A strongly consistent database transaction
//A dedicated idempotency table with a UNIQUE constraint on the key
//Row-level locks on accounts to prevent race conditions
//A double-entry ledger and a transfer record uniquely bound to the idempotency key
//
//This makes any retry (client/network/server) return the same result without duplicating money movements."	"*** transfer(from, to, amount, idempotencyKey)  ****
//
////Sample code for minimal entities for reference..
// // Transfer.java
//package com.example.payments.domain;
//
//import jakarta.persistence.*;
//import java.math.BigDecimal;
//import java.time.Instant;
//import java.util.UUID;
//
//@Entity
//@Table(name = ""transfers"", uniqueConstraints = {
//    @UniqueConstraint(name = ""ux_transfers_idempotency"", columnNames = ""idempotency_key"")
//})
//public class Transfer {
//    @Id
//    @Column(columnDefinition = ""uuid"")
//    private UUID id;
//
//    @Column(name = ""from_account_id"", columnDefinition = ""uuid"", nullable = false)
//    private UUID fromAccountId;
//
//    @Column(name = ""to_account_id"", columnDefinition = ""uuid"", nullable = false)
//    private UUID toAccountId;
//
//    @Column(nullable = false, precision = 18, scale = 2)
//    private BigDecimal amount;
//
//    @Column(nullable = false)
//    private String currency;
//
//    @Column(name = ""idempotency_key"", nullable = false)
//    private String idempotencyKey;
//
//    @Column(name = ""created_at"", nullable = false)
//    private Instant createdAt = Instant.now();
//
//    // getters/setters/constructors
//}"
//### Why this fulfills the exact requirements:
//
//1. **Idempotency Key Table:** Your `Transfer` table acts as the idempotency table. The `findByIdempotencyKey` read-check handles standard retries, and the `DataIntegrityViolationException` catch block handles exact-millisecond concurrent retries via your `UNIQUE` constraint.
//2. **Row-Level Locks:** `@Lock(LockModeType.PESSIMISTIC_WRITE)` forces the database to lock the specific account rows. Crucially, we sort the UUIDs before locking to prevent DB deadlocks (e.g., Thread A transferring X to Y, and Thread B transferring Y to X).
//3. **One DB Transaction:** Because of `@Transactional`, the balance updates, the transfer record (idempotency key), and the outbox event all commit to the database at the exact same time. It is impossible for money to move without an event being scheduled.
