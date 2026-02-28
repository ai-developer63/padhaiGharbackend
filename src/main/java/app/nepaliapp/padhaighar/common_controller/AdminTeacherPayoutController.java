package app.nepaliapp.padhaighar.common_controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import app.nepaliapp.padhaighar.repository.SalesRecordRepository;
import app.nepaliapp.padhaighar.service.SalesService;
import app.nepaliapp.padhaighar.serviceimp.CommonServiceImp;

@Controller
@RequestMapping("/admin/teacher-payouts")
public class AdminTeacherPayoutController {

    @Autowired
    private SalesRecordRepository salesRecordRepo;

    @Autowired
    private SalesService salesService;

    @Autowired
    private CommonServiceImp commonServiceImp;

    @GetMapping
    public String viewPayouts(Model model) {
        
        // Fetch the grouped data: "Teacher X is owed Y from Z sales"
        List<SalesRecordRepository.TeacherPayoutProjection> payouts = salesRecordRepo.getUnpaidTeacherPayouts();
        
        // Overall System Unpaid Teacher Debt
        Double totalSystemDebt = salesRecordRepo.sumUnpaidTeacherCuts();

        model.addAttribute("payouts", payouts);
        model.addAttribute("totalSystemDebt", totalSystemDebt != null ? totalSystemDebt : 0.0);
        
        commonServiceImp.modelForAuth(model);
        return "admin/admin_teacher_payouts";
    }

    @PostMapping("/settle")
    public String settleDues(@RequestParam("teacherName") String teacherName, RedirectAttributes redirectAttributes) {
        try {
            salesService.settleTeacherDues(teacherName);
            redirectAttributes.addFlashAttribute("succMsg", "Successfully marked all dues for " + teacherName + " as PAID.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to settle: " + e.getMessage());
        }
        return "redirect:/admin/teacher-payouts";
    }
}