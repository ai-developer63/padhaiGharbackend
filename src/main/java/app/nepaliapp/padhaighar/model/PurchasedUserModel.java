package app.nepaliapp.padhaighar.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Entity
@Data
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "purchasedSubjectId"}))
public class PurchasedUserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userName;
    private Long userId;
    private Long purchasedSubjectId;
    private String purchasedSubjectName;
    private String whoActivated;
    private Long salesId;        // If 0 it is admin No Sales is Recorded
    private Long salesAccountId;
    private String purchaseDate;
    private String purchaseUpto; // corrected camelCase
    private Boolean isLive;      // added to match frontend radio button
}

