package app.nepaliapp.padhaighar.api_controller;



import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import app.nepaliapp.padhaighar.model.UserModel;
import app.nepaliapp.padhaighar.service.NotificationService;
import app.nepaliapp.padhaighar.serviceimp.CommonServiceImp;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class ApiNotificationController {

    private final NotificationService notificationService;
    private final CommonServiceImp commonServiceImp;


  
    @PostMapping("/update-token")
    public ResponseEntity<String> updateToken(
            @RequestBody Map<String, Object> payload, 
            @RequestHeader("Authorization") String authHeader) { 
        try {
            // 1. Get the User using the Authorization Header
            UserModel userByToken = commonServiceImp.getUserByToken(authHeader);
            
            if (userByToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid User Session");
            }

            // 2. Extract the FCM token from the request body
            String fcmToken = (String) payload.get("fcmToken");

            if (fcmToken == null || fcmToken.isEmpty()) {
                return ResponseEntity.badRequest().body("FCM Token is missing");
            }

            // 3. Save the token using the User ID from the database
            notificationService.saveUserToken(userByToken.getId(), fcmToken); 
            
            return ResponseEntity.ok("Token registered successfully");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

   
    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(
            @RequestParam("userId") Long userId,
            @RequestParam("title") String title,
            @RequestParam("body") String body) {

        String result = notificationService.sendNotificationToUser(userId, title, body);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/broadcast")
    public ResponseEntity<String> sendBroadcast(
            @RequestParam("title") String title,
            @RequestParam("body") String body) {
        
        // This calls the Topic-based logic in your Service
        String result = notificationService.sendBroadcast(title, body);
        return ResponseEntity.ok(result);
    }

}
