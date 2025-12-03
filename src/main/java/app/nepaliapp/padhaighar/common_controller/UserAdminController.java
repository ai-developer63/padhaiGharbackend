package app.nepaliapp.padhaighar.common_controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import app.nepaliapp.padhaighar.cache.TimedCache;
import app.nepaliapp.padhaighar.cache.TimedCacheManager;
import app.nepaliapp.padhaighar.model.UserModel;
import app.nepaliapp.padhaighar.service.UserService;
import app.nepaliapp.padhaighar.serviceimp.CommonServiceImp;



@Controller
@RequestMapping("/admin")
public class UserAdminController {
	@Autowired
	CommonServiceImp commonServiceImp;
	@Autowired
	UserService userService;
	
	private static final String USER_CACHE = "userCache";

	TimedCacheManager manager = TimedCacheManager.getInstance();
	  TimedCache<String, UserModel> userCache = manager.getOrCreateCache(USER_CACHE, 1000);
	  
	  
	
	
	   // Page for search UI
    @GetMapping("/manage")
    public String manageUserPage(Model model) {
    	commonServiceImp.modelForAuth(model);
        return "admin/user-manage";
    }

    // API: Search user
    @GetMapping("/search")
    @ResponseBody
    public UserModel searchUser(@RequestParam("query") String query) {
    	System.err.println(query);
        return userService.getUserByPhoneorEmail(query);
    }

    // API: Update role
    @PostMapping("/update-role")
    @ResponseBody
    public String updateRole(@RequestParam("id") Long id,
                             @RequestParam("role") String role) {
    	UserModel user = userService.getUserById(id);
    	String role2 = user.getRole();
    	if (role2.equals("ROLE_ADMIN")) {
			return"ooo Admin is immortal";
		}else {
    	 user.setRole(role);
        userService.updateUser(user);
        return "success";
		}
		}
	
	
	
	
	
	

	 @GetMapping("/allusers")
	    public String getUsers(
	            @RequestParam(name="lastActive",required = false) String lastActive,
	            @RequestParam(name="country",required = false) String country,
	            @RequestParam(name="refer",required = false) String refer,
	            @RequestParam(name="page",defaultValue = "0") int page,
	            @RequestParam(name="size",defaultValue = "5") int size,
	            Model model) {

	        Pageable pageable = PageRequest.of(page, size);
	        Page<UserModel> usersPage = userService.getFilteredUsers(lastActive, country, refer, pageable);

	        model.addAttribute("usersPage", usersPage);
	        model.addAttribute("users", usersPage.getContent());

	        model.addAttribute("lastActive", lastActive);
	        model.addAttribute("country", country);
	        model.addAttribute("refer", refer);
	        commonServiceImp.modelForAuth(model);

	        return "admin/users"; 
	    }
	
	
}
