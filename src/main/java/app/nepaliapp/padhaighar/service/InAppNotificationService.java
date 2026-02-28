package app.nepaliapp.padhaighar.service;
import java.util.List;

import app.nepaliapp.padhaighar.enums.InteractionStatus;
import app.nepaliapp.padhaighar.model.InAppNotification;
public interface InAppNotificationService {
	InAppNotification createNotification(InAppNotification notification);
    List<InAppNotification> getActiveNotificationsForUser(Long userId);
    void logInteraction(Long userId, Long notificationId, InteractionStatus status);
	List<InAppNotification> getAllNotifications();
	void deleteNotification(Long id);
}
