package app.nepaliapp.padhaighar.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import app.nepaliapp.padhaighar.model.UserModel;

public interface UserService {
	public UserModel saveuser(UserModel user) throws Exception;
	public UserModel getUserByPhoneorEmail(String username);
	public Boolean updateUserDeviceId(UserModel user, String deviceID);
	
	 Page<UserModel> getFilteredUsers(String lastActive, String country, String refer, Pageable pageable);
}
