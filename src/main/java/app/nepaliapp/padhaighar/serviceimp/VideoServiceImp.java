package app.nepaliapp.padhaighar.serviceimp;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import app.nepaliapp.padhaighar.cache.TimedCache;
import app.nepaliapp.padhaighar.cache.TimedCacheManager;
import app.nepaliapp.padhaighar.config.ConfigConstants;
import app.nepaliapp.padhaighar.service.UsageTrackingService;
import app.nepaliapp.padhaighar.service.VideoService;

@Service
public class VideoServiceImp implements VideoService {
	@Autowired
	UsageTrackingService usageTrackingService;
	int corePoolSize = 20;
	int maxPoolSize = 60;
	long keepAliveTime = 60L; // seconds
	BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(200);

	ExecutorService executor = new ThreadPoolExecutor(
	    corePoolSize,
	    maxPoolSize,
	    keepAliveTime,
	    TimeUnit.SECONDS,
	    queue,
	    new ThreadPoolExecutor.CallerRunsPolicy() // backpressure
	);
    private static final String VIDEO_CACHE_NAME = "videoDataCache";
    private static final long MAX_CACHE_MEMORY_BYTES = 1_500L * 1024 * 1024; // 1.5 GB

    private final TimedCacheManager cacheManager = TimedCacheManager.getInstance();

    @Override
    public CompletableFuture<ResponseEntity<Resource>> streamVideo(Path path,String videoName, String range,Long userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String finalRange = range != null ? range : "bytes=0-";
                System.out.println("Requested Range: " + finalRange);

                // Parse range
                long rangeStart = 0;
                long rangeEnd;          
                Path videoPath =Paths.get(path.toString(),videoName);
                long totalFileLength = Files.size(videoPath);

                String[] ranges = finalRange.replace("bytes=", "").split("-");
                rangeStart = Long.parseLong(ranges[0]);
                rangeEnd = ranges.length > 1 && !ranges[1].isEmpty()
                        ? Long.parseLong(ranges[1])
                        : rangeStart + ConfigConstants.CHUNK_SIZE - 1;

                rangeEnd = Math.min(rangeEnd, totalFileLength - 1);

                if (rangeStart >= totalFileLength || rangeStart > rangeEnd) {
                    return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE).build();
                }
             // --- 3. TRACK USAGE HERE (CRITICAL STEP) ---
                // We track the calculated length irrespective of Cache or Disk source.
                long bytesToSend = rangeEnd - rangeStart + 1;
                usageTrackingService.track(userId, bytesToSend);

                // --- CACHE SECTION ---
                String cacheKey = videoName + ":" + rangeStart + "-" + rangeEnd;
                TimedCache<String, byte[]> videoCache =
                        cacheManager.getOrCreateMemoryCache(
                                VIDEO_CACHE_NAME,
                                MAX_CACHE_MEMORY_BYTES,
                                value -> value.length // each chunk size in bytes
                        );

                byte[] cachedData = videoCache.get(cacheKey);

                if (cachedData == null) {
                    // Read range if not cached
                    cachedData = readRangeFromFile(videoPath, rangeStart, rangeEnd);

                    System.out.println("Obtained from non-cached source");
                    if (cachedData != null) {
                        System.out.println("Saved in cache");
                        videoCache.put(cacheKey, cachedData);
                    } else {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                    }
                }

                // Build response
                return buildResponse(cachedData, rangeStart, rangeEnd, totalFileLength);

            } catch (IOException | NumberFormatException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }, executor);
    }

    private byte[] readRangeFromFile(Path videoPath, long rangeStart, long rangeEnd) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(videoPath.toFile(), "r")) {
            file.seek(rangeStart);
            byte[] data = new byte[(int) (rangeEnd - rangeStart + 1)];
            file.readFully(data);
            return data;
        }
    }

    private ResponseEntity<Resource> buildResponse(byte[] data, long rangeStart, long rangeEnd, long totalFileLength) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Range", String.format("bytes %d-%d/%d", rangeStart, rangeEnd, totalFileLength));
        headers.add("Accept-Ranges", "bytes");
        headers.setCacheControl(CacheControl.maxAge(5, TimeUnit.DAYS).cachePublic());
        headers.setContentLength(data.length);
        headers.setContentType(MediaType.parseMediaType("video/mp4"));
        headers.add("Content-Disposition", "inline");

        ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .headers(headers)
                .body(resource);
    }
}