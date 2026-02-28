package app.nepaliapp.padhaighar.logrepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.nepaliapp.padhaighar.logmodel.UserNotificationLog;

import java.util.Optional;

@Repository
public interface UserNotificationLogRepository extends JpaRepository<UserNotificationLog, Long> {
    Optional<UserNotificationLog> findByNotificationIdAndUserId(Long notificationId, Long userId);
}
