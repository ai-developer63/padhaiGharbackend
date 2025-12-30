package app.nepaliapp.padhaighar.common_controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;

import app.nepaliapp.padhaighar.model.PurchasedUserModel;
import app.nepaliapp.padhaighar.service.PurchasedService;
import app.nepaliapp.padhaighar.serviceimp.CommonServiceImp;
import app.nepaliapp.padhaighar.serviceimp.CourseServiceImp;

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
            public Long id = 1L;
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