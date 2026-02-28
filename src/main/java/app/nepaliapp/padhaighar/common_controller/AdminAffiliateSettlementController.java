package app.nepaliapp.padhaighar.common_controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import app.nepaliapp.padhaighar.repository.SalesRecordRepository;
import app.nepaliapp.padhaighar.service.SalesService;
import app.nepaliapp.padhaighar.serviceimp.CommonServiceImp;
import app.nepaliapp.padhaighar.serviceimp.SalesServiceImp;

@Controller
@RequestMapping("/admin/affiliate-settlements")
public class AdminAffiliateSettlementController {

    @Autowired private SalesRecordRepository salesRecordRepo;
    @Autowired private SalesService salesService;
    @Autowired private CommonServiceImp commonServiceImp;

    @GetMapping
    public String viewReceivables(Model model) {
        // Fetch grouped totals: "Agent X owes Y amount to Admin"
        List<SalesRecordRepository.AffiliateReceivableProjection> receivables = salesRecordRepo.getUnsettledAffiliateReceivables();
        Double totalSystemReceivable = salesRecordRepo.sumTotalAffiliateReceivables();

        model.addAttribute("receivables", receivables);
        model.addAttribute("totalSystemReceivable", totalSystemReceivable != null ? totalSystemReceivable : 0.0);
        commonServiceImp.modelForAuth(model);
        return "admin/admin_affiliate_settlements";
    }

    @PostMapping("/collect")
    public String collectPayment(@RequestParam("sellerId") Long sellerId, RedirectAttributes redirectAttributes) {
        try {
            // We use the settleAffiliateDues method we added to SalesServiceImp earlier
            ((SalesServiceImp) salesService).settleAffiliateDues(sellerId);
            redirectAttributes.addFlashAttribute("succMsg", "Cash collection recorded! The Agent's debt has been cleared.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed: " + e.getMessage());
        }
        return "redirect:/admin/affiliate-settlements";
    }
}