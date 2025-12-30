package app.nepaliapp.padhaighar.serviceimp;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.nepaliapp.padhaighar.model.ServerSettingModel;
import app.nepaliapp.padhaighar.repository.ServerSettingRepository;
import app.nepaliapp.padhaighar.service.ServerSettingService;

@Service
public class ServerSettingServiceImp implements ServerSettingService {

    @Autowired
    private ServerSettingRepository serverSettingRepository;

    @Override
    public Boolean getStatusById(Long id) {
        return serverSettingRepository.findById(id)
                .map(ServerSettingModel::isEnabled)
                .orElse(false);
    }

    @Override
    public Boolean createSetting(ServerSettingModel setting) {
        if (setting.getName() == null || setting.getName().isBlank()) {
            return false;
        }
        serverSettingRepository.save(setting);
        return true;
    }

    @Override
    public void changeSetting(Long id) {
        Optional<ServerSettingModel> opt = serverSettingRepository.findById(id);
        if (opt.isPresent()) {
            ServerSettingModel setting = opt.get();
            setting.setEnabled(!setting.isEnabled());
            serverSettingRepository.save(setting);
        }
    }
}
