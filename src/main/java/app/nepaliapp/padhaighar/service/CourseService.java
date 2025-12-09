package app.nepaliapp.padhaighar.service;

import java.util.List;

import org.springframework.data.domain.Page;

import app.nepaliapp.padhaighar.model.CourseModel;
import app.nepaliapp.padhaighar.model.CourseVideoModel;

public interface CourseService {

    CourseModel save(CourseModel model);

    CourseModel update(CourseModel model);

    void delete(Long id);

    CourseModel getById(Long id);

    List<CourseModel> getAll();
    
    Page<CourseModel> getAllPaginated(int page, int size);

	List<CourseModel> getAllActiveArranged();
	

    // For Admin Dashboard (Returns Page, All Status)
    Page<CourseModel> searchForAdmin(String keyword, String categoryName, int page, int size);

	List<CourseModel> searchForApp(String keyword, String categoryName);

	//Video part
	List<CourseVideoModel> getVideosByCourse(Long courseId);

	long countVideosByCourse(Long courseId);
}
