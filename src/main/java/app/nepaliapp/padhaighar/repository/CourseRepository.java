package app.nepaliapp.padhaighar.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import app.nepaliapp.padhaighar.model.CourseModel;

@Repository
public interface CourseRepository extends JpaRepository<CourseModel, Long> {
	
	// 🔍 The Master Search Query
	@Query("SELECT c FROM CourseModel c WHERE " +
	           "( :keyword IS NULL OR :keyword = '' OR " +
	           "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
	           "LOWER(c.searchTags) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
	           "LOWER(c.teacherName) LIKE LOWER(CONCAT('%', :keyword, '%')) ) " +
	           
	           // ✅ CHANGED: Now filtering by Category Name
	           "AND ( :categoryName IS NULL OR :categoryName = '' OR LOWER(c.categoryName) = LOWER(:categoryName) ) " +
	           
	           "AND ( :isActive IS NULL OR c.isActive = :isActive )")
	    Page<CourseModel> searchCourses(
	            @Param("keyword") String keyword,
	            @Param("categoryName") String categoryName, // Changed from Long to String
	            @Param("isActive") Boolean isActive,
	            Pageable pageable
	    );
	
	
	
	  Page<CourseModel> findAll(Pageable pageable);
}
