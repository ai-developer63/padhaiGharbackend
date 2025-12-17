package app.nepaliapp.padhaighar.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "user_daily_usage", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"userId", "usageDate"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor 
public class UserDailyUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId; // Stores ID as Long

    @Column(nullable = false)
    private LocalDate usageDate;

    private Long totalBytes;

    // Custom Constructor for easier creation
    public UserDailyUsage(Long userId, LocalDate usageDate, Long totalBytes) {
        this.userId = userId;
        this.usageDate = usageDate;
        this.totalBytes = totalBytes;
    }
}