package app.nepaliapp.padhaighar.model;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import app.nepaliapp.padhaighar.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class WalletTransactionModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId; // The Prepaid Seller's ID

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType; // CREDIT or DEBIT

    private String description; // e.g., "Recharged by Admin", "Course Sale Deduction"

    @CreationTimestamp
    private LocalDateTime createdAt;
}