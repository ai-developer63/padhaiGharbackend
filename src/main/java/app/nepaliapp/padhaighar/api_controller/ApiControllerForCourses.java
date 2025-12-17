package app.nepaliapp.padhaighar.api_controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.nepaliapp.padhaighar.api_model.SubjectDTO;
import app.nepaliapp.padhaighar.api_model.VideoAPIDTO;
import app.nepaliapp.padhaighar.model.CourseModel;
import app.nepaliapp.padhaighar.serviceimp.CommonServiceImp;
import app.nepaliapp.padhaighar.serviceimp.CourseServiceImp;
import app.nepaliapp.padhaighar.serviceimp.RatingAndCommentServiceImpl;

@RestController
@RequestMapping("/api/courses")
public class ApiControllerForCourses {
	@Autowired
	CourseServiceImp courseServiceImp;
	  
	@Autowired
	CommonServiceImp commonServiceImp;
	
	@Autowired
	RatingAndCommentServiceImpl ratingAndCommentServiceImpl;
	  
	  
	
	
	@GetMapping("/video/{id}")
	public ResponseEntity<List<VideoAPIDTO>> getCourseVideo(@PathVariable("id") Long id){
		
		return ResponseEntity.ok(courseServiceImp.requiredVideoTosupply(id,1L));
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
    @GetMapping("/{id}")
    public ResponseEntity<SubjectDTO> getCourseById(@PathVariable("id") Long id) {
        
        // 1. Fetch the course model
        CourseModel course = courseServiceImp.getById(id);
        
        // 2. Check if course exists
        if (course == null) {
            return ResponseEntity.notFound().build();
        }

        // 3. Map to DTO
        SubjectDTO dto = new SubjectDTO();
        dto.setSubjectName(course.getName());
        dto.setCategoryName(course.getCategoryName());
        dto.setTeacherName(course.getTeacherName());
        
        // Generate full URL for the logo
        dto.setSubjectLogo(commonServiceImp.buildUrlString("/course/", course.getLogo()));
        
        dto.setDescription(course.getDescription());
        
        // Price (convert String/Double to BigDecimal safely)
        if (course.getPrice() != null) {
            dto.setOriginalprice(new java.math.BigDecimal(course.getPrice()));
        }
        
        
        dto.setRating(ratingAndCommentServiceImpl.getAverageRatingBySubjectId(id));

        return ResponseEntity.ok(dto);
    }
	
	
	
	
	
	
	
	
	
	
	
	
	
	  
	  //Search 
	
	
	@GetMapping("/search")
    public List<SubjectDTO> searchCourses(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "category", required = false) String categoryName) { // Accepting Name

        // Call service with name
        List<CourseModel> results = courseServiceImp.searchForApp(query, categoryName);

        // Convert to DTO
        return results.stream().map(course -> {
            SubjectDTO dto = new SubjectDTO();
            dto.setSubjectName(course.getName());
            dto.setCategoryName(course.getCategoryName());
            dto.setTeacherName(course.getTeacherName());
            dto.setSubjectLogo(commonServiceImp.buildUrlString("/course/", course.getLogo()));
            dto.setDescription(course.getDescription());
            
            if (course.getPrice() != null) 
                dto.setOriginalprice(new java.math.BigDecimal(course.getPrice()));
            
            dto.setRating(new java.math.BigDecimal("4.5"));
            return dto;
        }).collect(Collectors.toList());
    }
	
	

}
