package app.nepaliapp.padhaighar.api_model;

import lombok.Data;

@Data
public class VideoStudyRequestDTO {
    private Long videoId;
    private Long courseId;
    private boolean studied;
}
