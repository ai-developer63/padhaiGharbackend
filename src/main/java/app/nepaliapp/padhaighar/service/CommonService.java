package app.nepaliapp.padhaighar.service;

import java.io.IOException;

import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

public interface CommonService {

	public void removeSessionMessage();
	public boolean checkIsloggedin();
	public boolean sendEmail(String to, String subject,String text);
	public String formateTextForOTPSend(String userName,String otp);
	public String otpGenerator(); 
	
	public Model modelForAuth(Model model);
	String buildUrlString(String folderPath, String fileName);
	String uploadFile(String folder, MultipartFile file) throws IOException;
	boolean deleteFile(String folder, String fileName);
	String updateFile(String folder, MultipartFile newFile, String oldFile) throws IOException;
	
	
}