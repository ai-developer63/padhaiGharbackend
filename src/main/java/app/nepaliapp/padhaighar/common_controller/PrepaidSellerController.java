package app.nepaliapp.padhaighar.common_controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import app.nepaliapp.padhaighar.enums.SaleType;
import app.nepaliapp.padhaighar.enums.SellerType;
import app.nepaliapp.padhaighar.model.CourseModel;
import app.nepaliapp.padhaighar.model.SalesRecordModel;
import app.nepaliapp.padhaighar.model.UserModel;
import app.nepaliapp.padhaighar.repository.SalesRecordRepository;
import app.nepaliapp.padhaighar.service.SalesService;
import app.nepaliapp.padhaighar.service.UserService;
import app.nepaliapp.padhaighar.serviceimp.CommonServiceImp;
import app.nepaliapp.padhaighar.serviceimp.CourseServiceImp;

@Controller
@RequestMapping("/prepaid")
public class PrepaidSellerController {

    @Autowired
    private UserService userService;

    @Autowired
    private SalesRecordRepository salesRecordRepo;

    @Autowired
    private SalesService salesService;

    @Autowired
    private CommonServiceImp commonServiceImp;
    
    @Autowired
    CourseServiceImp courseServiceImp;

    @GetMapping("/dashboard")
    public String prepaidDashboard(
            @RequestParam(name="page",defaultValue = "0") int page,
            @RequestParam(name="size",defaultValue = "10") int size,
            Model model) {
            
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserModel seller = userService.getUserByPhoneorEmail(auth.getName());

        if (seller == null || seller.getSellerType() != SellerType.PREPAID) {
            return "redirect:/index";
        }

        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("createdAt").descending());
            
        org.springframework.data.domain.Page<SalesRecordModel> salesPage = salesRecordRepo.findBySellerId(seller.getId(), pageable);

        model.addAttribute("seller", seller);
        model.addAttribute("salesPage", salesPage); 
        commonServiceImp.modelForAuth(model);

        return "prepaid_dashboard";
    }

    @PostMapping("/sell")
    public String makeSale(
            @RequestParam("userEmailOrPhone") String userEmailOrPhone,
            @RequestParam("courseId") Long courseId,
            @RequestParam("days") int days,
            @RequestParam(value = "couponCode", required = false) String couponCode,
            RedirectAttributes redirectAttributes) {
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UserModel seller = userService.getUserByPhoneorEmail(auth.getName());

            if (seller == null || seller.getSellerType() != SellerType.PREPAID) {
                throw new Exception("Unauthorized action.");
            }

            UserModel buyer = userService.getUserByPhoneorEmail(userEmailOrPhone.trim());
            if (buyer == null) {
                throw new Exception("Student not found.");
            }

            salesService.processSale(
                buyer.getId(), 
                courseId, 
                seller.getId(), 
                SaleType.PREPAID_SELLER, 
                days, 
                seller.getName() + " (Prepaid Partner)",
                couponCode
            );

            redirectAttributes.addFlashAttribute("succMsg", "Success! Subject assigned to " + buyer.getName() + ". Wallet deducted.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Sale Failed: " + e.getMessage());
        }
        
        return "redirect:/prepaid/dashboard";
    }
    
    // --- FOOLPROOF AJAX SEARCH FOR PREPAID SELLER ---
    @GetMapping("/search-courses")
    @ResponseBody
    public List<java.util.Map<String, Object>> searchCoursesAjax(@RequestParam("q") String query) {
        List<CourseModel> results = courseServiceImp.searchForApp(query, null);
        
        return results.stream().map(course -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("subjectId", course.getId());
            map.put("subjectName", course.getName());
            map.put("categoryName", course.getCategoryName() != null ? course.getCategoryName() : "Course");
            return map;
        }).collect(java.util.stream.Collectors.toList());
    }
}