package app.nepaliapp.padhaighar.serviceimp;

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
	public Resource getBannerImage(String filename) throws IOException {
		 Path uploadPath = Paths.get(uploadLocation, "banners").toAbsolutePath().normalize();

			String uploadLocations = uploadPath.toString();
			Path filePath = Paths.get(uploadLocations).resolve(filename).normalize();
			try {
				return getResourceByPath(filePath);
			}catch (IOException e) {
				throw new IOException("File not found or unreadable:"+filePath);
			}
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
