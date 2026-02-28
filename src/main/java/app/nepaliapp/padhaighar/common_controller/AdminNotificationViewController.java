package app.nepaliapp.padhaighar.common_controller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import app.nepaliapp.padhaighar.model.UserDevice;
import app.nepaliapp.padhaighar.repository.UserNotificationDeviceRepository;
import app.nepaliapp.padhaighar.serviceimp.CommonServiceImp;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationViewController {
    private final CommonServiceImp commonServiceImp;
    private final UserNotificationDeviceRepository deviceRepository;

    @GetMapping("/push")
    public String showPushCenter(
            Model model,
            @RequestParam(name="page",defaultValue = "0") int page,
            @RequestParam(name="size",defaultValue = "10") int size) {
        
        // Fetch only 10 devices at a time (sorted by most recent)
        Pageable pageable = PageRequest.of(page, size, Sort.by("lastUpdated").descending());
        Page<UserDevice> devicePage = deviceRepository.findAll(pageable);

        model.addAttribute("devicePage", devicePage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", devicePage.getTotalPages());
        
        commonServiceImp.modelForAuth(model, true);
        return "admin/push-notifications"; 
    }
    
    
}
