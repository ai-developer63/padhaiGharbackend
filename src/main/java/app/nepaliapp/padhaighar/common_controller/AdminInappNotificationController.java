package app.nepaliapp.padhaighar.common_controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import app.nepaliapp.padhaighar.enums.ConditionType;
import app.nepaliapp.padhaighar.enums.Priority;
import app.nepaliapp.padhaighar.enums.TargetType;
import app.nepaliapp.padhaighar.model.InAppNotification;
import app.nepaliapp.padhaighar.service.CommonService;
import app.nepaliapp.padhaighar.service.InAppNotificationService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/notifications")
@RequiredArgsConstructor
public class AdminInappNotificationController {

    private final InAppNotificationService notificationService;
    private final CommonService commonService; 

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("notification", new InAppNotification());
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("targetTypes", TargetType.values());
        model.addAttribute("conditionTypes", ConditionType.values());
        return "admin/add-notification";
    }

    // UPDATED: Added MultipartFile parameter
    @PostMapping("/save")
    public String saveNotification(
            @ModelAttribute InAppNotification notification,
            @RequestParam("imageFile") MultipartFile imageFile, // <--- Capture the file
            RedirectAttributes redirectAttributes) {
        try {
            // 1. Handle Image Upload using your CommonService
            if (!imageFile.isEmpty()) {
                // Uploads to: upload_location/notifications/filename.jpg
                String fileName = commonService.uploadFile("notifications", imageFile);
                notification.setImagePath(fileName);
            }

            // 2. Default Button Label if empty
            if (notification.getButtonLabel() == null || notification.getButtonLabel().isEmpty()) {
                notification.setButtonLabel("Open"); // Default fallback
            }

            // 3. Save
            notificationService.createNotification(notification);
            redirectAttributes.addFlashAttribute("success", "Notification broadcasted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating notification: " + e.getMessage());
        }
        return "redirect:/admin/notifications/create";
    }
    
    @GetMapping("/list")
    public String listNotifications(Model model) {
        model.addAttribute("notifications", notificationService.getAllNotifications());
        return "admin/list-notifications";
    }

    @PostMapping("/delete/{id}")
    public String deleteNotification(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            notificationService.deleteNotification(id);
            ra.addFlashAttribute("success", "Notification removed successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to delete notification: " + e.getMessage());
        }
        return "redirect:/admin/notifications/list";
    }
}
