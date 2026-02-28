package app.nepaliapp.padhaighar.common_controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import app.nepaliapp.padhaighar.model.CourseModel;
import app.nepaliapp.padhaighar.model.SalesRecordModel;
import app.nepaliapp.padhaighar.model.UserModel;
import app.nepaliapp.padhaighar.repository.SalesRecordRepository;
import app.nepaliapp.padhaighar.service.CourseService;
import app.nepaliapp.padhaighar.service.UserService;
import app.nepaliapp.padhaighar.serviceimp.CommonServiceImp;

@Controller
@RequestMapping("/dashboard/teacher")
public class TeacherDashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private SalesRecordRepository salesRecordRepo;

    @Autowired
    private CommonServiceImp commonServiceImp;

    @GetMapping
    public String teacherDashboard(
            @RequestParam(name="page",defaultValue = "0") int page,
            @RequestParam(name="size",defaultValue = "10") int size,
            Model model) {
            
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserModel teacher = userService.getUserByPhoneorEmail(auth.getName());

        if (teacher == null || !"ROLE_TEACHER".equals(teacher.getRole())) {
            return "redirect:/index";
        }

        // 1. Find teacher's courses and their IDs
        List<CourseModel> myCourses = courseService.getAll().stream()
                .filter(c -> c.getTeacherId() != null && c.getTeacherId().equals(teacher.getId()))
                .collect(Collectors.toList());

        List<Long> courseIds = myCourses.stream()
                .map(CourseModel::getId)
                .collect(Collectors.toList());

        org.springframework.data.domain.Page<SalesRecordModel> salesPage;
        double totalEarned = 0.0;
        long totalStudents = 0;

        // 2. Safely query only if the teacher actually has courses
        if (!courseIds.isEmpty()) {
            org.springframework.data.domain.Pageable pageable = 
                org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("createdAt").descending());
                
            salesPage = salesRecordRepo.findByPurchasedSubjectIdIn(courseIds, pageable);
            
            Double earnedRaw = salesRecordRepo.sumTeacherCutBySubjectIds(courseIds);
            totalEarned = earnedRaw != null ? earnedRaw : 0.0;
            totalStudents = salesRecordRepo.countByPurchasedSubjectIdInAndIsReversedFalse(courseIds);
        } else {
            salesPage = org.springframework.data.domain.Page.empty();
        }

        model.addAttribute("teacher", teacher);
        model.addAttribute("myCourses", myCourses);
        model.addAttribute("salesPage", salesPage); // Paginator variable
        model.addAttribute("totalEarned", totalEarned);
        model.addAttribute("totalStudents", totalStudents);
        
        commonServiceImp.modelForAuth(model);

        return "teacher_dashboard"; 
    }
}