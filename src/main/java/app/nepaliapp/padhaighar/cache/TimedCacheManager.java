package app.nepaliapp.padhaighar.cache;

import java.util.concurrent.*;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TimedCacheManager {

    private static final TimedCacheManager INSTANCE = new TimedCacheManager();
    private final ConcurrentHashMap<String, TimedCache<?, ?>> cacheRegistry = new ConcurrentHashMap<>();

    private TimedCacheManager() {}

    public static TimedCacheManager getInstance() {
        return INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public <K, V> TimedCache<K, V> getOrCreateTimedCache(String cacheName, long maxItems, long ttl, TimeUnit unit) {
        return (TimedCache<K, V>) cacheRegistry.computeIfAbsent(cacheName,
                name -> new TimedCache<>(maxItems, ttl, unit));
    }
    
    
    // Create or retrieve item-count cache
    @SuppressWarnings("unchecked")
    public <K, V> TimedCache<K, V> getOrCreateCache(String cacheName, long maxItems) {
        return (TimedCache<K, V>) cacheRegistry.computeIfAbsent(cacheName,
                name -> new TimedCache<>(maxItems));
    }

    // Create or retrieve memory-bound cache
    @SuppressWarnings("unchecked")
    public <K, V> TimedCache<K, V> getOrCreateMemoryCache(String cacheName, long maxBytes, java.util.function.ToLongFunction<V> sizeCalculator) {
        return (TimedCache<K, V>) cacheRegistry.computeIfAbsent(cacheName,
                name -> new TimedCache<>(maxBytes, sizeCalculator));
    }

    // Remove key from cache
    @SuppressWarnings("unchecked")
    public <K> void removeEntryFromCache(String cacheName, K key) {
        TimedCache<K, ?> cache = (TimedCache<K, ?>) cacheRegistry.get(cacheName);
        if (cache != null) cache.remove(key);
    }

    // Clear specific cache
    public void clearCache(String cacheName) {
        TimedCache<?, ?> cache = cacheRegistry.get(cacheName);
        if (cache != null) cache.clear();
    }

    // Clear all caches
    public void clearAllCaches() {
        cacheRegistry.values().forEach(TimedCache::clear);
    }

    // Check if key exists in cache
    @SuppressWarnings("unchecked")
    public <K> boolean containsData(String cacheName, K key) {
        TimedCache<K, ?> cache = (TimedCache<K, ?>) cacheRegistry.get(cacheName);
        return cache != null && cache.containsKey(key);
    }

    // Get number of entries in specific cache
    public long getCacheSize(String cacheName) {
        TimedCache<?, ?> cache = cacheRegistry.get(cacheName);
        return cache != null ? cache.size() : 0;
    }

    // Get memory usage of specific cache (only for memory-bound caches)
    public long getCacheMemoryUsed(String cacheName) {
        TimedCache<?, ?> cache = cacheRegistry.get(cacheName);
        if (cache != null) return cache.memoryUsed();
        return -1;
    }

    // Get overview of all caches
    public Map<String, Map<String, Long>> getAllCacheStats() {
        Map<String, Map<String, Long>> stats = new HashMap<>();
        for (Map.Entry<String, TimedCache<?, ?>> entry : cacheRegistry.entrySet()) {
            TimedCache<?, ?> cache = entry.getValue();
            Map<String, Long> cacheStats = new HashMap<>();
            cacheStats.put("entries", cache.size());
            long mem = cache.memoryUsed();
            cacheStats.put("memoryBytes", mem >= 0 ? mem : 0);
            stats.put(entry.getKey(), cacheStats);
        }
        return stats;
    }
}
