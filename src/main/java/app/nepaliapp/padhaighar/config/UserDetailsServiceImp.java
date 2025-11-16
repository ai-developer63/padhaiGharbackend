package app.nepaliapp.padhaighar.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import app.nepaliapp.padhaighar.model.UserModel;
import app.nepaliapp.padhaighar.repository.UserRepository;



@Service
public class UserDetailsServiceImp implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
	    UserModel user;
	    if (isEmail(username)) {
	        System.out.println("Looking up by email: " + username);
	        user = userRepository.findByEmailId(username);
	    } else {
	        System.out.println("Looking up by phone number: " + username);
	        user = userRepository.findByPhoneNumber(username);
	    }

	    if (user == null) {
	        throw new UsernameNotFoundException("User not found");
	    }

	    System.out.println("User found: " + user.toString()); // Will print the user details
	    return new CustomUser(user);
	}

	private boolean isEmail(String username) {
		return username != null && username.contains("@") && username.contains(".");
	}

}
