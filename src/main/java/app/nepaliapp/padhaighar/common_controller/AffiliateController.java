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
@RequestMapping("/dashboard/aff")
public class AffiliateController {

    @Autowired private UserService userService;
    @Autowired private SalesRecordRepository salesRecordRepo;
    @Autowired private SalesService salesService;
    @Autowired private CommonServiceImp commonServiceImp;
    @Autowired private CourseServiceImp courseServiceImp;

    @GetMapping
    public String affiliateDashboard(
            @RequestParam(name="page",defaultValue = "0") int page,
            @RequestParam(name="size",defaultValue = "10") int size,
            Model model) {
            
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserModel affiliate = userService.getUserByPhoneorEmail(auth.getName());

        if (affiliate == null || affiliate.getSellerType() != SellerType.POSTPAID) {
            return "redirect:/index";
        }

        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("createdAt").descending());
        org.springframework.data.domain.Page<SalesRecordModel> salesPage = salesRecordRepo.findBySellerId(affiliate.getId(), pageable);

        // Calculate Debt & Earnings (Only for non-reversed sales)
        Double totalEarnedRaw = salesRecordRepo.sumSellerCutBySellerId(affiliate.getId());
        double totalEarned = totalEarnedRaw != null ? totalEarnedRaw : 0.0;
        
        // Calculate Unsettled Debt Owed to Admin
        List<SalesRecordModel> unsettledSales = salesRecordRepo.findBySellerIdAndSaleTypeAndIsCreditPendingFalseAndIsAffiliateSettledFalseAndIsReversedFalse(affiliate.getId(), SaleType.AFFILIATOR);
        double totalOwedToAdmin = unsettledSales.stream().mapToDouble(s -> s.getTotalAmountPaid() - s.getSellerCut()).sum();

        long totalActiveSales = salesRecordRepo.countBySellerIdAndIsReversedFalse(affiliate.getId());

        model.addAttribute("affiliate", affiliate);
        model.addAttribute("salesPage", salesPage); 
        model.addAttribute("totalEarned", totalEarned);
        model.addAttribute("totalOwedToAdmin", totalOwedToAdmin);
        model.addAttribute("totalActiveSales", totalActiveSales);
        
        commonServiceImp.modelForAuth(model);
        return "affiliate_dashboard"; 
    }

    @PostMapping("/sell")
    public String issueCreditSale(
            @RequestParam("userEmailOrPhone") String userEmailOrPhone,
            @RequestParam("courseId") Long courseId,
            @RequestParam("days") int days,
            @RequestParam(value = "couponCode", required = false) String couponCode,
            RedirectAttributes redirectAttributes) {
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UserModel seller = userService.getUserByPhoneorEmail(auth.getName());

            if (seller == null || seller.getSellerType() != SellerType.POSTPAID) {
                throw new Exception("Unauthorized action.");
            }

            UserModel buyer = userService.getUserByPhoneorEmail(userEmailOrPhone.trim());
            if (buyer == null) throw new Exception("Student account not found.");

            // SaleType.AFFILIATOR tells SalesServiceImp to mark this as isCreditPending = true automatically!
            salesService.processSale(
                buyer.getId(), courseId, seller.getId(), 
                SaleType.AFFILIATOR, days, 
                seller.getName() + " (Credit Agent)", couponCode
            );

            redirectAttributes.addFlashAttribute("succMsg", "Credit Sale submitted! Waiting for Admin approval to unlock access for " + buyer.getName());

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Sale Failed: " + e.getMessage());
        }
        
        return "redirect:/dashboard/aff";
    }

    // Reuse the foolproof AJAX search for the Postpaid Agent
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