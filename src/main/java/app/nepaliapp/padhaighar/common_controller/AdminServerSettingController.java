package app.nepaliapp.padhaighar.common_controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import app.nepaliapp.padhaighar.model.ServerSettingModel;
import app.nepaliapp.padhaighar.repository.ServerSettingRepository;
import app.nepaliapp.padhaighar.service.ServerSettingService;
import app.nepaliapp.padhaighar.serviceimp.CommonServiceImp;

@Controller
@RequestMapping("/admin/settings")
public class AdminServerSettingController {

    @Autowired
    private ServerSettingRepository repository;

    @Autowired
    private ServerSettingService service;
    
    @Autowired
    CommonServiceImp commonServiceImp;

    @GetMapping
    public String settingsPage(Model model) {
        List<ServerSettingModel> settings = repository.findAll();
        model.addAttribute("settings", settings);
        commonServiceImp.modelForAuth(model);
        return "admin/admin-settings";
    }

    @PostMapping("/create")
    public String createSetting(ServerSettingModel setting) {
        service.createSetting(setting);
        return "redirect:/admin/settings";
    }

    @PostMapping("/toggle/{id}")
    @ResponseBody
    public ResponseEntity<?> toggleSetting(@PathVariable("id") Long id) {
        service.changeSetting(id);
        boolean status = service.getStatusById(id);
        return ResponseEntity.ok(status);
    }
}

