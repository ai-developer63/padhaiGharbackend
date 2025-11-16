package app.nepaliapp.padhaighar.cache;

import com.github.benmanes.caffeine.cache.*;
import java.util.concurrent.TimeUnit;
import java.util.function.ToLongFunction;

public class TimedCache<K, V> {

    private final Cache<K, V> cache;
    private final boolean memoryBound;
    @SuppressWarnings("unused")
	private final long maxBytes; // only for memory-bound cache
    private final ToLongFunction<V> sizeCalculator;

    // Item-count based constructor
    public TimedCache(long maxItems) {
        this.cache = Caffeine.newBuilder()
                .maximumSize(maxItems)
                .build();
        this.memoryBound = false;
        this.maxBytes = 0;
        this.sizeCalculator = null;
    }

    // Memory-bound constructor
    public TimedCache(long maxBytes, ToLongFunction<V> sizeCalculator) {
        this.cache = Caffeine.newBuilder()
                .maximumWeight(maxBytes)
                .weigher((K key, V value) -> (int) sizeCalculator.applyAsLong(value))
                .build();
        this.memoryBound = true;
        this.maxBytes = maxBytes;
        this.sizeCalculator = sizeCalculator;
    }

    // Optional: TTL cache
    public TimedCache(long maxItems, long ttl, TimeUnit unit) {
        this.cache = Caffeine.newBuilder()
                .maximumSize(maxItems)
                .expireAfterWrite(ttl, unit)
                .build();
        this.memoryBound = false;
        this.maxBytes = 0;
        this.sizeCalculator = null;
    }

    // Put or update
    public void put(K key, V value) {
        cache.put(key, value);
    }

    // Get value
    public V get(K key) {
        return cache.getIfPresent(key);
    }

    // Remove key
    public void remove(K key) {
        cache.invalidate(key);
    }

    // Clear cache
    public void clear() {
        cache.invalidateAll();
    }

    // Check if key exists
    public boolean containsKey(K key) {
        return cache.getIfPresent(key) != null;
    }

    // Approximate entry count
    public long size() {
        return cache.estimatedSize();
    }

    // Approximate memory usage (only for memory-bound cache)
    public long memoryUsed() {
        if (!memoryBound || sizeCalculator == null) return -1;
        return cache.asMap().values().stream()
                .mapToLong(sizeCalculator)
                .sum();
    }
}