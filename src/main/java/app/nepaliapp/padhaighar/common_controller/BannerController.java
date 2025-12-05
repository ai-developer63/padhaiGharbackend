package app.nepaliapp.padhaighar.common_controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import app.nepaliapp.padhaighar.model.BannerModel;
import app.nepaliapp.padhaighar.serviceimp.BannerServiceImp;
import app.nepaliapp.padhaighar.serviceimp.CommonServiceImp;

@Controller
@RequestMapping("/admin/banner")
public class BannerController {

	@Autowired
	BannerServiceImp bannerService;

	@Autowired
	CommonServiceImp commonServiceImp;

	@Value("${upload_location}")
	String uploadLocation;

	// Page
	@GetMapping("/manage")
	public String manageBannersPage(@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, Model model) {

		Pageable pageable = PageRequest.of(page, size);
		Page<BannerModel> bannersPage = bannerService.getBannersPage(pageable);

		// Hardcoded courses for now
		List<Map<String, Object>> courses = List.of(Map.of("id", 1, "name", "Mathematics"),
				Map.of("id", 2, "name", "Science"), Map.of("id", 3, "name", "English"),
				Map.of("id", 4, "name", "Computer Science"));

		model.addAttribute("bannersPage", bannersPage);
		model.addAttribute("banners", bannersPage.getContent());
		model.addAttribute("courses", courses);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", bannersPage.getTotalPages());
		commonServiceImp.modelForAuth(model);
		return "admin/banner-manage";
	}

	// BannerController.java
	@PostMapping("/save")
	@ResponseBody
	public ResponseEntity<?> saveBanner(@RequestParam("name") String name, @RequestParam("courseId") Long courseId,
			@RequestParam("bannerFile") MultipartFile bannerFile,
			@RequestParam(name = "isVisible", defaultValue = "true") Boolean isVisible) {

		if (bannerFile.isEmpty()) {
			return ResponseEntity.badRequest().body("Banner file is required");
		}

		try {
			// Ensure upload directory exists
			String uploadDir = uploadLocation + File.separator + "banners";
			File saveDir = new File(uploadDir);
			if (!saveDir.exists())
				saveDir.mkdirs();

			// Save the file
			String originalFilename = bannerFile.getOriginalFilename();
			String extension = "";
			int i = originalFilename.lastIndexOf('.');
			if (i > 0)
				extension = originalFilename.substring(i); // ".jpg"
			String randomName = generateRandomString(8);
			String newFilename = randomName + extension;
			Path path = Paths.get(saveDir.getAbsolutePath(), newFilename);
			Files.copy(bannerFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

			// Save Banner entity
			BannerModel banner = new BannerModel();
			banner.setName(name);
			banner.setCourseId(courseId);
			banner.setCourseName("Sample Course"); // TODO: map courseId to name dynamically later
			banner.setIsVisible(isVisible);
			banner.setBanner(newFilename); // store the saved filename, not original

			BannerModel savedBanner = bannerService.saveBanner(banner);

			return ResponseEntity.ok(savedBanner);

		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error saving banner file: " + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error saving banner: " + e.getMessage());
		}
	}

	// Delete banner
	@PostMapping("/delete")
	@ResponseBody
	public ResponseEntity<?> deleteBanner(@RequestParam("id") Long id) {
		try {
			// Fetch the banner first
			BannerModel banner = bannerService.getBannerById(id);
			if (banner == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Banner not found");
			}

			// Delete the file from disk
			String uploadDir = uploadLocation + File.separator + "banners";
			Path filePath = Paths.get(uploadDir, banner.getBanner());
			if (Files.exists(filePath)) {
				try {
					Files.delete(filePath);
				} catch (IOException e) {
					// If file deletion fails, do not delete database record
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.body("Failed to delete banner file: " + e.getMessage());
				}
			}

			// Delete banner from DB
			bannerService.deleteBanner(id);
			return ResponseEntity.ok("Banner deleted successfully");

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error deleting banner: " + e.getMessage());
		}
	}

	// Toggle visibility
	@PostMapping("/toggle")
	@ResponseBody
	public BannerModel toggleVisibility(@RequestParam("id") Long id) {
		return bannerService.toggleVisibility(id);
	}

	public String generateRandomString(int length) {
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		Random rnd = new Random();
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			sb.append(chars.charAt(rnd.nextInt(chars.length())));
		}
		return sb.toString();
	}
}
