package app.nepaliapp.padhaighar.api_model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommentAndReviewWrapperDTO {

    private boolean isPurchased;
    private List<CommentAndReviewResponseDTO> comments;
}

