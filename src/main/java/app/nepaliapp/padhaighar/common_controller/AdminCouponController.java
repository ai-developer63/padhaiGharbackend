package app.nepaliapp.padhaighar.common_controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import app.nepaliapp.padhaighar.model.CouponModel;
import app.nepaliapp.padhaighar.model.CourseModel;
import app.nepaliapp.padhaighar.repository.CouponRepository;
import app.nepaliapp.padhaighar.service.CourseService;
import app.nepaliapp.padhaighar.serviceimp.CommonServiceImp;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/coupons")
public class AdminCouponController {

    @Autowired
    private CouponRepository couponRepo;

    @Autowired
    private CourseService courseService;

    @Autowired
    private CommonServiceImp commonServiceImp;

    @GetMapping
    public String manageCoupons(Model model) {
        List<CouponModel> coupons = couponRepo.findAll();
        List<CourseModel> courses = courseService.getAll();
        
        model.addAttribute("coupons", coupons);
        model.addAttribute("courses", courses);
        commonServiceImp.modelForAuth(model);
        
        return "admin/admin_coupons";
    }

    @PostMapping("/add")
    public String addCoupon(
            @RequestParam("code") String code,
            @RequestParam("discountPercentage") Double discountPercentage,
            @RequestParam(value = "maxUses", required = false) Integer maxUses,
            @RequestParam(value = "expiryDate", required = false) String expiryDate,
            @RequestParam(value = "specificCourseId", required = false) Long specificCourseId,
            RedirectAttributes redirectAttributes) {
        try {
            CouponModel coupon = new CouponModel();
            coupon.setCode(code.toUpperCase().trim());
            coupon.setDiscountPercentage(discountPercentage);
            coupon.setMaxUses(maxUses != null && maxUses > 0 ? maxUses : null);
            
            if (expiryDate != null && !expiryDate.isEmpty()) {
                coupon.setExpiryDate(LocalDate.parse(expiryDate));
            }
            if (specificCourseId != null && specificCourseId > 0) {
                CourseModel course = courseService.getById(specificCourseId);
                coupon.setSpecificCourseId(course.getId());
                coupon.setSpecificCourseName(course.getName());
            }

            couponRepo.save(coupon);
            redirectAttributes.addFlashAttribute("succMsg", "Coupon " + coupon.getCode() + " created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to create coupon. Ensure code is unique.");
        }
        return "redirect:/admin/coupons";
    }

    @PostMapping("/toggle")
    public String toggleCoupon(@RequestParam("id") Long id) {
        CouponModel coupon = couponRepo.findById(id).orElse(null);
        if (coupon != null) {
            coupon.setIsActive(!coupon.getIsActive());
            couponRepo.save(coupon);
        }
        return "redirect:/admin/coupons";
    }
    
    // --- FOOLPROOF AJAX SEARCH FOR ADMIN ---
    @GetMapping("/search-courses")
    @ResponseBody
    public java.util.List<java.util.Map<String, Object>> searchCoursesAjax(@RequestParam("q") String query) {
        java.util.List<CourseModel> results = courseService.searchForApp(query, null);
        
        return results.stream().map(course -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("subjectId", course.getId());
            map.put("subjectName", course.getName());
            map.put("categoryName", course.getCategoryName() != null ? course.getCategoryName() : "Course");
            return map;
        }).collect(java.util.stream.Collectors.toList());
    }
}