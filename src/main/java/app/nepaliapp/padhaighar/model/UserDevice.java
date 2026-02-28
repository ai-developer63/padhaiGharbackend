package app.nepaliapp.padhaighar.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_devices")
@Data
public class UserDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // This links to your main User table (just storing the ID here)
    @Column(nullable = false, unique = true) // One token per user (simplest approach)
    private Long userId;

    @Column(nullable = false, length = 512) // Tokens can be long
    private String fcmToken;

    private LocalDateTime lastUpdated;

}