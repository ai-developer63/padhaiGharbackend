package app.nepaliapp.padhaighar.common_controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import app.nepaliapp.padhaighar.model.CategoryModel;
import app.nepaliapp.padhaighar.service.CategoryService;
import app.nepaliapp.padhaighar.serviceimp.CommonServiceImp;

@Controller
@RequestMapping("/admin/category")
public class CategoryController {

	@Value("${upload_location}")
	String uploadLocation;

    @Autowired
    CategoryService categoryService;

    @Autowired
    CommonServiceImp commonServiceImp;
    
    
    
    
    @GetMapping("/manage")
    public String managePage(Model model) {
        model.addAttribute("categories", categoryService.getAll());
        commonServiceImp.modelForAuth(model);
        return "admin/category-manage";
    }

    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<?> saveCategory(
            @RequestParam("name") String name,
            @RequestParam("logoFile") MultipartFile logoFile,
            @RequestParam(name="isEnable", defaultValue="true") Boolean isEnable) {

        try {
        	String uploadDir = uploadLocation + File.separator + "category";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String original = logoFile.getOriginalFilename();
            String ext = original.substring(original.lastIndexOf('.'));
            String newName = UUID.randomUUID().toString().substring(0,8) + ext;

            Path path = Paths.get(uploadDir, newName);
            Files.copy(logoFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            CategoryModel cm = new CategoryModel();
            cm.setName(name);
            cm.setLogo(newName);
            cm.setIsEnable(isEnable);

            return ResponseEntity.ok(categoryService.saveCategory(cm));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error uploading file");
        }
    }

    @PostMapping("/toggle")
    @ResponseBody
    public String toggle(@RequestParam("id") Long id) {
        categoryService.toggleEnable(id);
        return "toggled";
    }

    @PostMapping("/delete")
    @ResponseBody
    public String delete(@RequestParam("id") Long id) {
        CategoryModel cm = categoryService.getById(id);
        if (cm == null) return "notfound";

        try {
        	String uploadDir = uploadLocation + File.separator + "category";
            Files.deleteIfExists(Paths.get(uploadDir, cm.getLogo()));
        } catch (Exception ignored) {}

        categoryService.deleteCategory(id);
        return "deleted";
    }
}

