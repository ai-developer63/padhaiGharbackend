package app.nepaliapp.padhaighar.service;

import java.math.BigDecimal;
import java.util.List;

import app.nepaliapp.padhaighar.model.CommentRatingModel;

public interface RatingAndCommentService {
	 CommentRatingModel saveOrUpdateRating(CommentRatingModel model);

	    BigDecimal getAverageRatingBySubjectId(Long subjectId);

	    List<CommentRatingModel> getCommentsBySubjectId(Long subjectId);
}
