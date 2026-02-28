package app.nepaliapp.padhaighar.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class CouponModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code; 

    @Column(nullable = false)
    private Double discountPercentage; 

    private Integer maxUses; 
    private Integer currentUses = 0;

    private LocalDate expiryDate; 
    
    private Long specificCourseId;
    private String specificCourseName;

    private Boolean isActive = true;
}