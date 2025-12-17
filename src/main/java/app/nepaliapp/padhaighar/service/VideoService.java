package app.nepaliapp.padhaighar.service;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface VideoService {
	  CompletableFuture<ResponseEntity<Resource>> streamVideo(Path path,String videoId, String range,Long userId);
}
