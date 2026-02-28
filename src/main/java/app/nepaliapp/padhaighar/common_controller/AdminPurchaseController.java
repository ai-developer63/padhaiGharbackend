package app.nepaliapp.padhaighar.common_controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;

import app.nepaliapp.padhaighar.model.PurchasedUserModel;
import app.nepaliapp.padhaighar.model.UserModel;
import app.nepaliapp.padhaighar.service.PurchasedService;
import app.nepaliapp.padhaighar.serviceimp.CommonServiceImp;
import app.nepaliapp.padhaighar.serviceimp.CourseServiceImp;
import app.nepaliapp.padhaighar.serviceimp.UserServiceImp;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminPurchaseController {

    @Autowired
    private PurchasedService purchasedService;
    
    @Autowired
    CourseServiceImp courseServiceImp;
    
    @Autowired
    CommonServiceImp commonServiceImp;
    
    @Autowired
     UserServiceImp userServiceImp;
    
    
    
    
    
    
    @PostMapping("/purchases/update")
    public String updatePurchase(
            @RequestParam("userId") Long userId,
            @RequestParam("courseId") Long courseId,
            @RequestParam("days") int days,
            @RequestParam("whoActivated") String whoActivated,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // Basic validation
            if (userId == null || courseId == null || days <= 0) {
                redirectAttributes.addFlashAttribute(
                    "errorMsg", "Invalid input data. Please try again."
                );
                return "redirect:/admin/purchases";
            }

            // Check user exists
            UserModel user = userServiceImp.getUserById(userId);
            if (user == null) {
                redirectAttributes.addFlashAttribute(
                    "errorMsg", "User not found."
                );
                return "redirect:/admin/purchases";
            }

            // Check course exists
            var course = courseServiceImp.getById(courseId);
            if (course == null) {
                redirectAttributes.addFlashAttribute(
                    "errorMsg", "Selected subject not found."
                );
                return "redirect:/admin/purchases?keyword=" + user.getEmailId();
            }

            // Build purchase entity
            PurchasedUserModel purchase = new PurchasedUserModel();
            purchase.setUserId(userId);
            purchase.setPurchasedSubjectId(courseId);
            purchase.setPurchasedSubjectName(course.getName());
            purchase.setWhoActivated(whoActivated);

            LocalDate today = LocalDate.now();
            purchase.setPurchaseDate(today.toString());
            purchase.setPurchaseUpto(today.plusDays(days).toString());

            // Persist
            purchasedService.savePurchase(purchase);

            redirectAttributes.addFlashAttribute(
                "succMsg", "Course access granted successfully."
            );

            // Redirect back with same user loaded
            return "redirect:/admin/purchases?keyword=" + user.getEmailId();

        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute(
                "errorMsg", "Failed to update purchase: " + ex.getMessage()
            );
            return "redirect:/admin/purchases";
        }
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    @GetMapping("/purchases")
    public String purchasePage(
            @RequestParam(required = false, name = "keyword") String keyword,
            Model model
    ) {
        UserModel user = null;

        if (keyword != null && !keyword.isBlank()) {
            try {
                user = userServiceImp.getUserByPhoneorEmail(keyword);
            } catch (Exception ex) {
                // IMPORTANT:
                // Do nothing — we intentionally swallow auth-related exceptions
                user = null;
            }
        }

        model.addAttribute("user", user);
        model.addAttribute("keyword", keyword);
commonServiceImp.modelForAuth(model);
        return "admin/admin_days_manager";
    }

    
    
    
    
    
    
    
    
    

    // Update purchase
    @PostMapping("/updatePurchasedTopic")
    public String updatePurchasedTopic(@ModelAttribute PurchasedUserModel purchase,
                                       @RequestParam("Days") int days,
                                       HttpSession session) {
        try {
            purchase.setPurchaseDate(LocalDate.now().toString());
            purchase.setPurchaseUpto(LocalDate.now().plusDays(days).toString());

            purchasedService.savePurchase(purchase);

            session.setAttribute("succMsg", "Purchase updated successfully!");
        } catch (Exception e) {
            session.setAttribute("errorMsg", "Error updating purchase: " + e.getMessage());
        }
        return "redirect:/admin/updatePurchasePage";
    }

    // Search user (dummy for now, you must connect with UserRepository)
    @GetMapping("/searchUser")
    @ResponseBody
    public Object searchUser(@RequestParam String query) {
        // TODO: Replace with actual UserRepository logic
        return new Object() {
            @SuppressWarnings("unused")
			public Long id = 1L;
            @SuppressWarnings("unused")
            public String name = "Demo User";
        };
    }

    // List purchases
    @GetMapping("/listofpurchase/{userId}")
    @ResponseBody
    public List<PurchasedUserModel> listOfPurchase(@PathVariable Long userId) {
        return purchasedService.getPurchasesByUserId(userId);
    }

    // Page
    @GetMapping("/updatePurchasePage")
    public String updatePurchasePage(Model model) {
  
         commonServiceImp.modelForAuth(model);
        return "admin/admin_days_manager"; // Thymeleaf template name
    }
}