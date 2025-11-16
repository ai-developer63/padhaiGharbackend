package app.nepaliapp.padhaighar.api_model;

import lombok.Data;

@Data
public class LoginModelAPI {
	private String emailOrPhone;
	private String password;
	private String deviceId;
}