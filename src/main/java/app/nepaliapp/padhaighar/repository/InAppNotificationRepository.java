package app.nepaliapp.padhaighar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import app.nepaliapp.padhaighar.model.InAppNotification;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InAppNotificationRepository extends JpaRepository<InAppNotification, Long> {

    // Fetch all active notifications that target EVERYONE or THIS USER specifically
    @Query("SELECT n FROM InAppNotification n WHERE " +
           "(n.expiresAt IS NULL OR n.expiresAt > :now) AND " +
           "(n.targetType = 'ALL_USERS' OR (n.targetType = 'SPECIFIC_USER' AND n.specificUserId = :userId))")
    List<InAppNotification> findPotentialNotifications(@Param("userId") Long userId, 
            @Param("now") LocalDateTime now);
    
    List<InAppNotification> findAllByOrderByCreatedAtDesc();
}