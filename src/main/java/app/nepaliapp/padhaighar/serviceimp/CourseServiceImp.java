package app.nepaliapp.padhaighar.serviceimp;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.nepaliapp.padhaighar.model.CourseModel;
import app.nepaliapp.padhaighar.repository.CourseRepository;
import app.nepaliapp.padhaighar.service.CourseService;

@Service
public class CourseServiceImp implements CourseService {

    @Autowired
    CourseRepository courseRepo;

    @Override
    public CourseModel save(CourseModel model) {
        return courseRepo.save(model);
    }

    @Override
    public CourseModel update(CourseModel model) {
        return courseRepo.save(model);
    }

    @Override
    public void delete(Long id) {
        courseRepo.deleteById(id);
    }

    @Override
    public CourseModel getById(Long id) {
        return courseRepo.findById(id).orElse(null);
    }

    @Override
    public List<CourseModel> getAll() {
        return courseRepo.findAll();
    }
}
