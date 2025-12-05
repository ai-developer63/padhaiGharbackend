package app.nepaliapp.padhaighar.serviceimp;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import org.springframework.util.StringUtils;

import app.nepaliapp.padhaighar.cache.TimedCache;
import app.nepaliapp.padhaighar.cache.TimedCacheManager;
import app.nepaliapp.padhaighar.model.UserModel;
import app.nepaliapp.padhaighar.model.dto.TeacherModels;
import app.nepaliapp.padhaighar.repository.UserRepository;
import app.nepaliapp.padhaighar.service.UserService;

@Service
public class UserServiceImp implements UserService {
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	
	private static final String USER_CACHE = "userCache";

	TimedCacheManager manager = TimedCacheManager.getInstance();
	  TimedCache<String, UserModel> userCache = manager.getOrCreateCache(USER_CACHE, 1000);
	 
	  
	  private static final String TEACHER_CACHE = "teacherCache";
	  TimedCache<String, UserModel> teacherCache = manager.getOrCreateCache(TEACHER_CACHE, 1000);

	  

	  @Override
	  public UserModel getTeacherById(Long id) {

	      // 1. Check teacher cache using key: "teacher:{id}"
	      String key = "teacher:" + id;
	      UserModel cachedTeacher = teacherCache.get(key);
	      if (cachedTeacher != null) {
	          return cachedTeacher;
	      }

	      // 2. Fetch from DB
	      UserModel teacher = userRepository.findById(id)
	              .orElseThrow(() -> new RuntimeException("Teacher not found"));

	      // 3. Validate role
	      if (!"ROLE_TEACHER".equalsIgnoreCase(teacher.getRole())) {
	          throw new RuntimeException("User is not a teacher");
	      }

	      // 4. Cache result
	      teacherCache.put(key, teacher);

	      return teacher;
	  }

	  
	  


		@Override
		public List<TeacherModels> getAllTeachers() {
			List<UserModel> byRole = userRepository.findByRole("ROLE_TEACHER");

		    List<TeacherModels> allteacher = byRole.stream()
		            .map(u -> new TeacherModels(u.getName(), u.getId()))
		            .toList();
		    return allteacher;
		}


		
		
		
	  @Override
	    public Page<UserModel> getFilteredUsers(String lastActive, String country, String refer, Pageable pageable) {
	        List<UserModel> all = userRepository.findAll();

	        List<UserModel> filtered = all.stream()
	                .filter(u -> !StringUtils.hasText(lastActive) ||
	                        (u.getLastActive() != null && u.getLastActive().contains(lastActive)))
	                .filter(u -> !StringUtils.hasText(country) ||
	                        (u.getCountry() != null && u.getCountry().equalsIgnoreCase(country)))
	                .filter(u -> !StringUtils.hasText(refer) ||
	                        (u.getRefer() != null && u.getRefer().equalsIgnoreCase(refer)))
	                .toList();

	        int start = (int) pageable.getOffset();
	        int end = Math.min(start + pageable.getPageSize(), filtered.size());

	        List<UserModel> subList = filtered.subList(start, end);
	        return new PageImpl<>(subList, pageable, filtered.size());
	    }	  
	  
	  
	  
	  
	  
	  

	@Override
	@Transactional
	public UserModel saveuser(UserModel user) throws Exception {
		if (userRepository.existsByEmailId(user.getEmailId())) {
			throw new Exception("Email already Used");
		}
		if (userRepository.existsByPhoneNumber(user.getPhoneNumber())) {
			throw new Exception("Phone Number already Used");
		}
		user.setRole("ROLE_USER");
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setOtp("0000");
		UserModel save = userRepository.save(user);

		return save;
	}


	@Override
	public UserModel getUserByPhoneorEmail(String username) {
		 UserModel cachedUser = userCache.get(username);
		    if (cachedUser != null) {
		        return cachedUser;
		    }

		 // Not in cache, fetch from DB
		    UserModel user = null;
		    if (isEmail(username)) {
		        user = userRepository.findByEmailId(username);
		    }
		    if (user == null) {
		        user = userRepository.findByPhoneNumber(username);
		    }
		    
		    if (user == null) {
		        throw new UsernameNotFoundException("User not found");
		    }
		    userCache.put(username, user);

	    return user;
	}
	
	private boolean isEmail(String username) {
		return username != null && username.matches("^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$");
	}


	 @Override
	    public Boolean updateUserDeviceId(UserModel user, String deviceID) {
	        int updatedRows = userRepository.updateDeviceIdByUserId(user.getId(), deviceID);

	        if (updatedRows > 0) {
	            // Fetch updated user from DB
	            UserModel updatedUser = userRepository.findById(user.getId()).get();
	            refreshUserCache(updatedUser);
	        }

	        return updatedRows > 0;
	    }
	
	
	 @Transactional
	 @Override
	 public UserModel updateUser(UserModel user) {
	     UserModel updated = userRepository.save(user);

	     // update cache: email and phone both may be used as keys
	     if (user.getEmailId() != null) {
	         userCache.put(user.getEmailId(), updated);
	     }
	     if (user.getPhoneNumber() != null) {
	         userCache.put(user.getPhoneNumber(), updated);
	     }

	     return updated;
	 }

	 
	 @Override
	 public UserModel getUserById(Long id) {
	     return userRepository.findById(id)
	             .orElseThrow(() -> new RuntimeException("User not found"));
	 }



    // ----------------------------
    //  CLEAN CACHE HELPERS
    // ----------------------------
    private void refreshUserCache(UserModel user) {
        if (user.getEmailId() != null) {
            userCache.remove(user.getEmailId());
            userCache.put(user.getEmailId(), user);
        }

        if (user.getPhoneNumber() != null) {
            userCache.remove(user.getPhoneNumber());
            userCache.put(user.getPhoneNumber(), user);
        }
    }





}
