package app.nepaliapp.padhaighar.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import app.nepaliapp.padhaighar.model.CourseVideoWatched;

public interface CourseVideoWatchedRepository extends JpaRepository<CourseVideoWatched, Long> {

    Optional<CourseVideoWatched> 
        findByUserIdAndVideoId(Long userId, Long videoId);

    boolean existsByUserIdAndVideoId(Long userId, Long videoId);
    
    @Query("""
            SELECT COALESCE(c.studied, false)
            FROM CourseVideoWatched c
            WHERE c.userId = :userId AND c.videoId = :videoId
        """)
        Boolean isVideoStudied(
            @Param("userId") Long userId,
            @Param("videoId") Long videoId
        );
}