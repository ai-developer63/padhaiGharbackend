package app.nepaliapp.padhaighar.common_controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import app.nepaliapp.padhaighar.model.CategoryModel;
import app.nepaliapp.padhaighar.model.CourseModel;
import app.nepaliapp.padhaighar.model.UserModel;
import app.nepaliapp.padhaighar.model.dto.TeacherModels;
import app.nepaliapp.padhaighar.serviceimp.CategoryServiceImp;
import app.nepaliapp.padhaighar.serviceimp.CommonServiceImp;
import app.nepaliapp.padhaighar.serviceimp.CourseServiceImp;
import app.nepaliapp.padhaighar.serviceimp.UserServiceImp;


@Controller
@RequestMapping("/admin")
public class CourseController {

    private final CommonServiceImp commonServiceImp;

	@Autowired
	UserServiceImp userServiceImp;
	
	@Autowired
	CategoryServiceImp categoryServiceImp;
	
	@Autowired
	CourseServiceImp courseServiceImp;

    CourseController(CommonServiceImp commonServiceImp) {
        this.commonServiceImp = commonServiceImp;
    }
	
	@GetMapping("/course/add")
	public String addCoursePage(Model model) {

	    List<CategoryModel> categories = categoryServiceImp.getAll();
	    List<TeacherModels> teachers = userServiceImp.getAllTeachers();
    commonServiceImp.modelForAuth(model);
	    model.addAttribute("course", new CourseModel());
	    model.addAttribute("categoryList", categories);
	    model.addAttribute("teacherList", teachers);

	    return "admin/course_add";
	}
	
	
	@GetMapping("/course/list")
	public String listCourses(Model model) {
	    List<CourseModel> courses = courseServiceImp.getAll();
	    commonServiceImp.modelForAuth(model);
	    model.addAttribute("courses", courses);
	    return "admin/course_list";
	}

	@PostMapping("/course/save")
	public String saveCourse(@ModelAttribute("course") CourseModel course, RedirectAttributes ra) {

	    // Auto fill Category + Teacher names
	    CategoryModel cat = categoryServiceImp.getById(course.getCategoryId());
	    UserModel teacher = userServiceImp.getTeacherById(course.getTeacherId());

	    if (cat != null) course.setCategoryName(cat.getName());
	    if (teacher != null) course.setTeacherName(teacher.getName());

	    if (course.getId() == null) {
	        courseServiceImp.save(course);
	        ra.addFlashAttribute("msg", "Course Added Successfully!");
	    } else {
	        courseServiceImp.update(course);
	        ra.addFlashAttribute("msg", "Course Updated Successfully!");
	    }

	    return "redirect:/admin/course/list";
	}

	@GetMapping("/course/edit")
	public String editCourse(@RequestParam Long id, Model model) {
	    CourseModel course = courseServiceImp.getById(id);

	    model.addAttribute("course", course);
	    model.addAttribute("categoryList", categoryServiceImp.getAll());
	    model.addAttribute("teacherList", userServiceImp.getAllTeachers());
	    commonServiceImp.modelForAuth(model);

	    return "admin/course_add";
	}

	@GetMapping("/course/delete")
	public String deleteCourse(@RequestParam Long id, RedirectAttributes ra) {
		courseServiceImp.delete(id);
	    ra.addFlashAttribute("msg", "Course Deleted Successfully!");

	    return "redirect:/admin/course/list";
	}

	
	

}
