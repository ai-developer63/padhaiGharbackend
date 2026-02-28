package app.nepaliapp.padhaighar.common_controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import app.nepaliapp.padhaighar.model.CourseModel;
import app.nepaliapp.padhaighar.service.CourseService;

@Controller
public class WebSearchController {

    @Autowired
    private CourseService courseService;

    // A simple, crash-proof endpoint available to your dashboards
    @GetMapping("/search/courses")
    @ResponseBody
    public List<Map<String, Object>> searchCoursesAjax(@RequestParam("q") String query) {
        
        List<CourseModel> results = courseService.searchForApp(query, null);
        
        // We use a Map to only send exactly what the Javascript needs, preventing any DTO/Price crashes!
        return results.stream().map(course -> {
            Map<String, Object> map = new HashMap<>();
            map.put("subjectId", course.getId());
            map.put("subjectName", course.getName());
            map.put("categoryName", course.getCategoryName() != null ? course.getCategoryName() : "Course");
            return map;
        }).collect(Collectors.toList());
    }
}