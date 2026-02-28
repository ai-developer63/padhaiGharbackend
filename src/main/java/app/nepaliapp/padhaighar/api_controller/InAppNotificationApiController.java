package app.nepaliapp.padhaighar.api_controller;


import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import app.nepaliapp.padhaighar.model.InAppNotification;
import app.nepaliapp.padhaighar.model.UserModel;
import app.nepaliapp.padhaighar.model.dto.NotificationInteractionRequest;
import app.nepaliapp.padhaighar.service.CommonService;
import app.nepaliapp.padhaighar.service.InAppNotificationService;
import app.nepaliapp.padhaighar.serviceimp.UserServiceImp;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class InAppNotificationApiController {

    private final InAppNotificationService notificationService;
    private final CommonService commonService; 
    private final UserServiceImp userServiceImp;

    /**
     * 1. FETCH ENDPOINT
     * Android calls: GET /api/v1/notifications/pending?userId=GK-MK66...
     */
    @GetMapping("/pending")
    public ResponseEntity<List<InAppNotification>> getPendingNotifications(
            @RequestParam("userId") String userId) {
        
        // Convert Public ID (String) -> DB User Object
    	UserModel userByPublicId = userServiceImp.getUserByPublicId(userId);
    	
    	if (userByPublicId == null) {
            return ResponseEntity.badRequest().build();
        }

        // Pass Long ID to Service
        List<InAppNotification> notifications = notificationService.getActiveNotificationsForUser(userByPublicId.getId());

        // Fix Image URLs
        notifications.forEach(n -> {
            if (n.getImagePath() != null && !n.getImagePath().startsWith("http")) {
                String fullUrl = commonService.generateFileUrl("notificationimage", n.getImagePath());
                n.setImagePath(fullUrl); 
            }
        });

        return ResponseEntity.ok(notifications);
    }

    /**
     * 2. LOGGING ENDPOINT
     * Android calls: POST /api/v1/notifications/log
     * Body: { "userId": "GK-MK66...", "notificationId": 5, "status": "CLICKED" }
     */
    @PostMapping("/log")
    public ResponseEntity<?> logInteraction(@RequestBody NotificationInteractionRequest request) {
        
        if (request.getUserId() == null || request.getNotificationId() == null || request.getStatus() == null) {
            return ResponseEntity.badRequest().body("Missing required fields");
        }

        // --- THE FIX: Resolve Public ID to Long ID ---
        UserModel user = userServiceImp.getUserByPublicId(request.getUserId());
        
        if (user == null) {
            return ResponseEntity.badRequest().body("Invalid User Public ID");
        }

        // Now pass the Long ID (user.getId()) to the service
        notificationService.logInteraction(
                user.getId(), 
                request.getNotificationId(),
                request.getStatus()
        );

        return ResponseEntity.ok().build();
    }
}
