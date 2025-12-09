package app.nepaliapp.padhaighar.common_controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import app.nepaliapp.padhaighar.model.CategoryModel;
import app.nepaliapp.padhaighar.model.CourseModel;
import app.nepaliapp.padhaighar.model.CourseVideoModel;
import app.nepaliapp.padhaighar.model.CoursesArrangeModel;
import app.nepaliapp.padhaighar.model.UserModel;
import app.nepaliapp.padhaighar.model.dto.TeacherModels;
import app.nepaliapp.padhaighar.repository.CoursesArrangeRepository;
import app.nepaliapp.padhaighar.serviceimp.CategoryServiceImp;
import app.nepaliapp.padhaighar.serviceimp.CommonServiceImp;
import app.nepaliapp.padhaighar.serviceimp.CourseServiceImp;
import app.nepaliapp.padhaighar.serviceimp.UserServiceImp;

@Controller
@RequestMapping("/admin")
public class CourseController {

	@Autowired
	CommonServiceImp commonServiceImp;

	@Autowired
	UserServiceImp userServiceImp;

	@Autowired
	CategoryServiceImp categoryServiceImp;

	@Autowired
	CourseServiceImp courseServiceImp;

	@Autowired
	CoursesArrangeRepository arrangeRepository;

	// Courses Video Part Controller

	// Video Required

	@GetMapping("/courses/videos/{courseId}")
	public String videoJustme(Model model, @PathVariable("courseId") Long courseId,
			RedirectAttributes redirectAttributes) {
		commonServiceImp.modelForAuth(model);
		CourseModel course = courseServiceImp.getById(courseId);

		if (course == null) {
			// Handle case where course ID is invalid or deleted
			redirectAttributes.addFlashAttribute("errorMsg", "Course with ID " + courseId + " not found.");
			return "redirect:/admin/course/add"; // Redirect to a safe page
		}

		model.addAttribute("course", course);

		CourseVideoModel courseVideoModel = new CourseVideoModel();
		model.addAttribute("newVideo", courseVideoModel);
		List<CourseVideoModel> allVideos = courseServiceImp.getVideosByCourse(courseId);
		long totalVideoCount = allVideos.size();
		model.addAttribute("totalVideoCount", totalVideoCount);
		
		return "admin/coursevideoupload";
	}

	
	
	
	// Arrangement & Search of Courses Part

	@GetMapping("/arrange-courses")
	public String viewArrangePage(Model model) {
		// Always fetch the row with ID 1. If it doesn't exist, create a generic one.
		CoursesArrangeModel arrangeModel = arrangeRepository.findById(1L).orElse(new CoursesArrangeModel());

		model.addAttribute("arrangeModel", arrangeModel);
		commonServiceImp.modelForAuth(model);

		return "admin/arrange-courses";
	}

	@PostMapping("/arrangecourse/save")
	public String saveArrangement(@ModelAttribute CoursesArrangeModel formModel) {

		// 1. Fetch the EXISTING record from the database (ID 1)
		// If it doesn't exist yet, create a new one.
		CoursesArrangeModel dbModel = arrangeRepository.findById(1L).orElse(new CoursesArrangeModel());

		// 2. Copy the values from the form (formModel) to the database object (dbModel)
		dbModel.setFirstId(formModel.getFirstId());
		dbModel.setSecondId(formModel.getSecondId());
		dbModel.setThirdId(formModel.getThirdId());
		dbModel.setFourthId(formModel.getFourthId());
		dbModel.setFifthId(formModel.getFifthId());

		// 3. Save the DATABASE object (which is 'attached' to the session)
		arrangeRepository.save(dbModel);

		return "redirect:/admin/arrange-courses?success";
	}

//coursesPart controller

	@GetMapping("/course/add")
	public String addCoursePage(Model model, @RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "5") int size) {

		List<CategoryModel> categories = categoryServiceImp.getAll();
		List<TeacherModels> teachers = userServiceImp.getAllTeachers();

		commonServiceImp.modelForAuth(model);

		// ✅ Pagination instead of getAll()
		Page<CourseModel> coursePage = courseServiceImp.getAllPaginated(page, size);

		model.addAttribute("course", new CourseModel());
		model.addAttribute("categoryList", categories);
		model.addAttribute("teacherList", teachers);

		// ✅ Required for pagination UI
		model.addAttribute("courses", coursePage.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", coursePage.getTotalPages());

		return "admin/course_add";
	}

	@PostMapping("/course/save")
	public String saveCourse(@ModelAttribute("course") CourseModel course,
			@RequestParam("logoFile") MultipartFile logoFile, RedirectAttributes ra) {

		try {
			if (course.getId() == null) {

				// create new
				String fileName = commonServiceImp.uploadFile("coursesthumbnail", logoFile);
				course.setLogo(fileName);
				CategoryModel cat = categoryServiceImp.getById(course.getCategoryId());
				UserModel teacher = userServiceImp.getTeacherById(course.getTeacherId());

				if (cat != null)
					course.setCategoryName(cat.getName());
				if (teacher != null)
					course.setTeacherName(teacher.getName());

				courseServiceImp.save(course);
				ra.addFlashAttribute("msg", "Course Added!");

			} else {

				// update: delete old file + save new one
				CourseModel old = courseServiceImp.getById(course.getId());

				CategoryModel cat = categoryServiceImp.getById(course.getCategoryId());
				UserModel teacher = userServiceImp.getTeacherById(course.getTeacherId());

				if (cat != null)
					course.setCategoryName(cat.getName());
				if (teacher != null)
					course.setTeacherName(teacher.getName());

				String newFile = commonServiceImp.updateFile("coursesthumbnail", logoFile, old.getLogo());

				course.setLogo(newFile);

				courseServiceImp.update(course);
				ra.addFlashAttribute("msg", "Course Updated!");
			}

		} catch (Exception e) {
			ra.addFlashAttribute("msg", "Failed: " + e.getMessage());
		}

		return "redirect:/admin/course/add";
	}

	@GetMapping("/course/edit")
	public String editCourse(@RequestParam("id") Long id, Model model) {
		CourseModel course = courseServiceImp.getById(id);

		model.addAttribute("course", course);
		model.addAttribute("categoryList", categoryServiceImp.getAll());
		model.addAttribute("teacherList", userServiceImp.getAllTeachers());
		commonServiceImp.modelForAuth(model);

		return "admin/course_add";
	}

	@GetMapping("/course/delete")
	public String delete(@RequestParam("id") Long id, RedirectAttributes ra) {

		CourseModel course = courseServiceImp.getById(id);

		if (course != null) {
			commonServiceImp.deleteFile("coursesthumbnail", course.getLogo());
			courseServiceImp.delete(id);
		}

		ra.addFlashAttribute("msg", "Course Deleted!");
		return "redirect:/admin/course/add";
	}

}
