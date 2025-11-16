package app.nepaliapp.padhaighar.common_controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import app.nepaliapp.padhaighar.model.UserModel;
import app.nepaliapp.padhaighar.service.UserService;
import app.nepaliapp.padhaighar.serviceimp.CommonServiceImp;



@Controller
@RequestMapping("/admin/")
public class UserAdminController {
	@Autowired
	CommonServiceImp commonServiceImp;
	@Autowired
	UserService userService;

	 @GetMapping("allusers")
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
	        IsLoggedInProof(model);

	        return "admin/users"; 
	    }
	
	private Model IsLoggedInProof(Model model) {
		Boolean isLoggedIn = commonServiceImp.checkIsloggedin();
		model.addAttribute("isLoggedIn", isLoggedIn);
		model.addAttribute("isAdmin", true);
		return model;
	}
	
}
