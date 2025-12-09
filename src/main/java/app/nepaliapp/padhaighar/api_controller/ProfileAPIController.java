package app.nepaliapp.padhaighar.api_controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.nepaliapp.padhaighar.api_model.ProfileModelForAPI;
import app.nepaliapp.padhaighar.config.JwtUtil;
import app.nepaliapp.padhaighar.model.UserModel;
import app.nepaliapp.padhaighar.serviceimp.UserServiceImp;

@RestController
@RequestMapping("/api/")
public class ProfileAPIController {
	@Autowired
	UserServiceImp userServiceImp;
	
	
	@GetMapping("getProfile")
	public ResponseEntity<ProfileModelForAPI> getProfileData(@RequestHeader(value = "Authorization") String token){
		
		
	    token = token.substring(7); 
        String username = JwtUtil.extractUsername(token);

        // ✅ Get User info
        UserModel user = userServiceImp.getUserByPhoneorEmail(username);
        String country = user.getCountry();
	    String name = user.getName();
	    String profileUrl = user.getProfileImage();
	    String email = user.getEmailId();
		
	    
	    String remainingDays = String.valueOf(15);

	    boolean isPurchased = true;
		  ProfileModelForAPI profile = new ProfileModelForAPI(
			        name,    
			        country, 
			        email,
			        isPurchased,        
			        remainingDays,          
			        profileUrl     
			    );
		return ResponseEntity.ok(profile);
	}
	
	


}
