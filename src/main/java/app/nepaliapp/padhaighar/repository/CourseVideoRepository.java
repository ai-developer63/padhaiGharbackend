package app.nepaliapp.padhaighar.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import app.nepaliapp.padhaighar.model.CourseVideoModel;

@Repository
public interface CourseVideoRepository extends JpaRepository<CourseVideoModel, Long> {

    /** Finds all videos associated with a given course ID. */
    List<CourseVideoModel> findByCourseId(Long courseId);
    
    long countByCourseId(Long courseId);
}
