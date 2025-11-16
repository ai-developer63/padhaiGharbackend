package app.nepaliapp.padhaighar.auth_controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import app.nepaliapp.padhaighar.model.UserModel;
import app.nepaliapp.padhaighar.repository.UserRepository;
import app.nepaliapp.padhaighar.serviceimp.CommonServiceImp;
import app.nepaliapp.padhaighar.serviceimp.UserServiceImp;
import jakarta.servlet.http.HttpSession;



@Controller
public class AdminAuthController {

	@Autowired
	UserServiceImp userService;
	@Autowired
	CommonServiceImp commonServiceImp;

	@Autowired
	UserRepository userRepository;

	@Value("${upload_location}")
	String uploadLocation;

	@Autowired
	PasswordEncoder passwordEncoder;
	
//	@Autowired
//	PurchaseRequestRepository purchaseRequestRepository;

	
	@GetMapping("/admin")
	public String adminPage(Model model) {
		model.addAttribute("isAdmin", true);
		 long totalUsers = userRepository.count();
	        long totalNepaliUsers = userRepository.countBycountry("Nepal");
	        long pendingPurchaseRequests = 15;

	      model.addAttribute("totalUsers", totalUsers);
	        model.addAttribute("totalNepaliUsers", totalNepaliUsers);
	        model.addAttribute("pendingPurchaseRequests", pendingPurchaseRequests);
		IsLoggedInProof(model);
		return "admin/dashboard";
	}

	
	@PostMapping("/changePassword")
	public String changePassword(@RequestParam("newPassword") String newPassword, HttpSession session,
			RedirectAttributes redirectAttributes) {

		// Get verified email from session
		String email = (String) session.getAttribute("resetEmail");
		if (email == null) {
			redirectAttributes.addFlashAttribute("errorMsg", "Session expired. Please start forgot password again.");
			return "redirect:/forgetpassword";
		}

		// Find user
		UserModel user = userRepository.findByEmailId(email);
		if (user == null) {
			redirectAttributes.addFlashAttribute("errorMsg", "User not found.");
			return "redirect:/forgetpassword";
		}

		user.setPassword(passwordEncoder.encode(newPassword));
		user.setOtp("00000");

		userRepository.save(user);

		session.removeAttribute("resetEmail");

		redirectAttributes.addFlashAttribute("succMsg", "Password changed successfully. Please login.");
		return "redirect:/signin";
	}

	@PostMapping("/resend-otp")
	@ResponseBody
	public Map<String, Object> resendOtp(HttpSession session) {
		Map<String, Object> response = new HashMap<>();
		String email = (String) session.getAttribute("resetEmail");

		if (email == null) {
			response.put("success", false);
			response.put("message", "Session expired. Please start forgot password.");
			return response;
		}

		UserModel user = userRepository.findByEmailId(email);
		if (user == null) {
			response.put("success", false);
			response.put("message", "User not found.");
			return response;
		}

		LocalDateTime now = LocalDateTime.now();

		if (user.getLastOtpSentTime() != null && Duration.between(user.getLastOtpSentTime(), now).getSeconds() < 60) { // 60
																														// sec
																														// cooldown
			response.put("success", false);
			response.put("message", "Please wait before resending OTP.");
			return response;
		}

		// Generate new OTP and save timestamp
		String otp = user.getOtp();
		user.setLastOtpSentTime(now);
		userRepository.save(user);

		boolean isSent = commonServiceImp.sendEmail(email, "Your OTP for Password Reset (Mobile Repairing App)",
				commonServiceImp.formateTextForOTPSend(user.getName(), otp));

		response.put("success", isSent);
		response.put("message", isSent ? "OTP resent successfully." : "Failed to send OTP.");
		return response;
	}

	@GetMapping("/otp")
	public String otpPageLoader(HttpSession session, RedirectAttributes redirectAttributes) {
		String email = (String) session.getAttribute("resetEmail");

		if (email == null) {
			// 🚫 user typed URL directly, no email in session
			redirectAttributes.addFlashAttribute("errorMsg", "Please start with forgot password.");
			return "redirect:/forgetpassword";
		}

		return "otp";
	}

	@PostMapping("/verify")
	public String verifyOTP(@RequestParam("otp") String otp, Model model, HttpSession session,
			RedirectAttributes redirectAttributes) {

		String email = (String) session.getAttribute("resetEmail");
		UserModel user = userRepository.findByEmailId(email);
		String userOtp = user.getOtp();
		String userTypedOtp = otp;
		if (userOtp.equals(userTypedOtp)) {
			return "newpassword";
		} else {
			redirectAttributes.addFlashAttribute("errorMsg", "Invalid OTP. Please try again.");
			return "redirect:/otp";
		}

	}

	@PostMapping("/forgot")
	public String processForgotPassword(@RequestParam("email") String email, RedirectAttributes redirectAttributes,
			HttpSession session) {

		UserModel user = userRepository.findByEmailId(email);

		if (user == null) {
			redirectAttributes.addFlashAttribute("errorMsg", "No account found with that email.");
			return "redirect:/forgetpassword";
		}
		String otp = commonServiceImp.otpGenerator();
		user.setOtp(otp);
		user.setLastOtpSentTime(LocalDateTime.now());
		userRepository.save(user);
//		Boolean isSent = true;
		Boolean isSent = commonServiceImp.sendEmail(email, "Your OTP for Password Reset (Mobile Repairing App)",
				commonServiceImp.formateTextForOTPSend(user.getName(), otp));
		session.setAttribute("resetEmail", email);

		if (isSent) {
			return "redirect:/otp";
		} else {
			redirectAttributes.addFlashAttribute("errorMsg", "Sorry, email could not be sent.");
			return "redirect:/forgetpassword";
		}
	}

	@GetMapping("/forgetpassword")
	public String forgetPassword(HttpSession session) {
		session.removeAttribute("succMsg");
		session.removeAttribute("errorMsg");
		return "forgetpassword";
	}



	@GetMapping("/signin")
	public String login() {
		commonServiceImp.removeSessionMessage();
		Boolean isLoggedin = commonServiceImp.checkIsloggedin();
		if (isLoggedin) {
			return "redirect:/index";
		}
		return "login";
	}

	@GetMapping("/register")
	public String register() {
		return "signup";
	}

	@GetMapping({"/", "/index"})
	public String index(Model model) {
		Boolean isLoggedIn = commonServiceImp.checkIsloggedin();
		model.addAttribute("isLoggedIn", isLoggedIn);
		return "index";
	}
//
//	@GetMapping("/error")
//    public String handleError() {
//        return "error"; // Redirect to home page
//    }
//	

	@PostMapping("/saveUser")
	public String saveUser(@ModelAttribute UserModel user, @RequestParam("img") MultipartFile file, HttpSession session,
			RedirectAttributes redirectAttributes) {

		String fileName = file.isEmpty() ? "logo.png" : file.getOriginalFilename();
		user.setProfileImage(fileName);
		user.setRefer("none");
		UserModel saveDtls;

		try {

			if (!file.isEmpty()) {

				try {
					String uploadLocations = uploadLocation + "/profiles";
					File saveDir = new File(uploadLocations);

					if (!saveDir.exists()) {
						saveDir.mkdir();
					}

					Path path = Paths.get(saveDir.getAbsolutePath() + File.separator + file.getOriginalFilename());
					Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				} catch (Exception e) {
					session.setAttribute("errorMsg", "Failed to uploadFile");
				}

			}

			saveDtls = userService.saveuser(user);

			if (!ObjectUtils.isEmpty(saveDtls)) {
				session.setAttribute("succMsg", "User registered Successfully");
			} else {
				session.setAttribute("errorMsg", "Registration failed");
			}

		} catch (Exception e) {

			redirectAttributes.addFlashAttribute("errormsg", "Sorry Registration Failed");
			return "redirect:/register";
		}
		return "redirect:/signin";
		

	}

	private Model IsLoggedInProof(Model model) {
		Boolean isLoggedIn = commonServiceImp.checkIsloggedin();
		model.addAttribute("isLoggedIn", isLoggedIn);
		return model;
	}

}