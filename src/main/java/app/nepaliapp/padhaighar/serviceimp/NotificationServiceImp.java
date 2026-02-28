package app.nepaliapp.padhaighar.serviceimp;

import lombok.RequiredArgsConstructor;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;

import app.nepaliapp.padhaighar.model.UserDevice;
import app.nepaliapp.padhaighar.repository.UserNotificationDeviceRepository;
import app.nepaliapp.padhaighar.service.NotificationService;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImp implements NotificationService {

    private final UserNotificationDeviceRepository deviceRepository;


    @Override
    public void saveUserToken(Long userId, String token) {
        Optional<UserDevice> existingDevice = deviceRepository.findByUserId(userId);

        if (existingDevice.isPresent()) {
            // User exists, just update the token and timestamp
            UserDevice device = existingDevice.get();
            device.setFcmToken(token);
            device.setLastUpdated(LocalDateTime.now());
            deviceRepository.save(device);
        } else {
            // New User, create new record
            UserDevice newDevice = new UserDevice();
            newDevice.setUserId(userId);
            newDevice.setFcmToken(token);
            newDevice.setLastUpdated(LocalDateTime.now());
            deviceRepository.save(newDevice);
        }
    }

    @Override
    public String sendNotificationToUser(Long userId, String title, String body) {
        Optional<UserDevice> deviceOpt = deviceRepository.findByUserId(userId);
        if (deviceOpt.isEmpty()) return "User not registered.";

        String targetToken = deviceOpt.get().getFcmToken();
        Notification notification = Notification.builder().setTitle(title).setBody(body).build();
        Message message = Message.builder().setToken(targetToken).setNotification(notification).build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            return "Sent: " + response;
        } catch (FirebaseMessagingException e) {
            // DETECT DEAD TOKENS: If user uninstalled app, delete the token from our DB
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED || 
                e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT) {
                deviceRepository.delete(deviceOpt.get());
                return "Token stale, removed user device.";
            }
            return "FCM Error: " + e.getMessagingErrorCode();
        } catch (Exception e) {
            return "Failed: " + e.getMessage();
        }
    }
    
    
    @Override
    public void sendToAllUsers(String title, String body) {
        // 1. Get all tokens from your DB
        List<UserDevice> allDevices = deviceRepository.findAll();
        
        // 2. Extract tokens
        List<String> tokens = allDevices.stream()
                .map(UserDevice::getFcmToken)
                .collect(Collectors.toList());

        if (tokens.isEmpty()) return;

        // 3. Create a Multicast message (Google's way to send to many at once)
        MulticastMessage message = MulticastMessage.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .addAllTokens(tokens)
                .build();

        try {
            @SuppressWarnings("unused")
			BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public String sendBroadcast(String title, String body) {
        // We send ONE message to the TOPIC, not to a Token
        Message message = Message.builder()
                .setTopic("padhaiGhar") 
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();

        try {
            // One call handles 5,000 or 5,000,000 users!
            String response = FirebaseMessaging.getInstance().send(message);
            return "Broadcast sent successfully: " + response;
        } catch (Exception e) {
            return "Broadcast failed: " + e.getMessage();
        }
    }
}