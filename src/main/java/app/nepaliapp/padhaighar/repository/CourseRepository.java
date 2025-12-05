package app.nepaliapp.padhaighar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import app.nepaliapp.padhaighar.model.CourseModel;

@Repository
public interface CourseRepository extends JpaRepository<CourseModel, Long> {

}
