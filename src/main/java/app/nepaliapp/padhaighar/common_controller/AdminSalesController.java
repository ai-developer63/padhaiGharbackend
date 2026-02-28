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
@RequestMapping("/admin/sales")
public class AdminSalesController {

    @Autowired
    private SalesRecordRepository salesRecordRepo;

    @Autowired
    private SalesService salesService;

    @Autowired
    private CommonServiceImp commonServiceImp;

    @GetMapping
    public String viewSalesLedger(
            @RequestParam(name="keyword", required = false) String keyword,
            @RequestParam(name="startDate", required = false) String startDateStr,
            @RequestParam(name="endDate", required = false) String endDateStr,
            @RequestParam(name="page", defaultValue = "0") int page,
            @RequestParam(name="size", defaultValue = "20") int size,
            Model model) {
            
        java.time.LocalDateTime start = null;
        java.time.LocalDateTime end = null;
        
        // Safely parse Dates. End date is pushed to 23:59:59 to include the whole day!
        try {
            if (startDateStr != null && !startDateStr.isEmpty()) {
                start = java.time.LocalDate.parse(startDateStr).atStartOfDay();
            }
            if (endDateStr != null && !endDateStr.isEmpty()) {
                end = java.time.LocalDate.parse(endDateStr).atTime(java.time.LocalTime.MAX);
            }
        } catch (Exception e) {} // Ignore bad date formats

        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("createdAt").descending());
        
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;

        // 1. Fetch Paginated Table Data
        org.springframework.data.domain.Page<SalesRecordModel> salesPage = 
            salesRecordRepo.searchAndFilterSales(cleanKeyword, start, end, pageable);
        
        // 2. Fetch Dashboard Analytics
        Double totalVolume = salesRecordRepo.sumFilteredTotalVolume(cleanKeyword, start, end);
        Double appEarning = salesRecordRepo.sumFilteredAppEarnings(cleanKeyword, start, end);
        Long activeSalesCount = salesRecordRepo.countFilteredActiveSales(cleanKeyword, start, end);

        model.addAttribute("salesPage", salesPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("startDate", startDateStr);
        model.addAttribute("endDate", endDateStr);
        
        // Analytics
        model.addAttribute("totalVolume", totalVolume != null ? totalVolume : 0.0);
        model.addAttribute("appEarning", appEarning != null ? appEarning : 0.0);
        model.addAttribute("activeSalesCount", activeSalesCount != null ? activeSalesCount : 0);
        
        commonServiceImp.modelForAuth(model);
        
        return "admin/admin_sales_ledger";
    }
    @PostMapping("/reverse")
    public String reverseSale(@RequestParam("salesId") Long salesId, RedirectAttributes redirectAttributes) {
        try {
            salesService.reverseSale(salesId);
            redirectAttributes.addFlashAttribute("succMsg", "Sale successfully reversed! Access revoked and wallet refunded.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to reverse: " + e.getMessage());
        }
        return "redirect:/admin/sales";
    }
}