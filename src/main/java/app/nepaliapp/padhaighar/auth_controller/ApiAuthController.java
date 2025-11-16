package app.nepaliapp.padhaighar.auth_controller;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import app.nepaliapp.padhaighar.api_model.ForgetPasswordModelApi;
import app.nepaliapp.padhaighar.api_model.LoginModelAPI;
import app.nepaliapp.padhaighar.config.JwtUtil;
import app.nepaliapp.padhaighar.config.UserDetailsServiceImp;
import app.nepaliapp.padhaighar.model.UserModel;
import app.nepaliapp.padhaighar.repository.UserRepository;
import app.nepaliapp.padhaighar.serviceimp.CommonServiceImp;
import app.nepaliapp.padhaighar.serviceimp.UserServiceImp;


@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {

	@Autowired
	UserServiceImp userService;
	@Autowired
	CommonServiceImp commonServiceImp;

	@Autowired
	UserRepository userRepository;

	@Autowired
	PasswordEncoder passwordEncoder;
	
	@Autowired
	AuthenticationManager authenticationManager;
	
	@Autowired
	private UserDetailsServiceImp userDetailsServiceImp;
	
	
	
	@PostMapping("/setpassword")
	public ResponseEntity<Map<String,String>> setPassword(@RequestBody ForgetPasswordModelApi forgetpass){

		try {
					UserModel user = userRepository.findByEmailId(forgetpass.getEmail());

			        if (user == null) {
			            return ResponseEntity.badRequest()
			                    .body(Map.of("error", "No user found with the given email"));
			        }
			        if (!forgetpass.getOtp().equals(user.getOtp())) {
			            return ResponseEntity.badRequest()
			                    .body(Map.of("error", "Wrong OTP. Check it carefully"));
			        }

			        if (forgetpass.getNewpassword() == null || forgetpass.getNewpassword().isEmpty()) {
			            return ResponseEntity.badRequest()
			                    .body(Map.of("error", "Password cannot be empty"));
			        }

			        if (forgetpass.getNewpassword().length() < 6) {
			            return ResponseEntity.badRequest()
			                    .body(Map.of("error", "Password must be at least 6 characters"));
			        }

			   
			        user.setPassword(passwordEncoder.encode(forgetpass.getNewpassword()));
			        user.setOtp("00000"); // reset OTP
			        userRepository.save(user);
			        return ResponseEntity.ok(Map.of("message", "Password reset successful"));      
			        
		} catch (Exception e) {
	        return ResponseEntity.badRequest()
	                .body(Map.of("error", e.getMessage()));
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	@PostMapping("/verifyotp")
	public ResponseEntity<Map<String, String>> verifyOtpRequest(@RequestBody ForgetPasswordModelApi forgetpass) {
	    try {
	        UserModel user = userRepository.findByEmailId(forgetpass.getEmail());

	        if (user == null) {
	            return ResponseEntity.badRequest()
	                    .body(Map.of("error", "No user found with the given email"));
	        }

	        if (Objects.equals(forgetpass.getOtp(), user.getOtp())) {
	        
	            return ResponseEntity.ok(Map.of("message", "OTP verified successfully"));
	        } else {
	            return ResponseEntity.badRequest()
	                    .body(Map.of("error", "Wrong OTP. Check it carefully"));
	        }
	    } catch (Exception e) {
	        // Optional: log the exception here
	        return ResponseEntity.badRequest()
	                .body(Map.of("error", e.getMessage()));
	    }
	}

	
	
	
	
	
	@PostMapping("/otpRequest")
	public ResponseEntity<Map<String, String>> otpRequest(@RequestBody ForgetPasswordModelApi forgetpass) {
	    try {
	        UserModel user = userRepository.findByEmailId(forgetpass.getEmail());

	        if (user == null) {
	            return ResponseEntity.badRequest()
	                    .body(Map.of("error", "No user found with the given email"));
	        }

	        String otp = commonServiceImp.otpGenerator();
	        user.setOtp(otp);
	        user.setLastOtpSentTime(LocalDateTime.now());
	        userRepository.save(user);

	        boolean isSent = commonServiceImp.sendEmail(
	                user.getEmailId(),
	                "Your OTP for Password Reset (Mobile Repairing App)",
	                commonServiceImp.formateTextForOTPSend(user.getName(), otp)
	        );

	        if (isSent) {
	            return ResponseEntity.ok(Map.of("message", "OTP sent successfully"));
	        } else {
	            return ResponseEntity.badRequest()
	                    .body(Map.of("error", "SMTP Server seems busy. Try again later."));
	        }
	    } catch (Exception e) {
	        // Optional: log the exception
	        return ResponseEntity.badRequest()
	                .body(Map.of("error", e.getMessage()));
	    }
	}

	
	
	
	
	
	
	
	
	
	
	
	
	@PostMapping("/signup")
	public ResponseEntity<Map<String,String>> registerUser(@RequestBody UserModel userModel){
		
		try {
			userModel.setProfileImage("default.png");
			userModel.setOtp("00000");
			userModel.setRole("ROLE_USER");
			userModel.setLastOtpSentTime(LocalDateTime.now());
			UserModel createdUser = userService.saveuser(userModel);
			String token = JwtUtil.generateToken(createdUser.getEmailId());
			Map<String, String> response = new HashMap<>();
			response.put("message", "User registered SuccessFully");
			response.put("token", token);
			response.put("who", "universe");
		
			return ResponseEntity.ok(response);
			
		} catch (Exception e) {
			Map<String, String> errorResponse = new HashMap<>();
			errorResponse.put("error", e.getMessage());
			return ResponseEntity.badRequest().body(errorResponse);
		}
		
		
	} 
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@PostMapping("/login")
	public ResponseEntity<?> createJwtToken(@RequestBody LoginModelAPI loginModelAPI){
		
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginModelAPI.getEmailOrPhone(),loginModelAPI.getPassword()));
			UserDetails userDetails = userDetailsServiceImp.loadUserByUsername(loginModelAPI.getEmailOrPhone());
			String token = JwtUtil.generateToken(userDetails.getUsername());
			// Create response containing the JWT token
						Map<String, String> response = new HashMap<>();
						response.put("token", token);
						
						  UserModel userByPhoneorEmail = userService.getUserByPhoneorEmail(loginModelAPI.getEmailOrPhone());
							userByPhoneorEmail.setDeviceID(loginModelAPI.getDeviceId());
							if(userByPhoneorEmail.getRole().equalsIgnoreCase("ROLE_ADMIN")) {
								response.put("who","narayan" );	
							}else {
								response.put("who", "universe");
							}
							
							
							Boolean updateUserDeviceId = userService.updateUserDeviceId(userByPhoneorEmail, loginModelAPI.getDeviceId());
							if(!updateUserDeviceId) {
								throw new RuntimeException("Simulating an unexpected error");
							}
							return ResponseEntity.ok(response);
		}  catch (BadCredentialsException e) {
		
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid username or password",
					"details", Map.of("timestamp", LocalDateTime.now().toString(), "path", "/api/auth/login")));
		} catch (Exception e) {
			// General error handling
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "An unexpected error occurred", "details", e.getMessage()));
		}
		
	}
	
	
	
	
	
	
	
}
