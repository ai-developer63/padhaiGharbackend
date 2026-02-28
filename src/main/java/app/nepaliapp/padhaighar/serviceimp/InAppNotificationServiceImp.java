package app.nepaliapp.padhaighar.serviceimp;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.nepaliapp.padhaighar.cache.TimedCache;
import app.nepaliapp.padhaighar.cache.TimedCacheManager;
import app.nepaliapp.padhaighar.enums.ConditionType;
import app.nepaliapp.padhaighar.enums.InteractionStatus;
import app.nepaliapp.padhaighar.enums.Priority;
import app.nepaliapp.padhaighar.logmodel.UserNotificationLog;
import app.nepaliapp.padhaighar.logrepository.UserNotificationLogRepository;
import app.nepaliapp.padhaighar.model.InAppNotification;
import app.nepaliapp.padhaighar.repository.InAppNotificationRepository;
import app.nepaliapp.padhaighar.service.InAppNotificationService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InAppNotificationServiceImp implements InAppNotificationService {

    private final InAppNotificationRepository notificationRepository;
    private final UserNotificationLogRepository logRepository;
    private final TimedCacheManager cacheManager;

    private static final String CACHE_NAME = "USER_NOTIF_CACHE";
    // Cache up to 5000 users' lists, expire after 50 minutes to ensure freshness
    private static final long MAX_CACHE_SIZE = 5000;
    private static final long CACHE_TTL_MINUTES = 50;
    @Override
    @Transactional(readOnly = true)
    public List<InAppNotification> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
        // CRITICAL: Clear cache so deleted notifications are removed for all users
        cacheManager.clearCache(CACHE_NAME);
    }
    @Override
    public InAppNotification createNotification(InAppNotification notification) {
        notification.setCreatedAt(LocalDateTime.now());
        InAppNotification saved = notificationRepository.save(notification);
        
        // CRITICAL: New notification arrived! Clear cache so everyone sees it immediately.
        cacheManager.clearCache(CACHE_NAME);
        
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InAppNotification> getActiveNotificationsForUser(Long userId) {
        // 1. Get the Cache Wrapper
        TimedCache<Long, List<InAppNotification>> cache = cacheManager.getOrCreateTimedCache(
                CACHE_NAME, 
                MAX_CACHE_SIZE, 
                CACHE_TTL_MINUTES, 
                TimeUnit.MINUTES
        );

        // 2. HIT: Check if we have a valid list in RAM
        if (cache.containsKey(userId)) {
            return cache.get(userId);
        }

        // 3. MISS: Calculate from Database (The "Heavy" Logic)
        List<InAppNotification> calculatedList = calculateNotificationsFromDb(userId);

        // 4. Store in Cache for next time
        cache.put(userId, calculatedList);

        return calculatedList;
    }

    /**
     * Extracted the heavy logic to a private method for cleanliness.
     * This only runs if Cache Miss occurs.
     */
    private List<InAppNotification> calculateNotificationsFromDb(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<InAppNotification> candidates = notificationRepository.findPotentialNotifications(userId, now);
        List<InAppNotification> finalToDisplay = new ArrayList<>();

        for (InAppNotification notif : candidates) {
            // Check Logic Conditions
            if (!isConditionMet(notif, userId)) { 
                continue; 
            }

            // Check History (Logs)
            Optional<UserNotificationLog> logOpt = logRepository.findByNotificationIdAndUserId(notif.getId(), userId);

            if (logOpt.isEmpty()) {
                // Never seen -> Show
                finalToDisplay.add(notif);
            } else {
                UserNotificationLog log = logOpt.get();

                if (log.isClicked()) continue; 

                // Repetition Logic
                if (log.isDismissed()) {
                    if (notif.getPriority() == Priority.HIGH && notif.getRepeatAfterHours() != null) {
                        long hoursSinceDismissal = ChronoUnit.HOURS.between(log.getLastInteractionAt(), now);
                        if (hoursSinceDismissal >= notif.getRepeatAfterHours()) {
                            finalToDisplay.add(notif); 
                        }
                    }
                } else if (!log.isSeen()) {
                    finalToDisplay.add(notif);
                }
            }
        }
        return finalToDisplay;
    }

    @SuppressWarnings("incomplete-switch")
    @Override
    @Transactional
    public void logInteraction(Long userId, Long notificationId, InteractionStatus status) {
        UserNotificationLog log = logRepository.findByNotificationIdAndUserId(notificationId, userId)
                .orElse(new UserNotificationLog());

        log.setUserId(userId);
        log.setNotificationId(notificationId);
        log.setLastInteractionAt(LocalDateTime.now());

        switch (status) {
            case SEEN -> {
                log.setSeen(true);
                log.setTimesShown(log.getTimesShown() + 1);
            }
            case CLICKED -> log.setClicked(true);
            case DISMISSED -> log.setDismissed(true);
        }

        logRepository.save(log);
        
        // CRITICAL: User state changed (e.g., they clicked/dismissed). 
        // We MUST remove them from cache so the next fetch re-calculates 
        // and hides the notification they just interacted with.
        cacheManager.removeEntryFromCache(CACHE_NAME, userId);
    }

    // --- Helper for Conditions ---
    private boolean isConditionMet(InAppNotification notif, Long userId) {
        if (notif.getConditionType() == ConditionType.NONE) return true;
        
        // Actual User logic here if needed
        return true; 
    }
}
