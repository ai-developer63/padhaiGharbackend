package app.nepaliapp.padhaighar.api_controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import app.nepaliapp.padhaighar.api_model.SubjectDTO;

import app.nepaliapp.padhaighar.model.BannerModel;
import app.nepaliapp.padhaighar.model.CategoryModel;
import app.nepaliapp.padhaighar.model.CourseModel;
import app.nepaliapp.padhaighar.model.dto.BannerDTO;
import app.nepaliapp.padhaighar.model.dto.CategoryDTO;
import app.nepaliapp.padhaighar.model.dto.HomeResponseDTO;

import app.nepaliapp.padhaighar.serviceimp.BannerServiceImp;
import app.nepaliapp.padhaighar.serviceimp.CategoryServiceImp;
import app.nepaliapp.padhaighar.serviceimp.CommonServiceImp;
import app.nepaliapp.padhaighar.serviceimp.CourseServiceImp;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/")
public class ApiControllerForAppHome {

	@Autowired
	CategoryServiceImp categoryServiceImp;

	@Autowired
	BannerServiceImp bannerServiceImp;

	@Autowired
	CommonServiceImp commonServiceImp;
	
	@Autowired
	CourseServiceImp courseServiceImp;
	

	
	
	@GetMapping("subjects")
	public List<SubjectDTO> getHomeSubjectData() {
		
		// 1. Get the Sorted List from Service (Models)
		List<CourseModel> sortedCourses = courseServiceImp.getAllActiveArranged();

		// 2. Convert Model -> DTO and Return
		return sortedCourses.stream()
				.limit(30) //total subjects at a moment
				.map(course -> {
					SubjectDTO dto = new SubjectDTO();

					dto.setSubjectName(course.getName());
					dto.setCategoryName(course.getCategoryName());
					dto.setTeacherName(course.getTeacherName());

					// ✅ Build absolute image URL
					dto.setSubjectLogo(
						commonServiceImp.buildUrlString("/course/", course.getLogo())
					);

					dto.setDescription(course.getDescription());

					// ✅ Convert price safely
					if (course.getPrice() != null) {
						dto.setOriginalprice(new BigDecimal(course.getPrice()));
					}

					// ✅ Random rating
					dto.setRating(randomRating());

					return dto;
				})
				.collect(Collectors.toList());
	}
	
	

	@Operation(summary = "Get home Info", description = "returns data of User details")
	@GetMapping("home")
	public HomeResponseDTO getHomeData() {

		List<BannerModel> bannerList = bannerServiceImp.getAllBanners();
		List<CategoryModel> categoryList = categoryServiceImp.getAll();

		// Map Banner Models → DTOs
		// Filter only visible banners
		List<BannerDTO> bannerDTOs = bannerList.stream().filter(BannerModel::getIsVisible) // only true
				.map(b -> {
					BannerDTO dto = new BannerDTO();
					dto.setBanner_link(commonServiceImp.buildUrlString("/banner/", b.getBanner()));
					dto.setCourseId(b.getCourseId());
					dto.setCourseName(b.getCourseName());
					return dto;
				}).collect(Collectors.toList());

		// Filter only enabled categories
		List<CategoryDTO> categoryDTOs = categoryList.stream().filter(CategoryModel::getIsEnable) // only true
				.map(c -> {
					CategoryDTO dto = new CategoryDTO();
					dto.setCategoryName(c.getName());
					dto.setCategoryLogo(commonServiceImp.buildUrlString("/categories/", c.getLogo()));
					return dto;
				}).collect(Collectors.toList());

		// Prepare Final Response
		HomeResponseDTO response = new HomeResponseDTO();
		response.setBanner(bannerDTOs);
		response.setCategory(categoryDTOs);
		return response;
	}
	
	
	
	private BigDecimal randomRating() {
	    BigDecimal[] ratings = {
	        new BigDecimal("3.0"),
	        new BigDecimal("3.5"),
	        new BigDecimal("4.0"),
	        new BigDecimal("4.5"),
	        new BigDecimal("5.0")
	    };

	    int index = new java.util.Random().nextInt(ratings.length);
	    return ratings[index];
	}

}
