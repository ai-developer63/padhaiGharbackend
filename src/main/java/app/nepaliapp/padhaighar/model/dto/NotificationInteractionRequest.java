package app.nepaliapp.padhaighar.model.dto;

import app.nepaliapp.padhaighar.enums.InteractionStatus;
import lombok.Data;

@Data
public class NotificationInteractionRequest {
    private String userId;
    private Long notificationId;
    private InteractionStatus status; // SEEN, CLICKED, or DISMISSED
}