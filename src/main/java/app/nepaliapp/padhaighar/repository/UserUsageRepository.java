package app.nepaliapp.padhaighar.repository;


import app.nepaliapp.padhaighar.api_model.UsageStatDTO;
import app.nepaliapp.padhaighar.model.UserDailyUsage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserUsageRepository extends JpaRepository<UserDailyUsage, Long> {

    // 1. Existing methods
    Optional<UserDailyUsage> findByUserIdAndUsageDate(Long userId, LocalDate date);

    @Transactional
    @Modifying
    @Query("""
    UPDATE UserDailyUsage u
    SET u.totalBytes = u.totalBytes + :bytes
    WHERE u.userId = :userId AND u.usageDate = :date
    """)
    int incrementUsage(
        @Param("userId") Long userId,
        @Param("date") LocalDate date,
        @Param("bytes") long bytes
    );


    @Transactional
    @Modifying
    @Query("DELETE FROM UserDailyUsage u WHERE u.usageDate < :cutoffDate")
    void deleteOlderThan(@Param("cutoffDate") LocalDate cutoffDate);

    // --- NEW METHODS FOR DASHBOARD ---

    // 2. Filter logs by Date Range
    List<UserDailyUsage> findByUsageDateBetweenOrderByUsageDateDesc(LocalDate startDate, LocalDate endDate);

    // 3. Filter logs by Date Range AND Specific User
    List<UserDailyUsage> findByUserIdAndUsageDateBetweenOrderByUsageDateDesc(Long userId, LocalDate startDate, LocalDate endDate);

 // ✅ FIXED QUERY PATH: api_model instead of dto
    @Query("SELECT new app.nepaliapp.padhaighar.api_model.UsageStatDTO(u.userId, SUM(u.totalBytes)) " +
           "FROM UserDailyUsage u " +
           "GROUP BY u.userId " +
           "ORDER BY SUM(u.totalBytes) DESC")
    List<UsageStatDTO> findTopUsers(Pageable pageable);
    

    Page<UserDailyUsage> findByUserIdAndUsageDateBetweenOrderByUsageDateDesc(
            Long userId,
            LocalDate start,
            LocalDate end,
            Pageable pageable
    );

    Page<UserDailyUsage> findByUsageDateBetweenOrderByUsageDateDesc(
            LocalDate start,
            LocalDate end,
            Pageable pageable
    );
    
    
    
}