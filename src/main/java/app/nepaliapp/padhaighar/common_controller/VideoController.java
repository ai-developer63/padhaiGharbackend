package app.nepaliapp.padhaighar.common_controller;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.nepaliapp.padhaighar.config.JwtUtil;
import app.nepaliapp.padhaighar.model.UserModel;
import app.nepaliapp.padhaighar.service.UsageTrackingService;
import app.nepaliapp.padhaighar.serviceimp.UserServiceImp;
import app.nepaliapp.padhaighar.serviceimp.VideoServiceImp;

@RestController
@RequestMapping("/api/")
public class VideoController {
	@Value("${upload_location}")
	String uploadLocation;
	@Autowired
	VideoServiceImp videoService;
	@Autowired
    UsageTrackingService usageTrackingService;
	@Autowired
	UserServiceImp userServiceImp;

	@GetMapping("/range/course/{videoId}")
	public CompletableFuture<ResponseEntity<Resource>> streamVideoRange(@PathVariable("videoId") String videoId,
			@RequestHeader(value = "Range", required = false) String range,
			@RequestHeader(value = "Authorization", required = false) String authHeader) {

		// 1️⃣ Validate JWT header
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
		}

		String token = authHeader.substring(7);
		if (JwtUtil.isTokenExpired(token)) {
			return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
		}

		 String username = JwtUtil.extractUsername(token);

		    UserModel user = userServiceImp.getUserByPhoneorEmail(username);
		Path videoPath = Paths.get(uploadLocation, "CoursesVideo");
		System.err.println("Video ID: " + videoId);
		System.out.println("Range: " + range);
		return videoService.streamVideo(videoPath, videoId, range,user.getId());
	}

	
	@PostMapping("admin/cleanup-usage")
    public ResponseEntity<String> cleanupOldData() {
        // Calls the @Async method
        usageTrackingService.cleanOldData();
        return ResponseEntity.ok("Cleanup process started in background.");
    }
}
