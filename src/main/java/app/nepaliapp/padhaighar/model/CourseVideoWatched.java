package app.nepaliapp.padhaighar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Entity
@Table(
    name = "course_video_watched",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "video_id"})
    },
    indexes = {
        @Index(name = "idx_user_course", columnList = "user_id, course_id"),
        @Index(name = "idx_user_video", columnList = "user_id, video_id")
    }
)
@Data
public class CourseVideoWatched {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "video_id", nullable = false)
    private Long videoId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(nullable = false)
    private boolean studied;

    @Column(nullable = false)
    private LocalDate studiedDate;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
