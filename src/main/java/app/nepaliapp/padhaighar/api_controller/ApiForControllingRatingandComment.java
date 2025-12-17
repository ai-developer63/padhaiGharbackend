package app.nepaliapp.padhaighar.api_controller;

import java.math.BigDecimal;
import java.util.List;

import org.glassfish.jaxb.core.v2.TODO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import app.nepaliapp.padhaighar.api_model.CommentAndReviewRequestDTO;
import app.nepaliapp.padhaighar.api_model.CommentAndReviewResponseDTO;
import app.nepaliapp.padhaighar.api_model.CommentAndReviewWrapperDTO;
import app.nepaliapp.padhaighar.config.JwtUtil;
import app.nepaliapp.padhaighar.model.CommentRatingModel;
import app.nepaliapp.padhaighar.model.UserModel;
import app.nepaliapp.padhaighar.service.RatingAndCommentService;
import app.nepaliapp.padhaighar.serviceimp.UserServiceImp;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class ApiForControllingRatingandComment {
@Autowired
RatingAndCommentService service;

@Autowired
UserServiceImp userServiceImp;

@PostMapping("/save")
public CommentAndReviewResponseDTO saveRating(
        @RequestBody CommentAndReviewRequestDTO dto,
        @RequestHeader(value = "Authorization") String token) {

    token = token.substring(7);
    String username = JwtUtil.extractUsername(token);

    UserModel user = userServiceImp.getUserByPhoneorEmail(username);

    CommentRatingModel model = new CommentRatingModel();
    model.setSubjectId(dto.getSubjectId());
    model.setRating(dto.getRating());
    model.setComment(dto.getComment());
    model.setUserId(user.getId());
    model.setUserName(user.getName());

    CommentRatingModel saved = service.saveOrUpdateRating(model);

    return new CommentAndReviewResponseDTO(
            saved.getUserName(),
            saved.getRating(),
            saved.getComment()
    );
    }





    // GET → Average rating by subject
    @GetMapping("/average/{subjectId}")
    public BigDecimal getAverageRating(@PathVariable("subjectId") Long subjectId) {
        return service.getAverageRatingBySubjectId(subjectId);
    }
    

    @GetMapping("/comments/{subjectId}")
    public CommentAndReviewWrapperDTO getComments(@PathVariable("subjectId") Long subjectId) {

        List<CommentAndReviewResponseDTO> commentList = service.getCommentsBySubjectId(subjectId)
                .stream()
                .map(c -> new CommentAndReviewResponseDTO(
                        c.getUserName(),
                        c.getRating(),
                        c.getComment()
                ))
                .toList();

//      TODO : make it dynamic
        boolean isPurchased = true;

        return new CommentAndReviewWrapperDTO(isPurchased, commentList);
    }

}
