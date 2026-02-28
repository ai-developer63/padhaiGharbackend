package app.nepaliapp.padhaighar.service;

public interface NotificationService {
    void saveUserToken(Long userId, String token);
    String sendNotificationToUser(Long userId, String title, String body);
	void sendToAllUsers(String title, String body);
	String sendBroadcast(String title, String body);
}