package app.nepaliapp.padhaighar.common_controller;


import app.nepaliapp.padhaighar.api_model.UsageStatDTO;
import app.nepaliapp.padhaighar.model.UserDailyUsage;
import app.nepaliapp.padhaighar.service.UsageTrackingService;
import app.nepaliapp.padhaighar.serviceimp.CommonServiceImp;
import app.nepaliapp.padhaighar.serviceimp.UserServiceImp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminUsageController {
    @Autowired
   UsageTrackingService usageService;
    
    @Autowired
    UserServiceImp userServiceImp;
    
    
    @Autowired
    CommonServiceImp commonServiceImp;
    
 
    @GetMapping("/usage")
    public String viewUsageDashboard(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "100") int size,
            Model model
    ) {

        if (startDate == null) startDate = LocalDate.now();
        if (endDate == null) endDate = LocalDate.now();

        Pageable pageable = PageRequest.of(page, size);

        Page<UserDailyUsage> historyPage =
                usageService.getUsagesHistory(userId, startDate, endDate, pageable);

        List<UserDailyUsage> history = historyPage.getContent();

        List<UsageStatDTO> topUsers = usageService.getTopUsers(20);

        // USER NAME MAP (same logic, but smaller dataset now)
        Map<Long, String> userNamesMap = new HashMap<>();
        history.stream()
                .map(UserDailyUsage::getUserId)
                .distinct()
                .forEach(id -> userNamesMap.put(id, userServiceImp.getUserName(id)));

        model.addAttribute("userNamesMap", userNamesMap);
        model.addAttribute("history", history);
        model.addAttribute("topUsers", topUsers);

        // Pagination metadata
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", historyPage.getTotalPages());
        model.addAttribute("totalItems", historyPage.getTotalElements());
        model.addAttribute("pageSize", size);

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("userId", userId);

        model.addAttribute("formatter", new ByteFormatter());
        commonServiceImp.modelForAuth(model);

        return "admin/admin-usage";
    }


    @PostMapping("/usage/cleanup")
    public String triggerCleanup() {
        usageService.cleanOldData();
        return "redirect:/admin/usage?success=cleanup";
    }

    // Simple helper class for Thymeleaf to format bytes nicely
    public static class ByteFormatter {
        public String format(Long bytes) {
            if (bytes == null) return "0 MB";
            double mb = bytes / (1024.0 * 1024.0);
            if (mb > 1024) return String.format("%.2f GB", mb / 1024.0);
            return String.format("%.2f MB", mb);
        }
    }
}