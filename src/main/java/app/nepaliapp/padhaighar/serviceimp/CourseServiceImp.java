package app.nepaliapp.padhaighar.serviceimp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;

import app.nepaliapp.padhaighar.api_model.VideoAPIDTO;
import app.nepaliapp.padhaighar.model.CourseModel;
import app.nepaliapp.padhaighar.model.CourseVideoModel;
import app.nepaliapp.padhaighar.model.CoursesArrangeModel;
import app.nepaliapp.padhaighar.repository.CourseRepository;
import app.nepaliapp.padhaighar.repository.CourseVideoRepository;
import app.nepaliapp.padhaighar.repository.CoursesArrangeRepository;
import app.nepaliapp.padhaighar.service.CourseService;

@Service
public class CourseServiceImp implements CourseService {

    @Autowired
    CourseRepository courseRepo;
    
    @Autowired
    CoursesArrangeRepository arrangeRepo;
    
    @Autowired 
    CourseVideoRepository courseVideoRepository;
    
    @Autowired
    CommonServiceImp commonServiceImp;
    
    
    
    //Courses Video upload part
   @Override
   public List<VideoAPIDTO> requiredVideoTosupply(Long courseId, Long userId) {

       List<CourseVideoModel> videos = courseVideoRepository.findByCourseId(courseId);

       List<VideoAPIDTO> response = new ArrayList<>();

       for (CourseVideoModel video : videos) {

           boolean isUnlocked;

           // 🔒 Unlock logic (simplified for now)TODO
           if (Boolean.FALSE.equals(video.getIsPaid())) {
               isUnlocked = true; // Free video
           } else {
               isUnlocked = false; // Paid – user entitlement ignored for now TODO
           }

           VideoAPIDTO dto = new VideoAPIDTO(
                   video.getTitle(),
                  commonServiceImp.buildUrlString("/api/range/course/",video.getVideo()),
                   isUnlocked,
                   false // isStudied → intentionally ignored TODO
           );

           response.add(dto);
       }

       return response;
   }

    
    
 
 @Override
    public CourseVideoModel getVideoById(Long videoId) {
        return courseVideoRepository.findById(videoId).orElse(null);
    }

 @Override
    public void deleteVideo(Long videoId) {
        CourseVideoModel video = getVideoById(videoId);
        if (video != null) {
            commonServiceImp.deleteFile("coursesVideo", video.getVideo());
            courseVideoRepository.deleteById(videoId);
        }
    }
    
    
    
    @Override
    public void saveVideo(CourseVideoModel video) {
        courseVideoRepository.save(video);
    }
    
    @Override
    public List<CourseVideoModel> getVideosByCourse(Long courseId) {
        return courseVideoRepository.findByCourseId(courseId); 
    }
    
    @Override
    public long countVideosByCourse(Long courseId) {
        return courseVideoRepository.countByCourseId(courseId);
    }
    
    
    
    
    
    
    
    
    
    
    
    
    //Courses Other details
    // ✅ Pagination logic
    @Override
    public Page<CourseModel> getAllPaginated(int page, int size) {
        return courseRepo.findAll(
                PageRequest.of(page, size, Sort.by("id").descending())
        );
    }

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
    
    @Override
    public List<CourseModel> getAllActiveArranged() {
        // 1. Fetch all courses
        List<CourseModel> allCourses = getAll();

        // 2. Fetch the Arrangement Config (ID 1)
        CoursesArrangeModel arrange = arrangeRepo.findById(1L).orElse(new CoursesArrangeModel());

        // 3. Create Priority ID List
        List<Long> priorityIds = new ArrayList<>();
     // Removed "!= null" checks because primitives cannot be null
        if (arrange.getFirstId() > 0) priorityIds.add(arrange.getFirstId());
        if (arrange.getSecondId() > 0) priorityIds.add(arrange.getSecondId());
        if (arrange.getThirdId() > 0) priorityIds.add(arrange.getThirdId());
        if (arrange.getFourthId() > 0) priorityIds.add(arrange.getFourthId());
        if (arrange.getFifthId() > 0) priorityIds.add(arrange.getFifthId());
        // 4. Filter Active Courses & Map by ID
        Map<Long, CourseModel> activeCoursesMap = allCourses.stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsActive())) // Null-safe check
                .collect(Collectors.toMap(CourseModel::getId, c -> c));

        // 5. Build Final List
        List<CourseModel> sortedList = new ArrayList<>();

        // Add Priority items first
        for (Long id : priorityIds) {
            if (activeCoursesMap.containsKey(id)) {
                sortedList.add(activeCoursesMap.get(id));
                activeCoursesMap.remove(id); // Remove so we don't duplicate
            }
        }

        // --- NEW: Randomize remaining active items ---
        
        // Convert remaining map values to a List
        List<CourseModel> remaining = new ArrayList<>(activeCoursesMap.values());

//        // Create a seed based on the current date (Order changes daily, not every refresh)
//        long seed = java.time.LocalDate.now().toEpochDay();
//        java.util.Collections.shuffle(remaining, new java.util.Random(seed));

        
     // Shuffle without a seed (This makes it random every time you call the API)
        java.util.Collections.shuffle(remaining);
        
        
        // Add the randomized list to the final list
        sortedList.addAll(remaining);

        return sortedList;
    }
    
    @Override
    public List<CourseModel> searchForApp(String keyword, String categoryName) {
        Page<CourseModel> pageResult = courseRepo.searchCourses(
            keyword, 
            categoryName, // Passing Name
            true,         // Active Only
            PageRequest.of(0, 50, Sort.by("id").descending())
        );
        return pageResult.getContent();
    }

    // 2️⃣ For Admin Dashboard (With Pagination)
    @Override
    public Page<CourseModel> searchForAdmin(String keyword, String categoryName, int page, int size) {
        // isActive = NULL (Admin sees both Active and Inactive)
        return courseRepo.searchCourses(
            keyword, 
            categoryName, 
            null, 
            PageRequest.of(page, size, Sort.by("id").descending())
        );
    }
    
    
}
