package app.nepaliapp.padhaighar.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import app.nepaliapp.padhaighar.enums.ConditionType;
import app.nepaliapp.padhaighar.enums.Priority;
import app.nepaliapp.padhaighar.enums.TargetType;

@Entity
@Data
public class InAppNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    
    @Column(length = 1000) // Allow longer messages
    private String message;
    
    private String actionUrl; 
    
    // --- NEW FIELDS ---
    private String buttonLabel; // e.g., "Open Now", "Rate Us", "Claim Bonus"
    private String imagePath;   // Stores the filename (e.g., "123_uuid.jpg")
    // ------------------

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    private TargetType targetType;

    private Long specificUserId; 

    private Integer repeatAfterHours; 

    @Enumerated(EnumType.STRING)
    private ConditionType conditionType;
    
    private String conditionValue; 
    
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}