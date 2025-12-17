package app.nepaliapp.padhaighar.service;

import java.time.LocalDate;
import java.util.List;

import app.nepaliapp.padhaighar.api_model.UsageStatDTO;
import app.nepaliapp.padhaighar.model.UserDailyUsage;

public interface UsageTrackingService {

    /**
     * Non-blocking call to record bytes used by a user.
     * @param userId The ID of the user streaming the video.
     * @param bytes The size of the chunk sent.
     */
    void track(long userId, long bytes);

    /**
     * Admin task to delete usage records older than 30 days.
     * This method is Async.
     */
    void cleanOldData();
    
    
 // New Reporting Methods
    List<UsageStatDTO> getTopUsers(int limit);
    List<UserDailyUsage> getUsageHistory(Long userId, LocalDate start, LocalDate end);

}