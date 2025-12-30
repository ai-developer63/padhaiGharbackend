package app.nepaliapp.padhaighar.service;

import app.nepaliapp.padhaighar.model.ServerSettingModel;

public interface ServerSettingService {
	Boolean getStatusById(Long id);
	Boolean createSetting(ServerSettingModel serve); 
	void changeSetting(Long id);
}
