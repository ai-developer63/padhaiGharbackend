package app.nepaliapp.padhaighar.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class CommissionRuleModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long subjectId;

    private String subjectName;

    // Percentages (e.g., 50.0 for 50%)
    @Column(nullable = false)
    private Double appPercentage = 100.0; // Default: App takes all

    @Column(nullable = false)
    private Double teacherPercentage = 0.0;

    @Column(nullable = false)
    private Double affiliatePercentage = 0.0;
}