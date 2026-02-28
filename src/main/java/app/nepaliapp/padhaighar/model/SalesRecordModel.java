package app.nepaliapp.padhaighar.model;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import app.nepaliapp.padhaighar.enums.SaleType;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class SalesRecordModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String transactionId;

    private Long purchasedSubjectId;
    private String subjectName;

    private Long buyerId; 
    private String buyerName;

    private Long sellerId; 
    private String sellerName; 

    @Enumerated(EnumType.STRING)
    private SaleType saleType;

    private Double totalAmountPaid;
    private Double appCut;
    private Double teacherCut;
    private Double sellerCut;

    @Column(nullable = false)
    private Boolean isReversed = false;
    
    @Column(nullable = false)
    private Boolean isCreditPending = false; 

    // --- NEW: TEACHER PAYOUT TRACKING ---
    private String teacherName; 
    
    @Column(nullable = false)
    private Boolean isTeacherPaid = false; 
    
    @Column(nullable = false)
    private Boolean isAffiliateSettled = false;
    private LocalDateTime teacherPaidDate;
    // ------------------------------------

    @CreationTimestamp
    private LocalDateTime createdAt;
}