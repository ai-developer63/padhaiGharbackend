package app.nepaliapp.padhaighar.config;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;

import jakarta.annotation.PostConstruct;

@Component
public class ConfigConstants {

	@Value("${securitylieshere}")
	private String keys;
	  
    @Value("${upload_location}")
    private String positionUrl;
	
	public static String SECRET_KEY;

    public static String VIDEO_STORAGE_DIR;

    
    public static final int CHUNK_SIZE=1024*1024;
	
	@PostConstruct
	public void init() {
		SECRET_KEY = keys;
	}
	
	   public String encodingIfOtherUsed() {
	    	return Base64.getUrlEncoder().withoutPadding().encodeToString(keys.getBytes(StandardCharsets.UTF_8));
	    }
	    


	    @PostConstruct
	    public void initvideo() {
	    	VIDEO_STORAGE_DIR = Paths.get(positionUrl, "TutorialVideos").toString();
	    }

	    public String getVideoStorageDir() {
	        return VIDEO_STORAGE_DIR;
	    }
	    
	

}
