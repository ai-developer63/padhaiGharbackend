package app.nepaliapp.padhaighar.serviceimp;

import app.nepaliapp.padhaighar.api_model.UsageStatDTO;
import app.nepaliapp.padhaighar.model.UserDailyUsage;
import app.nepaliapp.padhaighar.repository.UserUsageRepository;
import app.nepaliapp.padhaighar.service.UsageTrackingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UsageTrackingServiceImp implements UsageTrackingService {

    private static final Logger logger = LoggerFactory.getLogger(UsageTrackingServiceImp.class);
    private final UserUsageRepository repository;

    // Buffer Key Format: "123:2025-01-01" (UserId + Date)
    private final ConcurrentHashMap<String, AtomicLong> usageBuffer = new ConcurrentHashMap<>();

    // Constructor Injection
    public UsageTrackingServiceImp(UserUsageRepository repository) {
        this.repository = repository;
    }
    
    
    @Override
    public List<UsageStatDTO> getTopUsers(int limit) {
        // Fetch top 'limit' users (e.g., Top 20)
        return repository.findTopUsers(PageRequest.of(0, limit));
    }

    @Override
    public List<UserDailyUsage> getUsageHistory(Long userId, LocalDate start, LocalDate end) {
        if (userId != null && userId > 0) {
            return repository.findByUserIdAndUsageDateBetweenOrderByUsageDateDesc(userId, start, end);
        }
        return repository.findByUsageDateBetweenOrderByUsageDateDesc(start, end);
    }

    @Override
    public void track(long userId, long bytes) {
        // If userId is 0 or negative (invalid), ignore
        if (userId <= 0) return;

        // Create a composite key for "User + Today"
        String key = userId + ":" + LocalDate.now();
        
        // Atomic add: Thread-safe and extremely fast
        usageBuffer.computeIfAbsent(key, k -> new AtomicLong(0)).addAndGet(bytes);
    }

    /**
     * Runs every 10 seconds.
     * Drains the memory buffer and updates the database.
     */
    @Scheduled(fixedRate = 10000)
    public void flushBufferToDb() {
        if (usageBuffer.isEmpty()) return;

        // Iterate over the map entries
        for (Map.Entry<String, AtomicLong> entry : usageBuffer.entrySet()) {
            String key = entry.getKey();
            AtomicLong atomicBytes = entry.getValue();
            
            // Atomically get the value and reset it to 0
            long bytes = atomicBytes.getAndSet(0);

            if (bytes > 0) {
                processBatchItem(key, bytes);
            }
            // Optional: You could remove keys here if desired, 
            // but keeping them reduces object creation churn for active users.
        }
    }

    private void processBatchItem(String key, long bytes) {
        try {
            int separatorIndex = key.lastIndexOf(":");
            
            // Extract and Parse ID (String -> Long)
            String userIdString = key.substring(0, separatorIndex);
            long userId = Long.parseLong(userIdString); 
            
            // Extract and Parse Date
            String dateStr = key.substring(separatorIndex + 1);
            LocalDate date = LocalDate.parse(dateStr);

            // 1. Try the fast UPDATE query first
            try {
                repository.incrementUsage(userId, date, bytes);
            } catch (Exception e) {
                // 2. If update fails (likely row doesn't exist), insert new row
                // Note: passing 'long userId' here works fine (autoboxing to Long)
                saveNewRecord(userId, date, bytes);
            }
            
        } catch (Exception e) {
            logger.error("Failed to flush usage for key: {}", key, e);
        }
    }

    /**
     * Synchronized to prevent race conditions during the very first insert of the day.
     * UPDATED: Accepts Long userId instead of String.
     */
    private synchronized void saveNewRecord(Long userId, LocalDate date, long bytes) {
        // Double-check existence inside the synchronized block
        if (repository.findByUserIdAndUsageDate(userId, date).isPresent()) {
            repository.incrementUsage(userId, date, bytes);
        } else {
            repository.save(new UserDailyUsage(userId, date, bytes));
        }
    }

    @Override
    @Async
    @Transactional
    public void cleanOldData() {
        logger.info("Admin Cleanup: Starting purge of data older than 30 days...");
        try {
            LocalDate cutoffDate = LocalDate.now().minusDays(30);
            repository.deleteOlderThan(cutoffDate);
            logger.info("Admin Cleanup: Completed successfully.");
        } catch (Exception e) {
            logger.error("Admin Cleanup: Failed.", e);
        }
    }
}