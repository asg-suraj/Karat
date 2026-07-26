import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BoundedBlockingQueue<T> {
    
    // The underlying array to hold the elements (circular buffer)
    private final Object[] items;
    
    // Pointers and state
    private int putIndex = 0;
    private int takeIndex = 0;
    private int count = 0;

    // Concurrency controls
    private final Lock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();

    public BoundedBlockingQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }
        this.items = new Object[capacity];
    }

    /**
     * Inserts the specified element into this queue, waiting if necessary
     * for space to become available.
     */
    public void put(T item) throws InterruptedException {
        // lockInterruptibly allows the thread to be interrupted while waiting for the lock
        lock.lockInterruptibly();
        try {
            // ALWAYS use a while loop with await() to guard against spurious wakeups
            while (count == items.length) {
                notFull.await(); // Release lock and wait until signalled that space is available
            }
            
            // Enqueue the item
            items[putIndex] = item;
            
            // Circular wrap-around
            if (++putIndex == items.length) {
                putIndex = 0;
            }
            count++;
            
            // Signal a waiting take() thread that there is now an item to consume
            notEmpty.signal();
            
        } finally {
            // ALWAYS unlock in a finally block to prevent deadlocks if an exception is thrown
            lock.unlock();
        }
    }

    /**
     * Retrieves and removes the head of this queue, waiting if necessary
     * until an element becomes available.
     */
    @SuppressWarnings("unchecked")
    public T take() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while (count == 0) {
                notEmpty.await(); // Release lock and wait until signalled that an item was put
            }
            
            // Dequeue the item
            T item = (T) items[takeIndex];
            items[takeIndex] = null; // Null out the reference to help the Garbage Collector
            
            // Circular wrap-around
            if (++takeIndex == items.length) {
                takeIndex = 0;
            }
            count--;
            
            // Signal a waiting put() thread that space is now available
            notFull.signal();
            
            return item;
            
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the number of elements currently in the queue.
     */
    public int size() {
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
    }
}
