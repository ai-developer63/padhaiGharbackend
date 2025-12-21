package app.nepaliapp.padhaighar.service;

import app.nepaliapp.padhaighar.api_model.VideoStudyRequestDTO;

public interface CourseVideoWatchedService {
    void markVideoStudied(Long userId, VideoStudyRequestDTO dto);
    boolean isVideoStudied(Long userId, Long videoId);
}

