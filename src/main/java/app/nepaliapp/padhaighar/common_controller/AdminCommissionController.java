package app.nepaliapp.padhaighar.common_controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import app.nepaliapp.padhaighar.model.CommissionRuleModel;
import app.nepaliapp.padhaighar.model.CourseModel;
import app.nepaliapp.padhaighar.repository.CommissionRuleRepository;
import app.nepaliapp.padhaighar.service.CourseService;
import app.nepaliapp.padhaighar.serviceimp.CommonServiceImp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/commissions")
public class AdminCommissionController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private CommissionRuleRepository commissionRuleRepo;

    @Autowired
    private CommonServiceImp commonServiceImp;

    @GetMapping
    public String viewCommissions(
            @RequestParam(name="keyword", required=false) String keyword,
            @RequestParam(name="page", defaultValue="0") int page,
            @RequestParam(name="size", defaultValue="10") int size,
            Model model) {

        // 1. Fetch paginated courses (Handles search internally if keyword is provided)
        Page<CourseModel> coursePage = courseService.searchForAdmin(keyword, null, page, size);

        // 2. Extract IDs and fetch ONLY the rules for the courses on this page
        List<Long> courseIds = coursePage.getContent().stream().map(CourseModel::getId).collect(Collectors.toList());
        List<CommissionRuleModel> rules = courseIds.isEmpty() ? new ArrayList<>() : commissionRuleRepo.findBySubjectIdIn(courseIds);

        Map<Long, CommissionRuleModel> rulesMap = rules.stream()
                .collect(Collectors.toMap(CommissionRuleModel::getSubjectId, r -> r));

        model.addAttribute("coursePage", coursePage); // Pass the Page object for pagination
        model.addAttribute("rulesMap", rulesMap);
        model.addAttribute("keyword", keyword);       // Pass keyword to preserve it in UI
        commonServiceImp.modelForAuth(model);
        
        return "admin/admin_commissions";
    }

    @PostMapping("/update")
    public String updateCommission(
            @RequestParam("subjectId") Long subjectId,
            @RequestParam("subjectName") String subjectName,
            @RequestParam("appPercentage") Double appPercentage,
            @RequestParam("teacherPercentage") Double teacherPercentage,
            @RequestParam("affiliatePercentage") Double affiliatePercentage,
            RedirectAttributes redirectAttributes
    ) {
        try {
            if ((appPercentage + teacherPercentage + affiliatePercentage) != 100.0) {
                redirectAttributes.addFlashAttribute("errorMsg", "Percentages must add up to exactly 100%.");
                return "redirect:/admin/commissions";
            }

            CommissionRuleModel rule = commissionRuleRepo.findBySubjectId(subjectId).orElse(new CommissionRuleModel());
            rule.setSubjectId(subjectId);
            rule.setSubjectName(subjectName);
            rule.setAppPercentage(appPercentage);
            rule.setTeacherPercentage(teacherPercentage);
            rule.setAffiliatePercentage(affiliatePercentage);

            commissionRuleRepo.save(rule);

            redirectAttributes.addFlashAttribute("succMsg", "Commission updated for " + subjectName);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to update: " + e.getMessage());
        }
        return "redirect:/admin/commissions";
    }
}