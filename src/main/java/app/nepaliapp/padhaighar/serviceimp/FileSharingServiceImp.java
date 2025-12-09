package app.nepaliapp.padhaighar.serviceimp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import app.nepaliapp.padhaighar.service.FileSharingService;

@Service
public class FileSharingServiceImp implements FileSharingService {

	@Value("${upload_location}")
	String uploadLocation;
	
	
	 @Override
	    public Resource getFile(String folder, String filename) throws IOException {

	        // Build folder path: upload_location/folder
	        Path folderPath = Paths.get(uploadLocation, folder)
	                .toAbsolutePath()
	                .normalize();

	        // Create folder if missing
	        File dir = new File(folderPath.toString());
	        if (!dir.exists()) dir.mkdirs();

	        // File path
	        Path finalPath = folderPath.resolve(filename).normalize();

	        return getResourceByPath(finalPath);
	    }

	
	private Resource getResourceByPath(Path filePath) throws IOException {
		Resource resource = new UrlResource(filePath.toUri());

		if (resource.exists() && resource.isReadable()) {
			return resource;
		} else {
			throw new IOException("File not found or unreadable: " + filePath);
		}
	}
}
