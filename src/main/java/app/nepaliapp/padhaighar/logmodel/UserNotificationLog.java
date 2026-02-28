package app.nepaliapp.padhaighar.logmodel;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"notificationId", "userId"})})
public class UserNotificationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long notificationId;
    private Long userId;

    private boolean seen;
    private boolean clicked;
    private boolean dismissed;

    private LocalDateTime lastInteractionAt;
    
    // To avoid infinite loops, maybe cap the repetition?
    private int timesShown; 
}