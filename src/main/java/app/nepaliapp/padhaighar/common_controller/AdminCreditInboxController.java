package app.nepaliapp.padhaighar.common_controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import app.nepaliapp.padhaighar.model.SalesRecordModel;
import app.nepaliapp.padhaighar.repository.SalesRecordRepository;
import app.nepaliapp.padhaighar.service.SalesService;
import app.nepaliapp.padhaighar.serviceimp.CommonServiceImp;

@Controller
@RequestMapping("/admin/credit-inbox")
public class AdminCreditInboxController {

    @Autowired
    private SalesRecordRepository salesRecordRepo;

    @Autowired
    private SalesService salesService;

    @Autowired
    private CommonServiceImp commonServiceImp;

    @GetMapping
    public String viewPendingCredits(
            @RequestParam(name="page", defaultValue = "0") int page,
            @RequestParam(name="size", defaultValue = "20") int size,
            Model model) {
            
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending()); // Oldest first!
        
        // Fetch ONLY pending credit sales
        Page<SalesRecordModel> pendingPage = salesRecordRepo.findByIsCreditPendingTrue(pageable);
        
        model.addAttribute("pendingPage", pendingPage);
        commonServiceImp.modelForAuth(model);
        
        return "admin/admin_credit_inbox";
    }

    @PostMapping("/accept")
    public String acceptCredit(@RequestParam("salesId") Long salesId, RedirectAttributes redirectAttributes) {
        try {
            // Because your SalesService interface doesn't have acceptCreditSale yet, we cast to the Imp directly for now
            ((app.nepaliapp.padhaighar.serviceimp.SalesServiceImp) salesService).acceptCreditSale(salesId);
            redirectAttributes.addFlashAttribute("succMsg", "Sale Officially Approved! Ledger locked.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed: " + e.getMessage());
        }
        return "redirect:/admin/credit-inbox";
    }

    @PostMapping("/reject")
    public String rejectCredit(@RequestParam("salesId") Long salesId, RedirectAttributes redirectAttributes) {
        try {
            salesService.reverseSale(salesId); // Instantly revoke access!
            redirectAttributes.addFlashAttribute("succMsg", "Credit Sale Rejected. Student access revoked.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed: " + e.getMessage());
        }
        return "redirect:/admin/credit-inbox";
    }
}