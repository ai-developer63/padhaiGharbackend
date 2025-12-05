package app.nepaliapp.padhaighar.service;

import java.util.List;
import app.nepaliapp.padhaighar.model.CourseModel;

public interface CourseService {

    CourseModel save(CourseModel model);

    CourseModel update(CourseModel model);

    void delete(Long id);

    CourseModel getById(Long id);

    List<CourseModel> getAll();
}
