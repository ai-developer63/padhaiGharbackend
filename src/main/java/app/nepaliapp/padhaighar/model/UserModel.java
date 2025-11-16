package app.nepaliapp.padhaighar.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;


@Entity
@Data
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
	
}