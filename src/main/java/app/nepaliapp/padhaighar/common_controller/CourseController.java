package app.nepaliapp.padhaighar.common_controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import app.nepaliapp.padhaighar.model.CategoryModel;
import app.nepaliapp.padhaighar.model.CourseModel;
import app.nepaliapp.padhaighar.model.dto.TeacherModels;
import app.nepaliapp.padhaighar.serviceimp.CategoryServiceImp;
import app.nepaliapp.padhaighar.serviceimp.CommonServiceImp;
import app.nepaliapp.padhaighar.serviceimp.UserServiceImp;


@Controller
@RequestMapping("/admin")
public class CourseController {

    private final CommonServiceImp commonServiceImp;

	@Autowired
	UserServiceImp userServiceImp;
	
	@Autowired
	CategoryServiceImp categoryServiceImp;

    CourseController(CommonServiceImp commonServiceImp) {
        this.commonServiceImp = commonServiceImp;
    }
	
	@GetMapping("/course/add")
	public String addCoursePage(Model model) {

	    List<CategoryModel> categories = categoryServiceImp.getAllCategories();
	    List<TeacherModels> teachers = userServiceImp.getAllTeachers();
    commonServiceImp.modelForAuth(model);
	    model.addAttribute("course", new CourseModel());
	    model.addAttribute("categoryList", categories);
	    model.addAttribute("teacherList", teachers);

	    return "admin/course_add";
	}
	

}
