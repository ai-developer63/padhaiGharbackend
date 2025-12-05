package app.nepaliapp.padhaighar.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import app.nepaliapp.padhaighar.model.UserModel;
import app.nepaliapp.padhaighar.model.dto.TeacherModels;

public interface UserService {
	public UserModel saveuser(UserModel user) throws Exception;
	public UserModel getUserByPhoneorEmail(String username);
	public Boolean updateUserDeviceId(UserModel user, String deviceID);
	
	public List<TeacherModels> getAllTeachers();
	
	 Page<UserModel> getFilteredUsers(String lastActive, String country, String refer, Pageable pageable);
	 UserModel updateUser(UserModel user);
	 UserModel getUserById(Long id);
	 UserModel getTeacherById(Long id);
}
