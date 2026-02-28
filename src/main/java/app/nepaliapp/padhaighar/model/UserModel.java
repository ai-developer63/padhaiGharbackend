package app.nepaliapp.padhaighar.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import app.nepaliapp.padhaighar.enums.SellerType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import jakarta.persistence.*;

@Entity
@Data
@Table( indexes = {
	    @Index(name = "idx_user_email", columnList = "emailId", unique = true),
	    @Index(name = "idx_user_phone", columnList = "phoneNumber", unique = true),
	    @Index(name = "idx_user_public_id", columnList = "publicId", unique = true),
	    @Index(name = "idx_user_name", columnList = "name")
	})
public class UserModel {

	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String phoneNumber;
	private String emailId;
	private String password;
	private String deviceID;
	private String profileImage;
	private String role;
	private String lastActive;
	private String otp;
	private String refer;
	private LocalDateTime lastOtpSentTime;
	private String country;
	@Column(unique = true, nullable = false)
    private String publicId;
	@Column(nullable = false)
	Boolean blocked = false;
	@Column(nullable = false)
	private Double walletBalance = 0.0;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SellerType sellerType = SellerType.NONE;
	@CreationTimestamp
	LocalDateTime accountCreationTime;
}