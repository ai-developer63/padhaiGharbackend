package app.nepaliapp.padhaighar.serviceimp;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.nepaliapp.padhaighar.api_model.VideoStudyRequestDTO;
import app.nepaliapp.padhaighar.model.CourseVideoWatched;
import app.nepaliapp.padhaighar.repository.CourseVideoWatchedRepository;
import app.nepaliapp.padhaighar.service.CourseVideoWatchedService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseVideoWatchedServiceImp implements CourseVideoWatchedService {
	@Autowired
	CourseVideoWatchedRepository repository;

	@Override
	public boolean isVideoStudied(Long userId, Long videoId) {
		Boolean result = repository.isVideoStudied(userId, videoId);
		System.out.println("lets see" + result + "userID  " + userId + "videoId" +videoId );
		return result != null && result;
	}

	@Override
	public void markVideoStudied(Long userId, VideoStudyRequestDTO dto) {

		CourseVideoWatched watched = repository.findByUserIdAndVideoId(userId, dto.getVideoId()).orElseGet(() -> {
			CourseVideoWatched newEntry = new CourseVideoWatched();
			newEntry.setUserId(userId);
			newEntry.setVideoId(dto.getVideoId());
			newEntry.setCourseId(dto.getCourseId());
			newEntry.setStudiedDate(LocalDate.now());
			return newEntry;
		});

		watched.setStudied(dto.isStudied());
		watched.setUpdatedAt(LocalDateTime.now());

		repository.save(watched);
	}
}