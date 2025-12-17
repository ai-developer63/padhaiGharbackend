package app.nepaliapp.padhaighar.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import app.nepaliapp.padhaighar.model.CommentRatingModel;

public interface RatingandCommentRepository extends JpaRepository<CommentRatingModel, Long>{

    List<CommentRatingModel> findBySubjectId(Long subjectId);

  

	Optional<CommentRatingModel> findBySubjectIdAndUserId(Long subjectId, Long userId);
}
