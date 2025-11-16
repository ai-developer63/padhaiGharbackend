package app.nepaliapp.padhaighar.api_model;

import lombok.Data;

@Data
public class ForgetPasswordModelApi {
	String email;
	String otp;
	String newpassword;
	
}