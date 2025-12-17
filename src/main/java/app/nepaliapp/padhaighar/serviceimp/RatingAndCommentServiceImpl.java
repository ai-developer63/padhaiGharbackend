package app.nepaliapp.padhaighar.serviceimp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.nepaliapp.padhaighar.model.CommentRatingModel;
import app.nepaliapp.padhaighar.repository.RatingandCommentRepository;
import app.nepaliapp.padhaighar.service.RatingAndCommentService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RatingAndCommentServiceImpl implements RatingAndCommentService {
	@Autowired
	RatingandCommentRepository repository;

	@Override
	public CommentRatingModel saveOrUpdateRating(CommentRatingModel model) {
		// If same user already rated same subject → update
		return repository.findBySubjectIdAndUserId(model.getSubjectId(), model.getUserId()).map(existing -> {
			existing.setRating(model.getRating());
			existing.setComment(model.getComment());
			return repository.save(existing);
		}).orElseGet(() -> repository.save(model));
	}

	@Override
	public BigDecimal getAverageRatingBySubjectId(Long subjectId) {

		List<CommentRatingModel> ratings = repository.findBySubjectId(subjectId);

		if (ratings.isEmpty()) {
			return BigDecimal.ZERO;
		}

		BigDecimal total = ratings.stream().map(CommentRatingModel::getRating).reduce(BigDecimal.ZERO, BigDecimal::add);

		return total.divide(BigDecimal.valueOf(ratings.size()), 2, RoundingMode.HALF_UP);
	}

	@Override
	public List<CommentRatingModel> getCommentsBySubjectId(Long subjectId) {
		return repository.findBySubjectId(subjectId);
	}
}
