package com.dcb.tree;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * Thread-safe, high-QPS LRU cache using: - ConcurrentHashMap for data (lock-free reads) - Striped
 * segments, each with a small access-ordered
 * LinkedHashMap for LRU metadata - Per-segment
 * ReentrantLock; get() uses tryLock() to avoid contended blocking
 * <p>
 * Characteristics: - "Approximate LRU" under contention
 * (touch may be skipped if tryLock fails) -
 * O(1) expected for get/put -
 * Eviction bounded per segment; total capacity ~ sum over segments
 */
public class StripedLRUCache<K, V> {

  private final ConcurrentHashMap<K, V> data;
  private final Segment<K>[] segments;
  private final int segmentMask;
  private final int segmentShift;

  // Optional metrics
  private final LongAdder hitCount = new LongAdder();
  private final LongAdder missCount = new LongAdder();
  private final LongAdder evictionCount = new LongAdder();

  @SuppressWarnings("unchecked")

  public StripedLRUCache(int capacity, int concurrencyLevel) {
    if (capacity <= 0) {
      throw new IllegalArgumentException("capacity must be > 0");
    }
    if (concurrencyLevel <= 0) {
      concurrencyLevel = 16;
    }

    int segCount = tableSizeFor(concurrencyLevel);   // power-of-two
    int perSegCap = Math.max(1, capacity / segCount);
    this.segments = (Segment<K>[]) new Segment<?>[segCount];

    // Spread/shift for fast segment selection
    this.segmentMask = segCount - 1;
    this.segmentShift = 32 - Integer.numberOfTrailingZeros(segCount);

    for (int i = 0; i < segCount; i++) {
      segments[i] = new Segment<>(perSegCap);
    }

    // CHM with a slightly larger initial capacity to reduce resizing
    this.data = new ConcurrentHashMap<>(tableSizeFor(capacity * 2));
  }

  /**
   * Fast spreader like ConcurrentHashMap
   */
  private static int spread(int h) {
    h ^= (h >>> 16);
    return h;
  }

  private int segmentIndex(Object key) {
    int h = spread(key.hashCode());
    // cheap: upper bits already mixed; use mask
    return (h >>> (segmentShift))
        & segmentMask; // Or simply h & segmentMask; both are fine with power-of-two
  }

  /**
   * Get value; best-effort LRU touch using tryLock()
   */
  public V get(K key) {
    V v = data.get(key);
    if (v == null) {
      missCount.increment();
      return null;
    }
    hitCount.increment();
    segments[segmentIndex(key)].onAccessBestEffort(key);
    return v;
  }

  /**
   * Put value; updates LRU and evicts if needed (segment-local)
   */
  public V put(K key, V value) {
    V prev = data.put(key, value);
    segments[segmentIndex(key)].onWriteAndEvict(key, data, evictionCount);
    return prev;
  }

  /**
   * Fast-path computeIfAbsent. - Read CHM first (no lock) - If miss, compute value once (callable
   * responsibility) and putIfAbsent - Update LRU & evict
   */
  public V computeIfAbsent(K key, Function<K, V> mappingFunction) {
    V v = data.get(key);
    if (v != null) {
      hitCount.increment();
      segments[segmentIndex(key)].onAccessBestEffort(key);
      return v;
    }
    missCount.increment();
    V newV = mappingFunction.apply(key);
    V existing = data.putIfAbsent(key, newV);
    V result = (existing != null) ? existing : newV;
    // Ensure we still update LRU on either path
    segments[segmentIndex(key)].onWriteAndEvict(key, data, evictionCount);
    return result;
  }

  /**
   * Remove key if present.
   */
  public V remove(K key) {
    V prev = data.remove(key);
    Segment<K> seg = segments[segmentIndex(key)];
    seg.removeFromLRU(key);
    return prev;
  }

  /**
   * Current size (approx O(1) from CHM)
   */
  public int size() {
    return data.size();
  }

  /**
   * Wipes all entries.
   */
  public void clear() {
    data.clear();
    for (Segment<K> s : segments) {
      s.clearLRU();
    }
  }

  /**
   * Optional metrics
   */
  public long hits() {
    return hitCount.sum();
  }

  public long misses() {
    return missCount.sum();
  }

  public long evictions() {
    return evictionCount.sum();
  }

  /**
   * Segment holds only LRU metadata and a small lock; values live in CHM.
   */
  private static final class Segment<K> {

    private final ReentrantLock lock = new ReentrantLock();
    // accessOrder=true -> recency updated on get/put
    // We store only keys (Boolean.TRUE used as a placeholder)
    private final LinkedHashMap<K, Boolean> lru;
    private final int maxEntries;

    Segment(int maxEntries) {
      this.maxEntries = maxEntries;
      this.lru = new LinkedHashMap<>(Math.max(16, tableSizeFor(maxEntries)),
          0.75f, true);
    }

    // Record access without blocking if contended. This is the throughput trick.
    void onAccessBestEffort(K key) {
      if (lock.tryLock()) {
        try {
          // touch recency
          lru.put(key, Boolean.TRUE);
        } finally {
          lock.unlock();
        }
      }
      // If we couldn't acquire lock, skip touching: approximate LRU
    }

    // On write, we must ensure recency recorded and evictions done:
    void onWriteAndEvict(K key, ConcurrentHashMap<K, ?> data, LongAdder evictionCounter) {
      lock.lock();
      try {
        lru.put(key, Boolean.TRUE);
        // Evict within this segment while over capacity
        while (lru.size() > maxEntries) {
          Iterator<Map.Entry<K, Boolean>> it = lru.entrySet().iterator();
          if (!it.hasNext()) {
            break;
          }
          K eldest = it.next().getKey();
          it.remove(); // remove from LRU
          // try remove from global data map (may already be gone)
          if (data.remove(eldest) != null) {
            evictionCounter.increment();
          }
        }
      } finally {
        lock.unlock();
      }
    }

    void removeFromLRU(K key) {
      lock.lock();
      try {
        lru.remove(key);
      } finally {
        lock.unlock();
      }
    }

    void clearLRU() {
      lock.lock();
      try {
        lru.clear();
      } finally {
        lock.unlock();
      }
    }
  }

  /**
   * Round up to a power of two.
   */
  private static int tableSizeFor(int cap) {
    int n = cap - 1;
    n |= n >>> 1;
    n |= n >>> 2;
    n |= n >>> 4;
    n |= n >>> 8;
    n |= n >>> 16;
    return (n < 1) ? 1 : (n >= (1 << 30)) ? (1 << 30) : n + 1;
  }

  // --- Simple demo ---
  public static void main(String[] args) throws InterruptedException {
    StripedLRUCache<Integer, String> cache = new StripedLRUCache<>(1_000, 32);

    // Warmup
    for (int i = 0; i < 2000; i++) {
      cache.put(i, "v" + i);
    }

    // Concurrent hits/misses
    int threads = 32;
    ExecutorService es = Executors.newFixedThreadPool(threads);
    CountDownLatch start = new CountDownLatch(1);
    CountDownLatch done = new CountDownLatch(threads);

    for (int t = 0; t < threads; t++) {
      es.submit(() -> {
        try {
          start.await();
          ThreadLocalRandom rnd = ThreadLocalRandom.current();
          for (int i = 0; i < 200_000; i++) {
            int k = rnd.nextInt(0, 5000);
            String v = cache.get(k);
            if (v == null) {
              cache.put(k, "v" + k);
            }
          }
        } catch (InterruptedException ignored) {
        } finally {
          done.countDown();
        }
      });
    }

    long ts = System.nanoTime();
    start.countDown();
    done.await();
    long durMs = (System.nanoTime() - ts) / 1_000_000;

    es.shutdown();

    System.out.println("Size:" + cache.size());
    System.out.println(
        "Hits:" + cache.hits() + " Misses: " + cache.misses() + " Evictions: " + cache.evictions());
    System.out.println("Elapsed:" + durMs + " ms ");
  }
}
