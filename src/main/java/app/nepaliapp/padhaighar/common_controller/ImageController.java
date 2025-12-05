package app.nepaliapp.padhaighar.common_controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import app.nepaliapp.padhaighar.serviceimp.FileSharingServiceImp;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import java.nio.file.Files;

import org.springframework.core.io.Resource;
@Controller
public class ImageController {
	@Autowired
	FileSharingServiceImp fileServingServiceImp;
	
	
	@GetMapping("/categories/{filename}")
	public ResponseEntity<Resource> serverCategoryIcon(@PathVariable("filename") String filename) {
	    try {
	        Resource resource = fileServingServiceImp.getCategoryIcon(filename);
	        String contentType;
	        try {
	            contentType = Files.probeContentType(resource.getFile().toPath());
	        } catch (IOException ex) {
	            contentType = null;
	        }

	        if (contentType == null) {
	            String name = resource.getFilename().toLowerCase();
	            if (name.endsWith(".png")) {
	                contentType = "image/png";
	            } else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
	                contentType = "image/jpeg";
	            } else if (name.endsWith(".gif")) {
	                contentType = "image/gif";
	            } else if (name.endsWith(".svg")) {
	                contentType = "image/svg+xml";
	            } else if (name.endsWith(".webp")) {
	                contentType = "image/webp";
	            } else {
	                contentType = "application/octet-stream"; // fallback for unknowns
	            }
	        }


	        return ResponseEntity.ok()
	                .contentType(MediaType.parseMediaType(contentType))
	                .body(resource);

	    } catch (IOException e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	    }
	}
	
	

	@GetMapping("/banner/{filename}")
	public ResponseEntity<Resource> serverCourseVideoThumnail(@PathVariable("filename") String filename) {
	    try {
	        Resource resource = fileServingServiceImp.getBannerImage(filename);
	        String contentType;
	        try {
	            contentType = Files.probeContentType(resource.getFile().toPath());
	        } catch (IOException ex) {
	            contentType = null;
	        }

	        if (contentType == null) {
	            String name = resource.getFilename().toLowerCase();
	            if (name.endsWith(".png")) {
	                contentType = "image/png";
	            } else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
	                contentType = "image/jpeg";
	            } else if (name.endsWith(".gif")) {
	                contentType = "image/gif";
	            } else if (name.endsWith(".svg")) {
	                contentType = "image/svg+xml";
	            } else if (name.endsWith(".webp")) {
	                contentType = "image/webp";
	            } else {
	                contentType = "application/octet-stream"; // fallback for unknowns
	            }
	        }


	        return ResponseEntity.ok()
	                .contentType(MediaType.parseMediaType(contentType))
	                .body(resource);

	    } catch (IOException e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	    }
	}
	
}
