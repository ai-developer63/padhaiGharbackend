package app.nepaliapp.padhaighar.api_model;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CommentAndReviewRequestDTO {
    private Long subjectId;
    private BigDecimal rating;
    private String comment;
}
